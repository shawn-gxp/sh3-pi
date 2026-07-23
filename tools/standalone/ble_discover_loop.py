#!/usr/bin/env python3
"""
Continuous BLE discovery + device behavior stats.

Tracks per MAC (while this process runs):
  - online / offline right now
  - exact current online session duration
  - total online time
  - longest online streak
  - times vanished (went offline)
  - times reappeared (came back)
  - ad count + ads/min
  - RSSI min / max / avg

  .venv/bin/python ble_discover_loop.py
  .venv/bin/python ble_discover_loop.py --watch 6C:EC:EB:45:6F:6A,E1:99:7D:27:1C:0A
  .venv/bin/python ble_discover_loop.py --watch-known   # Omron/NBP/MightySat/NT-100 style
  .venv/bin/python ble_discover_loop.py --jsonl

Keys: q quit | s sort | j/k select | f filter | p pause | r rescan | w toggle watch-only
"""

from __future__ import annotations

import argparse
import asyncio
import curses
import json
import threading
import time
from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Dict, List, Optional, Set

from bleak import BleakScanner
from bleak.backends.device import BLEDevice
from bleak.backends.scanner import AdvertisementData


def _hex(b: bytes) -> str:
    return b.hex() if b else ""


def _clip(s: str, width: int) -> str:
    if width <= 0:
        return ""
    if len(s) <= width:
        return s.ljust(width)
    if width <= 1:
        return s[:width]
    return s[: width - 1] + "…"


def _fmt_dur(seconds: float) -> str:
    """Human duration: 12s / 3m12s / 1h05m."""
    if seconds < 0:
        seconds = 0
    s = int(seconds)
    if s < 60:
        return f"{s}s"
    if s < 3600:
        return f"{s // 60}m{s % 60:02d}s"
    h = s // 3600
    m = (s % 3600) // 60
    return f"{h}h{m:02d}m"


# ---------------------------------------------------------------------------
# Per-device behavior model
# ---------------------------------------------------------------------------

@dataclass
class DeviceSnap:
    address: str
    name: str = ""
    local_name: str = ""
    address_type: str = ""
    rssi: Optional[int] = None
    tx_power: Optional[int] = None
    service_uuids: List[str] = field(default_factory=list)
    manufacturer_data: Dict[str, str] = field(default_factory=dict)
    manufacturer_ids: List[int] = field(default_factory=list)
    service_data: Dict[str, str] = field(default_factory=dict)

    # --- presence / lifecycle ---
    first_seen: float = 0.0
    last_seen: float = 0.0
    online: bool = False
    session_start: float = 0.0          # current online session start (0 if offline)
    total_online_s: float = 0.0         # closed sessions only
    longest_online_s: float = 0.0
    appear_count: int = 0               # times entered online (incl. first)
    vanish_count: int = 0               # times went offline
    reappear_count: int = 0             # appear after a vanish
    last_vanish_at: float = 0.0
    last_reappear_at: float = 0.0
    last_offline_s: float = 0.0         # duration of previous offline gap
    offline_since: float = 0.0          # when current offline started

    # --- traffic / signal ---
    adv_count: int = 0
    rssi_min: Optional[int] = None
    rssi_max: Optional[int] = None
    rssi_sum: float = 0.0
    rssi_n: int = 0

    def age_s(self, now: float) -> float:
        if self.last_seen <= 0:
            return 99999.0
        return max(0.0, now - self.last_seen)

    def session_online_s(self, now: float) -> float:
        if not self.online or self.session_start <= 0:
            return 0.0
        return max(0.0, now - self.session_start)

    def total_online_live(self, now: float) -> float:
        return self.total_online_s + self.session_online_s(now)

    def offline_s(self, now: float) -> float:
        if self.online:
            return 0.0
        if self.offline_since > 0:
            return max(0.0, now - self.offline_since)
        return self.age_s(now)

    def ads_per_min(self, now: float) -> float:
        span = max(1.0, now - self.first_seen) if self.first_seen else 1.0
        return self.adv_count * 60.0 / span

    def rssi_avg(self) -> Optional[float]:
        if self.rssi_n <= 0:
            return None
        return self.rssi_sum / self.rssi_n

    def display_name(self) -> str:
        return self.name or self.local_name or "—"

    def short_uuids(self, n: int = 3) -> str:
        if not self.service_uuids:
            return "—"
        out = []
        for u in self.service_uuids[:n]:
            u = u.lower()
            if u.startswith("0000") and "-0000-1000-8000-00805f9b34fb" in u:
                out.append(u[4:8])
            else:
                out.append(u[:8])
        if len(self.service_uuids) > n:
            out.append(f"+{len(self.service_uuids) - n}")
        return ",".join(out)

    def short_mfg(self) -> str:
        if not self.manufacturer_data:
            return "—"
        parts = []
        for i, (cid, hx) in enumerate(self.manufacturer_data.items()):
            if i >= 2:
                parts.append(f"+{len(self.manufacturer_data) - 2}")
                break
            parts.append(f"{cid}:{hx[:8]}…")
        return " ".join(parts)

    def flags(self) -> str:
        tags = []
        uu = " ".join(self.service_uuids).lower()
        name = (self.name or self.local_name or "").lower()
        if "fe4a" in uu or "0x020e" in self.manufacturer_data or "blesmart" in name:
            tags.append("OMRON")
        if "nbp" in name or "nmbp" in name:
            tags.append("NBP")
        if "nt-100" in name or "nt100" in name:
            tags.append("NT100")
        if "mightysat" in name:
            tags.append("MASIMO")
        if "1810" in uu:
            tags.append("BP")
        if "1808" in uu:
            tags.append("GLU")
        if "1809" in uu or "2a1c" in uu:
            tags.append("TEMP")
        if "180d" in uu:
            tags.append("HR")
        if "febe" in uu or "bose" in name or "jbl" in name:
            tags.append("AUDIO")
        if self.address_type:
            tags.append(self.address_type[:1].upper())
        return ",".join(tags) if tags else "—"

    def note_online(self, now: float) -> None:
        """Call when an advertisement arrives."""
        was_online = self.online
        if not was_online:
            # (re)appear
            self.appear_count += 1
            if self.vanish_count > 0:
                self.reappear_count += 1
                self.last_reappear_at = now
                if self.offline_since > 0:
                    self.last_offline_s = now - self.offline_since
            self.online = True
            self.session_start = now
            self.offline_since = 0.0
        # if already online, session continues

    def note_offline(self, now: float) -> None:
        """Call when device crosses stale threshold."""
        if not self.online:
            return
        sess = self.session_online_s(now)
        self.total_online_s += sess
        if sess > self.longest_online_s:
            self.longest_online_s = sess
        self.online = False
        self.session_start = 0.0
        self.vanish_count += 1
        self.last_vanish_at = now
        self.offline_since = now

    def note_rssi(self, rssi: int) -> None:
        self.rssi = rssi
        self.rssi_n += 1
        self.rssi_sum += rssi
        if self.rssi_min is None or rssi < self.rssi_min:
            self.rssi_min = rssi
        if self.rssi_max is None or rssi > self.rssi_max:
            self.rssi_max = rssi

    def to_dict(self, now: Optional[float] = None) -> Dict[str, Any]:
        now = now or time.time()
        return {
            "address": self.address,
            "name": self.display_name(),
            "local_name": self.local_name,
            "address_type": self.address_type,
            "flags": self.flags(),
            "online": self.online,
            "age_s": round(self.age_s(now), 2),
            "session_online_s": round(self.session_online_s(now), 2),
            "session_online": _fmt_dur(self.session_online_s(now)),
            "total_online_s": round(self.total_online_live(now), 2),
            "total_online": _fmt_dur(self.total_online_live(now)),
            "longest_online_s": round(
                max(self.longest_online_s, self.session_online_s(now)), 2
            ),
            "longest_online": _fmt_dur(
                max(self.longest_online_s, self.session_online_s(now))
            ),
            "appear_count": self.appear_count,
            "vanish_count": self.vanish_count,
            "reappear_count": self.reappear_count,
            "last_offline_s": round(self.last_offline_s, 2),
            "offline_s": round(self.offline_s(now), 2),
            "adv_count": self.adv_count,
            "ads_per_min": round(self.ads_per_min(now), 2),
            "rssi": self.rssi,
            "rssi_min": self.rssi_min,
            "rssi_max": self.rssi_max,
            "rssi_avg": round(self.rssi_avg(), 1) if self.rssi_avg() is not None else None,
            "tx_power": self.tx_power,
            "first_seen": datetime.fromtimestamp(self.first_seen).isoformat(timespec="seconds")
            if self.first_seen
            else None,
            "last_seen": datetime.fromtimestamp(self.last_seen).isoformat(timespec="seconds")
            if self.last_seen
            else None,
            "last_vanish": datetime.fromtimestamp(self.last_vanish_at).isoformat(timespec="seconds")
            if self.last_vanish_at
            else None,
            "last_reappear": datetime.fromtimestamp(self.last_reappear_at).isoformat(
                timespec="seconds"
            )
            if self.last_reappear_at
            else None,
            "service_uuids": list(self.service_uuids),
            "manufacturer_data": dict(self.manufacturer_data),
            "service_data": dict(self.service_data),
        }


def _clone_snap(d: DeviceSnap) -> DeviceSnap:
    return DeviceSnap(
        address=d.address,
        name=d.name,
        local_name=d.local_name,
        address_type=d.address_type,
        rssi=d.rssi,
        tx_power=d.tx_power,
        service_uuids=list(d.service_uuids),
        manufacturer_data=dict(d.manufacturer_data),
        manufacturer_ids=list(d.manufacturer_ids),
        service_data=dict(d.service_data),
        first_seen=d.first_seen,
        last_seen=d.last_seen,
        online=d.online,
        session_start=d.session_start,
        total_online_s=d.total_online_s,
        longest_online_s=d.longest_online_s,
        appear_count=d.appear_count,
        vanish_count=d.vanish_count,
        reappear_count=d.reappear_count,
        last_vanish_at=d.last_vanish_at,
        last_reappear_at=d.last_reappear_at,
        last_offline_s=d.last_offline_s,
        offline_since=d.offline_since,
        adv_count=d.adv_count,
        rssi_min=d.rssi_min,
        rssi_max=d.rssi_max,
        rssi_sum=d.rssi_sum,
        rssi_n=d.rssi_n,
    )


# ---------------------------------------------------------------------------
# Scanner
# ---------------------------------------------------------------------------

KNOWN_HINTS = (
    "blesmart",
    "omron",
    "nbp-1ble",
    "nbp",
    "nmbp",
    "nt-100",
    "nt100",
    "nsm-1",
    "mightysat",
    "ua-651",
)


class ContinuousScanner:
    def __init__(
        self,
        *,
        interval: float = 1.0,
        stale_s: float = 8.0,
        active: bool = True,
        name_filter: str = "",
        sort: str = "rssi",
        rescan_every: float = 0.0,
        watch_macs: Optional[Set[str]] = None,
        watch_known: bool = False,
        watch_only: bool = False,
    ) -> None:
        self.interval = max(0.2, float(interval))
        # vanish threshold: no ad for this long → count as offline
        self.stale_s = max(2.0, float(stale_s))
        self.active = active
        self.name_filter = (name_filter or "").strip().lower()
        self.sort = sort
        self.rescan_every = max(0.0, float(rescan_every))
        self.watch_macs = {m.strip().upper() for m in (watch_macs or set()) if m.strip()}
        self.watch_known = watch_known
        self.watch_only = watch_only
        self.devices: Dict[str, DeviceSnap] = {}
        self._lock = threading.Lock()
        self.started = time.time()
        self.tick = 0
        self.paused = False
        self.selected = 0
        self.error = ""
        self.status = "starting"
        self.rescan_count = 0
        self.last_adv_mono = 0.0
        self._force_rescan = asyncio.Event()
        self._scanner: Optional[BleakScanner] = None
        self._adv_total = 0

    def _is_watched(self, d: DeviceSnap) -> bool:
        if d.address.upper() in self.watch_macs:
            return True
        if not self.watch_known:
            return False
        blob = f"{d.name} {d.local_name} {d.flags()}".lower()
        return any(h in blob for h in KNOWN_HINTS)

    def on_detect(self, device: BLEDevice, adv: AdvertisementData) -> None:
        try:
            now = time.time()
            self.last_adv_mono = time.monotonic()
            self._adv_total += 1
            addr = (device.address or "").upper()
            if not addr:
                return

            name = (device.name or "").strip()
            local = (getattr(adv, "local_name", None) or "").strip()
            rssi = getattr(adv, "rssi", None)
            mfg_raw = getattr(adv, "manufacturer_data", None) or {}
            svc_data_raw = getattr(adv, "service_data", None) or {}
            uuids_raw = getattr(adv, "service_uuids", None) or []
            tx = getattr(adv, "tx_power", None)

            addr_type = ""
            plat = getattr(adv, "platform_data", None)
            if isinstance(plat, (list, tuple)) and len(plat) >= 2 and isinstance(plat[1], dict):
                at = plat[1].get("AddressType")
                if at:
                    addr_type = str(at)

            with self._lock:
                if self.name_filter:
                    blob = f"{name} {local} {addr}".lower()
                    if self.name_filter not in blob and addr not in self.devices:
                        return

                snap = self.devices.get(addr)
                if snap is None:
                    snap = DeviceSnap(address=addr, first_seen=now)
                    self.devices[addr] = snap

                if name:
                    snap.name = name
                if local:
                    snap.local_name = local
                if rssi is not None:
                    snap.note_rssi(int(rssi))
                if tx is not None:
                    snap.tx_power = int(tx)
                if addr_type:
                    snap.address_type = addr_type

                if uuids_raw:
                    have = set(snap.service_uuids)
                    for u in uuids_raw:
                        ul = str(u).lower()
                        if ul not in have:
                            snap.service_uuids.append(ul)
                            have.add(ul)
                            if len(snap.service_uuids) > 24:
                                break

                if mfg_raw:
                    for cid, payload in mfg_raw.items():
                        try:
                            cid_i = int(cid)
                        except Exception:
                            continue
                        if cid_i not in snap.manufacturer_ids:
                            snap.manufacturer_ids.append(cid_i)
                        snap.manufacturer_data[f"0x{cid_i:04x}"] = _hex(
                            bytes(payload) if payload is not None else b""
                        )

                if svc_data_raw:
                    for u, v in svc_data_raw.items():
                        snap.service_data[str(u).lower()] = _hex(
                            bytes(v) if v is not None else b""
                        )
                        if len(snap.service_data) > 16:
                            break

                snap.last_seen = now
                snap.adv_count += 1
                snap.note_online(now)
        except Exception as exc:
            self.error = f"callback: {type(exc).__name__}: {exc}"

    def refresh_online_state(self) -> None:
        """Mark devices offline when past stale threshold; accumulate online time."""
        now = time.time()
        with self._lock:
            for d in self.devices.values():
                if d.online and d.age_s(now) > self.stale_s:
                    d.note_offline(now)

    def snapshot_rows(self) -> List[DeviceSnap]:
        self.refresh_online_state()
        now = time.time()
        with self._lock:
            all_devs = [_clone_snap(d) for d in self.devices.values()]

        if self.watch_only:
            items = [d for d in all_devs if self._is_watched(d)]
        else:
            # live devices + watched ghosts (offline still shown)
            items = []
            seen = set()
            for d in all_devs:
                if d.age_s(now) <= self.stale_s or self._is_watched(d):
                    items.append(d)
                    seen.add(d.address)

        if self.name_filter:
            f = self.name_filter
            items = [
                d
                for d in items
                if f in d.address.lower()
                or f in (d.name or "").lower()
                or f in (d.local_name or "").lower()
                or f in d.flags().lower()
            ]

        if self.sort == "name":
            items.sort(key=lambda d: (d.display_name().lower(), d.address))
        elif self.sort == "seen":
            items.sort(key=lambda d: -d.last_seen)
        elif self.sort == "online":
            items.sort(
                key=lambda d: (
                    not d.online,
                    -d.total_online_live(now),
                    d.address,
                )
            )
        elif self.sort == "vanish":
            items.sort(key=lambda d: (-d.vanish_count, d.address))
        else:
            items.sort(
                key=lambda d: (
                    not d.online,
                    d.rssi is None,
                    -(d.rssi if d.rssi is not None else -999),
                    d.address,
                )
            )
        return items

    def cycle_sort(self) -> None:
        order = ["rssi", "online", "vanish", "name", "seen"]
        try:
            i = order.index(self.sort)
        except ValueError:
            i = 0
        self.sort = order[(i + 1) % len(order)]

    def request_rescan(self) -> None:
        self._force_rescan.set()

    def _make_scanner(self) -> BleakScanner:
        kwargs: Dict[str, Any] = {"detection_callback": self.on_detect}
        mode = "active" if self.active else "passive"
        try:
            return BleakScanner(**kwargs, scanning_mode=mode)
        except TypeError:
            return BleakScanner(detection_callback=self.on_detect)

    async def _safe_stop(self, sc: Optional[BleakScanner]) -> None:
        if sc is None:
            return
        try:
            await asyncio.wait_for(sc.stop(), timeout=0.8)
        except Exception:
            pass

    async def scanner_lifecycle(self) -> None:
        silent_limit = 15.0
        while True:
            self.status = "starting…"
            self.error = ""
            sc: Optional[BleakScanner] = None
            try:
                sc = self._make_scanner()
                self._scanner = sc
                await asyncio.wait_for(sc.start(), timeout=8.0)
                self.status = "scanning"
                self.rescan_count += 1
                self._force_rescan.clear()
                t0 = time.monotonic()
                while True:
                    if self._force_rescan.is_set():
                        self._force_rescan.clear()
                        self.status = "rescan (manual)…"
                        break
                    if self.rescan_every > 0 and (time.monotonic() - t0) >= self.rescan_every:
                        self.status = "rescan (timer)…"
                        break
                    if self.last_adv_mono > 0:
                        silent = time.monotonic() - self.last_adv_mono
                        if silent > silent_limit and (time.monotonic() - self.started) > 8:
                            self.status = f"silent {silent:.0f}s — rescan…"
                            break
                    elif (time.monotonic() - t0) > silent_limit:
                        self.status = "no ads — rescan…"
                        break
                    await asyncio.sleep(0.2)
            except asyncio.TimeoutError:
                self.error = "start timed out (BlueZ busy?)"
                self.status = "timeout"
            except asyncio.CancelledError:
                await self._safe_stop(sc)
                raise
            except Exception as exc:
                self.error = f"{type(exc).__name__}: {exc}"
                self.status = "error"
            finally:
                await self._safe_stop(sc)
                self._scanner = None
            await asyncio.sleep(0.15 if not self.error else 1.0)


# ---------------------------------------------------------------------------
# TUI
# ---------------------------------------------------------------------------

class LiveTable:
    def __init__(self, stdscr: Any, scanner: ContinuousScanner) -> None:
        self.stdscr = stdscr
        self.sc = scanner
        self._filter_edit: Optional[str] = None
        self._need_redraw = True

    def setup(self) -> None:
        curses.curs_set(0)
        try:
            curses.use_default_colors()
        except curses.error:
            pass
        if curses.has_colors():
            curses.init_pair(1, curses.COLOR_GREEN, -1)
            curses.init_pair(2, curses.COLOR_YELLOW, -1)
            curses.init_pair(3, curses.COLOR_RED, -1)
            curses.init_pair(4, curses.COLOR_CYAN, -1)
            curses.init_pair(5, curses.COLOR_BLACK, curses.COLOR_CYAN)
            curses.init_pair(6, curses.COLOR_WHITE, curses.COLOR_RED)  # offline watched
        self.stdscr.nodelay(True)
        self.stdscr.timeout(0)

    def _put(self, y: int, x: int, text: str, attr: int = curses.A_NORMAL) -> None:
        h, w = self.stdscr.getmaxyx()
        if y < 0 or y >= h or x >= w:
            return
        try:
            self.stdscr.addnstr(y, x, text, max(0, w - x - 1), attr)
        except curses.error:
            pass

    def _row_attr(self, d: DeviceSnap, selected: bool) -> int:
        if selected:
            return curses.color_pair(5) if curses.has_colors() else curses.A_REVERSE
        if not d.online:
            return curses.color_pair(6) if curses.has_colors() else curses.A_DIM
        if d.rssi is not None and curses.has_colors():
            if d.rssi >= -55:
                return curses.color_pair(1)
            if d.rssi >= -75:
                return curses.color_pair(2)
            return curses.color_pair(3)
        return curses.A_NORMAL

    def draw(self, rows: List[DeviceSnap]) -> None:
        sc = self.sc
        now = time.time()
        h, w = self.stdscr.getmaxyx()
        if h < 10 or w < 50:
            self.stdscr.erase()
            self._put(0, 0, "Terminal too small")
            self.stdscr.refresh()
            return

        if rows:
            sc.selected = max(0, min(sc.selected, len(rows) - 1))
        else:
            sc.selected = 0

        detail_h = min(12, max(6, h // 3))
        header_h = 4
        footer_h = 1
        table_top = header_h
        table_h = max(3, h - header_h - detail_h - footer_h - 1)
        detail_top = table_top + table_h + 1

        self.stdscr.erase()
        elapsed = now - sc.started
        ts = datetime.fromtimestamp(now).strftime("%H:%M:%S")
        silence = (
            time.monotonic() - sc.last_adv_mono if sc.last_adv_mono else 0.0
        )
        filt = sc.name_filter or "(none)"
        if self._filter_edit is not None:
            filt = self._filter_edit + "▌"
        pause = " [PAUSED]" if sc.paused else ""
        wonly = " WATCH-ONLY" if sc.watch_only else ""

        n_on = sum(1 for d in rows if d.online)
        n_off = len(rows) - n_on

        line0 = (
            f" BLE BEHAVIOR  {ts}  +{_fmt_dur(elapsed)}  tick={sc.tick}  "
            f"sort={sc.sort}{pause}{wonly}"
        )
        line1 = (
            f" rows={len(rows)} on={n_on} off={n_off}  ads={sc._adv_total}  "
            f"silent={silence:.0f}s  stale={sc.stale_s:.0f}s  filter={filt}"
        )
        line2 = (
            " ON=currently advertising | SESS=this online streak | TOT=all online time | "
            "VAN=vanish count | RE=reappear count"
        )
        line3 = " q quit  s sort  j/k select  f filter  p pause  r rescan  w watch-only"
        ah = curses.color_pair(4) | curses.A_BOLD if curses.has_colors() else curses.A_BOLD
        self._put(0, 0, _clip(line0, w - 1), ah)
        self._put(1, 0, _clip(line1, w - 1), curses.A_DIM)
        self._put(2, 0, _clip(line2, w - 1), curses.A_DIM)
        self._put(3, 0, _clip(line3, w - 1), curses.A_DIM)

        # Columns focused on behavior
        # # | ON | RSSI | SESS | TOT | VAN | RE | ADS | ADDRESS | NAME | FLAGS
        cols = {
            "num": 3,
            "on": 3,
            "rssi": 5,
            "sess": 7,
            "tot": 7,
            "van": 4,
            "re": 3,
            "ads": 5,
            "addr": 17,
        }
        fixed = sum(cols.values()) + len(cols) + 8
        rest = max(10, w - fixed - 1)
        col_name = max(10, rest * 2 // 3)
        col_flags = max(6, rest - col_name)

        hdr = (
            f"{'#':>{cols['num']}} "
            f"{'ON':<{cols['on']}} "
            f"{'RSSI':>{cols['rssi']}} "
            f"{'SESS':>{cols['sess']}} "
            f"{'TOT':>{cols['tot']}} "
            f"{'VAN':>{cols['van']}} "
            f"{'RE':>{cols['re']}} "
            f"{'ADS':>{cols['ads']}} "
            f"{'ADDRESS':<{cols['addr']}} "
            f"{'NAME':<{col_name}} "
            f"{'FLAGS':<{col_flags}}"
        )
        self._put(table_top, 0, _clip(hdr, w - 1), curses.A_REVERSE)

        max_rows = max(1, table_h - 1)
        start = 0
        if sc.selected >= start + max_rows:
            start = sc.selected - max_rows + 1
        if sc.selected < start:
            start = sc.selected

        for i in range(max_rows):
            yi = table_top + 1 + i
            idx = start + i
            if idx >= len(rows):
                self._put(yi, 0, " " * max(0, w - 1))
                continue
            d = rows[idx]
            on = "Y" if d.online else "n"
            rssi_s = f"{d.rssi}" if d.rssi is not None else "?"
            line = (
                f"{idx + 1:>{cols['num']}} "
                f"{on:<{cols['on']}} "
                f"{rssi_s:>{cols['rssi']}} "
                f"{_fmt_dur(d.session_online_s(now)):>{cols['sess']}} "
                f"{_fmt_dur(d.total_online_live(now)):>{cols['tot']}} "
                f"{d.vanish_count:>{cols['van']}} "
                f"{d.reappear_count:>{cols['re']}} "
                f"{d.adv_count:>{cols['ads']}} "
                f"{d.address:<{cols['addr']}} "
                f"{_clip(d.display_name(), col_name)} "
                f"{_clip(d.flags(), col_flags)}"
            )
            self._put(yi, 0, _clip(line, w - 1), self._row_attr(d, idx == sc.selected))

        self._put(detail_top - 1, 0, _clip("─" * (w - 1), w - 1), curses.A_DIM)
        if rows:
            d = rows[sc.selected]
            for i, text in enumerate(self._detail_lines(d, now)[:detail_h]):
                self._put(detail_top + i, 1, _clip(text, w - 2))
        else:
            self._put(detail_top, 1, "No devices yet — scanning…", curses.A_DIM)

        foot = f" {sc.error or sc.status}  |  row {sc.selected + 1}/{len(rows) or 0}"
        self._put(h - 1, 0, _clip(foot, w - 1), curses.A_DIM)
        self.stdscr.refresh()

    def _detail_lines(self, d: DeviceSnap, now: float) -> List[str]:
        avg = d.rssi_avg()
        avg_s = f"{avg:.1f}" if avg is not None else "—"
        longest = max(d.longest_online_s, d.session_online_s(now))
        lines = [
            f"▶ {d.address}  {d.display_name()}  [{d.flags()}]  type={d.address_type or '?'}",
            f"  STATE: {'ONLINE' if d.online else 'OFFLINE'}  "
            f"session={_fmt_dur(d.session_online_s(now))}  "
            f"total_online={_fmt_dur(d.total_online_live(now))}  "
            f"longest={_fmt_dur(longest)}",
            f"  LIFECYCLE: appears={d.appear_count}  vanishes={d.vanish_count}  "
            f"reappears={d.reappear_count}  "
            f"last_gap={_fmt_dur(d.last_offline_s)}  "
            f"offline_now={_fmt_dur(d.offline_s(now))}",
            f"  SIGNAL: rssi={d.rssi}  min={d.rssi_min}  max={d.rssi_max}  "
            f"avg={avg_s}  ads={d.adv_count}  ads/min={d.ads_per_min(now):.1f}  "
            f"tx={d.tx_power if d.tx_power is not None else '—'}",
            f"  TIME: first={_ts(d.first_seen)}  last={_ts(d.last_seen)}  "
            f"vanish={_ts(d.last_vanish_at)}  reappear={_ts(d.last_reappear_at)}",
        ]
        if d.service_uuids:
            lines.append("  UUIDs: " + ", ".join(d.service_uuids[:6]))
        if d.manufacturer_data:
            for k, v in list(d.manufacturer_data.items())[:2]:
                lines.append(f"  MFG {k}: {v[:40]}{'…' if len(v) > 40 else ''}")
        return lines

    def handle_key(self, ch: int) -> bool:
        sc = self.sc
        self._need_redraw = True
        if self._filter_edit is not None:
            if ch == 27:
                self._filter_edit = None
                sc.name_filter = ""
            elif ch in (10, 13):
                sc.name_filter = self._filter_edit.strip().lower()
                self._filter_edit = None
            elif ch in (curses.KEY_BACKSPACE, 127, 8):
                self._filter_edit = self._filter_edit[:-1]
            elif 32 <= ch < 127:
                self._filter_edit += chr(ch)
            return True

        if ch in (ord("q"), ord("Q")):
            return False
        if ch in (ord("s"), ord("S")):
            sc.cycle_sort()
        elif ch in (ord("p"), ord("P")):
            sc.paused = not sc.paused
        elif ch in (ord("r"), ord("R")):
            sc.request_rescan()
        elif ch in (ord("w"), ord("W")):
            sc.watch_only = not sc.watch_only
        elif ch in (ord("f"), ord("F"), ord("/")):
            self._filter_edit = sc.name_filter or ""
        elif ch in (ord("j"), curses.KEY_DOWN):
            sc.selected += 1
        elif ch in (ord("k"), curses.KEY_UP):
            sc.selected -= 1
        elif ch == curses.KEY_NPAGE:
            sc.selected += 10
        elif ch == curses.KEY_PPAGE:
            sc.selected -= 10
        return True


def _ts(epoch: float) -> str:
    if not epoch:
        return "—"
    return datetime.fromtimestamp(epoch).strftime("%H:%M:%S")


async def run_tui(scanner: ContinuousScanner) -> None:
    stdscr = curses.initscr()
    try:
        curses.noecho()
        curses.cbreak()
        stdscr.keypad(True)
        try:
            if curses.has_colors():
                curses.start_color()
        except curses.error:
            pass
        ui = LiveTable(stdscr, scanner)
        ui.setup()
        life = asyncio.create_task(scanner.scanner_lifecycle())
        last_draw = 0.0
        rows: List[DeviceSnap] = []
        try:
            while True:
                while True:
                    ch = stdscr.getch()
                    if ch == -1:
                        break
                    if not ui.handle_key(ch):
                        return
                now = time.time()
                due = (now - last_draw) >= scanner.interval
                if (due and not scanner.paused) or ui._need_redraw:
                    if due and not scanner.paused:
                        scanner.tick += 1
                        last_draw = now
                        rows = scanner.snapshot_rows()
                    elif not rows:
                        rows = scanner.snapshot_rows()
                    ui.draw(rows)
                    ui._need_redraw = False
                await asyncio.sleep(0.03)
        finally:
            life.cancel()
            try:
                await life
            except (asyncio.CancelledError, Exception):
                pass
            if scanner._scanner:
                try:
                    await scanner._scanner.stop()
                except Exception:
                    pass
    finally:
        try:
            curses.nocbreak()
            stdscr.keypad(False)
            curses.echo()
            curses.endwin()
        except Exception:
            pass


async def run_jsonl(scanner: ContinuousScanner) -> None:
    life = asyncio.create_task(scanner.scanner_lifecycle())
    try:
        while True:
            await asyncio.sleep(scanner.interval)
            scanner.tick += 1
            now = time.time()
            rows = scanner.snapshot_rows()
            print(
                json.dumps(
                    {
                        "tick": scanner.tick,
                        "ts": datetime.now().isoformat(timespec="milliseconds"),
                        "status": scanner.status,
                        "error": scanner.error,
                        "count": len(rows),
                        "online": sum(1 for d in rows if d.online),
                        "devices": [d.to_dict(now) for d in rows],
                    },
                    ensure_ascii=False,
                ),
                flush=True,
            )
    finally:
        life.cancel()
        try:
            await life
        except (asyncio.CancelledError, Exception):
            pass


def main() -> int:
    p = argparse.ArgumentParser(
        description="BLE discovery with online/vanish/reappear behavior stats"
    )
    p.add_argument("--interval", type=float, default=1.0)
    p.add_argument(
        "--stale",
        type=float,
        default=8.0,
        help="Seconds without ad → count as vanished/offline (default 8)",
    )
    p.add_argument("--passive", action="store_true")
    p.add_argument("--filter", dest="name_filter", default="")
    p.add_argument("--jsonl", action="store_true")
    p.add_argument(
        "--sort",
        choices=("rssi", "online", "vanish", "name", "seen"),
        default="rssi",
    )
    p.add_argument("--rescan-every", type=float, default=0.0)
    p.add_argument(
        "--watch",
        default="",
        help="Comma-separated MACs always shown (even offline), e.g. "
        "6C:EC:EB:45:6F:6A,E1:99:7D:27:1C:0A",
    )
    p.add_argument(
        "--watch-known",
        action="store_true",
        help="Always track Omron/NBP/NT-100/MightySat-like names",
    )
    p.add_argument(
        "--watch-only",
        action="store_true",
        help="Show only watched / known devices",
    )
    args = p.parse_args()

    watch = {m.strip().upper() for m in args.watch.split(",") if m.strip()}
    # Default known lab MACs if user listed them before — optional empty
    scanner = ContinuousScanner(
        interval=args.interval,
        stale_s=args.stale,
        active=not args.passive,
        name_filter=args.name_filter,
        sort=args.sort,
        rescan_every=args.rescan_every,
        watch_macs=watch,
        watch_known=args.watch_known or bool(watch),
        watch_only=args.watch_only,
    )

    try:
        if args.jsonl:
            asyncio.run(run_jsonl(scanner))
        else:
            asyncio.run(run_tui(scanner))
    except KeyboardInterrupt:
        pass
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

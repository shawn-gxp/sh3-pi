"""
Tier-1 hub hunt loop — concurrent multi-connection (BlueZ-safe).

Architecture:
  SCAN (exclusive, short) → match paired ADs → spawn workers (up to max_concurrent)
  Workers run brand drivers in parallel after connect is serialized.

BlueZ rules:
  - One scanner at a time; stop scan before connect
  - Connect with address known from scan; gap between connects
  - Multiple BleakClients may remain connected and notify concurrently

Caller supplies:
  get_roster() → list of {mac, brand, model, name, paired}
  run_session(target) → {ok, stored, error?}
"""

from __future__ import annotations

import asyncio
import logging
import time
from dataclasses import dataclass
from datetime import datetime
from typing import Any, Awaitable, Callable, Dict, List, Optional, Set

from .config import HubConfig, load_hub_config
from .connection_manager import ConnectionManager
from .policy import (
    TIER1_BRANDS,
    classify_brand,
    is_always_on,
    is_stream,
    is_windowed,
    priority_rank,
)

log = logging.getLogger("medical_ble.hub.daemon")

RosterFn = Callable[[], List[Dict[str, Any]]]
SessionFn = Callable[["SessionTarget"], Awaitable[Dict[str, Any]]]
StatusFn = Callable[["HubStatus"], None]


@dataclass
class SessionTarget:
    mac: str
    brand: str
    model: str = ""
    name: str = ""
    reason: str = ""  # advertising | omron_timer | omron_ad
    priority: int = 50


@dataclass
class HubStatus:
    active: bool = False
    phase: str = "idle"  # hunt | session | concurrent | paused | idle | stopped | error
    message: str = ""
    scan_round: int = 0
    last_mac: str = ""
    last_brand: str = ""
    last_result: str = ""
    last_stored: int = 0
    last_reason: str = ""
    paired_count: int = 0
    omron_next_s: Optional[float] = None
    config_omron_poll_s: float = 300.0
    concurrent_active: int = 0
    max_concurrent: int = 4
    active_sessions: Optional[List[dict]] = None
    updated_at: str = ""
    error: str = ""

    def as_dict(self) -> Dict[str, Any]:
        return {
            "active": self.active,
            "phase": self.phase,
            "status": self.phase,
            "message": self.message,
            "scan_round": self.scan_round,
            "last_mac": self.last_mac,
            "last_brand": self.last_brand,
            "last_result": self.last_result,
            "last_stored": self.last_stored,
            "last_reason": self.last_reason,
            "paired_count": self.paired_count,
            "omron_next_s": self.omron_next_s,
            "config_omron_poll_s": self.config_omron_poll_s,
            "concurrent_active": self.concurrent_active,
            "max_concurrent": self.max_concurrent,
            "active_sessions": list(self.active_sessions or []),
            "updated_at": self.updated_at,
            "error": self.error,
        }


def _now_str() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


class HubDaemon:
    """
    Continuous hunt + concurrent session workers (up to max_concurrent).
    """

    def __init__(
        self,
        *,
        get_roster: RosterFn,
        run_session: SessionFn,
        config: Optional[HubConfig] = None,
        on_status: Optional[StatusFn] = None,
        reload_config_each_round: bool = True,
        radio_lock: Optional[asyncio.Lock] = None,
    ):
        self.get_roster = get_roster
        self.run_session = run_session
        self.cfg = config or load_hub_config()
        self.on_status = on_status
        self.reload_config_each_round = reload_config_each_round
        # Shared with web Scan/Pair — only held during scan / manual ops
        self.radio_lock = radio_lock
        self._stop = asyncio.Event()
        self._manual_pause = asyncio.Event()
        self._last_attempt: Dict[str, float] = {}
        self._last_success: Dict[str, float] = {}
        self.status = HubStatus()
        self._last_health_log = 0.0
        self._workers: Dict[str, asyncio.Task] = {}
        # After MightySat releases radio: hunt other brands first (duty-cycle)
        self._prefer_others_until: float = 0.0
        self.conn_mgr = ConnectionManager(
            max_concurrent=int(getattr(self.cfg, "max_concurrent", 4) or 4),
            connect_gap_s=float(getattr(self.cfg, "connect_gap_s", 0.35) or 0.35),
        )

    def _prefer_others_active(self, now: Optional[float] = None) -> bool:
        t = time.monotonic() if now is None else now
        return t < float(self._prefer_others_until or 0.0)

    def _mark_prefer_others(self, *, had_valid: bool = True) -> None:
        """
        After a Mighty session: free radio and scan other devices for
        mightysat_others_window_s before re-attaching Mighty.
        If values were all '-', keep preferring others a bit longer.
        """
        win = float(getattr(self.cfg, "mightysat_others_window_s", 5.0) or 5.0)
        if not had_valid:
            # No good SpO2 — keep hunting others / re-check later
            win = max(win, 5.0)
        self._prefer_others_until = time.monotonic() + win
        log.info(
            "[HUB] duty-cycle: prefer other devices for %.1fs (had_valid_spo2=%s)",
            win,
            had_valid,
        )

    def request_stop(self) -> None:
        self._stop.set()
        for t in list(self._workers.values()):
            t.cancel()

    def request_manual_pause(self, *, cancel_workers: bool = True) -> None:
        """
        Yield radio to UI Scan/Pair.

        cancel_workers=True (default): cancel in-flight hub sessions so the
        radio frees within seconds instead of waiting out Omron/Mighty dumps.
        """
        self._manual_pause.set()
        n = 0
        if cancel_workers:
            for mac, t in list(self._workers.items()):
                if not t.done():
                    t.cancel()
                    n += 1
            if n:
                log.info(
                    "[HUB] manual pause — cancelled %d worker(s) for Scan/Pair",
                    n,
                )
        log.info("[HUB] manual pause requested (Scan/Pair)")

    def clear_manual_pause(self) -> None:
        self._manual_pause.clear()
        log.info("[HUB] manual pause cleared — resume hunt")

    @property
    def is_manually_paused(self) -> bool:
        return self._manual_pause.is_set()

    async def _wait_if_manual_pause(self) -> None:
        while self._manual_pause.is_set() and not self._stop.is_set():
            self._publish(
                phase="paused",
                message=(
                    f"Hub paused for Scan/Pair "
                    f"(sessions still active: {self.conn_mgr.active_count})"
                ),
                concurrent_active=self.conn_mgr.active_count,
                active_sessions=self.conn_mgr.snapshot(),
            )
            await asyncio.sleep(0.25)

    def _publish(self, **kwargs: Any) -> None:
        for k, v in kwargs.items():
            if hasattr(self.status, k):
                setattr(self.status, k, v)
        self.status.updated_at = _now_str()
        self.status.config_omron_poll_s = float(self.cfg.omron_poll_interval_s)
        self.status.concurrent_active = self.conn_mgr.active_count
        self.status.max_concurrent = self.conn_mgr.max_concurrent
        self.status.active_sessions = self.conn_mgr.snapshot()
        if self.on_status:
            try:
                self.on_status(self.status)
            except Exception as exc:  # noqa: BLE001
                log.debug("on_status: %s", exc)

    def _filter_roster(self, devices: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        out: List[Dict[str, Any]] = []
        for d in devices:
            mac = (d.get("mac") or "").strip().upper()
            brand = (d.get("brand") or "").strip().lower()
            if not mac or not brand:
                continue
            if d.get("paired") is False:
                continue
            profile_id = (d.get("profile_id") or d.get("brand") or "").strip().lower()
            if self.cfg.tier1_brands_only and profile_id not in TIER1_BRANDS and brand not in TIER1_BRANDS:
                continue
            out.append(
                {
                    **d,
                    "mac": mac,
                    "brand": brand,
                    "model": d.get("model") or "",
                    "name": (d.get("name") or "").strip(),
                }
            )
        return out

    def _in_cooldown(self, mac: str, brand: str, now: float) -> bool:
        last_try = self._last_attempt.get(mac, 0.0)
        if last_try <= 0:
            return False
        last_ok = self._last_success.get(mac, 0.0)
        if last_ok >= last_try - 0.01:
            cool = self.cfg.cooldown_ok_s(brand)
        else:
            cool = self.cfg.cooldown_fail_s(brand)
        return (now - last_try) < cool

    def _omron_due(self, mac: str, now: float) -> bool:
        last_try = self._last_attempt.get(mac, 0.0)
        last_ok = self._last_success.get(mac, 0.0)
        if last_try <= 0:
            return True
        if last_ok >= last_try - 0.01:
            return (now - last_ok) >= float(self.cfg.omron_poll_interval_s)
        return (now - last_try) >= float(self.cfg.omron_fail_cooldown_s)

    def _omron_next_s(self, roster: List[Dict[str, Any]], now: float) -> Optional[float]:
        waits: List[float] = []
        for d in roster:
            if d["brand"] != "omron":
                continue
            mac = d["mac"]
            last_ok = self._last_success.get(mac, 0.0)
            last_try = self._last_attempt.get(mac, 0.0)
            if last_try <= 0:
                waits.append(0.0)
                continue
            if last_ok >= last_try - 0.01:
                waits.append(
                    max(0.0, float(self.cfg.omron_poll_interval_s) - (now - last_ok))
                )
            else:
                waits.append(
                    max(0.0, float(self.cfg.omron_fail_cooldown_s) - (now - last_try))
                )
        if not waits:
            return None
        return min(waits)

    def _reap_workers(self) -> None:
        done = [m for m, t in self._workers.items() if t.done()]
        for m in done:
            t = self._workers.pop(m, None)
            if t:
                try:
                    exc = t.exception()
                    if exc:
                        log.debug("[HUB] worker %s ended with %s", m, exc)
                except (asyncio.CancelledError, asyncio.InvalidStateError):
                    pass

    async def _scan_once(self) -> List[Any]:
        from medical_ble_toolkit.ble_client import scan_devices

        # BlueZ: discovery + connect at the same time → org.bluez.Error.InProgress.
        # Pause hunt scans for a short quiet window after each worker starts.
        quiet_s = float(getattr(self.cfg, "scan_quiet_after_spawn_s", 12.0) or 12.0)
        if self.conn_mgr._connect_mutex.locked():
            log.debug("[HUB] skip scan — connect gate held")
            return []
        if self.conn_mgr.any_younger_than(quiet_s):
            log.debug(
                "[HUB] skip scan — worker still in connect/setup window (%.0fs)",
                quiet_s,
            )
            return []

        async def _do() -> List[Any]:
            try:
                return list(
                    await scan_devices(
                        profile=None,
                        timeout=float(self.cfg.scan_chunk_s),
                        retries=2,
                        quiet_busy=True,
                    )
                    or []
                )
            except Exception as exc:  # noqa: BLE001
                log.warning("[HUB] scan failed: %s", exc)
                return []

        if self.radio_lock is not None:
            # Non-blocking try: if UI holds lock, skip this hunt round
            if self.radio_lock.locked():
                return []
            async with self.radio_lock:
                return await _do()
        return await _do()

    def _match_ads(
        self,
        roster: List[Dict[str, Any]],
        scanned: List[Any],
        now: float,
    ) -> List[SessionTarget]:
        """
        MAC-strict matching for multi-device hubs.

        Only the roster MAC starts a session. Exact-name hits on a *different*
        MAC are logged and ignored (never rebind). Brand-hint "steal any AD"
        is intentionally removed — two NBPs must not cross-connect.
        """
        by_mac = {
            (getattr(d, "address", "") or "").strip().upper(): d for d in scanned
        }
        hits: List[SessionTarget] = []
        for d in roster:
            mac = (d.get("mac") or "").strip().upper()
            brand = d["brand"]
            if not mac:
                continue
            if self.conn_mgr.is_busy(mac):
                continue
            if self._in_cooldown(mac, brand, now):
                continue
            adv = by_mac.get(mac)
            name = (d.get("name") or "").strip()
            if adv is None and name:
                # Diagnostic only: same advertised name on wrong MAC
                for smac, sd in by_mac.items():
                    sname = (getattr(sd, "name", None) or "").strip()
                    if sname and sname == name and smac != mac:
                        log.warning(
                            "[HUB] MAC-strict: name %r seen at %s but roster "
                            "MAC is %s — not binding",
                            name,
                            smac,
                            mac,
                        )
                        break
            if adv is None:
                continue
            # Duty-cycle: after Mighty releases, skip Masimo briefly so others
            # (NBP / NT / Omron) can transfer. Also hold off while any non-stream
            # session is already dumping. If only Mighty is paired, still allow.
            if brand == "masimo":
                others_busy = any(
                    (s.get("brand") or "").lower() != "masimo"
                    for s in (self.conn_mgr.snapshot() or [])
                )
                if others_busy:
                    continue
                if self._prefer_others_active(now):
                    other_paired = any(x["brand"] != "masimo" for x in roster)
                    if other_paired:
                        continue
            if is_always_on(brand):
                if not self._omron_due(mac, now):
                    continue
                reason = "omron_ad"
            else:
                reason = "advertising"
            pr = priority_rank(brand)
            # While preferring others, demote stream brands so windowed go first
            if self._prefer_others_active(now) and brand == "masimo":
                pr = 90
            hits.append(
                SessionTarget(
                    mac=mac,
                    brand=brand,
                    model=d.get("model") or "",
                    name=getattr(adv, "name", None) or name,
                    reason=reason,
                    priority=pr,
                )
            )
        hits.sort(key=lambda t: (t.priority, t.mac))
        return hits

    def _omron_timer_targets(
        self, roster: List[Dict[str, Any]], now: float
    ) -> List[SessionTarget]:
        out: List[SessionTarget] = []
        for d in roster:
            if d["brand"] != "omron":
                continue
            mac = d["mac"]
            if self.conn_mgr.is_busy(mac):
                continue
            if not self._omron_due(mac, now):
                continue
            out.append(
                SessionTarget(
                    mac=mac,
                    brand="omron",
                    model=d.get("model") or "HEM-7143T1",
                    name=d.get("name") or "",
                    reason="omron_timer",
                    priority=priority_rank("omron"),
                )
            )
        return out

    async def _worker(self, target: SessionTarget) -> None:
        mac = target.mac
        brand = target.brand
        self._last_attempt[mac] = time.monotonic()
        self._publish(
            phase="concurrent" if self.conn_mgr.active_count > 1 else "session",
            last_mac=mac,
            last_brand=brand,
            last_reason=target.reason,
            message=(
                f"Session {brand} {mac} ({target.reason}) "
                f"[{self.conn_mgr.active_count}/{self.conn_mgr.max_concurrent}]"
            ),
        )
        log.info(
            "[HUB] WORKER start brand=%s mac=%s reason=%s class=%s concurrent=%d/%d",
            brand,
            mac,
            target.reason,
            classify_brand(brand).value,
            self.conn_mgr.active_count,
            self.conn_mgr.max_concurrent,
        )
        try:
            # Optional connect gate: serialize establishment if session wants it
            # (run_session / job_sync perform the actual connect)
            result = await self.run_session(target)
            ok = bool(result.get("ok", True))
            stored = int(result.get("stored") or 0)
            if brand == "masimo" and stored <= 0:
                ok = False
            # Mighty duty-cycle: after stream ends, hunt NBP/NT/Omron first
            if brand == "masimo" or result.get("prefer_others"):
                had_valid = bool(result.get("had_valid_spo2")) or stored > 0
                self._mark_prefer_others(had_valid=had_valid)
            if ok:
                self._last_success[mac] = time.monotonic()
                end_r = result.get("listen_end_reason") or ""
                self._publish(
                    last_result="ok",
                    last_stored=stored,
                    message=(
                        f"OK {brand} {mac}: stored {stored} "
                        f"({target.reason}"
                        f"{', ' + end_r if end_r else ''}) "
                        f"[{self.conn_mgr.active_count}/{self.conn_mgr.max_concurrent}]"
                    ),
                )
                log.info(
                    "[HUB] WORKER ok brand=%s mac=%s stored=%d end=%s",
                    brand,
                    mac,
                    stored,
                    end_r or "-",
                )
                if self.cfg.print_readings and result.get("readings"):
                    for row in (result.get("readings") or [])[:12]:
                        log.info("[HUB][READING] %s", row)
            else:
                err = result.get("error") or "session failed"
                self._publish(
                    last_result=f"fail: {err}",
                    last_stored=stored,
                    message=f"Fail {brand} {mac}: {err}",
                )
                log.warning(
                    "[HUB] WORKER fail brand=%s mac=%s stored=%s: %s",
                    brand,
                    mac,
                    stored,
                    err,
                )
        except asyncio.CancelledError:
            log.info("[HUB] WORKER cancelled mac=%s", mac)
            raise
        except Exception as exc:  # noqa: BLE001
            self._publish(
                last_result=f"fail: {exc}",
                last_stored=0,
                message=f"Fail {brand} {mac}: {exc}",
                error=str(exc),
            )
            log.warning(
                "[HUB] WORKER exception brand=%s mac=%s: %s",
                brand,
                mac,
                exc,
            )
        finally:
            await self.conn_mgr.release(mac)
            await asyncio.sleep(float(self.cfg.post_disconnect_settle_s))
            self._publish(error="")

    async def _spawn(self, target: SessionTarget) -> bool:
        """Acquire slot and start background worker. Returns True if spawned."""
        concurrent = bool(getattr(self.cfg, "concurrent_enabled", True))
        max_c = int(getattr(self.cfg, "max_concurrent", 4) or 4)
        self.conn_mgr.max_concurrent = max_c if concurrent else 1
        self.conn_mgr.connect_gap_s = float(
            getattr(self.cfg, "connect_gap_s", 0.35) or 0.35
        )

        if not await self.conn_mgr.try_acquire(
            target.mac, target.brand, target.reason
        ):
            return False

        task = asyncio.create_task(
            self._worker(target),
            name=f"hub-worker-{target.brand}-{target.mac}",
        )
        self._workers[target.mac] = task
        return True

    async def run(self, *, duration_s: float = 86400.0 * 7) -> None:
        self._stop.clear()
        deadline = time.monotonic() + float(duration_s)
        concurrent = bool(getattr(self.cfg, "concurrent_enabled", True))
        max_c = int(getattr(self.cfg, "max_concurrent", 4) or 4)
        self.conn_mgr.max_concurrent = max_c if concurrent else 1

        self.status = HubStatus(active=True, phase="hunt", max_concurrent=max_c)
        self._publish(
            message=(
                f"Hub ON — concurrent multi-connect "
                f"(max {self.conn_mgr.max_concurrent} links)"
            ),
            active=True,
            phase="hunt",
        )
        log.info(
            "[HUB] start concurrent=%s max_links=%d omron_poll=%.0fs scan=%.1fs "
            "connect_gap=%.2fs",
            concurrent,
            self.conn_mgr.max_concurrent,
            self.cfg.omron_poll_interval_s,
            self.cfg.scan_chunk_s,
            self.conn_mgr.connect_gap_s,
        )

        try:
            while not self._stop.is_set() and time.monotonic() < deadline:
                await self._wait_if_manual_pause()
                if self._stop.is_set():
                    break

                if self.reload_config_each_round:
                    self.cfg = load_hub_config()
                    concurrent = bool(getattr(self.cfg, "concurrent_enabled", True))
                    max_c = int(getattr(self.cfg, "max_concurrent", 4) or 4)
                    self.conn_mgr.max_concurrent = max_c if concurrent else 1
                    self.conn_mgr.connect_gap_s = float(
                        getattr(self.cfg, "connect_gap_s", 0.35) or 0.35
                    )

                self._reap_workers()
                roster = self._filter_roster(self.get_roster() or [])
                now = time.monotonic()
                omron_next = self._omron_next_s(roster, now)
                n_active = self.conn_mgr.active_count
                self._publish(
                    paired_count=len(roster),
                    omron_next_s=omron_next,
                    phase=(
                        "concurrent"
                        if n_active > 1
                        else ("session" if n_active == 1 else "hunt")
                    ),
                    concurrent_active=n_active,
                    max_concurrent=self.conn_mgr.max_concurrent,
                    active_sessions=self.conn_mgr.snapshot(),
                )

                if not roster:
                    self._publish(
                        message="Hub idle — Pair devices on this hub only (no phone)",
                        phase="idle",
                    )
                    await asyncio.sleep(5.0)
                    continue

                # --- HUNT (skip if pool full — still let workers run) ---
                if not self.conn_mgr.has_slot():
                    self._publish(
                        message=(
                            f"Slots full {n_active}/{self.conn_mgr.max_concurrent} "
                            f"{self.conn_mgr.snapshot()}"
                        ),
                    )
                    await asyncio.sleep(0.5)
                    continue

                self.status.scan_round += 1
                rnd = self.status.scan_round
                prefer = self._prefer_others_active(now)
                prefer_left = max(0.0, self._prefer_others_until - now) if prefer else 0.0
                self._publish(
                    message=(
                        f"Hunt #{rnd}: scan + spawn "
                        f"({n_active}/{self.conn_mgr.max_concurrent} active, "
                        f"{len(roster)} paired)"
                        + (
                            f" [prefer others {prefer_left:.1f}s]"
                            if prefer
                            else ""
                        )
                    ),
                    scan_round=rnd,
                )
                if now - self._last_health_log >= float(self.cfg.health_log_every_s):
                    self._last_health_log = now
                    log.info(
                        "[HUB] health paired=%d active=%d/%d round=%d omron_next=%s "
                        "sessions=%s",
                        len(roster),
                        n_active,
                        self.conn_mgr.max_concurrent,
                        rnd,
                        f"{omron_next:.0f}s" if omron_next is not None else "n/a",
                        self.conn_mgr.snapshot(),
                    )

                scanned = await self._scan_once()
                if self._stop.is_set():
                    break

                now = time.monotonic()
                targets = self._match_ads(roster, scanned, now)
                # Omron timer even if not advertising (when slot free)
                for ot in self._omron_timer_targets(roster, now):
                    if not any(t.mac == ot.mac for t in targets):
                        targets.append(ot)
                targets.sort(key=lambda t: (t.priority, t.mac))

                spawned = 0
                for t in targets:
                    if self._stop.is_set() or self._manual_pause.is_set():
                        break
                    if not self.conn_mgr.has_slot():
                        break
                    if self.conn_mgr.is_busy(t.mac):
                        continue
                    if await self._spawn(t):
                        spawned += 1
                        # Small yield so connect serialization can proceed
                        await asyncio.sleep(0.05)

                if spawned == 0 and n_active == 0:
                    self._publish(
                        message=(
                            f"Hunt #{rnd}: no device ready "
                            + (
                                f"(omron next {omron_next:.0f}s)"
                                if omron_next is not None
                                else ""
                            )
                        ),
                    )
                    await asyncio.sleep(float(self.cfg.idle_sleep_s))
                elif spawned > 0:
                    # Give BlueZ time to finish connect before next discovery
                    settle = float(
                        getattr(self.cfg, "post_spawn_settle_s", 2.0) or 2.0
                    )
                    await asyncio.sleep(max(settle, float(self.cfg.idle_sleep_s)))
                else:
                    # Workers already running — short idle then re-hunt
                    await asyncio.sleep(float(self.cfg.idle_sleep_s))

            # Shutdown: wait for workers
            self._publish(message="Hub stopping — waiting for workers…")
            if self._workers:
                await asyncio.gather(*self._workers.values(), return_exceptions=True)
            self._workers.clear()
            self._publish(
                active=False,
                phase="stopped",
                message="Hub stopped",
            )
        except asyncio.CancelledError:
            for t in list(self._workers.values()):
                t.cancel()
            if self._workers:
                await asyncio.gather(*self._workers.values(), return_exceptions=True)
            self._workers.clear()
            self._publish(active=False, phase="stopped", message="Hub cancelled")
            raise
        except Exception as exc:  # noqa: BLE001
            log.exception("[HUB] fatal")
            self._publish(
                active=False, phase="error", message=str(exc), error=str(exc)
            )
            raise
        finally:
            self.status.active = False
            self._publish(active=False)

#!/usr/bin/env python3
"""
Session monitor: DB + hub log timing until each paired device has ≥1 NEW reading.

Tracks per MAC:
  - first WORKER start / connect attempt
  - connect/link ready
  - first clinical row inserted (created_at)
  - session ok/fail + stored count

Usage:
  PYTHONPATH=. .venv/bin/python tools_monitor_session.py \\
    --db medical_ble_web/data/poc.db --log /tmp/hub_session.log
"""

from __future__ import annotations

import argparse
import re
import sqlite3
import sys
import time
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional


TS_RE = re.compile(
    r"^\[(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(?:\.\d+)?)\]"
)
WORKER_START = re.compile(
    r"\[HUB\] WORKER start brand=(\S+) mac=([0-9A-Fa-f:]+) reason=(\S+)"
)
WORKER_OK = re.compile(
    r"\[HUB\] WORKER ok brand=(\S+) mac=([0-9A-Fa-f:]+) stored=(\d+)"
)
WORKER_FAIL = re.compile(
    r"\[HUB\] WORKER (?:fail|exception) brand=(\S+) mac=([0-9A-Fa-f:]+)"
)
OMRON_READ_START = re.compile(r"\[OMRON\] READ start model=\S+ address=([0-9A-Fa-f:]+)")
OMRON_READ_DONE = re.compile(r"\[OMRON\] READ done total=(\d+)")
CONNECT_ATTEMPT = re.compile(r"\[CONNECT\] attempt (\d+).*")
CONNECT_MAC = re.compile(r"Connecting to ([0-9A-Fa-f:]+)")
BLE_CONNECTED = re.compile(r"BLE connected|Connected: True")
LINK_READY = re.compile(r"Link ready for protocol")
TOKEN_UNLOCK = re.compile(r"Token unlock")
POST_MEASURE = re.compile(r"\[POST-MEASURE\].*profile=(\S+)")
NT_TEMP = re.compile(r"NT-100B.*temp|object_temperature|temperature")
STORED_HINT = re.compile(r"stored=(\d+)")
HUB_TRANSFER = re.compile(
    r"\[HUB\] transfer brand=(\S+) mac=([0-9A-Fa-f:]+) model=(\S+) reason=(\S+)"
)


def parse_ts(line: str) -> Optional[float]:
    m = TS_RE.match(line)
    if not m:
        return None
    s = m.group(1)
    for fmt in ("%Y-%m-%d %H:%M:%S.%f", "%Y-%m-%d %H:%M:%S"):
        try:
            return datetime.strptime(s, fmt).timestamp()
        except ValueError:
            continue
    return None


def now_str() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]


@dataclass
class DeviceTrack:
    brand: str
    mac: str
    model: str
    baseline_max_id: int
    baseline_count: int
    session_starts: List[float] = field(default_factory=list)
    connect_starts: List[float] = field(default_factory=list)
    connected_at: List[float] = field(default_factory=list)
    first_data_log_at: Optional[float] = None
    session_ends: List[dict] = field(default_factory=list)
    new_reading_ids: List[int] = field(default_factory=list)
    first_new_created_at: Optional[str] = None
    first_new_measured_at: Optional[str] = None
    active_session_t0: Optional[float] = None

    def has_new_reading(self) -> bool:
        return len(self.new_reading_ids) > 0


def load_devices(db: Path) -> Dict[str, DeviceTrack]:
    conn = sqlite3.connect(str(db))
    conn.row_factory = sqlite3.Row
    out: Dict[str, DeviceTrack] = {}
    for r in conn.execute(
        "SELECT id, brand, model, mac FROM devices WHERE paired=1"
    ):
        mac = (r["mac"] or "").upper()
        max_id = conn.execute(
            "SELECT COALESCE(MAX(id),0) FROM readings WHERE device_id=?",
            (r["id"],),
        ).fetchone()[0]
        n = conn.execute(
            "SELECT COUNT(*) FROM readings WHERE device_id=?",
            (r["id"],),
        ).fetchone()[0]
        out[mac] = DeviceTrack(
            brand=r["brand"],
            mac=mac,
            model=r["model"] or "",
            baseline_max_id=int(max_id or 0),
            baseline_count=int(n or 0),
        )
    conn.close()
    return out


def poll_db(db: Path, tracks: Dict[str, DeviceTrack]) -> None:
    conn = sqlite3.connect(str(db))
    conn.row_factory = sqlite3.Row
    for mac, t in tracks.items():
        row = conn.execute(
            "SELECT id FROM devices WHERE upper(mac)=?", (mac,)
        ).fetchone()
        if not row:
            continue
        did = row["id"]
        rows = conn.execute(
            """
            SELECT id, reading_type, measured_at, created_at,
                   systolic, diastolic, pulse_rate, spo2, temperature
            FROM readings
            WHERE device_id=? AND id > ?
            ORDER BY id ASC
            """,
            (did, t.baseline_max_id),
        ).fetchall()
        for r in rows:
            rid = int(r["id"])
            if rid not in t.new_reading_ids:
                t.new_reading_ids.append(rid)
                if t.first_new_created_at is None:
                    t.first_new_created_at = r["created_at"]
                    t.first_new_measured_at = r["measured_at"]
                    # approximate wall time of insert
                    if t.first_data_log_at is None:
                        try:
                            for fmt in (
                                "%Y-%m-%d %H:%M:%S.%f",
                                "%Y-%m-%d %H:%M:%S",
                            ):
                                try:
                                    t.first_data_log_at = datetime.strptime(
                                        r["created_at"], fmt
                                    ).timestamp()
                                    break
                                except ValueError:
                                    continue
                        except Exception:
                            t.first_data_log_at = time.time()
    conn.close()


def process_log_line(line: str, tracks: Dict[str, DeviceTrack], ctx: dict) -> None:
    ts = parse_ts(line)
    line_u = line

    m = HUB_TRANSFER.search(line_u)
    if m:
        mac = m.group(2).upper()
        if mac in tracks:
            t = tracks[mac]
            if ts:
                t.session_starts.append(ts)
                t.active_session_t0 = ts
                t.connect_starts.append(ts)
            print(
                f"  >> SESSION START {now_str()} brand={t.brand} mac={mac} "
                f"reason={m.group(4)} model={m.group(3)}",
                flush=True,
            )
        return

    m = WORKER_START.search(line_u)
    if m:
        mac = m.group(2).upper()
        if mac in tracks:
            t = tracks[mac]
            if ts and (not t.session_starts or ts - t.session_starts[-1] > 0.5):
                t.session_starts.append(ts)
                t.active_session_t0 = ts
            print(
                f"  >> WORKER {now_str()} brand={m.group(1)} mac={mac} "
                f"reason={m.group(3)}",
                flush=True,
            )
        return

    m = CONNECT_MAC.search(line_u)
    if m:
        mac = m.group(1).upper()
        ctx["last_mac"] = mac
        if mac in tracks and ts:
            tracks[mac].connect_starts.append(ts)
            if tracks[mac].active_session_t0 is None:
                tracks[mac].active_session_t0 = ts
        return

    m = OMRON_READ_START.search(line_u)
    if m:
        mac = m.group(1).upper()
        ctx["last_mac"] = mac
        if mac in tracks and ts:
            tracks[mac].connect_starts.append(ts)
            tracks[mac].active_session_t0 = tracks[mac].active_session_t0 or ts
        return

    if BLE_CONNECTED.search(line_u) or LINK_READY.search(line_u):
        mac = ctx.get("last_mac")
        if mac and mac in tracks and ts:
            tracks[mac].connected_at.append(ts)
            t0 = tracks[mac].active_session_t0 or (
                tracks[mac].connect_starts[-1] if tracks[mac].connect_starts else None
            )
            dt = (ts - t0) if t0 else None
            print(
                f"  >> LINK UP {now_str()} mac={mac} "
                f"connect_to_link_s={dt:.2f}" if dt is not None else
                f"  >> LINK UP {now_str()} mac={mac}",
                flush=True,
            )
        return

    if TOKEN_UNLOCK.search(line_u):
        mac = ctx.get("last_mac")
        print(f"  >> UNLOCK {now_str()} mac={mac}", flush=True)
        return

    m = WORKER_OK.search(line_u)
    if m:
        mac = m.group(2).upper()
        stored = int(m.group(3))
        if mac in tracks:
            t = tracks[mac]
            t0 = t.active_session_t0
            dur = (ts - t0) if (ts and t0) else None
            t.session_ends.append(
                {"ok": True, "stored": stored, "ts": ts, "dur_s": dur}
            )
            t.active_session_t0 = None
            print(
                f"  >> SESSION OK {now_str()} brand={t.brand} mac={mac} "
                f"stored={stored} session_s={dur:.2f}" if dur is not None else
                f"  >> SESSION OK {now_str()} brand={t.brand} mac={mac} stored={stored}",
                flush=True,
            )
        return

    m = WORKER_FAIL.search(line_u)
    if m:
        mac = m.group(2).upper()
        if mac in tracks:
            t = tracks[mac]
            t0 = t.active_session_t0
            dur = (ts - t0) if (ts and t0) else None
            t.session_ends.append(
                {"ok": False, "stored": 0, "ts": ts, "dur_s": dur}
            )
            t.active_session_t0 = None
            print(
                f"  >> SESSION FAIL {now_str()} brand={t.brand} mac={mac} "
                f"session_s={dur:.2f}" if dur is not None else
                f"  >> SESSION FAIL {now_str()} brand={t.brand} mac={mac}",
                flush=True,
            )
        return


def summary(tracks: Dict[str, DeviceTrack]) -> None:
    print("\n" + "=" * 72)
    print(f"SUMMARY @ {now_str()}")
    print("=" * 72)
    for mac, t in sorted(tracks.items(), key=lambda x: x[1].brand):
        print(f"\n[{t.brand}] {t.model}  mac={mac}")
        print(f"  baseline readings: {t.baseline_count} (max_id={t.baseline_max_id})")
        print(f"  NEW readings this session: {len(t.new_reading_ids)}")
        if t.new_reading_ids:
            print(f"  first new id={t.new_reading_ids[0]}")
            print(f"  first measured_at={t.first_new_measured_at}")
            print(f"  first created_at (DB save)={t.first_new_created_at}")
        if t.session_starts:
            print(f"  session starts: {len(t.session_starts)}")
        if t.connected_at and t.session_starts:
            # first connect latency
            t0 = t.session_starts[0]
            t1 = t.connected_at[0]
            print(f"  time session_start → first link: {t1 - t0:.2f}s")
        if t.session_starts and t.first_data_log_at:
            print(
                f"  time session_start → first DB row: "
                f"{t.first_data_log_at - t.session_starts[0]:.2f}s"
            )
        if t.connected_at and t.first_data_log_at:
            # use last connect before first data if possible
            t_conn = t.connected_at[0]
            print(
                f"  time link_up → first DB row: "
                f"{t.first_data_log_at - t_conn:.2f}s"
            )
        for i, se in enumerate(t.session_ends[-3:], 1):
            print(
                f"  end#{i}: ok={se.get('ok')} stored={se.get('stored')} "
                f"duration_s={se.get('dur_s')}"
            )
        status = "DONE ✓" if t.has_new_reading() else "WAITING…"
        print(f"  status: {status}")
    done = sum(1 for t in tracks.values() if t.has_new_reading())
    print(f"\nProgress: {done}/{len(tracks)} devices have ≥1 new reading")
    print("=" * 72 + "\n", flush=True)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--db", type=Path, required=True)
    ap.add_argument("--log", type=Path, required=True)
    ap.add_argument("--poll", type=float, default=1.0)
    ap.add_argument("--max-hours", type=float, default=4.0)
    args = ap.parse_args()

    tracks = load_devices(args.db)
    if not tracks:
        print("No paired devices in DB", file=sys.stderr)
        return 1

    print(f"[{now_str()}] Monitor started", flush=True)
    print(f"  db={args.db}")
    print(f"  log={args.log}")
    print("  targets:")
    for t in tracks.values():
        print(
            f"    - {t.brand:14} {t.mac}  baseline_max_id={t.baseline_max_id}"
        )
    print(
        "\nWaiting for NEW clinical rows (id > baseline). "
        "Measure NBP/NT; finger in MightySat; Omron can dump when advertising.\n",
        flush=True,
    )

    log_path = args.log
    # wait for log file
    t_end = time.time() + args.max_hours * 3600
    pos = 0
    ctx: dict = {}
    last_summary = 0.0

    # if log already exists, start from end (only new lines)
    if log_path.is_file():
        pos = log_path.stat().st_size

    while time.time() < t_end:
        if log_path.is_file():
            with log_path.open("r", encoding="utf-8", errors="replace") as f:
                f.seek(pos)
                chunk = f.read()
                pos = f.tell()
            for line in chunk.splitlines():
                if line.strip():
                    process_log_line(line, tracks, ctx)

        poll_db(args.db, tracks)

        # announce newly completed devices
        for t in tracks.values():
            if t.has_new_reading() and not getattr(t, "_announced", False):
                t._announced = True  # type: ignore[attr-defined]
                print(
                    f"\n*** NEW DATA {now_str()} {t.brand} mac={t.mac} "
                    f"new_rows={len(t.new_reading_ids)} "
                    f"created={t.first_new_created_at} "
                    f"measured={t.first_new_measured_at} ***\n",
                    flush=True,
                )

        if time.time() - last_summary >= 30.0:
            summary(tracks)
            last_summary = time.time()

        if all(t.has_new_reading() for t in tracks.values()):
            summary(tracks)
            print(f"[{now_str()}] ALL DEVICES HAVE ≥1 NEW READING — done.", flush=True)
            return 0

        time.sleep(args.poll)

    summary(tracks)
    print(f"[{now_str()}] Timeout reached", flush=True)
    return 2


if __name__ == "__main__":
    raise SystemExit(main())

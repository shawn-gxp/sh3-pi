"""
Standalone NC-1BLE (Cocoron) stream session — bleak only.

Use this for live bring-up before / beside the main hub path.

  python -m medical_ble_toolkit.brands.nipro.nc1_session scan
  python -m medical_ble_toolkit.brands.nipro.nc1_session run -a AA:BB:... -t 60
"""

from __future__ import annotations

import argparse
import asyncio
import csv
import json
import logging
import sys
import time
from dataclasses import asdict
from datetime import datetime
from pathlib import Path
from typing import Any, Callable, List, Optional

from bleak import BleakClient, BleakScanner
from bleak.backends.device import BLEDevice
from bleak.exc import BleakError

from medical_ble_toolkit.parsers import nipro_nc1 as nc1

log = logging.getLogger("medical_ble.nc1_session")


def _brief(obj: Any) -> str:
    if hasattr(obj, "to_dict"):
        d = obj.to_dict()
        t = d.get("type")
        if t == "rri":
            lod = " LOD" if d.get("leads_off") else ""
            return f"RRI rr={d.get('rr_ms')}ms HR={d.get('heart_rate_bpm')}{lod}"
        if t == "ecg":
            return (
                f"ECG #{d.get('packet_counter')} n={d.get('n_samples')} "
                f"mV=[{d.get('mv_min')},{d.get('mv_max')}] ok={d.get('decode_ok')}"
            )
        if t == "battery":
            low = " LOW" if d.get("low_battery") else ""
            return f"BAT {d.get('level_mv')}mV fw={d.get('firmware')}{low}"
        return str(d)
    return str(obj)


async def scan(
    timeout: float = 12.0,
    *,
    service_filter: bool = True,
) -> List[BLEDevice]:
    """Scan for Cocoron service (or all devices if service_filter=False)."""
    log.info(
        "Scanning %.0fs (filter service %s)…",
        timeout,
        nc1.SVC_COCORON if service_filter else "OFF",
    )
    if service_filter:
        devices = await BleakScanner.discover(
            timeout=timeout,
            service_uuids=[nc1.SVC_COCORON],
        )
    else:
        devices = await BleakScanner.discover(timeout=timeout)

    # Also accept name hints when no service filter
    out: List[BLEDevice] = []
    for d in devices:
        name = d.name or ""
        if service_filter:
            out.append(d)
            continue
        upper = name.upper()
        if any(h.upper() in upper for h in nc1.NAME_HINTS) or "NC-1" in upper:
            out.append(d)
    # Prefer service-filter list as-is
    if service_filter and not out:
        # Fallback: broad scan + name/service ad check
        log.warning("No service-filtered devices; broad scan…")
        all_dev = await BleakScanner.discover(timeout=timeout)
        for d in all_dev:
            name = (d.name or "").upper()
            if any(h.upper() in name for h in nc1.NAME_HINTS) or "NC-1" in name:
                out.append(d)
    for d in out:
        log.info("  %s  %s", d.address, d.name or "(no name)")
    if not out:
        log.warning("No Cocoron/NC-1 candidates found")
    return out


async def _resolve_device(
    address: str,
    *,
    hunt_s: float = 12.0,
) -> Any:
    """
    Prefer a live BLEDevice from the scanner (WinRT is more reliable than
    connecting by bare MAC string alone).
    """
    mac = address.strip().upper()
    log.info("Resolving device %s (scan up to %.0fs)…", mac, hunt_s)
    try:
        found = await BleakScanner.find_device_by_address(mac, timeout=hunt_s)
        if found is not None:
            log.info("  found via scanner: %s (%s)", found.address, found.name)
            return found
    except Exception as exc:  # noqa: BLE001
        log.warning("find_device_by_address failed: %s", exc)

    # Fallback: service-filtered discover
    try:
        devs = await BleakScanner.discover(
            timeout=min(hunt_s, 10.0),
            service_uuids=[nc1.SVC_COCORON],
        )
        for d in devs:
            if (d.address or "").upper() == mac:
                log.info("  found via service scan: %s (%s)", d.address, d.name)
                return d
        if len(devs) == 1:
            log.info(
                "  address mismatch; using only Cocoron advertiser %s (%s)",
                devs[0].address,
                devs[0].name,
            )
            return devs[0]
    except Exception as exc:  # noqa: BLE001
        log.warning("service scan fallback failed: %s", exc)

    # Last resort: platform target
    import sys

    if sys.platform.startswith("win"):
        log.warning("  using synthetic BLEDevice(mac) — Windows address path")
        return BLEDevice(mac, "NC-1BLE", None)
    log.warning("  using bare MAC string")
    return mac


async def run_session(
    address: str,
    *,
    duration_s: float = 60.0,
    free_run: bool = False,
    interval_seconds: int = 60,
    host_id: Optional[str] = None,
    on_reading: Optional[Callable[[Any], None]] = None,
    save_csv: Optional[Path] = None,
    save_jsonl: Optional[Path] = None,
    raw_hex: bool = False,
    connect_timeout: float = 45.0,
    connect_retries: int = 3,
    hunt_s: float = 12.0,
) -> nc1.NiproNc1Parser:
    """
    Connect → notify ECG/RRT/Battery → CONFIG → DateTime → stream.

    Mirrors companion BluetoothLeService order (CCCD via start_notify).
    """
    parser = nc1.NiproNc1Parser()
    hid = host_id or nc1.make_host_id()
    prefs = nc1.seconds_to_prefs_index(interval_seconds)
    cfg = nc1.encode_config(
        free_run=free_run,
        interval_prefs=prefs,
        host_id=hid,
        use_companion_interval_map=True,
    )
    clock = nc1.encode_datetime()

    jsonl_f = None
    csv_w = None
    csv_f = None
    if save_jsonl:
        save_jsonl.parent.mkdir(parents=True, exist_ok=True)
        jsonl_f = open(save_jsonl, "a", encoding="utf-8")
    if save_csv:
        save_csv.parent.mkdir(parents=True, exist_ok=True)
        new_file = not save_csv.exists() or save_csv.stat().st_size == 0
        csv_f = open(save_csv, "a", newline="", encoding="utf-8")
        csv_w = csv.writer(csv_f)
        if new_file:
            csv_w.writerow(
                [
                    "ts",
                    "type",
                    "rr_ms",
                    "hr",
                    "leads_off",
                    "ecg_n",
                    "mv_min",
                    "mv_max",
                    "battery_mv",
                    "packet_counter",
                ]
            )

    def _emit(rec: Any) -> None:
        log.info("%s", _brief(rec))
        if raw_hex and hasattr(rec, "raw_hex"):
            log.info("  raw: %s", rec.raw_hex[:120])
        if on_reading:
            try:
                on_reading(rec)
            except Exception as exc:  # noqa: BLE001
                log.error("on_reading failed: %s", exc)
        d = rec.to_dict() if hasattr(rec, "to_dict") else {"type": "unknown"}
        if jsonl_f:
            jsonl_f.write(
                json.dumps({"ts": datetime.now().isoformat(), **d}, default=str)
                + "\n"
            )
            jsonl_f.flush()
        if csv_w:
            t = d.get("type")
            csv_w.writerow(
                [
                    datetime.now().isoformat(),
                    t,
                    d.get("rr_ms"),
                    d.get("heart_rate_bpm"),
                    d.get("leads_off"),
                    d.get("n_samples"),
                    d.get("mv_min"),
                    d.get("mv_max"),
                    d.get("level_mv"),
                    d.get("packet_counter"),
                ]
            )
            csv_f.flush()

    def handler_factory(char_uuid: str):
        def _handler(_sender: int, data: bytearray) -> None:
            try:
                rec = parser.parse(bytes(data), characteristic_uuid=char_uuid)
                _emit(rec)
            except Exception as exc:  # noqa: BLE001
                log.error(
                    "Parse fail %s (%d B): %s",
                    char_uuid[-12:],
                    len(data),
                    exc,
                )
                if raw_hex:
                    from medical_ble_toolkit.common.hexutil import format_hex_dump

                    log.error("  hex: %s", format_hex_dump(data))

        return _handler

    log.info(
        "Connecting %s  duration=%.0fs free_run=%s interval=%ss host_id=%s",
        address,
        duration_s,
        free_run,
        interval_seconds,
        hid,
    )
    log.info("CONFIG hex: %s", cfg.hex())
    log.info("DATETIME hex: %s", clock.hex())

    target = await _resolve_device(address, hunt_s=hunt_s)
    last_err: Optional[BaseException] = None
    client: Optional[BleakClient] = None

    # Narrow GATT discovery — full service walk often hangs on WinRT for this device
    known_services = [
        nc1.SVC_COCORON,
        "0000180f-0000-1000-8000-00805f9b34fb",  # Battery
        "00001805-0000-1000-8000-00805f9b34fb",  # Current Time (if present)
        "0000180a-0000-1000-8000-00805f9b34fb",  # DIS
        "00001800-0000-1000-8000-00805f9b34fb",  # GAP
        "00001801-0000-1000-8000-00805f9b34fb",  # GATT
    ]

    for attempt in range(1, max(1, connect_retries) + 1):
        use_pair = attempt == 2  # try OS pair only on 2nd attempt
        client = BleakClient(
            target,
            timeout=connect_timeout,
            pair=use_pair,
            services=known_services,
        )
        try:
            log.info(
                "Connect attempt %d/%d (pair=%s, filtered services)…",
                attempt,
                connect_retries,
                use_pair,
            )
            await client.connect()
            if client.is_connected:
                log.info("Connected.")
                last_err = None
                break
            last_err = BleakError("connect returned but not connected")
        except (BleakError, TimeoutError, asyncio.TimeoutError, OSError) as exc:
            last_err = exc
            log.warning("Connect attempt %d failed: %s: %s", attempt, type(exc).__name__, exc)
            try:
                await client.disconnect()
            except Exception:  # noqa: BLE001
                pass
            if attempt < connect_retries:
                target = await _resolve_device(address, hunt_s=min(8.0, hunt_s))
                await asyncio.sleep(1.5)
        except Exception as exc:  # noqa: BLE001
            last_err = exc
            log.exception("Unexpected connect error: %s", exc)
            break

    if client is None or not client.is_connected:
        if jsonl_f:
            jsonl_f.close()
        if csv_f:
            csv_f.close()
        raise BleakError(
            f"Failed to connect to {address}: {last_err}\n"
            "Windows tips:\n"
            "  1) Disconnect/forget the device on any phone running Cocoron app\n"
            "     (firmware blocks a 2nd simultaneous central).\n"
            "  2) Windows Settings → Bluetooth → remove NC20K007 if listed,\n"
            "     then power-cycle the patch and retry.\n"
            "  3) Keep the device near the PC and advertising (scan still works).\n"
            "  4) Toggle PC Bluetooth off/on once, then re-run scan + run."
        )

    try:
        log.info("Enabling notifications…")
        # Companion order prefers RRT CCCD first; bleak start_notify is sequential.
        for label, uid in (
            ("RRT", nc1.CHAR_RRT),
            ("ECG", nc1.CHAR_ECG),
            ("BATTERY", nc1.CHAR_BATTERY),
        ):
            try:
                await client.start_notify(uid, handler_factory(uid))
                log.info("  notify ON %s", label)
            except BleakError as exc:
                log.error("  notify FAIL %s: %s", label, exc)

        await asyncio.sleep(0.15)

        try:
            await client.write_gatt_char(nc1.CHAR_CONFIG, cfg, response=True)
            log.info("CONFIG written OK")
        except BleakError as exc:
            log.error("CONFIG write failed: %s", exc)
            try:
                await client.write_gatt_char(nc1.CHAR_CONFIG, cfg, response=False)
                log.info("CONFIG written (no-response)")
            except BleakError as exc2:
                log.error("CONFIG write retry failed: %s", exc2)

        await asyncio.sleep(0.1)
        try:
            await client.write_gatt_char(nc1.CHAR_DATETIME, clock, response=True)
            log.info("DATETIME written OK")
        except BleakError as exc:
            log.warning("DATETIME write failed (continuing): %s", exc)

        log.info("Streaming for %.0fs — wear device, stay nearby…", duration_s)
        t0 = time.monotonic()
        while time.monotonic() - t0 < duration_s:
            await asyncio.sleep(0.25)
            if not client.is_connected:
                log.warning("Disconnected early at %.1fs", time.monotonic() - t0)
                break
    finally:
        for uid in (nc1.CHAR_ECG, nc1.CHAR_RRT, nc1.CHAR_BATTERY):
            try:
                await client.stop_notify(uid)
            except Exception:  # noqa: BLE001
                pass
        try:
            await client.disconnect()
        except Exception:  # noqa: BLE001
            pass
        if jsonl_f:
            jsonl_f.close()
        if csv_f:
            csv_f.close()

    stats = nc1.summary_stats(parser)
    log.info("Session done: %s", stats)
    return parser


def _build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="Nipro Cocoron NC-1BLE live BLE test (standalone)"
    )
    p.add_argument(
        "-v",
        "--verbose",
        action="store_true",
        help="DEBUG logging",
    )
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("scan", help="Scan for Cocoron service / NC-1 name")
    s.add_argument("-t", "--timeout", type=float, default=12.0)
    s.add_argument(
        "--no-service-filter",
        action="store_true",
        help="Broad scan; filter by name hints only",
    )

    r = sub.add_parser("run", help="Connect + configure + stream")
    r.add_argument("-a", "--address", required=True, help="BLE address / device id")
    r.add_argument(
        "-t",
        "--duration",
        type=float,
        default=60.0,
        help="Stream seconds (default 60)",
    )
    r.add_argument(
        "--interval",
        type=int,
        default=60,
        choices=(5, 10, 30, 60),
        help="HR notify period seconds (default 60)",
    )
    r.add_argument(
        "--free-run",
        action="store_true",
        help="CONFIG mode free-run / continuous ECG path",
    )
    r.add_argument("--host-id", default=None, help="8-char host id (default random)")
    r.add_argument("--raw", action="store_true", help="Log raw hex")
    r.add_argument("--csv", type=Path, default=None, help="Append CSV path")
    r.add_argument("--jsonl", type=Path, default=None, help="Append JSONL path")
    r.add_argument(
        "--connect-timeout",
        type=float,
        default=45.0,
        help="Per-attempt GATT connect timeout (default 45)",
    )

    g = sub.add_parser("selftest", help="Offline unit checks (no BLE)")
    return p


async def _async_main(args: argparse.Namespace) -> int:
    logging.basicConfig(
        level=logging.DEBUG if args.verbose else logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )
    logging.getLogger("bleak").setLevel(logging.WARNING)

    if args.cmd == "selftest":
        return _selftest()

    if args.cmd == "scan":
        devs = await scan(
            timeout=args.timeout,
            service_filter=not args.no_service_filter,
        )
        print(f"\nFound {len(devs)} device(s)")
        for d in devs:
            print(f"  {d.address}\t{d.name or ''}")
        return 0 if devs else 1

    if args.cmd == "run":
        try:
            parser = await run_session(
                args.address,
                duration_s=args.duration,
                free_run=args.free_run,
                interval_seconds=args.interval,
                host_id=args.host_id,
                save_csv=args.csv,
                save_jsonl=args.jsonl,
                raw_hex=args.raw,
                connect_timeout=args.connect_timeout,
            )
        except (BleakError, TimeoutError, asyncio.TimeoutError, OSError) as exc:
            log.error("BLE error: %s", exc)
            return 2
        stats = nc1.summary_stats(parser)
        print("\n=== summary ===")
        print(json.dumps(stats, indent=2))
        ok = stats["rri_packets"] > 0 or stats["ecg_packets"] > 0
        if not ok:
            log.warning(
                "No RRI/ECG packets — device may be unworn, still connecting, "
                "or not streaming. Check LOD / battery lines above."
            )
        return 0 if ok else 3

    return 1


def _selftest() -> int:
    """No-hardware checks for encoders + empty/short packet guards."""
    logging.basicConfig(level=logging.INFO)
    errors = 0

    def check(cond: bool, msg: str) -> None:
        nonlocal errors
        if cond:
            print(f"  OK  {msg}")
        else:
            print(f"  FAIL {msg}")
            errors += 1

    cfg = nc1.encode_config(free_run=False, interval_prefs=0, host_id="abcd1234")
    check(len(cfg) == 10, f"config len 10 got {len(cfg)}")
    check(cfg[0] == 0, f"mode normal 0 got {cfg[0]}")
    check(cfg[1] == 3, f"wire interval 60s→3 got {cfg[1]}")
    check(cfg[2:10] == b"abcd1234", "host id")

    cfg2 = nc1.encode_config(free_run=True, interval_prefs=3, host_id="xyz")
    check(cfg2[0] == 0x02, "free_run mode 0x02")
    check(cfg2[1] == 0, "5s wire interval 0")

    dt = nc1.encode_datetime(datetime(2026, 7, 24, 12, 30, 45))
    check(len(dt) == 7, "datetime 7 bytes")
    check(dt[0] | (dt[1] << 8) == 2026, "year 2026")

    # Battery
    bat = nc1.parse_battery(bytes([0x04, 0x0B, 1, 0, 0]))  # 2820 mV
    check(bat.level_mv == 0x0B04, f"battery mv {bat.level_mv}")

    # RRT without datetime: flags + rr_lo rr_hi + interval
    # rr=1000 ms → HR 60
    rrt = nc1.parse_rrt(bytes([0x00, 0xE8, 0x03, 0x03]))
    check(rrt.rr_ms == 1000, f"rr {rrt.rr_ms}")
    check(rrt.heart_rate_bpm == 60.0, f"hr {rrt.heart_rate_bpm}")

    rrt_lod = nc1.parse_rrt(bytes([0x20, 0xE8, 0x03, 0x03]))
    check(rrt_lod.leads_off, "leads_off flag")

    # Huffman table loads
    try:
        table = nc1.load_huffman_table()
        check(len(table) == 4096, f"huffman nodes {len(table)}")
    except Exception as exc:  # noqa: BLE001
        check(False, f"huffman load: {exc}")

    # Interval map
    check(nc1.prefs_to_wire_interval(0) == 3, "prefs0→3")
    check(nc1.seconds_to_prefs_index(10) == 2, "10s→prefs2")

    print(f"\nselftest: {errors} failure(s)")
    return 1 if errors else 0


def main(argv: Optional[List[str]] = None) -> int:
    args = _build_parser().parse_args(argv)
    return asyncio.run(_async_main(args))


if __name__ == "__main__":
    sys.exit(main())

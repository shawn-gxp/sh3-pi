"""
CLI entry — interactive menu or argparse subcommands.

  python -m omron_bp              → interactive menu
  python -m omron_bp pair ...
  python -m omron_bp read ...     → wait for cuff, download once
  python -m omron_bp watch ...    → auto-fetch: wait longer, download, show latest
  python -m omron_bp list-models

This file only handles UX (args, prompts). Real work is in pairing/ and readout/.
"""

from __future__ import annotations

import argparse
import asyncio
import sys
from typing import Any, Dict, List, Optional, Sequence

from omron_bp import __app_name__, __version__
from omron_bp.ble.scanner import pick_device_interactive
from omron_bp.config.store import load_device_config, save_device_config
from omron_bp.export.csv_export import write_users_csv
from omron_bp.export.records_util import sort_records_newest_first
from omron_bp.logging_config import DBG_TAG, get_logger, setup_logging
from omron_bp.models.registry import get_profile, list_models
from omron_bp.pairing.service import pair_device
from omron_bp.readout.service import read_device_records

logger = get_logger("cli")

# Sensible default for this lab PC (override with -m / config)
DEFAULT_LAB_MAC = "E1:99:7D:27:1C:0A"
DEFAULT_LAB_MODEL = "HEM-7143T1"


def _verbose_flag(parser: argparse.ArgumentParser) -> None:
    """Allow -v before or after the subcommand (argparse parent vs child)."""
    parser.add_argument(
        "-v",
        "--verbose",
        action="store_true",
        help="DEBUG logs (hex TX/RX). Also: set OMRON_BP_DEBUG=1.",
    )


def _add_read_output_args(p: argparse.ArgumentParser) -> None:
    p.add_argument(
        "-o",
        "--output-dir",
        default=".",
        help="Directory for userN.csv (default: current directory)",
    )
    p.add_argument(
        "--no-merge",
        action="store_true",
        help="Overwrite CSV without merging previous rows",
    )
    p.add_argument(
        "--latest",
        type=int,
        default=5,
        metavar="N",
        help="How many newest readings to print (default: 5)",
    )


def _build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog=__app_name__,
        description="Omron BP BLE toolkit — pair once, read / auto-fetch measurements.",
    )
    p.add_argument(
        "--version",
        action="version",
        version=f"{__app_name__} {__version__}",
    )
    _verbose_flag(p)

    common = argparse.ArgumentParser(add_help=False)
    _verbose_flag(common)
    _add_common_device_args(common)

    sub = p.add_subparsers(dest="command")

    sub.add_parser(
        "pair",
        parents=[common],
        help="Pair / bond with the cuff (once per PC)",
    )

    read_p = sub.add_parser(
        "read",
        parents=[common],
        help="Download BP records (waits for cuff transfer mode)",
    )
    _add_read_output_args(read_p)
    read_p.add_argument(
        "--wait",
        type=float,
        default=60.0,
        metavar="SEC",
        help="Seconds to wait for cuff to advertise (default: 60)",
    )

    watch_p = sub.add_parser(
        "watch",
        parents=[common],
        help="Auto-fetch once: wait for cuff, download, print latest",
    )
    _add_read_output_args(watch_p)
    watch_p.add_argument(
        "--wait",
        type=float,
        default=180.0,
        metavar="SEC",
        help="Seconds to wait for cuff (default: 180 for auto-fetch)",
    )

    # ---- hands-free station: force sync every N seconds ----
    serve_p = sub.add_parser(
        "serve",
        parents=[common],
        help="Hands-free station: force-sync every minute (no button after pair)",
    )
    _add_read_output_args(serve_p)
    serve_p.add_argument(
        "--interval",
        type=float,
        default=60.0,
        metavar="SEC",
        help="Seconds between force-sync attempts (default: 60)",
    )
    serve_p.add_argument(
        "--wait",
        type=float,
        default=None,
        metavar="SEC",
        help="Max seconds to wait for cuff each cycle (default: interval - 5)",
    )

    sub.add_parser(
        "list-models",
        parents=[common],
        help="Show registered device profiles",
    )

    sub.add_parser(
        "menu",
        parents=[common],
        help="Interactive menu (default)",
    )

    return p


def _add_common_device_args(p: argparse.ArgumentParser) -> None:
    p.add_argument(
        "-d",
        "--device",
        type=str,
        default=None,
        help="Model id (e.g. HEM-7143T1). Uses config / default if omitted.",
    )
    p.add_argument(
        "-m",
        "--mac",
        type=str,
        default=None,
        help="Bluetooth address. Omit to scan and pick from a list.",
    )
    p.add_argument(
        "--no-save-config",
        action="store_true",
        help="Do not write omron_bp_device.json after success",
    )


def _resolve_model_and_mac(
    device: Optional[str],
    mac: Optional[str],
    *,
    allow_scan: bool,
) -> tuple[str, Optional[str]]:
    cfg = load_device_config()
    model = device or cfg.get("model_id") or DEFAULT_LAB_MODEL
    address = mac or cfg.get("address")

    # DBG-LOG
    logger.debug(
        "%s resolve model=%s address=%s (cli device=%s mac=%s cfg=%s)",
        DBG_TAG,
        model,
        address,
        device,
        mac,
        cfg,
    )

    if address is None and not allow_scan:
        address = DEFAULT_LAB_MAC
    return model, address


async def _ensure_address(address: Optional[str]) -> str:
    if address:
        return address
    print("No MAC stored — scanning. Put cuff in P or transfer mode.")
    return await pick_device_interactive()


def _print_latest_readings(
    records: Sequence[List[Dict[str, Any]]],
    *,
    latest_n: int = 5,
) -> None:
    """
    Print latest values clearly in the CLI (newest first).
    Highlights the single newest reading per user.
    """
    print()
    print("=" * 60)
    print("  LATEST READINGS")
    print("=" * 60)

    for user_idx, user_recs in enumerate(records):
        sorted_recs = sort_records_newest_first(list(user_recs))
        label = f"User {user_idx + 1}"
        if not sorted_recs:
            print(f"\n  {label}: (no records)")
            continue

        top = sorted_recs[0]
        print(f"\n  {label} — newest measurement")
        print(f"  ----------------------------------------")
        print(f"  Time     {top['datetime']}")
        print(f"  SYS      {top['sys']} mmHg")
        print(f"  DIA      {top['dia']} mmHg")
        print(f"  BPM      {top['bpm']}")
        print(f"  mov={top.get('mov', 0)}  ihb={top.get('ihb', 0)}")
        print(f"  ----------------------------------------")

        n = max(0, latest_n)
        if n > 1 and len(sorted_recs) > 1:
            print("  Previous readings (newest → older):")
            for rec in sorted_recs[1:n]:
                print(
                    f"    {rec['datetime']}  "
                    f"SYS={rec['sys']}  DIA={rec['dia']}  BPM={rec['bpm']}  "
                    f"mov={rec.get('mov', 0)} ihb={rec.get('ihb', 0)}"
                )
        if len(sorted_recs) > n:
            print(f"  ... ({len(sorted_recs) - n} older not shown)")
        print(f"  Total on cuff (this user): {len(sorted_recs)}")

    print()
    print("=" * 60)


async def _download_and_show(
    *,
    model: str,
    address: str,
    output_dir: str,
    merge: bool,
    find_timeout: float,
    latest_n: int,
    no_save_config: bool,
    banner: str,
) -> int:
    profile = get_profile(model)

    print()
    print("=" * 60)
    print(f"  {banner}")
    print(f"  model={profile.model_id}")
    print(f"  MAC  ={address}")
    print(f"  wait ={find_timeout:.0f}s for cuff advert")
    print("=" * 60)
    print("On the cuff: SHORT-press Bluetooth once (transfer mode).")
    print("Do NOT hold for P — that is only for pairing.")
    print("Waiting for cuff… press BT when ready.")
    print()

    records = await read_device_records(
        address,
        profile,
        find_timeout=find_timeout,
    )
    paths = write_users_csv(
        records,
        output_dir=output_dir,
        merge_existing=merge,
    )

    _print_latest_readings(records, latest_n=latest_n)

    print("CSV (newest first):")
    for path in paths:
        print(f"  {path}")

    if not no_save_config:
        save_device_config(address=address, model_id=profile.model_id)
    return 0


async def cmd_pair(args: argparse.Namespace) -> int:
    model, address = _resolve_model_and_mac(args.device, args.mac, allow_scan=True)
    profile = get_profile(model)
    address = await _ensure_address(address)

    print()
    print("=" * 60)
    print(f"  PAIR  model={profile.model_id}  mode={profile.pairing_mode.value}")
    print(f"  MAC   {address}")
    if profile.notes:
        print(f"  NOTE  {profile.notes}")
    print("=" * 60)
    print("On the cuff: hold Bluetooth 3–5s until flashing P / -P-")
    print()

    await pair_device(address, profile)

    if not args.no_save_config:
        save_device_config(address=address, model_id=profile.model_id)
    print("Pairing workflow completed.")
    return 0


async def cmd_read(args: argparse.Namespace) -> int:
    model, address = _resolve_model_and_mac(args.device, args.mac, allow_scan=True)
    address = await _ensure_address(address)
    return await _download_and_show(
        model=model,
        address=address,
        output_dir=args.output_dir,
        merge=not args.no_merge,
        find_timeout=float(args.wait),
        latest_n=int(args.latest),
        no_save_config=args.no_save_config,
        banner="READ (download)",
    )


async def cmd_watch(args: argparse.Namespace) -> int:
    """
    Auto-fetch once: wait for cuff, download, print latest values.
    """
    model, address = _resolve_model_and_mac(args.device, args.mac, allow_scan=True)
    address = await _ensure_address(address)
    return await _download_and_show(
        model=model,
        address=address,
        output_dir=args.output_dir,
        merge=not args.no_merge,
        find_timeout=float(args.wait),
        latest_n=int(args.latest),
        no_save_config=args.no_save_config,
        banner="AUTO-FETCH (watch)",
    )


async def cmd_serve(args: argparse.Namespace) -> int:
    """
    Hands-free data-collection station.

    Force-sync on a fixed interval (default 60s): try to connect and download
    without user input. Pair once beforehand. Cuff radio must be reachable
    (often after a measurement or when still awake).
    """
    import time

    model, address = _resolve_model_and_mac(args.device, args.mac, allow_scan=True)
    address = await _ensure_address(address)
    profile = get_profile(model)
    interval = max(15.0, float(args.interval))
    # Leave a few seconds between cycles so Windows BLE can reset
    wait = float(args.wait) if args.wait is not None else max(10.0, interval - 5.0)
    wait = min(wait, interval - 2.0) if interval > 12 else wait
    latest_n = int(args.latest)
    output_dir = args.output_dir
    merge = not args.no_merge
    cycle = 0
    last_newest_key: str | None = None

    print()
    print("=" * 60)
    print("  HANDS-FREE STATION (force sync)")
    print(f"  model    = {profile.model_id}")
    print(f"  MAC      = {address}")
    print(f"  interval = {interval:.0f}s  (force sync every cycle)")
    print(f"  wait     = {wait:.0f}s per cycle for cuff advert")
    print(f"  output   = {output_dir}")
    print("=" * 60)
    print("Pair once with 'pair' if not already bonded.")
    print("Daily use: leave this running. No BLE button needed if cuff is")
    print("reachable (often wakes after a measurement). Ctrl+C to stop.")
    print()

    if not args.no_save_config:
        save_device_config(address=address, model_id=profile.model_id)

    while True:
        cycle += 1
        t0 = time.monotonic()
        print()
        print("-" * 60)
        print(f"  SYNC #{cycle}  {time.strftime('%Y-%m-%d %H:%M:%S')}")
        print("-" * 60)
        logger.info(
            "Force sync #%d model=%s address=%s wait=%.0fs",
            cycle,
            profile.model_id,
            address,
            wait,
        )

        try:
            records = await read_device_records(
                address,
                profile,
                find_timeout=wait,
                session_retries=2,
            )
            write_users_csv(
                records,
                output_dir=output_dir,
                merge_existing=merge,
            )
            _print_latest_readings(records, latest_n=latest_n)

            # Flag if newest reading changed since last successful sync
            newest_key = None
            for user_recs in records:
                sorted_recs = sort_records_newest_first(list(user_recs))
                if sorted_recs:
                    r0 = sorted_recs[0]
                    newest_key = f"{r0.get('datetime')}|{r0.get('sys')}|{r0.get('dia')}|{r0.get('bpm')}"
                    break
            if newest_key and newest_key != last_newest_key:
                if last_newest_key is not None:
                    print("  >>> NEW measurement detected since last sync <<<")
                last_newest_key = newest_key
            print(f"  Sync #{cycle} OK")
        except KeyboardInterrupt:
            raise
        except Exception as exc:
            logger.warning(
                "Sync #%d failed: %s: %s",
                cycle,
                type(exc).__name__,
                exc,
            )
            print(f"  Sync #{cycle} FAILED: {type(exc).__name__}: {exc}")
            print("  (Will retry next interval — cuff may be sleeping.)")

        elapsed = time.monotonic() - t0
        sleep_for = max(0.0, interval - elapsed)
        if sleep_for > 0:
            print(f"  Next force sync in {sleep_for:.0f}s …")
            try:
                await asyncio.sleep(sleep_for)
            except asyncio.CancelledError:
                raise


def cmd_list_models() -> int:
    print(
        f"{'MODEL':16}  {'STACK':8}  {'PAIRING':12}  {'UNLOCK':12}  "
        f"{'U':>2}  {'SRC':8}  DISPLAY NAME"
    )
    print("-" * 100)
    for prof in list_models():
        print(
            f"{prof.model_id:16}  {prof.stack.value:8}  "
            f"{prof.pairing_mode.value:12}  {prof.unlock_mode.value:12}  "
            f"{prof.user_count:2}  {(prof.source or '-'):8}  {prof.display_name}"
        )
    print("-" * 100)
    n = len(list_models())
    print(f"{n} canonical profiles. Regional SKUs resolve as aliases.")
    return 0


async def interactive_menu(verbose: bool) -> int:
    setup_logging(verbose=verbose)
    cfg = load_device_config()
    print()
    print("=" * 60)
    print(f"  {__app_name__} v{__version__}  — Omron BP BLE")
    print("=" * 60)
    if cfg:
        print(f"  Saved: model={cfg.get('model_id')}  mac={cfg.get('address')}")
    else:
        print(f"  Lab default: model={DEFAULT_LAB_MODEL}  mac={DEFAULT_LAB_MAC}")
    print()
    print("  1) Pair        — bond this PC with the cuff (once)")
    print("  2) Read        — download BP records")
    print("  3) Auto-fetch  — wait for cuff once, show latest")
    print("  4) Station     — hands-free force sync every 1 min")
    print("  5) List models")
    print("  6) Quit")
    print()

    choice = input("Select option [1-6]: ").strip()
    # DBG-LOG
    logger.debug("%s menu choice=%r", DBG_TAG, choice)

    ns = argparse.Namespace(
        device=None,
        mac=None,
        no_save_config=False,
        output_dir=".",
        no_merge=False,
        verbose=verbose,
        wait=60.0,
        latest=5,
    )

    try:
        if choice == "1":
            model = input(f"Model [{cfg.get('model_id') or DEFAULT_LAB_MODEL}]: ").strip()
            mac = input(f"MAC blank=saved/scan [{cfg.get('address') or 'scan'}]: ").strip()
            ns.device = model or None
            ns.mac = mac or None
            return await cmd_pair(ns)
        if choice == "2":
            model = input(f"Model [{cfg.get('model_id') or DEFAULT_LAB_MODEL}]: ").strip()
            mac = input(f"MAC blank=saved/scan [{cfg.get('address') or 'scan'}]: ").strip()
            out = input("Output dir [.]: ").strip()
            ns.device = model or None
            ns.mac = mac or None
            ns.output_dir = out or "."
            ns.wait = 60.0
            return await cmd_read(ns)
        if choice == "3":
            model = input(f"Model [{cfg.get('model_id') or DEFAULT_LAB_MODEL}]: ").strip()
            mac = input(f"MAC blank=saved/scan [{cfg.get('address') or 'scan'}]: ").strip()
            out = input("Output dir [.]: ").strip()
            ns.device = model or None
            ns.mac = mac or None
            ns.output_dir = out or "."
            ns.wait = 180.0
            ns.latest = 5
            return await cmd_watch(ns)
        if choice == "4":
            out = input("Output dir [./data]: ").strip()
            ns.output_dir = out or "./data"
            ns.interval = 60.0
            ns.wait = None
            ns.latest = 5
            return await cmd_serve(ns)
        if choice == "5":
            return cmd_list_models()
        if choice in ("6", "q", "Q", ""):
            print("Bye.")
            return 0
    except KeyboardInterrupt:
        print("\nInterrupted.")
        return 130
    except Exception as exc:
        logger.error("%s: %s", type(exc).__name__, exc)
        if verbose:
            logger.exception("%s full traceback", DBG_TAG)
        print(
            "\nTip: If you saw 'Unreachable', the Windows BLE link dropped.\n"
            "  • Short-press BT again (transfer mode) and re-run watch/read.\n"
            "  • Keep cuff ~1 m from PC; avoid re-pairing mid-read.\n"
            "  • For hands-free station use: python -m omron_bp serve -o .\\data"
        )
        return 1

    print("Unknown option.")
    return 1


def main(argv: list[str] | None = None) -> int:
    argv = list(sys.argv[1:] if argv is None else argv)
    parser = _build_parser()
    args = parser.parse_args(argv)

    if args.command is None or args.command == "menu":
        return asyncio.run(interactive_menu(verbose=getattr(args, "verbose", False)))

    setup_logging(verbose=args.verbose)
    # DBG-LOG
    logger.debug("%s cli command=%s argv=%s", DBG_TAG, args.command, argv)

    try:
        if args.command == "pair":
            return asyncio.run(cmd_pair(args))
        if args.command == "read":
            return asyncio.run(cmd_read(args))
        if args.command == "watch":
            return asyncio.run(cmd_watch(args))
        if args.command == "serve":
            return asyncio.run(cmd_serve(args))
        if args.command == "list-models":
            return cmd_list_models()
    except KeyboardInterrupt:
        print("\nInterrupted.")
        return 130
    except Exception as exc:
        logger.error("%s: %s", type(exc).__name__, exc)
        if args.verbose:
            logger.exception("%s full traceback", DBG_TAG)
        return 1

    parser.print_help()
    return 2


if __name__ == "__main__":
    raise SystemExit(main())

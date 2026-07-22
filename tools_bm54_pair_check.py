#!/usr/bin/env python3
"""
Standalone BM54 pairing diagnostic for Linux/BlueZ (Pi hub).

Debugs connect → SMP passkey bond → CCCD Indicate → BP dump without the hub.
Use this to isolate pairing failures, then fold proven settings into the
Beurer session path.

Phone HCI reference (datasheets/beurer/BM54_PHONE_HCI_FINDINGS.md):
  connect → CCCD Indicate → ATT 0x05 → SMP passkey → CCCD retry → 0x2A35 dump

Examples:
  # Show BlueZ bond state for BM54
  PYTHONPATH=. .venv/bin/python tools_bm54_pair_check.py status

  # Forget BM54 on this Pi (clears stale/broken bond)
  PYTHONPATH=. .venv/bin/python tools_bm54_pair_check.py remove

  # Scan only (wake cuff: M1/M2 or finish a measurement)
  PYTHONPATH=. .venv/bin/python tools_bm54_pair_check.py scan -t 45

  # Full pair+sync — you do NOT need the passkey in advance.
  # Cuff shows 6 digits only after connect/SMP starts; type them then.
  PYTHONPATH=. .venv/bin/python tools_bm54_pair_check.py pair \\
      -a 0C:7F:ED:72:BC:40

  # Remove bond first, then pair (clean rebind)
  PYTHONPATH=. .venv/bin/python tools_bm54_pair_check.py pair \\
      -a 0C:7F:ED:72:BC:40 --remove-first

  # Already bonded? Just connect + dump history
  PYTHONPATH=. .venv/bin/python tools_bm54_pair_check.py sync \\
      -a 0C:7F:ED:72:BC:40 --no-pair

Passkey flow (normal — no --passkey):
  1. Wake cuff (M1/M2 or after measure) so it advertises
  2. Run pair — script connects and starts bonding
  3. Cuff LCD shows 6-digit code  ← only now is it known
  4. Type those 6 digits here and press Enter (90s window)
  5. Bond completes → BP history dump
"""

from __future__ import annotations

import argparse
import asyncio
import logging
import shutil
import subprocess
import sys
import time
from typing import Any, List, Optional, Tuple

# ---------------------------------------------------------------------------
# Constants (SIG BLP)
# ---------------------------------------------------------------------------

BM54_DEFAULT_MAC = "0C:7F:ED:72:BC:40"
BEURER_CID = 0x0611
BP_SERVICE = "00001810-0000-1000-8000-00805f9b34fb"
BP_MEASUREMENT = "00002a35-0000-1000-8000-00805f9b34fb"
NAME_HINTS = ("BM54", "Beurer", "BM5")

log = logging.getLogger("bm54_pair_check")


def _setup_logging(verbose: bool) -> None:
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(
        level=level,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
        datefmt="%H:%M:%S",
    )
    # Quiet noisy deps unless -v
    if not verbose:
        logging.getLogger("bleak").setLevel(logging.WARNING)
        logging.getLogger("dbus_fast").setLevel(logging.WARNING)


# ---------------------------------------------------------------------------
# BlueZ helpers (subprocess + dbus_fast where available)
# ---------------------------------------------------------------------------

def _btctl(*args: str, timeout: float = 15.0) -> Tuple[int, str, str]:
    btctl = shutil.which("bluetoothctl")
    if not btctl:
        return 127, "", "bluetoothctl not found"
    proc = subprocess.run(
        [btctl, *args],
        capture_output=True,
        text=True,
        timeout=timeout,
    )
    return proc.returncode, (proc.stdout or "").strip(), (proc.stderr or "").strip()


def bluez_device_info(mac: str) -> dict:
    """Parse bluetoothctl info into a small dict."""
    mac = mac.strip().upper()
    rc, out, err = _btctl("info", mac)
    info = {
        "mac": mac,
        "found": rc == 0 and "not available" not in out.lower(),
        "paired": False,
        "bonded": False,
        "trusted": False,
        "connected": False,
        "name": "",
        "raw": out or err,
    }
    if not info["found"]:
        return info
    for line in (out or "").splitlines():
        s = line.strip()
        low = s.lower()
        if s.startswith("Name:"):
            info["name"] = s.split(":", 1)[1].strip()
        elif low.startswith("paired:"):
            info["paired"] = "yes" in low
        elif low.startswith("bonded:"):
            info["bonded"] = "yes" in low
        elif low.startswith("trusted:"):
            info["trusted"] = "yes" in low
        elif low.startswith("connected:"):
            info["connected"] = "yes" in low
    return info


def bluez_list_bm54() -> List[dict]:
    """List devices that look like BM54 / Beurer BP from bluetoothctl devices."""
    _, out, _ = _btctl("devices")
    found = []
    for line in (out or "").splitlines():
        # Device AA:BB:CC:DD:EE:FF Name
        parts = line.split(None, 2)
        if len(parts) < 2 or parts[0] != "Device":
            continue
        mac = parts[1].upper()
        name = parts[2] if len(parts) > 2 else ""
        if any(h.lower() in name.lower() for h in NAME_HINTS) or "BM" in name.upper():
            found.append(bluez_device_info(mac))
    return found


def bluez_remove(mac: str) -> bool:
    mac = mac.strip().upper()
    log.info("BlueZ disconnect + remove %s …", mac)
    _btctl("disconnect", mac, timeout=8)
    rc, out, err = _btctl("remove", mac, timeout=12)
    log.info("remove rc=%s out=%s err=%s", rc, out[:160], err[:160])
    info = bluez_device_info(mac)
    ok = not info["found"] or not info["paired"]
    if ok:
        log.info("OK — %s no longer paired on this Pi", mac)
    else:
        log.warning("Still listed: paired=%s bonded=%s", info["paired"], info["bonded"])
    return ok


def print_status(mac: Optional[str] = None) -> int:
    print("=" * 60)
    print("  BM54 / BlueZ bond status (this Pi)")
    print("=" * 60)
    rc, show, _ = _btctl("show")
    if rc == 0:
        for line in show.splitlines()[:8]:
            print(f"  {line}")
    print()
    targets = []
    if mac:
        targets.append(bluez_device_info(mac))
    else:
        targets = bluez_list_bm54()
        if not targets:
            # still show default lab MAC
            targets = [bluez_device_info(BM54_DEFAULT_MAC)]
    if not targets:
        print("  (no BM54-like devices in bluetoothctl)")
        return 0
    for info in targets:
        print(f"  MAC:       {info['mac']}")
        print(f"  Name:      {info.get('name') or '(unknown)'}")
        print(f"  Found:     {info['found']}")
        print(f"  Paired:    {info['paired']}")
        print(f"  Bonded:    {info['bonded']}")
        print(f"  Trusted:   {info['trusted']}")
        print(f"  Connected: {info['connected']}")
        print()
    print("  Tip: stale bond →  tools_bm54_pair_check.py remove -a <MAC>")
    print("  Journal errors seen on this Pi earlier:")
    print("    Pair device failed: Timeout")
    print("    No agent available for request type 1  (passkey agent missing)")
    return 0


# ---------------------------------------------------------------------------
# BLE scan / pair / sync
# ---------------------------------------------------------------------------

async def scan_bm54(timeout: float = 30.0) -> List[Any]:
    from bleak import BleakScanner

    print(f"Scanning {timeout:.0f}s for BM54 (wake cuff: M1/M2 or after measure)…")
    hits: dict[str, Any] = {}

    def _cb(device: Any, adv: Any) -> None:
        name = (device.name or getattr(adv, "local_name", None) or "") or ""
        mfg = getattr(adv, "manufacturer_data", None) or {}
        is_name = any(h.lower() in name.lower() for h in NAME_HINTS)
        is_cid = BEURER_CID in mfg
        if not (is_name or is_cid):
            return
        mac = device.address.upper()
        rssi = getattr(adv, "rssi", None) or getattr(device, "rssi", None)
        payload = mfg.get(BEURER_CID)
        passkey_hint = None
        if payload is not None:
            b = bytes(payload)
            # PDF / phone HCI: 01 03 = passkey-capable gen
            passkey_hint = len(b) >= 2 and b[0] == 0x01 and b[1] == 0x03
        hits[mac] = {
            "mac": mac,
            "name": name,
            "rssi": rssi,
            "mfg": bytes(payload).hex() if payload is not None else "",
            "passkey_hint": passkey_hint,
            "device": device,
        }

    scanner = BleakScanner(detection_callback=_cb)
    await scanner.start()
    try:
        t0 = time.monotonic()
        while time.monotonic() - t0 < timeout:
            await asyncio.sleep(0.4)
            if hits:
                # keep scanning a bit so RSSI stabilizes
                if time.monotonic() - t0 > min(8.0, timeout):
                    break
    finally:
        await scanner.stop()

    rows = sorted(hits.values(), key=lambda r: -(r["rssi"] or -999))
    if not rows:
        print("  No BM54 / Beurer CID 0x0611 advertisements.")
        print("  → Press M1 or M2 on the cuff (or finish a measurement) and retry.")
        return []
    print(f"  Found {len(rows)} device(s):")
    for i, r in enumerate(rows, 1):
        pk = r["passkey_hint"]
        pk_s = "passkey-capable" if pk else ("legacy?" if pk is False else "?")
        print(
            f"  [{i}] {r['mac']}  name={r['name']!r}  "
            f"rssi={r['rssi']}  mfg={r['mfg'] or '-'}  ({pk_s})"
        )
    return rows


def _install_live_passkey_prompt(broker: Any, *, timeout_s: float = 90.0) -> None:
    """
    BM54 does not reveal the passkey until SMP starts.

    When BlueZ calls RequestPasskey, the broker fires on_need → we print a
    loud prompt and read 6 digits from stdin (or accept a line already typed).
    """

    def _on_need() -> None:
        print(file=sys.stderr)
        print("=" * 60, file=sys.stderr)
        print("  ★ CUFF SHOULD NOW SHOW A 6-DIGIT PASSKEY ON THE LCD", file=sys.stderr)
        print("  ★ Type those 6 digits here and press Enter", file=sys.stderr)
        print(f"  ★ Waiting up to {timeout_s:.0f}s …", file=sys.stderr)
        print("=" * 60, file=sys.stderr)
        sys.stderr.flush()
        try:
            # Agent thread may call this; blocking read is intentional.
            line = sys.stdin.readline()
            if line and line.strip():
                broker.provide(line.strip())
                print(f"  Passkey accepted ({len(''.join(c for c in line if c.isdigit()))} digits).",
                      file=sys.stderr)
            else:
                print("  (empty input — pair will time out)", file=sys.stderr)
        except Exception as exc:  # noqa: BLE001
            print(f"  Passkey input failed: {exc}", file=sys.stderr)
        sys.stderr.flush()

    broker.set_on_need(_on_need)
    print()
    print("  Passkey is NOT known yet — that is normal.")
    print("  After connect/pair starts, look at the cuff LCD for 6 digits,")
    print("  then type them here when this script prompts.")
    print()


def _parse_blp_indication(payload: bytes) -> str:
    """
    Lightweight BLP 0x2A35 decode for the debug script (no parsers package init).

    Avoids: parsers → omron → brands → beurer cycle during pair-check.
    Full parser remains in medical_ble_toolkit.parsers.blood_pressure.
    """
    if len(payload) < 7:
        return f"short len={len(payload)} hex={payload.hex()}"
    flags = payload[0]
    # IEEE 11073 SFLOAT LE (mantissa 12-bit, exp 4-bit)
    def _sfloat(lo: int, hi: int) -> float:
        raw = lo | (hi << 8)
        if raw in (0x07FF, 0x0800, 0x07FE, 0x0801, 0x0802):
            return float("nan")
        mantissa = raw & 0x0FFF
        if mantissa & 0x0800:
            mantissa = mantissa - 0x1000
        exp = (raw >> 12) & 0x0F
        if exp & 0x8:
            exp = exp - 0x10
        return float(mantissa * (10**exp))

    sys = _sfloat(payload[1], payload[2])
    dia = _sfloat(payload[3], payload[4])
    o = 7
    ts = ""
    if flags & 0x02 and len(payload) >= o + 7:
        y = payload[o] | (payload[o + 1] << 8)
        ts = f" {y:04d}-{payload[o+2]:02d}-{payload[o+3]:02d}"
        ts += f" {payload[o+4]:02d}:{payload[o+5]:02d}:{payload[o+6]:02d}"
        o += 7
    pulse = None
    if flags & 0x04 and len(payload) >= o + 2:
        pulse = _sfloat(payload[o], payload[o + 1])
        o += 2
    user = None
    if flags & 0x08 and len(payload) >= o + 1:
        user = payload[o]
    parts = [f"sys={sys:.0f}", f"dia={dia:.0f}"]
    if pulse is not None and pulse == pulse:
        parts.append(f"pulse={pulse:.0f}")
    if user is not None:
        parts.append(f"user={user}")
    if ts:
        parts.append(f"t={ts.strip()}")
    return " ".join(parts)


async def run_session(
    mac: str,
    *,
    passkey: Optional[int],
    do_pair: bool,
    remove_first: bool,
    connect_timeout: float,
    listen_s: float,
    quiet_s: float,
    settle_s: float,
) -> int:
    from bleak import BleakClient

    mac = mac.strip().upper()
    print("=" * 60)
    print(f"  BM54 pair-check  mac={mac}")
    print(f"  pair={do_pair}  remove_first={remove_first}")
    if passkey is not None:
        print("  passkey=pre-set (optional; normally leave unset)")
    else:
        print("  passkey=live from cuff LCD after SMP starts (default)")
    print("=" * 60)

    before = bluez_device_info(mac)
    print(
        f"  Before: paired={before['paired']} bonded={before['bonded']} "
        f"trusted={before['trusted']}"
    )

    if remove_first:
        if before["found"] and (before["paired"] or before["bonded"]):
            bluez_remove(mac)
            await asyncio.sleep(1.0)
        else:
            log.info("Nothing to remove for %s", mac)

    # Optional: scan so operator knows cuff is advertising
    print()
    hits = await scan_bm54(timeout=12.0)
    if hits and mac not in {h["mac"] for h in hits}:
        log.warning(
            "Target %s not in last scan hits — connect may still work if bonded.",
            mac,
        )

    readings: list = []
    raw_count = 0
    last_notif = None
    auth_error = False
    pair_attempted = False
    connect_ok = False

    def on_notify(_char: Any, data: bytearray) -> None:
        nonlocal raw_count, last_notif
        raw_count += 1
        last_notif = time.monotonic()
        payload = bytes(data) if data is not None else b""
        log.info("[NOTIF] #%d len=%d hex=%s", raw_count, len(payload), payload.hex())
        try:
            brief = _parse_blp_indication(payload)
            readings.append(brief)
            log.info("[PARSE] %s", brief)
        except Exception as exc:  # noqa: BLE001
            log.warning("[PARSE] %s: %s", type(exc).__name__, exc)

    # Single agent for whole session when pairing (matches phone HCI order)
    from contextlib import asynccontextmanager

    passkey_timeout_s = 90.0

    @asynccontextmanager
    async def _maybe_agent():
        if do_pair and sys.platform.startswith("linux"):
            from medical_ble_toolkit.brands.omron.ble.bluez_agent import (
                GLOBAL_PASSKEY_BROKER,
                bluez_pair_agent,
                ensure_adapter_pairable,
            )

            await ensure_adapter_pairable()
            GLOBAL_PASSKEY_BROKER.reset(preset=passkey)
            # Live prompt when BlueZ actually asks for the LCD code
            if passkey is None:
                _install_live_passkey_prompt(
                    GLOBAL_PASSKEY_BROKER, timeout_s=passkey_timeout_s
                )
            async with bluez_pair_agent(
                passkey=passkey,
                broker=GLOBAL_PASSKEY_BROKER,
                wait_timeout_s=passkey_timeout_s,
            ) as ok:
                if not ok:
                    log.warning("Agent not ready — pair may fail")
                yield
        else:
            yield

    client: Optional[BleakClient] = None
    try:
        async with _maybe_agent():
            log.info("Connecting to %s (timeout=%.0fs)…", mac, connect_timeout)
            print("  → Watch the cuff: passkey appears only after pairing starts.")
            client = BleakClient(mac, timeout=connect_timeout)
            await client.connect()
            if not client.is_connected:
                log.error("Connect returned but not connected")
                return 2
            connect_ok = True
            log.info("Connected OK")

            if settle_s > 0:
                log.info("Post-connect settle %.1fs (BM54 te marker)…", settle_s)
                await asyncio.sleep(settle_s)

            # DIS (often readable pre-bond)
            for label, uuid in (
                ("model", "00002a24-0000-1000-8000-00805f9b34fb"),
                ("mfg", "00002a29-0000-1000-8000-00805f9b34fb"),
            ):
                try:
                    raw = await client.read_gatt_char(uuid)
                    text = bytes(raw).decode("utf-8", errors="replace").strip("\x00 ")
                    log.info("[DIS] %s=%r", label, text)
                except Exception as exc:
                    log.debug("[DIS] %s: %s", label, exc)

            # CCCD probe → expect 0x05 if unbonded (phone HCI)
            need_bond = False
            try:
                log.info("CCCD Indicate probe on %s …", BP_MEASUREMENT)
                await client.start_notify(BP_MEASUREMENT, on_notify)
                log.info("CCCD OK without re-pair (already bonded?)")
            except Exception as exc:  # noqa: BLE001
                msg = str(exc).lower()
                log.warning("CCCD probe failed: %s: %s", type(exc).__name__, exc)
                if any(
                    s in msg
                    for s in (
                        "insufficient authentication",
                        "0x05",
                        "0x0305",
                        "not permitted",
                        "authentication required",
                    )
                ):
                    need_bond = True
                    auth_error = True
                    log.info("ATT 0x05 — SMP / passkey required (matches phone HCI)")
                else:
                    raise

            if do_pair and (need_bond or not bluez_device_info(mac)["paired"]):
                pair_attempted = True
                log.info(
                    "Starting OS pair — look at cuff LCD for 6-digit code "
                    "(type it when prompted; do not need it in advance)…"
                )
                from medical_ble_toolkit.brands.omron.ble.bluez_agent import (
                    ensure_bluez_trusted,
                )
                from medical_ble_toolkit.brands.omron.ble.connection import (
                    _pair_strategies,
                )

                await ensure_bluez_trusted(mac)
                # Agent already registered in outer context — do NOT nest pair_client
                # When BlueZ needs the passkey, on_need prompts stdin (see above).
                await _pair_strategies(client)
                pair_attempted = True
                auth_error = False
                log.info("Bond complete — settle 1.2s then CCCD retry")
                await asyncio.sleep(1.2)

                # Retry CCCD
                try:
                    await client.start_notify(BP_MEASUREMENT, on_notify)
                    log.info("CCCD Indicate OK after bond")
                except Exception as exc:  # noqa: BLE001
                    log.error("CCCD after bond failed: %s", exc)
                    raise

            # Listen for auto dump
            log.info(
                "Listening max=%.0fs quiet=%.1fs (auto BP dump after Indicate)…",
                listen_s,
                quiet_s,
            )
            t0 = time.monotonic()
            while True:
                now = time.monotonic()
                if now - t0 >= listen_s:
                    log.info("Max listen reached")
                    break
                if not client.is_connected:
                    log.warning("Link dropped")
                    break
                if (
                    last_notif is not None
                    and readings
                    and (now - last_notif) >= quiet_s
                    and (now - t0) >= 2.0
                ):
                    log.info("Quiet %.1fs — dump complete", quiet_s)
                    break
                await asyncio.sleep(0.2)

    except Exception as exc:  # noqa: BLE001
        log.error("Session failed: %s: %s", type(exc).__name__, exc)
        auth_error = auth_error or "auth" in str(exc).lower() or "pair" in str(exc).lower()
        return 1
    finally:
        if client is not None:
            try:
                if client.is_connected:
                    await client.disconnect()
                    log.info("Disconnected")
            except Exception:
                pass

    # SMP often completes *inside* start_notify (not explicit pair()) — detect bond
    mid = bluez_device_info(mac)
    if mid.get("paired") or mid.get("bonded"):
        if not before.get("paired"):
            pair_attempted = True
        try:
            from medical_ble_toolkit.brands.omron.ble.bluez_agent import (
                ensure_bluez_trusted,
            )

            await ensure_bluez_trusted(mac)
        except Exception as exc:  # noqa: BLE001
            log.debug("ensure_bluez_trusted: %s", exc)

    after = bluez_device_info(mac)
    print()
    print("=" * 60)
    print("  RESULT")
    print("=" * 60)
    print(f"  connect_ok:     {connect_ok}")
    print(f"  pair_attempted: {pair_attempted}  (True if bond/SMP during CCCD)")
    print(f"  after paired:   {after['paired']}")
    print(f"  after bonded:   {after['bonded']}")
    print(f"  after trusted:  {after['trusted']}")
    print(f"  raw_indications:{raw_count}")
    print(f"  readings:       {len(readings)}")
    for i, r in enumerate(readings[:20], 1):
        print(f"    [{i}] {r}")
    if len(readings) > 20:
        print(f"    … +{len(readings) - 20} more")

    if readings:
        print("\n  ★ SUCCESS — bond + Indicate dump works on this path.")
        print("    Working sequence (use in hub):")
        print("      agent first → connect → settle → CCCD Indicate")
        print("      (passkey from LCD mid-CCCD) → listen quiet-end")
        print("    Link drop after dump is normal for BM54.")
        return 0
    if connect_ok and after["paired"] and not readings:
        print("\n  Bond may be OK but no BP data.")
        print("  → Take a measurement or press M1/M2 so the cuff advertises/dumps.")
        return 3
    if auth_error:
        print("\n  Auth/pair failed.")
        print(f"  → tools_bm54_pair_check.py remove -a {mac}")
        print("  → pair again; enter 6 digits when the cuff LCD shows them")
        return 4
    return 5


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="BM54 pairing diagnostic (BlueZ / Pi)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    p.add_argument("-v", "--verbose", action="store_true")
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("status", help="Show BlueZ bond state for BM54")
    s.add_argument("-a", "--address", default=None, help="MAC (default: scan names + lab MAC)")

    r = sub.add_parser("remove", help="Remove BM54 bond from this Pi")
    r.add_argument(
        "-a",
        "--address",
        default=BM54_DEFAULT_MAC,
        help=f"MAC (default {BM54_DEFAULT_MAC})",
    )

    sc = sub.add_parser("scan", help="Scan for BM54 advertisements")
    sc.add_argument("-t", "--timeout", type=float, default=30.0)

    for name, help_ in (
        ("pair", "Connect + passkey pair + CCCD + dump"),
        ("sync", "Connect + optional pair + CCCD + dump (alias of pair)"),
    ):
        x = sub.add_parser(name, help=help_)
        x.add_argument("-a", "--address", default=BM54_DEFAULT_MAC)
        x.add_argument(
            "--passkey",
            type=str,
            default=None,
            help=(
                "Optional: only if you already know the 6-digit LCD code. "
                "Normally omit — cuff shows it after pair starts; script prompts then."
            ),
        )
        x.add_argument(
            "--remove-first",
            action="store_true",
            help="bluetoothctl remove MAC before pairing (clean rebind)",
        )
        x.add_argument(
            "--no-pair",
            action="store_true",
            help="Skip OS pair (use existing bond)",
        )
        x.add_argument("--connect-timeout", type=float, default=25.0)
        x.add_argument("--listen", type=float, default=45.0, help="Max listen seconds")
        x.add_argument("--quiet", type=float, default=4.0, help="Quiet-end after last indication")
        x.add_argument(
            "--settle",
            type=float,
            default=3.0,
            help="Post-connect settle (BM54 te ≈ 3s)",
        )
    return p


def _parse_passkey(raw: Optional[str]) -> Optional[int]:
    if raw is None or not str(raw).strip():
        return None
    digits = "".join(c for c in str(raw) if c.isdigit())
    if not digits:
        raise SystemExit("--passkey must contain digits")
    return int(digits[-6:]) if len(digits) > 6 else int(digits)


def main(argv: Optional[List[str]] = None) -> int:
    args = build_parser().parse_args(argv)
    _setup_logging(args.verbose)

    if args.cmd == "status":
        return print_status(getattr(args, "address", None))

    if args.cmd == "remove":
        ok = bluez_remove(args.address)
        return 0 if ok else 1

    if args.cmd == "scan":
        rows = asyncio.run(scan_bm54(timeout=args.timeout))
        return 0 if rows else 1

    if args.cmd in ("pair", "sync"):
        pk = _parse_passkey(args.passkey)
        return asyncio.run(
            run_session(
                args.address,
                passkey=pk,
                do_pair=not args.no_pair,
                remove_first=args.remove_first,
                connect_timeout=args.connect_timeout,
                listen_s=args.listen,
                quiet_s=args.quiet,
                settle_s=args.settle,
            )
        )

    return 2


if __name__ == "__main__":
    sys.exit(main())

"""
BLE scanner for Omron HEM-7143T1 and nearby BLE devices.

Usage:
  1. Put Omron in pairing mode (hold Bluetooth button 3-5s until "P" flashes)
  2. python ble_scan.py
  3. Optional: python ble_scan.py --seconds 60
"""

from __future__ import annotations

import argparse
import asyncio
import re
import sys
from datetime import datetime

try:
    from bleak import BleakScanner
    from bleak.backends.device import BLEDevice
    from bleak.backends.scanner import AdvertisementData
except ImportError:
    print("Missing dependency. Run: python -m pip install -r requirements.txt")
    sys.exit(1)

# Names / patterns often seen on Omron BP monitors
OMRON_HINTS = re.compile(
    r"omron|ble.?smart|hem[-_ ]?7|blp|blood.?pressure",
    re.IGNORECASE,
)

# Omron / common BP-related company IDs or service UUIDs (hints only)
BLOOD_PRESSURE_SERVICE = "00001810-0000-1000-8000-00805f9b34fb"
DEVICE_INFO_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb"


def is_omron_like(name: str | None, uuids: list[str] | tuple[str, ...] | None) -> bool:
    if name and OMRON_HINTS.search(name):
        return True
    if uuids:
        for u in uuids:
            u_l = u.lower()
            if "1810" in u_l or "blood" in u_l:
                return True
    return False


def fmt_device(
    device: BLEDevice,
    adv: AdvertisementData,
    first_seen: datetime,
) -> str:
    name = device.name or adv.local_name or "(no name)"
    rssi = adv.rssi
    uuids = list(adv.service_uuids or [])
    mfg = adv.manufacturer_data or {}
    mfg_ids = ", ".join(f"0x{k:04X}" for k in mfg.keys()) or "-"
    uuid_short = ", ".join(u[-8:] if len(u) > 12 else u for u in uuids[:4]) or "-"
    flag = " *** OMRON/BP CANDIDATE ***" if is_omron_like(name, uuids) else ""
    return (
        f"{device.address:20}  RSSI={rssi:4}  name={name!r:30}  "
        f"mfg=[{mfg_ids}]  svc=[{uuid_short}]{flag}"
    )


async def run_scan(seconds: float, omron_only: bool) -> None:
    print("=" * 72)
    print("  BLE SCAN — looking for Omron HEM-7143T1 / nearby BLE devices")
    print("=" * 72)
    print()
    print("Before scanning:")
    print("  1. Turn on the Omron monitor")
    print("  2. Hold the Bluetooth/Transfer button 3-5 seconds")
    print("  3. Confirm display shows flashing 'P' (pairing mode)")
    print("  4. Keep device within ~1 meter of this PC")
    print()
    print(f"Scanning for {seconds:.0f}s... (Ctrl+C to stop early)")
    print("-" * 72)

    seen: dict[str, tuple[BLEDevice, AdvertisementData, datetime]] = {}
    lock = asyncio.Lock()

    def callback(device: BLEDevice, adv: AdvertisementData) -> None:
        # bleak may call from another thread context; queue updates carefully
        addr = device.address
        name = device.name or adv.local_name
        if omron_only and not is_omron_like(name, list(adv.service_uuids or [])):
            # Still track unnamed strong signals? No — skip if filter on
            if not (name and OMRON_HINTS.search(name)):
                return

        is_new = addr not in seen
        seen[addr] = (device, adv, seen[addr][2] if addr in seen else datetime.now())
        line = fmt_device(device, adv, seen[addr][2])
        if is_new:
            print(f"[NEW] {line}")
        else:
            # refresh line for stronger signal / name update
            if adv.rssi is not None and (device.name or adv.local_name):
                print(f"[upd] {line}")

    scanner = BleakScanner(detection_callback=callback)

    try:
        await scanner.start()
        await asyncio.sleep(seconds)
    except KeyboardInterrupt:
        print("\nStopped by user.")
    finally:
        await scanner.stop()

    print("-" * 72)
    print(f"Scan finished. Unique devices seen: {len(seen)}")
    print()

    candidates = [
        (addr, d, adv, t)
        for addr, (d, adv, t) in seen.items()
        if is_omron_like(d.name or adv.local_name, list(adv.service_uuids or []))
    ]

    if candidates:
        print("Likely Omron / BP candidates:")
        for addr, d, adv, t in sorted(candidates, key=lambda x: x[2].rssi or -999, reverse=True):
            print(" ", fmt_device(d, adv, t))
        print()
        print("Next step — connect and list GATT services:")
        best = max(candidates, key=lambda x: x[2].rssi or -999)
        print(f"  python ble_pair_connect.py {best[0]}")
    else:
        print("No clear Omron/BP candidate found.")
        print()
        print("Try again with:")
        print("  - Device in pairing mode (flashing P)")
        print("  - Fresh batteries, closer to PC")
        print("  - Temporarily disconnect phone Omron Connect (can monopolize pairing)")
        print("  - python ble_scan.py --seconds 90")
        if seen:
            print()
            print("All devices (for manual inspection):")
            for addr, (d, adv, t) in sorted(
                seen.items(), key=lambda kv: kv[1][1].rssi or -999, reverse=True
            ):
                print(" ", fmt_device(d, adv, t))


def main() -> None:
    parser = argparse.ArgumentParser(description="BLE scan for Omron HEM-7143T1")
    parser.add_argument(
        "--seconds",
        type=float,
        default=45.0,
        help="How long to scan (default: 45)",
    )
    parser.add_argument(
        "--omron-only",
        action="store_true",
        help="Only print devices that look like Omron/BP",
    )
    args = parser.parse_args()
    asyncio.run(run_scan(args.seconds, args.omron_only))


if __name__ == "__main__":
    main()

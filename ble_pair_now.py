"""
Aggressively find Omron and CONNECT with Windows pairing support.

Usage:
  1. Put device in P mode (flashing P)
  2. python ble_pair_now.py
"""

from __future__ import annotations

import argparse
import asyncio
import re
import sys
import traceback

try:
    from bleak import BleakClient, BleakScanner
except ImportError:
    print("Run: python -m pip install bleak")
    sys.exit(1)

OMRON_HINTS = re.compile(r"omron|ble.?smart|hem[-_ ]?7", re.I)
OMRON_MFG = 0x020E  # 526 decimal


async def wait_for_device(address: str | None, timeout: float):
    found_event = asyncio.Event()
    box = {"dev": None, "adv": None}

    def callback(device, adv):
        if found_event.is_set():
            return
        name = (device.name or adv.local_name or "") or ""
        addr = device.address.upper()
        mfg_keys = list((adv.manufacturer_data or {}).keys())
        match = False
        if address and addr == address.upper():
            match = True
        if OMRON_HINTS.search(name):
            match = True
        if OMRON_MFG in mfg_keys:
            match = True
        if match:
            print(f"[HIT] {addr}  RSSI={adv.rssi}  name={name!r}  mfg={mfg_keys}")
            box["dev"] = device
            box["adv"] = adv
            found_event.set()

    scanner = BleakScanner(detection_callback=callback)
    await scanner.start()
    try:
        await asyncio.wait_for(found_event.wait(), timeout=timeout)
    except asyncio.TimeoutError:
        return None
    finally:
        await scanner.stop()
    return box["dev"]


async def try_connect(device, attempt: int) -> bool:
    address = device.address
    print(f"\n--- Attempt {attempt}: connect to {address} ---")

    # Strategy A: plain connect with longer timeout
    try:
        print("Strategy A: BleakClient connect...")
        async with BleakClient(address, timeout=30.0) as client:
            print(f"  is_connected={client.is_connected}")
            # Try pair if available (Windows)
            if hasattr(client, "pair"):
                try:
                    print("  calling client.pair()...")
                    await client.pair(protection_level=2)
                    print("  pair() OK")
                except Exception as pe:
                    print(f"  pair() note: {type(pe).__name__}: {pe}")

            services = client.services
            if services:
                print("\nGATT services:")
                for service in services:
                    print(f"SERVICE  {service.uuid}  {service.description}")
                    for char in service.characteristics:
                        props = ",".join(char.properties)
                        print(f"  CHAR   {char.uuid}  [{props}]")
                print("\nSUCCESS")
                return True
            print("  Connected but no services yet")
    except Exception as exc:
        print(f"  Strategy A failed: {type(exc).__name__}: {exc!r}")
        traceback.print_exc()

    # Strategy B: connect via BLEDevice object
    try:
        print("Strategy B: BleakClient(device object)...")
        async with BleakClient(device, timeout=30.0) as client:
            print(f"  is_connected={client.is_connected}")
            services = client.services
            if services:
                for service in services:
                    print(f"SERVICE  {service.uuid}")
                    for char in service.characteristics:
                        print(f"  CHAR {char.uuid} [{','.join(char.properties)}]")
                print("\nSUCCESS")
                return True
    except Exception as exc:
        print(f"  Strategy B failed: {type(exc).__name__}: {exc!r}")

    return False


async def main(address: str | None, timeout: float) -> None:
    print("=" * 60)
    print("  IMMEDIATE PAIR — keep Omron flashing P")
    print("=" * 60)
    print(f"Target: {address or 'any Omron-like'}")
    print(f"Listen timeout: {timeout:.0f}s")
    print()

    device = await wait_for_device(address, timeout)
    if device is None:
        print("Timed out — not advertising. Re-enter P mode and retry.")
        sys.exit(2)

    print(f"Found device object: {device}")
    # brief pause not used — connect immediately
    for attempt in range(1, 4):
        ok = await try_connect(device, attempt)
        if ok:
            return
        print("Re-scanning for a fresh advertisement...")
        device = await wait_for_device(address, 15)
        if device is None:
            print("Lost advertisement. Put device in P mode again.")
            sys.exit(3)

    print("\nAll attempts failed.")
    print("Most common fix for Omron on Windows:")
    print("  1. On phone: Omron Connect → remove this monitor")
    print("  2. Phone Settings → Bluetooth → forget device")
    print("  3. PC: Settings → Bluetooth → Show device battery / remove any old Omron")
    print("  4. Toggle PC Bluetooth OFF then ON")
    print("  5. Put monitor in P again and re-run this script")
    sys.exit(1)


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--address", default="E1:99:7D:27:1C:0A")
    p.add_argument("--timeout", type=float, default=90.0)
    p.add_argument("--any-omron", action="store_true")
    args = p.parse_args()
    addr = None if args.any_omron else args.address
    asyncio.run(main(addr, args.timeout))

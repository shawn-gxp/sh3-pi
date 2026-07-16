"""
Keep watching for Omron for several minutes. Connect the INSTANT it advertises.
Re-enter P mode anytime while this is running.

  python ble_watch_and_connect.py
  python ble_watch_and_connect.py --minutes 3
"""

from __future__ import annotations

import argparse
import asyncio
import sys
import time
import traceback

try:
    from bleak import BleakClient, BleakScanner
except ImportError:
    print("pip install bleak")
    sys.exit(1)

OMRON_MFG = 0x020E
DEFAULT_ADDR = "E1:99:7D:27:1C:0A"


async def connect_fast(address: str) -> bool:
    print(f"\n>>> CONNECTING to {address} NOW <<<")
    try:
        # pair_before_connect can help on Windows with some devices
        client = BleakClient(address, timeout=15.0)
        # bleak 3.x: pair_before_connect is property set before connect
        try:
            client._pair_before_connect = True  # type: ignore
        except Exception:
            pass

        await client.connect()
        print(f"Connected: {client.is_connected}")

        if hasattr(client, "pair"):
            try:
                await client.pair()
                print("Paired OK")
            except Exception as e:
                print(f"pair() skip: {e}")

        services = client.services
        if services:
            print("\n=== GATT ===")
            for svc in services:
                print(f"SERVICE {svc.uuid} {svc.description}")
                for ch in svc.characteristics:
                    print(f"  CHAR {ch.uuid} [{','.join(ch.properties)}]")
            print("=== SUCCESS ===")
            await client.disconnect()
            return True

        print("Connected but no services")
        await client.disconnect()
        return False
    except Exception as e:
        print(f"Connect failed: {type(e).__name__}: {e}")
        traceback.print_exc()
        return False


async def run(address: str, minutes: float) -> None:
    deadline = time.time() + minutes * 60
    print("=" * 60)
    print("  WATCHING for Omron — re-enter P mode WHEN READY")
    print("=" * 60)
    print(f"Address: {address}")
    print(f"Window:  {minutes:.0f} minute(s)")
    print("When display shows flashing P, hold device next to PC.")
    print("This script connects the instant it sees the ad.")
    print("-" * 60)

    hit = asyncio.Event()
    box: dict = {}

    def on_adv(device, adv):
        if hit.is_set():
            return
        addr = device.address.upper()
        mfg = list((adv.manufacturer_data or {}).keys())
        name = (device.name or adv.local_name or "") or ""
        if addr != address.upper() and OMRON_MFG not in mfg and "BLESmart" not in name:
            return
        if address and addr != address.upper() and OMRON_MFG not in mfg:
            # still allow mfg omron even if address changed
            if OMRON_MFG not in mfg and "BLESmart" not in name and "omron" not in name.lower():
                return
        print(f"\n[HIT {time.strftime('%H:%M:%S')}] {addr} RSSI={adv.rssi} name={name!r} mfg={mfg}")
        box["address"] = addr
        hit.set()

    while time.time() < deadline:
        remaining = int(deadline - time.time())
        print(f"[{time.strftime('%H:%M:%S')}] listening... {remaining}s left  (put device in P mode)")
        hit.clear()
        box.clear()
        scanner = BleakScanner(detection_callback=on_adv)
        await scanner.start()
        try:
            # wait up to 10s chunks so we can print heartbeat
            try:
                await asyncio.wait_for(hit.wait(), timeout=10.0)
            except asyncio.TimeoutError:
                await scanner.stop()
                continue
        finally:
            try:
                await scanner.stop()
            except Exception:
                pass

        target = box.get("address", address)
        ok = await connect_fast(target)
        if ok:
            return
        print("Will keep watching if P mode still active / re-enter P...")
        await asyncio.sleep(0.3)

    print("\nTimed out. Device never stayed reachable for a full GATT connect.")
    sys.exit(1)


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--address", default=DEFAULT_ADDR)
    p.add_argument("--minutes", type=float, default=3.0)
    args = p.parse_args()
    asyncio.run(run(args.address, args.minutes))

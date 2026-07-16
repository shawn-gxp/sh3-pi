"""
Connect to a BLE device by address and list GATT services/characteristics.

Usage:
  python ble_pair_connect.py AA:BB:CC:DD:EE:FF
  python ble_pair_connect.py AA:BB:CC:DD:EE:FF --timeout 30
"""

from __future__ import annotations

import argparse
import asyncio
import sys

try:
    from bleak import BleakClient, BleakScanner
except ImportError:
    print("Missing dependency. Run: python -m pip install -r requirements.txt")
    sys.exit(1)


async def connect_and_inspect(address: str, timeout: float) -> None:
    print(f"Looking for device {address} ...")
    print("Tip: put Omron in pairing (P) or transfer mode before connecting.")
    print()

    device = await BleakScanner.find_device_by_address(address, timeout=timeout)
    if device is None:
        print(f"Device {address} not found in a {timeout:.0f}s scan.")
        print("Re-enter pairing mode (flashing P) and try again.")
        sys.exit(2)

    name = device.name or "(no name)"
    print(f"Found: {name}  [{device.address}]")
    print("Connecting...")

    async with BleakClient(device, timeout=timeout) as client:
        print(f"Connected: {client.is_connected}")
        print()
        print("GATT services / characteristics:")
        print("-" * 72)

        # bleak 0.22+: services is a property after connect
        services = client.services
        if services is None:
            print("No services discovered.")
            return

        for service in services:
            print(f"SERVICE  {service.uuid}  {service.description}")
            for char in service.characteristics:
                props = ",".join(char.properties)
                print(f"  CHAR   {char.uuid}  [{props}]  {char.description}")
                for desc in char.descriptors:
                    print(f"    DESC {desc.uuid}")
            print()

        # Try reading Blood Pressure Measurement if present (standard UUID)
        bp_char = "00002a35-0000-1000-8000-00805f9b34fb"
        try:
            for service in services:
                for char in service.characteristics:
                    if char.uuid.lower() == bp_char or "2a35" in char.uuid.lower():
                        if "notify" in char.properties or "indicate" in char.properties:
                            print(
                                f"Blood Pressure Measurement characteristic found: {char.uuid}"
                            )
                            print(
                                "This device may support standard BLE BP profile (notifications)."
                            )
                        elif "read" in char.properties:
                            data = await client.read_gatt_char(char.uuid)
                            print(f"Read BP char raw: {data.hex()}")
        except Exception as exc:
            print(f"(Optional BP read skipped: {exc})")

        print("-" * 72)
        print("Done. Disconnecting.")


def main() -> None:
    parser = argparse.ArgumentParser(description="Connect to BLE device and list GATT")
    parser.add_argument("address", help="Bluetooth address, e.g. AA:BB:CC:DD:EE:FF")
    parser.add_argument(
        "--timeout",
        type=float,
        default=25.0,
        help="Connect / scan timeout seconds (default: 25)",
    )
    args = parser.parse_args()
    try:
        asyncio.run(connect_and_inspect(args.address, args.timeout))
    except Exception as exc:
        print(f"Connection failed: {exc}")
        print()
        print("Common causes:")
        print("  - Device left pairing mode (re-enter P mode)")
        print("  - Already bonded exclusively to a phone app")
        print("  - Windows Bluetooth radio glitch (toggle BT off/on)")
        sys.exit(1)


if __name__ == "__main__":
    main()

"""
BLE discovery helpers.

Responsible only for finding devices (address / name / Omron mfg id).
Does not connect and does not know about EEPROM or parsers.
"""

from __future__ import annotations

import asyncio
import re
from dataclasses import dataclass
from typing import List, Optional

from bleak import BleakScanner
from bleak.backends.device import BLEDevice
from bleak.backends.scanner import AdvertisementData

from omron_bp.logging_config import DBG_TAG, get_logger

logger = get_logger("ble.scanner")

# Omron company identifier often present in manufacturer data
OMRON_MFG_ID = 0x020E
OMRON_NAME_HINT = re.compile(r"omron|ble.?smart|hem[-_ ]?\d", re.I)


@dataclass
class DiscoveredDevice:
    address: str
    name: str
    rssi: Optional[int]
    is_omron_like: bool
    manufacturer_ids: List[int]


def _is_omron_like(name: str, mfg_ids: List[int]) -> bool:
    if OMRON_MFG_ID in mfg_ids:
        return True
    if name and OMRON_NAME_HINT.search(name):
        return True
    return False


async def scan_devices(seconds: float = 8.0) -> List[DiscoveredDevice]:
    """Active scan; returns devices sorted by RSSI (strongest first)."""
    logger.info("Scanning BLE for %.0f s...", seconds)
    # DBG-LOG: raw discovery path
    logger.debug("%s BleakScanner.discover return_adv=True", DBG_TAG)

    found = await BleakScanner.discover(timeout=seconds, return_adv=True)
    results: List[DiscoveredDevice] = []

    for addr, (dev, adv) in found.items():
        name = dev.name or adv.local_name or "(no name)"
        mfg_ids = list((adv.manufacturer_data or {}).keys())
        item = DiscoveredDevice(
            address=addr,
            name=name,
            rssi=adv.rssi,
            is_omron_like=_is_omron_like(name, mfg_ids),
            manufacturer_ids=mfg_ids,
        )
        results.append(item)
        # DBG-LOG: one line per advertisement seen
        logger.debug(
            "%s seen addr=%s rssi=%s name=%r omron_like=%s mfg=%s",
            DBG_TAG,
            addr,
            adv.rssi,
            name,
            item.is_omron_like,
            mfg_ids,
        )

    results.sort(key=lambda d: d.rssi if d.rssi is not None else -9999, reverse=True)
    omron = sum(1 for d in results if d.is_omron_like)
    logger.info("Scan done: %d device(s), %d Omron-like", len(results), omron)
    return results


async def find_device_by_address(
    address: str, timeout: float = 60.0
) -> Optional[BLEDevice]:
    """
    Wait until a specific MAC/UUID advertises; return Bleak device or None.

    Omron transfer/P windows are short — we scan continuously and return on
    first hit, with progress logs every ~5s so the operator knows to wake the cuff.
    """
    target = address.upper()
    logger.info(
        "Waiting for cuff %s to advertise (up to %.0fs)...",
        address,
        timeout,
    )
    logger.info(
        "Action: short-press Bluetooth once (transfer) or hold for P (pair). Keep near PC."
    )
    # DBG-LOG
    logger.debug("%s find_device_by_address target=%s timeout=%s", DBG_TAG, address, timeout)

    found_event = asyncio.Event()
    box: dict = {"dev": None}

    def _cb(device: BLEDevice, adv: AdvertisementData) -> None:
        if found_event.is_set():
            return
        if device.address.upper() != target:
            return
        box["dev"] = device
        found_event.set()
        # DBG-LOG
        logger.debug(
            "%s advert hit name=%r rssi=%s",
            DBG_TAG,
            device.name or adv.local_name,
            adv.rssi,
        )

    scanner = BleakScanner(detection_callback=_cb)
    await scanner.start()
    try:
        deadline = asyncio.get_event_loop().time() + timeout
        # Progress every 5s
        while not found_event.is_set():
            remaining = deadline - asyncio.get_event_loop().time()
            if remaining <= 0:
                break
            wait = min(5.0, remaining)
            try:
                await asyncio.wait_for(found_event.wait(), timeout=wait)
            except asyncio.TimeoutError:
                logger.info(
                    "Still scanning… (%.0fs left) — press BT on the cuff now if not already.",
                    max(0.0, deadline - asyncio.get_event_loop().time()),
                )
    finally:
        try:
            await scanner.stop()
        except Exception as exc:
            # DBG-LOG
            logger.debug("%s scanner.stop ignored: %s", DBG_TAG, exc)

    device = box["dev"]
    if device is None:
        logger.warning("Device %s not advertising within %.0fs", address, timeout)
    else:
        logger.info("Found %s name=%r — connecting immediately", device.address, device.name)
        # DBG-LOG
        logger.debug("%s found object=%r", DBG_TAG, device)
    return device


async def pick_device_interactive(seconds: float = 8.0) -> str:
    """Scan, print a table, ask user for ID; returns selected address."""
    while True:
        devices = await scan_devices(seconds=seconds)
        if not devices:
            print("No BLE devices found. Retrying after Enter, or Ctrl+C to abort.")
            input()
            continue

        print()
        print(f"{'ID':>3}  {'RSSI':>5}  {'OMRON':>5}  {'ADDRESS':20}  NAME")
        print("-" * 72)
        for idx, d in enumerate(devices):
            flag = "YES" if d.is_omron_like else ""
            rssi = d.rssi if d.rssi is not None else "?"
            print(f"{idx:>3}  {rssi!s:>5}  {flag:>5}  {d.address:20}  {d.name}")
        print("-" * 72)
        raw = input("Enter ID (or blank to rescan): ").strip()
        if raw == "":
            continue
        if raw.isdigit() and int(raw) in range(len(devices)):
            chosen = devices[int(raw)].address
            # DBG-LOG
            logger.debug("%s user picked id=%s addr=%s", DBG_TAG, raw, chosen)
            return chosen
        print("Invalid ID.")

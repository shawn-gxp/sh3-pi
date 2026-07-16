"""
Windows-resilient BLE target resolution.

Critical bleak/WinRT behavior (bleak 3.x):
  - BleakClient("AA:BB:...") → connect() calls find_device_by_address() → STARTS SCANNER
  - BleakClient(BLEDevice(...)) → _device_info set from MAC → NO scanner

When the watcher is ABORTED/STOPPING, we must never pass a bare address string
into BleakClient. Always wrap as BLEDevice for direct FromBluetoothAddressAsync.
"""

from __future__ import annotations

import asyncio
import time
from typing import Optional, Union

from bleak import BleakScanner
from bleak.backends.device import BLEDevice
from bleak.backends.scanner import AdvertisementData

from omron_bp.logging_config import DBG_TAG, get_logger

logger = get_logger("ble.winrt_scan")

DeviceRef = Union[BLEDevice, str]


def is_scanner_busy_error(exc: BaseException) -> bool:
    msg = str(exc).lower()
    return any(
        s in msg
        for s in (
            "failed to start scanner",
            "watcher status",
            "stopping",
            "aborted",
            "bluetooth turned on",
            "radio",
        )
    )


def ble_device_from_address(address: str, name: str = "Omron") -> BLEDevice:
    """
    Build a BLEDevice that lets WinRT connect WITHOUT scanning.

    Omron FE4A cuffs use random addresses (phone HCI: Random).
    """
    addr = address.strip().upper()
    # details=None is fine: WinRT client only uses .address when BLEDevice is passed
    return BLEDevice(addr, name, None)


def as_connect_target(device: DeviceRef) -> BLEDevice:
    """Ensure we never hand a bare MAC string to BleakClient on Windows."""
    if isinstance(device, BLEDevice):
        return device
    return ble_device_from_address(str(device))


async def resolve_device(
    address: str,
    timeout: float = 90.0,
    *,
    min_hits: int = 1,
    allow_address_fallback: bool = True,
) -> BLEDevice:
    """
    Prefer a live-advert BLEDevice; if scanner is dead, return BLEDevice(MAC)
    so connect still avoids the scanner path.
    """
    target = address.upper().strip()
    device = await _try_callback_scan(target, timeout, min_hits=min_hits)
    if device is not None:
        return device

    device = await _try_discover(target, min(12.0, timeout))
    if device is not None:
        return device

    if allow_address_fallback:
        logger.warning(
            "Scanner unavailable / no advert — using direct BLEDevice(%s) "
            "(no WinRT scan on connect). Wake cuff (short-press BT / flashing P).",
            address,
        )
        return ble_device_from_address(address)

    raise ConnectionError(
        f"No live advert from {address} and address fallback disabled.\n"
        "Short-press BT or flashing P; Bluetooth On."
    )


async def _try_callback_scan(
    target: str,
    timeout: float,
    *,
    min_hits: int,
) -> Optional[BLEDevice]:
    for start_try in range(1, 3):  # only 2 tries — fail fast if radio is wedged
        hits: list[tuple[float, Optional[int], BLEDevice]] = []
        ready = asyncio.Event()

        def _cb(dev: BLEDevice, adv: AdvertisementData) -> None:
            if dev.address.upper() != target:
                return
            rssi = getattr(adv, "rssi", None)
            hits.append((time.monotonic(), rssi, dev))
            logger.info(
                "Live advert #%d from %s (rssi=%s)",
                len(hits),
                dev.address,
                rssi if rssi is not None else "?",
            )
            if len(hits) >= min_hits:
                ready.set()

        budget = min(timeout, 15.0) if start_try > 1 else timeout
        logger.info(
            "Live-scanning for %s (try %d/2, up to %.0fs)…",
            target,
            start_try,
            budget,
        )
        scanner = None
        try:
            try:
                scanner = BleakScanner(detection_callback=_cb, scanning_mode="active")
            except TypeError:
                scanner = BleakScanner(detection_callback=_cb)
            await scanner.start()
        except Exception as exc:
            logger.warning(
                "Scanner start failed (try %d/2): %s: %s",
                start_try,
                type(exc).__name__,
                exc,
            )
            if scanner is not None:
                try:
                    await scanner.stop()
                except Exception:
                    pass
            if start_try < 2 and is_scanner_busy_error(exc):
                await asyncio.sleep(2.0)
                continue
            return None  # skip further scan thrash → direct MAC

        try:
            deadline = time.monotonic() + budget
            while not ready.is_set():
                left = deadline - time.monotonic()
                if left <= 0:
                    break
                try:
                    await asyncio.wait_for(ready.wait(), timeout=min(5.0, left))
                except asyncio.TimeoutError:
                    logger.info(
                        "Still scanning… (%.0fs left, hits=%d)",
                        max(0.0, left),
                        len(hits),
                    )
        finally:
            try:
                await scanner.stop()
            except Exception as exc:
                logger.debug("%s scanner.stop: %s", DBG_TAG, exc)

        if hits:
            _t, rssi, device = hits[-1]
            logger.info(
                "Using live advert (hits=%d, last_rssi=%s)",
                len(hits),
                rssi if rssi is not None else "?",
            )
            return device

        await asyncio.sleep(0.5)

    return None


async def _try_discover(target: str, timeout: float) -> Optional[BLEDevice]:
    logger.info("Fallback discover() for %.0fs…", timeout)
    try:
        found = await BleakScanner.discover(timeout=timeout, return_adv=True)
    except Exception as exc:
        logger.warning("discover() failed: %s: %s", type(exc).__name__, exc)
        return None

    if isinstance(found, dict):
        for addr, pair in found.items():
            dev = pair[0] if isinstance(pair, tuple) else pair
            if str(getattr(dev, "address", addr)).upper() == target:
                logger.info("Found via discover(): %s", getattr(dev, "address", addr))
                return dev
    else:
        for dev in found:
            if dev.address.upper() == target:
                logger.info("Found via discover(): %s", dev.address)
                return dev
    return None

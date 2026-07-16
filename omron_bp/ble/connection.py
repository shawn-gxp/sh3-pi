"""
Single place for Omron BLE connect / pair primitives (Windows-safe).

WHY THIS EXISTS
---------------
bleak on Windows (WinRT):
  BleakClient("AA:BB:CC:...")  → connect() calls find_device_by_address()
                                 → ALWAYS starts BluetoothLEAdvertisementWatcher
  BleakClient(BLEDevice(...)) → uses FromBluetoothAddressAsync(int)
                                 → NO scanner

When the watcher is stuck (ABORTED / STOPPING), any code path that starts a
scanner fails in <100ms with "Failed to start scanner". That is a *scanner*
bug/state, not the cuff.

RULE: If the operator already gave us a MAC (interactive CLI always does),
      NEVER start a scanner. Build BLEDevice(mac) and connect directly.
"""

from __future__ import annotations

import asyncio
import re
import sys
from typing import Optional, Union

from bleak import BleakClient
from bleak.backends.device import BLEDevice

from omron_bp.logging_config import DBG_TAG, get_logger

logger = get_logger("ble.connection")

_IS_WINDOWS = sys.platform == "win32"
_MAC_RE = re.compile(r"^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")


def is_mac_address(value: str) -> bool:
    return bool(_MAC_RE.match((value or "").strip()))


def ble_device(address: str, name: str = "Omron") -> BLEDevice:
    """MAC → BLEDevice so WinRT skips advertisement watcher on connect."""
    return BLEDevice(address.strip().upper(), name, None)


def _winrt_kwargs(address_type: Optional[str] = None) -> dict:
    if not _IS_WINDOWS:
        return {}
    # Omron phone HCI: Random. None = let WinRT decide (often best).
    if address_type is None:
        return {}
    return {"winrt": {"address_type": address_type}}


async def connect_client(
    address: str,
    *,
    timeout: float = 30.0,
    pair_before_connect: bool = False,
    name: str = "Omron",
) -> BleakClient:
    """
    Connect without scanning.

    Tries address_type: None → random → public (Windows only).
    """
    if not is_mac_address(address):
        raise ValueError(f"Expected Bluetooth MAC, got {address!r}")

    device = ble_device(address, name=name)
    types_to_try: list[Optional[str]] = [None]
    if _IS_WINDOWS:
        types_to_try = [None, "random", "public"]

    last_err: Optional[BaseException] = None
    for atype in types_to_try:
        kwargs: dict = {
            "timeout": timeout,
            "pair": pair_before_connect,
            **_winrt_kwargs(atype),
        }
        client = BleakClient(device, **kwargs)
        label = atype or "default"
        try:
            logger.info(
                "BLE connect %s via BLEDevice (no scan), address_type=%s …",
                device.address,
                label,
            )
            await client.connect()
            if client.is_connected:
                logger.info("BLE connected (%s)", label)
                return client
        except Exception as exc:
            last_err = exc
            logger.warning(
                "BLE connect failed address_type=%s: %s: %s",
                label,
                type(exc).__name__,
                exc,
            )
            try:
                if client.is_connected:
                    await client.disconnect()
            except Exception:
                pass
            await asyncio.sleep(0.3)

    assert last_err is not None
    raise last_err


def _is_already_paired_error(exc: BaseException) -> bool:
    msg = str(exc).lower()
    return any(
        s in msg
        for s in (
            "already paired",
            "already bonded",
            "device is already paired",
            "pairing already",
        )
    )


def is_pair_failed_error(exc: BaseException) -> bool:
    """WinRT often raises BleakError: Could not pair with device: FAILED."""
    msg = str(exc).lower()
    return (
        "could not pair" in msg
        or "pair with device" in msg
        or "pairing failed" in msg
        or "pair failed" in msg
        or (": failed" in msg and "pair" in msg)
    )


async def pair_client(client: BleakClient) -> None:
    """
    OS-level pair/bond after connect.

    Windows / bleak notes:
      - protection_level=2 (auth+encrypt) often FAILS on Just-Works Omron
      - try level 2 → plain pair() → level 1
      - FAILED in <100ms usually means stale bond or cuff not in flashing P
      - "already paired" is success
    """
    if not client.is_connected:
        raise ConnectionError("Cannot pair: not connected")

    strategies: list[tuple[str, dict]] = [
        ("protection_level=2", {"protection_level": 2}),
        ("default", {}),
        ("protection_level=1", {"protection_level": 1}),
    ]
    last_err: Optional[BaseException] = None

    for label, kwargs in strategies:
        logger.info("BLE pair() attempt strategy=%s …", label)
        try:
            await client.pair(**kwargs)
            logger.info("BLE pair() finished OK (strategy=%s)", label)
            return
        except TypeError:
            # Older bleak: pair() takes no kwargs
            if kwargs:
                continue
            try:
                await client.pair()
                logger.info("BLE pair() finished OK (no-kwargs)")
                return
            except Exception as exc:
                last_err = exc
                if _is_already_paired_error(exc):
                    logger.info("BLE already paired: %s", exc)
                    return
                logger.warning(
                    "BLE pair() failed strategy=%s: %s: %s",
                    label,
                    type(exc).__name__,
                    exc,
                )
        except Exception as exc:
            last_err = exc
            if _is_already_paired_error(exc):
                logger.info("BLE already paired: %s", exc)
                return
            logger.warning(
                "BLE pair() failed strategy=%s: %s: %s",
                label,
                type(exc).__name__,
                exc,
            )
            # On immediate FAILED, remaining strategies rarely help on same link
            if is_pair_failed_error(exc) and label == "protection_level=2":
                # still try plain pair once — some stacks reject level 2 only
                continue
            if is_pair_failed_error(exc):
                break

    assert last_err is not None
    raise last_err


async def fe4a_visible(client: BleakClient, parent_uuid: str = "") -> bool:
    """True if Omron parent FE4A (or given) service is in the GATT cache."""
    wanted = (parent_uuid or "0000fe4a-0000-1000-8000-00805f9b34fb").lower()
    try:
        services = client.services
        if services is None:
            return False
        return any(s.uuid.lower() == wanted for s in services)
    except Exception:
        return False


async def disconnect_client(client: Optional[BleakClient]) -> None:
    if client is None:
        return
    try:
        if client.is_connected:
            await client.disconnect()
            logger.info("BLE disconnected")
    except Exception as exc:
        logger.debug("%s disconnect: %s", DBG_TAG, exc)


async def unpair_address(address: str) -> None:
    """
    Best-effort OS unpair without scanning.

    Tries bleak unpair while connected and while disconnected; Windows often
    needs the device removed from Settings if both fail.
    """
    logger.info("BLE unpair %s …", address)
    client: Optional[BleakClient] = None
    try:
        device = ble_device(address)
        # Prefer random address type for modern Omron
        for atype in (None, "random", "public") if _IS_WINDOWS else (None,):
            try:
                kwargs: dict = {"timeout": 15.0, **_winrt_kwargs(atype)}
                client = BleakClient(device, **kwargs)
                # Try unpair without connect first (WinRT sometimes allows this)
                if hasattr(client, "unpair"):
                    try:
                        await client.unpair()
                        logger.info("BLE unpair OK (no-connect, type=%s)", atype or "default")
                        await asyncio.sleep(1.5)
                        return
                    except Exception as exc:
                        logger.debug(
                            "unpair without connect failed type=%s: %s",
                            atype,
                            exc,
                        )
                # Connect then unpair
                try:
                    await client.connect()
                except Exception as exc:
                    logger.debug("unpair-connect failed type=%s: %s", atype, exc)
                    try:
                        await client.disconnect()
                    except Exception:
                        pass
                    client = None
                    continue
                if hasattr(client, "unpair"):
                    await client.unpair()
                    logger.info("BLE unpair OK (after connect, type=%s)", atype or "default")
                    await asyncio.sleep(1.5)
                    return
                logger.warning(
                    "unpair() not available — remove device in Windows Settings"
                )
                return
            except Exception as exc:
                logger.debug("unpair path type=%s: %s", atype, exc)
            finally:
                await disconnect_client(client)
                client = None
        logger.warning(
            "BLE unpair failed for %s — remove the device manually: "
            "Settings → Bluetooth & devices → remove Omron/BLESmart entry",
            address,
        )
    except Exception as exc:
        logger.warning("BLE unpair failed: %s — remove device in Windows Settings", exc)
    await asyncio.sleep(1.5)

"""
Single place for Omron BLE connect / pair primitives (Windows + Linux).

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

On Linux (BlueZ), BLEDevice and address-string both work; we still prefer
BLEDevice for a consistent no-scan path when the operator gave a MAC.

RULE: If the operator already gave us a MAC (interactive CLI always does),
      NEVER start a scanner. Build BLEDevice(mac) and connect directly.
"""

from __future__ import annotations

import asyncio
import re
import shutil
import subprocess
import sys
from typing import Optional, Union

from bleak import BleakClient
from bleak.backends.device import BLEDevice

from medical_ble_toolkit.brands.omron.logging_config import DBG_TAG, get_logger

logger = get_logger("ble.connection")

_IS_WINDOWS = sys.platform == "win32"
_IS_LINUX = sys.platform.startswith("linux")
_MAC_RE = re.compile(r"^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")


def is_mac_address(value: str) -> bool:
    return bool(_MAC_RE.match((value or "").strip()))


def ble_device(address: str, name: str = "Omron") -> BLEDevice:
    """
    MAC → BLEDevice so WinRT skips advertisement watcher on connect.

    On Linux BlueZ, do NOT use this for BleakClient(): details=None crashes
    with TypeError (details["path"]). Pass the MAC string instead.
    """
    return BLEDevice(address.strip().upper(), name, None)


def connect_target(address: str, name: str = "Omron") -> Union[BLEDevice, str]:
    """
    Platform-correct BleakClient target.

    - Windows: BLEDevice(mac) → FromBluetoothAddressAsync (no scanner)
    - Linux:   plain MAC string → BlueZ resolves by address
               (BLEDevice requires details['path'] from a real scan)
    """
    mac = address.strip().upper()
    if _IS_WINDOWS:
        return ble_device(mac, name=name)
    return mac


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
    Linux BlueZ: MAC string (not fake BLEDevice).
    """
    if not is_mac_address(address):
        raise ValueError(f"Expected Bluetooth MAC, got {address!r}")

    target = connect_target(address, name=name)
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
        client = BleakClient(target, **kwargs)
        label = atype or "default"
        try:
            logger.info(
                "BLE connect %s via %s, address_type=%s …",
                address.strip().upper(),
                "BLEDevice" if _IS_WINDOWS else "MAC string (BlueZ)",
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
    """True when OS pairing rejected the bond (WinRT FAILED / BlueZ auth)."""
    msg = str(exc).lower()
    return (
        "could not pair" in msg
        or "pair with device" in msg
        or "pairing failed" in msg
        or "pair failed" in msg
        or "authenticationfailed" in msg
        or "authentication failed" in msg
        or "authenticationcanceled" in msg
        or "authentication canceled" in msg
        or "authentication cancelled" in msg
        or (": failed" in msg and "pair" in msg)
    )


async def _pair_strategies(client: BleakClient) -> None:
    """Try Windows protection levels then plain pair()."""
    if not client.is_connected:
        raise ConnectionError("Cannot pair: not connected")

    # On Linux BlueZ, protection_level is ignored — try plain pair first.
    if _IS_LINUX:
        strategies: list[tuple[str, dict]] = [
            ("default", {}),
            ("protection_level=1", {"protection_level": 1}),
            ("protection_level=2", {"protection_level": 2}),
        ]
    else:
        strategies = [
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
            # Keep trying other strategies on BlueZ auth failures
            if is_pair_failed_error(exc):
                continue

    assert last_err is not None
    raise last_err


async def pair_client(
    client: BleakClient,
    *,
    passkey: Optional[int] = None,
    use_passkey_agent: bool = False,
) -> None:
    """
    OS-level pair/bond after connect.

    Windows:
      - protection_level=2 (auth+encrypt) often FAILS on Just-Works Omron
      - try level 2 → plain pair() → level 1
    Linux BlueZ:
      - Requires a pair *agent*. Without one, Pair returns
        AuthenticationFailed / AuthenticationCanceled.
      - Just Works (default): NoInputNoOutput agent
      - Passkey devices (Beurer BM54): KeyboardDisplay agent + passkey /
        PasskeyBroker (UI can supply 6-digit code from cuff LCD)
    """
    if not client.is_connected:
        raise ConnectionError("Cannot pair: not connected")

    addr = getattr(client, "address", "") or ""

    if _IS_LINUX:
        from medical_ble_toolkit.brands.omron.ble.bluez_agent import (
            GLOBAL_PASSKEY_BROKER,
            bluez_pair_agent,
            ensure_adapter_pairable,
            ensure_bluez_trusted,
        )

        await ensure_adapter_pairable()
        await ensure_bluez_trusted(addr)
        pk_mode = use_passkey_agent or passkey is not None
        broker = GLOBAL_PASSKEY_BROKER if pk_mode else None
        async with bluez_pair_agent(passkey=passkey, broker=broker) as agent_ok:
            if agent_ok:
                if pk_mode:
                    logger.info(
                        "Pairing with BlueZ KeyboardDisplay agent "
                        "(passkey %s)…",
                        "pre-set" if passkey is not None else "await UI",
                    )
                else:
                    logger.info("Pairing with BlueZ Just-Works agent active…")
            else:
                logger.warning(
                    "No BlueZ agent — pair may fail with AuthenticationFailed. "
                    "Run: bluetoothctl → agent KeyboardDisplay → default-agent"
                )
            await _pair_strategies(client)
        return

    await _pair_strategies(client)


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


def _bluez_remove(address: str) -> bool:
    """Linux: remove bond via bluetoothctl (best-effort)."""
    mac = address.strip().upper()
    btctl = shutil.which("bluetoothctl")
    if not btctl:
        return False
    try:
        # disconnect first if linked, then remove bond from BlueZ cache
        for args in (
            [btctl, "disconnect", mac],
            [btctl, "remove", mac],
        ):
            proc = subprocess.run(
                args,
                capture_output=True,
                text=True,
                timeout=12,
            )
            logger.debug(
                "bluez %s → rc=%s out=%s err=%s",
                " ".join(args[1:]),
                proc.returncode,
                (proc.stdout or "").strip()[:120],
                (proc.stderr or "").strip()[:120],
            )
        # Success if device no longer listed as paired
        listed = subprocess.run(
            [btctl, "devices"],
            capture_output=True,
            text=True,
            timeout=8,
        )
        out = (listed.stdout or "").upper()
        if mac not in out:
            logger.info("BLE unpair OK via bluetoothctl remove %s", mac)
            return True
        # still listed — treat remove exit as soft success if it printed Device has been removed
        return True
    except Exception as exc:
        logger.debug("bluetoothctl remove failed: %s", exc)
        return False


async def unpair_address(address: str) -> None:
    """
    Best-effort OS unpair without scanning.

    Tries bleak unpair; on Linux also bluetoothctl remove. Windows often
    needs the device removed from Settings if bleak unpair fails.
    """
    logger.info("BLE unpair %s …", address)
    mac = address.strip().upper()

    # Linux BlueZ: bluetoothctl is the most reliable bond removal
    if _IS_LINUX:
        ok = await asyncio.to_thread(_bluez_remove, mac)
        if ok:
            await asyncio.sleep(1.0)
            return

    client: Optional[BleakClient] = None
    try:
        target = connect_target(address)
        # Prefer random address type for modern Omron on Windows
        for atype in (None, "random", "public") if _IS_WINDOWS else (None,):
            try:
                kwargs: dict = {"timeout": 15.0, **_winrt_kwargs(atype)}
                client = BleakClient(target, **kwargs)
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
                    "unpair() not available — remove device manually "
                    "(Windows Settings or bluetoothctl remove %s)",
                    mac,
                )
                return
            except Exception as exc:
                logger.debug("unpair path type=%s: %s", atype, exc)
            finally:
                await disconnect_client(client)
                client = None
        if _IS_LINUX:
            logger.warning(
                "BLE unpair failed for %s — try: bluetoothctl remove %s",
                address,
                mac,
            )
        else:
            logger.warning(
                "BLE unpair failed for %s — remove the device manually: "
                "Settings → Bluetooth & devices → remove Omron/BLESmart entry",
                address,
            )
    except Exception as exc:
        logger.warning(
            "BLE unpair failed: %s — remove bond manually "
            "(Windows Settings or bluetoothctl remove %s)",
            exc,
            mac,
        )
    await asyncio.sleep(1.5)

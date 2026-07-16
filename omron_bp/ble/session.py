"""
BLE session for Omron protocol (after link is up).

Connect uses omron_bp.ble.connection — never bare-MAC BleakClient (that
starts a broken WinRT scanner when the watcher is ABORTED).
"""

from __future__ import annotations

import asyncio
from typing import Callable, Optional

from bleak import BleakClient

from omron_bp.ble.connection import (
    connect_client,
    disconnect_client,
    is_pair_failed_error,
    pair_client,
)
from omron_bp.logging_config import DBG_TAG, get_logger
from omron_bp.models.base import DeviceProfile, PairingMode

logger = get_logger("ble.session")


class BondIncompleteError(ConnectionError):
    """Raised when FE4A is visible but encrypted CCCD/notify is denied."""


class BleSession:
    def __init__(
        self,
        address: str,
        profile: DeviceProfile,
        *,
        connect_timeout: float = 30.0,
        find_timeout: float = 60.0,  # kept for API compat; scan not used
        pair_on_connect: bool = False,
        skip_scan: bool = True,  # default: never scan when MAC known
    ) -> None:
        self.address = address
        self.profile = profile
        self.connect_timeout = connect_timeout
        self.find_timeout = find_timeout
        self.pair_on_connect = pair_on_connect
        self.skip_scan = skip_scan
        self._client: Optional[BleakClient] = None
        self.pair_ok: bool = False
        self.pair_error: Optional[str] = None

    @property
    def client(self) -> BleakClient:
        if self._client is None or not self._client.is_connected:
            raise RuntimeError("BLE session is not connected")
        return self._client

    @property
    def is_connected(self) -> bool:
        return self._client is not None and self._client.is_connected

    async def __aenter__(self) -> "BleSession":
        await self.connect()
        return self

    async def __aexit__(self, exc_type, exc, tb) -> None:
        await self.disconnect()

    async def connect(self) -> None:
        logger.info(
            "Connecting to %s (direct BLEDevice, no advertisement scan)…",
            self.address,
        )
        logger.debug(
            "%s connect timeout=%s pair_on_connect=%s",
            DBG_TAG,
            self.connect_timeout,
            self.pair_on_connect,
        )

        # Always direct connect — MAC is known from CLI/config.
        # Scanning is the failure mode on this Windows machine.
        self._client = await connect_client(
            self.address,
            timeout=self.connect_timeout,
            pair_before_connect=False,
            name=self.profile.model_id or "Omron",
        )
        logger.info("Connected: True")

        # WinRT: let GattSession settle before CCCD (reduces instant CLOSED)
        await asyncio.sleep(0.45)

        # CRITICAL (Windows / HEM-7143T1):
        # Do NOT call pair() on every READ. When the OS already has a partial/stale
        # bond, pair() returns FAILED in ~30ms and WinRT often closes GattSession
        # (ACTIVE→CLOSED, PDU 67→23). Bond once via dedicated PAIR; READ uses the
        # existing OS bond automatically.
        if self.pair_on_connect:
            await self._ensure_encryption()
        elif self.profile.pairing_mode == PairingMode.OS_BONDING:
            logger.info(
                "Skipping pair() on connect (read path). "
                "Use: python -m medical_ble_toolkit omron pair … once if notify fails."
            )

        if not self.is_connected:
            raise ConnectionError(
                "Link dropped right after connect. Short-press BT (transfer) and retry."
            )

        await self._wait_for_parent_service()

        if not self.is_connected:
            raise ConnectionError(
                "Link dropped after FE4A discovery. Short-press BT (transfer) and retry."
            )

        # Brief settle — encryption from an existing OS bond often lags service list
        await asyncio.sleep(0.4)

        if not self.is_connected:
            raise ConnectionError(
                "Link dropped during settle (cuff left transfer window). "
                "Short-press BT and retry immediately."
            )

        if self.profile.pairing_mode == PairingMode.OS_BONDING:
            await self._probe_encrypted_notify()

        logger.info("Link ready for protocol.")

    async def _ensure_encryption(self) -> None:
        if not self.is_connected:
            return
        logger.info("Ensuring BLE encryption (OS bond)…")
        self.pair_ok = False
        self.pair_error = None
        try:
            await pair_client(self.client)
            self.pair_ok = True
            logger.info("OS pair/bond OK")
        except Exception as exc:
            self.pair_error = f"{type(exc).__name__}: {exc}"
            # Windows often returns FAILED when already bonded — may still encrypt.
            # We continue and probe CCCD; if probe fails we raise BondIncompleteError.
            if is_pair_failed_error(exc):
                logger.warning(
                    "pair() FAILED (common if already bonded or bond is stale). "
                    "Will probe encrypted notify next. detail=%s",
                    exc,
                )
            else:
                logger.info("pair() note (continuing): %s: %s", type(exc).__name__, exc)
        if not self.is_connected:
            raise ConnectionError(
                "Link dropped during encryption/pair. "
                "Run PAIR once in flashing P, then READ in transfer mode."
            )

    async def _probe_encrypted_notify(self) -> None:
        """
        FE4A can appear without a usable bond. Probing CCCD early fails fast
        with a clear message instead of dying mid token-unlock as 'Not connected'.
        """
        if not self.is_connected:
            # Retryable link loss (not a permanent bond flag)
            raise ConnectionError(
                "Disconnected before encryption probe. Short-press BT (transfer) and retry."
            )
        rx = list(self.profile.rx_channel_uuids)
        if not rx:
            return
        uuid = rx[0]
        logger.info("Probing encrypted CCCD on RX %s …", uuid)

        def _noop(_char, _data: bytearray) -> None:
            return

        try:
            await self.client.start_notify(uuid, _noop)
            await asyncio.sleep(0.05)
            try:
                await self.client.stop_notify(uuid)
            except Exception:
                pass
            logger.info("Encrypted notify probe OK — bond usable")
            self.pair_ok = True
        except Exception as exc:
            msg = str(exc).lower()
            canceled = (
                "canceled" in msg
                or "cancelled" in msg
                or "0x800704c7" in msg
                or "-2147023673" in msg
            )
            not_conn = "not connected" in msg or not self.is_connected
            logger.error(
                "Encrypted notify probe FAILED: %s: %s",
                type(exc).__name__,
                exc,
            )
            raise BondIncompleteError(
                "Omron link is up and FE4A is visible, but Windows cannot enable "
                "encrypted notifications (bond incomplete or not in transfer mode). "
                f"probe_error={type(exc).__name__}: {exc}. "
                "Fix: (1) SHORT-press BT for transfer mode, "
                "(2) if still failing: Windows Bluetooth → remove cuff, "
                "unpair phone OMRON Connect, RE-PAIR with flashing P, "
                "accept Windows dialog, then READ again."
                + (" [WinRT canceled/auth]" if canceled or not_conn else "")
            ) from exc

    async def _wait_for_parent_service(
        self, attempts: int = 80, delay_s: float = 0.1
    ) -> None:
        """
        Wait for FE4A (or profile parent). WinRT may populate services late or
        after a 'services changed' event — re-read cache each tick.
        """
        wanted = self.profile.parent_service_uuid.lower()
        logger.debug("%s wait parent %s", DBG_TAG, wanted)
        last_seen: list[str] = []

        for i in range(attempts):
            if not self.is_connected:
                raise ConnectionError(
                    "Disconnected while waiting for GATT services. "
                    "Short-press BT (transfer) and retry immediately "
                    "(do not wait for a long scan first)."
                )
            try:
                services = self.client.services
                # Periodic force-refresh if API available (bleak backend dependent)
                if i in (5, 15, 30) or services is None or (
                    hasattr(services, "__len__") and len(list(services)) == 0
                ):
                    get_services = getattr(self.client, "get_services", None)
                    if callable(get_services):
                        try:
                            refreshed = get_services()
                            if asyncio.iscoroutine(refreshed):
                                services = await refreshed
                            elif refreshed is not None:
                                services = refreshed
                        except Exception as exc:
                            logger.debug("%s get_services: %s", DBG_TAG, exc)

                if services is None:
                    await asyncio.sleep(delay_s)
                    continue
                uuids = [s.uuid.lower() for s in services]
                last_seen = list(uuids)
                if wanted in uuids:
                    logger.info(
                        "Parent service present: %s",
                        self.profile.parent_service_uuid,
                    )
                    return
                if i and i % 20 == 0:
                    logger.info(
                        "Still waiting for FE4A… seen %d service(s) so far",
                        len(uuids),
                    )
            except Exception as exc:
                logger.debug("%s services inspect: %s", DBG_TAG, exc)
            await asyncio.sleep(delay_s)

        logger.error("GATT services seen (no FE4A): %s", last_seen)
        raise OSError(
            f"Omron parent service not found: {self.profile.parent_service_uuid}. "
            f"GATT had {len(last_seen)} service(s) without FE4A. "
            "If the cuff was advertising (BLESmart…), transfer mode is OK and "
            "the Windows bond is incomplete. "
            "Fix: remove cuff in Windows Bluetooth → unpair phone OMRON Connect → "
            "RE-PAIR with flashing P → accept dialog → short-press BT → READ. "
            "Also: do not run a long scan before connect (transfer window is short)."
        )

    async def enable_notify_now(self, callback: Callable) -> list:
        uuids = list(self.profile.rx_channel_uuids)
        for uuid in uuids:
            if not self.is_connected:
                raise ConnectionError("Not connected when enabling notify")
            await self.client.start_notify(uuid, callback)
            logger.info("Notify ON: %s", uuid)
        return uuids

    async def pair_os(self) -> None:
        logger.info("Requesting OS-level BLE pairing/bonding...")
        await self._ensure_encryption()
        logger.info("OS pairing request finished.")

    async def disconnect(self) -> None:
        await disconnect_client(self._client)
        self._client = None

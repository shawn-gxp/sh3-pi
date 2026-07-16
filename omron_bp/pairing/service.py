"""
Pair workflow — OS bond (modern) or classic unlock key.

Uses ble.connection only (no advertisement scanner).
"""

from __future__ import annotations

import asyncio

from omron_bp.ble.connection import (
    connect_client,
    disconnect_client,
    fe4a_visible,
    is_pair_failed_error,
    pair_client,
    unpair_address,
)
from omron_bp.ble.session import BleSession
from omron_bp.ble.transport import OmronTransport
from omron_bp.logging_config import DBG_TAG, get_logger
from omron_bp.models.base import DEFAULT_UNLOCK_KEY, DeviceProfile, PairingMode

logger = get_logger("pairing.service")


async def _os_bond_once(
    address: str,
    profile: DeviceProfile,
    *,
    attempt_label: str,
) -> None:
    """
    Connect + OS pair for modern FE4A.

    On WinRT pair FAILED:
      - If FE4A already visible, treat as soft success (stale re-pair noise)
      - Otherwise re-raise with operator guidance
    """
    client = None
    try:
        logger.info(
            "OS bond %s — connect %s (cuff must show flashing P)…",
            attempt_label,
            address,
        )
        client = await connect_client(
            address,
            timeout=35.0,
            pair_before_connect=False,
            name=profile.model_id,
        )
        # Give WinRT a moment to finish service discovery / encryption hooks
        await asyncio.sleep(0.6)

        parent = profile.parent_service_uuid
        try:
            await pair_client(client)
        except Exception as exc:
            # Soft-success: already bonded enough that FE4A is present
            if await fe4a_visible(client, parent):
                logger.warning(
                    "pair() reported %s but FE4A is visible — treating as bonded OK. "
                    "Next: READ with short-press BT.",
                    type(exc).__name__,
                )
                return
            if is_pair_failed_error(exc):
                raise OSError(
                    "Windows pair() FAILED. Usually: (1) cuff not flashing P, "
                    "(2) stale Windows bond, or (3) phone still owns the bond. "
                    "Fix: remove cuff in Settings → Bluetooth, unpair phone "
                    "OMRON Connect, hold BT until P, then RE-PAIR."
                ) from exc
            raise

        # Confirm Omron service after successful pair when possible
        if not await fe4a_visible(client, parent):
            logger.warning(
                "pair() OK but FE4A not in GATT yet — often fine; "
                "READ in transfer mode will re-discover services."
            )
        else:
            logger.info("FE4A parent service visible after pair — bond looks good")
    finally:
        await disconnect_client(client)


async def pair_device(
    address: str,
    profile: DeviceProfile,
    *,
    unlock_key: bytes = DEFAULT_UNLOCK_KEY,
    force_rebind: bool = False,
) -> None:
    """
    Pair once with the cuff.

    Modern FE4A (HEM-7143T1): connect + OS pair only — no scan, no EEPROM.
    Classic: BleSession + program unlock key.

    On modern WinRT pair FAILED without force_rebind, automatically unpairs
    once and retries (same as interactive RE-PAIR).
    """
    logger.info(
        "PAIR start model=%s address=%s mode=%s force_rebind=%s",
        profile.model_id,
        address,
        profile.pairing_mode.value,
        force_rebind,
    )
    logger.debug("%s pair_device notes=%r", DBG_TAG, profile.notes)

    if profile.pairing_mode == PairingMode.NONE:
        logger.info("Profile requires no pairing — nothing to do.")
        return

    # ---- Modern FE4A: OS bond only ----
    if profile.pairing_mode == PairingMode.OS_BONDING:
        if force_rebind:
            logger.info("force_rebind: removing existing Windows bond first…")
            await unpair_address(address)

        try:
            await _os_bond_once(address, profile, attempt_label="attempt 1")
        except Exception as first_exc:
            # Auto recovery once: unpair + retry (stale bond is the #1 WinRT cause)
            if force_rebind:
                raise
            logger.warning(
                "PAIR attempt 1 failed (%s: %s) — auto unpair + retry once…",
                type(first_exc).__name__,
                first_exc,
            )
            await unpair_address(address)
            await asyncio.sleep(1.0)
            logger.info(
                "Put cuff in flashing P again if it left pairing mode, then wait…"
            )
            await asyncio.sleep(2.0)
            await _os_bond_once(address, profile, attempt_label="attempt 2 (after unpair)")

        logger.info(
            "PAIR finished (OS bonding) model=%s. "
            "Next: READ / LIVE with short-press BT (transfer), not flashing P.",
            profile.model_id,
        )
        return

    # ---- Classic multi-channel: unlock key program ----
    if profile.pairing_mode == PairingMode.UNLOCK_KEY:
        if force_rebind:
            await unpair_address(address)
        async with BleSession(
            address,
            profile,
            pair_on_connect=False,
            find_timeout=30.0,
            skip_scan=True,
        ) as session:
            transport = OmronTransport(session.client, profile)
            await transport.program_unlock_key(unlock_key)
            try:
                await transport.start_transmission()
                await transport.end_transmission()
            except Exception as exc:
                logger.warning(
                    "Key programmed; start/end smoke failed (often OK): %s",
                    exc,
                )
            logger.info(
                "PAIR finished (unlock key written). key hex=%s",
                unlock_key.hex(),
            )
        return

    raise ValueError(f"Unsupported pairing mode: {profile.pairing_mode}")

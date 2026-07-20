"""
Read workflow — download stored BP records from a paired cuff.

Steps:
  1. Connect (BleSession) — with retries for Windows Unreachable
  2. Unlock if classic stack
  3. start_transmission
  4. Read each user's EEPROM region from DeviceProfile
  5. Split into record-sized chunks and parse
  6. end_transmission
"""

from __future__ import annotations

import asyncio
from typing import Any, Dict, List

from bleak.exc import BleakError

from medical_ble_toolkit.omron_bp.ble.session import BleSession
from medical_ble_toolkit.omron_bp.ble.transport import OmronTransport
from medical_ble_toolkit.omron_bp.logging_config import DBG_TAG, get_logger
from medical_ble_toolkit.omron_bp.models.base import DEFAULT_UNLOCK_KEY, DeviceProfile

logger = get_logger("readout.service")

UserRecords = List[Dict[str, Any]]
AllUsersRecords = List[UserRecords]


def _is_bond_error(exc: BaseException) -> bool:
    """True when re-pair is required — reconnect alone will not help."""
    name = type(exc).__name__.lower()
    msg = str(exc).lower()
    if "bondincomplete" in name:
        return True
    return any(
        s in msg
        for s in (
            "bond incomplete",
            "bond may be incomplete",
            "re-pair",
            "repair",
            "flashing p",
            "encrypted notify probe",
            "0x800704c7",
            "-2147023673",
            "could not pair",
            "pair() failed",
            "pairing_failed",
            "insufficient authentication",
            "access denied",
        )
    )


def _is_link_error(exc: BaseException) -> bool:
    """True for flaky Windows BLE errors worth a full reconnect."""
    # Bond/encryption problems need PAIR, not more READ retries
    if _is_bond_error(exc):
        return False
    msg = str(exc).lower()
    if "unreachable" in msg:
        return True
    if "not connected" in msg or "disconnected" in msg:
        # Session CLOSED after CCCD is often bond — still allow one reconnect if
        # transfer window was missed (short-press BT).
        if "start_notify aborted" in msg or "canceled" in msg or "cancelled" in msg:
            return True
        return True
    if "scanner" in msg or "watcher" in msg or "stopping" in msg or "aborted" in msg:
        return True
    if isinstance(exc, (BleakError, ConnectionError, TimeoutError)):
        if "notify" in msg or "unreachable" in msg or "connect" in msg:
            return True
    return False


async def read_device_records(
    address: str,
    profile: DeviceProfile,
    *,
    unlock_key: bytes = DEFAULT_UNLOCK_KEY,
    find_timeout: float = 60.0,
    session_retries: int = 3,
) -> AllUsersRecords:
    """
    Return list-per-user of vital-sign dicts:
      {datetime, sys, dia, bpm, mov, ihb}

    find_timeout: seconds to wait for the cuff to advertise (auto-fetch).
    session_retries: full reconnect attempts on Windows notify Unreachable.
    """
    profile.validate()
    logger.info(
        "READ start model=%s address=%s users=%d find_timeout=%.0fs retries=%d",
        profile.model_id,
        address,
        profile.user_count,
        find_timeout,
        session_retries,
    )
    # DBG-LOG
    logger.debug(
        "%s eeprom map starts=%s counts=%s rec_size=0x%02X block=0x%02X",
        DBG_TAG,
        list(profile.user_start_addresses),
        list(profile.per_user_records_count),
        profile.record_byte_size,
        profile.transmission_block_size,
    )

    last_err: BaseException | None = None
    for attempt in range(1, session_retries + 1):
        wait = find_timeout if attempt == 1 else min(90.0, find_timeout)
        try:
            if attempt > 1:
                logger.warning(
                    "Retry %d/%d — short-press Bluetooth (transfer) again.",
                    attempt,
                    session_retries,
                )
                await asyncio.sleep(1.5)
            return await _read_once(
                address,
                profile,
                unlock_key=unlock_key,
                find_timeout=wait,
            )
        except Exception as exc:
            last_err = exc
            if _is_bond_error(exc):
                logger.error(
                    "Bond/encryption error on attempt %d — stop retries, re-pair required: %s",
                    attempt,
                    exc,
                )
                raise
            if attempt < session_retries and _is_link_error(exc):
                logger.warning(
                    "Link error on attempt %d: %s: %s",
                    attempt,
                    type(exc).__name__,
                    exc,
                )
                continue
            raise

    assert last_err is not None
    raise last_err


async def _read_once(
    address: str,
    profile: DeviceProfile,
    *,
    unlock_key: bytes,
    find_timeout: float,
) -> AllUsersRecords:
    all_users: AllUsersRecords = []

    # pair_on_connect=False: never re-pair on READ (WinRT pair FAILED closes session)
    async with BleSession(
        address,
        profile,
        find_timeout=find_timeout,
        pair_on_connect=False,
    ) as session:
        # Build transport and open notify ASAP while link is still up
        transport = OmronTransport(session.client, profile)

        # Classic key or modern 0x11/0x91 token (HEM-7143T1 phone capture)
        if profile.requires_unlock:
            await transport.unlock_session(unlock_key)

        # start_transmission enables RX notify (if not already) then START packet
        await transport.start_transmission()

        try:
            for user_idx, start_addr in enumerate(profile.user_start_addresses):
                count = profile.per_user_records_count[user_idx]
                nbytes = count * profile.record_byte_size
                logger.info(
                    "Reading user%d: addr=0x%04X bytes=%d (%d slots)...",
                    user_idx + 1,
                    start_addr,
                    nbytes,
                    count,
                )
                # DBG-LOG
                logger.debug(
                    "%s user%d continuous read begin",
                    DBG_TAG,
                    user_idx + 1,
                )

                raw = await transport.read_eeprom(
                    start_addr,
                    nbytes,
                    block_size=profile.transmission_block_size,
                )
                records = _parse_user_blob(raw, profile, user_idx)
                logger.info("User%d: %d valid record(s)", user_idx + 1, len(records))
                all_users.append(records)
        finally:
            try:
                await transport.end_transmission()
            except Exception as exc:
                # DBG-LOG: end may fail if link already dead
                logger.debug("%s end_transmission ignored: %s", DBG_TAG, exc)

    total = sum(len(u) for u in all_users)
    logger.info("READ finished: %d record(s) across %d user(s)", total, len(all_users))
    return all_users


def _parse_user_blob(
    blob: bytearray | bytes,
    profile: DeviceProfile,
    user_idx: int,
) -> UserRecords:
    """Split raw EEPROM dump into records; skip empty/invalid slots."""
    assert profile.parse_record is not None
    size = profile.record_byte_size
    endian = profile.endianness.value
    out: UserRecords = []

    for offset in range(0, len(blob), size):
        chunk = bytes(blob[offset : offset + size])
        if len(chunk) < size:
            break
        if chunk == b"\xff" * size:
            # DBG-LOG: empty slot
            logger.debug(
                "%s user%d offset=0x%04X empty FF",
                DBG_TAG,
                user_idx + 1,
                offset,
            )
            continue
        try:
            rec = profile.parse_record(chunk, endian)
            out.append(rec)
            # DBG-LOG: each accepted record
            logger.debug(
                "%s user%d offset=0x%04X OK sys=%s dia=%s bpm=%s dt=%s",
                DBG_TAG,
                user_idx + 1,
                offset,
                rec.get("sys"),
                rec.get("dia"),
                rec.get("bpm"),
                rec.get("datetime"),
            )
        except Exception as exc:
            # DBG-LOG: parse failures are common for unused slots
            logger.debug(
                "%s user%d offset=0x%04X skip hex=%s err=%s",
                DBG_TAG,
                user_idx + 1,
                offset,
                chunk.hex(),
                exc,
            )
    return out

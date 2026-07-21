"""
Companion-style sync outcomes (BTDeviceSyncState-inspired).
"""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum
from typing import Any, List, Optional


class SyncStatus(str, Enum):
    SUCCESS = "success"  # ≥1 parsed or raw with data path OK
    SUCCESS_EMPTY = "success_empty"  # bonded, quiet end, no measurements
    NO_DATA = "no_data"  # connected but nothing received
    PAIRING_REQUIRED = "pairing_required"  # auth/bond fail
    PAIRING_FAILED = "pairing_failed"
    CONNECT_FAILED = "connect_failed"
    PARTIAL = "partial"  # got some data then link drop
    ABORTED = "aborted"


@dataclass
class SyncResult:
    status: SyncStatus
    model_id: str
    address: str
    readings: List[Any] = field(default_factory=list)
    raw_count: int = 0
    message: str = ""
    passkey_hint: bool = False
    deduped_dropped: int = 0

    @property
    def ok(self) -> bool:
        return self.status in (
            SyncStatus.SUCCESS,
            SyncStatus.SUCCESS_EMPTY,
            SyncStatus.PARTIAL,
        )

    def summary(self) -> str:
        return (
            f"{self.status.value}: {self.message} "
            f"(readings={len(self.readings)}, raw={self.raw_count}, "
            f"dedup_drop={self.deduped_dropped})"
        )


def classify_sync(
    *,
    model_id: str,
    address: str,
    readings: List[Any],
    raw_count: int,
    paired_attempt: bool,
    connect_ok: bool,
    link_dropped: bool,
    auth_error: bool,
    passkey_hint: bool = False,
    deduped_dropped: int = 0,
    error: Optional[BaseException] = None,
) -> SyncResult:
    if auth_error or (
        error
        and any(
            s in str(error).lower()
            for s in ("auth", "bond", "pair", "gatt_auth", "not permitted", "access")
        )
    ):
        return SyncResult(
            status=SyncStatus.PAIRING_REQUIRED
            if paired_attempt
            else SyncStatus.PAIRING_FAILED,
            model_id=model_id,
            address=address,
            readings=readings,
            raw_count=raw_count,
            message="Bonding/auth failed — pair again and accept OS dialog / passkey.",
            passkey_hint=passkey_hint,
            deduped_dropped=deduped_dropped,
        )
    if not connect_ok:
        return SyncResult(
            status=SyncStatus.CONNECT_FAILED,
            model_id=model_id,
            address=address,
            readings=readings,
            raw_count=raw_count,
            message=f"Connect failed: {error}",
            passkey_hint=passkey_hint,
            deduped_dropped=deduped_dropped,
        )
    if readings:
        st = SyncStatus.PARTIAL if link_dropped else SyncStatus.SUCCESS
        return SyncResult(
            status=st,
            model_id=model_id,
            address=address,
            readings=readings,
            raw_count=raw_count,
            message=(
                f"Collected {len(readings)} reading(s)"
                + (" (link dropped mid-sync)" if link_dropped else "")
            ),
            passkey_hint=passkey_hint,
            deduped_dropped=deduped_dropped,
        )
    if raw_count > 0:
        return SyncResult(
            status=SyncStatus.PARTIAL if link_dropped else SyncStatus.SUCCESS,
            model_id=model_id,
            address=address,
            readings=readings,
            raw_count=raw_count,
            message=f"Received {raw_count} packet(s); parse may be incomplete.",
            passkey_hint=passkey_hint,
            deduped_dropped=deduped_dropped,
        )
    if paired_attempt:
        return SyncResult(
            status=SyncStatus.SUCCESS_EMPTY,
            model_id=model_id,
            address=address,
            readings=[],
            raw_count=0,
            message="Connected/bonded but no measurements (empty memory or wrong mode).",
            passkey_hint=passkey_hint,
            deduped_dropped=deduped_dropped,
        )
    return SyncResult(
        status=SyncStatus.NO_DATA,
        model_id=model_id,
        address=address,
        readings=[],
        raw_count=0,
        message="No notifications — wake device, re-pair if needed, retry.",
        passkey_hint=passkey_hint,
        deduped_dropped=deduped_dropped,
    )

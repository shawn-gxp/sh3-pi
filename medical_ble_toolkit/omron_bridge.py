"""
Omron brand adapter — facade over in-package ``medical_ble_toolkit.omron_bp``.

Architecture
------------
  medical_ble_toolkit
  -------------------------
  omron_bridge.py          →   omron_bp.pairing.service.pair_device
                           →   omron_bp.readout.service.read_device_records
                           →   omron_bp.models.registry (23+ profiles)
  parsers/omron.py         →   omron_bp.models.parsers (pure record decode)
  models.BloodPressureReading  ← dict {sys,dia,bpm,mov,ihb,datetime}

BLE connection lifecycle for Omron lives in ``omron_bp/`` (proprietary EEPROM
protocol), vendored inside this package. This module only:
  1. Resolves model
  2. Calls pair / read / unpair workflows
  3. Maps results to shared dataclasses
  4. Optional CSV export via omron_bp.export

Kotlin port: treat omron_bp transport as the "Omron brand adapter"; keep
parsers/omron.py record layouts as pure Kotlin functions.
"""

from __future__ import annotations

import logging
from pathlib import Path
from typing import Any, List, Optional, Sequence

from .common.hexutil import ms_timestamp
from .models import BloodPressureReading, DeviceBrand
from .parsers.omron import dict_to_blood_pressure, parse_omron_record

log = logging.getLogger("medical_ble.omron")


def _require_omron_bp():
    try:
        import medical_ble_toolkit.omron_bp as omron_bp  # noqa: F401
        from medical_ble_toolkit.omron_bp.models.registry import get_profile, list_models
        from medical_ble_toolkit.omron_bp.pairing.service import pair_device
        from medical_ble_toolkit.omron_bp.readout.service import read_device_records
        from medical_ble_toolkit.omron_bp.export.csv_export import write_users_csv
    except ImportError as exc:
        raise ImportError(
            "Omron support requires medical_ble_toolkit.omron_bp "
            "(bundled under medical_ble_toolkit/omron_bp/)."
        ) from exc
    return get_profile, list_models, pair_device, read_device_records, write_users_csv


def list_omron_models() -> List[dict[str, Any]]:
    """Return catalog rows for CLI display."""
    get_profile, list_models, *_ = _require_omron_bp()
    rows = []
    for p in list_models():
        rows.append(
            {
                "model_id": p.model_id,
                "display_name": p.display_name,
                "stack": p.stack.value,
                "pairing": p.pairing_mode.value,
                "users": p.user_count,
                "record_size": p.record_byte_size,
                "slots": list(p.per_user_records_count),
                "aliases": list(p.aliases)[:5],
                "notes": (p.notes or "")[:80],
            }
        )
    return rows


def resolve_omron_model(model: str):
    """Resolve model id / alias → omron_bp DeviceProfile."""
    get_profile, *_ = _require_omron_bp()
    return get_profile(model)


async def unpair_omron(address: str) -> None:
    """Remove OS bond for an Omron cuff (BlueZ/WinRT)."""
    from medical_ble_toolkit.omron_bp.ble.connection import unpair_address

    log.info("[OMRON] UNPAIR address=%s ts=%s", address, ms_timestamp())
    await unpair_address(address)
    log.info("[OMRON] UNPAIR finished address=%s ts=%s", address, ms_timestamp())


async def pair_omron(
    address: str,
    model: str,
    *,
    force_rebind: bool = False,
) -> None:
    """
    Pair once with an Omron cuff.

    Cuff UX:
      - Modern (e.g. HEM-7143T1): hold BT until flashing **P**
      - Classic unlock-key models: same P mode; programs 16-byte app key

    force_rebind:
      Remove existing OS bond first (needed when Windows shows the cuff but
      GattSession flaps ACTIVE/CLOSED — stale bond).
    """
    from medical_ble_toolkit.omron_bp.pairing.service import pair_device as pair_device_fn

    profile = resolve_omron_model(model)
    log.info(
        "[OMRON] PAIR start model=%s address=%s mode=%s force_rebind=%s ts=%s",
        profile.model_id,
        address,
        profile.pairing_mode.value,
        force_rebind,
        ms_timestamp(),
    )
    log.info(
        "[OMRON] Put cuff in pairing mode (flashing P), keep < 1 m from PC. "
        "On Windows, ACCEPT any Bluetooth pairing dialog."
    )
    await pair_device_fn(address, profile, force_rebind=force_rebind)
    log.info("[OMRON] PAIR finished model=%s ts=%s", profile.model_id, ms_timestamp())


async def read_omron(
    address: str,
    model: str,
    *,
    find_timeout: float = 60.0,
    session_retries: int = 3,
    output_dir: Optional[str | Path] = None,
) -> List[List[BloodPressureReading]]:
    """
    Download stored BP history from a paired Omron cuff.

    Cuff UX for read: **transfer mode** (short-press BT — not flashing P).

    Returns:
      List per user of BloodPressureReading (shared toolkit schema).
    """
    _, _, _, read_device_records, write_users_csv = _require_omron_bp()
    profile = resolve_omron_model(model)
    log.info(
        "[OMRON] READ start model=%s address=%s users=%d find_timeout=%.0fs ts=%s",
        profile.model_id,
        address,
        profile.user_count,
        find_timeout,
        ms_timestamp(),
    )
    log.info(
        "[OMRON] Direct MAC read (no scan). Bonded cuff is often connectable for "
        "hours after last use — no button required when the radio is still up. "
        "If connect fails: wake cuff once (short-press BT), then retry."
    )

    all_users_dicts = await read_device_records(
        address,
        profile,
        find_timeout=find_timeout,
        session_retries=session_retries,
    )

    # Optional CSV via original exporter (dict shape)
    if output_dir is not None:
        paths = write_users_csv(all_users_dicts, output_dir)
        for p in paths:
            log.info("[OMRON] CSV → %s", p)

    # Map to shared dataclasses
    result: List[List[BloodPressureReading]] = []
    for user_idx, records in enumerate(all_users_dicts):
        user_readings: List[BloodPressureReading] = []
        for rec in records:
            user_readings.append(
                dict_to_blood_pressure(
                    rec,
                    model=profile.model_id,
                    user_id=user_idx,  # 0-based; display as user_idx+1 in CLI
                )
            )
        # Newest first (match omron_bp CSV convention)
        user_readings.sort(
            key=lambda r: r.measured_at or datetime_min(),
            reverse=True,
        )
        result.append(user_readings)
        log.info(
            "[OMRON] user%d: %d reading(s)",
            user_idx + 1,
            len(user_readings),
        )

    total = sum(len(u) for u in result)
    log.info(
        "[OMRON] READ done total=%d record(s) ts=%s",
        total,
        ms_timestamp(),
    )
    return result


def datetime_min():
    from datetime import datetime

    return datetime.min


def flatten_readings(
    all_users: Sequence[Sequence[BloodPressureReading]],
) -> List[BloodPressureReading]:
    """Flatten multi-user list and sort newest first."""
    flat: List[BloodPressureReading] = []
    for user_list in all_users:
        flat.extend(user_list)
    flat.sort(key=lambda r: r.measured_at or datetime_min(), reverse=True)
    return flat


# Re-export pure parse for tests / offline tools
__all__ = [
    "list_omron_models",
    "resolve_omron_model",
    "pair_omron",
    "unpair_omron",
    "read_omron",
    "flatten_readings",
    "parse_omron_record",
    "dict_to_blood_pressure",
    "DeviceBrand",
]

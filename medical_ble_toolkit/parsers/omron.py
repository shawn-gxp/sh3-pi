"""
Omron BP EEPROM record parser (pure logic — no bleak).

Reuses the battle-tested parsers from sibling package ``omron_bp``
(models/parsers/*) and maps results into medical_ble_toolkit dataclasses.

Omron does NOT use Bluetooth SIG BLP for history download. Each model has:
  - a proprietary GATT parent service (classic multi-channel or modern FE4A)
  - an EEPROM memory map (start address × slot count × record size)
  - a record decoder (14-byte classic, 16-byte bitpacked, …)

This module only does: raw slot bytes → BloodPressureReading.
Session framing / EEPROM read lives in omron_bridge.py → omron_bp.

Kotlin port:
  class OmronRecordParser(modelId: String) : VitalParser<BloodPressureReading>
  // pick layout by model catalog (same as omron_bp DeviceProfile.parse_record)
"""

from __future__ import annotations

from datetime import datetime
from typing import Any, Callable, Optional

from ..common.hexutil import format_hex_dump
from ..models import (
    BloodPressureReading,
    DeviceBrand,
    ParseError,
    PressureUnit,
)

# ---------------------------------------------------------------------------
# Import pure parsers from the earlier omron_bp project (no BLE)
# ---------------------------------------------------------------------------

try:
    from omron_bp.models.parsers import (
        parse_classic_vital_14,
        parse_classic_vital_14_6232_family,
        parse_classic_vital_14_bitpacked,
        parse_classic_vital_16_6401_family,
        parse_vital_16_715x_bitpacked,
    )
    from omron_bp.models.registry import get_profile as omron_get_profile
except ImportError as exc:  # pragma: no cover
    raise ImportError(
        "Omron support requires the sibling package `omron_bp` "
        "(experiments/omron_bp). Run from the experiments folder so both "
        "packages are importable."
    ) from exc


# Parser name → function (mirrors omron_bp catalog choices)
_PARSER_BY_NAME: dict[str, Callable[[bytes | bytearray, str], dict[str, Any]]] = {
    "classic_vital_14": parse_classic_vital_14,
    "classic_vital_14_bitpacked": parse_classic_vital_14_bitpacked,
    "classic_vital_14_6232": parse_classic_vital_14_6232_family,
    "vital_16_715x": parse_vital_16_715x_bitpacked,
    "vital_16_6401": parse_classic_vital_16_6401_family,
}


def dict_to_blood_pressure(
    rec: dict[str, Any],
    *,
    model: str = "",
    user_id: Optional[int] = None,
    raw: bytes | bytearray | None = None,
) -> BloodPressureReading:
    """Map omron_bp vital dict → shared BloodPressureReading schema."""
    measured_at = rec.get("datetime")
    if measured_at is not None and not isinstance(measured_at, datetime):
        measured_at = None

    mov = rec.get("mov", 0) or 0
    ihb = rec.get("ihb", 0) or 0

    return BloodPressureReading(
        systolic=float(rec["sys"]),
        diastolic=float(rec["dia"]),
        mean_arterial_pressure=None,
        pulse_rate=float(rec["bpm"]) if rec.get("bpm") is not None else None,
        unit=PressureUnit.MMHG,
        measured_at=measured_at,
        user_id=user_id,
        body_movement=bool(int(mov)),
        cuff_too_loose=bool(int(rec.get("cuff", 0) or 0)),
        irregular_pulse=bool(int(ihb)),
        pulse_rate_range=0,
        improper_position=bool(int(rec.get("pos", 0) or 0)),
        brand=DeviceBrand.OMRON,
        model=model,
        raw_flags=0,
        raw_hex=format_hex_dump(raw) if raw is not None else "",
    )


def parse_omron_record(
    payload: bytes | bytearray,
    *,
    model: str = "HEM-7143T1",
    user_id: Optional[int] = None,
    parser_name: Optional[str] = None,
    endianness: Optional[str] = None,
) -> BloodPressureReading:
    """
    Parse one EEPROM slot (14 or 16 bytes depending on model).

    If *model* is a known omron_bp id/alias, layout is taken from the catalog.
    Otherwise pass *parser_name* + *endianness* explicitly.
    """
    data = bytes(payload)
    if not data:
        raise ParseError("Empty Omron record", data)

    parse_fn: Optional[Callable] = None
    endian = endianness or "little"
    model_id = model

    if parser_name:
        parse_fn = _PARSER_BY_NAME.get(parser_name)
        if parse_fn is None:
            raise ParseError(
                f"Unknown Omron parser_name={parser_name!r}; "
                f"known={sorted(_PARSER_BY_NAME)}",
                data,
            )
    else:
        try:
            profile = omron_get_profile(model)
        except KeyError as exc:
            raise ParseError(
                f"Unknown Omron model {model!r}. "
                "Use a catalog id (e.g. HEM-7143T1) or pass parser_name=.",
                data,
            ) from exc
        parse_fn = profile.parse_record
        endian = profile.endianness.value
        model_id = profile.model_id

    assert parse_fn is not None
    try:
        rec = parse_fn(data, endian)
    except ValueError as exc:
        # Empty slot / invalid date — surface as ParseError for toolkit consistency
        raise ParseError(str(exc), data) from exc

    return dict_to_blood_pressure(
        rec, model=model_id, user_id=user_id, raw=data
    )


class OmronRecordParser:
    """
    VitalParser-compatible wrapper for one Omron model family.

    Usage:
      p = OmronRecordParser("HEM-7143T1")
      reading = p.parse(fourteen_raw_bytes)
    """

    name = "omron_eeprom_record"
    brand = DeviceBrand.OMRON

    def __init__(self, model: str = "HEM-7143T1"):
        self.model = model
        # Resolve early so bad model fails at construction
        self._profile = omron_get_profile(model)
        self.model = self._profile.model_id

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        # Omron history is not a single notify of BLP shape; slot size is model-specific
        size = self._profile.record_byte_size
        return len(payload) == size

    def parse(self, payload: bytes | bytearray) -> BloodPressureReading:
        return parse_omron_record(payload, model=self.model)

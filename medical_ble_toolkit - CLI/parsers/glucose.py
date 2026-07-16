"""
Bluetooth SIG Glucose Measurement (0x2A18) + RACP (0x2A52) helpers.

Beurer GL* family (HealthManager Pro Gl50SyncRepo):
  CCCD sequence → write RACP Report All → collect 2A18/2A34/2A52.

RACP opcodes (app):
  01          Report all stored records
  03 01 lo hi Report records ≥ sequence
  04 02 + dates  (some models)

Parse follows SIG Glucose Profile; validated structure against common field order.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import Any, Optional

from ..common.hexutil import format_hex_dump
from ..common.sfloat import decode_sfloat
from ..models import DeviceBrand, ParseError

GLUCOSE_SERVICE = "00001808-0000-1000-8000-00805f9b34fb"
GLUCOSE_MEASUREMENT = "00002a18-0000-1000-8000-00805f9b34fb"
GLUCOSE_CONTEXT = "00002a34-0000-1000-8000-00805f9b34fb"
GLUCOSE_FEATURE = "00002a51-0000-1000-8000-00805f9b34fb"
RACP_UUID = "00002a52-0000-1000-8000-00805f9b34fb"

# RACP opcodes
RACP_REPORT_STORED = 0x01
RACP_DELETE_STORED = 0x02
RACP_ABORT = 0x03
RACP_REPORT_NUMBER = 0x04
RACP_NUMBER_RESPONSE = 0x05
RACP_RESPONSE = 0x06

# Operators
RACP_OP_NULL = 0x00
RACP_OP_ALL = 0x01
RACP_OP_LE = 0x02
RACP_OP_GE = 0x03
RACP_OP_RANGE = 0x04
RACP_OP_FIRST = 0x05
RACP_OP_LAST = 0x06

# Filter types
RACP_FILTER_SEQ = 0x01
RACP_FILTER_USER_FACING = 0x02


def racp_report_all() -> bytes:
    """Opcode Report Stored Records + operator All records."""
    return bytes([RACP_REPORT_STORED, RACP_OP_ALL])


def racp_report_ge_sequence(seq: int) -> bytes:
    """Report records with sequence number ≥ seq (app: 03 01 lo hi)."""
    s = max(0, int(seq)) & 0xFFFF
    return bytes([RACP_REPORT_STORED, RACP_OP_GE, RACP_FILTER_SEQ, s & 0xFF, (s >> 8) & 0xFF])


def racp_abort() -> bytes:
    return bytes([RACP_ABORT, RACP_OP_NULL])


@dataclass
class GlucoseReading:
    sequence: int
    concentration: Optional[float]
    unit: str  # "mg/dL" or "mmol/L"
    measured_at: Optional[datetime]
    type_sample: Optional[int] = None
    sample_location: Optional[int] = None
    sensor_status: Optional[int] = None
    brand: DeviceBrand = DeviceBrand.BEURER
    model: str = ""
    raw_hex: str = ""
    context_hex: str = ""

    def to_dict(self) -> dict:
        return {
            "type": "glucose",
            "sequence": self.sequence,
            "concentration": self.concentration,
            "unit": self.unit,
            "measured_at": self.measured_at.isoformat() if self.measured_at else None,
            "type_sample": self.type_sample,
            "sample_location": self.sample_location,
            "sensor_status": self.sensor_status,
            "brand": self.brand.value,
            "model": self.model,
            "raw_hex": self.raw_hex,
        }


def _parse_base_time(data: bytes, offset: int) -> tuple[datetime, int]:
    if offset + 7 > len(data):
        raise ParseError("Glucose base time truncated", data)
    year = data[offset] | (data[offset + 1] << 8)
    month, day = data[offset + 2], data[offset + 3]
    hour, minute, second = data[offset + 4], data[offset + 5], data[offset + 6]
    try:
        dt = datetime(year, month, day, hour, minute, second)
    except ValueError as exc:
        raise ParseError(f"Invalid glucose timestamp: {exc}", data) from exc
    return dt, offset + 7


def parse_glucose_measurement(
    payload: bytes | bytearray,
    *,
    model: str = "",
) -> GlucoseReading:
    """
    SIG Glucose Measurement characteristic layout.

    flags bit0 time offset present
    flags bit1 type+location present
    flags bit2 concentration units (0=kg/L → often mg/dL path via SFLOAT; 1=mol/L)
    flags bit3 sensor status present
    flags bit4 context info
    """
    data = bytes(payload)
    if len(data) < 3:
        raise ParseError(f"Glucose measurement too short ({len(data)})", data)
    flags = data[0]
    seq = data[1] | (data[2] << 8)
    offset = 3
    measured_at: Optional[datetime] = None
    if True:  # base time always after seq in SIG when present — bit not in first flags for base time
        # SIG: base time is mandatory in Glucose Measurement
        if offset + 7 <= len(data):
            measured_at, offset = _parse_base_time(data, offset)
        else:
            raise ParseError("Missing glucose base time", data)

    if flags & 0x01:  # time offset
        if offset + 2 > len(data):
            raise ParseError("Missing time offset", data)
        toff = int.from_bytes(data[offset : offset + 2], "little", signed=True)
        offset += 2
        if measured_at is not None:
            measured_at = measured_at + timedelta(minutes=toff)

    concentration: Optional[float] = None
    unit = "mg/dL"
    if flags & 0x02:  # concentration present? Actually bit2 is units; concentration always when flags allow
        pass
    # Concentration is present unless only context — SIG includes SFLOAT after time fields
    if offset + 2 <= len(data) and not (flags & 0x00 and len(data) < offset + 2):
        # Always try concentration SFLOAT if bytes remain and concentration expected
        # SIG: concentration present if bit1 of flags? Spec: flags bit1 = Glucose Concentration, Type and Sample Location Present
        if flags & 0x02:
            concentration = decode_sfloat(data, offset)
            offset += 2
            unit = "mmol/L" if (flags & 0x04) else "mg/dL"
            if offset < len(data):
                nibble = data[offset]
                type_sample = nibble & 0x0F
                sample_location = (nibble >> 4) & 0x0F
                offset += 1
            else:
                type_sample = sample_location = None
        else:
            type_sample = sample_location = None
    else:
        type_sample = sample_location = None

    sensor_status = None
    if flags & 0x08:
        if offset + 2 <= len(data):
            sensor_status = data[offset] | (data[offset + 1] << 8)

    return GlucoseReading(
        sequence=seq,
        concentration=concentration,
        unit=unit,
        measured_at=measured_at,
        type_sample=type_sample,
        sample_location=sample_location,
        sensor_status=sensor_status,
        brand=DeviceBrand.BEURER,
        model=model,
        raw_hex=format_hex_dump(data),
    )


def parse_racp_response(payload: bytes | bytearray) -> dict:
    data = bytes(payload)
    if not data:
        return {"type": "racp_empty"}
    op = data[0]
    if op == RACP_RESPONSE and len(data) >= 4:
        return {
            "type": "racp_response",
            "request_opcode": data[1],
            "operator": data[2],
            "result": data[3],
            "raw_hex": format_hex_dump(data),
        }
    if op == RACP_NUMBER_RESPONSE and len(data) >= 3:
        n = data[2] | (data[3] << 8) if len(data) >= 4 else data[2]
        return {"type": "racp_number", "count": n, "raw_hex": format_hex_dump(data)}
    return {"type": "racp_raw", "opcode": op, "raw_hex": format_hex_dump(data)}


class GlucoseParser:
    name = "beurer_glucose"
    brand = DeviceBrand.BEURER

    def __init__(self, model: str = ""):
        self.model = model

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        u = characteristic_uuid.lower().replace("-", "")
        return "2a18" in u or "2a52" in u or "1808" in u

    def parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> Any:
        u = characteristic_uuid.lower().replace("-", "")
        if "2a52" in u:
            return parse_racp_response(payload)
        return parse_glucose_measurement(payload, model=self.model)

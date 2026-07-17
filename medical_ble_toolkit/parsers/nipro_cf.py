"""
Nipro Cocoron / NIPRO CF glucose meter (げんきノート BLEDeviceCFL).

NOT Bluetooth SIG Glucose 0x1808 — proprietary 128-bit UUIDs.

Sources:
  - decompiled BLELib BLEDeviceCFL
  - datasheets/nipro EXACT_HW_SEQUENCES.md
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Any, Dict, List, Optional

from ..common.hexutil import format_hex_dump
from ..common.sfloat import decode_sfloat
from ..models import DeviceBrand, ParseError

# --- GATT (companion exact) -------------------------------------------------

SVC_GLUCOSE = "5d87a4a0-e42d-11e5-beef-0002a5d5c51b"
CHAR_MEASUREMENT = "5d87a4a1-e42d-11e5-beef-0002a5d5c51b"
CHAR_CONTEXT = "5d87a4a2-e42d-11e5-beef-0002a5d5c51b"
CHAR_RACP = "5d87a4a3-e42d-11e5-beef-0002a5d5c51b"
CHAR_FEATURE = "5d87a4a4-e42d-11e5-beef-0002a5d5c51b"

SVC_TIME = "87f60001-a469-1ef4-637f-78b96a6f358b"
CHAR_CURRENT_TIME = "87f60002-a469-1ef4-637f-78b96a6f358b"

SVC_DIS = "8e5996e0-e42f-11e5-af97-0002a5d5c51b"
CHAR_SERIAL = "8e5996e3-e42f-11e5-af97-0002a5d5c51b"

CONTROL_SOLUTION_TYPE = "0A"

# RACP-style opcodes used by CFL (not SIG 0x2A52 layout exactly, but same op family)
RACP_REPORT = 0x01
RACP_NUMBER = 0x04
RACP_FILTER_SEQ = 0x03
RACP_OP_EQ = 0x01  # used in Diff/Last 5-byte forms
RACP_OP_ALL = 0x01  # short form 04 01 / 01 01


def encode_cf_clock(when: Optional[datetime] = None) -> bytes:
    """
    Companion TimeSetting: year as hex digit pairs of year.ToString("x4").

    For year 2026 → "07ea" → bytes EA 07 then mon/day/h/m/s.
    Equivalent to LE uint16 year for normal years.
    """
    dt = when or datetime.now()
    yhex = f"{dt.year:04x}"
    b0 = int(yhex[2:4], 16)
    b1 = int(yhex[0:2], 16)
    return bytes(
        [
            b0,
            b1,
            dt.month & 0xFF,
            dt.day & 0xFF,
            dt.hour & 0xFF,
            dt.minute & 0xFF,
            dt.second & 0xFF,
        ]
    )


def racp_number_of_records_all() -> bytes:
    """Companion All mode: count query."""
    return bytes([RACP_NUMBER, RACP_OP_ALL])


def racp_report_all() -> bytes:
    """Companion All mode: report all records."""
    return bytes([RACP_REPORT, RACP_OP_ALL])


def racp_number_of_records_seq(seq: int) -> bytes:
    """Diff/Last: 04 03 01 seq_lo seq_hi."""
    s = max(0, int(seq)) & 0xFFFF
    return bytes(
        [RACP_NUMBER, RACP_FILTER_SEQ, RACP_OP_EQ, s & 0xFF, (s >> 8) & 0xFF]
    )


def racp_report_from_seq(seq: int) -> bytes:
    """Diff/Last: 01 03 01 seq_lo seq_hi."""
    s = max(0, int(seq)) & 0xFFFF
    return bytes(
        [RACP_REPORT, RACP_FILTER_SEQ, RACP_OP_EQ, s & 0xFF, (s >> 8) & 0xFF]
    )


def racp_number_last_one() -> bytes:
    """Last mode count: seq=1 → 04 03 01 01 00."""
    return racp_number_of_records_seq(1)


@dataclass
class NiproCfGlucoseReading:
    sequence: int
    concentration_mg_dl: Optional[float]
    measured_at: Optional[datetime]
    sample_type: str = ""
    meal: int = 0
    exceed_limit: str = "0"  # 0 normal, 1 high, 2 low (companion)
    brand: DeviceBrand = DeviceBrand.UNKNOWN
    model: str = "NIPRO CF"
    raw_hex: str = ""
    is_control_solution: bool = False

    def to_dict(self) -> dict:
        return {
            "type": "glucose",
            "sequence": self.sequence,
            "concentration": self.concentration_mg_dl,
            "unit": "mg/dL",
            "measured_at": self.measured_at.isoformat() if self.measured_at else None,
            "sample_type": self.sample_type,
            "meal": self.meal,
            "exceed_limit": self.exceed_limit,
            "is_control_solution": self.is_control_solution,
            "brand": self.brand.value,
            "model": self.model,
            "raw_hex": self.raw_hex,
        }


def _clamp_glu(raw: float) -> tuple[float, str]:
    """Companion: >600 → 600 exceed=1; <20 → 20 exceed=2."""
    if raw > 600:
        return 600.0, "1"
    if raw < 20:
        return 20.0, "2"
    return float(raw), "0"


def parse_cf_measurement(payload: bytes | bytearray) -> NiproCfGlucoseReading:
    """
    Companion Cr_ValueUpdated Glucose Measurement layout (len > 13):

      [1:3]  seq int16 LE
      [3:5]  year int16 LE
      [5:10] mon, day, hour, min, sec
      [12:14] glucose SFLOAT
      [14]   type byte → hex string (control solution \"0A\")
    """
    data = bytes(payload)
    if len(data) <= 13:
        raise ParseError(
            f"NIPRO CF measurement too short ({len(data)} ≤ 13)", data
        )
    seq = data[1] | (data[2] << 8)
    year = data[3] | (data[4] << 8)
    mon, day = data[5], data[6]
    hour, minute, second = data[7], data[8], data[9]
    measured_at: Optional[datetime] = None
    try:
        if year and mon and day:
            measured_at = datetime(year, mon, day, hour, minute, second)
    except ValueError:
        measured_at = None

    sfloat = decode_sfloat(data, 12)
    # Companion: Data = (int)((decimal)sfloat * 100000m)  — SFLOAT as kg/L → mg/dL
    if sfloat is None:
        conc = None
        exceed = "0"
    else:
        raw_glu = float(int(sfloat * 100000))
        conc, exceed = _clamp_glu(raw_glu)

    type_hex = f"{data[14]:02X}" if len(data) > 14 else ""
    is_ctrl = type_hex.upper() == CONTROL_SOLUTION_TYPE

    return NiproCfGlucoseReading(
        sequence=seq,
        concentration_mg_dl=None if is_ctrl else conc,
        measured_at=measured_at,
        sample_type=type_hex,
        meal=0,
        exceed_limit=exceed,
        brand=DeviceBrand.UNKNOWN,
        model="NIPRO CF",
        raw_hex=format_hex_dump(data),
        is_control_solution=is_ctrl,
    )


def parse_cf_context(payload: bytes | bytearray) -> dict:
    """
    Companion: array[0]==2 and len==4 → seq at [1:3], meal at [3].
    """
    data = bytes(payload)
    if len(data) == 4 and data[0] == 2:
        seq = data[1] | (data[2] << 8)
        meal = data[3]
        return {
            "type": "glucose_context",
            "sequence": seq,
            "meal": meal,
            "raw_hex": format_hex_dump(data),
        }
    return {
        "type": "glucose_context_raw",
        "raw_hex": format_hex_dump(data),
    }


def parse_cf_racp(payload: bytes | bytearray) -> dict:
    """RACP response; companion checks len==4 and array[0]==5 for number response."""
    data = bytes(payload)
    out: Dict[str, Any] = {"type": "racp", "raw_hex": format_hex_dump(data)}
    if len(data) >= 1:
        out["opcode"] = data[0]
    if len(data) == 4 and data[0] == 5:
        # number of records response (companion)
        out["number_of_records"] = data[2] | (data[3] << 8)
        out["subtype"] = "number_response"
    return out


class NiproCfParser:
    """
    Stateful CF parser: merges measurement + context by sequence.
    """

    name = "nipro_cf"
    brand = DeviceBrand.UNKNOWN

    def __init__(self) -> None:
        self.model = "NIPRO CF"
        self._pending: Dict[int, NiproCfGlucoseReading] = {}
        self.completed: List[NiproCfGlucoseReading] = []

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        u = (characteristic_uuid or "").lower().replace("-", "")
        if "5d87a4a" in u or "7a1a000" in u or "87f6000" in u:
            return True
        # Measurement >13; RACP/context often 4 bytes
        return len(payload) > 13 or len(payload) == 4

    def parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> Any:
        u = (characteristic_uuid or "").lower().replace("-", "")
        data = bytes(payload)
        if "a4a3" in u or (len(data) == 4 and data[0] in (5, 6)):
            return parse_cf_racp(data)
        if "a4a2" in u or (len(data) == 4 and data[0] == 2):
            ctx = parse_cf_context(data)
            if "sequence" in ctx:
                seq = int(ctx["sequence"])
                meal = int(ctx.get("meal") or 0)
                if seq in self._pending:
                    self._pending[seq].meal = meal
                    rec = self._pending.pop(seq)
                    if not rec.is_control_solution:
                        self.completed.append(rec)
                    return rec
                # context before measurement
                self._pending[seq] = NiproCfGlucoseReading(
                    sequence=seq,
                    concentration_mg_dl=None,
                    measured_at=None,
                    meal=meal,
                )
            return ctx
        # measurement
        rec = parse_cf_measurement(data)
        if rec.is_control_solution:
            return rec
        if rec.sequence in self._pending:
            meal = self._pending[rec.sequence].meal
            rec.meal = meal
            del self._pending[rec.sequence]
            self.completed.append(rec)
            return rec
        # wait for context or complete without meal
        self._pending[rec.sequence] = rec
        # Companion often completes when both halves arrive; also emit on second sight
        # If no context ever comes, flush on next same-seq or explicit flush
        return rec

    def flush_pending(self) -> List[NiproCfGlucoseReading]:
        out = []
        for rec in list(self._pending.values()):
            if not rec.is_control_solution and rec.concentration_mg_dl is not None:
                out.append(rec)
                self.completed.append(rec)
        self._pending.clear()
        return out

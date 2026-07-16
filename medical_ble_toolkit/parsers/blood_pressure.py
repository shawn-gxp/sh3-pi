"""
Bluetooth SIG Blood Pressure Measurement (0x2A35) parser.

SHARED by:
  - Beurer BM54          (datasheets/beurer/BM54_transmissionprotocol_rev03_*)
  - A&D UA-651BLE        (datasheets/nipro/SH3 UA-651BLE SDK)

Both implement the same BLP characteristic layout. Brand-specific transport
differences (passkey, A&D custom 0xF000 commands, time sync) belong in the
BLE client / profile layer — NOT here.

Layout (LSO → MSO, little-endian multi-byte fields):
  [0]      flags (1)
  [1:3]    systolic SFLOAT
  [3:5]    diastolic SFLOAT
  [5:7]    MAP SFLOAT
  [7:14]   timestamp (7) if flag bit1
  [..]     pulse SFLOAT if flag bit2
  [..]     user_id uint8 if flag bit3
  [..]     measurement_status uint16 LE if flag bit4

Beurer golden example (PDF + HealthManager Pro analysis — SYS=117 = 0x75):
  0x1E 0x75 0x00 0x4D 0x00 0x00 0x00 0xDF 0x07 0x01 0x0E 0x0A 0x37 0x00
  0x48 0x00 0x01 0x00 0x00
  → SYS 117, DIA 77, MAP 0, 2015-01-14 10:55:00, pulse 72, user 1 (=user2), status 0

App notes (datasheets/beurer/BLE_PROTOCOL_ANALYSIS.md):
  - Indicate CCCD 02 00 only; no proprietary download command
  - Some models use swapped pulse SFLOAT bytes (pulse_swapped=True)
  - Status bit2 = IHB; bit6 (0x40) HSD path; AFib is model-specific
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from ..common.hexutil import format_hex_dump
from ..common.sfloat import decode_sfloat
from ..models import (
    BloodPressureReading,
    DeviceBrand,
    ParseError,
    PressureUnit,
)

# Standard characteristic UUID (16-bit 0x2A35 expanded)
BP_MEASUREMENT_UUID = "00002a35-0000-1000-8000-00805f9b34fb"
BP_SERVICE_UUID = "00001810-0000-1000-8000-00805f9b34fb"
BP_FEATURE_UUID = "00002a49-0000-1000-8000-00805f9b34fb"
INTERMEDIATE_CUFF_UUID = "00002a36-0000-1000-8000-00805f9b34fb"
CURRENT_TIME_SERVICE_UUID = "00001805-0000-1000-8000-00805f9b34fb"
CURRENT_TIME_CHAR_UUID = "00002a2b-0000-1000-8000-00805f9b34fb"

# Flag bits
FLAG_UNITS_KPA = 0x01
FLAG_TIMESTAMP = 0x02
FLAG_PULSE = 0x04
FLAG_USER_ID = 0x08
FLAG_STATUS = 0x10


def encode_current_time_2a2b(when: Optional[datetime] = None) -> bytes:
    """
    Current Time characteristic (0x2A2B) write used by BM59 / some Beurer BP paths.

    Layout (app BM59SetTimeRepoImpl):
      year_lo, year_hi, month, day, hour, min, sec, dayOfWeek, 0x00, 0x00
    dayOfWeek: ISO Mon=1 … Sun=7 (app remaps Sunday to 7).
    Not required for pure BM54 history dump (auto-Indicate).
    """
    dt = when or datetime.now()
    year = int(dt.year)
    return bytes(
        [
            year & 0xFF,
            (year >> 8) & 0xFF,
            dt.month & 0xFF,
            dt.day & 0xFF,
            dt.hour & 0xFF,
            dt.minute & 0xFF,
            dt.second & 0xFF,
            dt.isoweekday() & 0xFF,
            0x00,
            0x00,
        ]
    )


def _parse_blp_timestamp(data: bytes | bytearray, offset: int) -> datetime:
    """7-byte BLP/CTS date-time: year(u16 LE), mon, day, hour, min, sec."""
    if offset + 7 > len(data):
        raise ParseError(
            f"Timestamp needs 7 bytes at offset {offset}, got len={len(data)}",
            data,
        )
    year = data[offset] | (data[offset + 1] << 8)
    month = data[offset + 2]
    day = data[offset + 3]
    hour = data[offset + 4]
    minute = data[offset + 5]
    second = data[offset + 6]
    # Spec: 0 = unknown for month/day; clamp gracefully for RE robustness
    if year == 0 or month == 0 or day == 0:
        raise ParseError(
            f"Incomplete timestamp year={year} month={month} day={day}",
            data,
        )
    try:
        return datetime(year, month, day, hour, minute, second)
    except ValueError as exc:
        raise ParseError(f"Invalid timestamp fields: {exc}", data) from exc


def parse_blood_pressure_measurement(
    payload: bytes | bytearray,
    brand: DeviceBrand = DeviceBrand.UNKNOWN,
    model: str = "",
    *,
    pulse_swapped: bool = False,
) -> BloodPressureReading:
    """
    Pure function: raw 0x2A35 indication bytes → BloodPressureReading.

    pulse_swapped: some Beurer variants invert pulse SFLOAT byte order
    (HealthManager Pro mapper flag). BM54 uses normal LE (False).

    Raises ParseError on short payloads / bad timestamps (never IndexError).
    """
    data = bytes(payload)
    if len(data) < 7:
        raise ParseError(
            f"BP measurement too short: need ≥7 bytes (flags+3×SFLOAT), got {len(data)}",
            data,
        )

    flags = data[0]
    unit = PressureUnit.KPA if (flags & FLAG_UNITS_KPA) else PressureUnit.MMHG

    try:
        systolic = decode_sfloat(data, 1)
        diastolic = decode_sfloat(data, 3)
        map_val = decode_sfloat(data, 5)
    except IndexError as exc:
        raise ParseError(f"SFLOAT decode failed: {exc}", data) from exc

    if systolic is None or diastolic is None:
        raise ParseError("Systolic/diastolic SFLOAT is NaN/NRes (measurement error?)", data)

    offset = 7
    measured_at: Optional[datetime] = None
    pulse: Optional[float] = None
    user_id: Optional[int] = None
    body_movement = cuff_loose = irregular = improper = hsd = False
    pulse_range = 0
    raw_status = 0

    try:
        if flags & FLAG_TIMESTAMP:
            measured_at = _parse_blp_timestamp(data, offset)
            offset += 7

        if flags & FLAG_PULSE:
            if offset + 2 > len(data):
                raise ParseError("Pulse rate flag set but payload truncated", data)
            if pulse_swapped:
                # App: fj.a.a(bArr[15], bArr[14]) when endian flag set
                pulse = decode_sfloat(bytes([data[offset + 1], data[offset]]), 0)
            else:
                pulse = decode_sfloat(data, offset)
            offset += 2

        if flags & FLAG_USER_ID:
            if offset + 1 > len(data):
                raise ParseError("User ID flag set but payload truncated", data)
            user_id = data[offset]
            offset += 1

        if flags & FLAG_STATUS:
            if offset + 2 > len(data):
                raise ParseError("Measurement status flag set but payload truncated", data)
            # Measurement Status is Little Endian (A&D + Beurer)
            status = data[offset] | (data[offset + 1] << 8)
            raw_status = status
            body_movement = bool(status & 0x0001)
            cuff_loose = bool(status & 0x0002)
            irregular = bool(status & 0x0004)
            pulse_range = (status >> 3) & 0x03
            improper = bool(status & 0x0020)
            # Beurer app common HSD path: bit6 (0x40) on low status byte
            hsd = bool(status & 0x0040)
            offset += 2
    except ParseError:
        raise
    except Exception as exc:  # noqa: BLE001
        raise ParseError(f"Unexpected parse failure at offset {offset}: {exc}", data) from exc

    return BloodPressureReading(
        systolic=systolic,
        diastolic=diastolic,
        mean_arterial_pressure=map_val,
        pulse_rate=pulse,
        unit=unit,
        measured_at=measured_at,
        user_id=user_id,
        body_movement=body_movement,
        cuff_too_loose=cuff_loose,
        irregular_pulse=irregular,
        pulse_rate_range=pulse_range,
        improper_position=improper,
        hsd=hsd,
        afib=None,  # BM96/BM64 paths need model-specific decoders
        raw_status=raw_status,
        brand=brand,
        model=model,
        raw_flags=flags,
        raw_hex=format_hex_dump(data),
    )


class BlpBloodPressureParser:
    """
    Reusable BLP parser instance for Beurer BM54-class and A&D UA-651BLE.

    Kotlin:
      class BlpBloodPressureParser(
          private val brand: String = "unknown",
          private val model: String = ""
      ) : VitalParser<BloodPressureReading>
    """

    name = "blp_blood_pressure"
    brand = DeviceBrand.UNKNOWN

    def __init__(
        self,
        brand: DeviceBrand = DeviceBrand.UNKNOWN,
        model: str = "",
        pulse_swapped: bool = False,
    ):
        self.brand = brand
        self.model = model
        self.pulse_swapped = pulse_swapped

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        uuid = characteristic_uuid.lower().replace("-", "")
        if "2a35" in uuid:
            return True
        # Heuristic: flags always present, length typically 7–19
        if len(payload) < 7:
            return False
        flags = payload[0]
        # Beurer BM54 always uses flags 0x1E when all optional fields present
        # Accept any flags where reserved high bits are zero-ish
        return (flags & 0xE0) == 0 and len(payload) <= 20

    def parse(self, payload: bytes | bytearray) -> BloodPressureReading:
        return parse_blood_pressure_measurement(
            payload,
            brand=self.brand,
            model=self.model,
            pulse_swapped=self.pulse_swapped,
        )


def beurer_bm54_parser() -> BlpBloodPressureParser:
    return BlpBloodPressureParser(brand=DeviceBrand.BEURER, model="BM54")


def and_ua651ble_parser() -> BlpBloodPressureParser:
    return BlpBloodPressureParser(brand=DeviceBrand.AND, model="UA-651BLE")

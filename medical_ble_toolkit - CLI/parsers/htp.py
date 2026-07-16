"""
Bluetooth SIG Health Thermometer Profile — Temperature Measurement (0x2A1C).

Source: datasheets/nipro/SH3 HTP_V10 + Bluetooth HTS characteristic.

Included as a pure parser for collectors that speak standard HTP (the NT-100B
uses proprietary 8-byte frames instead — see thermometer.py).

Layout (little-endian multi-byte):
  [0]     flags
  [1:5]   temperature FLOAT (IEEE-11073 32-bit)
  [5:12]  timestamp (7) if flag bit1
  [..]    temperature type uint8 if flag bit2

Flag bits:
  0  units: 0=°C, 1=°F
  1  timestamp present
  2  temperature type present
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from ..common.hexutil import format_hex_dump
from ..models import (
    DeviceBrand,
    ParseError,
    TemperatureSite,
    TemperatureUnit,
    ThermometerReading,
)

TEMP_MEASUREMENT_UUID = "00002a1c-0000-1000-8000-00805f9b34fb"
HTS_SERVICE_UUID = "00001809-0000-1000-8000-00805f9b34fb"

FLAG_FAHRENHEIT = 0x01
FLAG_TIMESTAMP = 0x02
FLAG_TEMP_TYPE = 0x04

# Temperature Type characteristic values (Bluetooth Assigned Numbers)
_TYPE_TO_SITE = {
    0x01: TemperatureSite.ARMPIT,
    0x02: TemperatureSite.OBJECT_SURFACE,  # body (generic) → nearest
    0x03: TemperatureSite.EAR,
    0x04: TemperatureSite.OBJECT_SURFACE,  # finger
    0x05: TemperatureSite.RECTAL,  # gastro-intestinal
    0x06: TemperatureSite.OBJECT_SURFACE,  # mouth mapped loosely
    0x07: TemperatureSite.RECTAL,
    0x08: TemperatureSite.EAR,  # tympanic
    0x09: TemperatureSite.FOREHEAD,
}


def decode_float_11073(data: bytes | bytearray, offset: int = 0) -> float:
    """
    IEEE-11073 32-bit FLOAT: 24-bit signed mantissa + 8-bit signed exponent.
    Special: 0x007FFFFF NaN, etc.
    """
    if offset + 4 > len(data):
        raise ParseError(f"FLOAT needs 4 bytes at {offset}", data)
    raw = int.from_bytes(data[offset : offset + 4], "little", signed=False)
    mantissa = raw & 0x00FFFFFF
    exponent = (raw >> 24) & 0xFF
    # special NaN / NRes
    if mantissa in (0x007FFFFF, 0x00800000, 0x007FFFFE, 0x00800002, 0x00800001):
        raise ParseError(f"FLOAT special value 0x{raw:08X}", data)
    if mantissa & 0x00800000:
        mantissa = mantissa - 0x01000000
    if exponent & 0x80:
        exponent = exponent - 0x100
    return float(mantissa * (10 ** exponent))


def _parse_timestamp(data: bytes, offset: int) -> datetime:
    if offset + 7 > len(data):
        raise ParseError("HTP timestamp truncated", data)
    year = data[offset] | (data[offset + 1] << 8)
    month, day = data[offset + 2], data[offset + 3]
    hour, minute, second = data[offset + 4], data[offset + 5], data[offset + 6]
    if year == 0 or month == 0 or day == 0:
        raise ParseError(f"Incomplete HTP timestamp {year}-{month}-{day}", data)
    return datetime(year, month, day, hour, minute, second)


def parse_temperature_measurement(payload: bytes | bytearray) -> ThermometerReading:
    data = bytes(payload)
    if len(data) < 5:
        raise ParseError(f"HTP measurement too short ({len(data)} < 5)", data)
    flags = data[0]
    unit = (
        TemperatureUnit.FAHRENHEIT
        if (flags & FLAG_FAHRENHEIT)
        else TemperatureUnit.CELSIUS
    )
    temp = decode_float_11073(data, 1)
    offset = 5
    measured_at: Optional[datetime] = None
    site = TemperatureSite.UNKNOWN
    if flags & FLAG_TIMESTAMP:
        measured_at = _parse_timestamp(data, offset)
        offset += 7
    if flags & FLAG_TEMP_TYPE:
        if offset >= len(data):
            raise ParseError("Temperature type flag set but missing", data)
        site = _TYPE_TO_SITE.get(data[offset], TemperatureSite.UNKNOWN)
    return ThermometerReading(
        object_temperature=temp,
        ambient_temperature=None,
        unit=unit,
        site=site,
        measured_at=measured_at,
        brand=DeviceBrand.UNKNOWN,
        model="HTP",
        raw_hex=format_hex_dump(data),
    )


class HtpTemperatureParser:
    """SIG Health Thermometer Temperature Measurement parser."""

    name = "htp_temperature"
    brand = DeviceBrand.UNKNOWN

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        uuid = characteristic_uuid.lower().replace("-", "")
        return "2a1c" in uuid or "1809" in uuid

    def parse(self, payload: bytes | bytearray) -> ThermometerReading:
        return parse_temperature_measurement(payload)

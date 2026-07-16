"""
Beurer FT* thermometer — 13-byte Temperature Measurement (app TemperatureSyncRepoImpl).

Service 0x1809 / char 0x2A1C Indicate. App requires length == 13.

Layout (from decompiled et/b.java):
  [0]     flags: bit0 = Fahrenheit (convert to C); bit7 extra flag
  [1:4]   24-bit mantissa LE
  [4]     exponent (signed 8-bit) — value = mantissa * 10^exponent
            code uses mant / 10^(-exp) ≡ mant * 10^exp
  [5:6]   year as BIG-ENDIAN uint16 (ByteBuffer wrap b6,b5)
  [7]     month
  [8]     day
  [9]     hour
  [10]    minute
  [11]    second
  [12]    type: 2 = FOREHEAD else UNKNOWN
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

HTS_SERVICE = "00001809-0000-1000-8000-00805f9b34fb"
TEMP_MEASUREMENT = "00002a1c-0000-1000-8000-00805f9b34fb"


def parse_beurer_ft_frame(payload: bytes | bytearray, model: str = "") -> ThermometerReading:
    data = bytes(payload)
    if len(data) != 13:
        raise ParseError(f"Beurer FT frame must be 13 bytes, got {len(data)}", data)

    flags = data[0]
    mant = data[1] | (data[2] << 8) | (data[3] << 16)
    # sign-extend 24-bit
    if mant & 0x800000:
        mant -= 0x1000000
    exp = data[4]
    if exp & 0x80:
        exp -= 0x100
    # App: mant / Math.pow(10.0, -b12)  with b12 = exp
    temp = float(mant) * (10.0 ** exp)

    is_f = bool(flags & 0x01)
    unit = TemperatureUnit.FAHRENHEIT if is_f else TemperatureUnit.CELSIUS
    if is_f:
        # App converts to Celsius for storage
        temp_c = (temp - 32.0) * 5.0 / 9.0
        object_t = temp_c
        unit = TemperatureUnit.CELSIUS
    else:
        object_t = temp

    # Year: app ByteBuffer wrap(data[6], data[5]) BIG_ENDIAN → year
    year = (data[5] << 8) | data[6]
    # Some dumps use LE; if year absurd try LE
    if year < 2000 or year > 2100:
        year_le = data[5] | (data[6] << 8)
        if 2000 <= year_le <= 2100:
            year = year_le

    month, day = data[7], data[8]
    hour, minute, second = data[9], data[10], data[11]
    measured_at: Optional[datetime] = None
    try:
        if year and month and day:
            measured_at = datetime(year, month, day, hour, minute, second)
    except ValueError:
        measured_at = None

    site = TemperatureSite.FOREHEAD if data[12] == 2 else TemperatureSite.UNKNOWN

    return ThermometerReading(
        object_temperature=object_t,
        ambient_temperature=None,
        unit=unit,
        site=site,
        measured_at=measured_at,
        brand=DeviceBrand.BEURER,
        model=model or "FT",
        raw_hex=format_hex_dump(data),
    )


class BeurerFtParser:
    name = "beurer_ft"
    brand = DeviceBrand.BEURER

    def __init__(self, model: str = ""):
        self.model = model

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        u = characteristic_uuid.lower().replace("-", "")
        if "2a1c" in u or "1809" in u:
            return True
        return len(payload) == 13

    def parse(self, payload: bytes | bytearray) -> ThermometerReading:
        return parse_beurer_ft_frame(payload, model=self.model)

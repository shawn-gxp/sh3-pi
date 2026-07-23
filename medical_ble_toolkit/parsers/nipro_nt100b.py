"""
NT-100B companion-oriented parser (げんきノート + practical post-measure sync).

Companion decompiled path listens HTP 0x2A1C and requires len>=12, then:
  temp = SFLOAT(bytes[1:3]) * 10^(int8 byte[4])   # works for typical FLOAT encoding
  timestamp = DateTime.Now

In practice the indication often fires at measure time *before* the host connects.
TICD storage pull (0x25/0x26 index 0) recovers the last reading after connect.

This module:
  - Parses HTP the way companion does (with SIG FLOAT fallback)
  - Still understands TICD 8-byte frames via thermometer.py helpers
"""

from __future__ import annotations

from datetime import datetime
from typing import Any, Optional, Union

from ..common.hexutil import format_hex_dump
from ..models import (
    DeviceBrand,
    ParseError,
    TemperatureSite,
    TemperatureUnit,
    ThermometerReading,
)
from .thermometer import ThermometerParser
from .htp import decode_float_11073, parse_temperature_measurement

ParseResult = Union[ThermometerReading, dict]


def parse_htp_companion_style(payload: bytes | bytearray) -> ThermometerReading:
    """
    Match BLEDeviceNT100B handler:
      len >= 12 preferred; decode temp via SFLOAT([1],[2]) * 10^((int8)[4])
      measured_at = now (companion does not use packet time for display path)
    Falls back to standard SIG FLOAT if that fails.
    """
    data = bytes(payload)
    if len(data) < 5:
        raise ParseError(f"NT-100B HTP too short ({len(data)})", data)

    temp: Optional[float] = None
    # Companion formula (works when 24-bit mantissa high byte is 0)
    if len(data) >= 5:
        try:
            from ..common.sfloat import decode_sfloat

            base = decode_sfloat(data, 1)
            exp = data[4] if len(data) > 4 else 0
            if exp >= 128:
                exp = exp - 256
            if base is not None:
                temp = float(base) * (10.0 ** exp)
        except Exception:  # noqa: BLE001
            temp = None

    if temp is None or temp <= 0 or temp > 50:
        # Standard SIG 32-bit FLOAT at [1:5]
        try:
            temp = decode_float_11073(data, 1)
        except ParseError:
            # Last resort: full SIG parse
            return parse_temperature_measurement(data)

    # Sanity: body temps
    if temp is not None and (temp < 20 or temp > 45):
        # Might be °F or mis-decoded; try pure SIG
        try:
            sig = parse_temperature_measurement(data)
            if 20 <= sig.object_temperature <= 45:
                sig.model = "NT-100B"
                sig.brand = DeviceBrand.THERMO
                if sig.measured_at is None:
                    sig.measured_at = datetime.now()
                return sig
        except ParseError:
            pass

    return ThermometerReading(
        object_temperature=float(temp),
        ambient_temperature=None,
        unit=TemperatureUnit.CELSIUS,
        site=TemperatureSite.FOREHEAD,
        measured_at=datetime.now(),
        brand=DeviceBrand.THERMO,
        model="NT-100B",
        raw_hex=format_hex_dump(data),
    )


class Nt100bCompanionParser:
    """
    Dual-channel parser for NT-100B:
      - HTP 0x2A1C indications (companion primary)
      - TICD 0x1524 frames (history / post-measure recovery)
    """

    name = "nipro_nt100b"
    brand = DeviceBrand.THERMO

    def __init__(self) -> None:
        self.model = "NT-100B"
        self._ticd = ThermometerParser()

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        u = (characteristic_uuid or "").lower().replace("-", "")
        if "2a1c" in u or "1809" in u:
            return len(payload) >= 5
        if "1524" in u or "1523" in u:
            return len(payload) >= 3
        # bare payloads
        if len(payload) == 8 and payload[0] == 0x51:
            return True
        if len(payload) >= 5 and len(payload) <= 15:
            return True
        return False

    def parse(
        self, payload: bytes | bytearray, characteristic_uuid: str = ""
    ) -> ParseResult:
        u = (characteristic_uuid or "").lower().replace("-", "")
        data = bytes(payload)

        # TICD framed
        if (
            "1524" in u
            or "1523" in u
            or (len(data) == 8 and data[0] == 0x51)
        ):
            return self._ticd.parse(data)

        # HTP
        if "2a1c" in u or "1809" in u or len(data) >= 5:
            try:
                return parse_htp_companion_style(data)
            except ParseError:
                if len(data) == 8 and data[0] == 0x51:
                    return self._ticd.parse(data)
                raise

        raise ParseError("NT-100B: unrecognized payload", data)

    def set_history_index(self, index: int) -> None:
        self._ticd.set_history_index(index)

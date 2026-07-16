"""
Classic Omron 14-byte vital record parsers.

Sources:
  - hass-omron record_parsers.py
  - omblepy deviceSpecific drivers (7322 / 7600 / 6232 / 7530)

Raises ValueError when the slot is empty or invalid (caller skips the slot).
"""

from __future__ import annotations

import datetime
from typing import Any

from omron_bp.models.parsers.bit_utils import bits_to_int


def parse_classic_vital_14(data: bytes | bytearray, endianness: str = "little") -> dict[str, Any]:
    """
    Byte-aligned 14-byte vital record (modern FE4A / HEM-7146T family).

    Layout (hass-omron parse_classic_vital_14):
      [0]     raw_sys  (sys = raw_sys + 25); >0xE1 → empty
      [1]     dia
      [2]     bpm
      [3]     year = 2000 + (byte & 0x3F)
      [4..5]  flags1: hour, day, month, ihb, mov
      [6..7]  flags2: second, minute, cuff, battery, pos
    """
    if len(data) < 8:
        raise ValueError(f"record too short: {len(data)} bytes")

    raw_sys = data[0]
    if raw_sys > 0xE1:
        raise ValueError("record slot is empty (sys sentinel)")

    record: dict[str, Any] = {
        "sys": raw_sys + 25,
        "dia": data[1],
        "bpm": data[2],
    }

    year = 2000 + (data[3] & 0x3F)
    flags1 = data[4] | (data[5] << 8)
    flags2 = data[6] | (data[7] << 8)

    hour = flags1 & 0x1F
    day = (flags1 >> 5) & 0x1F
    month = (flags1 >> 10) & 0x0F
    record["ihb"] = (flags1 >> 14) & 0x01
    record["mov"] = (flags1 >> 15) & 0x01
    second = min(flags2 & 0x3F, 59)
    minute = min((flags2 >> 6) & 0x3F, 59)
    record["cuff"] = (flags2 >> 12) & 0x01
    record["battery"] = (flags2 >> 13) & 0x01
    record["pos"] = (flags2 >> 14) & 0x03

    if (
        data[1] == 0
        and data[2] == 0
        and (data[3] & 0x3F) == 0
        and flags1 == 0
        and flags2 == 0
    ):
        raise ValueError("record slot is empty (all zeros)")

    if not (1 <= month <= 12) or not (1 <= day <= 31) or hour > 23:
        raise ValueError("record slot is empty / invalid date")

    record["datetime"] = datetime.datetime(year, month, day, hour, minute, second)
    return record


def parse_classic_vital_14_bitpacked(
    data: bytes | bytearray, endianness: str = "big"
) -> dict[str, Any]:
    """
    Bit-packed 14-byte vital record (HEM-7322T / HEM-7600T family).

    Year at bits 16..23. Matches hass-omron + omblepy 7322/7600.
    """
    if len(data) < 8:
        raise ValueError(f"record too short: {len(data)} bytes")
    if data[: min(14, len(data))] == b"\xff" * min(14, len(data)):
        raise ValueError("record slot is empty (0xFF)")

    dia = bits_to_int(data, endianness, 0, 7)
    sys = bits_to_int(data, endianness, 8, 15) + 25
    year = bits_to_int(data, endianness, 16, 23) + 2000
    bpm = bits_to_int(data, endianness, 24, 31)
    mov = bits_to_int(data, endianness, 32, 32)
    ihb = bits_to_int(data, endianness, 33, 33)
    month = bits_to_int(data, endianness, 34, 37)
    day = bits_to_int(data, endianness, 38, 42)
    hour = bits_to_int(data, endianness, 43, 47)
    minute = bits_to_int(data, endianness, 52, 57)
    second = min(bits_to_int(data, endianness, 58, 63), 59)
    pos = bits_to_int(data, endianness, 48, 49)
    battery = bits_to_int(data, endianness, 50, 50)
    cuff = bits_to_int(data, endianness, 51, 51)

    if not (1 <= month <= 12) or not (1 <= day <= 31) or hour > 23:
        raise ValueError("record slot is empty / invalid date")

    return {
        "sys": sys,
        "dia": dia,
        "bpm": bpm,
        "mov": mov,
        "ihb": ihb,
        "pos": pos,
        "battery": battery,
        "cuff": cuff,
        "datetime": datetime.datetime(year, month, day, hour, minute, second),
    }


def parse_classic_vital_14_6232_family(
    data: bytes | bytearray, endianness: str = "big"
) -> dict[str, Any]:
    """
    Bit-packed 14-byte record for HEM-6232T / 7530T-style year packing.

    Year at bits 18..23 (not 16..23). Matches omblepy 6232/7530 and
    hass-omron parse_classic_vital_14_6232_family.
    """
    if len(data) < 8:
        raise ValueError(f"record too short: {len(data)} bytes")
    if data[: min(14, len(data))] == b"\xff" * min(14, len(data)):
        raise ValueError("record slot is empty (0xFF)")

    dia = bits_to_int(data, endianness, 0, 7)
    sys = bits_to_int(data, endianness, 8, 15) + 25
    year = bits_to_int(data, endianness, 18, 23) + 2000
    bpm = bits_to_int(data, endianness, 24, 31)
    mov = bits_to_int(data, endianness, 32, 32)
    ihb = bits_to_int(data, endianness, 33, 33)
    month = bits_to_int(data, endianness, 34, 37)
    day = bits_to_int(data, endianness, 38, 42)
    hour = bits_to_int(data, endianness, 43, 47)
    minute = bits_to_int(data, endianness, 52, 57)
    second = min(bits_to_int(data, endianness, 58, 63), 59)
    pos = bits_to_int(data, endianness, 48, 49)
    battery = bits_to_int(data, endianness, 50, 50)
    cuff = bits_to_int(data, endianness, 51, 51)

    if not (1 <= month <= 12) or not (1 <= day <= 31) or hour > 23:
        raise ValueError("record slot is empty / invalid date")

    return {
        "sys": sys,
        "dia": dia,
        "bpm": bpm,
        "mov": mov,
        "ihb": ihb,
        "pos": pos,
        "battery": battery,
        "cuff": cuff,
        "datetime": datetime.datetime(year, month, day, hour, minute, second),
    }

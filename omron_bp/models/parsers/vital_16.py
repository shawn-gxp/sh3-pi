"""
16-byte Omron vital record parsers.

  parse_vital_16_715x_bitpacked
      omblepy HEM-7150T / 7155T / 7342T / 7361T (little-endian bitfields).
      Empirically validated in omblepy; prefer over byte-aligned for these models.

  parse_classic_vital_16_6401_family
      hass-omron HEM-6401T wrist family.
"""

from __future__ import annotations

import datetime
from typing import Any

from omron_bp.models.parsers.bit_utils import bits_to_int


def parse_vital_16_715x_bitpacked(
    data: bytes | bytearray, endianness: str = "little"
) -> dict[str, Any]:
    """16-byte bit-packed layout used by omblepy 7150/7155/7342/7361 drivers."""
    if len(data) < 16:
        raise ValueError(f"record too short: {len(data)} bytes")
    if data[:16] == b"\xff" * 16:
        raise ValueError("record slot is empty (0xFF)")

    minute = bits_to_int(data, endianness, 68, 73)
    second = min(bits_to_int(data, endianness, 74, 79), 59)
    mov = bits_to_int(data, endianness, 80, 80)
    ihb = bits_to_int(data, endianness, 81, 81)
    month = bits_to_int(data, endianness, 82, 85)
    day = bits_to_int(data, endianness, 86, 90)
    hour = bits_to_int(data, endianness, 91, 95)
    year = bits_to_int(data, endianness, 98, 103) + 2000
    bpm = bits_to_int(data, endianness, 104, 111)
    dia = bits_to_int(data, endianness, 112, 119)
    sys = bits_to_int(data, endianness, 120, 127) + 25

    if not (1 <= month <= 12) or not (1 <= day <= 31) or hour > 23:
        raise ValueError("record slot is empty / invalid date")

    return {
        "sys": sys,
        "dia": dia,
        "bpm": bpm,
        "mov": mov,
        "ihb": ihb,
        "datetime": datetime.datetime(year, month, day, hour, minute, second),
    }


def parse_classic_vital_16_6401_family(
    data: bytes | bytearray, endianness: str = "little"
) -> dict[str, Any]:
    """16-byte layout for HEM-6401 wrist family (hass-omron)."""
    if len(data) < 16:
        raise ValueError("record too short")

    year_off = int(data[0])
    month = int(data[1])
    day = int(data[2])
    hour = int(data[3])
    minute = int(data[4])
    second = min(int(data[5]), 59)
    raw_sys = int(data[6])
    dia = int(data[7])
    bpm = int(data[8])

    if (
        year_off == 0
        and month == 0
        and day == 0
        and hour == 0
        and minute == 0
        and int(data[5]) == 0
        and raw_sys == 0
        and dia == 0
        and bpm == 0
    ):
        raise ValueError("record slot is empty")

    if raw_sys > 0xE1:
        raise ValueError("record slot is empty")

    flags = int(data[11])
    return {
        "sys": raw_sys + 25,
        "dia": dia,
        "bpm": bpm,
        "ihb": flags & 0x03,
        "mov": (flags >> 2) & 0x03,
        "datetime": datetime.datetime(
            year_off + 2000, month, day, hour, minute, second
        ),
    }

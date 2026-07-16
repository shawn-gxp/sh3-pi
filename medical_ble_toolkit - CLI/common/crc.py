"""CRC helpers for proprietary framed protocols (e.g. Masimo MightySat)."""

from __future__ import annotations


def crc8_ccitt(data: bytes | bytearray, poly: int = 0x07, init: int = 0x00) -> int:
    """
    CRC-8-CCITT as used by MightySat:
      polynomial P(x) = x^8 + x^2 + x + 1  → 0x07
      initial seed 0x00

    Covers PAYLOAD bytes only (after LEN, before CRC byte).
    """
    crc = init & 0xFF
    for byte in data:
        crc ^= byte
        for _ in range(8):
            if crc & 0x80:
                crc = ((crc << 1) ^ poly) & 0xFF
            else:
                crc = (crc << 1) & 0xFF
    return crc

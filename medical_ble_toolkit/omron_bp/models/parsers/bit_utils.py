"""Shared bit-field helpers for EEPROM record parsers."""

from __future__ import annotations


def bits_to_int(
    data: bytes | bytearray,
    endianness: str,
    first_bit: int,
    last_bit: int,
) -> int:
    """Extract integer from inclusive bit range [first_bit, last_bit] in buffer."""
    big_int = int.from_bytes(data, endianness)
    num_bits = (last_bit - first_bit) + 1
    shifted = big_int >> (len(data) * 8 - (last_bit + 1))
    return shifted & ((1 << num_bits) - 1)

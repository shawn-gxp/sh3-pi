"""
IEEE 11073-20601 SFLOAT (16-bit) codec.

Used by Bluetooth SIG profiles:
  - Blood Pressure Measurement (0x2A35)  — Beurer BM54, A&D UA-651BLE
  - Pulse Rate fields, etc.

Wire layout (little-endian):
  bits 0..11  : mantissa (signed 12-bit two's complement)
  bits 12..15 : exponent (signed 4-bit two's complement)

value = mantissa * 10^exponent

Special values (mantissa):
  0x07FF NaN | 0x0800 NRes | 0x07FE +INFINITY | 0x0802 -INFINITY | 0x0801 reserved
"""

from __future__ import annotations

from typing import Optional


_NAN = 0x07FF
_NRES = 0x0800
_POS_INF = 0x07FE
_NEG_INF = 0x0802
_RESERVED = 0x0801


def decode_sfloat(data: bytes | bytearray, offset: int = 0) -> Optional[float]:
    """Decode 2 little-endian bytes at *offset* to float, or None for NaN/NRes."""
    if offset + 2 > len(data):
        raise IndexError(
            f"SFLOAT needs 2 bytes at offset {offset}, payload len={len(data)}"
        )
    raw = data[offset] | (data[offset + 1] << 8)
    mantissa = raw & 0x0FFF
    exponent = (raw >> 12) & 0x0F

    # Sign-extend mantissa (12-bit)
    if mantissa & 0x0800:
        mantissa = mantissa - 0x1000
    # Sign-extend exponent (4-bit)
    if exponent & 0x08:
        exponent = exponent - 0x10

    special = raw & 0x0FFF
    if special in (_NAN, _NRES, _RESERVED):
        return None
    if special == _POS_INF:
        return float("inf")
    if special == _NEG_INF:
        return float("-inf")

    return float(mantissa * (10 ** exponent))


def encode_sfloat(value: float, exponent: int = 0) -> bytes:
    """Encode float → 2 LE bytes (for unit tests / golden vectors)."""
    if value != value:  # NaN
        raw = _NAN
    elif value == float("inf"):
        raw = _POS_INF
    elif value == float("-inf"):
        raw = _NEG_INF
    else:
        mantissa = int(round(value / (10 ** exponent)))
        if mantissa < -2048 or mantissa > 2047:
            raise ValueError(f"mantissa {mantissa} out of 12-bit range")
        if exponent < -8 or exponent > 7:
            raise ValueError(f"exponent {exponent} out of 4-bit range")
        mant_u = mantissa & 0x0FFF
        exp_u = exponent & 0x0F
        raw = mant_u | (exp_u << 12)
    return bytes((raw & 0xFF, (raw >> 8) & 0xFF))

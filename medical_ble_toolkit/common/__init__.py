"""Shared pure-logic codecs and forensic helpers (no BLE I/O)."""

from .sfloat import decode_sfloat, encode_sfloat
from .hexutil import format_hex_dump, ms_timestamp
from .crc import crc8_ccitt
from .winrt_errors import (
    ble_log_tag,
    classify_ble_error,
    format_diagnosis,
    is_linux,
    is_windows,
    os_pair_supported,
    pairing_ui_hint,
    remove_bond_instructions,
)

__all__ = [
    "decode_sfloat",
    "encode_sfloat",
    "format_hex_dump",
    "ms_timestamp",
    "crc8_ccitt",
    "classify_ble_error",
    "format_diagnosis",
    "is_windows",
    "is_linux",
    "os_pair_supported",
    "ble_log_tag",
    "pairing_ui_hint",
    "remove_bond_instructions",
]

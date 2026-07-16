"""
Beurer scale command builders (BF700 opcode table from APK).

Measurement body field offsets remain partial — commands + state machine
are companion-parity ready; parse improves with live captures.
"""

from __future__ import annotations

from typing import Optional

# BF700 commands (hex from TIER1_TIER2_PROTOCOLS.md)
CMD_TYPE1_INIT = bytes([0xF6, 0x01])
CMD_TYPE1_SET_TIME = bytes([0xF9])
CMD_TYPE2_INIT = bytes([0xE6, 0x01])
CMD_TYPE2_SET_TIME = bytes([0xE9])
CMD_SET_UNIT = bytes([0x4D])
CMD_FIRST_USER = bytes([0x33])
CMD_NEXT_USER = bytes([0xF1, 0x34])
CMD_USER_INFORMATION = bytes([0x36])
CMD_GET_MEASUREMENT = bytes([0x41])
CMD_NEXT_MEASUREMENT = bytes([0xF1, 0x42])
CMD_GET_SCALE_STATUS = bytes([0x4F])
CMD_TAKE_MEASUREMENT = bytes([0x40])

# Typical proprietary services (model-dependent)
SCALE_FFF0 = "0000fff0-0000-1000-8000-00805f9b34fb"
SCALE_FFFF = "0000ffff-0000-1000-8000-00805f9b34fb"
WEIGHT_SCALE_SVC = "0000181d-0000-1000-8000-00805f9b34fb"
WEIGHT_MEASUREMENT = "00002a9d-0000-1000-8000-00805f9b34fb"
BODY_COMPOSITION = "0000181b-0000-1000-8000-00805f9b34fb"
BODY_COMP_MEASUREMENT = "00002a9c-0000-1000-8000-00805f9b34fb"


def prepare_command(request_byte: Optional[int], command: bytes) -> bytes:
    """App prepareCommand: optional series-type requestByte || command.bytes."""
    if request_byte is None:
        return bytes(command)
    return bytes([request_byte & 0xFF]) + bytes(command)


def cmd_get_measurement(request_byte: Optional[int] = None) -> bytes:
    return prepare_command(request_byte, CMD_GET_MEASUREMENT)


def cmd_next_measurement(request_byte: Optional[int] = None) -> bytes:
    return prepare_command(request_byte, CMD_NEXT_MEASUREMENT)


def cmd_init_type1(request_byte: Optional[int] = None) -> bytes:
    return prepare_command(request_byte, CMD_TYPE1_INIT)


def cmd_set_time_type1(request_byte: Optional[int] = None) -> bytes:
    return prepare_command(request_byte, CMD_TYPE1_SET_TIME)


def cmd_init_type2(request_byte: Optional[int] = None) -> bytes:
    return prepare_command(request_byte, CMD_TYPE2_INIT)


class BeurerScaleParser:
    """Best-effort: hex dump + length; full body parser when capture available."""

    name = "beurer_scale"
    brand = "beurer"

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        return True

    def parse(self, payload: bytes | bytearray):
        data = bytes(payload)
        return {
            "type": "scale_raw",
            "length": len(data),
            "raw_hex": " ".join(f"{b:02X}" for b in data),
            "notes": "BF700 command path implemented; body layout needs live capture validation",
        }

"""
FORA / TaiDoc-bus parser (first-party frame helpers).

Full clinical multiparam packing for FORA 6 still refined by live HCI;
frame build/parse and BG value/meal flags follow iFORA Smart + td42xx.
"""
from __future__ import annotations

from typing import Any, Optional

from ..common.hexutil import format_hex_dump
from ..models import DeviceBrand, MultiParameterReading, ParseError
from medical_ble_toolkit.brands.fora import protocol as P


class ForaParser:
    name = "fora_6_connect"
    brand = DeviceBrand.FORA

    def __init__(self):
        self.model = "FORA 6 Connect"

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        uuid = (characteristic_uuid or "").lower()
        if "1524" in uuid or "1523" in uuid or "fora" in uuid:
            return len(bytes(payload)) >= 8 and bytes(payload)[0] in (P.FRAME_START, 0x77)
        return len(bytes(payload)) >= 8 and bytes(payload)[0] == P.FRAME_START

    def parse(self, payload: bytes | bytearray) -> MultiParameterReading:
        data = bytes(payload)
        if not data:
            raise ParseError("Empty FORA payload", data)
        frame = P.parse_frame(data)
        if frame is None:
            raise ParseError("Not a TaiDoc frame", data)
        if not frame["checksum_ok"]:
            raise ParseError(f"TaiDoc checksum fail cmd=0x{frame['command']:02X}", data)

        cmd = frame["command"]
        msg = frame["message"]
        # Record value frame (0x26): LE u16 glucose + meal flags in high byte of last field
        if cmd == P.CMD_RECORD_VALUE:
            value = int(msg[0]) | (int(msg[1]) << 8)
            meal = int(msg[3]) if len(msg) > 3 else 0
            meal_tag = None
            if meal & 0x40:
                meal_tag = "AC"
            elif meal & 0x80:
                meal_tag = "PC"
            if value == P.BG_INVALID:
                raise ParseError("Invalid BG (0xFFFF)", data)
            return MultiParameterReading(
                blood_glucose_mg_dl=float(value),
                meal_tag=meal_tag,
                brand=DeviceBrand.FORA,
                model=self.model,
                raw_hex=format_hex_dump(data),
                notes=f"cmd=0x{cmd:02X} meal_raw=0x{meal:02X}",
            )

        raise NotImplementedError(
            f"FORA clinical decode for cmd=0x{cmd:02X} msg={msg.hex()} "
            f"— use session history path; see datasheets/FORA/FORA_FIRST_PARTY_PROTOCOL.md"
        )

    def parse_or_raw(self, payload: bytes | bytearray) -> dict[str, Any]:
        data = bytes(payload)
        out: dict[str, Any] = {
            "type": "fora_frame",
            "length": len(data),
            "raw_hex": format_hex_dump(data),
            "service_uuid": P.SERVICE_UUID,
            "char_uuid": P.CHAR_UUID,
        }
        frame = P.parse_frame(data)
        if frame:
            out["frame"] = {
                k: (v.hex() if isinstance(v, (bytes, bytearray)) else v)
                for k, v in frame.items()
                if k != "raw"
            }
            if frame.get("command") == P.CMD_RECORD_VALUE and frame.get("checksum_ok"):
                msg = frame["message"]
                out["value_u16"] = int(msg[0]) | (int(msg[1]) << 8)
                out["meal_flags"] = int(msg[3]) if len(msg) > 3 else 0
        return out

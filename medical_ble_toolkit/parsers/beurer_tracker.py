"""
Beurer activity tracker UUID maps + RE-friendly parsers.

Exact opcodes are partially documented (HIGH UUIDs/flow, MEDIUM command bytes).
Session writes known structure; responses logged as structured hex until full
opcode tables are extracted from per-repo decompile.
"""

from __future__ import annotations

from typing import Any, Dict

from ..common.hexutil import format_hex_dump

AS87 = {
    "scan": "7905ff00-b5ce-4e99-a40f-4b1e122d00d0",
    "service": "d0a2ff00-2996-d38b-e214-86515df5a1df",
    "char_a": "7905ff01-b5ce-4e99-a40f-4b1e122d00d0",
    "char_b": "7905ff02-b5ce-4e99-a40f-4b1e122d00d0",
}
AS98 = {
    "scan": "0000fff0-0000-1000-8000-00805f9b34fb",
    "service": "0000fff0-0000-1000-8000-00805f9b34fb",
    "char_a": "0000fff6-0000-1000-8000-00805f9b34fb",
    "char_b": "0000fff7-0000-1000-8000-00805f9b34fb",
}
AS99 = {
    "scan": "00006006-0000-1000-8000-00805f9b34fb",
    "service": "00006006-0000-1000-8000-00805f9b34fb",
    "char_a": "00008001-0000-1000-8000-00805f9b34fb",
    "char_b": "00008002-0000-1000-8000-00805f9b34fb",
}


class BeurerTrackerParser:
    name = "beurer_tracker"

    def __init__(self, model: str = ""):
        self.model = model

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        return True

    def parse(self, payload: bytes | bytearray) -> Dict[str, Any]:
        data = bytes(payload)
        return {
            "type": "tracker_raw",
            "model": self.model,
            "length": len(data),
            "raw_hex": format_hex_dump(data),
            "notes": "UUID session armed; opcode tables partial — capture refine",
        }

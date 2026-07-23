"""
FORA 6 Connect parser scaffold.

Documentation status (datasheets/FORA):
  - Brochure only: multi-parameter meter (BG, HCT, HB, β-Ketone, UA, TCH)
  - Bluetooth connectivity claimed; NO wire protocol / GATT map published

This module exists so the toolkit architecture already has a FORA slot.
When RE captures arrive, implement parse() without touching ble_client.py.
"""

from __future__ import annotations

from datetime import datetime
from typing import Any

from ..common.hexutil import format_hex_dump
from ..models import DeviceBrand, MultiParameterReading, ParseError


class ForaParser:
    """
    Placeholder parser — raises ParseError with hex dump until protocol is known.

    RE checklist (fill in when discovered):
      [ ] Advertising name / manufacturer company ID
      [ ] Service UUID(s)
      [ ] Notify characteristic UUID
      [ ] Frame sync / length / checksum
      [ ] Field offsets for BG, HCT, HB, ketone, UA, cholesterol
      [ ] Meal tag encoding (AC/PC/AutoQC)
      [ ] Timestamp encoding
    """

    name = "fora_6_connect"
    brand = DeviceBrand.FORA

    def __init__(self):
        self.model = "FORA 6 Connect"

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        # Never auto-claim until protocol is reverse-engineered
        uuid = characteristic_uuid.lower()
        return "fora" in uuid  # only if profile injects a marker UUID

    def parse(self, payload: bytes | bytearray) -> MultiParameterReading:
        data = bytes(payload)
        if not data:
            raise ParseError("Empty FORA payload", data)

        raise NotImplementedError(
            "FORA parser is not yet implemented. "
            "Please collect hex logs and reverse engineer the protocol."
        )

    def parse_or_raw(self, payload: bytes | bytearray) -> dict[str, Any]:
        """RE helper: always return dict with hex, never throw on length."""
        data = bytes(payload)
        return {
            "type": "fora_unparsed",
            "length": len(data),
            "raw_hex": format_hex_dump(data),
            "notes": "Map these bytes after triggering a strip test on the meter",
        }

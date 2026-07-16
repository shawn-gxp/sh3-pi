"""
Beurer ECG + BP combo (BM93/95/96, ME90/95).

BP path: same SIG 0x2A35 as bp_sig.
ECG path: proprietary 6E80… UUIDs (headers/waveforms — partial).
"""

from __future__ import annotations

from typing import Any

from ..common.hexutil import format_hex_dump
from .blood_pressure import parse_blood_pressure_measurement
from ..models import DeviceBrand

ECG_SERVICE = "6e800001-b5a3-f393-e0a9-e50e24dcca9e"
ECG_CMD = "6e800002-b5a3-f393-e0a9-e50e24dcca9e"
ECG_DATA = "6e800003-b5a3-f393-e0a9-e50e24dcca9e"
ECG_ALT = "6e800004-b5a3-f393-e0a9-e50e24dcca9e"
OTA_SERVICE = "6e801000-b5a3-f393-e0a9-e50e24dcca9e"
HR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
BP_MEASUREMENT = "00002a35-0000-1000-8000-00805f9b34fb"


class BeurerEcgParser:
    name = "beurer_ecg"
    brand = DeviceBrand.BEURER

    def __init__(self, model: str = ""):
        self.model = model

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        return True

    def parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> Any:
        u = characteristic_uuid.lower().replace("-", "")
        data = bytes(payload)
        if "2a35" in u:
            return parse_blood_pressure_measurement(
                data, brand=DeviceBrand.BEURER, model=self.model
            )
        return {
            "type": "ecg_raw",
            "model": self.model,
            "uuid": characteristic_uuid,
            "length": len(data),
            "raw_hex": format_hex_dump(data),
            "notes": "BP via 2A35 fully parsed; ECG waveform encoding partial",
        }

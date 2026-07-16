"""
Medical BLE Reverse-Engineering Toolkit
=======================================

Strict layering for Python prototype → Kotlin Android port:

  models.py          → pure dataclasses (domain schema)
  parsers/*          → pure bytes → dataclass (NO bleak / I/O)
  ble_client.py      → bleak discovery / connect / notify only
  common/*           → shared codecs (SFLOAT, CRC, hex dumps)

Kotlin mapping:
  models.py       → data class BloodPressureReading, etc.
  parsers/*       → interface VitalParser { fun parse(bytes: ByteArray): T }
  ble_client.py   → BluetoothGatt / BleManager that calls VitalParser
"""

from .models import (
    BloodPressureReading,
    PulseOximeterData,
    ThermometerReading,
    MultiParameterReading,
    ParseError,
    RawPayload,
)

__all__ = [
    "BloodPressureReading",
    "PulseOximeterData",
    "ThermometerReading",
    "MultiParameterReading",
    "ParseError",
    "RawPayload",
]

__version__ = "0.1.0"

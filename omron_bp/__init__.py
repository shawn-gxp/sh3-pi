"""
Omron BP BLE toolkit — multi-model foundation.

Layers (keep them separate so debugging and later integration stay easy):

  cli.py              User-facing menu / CLI (pair | read)
  logging_config.py   Logging setup; debug lines marked for release cleanup
  ble/                Radio: scan, connect, GATT session, Omron TX/RX protocol
  pairing/            Pair workflows (OS bond vs unlock-key)
  readout/            Read workflows (EEPROM → records)
  models/             Device profiles + parsers (one model file = one cuff family)
  export/             CSV / future formats
  config/             Saved device preference (MAC, model)

Target: many Omron models now; other brands later via the same layer pattern.
"""

__version__ = "0.1.0"
__app_name__ = "omron_bp"

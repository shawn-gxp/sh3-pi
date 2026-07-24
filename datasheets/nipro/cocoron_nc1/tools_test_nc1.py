#!/usr/bin/env python3
"""
Live / offline test entry for Nipro Cocoron NC-1BLE.

Thin wrapper around medical_ble_toolkit.brands.nipro.nc1_session so you can
run from the datasheets folder without thinking about package paths.

Usage (from repo root):

  python datasheets/nipro/cocoron_nc1/tools_test_nc1.py selftest
  python datasheets/nipro/cocoron_nc1/tools_test_nc1.py scan -t 15
  python datasheets/nipro/cocoron_nc1/tools_test_nc1.py run -a <ADDR> -t 60 --csv out.csv

Equivalent:

  python -m medical_ble_toolkit.brands.nipro.nc1_session …
"""

from __future__ import annotations

import sys
from pathlib import Path

# Ensure repo root is on sys.path when launched as a script
_ROOT = Path(__file__).resolve().parents[3]
if str(_ROOT) not in sys.path:
    sys.path.insert(0, str(_ROOT))

from medical_ble_toolkit.brands.nipro.nc1_session import main  # noqa: E402

if __name__ == "__main__":
    raise SystemExit(main())

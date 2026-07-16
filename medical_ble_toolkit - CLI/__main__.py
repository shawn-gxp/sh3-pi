"""
Entry points:

  python -m medical_ble_toolkit                 → interactive wizard (default brand: Omron)
  python -m medical_ble_toolkit interactive     → same
  python -m medical_ble_toolkit omron pair ...  → direct Omron commands
  python -m medical_ble_toolkit --profile …     → direct connect / RE mode
"""

from .ble_client import main

raise SystemExit(main())

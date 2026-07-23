#!/usr/bin/env bash
# Bootstrap Medical BLE toolkit on Linux (BlueZ).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "============================================================"
echo "  Medical BLE — Linux setup"
echo "  $ROOT"
echo "============================================================"

if ! command -v python3 >/dev/null 2>&1; then
  echo "ERROR: python3 not found"
  exit 1
fi

echo "Python: $(python3 --version)"

if [[ ! -x .venv/bin/python ]]; then
  echo "Creating virtualenv .venv …"
  if python3 -m venv .venv 2>/dev/null; then
    :
  elif python3 -m virtualenv .venv 2>/dev/null; then
    :
  else
    echo "Installing virtualenv into user site…"
    python3 -m pip install --user --break-system-packages virtualenv
    export PATH="${HOME}/.local/bin:${PATH}"
    python3 -m virtualenv .venv
  fi
fi

.venv/bin/pip install -U pip
.venv/bin/pip install \
  -r requirements.txt \
  -r medical_ble_web/requirements.txt

echo ""
echo "Bluetooth check:"
if command -v bluetoothctl >/dev/null 2>&1; then
  bluetoothctl show 2>/dev/null | head -8 || true
  if ! bluetoothctl show 2>/dev/null | grep -q "Powered: yes"; then
    echo "  Adapter not powered — try: bluetoothctl power on"
  fi
else
  echo "  bluetoothctl not found — install bluez"
fi

echo ""
echo "Parser self-test…"
.venv/bin/python -m medical_ble_toolkit.tests.test_parsers

echo ""
echo "OK. Next:"
echo "  ./run_web.sh                 # http://127.0.0.1:8741"
echo "  ./run_toolkit.sh             # interactive CLI"
echo "  See LINUX.md for Omron pair/read and troubleshooting."

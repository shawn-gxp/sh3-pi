#!/usr/bin/env bash
# Interactive medical BLE toolkit CLI (Linux / BlueZ)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
VENV="$ROOT/.venv"

if [[ ! -x "$VENV/bin/python" ]]; then
  echo "ERROR: venv missing at $VENV"
  echo "Run:  ./setup_linux.sh"
  exit 1
fi

export PYTHONPATH="$ROOT${PYTHONPATH:+:$PYTHONPATH}"
cd "$ROOT"
exec "$VENV/bin/python" -m medical_ble_toolkit "$@"

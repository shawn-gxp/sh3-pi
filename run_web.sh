#!/usr/bin/env bash
# Linux launcher for Medical BLE Web POC (equivalent to run_web.ps1)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
WEB="$ROOT/medical_ble_web"
PORT="${PORT:-8741}"
VENV="$ROOT/.venv"
SKIP_INSTALL="${SKIP_INSTALL:-0}"

if [[ ! -x "$VENV/bin/python" ]]; then
  echo "ERROR: venv missing at $VENV"
  echo "Run:  ./setup_linux.sh"
  exit 1
fi

if [[ "$SKIP_INSTALL" != "1" ]]; then
  if ! "$VENV/bin/python" -c "import fastapi, uvicorn, bleak" 2>/dev/null; then
    echo "Installing dependencies…"
    "$VENV/bin/pip" install \
      -r "$ROOT/requirements.txt" \
      -r "$WEB/requirements.txt" \
      -r "$ROOT/omron_bp/requirements.txt"
  fi
fi

export PYTHONPATH="$ROOT${PYTHONPATH:+:$PYTHONPATH}"
cd "$WEB"

echo "============================================================"
echo "  Medical BLE Web POC (Linux / BlueZ)"
echo "  $WEB"
echo "  http://127.0.0.1:$PORT"
echo "  SQLite: $WEB/data/poc.db"
echo "  Stop:   Ctrl+C"
echo "============================================================"

exec "$VENV/bin/python" -c \
  "import uvicorn; uvicorn.run('app:app', host='127.0.0.1', port=$PORT, reload=False, log_level='info')"

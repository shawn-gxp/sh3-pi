#!/usr/bin/env bash
# Linux launcher for Medical BLE Web (Pi hub / LAN access)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
WEB="$ROOT/medical_ble_web"
PORT="${PORT:-8741}"
# Pi appliance: reachable from phone on same WiFi. Override: HOST=127.0.0.1
HOST="${HOST:-0.0.0.0}"
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
      -r "$WEB/requirements.txt"
  fi
fi

export PYTHONPATH="$ROOT${PYTHONPATH:+:$PYTHONPATH}"
# Fixed Nipro registry next to SQLite (optional override)
export MEDICAL_NIPRO_REGISTRY="${MEDICAL_NIPRO_REGISTRY:-$WEB/data/nipro_paired_devices.json}"
cd "$WEB"

# Best-effort LAN IP for operator message
LAN_IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
if [[ -z "${LAN_IP}" ]]; then
  LAN_IP="(this-host-ip)"
fi

echo "============================================================"
echo "  Medical BLE Web (Linux / BlueZ)"
echo "  $WEB"
echo "  bind:  http://${HOST}:${PORT}"
if [[ "$HOST" == "0.0.0.0" || "$HOST" == "::" ]]; then
  echo "  phone: http://${LAN_IP}:${PORT}  (same WiFi as Pi)"
else
  echo "  local: http://${HOST}:${PORT}"
fi
echo "  SQLite: $WEB/data/poc.db"
echo "  Paired: $WEB/data/paired_devices.json"
echo "  Nipro:  $MEDICAL_NIPRO_REGISTRY"
echo "  Stop:   Ctrl+C"
echo "============================================================"

exec "$VENV/bin/python" -c \
  "import uvicorn; uvicorn.run('app:app', host='$HOST', port=$PORT, reload=False, log_level='info')"

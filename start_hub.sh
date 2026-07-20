#!/usr/bin/env bash
# Pi appliance helper: power BLE/WiFi (best effort) + start web hub on LAN.
# Used by systemd (medical-ble-hub.service) and manual ./start_hub.sh.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "============================================================"
echo "  Medical BLE Hub — start"
echo "============================================================"

# Prestart (WiFi radio + BLE power). Idempotent.
if [[ -x "$ROOT/hub_prestart.sh" ]]; then
  # shellcheck disable=SC1091
  bash "$ROOT/hub_prestart.sh" || true
else
  if command -v rfkill >/dev/null 2>&1; then
    rfkill unblock bluetooth 2>/dev/null || true
    rfkill unblock wifi 2>/dev/null || true
  fi
  if command -v bluetoothctl >/dev/null 2>&1; then
    bluetoothctl power on 2>/dev/null || true
  fi
fi

# Optional BlueZ agent for passkey pairing (interactive only; skip under systemd)
if [[ -x "$ROOT/setup_bluez_hub.sh" ]] && [[ "${SKIP_BLUEZ_AGENT:-0}" != "1" ]]; then
  if [[ "$(id -u)" -eq 0 ]]; then
    echo "BlueZ hub helpers (optional)…"
    bash "$ROOT/setup_bluez_hub.sh" 2>/dev/null || true
  fi
fi

export HOST="${HOST:-0.0.0.0}"
export PORT="${PORT:-8741}"
# Boot path must not re-pip every time
export SKIP_INSTALL="${SKIP_INSTALL:-1}"
export PYTHONUNBUFFERED="${PYTHONUNBUFFERED:-1}"
export MEDICAL_NIPRO_REGISTRY="${MEDICAL_NIPRO_REGISTRY:-$ROOT/medical_ble_web/data/nipro_paired_devices.json}"
export PYTHONPATH="$ROOT${PYTHONPATH:+:$PYTHONPATH}"

exec "$ROOT/run_web.sh"

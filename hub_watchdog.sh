#!/usr/bin/env bash
# Health guardrail: if hub HTTP is down, restart systemd unit.
# Used by medical-ble-hub-watchdog.timer (every minute).
set -uo pipefail

PORT="${PORT:-8741}"
UNIT="${MEDICAL_HUB_UNIT:-medical-ble-hub.service}"
URL="http://127.0.0.1:${PORT}/health"

if curl -sf --max-time 5 "$URL" >/dev/null 2>&1; then
  exit 0
fi

echo "[hub-watchdog] $(date -Is) health check FAILED ($URL) — restarting $UNIT"
if command -v systemctl >/dev/null 2>&1; then
  systemctl restart "$UNIT" || true
fi
exit 0

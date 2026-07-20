#!/usr/bin/env bash
# Pre-start for boot / systemd: radio + BLE adapter ready (best-effort).
# Safe to re-run. Does not start the web server.
set -uo pipefail

log() { echo "[hub-prestart] $*"; }

# --- WiFi radio (NetworkManager) ---
if command -v nmcli >/dev/null 2>&1; then
  nmcli radio wifi on 2>/dev/null || true
  # Wait briefly for any configured WiFi to associate (non-fatal)
  for _ in $(seq 1 30); do
    state="$(nmcli -t -f STATE g 2>/dev/null | head -1 || true)"
    if [[ "$state" == "connected" ]]; then
      log "NetworkManager: connected"
      break
    fi
    sleep 1
  done
  log "NetworkManager state: $(nmcli -t -f STATE g 2>/dev/null || echo unknown)"
else
  log "nmcli not found — rely on system network config"
fi

# --- Unblock RF ---
if command -v rfkill >/dev/null 2>&1; then
  rfkill unblock wifi 2>/dev/null || true
  rfkill unblock bluetooth 2>/dev/null || true
fi

# --- Bluetooth adapter power ---
if command -v bluetoothctl >/dev/null 2>&1; then
  # Prefer controller power-on (works without interactive agent)
  bluetoothctl power on 2>/dev/null || true
  # Wait until adapter reports Powered: yes (up to ~20s)
  for _ in $(seq 1 20); do
    if bluetoothctl show 2>/dev/null | grep -q "Powered: yes"; then
      log "Bluetooth: Powered: yes"
      break
    fi
    bluetoothctl power on 2>/dev/null || true
    sleep 1
  done
  if ! bluetoothctl show 2>/dev/null | grep -q "Powered: yes"; then
    log "WARN: Bluetooth not powered yet — service will still start (hub retries scans)"
  fi
else
  log "WARN: bluetoothctl missing"
fi

exit 0

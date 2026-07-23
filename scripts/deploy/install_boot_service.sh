#!/usr/bin/env bash
# Install Medical BLE Hub as a system service (starts at boot, no login).
# Usage:  sudo ./install_boot_service.sh
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run as root:  sudo $0"
  exit 1
fi

ROOT="$(cd "$(dirname "$0")" && pwd)"
UNIT_DIR=/etc/systemd/system
HUB_USER="${SUDO_USER:-sh3}"
HUB_HOME="$(getent passwd "$HUB_USER" | cut -d: -f6)"

if [[ -z "$HUB_HOME" ]]; then
  echo "ERROR: cannot resolve home for user $HUB_USER"
  exit 1
fi

# Prefer repo next to this script (not always under Desktop after move)
REPO="$ROOT"
if [[ ! -x "$REPO/.venv/bin/python" ]]; then
  echo "ERROR: venv missing at $REPO/.venv — run ./setup_linux.sh as $HUB_USER first"
  exit 1
fi

echo "============================================================"
echo "  Install Medical BLE Hub boot service"
echo "  repo: $REPO"
echo "  user: $HUB_USER"
echo "============================================================"

# --- Permissions for BLE without login ---
usermod -aG bluetooth,plugdev "$HUB_USER" 2>/dev/null || true
loginctl enable-linger "$HUB_USER" 2>/dev/null || true

# --- Enable system dependencies ---
systemctl enable bluetooth.service 2>/dev/null || true
systemctl enable NetworkManager.service 2>/dev/null || true
# network-online is optional; do not hard-fail install if missing
systemctl enable NetworkManager-wait-online.service 2>/dev/null || true

# Bluetooth auto-power (if bluez policy exists)
if [[ -f /etc/bluetooth/main.conf ]]; then
  if ! grep -qE '^\s*AutoEnable\s*=' /etc/bluetooth/main.conf 2>/dev/null; then
    if grep -q '^\[Policy\]' /etc/bluetooth/main.conf; then
      sed -i '/^\[Policy\]/a AutoEnable = true' /etc/bluetooth/main.conf
    else
      printf '\n[Policy]\nAutoEnable = true\n' >> /etc/bluetooth/main.conf
    fi
    echo "Set AutoEnable=true in /etc/bluetooth/main.conf"
  fi
fi

# Optional BlueZ hub tweaks (non-fatal)
if [[ -x "$REPO/setup_bluez_hub.sh" ]]; then
  bash "$REPO/setup_bluez_hub.sh" || true
fi

chmod +x \
  "$REPO/hub_prestart.sh" \
  "$REPO/hub_watchdog.sh" \
  "$REPO/hub_open_ui.sh" \
  "$REPO/start_hub.sh" \
  "$REPO/run_web.sh" \
  "$REPO/hub_db_backup.sh"

# --- Render unit files with absolute paths / correct user ---
render_unit() {
  local src="$1" dest="$2"
  sed \
    -e "s|/home/sh3/Desktop/sh3-hw-layer-experiments|$REPO|g" \
    -e "s|^User=sh3|User=$HUB_USER|" \
    -e "s|^Group=sh3|Group=$HUB_USER|" \
    -e "s|/home/sh3/\\.Xauthority|${HUB_HOME}/.Xauthority|g" \
    -e "s|XAUTHORITY=/home/sh3/|XAUTHORITY=${HUB_HOME}/|g" \
    "$src" > "$dest"
  echo "Installed $dest"
}

render_unit "$REPO/systemd/medical-ble-hub.service" \
  "$UNIT_DIR/medical-ble-hub.service"
render_unit "$REPO/systemd/medical-ble-hub-watchdog.service" \
  "$UNIT_DIR/medical-ble-hub-watchdog.service"
render_unit "$REPO/systemd/medical-ble-hub-watchdog.timer" \
  "$UNIT_DIR/medical-ble-hub-watchdog.timer"
render_unit "$REPO/systemd/medical-ble-hub-ui.service" \
  "$UNIT_DIR/medical-ble-hub-ui.service"
render_unit "$REPO/systemd/medical-ble-hub-backup.service" \
  "$UNIT_DIR/medical-ble-hub-backup.service"
render_unit "$REPO/systemd/medical-ble-hub-backup.timer" \
  "$UNIT_DIR/medical-ble-hub-backup.timer"

# Desktop autostart (login session) — works with auto-login
AUTOSTART_DIR="$HUB_HOME/.config/autostart"
mkdir -p "$AUTOSTART_DIR"
sed -e "s|/home/sh3/Desktop/sh3-hw-layer-experiments|$REPO|g" \
  "$REPO/autostart/medical-ble-hub-ui.desktop" \
  > "$AUTOSTART_DIR/medical-ble-hub-ui.desktop"
chown -R "$HUB_USER:$HUB_USER" "$AUTOSTART_DIR" 2>/dev/null || true
echo "Installed $AUTOSTART_DIR/medical-ble-hub-ui.desktop"

# Data dir writable
mkdir -p "$REPO/medical_ble_web/data"
chown -R "$HUB_USER:$HUB_USER" "$REPO/medical_ble_web/data" 2>/dev/null || true

# Browser for kiosk (best-effort)
if ! command -v chromium-browser >/dev/null 2>&1 && ! command -v chromium >/dev/null 2>&1; then
  echo "NOTE: install a browser for on-screen UI, e.g.:"
  echo "  sudo apt-get install -y chromium-browser   # or chromium"
fi

systemctl daemon-reload
systemctl enable medical-ble-hub.service
systemctl enable medical-ble-hub-watchdog.timer
systemctl enable medical-ble-hub-backup.timer
# UI on screen needs a graphical session (auto-login desktop recommended)
systemctl enable medical-ble-hub-ui.service 2>/dev/null || true

# Start now (and leave enabled for next boot)
systemctl restart bluetooth.service 2>/dev/null || true
systemctl restart medical-ble-hub.service
systemctl restart medical-ble-hub-watchdog.timer
systemctl restart medical-ble-hub-backup.timer
# Open UI now if display is available
systemctl start medical-ble-hub-ui.service 2>/dev/null || true

sleep 2
echo ""
echo "Status:"
systemctl --no-pager --full status medical-ble-hub.service | head -25 || true
echo ""
systemctl --no-pager --full status medical-ble-hub-watchdog.timer | head -15 || true
echo ""
echo "Health:"
curl -sf --max-time 5 "http://127.0.0.1:8741/health" | head -c 400 || echo "(not up yet — check: journalctl -u medical-ble-hub -n 50)"
echo ""
echo "============================================================"
echo "  DONE — hub starts at multi-user boot (no login required)"
echo ""
echo "  On-screen UI:"
echo "    • Autostart desktop file + medical-ble-hub-ui.service"
echo "    • Needs a GUI session (auto-login recommended)"
echo "    • Manual test:  ./hub_open_ui.sh"
echo "    • Windowed (not kiosk):  HUB_UI_KIOSK=0 ./hub_open_ui.sh"
echo ""
echo "  Useful commands:"
echo "    sudo systemctl status medical-ble-hub"
echo "    sudo journalctl -u medical-ble-hub -f"
echo "    sudo systemctl restart medical-ble-hub"
echo "    sudo systemctl disable --now medical-ble-hub medical-ble-hub-watchdog.timer medical-ble-hub-ui"
echo ""
echo "  Guardrails:"
echo "    • Restart=always (crash / OOM / power-loss reboot)"
echo "    • Watchdog every 60s restarts unit if :8741/health fails"
echo "    • SQLite WAL (already in app) for unclean shutdowns"
echo "    • WiFi: configure once with nmcli/raspi-config so NM auto-connects"
echo "============================================================"

#!/usr/bin/env bash
# Open the local Medical BLE Hub UI on the attached display (kiosk-friendly).
# Used after boot once the hub service is healthy.
set -uo pipefail

PORT="${PORT:-8741}"
URL="${HUB_UI_URL:-http://127.0.0.1:${PORT}/}"
WAIT_S="${HUB_UI_WAIT_S:-90}"
KIOSK="${HUB_UI_KIOSK:-1}"

log() { echo "[hub-open-ui] $*"; }

# Wait for hub HTTP
ok=0
for i in $(seq 1 "$WAIT_S"); do
  if curl -sf --max-time 2 "http://127.0.0.1:${PORT}/health" >/dev/null 2>&1; then
    ok=1
    break
  fi
  sleep 1
done
if [[ "$ok" -ne 1 ]]; then
  log "WARN: hub not healthy after ${WAIT_S}s — opening UI anyway"
fi

# Prefer a graphical session display
export DISPLAY="${DISPLAY:-:0}"
if [[ -z "${XAUTHORITY:-}" ]]; then
  for xa in \
    "/run/user/$(id -u)/gdm/Xauthority" \
    "$HOME/.Xauthority" \
    "/home/${USER:-sh3}/.Xauthority"
  do
    if [[ -f "$xa" ]]; then
      export XAUTHORITY="$xa"
      break
    fi
  done
fi

# Pick a browser (Chromium/Chrome preferred for kiosk)
open_browser() {
  local bin="$1"
  shift
  if command -v "$bin" >/dev/null 2>&1; then
    log "Starting $bin → $URL"
    # shellcheck disable=SC2086
    nohup "$bin" "$@" "$URL" >/tmp/hub-open-ui.log 2>&1 &
    return 0
  fi
  return 1
}

if [[ "$KIOSK" == "1" ]]; then
  # Fullscreen kiosk (good for a dedicated Pi screen)
  open_browser chromium-browser \
    --kiosk --noerrdialogs --disable-infobars \
    --check-for-update-interval=31536000 \
    --disable-session-crashed-bubble \
    --password-store=basic \
    && exit 0
  open_browser chromium \
    --kiosk --noerrdialogs --disable-infobars \
    --check-for-update-interval=31536000 \
    --disable-session-crashed-bubble \
    --password-store=basic \
    && exit 0
  open_browser google-chrome \
    --kiosk --noerrdialogs --disable-infobars \
    && exit 0
fi

# Non-kiosk / fallback
open_browser xdg-open && exit 0
open_browser firefox && exit 0
open_browser chromium-browser && exit 0
open_browser chromium && exit 0

log "ERROR: no browser found (install chromium-browser)"
exit 1

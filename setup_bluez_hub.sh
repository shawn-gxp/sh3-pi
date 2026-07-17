#!/usr/bin/env bash
# Dedicated medical BLE hub — BlueZ tweaks (optional, safe defaults).
# Run once:  sudo ./setup_bluez_hub.sh
set -euo pipefail

CONF=/etc/bluetooth/main.conf
BACKUP="${CONF}.bak.medical-hub.$(date +%Y%m%d%H%M%S)"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run as root:  sudo $0"
  exit 1
fi

if [[ ! -f "$CONF" ]]; then
  echo "Missing $CONF — is bluez installed?"
  exit 1
fi

cp -a "$CONF" "$BACKUP"
echo "Backed up → $BACKUP"

# Ensure key hub settings exist under [General]
python3 - <<'PY'
from pathlib import Path
p = Path("/etc/bluetooth/main.conf")
text = p.read_text(encoding="utf-8", errors="replace")
lines = text.splitlines()
# Collect existing keys we care about
want = {
    "FastConnectable": "true",
    "JustWorksRepairing": "always",
    "Experimental": "true",
}
# Remove old assignments of these keys (commented or not)
out = []
for line in lines:
    stripped = line.strip()
    skip = False
    for k in want:
        if stripped.startswith(k + " ") or stripped.startswith(k + "=") or stripped.startswith("#" + k):
            # drop; we re-add under [General]
            skip = True
            break
    if not skip:
        out.append(line)

# Insert after [General]
final = []
inserted = False
for line in out:
    final.append(line)
    if line.strip() == "[General]" and not inserted:
        final.append("# --- medical BLE hub (setup_bluez_hub.sh) ---")
        for k, v in want.items():
            final.append(f"{k} = {v}")
        inserted = True

if not inserted:
    final.insert(0, "[General]")
    final.insert(1, "# --- medical BLE hub ---")
    i = 2
    for k, v in want.items():
        final.insert(i, f"{k} = {v}")
        i += 1

# Policy AutoEnable
if not any(l.strip().startswith("AutoEnable") for l in final):
    # append under [Policy] if present
    for i, line in enumerate(final):
        if line.strip() == "[Policy]":
            final.insert(i + 1, "AutoEnable = true")
            break

p.write_text("\n".join(final) + "\n", encoding="utf-8")
print("Updated", p)
PY

systemctl restart bluetooth
sleep 1
bluetoothctl show | head -12 || true
echo ""
echo "OK. Dedicated hub BlueZ settings applied."
echo "Optional pure-LE mode (test pair after): set ControllerMode = le in $CONF"
echo "User BLE group:  usermod -aG bluetooth \$SUDO_USER && re-login"

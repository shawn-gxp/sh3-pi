# Linux (BlueZ) — Medical BLE Toolkit

This project originally targeted **Windows 11 + WinRT**. It now runs on **Linux + BlueZ** the same way (CLI + web UI + Omron pair/read).

## Requirements

| Need | Notes |
|------|--------|
| Bluetooth adapter | Powered on (`bluetoothctl show` → `Powered: yes`) |
| BlueZ | Package `bluez` (usually preinstalled on Ubuntu / Raspberry Pi OS) |
| Python 3.10+ | 3.12 tested |
| User BLE access | Desktop session + polkit is enough on most systems; optional: `bluetooth` group |

```bash
# Optional: permanent BlueZ group membership
sudo usermod -aG bluetooth "$USER"
# then log out / reboot

# Ensure adapter is up
bluetoothctl power on
rfkill unblock bluetooth
```

## One-time setup

```bash
cd /path/to/sh3-hw-layer-experiments

# If python3-venv is missing, install virtualenv another way:
#   python3 -m pip install --user --break-system-packages virtualenv
python3 -m virtualenv .venv   # or: python3 -m venv .venv

.venv/bin/pip install -U pip
.venv/bin/pip install \
  -r requirements.txt \
  -r medical_ble_web/requirements.txt
```

Or:

```bash
chmod +x scripts/deploy/setup_linux.sh scripts/dev/run_web.sh scripts/dev/run_toolkit.sh
./scripts/deploy/setup_linux.sh
```

## Run — Web UI (recommended)

```bash
./scripts/dev/run_web.sh
# Pi / LAN (default): bind 0.0.0.0 — phone on same WiFi:
#   http://<pi-ip>:8741
# Local only:
#   HOST=127.0.0.1 ./scripts/dev/run_web.sh
```

### Pi appliance start (manual)

```bash
./scripts/deploy/start_hub.sh
# best-effort: WiFi radio + bluetooth power on → web hub on 0.0.0.0:8741
```

## Pi Service Management & Deployment

When installed as a persistent boot service (via `scripts/deploy/install_boot_service.sh`), the hub runs in the background using Linux `systemd`. 

Use the following commands to manage the daemon:

- **Check Status**: `sudo systemctl status medical-ble-hub`
- **Restart the Service**: `sudo systemctl restart medical-ble-hub`
- **View Live Logs**: `sudo journalctl -fu medical-ble-hub`
- **Stop the Service**: `sudo systemctl stop medical-ble-hub`

### Start at boot (no login) — systemd

One-time (after `./setup_linux.sh` and WiFi configured to auto-join):

```bash
sudo ./install_boot_service.sh
```

This installs:

| Unit | Role |
|------|------|
| `medical-ble-hub.service` | On multi-user boot: BLE/WiFi prep → web hub + auto-sync |
| `medical-ble-hub-watchdog.timer` | Every ~60s: if `http://127.0.0.1:8741/health` fails → restart hub |

**Guardrails (power loss / crash):**

- `Restart=always` — process crash or reboot after power loss starts the hub again
- Watchdog restarts if the HTTP port dies but the process hung
- SQLite **WAL** mode (app) — safer after unclean power cut
- BlueZ `AutoEnable=true` when install can edit `main.conf`
- User added to `bluetooth` / `plugdev` groups for BLE without desktop login

```bash
sudo systemctl status medical-ble-hub
sudo journalctl -u medical-ble-hub -f
sudo systemctl restart medical-ble-hub
# disable boot start:
sudo systemctl disable --now medical-ble-hub medical-ble-hub-watchdog.timer
```

**WiFi at boot:** configure NetworkManager once (GUI, `nmtui`, or `nmcli`) so the SSID is set to auto-connect. The service turns WiFi radio on and waits briefly for NM; it does not store WiFi passwords itself.

**Phone URL after boot:** `http://<pi-ip>:8741`

### Open local website on the Pi screen

After hub is healthy, open Chromium kiosk on the attached display:

```bash
./hub_open_ui.sh
# normal window instead of full screen:
HUB_UI_KIOSK=0 ./hub_open_ui.sh
```

Re-run install to enable boot/UI wiring:

```bash
sudo ./install_boot_service.sh
```

That installs:

1. **`medical-ble-hub-ui.service`** — starts with `graphical.target` after the hub  
2. **`~/.config/autostart/medical-ble-hub-ui.desktop`** — opens UI when the desktop session starts  

**Requirement:** a graphical session on boot (auto-login). On Ubuntu/Pi desktop:

- Settings → Users → Automatic Login for your user, **or**  
- `sudo raspi-config` → System → Auto Login (Pi OS)

Without auto-login, the **hub server still runs headless**; the browser only opens after someone logs into the desktop (or you run `./hub_open_ui.sh` manually).

```bash
sudo apt-get install -y chromium-browser   # if missing
```

### MQTT cloud transfer (Android hub drop-in)

Pi publishes clinical SQLite rows to MQTT like the SHHM Android hub.

Config file: `medical_ble_web/mqtt_config.json`

| Key | Default (edit these) |
|-----|----------------------|
| `broker` | `tcp://172.16.2.100:1883` (from APK assets) |
| `username` / `password` | `admin` / `admin123` |
| `topic` | `health/readings` |
| `hub_id` | Must be cloud **`hubs.id` UUID** (slug like `pi-hub-sh3-01` is rejected by Postgres) |
| `patient_id` | Cloud patient **UUID**, or leave unset so cloud resolves via hub/sensor |
| `enabled` | `true` |

- **sensorId** = device **MAC** (uppercase)
- On each new clinical insert (`bp` / `temp` / `spo2` / `glucose`) → publish
- Heartbeat every 30s → `hub/{hub_id}/heartbeat`
- Also mirrors to `patient/{patient_id}/sensor/{mac}/data`
- Local SQLite always kept; MQTT failures never block BLE

**Known issues (cloud ingest):** Pi currently may send non-UUID `hubId` / `patientId`; SHHM backend does `Hub.findByPk` on UUID column → readings arrive on MQTT but fail to save. Full write-up and local-only fix plan: **`EXECUTION_PLAN.md` § 20 Known Issues** (`KI-MQTT-01`, `KI-MQTT-02`). Do not change cloud code for this.

```bash
# after editing mqtt_config.json:
sudo systemctl restart medical-ble-hub
curl -s http://127.0.0.1:8741/health | python3 -m json.tool | grep -A20 mqtt
```

### Hub matching (MAC-strict)

Auto-sync only starts a session when a **paired** roster entry’s **MAC** appears in a scan.  
It does **not** bind “any NBP” to the only NBP in the list. Two devices of the same brand need their own saved MACs.

### Data locations

| Data | Path |
|------|------|
| SQLite (source of truth) | `medical_ble_web/data/poc.db` |
| Paired export (mirror) | `medical_ble_web/data/paired_devices.json` |
| Nipro exact-name registry | `medical_ble_web/data/nipro_paired_devices.json` (or `$MEDICAL_NIPRO_REGISTRY`) |

## Run — Interactive CLI

```bash
./run_toolkit.sh
# or:
source .venv/bin/activate
python -m medical_ble_toolkit
```

## Omron HEM-7143T1 (example)

```bash
source .venv/bin/activate

# Pair once — cuff flashing P (hold BT 3–5s)
python -m medical_ble_toolkit omron pair -d HEM-7143T1 -a E1:99:7D:27:1C:0A

# Read history — short-press BT (transfer mode, not P)
python -m medical_ble_toolkit omron read -d HEM-7143T1 -a E1:99:7D:27:1C:0A -o ./data
```

## Pairing notes (Linux)

- **Just Works** devices (most Omron): `BleakClient.pair()` completes without a PIN.
- **Passkey** devices (some Beurer): desktop may show a prompt, or use:
  ```bash
  bluetoothctl
  agent on
  default-agent
  ```
  Then re-run PAIR and enter the 6-digit code from the device LCD.
- **Remove a bad bond:**
  ```bash
  bluetoothctl remove AA:BB:CC:DD:EE:FF
  ```
- Only **one host** can own many medical meters. Unpair the phone companion app first.

## Differences vs Windows

| Topic | Windows | Linux |
|-------|---------|--------|
| BLE stack | WinRT | BlueZ D-Bus |
| Log tags | `[WINRT]` | `[BLUEZ]` |
| OS pair UI | Settings popup | Agent / desktop notification |
| Remove bond | Settings → Bluetooth | `bluetoothctl remove MAC` |
| PowerShell scripts | `*.ps1` | Use `run_web.sh` / `run_toolkit.sh` |

Protocol parsers, profiles, and the web API are **identical** on both platforms.

## Offline tests

```bash
source .venv/bin/activate
python -m medical_ble_toolkit.tests.test_parsers
python -m medical_ble_toolkit --list-profiles
```

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Scan empty | Wake device; `bluetoothctl power on`; keep &lt; 1 m |
| `Authentication Required` | PAIR once; check bond with `bluetoothctl devices` |
| FE4A missing (Omron) | RE-PAIR with flashing P; unpair phone first |
| Scanner busy | Stop web UI or other CLI; `bluetoothctl power off; power on` |
| Permission errors | Re-login after `usermod -aG bluetooth`; check `journalctl -u bluetooth` |
| Web vs CLI conflict | Only one process should hold the radio at a time |

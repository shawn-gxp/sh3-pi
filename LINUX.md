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
  -r medical_ble_web/requirements.txt \
  -r omron_bp/requirements.txt
```

Or:

```bash
chmod +x setup_linux.sh run_web.sh run_toolkit.sh
./setup_linux.sh
```

## Run — Web UI (recommended)

```bash
./run_web.sh
# → http://127.0.0.1:8741
```

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

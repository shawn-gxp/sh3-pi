# Medical BLE experiments (multi-brand hub)

Tools for **finding, pairing, and reading** smart medical devices over
**Bluetooth Low Energy (BLE)** on **Linux (BlueZ)** and **Windows (WinRT)**.

| Platform | Docs | Launch |
|----------|------|--------|
| **Linux / Pi** | [LINUX.md](LINUX.md) | `scripts/deploy/setup_linux.sh` → `scripts/deploy/start_hub.sh` (or `scripts/dev/run_web.sh`) |
| **Windows** | below | `scripts\dev\run_web.ps1` / `python -m medical_ble_toolkit` |
| **Execution plan** | [EXECUTION_PLAN.md](EXECUTION_PLAN.md) | Phased Pi hub production work |

## Active layout

| Path | Role |
|------|------|
| `medical_ble_toolkit/` | **Standalone** multi-brand BLE HAL + hub + bundled Omron (`omron_bp/`) |
| `medical_ble_web/` | FastAPI UI → http://127.0.0.1:8741 (depends **only** on toolkit) |
| `datasheets/` | Protocol PDFs, architecture notes (reference only — not a runtime dep) |
| `phoneblelog/` | Omron HCI findings + btsnoop helpers |
| `tools/standalone/` | Optional continuous AD watch tools (`ble_discover_loop.py` etc) |

Hub timings: `medical_ble_toolkit/hub_config.json` (or `$MEDICAL_HUB_CONFIG`).

## Linux quick start

```bash
./scripts/deploy/setup_linux.sh
./scripts/deploy/start_hub.sh        # BLE on + web on LAN → http://<pi-ip>:8741
# boot without login (once):
sudo ./scripts/deploy/install_boot_service.sh
# or:
./scripts/dev/run_web.sh          # same; HOST=127.0.0.1 for local-only
# optional CLI:
./scripts/dev/run_toolkit.sh
./scripts/deploy/setup_bluez_hub.sh  # BlueZ agent / hub helpers
```

Hub Auto-sync: Pair devices on this host only → leave hub running → measure.
MightySat uses a **duty-cycle** (valid SpO2 up to 20s, drop after 5s of `-`, then hunt NBP/NT/Omron). See `medical_ble_toolkit/hub_config.json` and [LINUX.md](LINUX.md).

## Findings & datasheets (kept)

- `datasheets/BLE_Medical_Device_Architecture_Reference.md` (+ PDF)
- `datasheets/beurer/*.md` + BM54 protocol PDFs
- `datasheets/nipro/*.md` + `EXACT_PROTOCOL.json` + vendor PDFs (MightySat, TICD, HTP, A&D, Cocoron)
- `phoneblelog/OMRON_FINDINGS.md`

Upstream references (not vendored here):

- [omblepy](https://github.com/userx14/omblepy) — classic Omron CLI
- [hass-omron](https://github.com/eigger/hass-omron) — Home Assistant Omron profiles

Logic from those projects lives in `medical_ble_toolkit/` (Omron under `medical_ble_toolkit/omron_bp/`).

## Windows

```powershell
python -m pip install -r requirements.txt
python -m pip install -r medical_ble_web\requirements.txt
.\scripts\dev\run_web.ps1
# or:
python -m medical_ble_toolkit
python -m medical_ble_toolkit omron pair -d HEM-7143T1 -a E1:99:7D:27:1C:0A
```

## Notes

- **One host bond only** for Omron / Nipro / MightySat — unpair the phone companion first.
- SQLite POC DB: `medical_ble_web/data/poc.db`
- Paired device lists: managed via web UI device DB (SQLite)

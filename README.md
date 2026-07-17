# Medical BLE experiments (multi-brand hub)

Tools for **finding, pairing, and reading** smart medical devices over
**Bluetooth Low Energy (BLE)** on **Linux (BlueZ)** and **Windows (WinRT)**.

| Platform | Docs | Launch |
|----------|------|--------|
| **Linux** | [LINUX.md](LINUX.md) | `./setup_linux.sh` → `./run_web.sh` |
| **Windows** | below | `.\run_web.ps1` / `python -m medical_ble_toolkit` |

## Active layout

| Path | Role |
|------|------|
| `medical_ble_toolkit/` | Multi-brand BLE toolkit + **hub** duty-cycle daemon |
| `medical_ble_web/` | FastAPI UI → http://127.0.0.1:8741 |
| `omron_bp/` | Omron FE4A pairing / EEPROM readout package |
| `datasheets/` | Protocol PDFs, architecture notes, distilled findings (MD/JSON) |
| `phoneblelog/` | Omron HCI findings + btsnoop helpers |
| `hub_config.json` | Pi hub timings (Mighty 20s / 5s dash exit / prefer others) |
| `ble_discover_loop.py` | Optional continuous AD watch (Linux debug) |

## Linux quick start

```bash
./setup_linux.sh
./run_web.sh          # browser: http://127.0.0.1:8741
# optional CLI:
./run_toolkit.sh
./setup_bluez_hub.sh  # BlueZ agent / hub helpers
```

Hub Auto-sync: Pair devices on this host only → leave hub running → measure.
MightySat uses a **duty-cycle** (valid SpO2 up to 20s, drop after 5s of `-`, then hunt NBP/NT/Omron). See `hub_config.json` and [LINUX.md](LINUX.md).

## Findings & datasheets (kept)

- `datasheets/BLE_Medical_Device_Architecture_Reference.md` (+ PDF)
- `datasheets/beurer/*.md` + BM54 protocol PDFs
- `datasheets/nipro/*.md` + `EXACT_PROTOCOL.json` + vendor PDFs (MightySat, TICD, HTP, A&D, Cocoron)
- `phoneblelog/OMRON_FINDINGS.md`

Upstream references (not vendored here):

- [omblepy](https://github.com/userx14/omblepy) — classic Omron CLI
- [hass-omron](https://github.com/eigger/hass-omron) — Home Assistant Omron profiles

Logic from those projects lives in `omron_bp/` and `medical_ble_toolkit/`.

## Windows

```powershell
python -m pip install -r requirements.txt
python -m pip install -r medical_ble_web\requirements.txt
python -m pip install -r omron_bp\requirements.txt
.\run_web.ps1
# or:
python -m medical_ble_toolkit
python -m omron_bp pair -d HEM-7143T1 -m E1:99:7D:27:1C:0A
```

## Notes

- **One host bond only** for Omron / Nipro / MightySat — unpair the phone companion first.
- SQLite POC DB: `medical_ble_web/data/poc.db`
- Paired device lists: `nipro_paired_devices.json`, web UI device DB

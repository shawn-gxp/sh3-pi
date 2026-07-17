# Medical BLE Web POC

Local-only proof-of-concept UI for the medical BLE toolkit.

- **Does not modify** `medical_ble_toolkit` (CLI stays as-is)
- **FastAPI** backend on `127.0.0.1:8741`
- **SQLite** at `data/poc.db`
- **All brands** from the interactive CLI catalog
- **Windows (WinRT)** and **Linux (BlueZ)**

## Setup

From the experiments root (so `bleak` and the toolkit are available):

```powershell
# Windows
pip install -r requirements.txt
pip install -r medical_ble_web\requirements.txt
```

```bash
# Linux
./setup_linux.sh
# or:
source .venv/bin/activate
pip install -r requirements.txt -r medical_ble_web/requirements.txt
```

## Run

From **experiments** root (easiest):

```powershell
# Windows
.\run_web.ps1
```

```bash
# Linux
./run_web.sh
# PORT=9000 ./run_web.sh
```

Or from this folder:

```powershell
cd medical_ble_web
.\run_web.ps1
# or: python app.py
```

```bash
cd medical_ble_web
PYTHONPATH=.. ../.venv/bin/python app.py
```

Opens: **http://127.0.0.1:8741**

**Live SpO2:** WebSocket push + **auto-reconnect** if the BLE link goes silent/drops (Masimo). Click **Live stop** to end.

## API

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/` | Simple console UI |
| GET | `/health` | Health + live status |
| GET | `/brands` | All companies / brands |
| POST | `/scan` | BLE scan `{ "brand": null, "timeout": 8 }` |
| GET | `/devices` | Saved devices |
| POST | `/devices` | Save device |
| POST | `/pair` | Pair / re-pair (Nipro also writes hands-free registry) |
| POST | `/sync` | One-shot read/sync (Nipro companion sessions) |
| POST | `/live/start` | Start live stream |
| POST | `/live/stop` | Stop live |
| GET | `/live/latest` | Poll latest live reading |
| GET | `/readings` | History from SQLite |
| GET | `/nipro/meters` | Paired Nipro registry |
| POST | `/nipro/register` | Register exact BLE name for hands-free |
| POST | `/nipro/handsfree/start` | Companion-like wait loop |
| POST | `/nipro/handsfree/stop` | Stop hands-free |
| GET | `/nipro/handsfree/status` | Hands-free status |

## Brands

Omron, Beurer, A&D (full SDK), **Nipro companion** (NBP / NMBP / NSM / NT-100B / CF), Masimo, NT-100B TICD lab, FORA, RE.

### Nipro hands-free (げんきノート-style)

1. Select a Nipro brand → Scan → set MAC (exact name from scan used on Pair).  
2. **Pair** once (registers `nipro_paired_devices.json` + SQLite device).  
3. Measure on the device → **Sync**, or click **Nipro Hands-free** to wait and auto-sync.

## Notes

- One BLE job at a time (global lock)
- Windows: accept Bluetooth pairing popups
- Keep CLI closed while the web app holds the radio if you hit scanner conflicts

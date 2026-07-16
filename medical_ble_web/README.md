# Medical BLE Web POC

Local-only proof-of-concept UI for the medical BLE toolkit.

- **Does not modify** `medical_ble_toolkit` (CLI stays as-is)
- **FastAPI** backend on `127.0.0.1:8741`
- **SQLite** at `data/poc.db`
- **All brands** from the interactive CLI catalog

## Setup

From the experiments root (so `bleak` and the toolkit are available):

```powershell
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"
pip install -r requirements.txt
pip install -r medical_ble_web\requirements.txt
```

## Run

From **experiments** root (easiest):

```powershell
.\run_web.ps1
```

Or from this folder:

```powershell
cd medical_ble_web
.\run_web.ps1
# or: python app.py
```

Options:

```powershell
.\run_web.ps1 -NoBrowser
.\run_web.ps1 -Port 8741 -SkipInstall
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
| POST | `/pair` | Pair / re-pair |
| POST | `/sync` | One-shot read/sync |
| POST | `/live/start` | Start live stream |
| POST | `/live/stop` | Stop live |
| GET | `/live/latest` | Poll latest live reading |
| GET | `/readings` | History from SQLite |

## Brands

Omron, Beurer, A&D, Masimo, NT-100B thermo, FORA, RE generic — same as CLI.

## Notes

- One BLE job at a time (global lock)
- Windows: accept Bluetooth pairing popups
- Keep CLI closed while the web app holds the radio if you hit scanner conflicts

# Medical BLE Web (hub UI + daemon)

BLE medical hub web app + job orchestrator.

- **Depends on** `medical_ble_toolkit` (HAL)
- **FastAPI** on port **8741** (LAN: `0.0.0.0` via `start_hub.sh` / systemd)
- **SQLite** at `data/poc.db` (source of truth for devices + readings)
- **MQTT** clinical publish via `mqtt_bridge.py` + `mqtt_config.json` (optional)
- **Fall detection is a separate process** on port **8742** (`fall_detection_pi.web_server`) — not mounted here
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

Opens: **http://127.0.0.1:8741** (phone: `http://<pi-ip>:8741`)

**Pi systemd unit:** `medical-ble-hub` (not `medical-ble-web`).

**Live SpO2:** WebSocket push + **auto-reconnect** if the BLE link goes silent/drops (Masimo). Click **Live stop** to end.

## Pairing (primary path = UI)

1. Scan for devices on the hub UI  
2. Select **brand** + MAC → **Pair** once  
3. Success → SQLite `devices.paired = 1` → auto-sync roster includes that MAC  

MAC/name alone without `paired=1` is **not** treated as paired. See `docs/LINUX.md`.

## MQTT (clinical only)

Edit `mqtt_config.json` (`hub_id` / `patient_id` must be cloud **UUIDs**, broker LAN IP).  
Restart hub after changes. Fall alerts use **HTTP** on the fall service, not this bridge.  
Details: `docs/LINUX.md`, `docs/EXECUTION_PLAN.md` §20.

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

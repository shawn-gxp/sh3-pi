# fall_detection_pi

Standalone **fall / bed-exit** computer vision for the SH3 Raspberry Pi hub.

**Layout rule: always a sibling of `sh3-pi`, never nested inside it.**

## Full design documentation

**Read first:** [`../sh3-pi/docs/FALL_DETECTION.md`](../sh3-pi/docs/FALL_DETECTION.md)

That doc covers equations, state machine, API, config, design history, and limits.

## Canonical layout (sibling only)

```
SHHMHub/   (or any workspace root)
├── fall_detection_pi/     # THIS package + web_server on :8742
├── sh3-pi/                # BLE hub only on :8741 (medical_ble_web, toolkit, …)
│   └── medical_ble_web/   # device UI; links out to fall service
└── edge-ai-fall-detection/
```

| Correct | Incorrect |
|---------|-----------|
| `…/fall_detection_pi` next to `…/sh3-pi` | `…/sh3-pi/fall_detection_pi` |
| `…/sh3-pi/fall_detection` (old name nested) |

## Install (from workspace root)

```bash
pip install -e ./fall_detection_pi
# or
pip install -r fall_detection_pi/requirements.txt
set PYTHONPATH=.
```

Optional: `FALL_DETECTION_HOME=/abs/path/to/fall_detection_pi`

## Quick test (no camera)

```bash
# workspace root
set PYTHONPATH=.
python -m pytest fall_detection_pi/tests/test_fall_detector.py -v
python fall_detection_pi/tests/_tryout_sim.py
```

## Run fall service (independent of BLE hub)

```bash
# workspace root (sibling of sh3-pi)
set PYTHONPATH=.
python -m fall_detection_pi.web_server --ssl
# UI:     https://127.0.0.1:8742/
# Status: https://127.0.0.1:8742/api/fall/status
```

BLE hub (optional, separate terminal):

```bash
cd sh3-pi/medical_ble_web
set PYTHONPATH=..;.
python app.py --ssl
# https://127.0.0.1:8741/  — devices only; links to fall on :8742
```

## Env (short list)

| Variable | Purpose |
|----------|---------|
| `FALL_DETECTION_HOME` | Absolute path to this package dir |
| `FALL_PATIENT_ID` | Required for **HTTP** alerts (else skipped) |
| `FALL_BACKEND_BASE_URL` | Alert API base → `POST …/fall-events` |
| `FALL_DEVICE_ID` | Payload device id (default `edge-ai-camera-01`) |
| `FALL_POSE_MODEL` | `lite` / `full` / `heavy` |
| `FALL_CAMERA_SOURCE` | OpenCV device index or use `RTSP_URL` |
| `FALL_HUB_CONFIG` | Path to ROI `hub_config.json` |
| `FALL_PORT` / `FALL_HOST` | HTTP listen (default `8742` / `0.0.0.0`) |

### Alerts: HTTP only (not MQTT)

Clinical vitals use the BLE hub MQTT bridge (`health/readings`).  
This package posts fall/bed-exit events via **HTTP** only (Android-compatible), not MQTT.

See `docs/FALL_DETECTION.md` §10 for payload and cloud contract.

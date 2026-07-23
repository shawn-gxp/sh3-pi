# fall_detection_pi

Standalone **fall / bed-exit** computer vision for the SH3 Raspberry Pi hub.

This package is **separate from BLE** (`medical_ble_toolkit` / hub pairing). The hub
web UI optionally imports it.

## Full design documentation

**Read first:** [`docs/FALL_DETECTION.md`](../docs/FALL_DETECTION.md) (in the `sh3-pi` repo)
or, in the SHHMHub monorepo: `sh3-pi/docs/FALL_DETECTION.md`.

That doc covers:

- Why the package is split from the BLE hub  
- End-to-end pipeline (camera / phone landmarks / JPEG)  
- **Equations** (torso center, angle, aspect, ROI, rapid drop, fall paths)  
- State machine and defaults  
- HTTP API, config env vars, alert payload  
- Design history and known reliability limits  

## Layout

### Dev repo (`shawn-gxp/sh3-pi`)

```
sh3-pi/
├── fall_detection_pi/     # THIS package
├── medical_ble_web/
└── medical_ble_toolkit/
```

### Monorepo (`gxpindia/SHHMHub`)

```
SHHMHub/
├── fall_detection_pi/     # THIS package (sibling)
├── sh3-pi/                # BLE hub only
└── edge-ai-fall-detection/
```

## Install

```bash
# From the directory that *contains* fall_detection_pi/
pip install -e ./fall_detection_pi
# or
pip install -r fall_detection_pi/requirements.txt
set PYTHONPATH=.
```

Optional: `FALL_DETECTION_HOME=/abs/path/to/fall_detection_pi`

## Quick test (no camera)

```bash
set PYTHONPATH=.
python -m pytest fall_detection_pi/tests/test_fall_detector.py -v
python fall_detection_pi/tests/_tryout_sim.py
```

## Hub try-out

```bash
cd medical_ble_web
set PYTHONPATH=..;.;../..
python app.py --ssl
# open https://127.0.0.1:8741/static/fall.html
```

## Env (short list)

| Variable | Purpose |
|----------|---------|
| `FALL_PATIENT_ID` | Required for HTTP alerts (else skipped) |
| `FALL_BACKEND_BASE_URL` | Alert API base |
| `FALL_POSE_MODEL` | `lite` / `full` / `heavy` |
| `FALL_CAMERA_SOURCE` | OpenCV device index or use `RTSP_URL` |
| `FALL_HUB_CONFIG` | Path to ROI `hub_config.json` |

See the full doc for equations and thresholds.

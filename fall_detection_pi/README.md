# fall_detection_pi

Standalone **fall / bed-exit** computer vision for the SH3 Raspberry Pi hub.

**Layout rule: always a sibling of `sh3-pi`, never nested inside it.**

## Full design documentation

**Read first:** [`../sh3-pi/docs/FALL_DETECTION.md`](../sh3-pi/docs/FALL_DETECTION.md)

That doc covers equations, state machine, API, config, design history, and limits.

## Canonical layout (sibling only)

```
SHHMHub/   (or any workspace root)
├── fall_detection_pi/     # THIS package  →  import fall_detection_pi
├── sh3-pi/                # BLE hub only (medical_ble_web, toolkit, …)
│   └── medical_ble_web/   # optional consumer via fall_import.py
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

## Hub try-out

```bash
# workspace root
cd sh3-pi/medical_ble_web
set PYTHONPATH=..;.;../..
python app.py --ssl
# open https://127.0.0.1:8741/static/fall.html
# GET /api/fall/status → package_path should be …/fall_detection_pi (sibling)
```

## Env (short list)

| Variable | Purpose |
|----------|---------|
| `FALL_DETECTION_HOME` | Absolute path to this package dir |
| `FALL_PATIENT_ID` | Required for HTTP alerts (else skipped) |
| `FALL_BACKEND_BASE_URL` | Alert API base |
| `FALL_POSE_MODEL` | `lite` / `full` / `heavy` |
| `FALL_CAMERA_SOURCE` | OpenCV device index or use `RTSP_URL` |
| `FALL_HUB_CONFIG` | Path to ROI `hub_config.json` |

See `sh3-pi/docs/FALL_DETECTION.md` for equations and thresholds.

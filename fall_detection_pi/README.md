# fall_detection_pi (standalone)

Computer-vision **fall / bed-exit** detection for the SH3 Raspberry Pi hub.

This package is **intentionally separate** from `sh3-pi` (BLE hub). In **SHHMHub**
they are siblings:

```
SHHMHub/
├── fall_detection_pi/     # THIS package (import fall_detection_pi)
├── sh3-pi/                # BLE hub + medical_ble_web
├── edge-ai-fall-detection/  # Android Kotlin reference
├── backend/
└── …
```

Do **not** nest this package inside `sh3-pi`.

## Install

```bash
# From SHHMHub (or workspace) root
pip install -e ./fall_detection_pi

# Or deps only + PYTHONPATH
pip install -r fall_detection_pi/requirements.txt
export PYTHONPATH="$PWD:$PYTHONPATH"   # parent of fall_detection_pi/
```

## Env vars

| Variable | Purpose |
|----------|---------|
| `FALL_DETECTION_HOME` | Absolute path to this package dir (optional if installed / sibling) |
| `FALL_HUB_CONFIG` | Path to `hub_config.json` for bed ROI persist |
| `FALL_PATIENT_ID` | Backend patient id (alerts skipped if unset/placeholder) |
| `FALL_BACKEND_BASE_URL` | Alert HTTP base URL |
| `FALL_POSE_MODEL` | `lite` \| `full` \| `heavy` |
| `FALL_CAMERA_SOURCE` / `RTSP_URL` | Camera index or RTSP URL |

## Tests (no camera)

```bash
cd fall_detection_pi
# parent must be on PYTHONPATH for `import fall_detection_pi`
set PYTHONPATH=..
python -m pytest tests/ -v
python tests/_tryout_sim.py
```

## Used by

- `sh3-pi/medical_ble_web` — starts camera loop on hub boot; serves `/api/fall/*` and `/static/fall.html`
- Android reference: `edge-ai-fall-detection` (Kotlin) in SHHMHub monorepo

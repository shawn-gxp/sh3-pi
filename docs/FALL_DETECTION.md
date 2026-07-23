# Fall Detection Architecture

Computer-vision fall detection for the Raspberry Pi Hub. Logic evolved from the Android
Kotlin port (`edge-ai-fall-detection`) with polygon ROI, temporal voting, and rapid-drop
velocity checks.

## Pipeline Overview

The pipeline can run as a background camera thread (FastAPI lifespan in
`medical_ble_web/app.py`) **or** from browser-uploaded frames / landmark JSON (no host
camera required).

1. **Capture / input**
   - Local: OpenCV `CAMERA_SOURCE` (device index or `RTSP_URL`)
   - Remote: `POST /api/fall/frame` (JPEG) or `POST /api/fall/landmarks` (33 keypoints / named dict)
2. **Pose inference** (camera/frame paths only): **MediaPipe Tasks** `PoseLandmarker` → 33 landmarks  
   (not the removed `mp.solutions.pose` Solutions API)
3. **Rules engine** (`fall_detection/fall_detector.py`): polygon ROI + posture + velocity + temporal voting
4. **Alerting** (`alert_api.py`): rate-limited HTTP POST to backend; local alarm hooks (stubs)

## Module Map

| Path | Role |
|------|------|
| `fall_detection/config.py` | Env overrides, ROI load, pose model path/URL |
| `fall_detection/pose_model.py` | Auto-download `pose_landmarker_*.task` on first use |
| `fall_detection/fall_detector.py` | `PolygonROI`, `FallDetector`, `DetectionState` |
| `fall_detection/camera_loop.py` | PoseLandmarker (IMAGE/VIDEO), frame/landmarks handlers |
| `fall_detection/alert_api.py` | Background `POST …/fall-events` |
| `medical_ble_web/app.py` | `/api/fall/*` routes + ROI persist |
| `medical_ble_web/static/fall.html` | ROI calibration + phone/browser camera UI |
| `fall_detection/tests/test_fall_detector.py` | Unit tests (no camera) |

## Vision stack (current)

| Package / API | Status |
|---------------|--------|
| `mediapipe>=0.10.30` | **Required** — Tasks API only (`PoseLandmarker`) |
| `mp.solutions.pose` | **Removed** in modern mediapipe — do not use |
| OpenCV | Brought in by mediapipe (`opencv-contrib-python`) |
| Model file | `fall_detection/models/pose_landmarker_lite.task` (auto-download) |

Env for models:

| Env | Default | Notes |
|-----|---------|-------|
| `FALL_POSE_MODEL` | `lite` | `lite` \| `full` \| `heavy` |
| `FALL_POSE_MODEL_PATH` | `fall_detection/models/pose_landmarker_<variant>.task` | Skip download if file exists |
| `FALL_POSE_MODEL_URL` | Google CDN for chosen variant | Override download URL |

Running modes:

- **IMAGE** — `POST /api/fall/frame` (independent stills)
- **VIDEO** — local camera loop (`detect_for_video` + monotonic timestamps)

## ROI (bed polygon)

- **Shape**: list of ≥3 normalized points `(x, y)` in `[0, 1]`
- **Load**: `hub_config.json` → `fall_detection.polygon` (repo root; path fixed — was wrong `parent³`)
- **Live update**: `POST /api/fall/roi` updates memory + rewrites `hub_config.json`
- **Read**: `GET /api/fall/roi`

Default fallback polygon if no hub config: `(0.1,0.1)…(0.1,0.9)`.

## Core Evaluation Logic

Landmarks used:

- Shoulders: 11, 12
- Hips: 23, 24
- Knees / ankles: 25–28 (visibility fallbacks)

Per frame:

1. **Torso center** — mean of shoulders + hips
2. **Torso angle** — `atan2` shoulder-mid → hip-mid; `is_horizontal` if angle &lt; 25°
3. **BBox aspect** — height/width; `is_lying` if ratio &lt; 0.6
4. **Polygon** — `contains` + signed `distance` (OpenCV or pure-Python edge distance)
5. **Rapid drop** — Δy of torso center &gt; 0.08 within last 1s of history
6. **Temporal voting**
   - FALL / NEAR / LEFT: ≥5 consecutive frames
   - INSIDE: ≥2 frames (faster clear)

### Fall conditions (two paths)

Both require **torso outside ROI** and **down posture** (`is_horizontal` angle &lt; 25° **or** `is_lying` bbox aspect &lt; 0.6):

| Path | Extra requirement | Confirm frames (default) | Catches |
|------|-------------------|--------------------------|---------|
| **Rapid** | torso Δy &gt; 0.08 within ~1s | 5 | hard falls |
| **Sustained** | none (posture only) | 15 | slow slide, already on floor |

```
confirmed_fall =
    rapid_frames >= 5
    OR sustained_down_frames >= 15
```

While posture is down outside the bed, the patient is on the **fall track** — not `left_bed` (walking away upright is separate).

### State machine

| State | Confirmed when | Event type |
|-------|----------------|------------|
| `in_safe_area` | 2× INSIDE | clears alarm; no backend event |
| `near_edge` | 5× outside upright, `dist > -edge_tolerance` (default 0.05) | `PATIENT_NEAR_EDGE` |
| `fall_detected` | rapid (5) or sustained down (15) | `FALL_DETECTED` |
| `left_bed` | 5× upright LEFT (not fall) | `PATIENT_LEFT_BED` |
| transitional | voting not met | **no alert** |

Thread safety: `FallDetector.evaluate` is locked; camera + HTTP paths share one detector under `camera_loop._lock` for ROI + alerts. ROI update calls `fall_detector.reset()`.

## HTTP API

| Method | Path | Body | Notes |
|--------|------|------|-------|
| `GET` | `/api/fall/stream` | — | MJPEG of last processed frame |
| `GET` | `/api/fall/roi` | — | Current polygon |
| `POST` | `/api/fall/roi` | `{ "polygon": [{"x","y"},…] }` | Live + persist |
| `POST` | `/api/fall/frame` | raw JPEG bytes | Host MediaPipe if installed |
| `POST` | `/api/fall/landmarks` | `{ "landmarks": … }` | 33-list or named dict; **best for try-out without camera** |

## Alerting & config

| Setting | Env var | Default |
|---------|---------|---------|
| Backend base | `FALL_BACKEND_BASE_URL` | `http://172.16.2.156:5173/api` |
| Patient id | `FALL_PATIENT_ID` | `REPLACE_WITH_PATIENT_ID` (**skips publish**) |
| Device id | `FALL_DEVICE_ID` | `edge-ai-camera-01` |
| Integration key | `FALL_INTEGRATION_KEY` | empty |
| Cooldown | `FALL_COOLDOWN_MS` | `60000` |
| Camera | `FALL_CAMERA_SOURCE` or `RTSP_URL` | `0` |
| Hub config path | `FALL_HUB_CONFIG` | optional override |

Cooldown is per event type: same `event_type` will not re-post within `COOLDOWN_MS`.
Posts run on a daemon thread so the camera loop is not blocked.

## How to try it out

### 1. Unit tests (no deps beyond pytest)

```bash
python -m pytest fall_detection/tests/test_fall_detector.py -v
```

### 2. Landmarks simulation (no camera)

```bash
# from repo root
set PYTHONPATH=.
python fall_detection/tests/_tryout_sim.py
```

Covers IN BED → NEAR EDGE → LEFT BED → FALL via `process_normalized_landmarks`.

### 3. Web UI + API

```bash
# from repo root, with your usual hub start command, e.g.:
uvicorn medical_ble_web.app:app --host 0.0.0.0 --port 8000
```

Then open `http://<hub>:8000/static/fall.html` to draw the bed ROI and stream / phone-camera frames.

Manual landmarks POST example:

```bash
curl -s -X POST http://127.0.0.1:8000/api/fall/landmarks ^
  -H "Content-Type: application/json" ^
  -d "{\"landmarks\":{\"LEFT_SHOULDER\":{\"x\":0.4,\"y\":0.3,\"visibility\":1},\"RIGHT_SHOULDER\":{\"x\":0.6,\"y\":0.3,\"visibility\":1},\"LEFT_HIP\":{\"x\":0.4,\"y\":0.45,\"visibility\":1},\"RIGHT_HIP\":{\"x\":0.6,\"y\":0.45,\"visibility\":1},\"LEFT_KNEE\":{\"x\":0.4,\"y\":0.6,\"visibility\":1},\"RIGHT_KNEE\":{\"x\":0.6,\"y\":0.6,\"visibility\":1},\"LEFT_ANKLE\":{\"x\":0.4,\"y\":0.8,\"visibility\":1},\"RIGHT_ANKLE\":{\"x\":0.6,\"y\":0.8,\"visibility\":1}}}"
```

Repeat 2–3 times to confirm `in_safe_area` (temporal voting).

## Known limitations / remaining work

| Item | Status |
|------|--------|
| ROI from `hub_config.json` | **Fixed** (correct repo-root path) |
| ROI REST + UI | **Done** (`/api/fall/roi`, `fall.html`) |
| Rapid-drop + temporal voting | **Done** (not the old single-frame “below_bed” only) |
| `left_bed` on `DetectionState` + temporal gate | **Fixed** (was firing every non-safe frame) |
| Pure-Python `near_edge` distance | **Fixed** (was constant ±0.1 stub) |
| Landmarks API accepts `NormalizedPoint` + JSON dicts | **Fixed** |
| MediaPipe `min_presence_confidence` on older builds | **Fixed** (TypeError fallback) |
| `FALL_PATIENT_ID` placeholder | Alerts intentionally skipped until set |
| Backend URL | Env-overridable; still a private LAN default |
| GPIO / real alarm | Still log stubs in `play_alarm_sound_and_vibrate` |
| Host without OpenCV/MediaPipe | Landmarks path works; camera/`/frame` returns note |
| MediaPipe Solutions (`mp.solutions.pose`) | **Replaced** by Tasks `PoseLandmarker` (mediapipe 0.10.30+) |
| `datetime.utcnow()` in alerts | **Replaced** with timezone-aware UTC |
| Dual OpenCV packages (`headless` + `contrib`) | Avoid — mediapipe already depends on `opencv-contrib-python` |

## Tests

`fall_detection/tests/test_fall_detector.py` covers:

- polygon contains + signed distance
- inside / fall / left_bed / near_edge temporal behaviour
- low visibility / missing ROI
- `process_normalized_landmarks` (objects + JSON dicts)
- config loads hub polygon

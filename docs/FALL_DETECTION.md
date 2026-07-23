# Fall Detection — Design, Math, and Operations

**Status:** Lab-validated prototype (phone landmarks + hub rules)  
**Package:** `fall_detection_pi` (standalone; not nested under BLE-only code)  
**Hub consumer:** `medical_ble_web` (optional)  
**Android lineage:** `edge-ai-fall-detection` (Kotlin MediaPipe / ROI rules)  
**Last updated:** 2026-07-23  

This document explains **what we built**, **why**, the **equations**, state machine,
APIs, config, and **known reliability limits**. It is the source of truth before further
hardening or MQTT work.

---

## 1. Goals and non-goals

### Goals
- Detect **in bed (safe)**, **near bed edge**, **left bed**, and **fall**.
- Run on a **Raspberry Pi hub** with optional USB/Pi camera.
- Allow **calibration** of the bed region from a browser/phone UI.
- Work **without a local camera** by accepting client-side pose landmarks (phone).
- Keep fall CV **separable** from BLE medical device collection (`sh3-pi` / hub toolkit).

### Non-goals (current)
- FDA/clinical certification of the algorithm.
- Full-body multi-person tracking (we take **one** pose).
- MQTT fall alerts (HTTP alert sink exists; MQTT not wired for fall yet).
- Hardware buzzer/GPIO (alarm is log stub).

---

## 2. Repository layout — **sibling only**

Fall detection is **not** inside the BLE tree. The **only supported layout** is
**siblings** under one workspace (or monorepo) root:

```
workspace/   (e.g. SHHMHub/ clone root)
├── fall_detection_pi/           # CV package  →  import fall_detection_pi
├── sh3-pi/                      # BLE hub only
│   ├── medical_ble_web/         # FastAPI + fall_import + /api/fall/*
│   ├── medical_ble_toolkit/
│   └── docs/FALL_DETECTION.md   # this file
├── edge-ai-fall-detection/      # Android Kotlin reference (SHHMHub)
└── …
```

| Path | Role |
|------|------|
| `fall_detection_pi/` | All pose / fall rules / camera loop |
| `sh3-pi/` | BLE, SQLite, MQTT, web UI — **no** fall package folder inside |

How the hub finds the package (`medical_ble_web/fall_import.py`):

1. Already on `PYTHONPATH` / `pip install -e ./fall_detection_pi`
2. `FALL_DETECTION_HOME` env → absolute path to the package directory
3. **Sibling of `sh3-pi`:** `<workspace>/fall_detection_pi`  
   (i.e. parent of the hub root + `/fall_detection_pi`)

**Do not** put the package at `sh3-pi/fall_detection_pi/` or `sh3-pi/fall_detection/`.
That nested layout is **unsupported** for SHHMHub and for new work.

### Why split (and keep sibling)
| Reason | Detail |
|--------|--------|
| Ownership | Fall CV vs multi-brand BLE evolve at different rates |
| Deploy | Hub can run BLE without MediaPipe installed |
| Clear imports | `import fall_detection_pi` vs toolkit brands |
| Milestone mapping | Aligns with H1.5.x fall tasks vs H1.1–H1.4 BLE |
| Monorepo clarity | SHHMHub top-level folders are product modules |
---

## 3. End-to-end pipeline

```
                    ┌─────────────────────┐
  USB/Pi camera ───►│ OpenCV VideoCapture │──┐
                    └─────────────────────┘  │
  Phone JPEG ──────►│ POST /api/fall/frame  │──┤
                    └─────────────────────┘  │
                                             ▼
                              MediaPipe Tasks PoseLandmarker
                              (33 landmarks, IMAGE or VIDEO mode)
                                             │
  Phone JS Pose ───►│ POST /api/fall/landmarks │ (skip host MediaPipe)
                    └──────────────────────────┘
                                             ▼
                              Landmark normalize (shoulders/hips/…)
                                             ▼
                              FallDetector.evaluate(landmarks, PolygonROI)
                                             ▼
                    ┌────────────────────────────────────────┐
                    │ DetectionState                         │
                    │  in_safe_area | near_edge | left_bed | │
                    │  fall_detected                         │
                    └────────────────────────────────────────┘
                           │                    │
                           ▼                    ▼
                    UI event string      publish_event_if_needed
                    (live banner)        (HTTP /fall-events, cooldown)
                           │
                           ▼
                    Local alarm stub (log)
```

### Input modes

| Mode | Who runs pose? | API | Landmarker mode |
|------|----------------|-----|-----------------|
| Local camera thread | Hub | background `camera_loop.run()` | **VIDEO** |
| Browser JPEG upload | Hub | `POST /api/fall/frame` | **IMAGE** |
| Browser landmarks | Phone (MediaPipe JS) | `POST /api/fall/landmarks` | none on hub |

**Lab result:** landmarks mode is the most reliable on Windows (no local camera required).

---

## 4. Coordinate system and landmarks

### Image coordinates
- All geometry uses **normalized** image space: \(x, y \in [0, 1]\).
- Origin: **top-left**.
- \(+x\): right; \(+y\): **down** (standard image convention).
- Therefore a **fall / drop** increases \(y\) over time.

### MediaPipe body indices used

| Name | Index | Role |
|------|-------|------|
| LEFT_SHOULDER | 11 | Torso |
| RIGHT_SHOULDER | 12 | Torso |
| LEFT_HIP | 23 | Torso |
| RIGHT_HIP | 24 | Torso |
| LEFT_KNEE / ANKLE | 25 / 27 | BBox aspect only (fallback to hip) |
| RIGHT_KNEE / ANKLE | 26 / 28 | BBox aspect only |

Visibility gate uses **shoulders + hips only** (core). Knees/ankles often have low
visibility on phone cameras; gating on them previously forced permanent “empty”
frames and a stuck SAFE UI.

---

## 5. Equations

### 5.1 Torso center

\[
x_t = \frac{x_{LS} + x_{RS} + x_{LH} + x_{RH}}{4}, \quad
y_t = \frac{y_{LS} + y_{RS} + y_{LH} + y_{RH}}{4}
\]

This point is tested against the bed polygon (not head or feet alone).

### 5.2 Torso angle (posture axis)

Shoulder midpoint and hip midpoint:

\[
(x_s, y_s) = \left(\frac{x_{LS}+x_{RS}}{2},\; \frac{y_{LS}+y_{RS}}{2}\right)
\]
\[
(x_h, y_h) = \left(\frac{x_{LH}+x_{RH}}{2},\; \frac{y_{LH}+y_{RH}}{2}\right)
\]

\[
\theta = \left|\operatorname{atan2}(y_h - y_s,\; x_h - x_s)\right| \cdot \frac{180}{\pi}
\quad \text{(degrees)}
\]

| Interpretation (image space) | Typical \(\theta\) |
|------------------------------|--------------------|
| Person upright (hips below shoulders) | near \(90^\circ\) |
| Person lying left–right | near \(0^\circ\) (or \(180^\circ\), same abs) |

**Horizontal posture flag:**

\[
\text{is\_horizontal} = (\theta < \theta_{\max}), \quad \theta_{\max} = 25^\circ
\quad \text{(config: `horizontal_angle_deg`)}
\]

### 5.3 Bounding-box aspect (lying proxy)

Using shoulders, hips, knees (or hip fallbacks):

\[
w = \max x_i - \min x_i, \quad
h = \max y_i - \min y_i
\]
\[
a = \begin{cases}
h / w & w > 0.01 \\
2.0 & \text{otherwise}
\end{cases}
\]

\[
\text{is\_lying} = (a < a_{\max}), \quad a_{\max} = 0.6
\quad \text{(config: `lying_aspect_ratio`)}
\]

Wide-and-short silhouette → “lying-like”.  
\[
\text{is\_posture\_down} = \text{is\_horizontal} \lor \text{is\_lying}
\]

### 5.4 Region of interest (bed polygon)

ROI is a polygon \(P = \{(x_i, y_i)\}_{i=1}^{n}\), \(n \ge 3\), normalized.

**Contains:** OpenCV `pointPolygonTest` when available; else ray-casting.

**Signed distance** \(d(x_t, y_t)\):
- \(d > 0\): inside  
- \(d < 0\): outside  
- Magnitude: distance to nearest edge (OpenCV measureDist, or pure-Python segment distance)

**Near edge** (outside but close):

\[
\text{near} = (\lnot \text{inside}) \land \bigl(d > -\;\tau\bigr), \quad
\tau = 0.05
\quad \text{(config: `edge_tolerance`)}
\]

**Axis-aligned bounds** of the polygon (for classic “below bed”):

\[
y_{\text{bed bottom}} = \max_i y_i
\]

\[
\text{below\_bed} = (y_t > y_{\text{bed bottom}})
\]

Note: this is an **AABB bottom**, not a true 3D bed plane. Tilted polygons are approximate.

### 5.5 Rapid drop (velocity)

Feature history stores \((x_t, y_t, \theta, a, t)\) for the last \(N\) samples
(`history_maxlen = 25`).

Over a window \(T = 1.0\,\mathrm{s}\):

\[
\Delta y = y_t^{(\text{latest})} - y_t^{(\text{oldest in window})}
\]

\[
\text{is\_rapid\_drop} = (\Delta y > v_{\min}), \quad
v_{\min} = 0.08
\quad \text{(8% of frame height in 1 second; `rapid_drop_threshold`)}
\]

Because \(+y\) is down, a **positive** \(\Delta y\) means the torso moved **downward** in the image.

### 5.6 Fall candidates

Three OR-ed raw conditions (then temporal confirm):

**Classic (Android-style):**
\[
\text{classic\_fall} =
(\lnot \text{inside}) \land \text{is\_horizontal} \land \text{below\_bed}
\]

**Velocity:**
\[
\text{velocity\_fall} =
(\lnot \text{inside}) \land \text{is\_posture\_down} \land \text{is\_rapid\_drop}
\]

**Sustained down outside** (slow slide / already on floor):
\[
\text{down\_outside} = (\lnot \text{inside}) \land \text{is\_posture\_down}
\]
Count consecutive frames with `down_outside`. When count \(\ge N_{\text{sust}}\)
(\(N_{\text{sust}} = 10\)):

\[
\text{sustained\_fall} = \text{true}
\]

**Raw fall signal:**
\[
\text{fall\_raw} = \text{classic\_fall} \lor \text{velocity\_fall}
\]
(and sustained contributes via the same confirm counter when active)

**Temporal confirm (anti-flicker for fall only):**
\[
\text{fall\_detected} =
\bigl(\text{consecutive\_fall\_frames} \ge N_{\text{fall}}\bigr), \quad
N_{\text{fall}} = 3
\]

Counter increments when `fall_raw` or `sustained_fall`, else resets to 0.

---

## 6. State machine (output)

Priority order in code:

1. If `fall_detected` → **FALL**  
2. Else if `inside` → **SAFE** (`in_safe_area`)  
3. Else if `near` → **NEAR_EDGE**  
4. Else → **LEFT_BED**

| Output | Instant? | UI / API event | Backend event type |
|--------|----------|----------------|--------------------|
| In bed | Yes | `SAFE` | (clears local published type) |
| Near edge | Yes | `PATIENT_NEAR_EDGE` | same |
| Left bed | Yes | `PATIENT_LEFT_BED` | same |
| Fall | After 3 frames of evidence | `FALL_DETECTED` | same |

### Why region states are instant, fall is not
- **UX:** Old Android port felt “live”; multi-frame voting on leave made the UI look stuck on SAFE during early experiments.
- **Safety for fall:** Require 3 frames so a single noisy pose does not spam CRITICAL.

### Side effects on state *entry* only
- Toast log + alarm stub fire when the **event string changes** (not every frame).
- HTTP publish is further limited by **cooldown** (default 60s per event type).

---

## 7. Module map

| Module | Responsibility |
|--------|----------------|
| `fall_detection_pi/fall_detector.py` | Pure rules: ROI, posture, velocity, state |
| `fall_detection_pi/camera_loop.py` | Camera / frame / landmarks I/O, landmarker, overlays, alarms |
| `fall_detection_pi/pose_model.py` | Download/cache `.task` model |
| `fall_detection_pi/config.py` | Env + hub_config ROI load/save path |
| `fall_detection_pi/alert_api.py` | Async HTTP POST to backend |
| `medical_ble_web/fall_import.py` | Locate **sibling** package `fall_detection_pi` |
| `medical_ble_web/app.py` | Lifespan camera thread + `/api/fall/*` |
| `medical_ble_web/static/fall.html` | ROI draw + phone pose + status banner |

---

## 8. HTTP API (hub)

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/fall/status` | Package found? path? |
| `GET` | `/api/fall/roi` | Current bed polygon |
| `POST` | `/api/fall/roi` | Set polygon + persist `hub_config.json` |
| `POST` | `/api/fall/landmarks` | Evaluate pose JSON (preferred for phone) |
| `POST` | `/api/fall/frame` | JPEG → hub PoseLandmarker |
| `GET` | `/api/fall/stream` | MJPEG last processed frame |
| UI | `/static/fall.html` | Calibration + monitoring |

If package missing → **503** on fall routes (BLE hub still runs).

### Landmarks body shapes
1. **Named dict** (tests / simple clients):
   ```json
   {
     "landmarks": {
       "LEFT_SHOULDER": {"x": 0.4, "y": 0.3, "visibility": 1},
       "RIGHT_SHOULDER": {"x": 0.6, "y": 0.3, "visibility": 1},
       "LEFT_HIP": {"x": 0.4, "y": 0.45, "visibility": 1},
       "RIGHT_HIP": {"x": 0.6, "y": 0.45, "visibility": 1}
     }
   }
   ```
2. **MediaPipe 33-list** (phone JS): array of `{x,y,z,visibility}`; indices 11,12,23–28 used.

### Response (landmarks)
```json
{
  "ok": true,
  "event": "SAFE",
  "has_pose": true,
  "detection_state": {
    "in_safe_area": true,
    "near_edge": false,
    "fall_detected": false,
    "left_bed": false,
    "torso_x": 0.5,
    "torso_y": 0.375,
    "torso_angle_deg": 90.0
  }
}
```

`event` is derived from **live** detection state (not blocked by HTTP cooldown).

---

## 9. Configuration

### Environment

| Variable | Default | Meaning |
|----------|---------|---------|
| `FALL_DETECTION_HOME` | (auto) | Path to package dir |
| `FALL_PATIENT_ID` | `REPLACE_WITH_PATIENT_ID` | **Must set** or HTTP alerts are skipped |
| `FALL_BACKEND_BASE_URL` | `http://172.16.2.156:5173/api` | Alert base URL |
| `FALL_DEVICE_ID` | `edge-ai-camera-01` | Device id in payload |
| `FALL_INTEGRATION_KEY` | empty | Optional header |
| `FALL_COOLDOWN_MS` | `60000` | Min ms between same event type |
| `FALL_CAMERA_SOURCE` / `RTSP_URL` | `0` | OpenCV capture |
| `FALL_POSE_MODEL` | `lite` | `lite` \| `full` \| `heavy` |
| `FALL_POSE_MODEL_PATH` | package `models/…` | Local `.task` file |
| `FALL_HUB_CONFIG` | auto-search | Path to `hub_config.json` |
| `FALL_MIN_DET_CONF` / `_TRACK_` / `_PRES_` | `0.5` | PoseLandmarker thresholds |

### Detector defaults (code)

| Parameter | Default | Role |
|-----------|---------|------|
| `edge_tolerance` | 0.05 | Near-edge band |
| `min_visibility` | 0.5 | Core landmark gate |
| `horizontal_angle_deg` | 25 | Horizontal torso |
| `lying_aspect_ratio` | 0.6 | Lying bbox |
| `rapid_drop_threshold` | 0.08 | Δy in 1s |
| `rapid_drop_window_s` | 1.0 | Velocity window |
| `fall_confirm_frames` | 3 | Fall temporal |
| `sustained_fall_frames` | 10 | Slow / on-floor |

### ROI persistence
JSON under key **`fall_detection.polygon`** (name kept for backward compatibility;
package folder is `fall_detection_pi`):

```json
{
  "fall_detection": {
    "polygon": [[0.2, 0.2], [0.8, 0.2], [0.8, 0.6], [0.2, 0.6]]
  }
}
```

Search order for load/save is documented in `fall_detection_pi/config.py`
(env → sibling hub `hub_config.json` → package-local → toolkit path).

---

## 10. Alert HTTP payload

When patient id is configured, `alert_api.post_event` POSTs to  
`{FALL_BACKEND_BASE_URL}/fall-events`:

```json
{
  "patientId": "…",
  "eventType": "FALL_DETECTED",
  "severity": "critical",
  "fallDetected": true,
  "detectedAt": "2026-07-23T…Z",
  "sourceApp": "EdgeAiFallDetection",
  "deviceId": "edge-ai-camera-01",
  "cooldownMs": 60000,
  "confidence": 0.95,
  "nearEdge": false,
  "inSafeArea": false
}
```

Posts run on a **daemon thread** (non-blocking). **No retry queue** yet.  
Default patient id → **skip publish** (log warning only).

---

## 11. Design history (what we did and why)

| Decision | Why |
|----------|-----|
| Leave Android Kotlin as reference; reimplement in Python | Pi hub runtime is Python FastAPI |
| MediaPipe **Tasks** `PoseLandmarker`, not `mp.solutions.pose` | Solutions API removed in mediapipe ≥0.10.30 |
| Auto-download `.task` model | Deploy without checking in multi‑MB binaries |
| Polygon ROI (not only axis-aligned rect) | Bed not always rectangular in camera view |
| Instant near/left | Multi-frame voting made UI look “stuck on SAFE” |
| Fall 3-frame confirm | Reduce single-frame false CRITICAL |
| Sustained down path | Catch slow slide / already on floor without velocity |
| Core-only visibility | Ankle/knee low-vis froze detection on phones |
| Alarm only on state entry | Prevent per-frame log spam |
| Package split `fall_detection_pi` | Separate CV from BLE lifecycle and monorepo ownership |
| Phone landmarks API | Reliable try-out without Pi camera on Windows |

### Evolution from first Python port
1. **v1 port:** `RectFNorm` + single-frame fall = outside ∧ horizontal ∧ below_bed.  
2. **v2:** Polygon + temporal + rapid drop (too sticky for leave/near).  
3. **v3 (current):** Instant region states + multi-path fall + package split + Tasks API.

---

## 12. How to run (development)

Assume **sibling** layout. All commands from the **workspace root** (folder that
contains both `fall_detection_pi/` and `sh3-pi/`), e.g. the SHHMHub clone root.

### Install

```bash
# Workspace root
python -m venv .venv
# Windows: .venv\Scripts\activate
source .venv/bin/activate   # Linux/Pi

pip install -r sh3-pi/requirements.txt
pip install -r sh3-pi/medical_ble_web/requirements.txt
pip install -e ./fall_detection_pi
# or: pip install -r fall_detection_pi/requirements.txt
```

### Unit tests (no camera)

```bash
# Workspace root — parent of fall_detection_pi must be on PYTHONPATH
set PYTHONPATH=.
python -m pytest fall_detection_pi/tests/test_fall_detector.py -v
python fall_detection_pi/tests/_tryout_sim.py
```

### Hub + SSL

```bash
cd sh3-pi/medical_ble_web
# so fall_import can resolve ../../fall_detection_pi and import hub modules
set PYTHONPATH=..;.;../..
python app.py --ssl
```

- UI: `https://127.0.0.1:8741/static/fall.html`  
- Status: `GET /api/fall/status` → `"package": "fall_detection_pi"` and a path  
  ending in `…/fall_detection_pi` (sibling of `sh3-pi`, not under it).  

### Enable backend alerts

```bash
set FALL_PATIENT_ID=your-patient-uuid
set FALL_BACKEND_BASE_URL=https://your-backend/api
```

---

## 13. Known limitations (audit summary)

Documented so we harden deliberately later—not as silent surprises.

| Area | Limitation |
|------|------------|
| Edge of bed | Instant near/left can **flicker** if torso jitters across boundary |
| Fall false positives | Sitting/kneeling outside + “lying-like” bbox can trip **sustained fall** |
| Rapid drop | Walking toward camera / sitting can increase \(y\) without a fall |
| below_bed | Uses polygon **max Y**, not true bed plane |
| No pose | Can surface as SAFE-like empty path depending on client |
| Frame upload event | Prefer **landmarks** path for UI correctness (frame path may lag publish state) |
| Alerts | Off until patient id set; no MQTT fall topic yet |
| Alarm | Log stub only |
| Pi camera | Continuous VIDEO path not soak-tested on device at 15+ FPS |
| Security | Fall APIs unauthenticated (LAN hub assumption) |

---

## 14. Reliability stance (current)

| Use case | Ready? |
|----------|--------|
| ROI calibration + leave/near demo | **Yes** |
| Fall demo (lie down outside bed / rapid drop) | **Yes** (tune thresholds if needed) |
| 24/7 ward without supervision | **Not yet** — needs hysteresis, hold-on-occlusion, alert pipeline, Pi soak |
| Clinical claim | **No** |

---

## 15. Tests

`fall_detection_pi/tests/test_fall_detector.py` covers:

- Polygon contains + signed distance  
- Instant inside / near / left  
- Classic fall + rapid-drop fall  
- Core visibility vs low knee visibility  
- API-style dict processing via `process_normalized_landmarks`  

---

## 16. Related code and docs

| Path | Role |
|------|------|
| `fall_detection_pi/README.md` | Short package install |
| `docs/LINUX.md` | Pi install of sibling package |
| `docs/PROJECT_AGENT_GUIDE.md` | Whole-repo agent map |
| `edge-ai-fall-detection/` (SHHMHub) | Android original |
| `hardware_tasks_milestone_1.md` | H1.5.1–H1.5.3 task IDs |

---

## 17. Suggested hardening order (future; not done here)

1. Hysteresis / debounce on near ↔ left (stop edge thrash).  
2. Hold last confirmed state on low visibility / no pose.  
3. Align `process_client_frame` event string with landmarks path.  
4. Tighten fall so “sit outside” ≠ fall without stronger evidence.  
5. MQTT fall-alert payload (H1.5.3).  
6. Pi camera soak + reconnect.  

Until then, treat this document as the contract for behavior and math.

# Fall Detection Architecture

This document outlines the architecture and math behind the computer-vision fall detection pipeline running on the Raspberry Pi Hub.

The logic in this module was ported 1:1 from the original Android Kotlin implementation (`edge-ai-fall-detection`), ensuring consistent clinical evaluation and behaviour.

## Pipeline Overview

The pipeline runs continuously in a background daemon thread, tied to the FastAPI startup lifecycle (`medical_ble_web/app.py`).

1. **Camera Capture**: `OpenCV` connects to `/dev/video0` and pulls live frames from the attached USB/Pi camera.
2. **Pose Inference**: Each frame is passed to **Google MediaPipe Pose**. MediaPipe evaluates the frame and returns 33 3D body landmarks.
3. **Logic Evaluation**: The core mathematical rules engine (`fall_detection/fall_detector.py`) extracts specific landmarks to determine if the patient has fallen or left the bed.
4. **Alerting**: State changes trigger local hooks (alarm sounds/vibration) and dispatch HTTP POST events to the backend (`alert_api.py`).

---

## Core Evaluation Logic

We only process a specific subset of the 33 MediaPipe landmarks:
- **Shoulders**: `LEFT_SHOULDER` (11), `RIGHT_SHOULDER` (12)
- **Hips**: `LEFT_HIP` (23), `RIGHT_HIP` (24)
- **Knees/Ankles**: (25-28) used as fall-backs if visibility is low.

The rules engine relies on an established **Region of Interest (ROI)** — a normalized bounding box (`0.0` to `1.0`) representing the "Safe Area" (the patient's bed).

For every frame, the detector calculates:

1. **Torso Center**: The midpoint of the bounding box formed by the shoulders and hips.
   ```python
   torso_center_x = (ls.x + rs.x + lh.x + rh.x) / 4.0
   torso_center_y = (ls.y + rs.y + lh.y + rh.y) / 4.0
   ```

2. **Torso Angle**: The angle between the center of the shoulders and the center of the hips, calculated using `math.atan2`.
   ```python
   angle_deg = abs(angle_rad * 180.0 / math.pi)
   is_horizontal = angle_deg < 25.0
   ```

3. **Body Height**: The average Y-coordinate of the torso.

### State Machine Rules

Based on the calculations, the engine determines the patient's state:

| State | Rule | Description |
|-------|------|-------------|
| `✅ in_safe_area` | `roi.contains(torso_center)` | The patient's torso center is safely inside the bed boundary. |
| `⚠️ near_edge` | `!inside && expanded_roi.contains(torso_center)` | The patient's torso is outside the bed, but within a 5% expanded margin. They are dangling or about to leave. |
| `🚨 fall_detected` | `!inside && is_horizontal && below_bed` | The patient is outside the bed, their body axis is horizontal (<25°), and their height is physically lower than the bottom boundary of the bed. |
| `⚠️ left_bed` | `!inside && !near_edge && !fall_detected` | The patient is entirely outside the bed boundaries and standing upright. |

---

## Alerting & Rate Limiting

To prevent spamming the backend or nursing dashboard with hundreds of events per second, alerts are rate-limited via `fall_detection/alert_api.py`.

- **Cooldown**: `COOLDOWN_MS` (default 60 seconds).
- **Behaviour**: If the state machine triggers a `FALL_DETECTED` event, the HTTP POST request is sent immediately. The system will not send another `FALL_DETECTED` event until 60 seconds have passed. 
- **Threading**: The HTTP POST request uses `requests.post()` but is wrapped in a `threading.Thread` daemon to ensure that network latency or backend timeouts do not block the live video inference loop.

---

## Current Limitations & TODOs

### 1. Hardcoded ROI
Currently, the Region of Interest is hardcoded in `camera_loop.py`:
```python
roi_rect = RectFNorm(0.0, 0.0, 1.0, 1.0)
```
This means the "bed" occupies the entire camera frame, making it impossible for the patient to technically "leave the bed". 

**Next Step**: Expose a REST API endpoint (e.g., `POST /api/fall/roi`) in FastAPI so the frontend web interface can submit drawn coordinate boundaries directly to the Hub.

### 2. Hardware Alarm Stubs
The functions `play_alarm_sound_and_vibrate()` and `stop_alarm_sound_and_vibration()` in `camera_loop.py` are currently just logging statements. They need to be wired to physical GPIO pins on the Raspberry Pi to trigger a hardware buzzer.

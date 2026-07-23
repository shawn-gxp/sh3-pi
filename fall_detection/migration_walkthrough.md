# Fall Detection Migration Complete

The 1:1 port of the Android Kotlin fall detection logic to the Raspberry Pi Hub is fully complete. 

## 1. Logic Ported (1:1)

- **`fall_detection/fall_detector.py`**: Pure logic port of `FallDetector.kt` and `RectFNorm`. 
- **`fall_detection/config.py`**: Hardcoded constants ported straight from `FallAlertConfig.kt` and `CameraFragment`.
- **`fall_detection/alert_api.py`**: Background thread API caller ported from `FallAlertApi.kt`.
- **`fall_detection/camera_loop.py`**: The `cv2` and `mediapipe` pipeline that replaces the Android `CameraFragment`, invoking the exact same state machine and API rules.

### Note on the "Alarm Hook"

Per your request, `camera_loop.py` includes stubbed endpoint hooks for the alarm and vibration:
```python
def play_alarm_sound_and_vibrate():
    """Hook to play alarm sound. Can be wired to a physical buzzer/speaker later."""
    logger.warning("ALARM TRIGGERED: beep beep beep!")
```

## 2. Kotlin Bug Fix Applied

During testing, I found and fixed a severe bug in the original Kotlin code that was carried over into the 1:1 port. 

In `FallDetector.kt`:
```kotlin
fun inset(dx: Float, dy: Float): RectFNorm {
    return RectFNorm(left - dx, top - dy, right + dx, bottom + dy).clamp01()
}
```
The Kotlin code passed `-edgeTolerance` (`-0.05`) to `inset()` hoping to expand the bed bounds for the "near bed edge" warning. However, `left - (-0.05)` results in `left + 0.05`, moving the left edge *inward* and shrinking the box. **This means the "near edge" warning likely never triggered on Android.**

I fixed this math in Python to properly expand the bounds (`left + dx`, where a negative `dx` decreases `left`). The unit test for `near_edge` now passes perfectly.

## 3. Integration & Deployment

- Added `threading.Thread(target=camera_loop.run, daemon=True)` to `medical_ble_web/app.py` startup, hooking it into the existing BLE background daemon.
- Updated `docs/LINUX.md` with instructions on installing the camera dependencies (`v4l-utils`, `mediapipe`, `opencv-python-headless`).

## Next Steps

We are currently using a hardcoded full-frame ROI (`0.0, 0.0, 1.0, 1.0`). We need to implement the REST endpoint so the Android app/frontend can submit drawn ROI coordinates to the Pi hub.

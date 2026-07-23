"""
Camera / frame fall-detection loop.

Pose inference uses MediaPipe **Tasks** API (PoseLandmarker), not the
deprecated `mp.solutions.pose` Solutions API (removed in mediapipe ≥0.10.30).
"""
from __future__ import annotations

import logging
import threading
import time
from typing import Any, List, Optional, Sequence, Tuple

# --- OpenCV (always useful for decode / overlay / capture) ---
try:
    import cv2
    import numpy as np

    HAVE_CV2 = True
except Exception:
    HAVE_CV2 = False
    cv2 = None  # type: ignore
    np = None  # type: ignore

# --- MediaPipe Tasks (PoseLandmarker) ---
HAVE_POSE = False
mp = None  # type: ignore
mp_vision = None  # type: ignore
BaseOptions = None  # type: ignore
PoseLandmarker = None  # type: ignore
PoseLandmarkerOptions = None  # type: ignore
RunningMode = None  # type: ignore
PoseLandmark = None  # type: ignore
PoseLandmarksConnections = None  # type: ignore
drawing_utils = None  # type: ignore

try:
    import mediapipe as mp
    from mediapipe.tasks.python.core.base_options import BaseOptions
    from mediapipe.tasks.python.vision import (
        PoseLandmark,
        PoseLandmarker,
        PoseLandmarkerOptions,
        PoseLandmarksConnections,
        RunningMode,
        drawing_utils,
    )

    HAVE_POSE = True
except Exception as err:  # pragma: no cover - import env dependent
    logging.getLogger("CameraLoop").debug("MediaPipe Tasks unavailable: %s", err)

from . import alert_api, config
from .fall_detector import DetectionState, FallDetector, NormalizedPoint, PolygonROI

logger = logging.getLogger("CameraLoop")

# Landmark indices (same numbering as classic Pose; Tasks PoseLandmark enum)
_LM = {
    "LEFT_SHOULDER": 11,
    "RIGHT_SHOULDER": 12,
    "LEFT_HIP": 23,
    "RIGHT_HIP": 24,
    "LEFT_KNEE": 25,
    "RIGHT_KNEE": 26,
    "LEFT_ANKLE": 27,
    "RIGHT_ANKLE": 28,
}

# Thread-safe state
_lock = threading.Lock()
_running = True
_latest_jpeg: Optional[bytes] = None
_active_polygon = config.DEFAULT_POLYGON
_roi: Optional[PolygonROI] = PolygonROI(_active_polygon)

last_published_event_type: Optional[str] = None
last_published_at: int = 0
last_toast_message: Optional[str] = None
last_toast_time: int = 0
# Last state we already toasted/alarmed (avoids per-frame beep spam)
_last_applied_event: Optional[str] = None

fall_detector = FallDetector()

# Landmarker instances (IMAGE for still uploads; VIDEO for camera stream)
_landmarker_image = None
_landmarker_video = None
_video_timestamp_ms = 0


def stop() -> None:
    """Signals background camera thread to terminate cleanly; closes landmarkers."""
    global _running, _landmarker_image, _landmarker_video
    _running = False
    logger.info("Camera loop shutdown requested.")
    with _lock:
        for attr in ("_landmarker_image", "_landmarker_video"):
            inst = globals().get(attr)
            if inst is not None:
                try:
                    inst.close()
                except Exception:
                    pass
                globals()[attr] = None


def get_latest_jpeg() -> Optional[bytes]:
    with _lock:
        return _latest_jpeg


def update_polygon(polygon: List[Tuple[float, float]]) -> None:
    global _active_polygon, _roi
    with _lock:
        _active_polygon = polygon
        _roi = PolygonROI(polygon)
        # ROI change invalidates temporal history (avoid false fall after redraw)
        fall_detector.reset()
    logger.info("Updated active bed polygon to: %s", polygon)


def get_active_polygon() -> List[Tuple[float, float]]:
    with _lock:
        return list(_active_polygon)


def get_current_detection_state() -> Optional[str]:
    return last_published_event_type


def show_toast(message: str) -> None:
    global last_toast_message, last_toast_time
    current_time = int(time.time() * 1000)
    if message != last_toast_message or current_time - last_toast_time > config.LOG_COOLDOWN_MS:
        logger.info("TOAST: %s", message)
        last_toast_message = message
        last_toast_time = current_time


def play_alarm_sound_and_vibrate() -> None:
    logger.warning("ALARM TRIGGERED: beep beep beep!")


def stop_alarm_sound_and_vibration() -> None:
    logger.debug("Alarm stopped.")


def publish_event_if_needed(
    event_type: str,
    severity: str,
    detection_state: DetectionState,
    confidence: Optional[float] = None,
) -> None:
    global last_published_event_type, last_published_at
    now = int(time.time() * 1000)

    if last_published_event_type == event_type and (now - last_published_at) < config.COOLDOWN_MS:
        return

    last_published_event_type = event_type
    last_published_at = now

    alert_api.post_event(
        patient_id=config.PATIENT_ID,
        event_type=event_type,
        severity=severity,
        confidence=confidence,
        near_edge=detection_state.near_edge,
        in_safe_area=detection_state.in_safe_area,
    )


def _visibility(lm: Any) -> float:
    v = getattr(lm, "visibility", None)
    if v is None:
        v = getattr(lm, "presence", 1.0)
    try:
        return float(v if v is not None else 1.0)
    except (TypeError, ValueError):
        return 1.0


def pose_landmarks_to_dict(pose_landmarks: Sequence[Any]) -> dict:
    """Convert Tasks list[NormalizedLandmark] (len 33) → named NormalizedPoint dict."""
    if pose_landmarks is None or len(pose_landmarks) <= _LM["RIGHT_ANKLE"]:
        return {}

    def pt(idx: int) -> NormalizedPoint:
        lm = pose_landmarks[idx]
        return NormalizedPoint(
            float(lm.x),
            float(lm.y),
            float(getattr(lm, "z", 0.0) or 0.0),
            _visibility(lm),
        )

    return {name: pt(idx) for name, idx in _LM.items()}


def handle_pose_landmarks(pose_landmarks: Sequence[Any]) -> None:
    """Evaluate fall state from one pose's landmark list (Tasks API)."""
    normalized = pose_landmarks_to_dict(pose_landmarks)
    if not normalized:
        return

    # Single lock for ROI snapshot + detector path + alert side-effects so
    # camera thread and HTTP frame/landmarks routes cannot interleave state.
    with _lock:
        current_roi = _roi
        if current_roi is None or not current_roi.is_valid():
            return
        detection_state = fall_detector.evaluate(normalized, current_roi)
        _apply_detection_state(detection_state)


# Back-compat alias (older call sites / tests)
def handle_fall_detection(landmarks) -> None:
    """
    Accept either:
    - Tasks API: list of 33 landmarks
    - Legacy Solutions: object with .landmark list
    """
    if landmarks is None:
        return
    if hasattr(landmarks, "landmark"):
        handle_pose_landmarks(landmarks.landmark)
    else:
        handle_pose_landmarks(landmarks)


def event_from_detection_state(detection_state: DetectionState) -> str:
    """UI event from current frame state (not publish cooldown)."""
    if detection_state.fall_detected:
        return "FALL_DETECTED"
    if detection_state.near_edge:
        return "PATIENT_NEAR_EDGE"
    if detection_state.left_bed:
        return "PATIENT_LEFT_BED"
    if detection_state.in_safe_area:
        return "SAFE"
    return "SAFE"


def _apply_detection_state(detection_state: DetectionState) -> None:
    """Side effects for a new detection state. Alarm/toast only on state *entry*."""
    global last_published_event_type, _last_applied_event
    event = event_from_detection_state(detection_state)
    entered = event != _last_applied_event

    if detection_state.fall_detected:
        if entered:
            show_toast("CRITICAL: Fall detected")
            play_alarm_sound_and_vibrate()
        publish_event_if_needed("FALL_DETECTED", "critical", detection_state, confidence=0.95)
    elif detection_state.near_edge:
        if entered:
            show_toast("Warning: Patient near bed edge")
            play_alarm_sound_and_vibrate()
        publish_event_if_needed("PATIENT_NEAR_EDGE", "high", detection_state)
    elif detection_state.left_bed:
        if entered:
            show_toast("ALERT: Patient has left the safe area!")
            play_alarm_sound_and_vibrate()
        publish_event_if_needed("PATIENT_LEFT_BED", "high", detection_state)
    elif detection_state.in_safe_area:
        if entered and _last_applied_event not in (None, "SAFE"):
            show_toast("Patient is inside the safe area.")
            stop_alarm_sound_and_vibration()
        last_published_event_type = None

    _last_applied_event = event


def process_normalized_landmarks(landmarks_data) -> dict:
    """Evaluates fall detection from landmark data (list of 33 points or named dict)."""
    normalized_pts: dict = {}
    if isinstance(landmarks_data, list) and len(landmarks_data) > 28:

        def get_pt(idx: int) -> NormalizedPoint:
            p = landmarks_data[idx]
            if isinstance(p, NormalizedPoint):
                return p
            if isinstance(p, dict):
                return NormalizedPoint(
                    p.get("x", 0), p.get("y", 0), p.get("z", 0), p.get("visibility", 1.0)
                )
            return NormalizedPoint(
                getattr(p, "x", 0),
                getattr(p, "y", 0),
                getattr(p, "z", 0),
                getattr(p, "visibility", 1.0),
            )

        normalized_pts = {
            "LEFT_SHOULDER": get_pt(11),
            "RIGHT_SHOULDER": get_pt(12),
            "LEFT_HIP": get_pt(23),
            "RIGHT_HIP": get_pt(24),
            "LEFT_KNEE": get_pt(25),
            "RIGHT_KNEE": get_pt(26),
            "LEFT_ANKLE": get_pt(27),
            "RIGHT_ANKLE": get_pt(28),
        }
    elif isinstance(landmarks_data, dict):
        for k, v in landmarks_data.items():
            if isinstance(v, NormalizedPoint):
                normalized_pts[k] = v
            elif isinstance(v, dict):
                normalized_pts[k] = NormalizedPoint(
                    v.get("x", 0), v.get("y", 0), v.get("z", 0), v.get("visibility", 1.0)
                )
            elif v is not None and hasattr(v, "x") and hasattr(v, "y"):
                normalized_pts[k] = NormalizedPoint(
                    float(v.x),
                    float(v.y),
                    float(getattr(v, "z", 0)),
                    float(getattr(v, "visibility", 1.0)),
                )

    if not normalized_pts:
        return {"ok": True, "event": "SAFE", "has_pose": False}

    with _lock:
        current_roi = _roi
        if current_roi is None or not current_roi.is_valid():
            return {"ok": False, "error": "Invalid ROI"}
        detection_state = fall_detector.evaluate(normalized_pts, current_roi)
        _apply_detection_state(detection_state)
        # Drive UI from live state (publish cooldown must not freeze banner on SAFE)
        event = event_from_detection_state(detection_state)

    return {
        "ok": True,
        "event": event,
        "has_pose": True,
        "detection_state": {
            "in_safe_area": detection_state.in_safe_area,
            "near_edge": detection_state.near_edge,
            "fall_detected": detection_state.fall_detected,
            "left_bed": detection_state.left_bed,
            "torso_x": getattr(detection_state, "torso_x", None),
            "torso_y": getattr(detection_state, "torso_y", None),
            "torso_angle_deg": getattr(detection_state, "torso_angle_deg", None),
        },
    }


def draw_overlay(frame, pose_landmarks_list: Optional[Sequence[Any]] = None) -> Any:
    """Draw bed polygon + optional pose landmarks on a BGR frame."""
    if not HAVE_CV2:
        return frame
    h, w = frame.shape[:2]
    with _lock:
        if _roi and _roi.is_valid():
            pts = [(int(pt[0] * w), int(pt[1] * h)) for pt in _roi.points]
            for i in range(len(pts)):
                cv2.line(frame, pts[i], pts[(i + 1) % len(pts)], (0, 255, 0), 2)

    if pose_landmarks_list and drawing_utils is not None and PoseLandmarksConnections is not None:
        try:
            drawing_utils.draw_landmarks(
                frame,
                pose_landmarks_list,
                PoseLandmarksConnections.POSE_LANDMARKS,
            )
        except Exception as exc:
            logger.debug("draw_landmarks failed: %s", exc)
    return frame


def _build_landmarker(running_mode) -> Any:
    if not HAVE_POSE:
        return None
    from .pose_model import ensure_pose_model

    model_path = ensure_pose_model()
    options = PoseLandmarkerOptions(
        base_options=BaseOptions(model_asset_path=str(model_path)),
        running_mode=running_mode,
        num_poses=config.NUM_POSES,
        min_pose_detection_confidence=config.MIN_POSE_DETECTION_CONFIDENCE,
        min_pose_presence_confidence=config.MIN_POSE_PRESENCE_CONFIDENCE,
        min_tracking_confidence=config.MIN_POSE_TRACKING_CONFIDENCE,
    )
    return PoseLandmarker.create_from_options(options)


def get_image_landmarker():
    """PoseLandmarker in IMAGE mode (browser/frame uploads)."""
    global _landmarker_image
    if not HAVE_POSE:
        return None
    if _landmarker_image is None:
        try:
            _landmarker_image = _build_landmarker(RunningMode.IMAGE)
            logger.info("PoseLandmarker (IMAGE) ready — model=%s", config.POSE_MODEL_VARIANT)
        except Exception as exc:
            logger.error("Failed to create PoseLandmarker (IMAGE): %s", exc)
            return None
    return _landmarker_image


def get_video_landmarker():
    """PoseLandmarker in VIDEO mode (camera stream, tracking)."""
    global _landmarker_video
    if not HAVE_POSE:
        return None
    if _landmarker_video is None:
        try:
            _landmarker_video = _build_landmarker(RunningMode.VIDEO)
            logger.info("PoseLandmarker (VIDEO) ready — model=%s", config.POSE_MODEL_VARIANT)
        except Exception as exc:
            logger.error("Failed to create PoseLandmarker (VIDEO): %s", exc)
            return None
    return _landmarker_video


# Back-compat name used by older tests/docs
def get_pose_instance():
    return get_image_landmarker()


def _bgr_to_mp_image(image_bgr):
    """OpenCV BGR → MediaPipe Image (SRGB)."""
    image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
    # MediaPipe requires C-contiguous uint8
    if not image_rgb.flags["C_CONTIGUOUS"]:
        image_rgb = np.ascontiguousarray(image_rgb)
    return mp.Image(image_format=mp.ImageFormat.SRGB, data=image_rgb)


def _first_pose(result) -> Optional[Sequence[Any]]:
    if result is None:
        return None
    poses = getattr(result, "pose_landmarks", None) or []
    if not poses:
        return None
    return poses[0]


def process_client_frame(image_bytes: bytes) -> dict:
    """Process a JPEG frame from the web client (phone camera)."""
    global _latest_jpeg

    if not HAVE_CV2:
        return {
            "ok": True,
            "event": "SAFE",
            "has_pose": False,
            "note": "OpenCV not installed in this Python environment.",
        }
    if not HAVE_POSE:
        return {
            "ok": True,
            "event": "SAFE",
            "has_pose": False,
            "note": (
                "MediaPipe Tasks (PoseLandmarker) not available. "
                "Install mediapipe>=0.10.30 or POST landmarks via /api/fall/landmarks."
            ),
        }

    nparr = np.frombuffer(image_bytes, dtype=np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if image is None:
        return {"ok": False, "error": "Invalid image bytes"}

    landmarker = get_image_landmarker()
    if landmarker is None:
        return {
            "ok": True,
            "event": "SAFE",
            "has_pose": False,
            "note": "PoseLandmarker failed to initialize (check model download).",
        }

    mp_image = _bgr_to_mp_image(image)
    result = landmarker.detect(mp_image)
    pose = _first_pose(result)
    if pose is not None:
        handle_pose_landmarks(pose)

    display_frame = draw_overlay(image, pose)
    encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 80]
    success, buffer = cv2.imencode(".jpg", display_frame, encode_param)
    if success:
        with _lock:
            _latest_jpeg = buffer.tobytes()

    return {
        "ok": True,
        "event": last_published_event_type or "SAFE",
        "has_pose": pose is not None,
    }


def run() -> None:
    global _running, _latest_jpeg, _video_timestamp_ms
    _running = True

    if not HAVE_CV2:
        logger.warning("OpenCV not available — camera loop inactive.")
        return
    if not HAVE_POSE:
        logger.warning(
            "MediaPipe PoseLandmarker not available — camera loop inactive. "
            "Landmarks API still works."
        )
        return

    logger.info("Starting Camera Fall Detection Loop (Source: %s)...", config.CAMERA_SOURCE)
    cap = cv2.VideoCapture(config.CAMERA_SOURCE)

    if not cap.isOpened():
        logger.warning(
            "Could not open video device %s (expected if no physical camera on host).",
            config.CAMERA_SOURCE,
        )
        return

    landmarker = get_video_landmarker()
    if landmarker is None:
        logger.warning("PoseLandmarker (VIDEO) unavailable — camera loop inactive.")
        cap.release()
        return

    try:
        while _running and cap.isOpened():
            success, image = cap.read()
            if not success:
                time.sleep(0.1)
                continue

            mp_image = _bgr_to_mp_image(image)
            # Monotonic ms timestamps required for VIDEO mode
            _video_timestamp_ms = max(
                _video_timestamp_ms + 33,
                int(time.time() * 1000),
            )
            result = landmarker.detect_for_video(mp_image, _video_timestamp_ms)
            pose = _first_pose(result)
            if pose is not None:
                handle_pose_landmarks(pose)

            display_frame = draw_overlay(image, pose)
            encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 80]
            success, buffer = cv2.imencode(".jpg", display_frame, encode_param)
            if success:
                with _lock:
                    _latest_jpeg = buffer.tobytes()

            time.sleep(0.03)
    finally:
        cap.release()
        logger.info("Camera capture device released cleanly.")

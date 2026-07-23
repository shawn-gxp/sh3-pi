import time
import logging
import cv2
import mediapipe as mp

from typing import Optional

from . import config
from . import alert_api
from .fall_detector import FallDetector, NormalizedPoint, RectFNorm, DetectionState

logger = logging.getLogger("CameraLoop")

# MediaPipe setup
mp_pose = mp.solutions.pose

# State variables
last_published_event_type: Optional[str] = None
last_published_at: int = 0
last_toast_message: Optional[str] = None
last_toast_time: int = 0

fall_detector = FallDetector()

# Hardcoded full-frame ROI (0.0 to 1.0)
# TODO: In future, get this from an API or hub_config.json
roi_rect = RectFNorm(0.0, 0.0, 1.0, 1.0)

def show_toast(message: str):
    """Simulates Android Toast to prevent spamming."""
    global last_toast_message, last_toast_time
    current_time = int(time.time() * 1000)
    if message != last_toast_message or current_time - last_toast_time > config.LOG_COOLDOWN_MS:
        logger.info(f"TOAST: {message}")
        last_toast_message = message
        last_toast_time = current_time

def play_alarm_sound_and_vibrate():
    """Hook to play alarm sound. Can be wired to a physical buzzer/speaker later."""
    logger.warning("ALARM TRIGGERED: beep beep beep!")

def stop_alarm_sound_and_vibration():
    """Hook to stop alarm sound."""
    logger.debug("Alarm stopped.")

def publish_event_if_needed(event_type: str, severity: str, detection_state: DetectionState, confidence: Optional[float] = None):
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
        in_safe_area=detection_state.in_safe_area
    )

def handle_fall_detection(landmarks) -> None:
    global last_published_event_type

    if roi_rect is None:
        return

    if not landmarks or len(landmarks.landmark) <= 24:
        # Not enough landmarks
        return
        
    # Map MediaPipe landmarks to our NormalizedPoint dict
    # Indices correspond to mediapipe.solutions.pose.PoseLandmark enum
    # 11: LEFT_SHOULDER, 12: RIGHT_SHOULDER
    # 23: LEFT_HIP, 24: RIGHT_HIP
    # 25: LEFT_KNEE, 26: RIGHT_KNEE
    # 27: LEFT_ANKLE, 28: RIGHT_ANKLE
    
    lm = landmarks.landmark
    normalized_landmarks = {
        "LEFT_SHOULDER": NormalizedPoint(lm[11].x, lm[11].y, lm[11].z, lm[11].visibility),
        "RIGHT_SHOULDER": NormalizedPoint(lm[12].x, lm[12].y, lm[12].z, lm[12].visibility),
        "LEFT_HIP": NormalizedPoint(lm[23].x, lm[23].y, lm[23].z, lm[23].visibility),
        "RIGHT_HIP": NormalizedPoint(lm[24].x, lm[24].y, lm[24].z, lm[24].visibility),
        "LEFT_KNEE": NormalizedPoint(lm[25].x, lm[25].y, lm[25].z, lm[25].visibility),
        "RIGHT_KNEE": NormalizedPoint(lm[26].x, lm[26].y, lm[26].z, lm[26].visibility),
        "LEFT_ANKLE": NormalizedPoint(lm[27].x, lm[27].y, lm[27].z, lm[27].visibility),
        "RIGHT_ANKLE": NormalizedPoint(lm[28].x, lm[28].y, lm[28].z, lm[28].visibility),
    }
    
    detection_state = fall_detector.evaluate(normalized_landmarks, roi_rect)
    
    if detection_state.fall_detected:
        show_toast("CRITICAL: Fall detected")
        play_alarm_sound_and_vibrate()
        publish_event_if_needed("FALL_DETECTED", "critical", detection_state, confidence=0.95)
    elif detection_state.near_edge:
        show_toast("Warning: Patient near bed edge")
        play_alarm_sound_and_vibrate()
        publish_event_if_needed("PATIENT_NEAR_EDGE", "high", detection_state)
    elif detection_state.in_safe_area:
        show_toast("Patient is inside the safe area.")
        stop_alarm_sound_and_vibration()
        last_published_event_type = None
    else:
        show_toast("ALERT: Patient has left the safe area!")
        play_alarm_sound_and_vibrate()
        publish_event_if_needed("PATIENT_LEFT_BED", "high", detection_state)

def run():
    logger.info("Starting Camera Fall Detection Loop...")
    
    cap = cv2.VideoCapture(0)
    
    if not cap.isOpened():
        logger.error("Could not open video device /dev/video0")
        return
        
    with mp_pose.Pose(
        min_detection_confidence=config.MIN_POSE_DETECTION_CONFIDENCE,
        min_tracking_confidence=config.MIN_POSE_TRACKING_CONFIDENCE,
        min_presence_confidence=config.MIN_POSE_PRESENCE_CONFIDENCE
    ) as pose:
        while cap.isOpened():
            success, image = cap.read()
            if not success:
                logger.warning("Ignoring empty camera frame.")
                time.sleep(0.1)
                continue

            # To improve performance, optionally mark the image as not writeable to pass by reference.
            image.flags.writeable = False
            image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            results = pose.process(image)

            # Fall detection logic
            if results.pose_landmarks:
                handle_fall_detection(results.pose_landmarks)
                
            # Sleep briefly to not peg CPU at 100% (simulate ~15-30fps)
            time.sleep(0.03)

    cap.release()

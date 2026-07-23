"""End-to-end landmarks simulation (no camera / no FastAPI)."""
from __future__ import annotations

import time

from fall_detection_pi import camera_loop, config
from fall_detection_pi.fall_detector import FallDetector


def lm(sx, hy, xo, horiz=False):
    if horiz:
        return {
            "LEFT_SHOULDER": {"x": 0.1, "y": sx, "visibility": 1},
            "RIGHT_SHOULDER": {"x": 0.1, "y": sx + 0.02, "visibility": 1},
            "LEFT_HIP": {"x": 0.5, "y": hy, "visibility": 1},
            "RIGHT_HIP": {"x": 0.5, "y": hy + 0.02, "visibility": 1},
            "LEFT_KNEE": {"x": 0.7, "y": hy + 0.05, "visibility": 1},
            "RIGHT_KNEE": {"x": 0.7, "y": hy + 0.07, "visibility": 1},
            "LEFT_ANKLE": {"x": 0.85, "y": hy + 0.1, "visibility": 1},
            "RIGHT_ANKLE": {"x": 0.85, "y": hy + 0.12, "visibility": 1},
        }
    return {
        "LEFT_SHOULDER": {"x": xo - 0.1, "y": sx, "visibility": 1},
        "RIGHT_SHOULDER": {"x": xo + 0.1, "y": sx, "visibility": 1},
        "LEFT_HIP": {"x": xo - 0.1, "y": hy, "visibility": 1},
        "RIGHT_HIP": {"x": xo + 0.1, "y": hy, "visibility": 1},
        "LEFT_KNEE": {"x": xo - 0.1, "y": hy + 0.15, "visibility": 1},
        "RIGHT_KNEE": {"x": xo + 0.1, "y": hy + 0.15, "visibility": 1},
        "LEFT_ANKLE": {"x": xo - 0.1, "y": hy + 0.3, "visibility": 1},
        "RIGHT_ANKLE": {"x": xo + 0.1, "y": hy + 0.3, "visibility": 1},
    }


def main():
    print("=== Fall Detection Try-Out ===")
    print(f"ROI from hub: {config.DEFAULT_POLYGON}")
    print(f"PATIENT_ID: {config.PATIENT_ID!r} (alerts skip if REPLACE_WITH_PATIENT_ID)")
    print(f"BACKEND: {config.BACKEND_BASE_URL}")
    print(f"OpenCV available: {camera_loop.HAVE_CV2}")
    print(f"MediaPipe PoseLandmarker available: {camera_loop.HAVE_POSE}")

    camera_loop.fall_detector = FallDetector()
    camera_loop.update_polygon([(0.2, 0.2), (0.8, 0.2), (0.8, 0.6), (0.2, 0.6)])
    camera_loop.last_published_event_type = None

    print("\n-- Scenario: IN BED --")
    for i in range(4):
        out = camera_loop.process_normalized_landmarks(lm(0.3, 0.45, 0.5))
        print(f"  frame {i+1}: event={out['event']} state={out['detection_state']}")

    print("\n-- Scenario: NEAR EDGE --")
    camera_loop.fall_detector = FallDetector()
    camera_loop.last_published_event_type = None
    for i in range(6):
        out = camera_loop.process_normalized_landmarks(lm(0.3, 0.45, 0.17))
        print(f"  frame {i+1}: event={out['event']} state={out['detection_state']}")

    print("\n-- Scenario: LEFT BED --")
    camera_loop.fall_detector = FallDetector()
    camera_loop.last_published_event_type = None
    for i in range(6):
        out = camera_loop.process_normalized_landmarks(lm(0.25, 0.45, 0.05))
        print(f"  frame {i+1}: event={out['event']} state={out['detection_state']}")

    print("\n-- Scenario: FALL (rapid drop) --")
    camera_loop.fall_detector = FallDetector()
    camera_loop.last_published_event_type = None
    for i in range(3):
        out = camera_loop.process_normalized_landmarks(lm(0.25, 0.45, 0.5))
        time.sleep(0.02)
        print(f"  seed {i+1}: event={out['event']} state={out['detection_state']}")
    fallen = {
        "LEFT_SHOULDER": {"x": 0.15, "y": 0.82, "visibility": 1},
        "RIGHT_SHOULDER": {"x": 0.15, "y": 0.84, "visibility": 1},
        "LEFT_HIP": {"x": 0.55, "y": 0.83, "visibility": 1},
        "RIGHT_HIP": {"x": 0.55, "y": 0.85, "visibility": 1},
        "LEFT_KNEE": {"x": 0.75, "y": 0.86, "visibility": 1},
        "RIGHT_KNEE": {"x": 0.75, "y": 0.88, "visibility": 1},
        "LEFT_ANKLE": {"x": 0.9, "y": 0.9, "visibility": 1},
        "RIGHT_ANKLE": {"x": 0.9, "y": 0.92, "visibility": 1},
    }
    for i in range(6):
        out = camera_loop.process_normalized_landmarks(fallen)
        time.sleep(0.02)
        print(f"  fall {i+1}: event={out['event']} state={out['detection_state']}")

    print("\n-- Scenario: SUSTAINED FALL (already on floor, no drop) --")
    camera_loop.fall_detector = FallDetector()
    camera_loop.last_published_event_type = None
    for i in range(16):
        out = camera_loop.process_normalized_landmarks(fallen)
        if i in (0, 4, 9, 14, 15):
            print(f"  frame {i+1}: event={out['event']} state={out['detection_state']}")

    print("\n=== Done ===")


if __name__ == "__main__":
    main()

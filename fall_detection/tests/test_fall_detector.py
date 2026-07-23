"""Unit tests for FallDetector + PolygonROI (no camera)."""
from __future__ import annotations

import time

import pytest

from fall_detection.fall_detector import FallDetector, NormalizedPoint, PolygonROI


def create_mock_landmarks(
    shoulder_y=0.2,
    hip_y=0.4,
    knee_y=0.6,
    ankle_y=0.8,
    x_offset=0.5,
    visibility=1.0,
    horizontal=False,
):
    if horizontal:
        return {
            "LEFT_SHOULDER": NormalizedPoint(0.2, shoulder_y, visibility=visibility),
            "RIGHT_SHOULDER": NormalizedPoint(0.2, shoulder_y + 0.02, visibility=visibility),
            "LEFT_HIP": NormalizedPoint(0.8, hip_y, visibility=visibility),
            "RIGHT_HIP": NormalizedPoint(0.8, hip_y + 0.02, visibility=visibility),
            "LEFT_KNEE": NormalizedPoint(0.9, knee_y, visibility=visibility),
            "RIGHT_KNEE": NormalizedPoint(0.9, knee_y + 0.02, visibility=visibility),
            "LEFT_ANKLE": NormalizedPoint(0.95, ankle_y, visibility=visibility),
            "RIGHT_ANKLE": NormalizedPoint(0.95, ankle_y + 0.02, visibility=visibility),
        }
    return {
        "LEFT_SHOULDER": NormalizedPoint(x_offset - 0.1, shoulder_y, visibility=visibility),
        "RIGHT_SHOULDER": NormalizedPoint(x_offset + 0.1, shoulder_y, visibility=visibility),
        "LEFT_HIP": NormalizedPoint(x_offset - 0.1, hip_y, visibility=visibility),
        "RIGHT_HIP": NormalizedPoint(x_offset + 0.1, hip_y, visibility=visibility),
        "LEFT_KNEE": NormalizedPoint(x_offset - 0.1, knee_y, visibility=visibility),
        "RIGHT_KNEE": NormalizedPoint(x_offset + 0.1, knee_y, visibility=visibility),
        "LEFT_ANKLE": NormalizedPoint(x_offset - 0.1, ankle_y, visibility=visibility),
        "RIGHT_ANKLE": NormalizedPoint(x_offset + 0.1, ankle_y, visibility=visibility),
    }


def bed_roi():
    return PolygonROI([(0.2, 0.2), (0.8, 0.2), (0.8, 0.6), (0.2, 0.6)])


def _fallen_horizontal_outside():
    return {
        "LEFT_SHOULDER": NormalizedPoint(0.15, 0.82, visibility=1.0),
        "RIGHT_SHOULDER": NormalizedPoint(0.15, 0.84, visibility=1.0),
        "LEFT_HIP": NormalizedPoint(0.55, 0.83, visibility=1.0),
        "RIGHT_HIP": NormalizedPoint(0.55, 0.85, visibility=1.0),
        "LEFT_KNEE": NormalizedPoint(0.75, 0.86, visibility=1.0),
        "RIGHT_KNEE": NormalizedPoint(0.75, 0.88, visibility=1.0),
        "LEFT_ANKLE": NormalizedPoint(0.9, 0.9, visibility=1.0),
        "RIGHT_ANKLE": NormalizedPoint(0.9, 0.92, visibility=1.0),
    }


def test_polygon_contains():
    roi = bed_roi()
    assert roi.contains(0.5, 0.4) is True
    assert roi.contains(0.05, 0.05) is False


def test_polygon_distance_signed():
    roi = bed_roi()
    assert roi.distance(0.5, 0.4) > 0
    d_near = roi.distance(0.17, 0.4)
    assert d_near < 0
    assert d_near > -0.05
    d_far = roi.distance(0.01, 0.4)
    assert d_far < d_near


def test_patient_inside_instant():
    """Inside is immediate (old-style), no multi-frame lag."""
    detector = FallDetector()
    roi = bed_roi()
    landmarks = create_mock_landmarks(shoulder_y=0.3, hip_y=0.45, x_offset=0.5)
    state = detector.evaluate(landmarks, roi)
    assert state.in_safe_area is True
    assert state.fall_detected is False
    assert state.left_bed is False


def test_left_bed_instant():
    """Upright outside ROI → left_bed immediately (old camera_loop else branch)."""
    detector = FallDetector()
    roi = bed_roi()
    outside = create_mock_landmarks(shoulder_y=0.25, hip_y=0.45, x_offset=0.05)
    state = detector.evaluate(outside, roi)
    assert state.left_bed is True
    assert state.in_safe_area is False
    assert state.fall_detected is False
    assert state.near_edge is False


def test_near_edge_instant():
    detector = FallDetector(edge_tolerance=0.05)
    roi = bed_roi()
    near = create_mock_landmarks(shoulder_y=0.3, hip_y=0.45, x_offset=0.17)
    state = detector.evaluate(near, roi)
    assert state.near_edge is True
    assert state.fall_detected is False
    assert state.left_bed is False


def test_fall_classic_below_bed():
    """Classic: outside + horizontal + y below bed bottom."""
    detector = FallDetector(fall_confirm_frames=3)
    roi = bed_roi()  # bottom = 0.6
    fallen = _fallen_horizontal_outside()  # y ~ 0.83 > 0.6
    state = None
    for _ in range(3):
        state = detector.evaluate(fallen, roi)
    assert state is not None
    assert state.fall_detected is True
    assert state.left_bed is False


def test_fall_with_rapid_drop():
    detector = FallDetector(fall_confirm_frames=3)
    roi = bed_roi()
    upright = create_mock_landmarks(shoulder_y=0.25, hip_y=0.45, x_offset=0.5)
    for _ in range(3):
        detector.evaluate(upright, roi)
        time.sleep(0.02)
    fallen = _fallen_horizontal_outside()
    state = None
    for _ in range(3):
        state = detector.evaluate(fallen, roi)
        time.sleep(0.02)
    assert state.fall_detected is True


def test_low_visibility_core_only():
    """Low knee/ankle vis should NOT block if shoulders/hips are visible."""
    detector = FallDetector(min_visibility=0.5)
    roi = bed_roi()
    landmarks = create_mock_landmarks(shoulder_y=0.3, hip_y=0.45, x_offset=0.5, visibility=1.0)
    landmarks["LEFT_KNEE"] = NormalizedPoint(0.4, 0.6, visibility=0.1)
    landmarks["RIGHT_KNEE"] = NormalizedPoint(0.6, 0.6, visibility=0.1)
    landmarks["LEFT_ANKLE"] = NormalizedPoint(0.4, 0.8, visibility=0.1)
    landmarks["RIGHT_ANKLE"] = NormalizedPoint(0.6, 0.8, visibility=0.1)
    state = detector.evaluate(landmarks, roi)
    assert state.in_safe_area is True


def test_low_visibility_shoulders_blocks():
    detector = FallDetector(min_visibility=0.5)
    roi = bed_roi()
    landmarks = create_mock_landmarks(visibility=0.3)
    state = detector.evaluate(landmarks, roi)
    assert state.in_safe_area is False
    assert state.fall_detected is False


def test_no_roi():
    detector = FallDetector()
    landmarks = create_mock_landmarks()
    state = detector.evaluate(landmarks, None)
    assert state.in_safe_area is False
    assert state.fall_detected is False


def test_process_landmarks_api_path():
    from fall_detection import camera_loop

    camera_loop.fall_detector = FallDetector()
    camera_loop.update_polygon([(0.2, 0.2), (0.8, 0.2), (0.8, 0.6), (0.2, 0.6)])

    upright = create_mock_landmarks(shoulder_y=0.3, hip_y=0.45, x_offset=0.5)
    out = camera_loop.process_normalized_landmarks(upright)
    assert out["ok"] is True
    assert out["event"] == "SAFE"
    assert out["detection_state"]["in_safe_area"] is True

    outside = create_mock_landmarks(shoulder_y=0.25, hip_y=0.45, x_offset=0.05)
    out2 = camera_loop.process_normalized_landmarks(outside)
    assert out2["event"] == "PATIENT_LEFT_BED"
    assert out2["detection_state"]["left_bed"] is True


def test_process_landmarks_json_dict_path():
    from fall_detection import camera_loop

    camera_loop.fall_detector = FallDetector()
    camera_loop.update_polygon([(0.2, 0.2), (0.8, 0.2), (0.8, 0.6), (0.2, 0.6)])

    upright = {
        k: {"x": v.x, "y": v.y, "z": v.z, "visibility": v.visibility}
        for k, v in create_mock_landmarks(shoulder_y=0.3, hip_y=0.45, x_offset=0.5).items()
    }
    out = camera_loop.process_normalized_landmarks(upright)
    assert out["ok"] is True
    assert out["detection_state"]["in_safe_area"] is True


def test_config_loads_hub_polygon():
    from fall_detection import config

    assert len(config.DEFAULT_POLYGON) >= 3
    assert config.hub_config_path().name == "hub_config.json"
    assert config.hub_config_path().is_file()


def test_vision_stack_flags():
    from fall_detection import camera_loop

    assert isinstance(camera_loop.HAVE_CV2, bool)
    assert isinstance(camera_loop.HAVE_POSE, bool)
    if camera_loop.HAVE_POSE:
        assert camera_loop.PoseLandmarker is not None


def test_pose_landmarks_to_dict_indices():
    from fall_detection.camera_loop import pose_landmarks_to_dict

    class LM:
        def __init__(self, i):
            self.x = i * 0.01
            self.y = 0.5
            self.z = 0.0
            self.visibility = 1.0

    lms = [LM(i) for i in range(33)]
    d = pose_landmarks_to_dict(lms)
    assert d["LEFT_SHOULDER"].x == pytest.approx(0.11)

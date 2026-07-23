import pytest
from fall_detection.fall_detector import FallDetector, NormalizedPoint, RectFNorm

def create_mock_landmarks(shoulder_y=0.2, hip_y=0.4, knee_y=0.6, ankle_y=0.8, x_offset=0.5, visibility=1.0):
    return {
        "LEFT_SHOULDER": NormalizedPoint(x_offset - 0.1, shoulder_y, visibility=visibility),
        "RIGHT_SHOULDER": NormalizedPoint(x_offset + 0.1, shoulder_y, visibility=visibility),
        "LEFT_HIP": NormalizedPoint(x_offset - 0.1, hip_y, visibility=visibility),
        "RIGHT_HIP": NormalizedPoint(x_offset + 0.1, hip_y, visibility=visibility),
        "LEFT_KNEE": NormalizedPoint(x_offset - 0.1, knee_y, visibility=visibility),
        "RIGHT_KNEE": NormalizedPoint(x_offset + 0.1, knee_y, visibility=visibility),
        "LEFT_ANKLE": NormalizedPoint(x_offset - 0.1, ankle_y, visibility=visibility),
        "RIGHT_ANKLE": NormalizedPoint(x_offset + 0.1, ankle_y, visibility=visibility)
    }

def test_patient_inside_roi():
    detector = FallDetector()
    roi = RectFNorm(0.1, 0.1, 0.9, 0.9)
    # Torso will be vertically between 0.2 and 0.4, centered at X=0.5. Inside ROI.
    landmarks = create_mock_landmarks(shoulder_y=0.2, hip_y=0.4, x_offset=0.5)
    
    state = detector.evaluate(landmarks, roi)
    assert state.in_safe_area is True
    assert state.near_edge is False
    assert state.fall_detected is False

def test_fall_detected():
    detector = FallDetector()
    roi = RectFNorm(0.1, 0.1, 0.9, 0.5)
    
    # Body is horizontal (shoulders at X=0.2, hips at X=0.8). Y is below the bed (Y > 0.5)
    landmarks = {
        "LEFT_SHOULDER": NormalizedPoint(0.2, 0.8),
        "RIGHT_SHOULDER": NormalizedPoint(0.2, 0.8),
        "LEFT_HIP": NormalizedPoint(0.8, 0.81),
        "RIGHT_HIP": NormalizedPoint(0.8, 0.81),
        "LEFT_KNEE": NormalizedPoint(0.9, 0.82),
        "RIGHT_KNEE": NormalizedPoint(0.9, 0.82)
    }
    
    state = detector.evaluate(landmarks, roi)
    assert state.in_safe_area is False
    assert state.near_edge is False
    assert state.fall_detected is True

def test_near_edge():
    detector = FallDetector(edge_tolerance=0.05)
    roi = RectFNorm(0.1, 0.1, 0.9, 0.9)
    
    # Place torso center just outside left edge (0.1). Center needs to be around 0.08
    # If shoulders/hips at X=0.08, they are outside ROI but within edge tolerance (0.1 - 0.05 = 0.05)
    landmarks = create_mock_landmarks(x_offset=0.08)
    
    state = detector.evaluate(landmarks, roi)
    assert state.in_safe_area is False
    assert state.near_edge is True
    assert state.fall_detected is False

def test_low_visibility():
    detector = FallDetector(min_visibility=0.5)
    roi = RectFNorm(0.1, 0.1, 0.9, 0.9)
    
    landmarks = create_mock_landmarks(visibility=0.3)
    
    state = detector.evaluate(landmarks, roi)
    assert state.in_safe_area is False
    assert state.near_edge is False
    assert state.fall_detected is False

def test_no_roi():
    detector = FallDetector()
    landmarks = create_mock_landmarks()
    
    state = detector.evaluate(landmarks, None)
    assert state.in_safe_area is False
    assert state.near_edge is False
    assert state.fall_detected is False

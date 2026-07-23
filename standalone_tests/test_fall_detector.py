import time
import math
import sys
from unittest.mock import MagicMock

# Mock cv2 if not installed locally
try:
    import cv2
except ImportError:
    class MockCV2:
        @staticmethod
        def pointPolygonTest(contour, pt, measureDist):
            # ray casting algorithm for polygon point test
            x, y = pt
            n = len(contour)
            inside = False
            p1x, p1y = contour[0]
            for i in range(n + 1):
                p2x, p2y = contour[i % n]
                if y > min(p1y, p2y):
                    if y <= max(p1y, p2y):
                        if x <= max(p1x, p2x):
                            if p1y != p2y:
                                xinters = (y - p1y) * (p2x - p1x) / (p2y - p1y) + p1x
                            if p1x == p2x or x <= xinters:
                                inside = not inside
                p1x, p1y = p2x, p2y
            
            if not measureDist:
                return 1.0 if inside else -1.0
            
            # Simple distance approximation for tests
            if inside:
                # distance to closest edge
                min_d = 999.0
                for i in range(n):
                    p1 = contour[i]
                    p2 = contour[(i+1)%n]
                    # dist to segment
                    dx = p2[0] - p1[0]
                    dy = p2[1] - p1[1]
                    if dx == 0 and dy == 0:
                        d = math.hypot(x - p1[0], y - p1[1])
                    else:
                        t = max(0, min(1, ((x - p1[0]) * dx + (y - p1[1]) * dy) / (dx*dx + dy*dy)))
                        proj_x = p1[0] + t * dx
                        proj_y = p1[1] + t * dy
                        d = math.hypot(x - proj_x, y - proj_y)
                    min_d = min(min_d, d)
                return min_d
            else:
                min_d = 999.0
                for i in range(n):
                    p1 = contour[i]
                    p2 = contour[(i+1)%n]
                    dx = p2[0] - p1[0]
                    dy = p2[1] - p1[1]
                    if dx == 0 and dy == 0:
                        d = math.hypot(x - p1[0], y - p1[1])
                    else:
                        t = max(0, min(1, ((x - p1[0]) * dx + (y - p1[1]) * dy) / (dx*dx + dy*dy)))
                        proj_x = p1[0] + t * dx
                        proj_y = p1[1] + t * dy
                        d = math.hypot(x - proj_x, y - proj_y)
                    min_d = min(min_d, d)
                return -min_d

    sys.modules['cv2'] = MockCV2()

try:
    import numpy as np
except ImportError:
    class MockNumPy:
        float32 = float
        @staticmethod
        def array(obj, dtype=None):
            return obj
        @staticmethod
        def isscalar(obj):
            return isinstance(obj, (int, float))
    sys.modules['numpy'] = MockNumPy()

import pytest
from fall_detection_pi.fall_detector import FallDetector, NormalizedPoint, PolygonROI

def create_mock_landmarks(x_offset: float = 0.1, y_offset: float = 0.5, is_lying: bool = True):
    """
    Creates mock landmarks.
    If is_lying is True: body is horizontal (width > height).
    If is_lying is False: body is vertical (height > width).
    """
    if is_lying:
        # Horizontal body: width=0.3, height=0.08 -> aspect ratio < 0.6
        hw, hh = 0.15, 0.04
    else:
        # Vertical standing body: width=0.08, height=0.3 -> aspect ratio > 1.0
        hw, hh = 0.04, 0.15

    # Shoulders and Hips
    lsx, lsy = x_offset - hw, y_offset - hh
    rsx, rsy = x_offset + hw, y_offset - hh
    lhx, lhy = x_offset - hw, y_offset + hh
    rhx, rhy = x_offset + hw, y_offset + hh

    return {
        "LEFT_SHOULDER": NormalizedPoint(lsx, lsy),
        "RIGHT_SHOULDER": NormalizedPoint(rsx, rsy),
        "LEFT_HIP": NormalizedPoint(lhx, lhy),
        "RIGHT_HIP": NormalizedPoint(rhx, rhy),
        "LEFT_KNEE": NormalizedPoint(lhx, lhy + 0.02),
        "RIGHT_KNEE": NormalizedPoint(rhx, rhy + 0.02),
        "LEFT_ANKLE": NormalizedPoint(lhx, lhy + 0.04),
        "RIGHT_ANKLE": NormalizedPoint(rhx, rhy + 0.04),
    }

def test_polygon_roi():
    roi = PolygonROI([(0.2, 0.2), (0.8, 0.2), (0.8, 0.8), (0.2, 0.8)])
    
    # Inside
    assert roi.contains(0.5, 0.5)
    assert roi.distance(0.5, 0.5) > 0
    
    # Outside
    assert not roi.contains(0.1, 0.1)
    assert roi.distance(0.1, 0.1) < 0
    
    # Near edge (within tolerance)
    # Edge is x=0.2. Point is x=0.18, so distance is -0.02.
    dist = roi.distance(0.18, 0.5)
    assert abs(dist - (-0.02)) < 1e-5

def test_fall_detector_temporal_voting():
    detector = FallDetector(edge_tolerance=0.05, history_maxlen=25)
    roi = PolygonROI([(0.2, 0.2), (0.8, 0.2), (0.8, 0.8), (0.2, 0.8)])
    
    # 1. Start inside, standing (is_lying=False). Should be SAFE.
    lm = create_mock_landmarks(x_offset=0.5, y_offset=0.5, is_lying=False)
    for _ in range(3):
        state = detector.evaluate(lm, roi)
    assert state.in_safe_area
    assert not state.fall_detected
    
    # 2. Rapid drop + horizontal (is_lying=True) + outside bed (x_offset=0.1 is outside x=0.2 boundary)
    # Frame 1: High up outside bed (y=0.3)
    lm_high = create_mock_landmarks(x_offset=0.1, y_offset=0.3, is_lying=True)
    detector.evaluate(lm_high, roi)
    
    # Frame 2: Rapid drop to floor outside bed (y=0.9, velocity drop = 0.6)
    lm_low = create_mock_landmarks(x_offset=0.1, y_offset=0.9, is_lying=True)
    
    # Needs 5 consecutive frames to confirm fall
    for i in range(4):
        state = detector.evaluate(lm_low, roi)
        assert not state.fall_detected, f"Should not fall on frame {i+1}"
        
    state = detector.evaluate(lm_low, roi)
    assert state.fall_detected, "Should fall on 5th consecutive frame"
    
def test_slow_bend_does_not_trigger_fall():
    detector = FallDetector(edge_tolerance=0.05, history_maxlen=25)
    roi = PolygonROI([(0.2, 0.2), (0.8, 0.2), (0.8, 0.8), (0.2, 0.8)])
    
    # Outside bed, horizontal (is_lying=True)
    lm = create_mock_landmarks(x_offset=0.1, y_offset=0.9, is_lying=True)
    
    # If the user has been bending over for a long time without a rapid drop...
    for _ in range(10):
        # We manually update timestamp to simulate slow movement over 2 seconds
        state = detector.evaluate(lm, roi)
        detector.history[-1].timestamp = time.time() - 2.0 
        
    # Re-evaluate with current time (velocity will be 0 since y hasn't changed)
    state = detector.evaluate(lm, roi)
    
    # They are outside and horizontal, but didn't drop rapidly.
    # Therefore, no fall detected.
    assert not state.fall_detected

"""
Fall / bed-edge rules engine.

Aligned with the original Android/port logic (instant inside / near / left),
with optional light temporal smoothing only on fall so pose noise is less
likely to spam FALL_DETECTED.

Fall if outside bed ROI and:
  - classic: horizontal torso AND below the bed polygon bottom, OR
  - rapid drop (Δy), OR
  - sustained down-posture outside (lying / horizontal for N frames)
"""
from __future__ import annotations

import math
import threading
import time
from collections import deque
from dataclasses import dataclass
from typing import Deque, Dict, List, Optional, Tuple

try:
    import cv2
    import numpy as np

    HAVE_CV2 = True
except ImportError:
    HAVE_CV2 = False
    cv2 = None
    np = None


@dataclass
class NormalizedPoint:
    x: float
    y: float
    z: float = 0.0
    visibility: float = 1.0


@dataclass
class FeatureVector:
    torso_center_x: float
    torso_center_y: float
    torso_angle_deg: float
    bbox_aspect_ratio: float
    timestamp: float


@dataclass
class DetectionState:
    in_safe_area: bool
    near_edge: bool
    fall_detected: bool
    left_bed: bool = False
    # Debug / UI helpers (optional consumers)
    torso_x: float = 0.0
    torso_y: float = 0.0
    torso_angle_deg: float = 0.0


class PolygonROI:
    def __init__(self, points: List[Tuple[float, float]]):
        self.points = points
        if HAVE_CV2 and np is not None:
            self.contour = np.array(points, dtype=np.float32)
        else:
            self.contour = points

    def is_valid(self) -> bool:
        return len(self.points) >= 3

    def bounds(self) -> Tuple[float, float, float, float]:
        """Axis-aligned bounds (left, top, right, bottom) of the polygon."""
        xs = [p[0] for p in self.points]
        ys = [p[1] for p in self.points]
        return min(xs), min(ys), max(xs), max(ys)

    def contains(self, x: float, y: float) -> bool:
        if HAVE_CV2 and cv2 is not None and isinstance(self.contour, np.ndarray):
            return cv2.pointPolygonTest(self.contour, (x, y), measureDist=False) >= 0

        n = len(self.points)
        inside = False
        p1x, p1y = self.points[0]
        for i in range(n + 1):
            p2x, p2y = self.points[i % n]
            if y > min(p1y, p2y):
                if y <= max(p1y, p2y):
                    if x <= max(p1x, p2x):
                        if p1y != p2y:
                            xinters = (y - p1y) * (p2x - p1x) / (p2y - p1y) + p1x
                        if p1x == p2x or x <= xinters:
                            inside = not inside
            p1x, p1y = p2x, p2y
        return inside

    @staticmethod
    def _point_to_segment_dist(
        px: float, py: float, x1: float, y1: float, x2: float, y2: float
    ) -> float:
        dx, dy = x2 - x1, y2 - y1
        if dx == 0 and dy == 0:
            return math.hypot(px - x1, py - y1)
        t = max(0.0, min(1.0, ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)))
        return math.hypot(px - (x1 + t * dx), py - (y1 + t * dy))

    def distance(self, x: float, y: float) -> float:
        """Signed distance: positive inside, negative outside (OpenCV convention)."""
        if HAVE_CV2 and cv2 is not None and isinstance(self.contour, np.ndarray):
            return cv2.pointPolygonTest(self.contour, (x, y), measureDist=True)
        n = len(self.points)
        if n < 2:
            return 0.0
        min_d = min(
            self._point_to_segment_dist(x, y, *self.points[i], *self.points[(i + 1) % n])
            for i in range(n)
        )
        return min_d if self.contains(x, y) else -min_d


class FallDetector:
    def __init__(
        self,
        edge_tolerance: float = 0.05,
        min_visibility: float = 0.5,
        history_maxlen: int = 25,
        rapid_drop_threshold: float = 0.08,
        rapid_drop_window_s: float = 1.0,
        # Light temporal only for fall (old path was 1-frame; 3 reduces flicker)
        fall_confirm_frames: int = 3,
        sustained_fall_frames: int = 10,
        horizontal_angle_deg: float = 25.0,
        lying_aspect_ratio: float = 0.6,
    ):
        self.edge_tolerance = edge_tolerance
        self.min_visibility = min_visibility
        self.history: Deque[FeatureVector] = deque(maxlen=history_maxlen)
        self.rapid_drop_threshold = rapid_drop_threshold
        self.rapid_drop_window_s = rapid_drop_window_s
        self.fall_confirm_frames = fall_confirm_frames
        self.sustained_fall_frames = sustained_fall_frames
        self.horizontal_angle_deg = horizontal_angle_deg
        self.lying_aspect_ratio = lying_aspect_ratio

        self.consecutive_fall_frames = 0
        self.consecutive_down_outside_frames = 0
        self._lock = threading.RLock()

    def reset(self) -> None:
        with self._lock:
            self.history.clear()
            self.consecutive_fall_frames = 0
            self.consecutive_down_outside_frames = 0

    def evaluate(
        self, landmarks: Dict[str, NormalizedPoint], roi: Optional[PolygonROI]
    ) -> DetectionState:
        with self._lock:
            return self._evaluate_unlocked(landmarks, roi)

    def _evaluate_unlocked(
        self, landmarks: Dict[str, NormalizedPoint], roi: Optional[PolygonROI]
    ) -> DetectionState:
        empty = DetectionState(
            in_safe_area=False, near_edge=False, fall_detected=False, left_bed=False
        )
        if roi is None or not roi.is_valid():
            return empty

        ls = landmarks.get("LEFT_SHOULDER")
        rs = landmarks.get("RIGHT_SHOULDER")
        lh = landmarks.get("LEFT_HIP")
        rh = landmarks.get("RIGHT_HIP")

        if ls is None or rs is None or lh is None or rh is None:
            return empty

        # Core torso only — knees/ankles are often low-vis on phone cams and
        # used to zero the whole frame (looked like "stuck on SAFE").
        core = [ls, rs, lh, rh]
        if any(pt.visibility < self.min_visibility for pt in core):
            return empty

        lk = landmarks.get("LEFT_KNEE") or landmarks.get("LEFT_ANKLE") or lh
        rk = landmarks.get("RIGHT_KNEE") or landmarks.get("RIGHT_ANKLE") or rh
        pts = [ls, rs, lh, rh, lk, rk]

        torso_center_x = (ls.x + rs.x + lh.x + rh.x) / 4.0
        torso_center_y = (ls.y + rs.y + lh.y + rh.y) / 4.0

        mid_sx = (ls.x + rs.x) / 2.0
        mid_sy = (ls.y + rs.y) / 2.0
        mid_hx = (lh.x + rh.x) / 2.0
        mid_hy = (lh.y + rh.y) / 2.0
        angle_rad = math.atan2((mid_hy - mid_sy), (mid_hx - mid_sx))
        angle_deg = abs(angle_rad * 180.0 / math.pi)

        all_xs = [pt.x for pt in pts]
        all_ys = [pt.y for pt in pts]
        width = max(all_xs) - min(all_xs)
        height = max(all_ys) - min(all_ys)
        bbox_aspect_ratio = height / width if width > 0.01 else 2.0

        feature = FeatureVector(
            torso_center_x=torso_center_x,
            torso_center_y=torso_center_y,
            torso_angle_deg=angle_deg,
            bbox_aspect_ratio=bbox_aspect_ratio,
            timestamp=time.time(),
        )
        self.history.append(feature)

        # --- Instant geometry (same spirit as original RectFNorm port) ---
        inside = roi.contains(torso_center_x, torso_center_y)
        dist = roi.distance(torso_center_x, torso_center_y)
        near = (not inside) and (dist > -self.edge_tolerance)

        _left, _top, _right, bed_bottom = roi.bounds()
        # Classic "below bed" — torso lower in image than bed bottom edge
        below_bed = torso_center_y > bed_bottom

        is_horizontal = angle_deg < self.horizontal_angle_deg
        is_lying = bbox_aspect_ratio < self.lying_aspect_ratio
        is_posture_down = is_horizontal or is_lying

        is_rapid_drop = False
        now = time.time()
        recent = [f for f in self.history if (now - f.timestamp) < self.rapid_drop_window_s]
        if len(recent) >= 2:
            velocity_y = recent[-1].torso_center_y - recent[0].torso_center_y
            is_rapid_drop = velocity_y > self.rapid_drop_threshold

        # Fall raw (OR of classic + velocity + will use sustained counter)
        classic_fall = (not inside) and is_horizontal and below_bed
        velocity_fall = (not inside) and is_posture_down and is_rapid_drop
        fall_raw = classic_fall or velocity_fall

        if (not inside) and is_posture_down:
            self.consecutive_down_outside_frames += 1
        else:
            self.consecutive_down_outside_frames = 0

        sustained_fall = self.consecutive_down_outside_frames >= self.sustained_fall_frames

        if fall_raw or sustained_fall:
            self.consecutive_fall_frames += 1
        else:
            self.consecutive_fall_frames = 0

        fall_detected = self.consecutive_fall_frames >= self.fall_confirm_frames

        # Instant region state like the old detector (no multi-frame lag on leave/near)
        if fall_detected:
            return DetectionState(
                in_safe_area=False,
                near_edge=False,
                fall_detected=True,
                left_bed=False,
                torso_x=torso_center_x,
                torso_y=torso_center_y,
                torso_angle_deg=angle_deg,
            )

        if inside:
            return DetectionState(
                in_safe_area=True,
                near_edge=False,
                fall_detected=False,
                left_bed=False,
                torso_x=torso_center_x,
                torso_y=torso_center_y,
                torso_angle_deg=angle_deg,
            )

        if near:
            return DetectionState(
                in_safe_area=False,
                near_edge=True,
                fall_detected=False,
                left_bed=False,
                torso_x=torso_center_x,
                torso_y=torso_center_y,
                torso_angle_deg=angle_deg,
            )

        # Outside, not near, not (yet) fall → left bed (instant, as old camera_loop else)
        return DetectionState(
            in_safe_area=False,
            near_edge=False,
            fall_detected=False,
            left_bed=True,
            torso_x=torso_center_x,
            torso_y=torso_center_y,
            torso_angle_deg=angle_deg,
        )

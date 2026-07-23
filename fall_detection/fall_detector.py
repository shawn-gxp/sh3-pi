import math
from dataclasses import dataclass
from typing import Dict, Optional

@dataclass
class NormalizedPoint:
    x: float
    y: float
    z: float = 0.0
    visibility: float = 1.0

@dataclass
class DetectionState:
    in_safe_area: bool
    near_edge: bool
    fall_detected: bool

@dataclass
class RectFNorm:
    left: float
    top: float
    right: float
    bottom: float

    def width(self) -> float:
        return self.right - self.left

    def height(self) -> float:
        return self.bottom - self.top

    def is_valid(self) -> bool:
        return self.width() > 0.0 and self.height() > 0.0

    def contains(self, x: float, y: float) -> bool:
        return self.left <= x <= self.right and self.top <= y <= self.bottom

    def clamp01(self) -> "RectFNorm":
        self.left = max(0.0, min(1.0, self.left))
        self.top = max(0.0, min(1.0, self.top))
        self.right = max(0.0, min(1.0, self.right))
        self.bottom = max(0.0, min(1.0, self.bottom))
        return self

    def inset(self, dx: float, dy: float) -> "RectFNorm":
        # Android RectF.inset(dx, dy) adds dx to left and subtracts dx from right.
        # If dx is negative, the rect expands.
        rect = RectFNorm(self.left + dx, self.top + dy, self.right - dx, self.bottom - dy)
        return rect.clamp01()

class FallDetector:
    def __init__(self, edge_tolerance: float = 0.05, min_visibility: float = 0.5):
        self.edge_tolerance = edge_tolerance
        self.min_visibility = min_visibility

    def evaluate(self, landmarks: Dict[str, NormalizedPoint], roi: Optional[RectFNorm]) -> DetectionState:
        if roi is None:
            return DetectionState(in_safe_area=False, near_edge=False, fall_detected=False)

        ls = landmarks.get("LEFT_SHOULDER")
        rs = landmarks.get("RIGHT_SHOULDER")
        lh = landmarks.get("LEFT_HIP")
        rh = landmarks.get("RIGHT_HIP")
        if ls is None or rs is None or lh is None or rh is None:
            return DetectionState(in_safe_area=False, near_edge=False, fall_detected=False)
            
        lk = landmarks.get("LEFT_KNEE") or landmarks.get("LEFT_ANKLE") or lh
        rk = landmarks.get("RIGHT_KNEE") or landmarks.get("RIGHT_ANKLE") or rh

        pts = [ls, rs, lh, rh, lk, rk]
        if any(pt.visibility < self.min_visibility for pt in pts):
            return DetectionState(in_safe_area=False, near_edge=False, fall_detected=False)

        torso_center_x = (ls.x + rs.x + lh.x + rh.x) / 4.0
        torso_center_y = (ls.y + rs.y + lh.y + rh.y) / 4.0

        inside = roi.contains(torso_center_x, torso_center_y)

        expanded = roi.inset(-self.edge_tolerance, -self.edge_tolerance)
        near = not inside and expanded.contains(torso_center_x, torso_center_y)

        mid_sx = (ls.x + rs.x) / 2.0
        mid_sy = (ls.y + rs.y) / 2.0
        mid_hx = (lh.x + rh.x) / 2.0
        mid_hy = (lh.y + rh.y) / 2.0

        angle_rad = math.atan2((mid_hy - mid_sy), (mid_hx - mid_sx))
        angle_deg = abs(angle_rad * 180.0 / math.pi)
        is_horizontal = angle_deg < 25.0

        average_y = (lh.y + rh.y + ls.y + rs.y) / 4.0
        below_bed = average_y > roi.bottom

        fall = (not inside) and is_horizontal and below_bed

        return DetectionState(in_safe_area=inside, near_edge=near, fall_detected=fall)

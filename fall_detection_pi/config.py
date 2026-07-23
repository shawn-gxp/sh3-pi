# Fall detection config — env-overridable for deploy without editing code.
# Package is standalone (sibling of sh3-pi), not nested inside the BLE hub.

from __future__ import annotations

import json
import os
from pathlib import Path
from typing import List, Optional, Tuple

# Backend alert sink (optional — skipped if PATIENT_ID not set)
BACKEND_BASE_URL = os.environ.get(
    "FALL_BACKEND_BASE_URL", "http://172.16.2.156:5173/api"
).rstrip("/")
PATIENT_ID = os.environ.get("FALL_PATIENT_ID", "REPLACE_WITH_PATIENT_ID")
DEVICE_ID = os.environ.get("FALL_DEVICE_ID", "edge-ai-camera-01")
INTEGRATION_KEY = os.environ.get("FALL_INTEGRATION_KEY", "")
COOLDOWN_MS = int(os.environ.get("FALL_COOLDOWN_MS", "60000"))

# Toast / log spam guard
LOG_COOLDOWN_MS = 3000

# MediaPipe Pose Landmarker (Tasks API) confidence thresholds
MIN_POSE_DETECTION_CONFIDENCE = float(os.environ.get("FALL_MIN_DET_CONF", "0.5"))
MIN_POSE_TRACKING_CONFIDENCE = float(os.environ.get("FALL_MIN_TRACK_CONF", "0.5"))
MIN_POSE_PRESENCE_CONFIDENCE = float(os.environ.get("FALL_MIN_PRES_CONF", "0.5"))
NUM_POSES = int(os.environ.get("FALL_NUM_POSES", "1"))

# Model: lite (fast, Pi-friendly) | full | heavy
POSE_MODEL_VARIANT = os.environ.get("FALL_POSE_MODEL", "lite").strip().lower()
_MODEL_FILES = {
    "lite": "pose_landmarker_lite.task",
    "full": "pose_landmarker_full.task",
    "heavy": "pose_landmarker_heavy.task",
}
if POSE_MODEL_VARIANT not in _MODEL_FILES:
    POSE_MODEL_VARIANT = "lite"

_PKG_DIR = Path(__file__).resolve().parent
_MODELS_DIR = _PKG_DIR / "models"
# Workspace root = parent of this package (sibling of sh3-pi when laid out correctly)
_WORKSPACE_ROOT = _PKG_DIR.parent

# Official Google-hosted MediaPipe Tasks models (float16)
_MODEL_URLS = {
    "lite": (
        "https://storage.googleapis.com/mediapipe-models/pose_landmarker/"
        "pose_landmarker_lite/float16/latest/pose_landmarker_lite.task"
    ),
    "full": (
        "https://storage.googleapis.com/mediapipe-models/pose_landmarker/"
        "pose_landmarker_full/float16/latest/pose_landmarker_full.task"
    ),
    "heavy": (
        "https://storage.googleapis.com/mediapipe-models/pose_landmarker/"
        "pose_landmarker_heavy/float16/latest/pose_landmarker_heavy.task"
    ),
}

POSE_MODEL_URL = os.environ.get(
    "FALL_POSE_MODEL_URL", _MODEL_URLS[POSE_MODEL_VARIANT]
)
_default_model_path = _MODELS_DIR / _MODEL_FILES[POSE_MODEL_VARIANT]
POSE_MODEL_PATH = Path(
    os.environ.get("FALL_POSE_MODEL_PATH", str(_default_model_path))
)

# Camera: device index or RTSP URL
_cam = os.environ.get("RTSP_URL") or os.environ.get("FALL_CAMERA_SOURCE", "0")
try:
    CAMERA_SOURCE = int(_cam)
except ValueError:
    CAMERA_SOURCE = _cam

# Default bed ROI (normalized 0–1)
DEFAULT_POLYGON: List[Tuple[float, float]] = [
    (0.1, 0.1),
    (0.9, 0.1),
    (0.9, 0.9),
    (0.1, 0.9),
]


def _hub_config_candidates() -> List[Optional[Path]]:
    """Where bed ROI may live (package is not inside sh3-pi)."""
    return [
        Path(os.environ["FALL_HUB_CONFIG"]) if os.environ.get("FALL_HUB_CONFIG") else None,
        # sibling hub checkouts
        _WORKSPACE_ROOT / "sh3-pi" / "hub_config.json",
        _WORKSPACE_ROOT / "hub_config.json",
        # package-local override
        _PKG_DIR / "hub_config.json",
        # legacy nested layout (if someone still vendors it)
        _WORKSPACE_ROOT / "sh3-pi" / "medical_ble_toolkit" / "hub_config.json",
    ]


def _load_polygon_from_hub() -> None:
    global DEFAULT_POLYGON
    for path in _hub_config_candidates():
        if path is None or not path.is_file():
            continue
        try:
            cfg = json.loads(path.read_text(encoding="utf-8"))
            poly = (cfg.get("fall_detection") or {}).get("polygon")
            if poly and len(poly) >= 3:
                DEFAULT_POLYGON = [tuple(pt) for pt in poly]
                return
        except Exception:
            continue


_load_polygon_from_hub()


def hub_config_path() -> Path:
    """
    Path used by web API to persist ROI.
    Prefers existing hub_config.json; otherwise writes beside sibling sh3-pi.
    """
    for path in _hub_config_candidates():
        if path is not None and path.is_file():
            return path
    # Default create location: sibling sh3-pi if present, else package dir
    sibling = _WORKSPACE_ROOT / "sh3-pi" / "hub_config.json"
    if (_WORKSPACE_ROOT / "sh3-pi").is_dir():
        return sibling
    return _PKG_DIR / "hub_config.json"


def models_dir() -> Path:
    return _MODELS_DIR


def package_dir() -> Path:
    return _PKG_DIR

# Fall detection config — env-overridable for deploy without editing code.

from __future__ import annotations

import json
import os
from pathlib import Path
from typing import List, Tuple

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
_REPO_ROOT = _PKG_DIR.parent
_MODELS_DIR = _PKG_DIR / "models"

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
# Override path, else fall_detection/models/<variant>.task
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

# Default bed ROI (normalized 0–1). Prefer hub_config.json at repo root.
DEFAULT_POLYGON: List[Tuple[float, float]] = [
    (0.1, 0.1),
    (0.9, 0.1),
    (0.9, 0.9),
    (0.1, 0.9),
]

# fall_detection/config.py → repo root is parent.parent
_HUB_CONFIG_CANDIDATES = [
    _REPO_ROOT / "hub_config.json",
    Path(os.environ["FALL_HUB_CONFIG"]) if os.environ.get("FALL_HUB_CONFIG") else None,
    _REPO_ROOT / "medical_ble_toolkit" / "hub_config.json",
]


def _load_polygon_from_hub() -> None:
    global DEFAULT_POLYGON
    for path in _HUB_CONFIG_CANDIDATES:
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
    """Path used by web API to persist ROI (repo-root hub_config.json)."""
    return _REPO_ROOT / "hub_config.json"


def models_dir() -> Path:
    return _MODELS_DIR

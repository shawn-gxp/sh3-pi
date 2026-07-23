"""Ensure MediaPipe Pose Landmarker .task model is on disk (download if needed)."""
from __future__ import annotations

import logging
import urllib.request
from pathlib import Path

from . import config

logger = logging.getLogger("PoseModel")

_MIN_MODEL_BYTES = 100_000


def ensure_pose_model(force: bool = False) -> Path:
    """
    Return path to a usable pose_landmarker_*.task file.
    Downloads from Google's model CDN on first use unless FALL_POSE_MODEL_PATH is set.
    """
    path = Path(config.POSE_MODEL_PATH)
    if not force and path.is_file() and path.stat().st_size >= _MIN_MODEL_BYTES:
        return path

    path.parent.mkdir(parents=True, exist_ok=True)
    url = config.POSE_MODEL_URL
    logger.info("Downloading Pose Landmarker model (%s) → %s", config.POSE_MODEL_VARIANT, path)
    tmp = path.with_suffix(path.suffix + ".partial")
    try:
        urllib.request.urlretrieve(url, tmp)  # noqa: S310 — fixed Google CDN URL
        if not tmp.is_file() or tmp.stat().st_size < _MIN_MODEL_BYTES:
            raise RuntimeError(f"Downloaded model too small or missing: {tmp}")
        tmp.replace(path)
    except Exception:
        if tmp.is_file():
            try:
                tmp.unlink()
            except OSError:
                pass
        raise

    logger.info("Pose model ready: %s (%d bytes)", path, path.stat().st_size)
    return path

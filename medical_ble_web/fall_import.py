"""
Resolve the standalone `fall_detection_pi` package (sibling of sh3-pi, not nested).

Search order:
  1. Already importable (pip install -e ./fall_detection_pi)
  2. FALL_DETECTION_HOME / FALL_DETECTION_PATH env → package directory
  3. Sibling of sh3-pi:  <workspace>/fall_detection_pi
  4. Legacy names:       <workspace>/fall_detection  (old)
  5. Legacy nested:      <sh3-pi>/fall_detection_pi or fall_detection
"""
from __future__ import annotations

import logging
import os
import sys
from pathlib import Path
from typing import Optional

log = logging.getLogger("medical_ble_web.fall_import")

# medical_ble_web/ → sh3-pi/
_SH3_ROOT = Path(__file__).resolve().parent.parent

_PKG_NAMES = ("fall_detection_pi", "fall_detection")  # preferred first


def _try_import() -> bool:
    try:
        import fall_detection_pi  # noqa: F401

        return True
    except ImportError:
        pass
    try:
        import fall_detection  # noqa: F401

        return True
    except ImportError:
        return False


def _add_parent_of_package(package_dir: Path) -> None:
    """sys.path needs the parent of the package folder."""
    parent = str(package_dir.resolve().parent)
    if parent not in sys.path:
        sys.path.insert(0, parent)


def ensure_fall_detection() -> bool:
    """Make `import fall_detection_pi` (or legacy fall_detection) work."""
    if _try_import():
        return True

    candidates: list[Path] = []
    for key in ("FALL_DETECTION_HOME", "FALL_DETECTION_PATH"):
        raw = os.environ.get(key)
        if raw:
            candidates.append(Path(raw).expanduser())

    # Sibling layout (preferred)
    for name in _PKG_NAMES:
        candidates.append(_SH3_ROOT.parent / name)
    # Legacy nested under sh3-pi
    for name in _PKG_NAMES:
        candidates.append(_SH3_ROOT / name)

    for cand in candidates:
        try:
            root = cand.resolve()
        except OSError:
            continue
        if not root.is_dir():
            continue
        if not ((root / "fall_detector.py").is_file() or (root / "__init__.py").is_file()):
            continue
        _add_parent_of_package(root)
        if _try_import():
            log.info("Using fall package from %s", root)
            return True

    return False


def fall_detection_location() -> Optional[Path]:
    """Return filesystem path of the loaded package, if known."""
    for mod_name in _PKG_NAMES:
        try:
            mod = __import__(mod_name)
            return Path(mod.__file__).resolve().parent
        except Exception:
            continue
    return None


def import_camera_loop():
    """Import camera_loop from fall_detection_pi (or legacy name)."""
    if not ensure_fall_detection():
        raise ImportError("fall_detection_pi not found")
    try:
        from fall_detection_pi import camera_loop

        return camera_loop
    except ImportError:
        from fall_detection import camera_loop

        return camera_loop


def import_fall_config():
    if not ensure_fall_detection():
        raise ImportError("fall_detection_pi not found")
    try:
        from fall_detection_pi import config

        return config
    except ImportError:
        from fall_detection import config

        return config

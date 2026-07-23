"""
Resolve the standalone `fall_detection_pi` package.

Canonical layouts (never inside medical_ble_web/ or brands/):

  SHHMHub monorepo (preferred product layout):
    workspace/
      fall_detection_pi/          # sibling of hub folder
      sh3-pi/medical_ble_web/     # this file

  Flat multi-package checkout (e.g. some local trees):
    hub_root/
      fall_detection_pi/          # sibling of medical_ble_web
      medical_ble_web/

Search order:
  1. Already importable (pip install -e …)
  2. FALL_DETECTION_HOME / FALL_DETECTION_PATH
  3. Sibling of hub folder:  parent(sh3-pi)/fall_detection_pi   ← monorepo
  4. Sibling of medical_ble_web:  hub_root/fall_detection_pi   ← flat checkout
"""
from __future__ import annotations

import logging
import os
import sys
from pathlib import Path
from typing import Optional

log = logging.getLogger("medical_ble_web.fall_import")

# medical_ble_web/ → hub package root (sh3-pi/ in monorepo, or repo root in flat layout)
_SH3_ROOT = Path(__file__).resolve().parent.parent

_PKG_NAME = "fall_detection_pi"


def _try_import() -> bool:
    try:
        import fall_detection_pi  # noqa: F401

        return True
    except ImportError:
        return False


def _add_parent_of_package(package_dir: Path) -> None:
    """sys.path needs the parent of the package folder."""
    parent = str(package_dir.resolve().parent)
    if parent not in sys.path:
        sys.path.insert(0, parent)


def ensure_fall_detection() -> bool:
    """Make `import fall_detection_pi` work."""
    if _try_import():
        return True

    candidates: list[Path] = []
    for key in ("FALL_DETECTION_HOME", "FALL_DETECTION_PATH"):
        raw = os.environ.get(key)
        if raw:
            candidates.append(Path(raw).expanduser())

    # Monorepo: SHHMHub/fall_detection_pi next to SHHMHub/sh3-pi/
    candidates.append(_SH3_ROOT.parent / _PKG_NAME)
    # Flat hub root: package next to medical_ble_web/
    candidates.append(_SH3_ROOT / _PKG_NAME)

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
    try:
        import fall_detection_pi

        return Path(fall_detection_pi.__file__).resolve().parent
    except Exception:
        return None


def import_camera_loop():
    """Import camera_loop from fall_detection_pi."""
    if not ensure_fall_detection():
        raise ImportError(
            "fall_detection_pi not found — place it as a sibling of sh3-pi "
            "or set FALL_DETECTION_HOME / pip install -e ./fall_detection_pi"
        )
    from fall_detection_pi import camera_loop

    return camera_loop


def import_fall_config():
    if not ensure_fall_detection():
        raise ImportError(
            "fall_detection_pi not found — place it as a sibling of sh3-pi "
            "or set FALL_DETECTION_HOME / pip install -e ./fall_detection_pi"
        )
    from fall_detection_pi import config

    return config

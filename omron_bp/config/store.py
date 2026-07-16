"""
Simple JSON config so the user does not retype MAC/model every run.

Default path: ./omron_bp_device.json (cwd) or path you pass in.
"""

from __future__ import annotations

import json
import pathlib
from typing import Any, Dict, Optional

from omron_bp.logging_config import DBG_TAG, get_logger

logger = get_logger("config.store")

DEFAULT_CONFIG_NAME = "omron_bp_device.json"


def load_device_config(path: str | pathlib.Path | None = None) -> Dict[str, Any]:
    p = pathlib.Path(path or DEFAULT_CONFIG_NAME)
    if not p.is_file():
        # DBG-LOG
        logger.debug("%s no config file at %s", DBG_TAG, p)
        return {}
    data = json.loads(p.read_text(encoding="utf-8"))
    logger.info("Loaded config from %s", p)
    # DBG-LOG
    logger.debug("%s config keys=%s", DBG_TAG, list(data.keys()))
    return data


def save_device_config(
    *,
    address: str,
    model_id: str,
    path: str | pathlib.Path | None = None,
    extra: Optional[Dict[str, Any]] = None,
) -> pathlib.Path:
    p = pathlib.Path(path or DEFAULT_CONFIG_NAME)
    data: Dict[str, Any] = {
        "address": address,
        "model_id": model_id,
    }
    if extra:
        data.update(extra)
    p.write_text(json.dumps(data, indent=2), encoding="utf-8")
    logger.info("Saved config → %s", p)
    # DBG-LOG
    logger.debug("%s saved %s", DBG_TAG, data)
    return p

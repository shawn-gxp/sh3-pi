"""
Central logging setup for omron_bp.

RELEASE CLEANUP
---------------
All temporary/verbose debug instrumentation is tagged so you can find and
strip it before a final release:

  1. Search the codebase for:  DBG-LOG
  2. Search for logger.debug(  (most verbose BLE hex dumps)
  3. Optional env: OMRON_BP_DEBUG=1 forces DEBUG level without CLI flag

Production should keep INFO + WARNING + ERROR only.
"""

from __future__ import annotations

import logging
import os
import sys

# Root logger name for the whole package (children use omron_bp.*)
APP_LOGGER_NAME = "omron_bp"

# Prefix used in debug messages so release greps stay simple
# DBG-LOG: keep this tag on every temporary debug line you add
DBG_TAG = "[DBG]"


def get_logger(name: str | None = None) -> logging.Logger:
    """Return package logger or a child (e.g. omron_bp.ble.transport)."""
    if name is None:
        return logging.getLogger(APP_LOGGER_NAME)
    if name.startswith(APP_LOGGER_NAME):
        return logging.getLogger(name)
    return logging.getLogger(f"{APP_LOGGER_NAME}.{name}")


def setup_logging(verbose: bool = False) -> None:
    """
    Configure console logging once.

    verbose=True or OMRON_BP_DEBUG=1 → DEBUG (hex dumps, state steps).
    Otherwise INFO (pair/read progress suitable for normal use).
    """
    # DBG-LOG: env override for CI / field debugging without CLI flags
    env_debug = os.environ.get("OMRON_BP_DEBUG", "").strip() in ("1", "true", "True", "yes")
    level = logging.DEBUG if (verbose or env_debug) else logging.INFO

    root = logging.getLogger(APP_LOGGER_NAME)
    root.setLevel(level)

    # Avoid duplicate handlers if setup_logging() is called twice
    if not root.handlers:
        handler = logging.StreamHandler(sys.stdout)
        handler.setLevel(level)
        # DBG-LOG: detailed format helps correlate BLE steps; simplify for release if desired
        formatter = logging.Formatter(
            fmt="%(asctime)s | %(levelname)-7s | %(name)s | %(message)s",
            datefmt="%H:%M:%S",
        )
        handler.setFormatter(formatter)
        root.addHandler(handler)
    else:
        for handler in root.handlers:
            handler.setLevel(level)

    # DBG-LOG: announce effective level once at startup
    root.debug("%s logging ready level=%s", DBG_TAG, logging.getLevelName(level))

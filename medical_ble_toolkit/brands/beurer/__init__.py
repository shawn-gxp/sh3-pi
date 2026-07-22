"""
Beurer multi-device support (HealthManager Pro APK-aligned).

OCR fallback is intentionally not included.

Import note: do NOT import session/run_beurer_sync at module top-level.
brands/__init__ loads this package while parsers.omron is still initializing
(parsers → brands.omron.models → brands → beurer). Eager session import
creates a cycle: session → parser → parsers.omron (partial) → ImportError.
Session is lazy via __getattr__.
"""

from .catalog import (
    BeurerDevice,
    get_device,
    list_categories,
    list_devices,
    match_advertisement_name,
    supported_summary,
)
from .capabilities import DeviceCapabilities, get_capabilities
from .sync_result import SyncResult, SyncStatus
from .timing import timing_for_profile

__all__ = [
    "BeurerDevice",
    "BeurerCompanionSession",
    "DeviceCapabilities",
    "SyncResult",
    "SyncStatus",
    "run_beurer_sync",
    "get_capabilities",
    "get_device",
    "list_categories",
    "list_devices",
    "match_advertisement_name",
    "supported_summary",
    "timing_for_profile",
]


def __getattr__(name: str):
    if name in ("BeurerCompanionSession", "run_beurer_sync"):
        from .session import BeurerCompanionSession, run_beurer_sync

        if name == "BeurerCompanionSession":
            return BeurerCompanionSession
        return run_beurer_sync
    raise AttributeError(f"module {__name__!r} has no attribute {name!r}")

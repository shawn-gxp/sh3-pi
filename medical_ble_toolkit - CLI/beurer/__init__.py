"""
Beurer multi-device support (HealthManager Pro APK-aligned).

OCR fallback is intentionally not included.
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
from .session import BeurerCompanionSession, run_beurer_sync
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

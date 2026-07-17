"""
Pi hub — Tier-1 dedicated BLE collector.

Policy (product lock):
  - 4 devices: Omron BP, Nipro NBP-1BLE, Nipro NT-100B, Masimo MightySat
  - One GATT session at a time; all four may be paired and present
  - Windowed devices: continuous hunt → connect on AD immediately
  - Omron: editable poll interval (default 5 min) + opportunistic AD
  - Pair / bond: hub only (no phone)
  - Debug now: print + SQLite; later MQTT to cloud
"""

from .config import HubConfig, load_hub_config, save_hub_config, default_config_path
from .connection_manager import ConnectionManager
from .policy import (
    TIER1_BRANDS,
    DeviceClass,
    classify_brand,
    priority_rank,
    is_windowed,
    is_stream,
)
from .daemon import HubDaemon, HubStatus, SessionTarget

__all__ = [
    "HubConfig",
    "load_hub_config",
    "save_hub_config",
    "default_config_path",
    "ConnectionManager",
    "TIER1_BRANDS",
    "DeviceClass",
    "classify_brand",
    "priority_rank",
    "is_windowed",
    "is_stream",
    "HubDaemon",
    "HubStatus",
    "SessionTarget",
]

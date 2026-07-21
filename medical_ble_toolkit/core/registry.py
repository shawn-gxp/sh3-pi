"""
Plugin registry — brand_id -> DevicePlugin instance.

Registration is explicit: each brands/<brand>/plugin.py module registers
itself when imported, and brands/__init__.py imports every active brand
module once. brands/__init__.py is the ONLY file that changes when a new
brand is added.
"""
from __future__ import annotations

from typing import Dict, List

from .device_plugin import DevicePlugin

_PLUGINS: Dict[str, DevicePlugin] = {}


def register(plugin: DevicePlugin) -> None:
    if plugin.brand_id in _PLUGINS:
        raise ValueError(f"Plugin already registered for brand_id={plugin.brand_id!r}")
    _PLUGINS[plugin.brand_id] = plugin


def get_plugin(brand_id: str) -> DevicePlugin:
    try:
        return _PLUGINS[brand_id]
    except KeyError:
        raise KeyError(
            f"No plugin registered for brand_id={brand_id!r}. "
            f"Known: {sorted(_PLUGINS)}. Is brands/__init__.py importing it?"
        ) from None


def all_plugins() -> List[DevicePlugin]:
    return list(_PLUGINS.values())


def has_plugin(brand_id: str) -> bool:
    return brand_id in _PLUGINS

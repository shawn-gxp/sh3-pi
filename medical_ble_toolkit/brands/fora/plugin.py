"""
FORA DevicePlugin — thin adapter.

FORA currently has no custom pair/session logic. It uses the default
MedicalBleClient auto-dispatch in ble_jobs.py. This plugin exists just to
claim the brand ID and provide advertisement matching.
"""
from __future__ import annotations
from typing import Any

from medical_ble_toolkit.core.device_plugin import (
    DeviceClass,
    DevicePlugin,
    PairResult,
    SessionResult,
)
from medical_ble_toolkit.core.registry import register


class ForaPlugin(DevicePlugin):
    brand_id = "fora"
    device_class = DeviceClass.ALWAYS
    priority_rank = 40

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        raise NotImplementedError("FORA relies on MedicalBleClient fallback in ble_jobs.py")

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        raise NotImplementedError("FORA relies on MedicalBleClient fallback in ble_jobs.py")

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        if "fora" in name_l:
            return True
        return False


_plugin = ForaPlugin()
register(_plugin)

"""
Nipro DevicePlugin — thin adapter over medical_ble_toolkit.brands.nipro.registry.

No BLE/parsing logic lives here. This file wraps the existing, field-proven
Nipro registry onto the DevicePlugin interface.

Note: Nipro does not have a monolithic run_session(). It relies on the orchestrator's
fallback to MedicalBleClient (the legacy path) for now.
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


class NiproPlugin(DevicePlugin):
    brand_id = "nipro"
    device_class = DeviceClass.WINDOWED
    priority_rank = 30

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        from medical_ble_toolkit.brands.nipro.registry import register_meter
        register_meter(
            device_id=mac,
            profile_id=model or "nipro_nbp",
            name=model or "Nipro Device",
        )
        return PairResult(ok=True, mac=mac, model=model)

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        raise NotImplementedError("Nipro relies on MedicalBleClient fallback in ble_jobs.py")

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        if name_l.startswith(("nbp", "nmbp", "nt-100", "nsm-", "cocoron", "nt100")):
            return True
        return False


_plugin = NiproPlugin()
register(_plugin)

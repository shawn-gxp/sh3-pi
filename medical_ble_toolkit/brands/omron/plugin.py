"""
Omron DevicePlugin — thin adapter over medical_ble_toolkit.omron_bridge.

No BLE/parsing logic lives here. This file only maps the existing,
field-proven omron_bridge functions (pair_omron, read_omron, unpair_omron)
onto the DevicePlugin interface so the orchestrator can call Omron generically.

Import note: omron_bridge is loaded lazily inside methods. Eager import here
creates a cycle:
  omron_bridge → parsers.omron → brands → plugin → omron_bridge
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


class OmronPlugin(DevicePlugin):
    brand_id = "omron"
    device_class = DeviceClass.ALWAYS
    priority_rank = 10

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        from medical_ble_toolkit.omron_bridge import pair_omron

        await pair_omron(mac, model, force_rebind=force_rebind)
        return PairResult(ok=True, mac=mac, model=model)

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.omron_bridge import flatten_readings, read_omron

        find_timeout = kwargs.get("find_timeout", 15.0)
        session_retries = kwargs.get("session_retries", 2)
        all_users = await read_omron(
            mac, model, find_timeout=find_timeout, session_retries=session_retries
        )
        readings = flatten_readings(all_users)
        return SessionResult(ok=True, readings=readings)

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        if any(h in name_l for h in ("omron", "blesmart", "hem-")):
            return True
        if mfg_ids and 0x020E in mfg_ids:
            return True
        return False


_plugin = OmronPlugin()
register(_plugin)

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

from typing import Any, Optional

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
        last_exc: Optional[BaseException] = None
        all_users = None
        for attempt in (1, 2):
            try:
                if attempt == 2:
                    import asyncio
                    import logging
                    logging.getLogger("medical_ble_web.ble").warning(
                        "Omron READ retry 2/2 — direct reconnect "
                        "(cuff may still be in long post-session window)…"
                    )
                    await asyncio.sleep(1.5)
                all_users = await read_omron(
                    mac, model, find_timeout=find_timeout, session_retries=2
                )
                last_exc = None
                break
            except Exception as exc:  # noqa: BLE001
                last_exc = exc
                msg = str(exc).lower()
                if "fe4a" in msg or "parent service" in msg:
                    if attempt == 1:
                        continue  # retry once
                    raise  # let orchestrator's outer except re-wrap it
                raise
        if last_exc is not None:
            raise last_exc
        assert all_users is not None
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

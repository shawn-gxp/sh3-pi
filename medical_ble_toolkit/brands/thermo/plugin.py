from __future__ import annotations

from typing import Any

from medical_ble_toolkit.core.device_plugin import (
    DeviceClass,
    DevicePlugin,
    PairResult,
    SessionResult,
)
from medical_ble_toolkit.core.registry import register


class ThermoPlugin(DevicePlugin):
    brand_id = "thermo"
    device_class = DeviceClass.WINDOWED
    priority_rank = 70

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        raise NotImplementedError("Thermo (NT-100B) pairs via Nipro OS bond path.")

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.ble_client import MedicalBleClient
        from medical_ble_toolkit.profiles import get_profile

        on_reading = kwargs.get("on_reading")
        profile_id = kwargs.get("profile_id", "thermometer")
        listen_s = float(kwargs.get("listen_s", 20.0))
        name_hint = str(kwargs.get("name_hint", ""))
        
        duration = max(45.0, listen_s)
        profile = get_profile(profile_id)

        client = MedicalBleClient(
            address=mac.strip().upper(),
            profile=profile,
            pair=False,
            connect_retries=1,
            on_reading=on_reading,
            auto_dispatch=False,
            find_timeout=0.0,
            name_hint=name_hint,
        )
        await client.run(
            duration=duration,
            connect_timeout=30.0,
            quiet_timeout=None,
            stream_good_hold_s=kwargs.get("stream_good_hold_s"),
            stream_invalid_exit_s=kwargs.get("stream_invalid_exit_s"),
            stream_no_data_grace_s=float(kwargs.get("stream_no_data_grace_s", 8.0)),
        )

        listen_end = getattr(client, "_listen_end_reason", "") or ""
        return SessionResult(ok=True, readings=[], detail={"listen_end": listen_end})

    def listen_s(self, slot_s: float) -> float:
        return min(float(slot_s), 20.0)

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        return "thermo" in name_l


_plugin = ThermoPlugin()
register(_plugin)

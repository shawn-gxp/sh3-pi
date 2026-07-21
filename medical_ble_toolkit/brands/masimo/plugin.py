from __future__ import annotations

from typing import Any

from medical_ble_toolkit.core.device_plugin import (
    DeviceClass,
    DevicePlugin,
    PairResult,
    SessionResult,
)
from medical_ble_toolkit.core.registry import register


class MasimoPlugin(DevicePlugin):
    brand_id = "masimo"
    device_class = DeviceClass.STREAM
    priority_rank = 50

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        from medical_ble_toolkit.brands.nipro.registry import register_meter
        # Masimo currently uses Nipro registry in job_pair
        register_meter(
            device_id=mac,
            profile_id=model or "mightysat",
            name=model or "Masimo Device",
        )
        return PairResult(ok=True, mac=mac, model=model)

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.ble_client import MedicalBleClient
        from medical_ble_toolkit.profiles import get_profile

        on_reading = kwargs.get("on_reading")
        profile_id = kwargs.get("profile_id", "mightysat")
        listen_s = float(kwargs.get("listen_s", 60.0))
        name_hint = str(kwargs.get("name_hint", ""))
        
        # duration capped at max(20.0, min(listen_s, 180.0))
        duration = max(20.0, min(listen_s, 180.0))
        profile = get_profile(profile_id)

        client = MedicalBleClient(
            address=mac.strip().upper(),
            profile=profile,
            pair=False,
            connect_retries=3,
            on_reading=on_reading,
            auto_dispatch=False,
            find_timeout=0.0,
            name_hint=name_hint,
        )
        await client.run(
            duration=duration,
            connect_timeout=35.0,
            quiet_timeout=0.0,  # stream mode
            stream_good_hold_s=kwargs.get("stream_good_hold_s"),
            stream_invalid_exit_s=kwargs.get("stream_invalid_exit_s"),
            stream_no_data_grace_s=float(kwargs.get("stream_no_data_grace_s", 8.0)),
        )

        listen_end = getattr(client, "_listen_end_reason", "") or ""
        return SessionResult(ok=True, readings=[], detail={"listen_end": listen_end})

    def listen_s(self, slot_s: float) -> float:
        return float(slot_s)

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        return "masimo" in name_l


_plugin = MasimoPlugin()
register(_plugin)

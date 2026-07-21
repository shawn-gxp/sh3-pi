from __future__ import annotations

from typing import Any

from medical_ble_toolkit.core.device_plugin import (
    DeviceClass,
    DevicePlugin,
    PairResult,
    SessionResult,
)
from medical_ble_toolkit.core.registry import register


class AndPlugin(DevicePlugin):
    brand_id = "and"
    device_class = DeviceClass.WINDOWED
    priority_rank = 60

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        from medical_ble_toolkit.ble_client import MedicalBleClient
        from medical_ble_toolkit.profiles import get_profile

        profile = get_profile("and_ua651")
        client = MedicalBleClient(
            address=mac.strip().upper(),
            profile=profile,
            pair=True,
            connect_retries=1,
            auto_dispatch=False,
            find_timeout=35.0,
        )
        await client.run(duration=0.5, connect_timeout=35.0)
        return PairResult(ok=True, mac=mac, model=model)

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.ble_client import MedicalBleClient
        from medical_ble_toolkit.common.winrt_errors import os_pair_supported
        from medical_ble_toolkit.profiles import get_profile

        original_cb = kwargs.get("on_reading")
        
        def _and_filter(r: Any) -> None:
            # Need to convert r to dict to check reading_type if necessary
            # Actually, `_is_clinical` check is normally done on the DB row, but here
            # we know A&D readings. The main thing is filtering non-clinical.
            # medical_ble_web/ble_jobs.py _is_clinical logic:
            # "systolic" in row, or "glucose_mg_dl", or "spo2", or "temperature"
            # A&D returns dicts with 'systolic', 'diastolic', etc.
            is_clinical = False
            d = {}
            if hasattr(r, "to_dict"):
                d = r.to_dict()
            elif isinstance(r, dict):
                d = r
            
            if d.get("systolic") is not None:
                is_clinical = True
                
            if not is_clinical:
                return
            if original_cb:
                original_cb(r)

        listen_s = float(kwargs.get("listen_s", 60.0))
        duration = max(45.0, listen_s)
        profile = get_profile("and_ua651")
        already_paired = bool(kwargs.get("already_paired", False))
        do_pair = os_pair_supported() and not already_paired

        client = MedicalBleClient(
            address=mac.strip().upper(),
            profile=profile,
            pair=do_pair,
            connect_retries=3,
            on_reading=_and_filter,
            auto_dispatch=False,
            find_timeout=0.0,
            name_hint=str(kwargs.get("name_hint", "")),
        )
        await client.run(
            duration=duration,
            connect_timeout=35.0,
            quiet_timeout=None,
            stream_good_hold_s=kwargs.get("stream_good_hold_s"),
            stream_invalid_exit_s=kwargs.get("stream_invalid_exit_s"),
            stream_no_data_grace_s=float(kwargs.get("stream_no_data_grace_s", 8.0)),
        )

        listen_end = getattr(client, "_listen_end_reason", "") or ""
        return SessionResult(ok=True, readings=[], detail={"listen_end": listen_end})

    def listen_s(self, slot_s: float) -> float:
        return min(float(slot_s), 60.0)

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        return "a&d" in name_l


_plugin = AndPlugin()
register(_plugin)

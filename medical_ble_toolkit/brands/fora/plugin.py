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
    device_class = DeviceClass.WINDOWED
    priority_rank = 40

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        raise NotImplementedError("FORA protocol not yet reverse-engineered.")

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.ble_client import MedicalBleClient
        from medical_ble_toolkit.common.winrt_errors import os_pair_supported
        from medical_ble_toolkit.profiles import get_profile

        on_reading = kwargs.get("on_reading")
        profile_id = kwargs.get("profile_id", "fora6")
        listen_s = float(kwargs.get("listen_s", 30.0))
        find_timeout = kwargs.get("find_timeout")
        already_paired = bool(kwargs.get("already_paired", False))
        name_hint = str(kwargs.get("name_hint", ""))

        find_to = max(0.0, float(find_timeout)) if find_timeout is not None else 0.0
        do_pair = os_pair_supported() and not already_paired
        profile = get_profile(profile_id)

        client = MedicalBleClient(
            address=mac.strip().upper(),
            profile=profile,
            pair=do_pair,
            connect_retries=2,
            on_reading=on_reading,
            auto_dispatch=True,   # FORA = RE mode, subscribe-all
            find_timeout=find_to,
            name_hint=name_hint,
        )
        await client.run(
            duration=listen_s,
            connect_timeout=35.0,
            quiet_timeout=None,  # RE mode: no quiet end
            stream_good_hold_s=kwargs.get("stream_good_hold_s"),
            stream_invalid_exit_s=kwargs.get("stream_invalid_exit_s"),
            stream_no_data_grace_s=float(kwargs.get("stream_no_data_grace_s", 8.0)),
        )
        listen_end = getattr(client, "_listen_end_reason", "") or ""
        return SessionResult(ok=True, readings=[], detail={"listen_end": listen_end})

    def listen_s(self, slot_s: float) -> float:
        return min(float(slot_s), 30.0)

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        return any(h in name_l for h in ("fora", "fora6", "ifora"))


_plugin = ForaPlugin()
register(_plugin)

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
    aliases = ("nipro_nbp", "nipro_nmbp", "nipro_cf", "nipro_companion")
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
        from medical_ble_toolkit.ble_client import MedicalBleClient
        from medical_ble_toolkit.brands.nipro import post_measure as pm
        from medical_ble_toolkit.common.winrt_errors import os_pair_supported
        from medical_ble_toolkit.profiles import get_profile

        on_reading = kwargs.get("on_reading")
        profile_id = kwargs.get("profile_id", model or "nipro_nbp")
        listen_s = float(kwargs.get("listen_s", 60.0))
        find_timeout = kwargs.get("find_timeout")
        already_paired = bool(kwargs.get("already_paired", False))
        name_hint = str(kwargs.get("name_hint", ""))
        dev_mac = mac.strip().upper()

        is_thermo = profile_id in ("nipro_nt100b", "nipro_nsm1", "thermometer")
        is_nipro_bp = profile_id in ("nipro_nbp", "nipro_nmbp")
        is_nipro_cf = profile_id == "nipro_cf"

        duration = max(listen_s, pm.receive_s_for(profile_id))

        if is_thermo or profile_id in ("nipro_nt100b", "nipro_nbp"):
            do_pair = False
        elif profile_id in ("nipro_nmbp", "and_ua651"):
            do_pair = os_pair_supported() and not already_paired
        else:
            do_pair = os_pair_supported() and not already_paired

        if find_timeout is not None:
            find_to = max(0.0, float(find_timeout))
        else:
            find_to = 12.0 if (is_nipro_bp or is_thermo) else 0.0

        connect_retries = 4 if find_to > 0 else 3
        connect_to = 12.0 if find_to > 0 else (30.0 if is_thermo else 35.0)

        profile = get_profile(profile_id)
        qt = pm.quiet_s_for(profile_id)

        client = MedicalBleClient(
            address=dev_mac,
            profile=profile,
            pair=do_pair,
            connect_retries=connect_retries,
            on_reading=on_reading,
            auto_dispatch=False,
            find_timeout=find_to,
            name_hint=name_hint,
        )
        await client.run(
            duration=duration,
            connect_timeout=connect_to,
            quiet_timeout=qt,
            stream_good_hold_s=kwargs.get("stream_good_hold_s"),
            stream_invalid_exit_s=kwargs.get("stream_invalid_exit_s"),
            stream_no_data_grace_s=float(kwargs.get("stream_no_data_grace_s", 8.0)),
        )

        try:
            from medical_ble_toolkit.brands.nipro.registry import register_meter
            register_meter(
                device_id=dev_mac,
                name=name_hint or profile_id,
                profile_id=profile_id,
                address=dev_mac,
            )
        except Exception:  # noqa: BLE001
            pass

        listen_end = getattr(client, "_listen_end_reason", "") or ""
        return SessionResult(ok=True, readings=[], detail={"listen_end": listen_end})

    def listen_s(self, slot_s: float) -> float:
        return min(float(slot_s), 65.0)

    def quiet_timeout_s(self, profile_id: str) -> float:
        from medical_ble_toolkit.brands.nipro.post_measure import quiet_s_for
        return quiet_s_for(profile_id) or 0.0

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        if name_l.startswith(("nbp", "nmbp", "nt-100", "nsm-", "cocoron", "nt100")):
            return True
        return False

_plugin = NiproPlugin()
register(_plugin)

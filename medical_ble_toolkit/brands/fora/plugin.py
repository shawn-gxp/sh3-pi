"""
FORA DevicePlugin — first-party TaiDoc-bus session (iFORA Smart 1.5.9 parity).
"""
from __future__ import annotations
from datetime import datetime, timezone
from typing import Any, Optional

from medical_ble_toolkit.core.device_plugin import (
    DeviceClass,
    DevicePlugin,
    PairResult,
    SessionResult,
)
from medical_ble_toolkit.core.registry import register
from medical_ble_toolkit.brands.fora import protocol as P


def _dt_from_rec(rec: dict) -> Optional[datetime]:
    dt = rec.get("datetime")
    if isinstance(dt, dict):
        return P.measured_at_from_dt(dt)
    return datetime.now(timezone.utc)


class ForaPlugin(DevicePlugin):
    brand_id = "fora"
    device_class = DeviceClass.WINDOWED
    priority_rank = 40

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        """
        Companion: OS bond + store prefs. Default PIN constant is 111111.
        Proves link with set-time + SN + project (max_records=0 skips history).
        """
        from medical_ble_toolkit.brands.fora.session import run_fora_sync

        res = await run_fora_sync(
            mac,
            name_hint=model or "",
            find_timeout=12.0,
            already_paired=False,
            set_time=True,
            power_off=True,
            max_records=0,
            do_firmware=True,
        )
        if not res.ok and not res.project_code and not res.serial:
            return PairResult(
                ok=False,
                mac=mac,
                model=model or "FORA",
                detail={"error": res.error, "pin_hint": P.METER_PIN_CODE},
            )
        return PairResult(
            ok=True,
            mac=mac,
            model=model or res.project_code or "FORA",
            detail={
                "project_code": res.project_code,
                "serial": res.serial,
                "import_type": res.import_type,
                "firmware": res.firmware,
                "pin_hint": P.METER_PIN_CODE,
                "service_uuid": P.SERVICE_UUID,
                "char_uuid": P.CHAR_UUID,
            },
        )

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.brands.fora.session import run_fora_sync
        from medical_ble_toolkit.models import (
            BloodPressureReading,
            DeviceBrand,
            MultiParameterReading,
            PressureUnit,
        )

        on_reading = kwargs.get("on_reading")
        find_timeout = kwargs.get("find_timeout")
        find_to = max(0.0, float(find_timeout)) if find_timeout is not None else 12.0
        already_paired = bool(kwargs.get("already_paired", True))
        name_hint = str(kwargs.get("name_hint", model or ""))
        listen_s = float(kwargs.get("listen_s", 60.0))
        max_records = int(kwargs.get("max_records", 200))

        res = await run_fora_sync(
            mac,
            name_hint=name_hint,
            find_timeout=find_to,
            already_paired=already_paired,
            max_records=max_records,
            set_time=True,
            power_off=True,
            connect_timeout=min(35.0, max(15.0, listen_s)),
            do_firmware=True,
        )

        readings: list[Any] = []
        model_label = model or res.project_code or "FORA"

        for rec in res.raw_records:
            if rec.get("invalid"):
                continue
            kind = rec.get("kind") or "bg"
            measured = _dt_from_rec(rec)

            if kind == "bp":
                try:
                    r = BloodPressureReading(
                        systolic=float(rec.get("systolic") or 0),
                        diastolic=float(rec.get("diastolic") or 0),
                        mean_arterial_pressure=rec.get("map"),
                        pulse_rate=rec.get("pulse"),
                        unit=PressureUnit.MMHG,
                        measured_at=measured,
                        user_id=None,
                        irregular_pulse=bool(rec.get("irregular_pulse")),
                        brand=DeviceBrand.FORA,
                        model=model_label,
                        raw_hex=str(rec.get("value_frame") or rec.get("raw_hex") or ""),
                    )
                except Exception:
                    continue
            elif kind == "ws":
                # Raw only until HCI maps scale body
                r = MultiParameterReading(
                    brand=DeviceBrand.FORA,
                    model=model_label,
                    measured_at=measured,
                    raw_hex=str(rec.get("raw_hex") or ""),
                    notes=f"ws_long len={rec.get('length')} project={res.project_code}",
                )
            else:
                val = rec.get("blood_glucose_mg_dl")
                if val is None:
                    val = rec.get("value_u16")
                    if val is not None and val != P.BG_INVALID:
                        val = float(val)
                    else:
                        continue
                if val is not None and (val < P.BG_LOW or val > P.BG_HIGH):
                    # Still emit with note — app marks High/Low states
                    pass
                r = MultiParameterReading(
                    brand=DeviceBrand.FORA,
                    model=model_label,
                    measured_at=measured,
                    blood_glucose_mg_dl=float(val) if val is not None else None,
                    meal_tag=rec.get("meal_tag"),
                    raw_hex=str(rec.get("value_frame") or ""),
                    notes=(
                        f"index={rec.get('index')} project={res.project_code} "
                        f"import_type={rec.get('import_type')} "
                        f"type_code={rec.get('type_code')}"
                    ),
                )

            readings.append(r)
            if on_reading:
                try:
                    on_reading(r)
                except Exception:
                    pass

        return SessionResult(
            ok=res.ok,
            readings=readings,
            detail={
                "project_code": res.project_code,
                "serial": res.serial,
                "record_count": res.record_count,
                "import_type": res.import_type,
                "firmware": res.firmware,
                "error": res.error,
                "frames": len(res.frames),
                "protocol": "taidoc_bus_v1",
                "pin_hint": P.METER_PIN_CODE,
                "timings": P.TIMINGS.__dict__,
                **(res.detail or {}),
            },
        )

    def listen_s(self, slot_s: float) -> float:
        return min(float(slot_s), 90.0)

    def quiet_timeout_s(self, profile_id: str) -> float:
        return 0.0

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        return P.name_matches_series(name or "")


_plugin = ForaPlugin()
register(_plugin)

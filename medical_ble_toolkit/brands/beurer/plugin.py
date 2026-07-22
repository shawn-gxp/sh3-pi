"""
Beurer DevicePlugin — thin adapter over medical_ble_toolkit.brands.beurer.session.

Production path (validated on Pi with BM54 lab experiment tools_bm54_pair_check.py):

  1. Optional force_rebind → bluetoothctl remove (stale bond / agent timeout)
  2. BlueZ KeyboardDisplay agent registered BEFORE connect (outer context)
  3. Connect by MAC → settle (BM54 te ≈ 3s)
  4. CCCD Indicate 0x2A35 — SMP/passkey often completes *inside* start_notify
     (6-digit code only appears on cuff LCD then; UI feeds PasskeyBroker)
  5. If ATT 0x05 → explicit pair_client(register_agent=False) → CCCD retry
  6. ensure_bluez_trusted → quiet-end listen for BLP dump
  7. Link drop after dump is normal

No BLE/parsing logic lives here — only session orchestration knobs.
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


# Lab-proven budgets (tools_bm54_pair_check.py pair success on Pi)
_PAIR_CONNECT_TIMEOUT_S = 25.0
_PAIR_CONNECT_RETRIES = 2
# Hub already saw AD → still need margin for settle + passkey mid-CCCD
_HUB_CONNECT_TIMEOUT_S = 20.0
_HUB_CONNECT_TIMEOUT_SLOW_S = 25.0


def _normalize_passkey(passkey: Any) -> int | None:
    if passkey is None or passkey is False:
        return None
    if isinstance(passkey, int):
        return passkey if 0 <= passkey <= 999999 else passkey % 1000000
    if isinstance(passkey, str) and passkey.strip():
        digits = "".join(c for c in passkey if c.isdigit())
        if not digits:
            return None
        return int(digits[-6:]) if len(digits) > 6 else int(digits)
    return None


class BeurerPlugin(DevicePlugin):
    brand_id = "beurer"
    device_class = DeviceClass.WINDOWED
    priority_rank = 20

    async def pair(
        self,
        mac: str,
        model: str,
        *,
        force_rebind: bool = False,
        passkey: int | None = None,
        **kwargs: Any,
    ) -> PairResult:
        from medical_ble_toolkit.brands.beurer.session import BeurerCompanionSession

        # Clean rebind: drop stale BlueZ bond (Pi: broken keys / "No agent" timeout)
        if force_rebind:
            try:
                from medical_ble_toolkit.brands.omron.ble.connection import (
                    unpair_address,
                )

                await unpair_address(mac)
            except Exception:  # noqa: BLE001
                pass

        # passkey optional — normally None; cuff shows 6 digits only after SMP starts
        pk = _normalize_passkey(
            passkey if passkey is not None else kwargs.get("passkey")
        )
        sess = BeurerCompanionSession(
            mac,
            model_id=model or "BM54",
            pair=True,
            connect_timeout=_PAIR_CONNECT_TIMEOUT_S,
            connect_retries=_PAIR_CONNECT_RETRIES,
            passkey=pk,
        )
        result = await sess.run()
        if not result.ok:
            raise RuntimeError(result.message or result.status.value)
        return PairResult(
            ok=True,
            mac=mac,
            model=model or "BM54",
            detail={
                "status": getattr(result.status, "value", str(result.status)),
                "message": result.message,
                "readings": list(result.readings or []),
                "reading_count": len(result.readings or []),
                "raw_count": int(result.raw_count or 0),
                "passkey_hint": bool(result.passkey_hint),
            },
        )

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.brands.beurer.session import BeurerCompanionSession

        find_timeout = float(kwargs.get("find_timeout") or 0.0)
        # Hub already saw AD → connect quickly but leave room for passkey if re-bond
        connect_timeout = (
            _HUB_CONNECT_TIMEOUT_S if find_timeout <= 0 else _HUB_CONNECT_TIMEOUT_SLOW_S
        )
        pk = _normalize_passkey(kwargs.get("passkey"))
        # After first bond, OS has keys; pair=True is still needed so agent can
        # re-bond if BlueZ lost the link (and matches companion behaviour).
        sess = BeurerCompanionSession(
            mac,
            model_id=model or "BM54",
            pair=True,
            connect_timeout=connect_timeout,
            connect_retries=2,
            passkey=pk,
        )
        result = await sess.run()
        if not result.ok and not result.readings:
            raise RuntimeError(result.message or str(result.status))
        return SessionResult(
            ok=result.ok or bool(result.readings),
            readings=list(result.readings or []),
            detail={
                "status": getattr(result.status, "value", str(result.status)),
                "message": result.message,
                "raw_count": int(result.raw_count or 0),
            },
        )

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        if any(h in name_l for h in ("beurer", "bm", "bc", "ft", "po60", "gl")):
            return True
        if mfg_ids and 0x0611 in mfg_ids:
            return True
        return False

    def listen_s(self, slot_s: float) -> float:
        # BP dump is short once CCCD works (~2–5s); leave margin for settle
        return min(float(slot_s), 45.0)

    def quiet_timeout_s(self, profile_id: str) -> float:
        return 4.0


_plugin = BeurerPlugin()
register(_plugin)

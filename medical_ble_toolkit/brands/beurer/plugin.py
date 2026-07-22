"""
Beurer DevicePlugin — thin adapter over medical_ble_toolkit.brands.beurer.session.

No BLE/parsing logic lives here. This file wraps the existing, field-proven
BeurerCompanionSession onto the DevicePlugin interface.

Import note: beurer session is loaded lazily inside methods to avoid circular
imports (same pattern as omron plugin).
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
        # Fast pair path: short connect budget (device must be advertising)
        # passkey = 6-digit code from cuff LCD (Beurer BM54 etc.)
        pk = passkey if passkey is not None else kwargs.get("passkey")
        if isinstance(pk, str) and pk.strip():
            pk = int("".join(c for c in pk if c.isdigit()) or "0") or None
        sess = BeurerCompanionSession(
            mac,
            model_id=model or "BM54",
            pair=True,
            connect_timeout=15.0,
            connect_retries=2,
            passkey=pk if isinstance(pk, int) else None,
        )
        result = await sess.run()
        if not result.ok:
            raise RuntimeError(result.message or result.status.value)
        return PairResult(ok=True, mac=mac, model=model)

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        from medical_ble_toolkit.brands.beurer.session import BeurerCompanionSession
        find_timeout = float(kwargs.get("find_timeout") or 0.0)
        # Hub already saw AD → quick connect; UI pair uses defaults above
        connect_timeout = 12.0 if find_timeout <= 0 else 20.0
        pk = kwargs.get("passkey")
        if isinstance(pk, str) and pk.strip():
            pk = int("".join(c for c in pk if c.isdigit()) or "0") or None
        sess = BeurerCompanionSession(
            mac,
            model_id=model or "BM54",
            pair=True,
            connect_timeout=connect_timeout,
            connect_retries=2,
            passkey=pk if isinstance(pk, int) else None,
        )
        result = await sess.run()
        if not result.ok and not result.readings:
            raise RuntimeError(result.message or str(result.status))
        return SessionResult(
            ok=result.ok,
            readings=result.readings,
            detail={"status": str(result.status), "message": result.message},
        )

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        if any(h in name_l for h in ("beurer", "bm", "bc", "ft", "po60", "gl")):
            return True
        if mfg_ids and 0x0611 in mfg_ids:
            return True
        return False


_plugin = BeurerPlugin()
register(_plugin)

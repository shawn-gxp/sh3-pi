"""
Per-model capability flags from HealthManager Pro APK ``mo.*`` markers.

Generated from ``entities/device/*.java`` implements lists + sync semantics in ``oi/*``.
"""

from __future__ import annotations

import json
import logging
from dataclasses import dataclass, field
from functools import lru_cache
from pathlib import Path
from typing import Any, Dict, List, Optional

log = logging.getLogger("medical_ble.beurer.capabilities")


@dataclass(frozen=True)
class DeviceCapabilities:
    model_id: str
    settle_3s: bool = False
    pulse_swapped: bool = False
    set_time: bool = False
    set_time_with_response: bool = True
    glucose_long_racp: bool = False
    afib_variant: Optional[str] = None  # "ag" | "uf" | None
    passkey_likely: bool = False
    ecg_combo: bool = False
    markers: tuple = field(default_factory=tuple)
    quiet_timeout_s: float = 4.0
    # Advertising manufacturer data (Beurer CID 0x0611)
    mfg_passkey_payload_hint: bytes = field(default=b"\x01")  # base 0x110601 style

    @property
    def post_connect_settle_s(self) -> float:
        return 3.0 if self.settle_3s else 0.8

    @property
    def before_racp_s(self) -> float:
        return 2.5 if self.glucose_long_racp else 0.5


def _caps_path() -> Path:
    return Path(__file__).resolve().parent / "capabilities.json"


@lru_cache(maxsize=1)
def _raw_caps() -> dict:
    path = _caps_path()
    if not path.is_file():
        log.warning("capabilities.json missing at %s", path)
        return {"devices": {}, "quiet_timeout_s": 4.0}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        log.error("capabilities load failed: %s", exc)
        return {"devices": {}, "quiet_timeout_s": 4.0}


def get_capabilities(model_id: str) -> DeviceCapabilities:
    mid = (model_id or "").strip().upper()
    # try exact then without suffix
    devices: Dict[str, Any] = _raw_caps().get("devices") or {}
    raw = devices.get(mid) or devices.get(model_id) or {}
    # case-insensitive key
    if not raw:
        for k, v in devices.items():
            if k.upper() == mid:
                raw = v
                mid = k
                break
    quiet = float(_raw_caps().get("quiet_timeout_s") or 4.0)
    if not raw:
        # Sensible defaults by family prefix
        return DeviceCapabilities(
            model_id=mid or "UNKNOWN",
            set_time=mid.startswith(("GL", "ME", "BM9")),
            passkey_likely=mid.startswith("BM5") or mid.startswith("BM6"),
            quiet_timeout_s=quiet,
        )
    afib = raw.get("afib_variant")
    return DeviceCapabilities(
        model_id=mid,
        settle_3s=bool(raw.get("settle_3s")),
        pulse_swapped=bool(raw.get("pulse_swapped")),
        set_time=bool(raw.get("set_time")),
        set_time_with_response=bool(raw.get("set_time_with_response", True)),
        glucose_long_racp=bool(raw.get("glucose_long_racp")),
        afib_variant=str(afib) if afib else None,
        passkey_likely=bool(raw.get("passkey_likely")),
        ecg_combo=bool(raw.get("ecg_combo")),
        markers=tuple(raw.get("markers") or ()),
        quiet_timeout_s=quiet,
        mfg_passkey_payload_hint=(
            b"\x01\x03" if raw.get("passkey_likely") else b"\x01"
        ),
    )


def list_models_with_flag(flag: str) -> List[str]:
    devices = _raw_caps().get("devices") or {}
    out = []
    for mid, raw in devices.items():
        if raw.get(flag):
            out.append(mid)
    return sorted(out)


# Beurer Bluetooth company ID
BEURER_COMPANY_ID = 0x0611


def mfg_data_suggests_passkey(manufacturer_data: dict) -> Optional[bool]:
    """
    PDF/APK: company 0x0611, payload often 0x01 (+ 0x03 on passkey-capable gen).

    manufacturer_data: bleak dict {company_id: bytes}
    """
    if not manufacturer_data:
        return None
    blob = manufacturer_data.get(BEURER_COMPANY_ID)
    if blob is None:
        # try int keys
        for k, v in manufacturer_data.items():
            if int(k) == BEURER_COMPANY_ID:
                blob = v
                break
    if blob is None:
        return None
    raw = bytes(blob)
    # Payloads like 01 or 01 03 or 11 06 01 03 (endian variants)
    if b"\x01\x03" in raw or raw.endswith(b"\x03") or (len(raw) >= 1 and raw[-1] == 0x03):
        return True
    if raw and raw[0] in (0x01, 0x11):
        return False  # older non-passkey style still syncable
    return None

"""
Beurer device catalog loaded from APK-derived device_registry.json.

OCR profile is intentionally excluded from the support matrix.
"""

from __future__ import annotations

import json
import logging
from dataclasses import dataclass, field
from functools import lru_cache
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Sequence

log = logging.getLogger("medical_ble.beurer.catalog")

# Skip OCR-only / non-BLE companion paths
EXCLUDED_PROFILES = frozenset({"ocr_fallback"})

# Map registry profile key → toolkit protocol id / parser profile
PROFILE_TO_TOOLKIT: Dict[str, str] = {
    "bp_sig": "beurer_bp",
    "glucose_sig": "beurer_glucose",
    "thermometer_sig": "beurer_thermo",
    "scale_mixed": "beurer_scale",
    "tracker_as87": "beurer_as87",
    "tracker_as98": "beurer_as98",
    "tracker_as99": "beurer_as99",
    "tracker_legacy": "beurer_tracker_legacy",
    "ecg_custom": "beurer_ecg",
    "pulse_ox": "beurer_po60",
    "hydration_dm20": "beurer_hydration",
}

CATEGORY_LABELS = {
    "blood_pressure": "Blood pressure (SIG BLP)",
    "blood_glucose": "Blood glucose",
    "thermometer": "Thermometer (HTS/FT)",
    "scale": "Scale",
    "activity_tracker": "Activity tracker",
    "ecg": "ECG + BP combo",
    "pulse_oximeter": "Pulse oximeter",
    "hydration": "Hydration",
    "other": "Other",
}


@dataclass(frozen=True)
class BeurerDevice:
    id: str
    storage_name: str
    advertisement_names: Sequence[str]
    primary_category: str
    ble_scan_uuid: Optional[str]
    protocol_profile: str
    toolkit_profile: str
    ocr_supported: bool = False
    ordinal: int = 0
    discover: Sequence[dict] = field(default_factory=tuple)

    @property
    def label(self) -> str:
        names = ", ".join(self.advertisement_names[:3]) if self.advertisement_names else self.id
        return f"{self.id}  [{names}]"


def _registry_path() -> Path:
    # medical_ble_toolkit/beurer/catalog.py → experiments/datasheets/beurer/tools/
    here = Path(__file__).resolve()
    candidates = [
        here.parents[2] / "datasheets" / "beurer" / "tools" / "device_registry.json",
        Path.cwd() / "datasheets" / "beurer" / "tools" / "device_registry.json",
    ]
    for p in candidates:
        if p.is_file():
            return p
    return candidates[0]


@lru_cache(maxsize=1)
def load_registry() -> dict:
    path = _registry_path()
    if not path.is_file():
        log.warning("Beurer device_registry.json not found at %s", path)
        return {}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        log.error("Failed to load Beurer registry: %s", exc)
        return {}


def _device_protocol(reg: dict, device_id: str) -> str:
    """Find which profile lists this device id."""
    profiles = reg.get("profiles") or {}
    for pid, meta in profiles.items():
        if pid in EXCLUDED_PROFILES:
            continue
        devices = meta.get("devices") or []
        if device_id in devices:
            return pid
    # Fallbacks by category
    return ""


@lru_cache(maxsize=1)
def list_devices(*, include_ocr_only: bool = False) -> List[BeurerDevice]:
    reg = load_registry()
    out: List[BeurerDevice] = []
    for raw in reg.get("devices") or []:
        did = str(raw.get("id") or "")
        if not did:
            continue
        proto = _device_protocol(reg, did)
        if not proto:
            # Infer from category
            cat = raw.get("primary_category") or ""
            infer = {
                "blood_pressure": "bp_sig",
                "blood_glucose": "glucose_sig",
                "thermometer": "thermometer_sig",
                "scale": "scale_mixed",
                "activity_tracker": "tracker_as87",
                "ecg": "ecg_custom",
                "pulse_oximeter": "pulse_ox",
                "hydration": "hydration_dm20",
            }.get(cat, "")
            proto = infer
        if proto in EXCLUDED_PROFILES:
            continue
        if not proto and not include_ocr_only:
            continue
        toolkit = PROFILE_TO_TOOLKIT.get(proto, "beurer_bp")
        names = tuple(raw.get("advertisement_names") or [did])
        out.append(
            BeurerDevice(
                id=did,
                storage_name=str(raw.get("storage_name") or did),
                advertisement_names=names,
                primary_category=str(raw.get("primary_category") or "other"),
                ble_scan_uuid=raw.get("ble_scan_uuid"),
                protocol_profile=proto or "unknown",
                toolkit_profile=toolkit,
                ocr_supported=bool(raw.get("ocr_supported")),
                ordinal=int(raw.get("ordinal") or 0),
                discover=tuple(raw.get("discover") or ()),
            )
        )
    out.sort(key=lambda d: (d.primary_category, d.id))
    return out


def list_categories() -> List[str]:
    cats = sorted({d.primary_category for d in list_devices()})
    # Prefer known order
    order = [
        "blood_pressure",
        "blood_glucose",
        "thermometer",
        "pulse_oximeter",
        "scale",
        "activity_tracker",
        "ecg",
        "hydration",
        "other",
    ]
    return [c for c in order if c in cats] + [c for c in cats if c not in order]


def devices_in_category(category: str) -> List[BeurerDevice]:
    return [d for d in list_devices() if d.primary_category == category]


def get_device(model_id: str) -> Optional[BeurerDevice]:
    key = (model_id or "").strip().upper()
    for d in list_devices():
        if d.id.upper() == key or d.storage_name.upper() == key:
            return d
        if key in {n.upper() for n in d.advertisement_names}:
            return d
    return None


def match_advertisement_name(name: str) -> Optional[BeurerDevice]:
    """Best-effort match of BLE adv local name to catalog entry."""
    if not name:
        return None
    n = name.strip().upper()
    # Exact adv name first
    for d in list_devices():
        for adv in d.advertisement_names:
            if adv.upper() == n:
                return d
    # Substring (adv in name or name in adv)
    for d in list_devices():
        for adv in d.advertisement_names:
            a = adv.upper()
            if a and (a in n or n in a):
                return d
        if d.id.upper() in n:
            return d
    return None


def profile_meta(protocol_profile: str) -> dict:
    reg = load_registry()
    return (reg.get("profiles") or {}).get(protocol_profile) or {}


def supported_summary() -> str:
    devices = list_devices()
    by_cat: Dict[str, int] = {}
    for d in devices:
        by_cat[d.primary_category] = by_cat.get(d.primary_category, 0) + 1
    parts = [f"{CATEGORY_LABELS.get(c, c)}: {n}" for c, n in sorted(by_cat.items())]
    return f"{len(devices)} Beurer models (OCR excluded) — " + "; ".join(parts)

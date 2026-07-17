"""
Brand / company catalog for the web POC (mirrors interactive CLI menu).
Includes Nipro げんきノート companion-like profiles from medical_ble_toolkit.
"""

from __future__ import annotations

from typing import Any, Dict, List, Optional

# id must match medical_ble_toolkit where possible
BRANDS: List[Dict[str, Any]] = [
    {
        "id": "omron",
        "company": "Omron",
        "label": "Omron (HEM-* BP, EEPROM history)",
        "connect_profile": "omron",
        "default_model": "HEM-7143T1",
        "is_omron": True,
        "supports": ["pair", "repair", "sync", "read"],
        "vital_kind": "bp",
        "notes": (
            "Pair once (flashing P). Sync often works with no button if bond is good; "
            "if FE4A fails, short-press BT then Sync."
        ),
    },
    {
        "id": "beurer",
        "company": "Beurer",
        "label": "Beurer multi-device (BP / glucose / FT / PO60 / scale / ECG)",
        "connect_profile": "beurer_bp",
        "default_model": "BM54",
        "is_omron": False,
        "supports": ["pair", "sync", "connect"],
        "vital_kind": "bp",
        "notes": "Companion-style sync; accept Windows pair dialog if shown.",
    },
    {
        "id": "and",
        "company": "A&D",
        "label": "A&D UA-651BLE full SDK (custom 0xA6/0xE1)",
        "connect_profile": "and_ua651",
        "default_model": "UA-651BLE",
        "is_omron": False,
        "supports": ["pair", "sync", "connect"],
        "vital_kind": "bp",
        "notes": (
            "Full A&D SDK path (buffer + request-all). For Nipro げんきノート meters "
            "prefer NBP-1BLE / NMBP brands below (clock + BLP only)."
        ),
    },
    # --- Nipro げんきノート companion-like ---
    {
        "id": "nipro_nbp",
        "company": "Nipro",
        "label": "Nipro NBP-1BLE (BP, companion)",
        "connect_profile": "nipro_nbp",
        "default_model": "NBP-1BLE",
        "is_omron": False,
        "is_nipro": True,
        "supports": ["pair", "sync", "connect", "handsfree"],
        "vital_kind": "bp",
        "notes": (
            "げんきノート BLEDeviceNBP1: 1s settle → DateTime 0x2A08 → BLP indicate. "
            "Pair once, measure, then Sync (or Hands-free wait)."
        ),
    },
    {
        "id": "nipro_nmbp",
        "company": "Nipro / A&D",
        "label": "Nipro NMBP (BP, bond recommended)",
        "connect_profile": "nipro_nmbp",
        "default_model": "NMBP",
        "is_omron": False,
        "is_nipro": True,
        "supports": ["pair", "sync", "connect", "handsfree"],
        "vital_kind": "bp",
        "notes": (
            "げんきノート BLEDeviceUM212. Prefer Pair (Windows bond) then Sync. "
            "Same BLP clock path as NBP-1BLE."
        ),
    },
    {
        "id": "nipro_nsm1",
        "company": "Nipro",
        "label": "Nipro NSM-1BLE (thermometer HTP)",
        "connect_profile": "nipro_nsm1",
        "default_model": "NSM-1BLE",
        "is_omron": False,
        "is_nipro": True,
        "supports": ["pair", "sync", "connect", "handsfree"],
        "vital_kind": "temp",
        "notes": "Companion: 1s → HTS clock 0x2A08 → Temperature 0x2A1C indicate.",
    },
    {
        "id": "nipro_nt100b",
        "company": "Nipro / TaiDoc",
        "label": "Nipro NT-100B (IR thermometer, companion)",
        "connect_profile": "nipro_nt100b",
        "default_model": "NT-100B",
        "is_omron": False,
        "is_nipro": True,
        "supports": ["pair", "sync", "connect", "handsfree"],
        "vital_kind": "temp",
        "notes": (
            "Measure first (BLE on ~1–2 min), then Sync quickly. Session pulls "
            "latest TICD storage + listens HTP (indication often fires before connect). "
            "Power-off on disconnect. Not a continuous stream."
        ),
    },
    {
        "id": "nipro_cf",
        "company": "Nipro",
        "label": "Nipro CF / Cocoron (glucose)",
        "connect_profile": "nipro_cf",
        "default_model": "NIPRO CF",
        "is_omron": False,
        "is_nipro": True,
        "supports": ["pair", "sync", "connect", "handsfree"],
        "vital_kind": "glucose",
        "notes": (
            "Proprietary CF UUIDs + RACP All. Measure on meter, then Sync. "
            "Diff/last-seq not required for first bring-up."
        ),
    },
    {
        "id": "masimo",
        "company": "Masimo",
        "label": "Masimo MightySat (SpO2 stream)",
        "connect_profile": "mightysat",
        "default_model": "MightySat",
        "is_omron": False,
        "is_nipro": True,  # also in げんきノート BLELib
        "supports": ["pair", "live", "connect", "handsfree"],
        "vital_kind": "spo2",
        "notes": (
            "STREAMING: use Live (not Sync). Companion order GetInfo → SetClock → "
            "EnableStream. Finger in sensor, Live start."
        ),
    },
    {
        "id": "thermo",
        "company": "Nipro / TaiDoc",
        "label": "NT-100B (alias → companion HTP+TICD)",
        "connect_profile": "nipro_nt100b",
        "default_model": "NT-100B",
        "is_omron": False,
        "is_nipro": True,
        "supports": ["pair", "sync", "connect", "handsfree"],
        "vital_kind": "temp",
        "notes": (
            "Same as Nipro NT-100B companion: measure → Sync within ~2 min. "
            "Pulls latest TICD slot + HTP indicate. (Lab full-history: CLI profile thermometer.)"
        ),
    },
    {
        "id": "fora",
        "company": "FORA",
        "label": "FORA 6 Connect (RE scaffold)",
        "connect_profile": "fora6",
        "default_model": "FORA 6 Connect",
        "is_omron": False,
        "supports": ["connect", "scan"],
        "vital_kind": "glucose",
        "notes": "No full wire protocol — hex dump / RE mode.",
    },
    {
        "id": "re",
        "company": "Unknown",
        "label": "Unknown / reverse-engineering (subscribe all)",
        "connect_profile": "re_generic",
        "default_model": "generic",
        "is_omron": False,
        "supports": ["connect", "scan", "live"],
        "vital_kind": "generic",
        "notes": "GATT tree + all notify/indicate + hex dumps.",
    },
]


def get_brand(brand_id: str) -> Optional[Dict[str, Any]]:
    bid = (brand_id or "").strip().lower()
    for b in BRANDS:
        if b["id"] == bid:
            return b
    # aliases
    aliases = {
        "nt100b": "nipro_nt100b",
        "nbp": "nipro_nbp",
        "nmbp": "nipro_nmbp",
        "nsm": "nipro_nsm1",
        "nsm1": "nipro_nsm1",
        "cocoron": "nipro_cf",
        "nipro": "nipro_nbp",
        "thermometer": "thermo",
    }
    bid2 = aliases.get(bid)
    if bid2:
        return get_brand(bid2)
    return None


def list_brands() -> List[Dict[str, Any]]:
    return list(BRANDS)


def resolve_profile_id(brand: Dict[str, Any], model: str = "") -> str:
    """Pick toolkit profile from brand (+ optional model override)."""
    model_l = (model or "").strip().lower()
    # model may be a profile id
    if model_l.startswith("nipro_") or model_l in (
        "mightysat",
        "and_ua651",
        "thermometer",
        "beurer_bp",
    ):
        return model_l.replace("-", "_")
    # name-based inference for Nipro
    if brand.get("is_nipro") or brand.get("id", "").startswith("nipro"):
        try:
            from medical_ble_toolkit.nipro.registry import infer_profile_from_name

            inferred = infer_profile_from_name(model or brand.get("default_model") or "")
            if inferred:
                return inferred
        except Exception:  # noqa: BLE001
            pass
    return brand.get("connect_profile") or "re_generic"

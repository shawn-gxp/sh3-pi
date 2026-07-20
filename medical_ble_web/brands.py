"""
Brand catalog for the web hub UI.

Tier-1 (hub SLA — default list): Omron BP, Nipro NBP-1BLE, NT-100B, MightySat.
Other brands kept under ADVANCED for lab/RE only.
"""

from __future__ import annotations

from typing import Any, Dict, List, Optional

# ---------------------------------------------------------------------------
# Tier-1 — dedicated Pi hub (pair hub-only, auto-hunt)
# ---------------------------------------------------------------------------

TIER1_BRANDS: List[Dict[str, Any]] = [
    {
        "id": "omron",
        "company": "Omron",
        "label": "Omron BP (HEM-7143T1 / FE4A)",
        "connect_profile": "omron",
        "default_model": "HEM-7143T1",
        "is_omron": True,
        "tier1": True,
        "supports": ["pair", "repair", "sync", "read"],
        "vital_kind": "bp",
        "notes": (
            "HUB ONLY bond. Pair once (cuff flashing P). Auto-sync dumps ~30 history "
            "every omron_poll_interval_s (default 5 min — edit hub_config.json)."
        ),
    },
    {
        "id": "nipro_nbp",
        "company": "Nipro",
        "label": "Nipro NBP-1BLE (BP)",
        "connect_profile": "nipro_nbp",
        "default_model": "NBP-1BLE",
        "is_omron": False,
        "is_nipro": True,
        "tier1": True,
        "supports": ["pair", "sync", "connect"],
        "vital_kind": "bp",
        "notes": (
            "HUB ONLY. After measure BLE ~1m05s — hub connects on AD "
            "(1s settle → DateTime 0x2A08 → BLP 0x2A35). Save exact adv name at Pair."
        ),
    },
    {
        "id": "nipro_nt100b",
        "company": "Nipro / TaiDoc",
        "label": "Nipro NT-100B (IR thermometer)",
        "connect_profile": "nipro_nt100b",
        "default_model": "NT-100B",
        "is_omron": False,
        "is_nipro": True,
        "tier1": True,
        "supports": ["pair", "sync", "connect"],
        "vital_kind": "temp",
        "notes": (
            "HUB ONLY. After measure BLE ~1m05s — hub connects on AD "
            "(HTP + TICD pull + power-off). Not continuous stream."
        ),
    },
    {
        "id": "masimo",
        "company": "Masimo",
        "label": "Masimo MightySat (SpO2 live)",
        "connect_profile": "mightysat",
        "default_model": "MightySat",
        "is_omron": False,
        "is_nipro": True,
        "tier1": True,
        "supports": ["pair", "live", "connect"],
        "vital_kind": "spo2",
        "notes": (
            "HUB ONLY. BLE only while measuring — hub starts full live stream on AD "
            "(GetInfo → SetClock → EnableStream). Finger in sensor."
        ),
    },
]

# Lab / non-SLA brands (hidden from default UI unless ?all=1)
ADVANCED_BRANDS: List[Dict[str, Any]] = [
    {
        "id": "nipro_nmbp",
        "company": "Nipro / A&D",
        "label": "[Adv] Nipro NMBP",
        "connect_profile": "nipro_nmbp",
        "default_model": "NMBP",
        "is_omron": False,
        "is_nipro": True,
        "tier1": False,
        "supports": ["pair", "sync"],
        "vital_kind": "bp",
        "notes": "Same BLP path as NBP-1BLE; prefer nipro_nbp for hub.",
    },
    {
        "id": "nipro_nsm1",
        "company": "Nipro",
        "label": "[Adv] NSM-1BLE thermometer",
        "connect_profile": "nipro_nsm1",
        "default_model": "NSM-1BLE",
        "is_omron": False,
        "is_nipro": True,
        "tier1": False,
        "supports": ["pair", "sync"],
        "vital_kind": "temp",
        "notes": "Not NT-100B — different device. Hub SLA uses nipro_nt100b.",
    },
    {
        "id": "beurer",
        "company": "Beurer",
        "label": "[Adv] Beurer multi",
        "connect_profile": "beurer_bp",
        "default_model": "BM54",
        "is_omron": False,
        "tier1": False,
        "supports": ["pair", "sync"],
        "vital_kind": "bp",
        "notes": "Out of hub Tier-1 scope.",
    },
    {
        "id": "and",
        "company": "A&D",
        "label": "[Adv] A&D UA-651BLE full SDK",
        "connect_profile": "and_ua651",
        "default_model": "UA-651BLE",
        "is_omron": False,
        "tier1": False,
        "supports": ["pair", "sync"],
        "vital_kind": "bp",
        "notes": "Full SDK path; NBP-1BLE uses companion BLP only (nipro_nbp).",
    },
    {
        "id": "nipro_cf",
        "company": "Nipro",
        "label": "[Adv] Nipro CF glucose",
        "connect_profile": "nipro_cf",
        "default_model": "NIPRO CF",
        "is_omron": False,
        "is_nipro": True,
        "tier1": False,
        "supports": ["pair", "sync"],
        "vital_kind": "glucose",
        "notes": "Out of hub Tier-1 scope.",
    },
    {
        "id": "fora",
        "company": "FORA",
        "label": "[Adv] FORA 6 RE scaffold",
        "connect_profile": "fora6",
        "default_model": "FORA 6 Connect",
        "is_omron": False,
        "tier1": False,
        "supports": ["scan", "connect"],
        "vital_kind": "glucose",
        "notes": "No full protocol.",
    },
    {
        "id": "re",
        "company": "Unknown",
        "label": "[Adv] RE subscribe-all",
        "connect_profile": "re_generic",
        "default_model": "generic",
        "is_omron": False,
        "tier1": False,
        "supports": ["scan", "connect", "live"],
        "vital_kind": "generic",
        "notes": "Lab only.",
    },
]

# Full catalog (internal)
BRANDS: List[Dict[str, Any]] = list(TIER1_BRANDS) + list(ADVANCED_BRANDS)


# Aliases → canonical brand id (stored in SQLite / hub roster)
_BRAND_ALIASES = {
    "nt100b": "nipro_nt100b",
    "thermo": "nipro_nt100b",
    "thermometer": "nipro_nt100b",
    "nbp": "nipro_nbp",
    "nbp1": "nipro_nbp",
    "nmbp": "nipro_nmbp",
    "nsm": "nipro_nsm1",
    "nsm1": "nipro_nsm1",
    "cocoron": "nipro_cf",
    "nipro": "nipro_nbp",
    "mightysat": "masimo",
    "spo2": "masimo",
    "hem7143t1": "omron",
    "hem-7143t1": "omron",
}


def canonicalize_brand_id(brand_id: str) -> str:
    """Map legacy / short ids to the catalog brand id."""
    bid = (brand_id or "").strip().lower()
    return _BRAND_ALIASES.get(bid, bid)


def get_brand(brand_id: str) -> Optional[Dict[str, Any]]:
    bid = canonicalize_brand_id(brand_id)
    for b in BRANDS:
        if b["id"] == bid:
            return b
    return None


def list_brands(*, include_advanced: bool = False) -> List[Dict[str, Any]]:
    """Default UI: Tier-1 only. Pass include_advanced for lab brands."""
    if include_advanced:
        return list(BRANDS)
    return list(TIER1_BRANDS)


def resolve_profile_id(brand: Dict[str, Any], model: str = "") -> str:
    """Pick toolkit profile from brand (+ optional model override)."""
    model_l = (model or "").strip().lower()
    if model_l.startswith("nipro_") or model_l in (
        "mightysat",
        "and_ua651",
        "thermometer",
        "beurer_bp",
    ):
        return model_l.replace("-", "_")
    # Prefer exact brand profile (do not mis-infer NT-100B as NSM-1)
    bid = (brand.get("id") or "").lower()
    if bid in ("nipro_nt100b", "thermo"):
        return "nipro_nt100b"
    if bid == "nipro_nbp":
        return "nipro_nbp"
    if bid == "masimo":
        return "mightysat"
    if bid == "omron":
        return "omron"
    if brand.get("is_nipro") or bid.startswith("nipro"):
        try:
            from medical_ble_toolkit.nipro.registry import infer_profile_from_name

            name = model or brand.get("default_model") or ""
            inferred = infer_profile_from_name(name)
            if inferred:
                return inferred
        except Exception:  # noqa: BLE001
            pass
    return brand.get("connect_profile") or "re_generic"

"""
Brand / company catalog for the web POC (mirrors interactive CLI menu).
"""

from __future__ import annotations

from typing import Any, Dict, List, Optional

# id must match medical_ble_toolkit.interactive brand ids where possible
BRANDS: List[Dict[str, Any]] = [
    {
        "id": "omron",
        "company": "Omron",
        "label": "Omron (HEM-* BP, EEPROM history)",
        "connect_profile": "omron",
        "default_model": "HEM-7143T1",
        "is_omron": True,
        "supports": ["pair", "repair", "sync", "read"],
        "notes": "Pair once (flashing P). Sync often works with no button if bond is good; if FE4A fails, short-press BT then Sync. History = 30 EEPROM slots (model map).",
    },
    {
        "id": "beurer",
        "company": "Beurer",
        "label": "Beurer multi-device (BP / glucose / FT / PO60 / scale / ECG)",
        "connect_profile": "beurer_bp",
        "default_model": "BM54",
        "is_omron": False,
        "supports": ["pair", "sync", "connect"],
        "notes": "Companion-style sync; accept Windows pair dialog if shown.",
    },
    {
        "id": "and",
        "company": "A&D / Nipro",
        "label": "A&D UA-651BLE (Nipro / SIG BLP)",
        "connect_profile": "and_ua651",
        "default_model": "UA-651BLE",
        "is_omron": False,
        "supports": ["pair", "sync", "connect"],
        "notes": "Bond + Indicate dump. Buffer mode 30; device sends only stored BPs (oldest first), not empty slots. Take several measurements then Sync.",
    },
    {
        "id": "masimo",
        "company": "Masimo",
        "label": "Masimo MightySat (SpO2 stream)",
        "connect_profile": "mightysat",
        "default_model": "MightySat",
        "is_omron": False,
        "supports": ["pair", "live", "connect"],
        "notes": "STREAMING device: use Live (not Sync). Pair once, finger in sensor, Live start — SpO2/PR update ~1 Hz until Live stop.",
    },
    {
        "id": "thermo",
        "company": "TaiDoc / Nipro pack",
        "label": "NT-100B non-contact thermometer",
        "connect_profile": "thermometer",
        "default_model": "NT-100B",
        "is_omron": False,
        "supports": ["pair", "sync", "connect"],
        "notes": "BLE OFF until you take a reading, then ~1–2 min window. Measure → Sync immediately (Pair once only). Not a continuous stream.",
    },
    {
        "id": "fora",
        "company": "FORA",
        "label": "FORA 6 Connect (RE scaffold)",
        "connect_profile": "fora6",
        "default_model": "FORA 6 Connect",
        "is_omron": False,
        "supports": ["connect", "scan"],
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
        "notes": "GATT tree + all notify/indicate + hex dumps.",
    },
]


def get_brand(brand_id: str) -> Optional[Dict[str, Any]]:
    bid = (brand_id or "").strip().lower()
    for b in BRANDS:
        if b["id"] == bid:
            return b
    return None


def list_brands() -> List[Dict[str, Any]]:
    return list(BRANDS)

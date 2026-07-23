"""
Tier-1 device policy: classification + connect priority.

Priority when several paired devices advertise at once:
  1. MightySat  — BLE dies when reading ends (irrecoverable)
  2. NBP-1BLE   — ~65 s window
  3. NT-100B    — ~65 s window
  4. Omron      — recoverable; timer/opportunistic only
"""

from __future__ import annotations

from enum import Enum
from typing import FrozenSet, Optional


# Brands we accept on the hub when tier1_brands_only=true
TIER1_BRANDS: FrozenSet[str] = frozenset(
    {
        "omron",
        "nipro_nbp",
        "nipro_nmbp",
        "nipro_nt100b",
        "thermo",
        "masimo",
        "mightysat",
        "beurer",
        "beurer_bp",
        "beurer_glucose",
        "beurer_thermo",
        "beurer_po60",
    }
)

# Advertisement name substrings (case-insensitive) for opportunistic match
NAME_HINTS = {
    "omron": ("omron", "blesmart", "hem-"),
    "nipro_nbp": ("nbp-1ble", "nbp-1"),
    "nipro_nt100b": ("nt-100b", "nt-100"),
    "thermo": ("nt-100b", "nt-100"),
    "masimo": ("mightysat", "masimo"),
    "beurer": (
        "beurer",
        "bm54",
        "bm",
        "bc",
        "gl50",
        "gl",
        "ft95",
        "ft",
        "po60",
    ),
}

# Manufacturer company IDs in AD
COMPANY_IDS = {
    "omron": (0x020E,),
    "masimo": (0x0243,),
    "beurer": (0x0611,),  # Beurer GmbH
}


class DeviceClass(str, Enum):
    STREAM = "stream"  # MightySat — live while measuring
    WINDOWED = "windowed"  # NBP / NT — AD then short dump
    ALWAYS = "always"  # Omron — scheduled / opportunistic history


def classify_brand(brand_id: str) -> DeviceClass:
    b = (brand_id or "").lower().strip()
    if b == "masimo":
        return DeviceClass.STREAM
    if b == "omron":
        return DeviceClass.ALWAYS
    if b in (
        "nipro_nbp",
        "nipro_nt100b",
        "thermo",
        "nipro_nmbp",
        "nipro_nsm1",
        "beurer",
    ):
        return DeviceClass.WINDOWED
    return DeviceClass.WINDOWED


def is_windowed(brand_id: str) -> bool:
    return classify_brand(brand_id) == DeviceClass.WINDOWED


def is_stream(brand_id: str) -> bool:
    return classify_brand(brand_id) == DeviceClass.STREAM


def is_always_on(brand_id: str) -> bool:
    return classify_brand(brand_id) == DeviceClass.ALWAYS


def priority_rank(brand_id: str) -> int:
    """Lower = more urgent."""
    b = (brand_id or "").lower().strip()
    order = {
        "masimo": 0,
        "nipro_nbp": 1,
        "nipro_nt100b": 2,
        "thermo": 2,
        "beurer": 3,  # windowed companion after measure
        "omron": 10,
    }
    return order.get(b, 50)


def brand_matches_adv(
    brand_id: str,
    *,
    name: str = "",
    mfg_ids: Optional[list] = None,
) -> bool:
    """True if advertisement looks like this brand (secondary to MAC match)."""
    b = (brand_id or "").lower()
    name_l = (name or "").lower()
    mfg = mfg_ids or []
    for cid in COMPANY_IDS.get(b, ()):
        if cid in mfg:
            return True
    for hint in NAME_HINTS.get(b, ()):
        if hint in name_l:
            return True
    return False


HUB_ONLY_PAIR_TIPS = [
    "Pair ONLY with this hub — unpair the phone companion first.",
    "One host bond only (Omron / Nipro / Beurer / MightySat).",
    "After Pair: leave Auto-sync ON; measure on the device; hub captures automatically.",
]

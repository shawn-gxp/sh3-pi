"""
Brand catalog for the web hub UI.

This is the single authoritative source for the supported device list.
Each entry maps the UI selection directly to the canonical profile_id.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Dict, List, Optional


@dataclass(frozen=True)
class SupportedDevice:
    profile_id: str      # exact key into PROFILES catalog (and what UI sends as "id")
    plugin_brand: str    # exact key into plugin registry (who handles the connection)
    display_brand: str   # UI label (e.g. "Nipro", "Masimo")
    display_label: str   # full human name
    vital_kind: str      # "bp" | "spo2" | "temp" | "glucose"
    tier1: bool          # appears in default UI
    default_model: str
    supports: List[str]
    notes: str

    def to_dict(self) -> Dict[str, Any]:
        """Convert to the legacy dictionary format expected by the frontend UI."""
        return {
            "id": self.profile_id,
            "company": self.display_brand,
            "label": self.display_label,
            "connect_profile": self.profile_id,
            "default_model": self.default_model,
            "is_omron": self.plugin_brand == "omron",
            "is_nipro": self.plugin_brand == "nipro",
            "is_beurer": self.plugin_brand == "beurer",
            "is_masimo": self.plugin_brand == "masimo",
            "plugin_brand": self.plugin_brand,
            "tier1": self.tier1,
            "supports": self.supports,
            "vital_kind": self.vital_kind,
            "notes": self.notes,
        }


TIER1_BRANDS: List[SupportedDevice] = [
    SupportedDevice(
        profile_id="omron",
        plugin_brand="omron",
        display_brand="Omron",
        display_label="Omron BP (HEM-7143T1 / FE4A)",
        vital_kind="bp",
        tier1=True,
        default_model="HEM-7143T1",
        supports=["pair", "repair", "sync", "read"],
        notes="HUB ONLY bond. Pair once (cuff flashing P). Auto-sync dumps ~30 history every omron_poll_interval_s (default 5 min).",
    ),
    SupportedDevice(
        profile_id="nipro_nbp",
        plugin_brand="nipro",
        display_brand="Nipro",
        display_label="Nipro NBP-1BLE (BP)",
        vital_kind="bp",
        tier1=True,
        default_model="NBP-1BLE",
        supports=["pair", "sync", "connect"],
        notes="HUB ONLY. After measure BLE ~1m05s — hub connects on AD. Save exact adv name at Pair.",
    ),
    SupportedDevice(
        profile_id="nipro_nt100b",
        plugin_brand="thermo",
        display_brand="Nipro / TaiDoc",
        display_label="Nipro NT-100B (IR thermometer)",
        vital_kind="temp",
        tier1=True,
        default_model="NT-100B",
        supports=["pair", "sync", "connect"],
        notes="HUB ONLY. After measure BLE ~1m05s — hub connects on AD. Not continuous stream.",
    ),
    SupportedDevice(
        profile_id="mightysat",
        plugin_brand="masimo",
        display_brand="Masimo",
        display_label="Masimo MightySat (SpO2 live)",
        vital_kind="spo2",
        tier1=True,
        default_model="MightySat",
        supports=["pair", "live", "connect"],
        notes="HUB ONLY. BLE only while measuring — hub starts full live SpO2 stream on AD. Finger in sensor.",
    ),
    # Beurer companion (SIG BLP / glucose / FT / PO60)
    SupportedDevice(
        profile_id="beurer_bp",
        plugin_brand="beurer",
        display_brand="Beurer",
        display_label="Beurer BP (BM / BC / BM54)",
        vital_kind="bp",
        tier1=True,
        default_model="BM54",
        supports=["pair", "sync", "connect"],
        notes=(
            "HUB ONLY bond. Pair once with cuff awake; hub dumps history on AD "
            "(SIG BLP 0x1810). Default model BM54 — change if your SKU differs."
        ),
    ),
    SupportedDevice(
        profile_id="beurer_glucose",
        plugin_brand="beurer",
        display_brand="Beurer",
        display_label="Beurer Glucose (GL*)",
        vital_kind="glucose",
        tier1=True,
        default_model="GL50",
        supports=["pair", "sync", "connect"],
        notes="HUB ONLY. After strip measurement BLE advertises — hub pulls RACP history.",
    ),
    SupportedDevice(
        profile_id="beurer_thermo",
        plugin_brand="beurer",
        display_brand="Beurer",
        display_label="Beurer Thermometer (FT*)",
        vital_kind="temp",
        tier1=True,
        default_model="FT95",
        supports=["pair", "sync", "connect"],
        notes="HUB ONLY. Measure first; short BLE window then hub captures temp.",
    ),
    SupportedDevice(
        profile_id="beurer_po60",
        plugin_brand="beurer",
        display_brand="Beurer",
        display_label="Beurer Pulse Oximeter (PO60)",
        vital_kind="spo2",
        tier1=True,
        default_model="PO60",
        supports=["pair", "sync", "connect"],
        notes="HUB ONLY. After SpO2 measure, hub pulls stored records.",
    ),
]

ADVANCED_BRANDS: List[SupportedDevice] = [
    SupportedDevice(
        profile_id="nipro_nmbp",
        plugin_brand="nipro",
        display_brand="Nipro / A&D",
        display_label="[Adv] Nipro NMBP",
        vital_kind="bp",
        tier1=False,
        default_model="NMBP",
        supports=["pair", "sync"],
        notes="Same BLP path as NBP-1BLE; prefer nipro_nbp for hub.",
    ),
    SupportedDevice(
        profile_id="nipro_nsm1",
        plugin_brand="nipro",
        display_brand="Nipro",
        display_label="[Adv] NSM-1BLE thermometer",
        vital_kind="temp",
        tier1=False,
        default_model="NSM-1BLE",
        supports=["pair", "sync"],
        notes="Not NT-100B — different device. Hub SLA uses nipro_nt100b.",
    ),
    SupportedDevice(
        profile_id="beurer_bm54",
        plugin_brand="beurer",
        display_brand="Beurer",
        display_label="[Adv] Beurer BM54 (lab alias)",
        vital_kind="bp",
        tier1=False,
        default_model="BM54",
        supports=["pair", "sync"],
        notes="Alias of beurer_bp — same companion session.",
    ),
    SupportedDevice(
        profile_id="beurer_scale",
        plugin_brand="beurer",
        display_brand="Beurer",
        display_label="[Adv] Beurer Scale (BF*)",
        vital_kind="weight",
        tier1=False,
        default_model="BF700",
        supports=["pair", "sync"],
        notes="Scale companion path.",
    ),
    SupportedDevice(
        profile_id="beurer_ecg",
        plugin_brand="beurer",
        display_brand="Beurer",
        display_label="[Adv] Beurer ECG (ME* / BM93+)",
        vital_kind="bp",
        tier1=False,
        default_model="BM96",
        supports=["pair", "sync"],
        notes="ECG + BP combo companion path.",
    ),
    SupportedDevice(
        profile_id="and_ua651",
        plugin_brand="and",
        display_brand="A&D",
        display_label="[Adv] A&D UA-651BLE full SDK",
        vital_kind="bp",
        tier1=False,
        default_model="UA-651BLE",
        supports=["pair", "sync"],
        notes="Full SDK path; NBP-1BLE uses companion BLP only (nipro_nbp).",
    ),
    SupportedDevice(
        profile_id="nipro_cf",
        plugin_brand="nipro",
        display_brand="Nipro",
        display_label="[Adv] Nipro CF glucose",
        vital_kind="glucose",
        tier1=False,
        default_model="NIPRO CF",
        supports=["pair", "sync"],
        notes="Out of hub Tier-1 scope.",
    ),
    SupportedDevice(
        profile_id="fora6",
        plugin_brand="fora",
        display_brand="FORA",
        display_label="[Adv] FORA 6 RE scaffold",
        vital_kind="glucose",
        tier1=False,
        default_model="FORA 6 Connect",
        supports=["scan", "connect"],
        notes="No full protocol.",
    ),
    SupportedDevice(
        profile_id="re_generic",
        plugin_brand="re",
        display_brand="Unknown",
        display_label="[Adv] RE subscribe-all",
        vital_kind="generic",
        tier1=False,
        default_model="generic",
        supports=["scan", "connect", "live"],
        notes="Lab only.",
    ),
]


BRANDS: List[SupportedDevice] = list(TIER1_BRANDS) + list(ADVANCED_BRANDS)


# UI / legacy aliases → canonical profile_id
_PROFILE_ALIASES = {
    "nipro_nt": "nipro_nt100b",
    "nt100b": "nipro_nt100b",
    "nt-100b": "nipro_nt100b",
    "masimo": "mightysat",
    "mighty_sat": "mightysat",
    "beurer": "beurer_bp",
    "bm54": "beurer_bp",
    "beurer_bm": "beurer_bp",
    "beurer_gl": "beurer_glucose",
    "beurer_ft": "beurer_thermo",
    "po60": "beurer_po60",
}


def get_brand(profile_id: str) -> Optional[Dict[str, Any]]:
    """Get the dictionary representation for a profile_id."""
    pid = (profile_id or "").strip().lower()
    pid = _PROFILE_ALIASES.get(pid, pid)
    for b in BRANDS:
        if b.profile_id == pid:
            return b.to_dict()
    return None


def list_brands(*, include_advanced: bool = False) -> List[Dict[str, Any]]:
    """Default UI: Tier-1 only. Pass include_advanced for lab brands."""
    if include_advanced:
        return [b.to_dict() for b in BRANDS]
    return [b.to_dict() for b in TIER1_BRANDS]

"""
Local pair registry for Nipro companion-like CheckPairing.

Companion stores:
  Id = Guid.ToString("N")  (no dashes)
  Name = exact advertised name
  SerialNumber, UserNo, ColorCode

On Windows, BLE "Id" is typically the MAC — we normalize by stripping
separators so CheckPairing matches ads the same way.
"""

from __future__ import annotations

import json
import logging
from dataclasses import asdict, dataclass, field
from pathlib import Path
from typing import Dict, List, Optional

from ..parsers.nipro_common import (
    PREFIX_CF,
    PREFIX_MIGHTY,
    PREFIX_NBCM,
    PREFIX_NBP,
    PREFIX_NMBP,
    PREFIX_NSM,
    PREFIX_NT100B,
)

log = logging.getLogger("medical_ble.nipro.registry")

STORE_NAME = "nipro_paired_devices.json"

# category → default profile id
CATEGORY_PROFILE = {
    "bp": "nipro_nbp",
    "ht": "nipro_nt100b",
    "gl": "nipro_cf",
    "bc": "nipro_nbcm",  # future
    "spo2": "mightysat",
}

PROFILE_CATEGORY = {
    "nipro_nbp": "bp",
    "nipro_nmbp": "bp",
    "and_ua651": "bp",
    "nipro_nsm1": "ht",
    "nipro_nt100b": "ht",
    "thermometer": "ht",
    "nipro_cf": "gl",
    "mightysat": "spo2",
}


@dataclass
class PairedMeter:
    """One paired meter entry (companion MeterSetting + extras)."""

    id_nodash: str
    name: str
    serial: str = ""
    category: str = "bp"
    profile_id: str = "nipro_nbp"
    address: str = ""
    user_no: str = ""
    color_code: str = "0"

    def matches_id(self, device_id: str) -> bool:
        return normalize_device_id(device_id) == normalize_device_id(self.id_nodash)

    def matches_name_exact(self, adv_name: str) -> bool:
        return (adv_name or "").strip() == (self.name or "").strip()


def normalize_device_id(device_id: str) -> str:
    """Companion: id.Replace(\"-\", \"\"). Also strip ':' for Windows MACs."""
    return (device_id or "").replace("-", "").replace(":", "").replace(" ", "").lower()


def _path() -> Path:
    return Path.cwd() / STORE_NAME


def load_registry() -> Dict[str, list]:
    p = _path()
    if not p.is_file():
        return {"meters": []}
    try:
        data = json.loads(p.read_text(encoding="utf-8"))
        if not isinstance(data, dict):
            return {"meters": []}
        data.setdefault("meters", [])
        return data
    except (OSError, json.JSONDecodeError) as exc:
        log.warning("Could not read %s: %s", p, exc)
        return {"meters": []}


def save_registry(data: Dict[str, list]) -> None:
    p = _path()
    try:
        p.write_text(json.dumps(data, indent=2), encoding="utf-8")
        log.info("[NIPRO] registry saved → %s (%d meter(s))", p, len(data.get("meters") or []))
    except OSError as exc:
        log.warning("Could not write %s: %s", p, exc)


def list_meters() -> List[PairedMeter]:
    raw = load_registry().get("meters") or []
    out: List[PairedMeter] = []
    for m in raw:
        if not isinstance(m, dict):
            continue
        try:
            out.append(
                PairedMeter(
                    id_nodash=normalize_device_id(m.get("id_nodash") or m.get("id") or ""),
                    name=(m.get("name") or "").strip(),
                    serial=str(m.get("serial") or m.get("SerialNumber") or ""),
                    category=str(m.get("category") or "bp"),
                    profile_id=str(m.get("profile_id") or "nipro_nbp"),
                    address=str(m.get("address") or ""),
                    user_no=str(m.get("user_no") or m.get("UserNo") or ""),
                    color_code=str(m.get("color_code") or m.get("ColorCode") or "0"),
                )
            )
        except (TypeError, ValueError):
            continue
    return [x for x in out if x.id_nodash and x.name]


def check_pairing(device_id: str) -> bool:
    """
    Companion MeterContext.CheckPairing:
      any category setting.Id equals deviceId without dashes.
    """
    nid = normalize_device_id(device_id)
    if not nid:
        return False
    return any(m.matches_id(nid) for m in list_meters())


def find_by_id(device_id: str) -> Optional[PairedMeter]:
    nid = normalize_device_id(device_id)
    for m in list_meters():
        if m.matches_id(nid):
            return m
    return None


def find_by_name(name: str) -> Optional[PairedMeter]:
    n = (name or "").strip()
    for m in list_meters():
        if m.matches_name_exact(n):
            return m
    return None


def register_meter(
    *,
    device_id: str,
    name: str,
    profile_id: str,
    serial: str = "",
    address: str = "",
    user_no: str = "",
    color_code: str = "0",
    category: Optional[str] = None,
) -> PairedMeter:
    """
    Upsert one meter. Companion overwrites HT/BP/BC per category file;
    we keep one entry per category (replace) and also match by id.
    """
    cat = category or PROFILE_CATEGORY.get(profile_id, "bp")
    meter = PairedMeter(
        id_nodash=normalize_device_id(device_id),
        name=(name or "").strip(),
        serial=serial or "",
        category=cat,
        profile_id=profile_id,
        address=address or device_id,
        user_no=user_no or "",
        color_code=color_code or "0",
    )
    if not meter.id_nodash or not meter.name:
        raise ValueError("register_meter requires device_id and name")

    data = load_registry()
    meters = data.get("meters") or []
    new_list = []
    replaced = False
    for m in meters:
        mid = normalize_device_id(m.get("id_nodash") or m.get("id") or "")
        mcat = m.get("category") or ""
        # replace same id or same category (one primary per category like companion)
        if mid == meter.id_nodash or mcat == meter.category:
            if not replaced:
                new_list.append(asdict(meter))
                replaced = True
            continue
        new_list.append(m)
    if not replaced:
        new_list.append(asdict(meter))
    data["meters"] = new_list
    save_registry(data)
    log.info(
        "[NIPRO] paired %s category=%s profile=%s id=%s serial=%s",
        meter.name,
        meter.category,
        meter.profile_id,
        meter.id_nodash,
        meter.serial or "(none)",
    )
    return meter


def delete_meter(
    *,
    device_id: Optional[str] = None,
    category: Optional[str] = None,
    name: Optional[str] = None,
) -> int:
    """Remove matching meters. Returns count removed."""
    data = load_registry()
    meters = data.get("meters") or []
    nid = normalize_device_id(device_id or "")
    nname = (name or "").strip()
    kept = []
    removed = 0
    for m in meters:
        mid = normalize_device_id(m.get("id_nodash") or m.get("id") or "")
        mcat = m.get("category") or ""
        mname = (m.get("name") or "").strip()
        hit = False
        if nid and mid == nid:
            hit = True
        if category and mcat == category:
            hit = True
        if nname and mname == nname:
            hit = True
        if hit:
            removed += 1
            continue
        kept.append(m)
    data["meters"] = kept
    save_registry(data)
    return removed


def infer_profile_from_name(adv_name: str) -> Optional[str]:
    """Map advertised name → companion profile id."""
    n = (adv_name or "").strip()
    if n.startswith(PREFIX_NBP) or PREFIX_NBP in n:
        return "nipro_nbp"
    if n.startswith(PREFIX_NMBP) or PREFIX_NMBP in n:
        return "nipro_nmbp"
    if n.startswith(PREFIX_NSM) or PREFIX_NSM in n:
        return "nipro_nsm1"
    if n.startswith(PREFIX_NT100B) or PREFIX_NT100B in n:
        return "nipro_nt100b"
    if n.startswith(PREFIX_CF) or PREFIX_CF in n:
        return "nipro_cf"
    if PREFIX_MIGHTY in n or n.startswith("Mighty"):
        return "mightysat"
    if n.startswith(PREFIX_NBCM) or PREFIX_NBCM in n:
        return None  # not implemented yet
    return None


def handsfree_name_list(categories: Optional[List[str]] = None) -> List[str]:
    """
    Exact names for ReceiveWait filter.
    Companion home: GL + HT + BP only (not NBCM).
    Default: all registered non-BC.
    """
    allow = set(categories) if categories else {"bp", "ht", "gl", "spo2"}
    names = []
    for m in list_meters():
        if m.category in allow and m.name:
            names.append(m.name.strip())
    return names

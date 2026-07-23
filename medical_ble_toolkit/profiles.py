"""
Static device profiles: scan filters, UUIDs, preferred parser.

Kotlin: data class DeviceProfile(...) + object ProfileCatalog
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import List, Optional, Sequence
from .models import DeviceProfile





# ---------------------------------------------------------------------------
# Catalog — derived from datasheets/
# ---------------------------------------------------------------------------

PROFILES: dict[str, DeviceProfile] = {
    "and_ua651": DeviceProfile(
        id="and_ua651",
        brand="and",
        model="UA-651BLE",
        parser_key="and_ua651",
        name_hints=("UA-651", "A&D", "651BLE"),
        service_uuid="00001810-0000-1000-8000-00805f9b34fb",
        notify_uuids=("00002a35-0000-1000-8000-00805f9b34fb",),
        write_uuid="233bf001-5a34-1b6d-975c-000d5690abe4",  # A&D custom char
        notes=(
            "Full A&D SDK path: BLP + custom 0xA6/0x01/0xE1. For Nipro げんきノート "
            "meters use nipro_nbp / nipro_nmbp (companion-simple BLP+0x2A08 only)."
        ),
    ),
    "mightysat": DeviceProfile(
        id="mightysat",
        brand="masimo",
        model="MightySat",
        parser_key="mightysat",
        name_hints=("MightySat", "Masimo"),
        company_ids=(0x0243,),  # Masimo
        service_uuid="54c21000-a720-4b4f-11e4-9fe20002a5d5",
        notify_uuids=("54c21002-a720-4b4f-11e4-9fe20002a5d5",),  # TX
        write_uuid="54c21001-a720-4b4f-11e4-9fe20002a5d5",       # RX
        notes=(
            "Companion order: notify → GetInfo → SetClock(.NET ticks) → EnableStream "
            "from device-info bytes. SOM=0x77 framed protocol."
        ),
    ),
    "thermometer": DeviceProfile(
        id="thermometer",
        brand="thermo",
        model="NT-100B TICD history",
        parser_key="thermometer",
        name_hints=("TICD",),
        service_uuid="00001523-1212-efde-1523-785feabcd123",
        notify_uuids=(
            "00001524-1212-efde-1523-785feabcd123",
            "00002a1c-0000-1000-8000-00805f9b34fb",
        ),
        write_uuid="00001524-1212-efde-1523-785feabcd123",
        subscribe_all_notifiable=False,
        notes=(
            "Lab/TICD history poll (0x2B/0x25/0x26). For companion-like NT-100B sync "
            "use profile nipro_nt100b (HTP + power-off)."
        ),
    ),

    # Generic RE profile: connect by address, dump everything
    "re_generic": DeviceProfile(
        id="re_generic",
        brand="unknown",
        model="generic",
        parser_key="blp",  # attempt BLP; auto-dispatch also available
        name_hints=(),
        subscribe_all_notifiable=True,
        notes="Blind reverse-engineering: GATT tree + subscribe-all + hex dumps.",
    ),
}



from .brands.omron.profiles import OMRON_PROFILES
PROFILES.update(OMRON_PROFILES)
from .brands.beurer.profiles import BEURER_PROFILES
PROFILES.update(BEURER_PROFILES)
from .brands.nipro.profiles import NIPRO_PROFILES
PROFILES.update(NIPRO_PROFILES)
from .brands.fora.profiles import FORA_PROFILES
PROFILES.update(FORA_PROFILES)

def list_profiles() -> List[DeviceProfile]:
    return list(PROFILES.values())


def get_profile(profile_id: str) -> DeviceProfile:
    key = profile_id.strip().lower()
    if key not in PROFILES:
        known = ", ".join(sorted(PROFILES))
        raise KeyError(f"Unknown profile '{profile_id}'. Known: {known}")
    return PROFILES[key]

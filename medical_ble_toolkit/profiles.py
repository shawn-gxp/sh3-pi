"""
Static device profiles: scan filters, UUIDs, preferred parser.

Kotlin: data class DeviceProfile(...) + object ProfileCatalog
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import List, Optional, Sequence


@dataclass(frozen=True)
class DeviceProfile:
    id: str
    brand: str
    model: str
    parser_key: str
    # Name substrings for scan filter (case-insensitive)
    name_hints: Sequence[str] = ()
    # Manufacturer company IDs (Bluetooth SIG assigned numbers)
    company_ids: Sequence[int] = ()
    # Primary service UUID to subscribe after connect
    service_uuid: Optional[str] = None
    # Characteristics to enable notify/indicate (full or short form)
    notify_uuids: Sequence[str] = ()
    # Optional write char for command-driven devices
    write_uuid: Optional[str] = None
    notes: str = ""
    # If True, subscribe to ALL notify/indicate chars (RE mode)
    subscribe_all_notifiable: bool = False


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
    "fora6": DeviceProfile(
        id="fora6",
        brand="fora",
        model="FORA 6 Connect",
        parser_key="fora",
        name_hints=("FORA", "FORA6", "iFORA"),
        notify_uuids=(),
        subscribe_all_notifiable=True,
        notes=(
            "NO protocol datasheet — RE mode. Subscribe to all Notify/Indicate "
            "chars and map hex dumps after strip tests."
        ),
    ),
    # Omron — proprietary EEPROM (use CLI: omron pair / omron read)
    "omron": DeviceProfile(
        id="omron",
        brand="omron",
        model="HEM-* (catalog)",
        parser_key="omron",
        name_hints=("Omron", "BLESmart", "HEM-"),
        company_ids=(0x020E,),  # Omron company ID
        service_uuid="0000fe4a-0000-1000-8000-00805f9b34fb",  # modern FE4A
        notify_uuids=("49123040-aee8-11e1-a74d-0002a5d5c51b",),
        write_uuid="db5b55e0-aee7-11e1-965e-0002a5d5c51b",
        notes=(
            "NOT SIG BLP history. Use: python -m medical_ble_toolkit omron pair|read "
            "-d HEM-7143T1 -a <MAC>. Backed by medical_ble_toolkit.omron_bp (23 models)."
        ),
    ),
    "hem7143t1": DeviceProfile(
        id="hem7143t1",
        brand="omron",
        model="HEM-7143T1",
        parser_key="hem-7143t1",
        name_hints=("HEM-7143", "BLESmart", "Omron"),
        company_ids=(0x020E,),
        service_uuid="0000fe4a-0000-1000-8000-00805f9b34fb",
        notify_uuids=("49123040-aee8-11e1-a74d-0002a5d5c51b",),
        write_uuid="db5b55e0-aee7-11e1-965e-0002a5d5c51b",
        notes="Lab cuff. Pair with flashing P; read with short-press BT transfer mode.",
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

def list_profiles() -> List[DeviceProfile]:
    return list(PROFILES.values())


def get_profile(profile_id: str) -> DeviceProfile:
    key = profile_id.strip().lower().replace("-", "_").replace(" ", "_")
    aliases = {
        "bm54": "beurer_bm54",
        "beurer": "beurer_bp",
        "beurer_bm54": "beurer_bm54",
        "bp_sig": "beurer_bp",
        "glucose": "beurer_glucose",
        "glucose_sig": "beurer_glucose",
        "beurer_ft": "beurer_thermo",
        "thermometer_sig": "beurer_thermo",
        "po60": "beurer_po60",
        "pulse_ox": "beurer_po60",
        "scale": "beurer_scale",
        "scale_mixed": "beurer_scale",
        "ecg": "beurer_ecg",
        "ecg_custom": "beurer_ecg",
        "as87": "beurer_as87",
        "as98": "beurer_as98",
        "as99": "beurer_as99",
        "ua651": "and_ua651",
        "ua_651ble": "and_ua651",
        "ua651ble": "and_ua651",
        "masimo": "mightysat",
        "spo2": "mightysat",
        "nt100b": "nipro_nt100b",
        "thermo": "nipro_nt100b",
        "nipro_thermo": "nipro_nt100b",
        "nbp": "nipro_nbp",
        "nbp1": "nipro_nbp",
        "nbp_1ble": "nipro_nbp",
        "nmbp": "nipro_nmbp",
        "nsm": "nipro_nsm1",
        "nsm1": "nipro_nsm1",
        "nsm_1ble": "nipro_nsm1",
        "nipro_cf": "nipro_cf",
        "cocoron": "nipro_cf",
        "niprocf": "nipro_cf",
        "ticd": "thermometer",
        "thermometer_ticd": "thermometer",
        "fora": "fora6",
        "hem-7143t1": "hem7143t1",
        "hem_7143t1": "hem7143t1",
        "generic": "re_generic",
        "re": "re_generic",
    }
    key = aliases.get(key, key)
    if key not in PROFILES:
        known = ", ".join(sorted(PROFILES))
        raise KeyError(f"Unknown profile '{profile_id}'. Known: {known}")
    return PROFILES[key]

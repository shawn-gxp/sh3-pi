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
    # --- Beurer family (APK catalog; OCR excluded) ---
    "beurer_bp": DeviceProfile(
        id="beurer_bp",
        brand="beurer",
        model="BM* / BC* SIG BLP",
        parser_key="beurer_bp",
        name_hints=(
            "BM",
            "BC",
            "SERIES",
            "ELITE",
            "DELUXE",
            "PREMIUM",
            "SENSE",
            "QUICK",
            "AUTO400",
            "Beurer",
        ),
        company_ids=(0x0611,),
        service_uuid="00001810-0000-1000-8000-00805f9b34fb",
        notify_uuids=("00002a35-0000-1000-8000-00805f9b34fb",),
        notes=(
            "Shared SIG BLP for ~66 BP models. Companion: settle → pair → DIS → "
            "optional 0x2A2B set-time → Indicate → quiet 4s. Use beurer session."
        ),
    ),
    "beurer_bm54": DeviceProfile(
        id="beurer_bm54",
        brand="beurer",
        model="BM54",
        parser_key="beurer_bp",
        name_hints=("BM54", "Beurer"),
        company_ids=(0x0611,),
        service_uuid="00001810-0000-1000-8000-00805f9b34fb",
        notify_uuids=("00002a35-0000-1000-8000-00805f9b34fb",),
        notes="Alias of beurer_bp for BM54 lab default.",
    ),
    "beurer_glucose": DeviceProfile(
        id="beurer_glucose",
        brand="beurer",
        model="GL*",
        parser_key="beurer_glucose",
        name_hints=("GL50", "GL60", "GL44", "GL48", "GL49", "GL40", "GL34", "GL22", "Beurer"),
        service_uuid="00001808-0000-1000-8000-00805f9b34fb",
        notify_uuids=(
            "00002a18-0000-1000-8000-00805f9b34fb",
            "00002a34-0000-1000-8000-00805f9b34fb",
            "00002a52-0000-1000-8000-00805f9b34fb",
        ),
        write_uuid="00002a52-0000-1000-8000-00805f9b34fb",
        notes="Glucose 0x1808 + RACP report-all after CCCD (Gl50SyncRepo timing).",
    ),
    "beurer_thermo": DeviceProfile(
        id="beurer_thermo",
        brand="beurer",
        model="FT*",
        parser_key="beurer_thermo",
        name_hints=("FT", "Beurer"),
        service_uuid="00001809-0000-1000-8000-00805f9b34fb",
        notify_uuids=("00002a1c-0000-1000-8000-00805f9b34fb",),
        notes="FT* 13-byte Temperature Measurement indicate (app length==13).",
    ),
    "beurer_po60": DeviceProfile(
        id="beurer_po60",
        brand="beurer",
        model="PO60",
        parser_key="beurer_po60",
        name_hints=("PO60", "Beurer"),
        service_uuid="0000ff12-0000-1000-8000-00805f9b34fb",
        notify_uuids=("0000ff02-0000-1000-8000-00805f9b34fb",),
        write_uuid="0000ff01-0000-1000-8000-00805f9b34fb",
        notes="PO60 proprietary; notify FF02, request-more 99 01 1A on FF01.",
    ),
    "beurer_scale": DeviceProfile(
        id="beurer_scale",
        brand="beurer",
        model="BF*",
        parser_key="beurer_scale",
        name_hints=("BF", "Beurer"),
        company_ids=(0x0611,),
        service_uuid="0000181d-0000-1000-8000-00805f9b34fb",
        notify_uuids=(
            "00002a9d-0000-1000-8000-00805f9b34fb",
            "00002a9c-0000-1000-8000-00805f9b34fb",
        ),
        write_uuid="0000fff1-0000-1000-8000-00805f9b34fb",
        subscribe_all_notifiable=True,
        notes="Scale mixed SIG+FFF0; BF700 GET_MEASUREMENT 0x41 state machine.",
    ),
    "beurer_as87": DeviceProfile(
        id="beurer_as87",
        brand="beurer",
        model="AS87",
        parser_key="beurer_tracker",
        name_hints=("AS87",),
        service_uuid="d0a2ff00-2996-d38b-e214-86515df5a1df",
        notify_uuids=(
            "7905ff01-b5ce-4e99-a40f-4b1e122d00d0",
            "7905ff02-b5ce-4e99-a40f-4b1e122d00d0",
        ),
        write_uuid="7905ff01-b5ce-4e99-a40f-4b1e122d00d0",
        notes="AS87 tracker UUIDs complete; opcodes partial.",
    ),
    "beurer_as98": DeviceProfile(
        id="beurer_as98",
        brand="beurer",
        model="AS98",
        parser_key="beurer_tracker",
        name_hints=("AS98",),
        service_uuid="0000fff0-0000-1000-8000-00805f9b34fb",
        notify_uuids=(
            "0000fff6-0000-1000-8000-00805f9b34fb",
            "0000fff7-0000-1000-8000-00805f9b34fb",
        ),
        write_uuid="0000fff6-0000-1000-8000-00805f9b34fb",
        notes="AS98 FFF0 tracker.",
    ),
    "beurer_as99": DeviceProfile(
        id="beurer_as99",
        brand="beurer",
        model="AS99",
        parser_key="beurer_tracker",
        name_hints=("AS99",),
        service_uuid="00006006-0000-1000-8000-00805f9b34fb",
        notify_uuids=(
            "00008001-0000-1000-8000-00805f9b34fb",
            "00008002-0000-1000-8000-00805f9b34fb",
        ),
        write_uuid="00008001-0000-1000-8000-00805f9b34fb",
        notes="AS99 tracker (richest command set in app).",
    ),
    "beurer_ecg": DeviceProfile(
        id="beurer_ecg",
        brand="beurer",
        model="BM93/95/96 ME*",
        parser_key="beurer_ecg",
        name_hints=("BM93", "BM95", "BM96", "ME90", "ME95"),
        company_ids=(0x0611,),
        service_uuid="00001810-0000-1000-8000-00805f9b34fb",
        notify_uuids=(
            "00002a35-0000-1000-8000-00805f9b34fb",
            "6e800003-b5a3-f393-e0a9-e50e24dcca9e",
            "6e800004-b5a3-f393-e0a9-e50e24dcca9e",
            "00002a37-0000-1000-8000-00805f9b34fb",
        ),
        write_uuid="6e800002-b5a3-f393-e0a9-e50e24dcca9e",
        notes="ECG combo: set-time + BP 2A35 full parse; ECG 6E80 waveforms partial.",
    ),
    "beurer_tracker_legacy": DeviceProfile(
        id="beurer_tracker_legacy",
        brand="beurer",
        model="AS80/81/97",
        parser_key="beurer_tracker",
        name_hints=("AS80", "AS81", "AS97", "PR102"),
        subscribe_all_notifiable=True,
        notes="Legacy trackers — limited config; subscribe-all RE mode.",
    ),
    "beurer_hydration": DeviceProfile(
        id="beurer_hydration",
        brand="beurer",
        model="DM20",
        parser_key="beurer_tracker",
        name_hints=("DM20",),
        subscribe_all_notifiable=True,
        notes="DM20 hydration — protocol partial; RE subscribe-all.",
    ),
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
    # --- Nipro げんきノート companion-like (BLELib parity) ---
    "nipro_nbp": DeviceProfile(
        id="nipro_nbp",
        brand="nipro",
        model="NBP-1BLE",
        parser_key="nipro_nbp",
        name_hints=("NBP-1BLE", "NBP-1"),
        service_uuid="00001810-0000-1000-8000-00805f9b34fb",
        notify_uuids=("00002a35-0000-1000-8000-00805f9b34fb",),
        write_uuid="00002a08-0000-1000-8000-00805f9b34fb",
        notes=(
            "Companion BLEDeviceNBP1: sleep 1s → write DateTime 0x2A08 → indicate "
            "0x2A35. No A&D custom 0xE1."
        ),
    ),
    "nipro_nmbp": DeviceProfile(
        id="nipro_nmbp",
        brand="nipro",
        model="NMBP",
        parser_key="nipro_nmbp",
        name_hints=("NMBP",),
        service_uuid="00001810-0000-1000-8000-00805f9b34fb",
        notify_uuids=("00002a35-0000-1000-8000-00805f9b34fb",),
        write_uuid="00002a08-0000-1000-8000-00805f9b34fb",
        notes=(
            "Companion BLEDeviceUM212 (NMBP): bond recommended + same BLP clock path "
            "as NBP-1BLE. Prefer --pair on Windows."
        ),
    ),
    "nipro_nsm1": DeviceProfile(
        id="nipro_nsm1",
        brand="nipro",
        model="NSM-1BLE",
        parser_key="nipro_nsm1",
        name_hints=("NSM-1BLE", "NSM-1"),
        service_uuid="00001809-0000-1000-8000-00805f9b34fb",
        notify_uuids=("00002a1c-0000-1000-8000-00805f9b34fb",),
        write_uuid="00002a08-0000-1000-8000-00805f9b34fb",
        notes=(
            "Companion BLEDeviceNSM1: sleep 1s → write HTS DateTime 0x2A08 → "
            "indicate Temperature 0x2A1C. Pair may write custom 02 01 03 disconnect."
        ),
    ),
    "nipro_nt100b": DeviceProfile(
        id="nipro_nt100b",
        brand="nipro",
        model="NT-100B",
        parser_key="nipro_nt100b",
        name_hints=("NT-100B", "NT-100"),
        service_uuid="00001809-0000-1000-8000-00805f9b34fb",
        notify_uuids=(
            "00002a1c-0000-1000-8000-00805f9b34fb",  # HTP (companion primary)
            "00001524-1212-efde-1523-785feabcd123",  # TICD notify (history recovery)
        ),
        write_uuid="00001524-1212-efde-1523-785feabcd123",
        notes=(
            "Post-measure sync: subscribe HTP 0x2A1C + pull latest TICD storage "
            "(0x25/0x26 idx 0) because indication often fires before connect. "
            "Teardown: TICD power-off 0x50."
        ),
    ),
    "nipro_cf": DeviceProfile(
        id="nipro_cf",
        brand="nipro",
        model="NIPRO CF",
        parser_key="nipro_cf",
        name_hints=("NIPRO CF", "NIPROCF", "Cocoron"),
        service_uuid="5d87a4a0-e42d-11e5-beef-0002a5d5c51b",
        notify_uuids=(
            "5d87a4a1-e42d-11e5-beef-0002a5d5c51b",
            "5d87a4a2-e42d-11e5-beef-0002a5d5c51b",
            "5d87a4a3-e42d-11e5-beef-0002a5d5c51b",
        ),
        write_uuid="5d87a4a3-e42d-11e5-beef-0002a5d5c51b",
        notes=(
            "Companion BLEDeviceCFL (Cocoron): proprietary glucose UUIDs, clock on "
            "87F60002, RACP 04 01 / 01 01 (All) or Diff by last seq."
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
            "-d HEM-7143T1 -a <MAC>. Backed by omron_bp (23 models, classic+modern)."
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

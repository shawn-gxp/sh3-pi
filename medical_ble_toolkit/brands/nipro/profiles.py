"""Nipro DeviceProfile entries — split out of profiles.py."""
from __future__ import annotations
from medical_ble_toolkit.models import DeviceProfile

NIPRO_PROFILES: dict[str, DeviceProfile] = {
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
    brand="thermo",
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
    brand="thermo",
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
}

"""Omron DeviceProfile entries — split out of the monolithic profiles.py catalog."""
from __future__ import annotations

from medical_ble_toolkit.profiles import DeviceProfile

OMRON_PROFILES: dict[str, DeviceProfile] = {
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
}

"""FORA DeviceProfile entries — split out of profiles.py."""
from __future__ import annotations
from medical_ble_toolkit.models import DeviceProfile

FORA_PROFILES: dict[str, DeviceProfile] = {
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
),}

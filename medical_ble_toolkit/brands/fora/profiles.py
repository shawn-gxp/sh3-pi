"""FORA DeviceProfile entries — first-party TaiDoc GATT."""
from __future__ import annotations
from medical_ble_toolkit.models import DeviceProfile
from medical_ble_toolkit.brands.fora.protocol import SERVICE_UUID, CHAR_UUID

FORA_PROFILES: dict[str, DeviceProfile] = {
    "fora6": DeviceProfile(
        id="fora6",
        brand="fora",
        model="FORA 6 Connect",
        parser_key="fora",
        name_hints=("FORA", "FORA6", "iFORA", "TNG", "TD-", "Diamond", "SootheNeb"),
        service_uuid=SERVICE_UUID,
        notify_uuids=(CHAR_UUID,),
        write_uuid=CHAR_UUID,
        subscribe_all_notifiable=False,
        notes=(
            "TaiDoc bus over GATT 1523/1524 (iFORA Smart 1.5.9). "
            "See datasheets/FORA/FORA_FIRST_PARTY_PROTOCOL.md"
        ),
    ),
}

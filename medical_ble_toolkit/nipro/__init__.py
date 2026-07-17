"""
Nipro げんきノート companion-like support: pair registry + hands-free wait.

  python -m medical_ble_toolkit nipro list
  python -m medical_ble_toolkit nipro pair -p nipro_nbp -a <MAC>
  python -m medical_ble_toolkit nipro wait
"""

from .registry import (
    PairedMeter,
    check_pairing,
    delete_meter,
    list_meters,
    load_registry,
    normalize_device_id,
    register_meter,
    save_registry,
)
from .handsfree import handsfree_wait
from . import post_measure

__all__ = [
    "PairedMeter",
    "check_pairing",
    "delete_meter",
    "list_meters",
    "load_registry",
    "normalize_device_id",
    "register_meter",
    "save_registry",
    "handsfree_wait",
    "post_measure",
]

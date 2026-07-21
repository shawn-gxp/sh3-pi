"""
Nipro げんきノート companion-like support: pair registry + hands-free wait.

  python -m medical_ble_toolkit nipro list
  python -m medical_ble_toolkit nipro pair -p nipro_nbp -a <MAC>
  python -m medical_ble_toolkit nipro wait
"""

# No eager imports to avoid circular dependencies.
# Callers must import submodules directly (e.g., .registry, .handsfree).

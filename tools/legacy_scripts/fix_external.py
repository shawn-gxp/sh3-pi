import os

def blanket_replace(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace('medical_ble_toolkit.omron_bp', 'medical_ble_toolkit.brands.omron')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

blanket_replace('medical_ble_toolkit/omron_bridge.py')
blanket_replace('medical_ble_toolkit/parsers/omron.py')

# ble_client.py targeted
with open('medical_ble_toolkit/ble_client.py', 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace(
    'from .omron_bp.ble.connection import pair_client as _omron_pair',
    'from .brands.omron.ble.connection import pair_client as _omron_pair'
)
with open('medical_ble_toolkit/ble_client.py', 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed external imports.")

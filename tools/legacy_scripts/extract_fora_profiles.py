import sys

content = open('medical_ble_toolkit/profiles.py', 'r', encoding='utf-8').read()

start_str = '    "fora6": DeviceProfile('
end_str = '    # Omron — proprietary EEPROM (use CLI: omron pair | omron read)'
if end_str not in content:
    end_str = '    # Omron '

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx == -1 or end_idx == -1:
    print(f"Could not find delimiters. Start: {start_idx}, End: {end_idx}")
    sys.exit(1)

fora_entries = content[start_idx:end_idx]
fora_entries = "\n".join([line[4:] if line.startswith("    ") else line for line in fora_entries.split("\n") if line.strip() != ""])

header = """\"\"\"FORA DeviceProfile entries — split out of profiles.py.\"\"\"
from __future__ import annotations
from medical_ble_toolkit.profiles import DeviceProfile

FORA_PROFILES: dict[str, DeviceProfile] = {
"""

footer = "}\n"

with open('medical_ble_toolkit/brands/fora/profiles.py', 'w', encoding='utf-8') as f:
    f.write(header + fora_entries + footer)

new_content = content[:start_idx] + content[end_idx:]

update_str = 'PROFILES.update(NIPRO_PROFILES)\n'
if update_str not in new_content:
    print("Could not find NIPRO_PROFILES update.")
    sys.exit(1)

addition = 'from .brands.fora.profiles import FORA_PROFILES\nPROFILES.update(FORA_PROFILES)\n'
new_content = new_content.replace(update_str, update_str + addition)

with open('medical_ble_toolkit/profiles.py', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("brands/fora/profiles.py created and profiles.py updated.")

import sys

with open('medical_ble_toolkit/profiles.py', 'r', encoding='utf-8') as f:
    content = f.read()

start_str = '    # --- Nipro げんきノート companion-like (BLELib parity) ---'
end_str = '    "mightysat": DeviceProfile('

if start_str not in content or end_str not in content:
    print("Could not find start or end string.")
    sys.exit(1)

start_idx = content.find(start_str)
end_idx = content.find(end_str)

new_content = content[:start_idx] + content[end_idx:]

update_str = 'PROFILES.update(BEURER_PROFILES)\n'
if update_str not in new_content:
    print("Could not find BEURER_PROFILES update.")
    sys.exit(1)

addition = 'from .brands.nipro.profiles import NIPRO_PROFILES\nPROFILES.update(NIPRO_PROFILES)\n'
new_content = new_content.replace(update_str, update_str + addition)

with open('medical_ble_toolkit/profiles.py', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("profiles.py updated for nipro.")

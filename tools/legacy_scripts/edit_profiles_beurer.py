import re
import sys

with open('medical_ble_toolkit/profiles.py', 'r', encoding='utf-8') as f:
    content = f.read()

# We need to remove the Beurer profiles from the PROFILES dict.
# We also need to add the import and update at the end of the file.

# Find the start of the Beurer block
start_str = '    # --- Beurer family (APK catalog; OCR excluded) ---'
end_str = '    "and_ua651": DeviceProfile('

if start_str not in content or end_str not in content:
    print("Could not find start or end string.")
    sys.exit(1)

start_idx = content.find(start_str)
end_idx = content.find(end_str)

new_content = content[:start_idx] + content[end_idx:]

# Now add the import and update at the end of the file.
# We look for:
# PROFILES.update(OMRON_PROFILES)
update_str = 'PROFILES.update(OMRON_PROFILES)\n'
if update_str not in new_content:
    print("Could not find OMRON_PROFILES update.")
    sys.exit(1)

addition = 'from .brands.beurer.profiles import BEURER_PROFILES\nPROFILES.update(BEURER_PROFILES)\n'
new_content = new_content.replace(update_str, update_str + addition)

with open('medical_ble_toolkit/profiles.py', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("profiles.py updated.")

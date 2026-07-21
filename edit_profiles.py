import re

with open('medical_ble_toolkit/profiles.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the omron and hem7143t1 entries
pattern = re.compile(r'    # Omron — proprietary EEPROM.*?},\n', re.DOTALL)
new_content = pattern.sub('', content)

# Also insert the import and update after the PROFILES dict
# The PROFILES dict ends with `}`. Let's find `PROFILES: dict[str, DeviceProfile] = { ... }`
# We can just append to the file before `_OMRON_COMPANY_ID` or before `def list_profiles()`.
# Let's insert before `def list_profiles():`
insertion = """
from .brands.omron.profiles import OMRON_PROFILES
PROFILES.update(OMRON_PROFILES)

"""
new_content = new_content.replace('def list_profiles() -> List[DeviceProfile]:', insertion + 'def list_profiles() -> List[DeviceProfile]:')

with open('medical_ble_toolkit/profiles.py', 'w', encoding='utf-8') as f:
    f.write(new_content)

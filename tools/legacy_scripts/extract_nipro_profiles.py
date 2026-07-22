import subprocess, sys

content = subprocess.check_output(['git', 'show', 'pre-migration-phase2-nipro:medical_ble_toolkit/profiles.py']).decode('utf-8')

start_str = '    # --- Nipro げんきノート companion-like (BLELib parity) ---'
end_str = '    "mightysat": DeviceProfile('

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx == -1 or end_idx == -1:
    print("Could not find delimiters in git show output")
    sys.exit(1)

nipro_entries = content[start_idx:end_idx]
nipro_entries = "\n".join([line[4:] if line.startswith("    ") else line for line in nipro_entries.split("\n") if line.strip() != ""])

header = """\"\"\"Nipro DeviceProfile entries — split out of profiles.py.\"\"\"
from __future__ import annotations
from medical_ble_toolkit.profiles import DeviceProfile

NIPRO_PROFILES: dict[str, DeviceProfile] = {
"""

footer = "}\n"

with open('medical_ble_toolkit/brands/nipro/profiles.py', 'w', encoding='utf-8') as f:
    f.write(header + nipro_entries + "\n" + footer)

print("brands/nipro/profiles.py created via git show.")

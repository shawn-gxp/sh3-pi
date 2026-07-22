import hashlib
import os

with open('migration_verify/omron_post_move.sha256', 'w') as f:
    lines = []
    base_dir = 'medical_ble_toolkit/brands/omron'
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith('.py'):
                path = os.path.join(root, file)
                rel_path = os.path.relpath(path, base_dir).replace('\\', '/')
                with open(path, 'rb') as fb:
                    h = hashlib.sha256(fb.read()).hexdigest()
                lines.append(f"{h}  {rel_path}\n")
    for line in sorted(lines):
        f.write(line)

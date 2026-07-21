import os, re

base_dir = 'medical_ble_toolkit/brands/nipro'
# These are the parent-relative imports that need converting to absolute
# (they currently resolve to medical_ble_toolkit.X but after move resolve to brands.X)
PARENT_RELATIVE = re.compile(r'from \.\.([\w.]+) import')
count = 0
for root, dirs, files in os.walk(base_dir):
    for file in files:
        if not file.endswith('.py'):
            continue
        path = os.path.join(root, file)
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
        # Replace `from ..X import` with `from medical_ble_toolkit.X import`
        new_content = PARENT_RELATIVE.sub(r'from medical_ble_toolkit.\1 import', content)
        if new_content != content:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            count += 1
            print(f"  Fixed: {path}")
print(f"Fixed {count} files.")

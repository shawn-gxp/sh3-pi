import os

base_dir = 'medical_ble_toolkit/brands/omron'
old_str = 'medical_ble_toolkit.omron_bp'
new_str = 'medical_ble_toolkit.brands.omron'
count = 0

for root, dirs, files in os.walk(base_dir):
    for file in files:
        if file.endswith('.py'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            if old_str in content:
                content = content.replace(old_str, new_str)
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)
                count += 1

print(f"Fixed internal imports in {count} files.")

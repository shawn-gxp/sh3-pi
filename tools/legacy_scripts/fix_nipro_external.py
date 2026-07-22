files_to_fix = [
    'medical_ble_web/ble_jobs.py',
    'medical_ble_web/brands.py',
    'medical_ble_web/app.py',
    'medical_ble_toolkit/tests/test_nipro_registry.py',
]
old = 'medical_ble_toolkit.nipro'
new = 'medical_ble_toolkit.brands.nipro'
for path in files_to_fix:
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    if old in content:
        content = content.replace(old, new)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed: {path}")
    else:
        print(f"No changes needed: {path}")

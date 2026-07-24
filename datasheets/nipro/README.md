# Nipro / SH3 pack protocol notes

This folder keeps **distilled findings** and vendor PDFs. Large decompile dumps
(`decompiled_cs/`, `decompiled_jadx_probe/`, `manifest_extract/`) were removed
from the repo after protocols were captured in the docs below.

| File | Content |
|------|---------|
| `EXACT_PROTOCOL.md` / `.json` | げんきノート BLELib command frames & device paths |
| `EXACT_HW_SEQUENCES.md` | Connect / measure / transfer sequences |
| `NIPRO_BLE_DEVICE_MAP.md` | Device ↔ profile map |
| `PARSER_VS_COMPANION_DIFF.md` | Toolkit vs companion differences |
| `FIRST_PARTY_HW_SUPPORT.md` | Hardware support notes |
| **`cocoron_nc1/`** | **Cocoron NC-1BLE ECG** — APK RE (`NC1BLE_FIRST_PARTY_PROTOCOL.md`) |
| `tools_*.py` | Offline extract helpers (need local decompile tree if re-run) |
| `*.pdf` | Vendor protocol / brochure PDFs |

Runtime code lives in `medical_ble_toolkit/` (parsers + hub).

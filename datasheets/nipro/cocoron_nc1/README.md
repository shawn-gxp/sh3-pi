# Cocoron NC-1BLE (APK reverse)

| Item | Path |
|------|------|
| **Canonical protocol** | [`NC1BLE_FIRST_PARTY_PROTOCOL.md`](./NC1BLE_FIRST_PARTY_PROTOCOL.md) |
| Machine constants | [`EXACT_PROTOCOL.json`](./EXACT_PROTOCOL.json) |
| Huffman node table | [`huffman_node_index.json`](./huffman_node_index.json) |
| APK (local) | `Cocoron_1.0.0_APKPure.apk` (or parent folder `Cocoron®_1.0.0_APKPure.apk`) |
| Decompiled sources | `decompiled/sources/jp/co/nipro/Cocoron/` (bulk; optional to keep) |
| Design PDF (behavior) | `../Cocoron通信仕様書.pdf` |

**Package:** `jp.co.nipro.Cocoron` 1.0.0  

**Do not confuse with** げんきノート `NIPRO CF` glucose (`nipro_cf`).

## Runtime (implemented)

| Piece | Path |
|-------|------|
| Pure protocol + Huffman | `medical_ble_toolkit/parsers/nipro_nc1.py` |
| Huffman table (packaged) | `medical_ble_toolkit/parsers/data/nc1_huffman_node_index.json` |
| **Standalone live test** | `tools_test_nc1.py` or `python -m medical_ble_toolkit.brands.nipro.nc1_session` |
| Toolkit profile | `nipro_nc1` in `brands/nipro/profiles.py` |
| Unit tests | `medical_ble_toolkit/tests/test_nipro_nc1.py` |

### Bring-up with physical device

```text
# offline checks
python -m medical_ble_toolkit.brands.nipro.nc1_session selftest

# scan (service UUID filter)
python -m medical_ble_toolkit.brands.nipro.nc1_session scan -t 15

# stream 60s (wear device)
python -m medical_ble_toolkit.brands.nipro.nc1_session run -a <ADDR> -t 60 --csv nc1.csv --raw

# optional free-run / 10s HR period
python -m medical_ble_toolkit.brands.nipro.nc1_session run -a <ADDR> -t 120 --interval 10 --free-run
```

Main hub path (after standalone OK):

```text
python -m medical_ble_toolkit --profile nipro_nc1 -a <ADDR> -t 60
```

## Status

Full GATT + CONFIG/DATETIME + RRT + Huffman ECG decode from companion APK.  
Standalone session + light toolkit profile wired. Validate on hardware first via `nc1_session`.

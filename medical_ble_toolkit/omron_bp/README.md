# omron_bp

CLI foundation for **Omron Bluetooth BP monitors** (multi-model), designed so
pairing, readout, BLE transport, and per-model parsers stay in **separate files**.

## Goals

- One CLI with **Pair** and **Read**
- Clean layers for debugging and later integration (apps, hubs, other brands)
- Explicit **DBG-LOG** markers so verbose debug can be stripped before release
- Start with Omron; add other vendors with the same pattern later

## Documentation

| Doc | For |
|-----|-----|
| [`docs/USER_GUIDE.md`](docs/USER_GUIDE.md) | **How to install, pair, read, and use the CLI** |
| [`docs/PARSE_EXAMPLE_HEM7143T1.md`](docs/PARSE_EXAMPLE_HEM7143T1.md) | **Byte-by-byte parse example from live HEM-7143T1 raw data** |
| [`docs/SPEC_BP_BLE_MULTI_BRAND.md`](docs/SPEC_BP_BLE_MULTI_BRAND.md) | Full rebuild / multi-brand engineering spec |
| `docs/_catalog_export.json` | Machine-readable model catalog |

Console preview and CSV rows are sorted **newest measurement first**.

## Layout (what goes where)

```
omron_bp/
  cli.py                 # menu + argparse only (no protocol)
  logging_config.py      # logging; DBG-LOG convention
  __main__.py            # python -m omron_bp

  ble/
    scanner.py           # find devices
    session.py           # connect / OS pair / disconnect
    transport.py         # Omron TX/RX + EEPROM read/write

  pairing/
    service.py           # pair workflow (OS bond vs unlock key)

  readout/
    service.py           # read workflow (EEPROM → records)

  models/
    base.py              # DeviceProfile contract
    registry.py          # model name → profile
    parsers/             # pure bytes → vitals (no BLE)
    profiles/catalog.py  # all Omron profiles (hass-omron + omblepy)

  export/
    csv_export.py        # userN.csv

  config/
    store.py             # omron_bp_device.json (last MAC/model)
```

| Layer | Owns | Does not own |
|-------|------|----------------|
| `cli` | User choices, paths | BLE bytes |
| `ble` | Radio + Omron framing | CSV, model EEPROM maps |
| `pairing` / `readout` | Workflows | GATT details (calls `ble`) |
| `models` | UUIDs, EEPROM map, parser | bleak |
| `export` | Files | BLE |

## Setup

From the `experiments` folder (parent of `omron_bp`):

```powershell
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"
python -m pip install -r omron_bp\requirements.txt
```

## Usage

Interactive menu:

```powershell
python -m omron_bp
python -m omron_bp -v          # DEBUG + hex dumps
```

Direct commands:

```powershell
python -m omron_bp list-models

# Pair (cuff flashing P)
python -m omron_bp pair -d HEM-7143T1 -m E1:99:7D:27:1C:0A

# Read (transfer mode — press BT once)
python -m omron_bp read -d HEM-7143T1 -m E1:99:7D:27:1C:0A -o .\data
```

Environment: `OMRON_BP_DEBUG=1` forces DEBUG without `-v`.

## Built-in models

**23 canonical profiles**, **~220 regional aliases**, merged from:

| Source | What we took |
|--------|----------------|
| `hass-omron/.../device_catalog.py` | EEPROM maps, modern FE4A UUIDs, equivalent SKUs |
| `omblepy/deviceSpecific/*.py` | Empirically validated parsers (esp. 715x 16-byte bitfields, 6232/7530 year packing, 7377T1 addresses) |

Run `python -m omron_bp list-models` for the live table.

Coverage includes (canonical ids):  
`HEM-7143T1` (lab), `7142T2`, `7150/51/55` (+ modern MW/MW3/K4), `623x`, `632x`, `6401T`, `7136T`, `6161T`, `7320/22`, `7342`, `7361`, `7377T1`, `7380T1`, `7530T`, `7600T`, …

### Add another Omron model

1. Open `models/profiles/catalog.py`
2. `add(**classic_profile(...))` or `add(**modern_profile(...))`
3. Pick a parser from `models/parsers/`
4. List regional SKUs in `aliases=`
5. `python -m omron_bp list-models` to verify

### Debug cleanup before release

Search the tree for:

- `DBG-LOG` — comments marking temporary debug instrumentation
- `logger.debug(` — verbose paths (often safe to leave gated behind DEBUG level)
- Tag in messages: `[DBG]`

You can keep `logger.debug` permanently if level stays INFO in production; remove only noisy lines you no longer want.

## Relation to existing folders

| Folder | Role |
|--------|------|
| `omblepy/` | Upstream reference CLI (still useful) |
| `hass-omron/` | Home Assistant port — profile source |
| `ble_*.py` | Low-level discovery experiments |
| **`omron_bp/`** | **Our structured app** |

## Cuff modes

| Mode | How | When |
|------|-----|------|
| Pairing | Hold BT 3–5s → flashing **P** | `pair` |
| Transfer | Press BT once | `read` |

Only **one** host bond is usual — unpair phone / OMRON connect if the PC cannot bond.

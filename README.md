# Experiments — Medical BLE (multi-brand) + Omron HEM-7143T1

Tools for **finding, pairing, and reading** smart medical devices over
**Bluetooth Low Energy (BLE)** on **Windows (WinRT)** and **Linux (BlueZ)**.

| Platform | Docs | Launch |
|----------|------|--------|
| **Linux** | [LINUX.md](LINUX.md) | `./setup_linux.sh` → `./run_web.sh` / `./run_toolkit.sh` |
| **Windows** | below + `*.ps1` | `.\run_web.ps1` / `python -m medical_ble_toolkit` |

**Multi-brand toolkit:** `medical_ble_toolkit/` (Omron, Beurer, Nipro, A&D, Masimo, …)  
**Web UI:** `medical_ble_web/` → http://127.0.0.1:8741  
**Omron package:** `omron_bp/`

## Linux (quick start)

```bash
./setup_linux.sh
./run_web.sh          # browser: http://127.0.0.1:8741
# or CLI:
./run_toolkit.sh
./run_toolkit.sh omron pair -d HEM-7143T1 -a E1:99:7D:27:1C:0A
```

See **[LINUX.md](LINUX.md)** for BlueZ pairing, `bluetoothctl`, and troubleshooting.

## Recommended path (Omron package): **omron_bp**

Structured multi-model CLI we maintain: `omron_bp/`

```powershell
# Windows
python -m pip install -r omron_bp\requirements.txt
python -m omron_bp              # menu: pair | read
python -m omron_bp pair -d HEM-7143T1 -m E1:99:7D:27:1C:0A
python -m omron_bp read -d HEM-7143T1 -m E1:99:7D:27:1C:0A
```

```bash
# Linux
source .venv/bin/activate
python -m omron_bp pair -d HEM-7143T1 -m E1:99:7D:27:1C:0A
python -m omron_bp read -d HEM-7143T1 -m E1:99:7D:27:1C:0A
```

See `omron_bp/README.md` for layout (ble / pairing / readout / models / export).

**How to use the CLI:** `omron_bp/docs/USER_GUIDE.md`  

**Full rebuild spec (multi-brand ready, AI-usable):**  
`omron_bp/docs/SPEC_BP_BLE_MULTI_BRAND.md`

---

## Reference / legacy path: **omblepy**

Standalone upstream CLI — no Home Assistant required.  
Upstream: https://github.com/userx14/omblepy  

Local copy: `experiments/omblepy/`  
Custom driver for this cuff: `omblepy/deviceSpecific/hem-7143t1.py`  
(EEPROM layout taken from hass-omron’s **HEM-7146T** profile, which lists HEM-7143T1 as equivalent.)

### Setup (once)

```powershell
cd experiments\omblepy
python -m pip install -r requirements.txt
```

### Important: one phone/PC bond only

Omron cuffs usually allow **only one paired host**.  
If the cuff is bonded to **OMRON connect** on a phone, **forget/unpair** it there first.  
Also remove any old Windows Bluetooth pairing for `BLESmart_...`.

### Pair (first time)

1. Cuff: hold Bluetooth button **3–5s** until flashing **`P`** / **`-P-`**.
2. Within the short pairing window, run:

```powershell
cd experiments\omblepy
python omblepy.py -p -d HEM-7143T1 -m E1:99:7D:27:1C:0A
```

3. If Windows shows a pairing dialog, **accept** it.
4. Success looks like OS bonding completed (for this modern-stack driver).  
   The cuff may show **`OK`** or stop flashing `P`.

Omit `-m ...` to scan and pick from a list.

### Read stored measurements

After a BP reading (or press Bluetooth once for transfer/sync), with the cuff awake:

```powershell
cd experiments\omblepy
python omblepy.py -d HEM-7143T1 -m E1:99:7D:27:1C:0A
```

Optional:

| Flag | Purpose |
|------|---------|
| `--loggerDebug` | Hex dump of BLE TX/RX (debug) |
| `-t` | Time sync (writes EEPROM — use carefully) |
| `-n` | New records only (not fully mapped on 7143T1) |

Output: `user1.csv` (and backup files) in the `omblepy` folder.

### Known address on this PC

| Field | Value |
|-------|--------|
| Name | `BLESmart_00000481E1997D271C0A` |
| Address | `E1:99:7D:27:1C:0A` |
| Model | HEM-7143T1 |
| omblepy profile | `HEM-7143T1` (modern FE4A / OS bond) |

---

## Also here: hass-omron

Folder: `experiments/hass-omron/`  
Upstream: https://github.com/eigger/hass-omron  

Home Assistant **custom component** (HACS). Better later if you run HA + Bluetooth proxy.  
For PC-side proof of pairing/readout, **omblepy is simpler**.

---

## Low-level BLE scripts (discovery only)

| File | Purpose |
|------|---------|
| `ble_scan.py` | Continuous BLE scan; highlights Omron-like devices |
| `ble_pair_connect.py` | Connect by address and list GATT services |
| `requirements.txt` | `bleak` |
| `run_scan.ps1` | PowerShell scan helper |

```powershell
cd experiments
python -m pip install -r requirements.txt
python ble_scan.py
python ble_pair_connect.py E1:99:7D:27:1C:0A
```

These only prove discovery/connect; they do **not** download BP records. Use omblepy for that.

---

## Pairing modes on the cuff

| Mode | How | Display |
|------|-----|---------|
| Pairing | Hold Bluetooth button 3–5s | Flashing **P** |
| Transfer/sync | Press Bluetooth button once | Flashing squares |

## Notes for SHHM later

- Android Hub / patient app can eventually reuse the same Omron memory protocol.
- If scan never finds the cuff: BT on, LE enumerator OK, not phone-only bonded, fresh batteries, true `P` mode.
- Windows **Add a device** often never shows this model — expected; use omblepy / bleak scripts.

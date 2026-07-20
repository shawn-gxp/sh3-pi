# Medical BLE Reverse-Engineering Toolkit

Python prototype HAL for proprietary smart medical devices.  
**Parsers are pure logic** so the Android team can port them to Kotlin without touching BLE APIs.

## Standalone package

`medical_ble_toolkit` is the **standalone BLE hardware layer**.  
`medical_ble_web` depends only on this package (no sibling packages).

Bundled internals:

| Path | Role |
|------|------|
| `omron_bp/` | Omron pair / EEPROM transport / 23-model catalog |
| `hub/` + `hub_config.json` | Pi multi-device hub duty-cycle |
| `beurer/` | Beurer companion sessions + capabilities |
| `nipro/` | Nipro pair registry + hands-free |
| `parsers/` | Pure bytes → vitals (Kotlin-portable) |

## Omron (bundled `omron_bp`)

| Layer | Where |
|-------|--------|
| Pair / read / transport / catalog | `medical_ble_toolkit/omron_bp/` |
| Record bytes → `BloodPressureReading` | `parsers/omron.py` |
| Public facade | `omron_bridge.py` (`pair_omron` / `read_omron` / `unpair_omron`) |
| CLI | `python -m medical_ble_toolkit omron …` |

```powershell
# List Omron models
python -m medical_ble_toolkit omron list-models

# Pair once — cuff flashing P
python -m medical_ble_toolkit omron pair -d HEM-7143T1 -a E1:99:7D:27:1C:0A

# Read history — short-press BT (transfer mode, not P)
python -m medical_ble_toolkit omron read -d HEM-7143T1 -a E1:99:7D:27:1C:0A -o .\data

# Optional low-level Omron CLI
python -m medical_ble_toolkit.omron_bp list-models
```

Offline parse of one 14-byte EEPROM slot:

```python
from medical_ble_toolkit.parser import parse_omron_record
r = parse_omron_record(bytes.fromhex("5142511a9216bb1480005a004fb0"), model="HEM-7143T1")
print(r.systolic, r.diastolic, r.pulse_rate, r.measured_at)
```

---

## Datasheet inventory & similarity analysis

| Folder | Device | Protocol class | Reuse |
|--------|--------|----------------|-------|
| `datasheets/beurer/` | **BM54** BP | Bluetooth SIG BLP `0x1810` / `0x2A35` | **Shared BLP parser** |
| `datasheets/nipro/SH3/` UA-651 SDK | **A&D UA-651BLE** BP | Same BLP + A&D custom `0xF000` | **Shared BLP parser** + custom cmds |
| `datasheets/nipro/SH3/` MightySat | **Masimo MightySat** SpO2 | Proprietary framed (`0x77`+CRC) | **Separate** parser |
| `datasheets/nipro/SH3/` TICD Thermo | **NT-100B** thermometer | 8-byte framed serial-over-BLE | **Separate** parser |
| `datasheets/nipro/SH3/` HTP | Health Thermometer Profile | Bluetooth SIG HTP (reference) | Standard (not used by NT-100B frames) |
| `datasheets/FORA/` | **FORA 6 Connect** | Brochure only — no wire protocol | **Scaffold / RE mode** |

### What is reusable

1. **IEEE 11073 SFLOAT** codec — Beurer + A&D BP (and any future SIG medical profile).
2. **BLP Blood Pressure Measurement** binary layout — identical structure on BM54 and UA-651BLE.
3. **Forensic helpers** — hex dumps, ms timestamps, GATT tree (all brands).
4. **Parser interface** — same `parse(bytes) → dataclass` contract for Kotlin.

### What stays separate

| Component | Why |
|-----------|-----|
| MightySat framing + CRC + streaming bitmask | Proprietary Masimo protocol |
| NT-100B 8-byte frames + packed date fields | Proprietary meter serial protocol |
| A&D custom service commands | Vendor memory/time ops (not clinical payload) |
| FORA | No documentation yet |

### Beurer multi-device (APK catalog, OCR excluded)

Interactive brand **Beurer** loads package-local `beurer/device_registry.json` (or synthesizes from `beurer/capabilities.json`)
(~118 models): BP, glucose, FT thermo, PO60, scales, ECG combo, trackers.

Companion-app timing + stability lives in `medical_ble_toolkit/beurer/`:

- `capabilities.json` — APK `mo.*` markers (settle 3s, pulse swap, set-time, mg RACP…)
- `session.py` — connect retry, bond-wait, CCCD order, quiet-end, SyncResult
- `store.py` — glucose last sequence + BP dedup keys (`beurer_sync_state.json`)
- `dedup.py` — filter duplicate BP/glucose records

```powershell
python -m medical_ble_toolkit
# → Beurer → category → model → Connect
# Or: from medical_ble_toolkit.beurer import run_beurer_sync
#      await run_beurer_sync("AA:BB:…", model_id="BM54")
```

### Datasheet implementation status (offline-complete)

| Brand | Parser | Host sequence (BLE client) | Remaining without hardware |
|-------|--------|----------------------------|----------------------------|
| **Beurer multi** | BLP + glucose + FT13 + PO60 + scale cmds + ECG BP | Companion session timing | Live bond / model quirks |
| **Beurer BM54** | BLP + SFLOAT (rev03) | Quiet-timeout Indicate dump | Live bond + passkey confirm |
| **A&D UA-651BLE** | Shared BLP + `parsers/and_ua651.py` custom cmds | CCCD + **0x2A08 Date Time** + custom **0x01 Set Time** (5s gate) | Live bond confirm |
| **Masimo MightySat** | Frame/CRC, params, waveforms, device info, trends | SetClock + GetInfo + ConfigureStreaming | Live stream / trend IDs |
| **NT-100B thermo** | Full TICD cmd set + history builders | Dual-wake → clock → SN → count → index poll | Live GATT confirm |
| **SIG HTP** | `parsers/htp.py` FLOAT 0x2A1C | N/A (reference; NT-100B is proprietary) | Optional HTP device |
| **FORA 6** | Scaffold only | Subscribe-all RE | **Needs APK or HCI** |
| **Omron** | via `omron_bp` | Pair + token + EEPROM read | Other families need their cuff |

Golden unit tests cover SDK/CSD example frames (A&D set-time, MightySat CRC/stream/trend, Beurer BLP).

---

## Architecture (strict 3-layer + shared common)

```
medical_ble_toolkit/
  models.py           # dataclasses only (Kotlin data class)
  parser.py           # facade: profile → pure parser  ← NO bleak
  parsers/
    blood_pressure.py # SHARED BLP (Beurer + A&D)
    mightysat.py      # Masimo proprietary
    thermometer.py    # NT-100B frames
    fora.py           # RE scaffold
  common/
    sfloat.py         # IEEE 11073 SFLOAT
    crc.py            # CRC-8-CCITT (MightySat)
    hexutil.py        # hex dumps + ms timestamps
    gatt_map.py       # GATT discovery tree
  ble_client.py       # bleak scan/connect/notify only
  profiles.py         # static UUID / name catalog
```

### Kotlin mapping guide

| Python | Kotlin |
|--------|--------|
| `@dataclass BloodPressureReading` | `data class BloodPressureReading(...)` |
| `VitalParser` protocol | `interface VitalParser<T> { fun parse(payload: ByteArray): T }` |
| `BlpBloodPressureParser` | `class BlpBloodPressureParser : VitalParser<BloodPressureReading>` |
| `decode_sfloat` | `fun decodeSfloat(bytes: ByteArray, offset: Int): Float?` |
| `MedicalBleClient` | `BluetoothGattCallback` / companion BLE manager |
| `profiles.py` | `object DeviceProfiles` or Room/JSON catalog |

**Rule:** Android BLE layer must only call `parser.parse(value)`. Never put field offsets in the GATT callback.

---

## Install

```powershell
# Windows
pip install -r requirements.txt
pip install -r medical_ble_web\requirements.txt
```

```bash
# Linux (BlueZ) — see also ../LINUX.md
./setup_linux.sh
# or: pip install -r requirements.txt -r medical_ble_web/requirements.txt
```

Root `requirements.txt` lists `bleak` (only runtime dep for the toolkit). Works on **Windows (WinRT)** and **Linux (BlueZ)**.

---

## Supported devices

See **[SUPPORT_MATRIX.md](SUPPORT_MATRIX.md)** for the full list: brands, model numbers,
regional/retail names, and support levels (**A** companion-like → **D** scaffold).

---

## Interactive app (recommended)

```powershell
# Windows
python -m medical_ble_toolkit
```

```bash
# Linux
./run_toolkit.sh
# or: source .venv/bin/activate && python -m medical_ble_toolkit
```

The wizard asks:

1. **Brand** — Enter = **Omron**
2. **Model** — Enter = **HEM-7143T1** (Omron lab default) or brand default
3. **Action** — pair / read (Omron) or connect / scan (other brands)
4. **Address** — optional scan, or paste MAC; last used is remembered

Preferences are saved to `medical_ble_device.json` in the current directory.

```text
python -m medical_ble_toolkit              # wizard
python -m medical_ble_toolkit interactive  # same
```

---

## Platform debugging (Windows WinRT / Linux BlueZ)

On Windows, bleak uses **WinRT**. On Linux, bleak uses **BlueZ** over D-Bus.
This toolkit logs `[WINRT]` or `[BLUEZ]` diagnosis blocks when OS-level failures occur.

| Situation | What you see | What to do |
|-----------|--------------|------------|
| Pairing popup dismissed / cancelled | `PAIRING_DIALOG_DISMISSED` | Re-run with `--pair`, accept OS prompt (Windows popup / BlueZ agent) |
| Pairing / connect hang | `TIMEOUT` | Wake device, increase `--connect-timeout 45`, retry |
| Link dies mid discovery | `GATT_UNREACHABLE` | Live re-advertise; remove stale Bluetooth entry; reconnect |
| Encrypted char without bond | `PAIRING_REQUIRED_OR_DENIED` | `--pair`, or pair in Settings → Bluetooth & devices |
| Empty GATT tree after connect | warning in map | Same as Unreachable — advertising window closed |

BP profiles (`beurer_bm54`, `and_ua651`, …) **auto-enable `--pair` on Windows and Linux**.
Use `--no-pair` to skip. Dismissing the OS dialog / agent is caught and logged, not swallowed.

Every notification logs **before parse**:

```text
[2026-07-15 14:32:01.123] INFO    [TS]    2026-07-15 14:32:01.123   (packet #1)
[2026-07-15 14:32:01.123] INFO    [NOTIF] #=1 char=00002a35-... len=19
[2026-07-15 14:32:01.123] INFO    [HEX]   0x1E 0x70 0x00 0x4D 0x00 ...
[2026-07-15 14:32:01.124] INFO    [PARSE] OK  packet=#1  →  BloodPressureReading{...}
```

On connect, a full **GATT DISCOVERY MAP** prints with `[R W  N I]` flags
(Read / Write / Notify / Indicate) for every characteristic.

---

## Usage

```powershell
# List known device profiles
python -m medical_ble_toolkit --list-profiles

# Scan for Beurer BM54
python -m medical_ble_toolkit --profile beurer_bm54 --scan

# Connect + listen 90s (hex dumps on every notification)
# On Windows/Linux, --pair is auto-enabled for BM54 / UA-651
python -m medical_ble_toolkit --profile beurer_bm54 --address AA:BB:CC:DD:EE:FF -t 90

# Explicit pairing (OS dialog — ACCEPT it; do not dismiss)
python -m medical_ble_toolkit --profile and_ua651 -a AA:BB:.. --pair --connect-timeout 45 --retries 3

# A&D / Nipro BP
python -m medical_ble_toolkit --profile and_ua651 --scan

# MightySat SpO2 (auto-sends ConfigureStreaming after connect)
python -m medical_ble_toolkit --profile mightysat --scan -t 120

# Blind RE (subscribe all notify/indicate)
python -m medical_ble_toolkit --profile re_generic --address AA:BB:CC:DD:EE:FF -t 180

# Auto-try all parsers when UUID ownership is unknown
python -m medical_ble_toolkit --profile re_generic -a AA:BB:.. --auto-parse
```

### Pure parser (no BLE) — for unit tests / Kotlin port validation

```python
from medical_ble_toolkit.parser import parse

# Beurer doc-style BLP frame
reading = parse(payload_bytes, profile="beurer_bm54")
print(reading.systolic, reading.diastolic, reading.pulse_rate)
```

### Run parser unit tests

```powershell
python -m medical_ble_toolkit.tests.test_parsers
```

---

## Reverse Engineering Guide

### Where to look in the terminal

| Log tag | Meaning | What you do |
|---------|---------|-------------|
| **`[TS]` / log timestamp** | Millisecond clock on every line | Align with stopwatch/video of hardware action |
| **`GATT DISCOVERY MAP`** | Full service/char/property tree at connect | Note every `notify` / `indicate` char — subscribe if missing |
| **`[SUBSCRIBE]`** | Which UUIDs got CCCD enabled | Failures mean wrong UUID or security/bonding needed |
| **`[NOTIF]`** | A notification arrived (char UUID + length) | Length changes = layout change or multi-packet protocol |
| **`[HEX]`** | **Raw payload** `0x0A 0x1B 0xFF` **before parse** | **Primary RE signal** — diff before/after physical action |
| **`[IDX]`** (debug) | `00:1E 01:70 02:00 …` index-aligned bytes | Pinpoint field offsets for the Kotlin port |
| **`[PARSE] OK`** | Parser produced a dataclass | Confirm clinical values match device LCD |
| **`[PARSE] ParseError`** | Length/CRC/flag failure | Payload shape unknown or changed — update parser |
| **`[DISCONNECT]`** | bleak disconnect callback | Device sleep, range, or bonding issue |

### Recommended RE session procedure

1. Start client with `--profile re_generic` (or brand profile) and high `-t`.
2. Leave device idle 10s — note any baseline `[HEX]` noise.
3. Perform **one** physical action (e.g. finish BP measurement, press M1, insert strip).
4. Find the new `[HEX]` line(s) whose timestamp matches that action.
5. Compare byte-by-byte with the previous packet / datasheet example.
6. Encode the mapping in `parsers/<device>.py` only — leave `ble_client.py` alone.
7. Re-run; confirm `[PARSE] OK` values match the device display.

### Brand-specific connect tips

- **Beurer BM54:** Device auto-indicates all stored readings after connect (M1/M2 or post-measure). Passkey on newer units.
- **UA-651BLE:** Bond + write Date Time + enable indicate within ~5s or data is stored for next session.
- **MightySat:** Filter by manufacturer company ID `0x0243`. After notify, client auto-sends Configure Streaming.
- **NT-100B:** Dual arbitrary command within 10s enters comm mode; then `0x2B` / `0x25` / `0x26`.
- **FORA 6:** No protocol doc — use `fora6` profile (subscribe-all) and map `[HEX]` after strip tests.

---

## Example: Beurer BM54 expected parse

Datasheet example (conceptual):

```
flags=0x1E  SYS=117  DIA=77  MAP=0
time=2015-01-14 10:55:00  pulse=72  user=2  status=0
```

Wire uses LSO→MSO multi-byte fields and IEEE 11073 SFLOAT for pressures/pulse.

---

## Porting checklist for Android

- [ ] Copy `models.py` fields → Kotlin `data class`es (same names).
- [ ] Port `common/sfloat.py` and `common/crc.py` as pure functions.
- [ ] Port each `parsers/*.py` class implementing `VitalParser<T>`.
- [ ] Implement BLE only in a `MedicalBleClient` equivalent; call `parser.parse(value)`.
- [ ] Keep hex logging in debug builds until clinical validation is done.
- [ ] Add instrumentation tests with golden `ByteArray` vectors from `tests/test_parsers.py`.

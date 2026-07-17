# Nipro げんきノート — First-party HW support pack

**Purpose:** Everything needed to implement **stable pairing + hands-free data extraction** as first-party support in *your* app (Windows/toolkit).  
**Source:** APK `ニプロげんきノート_2.19.0.0` — decompiled `BLELib.dll` + `NHL.dll` (ilspycmd 10.1.1, .NET 10).  
**Do not treat as legal advice** — reverse-engineered for interoperability with devices you own.

**Related local files (not required to re-open unless detail needed):**

| File | Role |
|------|------|
| `EXACT_HW_SEQUENCES.md` | Exact app→device wire bytes / GATT |
| `EXACT_PROTOCOL.json` / `.md` | Auto-extracted UUIDs/consts |
| `NIPRO_BLE_DEVICE_MAP.md` | Device ↔ PDF map |
| `decompiled_cs/BLELib/**` | Full C# drivers |
| `decompiled_cs/NHL/**` | App orchestration |
| `extracted/assemblies_decompressed/*.dll` | Open in ILSpy |

---

## 0. Architecture you must copy

```
┌─────────────────────────────────────────────────────────────┐
│  PAIRED REGISTRY (local)                                      │
│  per category: id_nodash, name, serial, userNo, colorCode     │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  HANDS-FREE LOOP (ReceiveWait)                                │
│  Scan Balanced · exact name filter · up to 8 hours            │
│  on AD: CheckPairing(id) → ReceiveStart (60s) → save → scan  │
│  health check every 5s: if scan died → restart wait           │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  DEVICE DRIVER (per BLE name prefix)                          │
│  connect → delay → clock/auth → notify → parse → disconnect  │
└─────────────────────────────────────────────────────────────┘
```

**Key insight:** First-party stability is **not** “keep GATT open.”  
It is: **long scan → short session → strict pairing check → settle delays → restart scan.**

---

## 1. Device catalog (implement these)

| BLE name contains / exact | Category | Driver class | Hands-free home? | Bond? |
|---------------------------|----------|--------------|------------------|-------|
| `NIPRO CF` | GL (glucose) | CFL | **Yes** | Soft / feature read |
| `NSM-1BLE` | HT (temp) | NSM1 | **Yes** | Optional + custom disconnect |
| `NT-100B` | HT | NT100B | **Yes** | No (AD serial) |
| `NBP-1BLE` | BP | NBP1 | **Yes** | Soft |
| `NMBP` | BP | UM212 | **Yes** (if registered as BP) | **Yes** (wait Bonded) |
| `NBCM` | BC (body comp) | NBCM | **Separate screen only** | **Yes** |
| `MightySat` | SpO2 | MightySat | **Not in home wait list** | No |

`BLEManager.GetDevice(name)`: first driver where `name.Contains(DeviceName)`.

**Home hands-free filters only GL + HT + BP** (`LoadTargetMeterNameList`).  
NBCM has its own `BodyCompositionMeasurementViewModel` wait loop.  
MightySat is in BLELib but not in home auto-wait.

---

## 2. Pairing registry (implement this store)

### 2.1 Schema (`MeterSetting`)

```json
{
  "Id": "<guid ToString(\"N\") — 32 hex, no dashes>",
  "Name": "<exact advertised BLE name>",
  "SerialNumber": "<string>",
  "UserNo": "<NBCM user index string; else empty>",
  "ColorCode": "<NBCM color code; default \"0\">"
}
```

### 2.2 Persistence (app’s layout — replicate logically)

| Category | File name pattern |
|----------|-------------------|
| GL | `{sanitizedUserId}_GLMeterSetting.txt` |
| HT | `HTMeterSetting.txt` |
| BP | `BPMeterSetting.txt` |
| BC | `BCMeterSetting.txt` |
| BF / PL | `BF…` / `PL…` (defined, secondary) |

Folder: `HNLSettingFolder` under local storage.  
Format: JSON via `DataContractJsonSerializer` (UTF-16 on read path in app).

### 2.3 Pairing check (exact)

```text
CheckPairing(deviceId):
  for each category:
    setting = load(category)
    if setting.Id == deviceId.Replace("-", "")  → paired
  else not paired
```

**Ignore ads from unpaired devices** even if name matches model prefix.

### 2.4 What to store at pair time

| Device | Id | Name | Serial source | Extra |
|--------|-----|------|---------------|-------|
| BP NBP/NMBP | GATT/OS id | adv name | DIS `0x2A25` read (after bond for NMBP) | — |
| HT NSM/NT | id | adv name | AD mfg last 4 bytes and/or DIS | — |
| GL CFL | id | adv name | DIS custom serial char | optional client type/mode |
| NBCM | id | adv name | DIS after bond | **UserNo + ColorCode required** for receive |

### 2.5 Manufacturer AD serial decode (no connect)

```text
adv Type = ManufacturerSpecificData
if data.length >= 4:
  serial = int64_from_hex( reverse(data[-4:]) ) as decimal string
```

Used in scan callbacks for NT-100B / MightySat / UI.

---

## 3. Connection stability constants (copy these)

| Constant | Value | Use |
|----------|-------|-----|
| Hands-free scan timeout | **28_800_000 ms (8 h)** | `ReceiveWait` |
| Per-session receive timeout | **60_000 ms** | `ReceiveStart` |
| Pair timeout | **30_000 ms** | most drivers |
| NBCM setup color scan | **30_000 ms** | |
| NBCM free-color / register | **60_000 ms** | |
| Pre-clock write delay | **1_000 ms** after connect | BP, NSM |
| Post-disconnect settle | **100 ms** | all |
| NT-100B connect default | **60_000 ms** | |
| NT-100B after measure before disconnect | TurnOff + **1_000 ms** | |
| Char discovery retries | **3** | |
| Fujitsu connect retry | **10 × 100 ms** | Android quirk |
| NBCM iOS serial | **2_000 + 1_000 ms** after bond | |
| Health check period | **5_000 ms** | if !IsScanning → restart wait |
| Android scan pause after first AD | **5_000 ms** then restart scan | multi-device |
| Scan mode | **Balanced** | Mode=2 |
| Connect | `autoConnect=true`, **`forceBleTransport=true`** | LE only |
| Scan duplicates | **false** | |
| Central manager ready wait | **10_000 ms** after BT request | |
| Pre-scan location/BT request delay | **1_000 ms** | Android |

### 3.1 Connect parameters (Windows mapping)

| App | Windows recommendation |
|-----|------------------------|
| forceBleTransport | Use LE-only / random address path; don’t use classic |
| autoConnect true | Prefer reconnect-friendly connect; retry on fail |
| Bond wait (NMBP/NBCM) | Ensure OS pairing completed before DIS serial read |
| “Encryption is insufficient” | App surfaces re-pair message — unpair in OS + re-register |

### 3.2 Session concurrency rules

- Only **one** active receive per `DeviceName` class at a time (`ReceiveDeviceList` guard).  
- While any receive `Status==Start`, health check does not restart scan.  
- When **all** receives Complete and list non-empty → clear list, restart wait.  
- On `SCAN_TIMEOUT` / `RCV_WAIT_TIMEOUT` → **RestartReceiveWait** (stop + 10 ms + start).  
- On hard failure → full `ReceiveStop` + `Dispose` BLE + `StartScan` (10 ms gap).

### 3.3 Disconnect semantics (treat as success for some)

| Device class | Connection lost / disconnect |
|--------------|------------------------------|
| NBP / NMBP / NSM / NT | Often **end of transfer** (esp. BP push-then-drop) |
| NIPRO CF | Connection lost with **0 records** → error UI |
| After RCV_END on BP-class names | Remove from receive list immediately |

---

## 4. Hands-free state machine (implement this)

```
StartHandsFree:
  ensure BT (+ location on Android)
  wait central ready (10s)
  names = registered exact names for GL, HT, BP  # home
  ReceiveWait(names, 8h, onAdvertisement)

onAdvertisement(name, id, rssi, state, serialFromAd):
  if not CheckPairing(id): return
  if name starts with NBCM: return   # home ignores body scale
  if already receiving this name: return
  mark Start; stop health check
  param = buildDeviceParams(name)    # CFL Diff/All etc.
  result = ReceiveStart(name, timeout=60s, param)
  if result: parse + dedupe + store
  mark Complete
  if no other Start: maybe snackbar; if sleep: stop; else StartHealthCheck

HealthCheck every 5s:
  if any Start: continue
  if not IsScanning: RestartReceiveWait
  if all Complete: clear list; RestartReceiveWait
```

### 4.1 Name filter rules (do not mix up)

| Mode | Filter |
|------|--------|
| Pairing scan (`ScanStart`) | `advName.StartsWith(prefix)` |
| Hands-free (`ReceiveWait`) | `advName.Trim() == registeredName` **exact** |
| Android ReceiveWait | same exact match |

### 4.2 CFL hands-free params (glucose)

```text
if last_seq exists in DB for this serial:
  param = ["Diff", str(last_seq + 1)]
else:
  param = ["All"]

if user has glucose thresholds:
  append targets: "700", high, littleHigh, littleLow, low, "0", "0"
  (pad empty if Diff already used first slot carefully — see source)
```

---

## 5. Per-device HW session (data extraction)

Full wire details: `EXACT_HW_SEQUENCES.md`. Summary for implementers:

### 5.1 BP — NBP-1BLE / NMBP

```
connect(force LE)
wait connected
delay 1000ms
WRITE 0x2A08 DateTime 7 bytes LE year+mdhms   on service 0x1810
READ  0x2A08 (verify)
enable indicate 0x2A35
wait until timeout OR connection lost
parse all BLS records (app UI keeps latest only)
disconnect; delay 100ms
```

**Parse BLS `0x2A35`:** standard flags + SFLOAT SBP/DBP/MAP + optional timestamp + pulse.  
**Reject if:** SBP/DBP/pulse == 2047 (or pulse 2048) or timestamp MinValue.  
**Store as 3 rows:** type `03` SBP, `04` DBP, `05` pulse — same timestamp.  
**App keeps only latest measurement** for BP (not full history).

### 5.2 Temperature — NSM-1BLE

```
connect
delay 1000ms
WRITE clock 0x2A08 on HTS 0x1809 (7 bytes)
enable indicate 0x2A1C
wait / parse HTP IEEE-11073
pair path also: WRITE custom 233BF001 = 02 01 03 (disconnect)
```

**Custom cmds defined** (A&D style Size|Type|Cmd): see EXACT_HW_SEQUENCES.

### 5.3 Temperature — NT-100B

```
connect (60s)
enable indicate HTS 0x2A1C  (no clock write in receive path)
wait one reading
finally: WRITE custom 1524: 51 50 00 00 00 00 A3 44  (power off)
delay 1000; disconnect; delay 100
```

**Reject if:** temp == 65535 or < 0 or timestamp MinValue.  
**App stores latest only**, type `02`.

### 5.4 Glucose — NIPRO CF

```
connect
read serial (custom DIS)
TimeSetting write on 87F60002 (7-byte clock, year hex-pair encoding)
enable notify: Measurement, Context, RACP
RACP write count:  04 01 | 04 03 01 seq_le | ...
RACP write report: 01 01 | 01 03 01 seq_le | ...
wait until counts match mode
disconnect
```

**Measurement notify layout (app parse):**

| Offset | Field |
|--------|--------|
| 1–2 | SeqNo int16 LE |
| 3–4 | Year int16 LE |
| 5–9 | mon,day,h,m,s |
| 12–13 | Glucose SFLOAT (IEEE-11073 16-bit) |
| 14 | Type byte → hex string e.g. control `"0A"` |

**Glu storage:** `(int)((decimal)sfloat * 100000m)` then treated as integer mg/dL in UI clamp.  
**Context:** if `array[0]==2` and len 4: meal flag at `array[3]`, same seq.  
**Skip** `Type == "0A"` (control solution).  
**Clamp:** >600 → value 600 exceed=`1`; <20 → 20 exceed=`2`; else exceed=`0`.  
**Dedup:** same `MeasuringEquipmentMeasurementId` (seq) + type `01` + equipment serial.

### 5.5 MightySat SpO2

```
connect
notify 54c2…1002
write-without-response 54c2…1001:
  frame 77 | LEN | CMD | data | CRC8_CCITT
  01 GetInfo → 02 SetClock(UTC ticks 8B) → 03 EnableStream(3B from info)
parse Parameters 0x05; complete on sensor-off bit
disconnect
```

Example GetInfo: `77 02 01 07`.

### 5.6 NBCM body composition (not home hands-free)

```
require UserNo + ColorCode
bond + serial
user auth / custom A&D 11127001 commands
weight 0x181D / body comp 0x181B notifications
types: 06 weight, 07 BMI, 08 fat%, 09 muscle, 10 BMR, 11 water, (+ derived 12–14)
```

---

## 6. Measurement type codes (your data model)

| Code | Meaning | Source devices |
|------|---------|----------------|
| `01` | Glucose | NIPRO CF |
| `02` | Temperature | NSM / NT |
| `03` | Systolic | NBP / NMBP |
| `04` | Diastolic | |
| `05` | Pulse | |
| `06` | Weight | NBCM |
| `07` | BMI | NBCM |
| `08` | Body fat % | NBCM |
| `09` | Muscle mass | NBCM |
| `10` | Basal metabolism | NBCM |
| `11` | Body water mass | NBCM |
| `12`–`14` | Derived fat mass / muscle% / water% | NBCM |
| `15` | Steps | (defined, not BLELib core) |

**Core record fields used by app:**

```
MeasurementAt, MeasurementValue, MeasurementType,
MeasuringEquipmentId (serial), MeasuringEquipmentName,
MeasuringEquipmentMeasurementId (glucose seq),
ExceedLimitType (0/1/2), IgUserId
```

### Dedup rules

| Type | Dedup key |
|------|-----------|
| Glucose `01` | equipmentSerial + measurementId (seq) |
| Others | type + MeasurementAt + MeasurementValue |

---

## 7. First-party implementation checklist

### Phase A — Registry & scan (stability foundation)

- [ ] Local pair store (Id N-format, Name, Serial, UserNo, Color)  
- [ ] Scan Balanced, LE-only  
- [ ] Exact-name filter for hands-free; StartsWith for discovery UI  
- [ ] AD serial decode  
- [ ] CheckPairing before any connect  
- [ ] 8h wait timeout + auto-restart  
- [ ] 5s health check if scan not running  
- [ ] Single-flight receive per device name  

### Phase B — Drivers (HW layer)

- [ ] BP: clock + indicate BLS (NBP + NMBP/bond)  
- [ ] HT: HTP indicate (NSM clock+custom; NT power-off)  
- [ ] GL: CFL UUIDs + RACP Diff/All + dual notify merge  
- [ ] Optional: MightySat stream; NBCM full user stack  

### Phase C — Data quality

- [ ] IEEE-11073 SFLOAT / FLOAT parsers (shared)  
- [ ] Invalid sentinels: 2047/2048 BP, 65535 temp, control `0A` glucose  
- [ ] Clamp glucose 20–600 with exceed flags  
- [ ] Dedup before insert  
- [ ] BP/HT: latest-only vs full history (product choice; app uses latest for HT/BP UI path)  

### Phase D — Windows-specific hardening

- [ ] OS pairing for bond devices before DIS read  
- [ ] Re-pair guidance on encryption errors  
- [ ] 1s post-connect before writes  
- [ ] 100ms post-disconnect  
- [ ] Don’t thrash scan start/stop (optional 5s cool-down like Android)  
- [ ] Capture btsnoop on failures; compare to EXACT_HW_SEQUENCES  

---

## 8. Error UX (from app)

Japanese string used for receive failure (re-pair guidance):

> データ受信できませんでした。エラーが続く場合は、OSのBluetooth設定から測定器の登録を一度解除して、測定器登録をやり直してください。

Trigger contexts: `RCV_TIMEOUT`, some `RCV_ERR`, CF connection lost with empty result, encryption insufficient.

---

## 9. What you do **not** need from APK

- Azure / hospital cloud sync for local first-party support  
- Charts, Caliburn UI, photo food features  
- MTU / connection interval (never set by app)  
- Continuous GATT keep-alive  

---

## 10. Recommended build order for *your* `medical_ble_toolkit`

1. **Pair registry + hands-free scanner** (exact names, CheckPairing, timeouts)  
2. **BP session** (highest similarity to existing Beurer/Omron patterns)  
3. **HT HTP** (NT + NSM)  
4. **CFL glucose** (proprietary UUIDs; most complex)  
5. **NBCM** if required  
6. **MightySat** (you already have `mightysat.py`)  

Align parsers you already have:

| Toolkit file | Device |
|--------------|--------|
| `and_ua651.py` | NMBP / NSM custom framing family |
| `blood_pressure.py` | BLS 0x2A35 |
| `htp.py` / `thermometer.py` | HT |
| `mightysat.py` | MightySat |
| *(new)* | CFL RACP + CF UUIDs |
| *(new)* | NBCM A&D 11127000 |

---

## 11. Source anchors (for audits)

| Topic | File |
|-------|------|
| Hands-free timeouts / handler | `NHL.../MasterDetailRootViewModel.cs` |
| Pair store | `NHL.../MeterSetting.cs`, `MeterSettingFiles.cs` |
| CheckPairing | `MeterContext.cs` |
| Scan/ReceiveWait/Start | `BLELib/BLELib.cs`, `BLELib.Android/BLELibrary.cs` |
| Retry util | `BLELib.Helper/RetryUtil.cs` |
| Drivers | `BLELib.BLEDevice/BLEDevice*.cs` |
| Measurement type codes | `NHL.Models.Types/MeasurementType.cs` |

---

*Local working document for first-party HW support. Not committed.*

# Tier 1 & Tier 2 — Complete BLE connect / send / receive / parse reference

Source: reverse-engineered **beurer HealthManager Pro 1.20.1** (`com.beurer.healthmanager`).  
Tier 3 (OCR / legacy stubs) is **out of scope** here.

**Related files**
- Device list: `tools/device_registry.json`
- BP deep dive: `BLE_PROTOCOL_ANALYSIS.md`
- Working BP parser: `tools/bm54_ble_parser.py`
- Decompiled sources: `decompiled/full/sources/`

---

## Shared connection stack (all tiers)

All modern device paths use the same outer stack:

```
Scan (KableDeviceSearch)
  → filter by advertisement name and/or discover UUID
  → Connect (KableDeviceConnection.peripheral.connect)
  → Bond if needed (BTDeviceTriggerAndWaitForBondingUseCase)
  → Discover services
  → Profile-specific: enable notify/indicate, write commands, collect data
  → Map to domain models → DB
```

**CCCD (Client Characteristic Configuration Descriptor)**  
UUID: `00002902-0000-1000-8000-00805f9b34fb`

| Value | Meaning |
|-------|---------|
| `00 00` | Disable |
| `01 00` | Enable **notification** |
| `02 00` | Enable **indication** |

**16-bit UUID expansion** (app helper `BTUUIDHelperImpl`):

```
UUID = 0000XXXX-0000-1000-8000-00805f9b34fb
where XXXX is the short assigned number
```

---

# TIER 1 — Fully specified (implement now)

## 1. Blood Pressure SIG profile

### Devices
**~66 models** in `blood_pressure` category, including BM54, BM55, BM57, BM58, BM59, BM64, BC*, SERIES*, etc.  
Same GATT map for all; small flag-bit differences for AFib/HSD by model markers.

### Identity / scan

| Field | Value |
|-------|--------|
| Adv names | e.g. `"BM54"`, `"BM55"`, … (see registry) |
| Scan / discover UUID | `00001810-0000-1000-8000-00805f9b34fb` |
| Manufacturer AD (BM54 PDF) | Company ID `0x0611`, payload `0x110601` (+ passkey flag variants) |

### Connect flow

```
1. Scan for name and/or service 0x1810
2. Connect + bond (6-digit passkey on newer BM54-class)
3. Optional: read Device Information 0x180A strings
4. Enable INDICATION on 0x2A35 (CCCD write 02 00)
5. Collect every indication payload into a list
6. Restart idle timer on each packet (app uses BTFlowTimer)
7. On quiet timeout or disconnect → parse all → save
```

**Important:** BM54-class devices **auto-send all stored measurements** after connect. Phone does **not** send a “download history” command in the standard BP path.

### GATT map

| Role | UUID | Properties |
|------|------|------------|
| Service | `00001810-…` (0x1810) | |
| Blood Pressure Measurement | `00002a35-…` (0x2A35) | **Indicate** |
| BP Feature | `00002a49-…` (0x2A49) | Read |
| Intermediate Cuff | `00002a36-…` (0x2A36) | Notify (unused on BM54) |
| Device Information | `0000180a-…` | Read strings |
| Current Time (optional set-time) | `00001805-…` / char `00002a2b-…` | Write |

Hardcoded app config: `BloodPressureDeviceConfigurationImpl` → `(6160, 10805)` = `(0x1810, 0x2A35)`.

### What phone sends

| Message | Bytes / action |
|---------|----------------|
| Enable indication | CCCD of 0x2A35 ← `02 00` |
| Optional set time | Write Current Time char 0x2A2B (see below) |
| Optional DIS reads | Read model/serial/FW strings |

### What device sends

| Message | When |
|---------|------|
| Indication on 0x2A35 | After connect (history dump) and after new measurements |

### Parse — Blood Pressure Measurement (typically 19 bytes)

| Off | Size | Field | Encoding |
|-----|------|-------|----------|
| 0 | 1 | flags | bit0 units kPa; bit1 time; bit2 pulse; bit3 userId; bit4 status |
| 1–2 | 2 | systolic | IEEE 11073 **SFLOAT** LE |
| 3–4 | 2 | diastolic | SFLOAT LE |
| 5–6 | 2 | MAP | SFLOAT (0 on BM54) |
| 7–8 | 2 | year | uint16 LE |
| 9 | 1 | month | 1–12 |
| 10 | 1 | day | |
| 11 | 1 | hour | |
| 12 | 1 | minute | |
| 13 | 1 | second | |
| 14–15 | 2 | pulse | SFLOAT LE (some models swap bytes) |
| 16 | 1 | userId | 0=user1, 1=user2 |
| 17–18 | 2 | measurement status | IHB / movement / cuff / position bits |

**SFLOAT** (app `fj.a`):

```
mant = low | ((high & 0x0F) << 8)   // 12-bit signed
exp  = high >> 4                    // 4-bit signed
value = mant * 10^exp
```

**Status bits (SIG-style, app extracts subsets):**
- bit0 body movement  
- bit1 cuff fit  
- bit2 irregular pulse (IHB)  
- bits 3–4 pulse rate range  
- bit5 measurement position  

**BM54 flags always `0x1E`** (time+pulse+user+status, mmHg).

**Optional set-time write** (`BM59SetTimeRepoImpl`, service 0x1805 / char 0x2A2B):

```
year_lo, year_hi, month, day, hour, min, sec, dayOfWeek, 0x00, 0x00
```

Reference implementation: `tools/bm54_ble_parser.py` (verified against PDF example 117/77/72).

### Confidence: **HIGH** (APK + Beurer BM54 PDF agree)

---

# TIER 2 — Connect + UUIDs known; payloads largely recovered

These are implementable, but some proprietary frames may need a live device to confirm edge cases.

---

## 2. Glucose (GL22–GL60 family)

### Devices
GL22, GL34, GL40, GL44, GL44Lean, GL48, GL49, GL50, GL50EVO, GL60  
Scan UUID: **`00001808-…` (Glucose Service)**

### Stack note
Uses **RxAndroidBle** path (`Gl50SyncRepo`), not only Kable — same BLE semantics.

### Connect flow (`Gl50SyncRepo.H`)

```
1. Connect to device advertising Glucose 0x1808
2. Discover services, locate Glucose service
3. For some device markers (mg):
   a. CCCD 2A18: write 00 00 then 01 00 (enable notify)
   b. CCCD 2A34: write 00 00 then 01 00 (context notify)
   c. CCCD 2A52: write 00 00 then 02 00 (enable indicate on RACP)
4. Build RACP query (see below)
5. Write query to RACP characteristic 0x2A52
6. Collect notifications from:
   - 0x2A18 Glucose Measurement  (short id 10776)
   - 0x2A34 Glucose Measurement Context (10804)
   - 0x2A52 RACP responses (10834)
7. Zip streams → map to GlucoseMeasurement models → DB
```

### GATT map

| Char | UUID | Role |
|------|------|------|
| Glucose Measurement | `00002a18-…` | Notify — readings |
| Glucose Measurement Context | `00002a34-…` | Notify — meal/context |
| Record Access Control Point | `00002a52-…` | Indicate + Write — history query |
| CCCD | `00002902-…` | Enable N/I |

### What phone sends

| Step | Char | Bytes | Meaning |
|------|------|-------|---------|
| Disable notify | 2A18 CCCD | `00 00` | reset |
| Enable notify | 2A18 CCCD | `01 00` | measurements |
| Same for context | 2A34 CCCD | `00 00` then `01 00` | |
| Disable then indicate RACP | 2A52 CCCD | `00 00` then `02 00` | |
| RACP: all records | 2A52 write | `01` | Report all stored records (opcode 1, operator 1 implicit) |
| RACP: ≥ sequence | 2A52 write | `03 01 seq_lo seq_hi` | Report records greater/equal sequence |
| RACP: time range (some models `og`) | 2A52 write | `04 02` + two 6-byte dates from `fj.b.a` | Filter by time |

App delays: 50 ms after CCCD read path, 300 ms after CCCD write, optional 2500 ms before RACP for marker `mg`.

### What device sends

- Notifications on **2A18** (glucose value + flags + optional time/seq)  
- Notifications on **2A34** (context)  
- Indications on **2A52** (RACP response / number of records / success)

### Parse (SIG Glucose Measurement — use SIG spec + app models)

Standard Bluetooth SIG Glucose Measurement layout (implement against SIG + validate on device):

| Field | Notes |
|-------|--------|
| flags | time, type/location, concentration units, sensor status, context |
| sequence number | uint16 — app stores last sequence for incremental sync |
| base time | if present |
| concentration | SFLOAT (mg/dL or mmol/L per flags) |
| type / sample location | nibble fields |
| sensor status annunciation | optional |

App domain model: `dp.GlucoseMeasurement` with sequence used for next RACP query.

### Confidence: **HIGH for connect/CCCD/RACP opcodes**; **MEDIUM-HIGH for full 2A18 bit layout** (follow SIG Glucose Profile + live capture once)

---

## 3. Thermometer (FT* family)

### Devices
FT09_1, FT15_1, FT16, FT17, FT58, FT65, FT85, FT90, FT95, FT100  
Scan: **`00001809-…` Health Thermometer**

### Connect flow (`TemperatureSyncRepoImpl`)

```
1. Connect
2. Service short id H() = 6153 = 0x1809
3. Characteristic E() = 10780 = 0x2A1C Temperature Measurement
4. Mode = INDICATION (ServiceType.INDICATION)
5. Enable indication, collect payloads
6. Each payload must be length 13 → parse → TemperatureData
```

Uses RxAndroidBle base `j1` like glucose.

### GATT map

| Role | Short | UUID |
|------|-------|------|
| Service | 0x1809 | `00001809-…` |
| Temperature Measurement | 0x2A1C | `00002a1c-…` |
| Mode | Indicate | CCCD `02 00` |

### What phone sends
- CCCD enable indication on 0x2A1C  
- Pairing if required (status 19 disconnect → `BlePairingPinException`)

### What device sends
- Indications, **13-byte** custom/extended temperature frames (app enforces `length == 13`)

### Parse (from `et/b.java` — decompiled)

```
byte[0]  flags
  bit0 = temperature unit: 1 → Fahrenheit (convert to C), 0 → already Celsius path
  bit7 = boolean flag stored on TemperatureData
byte[1..3]  24-bit mantissa LE
byte[4]     exponent (signed); value = mantissa / 10^(-exponent)
            i.e. mantissa * 10^(exponent) with exponent as stored with sign via -b12 in code:
            pow = mant / 10^(-b12)  == mant * 10^(b12) if b12 positive? 
            Code: / Math.pow(10.0, -b12)  so exp_applied = b12
byte[5..6]  year as short via ByteBuffer.wrap(b6,b5) BIG_ENDIAN → year
byte[7]     month
byte[8]     day
byte[9]     hour
byte[10]    minute
byte[11]    second
byte[12]    type: 2 = FOREHEAD, else UNKNOWN
```

If Fahrenheit flag set:

```
celsius = (fahrenheit - 32) * 5 / 9
```

### Confidence: **HIGH** (full 13-byte parser recovered from app)

---

## 4. Pulse oximeter (PO60)

### Devices
PO60  

### GATT (`PulseOxyGeneralDeviceConfigurationImpl`)

| Role | Short | UUID |
|------|-------|------|
| Service | 65298 = **0xFF12** | `0000ff12-…` |
| Char A (config a) | 65281 = **0xFF01** | `0000ff01-…` |
| Char B (config b) | 65282 = **0xFF02** | `0000ff02-…` |

App opens observer on **b()** config first (`0xFF12` / `0xFF02`), then uses **a()** as write target for “request more”.

### Connect / transfer flow (`PulseOxyDeviceSyncRepoImpl`)

```
1. Connect
2. Subscribe to notifications/indications on service FF12 / char FF02
3. Accumulate raw bytes in buffer
4. Look for header byte 0xE9 (-23)
5. Packet meta at E9+1:
     packetNumber = byte & 0x0F
     isLast       = (byte & 0x40) != 0
     complete 24-byte frame when (buffer.size - e9Index) == 24
6. If packetNumber requires more data and not last:
     write WITHOUT_RESPONSE to other char: [0x99, 0x01, 0x1A]
     (bytes: -103, 1, 26)
7. When COMPLETE: split buffer into 24-byte chunks → parse each
```

### What phone sends

| Message | Bytes | Where |
|---------|-------|--------|
| Request more packets | `99 01 1A` | write on FF01 (config a), WITHOUT_RESPONSE |
| CCCD enable | standard | on notify char FF02 |

### What device sends
- Stream of bytes containing one or more **24-byte records** starting near `E9 …`

### Parse 24-byte record (`ii.b.a` — reconstructed from decompiled smali/java)

| Offsets | Field |
|---------|--------|
| [2..7] | start datetime (6 bytes, year = byte0+2000) via `fj.b.b` |
| [1] bit6 | `lastMeasurementOnDevice` flag |
| [8..13] | end datetime (6 bytes) same format |
| [14..16] | packed `storageTimePeriod` (bit packing across 3 bytes) |
| [17] | spo2 max (raw) |
| [18] | spo2 min |
| [19] | spo2 avg |
| [14] bits + [20] | prMax packing |
| [14] bits + [21] | prMin packing |
| [14] bits + [22] | prAvg packing |

Mapped to domain: `spo2max/min/avg`, `prMax/Min/Avg`, start/end times (`PulseOxyMeasurementData`).

Datetime helper `fj.b.b` (6 bytes):

```
year = b0 + 2000
month = b1
day = b2
hour = b3
min = b4
sec = b5
```

### Confidence: **HIGH for flow/UUIDs/request-more**; **MEDIUM-HIGH for bit packing** (recovered from decompile; validate on device)

---

## 5. Activity trackers

### 5a. AS87

| Item | Value |
|------|--------|
| Scan UUID | `7905ff00-b5ce-4e99-a40f-4b1e122d00d0` |
| Service | `d0a2ff00-2996-d38b-e214-86515df5a1df` |
| Char primary | `7905ff01-b5ce-4e99-a40f-4b1e122d00d0` |
| Char secondary | `7905ff02-b5ce-4e99-a40f-4b1e122d00d0` |

**Flow (app pattern for all trackers):**
```
connect → set time → get device info → get personal info → get activity/sleep
→ get alarms → get heart rates (if supported) → set settings as needed
```

Repos live under:
`btdeviceimpl/repository/activitytracker/as87/*`  
Parsers under:
`btdeviceimpl/parser/activitytracker/*`

Each repo: write command bytes → wait notify → `parseResponseData`.

### 5b. AS98

| Item | Value |
|------|--------|
| Scan / service | `0000fff0-0000-1000-8000-00805f9b34fb` |
| Char A | `0000fff6-…` |
| Char B | `0000fff7-…` |

Repos: `…/as98` + `ni/*` decompiled.

### 5c. AS99

| Item | Value |
|------|--------|
| Scan / service | `00006006-0000-1000-8000-00805f9b34fb` |
| Chars | `00008001`, `00008002`, plus `00007006`/`00008004` for some ops |

Repos: `…/as99/*` — richest command set (activity, sleep, HR, alarms, vibration, camera trigger, call/message notify).

### Tracker parsing status

| Area | Status |
|------|--------|
| UUID map | **Complete** |
| Operation list (get/set time, activity, sleep, HR, alarms) | **Complete** (class inventory) |
| Exact command opcodes + response layouts | **Partial** — present in per-repo `parseResponseData` / parser classes; needs per-operation extraction (large but mechanical) |

**How to finish opcodes:** for each `BTTrackerAS##*RepoImpl`, read the write `byte[]` construction and the matching parser in `parser/activitytracker/`. Pattern is consistent.

### Confidence: **HIGH UUIDs/flow**; **MEDIUM command bytes** (not every opcode table fully transcribed yet)

---

## 6. Scales (BF600 / BF700 / Wi-Fi scales)

### Scan
Often **Weight Scale `0x181D`**, or proprietary **`0xFFF0` / `0xFFFF`** depending on model markers (`gh` etc.).

### 6a. BF700 series — command protocol (complete opcode table)

Service/char from scale config (typical proprietary FFF0-family).  
Commands are **small byte sequences**; `prepareCommand` prefixes a series-type request byte:

```
requestByte (from BTScaleBF700SeriesType) || command.bytes
```

| Command | Bytes (decimal) | Hex |
|---------|-----------------|-----|
| TYPE_1_INIT | 246, 1 | `F6 01` |
| TYPE_1_SET_TIME | 249 | `F9` |
| TYPE_2_3_INIT | 230, 1 | `E6 01` |
| TYPE_2_3_SET_TIME | 233 | `E9` |
| SET_UNIT | 77 | `4D` |
| FIRST_USER | 51 | `33` |
| NEXT_USER | 241, 52 | `F1 34` |
| USER_INFORMATION | 54 | `36` |
| CREATE_USER | 49 | `31` |
| UPDATE_USER | 53 | `35` |
| DELETE_USER | 50 | `32` |
| TAKE_MEASUREMENT | 64 | `40` |
| **GET_MEASUREMENT** | **65** | **`41`** |
| NEXT_MEASUREMENT | 241, 66 | `F1 42` |
| GET_SCALE_STATUS | 79 | `4F` |
| GET_UNKNOWN_MEASUREMENT | 70 | `46` |
| DELETE_UNKNOWN_MEASUREMENT | 73 | `49` |
| NEXT_UNKNOWN_MEASUREMENT | 241, 71 | `F1 47` |
| ASSIGN_UNKNOWN | 75 | `4B` |
| ASSIGN_COMPLETE / GET_SECOND | 241, 76 | `F1 4C` |
| SYNC_NOTIFICATION_SECOND | 241, 89 | `F1 59` |
| DELETE_USER_MEASUREMENT | 67 | `43` |
| SET_SCALE_THRESHOLD | 78 | `4E` |

### BF700 get-measurements flow (`BTScaleBF700GetMeasurementsRepoImpl`)

```
states: HEADER → DATA → END
1. Send GET_MEASUREMENT (0x41) with series requestByte
2. Receive header frame(s) → parse count / meta via BTScaleBF700NewResponseValidity
3. Loop NEXT_MEASUREMENT (F1 42) until all data frames collected
4. Parse user measurement with BTScaleBF700GeneralUserMeasurementDataParser
5. END
```

Response validity helpers track `metaData` / `userData` lengths.

### 6b. BF600 / Wi-Fi scales — service map (from `ScaleGeneralServiceConfigurationImpl`)

| Function | Service short | Char short | UUID form |
|----------|---------------|------------|-----------|
| Current Time | 0x1805 (6149) | 0x2A2B (10795) | SIG |
| Battery | 0x180F (6159) | 0x2A19 (10777) | SIG |
| Weight Scale | 0x181D (6173) | 0x2A9D (10909) | SIG |
| Body Composition | 0x181B (6171) | 0x2A9C (10908) | SIG |
| User Data block | 0x181C (6172) | various 108xx–109xx | SIG-ish |
| Custom FFF0 / FFFF | 0xFFF0 / 0xFFFF | many 0xFFF1–0xFFFA style | proprietary |
| BF600 Wi-Fi | 0xFFEF (65519) | 65504–65509 | Wi-Fi/OTA |
| BF700 custom | 0xFFE0 (65504) | 65505 | |
| Display (BF990) | 0xFFCF (65487) | 65472–65474 | |

**Write type:** BF990 often `WITHOUT_RESPONSE`; others often `WITH_RESPONSE`.

Wi-Fi cert hashes/paths in `BTScaleBF600Constants` + `assets/certificates/`.

### Scale parsing status

| Area | Status |
|------|--------|
| BF700 command opcodes | **Complete table** |
| BF700 multi-frame GET measurement state machine | **Complete** |
| Exact measurement body field offsets | **Partial** (parsers exist: `BTScaleBF700GeneralUserMeasurementDataParser`, BF600 measurement parsers) |
| BF600 analysis body/fat/muscle frames | **Partial** (parser classes + log `ByteData:`) |

### Confidence: **HIGH for BF700 commands + connect pattern**; **MEDIUM for full measurement struct**

---

## 7. ECG + BP combo (BM93 / BM95 / BM96 / ME90 / ME95)

### GATT (`ECGGeneralDeviceConfigurationImpl`)

| Function | UUID |
|----------|------|
| Custom service | `6E800001-B5A3-F393-E0A9-E50E24DCCA9E` |
| Cmd / data | `6E800002-…` / `6E800003-…` / `6E800004-…` |
| OTA service/char | `6E801000-…` / `6E801001-…` |
| Alt service | `0000A000-…` |
| Heart rate | `00002A37-…` |
| Cuff pressure | `00002A36-…` |
| **Also BP** | service `0x1810` / measurement `0x2A35` (same parse as Tier 1!) |
| Time | `0x1805` / `0x2A2B` |

### Connect / sync pattern (app use cases)

```
connect
→ set time
→ (BM95) set standby / get cuff pressure optional
→ get measurement count
→ download BP (+ headers) using same BP indication path or custom
→ download ECG headers + points (custom 6E80 frames)
→ optional delete measurements
→ ME95: live measurement toggle, validate, OTA firmware from assets/firmware/me95_*.bin
```

### Parse

| Data | How |
|------|-----|
| Blood pressure | **Same Tier-1 BP Measurement mapper** (`BTBloodPressureMeasurementDataMapper`) |
| ECG headers | `BTEcgBM95HeaderRepoModelParser` / BM93 / BM96 header parsers |
| ECG waveform points | `BTEcgBM96EcgMeasurementRepoImpl` / ME95 download repos — proprietary binary |
| Cuff pressure | Intermediate cuff 0x2A36 or custom |

### Confidence: **HIGH for dual-path BP + UUID map**; **MEDIUM for ECG sample encoding** (needs focused decompile of measurement repos or live capture)

---

## Cross-profile cheat sheet

| Profile | Scan UUID | Primary data char | Phone main action | Parse |
|---------|-----------|-------------------|-------------------|--------|
| BP | 0x1810 | 0x2A35 indicate | CCCD 02 00 | 19-byte SFLOAT SIG |
| Glucose | 0x1808 | 0x2A18 notify + RACP 0x2A52 | CCCD + RACP write | SIG glucose + seq |
| Thermo | 0x1809 | 0x2A1C indicate | CCCD 02 00 | **13-byte** app frame |
| PulseOx | 0xFF12 | 0xFF02 notify | CCCD + `99 01 1A` | 24-byte `E9` frames |
| AS87 | 7905ff00-… | 7905ff01 / ff02 | cmd/notify pairs | proprietary parsers |
| AS98 | 0xFFF0 | 0xFFF6 / 0xFFF7 | cmd/notify | proprietary |
| AS99 | 0x6006 | 0x8001 / 0x8002 | cmd/notify | proprietary |
| Scale BF700 | 0xFFF0/FFFF family | custom | opcode table above | multi-frame + parsers |
| ECG | 6E800001-… | 6E800002–4 | proprietary cmds | BP=SIG; ECG=custom |

---

## What is “complete enough to implement” vs “needs one more pass”

| Profile | Connect | Send | Receive | Parse | Ship? |
|---------|---------|------|---------|-------|-------|
| BP SIG | ✅ | ✅ | ✅ | ✅ | **Yes** |
| Thermometer | ✅ | ✅ | ✅ | ✅ | **Yes** |
| Glucose | ✅ | ✅ RACP | ✅ | ⚠️ use SIG + app seq logic | **Yes with SIG doc** |
| Pulse ox | ✅ | ✅ | ✅ | ✅ (validate bits) | **Yes** |
| BF700 scale | ✅ | ✅ opcodes | ✅ state machine | ⚠️ measurement body | **Yes for control; measure parse next** |
| BF600 / Wi-Fi scale | ✅ UUIDs | ⚠️ many cmds | ⚠️ | ⚠️ | **Partial** |
| AS87/98/99 | ✅ UUIDs | ⚠️ per-repo | ⚠️ | ⚠️ | **Partial (structure done)** |
| ECG | ✅ UUIDs + BP | ⚠️ | ⚠️ | BP ✅ / ECG ⚠️ | **BP yes; ECG partial** |

---

## Suggested implementation order (Tier 1+2 only)

1. **BP client** — largest device count, fully specified  
2. **Thermometer** — fully specified 13-byte parse  
3. **Glucose** — RACP flow from app + SIG measurement  
4. **Pulse ox** — proprietary but recovered  
5. **BF700 scale** — opcode table complete; finish measurement parser class  
6. **Trackers** — extract opcodes repo-by-repo (mechanical)  
7. **ECG waveform** — last (most complex proprietary)

---

## Code map (decompiled)

| Profile | Key classes |
|---------|-------------|
| BP | `oi/o.java` BloodPressureDeviceSyncRepoImpl, `gi/BTBloodPressureMeasurementDataMapper.java`, `fj/a.java` |
| Glucose | `fk/Gl50SyncRepo.java` |
| Thermo | `sk/TemperatureSyncRepoImpl.java`, `et/b.java` |
| PulseOx | `…/pulsoxy/PulseOxyDeviceSyncRepoImpl.java`, `ii/b.java` |
| BF700 | `BTScaleBF700Commands.java`, `BTScaleBF700GetMeasurementsRepoImpl.java` |
| Scale cfg | `ai/ScaleGeneralServiceConfigurationImpl.java` |
| Trackers | `…/activitytracker/as87|as98|as99/*` |
| ECG | `ai/ECGGeneralDeviceConfigurationImpl.java`, `si/*`, `ri/*`, `ti/*` |

---

### Bottom line

**Yes — for Tier 1 and most of Tier 2 you can get complete connect + send/receive + parse data from the APK.**

- **Fully complete now:** Blood pressure, thermometer, pulse-ox framing, glucose session control (RACP), BF700 command set.  
- **Structurally complete, field layouts need a short extraction pass or live capture:** tracker opcodes, BF600/Wi-Fi scale payloads, ECG sample streams.  

Tier 3 is not required for a broad multi-device BLE product if you ship the table above as plugins.

# FORA first-party protocol (iFORA Smart 1.5.9) — reliability edition

**Canonical doc** for implementing companion-like FORA/TaiDoc BLE support.  
**Status:** Host-side behaviour recovered from full jadx decompile.  
**Runtime:** `medical_ble_toolkit/brands/fora/`  
**Machine constants:** `EXACT_PROTOCOL_FROM_APK.json`  
**Supersedes:** `IFORA_SMART_APK_BLE_FINDINGS.md` (early string-mine notes; keep for history only)

---

## 0. Reliability checklist (use this when debugging)

| # | Check | Companion rule | Toolkit |
|---|--------|----------------|---------|
| 1 | Scan mode | `SCAN_MODE_LOW_LATENCY` (2), reportDelay 0 | Prefer aggressive scan |
| 2 | Name filter | substring `fora\|td-\|tng\|diamond\|taidoc\|sootheneb` | `name_matches_series` |
| 3 | Connect | `connectGatt(..., autoConnect=false, transport=LE=2)` | Bleak LE connect |
| 4 | Connect timeout | 10 s → fail + `refresh()` + close | `connect_timeout_ms` |
| 5 | After connect | wait **500 ms** then discover | session delay |
| 6 | GATT | service `1523`, char `1524` | constants |
| 7 | Notify | CCCD enable notify on first descriptor | `start_notify` |
| 8 | After CCCD | wait **500 ms** then start import | session delay |
| 9 | TX write | **WRITE_NO_RESPONSE** | `response=False` |
| 10 | TX frame | 7-byte template + sum checksum | `frame_bytes` |
| 11 | RX accept | start `0x51`, penultimate **`0xA5`**, checksum OK | `is_valid_app_rx` |
| 12 | Cmd timeout | **5 s** default; **15 s** set-time; **15 s** SpO2 poll | `TIMINGS` |
| 13 | Retries | max **12** (TNG BP forces 12) | `_transact` |
| 14 | On fail | disconnect + **refresh** cache + close | session finally |
| 15 | Import order | set-time → SN1 → SN2 → project → (path) → count → records → power-off `0x50` | `run_history` |
| 16 | Empty history | still power-off; finish import | `power_off` |
| 17 | PIN | app default **`111111`** (not cloud strings) | pair hint |

---

## 1. Recovery sources

| Artifact | Path |
|----------|------|
| XAPK | `iFORA+Smart_1.5.9_APKPure.xapk` |
| jadx sources | `extracted/decompiled/sources/com/foracare/tdlink/sm/` |
| Key classes | `LibraryConstant`, `MeterCommand`, `TDLinkConst`, `LibraryEnum`, `ByteUtils`, `BluetoothUtils`, `MeterPairUtils`, `iMeterConnService` |
| Toolkit | `brands/fora/protocol.py`, `session.py`, `plugin.py`, `parsers/fora.py` |

---

## 2. GATT / Android BLE API flags

```
ScanSettings.Builder()
  .setScanMode(2)      // SCAN_MODE_LOW_LATENCY
  .setReportDelay(0)
  .build()

device.connectGatt(ctx, /*autoConnect*/ false, callback, /*TRANSPORT_LE*/ 2)
```

| Flag | Value | Why it matters |
|------|-------|----------------|
| autoConnect | **false** | Direct connect only (no background auto) |
| transport | **LE = 2** | Avoid dual-mode BR/EDR path |
| Write type | **NO_RESPONSE (1)** | Faster; match companion |
| Fail path | disconnect → **refresh()** → close | Clears Android GATT cache (status 133 recovery) |

**GATT map**

| Role | UUID |
|------|------|
| Service | `00001523-1212-efde-1523-785feabcd123` |
| Char (notify + write) | `00001524-1212-efde-1523-785feabcd123` |

After services discovered:

1. `getService(1523).getCharacteristic(1524)`  
2. `setCharacteristicNotification(true)`  
3. First descriptor → `ENABLE_NOTIFICATION_VALUE` + `writeDescriptor`  
4. On success: **Timer 500 ms** → `gotoImportActivity` / `sendStartToBle`  

---

## 3. Wire frame (TaiDoc bus)

### 3.1 Layout

| Off | TX | RX |
|-----|----|----|
| 0 | `0x51` | `0x51` (WS long may use `0x77`) |
| 1 | command | echo command |
| 2–5 | message | message |
| 6 | **`0xA3` Out** | **`0xA5` In** (required by app RX check) |
| 7 | checksum | checksum |

**Checksum:** `sum(bytes[0..n-2]) & 0xFF` for RX validation over whole frame except last (`ByteUtils.calculateCheckSum`).  
TX: `appendOneByteCheckSumToCmd` sums entire 7-byte template including direction.

### 3.2 All MeterCommand templates (pre-checksum)

| Name | Template (decimal) |
|------|---------------------|
| project_code | 81,36,0,0,0,0,163 |
| record_number | 81,43,0,0,0,0,163 |
| record_datetime | 81,37,0,0,0,0,163 |
| record_value | 81,38,0,0,0,0,163 |
| power_off | 81,**80**,0,0,0,0,163 → **cmd 0x50** |
| serial_1 | 81,40,0,0,0,0,163 → **0x28** |
| serial_2 | 81,39,0,0,0,0,163 → **0x27** |
| set_datetime | 81,51,0,0,0,0,163 → **0x33** |
| firmware | 81,41,0,0,0,0,163 |
| clear_memory | 81,82,0,0,0,0,163 → **0x52** |
| set_ble_status | 81,57,255,255,255,255,163 |
| get_new_spo2 | 81,73,0,0,0,0,163 |
| set_monitor_sw_stop | 81,71,0,0,0,0,163 |
| ws_read | 81,113,2,0,0,0,163 → **0x71** (project 2560 uses ws_read2 **0x77**) |
| peakflow_* | see JSON (cmds 17–25, 32, 58–59, 64, 106–107) |
| set_ws_user_profile | long: 81,120,10,…,163 |

Full list in `EXACT_PROTOCOL_FROM_APK.json` → `command_templates_pre_checksum` and `protocol.COMMAND_TEMPLATES`.

### 3.3 Packing helpers

**Set datetime**

```
[2] = month*32 + day
[3] = (year-2000)*2 + (1 if month>7 else 0)
[4] = minute
[5] = hour
```

**Record index (getRecord1/2)**

```
[2] = index & 0xFF
[3] = (index >> 8) & 0xFF
[5] = userNo
```

**Serial**

```
chunk = sprintf("%02x%02x%02x%02x", data[5], data[4], data[3], data[2])
serial = sn1_chunk + sn2_chunk   # expect 16 hex chars
```

**Project code**

```
projectNo = sprintf("%02x%02x", data[3], data[2])  // e.g. 4272, 3250
```

**Record datetime decode** (from record1 message → tmpByte[0..3])

```
year   = (tmp1 // 2) + 2000
month  = ((tmp0 & 0xE0) // 32) + ((tmp1 & 1) * 8)
day    = tmp0 & 0x1F
hour   = tmp3 & 0x1F
minute = tmp2 & 0x3F
```

**BG value** (record2 message)

```
value = msg[0] | (msg[1] << 8)   # LE u16
invalid if value == 0xFFFF
meal: msg[3] bit0x40=AC, bit0x80=PC
valid range 20..600 mg/dL
```

---

## 4. Pairing & multi-slot inventory

| Item | Detail |
|------|--------|
| PIN constant | **`111111`** (`LibraryConstant.METER_PIN_CODE`) |
| Cloud strings | `iforasmartgm` / `…123` — **not** BLE PIN |
| Name filter | `checkMeterSeries` contains prefixes above |
| Slots | **12** max (`MeterPairUtils.maxCounts`) |
| Prefs | `BLE_PAIRED_METER_{ADDR,NAME,INIT}_` + index |
| `getPairedPos` | scan slots 0..10 for MAC match |
| `needRemoveRecord` | returns INIT pref; `"0"` used as wipe flag for re-import |
| Bond required | `MESSAGE_STATE_CONNECTED_METER_NOT_PAIRED_WITH_ANDROID_PHONE` (6) |

---

## 5. Timings (authoritative)

| Event | ms | Source |
|-------|-----|--------|
| Scan window / MSG_SCAN_TIMEOUT | **3000** | `deviceScan` |
| Connect overtime MSG_CONNECT_OVER_TIME | **10000** | `WorkRequest.MIN_BACKOFF_MILLIS` |
| STATE_CONNECTED → discover message | **500** | `onConnectionStateChange` |
| CCCD written → start import | **500** | `Timer.schedule` |
| Command response (most) | **5000** | `sendDataToBle` |
| Set-datetime response | **15000** | special-case |
| SpO2 empty poll retry window | **15000** | `timeoutHandler` |
| Max command retries | **12** | `retryCount` |
| Comm timeout → FINISH_IMPORT | **200** | `sendEmptyMessageDelayed(16,200)` |
| HTC extra delays | 3000 / 2000 | `LibraryConstant` |

---

## 6. Import FSM (full companion path)

```
connectGatt(autoConnect=false, TRANSPORT_LE)
  → 500 ms → discoverServices
  → enable notify 1524 + CCCD
  → 500 ms → sendStartToBle
       ImportHandler: READY(1) + CONNECTING(9)
       setDateTime (0x33)  [timeout 15s]
         → onSetDateTime → getSerialNumber1 (0x28)
         → getSerialNumber2 (0x27)  [serial must be ≤16 hex chars]
         → getCodeProject (0x24)
              projectNo = %02x%02x(data[3],data[2])
              set userNo (special models)
              getDataType(true) → importType
              if SpO2(7): firmware → maybe setBLEStatus → poll NewSpO2
              elif PeakFlow(31): peakflow info path
              else: firmware → getRecordsNumberForUserNo(userNo)
         → if count==0: IMPORT_SUCCESS(12) + powerOff + FINISH@200ms
         → else by type:
              WS(8): long readWSMeasureValue frames 34/40
              PeakFlow(31): peakflow records
              BG/BP/MP/TM/…: for i in 0..count-1:
                   getRecord1 datetime → fill tmpByte[0..3]
                   getRecord2 value    → fill tmpByte[4..7] → decode → store
         → powerOff (0x50)
```

### 6.1 `getDataType(projectNo)` map

| projectNo prefix | importType | Meaning |
|------------------|------------|---------|
| `32` (status=true) | **6** | Multiparam / 2-in-1 family |
| `32` after record1 bit7 | **2 or 1** | BP if `(tmp[2]&0x80)` else BG |
| first char `1` | **60** | Thermometer |
| first char `2` | **8** | Weight scale |
| first char `3` | **2** | BP |
| first char `4` | **1** | Glucose |
| first char `8` | **7** | SpO2 |
| `73` | **31** | Peak flow |
| `70` | **32** | Nebulizer |

Examples: `4272` → BG; `3250` → MP(6); `3140` → BP; `8201` → SpO2.

### 6.2 Special project codes

| Code | Behaviour |
|------|-----------|
| 3280, 3128 | force `userNo = 1` |
| 3261, 2560, 3132, 2555 | `userNo` from CaseProfile |
| 2560 | use **ws_read2** (0x77) not ws_read |
| 7301 | `peakflowType = data[5]` from project response |
| 4255 | special record-count handling |
| 3140 | alternate IHB bit layout for BP |

### 6.3 Record count parse

| importType | Formula (from full frame data[]) |
|------------|-----------------------------------|
| 8 (WS) | `data[2] \| (data[3] << 8)` little-endian |
| other | decompiled as `data[2] \| data[3]` **without shift** (hi often 0) |

---

## 7. Message / state codes

### GATT MainHandler `MSG_*`

| Code | Name |
|------|------|
| 1 | CONNECT_FAIL |
| 2 | DISCONNECTED |
| 3 | RECONNECT |
| 4 | DISCOVER_SERVICES |
| 5 | DISCOVER_FAIL |
| 6 | DISCOVER_SUCCESS |
| 7 | CONNECT_OVER_TIME |
| 8 | SCAN_ON |
| 9 | SCAN_TIMEOUT |
| 16 | SCAN_RESULT |
| 17 | SCAN_FAIL |

### ImportHandler `MESSAGE_STATE_*`

| Code | Name | Meaning |
|------|------|---------|
| 1 | READY_START_IMPORT | |
| 2 | NEED_TO_PAIR_DEVICE | bond first |
| 6 | NOT_PAIRED_WITH_PHONE | |
| 7 | EXCEED_RETRY_TIMES | after 12 cmd retries |
| 8 | NOT_SUPPORT_METER | |
| 9 | CONNECTING_METER | |
| 10 | CONNECT_METER_SUCCESS | |
| 11 | IMPORTING | |
| 12 | IMPORT_SUCCESSFUL | also used when 0 records |
| 13 | NO_RECORD_TO_IMPORT | |
| 14 | COMMUNICATION_TIMEOUT | |
| 16 | FINISH_IMPORT | |
| 18 | TURN_OFF_METER | |

GATT status **133**: clear connect-overtime; common Android race.

---

## 8. Clinical / multiparam notes (decompile, HCI still recommended)

### BG / multiparam (`onGetRecord2`)

1. Record1 fills `tmpByte[0..3]`; record2 fills `[4..7]` (or peakflow uses [16..19]).  
2. Decode datetime as §3.3.  
3. Primary value often `tmpByte[4]` as float for BG-like; type code `i22` selects HCT/HB/KT/UA/CHOL ranges.  
4. `hctCombine` 0/1/2 tracks multi-strip combos; may skip storing last indices when combining.  
5. **32xx**: may reclassify each record as BG vs BP via bit7.  
6. BP IHB decode variants for 3140 vs 32xx vs default (pulse 50/110 thresholds).

**This branch is partially obfuscated in jadx** — implement BG datetime+value first; refine multiparam with HCI golden samples per projectNo.

### MeasurementType wire values

`BG=1, BP=2, SpO2=7, BodyWeight=8, PeakFlow=31, Nebulizer=32, Ear=60, ForHead=61, HCT=90, HB=91, KT=92, LAC=93, UA=94, CHOL=95, TG=96`

### Clinical clamp ranges (`LibraryConstant`)

BG 20–600, KT 0.1–8, HCT 0–70, HB 6.8–24, UA 3–20, CHOL 100–500, LAC 0.3–23, TG 0–300.

---

## 9. Device catalog

**MeterModel enum:** TD3223…TD2599 (31 models) — full list in JSON.  

**UI labels:** FORA 6 CONNECT, D40, GD40, IR20/42, MD/MD6, O2, P20/P30+/P80, PREMIUM V10, Spiro10/20, W550/W600, TNG family, Diamond cuff, SootheNeb NBL300.

---

## 10. Toolkit mapping

| Companion | Toolkit |
|-----------|---------|
| Constants / templates / decode helpers | `brands/fora/protocol.py` |
| Bleak session FSM | `brands/fora/session.py` |
| DevicePlugin | `brands/fora/plugin.py` |
| Profile fora6 | `brands/fora/profiles.py` |
| Frame/BG parse | `parsers/fora.py` |
| Unit tests (no HW) | `tests/test_fora_protocol.py` |

### Implemented from APK (no HCI required)

| Feature | Status |
|---------|--------|
| set-time → SN → project → firmware → count → records → power-off | **Yes** |
| Empty history still power-off + ok | **Yes** |
| Special userNo (3280/3128) | **Yes** |
| SpO2 pre-path (firmware / setBLEStatus / NewSpO2 poll) | **Yes** (structure) |
| WS long-frame reassembly 34/40 | **Yes** (raw store) |
| BG decode (value + meal + datetime) | **Yes** |
| BP decode (sys/dia/pulse + IHB flag) | **Yes** (APK layout) |
| RX A5 + checksum gate | **Yes** |
| Cmd retries / timeouts | **Yes** |

### Still HCI-gated

FORA 6 multiparam secondary fields (HCT/HB/KT/UA/CHOL packing), peak-flow numeric scale factors, full WS body map.

---

## 11. Still HCI-gated (honest)

1. Exact multiparam field packing per FORA 6 project code  
2. Whether PIN `111111` is ever written over GATT (vs classic only)  
3. Confirm record-count non-WS formula on real meters (decompile lacks `<<8`)  
4. Long WS 34/40 body field map  
5. Peak-flow numeric scaling (`PeakFlowVo.convertValue*`)

When capturing:

```powershell
python phoneblelog\btsnoop_to_text.py <fora.cfa> -o datasheets\FORA\hci_fora.txt --skip-num-completed
```

---

## 12. Example frames

| Cmd | TX hex (with checksum) |
|-----|------------------------|
| Project | `51 24 00 00 00 00 a3 18` |
| Record value idx=3 | `51 26 03 00 00 00 a3 1d` |

Rebuild: `python -c "from medical_ble_toolkit.brands.fora import protocol as P; print(P.cmd_project_code().hex())"`

---

*Last reliability pass: 2026-07-23 — full MeterCommand set, connect/scan flags, getDataType map, serial/datetime decode, message codes, paired-slot rules.*

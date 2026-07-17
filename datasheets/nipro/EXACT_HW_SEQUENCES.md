# Nipro げんきノート 2.19.0.0 — exact app→device sequences

**Decompiler:** ilspycmd **10.1.1.8388** on .NET **10.0.302**  
**Assemblies:** XALZ-decompressed `BLELib.dll` / `NHL.dll`  
**Sources:** `decompiled_cs/BLELib/…`  
**Machine-readable dump:** `EXACT_PROTOCOL.json` + `EXACT_PROTOCOL.md`

All multi-byte integers below are **little-endian** unless noted.

---

## Global

| Item | Exact value |
|------|-------------|
| Scan mode default | `Mode = 2` → `ScanMode.Balanced` |
| Scan timeout | **argument** from UI (often 30000 ms) |
| Connect | `ConnectParameters(autoConnect: true, forceBleTransport: true)` |
| Pairing timeout | **30000 ms** (`PAIRING_REQUEST_TIMEOUT`) on CFL/NBP1/NSM1/NT100B/UM212 |
| Post-disconnect delay | **100 ms** |
| Name match | `advertisedName.Contains(DeviceName)` |

### NHL scan prefixes (`Constants.cs`)

| Prefix | Class |
|--------|--------|
| `NIPRO CF` | BLEDeviceCFL |
| `NSM-1BLE` | BLEDeviceNSM1 |
| `NT-100B` | BLEDeviceNT100B |
| `NBP-1BLE` | BLEDeviceNBP1 |
| `NBCM` | BLEDeviceNBCM |
| *(also in BLELib)* `NMBP` | BLEDeviceUM212 |
| *(also in BLELib)* `MightySat` | BLEDeviceMightySat |

---

## 1. Blood pressure — `NBP-1BLE` / `NMBP` (UM212)

**Identical receive path.**

### GATT

| Role | UUID |
|------|------|
| BLS service | `00001810-0000-1000-8000-00805F9B34FB` |
| Date Time char | `00002A08-0000-1000-8000-00805F9B34FB` |
| BP Measurement char | `00002A35-0000-1000-8000-00805F9B34FB` |
| DIS service (pair) | `0000180A-0000-1000-8000-00805F9B34FB` |
| Serial Number (pair) | `00002A25-0000-1000-8000-00805F9B34FB` |

### App → device: clock write (only host command on receive)

```
char 0x2A08  WRITE  (WithResponse via plugin default)
payload (7 bytes):
  [0..1] year   uint16 LE  (DateTime.Now.Year)
  [2]    month  uint8
  [3]    day    uint8
  [4]    hour   uint8
  [5]    minute uint8
  [6]    second uint8
```

Example for 2026-07-16 14:30:05:

```
EA 07 07 10 0E 1E 05
```

(`0x07EA` = 2026)

### Sequence (receive)

1. Connect  
2. Wait DeviceConnected  
3. **Delay 1000 ms**  
4. **WRITE** datetime → `0x2A08`  
5. **READ** `0x2A08` (verify)  
6. **StartUpdates** on `0x2A35` (indicate)  
7. Wait until timeout or connection-lost  
8. Disconnect + **Delay 100 ms**

### Pairing (UM212/NBP1)

1. Connect + bond (Android bond receiver on UM212)  
2. **READ** serial `0x2A25`  
3. Timeout 30 s  
4. Disconnect  

**No custom opcode writes** on BP receive path.

---

## 2. Thermometer HTP — `NSM-1BLE`

### GATT

| Role | UUID |
|------|------|
| HTS service | `00001809-0000-1000-8000-00805F9B34FB` |
| Date Time | `00002A08-0000-1000-8000-00805F9B34FB` |
| Temp Measurement | `00002A1C-0000-1000-8000-00805F9B34FB` |
| Custom service (A&D-style) | `233BF000-5A34-1B6D-975C-000D5690ABE4` |
| Custom characteristic | `233BF001-5A34-1B6D-975C-000D5690ABE4` |

### App → device: clock (receive)

Same **7-byte** datetime as BP, written to HTS `0x2A08` after **Delay 1000 ms**.

Then **StartUpdates** on `0x2A1C`.

### App → device: custom commands (defined)

| Name | Exact bytes |
|------|-------------|
| `SET_TIME_AS_HEADER` | `08 01 01` |
| `DISCONNECT` | `02 01 03` |
| `READ_SET_TIME_AND_DATE` | `02 00 04` |
| `UNPAIR` | `02 01 10` |
| `DELETE_ALL_MEMORY` | `02 01 12` |
| `SET_BUFFER_SIZE_AS_HEADER` | `03 01 A6` |
| buffer value 0 | `00` |
| buffer value 1 | `01` |
| `READ_DEVICE_SETTINGS` | `02 00 DB` |
| `REQUEST_TO_SEND_DATA_IN_BUFFER` | `02 00 E1` |

**Pairing path actually writes:** `DISCONNECT` = `02 01 03` to custom char `233BF001` after serial read.

Framing style = A&D UA-651: `[Size][Type][Cmd][Value…]`  
- Type `0` = read, `1` = write  
- Matches `sdk_ble_ua-651ble` custom buffer semantics.

---

## 3. Non-contact thermometer — `NT-100B`

### GATT

| Role | UUID |
|------|------|
| HTS | `00001809-…` / Temp `00002A1C-…` (receive path) |
| Custom service | `00001523-1212-EFDE-1523-785FEABCD123` |
| Custom write/notify | `00001524-1212-EFDE-1523-785FEABCD123` |

### Frame format (host → device)

```
[0]     0x51          start
[1]     CMD           command
[2..]   data          optional
[n]     0xA3          stop (host→device)
[n+1]   checksum      sum(bytes[0..n]) & 0xFF
```

`MakeCommand` always prepends `0x51`, appends `0xA3`, then checksum of entire frame so far.

### Command IDs (exact)

| CMD | Hex | Name |
|-----|-----|------|
| 35 | `0x23` | ReadDeviceClockTime |
| 36 | `0x24` | ReadDeviceModel |
| 37 | `0x25` | ReadTheStorageDataWithIndexTime |
| 38 | `0x26` | ReadTheStorageDataWithIndexResult |
| 39 | `0x27` | ReadDeviceSerialNumber1 |
| 40 | `0x28` | ReadDeviceSerialNumber2 |
| 43 | `0x2B` | ReadStorageNumberOfData |
| 51 | `0x33` | WriteSystemClockTime |
| 65 | `0x41` | StartAnInfraRedTemperatureMeasurement |
| **80** | **`0x50`** | **TurnOffTheDevice** ← **actually written** |
| 82 | `0x52` | ClearDeleteAllMemory |
| 84 | `0x54` | NotificationForEnteringCommunicationMode |

### TurnOff wire (only custom write on normal receive teardown)

```
MakeCommand(0x50, [0,0,0,0])
= 51 50 00 00 00 00 A3 CS
CS = (0x51+0x50+0+0+0+0+0xA3) & 0xFF = 0x44
```

Exact frame:

```
51 50 00 00 00 00 A3 44
```

Written to `00001524-1212-EFDE-1523-785FEABCD123`.

### Receive sequence

1. Connect (default timeout **60000 ms**)  
2. StartUpdates on HTP `0x2A1C`  
3. Wait for one indication → parse IEEE-11073 temp  
4. Finally: **TurnOff** write + **Delay 1000** + disconnect + **Delay 100**

**No clock write** and **no Start IR measure write** on this app’s receive path (those CMDs exist but unused here).

---

## 4. Glucose — `NIPRO CF` (Cocoron / CFL)

### Services / chars (exact)

| Name | UUID |
|------|------|
| Glucose Meter Service | `5D87A4A0-E42D-11E5-BEEF-0002A5D5C51B` |
| Glucose Measurement | `5D87A4A1-E42D-11E5-BEEF-0002A5D5C51B` |
| Glucose Measurement Context | `5D87A4A2-E42D-11E5-BEEF-0002A5D5C51B` |
| Record Access Control Point | `5D87A4A3-E42D-11E5-BEEF-0002A5D5C51B` |
| Glucose Feature | `5D87A4A4-E42D-11E5-BEEF-0002A5D5C51B` |
| Extension Service | `7A1A0001-8D7F-1727-A23F-DEDB5BF5DF46` |
| Target Glucose Concentration | `7A1A0002-8D7F-1727-A23F-DEDB5BF5DF46` |
| Remote Device Information | `7A1A0003-8D7F-1727-A23F-DEDB5BF5DF46` |
| Time and Alarm Service | `87F60001-A469-1EF4-637F-78B96A6F358B` |
| Current Time | `87F60002-A469-1EF4-637F-78B96A6F358B` |
| Device Info (custom) | `8E5996E0-E42F-11E5-AF97-0002A5D5C51B` |
| Serial Number | `8E5996E3-E42F-11E5-AF97-0002A5D5C51B` |

### Clock write (`TimeSetting`) — WithResponse

Year is encoded as **hex digit pairs of the decimal year string**, not plain LE uint16:

```
year_str = year.ToString("x4")   // e.g. 2026 → "07ea"
byte0 = parse(year_str[2:4], hex)  // "ea" → 0xEA
byte1 = parse(year_str[0:2], hex)  // "07" → 0x07
then: month, day, hour, minute, second
```

For year 2026 this still yields `EA 07` (same as LE uint16), but the algorithm is hex-string based.

### RACP writes — WithResponse on `5D87A4A3-…`

| Mode | Step | Exact payload |
|------|------|---------------|
| **All** | number of records | `04 01` |
| **All** | report all records | `01 01` |
| **Last** | number of records | `04 03 01 01 00` (seq=1) |
| **Last** | report | `01 03 01` + MaxSeqNo LE uint16 |
| **Diff** | number of records | `04 03 01` + LastId LE uint16 |
| **Diff** | report | `01 03 01` + LastId LE uint16 |

Template for 5-byte forms:

```
[op, filter, operator, seq_lo, seq_hi]
number-of-records: op=0x04
report-records:    op=0x01
filter:            0x03 = sequence number
operator:          0x01 = equals / greater-or-equal style used by meter
```

### Pairing write — Remote Device Information

```
default:  00 01
or:       [CliantType, AutoTransferMode]
  CliantType: 0=Own, 1=Family, 2=Hospital
  AutoTransferMode: 0=Off, 1=On
```

### Notify enable

StartUpdates on: Glucose Measurement, Context, RACP.

### Timeouts

- Pairing helper: **30000 ms**  
- Receive overall: caller timeout minus elapsed setup time  

---

## 5. SpO2 — `MightySat`

### GATT

| Role | UUID | Direction |
|------|------|-----------|
| Service | `54c21000-a720-4b4f-11e4-9fe20002a5d5` | |
| INCOMING (RX on device) | `54c21001-a720-4b4f-11e4-9fe20002a5d5` | **Host WRITE WithoutResponse** |
| GOINGOUT (TX from device) | `54c21002-a720-4b4f-11e4-9fe20002a5d5` | Notify |

### Frame builder (`MakeCommand`)

```
payload = [CMD] + optional data
crc     = CRC8_CCITT(payload)   // poly 0x07
frame   = [0x77, LEN, ...payload, crc]
LEN     = len(payload)+1        // includes crc, excludes SOM+LEN
```

### Commands (CMD byte)

| CMD | Hex | App use |
|-----|-----|---------|
| GetDeviceInformation | `0x01` | **sent first** after notify on |
| SetClock | `0x02` | after info; data = `BitConverter.GetBytes(DateTime.UtcNow.Ticks)` (**8 bytes**) |
| EnableStream | `0x03` | after ACK of set clock; data = 3 bytes from device info `[3],[4],[5]` |
| Waveforms | `0x04` | defined |
| Parameters | `0x05` | **received** stream |
| GetTrendRecord | `0x06` | defined |
| ClearallTrend | `0x07` | defined |
| Ack | `0xFE` | response |
| Nack | `0xFF` | error |

### Example: GetDeviceInformation only

```
payload = 01
crc = CRC8_CCITT([01]) = 0x07
frame = 77 02 01 07
```

### Sequence

1. Connect  
2. StartUpdates on GOINGOUT `…1002`  
3. **Send** GetInfo `0x01`  
4. On response: parse trend counts; **Send** SetClock `0x02` + UTC ticks  
5. On ACK(2): **Send** EnableStream `0x03` + 3 filter bytes  
6. On Parameters (`0x05`): parse SpO2/PR/PI; if bit21 “sensor off” → complete  
7. Disconnect + Delay 100  

**No host poll interval** — stream driven by device notifications.

---

## 6. Body composition — `NBCM`

### Key UUIDs

| Role | UUID |
|------|------|
| A&D custom service | `11127000-B364-11E4-AB27-0800200C9A66` |
| Custom WRITE | `11127001-B364-11E4-AB27-0800200C9A66` |
| Custom NOTIFY | `11127002-B364-11E4-AB27-0800200C9A66` |
| User Data service | `0000181C-0000-1000-8000-00805F9B34FB` |
| User Control Point | `00002A9F-0000-1000-8000-00805F9B34FB` |
| First Name | `00002A8A-…` |
| Birthday | `00002A85-…` |
| Sex | `00002A8C-…` |
| Height | `00002A8E-…` |
| Weight Scale | `0000181D` / `00002A9D` |
| Body Composition | `0000181B` / `00002A9C` |
| DIS Serial | `0000180A` / `00002A25` |

### DateTime write (10 bytes)

```
[year_lo, year_hi, mon, day, hour, min, sec, 0x00, 0x00, 0x00]
```

### Exact host command payloads

| Method | Char | Exact bytes | Notes |
|--------|------|-------------|-------|
| SetOperationMode | custom write | `04 01 05 0A XX` | XX = enum: `2` MeasurementMode, `3` SettingMode |
| RegisterNewUser | UCP `0x2A9F` | `01 E2 07` | + notify on UCP |
| UserAuthentication | UCP | `02 00 E2 07` (+ user index patched in full method) | Delay 1000 before start updates |
| UserNoAvailabilityCheck | custom | `03 01 14 12` | |
| DeleteSettings production | custom | `03 01 B0 00` | |
| DeleteSettings factory | custom | `03 01 AF 00` | |
| DeleteUser custom | custom | `04 01 14 13 XX` | XX = user no |
| DeleteUser UCP | UCP | `03` | |
| SetHeight custom | custom | `05 01 14 18 HH HH` | height LE |
| MeasurementResultDisplaySetting | custom | `04 01 05 20 01` | |
| BodyFatPercentageMeasurement | custom | `04 01 05 22 01` | |
| MedicalExaminationModeDisabled | custom | `04 01 05 28 00` | |
| AirplaneModeDisabled | custom | `04 01 05 2C 00` | |

### Delays

| When | ms |
|------|-----|
| iOS before serial read | **2000** |
| iOS after bond before serial | **1000** |
| Between user profile writes | **1000** |
| UserAuthentication pre-notify | **1000** |

### Receive

Uses Android dependency service `EnableBodyComposition` (platform hook); measurements via Weight + Body Composition notifications after user auth / step-on UX (`NBCM_STEP_ON/OFF`).

---

## 7. Quick “what does the app send?” matrix

| Device | Host writes anything? | What |
|--------|----------------------|------|
| NBP-1BLE / NMBP | **Yes** | 7-byte clock to BLS `0x2A08` only |
| NSM-1BLE | **Yes** | 7-byte clock to HTS `0x2A08`; pair: custom `02 01 03` |
| NT-100B | **Yes (teardown)** | TurnOff `51 50 00 00 00 00 A3 44` |
| NIPRO CF | **Yes** | Clock + RACP (`04 01`/`01 01` etc.) + pairing type |
| MightySat | **Yes** | Stream control frames on `…1001` (info/clock/stream) |
| NBCM | **Yes (many)** | A&D custom + User Data UCP/profile |

| Device | Fixed poll interval? |
|--------|----------------------|
| All BLS/HTP | **No** — indication-driven |
| MightySat | **No** — notify stream (~1 Hz device-side) |
| CFL | **No** — RACP request then notifications |

---

## Files generated

| File | Purpose |
|------|---------|
| `decompiled_cs/BLELib/**` | Full C# sources |
| `decompiled_cs/NHL/**` | App layer |
| `EXACT_PROTOCOL.json` | Auto extract (UUIDs, byte consts, write literals) |
| `EXACT_PROTOCOL.md` | Auto extract human view |
| `EXACT_HW_SEQUENCES.md` | **This file — curated exact wire sequences** |
| `NIPRO_BLE_DEVICE_MAP.md` | Datasheet mapping overview |
| `extracted/assemblies_decompressed/*.dll` | PE assemblies for ILSpy |

Open in ILSpy if you want to click through:

```
datasheets\nipro\extracted\assemblies_decompressed\BLELib.dll
```

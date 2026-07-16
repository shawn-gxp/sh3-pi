# Beurer HealthManager Pro — BLE Interaction Analysis

**App:** `com.beurer.healthmanager` 1.20.1  
**Source:** reverse-engineered from APK (jadx decompile of `classes.dex` / `classes2.dex`)  
**Primary focus:** Blood-pressure devices (BM54 and standard BP profile), with notes for other families  

**Multi-device coverage:** see **`MULTI_DEVICE_SUPPORT.md`** and machine-readable **`tools/device_registry.json`** (120 DeviceType models, protocol profiles, implementation roadmap).

Artifacts:
- Unpacked APK: `extracted/base/`
- Decompiled Java: `decompiled/full/sources/`
- Key classes mapped below (R8-obfuscated short names → original `.kt` sources)
- Device registry: `tools/device_registry.json`

---

## 1. Architecture overview

```
UI / ViewModels (pairing, sync screens)
        │
        ▼
BTDeviceSyncLogic / DeviceSearchAndConnectUseCase
        │
        ├─► KableDeviceSearch          (scan)
        └─► KableDeviceConnection      (connect, write, observe)
                │
                ▼
Device-family repos / use cases
  • GetBloodPressureDataUseCaseImpl  → BloodPressureDeviceSyncRepoImpl   (standard BP: BM54…)
  • BM59BloodPressureRepoImpl        (same GATT, slightly different timer path)
  • BM59SetTimeRepoImpl              (Current Time write)
  • Scale / Tracker / ECG / PulseOxy / Glucose repos (proprietary)
                │
                ▼
Parsers / mappers
  • BTBloodPressureMeasurementDataMapper  (IEEE 11073 SFLOAT BP payload)
  • fj.a (SFLOAT decoder)
  • ByteDataExtensions (time packing helpers)
                │
                ▼
Local DB + Health Connect / cloud sync
```

**BLE stack:** [Juul Kable](https://github.com/JuulLabs/kable) (primary) + leftover RxAndroidBle.

---

## 2. End-to-end flow (standard blood pressure / BM54)

This is what the app actually does for BM54-class devices.

### Phase A — Discovery (scan)

| Step | App action | Details |
|------|------------|---------|
| A1 | Ensure BT on | `BluetoothAdapter` state; abort if off |
| A2 | Start Kable scanner | `KableDeviceSearch` → log `"start scanning kable device search"` |
| A3 | Filter advertisements | Match **advertisement name** set for device type |
| A4 | Optional service UUID filter | **Discover UUID** from `DeviceType.getDiscoverUUID()` |

**BM54 identity (from decompiled `BM54.java`):**
- Advertisement name set: `{ "BM54" }`
- Storage name: `"BM54"`
- Discover UUID: from `AdvertisementServiceUuidProvider.g()` → **Blood Pressure Service**

```
discover UUID = 00001810-0000-1000-8000-00805f9b34fb
```

UUID construction (from `AdvertisementServiceUuidProviderImpl` / `BTUUIDHelperImpl`):

```java
// short 16-bit assigned number → full Bluetooth base UUID
new UUID(((shortId & 0xFFFFFFFFL) << 32) | 0x1000L, 0x800000805F9B34FBL);
// 6160 (0x1810) → 00001810-0000-1000-8000-00805f9b34fb
```

**Manufacturer-specific advertising (protocol PDF, confirmed useful for filtering):**
- Company ID Beurer: `0x0611`
- Payload style: `0x11 0x06 0x01` (+ newer `0x03` flag for passkey devices)
- AD type: `0xFF` Manufacturer Specific Data

### Phase B — Connect + bond

| Step | App action | Details |
|------|------------|---------|
| B1 | Create `KableDeviceConnection` | Holds `BTFoundDevice`, `DeviceType`, context |
| B2 | `peripheral.connect()` | With retry on `ConnectionLostException` |
| B3 | Wait services discovered | Log `"onServicesDiscovered device connected"` |
| B4 | Bonding if required | `BTDeviceTriggerAndWaitForBondingUseCase`; GATT auth fail → `"GATT_AUTH_FAIL"` / not bonded |
| B5 | Pairing process flag | `isPairingProcess` boolean passed into sync |

**Passkey (newer BM54, from protocol PDF):** 6-digit PassKey Entry.  
App surfaces system pairing UI; does not hardcode the PIN.

### Phase C — Subscribe to measurements (what is *sent*)

For **standard BP devices**, the app’s active write is essentially:

| Direction | What | Where | Value |
|-----------|------|-------|-------|
| **Phone → device** | Enable **indications** | CCCD `0x2902` on char `0x2A35` | `0x02 0x00` (`ENABLE_INDICATION_VALUE`) |
| Phone → device | (optional, some models) | Read DIS chars | strings |
| Phone → device | (BM59 path / time set) | Current Time `0x2A2B` | datetime bytes (see §5) |

**Characteristic observer setup** (`KableDeviceConnection.o` → `initCharacteristicListener`):

```
Service:        0x1810  (Blood Pressure)     = 6160 decimal
Characteristic: 0x2A35  (BP Measurement)    = 10805 decimal
Mode:           Indication (not notification)
CCCD write:     ENABLE_INDICATION_VALUE
```

Hardcoded in `BloodPressureDeviceConfigurationImpl`:

```java
return new a.C0451a(6160L, 10805L);  // service, characteristic
```

Same pair used by `BM59BloodPressureRepoImpl`.

**Important (protocol PDF + app behavior):**  
After a successful connection, the **BM54 automatically streams all stored measurements** (both users) as GATT indications. The phone does **not** send a “get history” command for standard BM54. Sync is “enable indications → collect until quiet/timeout/disconnect”.

### Phase D — Receive measurements (what is *received*)

| Direction | What | Format |
|-----------|------|--------|
| **Device → phone** | GATT Indication on `0x2A35` | Binary BP Measurement (Bluetooth SIG) |
| Device → phone | Multiple indications | One payload per stored measurement |
| App | Collect raw `byte[]` | `BloodPressureDeviceSyncRepoImpl` list `f48414d` |
| App | Log | `"Data received: " + hex` |
| App | Restart idle timer | `BTFlowTimer` (constructor arg `4` → multi-second quiet timeout) |
| App | On timeout / disconnect | `parseResponseData` → mapper → save |

Listener (`BloodPressureDeviceSyncRepoImpl$syncDeviceData$2` / `oi.r`):

```java
// each indication:
Timber: "Data received: <hex>"
bpDataList.add(payload)
timer.restart()
```

### Phase E — Parse + persist

```
byte[] payloads
  → BTBloodPressureMeasurementDataMapper.a(list, endianFlag, deviceData, deviceType)
  → List<SingleBloodPressureMeasurement>
  → BPMeasurementsFilterAndSaveRepo (dedupe/filter + DB)
```

---

## 3. Blood Pressure Measurement payload (exact layout)

Matches Bluetooth SIG Blood Pressure Profile **and** Beurer BM54 protocol PDF.  
Implemented in `gi/BTBloodPressureMeasurementDataMapper.java`.

### Byte map (little-endian multi-byte fields)

| Offset | Size | Field | Encoding | Notes |
|--------|------|-------|----------|-------|
| 0 | 1 | `flags` | bitfield | BM54 always `0x1E` per PDF |
| 1–2 | 2 | systolic | IEEE-11073 **SFLOAT** | mmHg if flag bit0=0 |
| 3–4 | 2 | diastolic | SFLOAT | |
| 5–6 | 2 | MAP | SFLOAT | BM54 always 0 |
| 7–8 | 2 | year | uint16 LE | e.g. 2015 = `0xDF 0x07` |
| 9 | 1 | month | uint8 | 1–12 |
| 10 | 1 | day | uint8 | 1–31 |
| 11 | 1 | hour | uint8 | 0–23 |
| 12 | 1 | minute | uint8 | 0–59 |
| 13 | 1 | second | uint8 | 0–59 |
| 14–15 | 2 | pulse rate | SFLOAT | endian may swap for some devices |
| 16 | 1 | user ID | uint8 | `0` = user1, `1` = user2 |
| 17–18 | 2 | measurement status | bitfield | IHB / HSD / AFib / body movement… |

**Total length:** typically **19 bytes** when all optional fields present (`flags=0x1E`).

### Flags byte (bit 0 = LSB)

| Bit | Meaning |
|-----|---------|
| 0 | Units: 0=mmHg, 1=kPa |
| 1 | Timestamp present |
| 2 | Pulse rate present |
| 3 | User ID present |
| 4 | Measurement status present |
| 5–7 | Reserved |

BM54: **always `0x1E`** = timestamp + pulse + userId + status (mmHg).

### SFLOAT decoder (app code `fj.a.a(low, high)`)

IEEE 11073 16-bit SFLOAT, little-endian as two bytes:

```
mantissa = low | ((high & 0x0F) << 8)   // 12-bit, sign-extended from bit 11
exponent = high >> 4                    // 4-bit, sign-extended from bit 3
value    = mantissa * 10^exponent
```

App implementation (decompiled):

```java
int mant = (bLow & 0xFF) + ((bHigh & 0x0F) << 8);
// sign-extend 12-bit mantissa if bit 11 set
int exp  = (bHigh & 0xFF) >> 4;
// sign-extend 4-bit exponent if bit 3 set
return (float)(Math.pow(10, exp) * mant);
```

For integer mmHg (exp=0): e.g. systolic 117 → `0x75 0x00`.

### Measurement status bits (app extracts these)

From mapper (device-type specific):

| Condition | Bit test (status byte) | Meaning |
|-----------|------------------------|---------|
| Common | `status & 0x04` | IHB / irregular pulse related |
| Common | `status & 0x40` / `0x80` | HSD variants |
| BM96 (`ag`) | status == -64 / -128; byte18 bit0 | AFib |
| BM64 (`uf`) | high bits `"01"`/`"00"`; byte18 bit5 | AFib |
| Some (`bi`) | status byte bit4 | extra flag |

Mapped into `SingleBloodPressureMeasurement`:
`time, systolic, diastolic, pulse, IHB, HSD, userId, deviceData, afib, extra`

### PDF example (verify against app parser)

```
Raw: 1E 75 00 4D 00 00 00 DF 07 01 0E 0A 37 00 48 00 01 00 00
     │  │     │     │     │        │  │  │  │  │  │     │  │
     fl sys   dia   MAP   year     m  d  h  mi s  pulse  u  st
```

| Field | Value |
|-------|-------|
| flags | `0x1E` |
| sys | 117 mmHg |
| dia | 77 mmHg |
| MAP | 0 |
| time | 2015-01-14 10:55:00 |
| pulse | 72 |
| user | 1 (user 2) |
| status | 0 (no error) |

---

## 4. What the phone sends vs receives (BM54)

### Sent (phone → BM54)

| Message | When | Payload |
|---------|------|---------|
| **CCCD enable indication** | After connect / start sync | On `0x2A35` descriptor `0x2902`: **`02 00`** |
| Pairing / bonding | First connect (if required) | OS-level SMP (passkey UI) |
| Optional DIS reads | After connect | Read requests on `0x180A` chars |
| Optional Current Time write | Some BP variants / set-time UX | See §5 (not required for BM54 pure measurement dump per PDF) |

There is **no proprietary “download command”** in the standard BP path used by `BloodPressureDeviceSyncRepoImpl`.

### Received (BM54 → phone)

| Message | When | Payload |
|---------|------|---------|
| **BP Measurement indication** | After connect (auto) + after each new measure | 19-byte layout above, one per stored record |
| Device Info responses | If read | UTF-8 strings (manufacturer, model, serial, FW, …) |

### Sync completion rules (from `BloodPressureDeviceSyncRepoImpl`)

1. Subscribe to indications on `(0x1810, 0x2A35)`.
2. For devices implementing marker interface `te`: wait **3 seconds** before subscribe (settling delay).
3. Collect every indication into `bpDataList`.
4. Each packet restarts `BTFlowTimer` (idle timeout; constructor `4`).
5. On quiet timeout → parse list → emit success.
6. On disconnect:
   - If pairing and not bonded and list empty → bonding/auth error
   - If list empty (non-pairing) → “no data” style error
   - If list non-empty → parse what was received

---

## 5. Set-time path (BM59 / shared ECG-BP stack)

Used by `BM59SetTimeRepoImpl` and ECG configs; service pair from `ECGGeneralDeviceConfigurationImpl.b()`:

| Role | Short ID | UUID |
|------|----------|------|
| Current Time Service | `0x1805` = 6149 | `00001805-0000-1000-8000-00805f9b34fb` |
| Current Time char | `0x2A2B` = 10795 | `00002a2b-0000-1000-8000-00805f9b34fb` |

**Write payload** (built in `BM59SetTimeRepoImpl.z0`):

```
year_lo, year_hi, month, day, hour, minute, second, dayOfWeek, 0x00, 0x00
```

- `year` = uint16 LE via `ByteDataExtensions.b(year, littleEndian=true)`
- `month` = Calendar.MONTH + 1
- `dayOfWeek` = Calendar.DAY_OF_WEEK − 1, with Sunday remapped to `7`
- Write type: `WITH_RESPONSE` or `WITHOUT_RESPONSE` depending on device marker `ii`
- Log line: `"Set time request: year=0x(...), month=0x..., ..."`

Local Time Information char `0x2A0F` is also known to the app (Current Time Service).

---

## 6. Device Information Service (read path)

`BTDeviceInfoType` enum values:

| Type | Typical UUID |
|------|----------------|
| DEVICE_NAME | `0x2A00` |
| MODEL_NUMBER | `0x2A24` |
| SERIAL_NUMBER | `0x2A25` |
| HARDWARE_REVISION | `0x2A27` |
| FIRMWARE_REVISION | `0x2A26` |
| SOFTWARE_REVISION | `0x2A28` |
| BATTERY_STATUS | Battery service / level |

Service DIS = `0x180A`.

---

## 7. Advertisement / discover UUID map (short IDs in app)

From `AdvertisementServiceUuidProviderImpl` methods:

| Method | Short ID | Full UUID use |
|--------|----------|----------------|
| `g()`, `i()`, `k()` | `0x1810` | **Blood pressure** (BM54 scan/discover) |
| `d()` | `0x1809` | Health Thermometer |
| `f()` | `0x1808` | Glucose |
| `m()` | `0x181D` | Weight Scale |
| `a()` | `0xFFF0` | Proprietary |
| `j()` | `0xFF12` | Proprietary |
| `l()` | `0xFFE0` | Proprietary |
| `e()` | `0xFFFF` | Wildcard / special |
| `b/c/h/n` | custom | AS98 / AS99 / AS81 / AS87 constants |

---

## 8. Other device families (summary)

These do **not** use pure SIG BP measurement dump.

### ECG + BP (BM93 / BM95 / BM96 / ME95)

| UUID | Role |
|------|------|
| `6E800001-B5A3-F393-E0A9-E50E24DCCA9E` | Custom service |
| `6E800002-...` | Command/data char |
| `6E800003-...` / `6E800004-...` | Alt command/notify |
| `6E801000-...` / `6E801001-...` | Firmware / OTA path |
| `0000A000-...` + `0x2A37` / `0x2A36` | HR / cuff pressure variants |

Commands are proprietary binary frames (header parsers: `BTEcgBM95HeaderRepoModelParser`, measurement count repos, etc.).

### Scales (BF600 / BF700 series)

- Custom command enums: `BTScaleBF700Commands` (`GET_MEASUREMENT_DATA_COMMAND`, `SET_TIME`, …)
- Frame validation: checksum-style (`fj.a.c` sum of bytes & 0x7F)
- Wi-Fi scales: TLS certs in `assets/certificates/`, OTA bins under firmware assets

### Activity trackers (AS87 / AS98 / AS99)

- Dedicated parsers under `btdeviceimpl/parser/activitytracker/`
- Custom service UUIDs in `As87Constants` / `As98Constants` / `As99Constants`

### Glucose / pulse ox / thermometer

- Scan UUIDs `0x1808`, `0xFFF0`-family, `0x1809`
- Own sync repos (`Gl50SyncRepo`, pulsoxy, temperature)

---

## 9. Key decompiled classes (quick index)

| Role | Decompiled path | Original source |
|------|-----------------|-----------------|
| BP sync (standard) | `oi/o.java` | `BloodPressureDeviceSyncRepoImpl.kt` |
| BP indication collector | `oi/r.java` | same |
| BP parse orchestration | `oi/p.java` | same |
| BP payload mapper | `gi/BTBloodPressureMeasurementDataMapper.java` | same name |
| SFLOAT | `fj/a.java` | internal util |
| Get BP use case | `bj/f.java` | `GetBloodPressureDataUseCaseImpl.kt` |
| BP service config | `ai/BloodPressureDeviceConfigurationImpl.java` | |
| BM59 BP repo | `oi/b.java` | `BM59BloodPressureRepoImpl.kt` |
| Set time | `oi/h.java` | `BM59SetTimeRepoImpl.kt` |
| Kable connection | `mj/e.java` | `KableDeviceConnection.kt` |
| Kable search | `nj/f.java`, `nj/g.java` | `KableDeviceSearch.kt` |
| UUID short→full | `qj/BTUUIDHelperImpl.java` | |
| Discover UUID table | `fq/AdvertisementServiceUuidProviderImpl.java` | |
| BM54 entity | `com/.../entities/device/BM54.java` | `BloodPressure.kt` |
| Service/char config types | `uj/a.java` | `BTServiceConfigLong(service, char)` |

---

## 10. Minimal reimplementation recipe (BM54)

```text
1. BLE scan for name "BM54" and/or service UUID 00001810-...
2. Connect + bond (handle 6-digit passkey if prompted)
3. Discover GATT
4. Optional: read 0x180A device info strings
5. Enable indications on 00002A35-... (write CCCD 02 00)
6. For each indication:
      parse 19-byte BP Measurement (table in §3)
7. Stop after ~few seconds with no new indications
8. Disconnect
```

No app-secret crypto is required for BM54 measurement download.  
Security is BLE pairing/bonding only (plus Android permissions).

Reference parser: `tools/bm54_ble_parser.py`

---

## 11. GATT services on BM54 (from protocol PDF + app)

| Service | UUID | Used by app? |
|---------|------|----------------|
| Generic Access | `0x1800` | implicit |
| Generic Attribute | `0x1801` | implicit |
| **Blood Pressure** | **`0x1810`** | **yes — core** |
| Device Information | `0x180A` | yes — metadata |
| Current Time (some BP) | `0x1805` | set-time path |

**Blood Pressure characteristics:**

| Char | UUID | Props | App use |
|------|------|-------|---------|
| Blood Pressure Measurement | `0x2A35` | Indicate | **subscribe + parse** |
| Intermediate Cuff Pressure | `0x2A36` | Notify | not used by BM54 |
| Blood Pressure Feature | `0x2A49` | Read | feature bits (IHB supported = `0x04 0x00`) |

---

## 12. Confidence notes

| Claim | Confidence | Evidence |
|-------|------------|----------|
| BM54 uses SIG BP service/measurement | **High** | PDF + `BloodPressureDeviceConfigurationImpl` + mapper |
| App enables indications, device pushes history | **High** | Sync repo + PDF auto-send after connect |
| Exact 19-byte field offsets | **High** | Mapper bytecode + PDF |
| SFLOAT algorithm | **High** | `fj.a` matches IEEE 11073 |
| No proprietary download command for BM54 | **High** | No write of command bytes in standard BP sync path |
| Passkey pairing for new BM54 | **High** | PDF + bonding use case + manufacturer flag |
| Exact idle timeout seconds | **Medium** | `BTFlowTimer(4, …)` — unit is seconds in practice but confirm on device |
| Manufacturer AD parse in app | **Medium** | Strings present; PDF is authoritative for AD layout |

---

## 13. Useful log tags when running the real app

```
start scanning kable device search
collect device ->
Call peripheral.connect()
onServicesDiscovered device connected
initCharacteristicListener
isPairingProcess ...
syncDeviceData BTServiceConfigLong(mainService=6160, characteristic=10805)
Data received: <hex>
BP Timeout call parseResponseData
bpDataList size: N
sys: X, dia: Y, pulse: Z
Parsed Measurements N
Start sync GetBloodPressureDataUseCaseImpl
```

Use these with `adb logcat` + a real BM54 to validate live traffic against this document.

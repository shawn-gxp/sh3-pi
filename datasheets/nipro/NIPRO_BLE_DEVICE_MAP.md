# Nipro げんきノート — BLE device map & hardware-layer protocol

**Source:** decompiled `BLELib.dll` + `NHL.dll` from `ニプロげんきノート_2.19.0.0`  
**Output:** `datasheets/nipro/decompiled_cs/`  
**App:** Xamarin.Forms / Mono (not pure Java)

---

## 1. Class → BLE name → datasheet → measurement type

| Class | `DeviceName` match | Advertised name prefix (app) | Type | Datasheet in `datasheets/nipro/` | Toolkit parser |
|-------|--------------------|------------------------------|------|----------------------------------|----------------|
| `BLEDeviceCFL` | `NIPRO CF` | `NIPRO CF` | Glucose (Cocoron / CF) | `Cocoron通信仕様書.pdf` | (custom — see CFL section) |
| `BLEDeviceNSM1` | `NSM-1BLE` | `NSM-1BLE` | Contact/skin thermometer (HTP) | HTP PDF + TICD-related timing | `htp.py` (indication) + A&D-like custom cmds |
| `BLEDeviceNT100B` | `NT-100B` | `NT-100B` | Non-contact IR thermometer | `【非接触体温計1-1】…TICD_Thermometer_v1.16…pdf` + HTP PDF | `thermometer.py` + `htp.py` |
| `BLEDeviceNBP1` | `NBP-1BLE` | `NBP-1BLE` | Blood pressure (standard BLS) | BLS + related BP docs | `blood_pressure.py` |
| `BLEDeviceUM212` | `NMBP` | `NMBP*` | Blood pressure (A&D-style / UA-651 family) | `【家庭血圧計】sdk_ble_ua-651ble_V1.2…pdf` | `and_ua651.py` + `blood_pressure.py` |
| `BLEDeviceNBCM` | `NBCM` | `NBCM` | Body composition / weight scale (A&D) | **No PDF in folder** — uses A&D custom `11127000-…` | (not yet) |
| `BLEDeviceMightySat` | `MightySat` | `MightySat` | SpO2 / PR / PI (Masimo) | `【SpO2】MightySat … CSD-1322B.pdf` | `mightysat.py` |

**Name matching rule** (`BLEManager.GetDevice`):

```csharp
DeviceList.FirstOrDefault(x => name.Contains(x.DeviceName));
```

So advertised names must **contain** the substring above (e.g. `NBP-1BLE-1234` → NBP1).

**NHL UI prefixes** (`NHL.ViewModels.Utils.Constants`):

| Constant | Value |
|----------|--------|
| `NIPRO_CF_PREFIX` | `NIPRO CF` |
| `NSM_PREFIX` | `NSM-1BLE` |
| `NT_100B_PREFIX` | `NT-100B` |
| `NBP_PREFIX` | `NBP-1BLE` |
| `NBCM_PREFIX` | `NBCM` |

`NMBP` and `MightySat` are implemented in BLELib but **not** listed in that UI constants set (still selectable via name match if discovered).

---

## 2. Global host / adapter behavior

| Setting | Value | Where |
|---------|-------|--------|
| Default scan mode | `ScanMode.Balanced` (`Mode = 2`) | `BLELib.Mode` |
| Scan timeout | **Caller-supplied** ms (`_IAdapter.ScanTimeout = timeout`) | `ScanStart(names, timeout, …)` |
| Pairing timeout (most devices) | **30_000 ms** | `PAIRING_REQUEST_TIMEOUT` |
| Connect parameters | `autoConnect: true`, `forceBleTransport: true` | all devices |
| Post-disconnect settle | `Task.Delay(100)` almost everywhere | finally blocks |
| Pre-datetime-write delay (BP / NSM) | `Task.Delay(1000)` | after connect, before clock write |
| Characteristic lookup retries | **3** | `CharacteristicAsyncOf` |
| NBCM iOS serial-read delay | **2000 ms** then **1000 ms** after bond | `GetSerialNumber` |
| NBCM Fujitsu Android special connect | `autoConnect: false`, retry **10× / 100 ms** | F-03K / F-42A / F-01L |
| NT-100B default connect timeout | **60_000 ms** | `DeviceConnect(timeoutMillisec = 60000)` |
| NT-100B after receive, before disconnect | **TurnOff + Delay(1000)** | finally of `ReciveStart` |

There is **no fixed measurement polling interval** for BLS/HTP devices: the host enables **indications/notifications** and waits until timeout or connection-lost / complete event.

MightySat live parameters arrive as device-driven **stream notifications** (protocol ~1 Hz per CSD-1322B / existing `mightysat.py` comments).

---

## 3. App → device writes (hardware layer)

### 3.1 Blood pressure — `BLEDeviceNBP1` (`NBP-1BLE`) & `BLEDeviceUM212` (`NMBP`)

**Almost identical stack.** Standard Bluetooth SIG BLS.

| Direction | Service | Characteristic | Action |
|-----------|---------|----------------|--------|
| Host → device | `0x1810` BLS | `0x2A08` Date Time | **Write** 7-byte clock |
| Device → host | `0x1810` | `0x2A35` BP Measurement | **Indicate** (StartUpdates) |
| Host ← device (pairing) | `0x180A` DIS | `0x2A25` Serial Number | **Read** (after bond on UM212) |

**Clock write payload (host → device):** little-endian year + mon/day/h/m/s:

```
[year_lo, year_hi, month, day, hour, minute, second]   // 7 bytes, local DateTime.Now
```

**Sequence (receive):**

1. Connect (`forceBleTransport`)
2. Wait connected
3. `Delay(1000)`
4. **Write** Date Time `0x2A08`
5. **Read** Date Time back (verify)
6. Enable indications on `0x2A35`
7. Wait for measurements until timeout / connection lost
8. Disconnect + `Delay(100)`

**No other custom opcodes** on these two classes in BLELib (no A&D `233BF000` path in UM212/NBP1 code paths).

**Measurement parse:** BLS flags + SFLOAT SBP/DBP/MAP + optional timestamp + pulse — standard.

---

### 3.2 Thermometer HTP — `BLEDeviceNSM1` (`NSM-1BLE`)

| Direction | Service | Characteristic | Action |
|-----------|---------|----------------|--------|
| Host → device | `0x1809` HTS | `0x2A08` Date Time | **Write** 7-byte clock (same layout as BP) |
| Device → host | `0x1809` | `0x2A1C` Temperature Measurement | **Indicate** |
| Host → device (pairing end) | `233BF000-5A34-1B6D-975C-000D5690ABE4` | `233BF001-…` | **Write** custom commands |

**Custom command constants (A&D-style header: Size | Type | Cmd | …):**

| Name | Bytes | Meaning (aligned with UA-651 SDK style) |
|------|-------|----------------------------------------|
| `SET_TIME_AS_HEADER` | `08 01 01` | write set-time header (value follows) |
| `DISCONNECT` | `02 01 03` | disconnect → standby |
| `READ_SET_TIME_AND_DATE` | `02 00 04` | read time |
| `UNPAIR` | `02 01 10` | unpair |
| `DELETE_ALL_MEMORY` | `02 01 12` | delete all memory |
| `SET_BUFFER_SIZE_AS_HEADER` | `03 01 A6` | set buffer size |
| `SET_BUFFER_SIZE_AS_VALUE_FOR_ZERO` | `00` | buffer none |
| `SET_BUFFER_SIZE_AS_VALUE_FOR_NINETY` | `01` | buffer on |
| `READ_DEVICE_SETTINGS` | `02 00 DB` | read settings |
| `REQUEST_TO_SEND_DATA_IN_BUFFER` | `02 00 E1` | request buffered data |

**App actually uses on pairing path:** write `DISCONNECT` (`02 01 03`) after serial/pair flow.

**Receive path:** clock write to `0x2A08` + HTP indications on `0x2A1C` (IEEE-11073 temperature).

> Note: custom service UUID is the **same family** as UA-651BLE (`and_ua651.py`). Datasheet `sdk_ble_ua-651ble` documents that custom framing; NSM1 reuses the command table even though primary measurement is HTP.

---

### 3.3 Non-contact thermometer — `BLEDeviceNT100B` (`NT-100B`)

**Primary data path (what the app uses on receive):** standard HTP indicate `0x2A1C` — **no continuous proprietary poll in ReciveStart**.

**App → device write that *is* issued:**

| When | Service | Char | Payload |
|------|---------|------|---------|
| End of receive (finally) | `00001523-1212-EFDE-1523-785FEABCD123` | `00001524-…` | **Turn off** command |

**Frame builder (`MakeCommand`):**

```
[0x51, CMD, ...data..., 0xA3, checksum]
checksum = sum(all previous bytes) & 0xFF
```

**Command IDs (defined; many unused in this build’s receive path):**

| ID | Name |
|----|------|
| `0x23` (35) | ReadDeviceClockTime |
| `0x24` (36) | ReadDeviceModel |
| `0x25` (37) | ReadTheStorageDataWithIndexTime |
| `0x26` (38) | ReadTheStorageDataWithIndexResult |
| `0x27` (39) | ReadDeviceSerialNumber1 |
| `0x28` (40) | ReadDeviceSerialNumber2 |
| `0x2B` (43) | ReadStorageNumberOfData |
| `0x33` (51) | WriteSystemClockTime |
| `0x41` (65) | StartAnInfraRedTemperatureMeasurement |
| **`0x50` (80)** | **TurnOffTheDevice** ← **written as `MakeCommand(80, 4 zero bytes)`** |
| `0x52` (82) | ClearDeleteAllMemory |
| `0x54` (84) | NotificationForEnteringCommunicationMode |

**Turn-off example wire:** `51-50-00-00-00-00-A3-xx` (checksum).

Matches TICD thermometer PDF / `thermometer.py` framing (`0x51` / `0xA3`).

**Pairing:** no GATT connect required in happy path — serial from manufacturer advertising data (last 4 bytes).

---

### 3.4 Glucose — `BLEDeviceCFL` (`NIPRO CF` / Cocoron)

**Proprietary UUIDs (not Bluetooth SIG glucose 0x1808):**

| Role | UUID |
|------|------|
| Glucose Meter Service | `5D87A4A0-E42D-11E5-BEEF-0002A5D5C51B` |
| Glucose Measurement | `…A1…` |
| Glucose Measurement Context | `…A2…` |
| Record Access Control Point | `…A3…` |
| Glucose Feature | `…A4…` |
| Extension Service | `7A1A0001-8D7F-1727-A23F-DEDB5BF5DF46` |
| Target Glucose Concentration | `…0002…` |
| Remote Device Information | `…0003…` |
| Fixed Message | `…0005…` |
| Image Data Transfer | `…0006…` |
| Transfer Access Control Point | `…0007…` |
| Time and Alarm Setting Service | `87F60001-A469-1EF4-637F-78B96A6F358B` |
| Current Time | `…0002…` |
| Device Information Service (custom) | `8E5996E0-E42F-11E5-AF97-0002A5D5C51B` |
| Battery Service (custom) | `74D4C620-E431-11E5-B5F8-0002A5D5C51B` |

**Host → device writes:**

| Purpose | Characteristic | Example payload |
|---------|----------------|-----------------|
| **Set meter clock** | Time `87F60002-…` | 7 bytes: year encoded oddly (hex digit pairs of year), then mon/day/h/m/s |
| **RACP: number of records** | RACP `5D87A4A3-…` | short form `04 01` or long `04 03 01 00 00` |
| **RACP: report records** | RACP | short `01 01` or long `01 03 01 00 00` |
| **Pairing client type/mode** | Remote Device Information | `00 01` or `[Type, Mode]` (Own/Family/Hospital × AutoTransfer Off/On) |
| **Target glucose concentrations** | Target Glucose Concentration | 13-byte packed targets |
| **Seq / count for Diff mode** | RACP | `04 03 01 00 00` then report with last seq |

**Receive modes** (from NHL): `All` / `Diff` / `Last` (`BLEDeviceCFL.ReceiveMode`).

**Notifications enabled on:** Glucose Measurement, Context, RACP.

**Timeouts:** pairing ops use **30 s**; overall receive timeout is **caller-supplied** (remaining time after connect/setup subtracted via Stopwatch).

---

### 3.5 SpO2 — `BLEDeviceMightySat`

| Role | UUID | Direction |
|------|------|-----------|
| Service | `54c21000-a720-4b4f-11e4-9fe20002a5d5` | — |
| **INCOMING (host → device)** | `…1001…` | **Write Without Response** |
| **GOINGOUT (device → host)** | `…1002…` | Notify |

**Frame:**

```
SOM=0x77 | LEN | CMD | [data…] | CRC8_CCITT(CMD||data)
```

**Commands the app sends:**

| CMD | Name | When |
|-----|------|------|
| `0x01` | GetDeviceInformation | first after notify enable |
| `0x02` | SetClock | after info response; payload = `BitConverter.GetBytes(DateTime.UtcNow.Ticks)` (8 bytes) |
| `0x03` | EnableStream | after ACK of SetClock; payload = 3 bytes from device info `[3],[4],[5]` |
| `0x04` | Waveforms | defined, not heavily used in success path |
| `0x05` | Parameters | **received** stream (SpO2/PR/PI) |
| `0x06` | GetTrendRecord | defined |
| `0x07` | ClearallTrend | defined |
| `0xFE` / `0xFF` | Ack / Nack | responses |

**Stream end condition:** Parameters message with bit 21 set (“Sensor Off Patient”) → store last SpO2/PR/PI and complete.

**No host-side poll interval** — continuous notify stream; complete when sensor-off or timeout.

---

### 3.6 Body composition — `BLEDeviceNBCM` (`NBCM`)

**SIG services used:**

| Service | UUID | Use |
|---------|------|-----|
| Device Info | `0x180A` | Serial read |
| User Data | `0x181C` | User Control Point, First Name, Sex, Height, Birthday |
| Weight Scale | `0x181D` | Measurement notify |
| Body Composition | `0x181B` | Measurement notify |
| Current Time | `0x1805` | time write (10-byte variant) |
| **A&D custom** | `11127000-B364-11E4-AB27-0800200C9A66` | control plane |
| Custom write | `11127001-…` | host → device |
| Custom notify | `11127002-…` | device → host status |

**Host → device command table (custom write char):**

| Method | Payload | Notes |
|--------|---------|-------|
| SetOperationMode | `04 01 05 0A 00` (+ mode byte patched) | Measurement vs Setting |
| RegisterNewUser | `01 E2 07` | on User Control Point `0x2A9F` |
| UserNoAvailabilityCheck | `03 01 14 12` | free user slots |
| DeleteSettings (production) | `03 01 B0 00` | |
| DeleteSettings (factory) | `03 01 AF 00` | |
| DeleteUser (custom) | `04 01 14 13 xx` | user index |
| DeleteUser (UCP) | `03` | consent/delete op |
| UserAuthentication | `02 00 E2 07` | + user index |
| SetHeight (custom) | `05 01 14 18 hh hh` | height |
| MeasurementResultDisplaySetting | `04 01 05 20 01` | |
| BodyFatPercentageMeasurement | `04 01 05 22 01` | |
| MedicalExaminationModeDisabled | `04 01 05 28 00` | |
| AirplaneModeDisabled | `04 01 05 2C 00` | |
| FreeColor / SetColor | (see source ~color cmds) | multi-user color LEDs |

**Also writes standard User Data chars:** First Name, Sex (`0x2A8C`), Height (`0x2A8E`), Birthday (`0x2A85`).

**DateTime write to device:** 10 bytes:

```
[year_lo, year_hi, mon, day, hour, min, sec, 0, 0, 0]
```

**Delays:** iOS serial **2 s / 1 s**; various **1 s** between user-profile writes; connect uses force BLE transport.

**UI status extras:** `NBCM_STEP_ON`, `NBCM_STEP_OFF`, `NBCM_RCV_END` (step-on scale UX).

---

## 4. Device → app data (receive summary)

| Device | Indication/Notify | Fields extracted |
|--------|-------------------|------------------|
| NBP-1BLE / NMBP | BLS `0x2A35` | SBP, DBP, MAP, pulse, timestamp |
| NSM-1BLE | HTP `0x2A1C` | Temperature (°C), timestamp |
| NT-100B | HTP `0x2A1C` | Temperature (IEEE-11073 + exponent) |
| NIPRO CF | Glucose + context + RACP | SeqNo, glucose, meal flag, time |
| MightySat | GoingOut notify | SpO2, PR, PI |
| NBCM | Weight + body composition | Weight, BMI, fat%, BMR, muscle, water |

---

## 5. What is *not* in the HW layer

- No Omron-style encrypted session tokens in BLELib.
- No continuous host→device **heartbeat** interval (except MightySat’s device-driven stream).
- NT-100B defines full TICD command set, but this app version mostly **listens HTP** and only **writes power-off** on teardown.
- UM212/NBP1 do **not** implement UA-651 custom buffer download (`0xE1`) in this library — only clock write + BLS indicate. (Custom A&D cmds appear on **NSM1** instead.)

---

## 6. Decompiled source locations

```
datasheets/nipro/decompiled_cs/
  BLELib/BLELib.BLEDevice/BLEDeviceCFL.cs
  BLELib/BLELib.BLEDevice/BLEDeviceNSM1.cs
  BLELib/BLELib.BLEDevice/BLEDeviceNT100B.cs
  BLELib/BLELib.BLEDevice/BLEDeviceNBP1.cs
  BLELib/BLELib.BLEDevice/BLEDeviceUM212.cs
  BLELib/BLELib.BLEDevice/BLEDeviceNBCM.cs
  BLELib/BLELib.BLEDevice/BLEDeviceMightySat.cs
  BLELib/BLELib.Common/GattServiceConstants.cs
  BLELib/BLELib/BLELib.cs
  NHL/NHL.ViewModels.Utils/Constants.cs
  NHL/NHL.ViewModels/MeterScanViewModel.cs
```

Assemblies (decompressed PE):

```
datasheets/nipro/extracted/assemblies_decompressed/BLELib.dll
datasheets/nipro/extracted/assemblies_decompressed/NHL.dll
```

---

## 7. Quick mapping cheat-sheet (PDF ↔ class)

```
Cocoron通信仕様書.pdf
  → BLEDeviceCFL / "NIPRO CF"

MightySat CSD-1322B.pdf
  → BLEDeviceMightySat / "MightySat"

sdk_ble_ua-651ble_V1.2.pdf
  → A&D custom framing; app: NMBP (UM212) uses BLS clock+measure;
     NSM-1BLE reuses 233BF000 custom command IDs

TICD_Thermometer_v1.16.pdf
  → BLEDeviceNT100B proprietary frames (0x51/0xA3), power-off 0x50

HTP_V10.pdf
  → BLEDeviceNT100B + BLEDeviceNSM1 temperature indications (0x1809/0x2A1C)

(no PDF yet)
  → BLEDeviceNBCM body composition (A&D 11127000-…)
  → BLEDeviceNBP1 pure BLS Nipro BP
```

---

*Generated from ilspycmd 9.1 decompilation of app 2.19.0.0.*

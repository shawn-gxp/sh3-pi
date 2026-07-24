# Nipro Cocoron NC-1BLE — first-party BLE protocol

**Source:** decompiled APK `Cocoron®_1.0.0_APKPure.apk`  
**Package:** `jp.co.nipro.Cocoron` **v1.0.0** (versionCode 3)  
**Decompile:** jadx → `decompiled/sources/jp/co/nipro/Cocoron/`  
**Product:** Wearable single-lead ECG transmitter (Cocoron / NC-1BLE family)  
**Related design PDF:** `datasheets/nipro/Cocoron通信仕様書.pdf` (NIPROA01-SD-002 Rev 2.0)

> Interoperability notes for devices you own. Not vendor-endorsed SDK.

---

## 1. Identity & discovery

| Item | Value |
|------|--------|
| App | Cocoron® (`jp.co.nipro.Cocoron`) |
| Hardware (public) | Nipro Cocoron NC-1BLE telemetric single-lead ECG |
| Scan filter | **Service UUID** (not name): `C74D1000-457D-4194-8B37-E7188723AEA9` |
| Scan mode | Balanced (`ScanSettings.SCAN_MODE_BALANCED` = 1) |
| Scan timeout (UI) | 30_000 ms |
| Name match | App does **not** hardcode `NC-1BLE`; any peripheral advertising the service is listed. Store exact adv name at pair. |
| Transport | `connectGatt(..., TRANSPORT_LE)` (= 2) |
| Auto-connect | `autoConnect = true` (unless ECG sim flag) |
| Auto-reconnect | Default **on**; reconnect on disconnect status 133 / sim path |

**Not the same as** げんきノート `NIPRO CF` glucose (`nipro_cf`).

---

## 2. GATT map

### Service

| Role | UUID |
|------|------|
| **Cocoron primary service** | `C74D1000-457D-4194-8B37-E7188723AEA9` |

### Characteristics (from `BluetoothLeService`)

| Constant | UUID | Direction | Use |
|----------|------|-----------|-----|
| **ECG** | `C74D2000-457D-4194-8B37-E7188723AEA9` | Notify | Huffman-compressed ECG samples |
| **CONFIG** | `C74D2001-457D-4194-8B37-E7188723AEA9` | Write | Mode + HR interval + host id |
| **RRT** (RR-time / HR) | `C74D2002-457D-4194-8B37-E7188723AEA9` | Notify | RR interval + flags (LOD, free-run, illegal phone) |
| **DATETIME** | `00002A08-0000-1000-8000-00805F9B34FB` | Write | Clock (SIG Date Time layout) |
| **BATTERY** | `00002A19-0000-1000-8000-00805F9B34FB` | Notify | Level (mV-scale) + FW version bytes |
| CCCD | `00002902-0000-1000-8000-00805F9B34FB` | Write | Enable notifications (`0x01 0x00`) |

Notify enables on: **ECG**, **RRT**, **BATTERY**.  
Write targets: **CONFIG**, **DATETIME**.

---

## 3. Connect sequence (companion exact)

```
1. Scan with filter service C74D1000-…
2. connectGatt(autoConnect=true, TRANSPORT_LE)
3. onConnected → discoverServices()
4. For each char in {ECG, RRT, BATTERY}:
     setCharacteristicNotification(true)
     queue CCCD = ENABLE_NOTIFICATION (0x01 0x00)
5. Sort descriptor queue by characteristic UUID string
6. Prefer start writeDescriptor on RRT first (if present)
7. Chain remaining CCCDs on onDescriptorWrite success
8. When CCCD queue empty:
     write CONFIG  (see §4)
9. on CONFIG write success:
     write DATETIME  (see §5)
10. Receive RRT + ECG + BATTERY notifies for session life
11. On disconnect with autoReconnect: reconnect same device
```

This is a **long-lived stream** session (not BP-style short dump).

---

## 4. CONFIG write (host → device)

**Char:** `C74D2001-457D-4194-8B37-E7188723AEA9`  
**Builder:** `ConfigCharacteristicValue.writeToDate(mode, interval)`

### Payload

| Offset | Len | Field |
|--------|-----|--------|
| 0 | 1 | Mode: `0x02` if UI mode==1 (free-run / abnormal continuous ECG), else `0x00` |
| 1 | 1 | Interval code (see below) |
| 2.. | 8 | Host id: first **8 ASCII chars** of app-generated UUID (UTF-8), prefs key `UUID` |

Total length: **10 bytes** (2 + 8).

### Mode

| UI `ecgMode` | Wire byte[0] | Meaning (app) |
|--------------|--------------|----------------|
| 0 (default) | `0x00` | Normal ECG policy (10 s / hour style on device firmware) |
| 1 | `0x02` | Free-run / continuous abnormal ECG path (`RRT_ECG_FREE_RUN`) |

Constant: `RRT_ECG_FREE_RUN = 0x02`.

### Interval encoding

UI labels: `["60s","30s","10s","5s"]` (prefs index 0..3, default 0 = 60 s).

After all CCCDs enabled, companion sends:

```text
wire_interval = (4 - prefs_sendInterval) - 1
```

| Prefs index | UI | wire_interval byte |
|-------------|-----|--------------------|
| 0 | 60 s | **3** |
| 1 | 30 s | **2** |
| 2 | 10 s | **1** |
| 3 | 5 s | **0** |

(`setSendInterval()` API path writes the raw index without inversion — prefer the post-CCCD formula for companion parity.)

### Host UUID purpose

Device can flag “illegal iPhone” / second central (design PDF: block 2nd simultaneous phone). App stores an 8-char id and includes it on every CONFIG write.

---

## 5. DATETIME write (host → device)

**Char:** SIG `0x2A08`  
**7 bytes** local now:

```
[year_lo, year_hi, month, day, hour, minute, second]
year = full calendar year (LE uint16)
month = 1..12
```

Written **after** successful CONFIG write.

---

## 6. RRT notify (device → host) — HR / LOD

**Char:** `C74D2002-…`  
**Parser:** `RRTimeCharacteristicValue.readRRTimePacket`

### Flags in byte[0]

| Bit / mask | Name | Meaning |
|------------|------|---------|
| `0x10` | `RRT_DATETIME` | Timestamp fields present |
| `0x20` | `RRT_LEADS_OFF_DETECT` | LOD / not worn (“out of service”) |
| `0x02` | `RRT_ECG_FREE_RUN` | Free-run / continuous ECG mode active |
| `0x40` | `RRT_ILLEGAL_IPHONE` | Second/unauthorized host |

### Layout

```
[0]      flags
if DATETIME:
  [1] year_offset (year = value + 2000)
  [2] month
  [3] day
  [4] hour
  [5] min
  [6] sec
  then i = 7
else:
  i = 1
[i]      RR time low  (uint16 LE)
[i+1]    RR time high
[i+2]    interval echo (device)
```

**RR time:** milliseconds (or device tick) between R peaks — app plots as RRI; HR ≈ 60000 / RR_ms when RR_ms > 0.

App battery warning threshold: **2300** (matches design 2.3 V in mV).

---

## 7. ECG notify (device → host)

**Char:** `C74D2000-…`  
**Parser:** `ECGMeasurementCharacteristicValue.readECGMeasurementPacket`  
**Codec:** pure Java Huffman (`Huffman.java` + `HuffmanNodes.java`)  
**Sample rate (design PDF):** 125 Hz

### Header

| Field | Encoding |
|-------|----------|
| byte[0] bit6 (`0x40`) | `ECG_DATETIME` — timestamp present |
| byte[0] low 6 bits (`0x3F`) | `dataSize` — number of samples in this packet |
| byte[1..2] | `packetCounter` uint16 LE |
| if datetime: byte[3..8] | year_offset(+2000), mon, day, hour, min, sec |
| next 2 bytes | `trigger1`, `trigger2` (R-wave markers; default 255 = none) |
| remainder | Huffman bitstream |

### Decode steps

1. Slice compressed payload after header+triggers.  
2. `Huffman.decode(payload, dataSize)` → int samples (delta + first absolute).  
3. For each sample: `mV = (sample - 512) * 0.01`  
4. Optional: pass through native **notch/noise filter** `Ntf.apply()` (`libntf-lib.so`) — **not required** for raw mV stream.

### Huffman

- Canonical tree walk using `HuffmanNodes.nodeIndex` (**4096** signed shorts).  
- Bit order: MSB first within each byte.  
- Leaf: negative node index → symbol = `(-node) - 1`.  
- After symbols: first sample if `> 1023` then `-= 2048`; subsequent are cumulative deltas with same fold.  
- Table export: `huffman_node_index.json` (this folder).

If decoded length ≠ `dataSize`, packet is discarded (`return null`).

---

## 8. Battery notify

**Char:** SIG `0x2A19` (non-standard payload layout in this product)

```
if len >= 5:
  level     = uint16 LE  // millivolts-ish; warn if < 2300
  majorVer  = byte[2]
  minorVer  = byte[3]
  bugfixVer = byte[4]
```

---

## 9. Session / UX constants

| Constant | Value |
|----------|--------|
| Scan timeout | 30 s |
| BLE health check period | 1 s |
| Reconnect delay | 1.0 s |
| RRI plot timeout by interval | 210 / 105 / 60 / 60 s for 60/30/10/5 |
| ECG linebreak UI | 10 |
| Default HR interval UI | 60 s |
| Hum noise filter | prefs `hum_noise_filter` (drives Ntf path) |

---

## 10. Implementation readiness

| Layer | Status after this APK |
|-------|------------------------|
| Service / char UUIDs | **Exact** |
| Connect / CCCD / CONFIG / DATETIME order | **Exact** |
| RRT + battery parse | **Exact** |
| ECG Huffman decode | **Exact** (Java portable) |
| Sample → mV scale | **Exact** `(raw-512)*0.01` |
| Native Ntf filter | Optional; skip for first-party raw |
| Adv name guarantee `NC-1BLE` | Confirm on hardware scan |
| Toolkit profile `nipro_nc1` | **Not yet** (docs only) |

---

## 11. Key source files (decompiled)

```
service/BluetoothLeService.java          # UUIDs, scan, connect, writes
service/BluetoothLeService$gattCallback$1*.java
data/value/ConfigCharacteristicValue.java
data/value/DateTimeCharacteristicValue.java
data/value/ECGMeasurementCharacteristicValue.java
data/value/RRTimeCharacteristicValue.java
data/value/BatteryLevelCharacteristicValue.java
data/value/Huffman.java
data/value/HuffmanNodes.java             # 4096-node table
data/noise/Ntf.java                      # JNI libntf-lib
common/Config.java                       # timings / interval labels
```

Machine constants: `EXACT_PROTOCOL.json`  
Huffman table: `huffman_node_index.json`

---

## 12. Suggested toolkit profile (future)

```text
id: nipro_nc1
brand: nipro
model: NC-1BLE / Cocoron
name_hints: ("NC-1BLE", "Cocoron", "NC-1")
service_uuid: C74D1000-457D-4194-8B37-E7188723AEA9
notify: ECG, RRT, BATTERY
write: CONFIG then DATETIME
session: long-lived stream + auto-reconnect
parser: huffman ECG + RRI + battery mV
```

---

*Generated from Cocoron app 1.0.0 APK reverse engineering (2026-07-24).*

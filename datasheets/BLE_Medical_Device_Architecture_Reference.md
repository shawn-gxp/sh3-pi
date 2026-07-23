# BLE Medical Device Hardware-Layer Architecture Reference

**Document type:** Cross-brand systems analysis & microservice design input  
**Audience:** Senior BLE / medical device integration engineers  
**Scope:** Protocol reverse-engineering synthesis from vendor datasheets in this repository  
**Constraint:** Architecture and protocol mapping only (no implementation code)  
**Generated from:** Local datasheet corpus under `experiments/datasheets/`  
**Date:** 2026-07-15  
**Status:** Comprehensive extraction from all available protocol and product documents  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Document Corpus & Coverage](#2-document-corpus--coverage)
3. [Cross-Brand Similarity & Difference Matrix](#3-cross-brand-similarity--difference-matrix)
4. [Brand / Device Deep Dives](#4-brand--device-deep-dives)
   - 4.1 Beurer BM54
   - 4.2 FORA 6 Connect
   - 4.3 A&D UA-651BLE (Nipro SH3 pack)
   - 4.4 Masimo MightySat (Nipro SH3 pack)
   - 4.5 Non-Contact Thermometer NT-100B / TICD (Nipro SH3 pack)
   - 4.6 Bluetooth SIG Health Thermometer Profile (HTP) Reference
   - 4.7 Nipro Cocoron ECG
   - 4.8 SH3 Hub Platform Context
5. [End-to-End Workflows (Discovery → Disconnection)](#5-end-to-end-workflows-discovery--disconnection)
6. [Data Parsing Specifications (Per Brand)](#6-data-parsing-specifications-per-brand)
7. [Keep-Alive, Timeouts & Link Management](#7-keep-alive-timeouts--link-management)
8. [Error, Quality & Battery Signaling](#8-error-quality--battery-signaling)
9. [Unified Microservice Abstraction](#9-unified-microservice-abstraction)
10. [Reusable Modules Inventory](#10-reusable-modules-inventory)
11. [Red Flags, Anomalies & Implementation Risks](#11-red-flags-anomalies--implementation-risks)
12. [Documentation Gaps & Capture Plan](#12-documentation-gaps--capture-plan)
13. [Appendix A — UUID & Constant Catalog](#appendix-a--uuid--constant-catalog)
14. [Appendix B — Command Catalogs](#appendix-b--command-catalogs)
15. [Appendix C — Source Document Index](#appendix-c--source-document-index)

---

## 1. Executive Summary

This repository contains protocol and product documentation for building a **unified hardware-layer microservice** that connects to multiple proprietary smart medical devices and behaves like their official companion apps.

### 1.1 Brands / folders analyzed

| Folder | Brand / ecosystem | What is actually documented |
|--------|-------------------|-----------------------------|
| `beurer/` | Beurer GmbH | Full BLE transmission protocol for **BM54** blood pressure monitor (rev01–rev03) |
| `FORA/` | ForaCare / FORA | **Product brochure only** for FORA 6 Connect — **no BLE protocol** |
| `nipro/SH3/` | Nipro SH3 hub + multi-vendor sensors | Mixed pack: **A&D UA-651BLE**, **Masimo MightySat**, **TaiDoc-style thermometer TICD**, **SIG HTP**, **Nipro Cocoron ECG**, SH3 platform feature list |

### 1.2 Architectural finding (one sentence)

Medical BLE peripherals in this corpus fall into **two session archetypes**:

1. **Episodic dump devices** (BP meters, many thermometers): advertise when they have data → short secure session → push/pull store → disconnect.  
2. **Configured stream devices** (SpO2, ECG): connect → host configures → continuous or periodic notify stream → long-lived or supervised session.

A single naive “connect and subscribe” path **will fail** on A&D (5-second arming gate), thermometer (dual-command wake), MightySat (framed proprietary protocol), and Cocoron (always-on HR heartbeat).

### 1.3 Implementability from this corpus alone

| Device | Protocol completeness | Can implement companion-like driver? |
|--------|----------------------|--------------------------------------|
| Beurer BM54 | High | Yes |
| A&D UA-651BLE | High | Yes |
| Masimo MightySat | High | Yes |
| NT-100B Thermometer (TICD) | High (app-layer serial) | Yes (BLE transport + serial framing) |
| SIG HTP | High (profile reference) | Yes as generic thermometer collector |
| Nipro Cocoron ECG | Medium (behavior/design; UUID/payload IF external) | Partial — need interface workbook or protocol logs |
| FORA 6 Connect | None (brochure) | **No** — blocked |

---

## 2. Document Corpus & Coverage

### 2.1 File inventory

#### Beurer (`beurer/`)

| File | Role |
|------|------|
| `BM54_transmissionprotocol_rev01_20190805.pdf` | Initial BM54 BLE protocol |
| `BM54_transmissionprotocol_rev02_20200430.pdf` | BPM field changes |
| `BM54_transmissionprotocol_rev03_20210716.pdf` | **Authoritative latest** — advertising data update |

#### FORA (`FORA/`)

| File | Role |
|------|------|
| `FORA 6 Connect Brochure-Ver1.0_20250407.pdf` | Marketing brochure (Bluetooth mentioned; no GATT) |

#### Nipro SH3 (`nipro/SH3/`)

| File | Role |
|------|------|
| `【家庭血圧計】sdk_ble_ua-651ble_ V1.2_141027_-En.pdf` | A&D UA-651BLE Application Development Spec v1.2 |
| `【SpO2】MightySat Customer Support Document and Interface Protocol - CSD-1322B.pdf` | Masimo MightySat BLE protocol CSD-1322 Rev B |
| `【非接触体温計1-1】通信仕様① Communication Protocol TICD_Thermometer_v1.16_20210216.pdf` | NT-100B thermometer serial/BLE command ICD v1.16 |
| `【非接触体温計1-2】通信仕様② BLUETOOTH PROFILE SPECIFICATION 9.非接触体温計HTP_V10.pdf` | Bluetooth SIG Health Thermometer Profile v1.0 |
| `Cocoron通信仕様書.pdf` (and duplicate) | Nipro ECG (Cocoron) body software design Rev 2.0 |
| `SH3_Features.pdf` / `SH3_FeatureCompact.pdf` | SH3 hub/app platform features (not radio protocol) |
| `_____SaMD_DTx...pptx` | SaMD/DTx trends — out of BLE scope |

### 2.2 What each document type contributes

| Doc type | Contribution to microservice design |
|----------|-------------------------------------|
| Vendor BLE protocol PDFs | UUIDs, pairing, sequences, parse layouts |
| SIG HTP | Generic thermometer collector behavior, idle timeouts, reconnection |
| Cocoron software design | Always-on model, HR period, ECG cadence, battery/LOD policy |
| SH3 features | Outer hub lifecycle (scan → assign → MQTT), not per-device GATT |
| FORA brochure | Product capability only (6 analytes, 1000 memory, Bluetooth) |

---

## 3. Cross-Brand Similarity & Difference Matrix

### 3.1 Master comparison (Core Extraction Targets)

| Dimension | Beurer BM54 | FORA 6 Connect | A&D UA-651BLE | Masimo MightySat | Thermometer TICD NT-100B | Cocoron ECG | SIG HTP (ref) |
|-----------|-------------|----------------|---------------|------------------|--------------------------|-------------|---------------|
| **Product role** | Upper-arm BP | Multi-parameter BG meter | Upper-arm BP | Finger SpO2 multi-param | IR non-contact thermometer | Wearable ECG + HR | Generic thermometer profile |
| **GAP role** | Peripheral / GATT server | Unknown | Peripheral / GATT server | Peripheral / GATT server | Peripheral (BLE v4) / Classic SPP (v2/v3) | Peripheral | Peripheral |
| **Autonomous silent fetch** | Yes — auto Indicate all stored BP after connect | Unknown | Gated auto Indicate after CCCD+time within 5 s | No — host must configure streaming / request trends | No — host poll after dual-wake | Yes for HR/ECG after config; continuous model | Yes — Indicate when measurement ready |
| **Historical sync** | Bulk Indicate every stored record (both users) | 1000 records claimed | Bulk Indicate oldest-first; optional custom memory cmds | Get Trend Record by session ID (0–100) | Index poll 0x25/0x26 after count 0x2B | Not classic store-download; streaming dominant | Multiple Temperature Measurement indications |
| **Real-time** | Not primary (ICP Notify unused) | Unknown | Not primary path in SDK | Params 1 Hz + waveforms ~31.25 Hz after Configure Streaming | Host can start IR measure 0x41 (some models) | HR periodic + ECG 10 s / hourly / continuous abnormal | Intermediate Temperature Notify optional |
| **Wake / fetch command** | None — connect only | Unknown | CCCD=0x0002 + DateTime write (mandatory gates) | Framed cmds on RX char (0x01/0x02/0x03/0x06/0x07) | Two arbitrary cmds in 10 s; optional 0x54 notify | App config (HR period, ECG on) after worn | CCCD enable for Indicate |
| **Keep-alive** | Episodic; preferred CI 100–200 ms | Unknown | 5 s / 30 s idle timeouts; device disconnects after dump | Streaming traffic is keep-alive | Short session; no ping | HR notify is heartbeat; silence → device reset | Idle terminate ~5 s |
| **Pairing model** | 6-digit Passkey (newer) | Unknown | SM Security Request + encryption + bond; Unpair 0x10 | Not documented | Classic PIN 111111 / SSP or 0000; BLE pairing not fully specified | Multi-phone blocked; security in higher-level reqs | Bonding recommended; re-encrypt each connect |
| **Profile model** | SIG BLP 0x1810 + DIS | Unknown | SIG BLP + Battery + DIS + proprietary 0xF000 | Proprietary 128-bit service + framed messages | Proprietary serial GATT 0x1523/0x1524 | Custom Renesas profile (DateTime, ECG Config) | SIG Health Thermometer Service + DIS |
| **Invalid / quality** | Measurement status bits | Unknown | SFLOAT error sentinels; status bits; no-time flag | System + parameter exception bitmasks; trend 255 | Checksum / ACK mismatch; limited clinical flags | LOD; battery <2.3 V; HR clamp 0–350 | RFU handling rules |
| **Low battery** | Not in BM54 protocol | “1000 times” marketing | Battery Service 0x180F / 0x2A19 | Not a dedicated battery GATT in CSD | Not specified in TICD | BLE notify + LED patterns | Not defined by HTP itself |
| **Mid-transfer drop** | Reconnect + re-Indicate remaining (practical) | Unknown | Store residual; send next successful session | Ordinal gap detect; re-arm stream; resume trend IDs | Re-poll by index | Reconnect policy; packet loss tolerated by IF design | Link-loss re-advertise / re-scan |

### 3.2 Similarity clusters (what can be shared)

| Cluster | Members | Shared pattern |
|---------|---------|----------------|
| **SIG Blood Pressure Indicate dump** | Beurer BM54, A&D UA-651BLE | Service `0x1810`, char `0x2A35` Indicate, flags + SFLOAT + optional timestamp/pulse/user/status |
| **Episodic measurement peripherals** | Beurer, A&D, HTP thermometers | Advertise when ready → short connect → transfer → disconnect |
| **Framed proprietary command channel** | MightySat, TICD thermometer | Host writes requests; device responds on notify/write channel with checksummed frames |
| **Configured clinical stream** | MightySat, Cocoron | Host sets what/how often; device pushes asynchronously |
| **Device Information Service** | Beurer, A&D, HTP (and typical SIG devices) | Manufacturer, model, serial, FW/HW revision strings |
| **Time synchronization gate** | A&D (hard), MightySat SetClock, TICD 0x33 | Historical timestamps require host clock write |
| **Manufacturer Specific advertising** | Beurer CID 0x0611, Masimo CID 0x0243 | Prefer Company ID filter over optional service UUID |

### 3.3 Difference clusters (what must diverge)

| Difference | Why it matters |
|------------|----------------|
| Passkey (Beurer) vs SM bond (A&D) vs undocumented (MightySat) vs Classic PIN (thermometer legacy) | Pairing module cannot be one-size-fits-all |
| Auto-dump vs 5 s gated dump vs poll-index vs configure-stream | Session arming is driver-specific |
| SIG health profiles vs full proprietary GATT | Discovery and parsers branch early |
| Episodic BP vs always-on ECG | Connection manager policies opposite |
| Quality via BP status bits vs Masimo exception bitmasks vs LOD voltage | Normalization table required |
| FORA unknown | Cannot join any cluster until protocol obtained |

### 3.4 Service / characteristic presence matrix

| Service / capability | Beurer | FORA | A&D | MightySat | TICD thermo | Cocoron | HTP |
|----------------------|--------|------|-----|-----------|-------------|---------|-----|
| GAP 0x1800 | Yes | ? | Yes | (implied) | (implied) | Yes | Yes |
| GATT 0x1801 | Yes | ? | Yes | ? | ? | Yes | Yes |
| Blood Pressure 0x1810 | Yes | ? | Yes | No | No | No | No |
| Health Thermometer (HTS) | No | ? | No | No | Optional dual path | No | Yes (profile) |
| Battery 0x180F | No (not documented) | ? | Yes | No (in CSD) | No | Custom battery notify | Optional |
| Device Information 0x180A | Yes | ? | Yes | Via cmd 0x01 | Via cmds 0x24/0x27/0x28 | Version features | Yes |
| Pulse Oximeter 0x1822 | No | ? | No | Optional in adv only | No | No | No |
| Proprietary custom service | No | ? | Yes 0xF000 | Yes 128-bit | Yes 0x1523 | Yes custom | No |
| Date/Time characteristic | No dedicated (timestamp in measurement) | ? | Yes 0x2A08 | SetClock cmd | Clock cmds 0x23/0x33 | Date Time char | Optional in measurement |

### 3.5 Session archetype matrix

| Device | Archetype | Typical session length | Who terminates |
|--------|-----------|------------------------|----------------|
| Beurer BM54 | Episodic dump | Seconds–tens of seconds | Not fully specified; expect short session after dump |
| A&D UA-651BLE | Episodic dump (gated) | Seconds; hard 5 s / 30 s timers | Device after memory send complete |
| MightySat | Configured stream + on-demand history | Minutes while streaming | Host or link loss |
| TICD thermometer | Host-polled short session | Seconds per poll sequence | Host (or power-off 0x50) |
| Cocoron ECG | Always-on stream | Hours/days while worn | Link loss / reset / user |
| HTP thermometer | Episodic dump | ~5 s idle window | Either side after idle |
| FORA | Unknown | Unknown | Unknown |

---

## 4. Brand / Device Deep Dives

---

### 4.1 Beurer BM54

#### 4.1.1 Identity

| Field | Value |
|-------|-------|
| Manufacturer | Beurer GmbH |
| Model | BM54 blood pressure monitor |
| Transport | Bluetooth Low Energy |
| Role | GAP Peripheral / GATT Server |
| Advertising name | `BM54` |
| Company ID (Mfg Specific Data) | `0x0611` (Beurer) |
| Passkey feature flag in adv | `0x01` in Mfg payload; newest also documents `0x11060103` |
| Primary protocol doc | `BM54_transmissionprotocol_rev03_20210716.pdf` |

#### 4.1.2 Services and characteristics

**Primary services**

| Spec name | Assigned number | Full UUID |
|-----------|-----------------|-----------|
| Generic Access | 0x1800 | `00001800-0000-1000-8000-00805f9b34fb` |
| Generic Attribute | 0x1801 | `00001801-0000-1000-8000-00805f9b34fb` |
| Blood Pressure | 0x1810 | `00001810-0000-1000-8000-00805f9b34fb` |
| Device Information | 0x180A | `0000180A-0000-1000-8000-00805f9b34fb` |

**Generic Access**

| Characteristic | UUID | Properties | Notes |
|----------------|------|------------|-------|
| Device Name | 0x2A00 | Read | `"BM54"` |
| Appearance | 0x2A01 | Read | `0` Unknown |
| Peripheral Privacy Flag | 0x2A02 | Read, Write | `0x00` |
| Reconnection Address | 0x2A03 | Write | — |
| Peripheral Preferred Connection Parameters | 0x2A04 | Read | CI 100–200 ms; Slave Latency 0; Supervision Timeout Multiplier 1000 |

**Generic Attribute**

| Characteristic | UUID | Properties |
|----------------|------|------------|
| Service Changed | 0x2A05 | Indicate |

**Blood Pressure service**

| Characteristic | UUID | Properties | Notes |
|----------------|------|------------|-------|
| Blood Pressure Measurement | 0x2A35 | **Indicate** | Primary data path |
| Intermediate Cuff Pressure | 0x2A36 | Notify | **Not used by BM54** |
| Blood Pressure Feature | 0x2A49 | Read | Always `[0x04, 0x00]` — irregular pulse supported |

**Device Information (selected)**

| Characteristic | UUID | Notes |
|----------------|------|-------|
| Manufacturer Name String | 0x2A29 | Read |
| Model Number String | 0x2A24 | Read |
| Serial Number String | 0x2A25 | Read |
| Hardware / Firmware / Software Revision | 0x2A27 / 0x2A26 / 0x2A28 | Read |
| System ID | 0x2A23 | All zeros documented |
| IEEE 11073-20601 Regulatory Cert | 0x2A2A | Includes ASCII “Experimental” style payload in doc |
| PnP ID | 0x2A50 | TI company 0x000D; Product Id 0; Product Version 272 |

#### 4.1.3 Advertising

- Name: `BM54`
- Manufacturer Specific Data type `0xFF`
- Payload pattern: first two bytes company ID `0x0611` (little-endian presentation as `0x1106…` in doc tables), then passkey-feature identifier `0x01`
- Newest generation documents extended form including trailing `0x03` (`0x11060103`)

#### 4.1.4 Pairing & security

- Newer BM54 versions use **6-digit Passkey Entry** for connection.
- Older behavior not fully differentiated beyond advertising passkey flag.
- Multiple bond feature: **not supported** in BP Feature bitfield (bit clear).

#### 4.1.5 Autonomous data fetching

Documented behavior:

> The BM54 is sending the data automatically after a connection has been established successfully. This can be triggered by clicking the M1 or the M2 Button on the device. It is also triggered after every measurement. The device is sending all data from both users. For every measurement stored on the BM54 an indication is sent.

Implications for companion-like behavior:

1. No proprietary “wake” write is required.
2. Host must enable **Indications** on `0x2A35` (CCCD bit for Indicate).
3. Expect **N indications** for N stored measurements.
4. Both user memories are sent in one session.
5. Intermediate cuff pressure is not part of the product path.

#### 4.1.6 Historical vs real-time

| Mode | Support |
|------|---------|
| Historical store dump | Yes — all stored measurements via Indicate |
| Real-time cuff inflation stream | No (0x2A36 unused) |
| Live continuous BP | No |

#### 4.1.7 Keep-alive

- Preferred connection interval: **100.00 ms – 200 ms**
- Slave latency: **0**
- Supervision timeout multiplier: **1000**
- No ping/dummy-read documented.
- Design assumes **short dump session**, not long keep-alive.

#### 4.1.8 Revision deltas (must track in parser)

| Rev | Date | Change |
|-----|------|--------|
| 01 | 2019-08-05 | Initial |
| 02 | 2020-04-30 | Blood Pressure Measurement changes |
| 03 | 2021-07-06 | Advertising data changes |
| User ID mapping | rev01 vs rev03 | rev01 text used user values 1 and 2; rev03 uses **0x00 = user1, 0x01 = user2** |

---

### 4.2 FORA 6 Connect

#### 4.2.1 Identity (product only)

| Field | Value from brochure |
|-------|---------------------|
| Model | FORA 6 Connect |
| Parameters | Blood Glucose, Hematocrit, Hemoglobin, β-Ketone, Uric Acid, Total Cholesterol |
| Connectivity | Bluetooth |
| App | iFORA HM (free download) |
| Memory | 1000 measurement results with date/time |
| Power | 1× 1.5V AAA; battery life “1000 times” |
| Dimensions / weight | 89.8 × 54.9 × 18 mm; 46.1 g without batteries |
| BG range | 10–600 mg/dL (0.55–33.3 mmol/L) |
| Meal tags | AC, PC, AutoQC |
| Strip series | BG-HCT-HB, KB, UA, TCH (sample volumes and reaction times listed) |

#### 4.2.2 BLE protocol status

| Required extraction target | Status in folder |
|----------------------------|------------------|
| GATT services/characteristics | **Missing** |
| Pairing model | **Missing** |
| Autonomous fetch mechanism | **Missing** |
| Historical sync sequence | **Missing** |
| Keep-alive | **Missing** |
| Error/battery signaling | **Missing** |
| Data parse layouts | **Missing** |

#### 4.2.3 Microservice implication

**Driver cannot be implemented from current documentation.**  
Place FORA in a **blocked adapter** slot until ForaCare protocol/SDK or BLE sniffer capture vs iFORA is available. Do **not** assume TaiDoc-family framing (even if industry-adjacent) without evidence.

---

### 4.3 A&D UA-651BLE (Nipro SH3 pack)

#### 4.3.1 Identity

| Field | Value |
|-------|-------|
| Manufacturer | A&D Company, LTD. |
| Model | UA-651BLE blood pressure monitor |
| Doc | Application Development Specification for A&D Bluetooth BLE Series v1.2 (2014-10-27) |
| Stack model | SIG profiles + A&D proprietary profile |

#### 4.3.2 Services

| Service | UUID | Purpose |
|---------|------|---------|
| GAP | 0x1800 | Standard |
| GATT | 0x1801 | Standard |
| Blood Pressure | 0x1810 | Measurement Indicate + Feature + Date Time |
| Device Information | 0x180A | Identity strings + System ID + Regulatory |
| Battery | 0x180F | Battery Level |
| **A&D Custom** | **0xF000** | Settings/commands (20-byte R/W characteristic) |

Documented custom characteristic UUID fragment (128-bit):  
`233BF001-5A34-1B6D-975C-000D5690ABE4` style mapping in profile table (base family with `233BF000…` service context).

#### 4.3.3 Blood Pressure Measurement (0x2A35)

Uses standard BLP structure with A&D fixed/flagged fields:

- Pulse Rate Flag: **present (fixed)**
- User ID Flag: **not present (fixed)** → User ID unknown/255 semantics when absent
- Measurement Status Flag: **present (fixed)**
- Measurement Status is **Little Endian**
- Body Movement Detection Flag fixed to “no body movement” in feature/status constraints as documented
- Irregular Pulse supported (Feature value example `0x0C, 0x00`)

**Error encodings (critical):**

| Condition | Encoding |
|-----------|----------|
| Measurement Error | C1/C2/C4 value = `0xFF, 0x07` (SFLOAT NaN-style error) |
| Pulse Rate Range Error | C4 = `0x00, 0x08`; LCD shows “E”; upper/lower detection possible |
| Not Time Settings | Flags bit1 = 0; timestamp N/A |
| Irregular Pulse example status | C6 = `0x04, 0x00` |

#### 4.3.4 Blood Pressure Feature (0x2A49)

Little Endian. Documented support emphasizes irregular pulse; multiple bonds **not supported (fixed)**. Example irregular pulse support value `0x0C, 0x00`.

#### 4.3.5 CCCD

- Blood Pressure Measurement is sent with **Indication**
- CCCD Indicate enable value: **`0x02, 0x00`** (little endian)

#### 4.3.6 Date and Time (0x2A08)

- Used to set/read device clock
- Year/month/day/hour/minute/second fields with range checks
- **If any field out of range, device does not update clock**
- A&D requires time sync **at every connection and pairing**
- After battery change without time adjust, measurements may be stored/sent **without** date/time
- UA-651BLE **does not support reading time** via 0x2A08 nor custom cmd 0x04 (document restriction)

#### 4.3.7 Battery Service

| Level examples | Value |
|----------------|-------|
| 100% | 0x64 |
| 66% | 0x42 |
| 40% | — |
| 33% | 0x21 |

Devices can take a measurement when they can still send battery information (device-defined thresholds).

#### 4.3.8 Device Information (selected)

| Field | Example / rule |
|-------|----------------|
| Manufacturer | `A&D Medical` (even number of chars required for strings) |
| Model | `UA-651BLE` |
| Serial | e.g. `5140700001` |
| System ID | BD address with `FFFE` insertion pattern |
| Regulatory | Continua-style IEEE 11073 certification blob |

#### 4.3.9 Pairing & communication sequence (official)

**Pairing mode:**

1. App enters pairing scan mode.  
2. User puts device into pairing per manual (switch on / hold ~3 s as diagrammed).  
3. Advertise limited discoverable (~60 s window in diagram).  
4. Connect; detect services: BP 0x1810, Custom 0xF000, DIS 0x180A, Battery 0x180F, Date Time 0x2A08.  
5. Security Request → Pairing Confirm → Encryption.  
6. Read DIS/Battery/DateTime as needed.  
7. Write settings via custom service (time, etc.).  
8. After successful pairing device shows **“End”**.  
9. Idle timeout if no commands: **30 seconds** → disconnect.

**After measurement (data transfer mode):**

1. Measurement finishes → undirected connectable advertising (~60 s).  
2. App should scan with aggressive interval to catch advertising quickly.  
3. Connect + security/encrypt.  
4. Within **5 seconds after encrypted** (or after needed write request):  
   - Write Date Time  
   - Write CCCD = **0x0002** (Indications enabled)  
5. Once **both** CCCD and Date Time are done, device goes to **Send Memory Data immediately** (without waiting remaining timeout).  
6. Device Indicates stored measurements **oldest first**.  
7. Host confirms each Indication (ATT).  
8. During Send Memory Data, device **does not accept settings**.  
9. After finished, host may set CCCD back to 0x0000; device **disconnects after send complete**.  
10. If CCCD/time not received in time: data **remains stored** for next successful connection.  
11. After encrypted, if no command: **5 s** timeout disconnect.

#### 4.3.10 Custom service command model

20-byte readable/writable buffer:

| Field | Meaning |
|-------|---------|
| Size | Length from Type to end of Value |
| Type | 0 = Read cmd, 1 = Write cmd, 2 = Response (peripheral) |
| Command | Command ID |
| Value | Payload |

**Documented UA-651BLE commands (partial table recoverable from PDF):**

| ID | Operation |
|----|-----------|
| 0x01 | Set Time |
| 0x03 | Request disconnection by device / disconnect to stand-by |
| 0x10 | Unpair and stand-by |
| 0x12 | Delete all memory |
| 0xA6 | Number of memory / buffer related |
| 0xD6 | Read buffer size (example request/response in doc) |
| 0xE1 | Request all memory data (shown in sequence diagrams) |

Set Time value format example fields: year lower 2 digits, month, day, hour, minute, second.

#### 4.3.11 Preferred connection parameters (profile mapping)

From attribute table notes:

- Peripheral Preferred Connection Parameters example values in mapping: `0x0050, 0x00A0, 0x0000, 0x0258` style (interval/latency/timeout fields as mapped in A&D profile table)

---

### 4.4 Masimo MightySat (Nipro SH3 pack)

#### 4.4.1 Identity

| Field | Value |
|-------|-------|
| Manufacturer | Masimo Corporation |
| Product | MightySat |
| Doc | CSD-1322 Revision B / CO-067126 |
| Transport | BLE asynchronous proprietary protocol |
| Endianness | Little Endian |
| Company ID | **0x0243** |

#### 4.4.2 Advertising (critical discovery rule)

**Do not rely on Service UUID filtering** — PLXP service UUID presence varies.

Filter using **Manufacturer Specific Data**:

| Field | Value |
|-------|-------|
| Company ID | 0x0243 |
| Structure Version | 0x01 |
| Product Type | 0x01 |
| Product Variant | 0x00 Consumer (SpO2, PR, PI); 0x01 RX (adds PVi, RRp) |
| Serial Number | uint32 |

Two adv formats:

1. **With PLXP support:** includes Service UUID `0x1822` + name `MightySat` + Mfg data  
2. **Without PLXP:** name + Mfg data only (no Service UUID field)

#### 4.4.3 Proprietary GATT service

| Item | Value |
|------|-------|
| 128-bit Service UUID | `54c21000-a720-4b4f-11e4-9fe20002a5d5` |
| RX characteristic (host → device) | 16-bit ID **0x1001**, **Write Without Response** |
| TX characteristic (device → host) | 16-bit ID **0x1002**, **Notify only** |

#### 4.4.4 Message framing

```
SOM | LEN | PAYLOAD[N] | CRC
0x77 | N   | d1..dN     | crc8
```

| Field | Rule |
|-------|------|
| SOM | Always `0x77` |
| LEN | Size of (PAYLOAD + CRC); does **not** include LEN itself; valid 1–255 |
| CRC | CRC-8-CCITT over bytes after LEN and before CRC |
| CRC poly | P(x) = x^8 + x^2 + x + 1 |
| CRC seed | 0 |
| Multi-byte ints | LSB first |

#### 4.4.5 Commands

| Command | ID | Direction | Purpose |
|---------|----|-----------|---------|
| Get Device Information | 0x01 | Host→Dev | SW version, available params/waveforms, trend meta, session IDs |
| Set Clock | 0x02 | Host→Dev | Unix epoch seconds (uint32) |
| Configure Streaming Data | 0x03 | Host→Dev | Enable param/waveform bitmasks |
| Get Trend Record | 0x06 | Host→Dev | One trend session by ID |
| Clear Trend Records | 0x07 | Host→Dev | Wipe all trends |

**ACK/NACK**

| Type | ID | Meaning |
|------|----|---------|
| ACK | 0xFE | Success; data byte = original command ID |
| NACK | 0xFF | Failure; command ID + error code |

**Error codes**

| Code | Name |
|------|------|
| 0 | None |
| 1 | Record Not Found |
| 2 | Command Failed |
| 3 | Not Supported |
| 4 | Record Corrupted |
| 5 | Bad CRC |

#### 4.4.6 Get Device Information response (payload concept)

- Command ID 0x01  
- SW Version (2)  
- Available Parameters bitmask (2)  
- Available Waveforms bitmask (1)  
- Trend version (2)  
- Trended Parameters bitmask (2)  
- Number of trend records uint8 **[0–100]**  
- Oldest Trend Session Id (4)  
- Current Trend Session Id (4)

**Parameter bits:** 0 SpO2, 1 PR, 2 PVI, 3 PI, 4 RRp  
**Waveform bits:** 0 Pleth, 1 SIQ  

Example command frame: `[0x77, 0x02, 0x01, 0x07]`

#### 4.4.7 Configure Streaming → real-time path

- Payload: Parameters bitmask (2) + Waveforms bitmask (1)  
- Example: `[0x77, 0x05, 0x03, 0x1F, 0x00, 0x03, 0xD6]`  
- On ACK: device starts periodic transfer  

**Parameter Streaming response (async, ~1 Hz)**

- Response ID **0x05**  
- System Exceptions bitmask (4 bytes)  
- For each enabled parameter (LSB→MSB order): exception byte + value  
- Values uint8 except **PI = uint16 ×100**

**System exception bits (documented):**

| Bit | Meaning |
|-----|---------|
| 21 | Sensor Off Patient |
| 22 | Pulse Search |
| 23 | Interference Detected |
| 24 | Low Perfusion |

**Parameter exception bits:**

| Bit | Meaning |
|-----|---------|
| 0 | Low Confidence |
| 2 | Invalid |
| 4 | Startup State |

**Waveform Streaming response (async)**

- Response ID **0x04**  
- Ordinal uint8 (wraps; detects missing packets)  
- Samples: Pleth int8 + SIQ  
- Sampling period **32 ms** (~31.25 Hz)  
- SIQ bit7 = Invalid; bits0–6 = 0–100 quality-ish value  

#### 4.4.8 Historical trends

Rules:

- Do **not** request next record before previous response/NACK.  
- Trend Session ID increments when user removes device and values freeze.  
- **PI is not saved** as trend even if streamed.  
- Invalid sample value = **255**.  
- Each param trend record: Min, Max, Average, Last (4× uint8).  
- Response includes session id, duration seconds, timestamp of last sample (Unix epoch).

#### 4.4.9 Pairing / keep-alive

- Pairing/bonding **not specified** in CSD-1322B.  
- No dedicated ping command.  
- Keep-alive = configured streaming notifications.  
- Integrity = frame CRC + ordinal continuity.

---

### 4.5 Non-Contact Thermometer NT-100B / TICD (Nipro SH3 pack)

#### 4.5.1 Identity

| Field | Value |
|-------|-------|
| Document | Meter Interface Control Document, Thermometer Meter NT-100B, Ver 1.16 (2021-02-16 / history to 2018-04-30) |
| Roles | GW = gateway/hub/computer; MD = medical device |
| Physical serial baseline | RS232-like framing over link |

#### 4.5.2 Transport variants

| Bluetooth generation | Connection model | Pairing notes |
|----------------------|------------------|---------------|
| v2 meter | SPP | PIN **111111**; always stands by for pairing/connection |
| v3 meter | SPP | Must pair first; **SSP** preferred; if no SSP use PIN **0000** |
| v4 meter (BLE) | GATT | UUID base `1212-efde-1523-785feabcd123`; **Service 0x1523**; **Characteristic 0x1524 (write/notify)** |

#### 4.5.3 Entering communication mode (wake)

Critical companion-app behavior:

1. Establish connection channel.  
2. Send **first two adjoining commands within 10 seconds** to ensure device is in communication mode.  
3. Commands may be arbitrary (example `0x24`).  
4. **Skip responses** whose ACK command does not match the request.  
5. Some models send unsolicited **`0x54` Notification for entering communication mode** — host must handle at start and not confuse with request responses.  
6. TD1241 does not support 0x54 / 0x41 / 0x52 (model exceptions).

#### 4.5.4 Frame structure

```
Byte:  1     2    3..n-2           n-1   n
Name:  Start CMD  Index/Addr/Data  Stop  CheckSum
GW→MD: 0x51  CMD  ...              0xA3  sum(1..n-1)
GW←MD: 0x51  ACK  ...              0xA5  sum(1..n-1)
```

- Checksum: 8-bit sum of bytes 1..n-1  
- Typical command frames: 8 bytes  

Serial parameters (RS232 baseline in doc): baud 19200, 8N1.

#### 4.5.5 Full command list

| CMD | Name | Direction | Notes |
|-----|------|-----------|-------|
| 0x23 | Read device clock time | Both | To minute packing tables |
| 0x24 | Read device model | Both | Project code |
| 0x25 | Read storage data part 1 (time) | Both | Index 0 = latest |
| 0x26 | Read storage data part 2 (result) | Both | Object + ambient temps |
| 0x27 | Read serial number part 1 | Both | SN_0..SN_3 |
| 0x28 | Read serial number part 2 | Both | SN_4..SN_7 |
| 0x2B | Read storage number of data | Both | Count word |
| 0x33 | Write system clock time | Both | Same packing as 0x23 |
| 0x41 | Start IR temperature measurement | Both | Not on TD1241 |
| 0x50 | Turn off device | Both | ACK field notes 0x55 in one table row |
| 0x52 | Clear/delete all memory | Both | Not on TD1241 |
| 0x54 | Entering communication mode notify | MD→GW only | Unsolicited |

#### 4.5.6 Storage read semantics

- Index `0x0000` = latest reading.  
- Part1 (0x25): date, minute, hour, temperature category type, unit (°C/°F).  
- Types (3-bit category): ear, forehead, rectal, armpit, object surface, room, children.  
- Part2 (0x26): object temperature and ambient/background temperature in **0.1°C units** (when unit Celsius path).  
- Historical sync is **fully host-polled**, not device push.

#### 4.5.7 Real-time

- Command **0x41** starts an IR measurement and returns object + ambient temperatures.  
- Not supported on all model variants.

---

### 4.6 Bluetooth SIG Health Thermometer Profile (HTP) Reference

*(Included in Nipro folder as profile baseline for non-contact thermometer integration.)*

#### 4.6.1 Roles

| Role | Stack role |
|------|------------|
| Thermometer | GATT Server, GAP Peripheral |
| Collector | GATT Client, GAP Central |

Services: **one** Health Thermometer Service + Device Information Service.

#### 4.6.2 Collector mandatory capabilities

- Discover HTS  
- Discover Temperature Measurement + its CCCD  
- Configure **Indications**  
- Receive **multiple** indications (stored measurements)  
- Support with/without Time Stamp and Temperature Type fields  
- Support °C and °F  
- Ignore RFU flag bits and unknown trailing octets  

Optional: Intermediate Temperature notifications, Measurement Interval R/W, Temperature Type read.

#### 4.6.3 Connection establishment patterns

Informative typical scenario:

> Thermometer stays powered off between uses; advertises when it has data; collector scans (often whitelist); thermometer sends indications/notifications; thermometer terminates when done.

**Advertising recommendations**

| Phase | Adv interval |
|-------|--------------|
| First 30 s (fast) | 20–30 ms |
| After 30 s (reduced power) | 1–2.5 s |

**Collector scan recommendations**

| Phase | Scan interval / window |
|-------|------------------------|
| First 30 s | 30–60 ms interval, 30 ms window |
| Later option 1 | 1.28 s / 11.25 ms |
| Later option 2 | 2.56 s / 11.25 ms |

**Fast connection interval for discovery/encrypt:** 50–70 ms min/max, then switch to preferred params.

**Idle connection:** either side may terminate if idle **> 5 seconds**.

**Bonded reconnect:** collector shall start encryption after each connection; if encryption fails, re-bond, rediscover if needed, re-write CCCD.

**Link loss:** thermometer re-enters connectable advertising; collector restarts connection procedures.

#### 4.6.4 Security

- Thermometer/collector security considerations defined in HTP §6 (bonding recommended for repeated use).  
- Use as policy baseline for thermometer-class episodic devices when vendor is silent.

---

### 4.7 Nipro Cocoron ECG

#### 4.7.1 Identity

| Field | Value |
|-------|-------|
| Document | NIPROA01-SD-002 Rev 2.0 (2019-03-12) — body software design |
| Product domain | ECG development / Cocoron communication |
| MCU | Renesas RL78/G1D |
| BLE stack | Renesas RBLE + RWKE RTOS |
| Phone role | iPhone Central; device Peripheral |
| Power | 3V coin cell |

#### 4.7.2 Functional capabilities

| # | Function | Behavior |
|---|----------|----------|
| 1 | ECG waveform send | 125 Hz sample → Huffman compress → BLE send |
| 2 | Heart rate notify | Period selected by iPhone: **5 / 10 / 30 / 60 s**; 60 s moving average RR; HR range 0–350 (clamp) |
| 3 | Battery monitor | Sample every **1 min**; notify phone; LED patterns |
| 4 | LOD (leads-off / not worn) | Check every **1 s**; notify on change; **ECG/HR start only after return-to-worn** |
| 5 | BLE connection | app peripheral middleware; BD address + serial from code flash; **block 2nd simultaneous iPhone** with warning |

#### 4.7.3 ECG transmission policy (companion-like)

| Condition | ECG behavior |
|-----------|--------------|
| App launch / connect baseline | Send **10 seconds** of ECG |
| Normal ongoing | **10 seconds every 1 hour** |
| HR outside app-configured normal range **and** abnormal ECG-send enabled | **Continuous ECG until battery depletes** |

#### 4.7.4 Always-on model & keep-alive

- Device is designed for **persistent BLE communication** while worn.  
- Regular HR notifications act as **application-level heartbeat**.  
- If HR notifications stop for a defined period: **device self-reset** (life-safety oriented recovery).  
- Communication errors recover primarily by **BLE reconnect**, not local logging.

#### 4.7.5 Battery & LED

| Condition | Threshold / pattern |
|-----------|---------------------|
| Battery OK | ≥ **2.3 V** |
| Battery abnormal | < **2.3 V** |
| LED normal | 1 flash / 5 s (ON 10 ms) |
| LED abnormal | 3 flashes / 5 s (ON 10 ms, OFF 200 ms) |

#### 4.7.6 Error handling policy

| Category | Example | Recovery |
|----------|---------|----------|
| Device-alone | Deadlock | WDT self-reset |
| Device-alone | BLE packet send error | Wait/discard; stop continuous stream if prolonged; re-enable GAP |
| Link | Out of range | GAP enable/disable around communications; seamless reconnect |
| Link | Packet loss | Format designed to limit blast radius (no TCP retransmit) |
| Link | No communication | Silence detection → reset |

#### 4.7.7 Characteristics mentioned (incomplete UUID map)

Design sequences reference:

- Date Time Characteristic  
- ECG Config Characteristic  

**Exact UUIDs and on-wire payload layouts are not fully present in this PDF** (referenced external interface design workbook / xlsx). Behavior and timing **are** specified.

#### 4.7.8 Non-worn recovery

- On return from non-worn: clear previous HR average; after **5 seconds** of new averaging, send HR.

---

### 4.8 SH3 Hub Platform Context

`SH3_Features.pdf` is **not** a radio protocol, but defines the product envelope your microservice sits in:

| Hub operator capability | Relevance |
|-------------------------|-----------|
| BLE scan / discover sensors | Outer discovery loop |
| Register sensors with backend | Inventory |
| Assign sensors to patients | Multi-user routing (critical for Beurer dual-user dumps) |
| Auto collect BLE health data | Driver orchestration |
| MQTT publish (hubId, sensorId, patientId, vitals, timestamp) | Northbound bus |
| Heartbeat every 30 s | Hub health (not device ping) |
| Supported sensor types | HR, SpO2, Temperature, BP (matches this datasheet pack) |

Use SH3 as **deployment topology**; use sections 4.1–4.7 as **device drivers**.

---

## 5. End-to-End Workflows (Discovery → Disconnection)

This section is the companion-app behavioral specification for the microservice.

### 5.1 Global state machine (unified)

```
[Idle/Scan]
    → identify candidate (name / CID / service UUID)
    → [Connecting]
    → [Pairing/Bonding/Encrypting]   (policy by driver)
    → [Service Discovery]
    → [Session Arming]               (CCCD / time / dual-wake / configure stream)
    → [Historical Sync] and/or [Realtime Streaming]
    → [Optional Control]             (clear memory, set clock, power off)
    → [Teardown]                     (disable CCCD / stop stream / disconnect)
    → [Idle/Scan] or [Bonded Wait]
On error: [Recover] → reconnect / re-arm / resume cursor
```

### 5.2 Discovery workflow (all brands)

#### 5.2.1 Common discovery steps

1. Start LE scan (and Classic inquiry only if thermometer legacy SPP required).  
2. Parse advertising/scan response:  
   - Local Name  
   - Service UUIDs  
   - Manufacturer Specific Data (Company ID + payload)  
3. Score against device registry.  
4. Apply whitelist for bonded peripherals when re-connecting episodic devices.  
5. Prefer **fast scan** first 30 s when expecting a post-measurement advertisement (HTP guidance; A&D “catch advertising ASAP”).

#### 5.2.2 Per-brand discovery filters

| Device | Primary filter | Secondary | Anti-pattern |
|--------|----------------|-----------|--------------|
| Beurer BM54 | Name `BM54` + CID `0x0611` | Passkey flag in Mfg data | Ignoring passkey-capable flag |
| A&D UA-651BLE | Service 0x1810 + custom 0xF000 after connect | Name/model via DIS | Assuming any 0x1810 is A&D |
| MightySat | **CID 0x0243** + name `MightySat` | Variant byte; serial | Filtering only on 0x1822 |
| TICD thermo BLE | Service 0x1523 / base UUID | Name from manual | Treating as HTP-only |
| HTP thermo | HTS UUID in adv (should) | Local name | Forcing bond before first connect always |
| Cocoron | Name/custom (from missing IF doc) | BD address + serial in flash | Treating as episodic BP |
| FORA | Unknown | App-paired name unknown | Guessing TaiDoc UUIDs |

### 5.3 Pairing / bonding workflow

#### 5.3.1 Beurer BM54

```
Scan BM54 (note passkey flag)
→ Connect
→ If newer FW: Passkey Entry (6 digits shown on device/UI)
→ Bond keys stored by host OS / microservice secret store
→ Proceed to service discovery
```

#### 5.3.2 A&D UA-651BLE

```
User puts device in pairing mode (manual)
→ Limited discoverable advertise (~60 s)
→ Connect
→ Security Request / Pairing / Encryption
→ Write time + any production/settings via custom service
→ Device displays "End" on success
→ Disconnect / stand-by
Later measurements use bonded undirected connectable advertising
Unpair command 0x10 available to clear bond side on device
```

#### 5.3.3 MightySat

```
Connect to advertised MightySat
→ (Pairing policy not documented — implement defensively:
    try OS default LE security; capture official app if fails)
→ Enable Notify on TX 0x1002
→ Optional GetDeviceInfo / SetClock
```

#### 5.3.4 Thermometer TICD

```
If Classic v2: pair PIN 111111, SPP connect
If Classic v3: pair (SSP or 0000), SPP connect
If BLE v4: GATT connect to 0x1523/0x1524
→ Dual arbitrary commands within 10 s
→ Handle optional 0x54
→ Continue command session
```

#### 5.3.5 HTP collector

```
Prefer bond on first configuration
→ On each reconnect: Start encryption to verify bond
→ If encrypt fails: user re-bond, rediscover if needed, rewrite CCCD
```

#### 5.3.6 Cocoron

```
Connect single phone only
→ If second central attempts: send warning info
→ Configure DateTime / ECG Config characteristics
→ Ensure worn (LOD)
→ Start HR/ECG paths
```

### 5.4 Service discovery & capability read

| Step | Shared action | Brand specifics |
|------|---------------|-----------------|
| 1 | Discover primary services | SIG vs proprietary branches |
| 2 | Discover characteristics + CCCDs | Mandatory for Indicate/Notify paths |
| 3 | Read DIS | Beurer/A&D/HTP; MightySat uses cmd 0x01 instead/in addition |
| 4 | Read feature bits | BP Feature 0x2A49; MightySat param bitmasks |
| 5 | Read battery if present | A&D 0x2A19; Cocoron battery notify |

### 5.5 Session arming workflow (critical)

This is where companion apps “silently” unlock data.

#### 5.5.1 Beurer arming

```
Write CCCD of 0x2A35 = Indications enabled (0x0002)
→ Wait for N Indications (all stored records, both users)
→ Confirm each Indication
```

#### 5.5.2 A&D arming (hard real-time gate)

```
After Encrypted:
  t0 = now
  Write Date Time (0x2A08 and/or custom 0x01)
  Write CCCD 0x2A35 = 0x0002
  Must complete required gates within 5 seconds of encrypt
→ Device enters Send Memory Data (oldest first)
If timeout: no send; data retained
```

#### 5.5.3 MightySat arming

```
Enable Notify TX
→ GetDeviceInfo (0x01)
→ SetClock (0x02) if historical timestamps needed
→ For realtime: ConfigureStreaming (0x03) with bitmasks
→ For history: iterate GetTrendRecord (0x06) from oldest..current session IDs
  (never pipeline next before response)
```

#### 5.5.4 TICD thermometer arming

```
Send cmd A (e.g. 0x24)
Send cmd B within 10 s total window
Ignore mismatched ACKs
Optionally observe 0x54
→ Write clock 0x33 if needed
→ Read count 0x2B
→ For i in 0..count-1: 0x25(i), 0x26(i)
```

#### 5.5.5 HTP arming

```
Write CCCD Temperature Measurement = Indications enabled
Optional: enable Intermediate Temperature notifications
Optional: set Measurement Interval non-zero for periodic indications
→ Receive one or more indications
```

#### 5.5.6 Cocoron arming

```
Confirm LOD = worn
→ Set HR notification period (5/10/30/60)
→ Enable ECG path per policy (10s now / hourly / continuous-on-abnormal)
→ Receive HR + compressed ECG packets
```

### 5.6 Historical sync workflows

#### Beurer

```
Armed Indicate session
→ For each Indication: parse BPM record → emit Reading
→ Deduplicate by (timestamp, userId, systolic, diastolic, pulse) if re-sent
→ Map userId 0/1 to patient binding
→ End when indications stop / idle timeout / disconnect
```

#### A&D

```
Armed gated session
→ Receive Indications oldest-first with ATT Confirm
→ Parse including error sentinels
→ Optionally query memory count via custom cmds before/after
→ Do not clear memory until durable store commit
→ Optional clear 0x12 only after success
→ Expect device disconnect when done
```

#### MightySat

```
info = GetDeviceInfo
for sessionId in [oldest .. current] (or known missing set):
    resp = GetTrendRecord(sessionId)
    if NACK Record Not Found: continue/skip
    parse min/max/avg/last per available params
    if value == 255: mark INVALID
```

#### TICD

```
count = ReadStorageNumber (0x2B)
for index in 0 .. count-1:
    t = ReadStoragePart1 (0x25, index)
    v = ReadStoragePart2 (0x26, index)
    join into single Reading (time + object/ambient + type + unit)
```

#### HTP

```
While indications arrive:
    parse Temperature Measurement flags → fields
    emit Reading
Optional Measurement Interval for future periodic indications
```

#### Cocoron

```
No full “download all ECG history” in design doc
Optional: capture streaming windows as time-series artifacts
Persist HR time series continuously while connected
```

#### FORA

```
BLOCKED — unknown
```

### 5.7 Real-time streaming workflows

#### MightySat realtime

```
ConfigureStreaming(paramsMask, waveMask)
loop:
  on Notify:
    demux by response ID
      0x05 → parameter sample @1Hz + exceptions
      0x04 → waveform chunk + ordinal gap check
    if ordinal discontinuity: flag packet loss; optional resync policy
  on link loss: reconnect → re-enable notify → re-configure streaming
```

#### Cocoron realtime

```
Receive HR at configured period (heartbeat)
Receive ECG bursts per policy
If HR silence beyond threshold: expect device reset; reconnect
If out-of-range HR + ECG-on: expect continuous ECG flood — size buffers accordingly
```

#### TICD “realtime measure”

```
Send 0x41
Wait matching ACK with temps
(Not continuous stream)
```

#### Beurer / A&D realtime cuff

```
Not supported as product path (Beurer ICP unused; A&D path is post-measure dump)
```

### 5.8 Control operations workflows

| Operation | Beurer | A&D | MightySat | TICD | Cocoron |
|-----------|--------|-----|-----------|------|---------|
| Set clock | Not as separate char (timestamps come from device) | Required each session | Cmd 0x02 | Cmd 0x33 | Date Time characteristic |
| Clear memory | Not documented | Custom 0x12 | Cmd 0x07 | Cmd 0x52 | N/A (stream device) |
| Power off | Not documented | Disconnect/stand-by cmds | Not documented | Cmd 0x50 | Always-on design |
| Unpair | Host-side unbond | Cmd 0x10 | Host-side | Host-side | Host-side |
| Request all memory | Automatic on connect | Auto after arm; also 0xE1 in diagrams | Trend iterator | Index iterator | N/A |

### 5.9 Disconnection & teardown workflows

#### Episodic devices (Beurer, A&D, HTP)

```
If dump complete:
  optional disable CCCD
  accept device-initiated disconnect (A&D explicit)
  return to bonded scan/whitelist wait for next measurement advertise
If dump incomplete:
  mark resume needed
  reconnect when device re-advertises
  re-arm session
  rely on residual storage (A&D/Beurer store until successful transfer)
```

#### Streaming devices (MightySat, Cocoron)

```
StopStreaming / disable notifications (if applicable)
Clear host stream state
Disconnect gracefully
On unexpected drop: exponential backoff reconnect; restore config
```

#### TICD

```
Optional Clear memory after commit
Optional Turn off 0x50
Close GATT/SPP
```

### 5.10 Link-loss recovery matrix

| Device | Device-side recovery | Host-side recovery |
|--------|----------------------|--------------------|
| Beurer | Re-advertise on next user action / measurement (inferred) | Rescan; reconnect; re-enable Indicate |
| A&D | Retain memory; advertise after measure again | Fast scan; re-encrypt; re-time; re-CCCD in 5 s |
| MightySat | Continues locally; trends retained until cleared | Re-notify; GetDeviceInfo; re-stream; resume trend IDs |
| TICD | Stays with stored readings | Reconnect; dual-wake; resume index |
| HTP | Re-enter connectable adv with recommended intervals | Reconnect procedures; re-encrypt if bonded |
| Cocoron | GAP enable cycles; WDT; silence reset | Auto reconnect; re-apply ECG/HR config; check LOD |

---

## 6. Data Parsing Specifications (Per Brand)

### 6.1 Common normalized Reading model (logical)

| Field | Description |
|-------|-------------|
| `deviceId` | Stable identity (serial / BD_ADDR / System ID) |
| `brand` | beurer / aand / masimo / ticd / cocoron / fora / htp |
| `metric` | bp_systolic, bp_diastolic, pulse, spo2, pr, pi, temp_object, hr, ecg_samples, … |
| `value` | Numeric SI-ish value after conversion |
| `unit` | mmHg, bpm, %, °C, mV, … |
| `timestamp` | UTC if convertible; else device-local + quality flag |
| `userId` | Beurer multi-user; others often single |
| `quality` | Normalized flags (see §8) |
| `raw` | Original bytes + parse path id for audit |

### 6.2 Beurer BM54 — Blood Pressure Measurement Indicate payload

**Flags byte always `0x1E` for BM54** meaning present: timestamp, pulse, user ID, measurement status; unit mmHg path.

| Field | Size | Format | Notes |
|-------|------|--------|-------|
| Flags | 1 | bitfield | always 0x1E |
| Systolic | 2 | SFLOAT | 1 == 1.0 mmHg; range 0–300 |
| Diastolic | 2 | SFLOAT | same |
| MAP | 2 | SFLOAT | **always 0 / not supported** |
| Timestamp | 7 | year uint16, month, day, hours, minutes, seconds | year 1582–9999 |
| Pulse | 2 | SFLOAT | 0–255 |
| User ID | 1 | uint8 | **rev03: 0x00 user1, 0x01 user2** |
| Measurement Status | 2 | bitfield | see below |

**Example (from doc):**  
`[0x1E 0x70 0x00 0x4D 0x00 0x00 0x00 0xDF 0x07 0x01 0x0E 0x0A 0x37 0x00 0x48 0x00 0x01 0x00 0x00]`  
→ SYS 117, DIA 77, MAP 0, 2015-01-14 10:55:00, pulse 72, user2 (rev03 mapping), status no error.

**SFLOAT decode note:** Bluetooth MEDFLOAT16 — parser must implement IEEE-11073 SFLOAT, not raw int16.

**Measurement status bits:**

| Bit field | Meaning |
|-----------|---------|
| body movement | 0 none / 1 movement |
| cuff fit | 0 proper / 1 too loose |
| irregular pulse | 0 none / 1 detected |
| pulse rate range | 00 in range / 01 high / 10 low / 11 RFU |
| measurement position | 0 proper / 1 improper |

**Feature characteristic:** `[0x04, 0x00]` → irregular pulse feature supported only (among the set).

### 6.3 A&D UA-651BLE — BPM + custom

#### 6.3.1 BPM Indicate

Same general BLP layout as SIG with A&D constraints:

- Pulse present fixed  
- User ID not present fixed  
- Status present fixed; little endian  
- Parse error sentinels **before** numeric conversion:

| Sentinel | Meaning |
|----------|---------|
| C1/C2/C4 = 0xFF 0x07 | Measurement error |
| C4 = 0x00 0x08 | Pulse range error |

#### 6.3.2 Custom 20-byte command buffer parse

```
[Size][Type][Command][Value...]
Type: 0 read, 1 write, 2 response
```

Parse responses by Command ID; examples in doc for time set and buffer size (`0xD6`).

### 6.4 Masimo MightySat — framed messages

#### 6.4.1 Ingress demux

```
assert SOM == 0x77
len = LEN
payload = next (len-1) bytes
crc = last byte
verify CRC-8-CCITT(payload)  # over bytes after LEN before CRC per spec wording
cmd = payload[0]
switch cmd:
  0x01 device info response
  0x04 waveform stream
  0x05 parameter stream
  0x06 trend record
  0xFE ACK
  0xFF NACK
```

#### 6.4.2 Parameter stream value order

Enabled bits from **LSB to MSB** define field order in the packet.  
PI special-case: uint16, divide by 100.0 for display value.

#### 6.4.3 Trend record

Per parameter block: min, max, avg, last (uint8).  
Map 255 → INVALID.  
Attach sessionId, duration, lastSampleUnix.

### 6.5 TICD thermometer — 8-byte command frames

#### 6.5.1 Validate frame

```
start == 0x51
stop == 0xA3 (host) or 0xA5 (device)
checksum == sum(bytes[0..n-2]) & 0xFF
```

#### 6.5.2 Clock packing (0x23/0x33)

- Data_1||Data_0: Year(7) | Month(4) | Day(5)  
- Data_2/Data_3: minute (6) / hour (5) with reserved bits per tables  

#### 6.5.3 Storage part1 (0x25)

- Index little-endian request  
- Response includes D_M_Year, minute+type, hour+type+unit packing (Table C)  
- Temperature category enum 0..6  
- OutUnit 0=°C, 1=°F  

#### 6.5.4 Storage part2 (0x26)

- Object temperature uint16, unit 0.1°C (when Celsius path)  
- Ambient/background uint16, unit 0.1°C  

#### 6.5.5 Serial number

- 16 hex characters: SN from 0x28 (high) + 0x27 (low) as documented example order  

### 6.6 HTP Temperature Measurement

Follow Bluetooth HTS characteristic:

- Flags determine unit, timestamp presence, temperature type presence  
- Temperature value is FLOAT (IEEE-11073 32-bit), not SFLOAT  
- Collector must accept missing optional fields  

### 6.7 Cocoron ECG / HR

| Stream | Parse notes |
|--------|-------------|
| HR | Scalar bpm 0–350; period-driven; average from 60 s RR window |
| ECG | 125 Hz samples Huffman-compressed; requires external IF tables for bit packing |
| Battery | Voltage-oriented notify; threshold 2.3 V |
| LOD | Boolean worn/not; gate other streams |

**Without the external interface workbook, ECG sample reassembly cannot be fully specified here.** HR/battery/LOD behaviors can still drive session logic.

### 6.8 FORA

No parse specification available.

### 6.9 Multi-user / patient routing rules

| Device | Rule |
|--------|------|
| Beurer | Every dump includes **both users** — filter by User ID before MQTT publish to patient topic |
| A&D | Single-user style (User ID flag fixed absent) |
| Others | Single patient assignment at hub |

---

## 7. Keep-Alive, Timeouts & Link Management

### 7.1 Timeout catalog

| Timer | Device | Value | Effect |
|-------|--------|-------|--------|
| Preferred CI | Beurer | 100–200 ms | Connection param target |
| Supervision multiplier | Beurer | 1000 | Link supervision |
| Pairing idle | A&D | 30 s | Disconnect if no commands after encrypt in pairing |
| Post-measure arming | A&D | **5 s** | Must CCCD+time or no dump |
| Post-confirm idle | A&D | 5 s | Disconnect paths in sequence diagrams |
| Advertising windows | A&D diagrams | ~60 s | Pairing / post-measure advertise |
| Dual-command wake | TICD | **10 s** | Enter communication mode |
| HTP idle | HTP | **5 s** | Either side may disconnect |
| HTP fast adv | HTP | 30 s then slower | Discovery power tradeoff |
| HTP fast CI | HTP | 50–70 ms | During discovery/encrypt |
| MightySat param rate | MightySat | 1 Hz | Stream keep-alive |
| MightySat wave period | MightySat | 32 ms | Waveform pacing |
| Cocoron HR period | Cocoron | 5/10/30/60 s | App-level heartbeat |
| Cocoron battery poll | Cocoron | 1 min | Battery notify cadence |
| Cocoron LOD poll | Cocoron | 1 s | Wear state |
| Cocoron ECG baseline | Cocoron | 10 s / hour | Burst policy |
| SH3 hub MQTT heartbeat | Hub | 30 s | Hub liveness (not device) |

### 7.2 Keep-alive strategies to implement

| Strategy name | When to use | Mechanism |
|---------------|-------------|-----------|
| `NoneEpisodic` | Beurer, A&D, HTP, TICD | Complete transfer; allow disconnect |
| `TrafficStream` | MightySat | Maintain ConfigureStreaming; watch 1 Hz |
| `AppHeartbeatHR` | Cocoron | Expect HR notify within 2–3× period; else recover |
| `HubHeartbeat` | SH3 | MQTT 30 s — orthogonal to BLE |

**No device in this corpus documents a dummy GATT read ping.** Do not invent one unless sniffer shows the official app doing so.

### 7.3 Connection interval policy

1. Use **fast intervals** during service discovery, bonding, encryption, and bulk indicate dumps.  
2. After setup, accept peripheral preferred parameters (Beurer 100–200 ms; A&D mapped prefs; HTP guidance).  
3. For high-rate waveform (MightySat ~31.25 Hz samples in chunks), ensure interval/throughput supports Notify payload rates.  
4. For Cocoron continuous ECG abnormal mode, budget for sustained throughput + Huffman frames.

---

## 8. Error, Quality & Battery Signaling

### 8.1 Normalized quality enum (recommended)

```
OK
BODY_MOVEMENT
CUFF_LOOSE
IRREGULAR_PULSE
PULSE_OUT_OF_RANGE
IMPROPER_POSITION
NO_TIMESTAMP
MEASUREMENT_ERROR
SENSOR_OFF
PULSE_SEARCH
INTERFERENCE
LOW_PERFUSION
LOW_CONFIDENCE
INVALID
STARTUP
LEADS_OFF
LOW_BATTERY
CRC_FAIL
NACK_*
PARTIAL_TRANSFER
```

### 8.2 Mapping tables

#### Beurer status → normalized

| Source bit | Normalized |
|------------|------------|
| body movement | BODY_MOVEMENT |
| cuff too loose | CUFF_LOOSE |
| irregular pulse | IRREGULAR_PULSE |
| pulse high/low | PULSE_OUT_OF_RANGE |
| improper position | IMPROPER_POSITION |
| all clear | OK |

#### A&D → normalized

| Source | Normalized |
|--------|------------|
| 0x07FF measurement fields | MEASUREMENT_ERROR |
| pulse 0x0008 encoding | PULSE_OUT_OF_RANGE |
| timestamp flag 0 | NO_TIMESTAMP |
| irregular pulse status | IRREGULAR_PULSE |
| battery low discrete levels | LOW_BATTERY (policy threshold) |

#### MightySat → normalized

| Source | Normalized |
|--------|------------|
| bit21 Sensor Off | SENSOR_OFF |
| bit22 Pulse Search | PULSE_SEARCH |
| bit23 Interference | INTERFERENCE |
| bit24 Low Perfusion | LOW_PERFUSION |
| param Low Confidence | LOW_CONFIDENCE |
| param Invalid / value 255 | INVALID |
| param Startup | STARTUP |
| NACK codes | NACK_* |
| Bad CRC | CRC_FAIL |

#### TICD → normalized

| Source | Normalized |
|--------|------------|
| checksum fail | CRC_FAIL |
| mismatched ACK | PARTIAL_TRANSFER / protocol error |
| (clinical invalid not richly coded) | OK unless transport error |

#### Cocoron → normalized

| Source | Normalized |
|--------|------------|
| LOD not worn | LEADS_OFF |
| V < 2.3 | LOW_BATTERY |
| HR silence recovery | PARTIAL_TRANSFER / link recovery event |
| HR > 350 clamped | note as clamped value |

### 8.3 Mid-transfer failure playbooks

| Brand | Playbook |
|-------|----------|
| Beurer | On disconnect, keep last fully confirmed indication only; on reconnect accept full dump; dedupe |
| A&D | Trust device storage; next successful 5 s arming resends residual; never clear early |
| MightySat | Use ordinal gaps to drop incomplete wave segments; re-request trends by session id |
| TICD | Resume index loop from last successful index |
| Cocoron | Reconnect + reconfig; do not assume gapless ECG across reset |
| HTP | Standard link-loss reconnect; re-enable CCCD if bond lost |

---

## 9. Unified Microservice Abstraction

### 9.1 Logical architecture layers

```
┌──────────────────────────────────────────────┐
│ Northbound: MQTT / REST / SH3 backend        │
├──────────────────────────────────────────────┤
│ Normalization: Reading, Quality, Patient map │
├──────────────────────────────────────────────┤
│ Session Orchestrator (archetype A/B engines) │
├───────────────┬──────────────────────────────┤
│ EpisodicEngine│ StreamEngine                 │
├───────────────┴──────────────────────────────┤
│ DeviceDriver plugins                         │
│  Beurer | A&D | MightySat | TICD | Cocoron | HTP | FORA(blocked)
├──────────────────────────────────────────────┤
│ BLE Stack Facade (scan, connect, GATT, SM)   │
└──────────────────────────────────────────────┘
```

### 9.2 Driver interface (logical)

```
DeviceDriver {
  id, brand, archetype
  matchAdvertisement(adv) -> score
  pairingPolicy() -> PairingMode
  discover(conn)
  armSession(conn, options)   // CCCD/time/wake/configure
  syncHistory(conn) -> iterator[Reading]
  startRealtime(conn, mask) -> subscription
  stopRealtime(conn)
  readDeviceInfo(conn)
  readBattery(conn) -> optional
  clearMemory(conn) // dangerous; policy gated
  setTime(conn, ts)
  onLinkLoss(conn)
  teardown(conn)
}
```

### 9.3 Orchestrator algorithm (companion-app equivalent)

```
loop forever:
  adv = scan.next()
  driver = registry.bestMatch(adv)
  if driver is null: continue

  conn = connect(adv)
  try:
    driver.pairIfNeeded(conn)
    driver.discover(conn)
    driver.setTime(conn) if driver.requiresTimeGate
    driver.armSession(conn)

    if policy.wantsHistory:
      for reading in driver.syncHistory(conn):
        normalize -> route to patient -> publish
        durableAck()
      maybeClearMemory(driver) // only after durableAck

    if policy.wantsRealtime and driver.supportsRealtime:
      driver.startRealtime(conn)
      supervise(heartbeats/timeouts)
    else:
      driver.teardown(conn)
  catch LinkLoss:
    driver.onLinkLoss(conn)
    scheduleReconnect(driver, bondedAddress)
```

### 9.4 Archetype engines

#### EpisodicEngine (Beurer, A&D, HTP, TICD poll)

- Aggressive scan when expecting post-measure advertising  
- Short connection  
- Strict arming deadlines (A&D 5 s, TICD 10 s)  
- History first; no long-lived keep-alive  
- Device or host disconnect after completion  

#### StreamEngine (MightySat, Cocoron)

- Maintain connection while clinically needed  
- Configuration transaction at start  
- Watchdog on expected notify cadence  
- Backpressure buffers for ECG/waveform floods  
- Reconfigure after reconnect  

### 9.5 Configuration surface (per installed device)

| Config key | Purpose |
|------------|---------|
| `pairingMode` | Passkey / SM / PIN / none |
| `armTimeoutMs` | Driver-specific |
| `historyMode` | push-indicate / poll-index / trend-session |
| `realtimeMask` | Params/waveforms/HR period |
| `autoClearMemory` | default false |
| `patientBinding` | including Beurer userId map |
| `qualityPolicy` | which flags reject vs accept-with-warning |

---

## 10. Reusable Modules Inventory

This is the reuse plan for the microservice codebase (module boundaries — not code).

### 10.1 Highly reusable (use across many brands)

| Module | Responsibility | Reused by |
|--------|----------------|-----------|
| **BleScanner** | LE scan, whitelist, duty-cycle fast/slow profiles (HTP tables) | All BLE devices |
| **AdvertisementParser** | Name, service UUIDs, Mfg data TLV | All |
| **CompanyIdRegistry** | Map CID → brand (0x0611 Beurer, 0x0243 Masimo, …) | Discovery |
| **GattClientFacade** | Connect, MTU, discover, read/write, CCCD, indicate/notify callbacks | All GATT devices |
| **SecurityManagerFacade** | Just Works / Passkey / bond storage / re-encrypt on connect | Beurer, A&D, HTP, others |
| **CccdHelper** | Encode 0x0001 Notify / 0x0002 Indicate; enable/disable | Beurer, A&D, HTP, streams |
| **SfloatCodec** | IEEE-11073 SFLOAT encode/decode | Beurer, A&D BP |
| **Float11073Codec** | 32-bit medical FLOAT | HTP temperature |
| **BlpMeasurementParser** | Flags-driven BP measurement parse skeleton | Beurer + A&D (with hooks) |
| **DisReader** | Standard Device Information characteristics | Beurer, A&D, HTP |
| **BatteryServiceReader** | 0x180F / 0x2A19 | A&D (+ future SIG devices) |
| **SessionOrchestrator** | Archetype engines + retries | All |
| **ReadingNormalizer** | Metric/unit/quality canonicalization | All |
| **PatientRouter** | Map device+userId → patientId | Beurer multi-user + hub assign |
| **DurableInbox** | At-least-once store before memory clear | A&D/MightySat/TICD clears |
| **LinkWatchdog** | Expected notify cadence supervision | MightySat, Cocoron |
| **ReconnectPolicy** | Backoff, bonded direct connect, scan fallback | All |
| **TimeSyncService** | Write device clocks in vendor formats | A&D, MightySat, TICD, Cocoron |
| **MqttPublisher** | SH3 northbound schema | Hub integration |
| **AuditRawFrameLog** | Raw bytes for regulatory/debug | All ( esp. medical) |

### 10.2 Partially reusable (shared with adapters)

| Module | Shared core | Brand-specific adapter |
|--------|-------------|------------------------|
| **BloodPressureDriverBase** | BLP parse + Indicate dump loop | Beurer auto-arm vs A&D 5 s gate + custom service |
| **EpisodicDumpEngine** | advertise→connect→dump→disconnect | HTP / Beurer / A&D timing params |
| **FramedCommandChannel** | request/response correlation, timeout | MightySat CRC framing vs TICD checksum framing |
| **TrendHistoryCursor** | iterate IDs with ack gating | MightySat sessions; TICD indices |
| **QualityMapper** | enum + bit extractors | per-brand bit dictionaries |
| **PairingWizard** | UX/state for PIN/passkey | different PIN sources |

### 10.3 Brand-specific modules (do not force-share)

| Module | Why isolated |
|--------|--------------|
| **BeurerPasskeyAdvertisingDecoder** | Unique Mfg flag generations |
| **BeurerUserIdRevMapper** | rev01 vs rev03 user id semantics |
| **AndCustomServiceCodec** | 20-byte Size/Type/Cmd buffer + cmd set |
| **AndFiveSecondArmingGate** | Hard real-time correctness |
| **MightySatCrc8Ccitt** | Specific poly/seed and LEN definition |
| **MightySatStreamDemux** | 0x04/0x05 async interleaving |
| **TicdDualWake** | 10 s two-command + 0x54 ignore rules |
| **TicdBitPackedClockTables** | Non-obvious date bit packing |
| **CocoronHuffmanEcgDecoder** | Unique compression; external tables |
| **CocoronLodGate** | Wear gating before streams |
| **ForaDriver** | Placeholder blocked |

### 10.4 Reusable test fixtures / golden vectors

| Fixture | Source |
|---------|--------|
| Beurer example indication bytes | BM54 protocol example |
| MightySat example command frames | CSD examples (`[0x77,0x02,0x01,0x07]`, etc.) |
| A&D error SFLOAT sentinels | SDK error section |
| TICD frame checksum examples | Construct from sum rule |
| HTP idle/scan timing tables | HTP §5 |

### 10.5 Suggested package map

```
hw_ble/
  stack/          # scanner, gatt, sm
  codec/          # sfloat, float, crc8, ticd_checksum
  normalize/      # reading, quality
  session/        # episodic, stream orchestrators
  drivers/
    beurer_bm54/
    aand_ua651/
    masimo_mightysat/
    ticd_thermometer/
    htp_thermometer/
    nipro_cocoron/
    fora_connect/   # blocked stub
  hub/            # patient bind, mqtt
```

### 10.6 What NOT to reuse naively

| Anti-reuse | Reason |
|------------|--------|
| Single “BP driver” for Beurer+A&D without arming strategy | Will miss A&D dumps |
| Single “thermometer driver” for TICD+HTP | Different transports and frames |
| Service UUID-only discovery | Breaks MightySat variants |
| Global keep-alive ping | Not in corpus; may break episodic devices |
| Auto memory clear after any receive | Risk of data loss on northbound failure |
| Assuming Just Works everywhere | Beurer passkey; A&D encryption; classic PINs |

---

## 11. Red Flags, Anomalies & Implementation Risks

| ID | Severity | Item | Mitigation |
|----|----------|------|------------|
| R1 | Critical | FORA has no protocol in corpus | Block driver; obtain SDK/sniff |
| R2 | Critical | A&D 5-second CCCD+time gate | Arm immediately after encrypt; precompute writes |
| R3 | Critical | Beurer User ID rev mismatch | Version-detect; map correctly |
| R4 | High | Beurer Passkey required on newer units | Implement Passkey Entry UX/automation |
| R5 | High | MightySat service UUID optional in adv | Filter CID 0x0243 |
| R6 | High | TICD dual-wake + unsolicited 0x54 | State machine with ignore rules |
| R7 | High | Cocoron always-on vs BP episodic | Separate connection managers |
| R8 | High | Cocoron on-wire UUID/payload incomplete | Need IF workbook or sniffer |
| R9 | Medium | Beurer sends both users always | PatientRouter must filter |
| R10 | Medium | A&D rejects settings during dump | Don’t interleave commands mid-indicate |
| R11 | Medium | MightySat PI not in trends | Don’t expect historical PI |
| R12 | Medium | Invalid trend value 255 | Explicit INVALID quality |
| R13 | Medium | TD1241 command subset | Model capability matrix |
| R14 | Medium | No battery on Beurer protocol | UI must not assume Battery Service |
| R15 | Low | Beurer MAP always 0 | Don’t validate MAP clinically |
| R16 | Low | HTP idle 5 s | Finish config quickly |
| R17 | High | Official apps may do undocumented extras | Validate with HCI sniff before production |

---

## 12. Documentation Gaps & Capture Plan

| Gap | Impact | Recommended action |
|-----|--------|--------------------|
| FORA GATT/commands/pairing | Cannot integrate | Request ForaCare protocol; sniff iFORA |
| Cocoron UUID & Huffman tables | ECG parse blocked | Obtain interface design xlsx; sniff app |
| MightySat LE security level | Pairing uncertainty | Sniff official app pairing features |
| Beurer exact CCCD timing / disconnect initiator | Minor session polish | Sniff HealthManager-style app |
| A&D full command binary examples beyond partial table | Custom service completeness | Lab exercise all cmd IDs |
| Whether any device needs explicit L2CAP conn_param_update | Throughput/stability | Log official app LL/L2CAP |
| Multi-phone policies except Cocoron warning | Fleet deployments | Test second central behavior |

---

## Appendix A — UUID & Constant Catalog

### A.1 SIG UUIDs used

| Name | UUID |
|------|------|
| GAP | 0x1800 |
| GATT | 0x1801 |
| Device Information | 0x180A |
| Battery | 0x180F |
| Blood Pressure | 0x1810 |
| Pulse Oximeter (PLXP) | 0x1822 |
| Device Name | 0x2A00 |
| Appearance | 0x2A01 |
| Peripheral Preferred Connection Parameters | 0x2A04 |
| Service Changed | 0x2A05 |
| Date Time | 0x2A08 |
| Battery Level | 0x2A19 |
| System ID | 0x2A23 |
| Model Number | 0x2A24 |
| Serial Number | 0x2A25 |
| Firmware Revision | 0x2A26 |
| Hardware Revision | 0x2A27 |
| Software Revision | 0x2A28 |
| Manufacturer Name | 0x2A29 |
| IEEE Regulatory Cert List | 0x2A2A |
| Blood Pressure Measurement | 0x2A35 |
| Intermediate Cuff Pressure | 0x2A36 |
| Blood Pressure Feature | 0x2A49 |
| PnP ID | 0x2A50 |
| CCCD | 0x2902 |

### A.2 Proprietary UUIDs / IDs

| Brand | Constant | Value |
|-------|----------|-------|
| Beurer | Company ID | 0x0611 |
| Beurer | Adv name | BM54 |
| A&D | Custom service | 0xF000 |
| A&D | Custom char family | 233BF001-5A34-1B6D-975C-000D5690ABE4 (as mapped) |
| Masimo | Company ID | 0x0243 |
| Masimo | Service | 54c21000-a720-4b4f-11e4-9fe20002a5d5 |
| Masimo | RX / TX | 0x1001 / 0x1002 |
| Masimo | SOM | 0x77 |
| TICD BLE | UUID base | 1212-efde-1523-785feabcd123 |
| TICD BLE | Service / Char | 0x1523 / 0x1524 |
| TICD | Frame start | 0x51 |
| TICD | Stop GW→MD / MD→GW | 0xA3 / 0xA5 |

### A.3 CCCD values

| Value | Meaning |
|-------|---------|
| 0x0000 | Disable |
| 0x0001 | Notifications |
| 0x0002 | Indications |

---

## Appendix B — Command Catalogs

### B.1 MightySat

| ID | Name |
|----|------|
| 0x01 | Get Device Information |
| 0x02 | Set Clock |
| 0x03 | Configure Streaming Data |
| 0x04 | Waveform Streaming Response (device) |
| 0x05 | Parameter Streaming Response (device) |
| 0x06 | Get Trend Record |
| 0x07 | Clear Trend Records |
| 0xFE | ACK |
| 0xFF | NACK |

### B.2 TICD thermometer

| ID | Name |
|----|------|
| 0x23 | Read clock |
| 0x24 | Read model |
| 0x25 | Read storage time |
| 0x26 | Read storage result |
| 0x27 | Read SN part1 |
| 0x28 | Read SN part2 |
| 0x2B | Read storage count |
| 0x33 | Write clock |
| 0x41 | Start IR measure |
| 0x50 | Power off |
| 0x52 | Clear memory |
| 0x54 | Enter comm mode notify |

### B.3 A&D custom (UA-651BLE)

| ID | Name |
|----|------|
| 0x01 | Set Time |
| 0x03 | Disconnect / stand-by request |
| 0x10 | Unpair |
| 0x12 | Delete all memory |
| 0xA6 | Memory/buffer number related |
| 0xD6 | Read buffer size |
| 0xE1 | Request all memory data (sequence diagrams) |

---

## Appendix C — Source Document Index

| Path | Brand | Used for |
|------|-------|----------|
| `beurer/BM54_transmissionprotocol_rev03_20210716.pdf` | Beurer | Primary BM54 protocol |
| `beurer/BM54_transmissionprotocol_rev01_20190805.pdf` | Beurer | User ID delta / history |
| `beurer/BM54_transmissionprotocol_rev02_20200430.pdf` | Beurer | Intermediate revision |
| `FORA/FORA 6 Connect Brochure-Ver1.0_20250407.pdf` | FORA | Product capabilities only |
| `nipro/SH3/【家庭血圧計】sdk_ble_ua-651ble_ V1.2_141027_-En.pdf` | A&D | Full BP BLE SDK |
| `nipro/SH3/【SpO2】MightySat ... CSD-1322B.pdf` | Masimo | Full SpO2 protocol |
| `nipro/SH3/【非接触体温計1-1】... TICD_Thermometer_v1.16...pdf` | Thermometer | Serial/BLE commands |
| `nipro/SH3/【非接触体温計1-2】... HTP_V10.pdf` | SIG | HTP collector rules |
| `nipro/SH3/Cocoron通信仕様書.pdf` | Nipro ECG | Always-on ECG design |
| `nipro/SH3/SH3_Features.pdf` | SH3 hub | Platform/MQTT context |

---

## Document Control

| Item | Value |
|------|-------|
| Title | BLE Medical Device Hardware-Layer Architecture Reference |
| Version | 1.0 |
| Based on local datasheets only | Yes |
| Code included | No |
| Completeness intent | All extractable protocol facts from corpus + architecture synthesis |

**End of document.**

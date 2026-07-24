# Device support matrix (hardware + data collection)

**Project:** `medical_ble_toolkit` (Omron bundled as `medical_ble_toolkit/omron_bp`)  
**Scope:** BLE connect, bond/pair, session timing, history/stream collect, parse to structured vitals.  
**Out of scope:** cloud, accounts, charts, OCR/camera, clinical claims, full companion-app UI.

**Last updated:** 2026-07-16  
**Evidence:** datasheets, Beurer HealthManager Pro APK registry, Omron catalog, Nipro げんきノート BLELib decompile, parsers/sessions in this repo.

This is **not** a regulatory or “certified companion” claim.  
**Companion-like** = hardware sync path close to the vendor app’s BLE behavior.

---

## Support levels

| Level | Code | Meaning |
|-------|------|---------|
| **Companion-like / seamless** | **A** | Bond + correct session timing + full parse of main measurement dump. Suitable as production HW-layer “supported”. |
| **Strong limited** | **B** | Connect + collect implemented; parse largely done; live QA recommended per SKU before customer enablement. |
| **Partial / catalog** | **C** | UUIDs, commands, or session present; body parse incomplete or unproven; hex/RE still useful. |
| **Scaffold only** | **D** | Architecture slot only; insufficient protocol for real support claims. |

---

## How to run

```powershell
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"
python -m medical_ble_toolkit
```

- Default brand **Enter = Omron**.
- Beurer: category → model (from APK catalog) → Connect (companion session).
- Prefer **known MAC** on Windows if BLE scan aborts.

State files (cwd): `medical_ble_device.json`, `beurer_sync_state.json` (glucose seq + BP dedup).

---

## 1. Beurer

**Sources:** `datasheets/beurer/tools/device_registry.json`, `beurer/capabilities.json`, `beurer/session.py`.  
**OCR / camera path:** excluded by design.

### 1.1 Blood pressure — Level **A** (companion-like)

| Item | Detail |
|------|--------|
| Protocol | Bluetooth SIG BLP service `0x1810`, measurement Indicate `0x2A35` |
| Session | Pair → settle → optional set-time → CCCD → auto dump → quiet ~4s → parse |
| Profile | `beurer_bp` (shared by ~66 catalog models) |

#### Core model numbers (global BM / BC)

| Series | Model IDs |
|--------|-----------|
| **Upper arm BM** | BM23, BM25, BM26, BM27, BM28, BM2R, BM30, BM32, BM35, BM36, BM38, BM40, BM44, BM45, BM46, BM46Connect, BM47, BM48, BM49, BM50, BM51, BM53, **BM54**, BM55, BM57, BM58, BM59, BM64, BM69, BM77, BM81, BM82, BM85, BM92 |
| **Wrist BC** | BC1R, BC21, BC27, BC28, BC30, BC32, BC40, BC44, BC51, BC54, BC54W, BC57, BC58, BC81, BC85, BC87, BC87W |

#### Regional / retail names (same BP stack)

These appear in the companion catalog as advertisement/storage names (often EU / multi-region rebadges of BM/BC hardware):

| Catalog ID | Advertisement / retail name | Notes |
|------------|----------------------------|--------|
| SERIES600 | SERIES600 | Series retail line |
| SERIES700 | SERIES700 | |
| SERIES700W | SERIES700W | |
| SERIES800 | SERIES800 | |
| SERIES1000 | SERIES1000 | |
| BC87W | SERIES800W | Wrist |
| BM92 | PREMIUM800 | |
| BC54W | PREMIUM800W | |
| DELUXE500 | DELUXE500 | |
| BM82 | DELUXE600 | |
| ELITE | ELITE | |
| ELITE900 | ELITE900 | APK pulse-swap marker (`t6`) |
| ELITE950 | ELITE950 | |
| ELITEPLUS | ELITEPLUS | |
| AUTO400 | AUTO400 | |
| QUICK | QUICK | |
| SENSE | SENSE | |
| IBC55 | IBC55 | |
| SRBM1 | SR BM1 | 3s settle marker (`te`) like BM54 |

*Beurer regional mapping is incomplete in public docs; names above are **as shipped in HealthManager Pro 1.20.1**.*

#### APK capability extras (seamless path)

| Capability | Models (from `capabilities.json` / `mo.*`) |
|------------|-----------------------------------------------|
| 3s post-connect settle (`te`) | BM54, BM57, BM85, BM46Connect, SRBM1 |
| Pulse SFLOAT byte-swap (`t6`) | BM48, BM59, BM85, ELITE900 |
| Set clock (`0x2A2B` family) | BM59, BM64, many older BM + ECG class (see capabilities) |
| Passkey likely (`fa`/`xh` / mfg `0x0611`+`01 03`) | Newer BM/BC generations |

---

### 1.2 Blood glucose — Level **B**

| Model ID | Adv name | Level |
|----------|----------|-------|
| GL22 | GL22 | B |
| GL34 | GL34 | B |
| GL40 | GL40 | B |
| GL44 | GL44 | B |
| GL44Lean | GL44lean | B |
| GL48 | GL48 | B |
| GL49 | GL49 | B (long RACP `mg`) |
| GL50 | GL50 | B |
| GL50EVO | Beurer GL50EVO | B |
| GL60 | GL60 | B (long RACP `mg`) |

**Strong:** Glucose `0x1808`, CCCD order, RACP report-all / incremental sequence, parse.  
**Limited:** per-SKU live QA; long pre-RACP delay confirmed for **GL49 / GL60**.

---

### 1.3 Thermometer (FT*) — Level **B**

| Model ID | Level |
|----------|-------|
| FT09_1, FT15_1, FT16, FT17, FT58, FT65, FT85, FT90, FT95, FT100 | B |

**Strong:** 13-byte APK Temperature Measurement parse, Indicate `0x2A1C`.  
**Limited:** no live unit QA in this repo.

---

### 1.4 Pulse oximeter — Level **B**

| Model | Level | Notes |
|-------|-------|--------|
| **PO60** | B | Service `0xFF12`, notify `0xFF02`, request-more `99 01 1A`, 24-byte records |

---

### 1.5 Scales — Level **C**

| Model ID | Adv / name | Level |
|----------|------------|-------|
| BF1000, BF105, BF405, BF450, BF451, BF500, BF600 | same | C |
| BF700 | BF700, Beurer BF700 | C |
| BF710 | Beurer BF710 | C |
| BF720, BF722, BF850, BF880, BF915, BF950, BF980, BF990 | same | C |
| GS435, SRBF1 | same | C |

**Have:** UUIDs, BF700 command table, GET_MEASUREMENT flow.  
**Limited:** full weight/body-composition field layout.

---

### 1.6 ECG + BP combo — Level **B (BP) / C (ECG)**

| Model | BP (`0x2A35`) | ECG waveform |
|-------|---------------|--------------|
| BM93, BM95, BM96 | A-like | C (UUIDs + raw) |
| ME90, ME95 | A-like | C |

---

### 1.7 Activity trackers — Level **C**

| Model | Adv | Level |
|-------|-----|-------|
| AS87 | AS87 | C |
| AS98 | AS98 | C |
| AS99 | AS99 | C |
| AS80 | PR102 | C (legacy) |
| AS81 | AS81 | C (legacy) |
| AS97 | AS97 | C (legacy) |

---

### 1.8 Hydration — Level **C**

| Model | Level |
|-------|-------|
| DM20 | C (RE / subscribe-all) |

### 1.9 Listed but no solid protocol claim

| Model | Note |
|-------|------|
| BC22, BM76 | In broader device list; do not claim production support |

### 1.10 Explicitly not supported
- Beurer **OCR / camera** fallback

---

## 2. Omron

**Sources:** `medical_ble_toolkit/omron_bp` model catalog + live lab path for HEM-7143T1.  
**CLI:** `python -m medical_ble_toolkit` → Omron, or `python -m medical_ble_toolkit.omron_bp`.

### Region suffix cheat-sheet (approximate)

| Suffix / token | Typical region / channel |
|----------------|---------------------------|
| **-E**, -EBK, -ESL, -ALRU | Europe / EU retail |
| **-Z**, -CA | North America / Canada |
| **-AP**, -AAP, -AIN | Asia-Pacific / India-style |
| **-AU** | Australia |
| **-BR**, -LA | Brazil / LatAm |
| **-D** | Specific EU / DE channel |
| **-SH**, SH3 | JP / Asia channel variants |
| **BP7xxx**, **M4/M7/X4/X7**, **Evolv**, **Complete** | NA / global consumer names |

### 2.1 Modern FE4A + TOKEN unlock — Level **A** (lab-proven on 7143)

| Canonical ID | Consumer / regional aliases (examples) | Level |
|--------------|------------------------------------------|-------|
| **HEM-7143T1** | HEM-7143T1-**E**, -**AP**, -**D**, -**AIN**, -**EBK**, HEM-7143T1_D, HEM-7143T2-E, HEM-7143T2_ESL, HEM-7146T, HEM-7144T1-**AU**, … | **A** (live lab cuff) |
| HEM-7142T2 | -AP, -Z, -ZAZ; related 7140/7141/716* | A-expected |
| HEM-7155T-MW3 | X4 Smart FE4A; HEM-7155T_ESL1 | A-expected |
| HEM-7380T1 | **X7 Smart AFib**, M7 Intelli IT AFib; HEM-7183T1-AP/CAP, … | A-expected |

**Production recommendation:** ship **HEM-7143T1 family** as supported; enable other token models after one live sync QA.

### 2.2 Modern FE4A, OS bond only — Level **B**

| Canonical ID | Aliases (examples) | Level |
|--------------|--------------------|-------|
| HEM-7155T-K4 | -D, -EBK, -ESL; HEM-7340T_K4-CA/-Z | B |
| HEM-7155T-MW | modern V2 | B |
| HEM-7377T1 | **BP5360** (often NA) | B |

### 2.3 Classic multi-channel + classic unlock key — Level **B**

| Canonical ID | Regional / retail aliases (examples) | Level |
|--------------|--------------------------------------|-------|
| HEM-6232T | **RS7 Intelli IT**; -E, -Z, -AP, -D; HEM-1026T2-*; HEM-6233T, … | B |
| HEM-7322T | **M700 Intelli IT**; HEM-7280T-E/-AP, HEM-7321T-*, … | B |
| HEM-7361T | **M500 / M7 Intelli IT**; -E, -AP, -D, -EBK, -ALRU, … | B |
| HEM-7155T | **M4 Smart / X4 Smart / M400**; -E, -D, -AP, -ALRU, … | B |
| HEM-7150T | **BP7250**; -CA, -Z, -AP, -BR, -LA, … | B |
| HEM-7342T | **BP7450**; -CA, -Z, ASH3*, … | B |
| HEM-7600T | **Omron Evolv**; -E, -Z, -SH3*, … | B |
| HEM-7530T | **Omron Complete** (BP only); -E3, -AP3, -Z, BR3, … | B |
| HEM-7320T | -CA, -ZV, TI-*, … | B |
| HEM-6320T | wrist; -Z | B |
| HEM-6321T | wrist dual user; -Z | B |
| HEM-6401T | wrist family; -Z, 6402/6410 | B |
| HEM-6161T | -E, -RU, BR, SH3, … | B |
| HEM-6231T | -SH, _Z | B |
| HEM-7136T | SH3 / SH variants | B |
| HEM-7151T | -Z | B |

Full alias lists: `python -m medical_ble_toolkit.omron_bp list-models` / catalog in `omron_bp/models/profiles/catalog.py`.

---

## 3. A&D Medical

| Model | Regions | Level | Notes |
|-------|---------|-------|--------|
| **UA-651BLE** | Global (no regional SKUs in repo) | **B** | Full SDK path (`and_ua651`): BLP + custom `0xA6`/`0xE1`. Prefer Nipro profiles for げんきノート meters |

---

## 3b. Nipro げんきノート companion-like (2026-07-16)

Evidence: decompiled BLELib + NHL. Profiles in `profiles.py`.

| Profile | Adv name | Level | Session (companion) |
|---------|----------|-------|---------------------|
| **nipro_nbp** | `NBP-1BLE*` | **B** | 1s settle → write `0x2A08` → indicate BLP `0x2A35` |
| **nipro_nmbp** | `NMBP*` | **B** | Same + **bond** recommended (`--pair`) |
| **nipro_nsm1** | `NSM-1BLE*` | **B** | 1s → HTS `0x2A08` → HTP `0x2A1C` |
| **nipro_nt100b** | `NT-100B*` | **B** | HTP `0x2A1C` only; TICD power-off on disconnect |
| **nipro_cf** | `NIPRO CF*` | **B** | Proprietary CF UUIDs + RACP All; Diff/seq later |
| **nipro_nc1** | `NC-1BLE*` / Cocoron ECG | **B** (bring-up) | Stream: CONFIG+DateTime; Huffman ECG + RRI; use `nc1_session` first |
| **thermometer** | (lab) | **C** | TICD full history poll (not default companion) |
| **mightysat** | `MightySat*` | **B** | GetInfo → SetClock(ticks) → EnableStream(from info) |

CLI aliases: `nt100b`, `nbp`, `nmbp`, `nsm1`, `nc1` / `nipro_nc1`.  
**Note:** `cocoron` was an old alias for glucose CF — use `nipro_nc1` for the ECG transmitter.

**Pair registry + hands-free** (local `nipro_paired_devices.json`):

```text
python -m medical_ble_toolkit nipro pair -p nipro_nbp -a <MAC> --name "NBP-1BLE-…"
python -m medical_ble_toolkit nipro list
python -m medical_ble_toolkit nipro wait -t 3600
```

`wait` = companion ReceiveWait: exact name + CheckPairing(id) → 60s session → loop.

---

## 4. Masimo

| Model | Regions | Level | Notes |
|-------|---------|-------|--------|
| **MightySat** (consumer / RX) | Global | **B** | Same as §3b mightysat; framed `0x77` + CRC |

---

## 5. Non-contact thermometer

| Model | Profile | Level | Notes |
|-------|---------|-------|--------|
| **NT-100B** companion | `nipro_nt100b` | **B** | HTP indicate + power-off (げんきノート) |
| **NT-100B** TICD history | `thermometer` | **C** | Lab dual-wake + 0x2B/0x25/0x26 |

---

## 6. FORA

| Model | Regions | Level | Notes |
|-------|---------|-------|--------|
| **FORA 6 Connect** | — | **D** | Brochure only; RE scaffold / subscribe-all |

---

## Summary counts

| Brand / class | Level A | Level B | Level C | Level D |
|---------------|--------:|--------:|--------:|--------:|
| Beurer BP (SIG) | ~66 | — | — | — |
| Beurer glucose / FT / PO60 | — | ~21 | — | — |
| Beurer ECG (BP side) | — | 5 (BP) | 5 (ECG wave) | — |
| Beurer scales / trackers / DM20 | — | — | ~28 | — |
| Omron modern token | 1 proven + peers A-expected | — | — | — |
| Omron classic + modern none | — | rest of catalog (~20 profiles) | — | — |
| A&D UA-651BLE | — | 1 | — | — |
| Nipro companion (NBP/NMBP/NSM/NT/CF) | — | 5 | — | — |
| Masimo MightySat | — | 1 | — | — |
| NT-100B TICD lab | — | — | 1 | — |
| FORA 6 | — | — | — | 1 |

---

## Recommended ship wording (HW layer only)

**Production supported**
- Beurer SIG blood-pressure models listed under §1.1 (~66 catalog IDs + regional retail names).
- Omron **HEM-7143T1** family (and regional aliases); other token FE4A models after one live QA.

**Beta**
- Beurer GL*, FT*, PO60; Omron classic / non-token modern; A&D UA-651BLE; Nipro げんきノート profiles (NBP/NMBP/NSM/NT/CF); Masimo MightySat; Beurer ECG for BP-only.

**Experimental**
- Beurer scales, trackers, DM20, ECG waveforms; FORA scaffold.

---

## Maintenance

| Artifact | Role |
|----------|------|
| `datasheets/beurer/tools/device_registry.json` | Beurer model / adv names / categories |
| `medical_ble_toolkit/beurer/capabilities.json` | APK `mo.*` per-model flags |
| `medical_ble_toolkit/omron_bp/models/profiles/catalog.py` | Omron profiles + aliases |
| `medical_ble_toolkit/profiles.py` | Toolkit BLE profiles |
| This file | Human-facing support matrix |

When adding a device: update catalog + profile + this matrix (level A/B/C/D) in the same change.

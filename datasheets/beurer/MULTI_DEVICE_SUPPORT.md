# Multi-device support matrix (from APK)

**App:** beurer HealthManager Pro 1.20.1  
**Concrete DeviceType models:** 120  

## Category counts

| Category | Count |
|----------|------:|
| blood_pressure | 66 |
| scale | 19 |
| thermometer | 10 |
| blood_glucose | 10 |
| other | 5 |
| ecg | 5 |
| activity_tracker | 3 |
| hydration | 1 |
| pulse_oximeter | 1 |

## Protocol profiles & support tier

| Profile | Tier | Confidence | Device count | Status |
|---------|-----:|------------|-------------:|--------|
| `bp_sig` — Bluetooth SIG Blood Pressure Profile | 1 | high | 66 | Ready to implement (documented) |
| `glucose_sig` — Bluetooth SIG Glucose Service (+ custom variants) | 2 | medium | 10 | UUIDs + flow known; payloads partial |
| `thermometer_sig` — Bluetooth SIG Health Thermometer | 2 | medium | 10 | UUIDs + flow known; payloads partial |
| `scale_mixed` — Scales: SIG Weight Scale + proprietary FFF0/FFFF/BF600/BF700 | 2 | high for UUIDs; medium for full payloads | 19 | UUIDs + flow known; payloads partial |
| `tracker_as87` — AS87 activity tracker custom | 2 | high for UUIDs; medium for frames (dedicated parsers exist) | 1 | UUIDs + flow known; payloads partial |
| `tracker_as98` — AS98 activity tracker FFF0 | 2 | high for UUIDs; medium for frames | 1 | UUIDs + flow known; payloads partial |
| `tracker_as99` — AS99 activity tracker 6006 | 2 | high for UUIDs; medium for frames | 1 | UUIDs + flow known; payloads partial |
| `tracker_legacy` — Legacy trackers AS80/AS81/AS97 (limited config) | 3 | low-medium | 6 | Stub / OCR / needs more RE |
| `ecg_custom` — ECG + BP combo (BM93/95/96, ME90/95) | 2 | high for UUIDs; medium-low for full ECG sample encoding | 5 | UUIDs + flow known; payloads partial |
| `pulse_ox` — Pulse oximeter PO60 proprietary | 2 | high for UUIDs; medium for payload | 1 | UUIDs + flow known; payloads partial |
| `hydration_dm20` — DM20 hydration manager | 3 | low (sync repo exists; protocol not fully extracted) | 1 | Stub / OCR / needs more RE |
| `ocr_fallback` — Camera OCR (works for many non-BT and dual-path devices) | 3 | high that path exists | 86 | Stub / OCR / needs more RE |

## Devices by category

### blood_pressure (66)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| AUTO400 | AUTO400 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC1R | BC1R | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC21 | BC21 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC27 | BC27 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC28 | BC28 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC30 | BC30 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC32 | BC32 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC40 | BC40 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC44 | BC44 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC51 | BC51 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC54 | BC54 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC54W | PREMIUM800W | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC57 | BC57 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC58 | BC58 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC81 | BC81 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC85 | BC85 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC87 | BC87 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BC87W | SERIES800W | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM23 | BM23 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM25 | BM25 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM26 | BM26 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM27 | BM27 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM28 | BM28 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM2R | BM2R | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM30 | BM30 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM32 | BM32 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM35 | BM35 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM36 | BM36 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM38 | BM38 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM40 | BM40 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM44 | BM44 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM45 | BM45 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM46 | BM46 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM46Connect | BM46connect | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM47 | BM47 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM48 | BM48 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM49 | BM49 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM50 | BM50 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM51 | BM51 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM53 | BM53 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM54 | BM54 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM55 | BM55 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM57 | BM57 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM58 | BM58 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM59 | BM59 | `00001810-0000-1000-8000-00805f9b34fb (1810)` |  |
| BM64 | BM64 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM69 | BM69 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM77 | BM77 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM81 | BM81 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM82 | DELUXE600 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM85 | Beurer BM85 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM92 | PREMIUM800 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| DELUXE500 | DELUXE500 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| ELITE | ELITE | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| ELITE900 | ELITE900 | `00001810-0000-1000-8000-00805f9b34fb (1810)` |  |
| ELITE950 | ELITE950 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| ELITEPLUS | ELITEPLUS | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| IBC55 | IBC55 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| QUICK | QUICK | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| SENSE | SENSE | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| SERIES1000 | SERIES1000 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| SERIES600 | SERIES600 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| SERIES700 | SERIES700 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| SERIES700W | SERIES700W | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| SERIES800 | SERIES800 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| SRBM1 | SR BM1 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |

### scale (19)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| BF1000 | BF1000 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF105 | BF105 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF405 | BF405 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF450 | BF450 | `0000181d-0000-1000-8000-00805f9b34fb (181D)` |  |
| BF451 | BF451 | `0000181d-0000-1000-8000-00805f9b34fb (181D)` |  |
| BF500 | BF500 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF600 | BF600 | `0000181d-0000-1000-8000-00805f9b34fb (181D)` |  |
| BF700 | BF700, Beurer BF700 | `0000ffe0-0000-1000-8000-00805f9b34fb (FFE0)` |  |
| BF710 | Beurer BF710 | `0000ffe0-0000-1000-8000-00805f9b34fb (FFE0)` |  |
| BF720 | BF720 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF722 | BF722 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF850 | BF850 | `0000fff0-0000-1000-8000-00805f9b34fb (FFF0)` |  |
| BF880 | BF880 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF915 | BF915 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF950 | BF950 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF980 | BF980 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| BF990 | BF990 | `0000ffff-0000-1000-8000-00805f9b34fb (FFFF)` |  |
| GS435 | GS435 | `0000181d-0000-1000-8000-00805f9b34fb (181D)` |  |
| SRBF1 | SRBF1 | `0000fff0-0000-1000-8000-00805f9b34fb (FFF0)` |  |

### blood_glucose (10)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| GL22 | GL22 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL34 | GL34 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL40 | GL40 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL44 | GL44 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL44Lean | GL44lean | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL48 | GL48 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL49 | GL49 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL50 | GL50 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |
| GL50EVO | Beurer GL50EVO | `00001808-0000-1000-8000-00805f9b34fb (1808)` |  |
| GL60 | GL60 | `00001808-0000-1000-8000-00805f9b34fb (1808)` | yes |

### thermometer (10)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| FT09_1 | FT09_1 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT100 | FT100 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT15_1 | FT15_1 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT16 | FT16 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT17 | FT17 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT58 | FT58 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT65 | FT65 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT85 | FT85 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT90 | FT90 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |
| FT95 | FT95 | `00001809-0000-1000-8000-00805f9b34fb (1809)` | yes |

### ecg (5)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| BM93 | BM93 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM95 | BM95 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| BM96 | BM96 | `00001810-0000-1000-8000-00805f9b34fb (1810)` | yes |
| ME90 | ME90 | `00001810-0000-1000-8000-00805f9b34fb (1810)` |  |
| ME95 | ME95 | `00001810-0000-1000-8000-00805f9b34fb (1810)` |  |

### other (5)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| AS80 | PR102 | `0000fed0-494c-4f47-4943-544543480000 (AS81)` |  |
| AS81 | AS81 | `0000fed0-494c-4f47-4943-544543480000 (AS81)` |  |
| AS97 | AS97 | `7905ff00-b5ce-4e99-a40f-4b1e122d00d0 (AS87)` |  |
| BC22 | BC22 | `00001810-0000-1000-8000-00805f9b34fb (1810)` |  |
| BM76 | BM76 | `00001810-0000-1000-8000-00805f9b34fb (1810)` |  |

### activity_tracker (3)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| AS87 | AS87 | `7905ff00-b5ce-4e99-a40f-4b1e122d00d0 (AS87)` |  |
| AS98 | AS98 | `0000fff0-0000-1000-8000-00805f9b34fb (AS98)` |  |
| AS99 | AS99 | `00006006-0000-1000-8000-00805f9b34fb (AS99)` |  |

### hydration (1)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| DM20 | DM20 | `00001810-0000-1000-8000-00805f9b34fb (1810)` |  |

### pulse_oximeter (1)

| Model | Adv names | Scan UUID / method | OCR |
|-------|-----------|--------------------|-----|
| PO60 | PO60 | `0000ff12-0000-1000-8000-00805f9b34fb (FF12)` |  |

## Recommended build strategy for max device coverage

1. **One shared BP SIG client** covers the largest set (~69 BP models) with one parser.
2. **Glucose + thermometer SIG clients** next (standard profiles).
3. **Profile plugins** for PO60, AS87/98/99 (isolated UUID maps already extracted).
4. **Scale plugin** with BF700 command enum + BF600 service map (large effort).
5. **ECG plugin** for BM93/95/96/ME9x (custom 6E80… + shared BP measurement).
6. **OCR fallback** for non-BLE / failed BLE using app TFLite assets.

## What is fully documented vs remaining work

| Area | Documented? | Notes |
|------|-------------|-------|
| Full device list + categories + adv names | Yes | `tools/device_registry.json` |
| Discover/scan UUID per provider method | Yes | this file + registry |
| BP send/receive/parse | Yes | `BLE_PROTOCOL_ANALYSIS.md` |
| Scale service/char map | Yes (UUIDs) | payloads partial |
| Tracker service/char map AS87/98/99 | Yes (UUIDs) | frame parsers need deep dive |
| ECG custom UUIDs + BP overlap | Yes | ECG waveform encoding partial |
| Pulse ox UUIDs | Yes | payload partial |
| Glucose RACP opcodes | Partial | needs Gl50SyncRepo pass |
| Per-device capability markers (mo.*) | Partial | feature flags not fully named |

See also: `BLE_PROTOCOL_ANALYSIS.md` (BP deep dive), `tools/device_registry.json` (machine-readable).
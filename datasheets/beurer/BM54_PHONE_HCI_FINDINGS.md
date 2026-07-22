# Beurer BM54 findings (phone HCI dump + HealthManager Pro)

**Source capture:** `btsnoop_hci_202607221152.cfa`  
**Readable decode:** `btsnoop_hci_202607221152.txt` (via `phoneblelog/btsnoop_to_text.py`)  
**Capture window:** 2026-07-22 **11:47:13 – 11:53:28 UTC** (~375 s total HCI log)  
**App:** Beurer HealthManager Pro (phone ↔ BM54)  
**Analysis helper:** `_analyze_cfa.py` (optional one-shot; generic decode uses `phoneblelog/btsnoop_to_text.py`)

> **Not this file:** `btsnoop_hci_202607151125.cfa` is an **Omron HEM-7143T1** dump (see `phoneblelog/OMRON_FINDINGS.md`). It is not Beurer traffic.

---

## Device identity (confirmed)

| Field | Value |
|--------|--------|
| BLE address | **`0C:7F:ED:72:BC:40`** (HCI **public** address type = 0) |
| Advertised name | **`BM54`** (Complete Local Name, AD type `0x09`) |
| Manufacturer company ID | **`0x0611`** = Beurer GmbH |
| Mfg AD payload | **`01 03`** (passkey-capable generation; matches protocol PDF “newer” form) |
| Service in AD | Incomplete 16-bit UUID list includes **`0x1810`** (Blood Pressure) |
| Clinical protocol | Bluetooth SIG **BLP** — service `0x1810`, measurement **Indicate** on **`0x2A35`** |
| Proprietary Omron FE4A / BLESmart | **Not used** |

### Advertising payload (decoded from capture)

Typical AD structure seen with the name:

```text
Flags + …
Incomplete UUID16: 1810
Mfg Specific: company=0x0611  payload=01 03
Complete Local Name: "BM54"
```

Raw fragment around name (from packet analysis):

```text
03 02 10 18          ; Incomplete list of 16-bit UUIDs: 0x1810
05 FF 11 06 01 03    ; Mfg: CID 0x0611 LE, data 01 03
05 09 42 4D 35 34    ; Complete name "BM54"
```

BM54-name advertisements in this log appeared for only **~0.8 s** before the phone connected (phone grabbed the radio quickly). That is **not** proof the cuff only advertises for 0.8 s in free field — only that connect was almost immediate after the first logged name hit.

---

## Connection session (one successful link)

| Event | Time (UTC) | Notes |
|--------|------------|--------|
| First `BM54` AD | 11:49:35.261 | |
| LE Enhanced Connection Complete | **11:49:36.105** | handle **`0x000B`**, peer `0C:7F:ED:72:BC:40` |
| Early GATT discovery | 11:49:36.7+ | Many ATT **Error 0x0A** (Attribute Not Found) while unauthenticated |
| CCCD Indicate attempt | 11:49:40.161 | handle **`0x0013`** ← `02 00` |
| CCCD fails auth | 11:49:40.266 | ATT Error **0x05** Insufficient Authentication |
| SMP Pairing Request | 11:49:40.277 | code `0x01` |
| SMP complete (keys) | ~11:49:48.668 | ~**8.4 s** of pairing (passkey UI window) |
| CCCD Indicate (retry) | 11:49:49.262 | handle `0x0013` ← `02 00` (OK) |
| BP Indications (14×) | 11:49:49.435 – 11:49:50.804 | handle **`0x0012`**, ~100 ms spacing |
| Extra CCCD Indicate | 11:49:49.998 | handle **`0x000F`** ← `02 00` |
| Disconnect | **11:49:52.744** | |

| Metric | Value |
|--------|--------|
| Connect → disconnect | **~16.6 s** |
| Pairing (SMP start → keys) | **~8.4 s** |
| All 14 BP frames after successful CCCD | **~1.37 s** |
| Host download command | **None** — pure auto-dump after Indicate enable |

---

## GATT / ATT sequence (what the app did)

### Handles used on this firmware instance

| Handle | Role in this capture |
|--------|----------------------|
| **`0x0012`** | Blood Pressure Measurement **value** (Handle Value **Indication**) |
| **`0x0013`** | CCCD for measurement — write **`02 00`** (Indicate) |
| **`0x000F`** | Second CCCD Indicate (app also enables another indicate char; not required for BP parse) |

UUIDs are standard BLP; numeric handles are **instance-specific** (phone discovered them). Hub code should prefer **UUID `0x2A35` / CCCD `0x2902`**, not hardcode `0x0012`/`0x0013`.

### Order that matters

```text
1. Connect
2. Discover services / characteristics  (pre-bond reads often fail 0x0A)
3. Attempt CCCD Indicate on BP CCCD
   → if Error 0x05 Insufficient Authentication → go to 4
4. SMP bond / passkey (KeyboardDisplay / 6-digit on cuff LCD)
5. Retry CCCD Indicate  02 00
6. Receive Handle Value Indication × N on BP measurement handle
7. (optional) other CCCDs / cleanup
8. Disconnect
```

**There is no proprietary “download history” write.** After encryption + CCCD Indicate, the cuff streams stored measurements as indications (same as Beurer PDF + `BLE_PROTOCOL_ANALYSIS.md`).

### ATT error codes observed

| Code | Meaning | When |
|------|---------|------|
| **0x0A** | Attribute Not Found | Pre-bond discovery probes |
| **0x05** | Insufficient Authentication | CCCD write before SMP complete |

### SMP (CID 0x0006) — passkey path present

| Code | Name (approx) | Time |
|------|----------------|------|
| 0x01 | Pairing Request | 11:49:40.277 |
| 0x02 | Pairing Response | 11:49:40.370 |
| 0x03 | Pairing Confirm | 11:49:48.258 / .350 |
| 0x04 | Pairing Random | 11:49:48.351 / .456 |
| 0x06–0x0A | Encryption / identity / signing info | 11:49:48.667 |

Pairing Request bytes: `01 04 00 0d 10 0f 0f`  
Pairing Response bytes: `02 00 00 05 10 00 07`  

This is a **real bonding session** (not Just Works only). Hub on BlueZ needs a **KeyboardDisplay** (or equivalent) agent and the **6-digit LCD passkey** for first pair — consistent with `BeurerCompanionSession` + `pair_client(use_passkey_agent=…)`.

---

## Clinical payload (BLP Indicate)

### Wire format (all 14 frames)

- Length: **19 bytes**
- Flags: **`0x1E`**
  - bit1 timestamp  
  - bit2 pulse  
  - bit3 user id  
  - bit4 measurement status  
- Units: mmHg (flag bit0 clear)
- Systolic / Diastolic / MAP: IEEE 11073 **SFLOAT** LE  
- MAP: **always 0** on this device (matches Beurer docs)
- Timestamp: 7-byte BLP date-time  
- Pulse: SFLOAT LE  
- User id: 1 byte  
- Status: uint16 LE  

### Example (first indication)

```text
1E 79 00 57 00 00 00 E6 07 01 01 00 02 00 62 00 00 00 00
```

| Field | Bytes | Decoded |
|-------|--------|---------|
| flags | `1E` | time + pulse + user + status |
| systolic | `79 00` | **121** mmHg |
| diastolic | `57 00` | **87** mmHg |
| MAP | `00 00` | 0 |
| year | `E6 07` | 2022 |
| mon/day/h/m/s | `01 01 00 02 00` | 2022-01-01 00:02:00 (device clock as stored) |
| pulse | `62 00` | **98** bpm |
| user | `00` | user 1 (BLP: 0 = user1) |
| status | `00 00` | 0 |

### All 14 measurements (sys / dia / pulse)

| # | Sys | Dia | Pulse |
|---|-----|-----|-------|
| 1 | 121 | 87 | 98 |
| 2 | 119 | 85 | 94 |
| 3 | 135 | 109 | 80 |
| 4 | 131 | 86 | 82 |
| 5 | 141 | 89 | 85 |
| 6 | 134 | 97 | 83 |
| 7 | 123 | 97 | 86 |
| 8 | 120 | 96 | 86 |
| 9 | 120 | 84 | 91 |
| 10 | 122 | 88 | 83 |
| 11 | 114 | 88 | 79 |
| 12 | 130 | 90 | 92 |
| 13 | 104 | 74 | 79 |
| 14 | 115 | 84 | 98 |

Full hex for each frame is in the analysis output / readable log around packets **#9988–#10069**.

Indication → Confirmation: phone sent **Handle Value Confirmation** (`0x1E`) for each indication (14 confirmations).

---

## Timing takeaways for Pi hub

| Observation | Hub implication |
|-------------|-----------------|
| Bond required before CCCD succeeds | First pair: passkey agent; later sessions: OS bond must already exist |
| CCCD before bond → `0x05` | Order: **pair → then Indicate CCCD**; soft-continue without bond will get zero BP |
| Dump is ~1.4 s once CCCD works | Quiet-end ~3–5 s after last indication is enough |
| Full radio session ~17 s (including pair) | After bond, connect+dump can be **&lt; 10 s** if discovery is lean |
| No history download write | Do **not** wait for a proprietary command; enable Indicate and listen |
| Short AD window after measure | Scan/connect must be **fast** (same class as other windowed meters) |
| 14 records @ ~100 ms | Multi-record dump; do not quiet-end on first packet only without timer restart |

### Suggested host sequence (aligned with this capture + companion notes)

```text
connect (MAC string on Linux BlueZ)
optional: short settle
if not bonded: pair with passkey agent (6-digit from LCD)
discover / resolve 0x2A35 + CCCD
start_notify/indicate → CCCD 02 00
listen until quiet (~4 s no indication) or max budget
disconnect
```

Matches existing docs:

- `BLE_PROTOCOL_ANALYSIS.md` (BTFlowTimer quiet ~4 s, auto dump)
- `TIER1_TIER2_PROTOCOLS.md` BP SIG section
- Toolkit `brands/beurer/session.py` + `timing.py` (BP quiet_timeout_s ≈ 4)

---

## ATT opcode histogram (session-related)

| Op | Count | Role |
|----|------:|------|
| 0x08 | 21 | Read By Type Request |
| 0x1D | 14 | **Handle Value Indication** (BP) |
| 0x1E | 14 | Handle Value Confirmation |
| 0x01 | 13 | Error Response |
| 0x09 | 11 | Read By Type Response |
| 0x1B | 8 | Notification (other device/session noise earlier in log) |
| 0x12 | 3 | Write Request (incl. CCCDs) |

---

## How to re-decode this capture

Generic full log (already generated):

```bash
python phoneblelog/btsnoop_to_text.py \
  datasheets/beurer/btsnoop_hci_202607221152.cfa \
  -o datasheets/beurer/btsnoop_hci_202607221152.txt \
  --skip-num-completed
```

Optional structured summary:

```bash
python datasheets/beurer/_analyze_cfa.py
```

**Note:** `phoneblelog/extract_omron.py` is **hardcoded** for Omron MAC `E1:99:7D:27:1C:0A` and will **not** extract BM54 without changes.

---

## Related docs / code

| Path | Relevance |
|------|-----------|
| `BM54_transmissionprotocol_rev03_20210716.pdf` | Official wire / AD / passkey notes |
| `BLE_PROTOCOL_ANALYSIS.md` | HealthManager Pro APK sequence |
| `TIER1_TIER2_PROTOCOLS.md` | Shared BLP stack |
| `medical_ble_toolkit/parsers/blood_pressure.py` | Pure BLP parser (flags `0x1E` + SFLOAT) |
| `medical_ble_toolkit/brands/beurer/session.py` | Companion session + Linux MAC connect + passkey pair |
| `phoneblelog/OMRON_FINDINGS.md` | Different device (do not mix captures) |

---

## Summary

1. Phone dump **is genuine BM54 + Beurer app** traffic.  
2. Protocol is **standard BLP Indicate dump**, not a custom serial protocol.  
3. **Passkey bonding is mandatory** before CCCD/indications work on this unit.  
4. After bond + CCCD `02 00`, cuff sent **14** complete BP records in **~1.4 s**.  
5. Pi hub should: **fast connect after AD → ensure bond → Indicate CCCD → quiet-end listen** — no download command.

---

*Generated from HCI analysis of `btsnoop_hci_202607221152.cfa` (2026-07-22).*

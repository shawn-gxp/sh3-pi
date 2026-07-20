# Worked example: parsing raw BLE data from **your** device

**Device:** Omron **HEM-7143T1** (modern FE4A stack)  
**Source:** real capture from this lab PC during a successful `omron_bp read`  
**Goal:** show, byte by byte, how a raw response becomes  
`datetime / sys / dia / bpm / mov / ihb`

You only need this document to explain the **7143T1** parse path.  
Other Omron models may use different layouts.

---

## 0. Big picture (one measurement journey)

```text
  CUFF EEPROM                          BLE                            YOUR APP
 ─────────────                    ─────────────                    ─────────────
 14-byte slot  ──read command──►  notify packet  ──extract──►  14 raw bytes
 at 0x02E8 …                      (type 0x8100)                ──parse──► sys/dia/…
```

For this device:

| Setting | Value |
|---------|--------|
| BLE parent service | `0000fe4a-0000-1000-8000-00805f9b34fb` |
| Notify (RX) | `49123040-aee8-11e1-a74d-0002a5d5c51b` |
| Write (TX) | `db5b55e0-aee7-11e1-965e-0002a5d5c51b` |
| First record address | **`0x02E8`** |
| Records stored | **30** |
| Bytes per record | **14** (`0x0E`) |
| Full bank size | 30 × 14 = **420** bytes |

---

## 1. Session framing (context only)

Before memory reads, host and cuff exchange fixed commands (hex):

| Step | Host → cuff (TX) | Cuff → host (RX type) |
|------|------------------|------------------------|
| Start | `08 00 00 00 00 10 00 18` | `80 00` |
| Read block | see §2 | `81 00` |
| End | `08 0F 00 00 00 00 00 07` | `8F 00` |

This example focuses on **one successful read response** and how to decode the **measurement bytes** inside it.

---

## 2. Real raw packet (from your log)

### 2.1 Full notify payload (64 bytes)

Captured when reading address `0x02E8`, size `0x38` (56 data bytes):

```text
40 81 00 02 E8 38 51 42 51 1A 92 16 BB 14 80 00
5A 00 4F B0 50 41 57 1A AC 1B DF 15 80 00 5B 00
98 67 52 42 60 1A D1 1B A5 1D 80 00 5C 00 98 67
80 5C 4F 1A 29 1C 5A 1B 80 00 5D 00 DC 23 00 A4
```

As one hex string:

```text
40810002e8385142511a9216bb1480005a004fb05041571aac1bdf1580005b0098675242601ad11ba51d80005c009867805c4f1a291c5a1b80005d00dc2300a4
```

### 2.2 Split the **packet header** (first 6 bytes)

| Bytes (hex) | Meaning |
|-------------|---------|
| `40` | Total packet length = **64** decimal |
| `81 00` | Response type = **EEPROM read OK** |
| `02 E8` | EEPROM address that was read = **`0x02E8`** |
| `38` | Payload data length = **56** (`0x38`) bytes |

```text
 Index:   0     1  2     3  4     5
        [40]  [81 00]  [02 E8]  [38]  [ ..... 56 data bytes ..... ] [pad/crc]
         │       │        │       │
         │       │        │       └── how many measurement bytes follow
         │       │        └── where in cuff memory this came from
         │       └── “this is a read reply”
         └── length of whole packet
```

### 2.3 Integrity check (XOR)

XOR **every** byte of the 64-byte packet:

```text
0x40 ⊕ 0x81 ⊕ 0x00 ⊕ … ⊕ 0xA4  ==  0
```

If the result is not `0`, the packet is corrupted — discard it.

---

## 3. Pull out the **measurement payload**

Skip the 6-byte header; take the next **56** bytes (`0x38`):

```text
51 42 51 1A 92 16 BB 14 80 00 5A 00 4F B0
50 41 57 1A AC 1B DF 15 80 00 5B 00 98 67
52 42 60 1A D1 1B A5 1D 80 00 5C 00 98 67
80 5C 4F 1A 29 1C 5A 1B 80 00 5D 00 DC 23
```

That is **exactly four records** of 14 bytes each:

```text
56 ÷ 14 = 4 records in this one BLE reply
```

(A full download needs more replies until 420 bytes / 30 records are collected.)

```text
Payload:
├─ record 0  [14 bytes]  offsets  0 … 13
├─ record 1  [14 bytes]  offsets 14 … 27
├─ record 2  [14 bytes]  offsets 28 … 41
└─ record 3  [14 bytes]  offsets 42 … 55
```

---

## 4. One record in detail (record 0)

### 4.1 The 14 raw bytes

```text
Index:  0  1  2  3  4  5  6  7  8  9 10 11 12 13
Hex:   51 42 51 1A 92 16 BB 14 80 00 5A 00 4F B0
Dec:   81 66 81 26 ...
```

### 4.2 Byte map for HEM-7143T1 (`classic_vital_14`)

| Byte(s) | Role | How to read |
|---------|------|-------------|
| **0** | Systolic (encoded) | `sys_mmHg = byte0 + 25` |
| **1** | Diastolic | `dia_mmHg = byte1` |
| **2** | Pulse | `bpm = byte2` |
| **3** | Year | `year = 2000 + (byte3 & 0x3F)` |
| **4–5** | Date/time flags (low, high) | Build `flags1 = byte4 \| (byte5 << 8)` |
| **6–7** | More time + quality flags | Build `flags2 = byte6 \| (byte7 << 8)` |
| **8–13** | Extra / id (not needed for basic vitals) | Can ignore for sys/dia/bpm/time |

Empty slot rules (skip record if):

- `byte0 > 0xE1`, or  
- dia/bpm/year bits and both flag words are all zero, or  
- invalid calendar date  

### 4.3 Compute vitals from this example

```text
byte0 = 0x51 = 81
  sys = 81 + 25 = 106 mmHg

byte1 = 0x42 = 66
  dia = 66 mmHg

byte2 = 0x51 = 81
  bpm = 81

byte3 = 0x1A = 26
  year = 2000 + (26 & 0x3F) = 2000 + 26 = 2026
```

### 4.4 Unpack `flags1` from bytes 4–5

```text
byte4 = 0x92
byte5 = 0x16
flags1 = 0x92 | (0x16 << 8) = 0x1692
```

Bit fields inside `flags1` (bit 0 = least significant bit):

| Bits | Width | Field | Formula | This packet |
|------|-------|-------|---------|-------------|
| 0–4 | 5 | hour | `flags1 & 0x1F` | **18** |
| 5–9 | 5 | day | `(flags1 >> 5) & 0x1F` | **20** |
| 10–13 | 4 | month | `(flags1 >> 10) & 0x0F` | **5** |
| 14 | 1 | ihb | `(flags1 >> 14) & 1` | **0** |
| 15 | 1 | mov | `(flags1 >> 15) & 1` | **0** |

```text
datetime date part → 2026-05-20 , hour 18
ihb = 0 (no irregular heartbeat flag)
mov = 0 (no movement flag)
```

### 4.5 Unpack `flags2` from bytes 6–7

```text
byte6 = 0xBB
byte7 = 0x14
flags2 = 0xBB | (0x14 << 8) = 0x14BB
```

| Bits | Field | Formula | This packet |
|------|-------|---------|-------------|
| 0–5 | second | `min(flags2 & 0x3F, 59)` | **59** |
| 6–11 | minute | `min((flags2 >> 6) & 0x3F, 59)` | **18** |
| 12 | cuff (optional) | `(flags2 >> 12) & 1` | 1 |
| 13 | battery (optional) | `(flags2 >> 13) & 1` | 0 |
| 14–15 | pos (optional) | `(flags2 >> 14) & 3` | 0 |

```text
time → 18:18:59
```

### 4.6 Final parsed reading (record 0)

```text
┌─────────────────────────────────────────────┐
│  2026-05-20 18:18:59                        │
│  SYS = 106 mmHg                             │
│  DIA =  66 mmHg                             │
│  BPM =  81                                  │
│  mov = 0    ihb = 0                         │
└─────────────────────────────────────────────┘
```

Matches the app log line:

```text
user1 offset=0x0000 OK sys=106 dia=66 bpm=81 dt=2026-05-20 18:18:59
```

---

## 5. Same method on the next three records (quick view)

| # | 14-byte hex (start) | sys | dia | bpm | datetime |
|---|---------------------|-----|-----|-----|----------|
| 0 | `51 42 51 1A 92 16 …` | 106 | 66 | 81 | 2026-05-20 18:18:59 |
| 1 | `50 41 57 1A AC 1B …` | 105 | 65 | 87 | 2026-06-29 12:23:31 |
| 2 | `52 42 60 1A D1 1B …` | 107 | 66 | 96 | 2026-06-30 17:54:37 |
| 3 | `80 5C 4F 1A 29 1C …` | 153 | 92 | 79 | 2026-07-01 09:45:26 |

Record 3 check (sys only):

```text
byte0 = 0x80 = 128
sys   = 128 + 25 = 153   ✓
```

---

## 6. Pseudocode you can implement anywhere (Kotlin / Python / …)

```text
function parse_hem7143t1_record(b: byte[14]) -> Reading or Empty:

    raw_sys = b[0]
    if raw_sys > 0xE1: return Empty

    sys = raw_sys + 25
    dia = b[1]
    bpm = b[2]
    year = 2000 + (b[3] & 0x3F)

    flags1 = b[4] | (b[5] << 8)
    flags2 = b[6] | (b[7] << 8)

    hour  =  flags1        & 0x1F
    day   = (flags1 >>  5) & 0x1F
    month = (flags1 >> 10) & 0x0F
    ihb   = (flags1 >> 14) & 0x01
    mov   = (flags1 >> 15) & 0x01

    second = min( flags2        & 0x3F, 59)
    minute = min((flags2 >>  6) & 0x3F, 59)

    if dia==0 and bpm==0 and (b[3]&0x3F)==0 and flags1==0 and flags2==0:
        return Empty
    if month not in 1..12 or day not in 1..31 or hour > 23:
        return Empty

    return Reading(year, month, day, hour, minute, second, sys, dia, bpm, mov, ihb)
```

### Full bank after you have all EEPROM bytes

```text
blob = concatenate all read payloads for addresses starting 0x02E8
       total length 420

for i in 0, 14, 28, … while i+14 <= 420:
    rec = parse_hem7143t1_record(blob[i : i+14])
    if rec is not Empty: keep it

sort by datetime, newest first   // presentation only
```

---

## 7. How the **host read command** looks (for completeness)

To request those 56 bytes starting at `0x02E8`:

```text
Build:
  08 01 00 02 E8 38     ← length, type=read, address, size
  then pad 0x00 and CRC so XOR of whole command is 0

Actual command used by omron_bp in the log:
  08 01 00 02 E8 38 00 DB
```

| Bytes | Meaning |
|-------|---------|
| `08` | command length |
| `01 00` | read EEPROM |
| `02 E8` | start address |
| `38` | read 56 bytes |
| `00 DB` | padding + XOR CRC |

Cuff answers with the **64-byte** packet dissected in §2.

---

## 8. Teaching diagram (one record)

```text
 51  42  51  1A  92  16  BB  14  80  00  5A  00  4F  B0
 │   │   │   │   └─┬──┘  └─┬──┘  └──────── extras ────────┘
 │   │   │   │     │        │
 │   │   │   │     │        └─ flags2 → minute, second, …
 │   │   │   │     └─ flags1 → hour, day, month, ihb, mov
 │   │   │   └─ year offset (26 → 2026)
 │   │   └─ bpm = 81
 │   └─ dia = 66
 └─ raw sys 81 → display sys 106
```

---

## 9. Common mistakes (for this device)

| Mistake | Wrong result |
|---------|----------------|
| Forget `sys = raw + 25` | Systolic 25 too low (e.g. 81 instead of 106) |
| Treat bytes 4–5 as two separate fields without combining | Broken date/time |
| Use bit-packed classic parser (7322 style) | Nonsense dates / swapped values |
| Use only device “now” timestamp for all rows | All 30 readings share one time |
| Assume SHHM `0x34` / LE-word parser | Different format — not this EEPROM slot |

---

## 10. One-sentence summary

> On the **HEM-7143T1**, each stored measurement is a **14-byte** EEPROM slot:  
> **byte0+25 → sys**, **byte1 → dia**, **byte2 → bpm**, **byte3 → year-2000**,  
> **bytes 4–7 → packed date/time and flags**; BLE only wraps those bytes in an `81 00` read packet starting at address **`0x02E8`**.

---

*Example packet taken from live lab read of device `E1:99:7D:27:1C:0A` (HEM-7143T1).*

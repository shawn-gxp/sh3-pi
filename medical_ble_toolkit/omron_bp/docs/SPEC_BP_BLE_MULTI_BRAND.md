# Blood Pressure BLE Bridge — Complete Implementation Specification

**Document ID:** `SPEC-BP-BLE-MULTI-BRAND`  
**Version:** `1.0.0`  
**Date:** `2026-07-14`  
**Status:** Normative for re-implementation  
**Audience:** Engineers and AI agents building a full multi-brand BP BLE application  

---

## 0. How to use this document

### 0.1 Purpose

This specification is a **self-contained blueprint** for building software that:

1. Discovers Bluetooth Low Energy (BLE) blood pressure monitors  
2. Pairs / bonds with them  
3. Downloads stored measurement history from device memory  
4. Decodes raw bytes into clinical fields (systolic, diastolic, pulse, timestamp, flags)  
5. Exports or stores those measurements  
6. Scales from **one Omron model** → **many Omron models** → **other brands**

An implementer (human or AI) should be able to rebuild a production-grade system **using only this document** plus a BLE stack for the target OS (e.g. Windows BLE / bleak / CoreBluetooth / Android BluetoothGatt). No access to the original experimental repository is required.

### 0.2 Companion machine-readable files (same folder)

| File | Role |
|------|------|
| `SPEC_BP_BLE_MULTI_BRAND.md` | **This document** — normative prose, algorithms, schemas |
| `_catalog_export.json` | Machine dump of all Omron profiles (canonical numeric fields) |
| `_table.md` | Compact markdown table of profiles |
| `_aliases.md` | Alias → canonical model_id map |

If prose and JSON ever disagree on a number, prefer **`_catalog_export.json`** for addresses/counts and re-check parser sections here for decode rules.

### 0.3 Non-goals of this document

- Clinical validation, medical device certification, or regulatory strategy  
- ECG/EKG download from Omron Complete (BP path only is specified)  
- Cloud/HIPAA architecture (only measurement extraction is specified)  
- Fully reverse-engineered encrypted “secure session” Omron variants (called out as unsupported)  

### 0.4 Glossary

| Term | Definition |
|------|------------|
| **Cuff / device** | The blood pressure monitor (often Omron HEM-…T) |
| **Host** | PC, phone, hub, or server-side agent running this software |
| **BLE** | Bluetooth Low Energy |
| **GATT** | BLE attribute protocol (services / characteristics) |
| **EEPROM** | On-device non-volatile memory holding past measurements |
| **Profile** | Static description of one device family (UUIDs, addresses, parser) |
| **Parser** | Pure function: record bytes → vital-sign dictionary |
| **Stack (classic/modern)** | Omron BLE generation (multi-channel legacy vs FE4A single-channel) |
| **Pairing** | Making the host trusted by the cuff (once per host, typically) |
| **Unlock** | Per-session step before memory access (key / token / none) |
| **Transfer mode** | Cuff awake for data download (not the “P” pairing screen) |
| **sys / dia / bpm** | Systolic mmHg, diastolic mmHg, pulse rate |
| **ihb** | Irregular heartbeat flag |
| **mov** | Body movement flag during measurement |
| **Alias** | Regional / sold-as model string that maps to a canonical profile |

---

## 1. Problem statement

### 1.1 What users need

- Pair a BP monitor to a hub/PC once  
- After each measurement (or on schedule), download history  
- Obtain structured records: `{datetime, sys, dia, bpm, mov, ihb, …}`  
- Support **many Omron SKUs** and later **other brands** without rewriting the whole app  

### 1.2 Why it is not trivial

Most Omron Bluetooth cuffs do **not** primarily use the standard Bluetooth SIG Blood Pressure Profile (`0x1810` / `0x2A35`) for history download. Instead they expose a **vendor proprietary GATT service** and a **private memory-read protocol** over custom characteristics.

Therefore the application must:

1. Speak Omron’s (or another brand’s) **transport protocol**  
2. Know each model’s **memory map**  
3. Apply the correct **record decoder**  

### 1.3 Design principle (mandatory)

```text
SEPARATE:
  (A) Brand transport protocol   — how bytes move over BLE
  (B) Device profile             — where data lives for this model
  (C) Record parser              — how one measurement is packed
  (D) Application workflows      — pair, read, export, UI/API
  (E) Clinical storage           — patient DB, not EEPROM maps
```

Never mix (E) patient data tables with (B) device encyclopedia.

---

## 2. Target architecture (multi-brand ready)

### 2.1 Logical module map

```text
┌─────────────────────────────────────────────────────────────┐
│  APPLICATION LAYER                                          │
│  CLI | Desktop UI | Mobile | REST API | Hub agent           │
│  Commands: scan | pair | read | list-models | export        │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│  WORKFLOW LAYER                                             │
│  PairWorkflow(brand, model, address)                        │
│  ReadWorkflow(brand, model, address) → List[UserRecords]    │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│  BRAND ADAPTER REGISTRY                                     │
│  brand_id → BrandAdapter { scan_hints, session, transport } │
│                                                             │
│  adapters/omron/     ← fully specified in this document     │
│  adapters/<other>/   ← same interfaces, different protocol  │
└────────────────────────────┬────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
┌───────────────┐   ┌────────────────┐   ┌────────────────┐
│ BLE SESSION   │   │ TRANSPORT      │   │ PROFILE CATALOG│
│ connect       │   │ start/read/end │   │ model → map    │
│ OS pair       │   │ unlock         │   │ + parser name  │
│ disconnect    │   │ (brand-specific)│   │                │
└───────────────┘   └────────────────┘   └────────┬───────┘
                                                  │
                                         ┌────────▼───────┐
                                         │ PARSER REGISTRY│
                                         │ name → function│
                                         └────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│  EXPORT / DOMAIN                                            │
│  CSV | JSON | FHIR Observation | local DB rows              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Recommended package layout (language-agnostic)

```text
bp_bridge/
  app/                      # CLI or API entrypoints only
  workflows/
    pair.py
    read.py
  ble/
    scanner.py              # OS-agnostic discovery interface
    session.py              # connect / disconnect / os_pair
  brands/
    registry.py             # brand_id → adapter
    omron/
      transport.py          # Omron packet protocol (Section 5)
      unlock.py             # classic key / token (Section 6)
      constants.py          # UUIDs
    # future: andon/, beurer/, microlife/, ...
  profiles/
    schema.md               # DeviceProfile schema (Section 7)
    omron/
      catalog.json          # or YAML — all models
    # future: other brands
  parsers/
    registry.py             # parser_id → implementation
    omron/
      classic_vital_14.py
      classic_vital_14_bitpacked.py
      classic_vital_14_6232.py
      vital_16_715x.py
      vital_16_6401.py
  export/
    csv.py
  config/
    paired_devices.json     # MAC, model, brand, last_seen (per install)
```

### 2.3 Interfaces (must implement)

#### 2.3.1 DeviceProfile (data only)

See **Section 7** for full field list. Profiles contain **no BLE calls**.

#### 2.3.2 BrandTransport

```text
interface BrandTransport:
  start_session()
  end_session()
  read_memory(address: uint16, length: int) -> bytes
  write_memory(address: uint16, data: bytes) -> void   # optional; dangerous
  unlock_if_required(profile, secrets) -> void
```

#### 2.3.3 Parser

```text
function parse_record(raw: bytes, endianness: "little"|"big") -> Record | throws EmptySlot
```

#### 2.3.4 Canonical measurement Record

```text
Record {
  datetime: ISO-8601 local or naive local wall time from device clock
  sys: int          # mmHg
  dia: int          # mmHg
  bpm: int          # pulse
  mov: int          # 0/1 or small enum
  ihb: int          # 0/1 or small enum
  # optional extras when present:
  pos?: int
  battery?: int
  cuff?: int
}
```

---

## 3. Physical device UX (Omron)

### 3.1 Bluetooth button modes

| Mode | How user activates | Display (typical) | When to use |
|------|--------------------|-------------------|-------------|
| **Pairing** | Hold Bluetooth button ~3–5 seconds | Flashing **`P`** or **`-P-`** | First-time bond / key program |
| **Transfer / sync** | Short press Bluetooth once | Flashing squares / transfer icon | Download measurements |

### 3.2 Host exclusivity

Many Omron cuffs allow **only one paired host** at a time (phone OMRON connect **or** PC, not both).  

**Operator rule:** If PC pairing fails, forget the device on the phone and in Windows Bluetooth settings, then pair again in **P** mode.

### 3.3 Discovery hints

| Hint | Value |
|------|--------|
| Advertised name pattern | Often `BLESmart_<hex>` or contains `OMRON` / model |
| Manufacturer company ID | `0x020E` (Omron) frequently present in manufacturer data |
| Windows “Add a device” | Often **does not** list these cuffs — use BLE scan API / this app |

### 3.4 Lab reference device (proven on Windows)

| Field | Value |
|-------|--------|
| Model (canonical) | `HEM-7143T1` |
| BLE name (example) | `BLESmart_00000481E1997D271C0A` |
| Address (example PC) | `E1:99:7D:27:1C:0A` |
| Stack | modern FE4A |
| Pairing | OS bonding only |
| Unlock | none |

---

## 4. BLE session rules (all brands)

### 4.1 Connect algorithm

```text
INPUT: address (MAC on Windows/Linux, UUID on some macOS stacks), timeout
1. Optionally active-scan until advertisement for address is seen
2. Connect GATT
3. Wait until services are resolved (poll 0.25s × ~20, or await services discovery)
4. VERIFY brand parent service UUID is present
   IF missing → wrong device OR OS GATT cache bug → fail with clear error
5. Return connected client
```

### 4.2 Disconnect

Always disconnect in a `finally` block. Ignore benign disconnect errors.

### 4.3 OS-level pairing / bonding

For profiles with `pairing_mode = os_bonding`:

```text
1. Connect (device in P mode)
2. Call platform Pair/Bond API (Windows: device pair; bleak: client.pair())
3. User accepts OS dialog if shown
4. Optional smoke test: brand start_session + end_session
5. Persist {address, brand, model_id} to local config
```

### 4.4 Platform notes

| Platform | Notes |
|----------|--------|
| Windows 10/11 | Prefer BLE 4.2+ radio; avoid ancient CSR harmony stacks |
| Linux BlueZ | Bonding dialogs may need `bluetoothctl`; multi-adapter needs explicit adapter pin |
| macOS | Device address may be UUID; user permission for Bluetooth |
| Android | Bond + GATT; background limits; use companion device APIs if needed |

---

## 5. Omron transport protocol (normative)

This section fully specifies the **Omron proprietary memory protocol** used by classic and modern stacks in this project.

### 5.1 Channel topology

#### Classic stack

| Role | UUIDs (order = channel index 0..3) |
|------|-------------------------------------|
| Parent service | `ecbe3980-c9a2-11e1-b1bd-0002a5d5c51b` |
| RX (notify) ch0–3 | `49123040-aee8-11e1-a74d-0002a5d5c51b`, `4d0bf320-aee8-11e1-a0d9-0002a5d5c51b`, `5128ce60-aee8-11e1-b84b-0002a5d5c51b`, `560f1420-aee8-11e1-8184-0002a5d5c51b` |
| TX (write) ch0–3 | `db5b55e0-aee7-11e1-965e-0002a5d5c51b`, `e0b8a060-aee7-11e1-92f4-0002a5d5c51b`, `0ae12b00-aee8-11e1-a192-0002a5d5c51b`, `10e1ba60-aee8-11e1-89e5-0002a5d5c51b` |
| Unlock | `b305b680-aee7-11e1-a730-0002a5d5c51b` |

Packets longer than 16 bytes are split across TX channels in 16-byte chunks.  
Responses arrive on RX channels and must be **reassembled**.

#### Modern FE4A stack

| Role | UUID |
|------|------|
| Parent service | `0000fe4a-0000-1000-8000-00805f9b34fb` |
| RX (notify) | `49123040-aee8-11e1-a74d-0002a5d5c51b` (single) |
| TX (write) | `db5b55e0-aee7-11e1-965e-0002a5d5c51b` (single) |
| Unlock char | Often unused (`requiresUnlock = false`); classic unlock UUID may still exist but is not used for OS-bond-only models |

Single-channel: entire command/response fits one write/notify (or one logical buffer).

### 5.2 Checksum (XOR CRC)

For a complete logical packet `P[0..n-1]`:

```text
xor = 0
for each byte b in P:
    xor ^= b
Valid if and only if xor == 0
```

When building a TX command, append padding and final CRC byte so the full buffer XORs to 0.

### 5.3 Logical packet layout (after reassembly)

| Offset | Size | Field |
|--------|------|--------|
| 0 | 1 | `packet_length` (total size including header and CRC) |
| 1–2 | 2 | `packet_type` |
| 3–4 | 2 | `eeprom_address` (big-endian) |
| 5 | 1 | `data_length` (payload size) |
| 6 … | N | payload |
| end | | CRC / padding so XOR of whole packet is 0 |

### 5.4 Command types (host → device)

| Type bytes | Name | Typical total size | Purpose |
|------------|------|--------------------|---------|
| `00 00` | Start transmission | 0x08 | Begin readout session |
| `01 00` | Read EEPROM | 0x08 | Read block from address |
| `01 C0` | Write EEPROM | 0x08 + data | Write block (settings/time) |
| `0F 00` | End transmission | 0x08 | Close session |

### 5.5 Response types (device → host)

| Type bytes | Name | Notes |
|------------|------|--------|
| `80 00` | Start ACK | Expected after start |
| `81 00` | Read response | Payload = EEPROM bytes |
| `81 C0` | Write ACK | |
| `8F 00` | End response | First data byte = status; **0 = OK** |

### 5.6 Fixed session framing commands

#### Start transmission (TX)

```text
Hex: 08 00 00 00 00 10 00 18
```

Expect RX type `80 00`.

#### End transmission (TX)

```text
Hex: 08 0F 00 00 00 00 00 07
```

Expect RX type `8F 00` and status byte `0x00`.

### 5.7 Read EEPROM block

**Exact build (normative):**

```text
cmd = bytes([0x08, 0x01, 0x00]) + address.to_bytes(2, 'big') + bytes([blocksize])
R = XOR of every byte currently in cmd
cmd = cmd + bytes([0x00, R])
# Proof: XOR(cmd || 0x00 || R) = R ^ 0x00 ^ R = 0
```

Example: read 0x10 bytes from address 0x02E8:

```text
header = 08 01 00 02 E8 10
R = 0x08^0x01^0x00^0x02^0xE8^0x10 = 0xF3
full  = 08 01 00 02 E8 10 00 F3
```

Verify:

- Response type == `81 00`  
- Response address == requested address  
- Payload length field matches  
- Return payload bytes  

### 5.8 Write EEPROM block

```text
len_byte = len(data) + 8
cmd = [len_byte, 0x01, 0xC0] + addr_be16 + [len(data)] + data
R = XOR(cmd)
cmd += [0x00, R]
```

Expect type `81 C0` and matching address.

**Safety:** EEPROM writes can corrupt calibration if wrong addresses are used. Default product behavior should **not** write settings unless the user explicitly enables time-sync / unread-reset features after validation.

### 5.9 Continuous read

```text
function read_continuous(start, total_bytes, block_size):
  out = empty
  addr = start
  remaining = total_bytes
  while remaining > 0:
    n = min(remaining, block_size)
    out += read_block(addr, n)
    addr += n
    remaining -= n
  return out
```

`block_size` comes from profile `transmission_block_size` (commonly `0x10` or `0x38`).

### 5.10 TX segmentation

```text
width = 16
if single TX UUID:
  width = max(16, len(command))   # usually send whole command on ch0
for each chunk of command with size width:
  write_gatt(tx_uuid[channel_index], chunk)
  # classic multi-channel: response=default; modern single often write without response
```

### 5.11 RX reassembly (classic multi-channel)

```text
On notify on channel i: store buffer[i] = data

When buffer[0] is present:
  packet_size = buffer[0][0]
  needed_channels = ceil(packet_size / 16)
  if any buffer[0..needed-1] missing: wait
  combined = concat(buffer[0], buffer[1], ...)[0:packet_size]
  clear buffers
  verify XOR(combined) == 0
  parse fields as Section 5.3
```

### 5.12 RX (modern single-channel)

```text
combined = notification bytes
verify XOR(combined) == 0   # if packet follows same framing
parse as Section 5.3
```

### 5.13 Retry policy

```text
On missing response within 1.0s:
  retry same TX up to 5 times
  then fail with TimeoutError
```

### 5.14 Full readout session sequence

```text
enable notifications on all RX UUIDs
unlock_if_required()                 # Section 6
start_transmission()                 # 5.6
for each user index u:
  start = profile.user_start_addresses[u]
  nbytes = profile.per_user_records_count[u] * profile.record_byte_size
  blob = read_continuous(start, nbytes, profile.transmission_block_size)
  records[u] = parse_blob(blob, profile)
end_transmission()
disable notifications
return records
```

### 5.15 Parse blob into records

```text
function parse_blob(blob, profile):
  size = profile.record_byte_size
  out = []
  for offset in 0, size, 2*size, ... while offset+size <= len(blob):
    chunk = blob[offset:offset+size]
    if chunk == (0xFF repeated size times): skip
    try:
      out.append( profile.parser(chunk, profile.endianness) )
    except EmptyOrInvalid:
      skip   # empty / corrupt slot
  return out
```

---

## 6. Omron trust: pairing modes and unlock modes

### 6.1 Concepts (do not conflate)

| Concept | When | What |
|---------|------|------|
| **pairing_mode** | Once per host (setup) | How the cuff learns to trust this PC/phone |
| **unlock_mode** | Every memory session | Extra step after connect before EEPROM access |

### 6.2 pairing_mode values

#### `os_bonding` (modern FE4A)

1. Cuff in **P** mode  
2. Connect  
3. OS BLE bond/pair  
4. Done  

No 16-byte app key is programmed.

#### `unlock_key` (classic)

1. Cuff in **P** mode  
2. Connect  
3. Program 16-byte key into device via unlock characteristic (Section 6.3)  
4. Optional start/end smoke test  
5. Future sessions only need **unlock with same key**, not re-program  

Default reference key (widely used by community tools; 16 bytes):

```text
hex: deadbeaf12341234deadbeaf12341234
```

Products may allow user-configured keys; store per device in config if customized.

#### `none`

No pairing step (rare).

### 6.3 Classic unlock characteristic protocol

Characteristic UUID: `b305b680-aee7-11e1-a730-0002a5d5c51b`  
Use **write with response** + **notify** on the same characteristic for replies.

#### 6.3.1 Program new key (pairing)

```text
1. Optionally start_notify on RX ch0 (can trigger SMP security request)
2. start_notify(unlock_uuid)
3. Retry up to 10 times:
     write unlock_uuid: 0x02 || 16 zero bytes
     wait notify
     if response[0:2] == 0x82 0x00: break  # entered programming mode
     sleep 1s
   else: fail "Could not enter key programming mode — is cuff in P?"
4. write unlock_uuid: 0x00 || 16-byte-key
   wait notify
   require response[0:2] == 0x80 0x00
5. stop notifies
```

#### 6.3.2 Unlock with existing key (each read)

```text
1. start_notify(unlock_uuid)
2. write unlock_uuid: 0x01 || 16-byte-key
3. wait notify
4. require response[0:2] == 0x81 0x00  else fail "key mismatch"
5. stop_notify
```

### 6.4 unlock_mode values

| Value | Behavior |
|-------|----------|
| `none` | Skip app unlock (e.g. HEM-7143T1) |
| `classic_key` | Section 6.3.2 before start_transmission |
| `token_key` | Stateless handshake documented as `0x11` + 4 nonce bytes → device `0x91 0x00` ack (HCI-confirmed on some modern firmwares). **Not fully specified for production in this doc’s reference stack; implement before relying on those models outside pairing grace window.** |
| `secure_session` | ECDH/AES style session — **out of scope / unsupported** |

### 6.5 Pair workflow (canonical)

```text
LOAD profile(model)
IF pairing_mode == none: return success
PUT cuff in P mode; CONNECT
IF pairing_mode == os_bonding:
  OS_pair()
  TRY start_transmission; end_transmission EXCEPT warn
IF pairing_mode == unlock_key:
  program_new_key(key)
  start_transmission; end_transmission
SAVE config {address, brand=omron, model_id}
DISCONNECT
```

### 6.6 Read workflow (canonical)

```text
LOAD profile(model)
PUT cuff in transfer mode; CONNECT
VERIFY parent service
CREATE transport(profile)
IF unlock_mode == classic_key: unlock_with_key(key)
# IF token_key: perform token handshake when implemented
start_transmission
FOR each user: read + parse
end_transmission
DISCONNECT
EXPORT records
```

---

## 7. DeviceProfile schema (normative)

Every supported model is one profile object.

### 7.1 Fields

| Field | Type | Required | Meaning |
|-------|------|----------|---------|
| `model_id` | string | yes | Canonical ID, e.g. `HEM-7143T1` |
| `display_name` | string | yes | UI label |
| `brand` | string | yes | Always `omron` in this catalog; for multi-brand use `andon`, etc. |
| `stack` | enum | yes | `classic` \| `modern` |
| `pairing_mode` | enum | yes | `os_bonding` \| `unlock_key` \| `none` |
| `unlock_mode` | enum | yes | `none` \| `classic_key` \| `token_key` \| `secure_session` |
| `endianness` | enum | yes | `little` \| `big` — used by bitfield parsers |
| `parent_service_uuid` | UUID string | yes | Must exist after connect |
| `rx_channel_uuids` | UUID[] | yes | Notify characteristics in order |
| `tx_channel_uuids` | UUID[] | yes | Write characteristics in order |
| `unlock_uuid` | UUID string | if classic key | Unlock characteristic |
| `user_start_addresses` | uint16[] | yes | EEPROM start per user bank |
| `per_user_records_count` | int[] | yes | Slot count per user; **same length** as addresses |
| `record_byte_size` | int | yes | Bytes per measurement slot (`0x0E` or `0x10` typical) |
| `transmission_block_size` | int | yes | Max BLE read chunk |
| `settings_read_address` | uint16\|null | no | Settings region base for reads |
| `settings_write_address` | uint16\|null | no | Settings region base for writes |
| `settings_unread_records_bytes` | [start,end)\|\null | no | Byte offsets within cached settings for unread counters |
| `settings_time_sync_bytes` | [start,end)\|null | no | Byte offsets for clock fields |
| `parser_id` | string | yes | Key into parser registry |
| `aliases` | string[] | no | Other names resolving to this profile |
| `source` | string | no | Provenance notes |
| `notes` | string | no | Operator / implementer notes |

### 7.2 Validation rules

1. `len(user_start_addresses) == len(per_user_records_count) >= 1`  
2. `parser_id` must exist in parser registry  
3. Alias uniqueness: first registration wins; log conflicts  
4. If `pairing_mode == unlock_key` then `unlock_mode` should be `classic_key`  
5. If `stack == modern` then parent service is typically FE4A and RX/TX length is 1  

### 7.3 Defaults

**Classic defaults:**

```text
parent = ecbe3980-c9a2-11e1-b1bd-0002a5d5c51b
rx = 4 classic RX UUIDs
tx = 4 classic TX UUIDs
unlock = b305b680-aee7-11e1-a730-0002a5d5c51b
pairing_mode = unlock_key
unlock_mode = classic_key
```

**Modern defaults:**

```text
parent = 0000fe4a-0000-1000-8000-00805f9b34fb
rx = [49123040-aee8-11e1-a74d-0002a5d5c51b]
tx = [db5b55e0-aee7-11e1-965e-0002a5d5c51b]
pairing_mode = os_bonding
unlock_mode = none  (or token_key when documented)
```

---

## 8. Record parsers (normative algorithms)

### 8.1 Bit extraction helper

Used by bit-packed parsers. Bits are numbered from the **MSB of the entire buffer** as bit 0 in the sense of:

```text
function bits_to_int(data: bytes, endianness: "big"|"little", first_bit, last_bit) -> int:
  big_int = int.from_bytes(data, byteorder=endianness)
  num_bits = (last_bit - first_bit) + 1
  shifted = big_int >> (len(data)*8 - (last_bit + 1))
  return shifted & ((1 << num_bits) - 1)
```

### 8.2 Parser registry IDs

| parser_id | Function name | Typical use |
|-----------|---------------|-------------|
| `classic_vital_14` | byte-aligned 14-byte | Modern FE4A 714x / many modern records |
| `classic_vital_14_bitpacked` | bit-packed 14-byte, year bits 16–23 | 7322, 7600, 7320, 6320… |
| `classic_vital_14_6232_family` | bit-packed 14-byte, year bits 18–23 | 6232, 7530, 6161, 7136, 6231 |
| `vital_16_715x_bitpacked` | 16-byte LE bitfields | 7150, 7155 classic, 7342, 7361 |
| `classic_vital_16_6401_family` | 16-byte wrist layout | 6401 family |

### 8.3 `classic_vital_14`

**Input:** at least 8 bytes of a 14-byte slot (use full slot if available).  
**Endianness parameter:** unused for slicing (layout is byte-oriented); pass profile endianness for API consistency.

```text
raw_sys = data[0]
IF raw_sys > 0xE1: EMPTY
sys = raw_sys + 25
dia = data[1]
bpm = data[2]
year = 2000 + (data[3] & 0x3F)
flags1 = data[4] | (data[5] << 8)     # little-endian assembly of two bytes
flags2 = data[6] | (data[7] << 8)
hour  = flags1 & 0x1F
day   = (flags1 >> 5) & 0x1F
month = (flags1 >> 10) & 0x0F
ihb   = (flags1 >> 14) & 0x01
mov   = (flags1 >> 15) & 0x01
second = min(flags2 & 0x3F, 59)
minute = min((flags2 >> 6) & 0x3F, 59)
cuff = (flags2 >> 12) & 0x01
battery = (flags2 >> 13) & 0x01
pos = (flags2 >> 14) & 0x03

IF dia==0 and bpm==0 and (data[3]&0x3F)==0 and flags1==0 and flags2==0: EMPTY
IF month not in 1..12 or day not in 1..31 or hour > 23: EMPTY
datetime = DateTime(year, month, day, hour, minute, second)
RETURN {sys,dia,bpm,mov,ihb,cuff,battery,pos,datetime}
```

### 8.4 `classic_vital_14_bitpacked`

**Default endianness:** `big`.

```text
IF all 0xFF: EMPTY
dia = bits(0,7)
sys = bits(8,15) + 25
year = bits(16,23) + 2000
bpm = bits(24,31)
mov = bits(32,32)
ihb = bits(33,33)
month = bits(34,37)
day = bits(38,42)
hour = bits(43,47)
pos = bits(48,49)
battery = bits(50,50)
cuff = bits(51,51)
minute = bits(52,57)
second = min(bits(58,63), 59)
validate date → datetime
RETURN record
```

### 8.5 `classic_vital_14_6232_family`

Same as 8.4 **except**:

```text
year = bits(18,23) + 2000    # NOT bits(16,23)
```

### 8.6 `vital_16_715x_bitpacked`

**Requires 16 bytes. Default endianness: `little`.**

```text
IF data[0:16] all 0xFF: EMPTY
minute = bits(68,73)
second = min(bits(74,79), 59)
mov = bits(80,80)
ihb = bits(81,81)
month = bits(82,85)
day = bits(86,90)
hour = bits(91,95)
year = bits(98,103) + 2000
bpm = bits(104,111)
dia = bits(112,119)
sys = bits(120,127) + 25
validate date → datetime
RETURN record
```

### 8.7 `classic_vital_16_6401_family`

```text
year_off, month, day, hour, minute, second = data[0..5] as ints
second = min(second, 59)
raw_sys, dia, bpm = data[6], data[7], data[8]
IF all those zero: EMPTY
IF raw_sys > 0xE1: EMPTY
flags = data[11]
ihb = flags & 0x03
mov = (flags >> 2) & 0x03
sys = raw_sys + 25
datetime = DateTime(2000+year_off, month, day, hour, minute, second)
RETURN record
```

### 8.8 Systolic encoding note

Across Omron layouts, stored raw systolic often equals **display_sys − 25**. Always apply `+ 25` where specified.

---

## 9. Complete Omron device catalog (canonical profiles)

Column header:

`model | stack | pairing | unlock | endian | starts | counts | rec | block | set_r | set_w | unread | time | parser`

```text
| model_id | stack | pairing | unlock | end | starts | counts | rec | blk | set_r | set_w | unread | time | parser |
```

| model_id | stack | pairing | unlock | end | starts | counts | rec | blk | set_r | set_w | unread | time | parser |
|----------|-------|---------|--------|-----|--------|--------|-----|-----|-------|-------|--------|------|--------|
| HEM-6161T | classic | unlock_key | classic_key | big | 0x02E8 | 30 | 0x0E | 0x10 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14_6232_family |
| HEM-6231T | classic | unlock_key | classic_key | big | 0x02E8 | 100 | 0x0E | 0x10 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14_6232_family |
| HEM-6232T | classic | unlock_key | classic_key | big | 0x02E8,0x0860 | 100,100 | 0x0E | 0x38 | 0x0260 | 0x02A4 | [0x00,0x08] | [0x2C,0x3C] | classic_vital_14_6232_family |
| HEM-6320T | classic | unlock_key | classic_key | big | 0x0370 | 100 | 0x0E | 0x38 | 0x0F74 | 0x0F9A | [0x00,0x08] | [0x14,0x1E] | classic_vital_14_bitpacked |
| HEM-6321T | classic | unlock_key | classic_key | big | 0x0370,0x08E8 | 100,100 | 0x0E | 0x38 | 0x0F74 | 0x0F9A | [0x00,0x08] | [0x14,0x1E] | classic_vital_14_bitpacked |
| HEM-6401T | classic | unlock_key | classic_key | little | 0x1350 | 100 | 0x10 | 0x10 | 0x0100 | 0x0160 | null | [0x10,0x20] | classic_vital_16_6401_family |
| HEM-7136T | classic | unlock_key | classic_key | big | 0x02E8 | 60 | 0x0E | 0x10 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14_6232_family |
| HEM-7142T2 | modern | os_bonding | token_key | little | 0x02E8 | 14 | 0x0E | 0x38 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14 |
| HEM-7143T1 | modern | os_bonding | none | little | 0x02E8 | 30 | 0x0E | 0x38 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14 |
| HEM-7150T | classic | unlock_key | classic_key | little | 0x0098 | 60 | 0x10 | 0x10 | 0x0010 | 0x0054 | [0x00,0x10] | [0x2C,0x3C] | vital_16_715x_bitpacked |
| HEM-7151T | classic | unlock_key | classic_key | little | 0x0098 | 80 | 0x10 | 0x10 | 0x0010 | 0x0054 | [0x00,0x10] | [0x2C,0x3C] | vital_16_715x_bitpacked |
| HEM-7155T | classic | unlock_key | classic_key | little | 0x0098,0x0458 | 60,60 | 0x10 | 0x10 | 0x0010 | 0x0054 | [0x00,0x10] | [0x2C,0x3C] | vital_16_715x_bitpacked |
| HEM-7155T-K4 | modern | os_bonding | none | little | 0x02E8,0x06A8 | 60,60 | 0x10 | 0x38 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14 |
| HEM-7155T-MW | modern | os_bonding | none | little | 0x0098,0x0458 | 60,60 | 0x10 | 0x38 | 0x0010 | 0x0054 | null | [0x2C,0x3C] | classic_vital_14 |
| HEM-7155T-MW3 | modern | os_bonding | token_key | little | 0x02E8,0x06A8 | 60,60 | 0x10 | 0x38 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14 |
| HEM-7320T | classic | unlock_key | classic_key | big | 0x02AC,0x05F4 | 60,60 | 0x0E | 0x38 | 0x0260 | 0x0286 | [0x00,0x08] | [0x14,0x1E] | classic_vital_14_bitpacked |
| HEM-7322T | classic | unlock_key | classic_key | big | 0x02AC,0x0824 | 100,100 | 0x0E | 0x38 | 0x0260 | 0x0286 | [0x00,0x08] | [0x14,0x1E] | classic_vital_14_bitpacked |
| HEM-7342T | classic | unlock_key | classic_key | little | 0x0098,0x06D8 | 100,100 | 0x10 | 0x10 | 0x0010 | 0x0054 | [0x00,0x10] | [0x2C,0x3C] | vital_16_715x_bitpacked |
| HEM-7361T | classic | unlock_key | classic_key | little | 0x0098,0x06D8 | 100,100 | 0x10 | 0x10 | 0x0010 | 0x0054 | [0x00,0x10] | [0x2C,0x3C] | vital_16_715x_bitpacked |
| HEM-7377T1 | modern | os_bonding | none | little | 0x01CC,0x080C | 100,100 | 0x10 | 0x38 | 0x0040 | 0x0088 | null | null | classic_vital_14 |
| HEM-7380T1 | modern | os_bonding | token_key | little | 0x01C4,0x0804 | 100,100 | 0x10 | 0x38 | 0x0010 | 0x0054 | null | [0x2C,0x3C] | classic_vital_14 |
| HEM-7530T | classic | unlock_key | classic_key | big | 0x02E8 | 90 | 0x0E | 0x10 | 0x0260 | 0x02A4 | null | [0x2C,0x3C] | classic_vital_14_6232_family |
| HEM-7600T | classic | unlock_key | classic_key | big | 0x02AC | 100 | 0x0E | 0x38 | 0x0260 | 0x0286 | [0x00,0x08] | [0x14,0x1E] | classic_vital_14_bitpacked |

UUIDs for each row: apply **classic defaults** or **modern defaults** from Section 7.3 based on `stack`.

### 9.1 Important family notes

| Family | Notes |
|--------|------|
| **HEM-7143T1** | Lab primary; equivalent SKUs include many `HEM-7146T*` / `HEM-7143T1*` regional IDs (see aliases JSON) |
| **HEM-7155T vs MW/MW3/K4** | Same marketing line, **different BLE stack and memory**; wrong profile ⇒ wrong data |
| **HEM-7377T1 vs 7380T1** | Similar modern dual-user; **addresses differ by 8 bytes**; do not merge blindly |
| **HEM-7530T** | Omron Complete BP memory only; EKG not covered |
| **token_key models** | Pairing may work; reliable background read may need token handshake implementation |

### 9.2 Alias resolution rule

```text
normalize(name) = trim + casefold
lookup alias map → canonical model_id → profile
```

Full alias list is in `_aliases.md` / `_catalog_export.json` (200+ entries). Examples:

| Alias | Canonical |
|-------|-----------|
| HEM-7146T | HEM-7143T1 |
| BP7250 | HEM-7150T |
| X7 Smart AFib | HEM-7380T1 |
| M700 Intelli IT | HEM-7322T |
| Omron Evolv | HEM-7600T |
| BP5360 | HEM-7377T1 |

---

## 10. Export format (CSV)

### 10.1 File naming

```text
user1.csv, user2.csv, ...   # one file per user bank index
```

### 10.2 Columns (header required)

```text
datetime,dia,sys,bpm,mov,ihb
```

### 10.3 datetime format

```text
YYYY-MM-DD HH:MM:SS
```

Naive local time as reported by device clock (not necessarily UTC).

### 10.4 Merge policy (recommended)

When rewriting `userN.csv`:

1. Backup previous file with timestamp  
2. Union rows by `datetime` key  
3. Sort ascending by datetime  

### 10.5 JSON export (optional UBPM-like)

```json
{
  "UBPM": {
    "U1": [
      {
        "date": "DD.MM.YYYY",
        "time": "HH:MM:SS",
        "msg": "",
        "sys": 120,
        "dia": 80,
        "bpm": 70,
        "ihb": 0,
        "mov": 0
      }
    ]
  }
}
```

---

## 11. Configuration store (per installation)

Suggested `paired_devices` / `omron_bp_device.json`:

```json
{
  "brand": "omron",
  "model_id": "HEM-7143T1",
  "address": "E1:99:7D:27:1C:0A",
  "unlock_key_hex": null,
  "last_paired_utc": "2026-07-14T00:00:00Z",
  "last_read_utc": null
}
```

For classic devices store `unlock_key_hex` if not using the default key.

---

## 12. End-to-end scenarios

### 12.1 First-time setup: HEM-7143T1 (modern)

```text
1. Ensure phone is unpaired from cuff
2. Cuff batteries OK; hold BT until flashing P
3. Host: scan or use known MAC
4. pair workflow with model HEM-7143T1
5. Accept Windows pairing prompt if any
6. Verify config saved
7. Take a BP measurement on cuff
8. Short-press BT (transfer mode)
9. read workflow → user1.csv contains new row
```

### 12.2 Classic HEM-7322T

```text
1. P mode
2. pair with unlock_key programming (default key or custom)
3. Later: transfer mode
4. read: unlock with key → download both user banks → user1.csv, user2.csv
```

### 12.3 Failure diagnosis tree

```text
Scan never sees device?
  → BT on host? cuff awake? P/transfer window short? distance? phone holding exclusive bond?

Connect fails?
  → retry while advertising; Windows radio driver; remove stale bond

Parent service missing?
  → wrong model profile (classic vs modern UUID) OR OS GATT issue

Unlock fails (classic)?
  → not in P for first key program; wrong key; not classic device

Start transmission fails?
  → not unlocked; not bonded; wrong mode; flaky link (retry)

Parses zero records?
  → empty memory; wrong addresses; wrong parser; all slots 0xFF

Parses nonsense vitals?
  → wrong parser_id or endianness (classic symptom: dia>sys, bpm≈26)
```

---

## 13. Time sync and unread counters (optional, advanced)

### 13.1 Risk

Settings region is near configuration data. Incorrect writes may brick timekeeping or worse. **Do not enable by default** in production until tested per model.

### 13.2 Unread-only download (outline)

When `settings_unread_records_bytes` is non-null:

1. Read settings slice into a cache buffer sized `(settings_write - settings_read)` or at least covering the slice  
2. Decode per-user last-written slot and unread count (layout is endian-sensitive; reference uses bit extraction on 2-byte fields)  
3. Compute ring-buffer read ranges within the user bank  
4. Read only those ranges  
5. Reset unread counters in cache (special value `0x8000` used in classic tools) and write back  

Exact bit layout of counters is model-family specific; if implementing, port carefully from validated sources and test with `--newRecOnly` equivalents on hardware.

### 13.3 Time sync (outline)

When `settings_time_sync_bytes` is non-null:

1. Read settings  
2. Decode current device time from the slice (layouts differ: classic mixed order vs modern offset-8 linear year,month,day,hour,minute,second)  
3. Encode host local time into the same layout + checksum byte  
4. Write only that slice back  

---

## 14. Multi-brand extension guide (how to add non-Omron)

### 14.1 Steps

1. **Identify protocol class**  
   - Standard BT SIG BP Profile?  
   - Vendor EEPROM protocol?  
   - Proprietary encrypted session?

2. **Implement BrandAdapter** with same workflow hooks:  
   `pair()`, `read_all_records(profile) → List[List[Record]]`

3. **Define profile schema** for that brand (may differ from Omron fields; keep a common measurement Record output).

4. **Register brand** in brand registry: `omron`, `beurer`, …

5. **Do not** force other brands through Omron transport.

### 14.2 Common abstraction that remains shared

- BLE scanner / connect helpers  
- App workflows (pair/read menu)  
- Canonical `Record` + CSV export  
- Device config store  
- Logging strategy  

### 14.3 What stays brand-specific

- GATT UUIDs  
- Packet framing  
- Pairing/bonding quirks  
- Memory maps / parsers  

---

## 15. Logging and release hygiene

### 15.1 Levels

| Level | Use |
|-------|-----|
| INFO | Pair/read progress, counts, file paths |
| WARNING | Retries, optional smoke-test failures |
| ERROR | Hard failures |
| DEBUG | Hex TX/RX, every slot parse |

### 15.2 Temporary debug markers

Tag temporary instrumentation with comment `DBG-LOG` and message token `[DBG]` so release builds can strip or silence them. Production default log level = INFO.

### 15.3 Environment flag

`OMRON_BP_DEBUG=1` (or brand-neutral `BP_BRIDGE_DEBUG=1`) forces DEBUG.

---

## 16. Security and privacy

1. BLE bonds and unlock keys grant access to health history on the device — protect host OS accounts.  
2. Do not log full unlock keys at INFO level in production.  
3. Measurement CSV may be PHI — apply product privacy policy.  
4. Prefer OS bonding storage; do not hardcode customer-specific secrets in source.  

---

## 17. Implementation checklist (build order)

Use this as a project plan for a full app:

- [ ] **P0** BLE scan + connect + list GATT services  
- [ ] **P0** Omron modern session: start / read block / end  
- [ ] **P0** Profile for HEM-7143T1 + `classic_vital_14` + CSV  
- [ ] **P0** OS bond pair workflow  
- [ ] **P1** Classic multi-channel TX/RX reassembly  
- [ ] **P1** Classic key program + unlock  
- [ ] **P1** Load full Omron catalog from JSON/YAML  
- [ ] **P1** All five parsers + unit tests with synthetic hex  
- [ ] **P2** Alias resolution + model list UI  
- [ ] **P2** Config persistence  
- [ ] **P2** Robust retries / timeouts / user-facing errors  
- [ ] **P3** token_key handshake  
- [ ] **P3** time sync / unread (opt-in)  
- [ ] **P3** Second brand adapter  
- [ ] **P3** API/service mode for hub  

---

## 18. Test plan (minimum)

### 18.1 Offline unit tests

- Each parser: fixture hex → expected sys/dia/bpm/datetime  
- Empty slot fixtures → skip  
- XOR CRC builder: known vectors  
- Alias lookup table  

### 18.2 Hardware tests (per model tier)

| Tier | Models | Tests |
|------|--------|-------|
| A (lab) | HEM-7143T1 | pair, read, CSV match cuff display |
| B | one classic dual-user (7322 or 7361) | key pair + dual CSV |
| C | one 16-byte 715x | parser correctness |
| D | token_key model | only after token implemented |

### 18.3 Acceptance criteria for a “successful read”

1. Connect + parent service OK  
2. start/end without error  
3. At least the measurements known to be on cuff appear with **sys/dia/bpm within 0 of cuff screen** and timestamp within device clock skew  
4. CSV written and re-readable  

---

## 19. Reference algorithms in pseudocode (complete read)

```text
function READ_OMRON(address, model_id, unlock_key = DEFAULT_KEY):
  profile = CATALOG.resolve(model_id)
  client = BLE.connect(address, timeout=30)
  assert profile.parent_service_uuid in client.services
  t = OmronTransport(client, profile)

  if profile.unlock_mode == classic_key:
    t.unlock_with_key(unlock_key)

  t.start_transmission()
  all_users = []
  try:
    for u in range(len(profile.user_start_addresses)):
      start = profile.user_start_addresses[u]
      nbytes = profile.per_user_records_count[u] * profile.record_byte_size
      blob = t.read_continuous(start, nbytes, profile.transmission_block_size)
      all_users.append(parse_blob(blob, profile))
  finally:
    t.end_transmission()
  client.disconnect()
  return all_users
```

```text
function PAIR_OMRON(address, model_id, unlock_key = DEFAULT_KEY):
  profile = CATALOG.resolve(model_id)
  # Operator: cuff must be in P mode
  client = BLE.connect(address, timeout=30)
  assert profile.parent_service_uuid in client.services

  if profile.pairing_mode == os_bonding:
    client.os_pair()
  elif profile.pairing_mode == unlock_key:
    t = OmronTransport(client, profile)
    t.program_unlock_key(unlock_key)
    t.start_transmission(); t.end_transmission()

  save_config(address, model_id)
  client.disconnect()
```

---

## 20. Worked example: HEM-7143T1 numbers

| Item | Value |
|------|--------|
| stack | modern |
| parent | `0000fe4a-0000-1000-8000-00805f9b34fb` |
| RX | `49123040-aee8-11e1-a74d-0002a5d5c51b` |
| TX | `db5b55e0-aee7-11e1-965e-0002a5d5c51b` |
| pairing | os_bonding |
| unlock | none |
| user0 start | `0x02E8` |
| slots | 30 |
| record size | 14 (`0x0E`) |
| total bytes to read | 30 × 14 = 420 = `0x1A4` |
| block size | `0x38` (56) |
| parser | classic_vital_14 |

**Example decode intuition:** if byte0 = `0x5F` (95), sys = 95+25 = **120 mmHg**.

---

## 21. Explicit non-supported / unknown areas

| Topic | Status |
|-------|--------|
| Omron secure encrypted sessions | Unsupported |
| Full TOKEN_KEY (`0x11`/`0x91`) production implementation | Documented as required on some models; implement before claiming support |
| Automatic model detection from advertisement alone | Best-effort only (name/mfg id); user or config should supply model |
| EKG from Complete | Unsupported |
| Writing arbitrary EEPROM | Forbidden except validated settings slices |
| HEM-7196T encrypted traffic (historical note) | Unsupported |

---

## 22. Document maintenance rules

When adding a model:

1. Add profile to catalog JSON with all required fields  
2. Reuse an existing parser_id if possible  
3. If new packing appears, add parser + unit fixtures  
4. List all known regional aliases  
5. Record provenance (`source`) and hardware validation status  
6. Update this SPEC version and changelog  

### Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-07-14 | Initial complete specification from lab omron_bp work: architecture, Omron protocol, 5 parsers, 23 canonical profiles, multi-brand extension plan |

---

## 23. One-page summary for an AI implementer

```text
GOAL: Build multi-brand BP BLE download app.

OMRON PATH:
  Connect BLE → ensure parent service →
  (classic: unlock key | modern: OS bond already done) →
  TX 08 00 00 00 00 10 00 18 (start) →
  loop read EEPROM blocks from profile addresses →
  TX end 08 0F 00 00 00 00 00 07 →
  split by record_byte_size → parser_id → CSV.

DATA MODEL:
  BrandAdapter + DeviceProfile + Parser + Record.

DO NOT:
  Hardcode one model’s addresses in transport.
  Mix patient DB with device catalog.
  Enable EEPROM writes by default.

START HARDWARE:
  HEM-7143T1 modern FE4A, OS bond, 0x02E8, 30×14, classic_vital_14.

CATALOG:
  23 Omron families in Section 9 + _catalog_export.json.

NEXT BRAND:
  New adapter, same workflows and Record schema.
```

---

**End of specification.**

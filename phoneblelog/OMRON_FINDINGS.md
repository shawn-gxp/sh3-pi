# Omron Smart BP meter findings (S23 Ultra HCI dump)

**Source:** `btsnoop_hci_202607151125.cfa`  
**Capture window:** 2026-07-15 11:22:40 – 11:25:59 UTC  
**Target MAC:** `E1:99:7D:27:1C:0A`  
**Detailed extract:** `omron_E1997D271C0A_extract.log`  
**GATT session log:** `omron_gatt_session.log`

---

## Device identity (confirmed)

| Field | Value |
|--------|--------|
| BLE address | `E1:99:7D:27:1C:0A` (Random) |
| Advertised name | `BLESmart_00000481E1997D271C0A` |
| Model (GATT Device Name) | **`HEM-7143T1`** |
| Firmware / software string | `SI.W01.12.01` |
| Device ID string | `0000000000100101` |
| Device info string | `00000B1F02000E708A17` (contains Omron company id `0x020E`) |
| BLE company ID | **`0x020E` = Omron** |
| Custom service UUID | **`0xFE4A`** (Omron BLE service) |
| RSSI while advertising | about −44 to −70 dBm (avg ~−51 dBm) |

The phone already had this meter paired/bonded: at stack startup it loaded the MAC into the **resolving list** and set **device privacy mode**.

---

## What Omron connect was doing

### 1. Advertising (meter → phone)

The cuff advertises continuously with:

- Flags + TxPower
- Incomplete 16-bit UUID list: **`FE4A`**
- Manufacturer data: company **Omron (`0x020E`)** + payload like `01 00 6B/6C 00 1E`
- Scan response complete local name:  
  **`BLESmart_00000481E1997D271C0A`**

~1050 advertising/scan-response reports for this MAC in the dump (~1078 packets total containing the MAC).

### 2. Two successful BLE connections

| # | Time (UTC) | HCI handle | Result |
|---|------------|------------|--------|
| 1 | 11:24:18.554 | **0x0003** | Success (`status=0x00`) peer=`E1:99:7D:27:1C:0A` |
| 2 | 11:24:35.673 | **0x0004** | Success (`status=0x00`) peer=`E1:99:7D:27:1C:0A` |

Connection method used by the app/stack:

1. Add Omron MAC to **Filter Accept List** (both Random and Public type variants)
2. **LE Extended Create Connection** with whitelist / filter policy (peer often `00:00:00:00:00:00`)
3. Enhanced Connection Complete for the Omron address
4. Meter sends **SMP Security Request** (`0x0B 0x01`) — encrypted/bonded path

There were also failed create-connection attempts (`status=0x02`) interleaved with other nearby devices.

### 3. GATT / Omron custom protocol (both sessions look the same)

After connect, OMRON connect did **not** use the standard Bluetooth Blood Pressure Profile (`0x1810`) for the main transfer. It used Omron’s proprietary channel on custom handles:

| Handle | Role in this capture |
|--------|----------------------|
| `0x0003` | Read → model name `HEM-7143T1` |
| `0x0012` | Read → device info `00000B1F02000E708A17` |
| `0x0014` | Read → `SI.W01.12.01` |
| `0x0016` | Read → `0000000000100101` |
| `0x001B` | Phone **Write Command** + meter **Notification** (handshake / challenge-style exchange) |
| `0x001C` | CCCD write `01 00` (enable notifications) |
| `0x001E` | Phone **Write Command** (commands into meter) |
| `0x0020` | Meter **Notification** (command responses / data blocks) |
| `0x0021` | CCCD write `01 00` (enable notifications) |

**Session outline (session 1 @ handle 0x0003):**

1. SMP security request from meter  
2. Phone probes for service changed UUID `0x2B3A` → Attribute Not Found  
3. LE credit-based connection parameter / L2CAP signaling  
4. Read version / serial / model strings  
5. MTU: phone asks **517**, meter answers **67**  
6. Enable CCCDs on `0x0021` and `0x001C`  
7. Proprietary exchange:
   - Phone → `0x001B`: `11 cd 02 37 b7 ...` (session 1) / `11 b7 a7 fd 9f ...` (session 2)  
   - Meter → `0x001B` notify: `91 00 ...` (echo/ack style)  
   - Phone → `0x001E` series of short commands (`08 00 ...`, `08 01 ...`, `08 0f ...`)  
   - Meter → `0x0020` multi-fragment notifications with data blocks  

Session 2 on handle `0x0004` is essentially a **repeat** of the same sequence ~17 seconds later (app reconnect / second sync attempt).

### 4. Possible measurement-related payload

Inside a meter notification on `0x0020` (session 1):

```text
20 81 00 02 8c 18 c0 50 00 00 00 00 00 00 1a 07 0f 0b 18 16 79 86 ff ff ff ff ff ff ff ff 00 4f
```

Bytes `1a 07 0f 0b 18 16` decode cleanly as a **device timestamp**:

**2026-07-15 11:24:22**

Session 2 has a similar stamp: **2026-07-15 11:24:39**.

Surrounding bytes are **not** plain ASCII BP values; they look like Omron’s framed binary records (likely include systolic/diastolic/pulse under a custom encoding, often with bit flags and `0xFF` fillers). Full decode of Omron’s `FE4A` payload format needs a protocol map (or more captures with known BP readings for correlation).

---

## What this dump is *not*

- Not only advertising — **full connect + GATT sessions exist**.
- Not a clean standard BLP (`Blood Pressure Measurement` UUID `0x2A35`) export in the clear.
- No obvious plaintext like `"120/80"` in ACL payloads.
- Handle `0x0001` traffic in the same file is a **different** connected device (not the Omron MAC).

---

## Timeline (high level)

| UTC time | Event |
|----------|--------|
| 11:22:40 | BT stack reset; Omron MAC loaded into resolving list + privacy mode |
| 11:22:41 | Vendor HCI references Omron MAC |
| 11:23:31+ | Heavy advertising from Omron (`BLESmart_...`, UUID `FE4A`, company `0x020E`) |
| 11:24:18 | **Connect #1** handle `0x0003` → identity reads → proprietary sync on `0x001B`/`0x001E`/`0x0020` |
| 11:24:35 | **Connect #2** handle `0x0004` → same sequence again |
| 11:25:xx | Continued advertising from meter after sessions |

---

## Files generated for you

| File | Contents |
|------|----------|
| `omron_E1997D271C0A_extract.log` | All packets/ads/host actions for this MAC |
| `omron_gatt_session.log` | Raw ATT lines on connection handles |
| `OMRON_FINDINGS.md` | This summary |
| `extract_omron.py` | Re-runnable extractor |

---

## Implemented in toolkit (2026-07-15)

The **0x11 / 0x91 token handshake** is now in `omron_bp`:

- `OmronTransport.unlock_with_token()` — 20-byte frame `11 | nonce[4] | 00×15`, wait for `91 00 | echo`
- `HEM-7143T1` profile: `unlock_mode=TOKEN_KEY`
- Read path: connect → encrypt → **token unlock** → START → EEPROM → END

Matches this phone capture sequence before START.

---

## Next steps to fully decode BP numbers

1. Capture while taking a **known** measurement (write down SYS/DIA/PULSE and exact time).  
2. Re-run HCI dump during OMRON connect sync only.  
3. Diff `0x0020` notification bodies against known readings to map field offsets.  
4. Optionally open the `.cfa` in Wireshark with `btatt` + follow the connection handle for Omron.

If you want, the next pass can build a small decoder that tracks only handles `0x0003`/`0x0004` and dumps every proprietary frame as a CSV for reverse-engineering.

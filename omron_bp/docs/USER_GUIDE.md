# omron_bp — User guide

How to install, pair, and download blood pressure readings from Omron BLE
monitors (lab device: **HEM-7143T1**).

For architecture and protocol details, see  
[`SPEC_BP_BLE_MULTI_BRAND.md`](SPEC_BP_BLE_MULTI_BRAND.md).

---

## 1. Requirements

| Item | Notes |
|------|--------|
| OS | Windows 10/11 (this lab), also Linux/macOS via bleak |
| Python | 3.10+ recommended (lab uses 3.12) |
| Bluetooth | PC Bluetooth ON, BLE-capable radio |
| Cuff | Omron model supported in the catalog (23 families) |

---

## 2. One-time setup

Open PowerShell:

```powershell
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"
python -m pip install -r omron_bp\requirements.txt
```

Check the app:

```powershell
python -m omron_bp list-models
python -m omron_bp --help
```

---

## 3. Cuff button modes (important)

| Mode | How | When to use |
|------|-----|-------------|
| **Pairing (P)** | **Hold** Bluetooth button 3–5 seconds until flashing **P** / **-P-** | First-time bond with this PC only |
| **Transfer** | **Short-press** Bluetooth once | Download readings |

- Pairing and reading are **different** modes.  
- After pairing, the cuff often sleeps — you must wake it again for a read.  
- Many cuffs allow **only one phone/PC bond**. If the PC cannot connect, unpair the cuff from the **OMRON connect** phone app and from Windows Bluetooth settings, then pair again.

---

## 4. Your lab device (defaults)

| Field | Value |
|-------|--------|
| Model | `HEM-7143T1` |
| Example MAC | `E1:99:7D:27:1C:0A` |

After a successful pair/read, the app saves:

```text
experiments\omron_bp_device.json
```

with `model_id` and `address`. Later commands can omit `-d` and `-m`.

These defaults are **not** the only supported models — pass `-d` / `-m` for any catalog device.

---

## 5. Commands

All commands are run from the `experiments` folder:

```powershell
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"
```

### 5.1 Interactive menu

```powershell
python -m omron_bp
```

Options: **1 Pair** · **2 Read** · **3 List models** · **4 Quit**

With debug logs:

```powershell
python -m omron_bp -v
```

### 5.2 List supported models

```powershell
python -m omron_bp list-models
```

Shows stack (classic/modern), pairing type, user count, and display name.

### 5.3 Pair (once per PC)

1. Unpair phone if needed.  
2. On the cuff: **hold** BT until flashing **P**.  
3. Run:

```powershell
python -m omron_bp pair -d HEM-7143T1 -m E1:99:7D:27:1C:0A -v
```

4. Accept the Windows pairing dialog if it appears.  
5. Success looks like: `PAIR finished` and `Post-bond smoke test … OK`.

Without `-m`, the app can scan and ask you to pick a device.

### 5.4 Read measurements

1. Prefer a recent BP measurement on the cuff (optional but good for checking).  
2. Run the command, then **short-press** BT when you see `Waiting for cuff…`.  
3. Keep the cuff near the PC.

```powershell
python -m omron_bp read -o .\data -v
```

If config is already saved, `-d` and `-m` are optional:

```powershell
python -m omron_bp read -o .\data
```

Explicit flags:

```powershell
python -m omron_bp read -d HEM-7143T1 -m E1:99:7D:27:1C:0A -o .\data -v
```

### 5.5 Auto-fetch once (`watch`)

Waits longer for the cuff (default **180s**), downloads when it advertises, then
prints **latest readings**.

```powershell
python -m omron_bp watch -o .\data
python -m omron_bp watch -o .\data --latest 10 --wait 180
```

### 5.6 Hands-free station (`serve`) — force sync every 1 minute

**Data collection station mode.** After pairing once, leave this running:

```powershell
# Force sync every 60 seconds (default)
python -m omron_bp serve -o .\data

# Custom interval (e.g. every 30s)
python -m omron_bp serve -o .\data --interval 30
```

| Setting | Default | Meaning |
|---------|---------|---------|
| `--interval` | 60 | Seconds between force-sync attempts |
| `--wait` | interval − 5 | Max time each cycle waits for cuff BLE |
| `-o` | `.` | CSV output folder |

Behavior each cycle:

1. Try to find cuff and download history (no menu prompts)  
2. On success → print **LATEST READINGS** + update CSV  
3. On failure → log and wait for next cycle (cuff may be sleeping)  
4. Sleep until the next interval  

**Hands-free reality:** Pairing still needs **P** once. Daily use needs **no BLE button** when the cuff radio is on (often briefly after a measurement). The station keeps trying every minute so you always pick up the latest stored data when the device is reachable.

Ctrl+C stops the station.

**Success looks like:**

- `Found … — connecting immediately`  
- Banner **LATEST READINGS** with SYS / DIA / BPM  
- `data\user1.csv` written  

### 5.5 Verbose / debug

| Flag | Meaning |
|------|---------|
| `-v` / `--verbose` | Hex TX/RX and step-by-step logs |
| Env `OMRON_BP_DEBUG=1` | Same as `-v` without the flag |

`-v` works **before or after** the subcommand:

```powershell
python -m omron_bp -v read -o .\data
python -m omron_bp read -o .\data -v
```

---

## 6. Output files

### 6.1 CSV

| Path | Content |
|------|---------|
| `data\user1.csv` | User 1 measurements |
| `data\user2.csv` | User 2 (if the model has two banks) |
| `data\backup_user1_….csv` | Automatic backup before merge |

**Columns:**

```text
datetime,dia,sys,bpm,mov,ihb
```

**Sort order:** **newest datetime first** (latest reading at the top of the file and in the console preview).

**Example row:**

```text
2026-07-14 15:38:00,76,112,75,0,0
```

- `sys` / `dia` — mmHg  
- `bpm` — pulse  
- `mov` — movement flag  
- `ihb` — irregular heartbeat flag  

### 6.2 Config

```text
omron_bp_device.json
```

```json
{
  "address": "E1:99:7D:27:1C:0A",
  "model_id": "HEM-7143T1"
}
```

Skip saving with `--no-save-config`.

### 6.3 Merge behavior

By default, a new read **merges** with an existing CSV (union by `datetime`), then sorts newest first.  
Overwrite without merge:

```powershell
python -m omron_bp read -o .\data --no-merge
```

---

## 7. Typical daily workflow (after first pair)

```powershell
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"
python -m omron_bp read -o .\data
```

When the app prints `Waiting for cuff…`, short-press Bluetooth on the monitor.  
Open `data\user1.csv` — latest reading is the first data row after the header.

---

## 8. Other models

```powershell
python -m omron_bp list-models
python -m omron_bp pair -d HEM-7322T -m AA:BB:CC:DD:EE:FF -v
python -m omron_bp read  -d HEM-7322T -m AA:BB:CC:DD:EE:FF -o .\data -v
```

- **Classic** models (`unlock_key`): first pair programs a key; later reads unlock automatically.  
- **Modern** models (`os_bonding`): Windows bond only (like HEM-7143T1).  
- Some modern models list `token_key` unlock — may need extra work for reliable background reads; pair/smoke may still work.

Regional names (e.g. `HEM-7146T`, `BP7250`) often resolve as **aliases** to a canonical model.

---

## 9. Troubleshooting

| Problem | What to try |
|---------|-------------|
| `not advertising` / not found | Short-press BT (transfer); keep near PC; wait for `Waiting for cuff` then press; batteries |
| Connect works only in P | For **read**, use short-press transfer, not long-press P |
| Pair fails | Unpair phone + Windows; enter flashing **P**; retry `pair` |
| Parent service missing | Wrong `-d` model (classic vs modern); retry |
| 0 records | Memory empty or wrong profile; check with `-v` hex dumps |
| Nonsense sys/dia | Wrong model profile / parser — confirm model with `list-models` |
| Phone works, PC does not | Cuff still bonded only to phone — forget phone bond |

---

## 10. Command cheat sheet

```powershell
# Setup
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"
python -m pip install -r omron_bp\requirements.txt

# Explore
python -m omron_bp list-models
python -m omron_bp --help

# First time (cuff: hold BT → flashing P)
python -m omron_bp pair -d HEM-7143T1 -m E1:99:7D:27:1C:0A -v

# Every download (cuff: short-press BT for transfer)
python -m omron_bp read -o .\data -v

# Quiet daily use (uses saved config)
python -m omron_bp read -o .\data
```

---

## 11. Related docs

| Document | Audience |
|----------|----------|
| [`USER_GUIDE.md`](USER_GUIDE.md) | Operators / you (this file) |
| [`SPEC_BP_BLE_MULTI_BRAND.md`](SPEC_BP_BLE_MULTI_BRAND.md) | Engineers / AI rebuilding the system |
| [`../README.md`](../README.md) | Package layout overview |

---

*Last updated: 2026-07-14 — console + CSV sort newest-first confirmed with live HEM-7143T1 read.*

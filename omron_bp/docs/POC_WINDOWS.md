# Windows PoC notes (before Android deployment)

## Strategy

| Phase | Host | Goal |
|-------|------|------|
| **Now** | Windows + `omron_bp` | Prove pair → fetch → show latest readings |
| **Later** | Android hub | Final hands-free station (more stable BLE) |

Do **not** redesign the Omron protocol for Windows flakiness. Protocol already worked once (30 records).

## PoC success criteria

1. `pair` once (cuff flashing **P**)
2. `watch` or `read` succeeds **3 times in a row** with transfer mode
3. CLI shows **LATEST READINGS** (sys/dia/bpm)
4. Optional: run `serve` for a few cycles as demo of force-sync loop

## Commands

```powershell
cd "C:\Users\Shawn A\Desktop\Medical project\experiments"

# 1) Pair once
python -m omron_bp pair

# 2) PoC fetch (short-press BT when scanning)
python -m omron_bp watch -o .\data -v

# 3) Hands-free loop demo (after watch is reliable)
python -m omron_bp serve -o .\data --interval 60
```

## Windows reliability checklist

1. Unpair cuff from **phone** (OMRON connect) — one host only  
2. Windows Bluetooth → remove old Omron / BLESmart entry if present  
3. Pair with our app in **P** mode  
4. For read/watch: **short-press** BT (transfer), cuff **&lt; 1 m** from PC  
5. Prefer built-in Bluetooth or a known-good BLE 5 dongle (avoid CSR Harmony)  
6. Close other apps using BLE during the demo  

## Known Windows errors (not protocol bugs)

| Error | Meaning |
|-------|---------|
| `Could not get GATT services: Unreachable` | Link died during service discovery |
| `Could not start notify … Unreachable` | Link died when enabling CCCD |
| Instant `Found` then drop | Often a **cached** device, not live transfer — fixed by live active scan (2 advert hits) |
| Advert found, then drop | Transfer window too short / stack race |

### Live advert requirement

`watch` / `read` / `serve` now require **live active-scan hits** (not
`find_device_by_address` cache). You should see:

```text
Live advert #1 from E1:99:…
Live advert #2 from E1:99:…
Using live advert (hits=2, …) — connecting immediately
```

If you only get instant "Found" with no "Live advert", update to latest code.

**Mitigation in code:** simple connect path, retries, no `pair=True` on read.

## Android later

Port the same layers:

- profiles / parsers (already device-agnostic)
- transport framing (start / read / end)
- workflows: pair once, serve poll loop

Replace only `ble/session` with Android `BluetoothGatt` (like SHHM `BleManager`).

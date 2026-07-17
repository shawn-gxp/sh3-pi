# Ours vs Nipro companion — parser & session gaps

**Goal:** make `medical_ble_toolkit` behave like げんきノート for HW sync.  
**Ours:** datasheet-based parsers + `ble_client` sessions.  
**Theirs:** decompiled `BLELib` / `NHL` (see `FIRST_PARTY_HW_SUPPORT.md`, `EXACT_HW_SEQUENCES.md`).

**No git commit.** Local analysis only.

---

## Executive summary

| Area | Verdict | Blocker for “works like companion”? |
|------|---------|-------------------------------------|
| **BP BLP parse (`0x2A35`)** | Largely OK | Low — parse is fine; session/name mapping incomplete |
| **A&D/UA-651 session** | **Over-engineered vs app** | Medium — extra custom cmds can break NMBP/NBP path |
| **NT-100B** | **Wrong primary path** | **High** — we TICD-poll; app uses **HTP indicate** |
| **NSM-1BLE** | Missing profile | **High** — no driver |
| **NBP-1BLE** | Missing name/session | **High** — only generic BLP / UA-651 profile |
| **NIPRO CF glucose** | **Missing entirely** | **Critical** — wrong UUIDs (SIG vs proprietary) |
| **MightySat parse** | Mostly OK | Medium — **session order + SetClock payload wrong** |
| **NBCM** | Missing | High if scale needed |
| **Hands-free / pair registry** | Missing | **Critical** for companion-like UX |
| **Timing (1s settle, 60s, exact name)** | Partial | High for reliability |

**Bottom line:** pure measurement **parsers** for SIG BLP / HTP / MightySat frames are mostly good.  
What fails “like the company app” is mostly **session orchestration, name/device matrix, and which transport path is used** — not SFLOAT math.

---

## Device matrix: what the app does vs what we have

| Companion device | Adv name | App path | Our profile / parser | Match? |
|------------------|----------|----------|----------------------|--------|
| BP (Nipro) | `NBP-1BLE*` | BLS: delay 1s → write `0x2A08` → indicate `0x2A35` | no dedicated profile | ❌ |
| BP (A&D/NMBP) | `NMBP*` | same BLS path + **bond** + serial DIS | `and_ua651` does BLP + **extra custom 0xA6/0xE1/0x01** | ⚠️ overshoot |
| HT contact | `NSM-1BLE*` | HTS clock `0x2A08` + indicate `0x2A1C` (+ custom disconnect on pair) | no profile (only generic HTP parse) | ❌ |
| HT IR | `NT-100B*` | **Indicate HTP `0x2A1C` only**; teardown **power-off** TICD `0x50` | `thermometer` = **full TICD history poll** | ❌ wrong path |
| Glucose | `NIPRO CF*` | Proprietary CF UUIDs + RACP-like `04 01`/`01 01` | `glucose.py` = **SIG 0x1808** (Beurer) | ❌ different stack |
| SpO2 | `MightySat*` | notify first → **GetInfo → SetClock(Ticks) → EnableStream(3B from info)** | SetClock first (unix s) → GetInfo → ConfigureStreaming(fixed mask) | ⚠️ |
| Scale | `NBCM*` | A&D custom `11127000` + User Data + WSS/BCS | none | ❌ |

---

## 1. Blood pressure

### What we have (good)

- `blood_pressure.py`: standard BLP `0x2A35` (flags, 3×SFLOAT, timestamp, pulse, status).
- Matches companion field order for **mmHg** path.
- Sentinels: companion rejects SBP/DBP/pulse **2047** (and pulse **2048**); we treat NaN/NRes via SFLOAT — should **also** reject 2047 explicitly for companion parity.

### Diffs / issues

| # | Issue | Companion | Ours | Fix |
|---|--------|-----------|------|-----|
| BP1 | **Clock characteristic** | Writes **Date Time `0x2A08`** on BLS service (7 bytes) | Beurer uses `0x2A2B` Current Time (10B); UA-651 helper has `0x2A08` but only in `and_ua651` session | Always use **`0x2A08` 7-byte** for Nipro BP; delay **1s** after connect |
| BP2 | **Custom A&D path** | **NMBP/NBP receive does NOT** send 0xA6 / 0xE1 / custom SetTime | `and_ua651` session **always** set buffer + E1 + custom set time | Split profiles: `nipro_nbp` / `nipro_nmbp` = BLP+2A08 only; keep full UA-651 SDK path as optional `and_ua651_full` |
| BP3 | **Bond** | NMBP waits **Bonded** before serial | We warn on Windows if no `--pair` | Require pair for NMBP; NBP-1BLE may work without |
| BP4 | **Name hints** | `NBP-1BLE`, `NMBP` | profile hints are UA-651 oriented | Add exact name prefixes |
| BP5 | **History policy** | UI keeps **latest** BP reading | We may collect all indications | Product choice: store all, surface latest like app |
| BP6 | **kPa flag** | Decompiled kPa branch looks odd (skips bytes) | Correct SIG layout | Prefer our SIG parse |
| BP7 | **Post-disconnect** | 100 ms settle | not standardized | add 100 ms |

### Companion session (target)

```
connect(LE) → wait connected → sleep(1.0)
→ write 0x2A08 DateTime(7)
→ read 0x2A08 (optional verify)
→ start_notify 0x2A35
→ wait ≤60s or disconnect
→ disconnect → sleep(0.1)
```

---

## 2. Thermometer NT-100B — **largest behavioral mismatch**

### Companion (what actually runs on receive)

1. Connect  
2. **StartUpdates on HTP `0x1809` / `0x2A1C` only**  
3. Wait for indication (IEEE-11073 temperature)  
4. **On teardown:** TICD power-off `51 50 00 00 00 00 A3 44` on `1523/1524`  
5. Delay 1s, disconnect  

**Does not** dual-wake, write clock via TICD, poll 0x2B/0x25/0x26 for normal receive.

### Ours (`thermometer` profile + `ble_client`)

1. Dual-wake TICD  
2. Write clock 0x33  
3. Read storage count 0x2B  
4. Poll history 0x25/0x26 pairs  
5. Optional 0x41 live measure  

| # | Issue | Impact |
|---|--------|--------|
| T1 | **Wrong primary channel** (TICD vs HTP) | Sync fails or empty if device only indicates HTP after measure |
| T2 | **No power-off** | Device may stay on / behave differently after session |
| T3 | **No HTP parse wired as primary** | `htp.py` exists but thermometer session doesn’t use it |
| T4 | **Reject rules** | App rejects temp 65535 / &lt;0 / MinValue timestamp | add |
| T5 | **Name** | App expects `NT-100B` prefix | ensure hints |

### Target session (match companion)

```
connect → notify 0x2A1C (HTS)
→ collect HTP indication(s)  [parser: htp.py]
→ write power-off TICD 0x50 (thermometer.cmd_power_off)
→ sleep(1) → disconnect
```

Keep TICD history poll as **optional advanced mode**, not default.

---

## 3. Thermometer NSM-1BLE — missing

| # | Gap |
|---|-----|
| N1 | No profile `NSM-1BLE` |
| N2 | Session = same as HTP + **write 0x2A08 on 0x1809** after 1s (like BP) |
| N3 | Pair path: custom `233BF001` write `02 01 03` disconnect (our `and_ua651.cmd_disconnect` is 20-byte padded; app sends **raw 3 bytes** `02 01 03`) |
| N4 | Custom UUID same family as UA-651 — **don’t** conflate with full BP UA-651 session |

**Padding issue:**  
- Ours: `build_custom_command` → **20-byte** padded buffer  
- App NSM: writes **`new byte[3]{2,1,3}`** only  

Some stacks accept both; for companion parity prefer **exact short writes** unless device requires 20-byte.

---

## 4. Glucose NIPRO CF — missing (not Beurer GL)

### Ours

- `glucose.py` / `beurer_glucose`: SIG service **`0x1808`**, RACP **`0x2A52`**, measurement **`0x2A18`**.

### Companion CFL

| Item | Value |
|------|--------|
| Service | `5D87A4A0-E42D-11E5-BEEF-0002A5D5C51B` |
| Measurement | `…A1…` |
| Context | `…A2…` |
| RACP | `…A3…` |
| Time service | `87F60001-…` / char `87F60002-…` |
| RACP count all | `04 01` |
| RACP report all | `01 01` |
| Diff | `04 03 01` + seq LE; report `01 03 01` + seq LE |

| # | Issue | Severity |
|---|--------|----------|
| G1 | **Completely different GATT** | Critical |
| G2 | Measurement layout (seq, time, SFLOAT @12–13, type @14) | Critical |
| G3 | Glu scale: app uses `(int)(sfloat * 100000m)` then clamps 20–600 | High |
| G4 | Skip control solution type `"0A"` | High |
| G5 | Diff mode last_seq+1 from store | High (hands-free) |
| G6 | Meal from context notify | Medium |

**Action:** new `parsers/nipro_cf.py` + profile `nipro_cf` + session — do **not** reuse Beurer SIG glucose for CF meters.

---

## 5. MightySat SpO2

### Parse layer — mostly OK

- Framing `0x77` / LEN / CRC8-CCITT matches app.  
- Parameters `0x05`, sensor-off bit 21, PI /100 — matches.  
- Reassembler is **better** than companion (they assume single notify chunks).

### Session / command layer — diffs

| # | Issue | Companion | Ours | Fix |
|---|--------|-----------|------|-----|
| M1 | **Command order** | Notify ON → **GetInfo(1)** → on response SetClock(2) → on ACK EnableStream(3) | SetClock → GetInfo → ConfigureStreaming | Reorder to match state machine |
| M2 | **SetClock payload** | `BitConverter.GetBytes(DateTime.UtcNow.Ticks)` = **8-byte .NET ticks** | `unix_seconds` **4-byte** uint32 | **Mismatch** — use 8-byte ticks **or** verify CSD; app uses ticks |
| M3 | **EnableStream data** | **3 bytes** copied from device info `[3],[4],[5]` (available params/wave) | Fixed mask `0x001F` + wave `0x03` (4-byte style body) | After GetInfo, use device-reported bytes like app |
| M4 | **Write type** | WithoutResponse | we use response=False | OK |
| M5 | **End condition** | sensor-off bit → complete | we stream until quiet timeout | Prefer sensor-off complete + timeout backup |
| M6 | **Pairing** | serial from mfg AD only | optional | optional |

### SetClock detail

```
// Companion
MakeCommand(2, BitConverter.GetBytes(DateTime.UtcNow.Ticks));
// .NET Ticks = 100ns since 0001-01-01, 8 bytes LE
```

```
// Ours
cmd_set_clock(int(time.time()))  # 4-byte unix
```

If live stream works with our clock, device may ignore payload; for **strict companion parity**, send 8-byte ticks.

---

## 6. A&D UA-651 helpers vs companion BP

| Feature | Datasheet / ours | Companion NMBP/NBP |
|---------|------------------|---------------------|
| Custom set time 0x01 | yes | **not used on receive** |
| Set buffer 0xA6 | yes | **not used** |
| Request memory 0xE1 | yes | **not used** |
| 0x2A08 Date Time | yes | **yes — only write** |
| Indicate 0x2A35 | yes | yes |
| 20-byte pad | yes (SDK) | short writes on NSM custom |

**Risk:** sending 0xE1 / 0xA6 on devices that only implement “simple BLP” may no-op or error; companion never does it for NMBP/NBP receive.

---

## 7. Orchestration (not parsers) — required for companion-like UX

Missing in toolkit today:

| Feature | Companion | Ours |
|---------|-----------|------|
| Pair store (Id without dashes, serial, userNo) | yes | ad-hoc / medical_ble_device.json |
| Hands-free exact-name wait 8h | yes | no |
| 60s receive timeout | yes | varies by profile |
| Health check restart scan 5s | yes | no |
| CheckPairing before connect | yes | optional |
| Dedup glucose by seq+serial | yes | Beurer only |
| BP invalid 2047 | yes | partial |
| Multi-device ReceiveDeviceList lock | yes | no |

Without this layer, even perfect parsers won’t “feel” like the companion.

---

## 8. Priority fix list (to match companion)

### P0 — must fix for correct devices

1. **NT-100B default session → HTP indicate + power-off** (not TICD history).  
2. **Add NIPRO CF glucose** (UUIDs + RACP + parse + Diff seq).  
3. **Add NBP-1BLE / NMBP profiles** — BLP + `0x2A08` + 1s delay; **no** forced 0xE1.  
4. **Add NSM-1BLE** — HTP + `0x2A08` clock.  

### P1 — session parity

5. **MightySat** reorder + ticks clock + EnableStream from device info.  
6. **Split `and_ua651`**: simple companion path vs full SDK lab path.  
7. **Sentinels / clamps** (BP 2047, temp 65535, glu 20–600, skip type 0A).  

### P2 — product UX

8. Hands-free registry + exact name wait + 60s/8h/1s/100ms timings.  
9. NBCM if needed.  

---

## 9. What NOT to “fix” in parsers

- Our BLP SFLOAT layout is **better or equal** to companion for edge cases.  
- MightySat reassembler is **superior** — keep it.  
- TICD full history is still useful as **lab mode**, not default NT-100B sync.  
- Beurer SIG glucose must stay for Beurer GL* — don’t overwrite with CF.

---

## 10. Suggested new profiles (names)

| Profile id | name_hints | Session |
|------------|------------|---------|
| `nipro_nbp` | `NBP-1BLE` | BLP + 2A08, no custom |
| `nipro_nmbp` | `NMBP` | BLP + 2A08 + bond |
| `nipro_nsm1` | `NSM-1BLE` | HTP + 2A08 |
| `nipro_nt100b` | `NT-100B` | HTP + power-off |
| `nipro_cf` | `NIPRO CF` | CF UUIDs + RACP |
| `mightysat` | (existing) | fix session only |
| `and_ua651_sdk` | optional | current full custom path |

---

## 11. Quick verification plan

| Test | Expect like companion |
|------|------------------------|
| NBP measure → sync | 1s wait, clock write, multi or latest BP, disconnect OK |
| NT-100B after forehead read | HTP value appears; device powers off |
| NSM-1BLE | HTP value after clock write |
| NIPRO CF | Diff pulls only new seqs |
| MightySat on finger | stream SpO2/PR/PI; ends on sensor off |

---

*Sources: `medical_ble_toolkit/parsers/*`, `ble_client.py`, `profiles.py` vs `decompiled_cs/BLELib` + `NHL`.*

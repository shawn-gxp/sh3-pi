# Pi Medical BLE Hub — Execution Plan

**Status:** Phase 0 + critical Phase 1 **implemented** (2026-07-20). P2+ not started.  
**Date:** 2026-07-20  
**Product:** Raspberry Pi appliance — WiFi + BLE hub + web UI (phone on same LAN) → SQLite → (later) MQTT  

### Implementation log

| Phase | Status | Notes |
|-------|--------|-------|
| P0 | **Done** | Roster paired-only, WAL, MAC-strict match, LAN bind, unit tests |
| P1 | **Done** (core) | Fixed Nipro path, brand migrate, paired_devices.json export |
| P2 | Not started | supported_devices.json |
| P3 | **Partial** | `start_hub.sh` + LAN bind; systemd unit still optional |
| P4–P6 | Not started | |

---

## 1. Goals

| # | Goal |
|---|------|
| G1 | Deploy on a Pi: boot → WiFi → BLE on → web server → phone access over LAN |
| G2 | SQLite is the **source of truth** for paired devices and clinical readings |
| G3 | Clear separation: **supported catalog** (what software can do) vs **paired inventory** (this Pi’s meters) |
| G4 | Hub auto-sync remains reliable for Tier-1: Omron, NBP-1BLE, NT-100B, MightySat |
| G5 | Multi-device safe matching (MAC-strict) |
| G6 | Prepare clean ground for MQTT **without** changing clinical storage model |

## 2. Non-goals (out of scope for this plan)

| # | Non-goal |
|---|----------|
| N1 | Rewriting or “simplifying” parsers / post-connect unlock / stream-enable sequences |
| N2 | Deleting interactive CLI, live_monitor, FORA scaffold, debug tools, datasheets |
| N3 | MQTT implementation (planned after store + hub stability) |
| N4 | Full collapse of `omron_bp` nested package or dual CLI |
| N5 | Big-bang merge of `ble_jobs` + `ble_client` into one god module |
| N6 | Public internet exposure / cloud auth (LAN appliance only for now) |

## 3. Locked product decisions

| Decision | Choice |
|----------|--------|
| Platform | Raspberry Pi + BlueZ + Linux |
| Clinical store | **SQLite** (`medical_ble_web/data/poc.db`) |
| Next integration | **MQTT** after this plan’s phases land |
| Pair inventory | SQLite primary; optional JSON **mirror/export** only |
| Supported devices | Catalog in code today; optional Tier-1 JSON catalog later (read-only for UI) |
| Access model | Phone browser → `http://<pi-ip>:8741` on same WiFi |
| BLE unlock / post-connect | **Must not be removed or rewritten** — only call-sites may wrap later |
| Debug / RE logging | Keep |
| Device scaffolding | Keep (FORA, re_generic, profiles, parser factories) |

## 4. Hard “do not break / do not touch” list

Implementation tasks **must not** edit the following unless a later phase explicitly allows a **minimal bugfix** with test + manual checklist:

| Area | Paths (representative) | Why |
|------|------------------------|-----|
| Parsers | `medical_ble_toolkit/parsers/*`, `common/sfloat.py`, `common/crc.py` | Working golden logic |
| Post-connect / unlock | `ble_client.py` → `run_post_connect_setup`, teardown; Omron transport unlock/token; MightySat enable-stream; Nipro clock / TICD pull | Field-proven sequences |
| Omron pair/read core | `omron_bp/ble/*`, `omron_bp/pairing/*`, `omron_bp/readout/*` | Working stack |
| Beurer session timing | `beurer/session.py`, timing helpers | When Beurer used |
| Quiet / duty-cycle listen | `MedicalBleClient.listen` stream duty + quiet timeouts | Hub multi-device behavior |

**Allowed** in early phases: hub roster, match policy, registry **path**, SQLite pragmas, brand-id normalize on pair, web bind host, boot scripts, catalog **files** that only describe UI/SLA, tests for those layers, docs.

## 5. Architecture target (after plan)

```
supported_devices (catalog — ship with image)
        │  UI brand list / hub tier policy
        ▼
pair on phone ──► SQLite devices (mac, name, brand, model, paired=1)
        │              │
        │              ├── optional export: data/paired_devices.json (mirror)
        │              └── optional: Nipro registry file FIXED next to DB
        ▼
HubDaemon (MAC-strict match) ──► existing pair/sync drivers
        │                            (unlock sequences UNCHANGED)
        ▼
SQLite readings ──► dashboard
        │
        └── (later) MQTT publish same rows
```

### Two data layers (explicit)

| Layer | Meaning | Store | Mutated by |
|-------|---------|-------|------------|
| **Supported** | What the software knows how to talk to | Code (`brands.py`, `profiles.py`, Omron catalog) + optional `supported_devices.json` | Developers / releases |
| **Paired** | What is bonded to *this* Pi | **SQLite `devices`** (+ optional JSON mirror) | Pair / repair / admin reset |

---

## 6. Phases overview

| Phase | Name | Risk | Depends on | Outcome |
|-------|------|------|------------|---------|
| **P0** | Correctness hotfixes | Low | — | Safe roster, fixed paths, WAL, MAC match, LAN bind |
| **P1** | Data model clarity | Low–med | P0 | Brand ids normalized; paired export; Nipro path pinned |
| **P2** | Supported catalog file | Low | P1 optional | Tier-1 `supported_devices.json` drives UI list |
| **P3** | Pi appliance boot | Low | P0 | Script/service: BT + web + LAN URL |
| **P4** | Driver facade (thin) | Med | P0–P1 | Single API wrapper; **no** unlock moves |
| **P5** | MQTT | Med | P1 stable | Publish readings; SQLite remains SoT |
| **P6** | Optional cleanup | Low | After field soak | Only with explicit approval |

**Implementation rule:** finish a phase → manual checklist green → then open next phase. No multi-phase mega-PR.

---

## 7. Phase 0 — Correctness hotfixes

**Objective:** Fix known production bugs without touching BLE unlock/parser logic.

### Subtasks

| ID | Subtask | Files (expected) | Notes |
|----|---------|------------------|-------|
| **P0.1** | Hub roster = **paired only** | `medical_ble_web/ble_jobs.py` (`_hub_roster`) | Remove fallback that uses unpaired devices |
| **P0.2** | SQLite WAL + busy_timeout | `medical_ble_web/db.py` | `PRAGMA journal_mode=WAL`, `busy_timeout=30000` on connect |
| **P0.3** | MAC-strict hub AD match | `medical_ble_toolkit/hub/daemon.py` (`_match_ads`) | Match roster MAC first; drop “single brand → any AD” steal; optional exact-name secondary **only if MAC already known / update name**, not rebind random MAC |
| **P0.4** | Document match policy | `LINUX.md` or plan note | One short paragraph for operators |
| **P0.5** | Web LAN bind | `run_web.sh`, maybe `app.py` / env | `HOST` env default `0.0.0.0` on Pi path; print URL; keep `127.0.0.1` possible via env |
| **P0.6** | Regression smoke (automated) | existing unit tests | `test_parsers`, token unlock; no bleak required for pure tests if easy **without** changing unlock code — optional lazy-import only if zero behavior change |
| **P0.7** | Manual checklist run (Pi or Linux host) | — | See §10 |

### Acceptance criteria (P0)

- [ ] Unpaired devices never enter hub auto-sync roster  
- [ ] Two devices of same brand (two NBP) only sync when their own MAC advertises  
- [ ] Concurrent hub sessions do not fail with immediate `database is locked` under light load  
- [ ] Phone on same WiFi can open web UI (`0.0.0.0` bind)  
- [ ] Omron pair/read, NBP post-measure, NT TICD, MightySat stream still work (manual)  
- [ ] No edits to parser decode logic or Omron unlock frame builders  

### Rollback

- Revert the single PR for P0; SQLite WAL is safe to leave if already enabled  

---

## 8. Phase 1 — Data model clarity (paired inventory)

**Objective:** One clear place for “name + MAC on this Pi”; kill dual-registry confusion.

### Subtasks

| ID | Subtask | Files (expected) | Notes |
|----|---------|------------------|-------|
| **P1.1** | Fixed Nipro registry path | `nipro/registry.py` | Absolute path: e.g. `medical_ble_web/data/nipro_paired_devices.json` or env `MEDICAL_NIPRO_REGISTRY`; **not** `Path.cwd()` |
| **P1.2** | Migrate / merge existing JSON copies | one-shot on startup or setup script | Prefer `medical_ble_web/` copy (has MightySat); do not delete root copy until confirmed |
| **P1.3** | Brand-id normalize on pair | `ble_jobs.job_pair`, brands helpers | Map `thermo`→`nipro_nt100b`, `and`→`nipro_nbp` when pairing Tier-1; store canonical ids in SQLite |
| **P1.4** | One-time DB brand migrate | `db.py` or startup | Update existing rows to canonical brand ids if present |
| **P1.5** | Ensure exact adv `name` always saved at pair | `job_pair` | Already partially done; verify required for Nipro |
| **P1.6** | Optional: export `paired_devices.json` | `db.py` or `ble_jobs` after pair | Mirror of SQLite devices for humans/backup; **SQLite remains SoT**; hub continues to read SQLite |
| **P1.7** | Admin reset clears fixed registry + export | `app.py` `/admin/reset` | Same paths as P1.1/P1.6 |
| **P1.8** | Unit tests for registry path + brand normalize | `tests/` | No BLE hardware |

### Paired export schema (if P1.6)

```json
{
  "version": 1,
  "updated_at": "ISO-8601",
  "devices": [
    {
      "mac": "AA:BB:CC:DD:EE:FF",
      "name": "exact-adv-name",
      "brand": "nipro_nbp",
      "model": "NBP-1BLE",
      "paired": true
    }
  ]
}
```

### Acceptance criteria (P1)

- [ ] Only one Nipro registry file path used regardless of CWD  
- [ ] New pairs write canonical brand ids  
- [ ] Hub roster still SQLite-only (paired)  
- [ ] Export (if enabled) matches SQLite after pair  
- [ ] Unlock / sync behavior unchanged  

### Rollback

- Env var to restore old CWD registry temporarily if needed  

---

## 9. Phase 2 — Supported devices catalog (optional, low risk)

**Objective:** One shippable file describing Tier-1 (and advanced) **supported** devices for UI/docs; code catalogs remain authoritative for UUIDs/parsers.

### Subtasks

| ID | Subtask | Notes |
|----|---------|-------|
| **P2.1** | Author `medical_ble_toolkit/supported_devices.json` (or `medical_ble_web/data/`) | Tier-1 first: omron, nipro_nbp, nipro_nt100b, masimo |
| **P2.2** | Loader with fallback to current `brands.py` | If file missing, existing Python list wins (no break) |
| **P2.3** | Wire `/brands` to loader | UI unchanged for phone |
| **P2.4** | Document how to add a supported device | Steps: parser (existing) + profile + catalog entry + optional hub policy — **no unlock removal** |
| **P2.5** | Do **not** move Omron EEPROM catalog into this JSON yet | Keep `omron_bp` catalog in code |

### Supported catalog schema (draft)

```json
{
  "version": 1,
  "devices": [
    {
      "id": "omron",
      "company": "Omron",
      "label": "Omron BP",
      "tier1": true,
      "connect_profile": "omron",
      "default_model": "HEM-7143T1",
      "vital_kind": "bp",
      "supports": ["pair", "repair", "sync", "read"],
      "name_hints": ["omron", "blesmart", "hem-"]
    }
  ]
}
```

### Acceptance criteria (P2)

- [ ] Phone brand list still shows Tier-1 by default  
- [ ] Missing JSON file → same behavior as today  
- [ ] Adding a catalog row alone does **not** change BLE unlock sequences  

---

## 10. Phase 3 — Pi appliance boot & LAN access

**Objective:** One operator path: power on Pi → service usable from phone.

### Subtasks

| ID | Subtask | Notes |
|----|---------|-------|
| **P3.1** | `run_web.sh` HOST/PORT env (from P0.5) | Document `HOST=0.0.0.0 PORT=8741` |
| **P3.2** | Boot helper script e.g. `start_hub.sh` | `bluetoothctl power on` (best effort) → start web with LAN bind → print IP |
| **P3.3** | Optional systemd unit skeleton | `medical-ble-hub.service` User=, WorkingDirectory=, After=network bluetooth |
| **P3.4** | WiFi | **Out of repo** or document only: use raspi-config / NetworkManager / your existing auto-WiFi script — do not invent WiFi stack in Python |
| **P3.5** | Firewall note | Allow TCP 8741 on LAN if ufw enabled |
| **P3.6** | Update `LINUX.md` + root `README.md` | Phone URL, pair-once hub-only policy |

### Acceptance criteria (P3)

- [ ] Fresh boot (or one command) → web reachable from phone on same WiFi  
- [ ] Auto-sync starts if paired devices exist (existing behavior)  
- [ ] No dependency on Windows paths  

---

## 11. Phase 4 — Thin driver facade (reuse without rewrite)

**Objective:** Single entry for pair/sync used by web + hub; **internally calls existing code** (including unlocks).

### Subtasks

| ID | Subtask | Notes |
|----|---------|-------|
| **P4.1** | Design API only (signature review) | e.g. `async def pair_device(...)`, `async def sync_device(...) → SyncResult` |
| **P4.2** | Implement facade as thin wrappers | Call `pair_omron`, `MedicalBleClient.run`, Beurer session — **copy zero unlock logic** |
| **P4.3** | Point `job_sync` / `_hub_run_session` at facade | One call site change at a time |
| **P4.4** | Leave interactive / live_monitor as-is | Or optional later thin callers |
| **P4.5** | Do not delete handsfree yet | Document “hub is preferred auto path”; stop dual-start remains |

### Acceptance criteria (P4)

- [ ] Same field behavior as before wrapper  
- [ ] Diff of unlock-related functions is empty or import-only  
- [ ] Manual checklist green  

---

## 12. Phase 5 — MQTT (after store stable)

**Objective:** Fan-out clinical rows; SQLite remains system of record.

### Subtasks

| ID | Subtask | Notes |
|----|---------|-------|
| **P5.1** | Enable config already stubbed | `hub_config.json` `mqtt_enabled`, topic prefix |
| **P5.2** | Publish on successful clinical insert | Payload from reading row + device mac/brand |
| **P5.3** | Fail-open | MQTT down must not fail BLE session |
| **P5.4** | Docs + smoke test with local broker |

### Acceptance criteria (P5)

- [ ] Readings always land in SQLite even if broker down  
- [ ] Message schema versioned  

---

## 13. Phase 6 — Optional cleanup (approval only)

Only after field soak. Examples (each separate decision):

- Collapse dual Nipro hands-free vs hub documentation  
- Parser alias normalize helper  
- Rename dual `DeviceProfile` types for clarity  
- Remove dead `medical_ble_device.json` / unused root files **if** confirmed unused  

**Still not:** deleting unlock sequences, FORA scaffold, debug tools.

---

## 14. Manual regression checklist (run after every phase)

Use the **same physical devices** when possible. Do not skip if that brand is in Tier-1.

| # | Check | Pass? |
|---|--------|-------|
| M1 | Web opens from phone (LAN) | |
| M2 | Scan lists nearby meters | |
| M3 | **Omron** pair (flashing P) then sync/read (history) | |
| M4 | **NBP-1BLE** pair with exact name; measure; auto or manual sync gets BP | |
| M5 | **NT-100B** measure; hub/sync gets temperature (TICD/post-connect) | |
| M6 | **MightySat** finger in; SpO2 stream/duty-cycle; hub returns to others | |
| M7 | Two paired devices: second brand still syncs (no MAC steal) | |
| M8 | Admin reset → re-pair works | |
| M9 | Parser unit tests still pass | |

If any Tier-1 check fails → **stop**, revert phase PR, do not start next phase.

---

## 15. Dependency graph

```
P0.1 roster ──┐
P0.2 WAL    ──┼──► P0 done ──► P1 (paths + brands) ──► P2 (supported JSON, optional)
P0.3 MAC    ──┤                      │
P0.5 LAN    ──┘                      ├──► P3 (boot/systemd)  [can start after P0 if needed]
                                     └──► P4 (facade) ──► P5 (MQTT)
                                                     └──► P6 (cleanup, optional)
```

**Parallel allowed after P0:** P3 (boot) can proceed in parallel with P1 if only using LAN bind from P0.  
**P5 must not start** before P1 brand/path stability.

---

## 16. Suggested PR / commit split

| PR | Contains |
|----|----------|
| PR-A | P0.1 + P0.2 + tests/docs note |
| PR-B | P0.3 MAC-strict + docs |
| PR-C | P0.5 + P3.1–P3.2 LAN + start script |
| PR-D | P1 registry path + brand normalize + export |
| PR-E | P2 supported_devices.json |
| PR-F | P4 facade |
| PR-G | P5 MQTT |

Prefer small PRs; never combine P0.3 with P4.

---

## 17. Current baseline (for implementers)

| Item | Status today |
|------|----------------|
| SQLite devices | 4 lab meters; brands partly non-canonical (`and`, `thermo`) |
| Nipro JSON | Two files (root vs web); CWD-dependent |
| Supported list | `brands.py` Tier-1 + `profiles.py` (24) + Omron/Beurer code catalogs |
| Web bind | `127.0.0.1` only in `run_web.sh` — phone LAN fails |
| Hub roster fallback | Unpaired devices can be hunted — lab hazard |
| Unlock / parsers | Working — **frozen** for early phases |

---

## 18. Approval gate

Before any code:

- [ ] Product owner confirms phase order (especially P2 optional vs skip)  
- [ ] Confirm paired export JSON desired in P1 (yes/no)  
- [ ] Confirm LAN default bind `0.0.0.0` (yes for Pi appliance)  
- [ ] Confirm WiFi remains external script (yes recommended)  

**Implementation starts only after:** “Approved — start Phase 0” (or “start P0 PR-A”).

---

## 19. Out of scope reminders for implementers

- Do not “clean up” `run_post_connect_setup` while doing P0/P1  
- Do not delete `interactive.py` / `live_monitor.py` / FORA  
- Do not change Omron FE4A / token / classic key logic  
- Do not switch clinical store off SQLite for MQTT  

---

*End of plan. Implementation is intentionally blocked until explicit go-ahead.*

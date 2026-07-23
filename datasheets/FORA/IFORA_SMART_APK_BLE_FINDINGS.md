# iFORA Smart APK findings — **SUPERSEDED**

> **Use instead:** [`FORA_FIRST_PARTY_PROTOCOL.md`](./FORA_FIRST_PARTY_PROTOCOL.md)  
> and [`EXACT_PROTOCOL_FROM_APK.json`](./EXACT_PROTOCOL_FROM_APK.json)

This file was the **early** RE pass (DEX string mining only, no jadx).  
It is **out of date** on:

- jadx availability (we later decompiled fully)
- PIN (confirmed **`111111`**)
- FSM confidence (now high for host path)
- Direction byte / command templates (now exact)

Keep only for historical context of the RE pipeline. **Do not implement from this file.**

### What replaced it

| Topic | Canonical location |
|-------|-------------------|
| GATT, frames, all cmds, timings | `FORA_FIRST_PARTY_PROTOCOL.md` |
| Machine constants | `EXACT_PROTOCOL_FROM_APK.json` |
| Runtime | `medical_ble_toolkit/brands/fora/*` |
| Decompiled Java | `extracted/decompiled/sources/com/foracare/tdlink/sm/` (gitignored bulk) |

### Original package

- App: iFORA Smart `com.foracare.tdlink.sm` 1.5.9  
- XAPK: `iFORA+Smart_1.5.9_APKPure.xapk`

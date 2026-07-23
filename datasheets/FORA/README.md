# FORA / iFORA Smart — protocol research + first-party support

## Start here (agents)

| Doc | Role |
|-----|------|
| **`FORA_FIRST_PARTY_PROTOCOL.md`** | **Canonical** — reliability checklist §0, GATT flags, all cmds, FSM, decode, timings |
| **`EXACT_PROTOCOL_FROM_APK.json`** | Machine-readable dump (31 command templates, message codes, getDataType map) |
| `IFORA_SMART_APK_BLE_FINDINGS.md` | **Superseded** — do not implement from this |

## Runtime (toolkit)

| Path | Role |
|------|------|
| `medical_ble_toolkit/brands/fora/protocol.py` | Constants + encode/decode helpers |
| `medical_ble_toolkit/brands/fora/session.py` | Bleak companion-like history session |
| `medical_ble_toolkit/brands/fora/plugin.py` | DevicePlugin |
| `medical_ble_toolkit/parsers/fora.py` | Frame + BG parse |

## Headline (for reliability)

| Item | Value |
|------|--------|
| GATT | Service `1523…`, char `1524…`, write **no-response** |
| Connect | `autoConnect=false`, **TRANSPORT_LE** |
| Scan | **LOW_LATENCY**, reportDelay 0 |
| Frame | `51 \| cmd \| msg \| A3/A5 \| sum` |
| RX rule | start 51 + penultimate **A5** + checksum |
| PIN | **`111111`** (not cloud strings) |
| Name filter | contains `fora|td-|tng|diamond|taidoc|sootheneb` |
| Timings | scan 3s, connect 10s, CCCD+500ms, cmd 5s / set-time 15s, retries 12 |
| Import | set-time → SN1 → SN2 → project → count → records → power-off **0x50** |
| Project→type | `4xxx`=BG, `3xxx`=BP, `32xx`=MP, `8xxx`=SpO2, `2xxx`=WS, `1xxx`=TM, `73`=peakflow |

## Extracted artifacts (local / gitignored bulk)

| Path | Content |
|------|---------|
| `iFORA+Smart_1.5.9_APKPure.xapk` | App package |
| `extracted/decompiled/sources/` | jadx Java (truth for edge cases) |
| `extracted/deep/`, `re_extract/` | Automated mines |
| `tools/` | Portable JDK + jadx |

## Still needs HCI

FORA 6 multiparam packing, confirm record-count on real meters, PIN-over-GATT, WS 34/40 body map.

# Beurer BM54 findings (phone HCI dump)

Canonical write-up lives next to the capture and Beurer PDFs:

**→ [`../datasheets/beurer/BM54_PHONE_HCI_FINDINGS.md`](../datasheets/beurer/BM54_PHONE_HCI_FINDINGS.md)**

| Item | Path |
|------|------|
| Capture | `datasheets/beurer/btsnoop_hci_202607221152.cfa` |
| Readable log | `datasheets/beurer/btsnoop_hci_202607221152.txt` |
| Generic decoder | `phoneblelog/btsnoop_to_text.py` |
| Omron findings (different dump) | `phoneblelog/OMRON_FINDINGS.md` |

### Quick facts

- **MAC:** `0C:7F:ED:72:BC:40` (public), name **`BM54`**, company **`0x0611`**
- **Protocol:** BLP `0x1810` / measurement **Indicate** (19-byte, flags `0x1E`)
- **14 BP indications** after passkey SMP + CCCD `02 00`
- **No** proprietary download command; bond required before CCCD succeeds

Re-decode:

```bash
python phoneblelog/btsnoop_to_text.py datasheets/beurer/btsnoop_hci_202607221152.cfa \
  -o datasheets/beurer/btsnoop_hci_202607221152.txt --skip-num-completed
```

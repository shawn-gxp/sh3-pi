#!/usr/bin/env python3
"""Extract Omron BP meter traffic from BTSnoop HCI capture."""

from __future__ import annotations

import struct
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

BTSNOOP_EPOCH_DELTA_US = 0x00DCDDB30F2F8000
MAC_LE = bytes([0x0A, 0x1C, 0x27, 0x7D, 0x99, 0xE1])
TARGET = "E1:99:7D:27:1C:0A"


def ts_str(ts: int) -> str:
    u = ts - BTSNOOP_EPOCH_DELTA_US
    dt = datetime.fromtimestamp(u // 1_000_000, tz=timezone.utc)
    return dt.strftime("%Y-%m-%d %H:%M:%S.") + f"{u % 1_000_000:06d} UTC"


def bd(b: bytes) -> str:
    return ":".join(f"{x:02X}" for x in reversed(b[:6]))


def parse_ad(data: bytes) -> list[str]:
    out: list[str] = []
    i = 0
    while i < len(data):
        ln = data[i]
        if ln == 0 or i + ln >= len(data):
            break
        t = data[i + 1]
        v = data[i + 2 : i + 1 + ln]
        if t == 0x01 and v:
            out.append(f"Flags=0x{v[0]:02X}")
        elif t in (0x02, 0x03) and len(v) >= 2:
            uids = ",".join(f"{v[j] | (v[j + 1] << 8):04X}" for j in range(0, len(v) - 1, 2))
            kind = "UUID16-incomplete" if t == 0x02 else "UUID16"
            out.append(f"{kind}={uids}")
        elif t == 0x08:
            out.append("ShortName=" + v.decode("utf-8", "replace"))
        elif t == 0x09:
            out.append("CompleteName=" + v.decode("utf-8", "replace"))
        elif t == 0x0A and v:
            out.append(f"TxPower={struct.unpack('b', v[:1])[0]}dBm")
        elif t == 0xFF and len(v) >= 2:
            cid = v[0] | (v[1] << 8)
            company = "Omron" if cid == 0x020E else f"0x{cid:04X}"
            out.append(f"MfgData company={company}(0x{cid:04X}) raw={v[2:].hex()}")
        elif t == 0x16 and len(v) >= 2:
            out.append(f"ServiceData UUID=0x{v[0] | (v[1] << 8):04X} data={v[2:].hex()}")
        else:
            out.append(f"Type0x{t:02X}={v.hex()}")
        i += 1 + ln
    return out


def load_packets(path: Path):
    packets = []
    with path.open("rb") as f:
        header = f.read(16)
        if header[:8] != b"btsnoop\x00":
            raise SystemExit("Not a btsnoop file")
        while True:
            rec = f.read(24)
            if len(rec) < 24:
                break
            _ol, il, flags, _drops, ts = struct.unpack(">IIIIq", rec)
            data = f.read(il)
            if len(data) < il:
                break
            packets.append((ts, flags, data))
    return packets


def main() -> None:
    src = Path("btsnoop_hci_202607151125.cfa")
    out_path = Path("omron_E1997D271C0A_extract.log")
    packets = load_packets(src)
    t0 = packets[0][0]

    lines: list[str] = []
    lines.append("=" * 88)
    lines.append("OMRON DEVICE EXTRACT")
    lines.append(f"Target MAC : {TARGET}")
    lines.append(f"Source     : {src.name}")
    lines.append(f"Capture    : {ts_str(packets[0][0])}  ->  {ts_str(packets[-1][0])}")
    lines.append("=" * 88)
    lines.append("")

    mac_pkts = [(i, ts, flags, data) for i, (ts, flags, data) in enumerate(packets, 1) if MAC_LE in data]

    # Model / name strings
    lines.append("## Identity strings found in capture")
    for i, (ts, flags, data) in enumerate(packets, 1):
        for needle in (b"HEM-", b"BLESmart", b"Omron", b"OMRON"):
            p = data.find(needle)
            if p >= 0:
                s = bytes(b if 32 <= b < 127 else 0x2E for b in data[p : p + 40]).decode()
                lines.append(f"  #{i:05d} {ts_str(ts)}  {s}")
    lines.append("")

    # Unique advertising payloads
    adv_payloads: Counter[str] = Counter()
    rssi_vals: list[int] = []
    for i, ts, flags, data in mac_pkts:
        if not (data[:2] == b"\x04\x3e" and len(data) > 28 and data[3] == 0x0D):
            continue
        rssi = struct.unpack("b", bytes([data[18]]))[0]
        dlen = data[28]
        pdata = data[29 : 29 + dlen]
        rssi_vals.append(rssi)
        adv_payloads[pdata.hex()] += 1

    lines.append("## Advertising / scan-response payloads (decoded)")
    for hx, c in adv_payloads.most_common():
        pdata = bytes.fromhex(hx)
        lines.append(f"  count={c}")
        for item in parse_ad(pdata):
            lines.append(f"    - {item}")
        lines.append(f"    hex: {hx}")
        lines.append("")
    if rssi_vals:
        lines.append(
            f"  RSSI over {len(rssi_vals)} reports: "
            f"min={min(rssi_vals)} dBm  max={max(rssi_vals)} dBm  "
            f"avg={sum(rssi_vals)/len(rssi_vals):.1f} dBm"
        )
    lines.append("")

    # Host actions involving Omron
    lines.append("## Host actions involving this MAC (privacy, whitelist, connect)")
    for i, (ts, flags, data) in enumerate(packets, 1):
        if data[0] != 0x01 or len(data) < 4:
            continue
        opc = data[1] | (data[2] << 8)
        params = data[4:]
        interesting = MAC_LE in data or opc in (0x2043, 0x200D, 0x200E)
        if not interesting:
            continue
        note = f"opcode=0x{opc:04X}"
        if opc == 0x2027 and len(params) >= 39:
            note = (
                f"LE Add Device To Resolving List  "
                f"type={params[0]} addr={bd(params[1:7])}  "
                f"peer_irk={params[7:23].hex()}  local_irk={params[23:39].hex()}"
            )
        elif opc == 0x204E and len(params) >= 8:
            note = f"LE Set Privacy Mode  type={params[0]} addr={bd(params[1:7])} mode={params[7]}"
        elif opc == 0x2011 and len(params) >= 7:
            note = f"LE Add To Filter Accept List  type={params[0]} addr={bd(params[1:7])}"
        elif opc == 0x2012 and len(params) >= 7:
            note = f"LE Remove From Filter Accept List  type={params[0]} addr={bd(params[1:7])}"
        elif opc == 0x2043 and len(params) >= 9:
            note = (
                f"LE Extended Create Connection  "
                f"filter_policy={params[0]} own_type={params[1]} "
                f"peer_type={params[2]} peer={bd(params[3:9])}"
            )
        elif opc == 0x200E:
            note = "LE Create Connection Cancel"
        elif opc == 0xFD57 and MAC_LE in data:
            note = f"Vendor 0xFD57 params={params.hex()}"
        elif MAC_LE not in data and opc not in (0x2043, 0x200D, 0x200E):
            continue
        elif MAC_LE not in data:
            # still log create conn attempts
            pass
        else:
            note = f"CMD 0x{opc:04X} params={params.hex()}"
        # Only print create-conn always; for others require MAC
        if MAC_LE not in data and opc not in (0x2043, 0x200D, 0x200E):
            continue
        lines.append(f"  #{i:05d} +{(ts-t0)/1000:10.1f}ms  {ts_str(ts)}")
        lines.append(f"         TX  {note}")
    lines.append("")

    # Connection completes?
    lines.append("## LE Connection Complete events (all devices in capture)")
    any_conn = False
    for i, (ts, flags, data) in enumerate(packets, 1):
        if data[:2] == b"\x04\x3e" and len(data) > 3 and data[3] in (0x01, 0x0A, 0x29):
            any_conn = True
            sub = data[3]
            if sub == 0x01 and len(data) >= 22:
                status, handle = data[4], data[5] | (data[6] << 8)
                peer = bd(data[9:15])
                lines.append(
                    f"  #{i:05d} {ts_str(ts)} LE Connection Complete "
                    f"status=0x{status:02X} handle=0x{handle:04X} peer={peer}"
                )
            elif sub in (0x0A, 0x29) and len(data) >= 34:
                status, handle = data[4], data[5] | (data[6] << 8)
                peer = bd(data[9:15])
                lines.append(
                    f"  #{i:05d} {ts_str(ts)} LE Enhanced Connection Complete "
                    f"status=0x{status:02X} handle=0x{handle:04X} peer={peer}"
                )
    if not any_conn:
        lines.append("  (none)  -- NO successful BLE connection completed in this capture")
    lines.append("")

    # ACL with markers
    lines.append("## ACL / GATT packets containing Omron markers")
    acl_n = 0
    for i, (ts, flags, data) in enumerate(packets, 1):
        if data[0] != 0x02:
            continue
        if MAC_LE in data or b"HEM-" in data or b"BLESmart" in data:
            acl_n += 1
            lines.append(f"  #{i:05d} {ts_str(ts)} {data.hex()}")
    if acl_n == 0:
        lines.append("  (none)  -- no ACL/GATT payload carried the Omron MAC/name")
    lines.append("")

    # Summary
    cmd_n = sum(1 for _, _, _, d in mac_pkts if d[0] == 1)
    evt_n = sum(1 for _, _, _, d in mac_pkts if d[0] == 4)
    lines.append("## Summary")
    lines.append(f"  Packets containing {TARGET} : {len(mac_pkts)}")
    lines.append(f"    HCI commands : {cmd_n}")
    lines.append(f"    HCI events   : {evt_n}")
    lines.append(f"  First seen     : #{mac_pkts[0][0]} {ts_str(mac_pkts[0][1])}" if mac_pkts else "  n/a")
    lines.append(f"  Last seen      : #{mac_pkts[-1][0]} {ts_str(mac_pkts[-1][1])}" if mac_pkts else "  n/a")
    lines.append("")
    lines.append("## Interpretation")
    lines.append("  1. Device is advertising as an Omron BLE BP meter (company ID 0x020E).")
    lines.append("  2. Local name in scan response embeds the MAC: BLESmart_00000481E1997D271C0A.")
    lines.append("  3. Service UUID 0xFE4A is Omron's custom BLE service (common on HEM-* meters).")
    lines.append("  4. Phone already knew the device: loaded into resolving list + privacy mode at boot.")
    lines.append("  5. OMRON connect attempted filter-accept-list connects (add MAC -> Extended Create Connection")
    lines.append("     with filter_policy enabled / peer 00:00:00:00:00:00 meaning 'use whitelist').")
    lines.append("  6. No LE Connection Complete for this (or any) peer appears in this ~3 min dump.")
    lines.append("     So you mostly captured advertising + failed/pending connect attempts, NOT a full")
    lines.append("     measurement transfer (no ATT/GATT BP records in this file).")
    lines.append("=" * 88)

    # Full chronological non-spam extract: host cmds + first/last ads + connect windows
    lines.append("")
    lines.append("## Chronological Omron-related host/controller events (deduped ads)")
    last_adv_key = None
    adv_run = 0
    for i, ts, flags, data in mac_pkts:
        rel = (ts - t0) / 1000.0
        if data[0] == 0x01:
            opc = data[1] | (data[2] << 8)
            lines.append(f"[{i:05d}] +{rel:10.1f}ms {ts_str(ts)}")
            lines.append(f"        TX CMD 0x{opc:04X} {data.hex()}")
            lines.append("")
            continue
        if data[:2] == b"\x04\xff":
            # vendor event with name/payload
            ascii_s = "".join(chr(b) if 32 <= b < 127 else "." for b in data)
            if "BLESmart" in ascii_s or "HEM" in ascii_s:
                key = data.hex()
                if key != last_adv_key:
                    if adv_run:
                        lines.append(f"        ... repeated similar adv/vendor {adv_run} more times ...")
                        lines.append("")
                    adv_run = 0
                    last_adv_key = key
                    lines.append(f"[{i:05d}] +{rel:10.1f}ms {ts_str(ts)}")
                    lines.append(f"        RX Vendor/Scan  {ascii_s}")
                    lines.append(f"        RAW {data.hex()}")
                    lines.append("")
                else:
                    adv_run += 1
            continue
        if data[:2] == b"\x04\x3e" and len(data) > 28 and data[3] == 0x0D:
            dlen = data[28]
            pdata = data[29 : 29 + dlen]
            rssi = struct.unpack("b", bytes([data[18]]))[0]
            key = pdata.hex()
            if key != last_adv_key:
                if adv_run:
                    lines.append(f"        ... repeated similar adv {adv_run} more times ...")
                    lines.append("")
                adv_run = 0
                last_adv_key = key
                lines.append(f"[{i:05d}] +{rel:10.1f}ms {ts_str(ts)}")
                lines.append(f"        RX ADV/SCAN  {TARGET}  rssi={rssi}dBm")
                for item in parse_ad(pdata):
                    lines.append(f"          {item}")
                lines.append("")
            else:
                adv_run += 1
            continue
        # other
        lines.append(f"[{i:05d}] +{rel:10.1f}ms {ts_str(ts)}")
        lines.append(f"        {data.hex()}")
        lines.append("")

    if adv_run:
        lines.append(f"        ... repeated similar adv {adv_run} more times ...")

    text = "\n".join(lines) + "\n"
    out_path.write_text(text, encoding="utf-8")
    print(f"Wrote {out_path} ({out_path.stat().st_size:,} bytes)")
    print(f"Omron MAC packets: {len(mac_pkts)}")
    # print short console summary
    print("\n".join(lines[:80]))


if __name__ == "__main__":
    main()

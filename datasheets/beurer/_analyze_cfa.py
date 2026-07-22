#!/usr/bin/env python3
"""One-shot analysis of Beurer phone HCI btsnoop capture."""
from __future__ import annotations

import struct
from datetime import datetime, timedelta, timezone
from pathlib import Path

BTSNOOP_EPOCH_DELTA_US = 0x00DCDDB30F2F8000


def ts_to_dt(ts: int) -> datetime:
    unix_us = ts - BTSNOOP_EPOCH_DELTA_US
    return datetime(1970, 1, 1, tzinfo=timezone.utc) + timedelta(microseconds=unix_us)


def parse_packets(path: Path):
    raw = path.read_bytes()
    if raw[:8] != b"btsnoop\x00":
        raise SystemExit(f"not btsnoop: {raw[:8]!r}")
    off = 16
    packets = []
    while off + 24 <= len(raw):
        _orig, incl, flags, _drops, ts = struct.unpack(">IIIIq", raw[off : off + 24])
        off += 24
        data = raw[off : off + incl]
        off += incl
        if len(data) < incl:
            break
        packets.append((ts, flags, data))
    return packets


def main() -> None:
    path = Path(__file__).with_name("btsnoop_hci_202607221152.cfa")
    packets = parse_packets(path)
    print(f"file={path.name} packets={len(packets)}")
    print(f"first={ts_to_dt(packets[0][0])} last={ts_to_dt(packets[-1][0])}")
    dur = (packets[-1][0] - packets[0][0]) / 1e6
    print(f"duration_s={dur:.1f}")

    print("\n=== BM54 string hits ===")
    for i, (ts, _flags, data) in enumerate(packets):
        if b"BM54" in data:
            idx = data.find(b"BM54")
            ctx = data[max(0, idx - 20) : idx + 24]
            print(f"  #{i} {ts_to_dt(ts)} {ctx!r}")

    print("\n=== Successful LE connections ===")
    conn_ok = []
    for i, (ts, _flags, data) in enumerate(packets):
        if not data or data[0] != 0x04 or len(data) < 15:
            continue
        if data[1] != 0x3E:
            continue
        sub = data[3]
        if sub not in (0x01, 0x0A):
            continue
        status = data[4]
        handle = data[5] | (data[6] << 8)
        peer_type = data[8]
        peer = ":".join(f"{b:02X}" for b in reversed(data[9:15]))
        if status == 0:
            conn_ok.append((i, ts_to_dt(ts), handle, peer_type, peer, sub))
            print(
                f"  #{i} {ts_to_dt(ts)} handle=0x{handle:04X} "
                f"addr_type={peer_type} peer={peer} subevt=0x{sub:02X}"
            )
    print(f"success_count={len(conn_ok)}")

    # Disconnects
    print("\n=== Disconnections (HCI evt 0x05) ===")
    for i, (ts, _flags, data) in enumerate(packets):
        if data and data[0] == 0x04 and len(data) >= 7 and data[1] == 0x05:
            handle = data[3] | (data[4] << 8)
            reason = data[5]
            print(f"  #{i} {ts_to_dt(ts)} handle=0x{handle:04X} reason=0x{reason:02X}")

    att_ops: dict[int, int] = {}
    indications = []
    notifications = []
    cccd = []
    write_all = []
    read_rsp = []
    err_rsp = []
    for i, (ts, _flags, data) in enumerate(packets):
        if not data or data[0] != 0x02 or len(data) < 9:
            continue
        cid = data[7] | (data[8] << 8)
        if cid != 0x0004:
            continue
        att = data[9:]
        if not att:
            continue
        op = att[0]
        att_ops[op] = att_ops.get(op, 0) + 1
        if op == 0x1D and len(att) >= 3:
            h = att[1] | (att[2] << 8)
            indications.append((i, ts_to_dt(ts), h, att[3:]))
        elif op == 0x1B and len(att) >= 3:
            h = att[1] | (att[2] << 8)
            notifications.append((i, ts_to_dt(ts), h, att[3:]))
        elif op in (0x12, 0x52) and len(att) >= 3:
            h = att[1] | (att[2] << 8)
            val = att[3:]
            write_all.append((i, ts_to_dt(ts), op, h, val))
            if len(val) == 2 and val in (b"\x01\x00", b"\x02\x00", b"\x00\x00"):
                cccd.append((i, ts_to_dt(ts), op, h, val))
        elif op == 0x0B:  # Read By Type Response
            read_rsp.append((i, ts_to_dt(ts), att))
        elif op == 0x09 and len(att) >= 2:  # Read Blob? no 0x0B is by type
            pass
        elif op == 0x0A and len(att) >= 2:  # Read Response
            read_rsp.append((i, ts_to_dt(ts), att[1:]))
        elif op == 0x01 and len(att) >= 5:  # Error Response
            req = att[1]
            h = att[2] | (att[3] << 8)
            err = att[4]
            err_rsp.append((i, ts_to_dt(ts), req, h, err))

    print("\n=== ATT opcode histogram ===")
    for op, c in sorted(att_ops.items(), key=lambda x: -x[1]):
        print(f"  0x{op:02X}: {c}")

    print(f"\n=== CCCD-like writes ({len(cccd)}) ===")
    for i, t, op, h, val in cccd:
        kind = {b"\x01\x00": "NOTIFY", b"\x02\x00": "INDICATE", b"\x00\x00": "OFF"}[val]
        print(f"  #{i} {t} op=0x{op:02X} handle=0x{h:04X} {kind} {val.hex(' ')}")

    print(f"\n=== Indications ({len(indications)}) ===")
    for i, t, h, pl in indications:
        hx = pl.hex(" ")
        print(f"  #{i} {t} handle=0x{h:04X} len={len(pl)} {hx}")

    print(f"\n=== Notifications sample ({len(notifications)} total, first 15) ===")
    for i, t, h, pl in notifications[:15]:
        print(f"  #{i} {t} handle=0x{h:04X} len={len(pl)} {pl.hex(' ')}")

    print("\n=== ASCII-looking read/write payloads ===")
    for i, t, op, h, val in write_all:
        if any(32 <= b < 127 for b in val) and len(val) >= 3:
            asc = "".join(chr(b) if 32 <= b < 127 else "." for b in val)
            if sum(c.isalpha() for c in asc) >= 2:
                print(f"  W #{i} {t} h=0x{h:04X} {val.hex(' ')} | {asc}")
    for i, t, pl in read_rsp:
        if isinstance(pl, (bytes, bytearray)) and any(32 <= b < 127 for b in pl):
            asc = "".join(chr(b) if 32 <= b < 127 else "." for b in pl)
            if "BM" in asc or sum(c.isalpha() for c in asc) >= 3:
                print(f"  R #{i} {t} {bytes(pl).hex(' ')} | {asc}")

    # Try parse BLP indications
    print("\n=== BLP parse of indications (flags+SFLOAT) ===")
    for i, t, h, pl in indications:
        if len(pl) < 7:
            print(f"  #{i} too short")
            continue
        flags = pl[0]
        sys = pl[1] | (pl[2] << 8)
        dia = pl[3] | (pl[4] << 8)
        # rough SFLOAT decode for display
        def sfloat(raw16: int):
            mant = raw16 & 0x0FFF
            exp = (raw16 >> 12) & 0x0F
            if mant & 0x0800:
                mant -= 0x1000
            if exp & 0x08:
                exp -= 0x10
            special = raw16 & 0x0FFF
            if special in (0x07FF, 0x0800, 0x0801):
                return None
            return mant * (10**exp)

        print(
            f"  #{i} flags=0x{flags:02X} sys_raw=0x{sys:04X}->{sfloat(sys)} "
            f"dia_raw=0x{dia:04X}->{sfloat(dia)} full={pl.hex(' ')}"
        )

    # Adv reports containing BM54 or Beurer CID
    print("\n=== Advertising reports with BM54 or Beurer CID 0x0611 ===")
    adv_hits = 0
    first_adv = None
    last_adv = None
    macs = set()
    for i, (ts, _flags, data) in enumerate(packets):
        if not data or data[0] != 0x04 or data[1] != 0x3E:
            continue
        # LE Advertising Report 0x02 or Extended 0x0D
        blob = data
        if b"BM54" in blob or b"\x11\x06" in blob:
            # extract possible MAC if present in classic report
            if len(data) >= 15 and data[3] == 0x02:
                # event: 04 3E len 02 num ...
                pass
            if b"BM54" in blob:
                adv_hits += 1
                t = ts_to_dt(ts)
                if first_adv is None:
                    first_adv = t
                last_adv = t
                # try find 6-byte address near name (heuristic)
    print(f"  BM54-in-adv-ish packets ~{adv_hits}")
    print(f"  first={first_adv} last={last_adv}")
    if first_adv and last_adv:
        print(f"  adv_span_s={(last_adv - first_adv).total_seconds():.1f}")

    # SMP pairing events in text form via opcodes on L2CAP CID 0x0006
    print("\n=== SMP (CID 0x0006) packets ===")
    smp_n = 0
    for i, (ts, _flags, data) in enumerate(packets):
        if not data or data[0] != 0x02 or len(data) < 10:
            continue
        cid = data[7] | (data[8] << 8)
        if cid != 0x0006:
            continue
        smp = data[9:]
        code = smp[0] if smp else -1
        smp_n += 1
        print(f"  #{i} {ts_to_dt(ts)} code=0x{code:02X} {smp.hex(' ')}")
    print(f"smp_count={smp_n}")

    print("\n=== ATT Error Responses ===")
    for i, t, req, h, err in err_rsp[:30]:
        print(f"  #{i} {t} req=0x{req:02X} handle=0x{h:04X} err=0x{err:02X}")
    print(f"err_total={len(err_rsp)}")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Extract readable strings/arrays from APK resources.arsc + package binary XML layouts
using a best-effort pure Python ARSC string pool reader.
"""
from __future__ import annotations

import argparse
import re
import struct
import zipfile
from pathlib import Path
from typing import List, Tuple


def read_string_pool(data: bytes, pool_off: int) -> List[str]:
    """Parse ResStringPool at pool_off."""
    # header: type(u16), headerSize(u16), size(u32), stringCount, styleCount, flags, stringsStart, stylesStart
    if pool_off + 28 > len(data):
        return []
    typ, header_size, size, string_count, style_count, flags, strings_start, styles_start = struct.unpack_from(
        "<HHIIIIII", data, pool_off
    )
    if typ != 0x0001:  # RES_STRING_POOL_TYPE
        return []
    utf8 = bool(flags & (1 << 8))
    # offsets table
    offsets = []
    o = pool_off + header_size
    for i in range(string_count):
        if o + 4 > len(data):
            break
        offsets.append(struct.unpack_from("<I", data, o)[0])
        o += 4
    strings: List[str] = []
    base = pool_off + strings_start
    for off in offsets:
        p = base + off
        if p >= len(data):
            strings.append("")
            continue
        try:
            if utf8:
                # uleb128 char len, uleb128 byte len
                def uleb(buf, i):
                    r = 0
                    s = 0
                    while True:
                        b = buf[i]
                        i += 1
                        r |= (b & 0x7F) << s
                        if not (b & 0x80):
                            break
                        s += 7
                    return r, i

                _clen, p = uleb(data, p)
                blen, p = uleb(data, p)
                raw = data[p : p + blen]
                strings.append(raw.decode("utf-8", errors="replace"))
            else:
                # utf-16: u16 length (or high bit extended)
                strlen = struct.unpack_from("<H", data, p)[0]
                p += 2
                if strlen & 0x8000:
                    strlen = ((strlen & 0x7FFF) << 16) | struct.unpack_from("<H", data, p)[0]
                    p += 2
                raw = data[p : p + strlen * 2]
                strings.append(raw.decode("utf-16-le", errors="replace"))
        except Exception:
            strings.append("")
    return strings


def extract_arsc_strings(arsc: bytes) -> List[str]:
    # Find all string pools
    found: List[str] = []
    i = 0
    while i + 8 < len(arsc):
        typ, hs, size = struct.unpack_from("<HHI", arsc, i)
        if size < 8 or i + size > len(arsc):
            i += 2
            continue
        if typ == 0x0001:  # string pool
            found.extend(read_string_pool(arsc, i))
        i += size if size >= 8 else 2
    # unique preserve order
    seen = set()
    out = []
    for s in found:
        if s and s not in seen:
            seen.add(s)
            out.append(s)
    return out


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("apk", type=Path)
    ap.add_argument("-o", type=Path, required=True)
    args = ap.parse_args()
    args.o.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(args.apk) as z:
        arsc = z.read("resources.arsc")
        (args.o / "resources.arsc").write_bytes(arsc)
        # extract layouts of interest
        for name in z.namelist():
            if name.startswith("res/layout") and any(
                k in name for k in ("ble", "pair", "import", "meter", "search")
            ):
                dest = args.o / name
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(z.read(name))

    strings = extract_arsc_strings(arsc)
    (args.o / "arsc_all_strings.txt").write_text("\n".join(strings), encoding="utf-8", errors="replace")

    keys = re.compile(
        r"(?i)ble|pair|bond|meter|fora|tng|td-|timeout|second|retry|connect|bluetooth|"
        r"import|device|glucose|ketone|spirom|diamond|premium|serial|pin|password|scan|"
        r"command|record|memory|power|battery|weight|spo2|pressure|thermo"
    )
    hits = [s for s in strings if keys.search(s) and len(s) < 300]
    (args.o / "arsc_interesting_strings.txt").write_text("\n".join(hits), encoding="utf-8", errors="replace")

    # Possible name arrays / short labels
    short = [
        s
        for s in strings
        if re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9 \-_+/]{1,40}", s)
        and re.search(r"(?i)fora|tng|td|diamond|md|gd|ir|p20|p30|p80|spiro|vita|premium|soothe|w550|w600", s)
    ]
    (args.o / "arsc_device_labels.txt").write_text("\n".join(sorted(set(short))), encoding="utf-8")

    print(f"ARSC strings: {len(strings)}, interesting: {len(hits)}, device labels: {len(set(short))}")
    print(f"Wrote {args.o}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""
Lightweight DEX string/type/method miner for APK RE without jadx/Java.

Extracts:
  - all DEX string_ids
  - class descriptors that look package-interesting
  - filtered BLE / UUID / command-related hits

Usage:
  python datasheets/FORA/tools_dex_strings.py datasheets/FORA/extracted/base
"""
from __future__ import annotations

import argparse
import re
import struct
import sys
from pathlib import Path
from typing import Iterable, List, Sequence, Tuple


def _u32(data: bytes, off: int) -> int:
    return struct.unpack_from("<I", data, off)[0]


def _uleb128(data: bytes, off: int) -> Tuple[int, int]:
    result = 0
    shift = 0
    while True:
        b = data[off]
        off += 1
        result |= (b & 0x7F) << shift
        if (b & 0x80) == 0:
            break
        shift += 7
    return result, off


def parse_dex_strings(path: Path) -> List[str]:
    data = path.read_bytes()
    if data[:4] != b"dex\n":
        raise ValueError(f"Not a DEX: {path}")
    string_ids_size = _u32(data, 56)
    string_ids_off = _u32(data, 60)
    strings: List[str] = []
    for i in range(string_ids_size):
        str_data_off = _u32(data, string_ids_off + i * 4)
        # MUTF-8: uleb128 length then bytes
        _n_chars, p = _uleb128(data, str_data_off)
        # read until 0
        end = data.index(b"\x00", p)
        raw = data[p:end]
        try:
            s = raw.decode("utf-8")
        except UnicodeDecodeError:
            s = raw.decode("utf-8", errors="replace")
        strings.append(s)
    return strings


def parse_dex_type_names(path: Path, strings: Sequence[str]) -> List[str]:
    data = path.read_bytes()
    type_ids_size = _u32(data, 64)
    type_ids_off = _u32(data, 68)
    out: List[str] = []
    for i in range(type_ids_size):
        idx = _u32(data, type_ids_off + i * 4)
        if 0 <= idx < len(strings):
            out.append(strings[idx])
    return out


UUID_RE = re.compile(
    r"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
)
SHORT_UUID_RE = re.compile(r"0000[0-9a-fA-F]{4}-0000-1000-8000-00805[fF]9[bB]34[fF][bB]")
HEX_BYTE_CMD_RE = re.compile(r"(?:0x[0-9a-fA-F]{2}|\\x[0-9a-fA-F]{2})")

KEYWORDS = (
    "bluetooth",
    "ble",
    "gatt",
    "uuid",
    "characteristic",
    "service",
    "notify",
    "indicate",
    "descriptor",
    "cccd",
    "pair",
    "bond",
    "connect",
    "disconnect",
    "scan",
    "advertis",
    "unlock",
    "auth",
    "password",
    "passkey",
    "pin",
    "crc",
    "checksum",
    "glucose",
    "ketone",
    "cholesterol",
    "blood",
    "pressure",
    "spo2",
    "meter",
    "fora",
    "foracare",
    "tdlink",
    "tai",
    "write",
    "read",
    "history",
    "record",
    "sync",
    "download",
    "upload",
    "time",
    "clock",
    "serial",
    "mac",
    "device",
    "protocol",
    "command",
    "frame",
    "packet",
    "opcode",
    "header",
    "tail",
    "stx",
    "etx",
    "ack",
    "nack",
    "racp",
    "gm",
    "hts",
    "blp",
    "cgm",
    "weight",
    "temperature",
    "ifora",
    "td-",
    "d40",
    "d42",
    "diamond",
    "premium",
    "connect",
    "nordic",
    "nrf",
    "ti.",
    "cc254",
)


def interesting(s: str) -> bool:
    low = s.lower()
    if UUID_RE.search(s) or SHORT_UUID_RE.search(s):
        return True
    if any(k in low for k in KEYWORDS):
        return True
    # binary-looking command tables
    if re.search(r"^[0-9A-Fa-f]{4,}$", s) and len(s) in (4, 6, 8, 10, 12, 16, 20, 24, 32):
        return True
    return False


def package_interesting(desc: str) -> bool:
    d = desc.lower()
    return any(
        x in d
        for x in (
            "fora",
            "foracare",
            "tdlink",
            "bluetooth",
            "ble",
            "gatt",
            "meter",
            "device",
            "glucose",
            "health",
            "tai",
            "serial",
            "protocol",
            "nordic",
            "nrf",
        )
    )


def main(argv: Sequence[str] | None = None) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("base_dir", type=Path, help="Directory with classes*.dex")
    ap.add_argument("-o", "--out", type=Path, default=None)
    args = ap.parse_args(argv)

    dex_files = sorted(args.base_dir.glob("classes*.dex"))
    if not dex_files:
        print("No classes*.dex found", file=sys.stderr)
        return 1

    all_strings: List[str] = []
    all_types: List[str] = []
    for dex in dex_files:
        print(f"Parsing {dex.name} ...", flush=True)
        strings = parse_dex_strings(dex)
        types = parse_dex_type_names(dex, strings)
        all_strings.extend(strings)
        all_types.extend(types)
        print(f"  strings={len(strings)} types={len(types)}", flush=True)

    uniq = sorted(set(all_strings), key=lambda s: (s.lower(), s))
    hits = [s for s in uniq if interesting(s)]
    uuids = sorted({m.group(0).lower() for s in uniq for m in UUID_RE.finditer(s)})
    pkg_types = sorted({t for t in all_types if package_interesting(t)})

    out = args.out or (args.base_dir.parent / "re_extract")
    out.mkdir(parents=True, exist_ok=True)

    (out / "all_strings.txt").write_text("\n".join(uniq), encoding="utf-8", errors="replace")
    (out / "interesting_strings.txt").write_text("\n".join(hits), encoding="utf-8", errors="replace")
    (out / "uuids.txt").write_text("\n".join(uuids), encoding="utf-8", errors="replace")
    (out / "interesting_types.txt").write_text("\n".join(pkg_types), encoding="utf-8", errors="replace")

    # Package class prefixes
    fora_types = [t for t in pkg_types if "fora" in t.lower() or "tdlink" in t.lower() or "foracare" in t.lower()]
    (out / "fora_types.txt").write_text("\n".join(sorted(set(fora_types))), encoding="utf-8")

    print(f"Wrote {out}")
    print(f"  unique strings: {len(uniq)}")
    print(f"  interesting:    {len(hits)}")
    print(f"  uuids:          {len(uuids)}")
    print(f"  package types:  {len(pkg_types)}")
    print(f"  fora-ish types: {len(set(fora_types))}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

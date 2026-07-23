#!/usr/bin/env python3
"""
Deep DEX mining for FORA/iFORA without jadx:
  - class/field/method inventory for foracare + taidoc packages
  - string xref from method code items (string-id const usage)
  - numeric const pool near BLE/cmd methods
  - static field name dumps for enums
"""
from __future__ import annotations

import argparse
import json
import re
import struct
from collections import defaultdict
from pathlib import Path
from typing import Any, Dict, List, Optional, Sequence, Tuple


def u32(data: bytes, off: int) -> int:
    return struct.unpack_from("<I", data, off)[0]


def u16(data: bytes, off: int) -> int:
    return struct.unpack_from("<H", data, off)[0]


def sleb128(data: bytes, off: int) -> Tuple[int, int]:
    result = 0
    shift = 0
    while True:
        b = data[off]
        off += 1
        result |= (b & 0x7F) << shift
        if shift < 32:
            # keep going
            pass
        if (b & 0x80) == 0:
            if shift < 35 and (b & 0x40):
                result |= -(1 << (shift + 7))
            break
        shift += 7
    # signed decode properly:
    return result, off  # simplified — see uleb for most uses


def uleb128(data: bytes, off: int) -> Tuple[int, int]:
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


def sleb128_real(data: bytes, off: int) -> Tuple[int, int]:
    result = 0
    shift = 0
    while True:
        b = data[off]
        off += 1
        result |= (b & 0x7F) << shift
        shift += 7
        if (b & 0x80) == 0:
            if b & 0x40 and shift < 64:
                result |= - (1 << shift)
            break
    return result, off


class Dex:
    def __init__(self, path: Path):
        self.path = path
        self.data = path.read_bytes()
        if self.data[:4] != b"dex\n":
            raise ValueError(path)
        self.string_ids_size = u32(self.data, 56)
        self.string_ids_off = u32(self.data, 60)
        self.type_ids_size = u32(self.data, 64)
        self.type_ids_off = u32(self.data, 68)
        self.proto_ids_size = u32(self.data, 72)
        self.proto_ids_off = u32(self.data, 76)
        self.field_ids_size = u32(self.data, 80)
        self.field_ids_off = u32(self.data, 84)
        self.method_ids_size = u32(self.data, 88)
        self.method_ids_off = u32(self.data, 92)
        self.class_defs_size = u32(self.data, 96)
        self.class_defs_off = u32(self.data, 100)
        self.strings = self._load_strings()
        self.types = [self.strings[u32(self.data, self.type_ids_off + i * 4)] for i in range(self.type_ids_size)]

    def _load_strings(self) -> List[str]:
        out: List[str] = []
        for i in range(self.string_ids_size):
            off = u32(self.data, self.string_ids_off + i * 4)
            _n, p = uleb128(self.data, off)
            end = self.data.index(b"\x00", p)
            raw = self.data[p:end]
            out.append(raw.decode("utf-8", errors="replace"))
        return out

    def field_name(self, idx: int) -> Tuple[str, str, str]:
        # class_idx, type_idx, name_idx
        base = self.field_ids_off + idx * 8
        c = u16(self.data, base)
        t = u16(self.data, base + 2)
        n = u32(self.data, base + 4)
        return self.types[c], self.types[t], self.strings[n]

    def method_name(self, idx: int) -> Tuple[str, str, str]:
        base = self.method_ids_off + idx * 8
        c = u16(self.data, base)
        p = u16(self.data, base + 2)
        n = u32(self.data, base + 4)
        # proto shorty at proto_ids
        proto_off = self.proto_ids_off + p * 12
        shorty_idx = u32(self.data, proto_off)
        return self.types[c], self.strings[shorty_idx], self.strings[n]

    def interesting_class(self, type_desc: str) -> bool:
        d = type_desc.lower()
        return any(
            x in d
            for x in (
                "foracare",
                "taidoc",
                "tdlink",
                "pclink",
                "meter",
                "ble",
                "bluetooth",
                "uuid",
                "command",
                "import",
                "pair",
                "gatt",
            )
        )

    def parse_classes(self) -> List[Dict[str, Any]]:
        results: List[Dict[str, Any]] = []
        for i in range(self.class_defs_size):
            base = self.class_defs_off + i * 32
            class_idx = u32(self.data, base)
            access = u32(self.data, base + 4)
            superclass_idx = u32(self.data, base + 8)
            interfaces_off = u32(self.data, base + 12)
            source_file_idx = u32(self.data, base + 16)
            annotations_off = u32(self.data, base + 20)
            class_data_off = u32(self.data, base + 24)
            static_values_off = u32(self.data, base + 28)
            type_desc = self.types[class_idx]
            if not self.interesting_class(type_desc):
                continue
            cls: Dict[str, Any] = {
                "type": type_desc,
                "access": access,
                "source": self.strings[source_file_idx] if source_file_idx != 0xFFFFFFFF else None,
                "fields": [],
                "methods": [],
                "static_values": None,
            }
            if class_data_off:
                self._parse_class_data(class_data_off, cls)
            if static_values_off:
                cls["static_values"] = self._parse_encoded_array(static_values_off)
            results.append(cls)
        return results

    def _parse_class_data(self, off: int, cls: Dict[str, Any]) -> None:
        data = self.data
        static_fields_size, off = uleb128(data, off)
        instance_fields_size, off = uleb128(data, off)
        direct_methods_size, off = uleb128(data, off)
        virtual_methods_size, off = uleb128(data, off)

        field_idx = 0
        for _ in range(static_fields_size):
            diff, off = uleb128(data, off)
            access, off = uleb128(data, off)
            field_idx += diff
            c, t, n = self.field_name(field_idx)
            cls["fields"].append({"kind": "static", "class": c, "type": t, "name": n, "access": access})

        field_idx = 0
        for _ in range(instance_fields_size):
            diff, off = uleb128(data, off)
            access, off = uleb128(data, off)
            field_idx += diff
            c, t, n = self.field_name(field_idx)
            cls["fields"].append({"kind": "instance", "class": c, "type": t, "name": n, "access": access})

        method_idx = 0
        for _ in range(direct_methods_size):
            diff, off = uleb128(data, off)
            access, off = uleb128(data, off)
            code_off, off = uleb128(data, off)
            method_idx += diff
            c, shorty, n = self.method_name(method_idx)
            minfo = {
                "kind": "direct",
                "class": c,
                "name": n,
                "shorty": shorty,
                "access": access,
                "code_off": code_off,
                "string_refs": [],
                "int_consts": [],
                "method_refs": [],
                "field_refs": [],
            }
            if code_off:
                self._scan_code(code_off, minfo)
            cls["methods"].append(minfo)

        method_idx = 0
        for _ in range(virtual_methods_size):
            diff, off = uleb128(data, off)
            access, off = uleb128(data, off)
            code_off, off = uleb128(data, off)
            method_idx += diff
            c, shorty, n = self.method_name(method_idx)
            minfo = {
                "kind": "virtual",
                "class": c,
                "name": n,
                "shorty": shorty,
                "access": access,
                "code_off": code_off,
                "string_refs": [],
                "int_consts": [],
                "method_refs": [],
                "field_refs": [],
            }
            if code_off:
                self._scan_code(code_off, minfo)
            cls["methods"].append(minfo)

    def _scan_code(self, code_off: int, minfo: Dict[str, Any]) -> None:
        """Scan Dalvik insns for const-string, const/*, invoke, sget/sput."""
        data = self.data
        registers_size = u16(data, code_off)
        ins_size = u16(data, code_off + 2)
        outs_size = u16(data, code_off + 4)
        tries_size = u16(data, code_off + 6)
        debug_info_off = u32(data, code_off + 8)
        insns_size = u32(data, code_off + 12)
        insns_off = code_off + 16
        # insns are u16 units
        end = insns_off + insns_size * 2
        p = insns_off
        strings: List[str] = []
        ints: List[int] = []
        mrefs: List[str] = []
        frefs: List[str] = []
        while p + 2 <= end:
            op = data[p]
            # const/4
            if op == 0x12:
                nibble = data[p + 1]
                val = (nibble >> 4) & 0x0F
                if val & 0x8:
                    val -= 16
                ints.append(val)
                p += 2
                continue
            # const/16
            if op == 0x13:
                val = struct.unpack_from("<h", data, p + 2)[0]
                ints.append(val)
                p += 4
                continue
            # const
            if op == 0x14:
                val = struct.unpack_from("<i", data, p + 2)[0]
                ints.append(val)
                p += 6
                continue
            # const/high16
            if op == 0x15:
                val = struct.unpack_from("<h", data, p + 2)[0] << 16
                ints.append(val)
                p += 4
                continue
            # const-wide/16
            if op == 0x16:
                val = struct.unpack_from("<h", data, p + 2)[0]
                ints.append(val)
                p += 4
                continue
            # const-wide/32
            if op == 0x17:
                val = struct.unpack_from("<i", data, p + 2)[0]
                ints.append(val)
                p += 6
                continue
            # const-wide
            if op == 0x18:
                val = struct.unpack_from("<q", data, p + 2)[0]
                ints.append(int(val))
                p += 10
                continue
            # const-wide/high16
            if op == 0x19:
                val = struct.unpack_from("<h", data, p + 2)[0] << 48
                ints.append(int(val))
                p += 4
                continue
            # const-string
            if op == 0x1A:
                sidx = u16(data, p + 2)
                if sidx < len(self.strings):
                    strings.append(self.strings[sidx])
                p += 4
                continue
            # const-string/jumbo
            if op == 0x1B:
                sidx = u32(data, p + 2)
                if sidx < len(self.strings):
                    strings.append(self.strings[sidx])
                p += 6
                continue
            # sget / sput family 0x60-0x6d — field ref at +2
            if 0x60 <= op <= 0x6D:
                fidx = u16(data, p + 2)
                try:
                    c, t, n = self.field_name(fidx)
                    frefs.append(f"{c}->{n}:{t}")
                except Exception:
                    pass
                p += 4
                continue
            # invoke-* 0x6e-0x72, 0x74-0x78
            if op in (0x6E, 0x6F, 0x70, 0x71, 0x72, 0x74, 0x75, 0x76, 0x77, 0x78):
                midx = u16(data, p + 2)
                try:
                    c, shorty, n = self.method_name(midx)
                    mrefs.append(f"{c}->{n}{shorty}")
                except Exception:
                    pass
                # format 35c is 6 bytes, 3rc is 6 bytes
                p += 6
                continue
            # packed-switch / sparse-switch / fill-array-data payloads handled by skipping via length
            # default: use simple size table for common formats
            size = _insn_size_bytes(op, data, p, end)
            if size <= 0:
                p += 2
            else:
                p += size
        minfo["string_refs"] = strings
        # unique ints of interest
        minfo["int_consts"] = sorted({i for i in ints if -1 <= i <= 0xFFFF or i in (3000, 5000, 10000, 15000, 20000, 30000, 60000)})
        minfo["all_int_consts"] = sorted(set(ints))[:200]
        minfo["method_refs"] = mrefs[:80]
        minfo["field_refs"] = frefs[:80]
        minfo["registers"] = registers_size
        minfo["insns_units"] = insns_size

    def _parse_encoded_array(self, off: int) -> Any:
        size, off = uleb128(self.data, off)
        values = []
        for _ in range(size):
            v, off = self._parse_encoded_value(off)
            values.append(v)
        return values

    def _parse_encoded_value(self, off: int) -> Tuple[Any, int]:
        data = self.data
        header = data[off]
        off += 1
        value_arg = header >> 5
        value_type = header & 0x1F
        # VALUE_BYTE=0x00 ... VALUE_INT=0x04, VALUE_STRING=0x17, VALUE_TYPE=0x18, VALUE_BOOLEAN=0x1f
        if value_type == 0x1F:  # boolean
            return (value_arg != 0), off
        if value_type == 0x1E:  # null
            return None, off
        if value_type == 0x00:  # byte
            return struct.unpack_from("<b", data, off)[0], off + 1
        size = value_arg + 1
        raw = data[off : off + size]
        off += size
        if value_type in (0x02, 0x03, 0x04, 0x06):  # short, char, int, long — little endian signed extend
            val = int.from_bytes(raw, "little", signed=True)
            return val, off
        if value_type == 0x17:  # string
            idx = int.from_bytes(raw, "little")
            return self.strings[idx] if idx < len(self.strings) else idx, off
        if value_type == 0x18:  # type
            idx = int.from_bytes(raw, "little")
            return self.types[idx] if idx < len(self.types) else idx, off
        if value_type == 0x1C:  # array
            # nested — value_arg unused; re-parse at previous? Spec says encoded_array follows
            # Actually for array type, the value is immediately an encoded_array
            # We already consumed header; need to parse array at current off-1? 
            # Restart: for VALUE_ARRAY, no value bytes after header — encoded_array follows
            # Fix: we shouldn't have advanced size. Re-read properly next time.
            arr, noff = self._parse_encoded_array_from(off - size if False else off)
            # Since we wrongly read size bytes, this is messy for nested arrays.
            # For our use, skip complex nesting.
            return f"<array@{off}>", off
        return f"<type={value_type:02x} raw={raw.hex()}>", off

    def _parse_encoded_array_from(self, off: int) -> Tuple[Any, int]:
        return self._parse_encoded_array(off), off


def _insn_size_bytes(op: int, data: bytes, p: int, end: int) -> int:
    """Return instruction size in bytes; conservative."""
    # 10x: 2 bytes
    if op in (0x00, 0x01, 0x04, 0x07, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11,
              0x1C, 0x1D, 0x1E, 0x1F, 0x21, 0x27, 0x28, 0x3E, 0x3F, 0x40, 0x41, 0x42, 0x43,
              0x73, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F, 0x80, 0x81, 0x82, 0x83, 0x84,
              0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F, 0xB0, 0xB1,
              0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE,
              0xBF, 0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB,
              0xCC, 0xCD, 0xCE, 0xCF):
        return 2
    # many 4-byte
    if op in (0x02, 0x05, 0x08, 0x13, 0x15, 0x16, 0x19, 0x1A, 0x1C, 0x1F, 0x20, 0x22, 0x23,
              0x29, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38,
              0x39, 0x3A, 0x3B, 0x3C, 0x3D):
        return 4
    if 0x44 <= op <= 0x6D:
        return 4
    if 0x90 <= op <= 0xAF:
        return 4
    if 0xD0 <= op <= 0xE2:
        return 4
    # 6-byte
    if op in (0x03, 0x06, 0x09, 0x14, 0x17, 0x1B, 0x24, 0x25, 0x26, 0x2A, 0x2B, 0x2C):
        return 6
    if 0x6E <= op <= 0x72 or 0x74 <= op <= 0x78:
        return 6
    # 10-byte const-wide
    if op == 0x18:
        return 10
    # nop / unknown
    if op == 0x00:
        return 2
    # payload pseudo-ops after switch — if we land on 0x00 0x01 packed-switch payload
    return 2


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("base_dir", type=Path)
    ap.add_argument("-o", "--out", type=Path, required=True)
    args = ap.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)

    all_classes: List[Dict[str, Any]] = []
    for dex_path in sorted(args.base_dir.glob("classes*.dex")):
        print(f"Deep parse {dex_path.name} ...", flush=True)
        dex = Dex(dex_path)
        classes = dex.parse_classes()
        print(f"  interesting classes: {len(classes)}", flush=True)
        all_classes.extend(classes)

    # Write full dump
    (args.out / "classes_deep.json").write_text(
        json.dumps(all_classes, indent=2, ensure_ascii=False), encoding="utf-8"
    )

    # Summaries
    timings: Dict[str, List[Any]] = defaultdict(list)
    cmd_methods: List[Dict[str, Any]] = []
    pair_methods: List[Dict[str, Any]] = []
    enum_fields: Dict[str, List[str]] = defaultdict(list)
    string_in_methods: Dict[str, List[str]] = defaultdict(list)

    TIME_KEYS = re.compile(
        r"(?i)timeout|delay|interval|retry|sleep|wait|duration|millis|seconds|cooldown|settle|quiet|poll|timer|TTL|expire"
    )
    CMD_KEYS = re.compile(r"(?i)cmd|command|checksum|frame|record|serial|project|power|datetime|ble|gatt|write|notify|pair|bond|scan|import|meter")

    for cls in all_classes:
        t = cls["type"]
        for f in cls.get("fields", []):
            if "Enum" in t or f["name"].isupper() or f["name"].startswith("CMD") or f["name"].startswith("E_"):
                enum_fields[t].append(f"{f['kind']}:{f['name']}:{f['type']}")
        for m in cls.get("methods", []):
            name = m["name"]
            key = f"{t}->{name}"
            if m.get("string_refs"):
                string_in_methods[key] = m["string_refs"]
            ints = m.get("all_int_consts") or m.get("int_consts") or []
            if TIME_KEYS.search(name) or any(TIME_KEYS.search(s) for s in m.get("string_refs", [])):
                timings[key] = ints
            if CMD_KEYS.search(name) or CMD_KEYS.search(t):
                cmd_methods.append(
                    {
                        "method": key,
                        "strings": m.get("string_refs", [])[:40],
                        "ints": ints[:80],
                        "calls": m.get("method_refs", [])[:40],
                    }
                )
            if re.search(r"(?i)pair|bond|scan|connect|gatt|import|notify|descriptor", name) or re.search(
                r"(?i)pair|bond|MeterConn|BLEUtil|UUIDUtil", t
            ):
                pair_methods.append(
                    {
                        "method": key,
                        "strings": m.get("string_refs", [])[:50],
                        "ints": ints[:100],
                        "calls": m.get("method_refs", [])[:50],
                        "fields": m.get("field_refs", [])[:40],
                    }
                )

    (args.out / "timings_candidates.json").write_text(
        json.dumps(timings, indent=2), encoding="utf-8"
    )
    (args.out / "cmd_methods.json").write_text(
        json.dumps(cmd_methods, indent=2), encoding="utf-8"
    )
    (args.out / "pair_connect_methods.json").write_text(
        json.dumps(pair_methods, indent=2), encoding="utf-8"
    )
    (args.out / "enum_fields.json").write_text(
        json.dumps(enum_fields, indent=2), encoding="utf-8"
    )

    # Flatten all ints that look like timeouts (ms)
    timeout_ms = sorted(
        {
            i
            for vals in timings.values()
            for i in vals
            if isinstance(i, int) and 100 <= i <= 120000
        }
    )
    (args.out / "timeout_ms_values.json").write_text(
        json.dumps(timeout_ms, indent=2), encoding="utf-8"
    )

    # Methods that reference CMD_ or E_Meter strings
    cmd_string_hits = {
        k: v
        for k, v in string_in_methods.items()
        if any(re.search(r"(?i)CMD_|E_Meter|0x5|timeout|pair|bond|1523|1524|checksum|record", s) for s in v)
    }
    (args.out / "methods_with_protocol_strings.json").write_text(
        json.dumps(cmd_string_hits, indent=2), encoding="utf-8"
    )

    print(f"Wrote {args.out}")
    print(f"  classes: {len(all_classes)}")
    print(f"  cmd methods: {len(cmd_methods)}")
    print(f"  pair/connect methods: {len(pair_methods)}")
    print(f"  timeout candidates ms: {timeout_ms[:40]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

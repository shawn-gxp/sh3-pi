#!/usr/bin/env python3
"""Extract exact BLE protocol data from decompiled Nipro BLELib C# sources."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SRC = ROOT / "decompiled_cs" / "BLELib"
OUT_JSON = ROOT / "EXACT_PROTOCOL.json"
OUT_MD = ROOT / "EXACT_PROTOCOL.md"

DEVICE_FILES = {
    "CFL": SRC / "BLELib.BLEDevice" / "BLEDeviceCFL.cs",
    "MightySat": SRC / "BLELib.BLEDevice" / "BLEDeviceMightySat.cs",
    "NBCM": SRC / "BLELib.BLEDevice" / "BLEDeviceNBCM.cs",
    "NBP1": SRC / "BLELib.BLEDevice" / "BLEDeviceNBP1.cs",
    "NSM1": SRC / "BLELib.BLEDevice" / "BLEDeviceNSM1.cs",
    "NT100B": SRC / "BLELib.BLEDevice" / "BLEDeviceNT100B.cs",
    "UM212": SRC / "BLELib.BLEDevice" / "BLEDeviceUM212.cs",
}


def read(p: Path) -> str:
    return p.read_text(encoding="utf-8", errors="replace")


def find_device_name(text: str) -> str | None:
    m = re.search(r'DeviceName\s*=>\s*"([^"]+)"', text)
    return m.group(1) if m else None


def find_string_consts(text: str) -> dict[str, str]:
    out = {}
    for m in re.finditer(
        r'(?:public|private|protected|internal)?\s*(?:static\s+)?(?:readonly\s+)?'
        r'const\s+string\s+(\w+)\s*=\s*"([^"]+)"',
        text,
    ):
        out[m.group(1)] = m.group(2)
    return out


def find_byte_consts(text: str) -> dict[str, int]:
    out = {}
    for m in re.finditer(
        r'(?:public|private|protected)?\s*(?:static\s+)?(?:readonly\s+)?'
        r'const\s+byte\s+(\w+)\s*=\s*(\d+)',
        text,
    ):
        out[m.group(1)] = int(m.group(2))
    return out


def find_int_consts(text: str) -> dict[str, int]:
    out = {}
    for m in re.finditer(
        r'(?:private|public|protected)?\s*static\s+readonly\s+int\s+(\w+)\s*=\s*(\d+)',
        text,
    ):
        out[m.group(1)] = int(m.group(2))
    for m in re.finditer(r'const\s+int\s+(\w+)\s*=\s*(\d+)', text):
        out[m.group(1)] = int(m.group(2))
    return out


def find_byte_arrays(text: str) -> list[dict]:
    """Find `static readonly byte[] NAME = new byte[N] { ... }` and inline new byte[] { }."""
    results = []
    for m in re.finditer(
        r'(?:public|private|protected)?\s*static\s+readonly\s+byte\[\]\s+(\w+)\s*=\s*'
        r'new\s+byte\[(\d*)\]\s*\{([^}]*)\}',
        text,
    ):
        name = m.group(1)
        body = m.group(3)
        vals = parse_byte_list(body)
        results.append({"name": name, "bytes": vals, "hex": to_hex(vals)})
    return results


def parse_byte_list(body: str) -> list[int]:
    vals: list[int] = []
    body = body.strip()
    if not body:
        return vals
    for part in body.split(","):
        part = part.strip()
        if not part:
            continue
        # skip non-literals like bytes[0]
        if re.fullmatch(r"\d+", part):
            vals.append(int(part) & 0xFF)
        elif re.fullmatch(r"0x[0-9A-Fa-f]+", part):
            vals.append(int(part, 16) & 0xFF)
    return vals


def to_hex(vals: list[int]) -> str:
    return " ".join(f"{b:02X}" for b in vals)


def find_guid_parses(text: str) -> list[str]:
    return sorted(set(re.findall(r'Guid\.Parse\("([0-9A-Fa-f-]{36})"\)', text, re.I)))


def find_delays(text: str) -> list[int]:
    return sorted({int(x) for x in re.findall(r"Task\.Delay\((\d+)\)", text)})


def find_write_payloads(text: str) -> list[dict]:
    """Capture nearby context for WriteAsync with new byte[] literals."""
    results = []
    # new byte[N] { a, b, c } near WriteAsync within ~15 lines
    lines = text.splitlines()
    for i, line in enumerate(lines):
        if "WriteAsync" not in line and "new byte[" not in line:
            continue
        window = "\n".join(lines[max(0, i - 8) : min(len(lines), i + 3)])
        if "WriteAsync" not in window:
            continue
        for m in re.finditer(r"new byte\[(\d*)\]\s*\{([^}]*)\}", window):
            vals = parse_byte_list(m.group(2))
            if not vals:
                continue
            # method name heuristic
            method = ""
            for j in range(i, max(-1, i - 40), -1):
                mm = re.search(r"(?:async\s+)?(?:Task|void|bool|string|object|IList)[^{]*\s+(\w+)\s*\(", lines[j])
                if mm:
                    method = mm.group(1)
                    break
            results.append(
                {
                    "method": method,
                    "bytes": vals,
                    "hex": to_hex(vals),
                    "line": i + 1,
                    "context": lines[i].strip()[:160],
                }
            )
    # dedupe by hex+method
    seen = set()
    uniq = []
    for r in results:
        k = (r["method"], r["hex"])
        if k in seen:
            continue
        seen.add(k)
        uniq.append(r)
    return uniq


def find_uuid_dicts(text: str) -> dict[str, dict[str, str]]:
    """Parse Dictionary string,string UUID tables like _UuidsS."""
    out: dict[str, dict[str, str]] = {}
    for m in re.finditer(
        r'(?:private|public)?\s*static\s+readonly\s+Dictionary<string,\s*string>\s+(\w+)\s*=\s*'
        r'new Dictionary<string,\s*string>\s*\{(.*?)\};',
        text,
        re.S,
    ):
        name = m.group(1)
        body = m.group(2)
        pairs = re.findall(r'\{\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\}', body)
        out[name] = {k: v for k, v in pairs}
    return out


def extract_device(key: str, path: Path) -> dict:
    text = read(path)
    return {
        "class_file": str(path.relative_to(ROOT)),
        "device_name_match": find_device_name(text),
        "string_constants": find_string_consts(text),
        "byte_constants": find_byte_consts(text),
        "int_constants": find_int_consts(text),
        "static_byte_arrays": find_byte_arrays(text),
        "guid_parses": find_guid_parses(text),
        "task_delays_ms": find_delays(text),
        "write_payloads": find_write_payloads(text),
        "uuid_dictionaries": find_uuid_dicts(text),
    }


def make_md(data: dict) -> str:
    lines = [
        "# Nipro BLELib — exact protocol extract",
        "",
        "Auto-extracted from decompiled C# (ilspycmd 10.x).",
        "",
    ]
    for key, d in data["devices"].items():
        lines.append(f"## {key} — match `{d['device_name_match']}`")
        lines.append("")
        lines.append(f"Source: `{d['class_file']}`")
        lines.append("")
        if d["int_constants"]:
            lines.append("### Timeouts / ints")
            lines.append("")
            for k, v in d["int_constants"].items():
                lines.append(f"- `{k}` = **{v}**")
            lines.append("")
        if d["task_delays_ms"]:
            lines.append(f"### Task.Delay values (ms): {d['task_delays_ms']}")
            lines.append("")
        if d["string_constants"]:
            lines.append("### String / UUID constants")
            lines.append("")
            lines.append("| Name | Value |")
            lines.append("|------|-------|")
            for k, v in d["string_constants"].items():
                lines.append(f"| `{k}` | `{v}` |")
            lines.append("")
        if d["uuid_dictionaries"]:
            lines.append("### UUID dictionaries")
            lines.append("")
            for dname, pairs in d["uuid_dictionaries"].items():
                lines.append(f"**{dname}**")
                lines.append("")
                lines.append("| Label | UUID |")
                lines.append("|-------|------|")
                for k, v in pairs.items():
                    lines.append(f"| {k} | `{v}` |")
                lines.append("")
        if d["byte_constants"]:
            lines.append("### Byte command constants")
            lines.append("")
            lines.append("| Name | Dec | Hex |")
            lines.append("|------|-----|-----|")
            for k, v in d["byte_constants"].items():
                lines.append(f"| `{k}` | {v} | `0x{v:02X}` |")
            lines.append("")
        if d["static_byte_arrays"]:
            lines.append("### Static command byte arrays")
            lines.append("")
            lines.append("| Name | Hex |")
            lines.append("|------|-----|")
            for a in d["static_byte_arrays"]:
                lines.append(f"| `{a['name']}` | `{a['hex']}` |")
            lines.append("")
        if d["write_payloads"]:
            lines.append("### WriteAsync payloads (literal bytes near write)")
            lines.append("")
            lines.append("| Method | Hex | Line |")
            lines.append("|--------|-----|------|")
            for w in d["write_payloads"]:
                lines.append(f"| `{w['method']}` | `{w['hex']}` | {w['line']} |")
            lines.append("")
        if d["guid_parses"]:
            lines.append("### Guid.Parse UUIDs used in code")
            lines.append("")
            for g in d["guid_parses"]:
                lines.append(f"- `{g}`")
            lines.append("")
        lines.append("---")
        lines.append("")
    return "\n".join(lines)


def main() -> None:
    devices = {}
    for key, path in DEVICE_FILES.items():
        if not path.exists():
            print(f"MISSING {path}")
            continue
        devices[key] = extract_device(key, path)
        print(f"OK {key}: name={devices[key]['device_name_match']} writes={len(devices[key]['write_payloads'])}")

    # BLELib scan defaults
    blelib = read(SRC / "BLELib" / "BLELib.cs")
    gatt = read(SRC / "BLELib.Common" / "GattServiceConstants.cs")
    nhl_const_path = ROOT / "decompiled_cs" / "NHL" / "NHL.ViewModels.Utils" / "Constants.cs"
    nhl = read(nhl_const_path) if nhl_const_path.exists() else ""

    data = {
        "meta": {
            "source": "decompiled_cs from BLELib.dll / NHL.dll",
            "ilspycmd": "10.1.1",
        },
        "global": {
            "default_scan_mode": 2,
            "nhl_name_prefixes": find_string_consts(nhl),
            "gatt_shared_constants": find_string_consts(gatt),
            "blelib_scan_timeout_is_caller_arg": True,
        },
        "devices": devices,
    }
    OUT_JSON.write_text(json.dumps(data, indent=2), encoding="utf-8")
    OUT_MD.write_text(make_md(data), encoding="utf-8")
    print(f"Wrote {OUT_JSON}")
    print(f"Wrote {OUT_MD}")


if __name__ == "__main__":
    main()

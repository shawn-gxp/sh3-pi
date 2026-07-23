#!/usr/bin/env python3
"""Summarize deep DEX dump into a compact first-party protocol brief."""
from __future__ import annotations

import json
import re
from collections import defaultdict
from pathlib import Path


def main() -> int:
    deep = Path("datasheets/FORA/extracted/deep")
    classes = json.loads((deep / "classes_deep.json").read_text(encoding="utf-8"))
    out = Path("datasheets/FORA/extracted/deep/summary")
    out.mkdir(parents=True, exist_ok=True)

    def find(sub: str):
        return [c for c in classes if sub in c["type"]]

    report: dict = {"enums": {}, "key_methods": {}, "timeouts": {}, "scan_filters": []}

    # Enums of interest
    for enum_name in (
        "LibraryEnum$CmdType",
        "LibraryEnum$MeterModel",
        "LibraryEnum$E_MeterStatus",
        "LibraryEnum$MeasurementType",
        "LibraryEnum$BloodGlucoseType",
        "LibraryEnum$MeterUsers",
        "LibraryEnum$IHB",
        "LibraryEnum$ValueState",
        "LibraryEnum$GenderType",
        "LibraryEnum$ObjectType",
        "LibraryEnum$ABPMWriteType",
        "ImportState",
    ):
        for c in find(enum_name):
            for m in c.get("methods", []):
                if m["name"] == "<clinit>":
                    report["enums"][c["type"]] = {
                        "strings": m.get("string_refs", []),
                        "ints": m.get("all_int_consts") or m.get("int_consts") or [],
                        "fields": [f["name"] for f in c.get("fields", [])],
                    }

    # Key classes/methods
    key_types = (
        "iMeterConnService;",
        "iMeterConnService$",
        "MeterPairUtils",
        "MeterCommand",
        "LibraryConstant",
        "BLEUtil",
        "UUIDUtil",
        "ByteUtils",
        "ImportMeterService",
        "ImportService;",
        "BloodGlucoseRecord",
        "BloodPressureRecord",
        "AbstractRecord",
        "CLSUUID",
        "BlePair;",
    )
    for c in classes:
        t = c["type"]
        if not any(k in t for k in key_types):
            continue
        if "foracare" not in t and "taidoc" not in t:
            continue
        methods = []
        for m in c.get("methods", []):
            if m["name"].startswith("access$") or m["name"].startswith("$r8"):
                continue
            methods.append(
                {
                    "name": m["name"],
                    "strings": m.get("string_refs", []),
                    "ints": m.get("all_int_consts") or m.get("int_consts") or [],
                    "calls": [
                        x
                        for x in (m.get("method_refs") or [])
                        if "foracare" in x or "taidoc" in x or "Bluetooth" in x or "Timer" in x
                    ][:30],
                    "fields": (m.get("field_refs") or [])[:20],
                }
            )
        report["key_methods"][t] = {
            "source": c.get("source"),
            "fields": [
                {"kind": f["kind"], "name": f["name"], "type": f["type"]}
                for f in c.get("fields", [])
            ],
            "static_values": c.get("static_values"),
            "methods": methods,
        }

    # Collect timeouts from foracare methods
    timeout_hits = []
    for c in classes:
        if "foracare" not in c["type"]:
            continue
        for m in c.get("methods", []):
            ints = m.get("all_int_consts") or []
            name = m["name"]
            strs = m.get("string_refs") or []
            interesting_ints = [i for i in ints if isinstance(i, int) and (100 <= i <= 300000 or i in (0x51, 0xA3, 0xA5, 133))]
            if interesting_ints and (
                re.search(r"(?i)time|delay|retry|wait|sleep|scan|connect|import|pair|timer|timeout|schedule|handler|msg", name)
                or any(re.search(r"(?i)time|delay|retry|over.?time|timeout|second|ms", s) for s in strs)
            ):
                timeout_hits.append(
                    {
                        "method": f"{c['type']}->{name}",
                        "ints": interesting_ints,
                        "strings": strs,
                    }
                )
    report["timeouts"] = timeout_hits

    # Scan filter strings from checkMeterSeries etc.
    for c in classes:
        for m in c.get("methods", []):
            if m["name"] in ("checkMeterSeries", "startSearch", "onScanResult") or "Scan" in m["name"]:
                if "foracare" in c["type"]:
                    report["scan_filters"].append(
                        {
                            "method": f"{c['type']}->{m['name']}",
                            "strings": m.get("string_refs", []),
                            "ints": m.get("all_int_consts") or [],
                        }
                    )

    (out / "first_party_summary.json").write_text(
        json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8"
    )

    # Human readable brief
    lines = ["# Deep DEX summary (auto)", ""]
    lines.append("## Enums")
    for k, v in report["enums"].items():
        lines.append(f"### {k}")
        lines.append("values: " + ", ".join(v.get("strings") or []))
        lines.append("ints: " + ", ".join(str(i) for i in (v.get("ints") or [])[:40]))
        lines.append("")

    lines.append("## Timeouts / delays (method → ints)")
    for h in timeout_hits:
        lines.append(f"- `{h['method']}`: {h['ints']} | {h['strings'][:8]}")
    lines.append("")

    lines.append("## Scan / series filters")
    for h in report["scan_filters"]:
        lines.append(f"- `{h['method']}`: strings={h['strings']} ints={h['ints']}")
    lines.append("")

    # iMeterConnService methods of interest
    lines.append("## iMeterConnService critical methods")
    for t, info in report["key_methods"].items():
        if "iMeterConnService;" not in t and "iMeterConnService$" not in t:
            continue
        lines.append(f"### {t}")
        for m in info["methods"]:
            if m["name"] in (
                "connect",
                "checkMeterSeries",
                "dealServicesDiscovered",
                "dataReceived",
                "checkBleEnable",
                "startScan",
                "stopScan",
                "onStartCommand",
                "returnRun$lambda$12",
                "gotoImportActivity",
            ) or re.search(r"(?i)connect|scan|import|pair|discover|notify|write|timeout|handler", m["name"]):
                lines.append(f"- **{m['name']}**")
                if m["strings"]:
                    lines.append(f"  - strings: {m['strings']}")
                if m["ints"]:
                    lines.append(f"  - ints: {m['ints']}")
                if m["calls"]:
                    lines.append(f"  - calls: {m['calls'][:15]}")
        lines.append("")

    # MeterPairUtils / BLEUtil / UUIDUtil
    for key in ("MeterPairUtils", "BLEUtil", "UUIDUtil", "MeterCommand", "LibraryConstant", "ByteUtils"):
        lines.append(f"## {key}")
        for t, info in report["key_methods"].items():
            if key not in t:
                continue
            lines.append(f"### {t} fields")
            for f in info["fields"]:
                lines.append(f"- {f['kind']} {f['name']}: {f['type']}")
            for m in info["methods"]:
                if m["name"] in ("<clinit>", "<init>") or m["strings"] or any(
                    100 <= i <= 300000 for i in m["ints"] if isinstance(i, int)
                ):
                    lines.append(f"- **{m['name']}** strings={m['strings'][:20]} ints={m['ints'][:30]}")
        lines.append("")

    # Record parsers
    lines.append("## Record classes")
    for t, info in report["key_methods"].items():
        if "record" not in t.lower() and "Record" not in t:
            continue
        lines.append(f"### {t}")
        for m in info["methods"]:
            if re.search(r"(?i)parse|decode|from|init|getValue|getDate|getGlucose|getSystolic", m["name"]):
                lines.append(f"- {m['name']} strings={m['strings'][:15]} ints={m['ints'][:20]}")
        lines.append("")

    (out / "FIRST_PARTY_BRIEF.md").write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {out}")
    print(f"enums={len(report['enums'])} key_classes={len(report['key_methods'])} timeouts={len(timeout_hits)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

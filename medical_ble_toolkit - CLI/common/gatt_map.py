"""GATT service/characteristic tree printer (forensic discovery map)."""

from __future__ import annotations

from typing import Any, Iterable


# Well-known short UUID names for readability in the tree.
_KNOWN = {
    "00001800-0000-1000-8000-00805f9b34fb": "Generic Access",
    "00001801-0000-1000-8000-00805f9b34fb": "Generic Attribute",
    "0000180a-0000-1000-8000-00805f9b34fb": "Device Information",
    "0000180f-0000-1000-8000-00805f9b34fb": "Battery",
    "00001810-0000-1000-8000-00805f9b34fb": "Blood Pressure",
    "00001809-0000-1000-8000-00805f9b34fb": "Health Thermometer",
    "00001822-0000-1000-8000-00805f9b34fb": "Pulse Oximeter",
    "00002a35-0000-1000-8000-00805f9b34fb": "Blood Pressure Measurement",
    "00002a36-0000-1000-8000-00805f9b34fb": "Intermediate Cuff Pressure",
    "00002a49-0000-1000-8000-00805f9b34fb": "Blood Pressure Feature",
    "00002a19-0000-1000-8000-00805f9b34fb": "Battery Level",
    "00002a08-0000-1000-8000-00805f9b34fb": "Date Time",
    "00002a1c-0000-1000-8000-00805f9b34fb": "Temperature Measurement",
    "00002a1d-0000-1000-8000-00805f9b34fb": "Temperature Type",
    "00002a1e-0000-1000-8000-00805f9b34fb": "Intermediate Temperature",
    "00002a00-0000-1000-8000-00805f9b34fb": "Device Name",
    "00002a24-0000-1000-8000-00805f9b34fb": "Model Number String",
    "00002a25-0000-1000-8000-00805f9b34fb": "Serial Number String",
    "00002a26-0000-1000-8000-00805f9b34fb": "Firmware Revision String",
    "00002a27-0000-1000-8000-00805f9b34fb": "Hardware Revision String",
    "00002a28-0000-1000-8000-00805f9b34fb": "Software Revision String",
    "00002a29-0000-1000-8000-00805f9b34fb": "Manufacturer Name String",
    "0000f000-0000-1000-8000-00805f9b34fb": "A&D Custom Service",
}


# Canonical property tags shown in the tree (uppercase for visual scan).
_PROP_ORDER = (
    ("read", "Read"),
    ("write", "Write"),
    ("write-without-response", "WriteWithoutResponse"),
    ("notify", "Notify"),
    ("indicate", "Indicate"),
    ("broadcast", "Broadcast"),
    ("authenticated-signed-writes", "AuthSignedWrites"),
    ("extended-properties", "ExtendedProps"),
)


def _props_list(char: Any) -> list[str]:
    props = getattr(char, "properties", None) or []
    if isinstance(props, dict):
        tags = [k for k, v in props.items() if v]
    else:
        tags = list(props)
    return [t.lower() for t in tags]


def _props(char: Any) -> str:
    """Render properties as: Read | Write | Notify | Indicate | …"""
    tags = set(_props_list(char))
    ordered = [label for key, label in _PROP_ORDER if key in tags]
    # Any unknown property strings from the stack
    known_keys = {k for k, _ in _PROP_ORDER}
    ordered.extend(sorted(t for t in tags if t not in known_keys))
    return " | ".join(ordered) if ordered else "(none)"


def _flag_matrix(char: Any) -> str:
    """
    Fixed-width capability matrix so RE sessions can greppably spot notifiables:

      [R W  N I ]  or  [R -  - I ]
    """
    tags = set(_props_list(char))
    r = "R" if "read" in tags else "-"
    w = "W" if ("write" in tags or "write-without-response" in tags) else "-"
    n = "N" if "notify" in tags else "-"
    i = "I" if "indicate" in tags else "-"
    return f"[{r} {w}  {n} {i}]"


def _label(uuid_obj: Any) -> str:
    u = str(uuid_obj).lower()
    name = _KNOWN.get(u, "")
    return f"{uuid_obj}  [{name}]" if name else str(uuid_obj)


def format_gatt_tree(services: Iterable[Any]) -> str:
    """
    Build a visual tree of services → characteristics → properties.

    Example output:
      SERVICE 00001810-... [Blood Pressure]
        ├─ CHAR [R -  - I] 00002a35-... [Blood Pressure Measurement]
        │    props: Indicate
        └─ CHAR [R -  - -] 00002a49-... [Blood Pressure Feature]
             props: Read

    Legend: R=Read  W=Write/WriteWithoutResponse  N=Notify  I=Indicate
    """
    lines: list[str] = []
    lines.append("=" * 72)
    lines.append("GATT DISCOVERY MAP  (Services → Characteristics → Properties)")
    lines.append("  Legend: [R W  N I]  = Read | Write | Notify | Indicate")
    lines.append("  RE tip: Subscribe to every char with N or I during RE sessions.")
    lines.append("  WinRT tip: empty tree after connect often means link died mid-discovery")
    lines.append("             (see [WINRT] GATT_UNREACHABLE) — re-advertise and reconnect.")
    lines.append("=" * 72)

    svc_list = list(services)
    if not svc_list:
        lines.append("  (no services discovered)")
        lines.append("  → On Windows this often means the connection dropped before")
        lines.append("    GetGattServicesAsync completed. Wake device and retry.")
        return "\n".join(lines)

    notifiable_count = 0
    for si, svc in enumerate(svc_list):
        svc_uuid = getattr(svc, "uuid", svc)
        lines.append(f"SERVICE {_label(svc_uuid)}")
        chars = list(getattr(svc, "characteristics", []) or [])
        if not chars:
            lines.append("  (no characteristics)")
            continue
        for ci, ch in enumerate(chars):
            is_last = ci == len(chars) - 1
            branch = "└─" if is_last else "├─"
            cont = "  " if is_last else "│ "
            ch_uuid = getattr(ch, "uuid", ch)
            flags = _flag_matrix(ch)
            if "N" in flags or "I" in flags:
                notifiable_count += 1
            lines.append(f"  {branch} CHAR {flags} {_label(ch_uuid)}")
            lines.append(f"  {cont}    props: {_props(ch)}")
            # Descriptors (CCCD 0x2902 is what enable_notify writes on WinRT)
            descs = list(getattr(ch, "descriptors", []) or [])
            for di, d in enumerate(descs):
                d_last = di == len(descs) - 1
                d_branch = "└─" if d_last else "├─"
                d_uuid = getattr(d, "uuid", d)
                d_s = str(d_uuid).lower()
                cccd = "  [CCCD — WinRT writes here for Notify/Indicate]" if "2902" in d_s else ""
                lines.append(f"  {cont}    {d_branch} DESC {d_uuid}{cccd}")
        if si < len(svc_list) - 1:
            lines.append("")

    lines.append("-" * 72)
    lines.append(f"  Totals: services={len(svc_list)}  notifiable(N|I)={notifiable_count}")
    lines.append("=" * 72)
    return "\n".join(lines)

"""Forensic hex dumps and millisecond timestamps for reverse engineering."""

from __future__ import annotations

from datetime import datetime, timezone


def format_hex_dump(data: bytes | bytearray | memoryview) -> str:
    """
    Format payload as zero-padded hex bytes: '0x0A 0x1B 0xFF'.

    RE WORKFLOW
    -----------
    1. Leave the device idle and note baseline traffic (if any).
    2. Trigger ONE physical action (measure BP, press M1, attach finger, …).
    3. Diff the next [HEX] line against baseline — changed / new bytes are
       candidates for that action.
    4. Correlate [TS] millisecond stamps with your stopwatch / video of the
       hardware button press.
    """
    return " ".join(f"0x{b:02X}" for b in bytes(data))


def ms_timestamp(when: datetime | None = None) -> str:
    """Precise local timestamp with milliseconds: '2026-07-15 14:32:01.123'."""
    dt = when or datetime.now()
    return dt.strftime("%Y-%m-%d %H:%M:%S.") + f"{dt.microsecond // 1000:03d}"


def utc_ms_timestamp(when: datetime | None = None) -> str:
    dt = when or datetime.now(timezone.utc)
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{dt.microsecond // 1000:03d}Z"

"""
Deduplicate BP / glucose readings (companion BPMeasurementsFilterAndSaveRepo style).
"""

from __future__ import annotations

from datetime import datetime
from typing import Any, Iterable, List, Optional, Set, Tuple


def bp_dedup_key(reading: Any) -> Optional[str]:
    """time|sys|dia|pulse|user — stable string key."""
    try:
        ts = getattr(reading, "measured_at", None)
        ts_s = ts.isoformat(sep=" ", timespec="seconds") if isinstance(ts, datetime) else ""
        sys = getattr(reading, "systolic", None)
        dia = getattr(reading, "diastolic", None)
        pulse = getattr(reading, "pulse_rate", None)
        user = getattr(reading, "user_id", None)
        if sys is None and dia is None:
            return None
        return f"{ts_s}|{sys}|{dia}|{pulse}|{user}"
    except Exception:
        return None


def glucose_dedup_key(reading: Any) -> Optional[str]:
    try:
        seq = getattr(reading, "sequence", None)
        if seq is not None:
            return f"seq:{seq}"
        ts = getattr(reading, "measured_at", None)
        conc = getattr(reading, "concentration", None)
        ts_s = ts.isoformat(sep=" ", timespec="seconds") if isinstance(ts, datetime) else ""
        return f"{ts_s}|{conc}"
    except Exception:
        return None


def dedupe_readings(
    readings: Iterable[Any],
    *,
    prior_keys: Optional[Set[str]] = None,
    key_fn=bp_dedup_key,
) -> Tuple[List[Any], int, List[str]]:
    """
    Returns (kept, dropped_count, new_keys_for_store).
    Drops duplicates within the batch and against prior_keys.
    """
    prior = set(prior_keys or ())
    seen: Set[str] = set()
    kept: List[Any] = []
    new_keys: List[str] = []
    dropped = 0
    for r in readings:
        k = key_fn(r)
        if k is None:
            kept.append(r)
            continue
        if k in seen or k in prior:
            dropped += 1
            continue
        seen.add(k)
        new_keys.append(k)
        kept.append(r)
    return kept, dropped, new_keys

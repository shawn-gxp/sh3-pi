"""Shared helpers for measurement record lists."""

from __future__ import annotations

import datetime
from typing import Any, Dict, List, Sequence


def _datetime_key(rec: Dict[str, Any]) -> datetime.datetime:
    """Normalize record datetime for sorting (missing → oldest)."""
    dt = rec.get("datetime")
    if isinstance(dt, datetime.datetime):
        return dt
    if isinstance(dt, str) and dt.strip():
        for fmt in ("%Y-%m-%d %H:%M:%S", "%Y-%m-%dT%H:%M:%S"):
            try:
                return datetime.datetime.strptime(dt.strip(), fmt)
            except ValueError:
                continue
    return datetime.datetime.min


def sort_records_newest_first(
    records: Sequence[Dict[str, Any]],
) -> List[Dict[str, Any]]:
    """Return a new list sorted by measurement time, latest first."""
    return sorted(records, key=_datetime_key, reverse=True)


def sort_all_users_newest_first(
    all_users: Sequence[Sequence[Dict[str, Any]]],
) -> List[List[Dict[str, Any]]]:
    return [sort_records_newest_first(u) for u in all_users]

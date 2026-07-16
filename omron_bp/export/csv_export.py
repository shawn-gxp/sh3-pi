"""
CSV export for BP records.

Writes user1.csv, user2.csv, ... and optional timestamped backups when
merging with an existing file.

Rows are always written **newest datetime first**.
"""

from __future__ import annotations

import csv
import datetime
import pathlib
from typing import Any, Dict, List, Sequence

from omron_bp.export.records_util import sort_records_newest_first
from omron_bp.logging_config import DBG_TAG, get_logger

logger = get_logger("export.csv")

FIELDNAMES = ["datetime", "dia", "sys", "bpm", "mov", "ihb"]


def _as_row(rec: Dict[str, Any]) -> Dict[str, Any]:
    dt = rec["datetime"]
    if isinstance(dt, datetime.datetime):
        dt_str = dt.strftime("%Y-%m-%d %H:%M:%S")
    else:
        dt_str = str(dt)
    return {
        "datetime": dt_str,
        "dia": rec.get("dia", ""),
        "sys": rec.get("sys", ""),
        "bpm": rec.get("bpm", ""),
        "mov": rec.get("mov", 0),
        "ihb": rec.get("ihb", 0),
    }


def write_users_csv(
    all_users: Sequence[List[Dict[str, Any]]],
    output_dir: str | pathlib.Path = ".",
    *,
    merge_existing: bool = True,
) -> List[pathlib.Path]:
    """
    Write one CSV per user, sorted newest measurement first.
    Returns paths written.
    """
    out_dir = pathlib.Path(output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    written: List[pathlib.Path] = []

    for user_idx, records in enumerate(all_users):
        path = out_dir / f"user{user_idx + 1}.csv"
        # Sort raw records first so string rows keep order
        sorted_recs = sort_records_newest_first(records)
        rows = [_as_row(r) for r in sorted_recs]

        if merge_existing and path.is_file():
            backup = out_dir / (
                f"backup_user{user_idx + 1}_"
                f"{datetime.datetime.now().strftime('%Y_%m_%d__%H_%M_%S')}.csv"
            )
            backup.write_bytes(path.read_bytes())
            logger.info("Backup → %s", backup.name)
            # DBG-LOG
            logger.debug("%s backup path=%s", DBG_TAG, backup)

            with path.open("r", newline="", encoding="utf-8") as f:
                old_rows = list(csv.DictReader(f))
            new_dates = {r["datetime"] for r in rows}
            for old in old_rows:
                if old.get("datetime") not in new_dates:
                    rows.append(old)

        # Always newest first (string YYYY-MM-DD HH:MM:SS sorts correctly)
        rows.sort(key=lambda r: r.get("datetime") or "", reverse=True)

        with path.open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=FIELDNAMES)
            writer.writeheader()
            for row in rows:
                writer.writerow(row)

        logger.info(
            "Wrote %d row(s) → %s (newest first)",
            len(rows),
            path,
        )
        # DBG-LOG
        logger.debug("%s csv done user=%d path=%s", DBG_TAG, user_idx + 1, path)
        written.append(path)

    return written

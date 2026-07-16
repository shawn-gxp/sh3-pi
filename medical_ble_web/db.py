"""
SQLite persistence for the medical BLE web POC.
"""

from __future__ import annotations

import json
import sqlite3
from contextlib import contextmanager
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, Iterator, List, Optional

ROOT = Path(__file__).resolve().parent
DATA_DIR = ROOT / "data"
DB_PATH = DATA_DIR / "poc.db"


def _now() -> str:
    return datetime.now(timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]


def init_db(path: Path = DB_PATH) -> None:
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    with connect(path) as conn:
        conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS devices (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                brand TEXT NOT NULL,
                model TEXT,
                mac TEXT NOT NULL UNIQUE,
                name TEXT,
                company TEXT,
                paired INTEGER NOT NULL DEFAULT 0,
                last_seen TEXT,
                notes TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id INTEGER,
                action TEXT NOT NULL,
                status TEXT NOT NULL,
                started_at TEXT NOT NULL,
                ended_at TEXT,
                error TEXT,
                FOREIGN KEY(device_id) REFERENCES devices(id)
            );

            CREATE TABLE IF NOT EXISTS readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id INTEGER,
                session_id INTEGER,
                brand TEXT,
                reading_type TEXT NOT NULL,
                measured_at TEXT,
                systolic REAL,
                diastolic REAL,
                pulse_rate REAL,
                spo2 REAL,
                perfusion_index REAL,
                temperature REAL,
                glucose_mg_dl REAL,
                payload_json TEXT,
                raw_hex TEXT,
                created_at TEXT NOT NULL,
                FOREIGN KEY(device_id) REFERENCES devices(id),
                FOREIGN KEY(session_id) REFERENCES sessions(id)
            );

            CREATE TABLE IF NOT EXISTS scan_cache (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                mac TEXT NOT NULL,
                name TEXT,
                rssi INTEGER,
                seen_at TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT
            );

            CREATE INDEX IF NOT EXISTS idx_readings_mac ON readings(device_id, measured_at);
            CREATE INDEX IF NOT EXISTS idx_devices_brand ON devices(brand);
            """
        )


@contextmanager
def connect(path: Path = DB_PATH) -> Iterator[sqlite3.Connection]:
    conn = sqlite3.connect(str(path), check_same_thread=False)
    conn.row_factory = sqlite3.Row
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def upsert_device(
    *,
    brand: str,
    mac: str,
    model: str = "",
    name: str = "",
    company: str = "",
    paired: Optional[bool] = None,
    notes: str = "",
) -> Dict[str, Any]:
    mac_u = (mac or "").strip().upper()
    now = _now()
    with connect() as conn:
        row = conn.execute(
            "SELECT * FROM devices WHERE mac = ?", (mac_u,)
        ).fetchone()
        if row:
            fields = {
                "brand": brand or row["brand"],
                "model": model or row["model"] or "",
                "name": name or row["name"] or "",
                "company": company or row["company"] or "",
                "notes": notes if notes else (row["notes"] or ""),
                "last_seen": now,
                "updated_at": now,
            }
            if paired is not None:
                fields["paired"] = 1 if paired else 0
            conn.execute(
                """
                UPDATE devices SET
                    brand=?, model=?, name=?, company=?, notes=?,
                    last_seen=?, updated_at=?,
                    paired=COALESCE(?, paired)
                WHERE mac=?
                """,
                (
                    fields["brand"],
                    fields["model"],
                    fields["name"],
                    fields["company"],
                    fields["notes"],
                    fields["last_seen"],
                    fields["updated_at"],
                    fields.get("paired"),
                    mac_u,
                ),
            )
        else:
            conn.execute(
                """
                INSERT INTO devices
                    (brand, model, mac, name, company, paired, last_seen, notes, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    brand,
                    model or "",
                    mac_u,
                    name or "",
                    company or "",
                    1 if paired else 0,
                    now,
                    notes or "",
                    now,
                    now,
                ),
            )
        row = conn.execute(
            "SELECT * FROM devices WHERE mac = ?", (mac_u,)
        ).fetchone()
        return dict(row) if row else {}


def list_devices() -> List[Dict[str, Any]]:
    with connect() as conn:
        rows = conn.execute(
            "SELECT * FROM devices ORDER BY updated_at DESC"
        ).fetchall()
        return [dict(r) for r in rows]


def get_device_by_mac(mac: str) -> Optional[Dict[str, Any]]:
    with connect() as conn:
        row = conn.execute(
            "SELECT * FROM devices WHERE mac = ?",
            ((mac or "").strip().upper(),),
        ).fetchone()
        return dict(row) if row else None


def start_session(
    action: str,
    *,
    device_id: Optional[int] = None,
    status: str = "running",
) -> int:
    now = _now()
    with connect() as conn:
        cur = conn.execute(
            """
            INSERT INTO sessions (device_id, action, status, started_at)
            VALUES (?, ?, ?, ?)
            """,
            (device_id, action, status, now),
        )
        return int(cur.lastrowid)


def end_session(
    session_id: int,
    status: str,
    error: str = "",
) -> None:
    with connect() as conn:
        conn.execute(
            """
            UPDATE sessions SET status=?, ended_at=?, error=?
            WHERE id=?
            """,
            (status, _now(), error or None, session_id),
        )


def insert_reading(
    *,
    device_id: Optional[int],
    session_id: Optional[int],
    brand: str,
    reading_type: str,
    measured_at: Optional[str] = None,
    systolic: Optional[float] = None,
    diastolic: Optional[float] = None,
    pulse_rate: Optional[float] = None,
    spo2: Optional[float] = None,
    perfusion_index: Optional[float] = None,
    temperature: Optional[float] = None,
    glucose_mg_dl: Optional[float] = None,
    payload: Optional[Dict[str, Any]] = None,
    raw_hex: str = "",
) -> int:
    with connect() as conn:
        cur = conn.execute(
            """
            INSERT INTO readings (
                device_id, session_id, brand, reading_type, measured_at,
                systolic, diastolic, pulse_rate, spo2, perfusion_index,
                temperature, glucose_mg_dl, payload_json, raw_hex, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                device_id,
                session_id,
                brand,
                reading_type,
                measured_at or _now(),
                systolic,
                diastolic,
                pulse_rate,
                spo2,
                perfusion_index,
                temperature,
                glucose_mg_dl,
                json.dumps(payload) if payload is not None else None,
                raw_hex or "",
                _now(),
            ),
        )
        return int(cur.lastrowid)


def list_readings(
    *,
    mac: Optional[str] = None,
    brand: Optional[str] = None,
    limit: int = 100,
) -> List[Dict[str, Any]]:
    """Return readings with newest measurement time first (then newest insert)."""
    limit = max(1, min(1000, int(limit)))
    sql = """
        SELECT r.*, d.mac, d.name AS device_name, d.model AS device_model
        FROM readings r
        LEFT JOIN devices d ON d.id = r.device_id
        WHERE 1=1
    """
    args: List[Any] = []
    if mac:
        sql += " AND d.mac = ?"
        args.append(mac.strip().upper())
    if brand:
        sql += " AND r.brand = ?"
        args.append(brand)
    # Latest clinical time on top (not insert order — A&D dumps oldest-first)
    sql += """
        ORDER BY
            COALESCE(r.measured_at, r.created_at) DESC,
            r.id DESC
        LIMIT ?
    """
    args.append(limit)
    with connect() as conn:
        rows = conn.execute(sql, args).fetchall()
        return [dict(r) for r in rows]


def save_scan_hits(devices: List[Dict[str, Any]]) -> None:
    now = _now()
    with connect() as conn:
        conn.execute("DELETE FROM scan_cache")
        for d in devices:
            conn.execute(
                """
                INSERT INTO scan_cache (mac, name, rssi, seen_at)
                VALUES (?, ?, ?, ?)
                """,
                (
                    (d.get("mac") or "").upper(),
                    d.get("name") or "",
                    d.get("rssi"),
                    now,
                ),
            )


def list_scan_cache() -> List[Dict[str, Any]]:
    with connect() as conn:
        rows = conn.execute(
            "SELECT * FROM scan_cache ORDER BY id ASC"
        ).fetchall()
        return [dict(r) for r in rows]


def set_setting(key: str, value: str) -> None:
    with connect() as conn:
        conn.execute(
            """
            INSERT INTO settings(key, value) VALUES(?, ?)
            ON CONFLICT(key) DO UPDATE SET value=excluded.value
            """,
            (key, value),
        )


def get_setting(key: str, default: str = "") -> str:
    with connect() as conn:
        row = conn.execute(
            "SELECT value FROM settings WHERE key = ?", (key,)
        ).fetchone()
        return str(row["value"]) if row else default

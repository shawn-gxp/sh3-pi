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


def reset_db(path: Path = DB_PATH) -> None:
    """Delete all rows (keep schema). Fresh hub start."""
    init_db(path)
    with connect(path) as conn:
        conn.executescript(
            """
            DELETE FROM readings;
            DELETE FROM sessions;
            DELETE FROM devices;
            DELETE FROM scan_cache;
            """
        )
        try:
            conn.execute("DELETE FROM sqlite_sequence")
        except sqlite3.OperationalError:
            pass
    try:
        export_paired_devices(path)
    except OSError:
        pass


PAIRED_EXPORT_PATH = DATA_DIR / "paired_devices.json"


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
    migrate_brand_ids(path)
    export_paired_devices(path)


def migrate_brand_ids(path: Path = DB_PATH) -> int:
    """
    One-time / startup normalize of legacy brand ids in SQLite.

    Lab DB used thermo / and for Tier-1 Nipro meters; hub policy expects
    nipro_nt100b / nipro_nbp.
    """
    if not Path(path).is_file():
        return 0
    n = 0
    with connect(path) as conn:
        for sql in (
            "UPDATE devices SET brand='nipro_nt100b' "
            "WHERE lower(brand) IN ('thermo','thermometer','nt100b')",
            "UPDATE devices SET brand='nipro_nbp' "
            "WHERE lower(brand)='and' AND ("
            "  upper(ifnull(model,'')) LIKE '%NBP%' OR "
            "  upper(ifnull(name,'')) LIKE '%NBP%'"
            ")",
            "UPDATE devices SET brand='masimo' "
            "WHERE lower(brand) IN ('mightysat','spo2')",
        ):
            cur = conn.execute(sql)
            n += cur.rowcount if cur.rowcount and cur.rowcount > 0 else 0
    return n


def export_paired_devices(path: Path = DB_PATH) -> Path:
    """
    Mirror SQLite devices → data/paired_devices.json (human backup).

    SQLite remains source of truth for the hub roster.
    """
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    devices: List[Dict[str, Any]] = []
    if Path(path).is_file():
        with connect(path) as conn:
            rows = conn.execute(
                "SELECT brand, model, mac, name, paired FROM devices "
                "ORDER BY updated_at DESC"
            ).fetchall()
            for r in rows:
                devices.append(
                    {
                        "mac": (r["mac"] or "").upper(),
                        "name": r["name"] or "",
                        "brand": r["brand"] or "",
                        "model": r["model"] or "",
                        "paired": bool(r["paired"]),
                    }
                )
    payload = {
        "version": 1,
        "updated_at": _now(),
        "devices": devices,
    }
    out = PAIRED_EXPORT_PATH
    out.write_text(json.dumps(payload, indent=2), encoding="utf-8")
    return out


@contextmanager
def connect(path: Path = DB_PATH) -> Iterator[sqlite3.Connection]:
    # timeout: wait on locks (concurrent hub workers + web)
    conn = sqlite3.connect(str(path), check_same_thread=False, timeout=30.0)
    conn.row_factory = sqlite3.Row
    try:
        conn.execute("PRAGMA busy_timeout=30000")
        conn.execute("PRAGMA journal_mode=WAL")
        conn.execute("PRAGMA synchronous=NORMAL")
    except sqlite3.Error:
        pass
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


def has_similar_reading(
    *,
    device_id: Optional[int],
    reading_type: str,
    measured_at: Optional[str],
    systolic: Optional[float] = None,
    diastolic: Optional[float] = None,
    pulse_rate: Optional[float] = None,
    spo2: Optional[float] = None,
    temperature: Optional[float] = None,
    glucose_mg_dl: Optional[float] = None,
) -> bool:
    """
    True if an equivalent clinical row already exists (prevents re-sync duplicates).

    Matches device + type + measured_at + primary vital fields.
    Live SpO2 streams intentionally call insert_reading without this check
    when values change (see ble_jobs live path).
    """
    if not device_id or not measured_at:
        return False
    with connect() as conn:
        row = conn.execute(
            """
            SELECT id FROM readings
            WHERE device_id = ?
              AND reading_type = ?
              AND measured_at = ?
              AND (systolic IS ? OR systolic = ?)
              AND (diastolic IS ? OR diastolic = ?)
              AND (pulse_rate IS ? OR pulse_rate = ?)
              AND (spo2 IS ? OR spo2 = ?)
              AND (temperature IS ? OR temperature = ?)
              AND (glucose_mg_dl IS ? OR glucose_mg_dl = ?)
            LIMIT 1
            """,
            (
                device_id,
                reading_type,
                measured_at,
                systolic,
                systolic,
                diastolic,
                diastolic,
                pulse_rate,
                pulse_rate,
                spo2,
                spo2,
                temperature,
                temperature,
                glucose_mg_dl,
                glucose_mg_dl,
            ),
        ).fetchone()
        return row is not None


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
    dedupe: bool = False,
) -> Optional[int]:
    """
    Insert a reading. If dedupe=True and an equivalent row exists, skip and return None.
    """
    measured = measured_at or _now()
    if dedupe and has_similar_reading(
        device_id=device_id,
        reading_type=reading_type,
        measured_at=measured_at,  # only dedupe when device supplied a timestamp
        systolic=systolic,
        diastolic=diastolic,
        pulse_rate=pulse_rate,
        spo2=spo2,
        temperature=temperature,
        glucose_mg_dl=glucose_mg_dl,
    ):
        return None

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
                measured,
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
        rid = int(cur.lastrowid)

    # Cloud transfer (Android hub drop-in) — never fail local insert
    try:
        from mqtt_bridge import notify_reading_inserted  # type: ignore

        notify_reading_inserted(
            reading_id=rid,
            device_id=device_id,
            brand=brand,
            reading_type=reading_type,
            measured_at=measured,
            systolic=systolic,
            diastolic=diastolic,
            pulse_rate=pulse_rate,
            spo2=spo2,
            temperature=temperature,
            glucose_mg_dl=glucose_mg_dl,
            perfusion_index=perfusion_index,
        )
    except Exception:
        pass

    return rid


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


def latest_reading_for_device(device_id: int) -> Optional[Dict[str, Any]]:
    """
    Newest clinical reading for one device (dashboard hero).

    Prefer **created_at** (when the hub received it) so NT-100B / TICD device
    clocks that lag wall time cannot keep an older HTP row "on top" forever.
    """
    with connect() as conn:
        row = conn.execute(
            """
            SELECT r.*, d.mac, d.name AS device_name, d.model AS device_model,
                   d.brand AS device_brand, d.company
            FROM readings r
            LEFT JOIN devices d ON d.id = r.device_id
            WHERE r.device_id = ?
              AND r.reading_type IN ('bp', 'spo2', 'temp', 'glucose')
            ORDER BY
                r.created_at DESC,
                r.id DESC
            LIMIT 1
            """,
            (device_id,),
        ).fetchone()
        return dict(row) if row else None


def dashboard_board(macs: Optional[List[str]] = None) -> List[Dict[str, Any]]:
    """
    One card per saved device: identity + latest clinical reading.

    Optionally filter to a MAC allow-list (cycle roster).
    """
    devices = list_devices()
    if macs:
        want = {m.strip().upper() for m in macs if m}
        devices = [d for d in devices if (d.get("mac") or "").upper() in want]

    board: List[Dict[str, Any]] = []
    for d in devices:
        did = d.get("id")
        latest = latest_reading_for_device(int(did)) if did is not None else None
        board.append(
            {
                "device_id": did,
                "brand": d.get("brand") or "",
                "company": d.get("company") or "",
                "mac": (d.get("mac") or "").upper(),
                "model": d.get("model") or "",
                "name": d.get("name") or d.get("model") or "",
                "paired": bool(d.get("paired")),
                "latest": latest,
            }
        )
    return board


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

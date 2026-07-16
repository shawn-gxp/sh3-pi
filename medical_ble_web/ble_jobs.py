"""
BLE job adapters — call medical_ble_toolkit without modifying it.
"""

from __future__ import annotations

import asyncio
import logging
import sys
from datetime import datetime
from pathlib import Path
from typing import Any, Callable, Dict, List, Optional

# Parent experiments/ on path so medical_ble_toolkit imports work
_EXPERIMENTS = Path(__file__).resolve().parent.parent
if str(_EXPERIMENTS) not in sys.path:
    sys.path.insert(0, str(_EXPERIMENTS))

from brands import get_brand  # noqa: E402
import db  # noqa: E402

log = logging.getLogger("medical_ble_web.ble")

# Global lock: one BLE radio job at a time (Windows-safe POC)
_ble_lock = asyncio.Lock()

# Live stream state (in-memory + DB writes)
_live_task: Optional[asyncio.Task] = None
_live_stop = asyncio.Event()
_live_status: Dict[str, Any] = {
    "active": False,
    "brand": "",
    "mac": "",
    "model": "",
    "status": "idle",
    "latest": None,
    "error": "",
    "session_id": None,
    "vitals_count": 0,
    "packets": 0,
    "updated_at": "",
}

# WebSocket / SSE-style push: each browser gets a queue of live snapshots
_live_queues: List[asyncio.Queue] = []


def live_status() -> Dict[str, Any]:
    # Shallow copy; nested latest dict is replaced on each vital (not mutated in place)
    snap = dict(_live_status)
    if snap.get("latest") is not None and isinstance(snap["latest"], dict):
        snap["latest"] = dict(snap["latest"])
    return snap


def subscribe_live() -> asyncio.Queue:
    """Register a browser client for push updates (WebSocket)."""
    q: asyncio.Queue = asyncio.Queue(maxsize=64)
    _live_queues.append(q)
    return q


def unsubscribe_live(q: asyncio.Queue) -> None:
    try:
        _live_queues.remove(q)
    except ValueError:
        pass


def _push_live_to_clients() -> None:
    """Non-blocking fan-out of current live snapshot to all WS clients."""
    if not _live_queues:
        return
    snap = live_status()
    dead: List[asyncio.Queue] = []
    for q in list(_live_queues):
        try:
            # Drop oldest if client is slow so we always push newest
            if q.full():
                try:
                    q.get_nowait()
                except asyncio.QueueEmpty:
                    pass
            q.put_nowait(snap)
        except Exception:  # noqa: BLE001
            dead.append(q)
    for q in dead:
        unsubscribe_live(q)


def _reading_to_row(obj: Any, brand: str) -> Dict[str, Any]:
    """Normalize toolkit dataclasses / dicts into a DB-friendly reading row."""
    d: Dict[str, Any]
    if hasattr(obj, "to_dict"):
        d = obj.to_dict()
    elif isinstance(obj, dict):
        d = dict(obj)
    else:
        d = {"repr": repr(obj)}

    measured = d.get("measured_at") or d.get("timestamp")
    if isinstance(measured, datetime):
        measured = measured.isoformat()

    row: Dict[str, Any] = {
        "brand": brand,
        "reading_type": "other",
        "measured_at": measured,
        "systolic": None,
        "diastolic": None,
        "pulse_rate": None,
        "spo2": None,
        "perfusion_index": None,
        "temperature": None,
        "glucose_mg_dl": None,
        "payload": d,
        "raw_hex": d.get("raw_hex") or "",
    }

    if d.get("systolic") is not None:
        row["reading_type"] = "bp"
        row["systolic"] = _f(d.get("systolic"))
        row["diastolic"] = _f(d.get("diastolic"))
        row["pulse_rate"] = _f(d.get("pulse_rate"))
    elif d.get("spo2") is not None:
        row["reading_type"] = "spo2"
        row["spo2"] = _f(d.get("spo2"))
        row["pulse_rate"] = _f(d.get("pulse_rate"))
        row["perfusion_index"] = _f(d.get("perfusion_index"))
    elif d.get("object_temperature") is not None or d.get("temperature") is not None:
        row["reading_type"] = "temp"
        row["temperature"] = _f(
            d.get("object_temperature", d.get("temperature"))
        )
        row["pulse_rate"] = _f(d.get("pulse_rate"))
    elif d.get("blood_glucose_mg_dl") is not None:
        row["reading_type"] = "glucose"
        row["glucose_mg_dl"] = _f(d.get("blood_glucose_mg_dl"))
    elif d.get("type") in ("ack", "nack", "device_info", "raw_message"):
        row["reading_type"] = "meta"
    elif "ordinal" in d or "samples" in d:
        row["reading_type"] = "waveform"
    else:
        row["reading_type"] = "raw"

    return row


def _f(v: Any) -> Optional[float]:
    if v is None:
        return None
    try:
        return float(v)
    except (TypeError, ValueError):
        return None


def _is_clinical(row: Dict[str, Any]) -> bool:
    return row.get("reading_type") in ("bp", "spo2", "temp", "glucose")


def _ts_key(row: Optional[Dict[str, Any]]) -> str:
    """Sort key for measured_at (ISO / display strings); empty sorts last."""
    if not row:
        return ""
    return str(row.get("measured_at") or row.get("created_at") or "")


def _pick_newest(rows: List[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
    """Clinical row with newest timestamp (for hero / latest)."""
    clinical = [r for r in rows if _is_clinical(r)]
    if not clinical:
        return None
    return max(clinical, key=_ts_key)


async def job_scan(
    *,
    brand_id: Optional[str] = None,
    timeout: float = 8.0,
) -> List[Dict[str, Any]]:
    """Scan nearby BLE devices; optionally filter by brand profile hints."""
    from medical_ble_toolkit.ble_client import scan_devices
    from medical_ble_toolkit.profiles import get_profile

    profile = None
    brand = get_brand(brand_id or "")
    if brand and brand.get("connect_profile"):
        try:
            # Unfiltered scan is better for discovery; still load profile for filter
            # when brand explicitly set — use None for broad scan if brand empty
            if brand_id:
                profile = get_profile(brand["connect_profile"])
        except KeyError:
            profile = None

    async with _ble_lock:
        # Broad scan when no brand — easier to find new devices
        use_profile = profile if brand_id else None
        devices = await scan_devices(profile=use_profile, timeout=timeout)

    out: List[Dict[str, Any]] = []
    for d in devices:
        out.append(
            {
                "mac": (getattr(d, "address", "") or "").upper(),
                "name": (getattr(d, "name", None) or "") or "(no name)",
                "rssi": getattr(d, "rssi", None),
            }
        )
    db.save_scan_hits(out)
    return out


class BleJobError(Exception):
    """Structured BLE job failure for the API layer."""

    def __init__(
        self,
        message: str,
        *,
        code: str = "BLE_ERROR",
        tips: Optional[List[str]] = None,
        retryable: bool = False,
    ):
        super().__init__(message)
        self.code = code
        self.tips = tips or []
        self.retryable = retryable

    def as_dict(self) -> Dict[str, Any]:
        return {
            "ok": False,
            "error": str(self),
            "code": self.code,
            "tips": self.tips,
            "retryable": self.retryable,
        }


def _omron_fe4a_error(exc: BaseException) -> BleJobError:
    return BleJobError(
        str(exc),
        code="OMRON_FE4A_MISSING",
        retryable=True,
        tips=[
            "FE4A missing = Windows cannot open Omron private service (bond/mode).",
            "If Sync usually works without a button: wait 2s and Sync again.",
            "If still failing: SHORT-press BT (transfer), then Sync immediately.",
            "Long-hold flashing P is for PAIR only, not every Sync.",
            "Persistent fail: remove cuff in Windows Bluetooth → Re-pair → Sync.",
            "Phone OMRON Connect must not own the bond (one host only).",
        ],
    )


async def job_pair(
    *,
    brand_id: str,
    mac: str,
    model: str = "",
    repair: bool = False,
) -> Dict[str, Any]:
    brand = get_brand(brand_id)
    if not brand:
        raise ValueError(f"Unknown brand: {brand_id}")

    model = model or brand.get("default_model") or ""
    mac_u = mac.strip().upper()
    device = db.upsert_device(
        brand=brand_id,
        mac=mac_u,
        model=model,
        company=brand.get("company", ""),
        name=model,
    )
    sid = db.start_session(
        "repair" if repair else "pair",
        device_id=device.get("id"),
    )

    try:
        async with _ble_lock:
            if brand.get("is_omron"):
                from medical_ble_toolkit.omron_bridge import pair_omron

                await pair_omron(mac_u, model, force_rebind=repair)
            else:
                await _generic_pair(
                    brand_id=brand_id,
                    profile_id=brand["connect_profile"],
                    mac=mac_u,
                    model=model,
                    force_rebind=repair,
                )
        db.upsert_device(
            brand=brand_id,
            mac=mac_u,
            model=model,
            company=brand.get("company", ""),
            paired=True,
        )
        db.end_session(sid, "ok")
        out: Dict[str, Any] = {
            "ok": True,
            "mac": mac_u,
            "brand": brand_id,
            "session_id": sid,
        }
        if brand.get("is_omron"):
            out["next_steps"] = [
                "Pair OK. Many setups Sync without any button if the bond is solid.",
                "If Sync fails (FE4A): SHORT-press BT once, then Sync again.",
                "Flashing P is only for Pair / Re-pair, not every Sync.",
                "HEM-7143T1 history map is 30 EEPROM slots (by design).",
            ]
            # Let Windows finish storing the bond before a frantic re-connect
            await asyncio.sleep(1.5)
        return out
    except Exception as exc:  # noqa: BLE001
        db.end_session(sid, "fail", error=str(exc))
        raise


async def _generic_pair(
    *,
    brand_id: str,
    profile_id: str,
    mac: str,
    model: str,
    force_rebind: bool,
) -> None:
    from medical_ble_toolkit.ble_client import MedicalBleClient
    from medical_ble_toolkit.common.winrt_errors import is_windows
    from medical_ble_toolkit.profiles import get_profile

    if force_rebind and is_windows():
        try:
            from omron_bp.ble.connection import unpair_address

            await unpair_address(mac)
            await asyncio.sleep(1.0)
        except Exception as exc:  # noqa: BLE001
            log.warning("unpair skip: %s", exc)

    if brand_id == "beurer":
        from medical_ble_toolkit.beurer.session import BeurerCompanionSession

        sess = BeurerCompanionSession(mac, model_id=model or "BM54", pair=True)
        result = await sess.run()
        if not result.ok:
            raise RuntimeError(result.message or result.status.value)
        return

    profile = get_profile(profile_id)
    client = MedicalBleClient(
        address=mac,
        profile=profile,
        pair=is_windows(),
        connect_retries=2,
        auto_dispatch=profile_id in ("re_generic", "fora6"),
    )
    await client.run(duration=12.0 if brand_id == "masimo" else 8.0, connect_timeout=35.0)


async def job_sync(
    *,
    brand_id: str,
    mac: str,
    model: str = "",
    listen_s: float = 30.0,
) -> Dict[str, Any]:
    """One-shot read/sync for the brand; store clinical readings in SQLite."""
    brand = get_brand(brand_id)
    if not brand:
        raise ValueError(f"Unknown brand: {brand_id}")

    model = model or brand.get("default_model") or ""
    mac_u = mac.strip().upper()
    device = db.upsert_device(
        brand=brand_id,
        mac=mac_u,
        model=model,
        company=brand.get("company", ""),
    )
    device_id = device.get("id")
    sid = db.start_session("sync", device_id=device_id)
    stored = 0
    collected: List[Dict[str, Any]] = []

    try:
        async with _ble_lock:
            if brand.get("is_omron"):
                from medical_ble_toolkit.omron_bridge import (
                    flatten_readings,
                    read_omron,
                )

                # READ needs transfer mode (short-press BT), not flashing P.
                # One quick retry: often user still has cuff in P or bond is settling.
                last_omron_exc: Optional[BaseException] = None
                all_users = None
                for attempt in (1, 2):
                    try:
                        if attempt == 2:
                            log.warning(
                                "Omron READ retry 2/2 — short-press BT (transfer) now…"
                            )
                            await asyncio.sleep(2.0)
                        all_users = await read_omron(
                            mac_u, model, find_timeout=45.0, session_retries=2
                        )
                        last_omron_exc = None
                        break
                    except Exception as exc:  # noqa: BLE001
                        last_omron_exc = exc
                        msg = str(exc).lower()
                        if "fe4a" in msg or "parent service" in msg:
                            if attempt == 1:
                                log.warning(
                                    "FE4A missing on attempt 1 — will retry once "
                                    "(use short-press transfer, not flashing P)"
                                )
                                continue
                            raise _omron_fe4a_error(exc) from exc
                        raise
                if last_omron_exc is not None:
                    raise last_omron_exc
                assert all_users is not None
                flat = flatten_readings(all_users)
                for r in flat:
                    row = _reading_to_row(r, brand_id)
                    if not _is_clinical(row):
                        continue
                    db.insert_reading(
                        device_id=device_id,
                        session_id=sid,
                        brand=brand_id,
                        reading_type=row["reading_type"],
                        measured_at=row.get("measured_at"),
                        systolic=row.get("systolic"),
                        diastolic=row.get("diastolic"),
                        pulse_rate=row.get("pulse_rate"),
                        spo2=row.get("spo2"),
                        perfusion_index=row.get("perfusion_index"),
                        temperature=row.get("temperature"),
                        glucose_mg_dl=row.get("glucose_mg_dl"),
                        payload=row.get("payload"),
                        raw_hex=row.get("raw_hex") or "",
                    )
                    stored += 1
                    collected.append(row)
            elif brand_id == "beurer":
                from medical_ble_toolkit.beurer.session import BeurerCompanionSession

                sess = BeurerCompanionSession(
                    mac_u, model_id=model or "BM54", pair=True
                )
                result = await sess.run()
                for r in result.readings:
                    row = _reading_to_row(r, brand_id)
                    if not _is_clinical(row) and row["reading_type"] not in (
                        "raw",
                        "other",
                    ):
                        if row["reading_type"] == "waveform":
                            continue
                    if row["reading_type"] == "waveform":
                        continue
                    if _is_clinical(row) or row.get("payload"):
                        db.insert_reading(
                            device_id=device_id,
                            session_id=sid,
                            brand=brand_id,
                            reading_type=row["reading_type"],
                            measured_at=row.get("measured_at"),
                            systolic=row.get("systolic"),
                            diastolic=row.get("diastolic"),
                            pulse_rate=row.get("pulse_rate"),
                            spo2=row.get("spo2"),
                            perfusion_index=row.get("perfusion_index"),
                            temperature=row.get("temperature"),
                            glucose_mg_dl=row.get("glucose_mg_dl"),
                            payload=row.get("payload"),
                            raw_hex=row.get("raw_hex") or "",
                        )
                        if _is_clinical(row):
                            stored += 1
                            collected.append(row)
                if not result.ok and stored == 0:
                    raise RuntimeError(result.message or str(result.status))
            else:
                from medical_ble_toolkit.ble_client import MedicalBleClient
                from medical_ble_toolkit.common.winrt_errors import is_windows
                from medical_ble_toolkit.profiles import get_profile

                profile = get_profile(brand["connect_profile"])
                is_thermo = brand_id in ("thermo", "thermometer")
                # Session length by brand
                if is_thermo:
                    # History poll is in post-connect setup; short quiet listen after
                    duration = max(12.0, min(float(listen_s), 25.0))
                elif brand_id == "and":
                    duration = max(60.0, listen_s)
                elif brand_id == "masimo":
                    duration = max(20.0, listen_s)
                else:
                    duration = listen_s

                # Thermo: skip re-pair every sync (eats the ~2 min BLE window)
                dev_row = db.get_device_by_mac(mac_u) or {}
                do_pair = is_windows() and (
                    not is_thermo or not bool(dev_row.get("paired"))
                )
                client = MedicalBleClient(
                    address=mac_u,
                    profile=profile,
                    pair=do_pair,
                    connect_retries=2 if not is_thermo else 1,
                    auto_dispatch=brand["connect_profile"]
                    in ("re_generic", "fora6"),
                )
                await client.run(
                    duration=duration,
                    connect_timeout=25.0 if is_thermo else 35.0,
                    quiet_timeout=3.0 if is_thermo else None,
                )
                for r in client.readings:
                    row = _reading_to_row(r, brand_id)
                    if row["reading_type"] == "waveform":
                        continue
                    # A&D: only store real BP (sys/dia), never custom meta garbage
                    if brand_id == "and" and not _is_clinical(row):
                        continue
                    # Thermo: store temps; skip meta dicts (storage_count, model, …)
                    if is_thermo and not _is_clinical(row):
                        continue
                    if _is_clinical(row) or row["reading_type"] in ("meta", "raw"):
                        if row["reading_type"] == "meta":
                            continue
                        db.insert_reading(
                            device_id=device_id,
                            session_id=sid,
                            brand=brand_id,
                            reading_type=row["reading_type"],
                            measured_at=row.get("measured_at"),
                            systolic=row.get("systolic"),
                            diastolic=row.get("diastolic"),
                            pulse_rate=row.get("pulse_rate"),
                            spo2=row.get("spo2"),
                            perfusion_index=row.get("perfusion_index"),
                            temperature=row.get("temperature"),
                            glucose_mg_dl=row.get("glucose_mg_dl"),
                            payload=row.get("payload"),
                            raw_hex=row.get("raw_hex") or "",
                        )
                        if _is_clinical(row):
                            stored += 1
                            collected.append(row)

        latest = _pick_newest(collected)
        db.end_session(sid, "ok")
        out: Dict[str, Any] = {
            "ok": True,
            "mac": mac_u,
            "brand": brand_id,
            "stored": stored,
            "latest": latest,
            "session_id": sid,
        }
        if brand_id in ("thermo", "thermometer") and stored == 0:
            out["warning"] = (
                "NT-100B stored 0 temps. Device BLE is usually OFF until you take "
                "a forehead reading; advertising lasts ~1–2 minutes only. "
                "Workflow: measure → (optional Pair once) → Sync immediately while "
                "BLE is on. If still 0: check MAC/name, keep device awake, retry Sync."
            )
            out["tips"] = [
                "1. Take a temperature measurement on the NT-100B first.",
                "2. Within ~2 minutes (while BLE is advertising), click Sync.",
                "3. Pair only once; re-pairing every Sync can miss the window.",
                "4. Do not wait — the device turns BLE off after the short window.",
            ]
        return out
    except BleJobError as exc:
        db.end_session(sid, "fail", error=str(exc))
        raise
    except Exception as exc:  # noqa: BLE001
        db.end_session(sid, "fail", error=str(exc))
        msg = str(exc).lower()
        if "fe4a" in msg or "parent service" in msg:
            raise _omron_fe4a_error(exc) from exc
        raise


async def job_live_start(
    *,
    brand_id: str,
    mac: str,
    model: str = "",
    duration_s: float = 3600.0,
    on_reading: Optional[Callable[[Dict[str, Any]], None]] = None,
    auto_reconnect: bool = True,
    max_reconnects: int = 30,
    reconnect_delay_s: float = 2.5,
    stale_vitals_s: float = 20.0,
) -> Dict[str, Any]:
    """
    Start background LIVE stream (Masimo SpO2 etc.).

    Unlike Sync (one-shot history dump), this keeps the BLE link open and
    updates /live/latest + WebSocket on every clinical packet (~1 Hz SpO2).

    auto_reconnect: if the link drops or SpO2 goes silent, reconnect until
    Live stop or max_reconnects (streaming brands only by default).
    """
    global _live_task

    if _live_task and not _live_task.done():
        raise RuntimeError("Live session already running — stop it first")

    brand = get_brand(brand_id)
    if not brand:
        raise ValueError(f"Unknown brand: {brand_id}")

    model = model or brand.get("default_model") or ""
    mac_u = mac.strip().upper()
    device = db.upsert_device(
        brand=brand_id,
        mac=mac_u,
        model=model,
        company=brand.get("company", ""),
    )
    device_id = device.get("id")
    already_paired = bool(device.get("paired"))
    sid = db.start_session("live", device_id=device_id)

    _live_stop.clear()
    _live_status.update(
        {
            "active": True,
            "brand": brand_id,
            "mac": mac_u,
            "model": model,
            "status": "starting",
            "latest": None,
            "error": "",
            "session_id": sid,
            "vitals_count": 0,
            "packets": 0,
            "updated_at": "",
            "reconnect_attempt": 0,
            "auto_reconnect": bool(auto_reconnect),
        }
    )
    _push_live_to_clients()

    async def _runner() -> None:
        import time as _time

        from medical_ble_toolkit.ble_client import MedicalBleClient
        from medical_ble_toolkit.common.winrt_errors import is_windows
        from medical_ble_toolkit.profiles import get_profile

        last_db_mono = 0.0
        last_spo2_key = ""
        last_vital_mono = 0.0
        profile = get_profile(brand["connect_profile"])
        is_stream = brand_id == "masimo" or profile.id == "mightysat"
        do_reconnect = bool(auto_reconnect) and is_stream
        session_end = _time.monotonic() + max(30.0, float(duration_s))
        attempt = 0
        had_any_vitals = False

        def _on(r: Any) -> None:
            nonlocal last_db_mono, last_spo2_key, last_vital_mono, had_any_vitals
            _live_status["packets"] = int(_live_status.get("packets") or 0) + 1
            row = _reading_to_row(r, brand_id)
            if not _is_clinical(row):
                return
            had_any_vitals = True
            last_vital_mono = _time.monotonic()
            now_s = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
            row["measured_at"] = row.get("measured_at") or now_s
            _live_status["latest"] = row
            _live_status["status"] = "live"
            _live_status["updated_at"] = now_s
            _live_status["error"] = ""
            _live_status["vitals_count"] = (
                int(_live_status.get("vitals_count") or 0) + 1
            )
            _push_live_to_clients()
            key = (
                f"{row.get('spo2')}|{row.get('pulse_rate')}|"
                f"{row.get('perfusion_index')}"
            )
            mono = _time.monotonic()
            if key != last_spo2_key or (mono - last_db_mono) >= 5.0:
                last_spo2_key = key
                last_db_mono = mono
                try:
                    db.insert_reading(
                        device_id=device_id,
                        session_id=sid,
                        brand=brand_id,
                        reading_type=row["reading_type"],
                        measured_at=row.get("measured_at"),
                        systolic=row.get("systolic"),
                        diastolic=row.get("diastolic"),
                        pulse_rate=row.get("pulse_rate"),
                        spo2=row.get("spo2"),
                        perfusion_index=row.get("perfusion_index"),
                        temperature=row.get("temperature"),
                        glucose_mg_dl=row.get("glucose_mg_dl"),
                        payload=row.get("payload"),
                        raw_hex=row.get("raw_hex") or "",
                    )
                except Exception as db_exc:  # noqa: BLE001
                    log.warning("live DB write: %s", db_exc)
            if on_reading:
                try:
                    on_reading(row)
                except Exception:  # noqa: BLE001
                    pass

        try:
            while not _live_stop.is_set():
                remaining = session_end - _time.monotonic()
                if remaining < 5.0:
                    log.info("Live session time budget exhausted")
                    break

                attempt += 1
                if attempt > max(1, int(max_reconnects)):
                    _live_status["error"] = (
                        f"Gave up after {max_reconnects} connect/reconnect attempts"
                    )
                    log.error(_live_status["error"])
                    break

                _live_status["reconnect_attempt"] = attempt
                _live_status["status"] = (
                    "connecting" if attempt == 1 else "reconnecting"
                )
                _live_status["error"] = (
                    ""
                    if attempt == 1
                    else f"Reconnecting (attempt {attempt}/{max_reconnects})…"
                )
                _push_live_to_clients()
                log.info(
                    "Live link attempt %d/%d mac=%s remaining=%.0fs",
                    attempt,
                    max_reconnects,
                    mac_u,
                    remaining,
                )

                # First attempt may pair if never bonded; later reconnects skip pair
                do_pair = is_windows() and not already_paired and attempt == 1
                client = MedicalBleClient(
                    address=mac_u,
                    profile=profile,
                    pair=do_pair,
                    connect_retries=2,
                    on_reading=_on,
                    auto_dispatch=brand["connect_profile"]
                    in ("re_generic", "fora6"),
                )

                segment_ok = False
                try:
                    async with _ble_lock:
                        if _live_stop.is_set():
                            break
                        chunk = min(remaining, 3600.0)
                        _live_status["status"] = (
                            "streaming" if is_stream else "connected"
                        )
                        _push_live_to_clients()
                        last_vital_mono = 0.0  # reset silence watch for this link
                        segment_start = _time.monotonic()

                        run_task = asyncio.create_task(
                            client.run(
                                duration=chunk,
                                connect_timeout=35.0,
                                quiet_timeout=0.0 if is_stream else None,
                                raise_on_error=True,
                            )
                        )
                        stop_task = asyncio.create_task(_live_stop.wait())

                        # Watchdog: exit segment early if stream goes silent / never starts
                        while not run_task.done() and not stop_task.done():
                            await asyncio.sleep(0.5)
                            now_m = _time.monotonic()
                            if now_m >= session_end:
                                log.info("Live wall-clock end — stopping segment")
                                break
                            if not do_reconnect:
                                continue
                            # Had vitals then silence → reconnect
                            if last_vital_mono > 0 and (
                                now_m - last_vital_mono
                            ) >= float(stale_vitals_s):
                                log.warning(
                                    "No clinical vitals for %.0fs — forcing reconnect",
                                    stale_vitals_s,
                                )
                                _live_status["status"] = "reconnecting"
                                _live_status["error"] = (
                                    f"Stream silent {stale_vitals_s:.0f}s — reconnecting…"
                                )
                                _push_live_to_clients()
                                break
                            # No clinical vitals at all this link after 40s → retry
                            if (
                                last_vital_mono <= 0
                                and (now_m - segment_start) >= 40.0
                            ):
                                log.warning(
                                    "No clinical vitals within 40s of connect — reconnecting"
                                )
                                _live_status["status"] = "reconnecting"
                                hint = (
                                    "check finger / sensor"
                                    if is_stream
                                    else "check measure / transfer mode"
                                )
                                _live_status["error"] = (
                                    f"No vitals yet — reconnecting ({hint})…"
                                )
                                _push_live_to_clients()
                                break

                        # Tear down current segment
                        if not run_task.done():
                            run_task.cancel()
                            try:
                                await client.disconnect()
                            except Exception:  # noqa: BLE001
                                pass
                            try:
                                await run_task
                            except (asyncio.CancelledError, Exception):  # noqa: BLE001
                                pass
                        else:
                            exc = run_task.exception()
                            if exc and not _live_stop.is_set():
                                raise exc
                            segment_ok = True

                        if not stop_task.done():
                            stop_task.cancel()
                            try:
                                await stop_task
                            except (asyncio.CancelledError, Exception):  # noqa: BLE001
                                pass

                except Exception as exc:  # noqa: BLE001
                    if _live_stop.is_set():
                        break
                    log.warning(
                        "Live segment failed (attempt %d): %s: %s",
                        attempt,
                        type(exc).__name__,
                        exc,
                    )
                    _live_status["status"] = "reconnecting"
                    _live_status["error"] = (
                        f"{type(exc).__name__}: {exc} — reconnecting…"
                    )
                    _push_live_to_clients()

                if _live_stop.is_set():
                    break

                if not do_reconnect:
                    # One-shot live for non-stream brands
                    break

                # Full duration completed cleanly with vitals — end
                if segment_ok and _time.monotonic() >= session_end - 1.0:
                    break

                # Reconnect delay (user stop aborts wait)
                _live_status["status"] = "reconnecting"
                _push_live_to_clients()
                try:
                    await asyncio.wait_for(
                        _live_stop.wait(), timeout=float(reconnect_delay_s)
                    )
                    break  # stop requested during delay
                except asyncio.TimeoutError:
                    continue  # reconnect

            # Final status
            if _live_stop.is_set():
                db.end_session(sid, "ok")
                _live_status["status"] = "stopped"
                _live_status["error"] = ""
            elif int(_live_status.get("vitals_count") or 0) == 0:
                _live_status["error"] = (
                    "No SpO2/PR packets. Use brand=Masimo, finger in sensor, "
                    "Pair once, then Live (not Sync). Keep probe on during stream."
                )
                db.end_session(sid, "fail", error=_live_status["error"])
                _live_status["status"] = "error"
            else:
                db.end_session(sid, "ok")
                _live_status["status"] = "stopped"
                _live_status["error"] = ""
        except Exception as exc:  # noqa: BLE001
            log.exception("live failed")
            db.end_session(sid, "fail", error=str(exc))
            _live_status["status"] = "error"
            _live_status["error"] = str(exc)
        finally:
            _live_status["active"] = False
            _push_live_to_clients()

    _live_task = asyncio.create_task(_runner())
    return {
        "ok": True,
        "mac": mac_u,
        "brand": brand_id,
        "session_id": sid,
        "message": (
            "Live STREAM started — WebSocket push + auto-reconnect on drop. "
            "Keep finger in sensor. Use Live stop to end."
        ),
        "streaming": brand_id == "masimo",
        "auto_reconnect": bool(auto_reconnect) and brand_id == "masimo",
    }


async def job_live_stop() -> Dict[str, Any]:
    global _live_task
    _live_stop.set()
    if _live_task and not _live_task.done():
        try:
            await asyncio.wait_for(_live_task, timeout=15.0)
        except asyncio.TimeoutError:
            _live_task.cancel()
    _live_status["active"] = False
    if _live_status.get("status") not in ("error",):
        _live_status["status"] = "stopped"
    return {"ok": True, "status": live_status()}

"""
BLE job adapters — call medical_ble_toolkit without modifying it.
"""

from __future__ import annotations

import asyncio
import logging
import sys
from contextlib import asynccontextmanager
from datetime import datetime
from pathlib import Path
from typing import Any, Callable, Dict, List, Optional

# Parent experiments/ on path so medical_ble_toolkit imports work
_EXPERIMENTS = Path(__file__).resolve().parent.parent
if str(_EXPERIMENTS) not in sys.path:
    sys.path.insert(0, str(_EXPERIMENTS))

from brands import get_brand, resolve_profile_id  # noqa: E402
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
    elif d.get("blood_glucose_mg_dl") is not None or d.get("concentration") is not None:
        row["reading_type"] = "glucose"
        row["glucose_mg_dl"] = _f(
            d.get("blood_glucose_mg_dl", d.get("concentration"))
        )
        if d.get("is_control_solution"):
            row["reading_type"] = "meta"
    elif d.get("type") in (
        "ack",
        "nack",
        "device_info",
        "raw_message",
        "racp",
        "glucose_context",
        "glucose_context_raw",
    ):
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


@asynccontextmanager
async def _pause_hub_for_manual(reason: str = "manual"):
    """
    Yield radio to UI Scan/Pair: pause hub hunt, wait for concurrent workers
    to finish (or timeout), then hold _ble_lock so BlueZ is not double-scanned.
    """
    hub = _daemon_hub
    if hub is not None:
        try:
            hub.request_manual_pause()
        except Exception as exc:  # noqa: BLE001
            log.debug("hub pause: %s", exc)
        # Wait until no concurrent workers (up to ~120s for MightySat stream)
        for _ in range(400):
            try:
                n = 0
                if hasattr(hub, "conn_mgr"):
                    n = hub.conn_mgr.active_count
                phase = (hub.status.phase or "") if hub else ""
            except Exception:
                n = 0
                phase = ""
            if n <= 0 and phase not in ("session", "concurrent"):
                break
            await asyncio.sleep(0.3)
        await asyncio.sleep(0.35)
    try:
        async with _ble_lock:
            log.info("[RADIO] manual op start (%s)", reason)
            yield
            log.info("[RADIO] manual op end (%s)", reason)
    finally:
        if hub is not None:
            try:
                hub.clear_manual_pause()
            except Exception as exc:  # noqa: BLE001
                log.debug("hub unpause: %s", exc)


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
            if brand_id:
                profile = get_profile(brand["connect_profile"])
        except KeyError:
            profile = None

    use_profile = profile if brand_id else None
    devices: List[Any] = []
    last_err: Optional[BaseException] = None

    async with _pause_hub_for_manual("scan"):
        for attempt in (1, 2):
            try:
                devices = await scan_devices(
                    profile=use_profile,
                    timeout=timeout,
                    retries=2,
                )
                last_err = None
                if devices:
                    break
                # Empty list can mean BlueZ busy returned [] — brief retry
                if attempt == 1:
                    await asyncio.sleep(0.8)
            except Exception as exc:  # noqa: BLE001
                last_err = exc
                log.warning("job_scan attempt %d: %s", attempt, exc)
                await asyncio.sleep(0.8)

    out: List[Dict[str, Any]] = []
    for d in devices or []:
        out.append(
            {
                "mac": (getattr(d, "address", "") or "").upper(),
                "name": (getattr(d, "name", None) or "") or "(no name)",
                "rssi": getattr(d, "rssi", None),
            }
        )
    # Prefer named / medical-looking first for UI
    def _rank(row: Dict[str, Any]) -> tuple:
        n = (row.get("name") or "").lower()
        named = 0 if n and n != "(no name)" else 1
        return (named, -(row.get("rssi") or -999))

    out.sort(key=_rank)
    db.save_scan_hits(out)
    if not out and last_err is not None:
        raise BleJobError(
            f"Scan failed: {last_err}",
            code="SCAN_BUSY",
            tips=[
                "Hub was using the radio — try Scan again.",
                "Only one BLE scan at a time on this Pi.",
            ],
            retryable=True,
        )
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
            "FE4A missing = OS cannot open Omron private service (bond/mode).",
            "If Sync usually works without a button: wait 2s and Sync again.",
            "If still failing: SHORT-press BT (transfer), then Sync immediately.",
            "Long-hold flashing P is for PAIR only, not every Sync.",
            "Persistent fail: remove OS bond (bluetoothctl remove MAC or OS Settings) → Re-pair → Sync.",
            "Phone OMRON Connect must not own the bond (one host only).",
        ],
    )


async def job_pair(
    *,
    brand_id: str,
    mac: str,
    model: str = "",
    name: str = "",
    repair: bool = False,
) -> Dict[str, Any]:
    brand = get_brand(brand_id)
    if not brand:
        raise ValueError(f"Unknown brand: {brand_id}")

    model = model or brand.get("default_model") or ""
    mac_u = mac.strip().upper()
    adv_name = (name or model or brand.get("default_model") or "").strip()
    profile_id = resolve_profile_id(brand, model)
    device = db.upsert_device(
        brand=brand_id,
        mac=mac_u,
        model=model,
        company=brand.get("company", ""),
        name=adv_name or model,
    )
    sid = db.start_session(
        "repair" if repair else "pair",
        device_id=device.get("id"),
    )

    try:
        # Pause hub hunt so BlueZ is free for OS pair / GATT
        async with _pause_hub_for_manual("repair" if repair else "pair"):
            if brand.get("is_omron"):
                from medical_ble_toolkit.omron_bridge import pair_omron

                await pair_omron(mac_u, model, force_rebind=repair)
            else:
                await _generic_pair(
                    brand_id=brand_id,
                    profile_id=profile_id,
                    mac=mac_u,
                    model=model,
                    force_rebind=repair,
                )
        db.upsert_device(
            brand=brand_id,
            mac=mac_u,
            model=model,
            company=brand.get("company", ""),
            name=adv_name or model,
            paired=True,
        )
        # Nipro companion registry (exact name + CheckPairing id)
        if brand.get("is_nipro") or brand_id.startswith("nipro"):
            try:
                from medical_ble_toolkit.nipro.registry import register_meter

                register_meter(
                    device_id=mac_u,
                    name=adv_name or model,
                    profile_id=profile_id,
                    address=mac_u,
                    serial="",
                )
            except Exception as exc:  # noqa: BLE001
                log.warning("nipro registry register skip: %s", exc)
        db.end_session(sid, "ok")
        out: Dict[str, Any] = {
            "ok": True,
            "mac": mac_u,
            "brand": brand_id,
            "profile": profile_id,
            "session_id": sid,
        }
        # Hub-only bond policy (dedicated Pi collector)
        hub_tips = [
            "Pair ONLY with this hub — unpair any phone companion first.",
            "Hub owns the bond; leave Auto-sync ON after Pair.",
        ]
        if brand.get("is_omron"):
            out["next_steps"] = hub_tips + [
                "Omron: hub dumps history every omron_poll_interval_s "
                "(default 5 min — edit hub_config.json).",
                "If FE4A missing: RE-PAIR on hub with cuff flashing P.",
            ]
            await asyncio.sleep(1.5)
        elif brand.get("is_nipro") or brand_id.startswith("nipro") or brand_id == "thermo":
            out["next_steps"] = hub_tips + [
                "Measure on device — hub connects within ~1m05s BLE window.",
                "No phone; store exact advertised name at Pair.",
            ]
        elif brand_id == "masimo":
            out["next_steps"] = hub_tips + [
                "Put finger in sensor — hub starts full live SpO2 stream on AD.",
            ]
        else:
            out["next_steps"] = hub_tips
            
        # Hands-free: start auto-sync so the next measurement is pulled
        # without UI Sync (device must advertise / stay connectable).
        try:
            await job_daemon_start()
            out["auto_sync"] = True
            out["next_steps"] = list(out.get("next_steps") or []) + [
                "Auto-sync is ON — measure on the device; data should appear without Sync.",
            ]
        except Exception as exc:
            log.warning("Auto-start auto-sync failed: %s", exc)
            out["auto_sync"] = False
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
    from medical_ble_toolkit.common.winrt_errors import os_pair_supported
    from medical_ble_toolkit.profiles import get_profile

    if force_rebind and os_pair_supported():
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
    # Bond on Windows/Linux for medical meters that use encrypted GATT
    need_pair = os_pair_supported() and brand_id not in ("nipro_nt100b", "thermo")
    # short pair session for Nipro companion (register + light connect)
    duration = 12.0 if brand_id == "masimo" else 8.0
    if profile_id.startswith("nipro_") or profile_id == "mightysat":
        duration = 10.0
    client = MedicalBleClient(
        address=mac,
        profile=profile,
        pair=need_pair,
        connect_retries=2,
        auto_dispatch=profile_id in ("re_generic", "fora6"),
    )
    await client.run(duration=duration, connect_timeout=35.0)


@asynccontextmanager
async def _maybe_ble_lock(exclusive: bool):
    """Exclusive radio for manual Sync; hub concurrent workers skip this."""
    if exclusive:
        async with _ble_lock:
            yield
    else:
        yield


async def job_sync(
    *,
    brand_id: str,
    mac: str,
    model: str = "",
    listen_s: float = 30.0,
    on_reading_cb: Optional[Callable[[], None]] = None,
    exclusive_radio: bool = True,
    stream_good_hold_s: Optional[float] = None,
    stream_invalid_exit_s: Optional[float] = None,
    stream_no_data_grace_s: float = 8.0,
) -> Dict[str, Any]:
    """
    One-shot read/sync for the brand; store clinical readings in SQLite.

    exclusive_radio=True  — hold global BLE lock (UI / single-session).
    exclusive_radio=False — concurrent hub workers (multi BleakClient).
    stream_*: MightySat hub duty-cycle (valid SpO2 hold / "-" exit).
    """
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
    listen_end = ""

    try:
        async with _maybe_ble_lock(exclusive_radio):
            if brand.get("is_omron"):
                from medical_ble_toolkit.omron_bridge import (
                    flatten_readings,
                    read_omron,
                )

                # Direct MAC connect (Win11 parity). Omron BLE often stays up for
                # hours after last use — no button / no active measurement required.
                last_omron_exc: Optional[BaseException] = None
                all_users = None
                for attempt in (1, 2):
                    try:
                        if attempt == 2:
                            log.warning(
                                "Omron READ retry 2/2 — direct reconnect "
                                "(cuff may still be in long post-session window)…"
                            )
                            await asyncio.sleep(1.5)
                        all_users = await read_omron(
                            mac_u,
                            model,
                            find_timeout=15.0,  # direct path; scan not required
                            session_retries=2,
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
                skipped_dup = 0
                for r in flat:
                    row = _reading_to_row(r, brand_id)
                    if not _is_clinical(row):
                        continue
                    rid = db.insert_reading(
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
                        dedupe=True,
                    )
                    if rid is None:
                        skipped_dup += 1
                        collected.append(row)  # still show on dashboard
                        continue
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
                    if row["reading_type"] == "waveform":
                        continue
                    if _is_clinical(row) or row.get("payload"):
                        rid = db.insert_reading(
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
                            dedupe=_is_clinical(row),
                        )
                        if _is_clinical(row):
                            collected.append(row)
                            if rid is not None:
                                stored += 1
                if not result.ok and stored == 0:
                    raise RuntimeError(result.message or str(result.status))
            else:
                from medical_ble_toolkit.ble_client import MedicalBleClient
                from medical_ble_toolkit.common.winrt_errors import os_pair_supported
                from medical_ble_toolkit.profiles import get_profile

                profile_id = resolve_profile_id(brand, model)
                profile = get_profile(profile_id)
                is_thermo = brand_id in (
                    "thermo",
                    "thermometer",
                    "nipro_nt100b",
                    "nipro_nsm1",
                )
                is_nipro_bp = brand_id in ("nipro_nbp", "nipro_nmbp") or profile_id in (
                    "nipro_nbp",
                    "nipro_nmbp",
                )
                is_nipro_cf = brand_id == "nipro_cf" or profile_id == "nipro_cf"
                # Session length by brand (companion defaults ~60s receive)
                if brand_id == "masimo":
                    # Stream while finger on; cap so hub can return to hunt
                    duration = max(20.0, min(float(listen_s), 180.0))
                elif is_thermo or is_nipro_bp or is_nipro_cf or brand_id == "and":
                    # Companion receive timeout ~60s; quiet-end finishes earlier
                    duration = max(45.0, float(listen_s))
                else:
                    duration = listen_s

                # Never re-pair mid post-measure window (eats BLE time).
                # NBP still needs OS bond once — soft pair only if never paired.
                dev_row = db.get_device_by_mac(mac_u) or {}
                already_paired = bool(dev_row.get("paired"))
                if is_thermo or brand_id in ("nipro_nt100b", "thermo", "nipro_nbp"):
                    do_pair = False
                elif profile_id in ("nipro_nmbp", "and_ua651"):
                    do_pair = os_pair_supported() and not already_paired
                elif brand_id.startswith("nipro") or brand.get("is_nipro"):
                    do_pair = os_pair_supported() and not already_paired
                else:
                    do_pair = os_pair_supported() and not already_paired

                def _live_cb(r: Any) -> None:
                    nonlocal stored
                    row = _reading_to_row(r, brand_id)
                    if row["reading_type"] == "waveform":
                        return
                    if brand_id == "and" and not _is_clinical(row):
                        return
                    if (is_thermo or is_nipro_bp or is_nipro_cf) and not _is_clinical(
                        row
                    ):
                        return
                    if _is_clinical(row) or row["reading_type"] in ("meta", "raw"):
                        if row["reading_type"] == "meta":
                            return
                        rid = db.insert_reading(
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
                            dedupe=_is_clinical(row),
                        )
                        if _is_clinical(row):
                            collected.append(row)
                            if rid is not None:
                                stored += 1
                                # Always refresh dashboard on new clinical row
                                _push_dashboard(highlight_mac=mac_u)
                            if on_reading_cb:
                                on_reading_cb()

                # Post-measure: hub already saw AD → short connect retries only.
                # Long find_window eats the ~65s BLE window (NBP got 0 readings).
                find_to = 0.0
                name_hint = (
                    (dev_row.get("name") or model or brand.get("default_model") or "")
                ).strip()
                connect_retries = 2 if not is_thermo else 1
                connect_to = 30.0 if is_thermo else 35.0
                if (
                    brand.get("is_nipro")
                    or brand_id.startswith("nipro")
                    or brand_id in ("masimo", "thermo")
                ):
                    from medical_ble_toolkit.nipro import post_measure as pm

                    if brand_id != "masimo":
                        duration = max(duration, pm.receive_s_for(profile_id))
                    # Brief retry only (device just advertised)
                    find_to = 12.0 if (is_nipro_bp or is_thermo or brand_id == "masimo") else 0.0
                    connect_retries = 4 if find_to > 0 else connect_retries
                    connect_to = 12.0 if find_to > 0 else connect_to
                    log.info(
                        "[POST-MEASURE] web sync find=%.0fs receive=%.0fs profile=%s "
                        "pair=%s",
                        find_to,
                        duration,
                        profile_id,
                        do_pair,
                    )

                client = MedicalBleClient(
                    address=mac_u,
                    profile=profile,
                    pair=do_pair,
                    connect_retries=connect_retries,
                    on_reading=_live_cb,
                    auto_dispatch=profile_id in ("re_generic", "fora6"),
                    find_timeout=find_to,
                    name_hint=name_hint,
                )
                from medical_ble_toolkit.nipro import post_measure as pm2

                # MightySat: no quiet-end (stream until radio drops / duration)
                # Hub passes stream_good_hold_s / stream_invalid_exit_s for duty-cycle.
                if brand_id == "masimo" or profile_id == "mightysat":
                    qt = 0.0
                else:
                    qt = pm2.quiet_s_for(profile_id)
                await client.run(
                    duration=duration,
                    connect_timeout=connect_to,
                    quiet_timeout=qt,
                    stream_good_hold_s=stream_good_hold_s,
                    stream_invalid_exit_s=stream_invalid_exit_s,
                    stream_no_data_grace_s=stream_no_data_grace_s,
                )
                listen_end = getattr(client, "_listen_end_reason", "") or ""
                # Ensure Nipro registry has exact name for hands-free
                if brand.get("is_nipro") or brand_id.startswith("nipro"):
                    try:
                        from medical_ble_toolkit.nipro.registry import register_meter

                        register_meter(
                            device_id=mac_u,
                            name=(
                                (dev_row.get("name") or model or brand.get("default_model") or "")
                            ).strip(),
                            profile_id=profile_id,
                            address=mac_u,
                        )
                    except Exception as exc:  # noqa: BLE001
                        log.debug("nipro registry upsert: %s", exc)
                # Data is now inserted via _live_cb during the run

        latest = _pick_newest(collected)
        db.end_session(sid, "ok")
        # Compact rows for UI (no huge payload blobs)
        ui_rows: List[Dict[str, Any]] = []
        for row in collected:
            ui_rows.append(
                {
                    "brand": brand_id,
                    "mac": mac_u,
                    "reading_type": row.get("reading_type"),
                    "measured_at": row.get("measured_at"),
                    "systolic": row.get("systolic"),
                    "diastolic": row.get("diastolic"),
                    "pulse_rate": row.get("pulse_rate"),
                    "spo2": row.get("spo2"),
                    "perfusion_index": row.get("perfusion_index"),
                    "temperature": row.get("temperature"),
                    "glucose_mg_dl": row.get("glucose_mg_dl"),
                }
            )
        out: Dict[str, Any] = {
            "ok": True,
            "mac": mac_u,
            "brand": brand_id,
            "stored": stored,
            "total": len(collected),
            "skipped_duplicates": max(0, len(collected) - stored),
            "latest": latest,
            "readings": ui_rows[:80],  # this sync batch for immediate dashboard
            "session_id": sid,
            "listen_end_reason": listen_end,
            "had_valid_spo2": bool(
                any(
                    r.get("spo2") is not None
                    for r in collected
                    if isinstance(r, dict)
                )
            )
            if brand_id == "masimo"
            else None,
        }
        if brand_id in ("thermo", "thermometer", "nipro_nt100b") and stored == 0:
            out["warning"] = (
                "NT-100B stored 0 temps. Device BLE is usually OFF until you take "
                "a forehead reading; advertising lasts ~1–2 minutes only. "
                "Workflow: measure → (optional Pair once) → Sync immediately while "
                "BLE is on. Companion path uses HTP indicate + power-off."
            )
            out["tips"] = [
                "1. Take a temperature measurement on the NT-100B first.",
                "2. Within ~2 minutes (while BLE is advertising), click Sync.",
                "3. Pair only once; re-pairing every Sync can miss the window.",
                "4. Prefer brand 'Nipro NT-100B (companion)' not TICD lab.",
            ]
        if brand_id in ("nipro_nbp", "nipro_nmbp") and stored == 0:
            out["tips"] = [
                "1. Complete a BP measurement on the cuff first.",
                "2. Keep the cuff near the PC and click Sync (history indicates after clock).",
                "3. NMBP: Pair once (OS bond) if indications never arrive.",
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
    store_every_clinical: bool = False,
) -> Dict[str, Any]:
    """
    Start background LIVE stream (Masimo SpO2 etc.).

    Unlike Sync (one-shot history dump), this keeps the BLE link open and
    updates /live/latest + WebSocket on every clinical packet (~1 Hz SpO2).

    auto_reconnect: if the link drops or SpO2 goes silent, reconnect until
    Live stop or max_reconnects (streaming brands only by default).
    """
    global _live_task

    if _cycle_task and not _cycle_task.done():
        raise RuntimeError(
            "Multi-device cycle is running — stop Cycle first, then Live"
        )

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
        from medical_ble_toolkit.common.winrt_errors import os_pair_supported
        from medical_ble_toolkit.profiles import get_profile

        last_db_mono = 0.0
        last_spo2_key = ""
        last_vital_mono = 0.0
        profile = get_profile(resolve_profile_id(brand, model))
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
            # Full stream (hub): store every clinical packet; else throttle ~5s
            should_store = store_every_clinical or (
                key != last_spo2_key or (mono - last_db_mono) >= 5.0
            )
            if should_store:
                last_spo2_key = key
                last_db_mono = mono
                try:
                    rid = db.insert_reading(
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
                        dedupe=not store_every_clinical,
                    )
                    if rid is not None:
                        _push_dashboard(highlight_mac=mac_u)
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
                do_pair = os_pair_supported() and not already_paired and attempt == 1
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
    # Keep last live packet for reference, but mark inactive so UI uses device history
    _push_live_to_clients()
    return {"ok": True, "status": live_status()}


# ---------------------------------------------------------------------------
# Multi-device auto-cycle (one radio at a time, all cards always visible)
# ---------------------------------------------------------------------------

_cycle_task: Optional[asyncio.Task] = None
_cycle_stop = asyncio.Event()
_cycle_status: Dict[str, Any] = {
    "active": False,
    "status": "idle",
    "slot_s": 30.0,
    "round": 0,
    "index": 0,
    "total": 0,
    "current_mac": "",
    "current_brand": "",
    "current_model": "",
    "message": "",
    "error": "",
    "last_results": [],  # per-device outcome of last pass
    "roster": [],  # [{mac, brand, model, company}]
    "updated_at": "",
}


def cycle_status() -> Dict[str, Any]:
    snap = dict(_cycle_status)
    snap["roster"] = list(_cycle_status.get("roster") or [])
    snap["last_results"] = list(_cycle_status.get("last_results") or [])
    return snap


def build_dashboard(
    *,
    macs: Optional[List[str]] = None,
    highlight_mac: str = "",
) -> Dict[str, Any]:
    """All devices side-by-side with latest clinical + cycle highlight."""
    board = db.dashboard_board(macs=macs)
    hm = (highlight_mac or "").strip().upper()
    cs = cycle_status()
    for card in board:
        mac = (card.get("mac") or "").upper()
        card["active"] = bool(cs.get("active") and mac == hm)
        card["slot_status"] = "reading" if card["active"] else "idle"
        
        card["online"] = False
        for res in cs.get("last_results", []):
            if (res.get("mac") or "").upper() == mac:
                card["online"] = bool(res.get("ok"))
                break
        # Attach compact vitals for UI
        latest = card.get("latest") or {}
        card["vitals"] = {
            "reading_type": latest.get("reading_type"),
            "measured_at": latest.get("measured_at") or latest.get("created_at"),
            "systolic": latest.get("systolic"),
            "diastolic": latest.get("diastolic"),
            "pulse_rate": latest.get("pulse_rate"),
            "spo2": latest.get("spo2"),
            "perfusion_index": latest.get("perfusion_index"),
            "temperature": latest.get("temperature"),
            "glucose_mg_dl": latest.get("glucose_mg_dl"),
        } if latest else None
    return {
        "ok": True,
        "board": board,
        "cycle": cs,
        "count": len(board),
    }


def _push_dashboard(highlight_mac: str = "") -> None:
    """Fan-out multi-device board (+ cycle state) to all WS clients."""
    if not _live_queues:
        return
    payload = {
        "type": "dashboard",
        "dashboard": build_dashboard(
            highlight_mac=highlight_mac or _cycle_status.get("current_mac") or ""
        ),
    }
    dead: List[asyncio.Queue] = []
    for q in list(_live_queues):
        try:
            if q.full():
                try:
                    q.get_nowait()
                except asyncio.QueueEmpty:
                    pass
            q.put_nowait(payload)
        except Exception:  # noqa: BLE001
            dead.append(q)
    for q in dead:
        unsubscribe_live(q)


def _cycle_now() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def _listen_for_brand(brand_id: str, slot_s: float) -> float:
    """How long to listen during a cycle slot (capped by slot)."""
    slot = max(5.0, float(slot_s))
    if brand_id == "masimo":
        return slot  # stream for full slot
    if brand_id == "and":
        return min(slot, 60.0)  # A&D may need longer indicate dump
    if brand_id in ("thermo", "thermometer"):
        return min(slot, 20.0)
    if brand_id == "omron":
        return min(slot, 45.0)
    return min(slot, 30.0)


async def job_cycle_start(
    *,
    macs: Optional[List[str]] = None,
    slot_s: float = 30.0,
    rounds: int = 0,
) -> Dict[str, Any]:
    """
    Auto-cycle: spend ``slot_s`` seconds on each saved device in turn.

    BLE is one radio — devices are read sequentially, but the dashboard
    always shows *all* devices' latest vitals together. Highlight follows the
    active slot.

    rounds: 0 = forever until stop; else stop after N full passes.
    """
    global _cycle_task, _live_task

    if _cycle_task and not _cycle_task.done():
        raise RuntimeError("Multi-device cycle already running — stop it first")

    # Live stream holds the radio — stop it first
    if _live_task and not _live_task.done():
        log.info("Stopping single-device live so multi-device cycle can start")
        await job_live_stop()

    devices = db.list_devices()
    if macs:
        want = {m.strip().upper() for m in macs if m and str(m).strip()}
        devices = [d for d in devices if (d.get("mac") or "").upper() in want]
    # Prefer paired devices; still include unpaired if explicitly listed
    if not devices:
        raise ValueError("No saved devices to cycle — Save devices first")

    roster: List[Dict[str, Any]] = []
    for d in devices:
        brand_id = (d.get("brand") or "").lower()
        if brand_id in ("re", "fora"):
            continue  # not useful for clinical cycle POC
        if not get_brand(brand_id):
            continue
        roster.append(
            {
                "mac": (d.get("mac") or "").upper(),
                "brand": brand_id,
                "model": d.get("model") or "",
                "company": d.get("company") or "",
                "name": d.get("name") or d.get("model") or "",
            }
        )
    if not roster:
        raise ValueError("No cycleable clinical devices in the roster")

    slot = max(10.0, min(float(slot_s), 120.0))
    max_rounds = max(0, int(rounds))

    _cycle_stop.clear()
    _cycle_status.update(
        {
            "active": True,
            "status": "starting",
            "slot_s": slot,
            "round": 0,
            "index": 0,
            "total": len(roster),
            "current_mac": "",
            "current_brand": "",
            "current_model": "",
            "message": f"Cycling {len(roster)} device(s), {slot:.0f}s each",
            "error": "",
            "last_results": [],
            "roster": roster,
            "updated_at": _cycle_now(),
            "max_rounds": max_rounds,
        }
    )
    _push_dashboard()

    async def _runner() -> None:
        import time as _time

        round_i = 0
        try:
            while not _cycle_stop.is_set():
                round_i += 1
                if max_rounds and round_i > max_rounds:
                    _cycle_status["status"] = "completed"
                    _cycle_status["message"] = f"Finished {max_rounds} round(s)"
                    break

                _cycle_status["round"] = round_i
                round_results: List[Dict[str, Any]] = []

                for idx, dev in enumerate(roster):
                    if _cycle_stop.is_set():
                        break

                    mac = dev["mac"]
                    brand_id = dev["brand"]
                    model = dev["model"]
                    slot_start = _time.monotonic()

                    _cycle_status.update(
                        {
                            "status": "reading",
                            "index": idx + 1,
                            "current_mac": mac,
                            "current_brand": brand_id,
                            "current_model": model,
                            "message": (
                                f"Round {round_i} · {idx + 1}/{len(roster)} · "
                                f"{brand_id} {model or mac} · slot {slot:.0f}s"
                            ),
                            "error": "",
                            "updated_at": _cycle_now(),
                        }
                    )
                    _push_dashboard(highlight_mac=mac)
                    log.info(
                        "Cycle slot brand=%s mac=%s slot=%.0fs round=%d",
                        brand_id,
                        mac,
                        slot,
                        round_i,
                    )

                    result_row: Dict[str, Any] = {
                        "mac": mac,
                        "brand": brand_id,
                        "model": model,
                        "ok": False,
                        "stored": 0,
                        "error": "",
                        "latest": None,
                    }
                    try:
                        listen = _listen_for_brand(brand_id, slot)
                        last_push = [0.0]
                        def _on_live():
                            now_t = _time.monotonic()
                            if now_t - last_push[0] > 0.5:
                                _push_dashboard(highlight_mac=mac)
                                last_push[0] = now_t
                            
                        # History dump / short stream for this device
                        sync_out = await job_sync(
                            brand_id=brand_id,
                            mac=mac,
                            model=model,
                            listen_s=listen,
                            on_reading_cb=_on_live,
                        )
                        result_row["ok"] = bool(sync_out.get("ok"))
                        result_row["stored"] = int(sync_out.get("stored") or 0)
                        result_row["total"] = int(sync_out.get("total") or 0)
                        result_row["latest"] = sync_out.get("latest")
                        result_row["skipped_duplicates"] = int(
                            sync_out.get("skipped_duplicates") or 0
                        )
                        _cycle_status["message"] = (
                            f"OK {brand_id} · stored {result_row['stored']} · "
                            f"holding slot until {slot:.0f}s"
                        )
                        _cycle_status["updated_at"] = _cycle_now()
                        _push_dashboard(highlight_mac=mac)
                    except BleJobError as exc:
                        result_row["error"] = str(exc)
                        _cycle_status["error"] = f"{brand_id}: {exc}"
                        log.warning("Cycle slot failed %s: %s", mac, exc)
                        _push_dashboard(highlight_mac=mac)
                    except Exception as exc:  # noqa: BLE001
                        result_row["error"] = f"{type(exc).__name__}: {exc}"
                        _cycle_status["error"] = result_row["error"]
                        log.warning("Cycle slot failed %s: %s", mac, exc)
                        _push_dashboard(highlight_mac=mac)

                    round_results.append(result_row)
                    _cycle_status["last_results"] = list(round_results)

                    # Done-then-move: minimum 10s dwell (except masimo uses full slot)
                    elapsed = _time.monotonic() - slot_start
                    target_dwell = slot if brand_id == "masimo" else max(10.0, elapsed)
                    remain = target_dwell - elapsed
                    
                    if remain > 0.2 and not _cycle_stop.is_set():
                        _cycle_status["status"] = "dwell"
                        _cycle_status["message"] = (
                            f"{brand_id} done · dwell {remain:.0f}s more "
                            f"({idx + 1}/{len(roster)})"
                        )
                        _push_dashboard(highlight_mac=mac)
                        try:
                            await asyncio.wait_for(_cycle_stop.wait(), timeout=remain)
                            break  # stop requested
                        except asyncio.TimeoutError:
                            pass

                if _cycle_stop.is_set():
                    break

                # Brief pause between rounds
                _cycle_status["status"] = "between_rounds"
                _cycle_status["current_mac"] = ""
                _cycle_status["message"] = f"Round {round_i} complete — next round…"
                _push_dashboard(highlight_mac="")
                try:
                    await asyncio.wait_for(_cycle_stop.wait(), timeout=2.0)
                    break
                except asyncio.TimeoutError:
                    continue

            if _cycle_stop.is_set():
                _cycle_status["status"] = "stopped"
                _cycle_status["message"] = "Cycle stopped"
            elif _cycle_status.get("status") != "completed":
                _cycle_status["status"] = "stopped"
        except Exception as exc:  # noqa: BLE001
            log.exception("cycle failed")
            _cycle_status["status"] = "error"
            _cycle_status["error"] = str(exc)
            _cycle_status["message"] = f"Cycle error: {exc}"
        finally:
            _cycle_status["active"] = False
            _cycle_status["current_mac"] = ""
            _cycle_status["updated_at"] = _cycle_now()
            if _cycle_status.get("status") not in ("completed", "error", "stopped"):
                _cycle_status["status"] = "stopped"
            _push_dashboard(highlight_mac="")

    _cycle_task = asyncio.create_task(_runner())
    return {
        "ok": True,
        "message": (
            f"Multi-device cycle started: {len(roster)} device(s), "
            f"{slot:.0f}s each, sequential BLE (all cards always visible)"
        ),
        "slot_s": slot,
        "roster": roster,
        "rounds": max_rounds or "infinite",
        "dashboard": build_dashboard(),
    }


async def job_cycle_stop() -> Dict[str, Any]:
    global _cycle_task
    _cycle_stop.set()
    if _cycle_task and not _cycle_task.done():
        try:
            await asyncio.wait_for(_cycle_task, timeout=20.0)
        except asyncio.TimeoutError:
            _cycle_task.cancel()
            try:
                await _cycle_task
            except (asyncio.CancelledError, Exception):  # noqa: BLE001
                pass
    _cycle_status["active"] = False
    if _cycle_status.get("status") not in ("error", "completed"):
        _cycle_status["status"] = "stopped"
    _cycle_status["message"] = "Cycle stopped"
    _cycle_status["current_mac"] = ""
    _cycle_status["updated_at"] = _cycle_now()
    _push_dashboard(highlight_mac="")
    return {"ok": True, "cycle": cycle_status(), "dashboard": build_dashboard()}


# ---------------------------------------------------------------------------
# Nipro registry + hands-free (companion-like)
# ---------------------------------------------------------------------------

_handsfree_task: Optional[asyncio.Task] = None
_handsfree_status: Dict[str, Any] = {
    "active": False,
    "status": "idle",
    "message": "",
    "readings": 0,
    "started_at": "",
    "updated_at": "",
}


def handsfree_status() -> Dict[str, Any]:
    return dict(_handsfree_status)


async def job_nipro_list() -> Dict[str, Any]:
    from medical_ble_toolkit.nipro.registry import list_meters

    meters = list_meters()
    return {
        "ok": True,
        "meters": [
            {
                "name": m.name,
                "category": m.category,
                "profile_id": m.profile_id,
                "id_nodash": m.id_nodash,
                "address": m.address,
                "serial": m.serial,
            }
            for m in meters
        ],
    }


async def job_nipro_register(
    *,
    mac: str,
    name: str,
    brand_id: str = "",
    model: str = "",
    serial: str = "",
) -> Dict[str, Any]:
    """Register device into Nipro hands-free registry without full BLE pair."""
    from medical_ble_toolkit.nipro.registry import register_meter

    brand = get_brand(brand_id) if brand_id else None
    profile_id = (
        resolve_profile_id(brand, model)
        if brand
        else (brand_id or "nipro_nbp")
    )
    if brand:
        profile_id = resolve_profile_id(brand, model or name)
    meter = register_meter(
        device_id=mac,
        name=name.strip(),
        profile_id=profile_id,
        address=mac.strip().upper(),
        serial=serial or "",
    )
    # Also save web device row
    if brand:
        db.upsert_device(
            brand=brand_id,
            mac=mac.strip().upper(),
            model=model or brand.get("default_model", ""),
            name=name.strip(),
            company=brand.get("company", ""),
            paired=True,
        )
    return {
        "ok": True,
        "meter": {
            "name": meter.name,
            "category": meter.category,
            "profile_id": meter.profile_id,
            "address": meter.address,
            "id_nodash": meter.id_nodash,
        },
    }


async def job_nipro_handsfree_start(
    *,
    duration_s: float = 3600.0,
    receive_timeout: float = 60.0,
    categories: Optional[List[str]] = None,
) -> Dict[str, Any]:
    """Background hands-free wait (uses BLE lock inside toolkit sessions)."""
    global _handsfree_task

    if _handsfree_task and not _handsfree_task.done():
        raise RuntimeError("Nipro hands-free already running — stop it first")
    if _live_task and not _live_task.done():
        raise RuntimeError("Live is running — stop Live first")
    if _cycle_task and not _cycle_task.done():
        raise RuntimeError("Cycle is running — stop Cycle first")

    from medical_ble_toolkit.nipro.handsfree import handsfree_wait
    from medical_ble_toolkit.nipro.registry import list_meters

    if not list_meters():
        raise ValueError(
            "No Nipro meters registered. Pair a Nipro brand device first "
            "(or POST /nipro/register with exact BLE name)."
        )

    _handsfree_status.update(
        {
            "active": True,
            "status": "running",
            "message": "Hands-free wait started",
            "readings": 0,
            "started_at": datetime.now().isoformat(timespec="seconds"),
            "updated_at": datetime.now().isoformat(timespec="seconds"),
        }
    )

    async def _runner() -> None:
        stored = 0
        try:

            def _on_reading(obj: Any) -> None:
                nonlocal stored
                row = _reading_to_row(obj, "nipro")
                if not _is_clinical(row):
                    return
                # Best-effort device id by MAC unknown in callback — store without device
                rid = db.insert_reading(
                    device_id=None,
                    session_id=None,
                    brand="nipro",
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
                    dedupe=True,
                )
                if rid is not None:
                    stored += 1
                _handsfree_status["readings"] = stored
                _handsfree_status["updated_at"] = datetime.now().isoformat(
                    timespec="seconds"
                )
                _handsfree_status["latest"] = {
                    k: row.get(k)
                    for k in (
                        "reading_type",
                        "measured_at",
                        "systolic",
                        "diastolic",
                        "pulse_rate",
                        "spo2",
                        "temperature",
                        "glucose_mg_dl",
                    )
                }
                _push_live_to_clients()

            # handsfree_wait holds BLE sessions; run outside _ble_lock (it uses client lock per session)
            # Use toolkit lock via nesting: handsfree creates MedicalBleClient which needs radio
            async with _ble_lock:
                # Re-enter: handsfree itself calls scan/connect — hold lock for whole wait
                await handsfree_wait(
                    duration=float(duration_s),
                    receive_timeout=float(receive_timeout),
                    categories=categories,
                    on_reading=_on_reading,
                )
            _handsfree_status["status"] = "completed"
            _handsfree_status["message"] = f"Done — stored ~{stored} clinical reading(s)"
        except Exception as exc:  # noqa: BLE001
            log.exception("hands-free failed")
            _handsfree_status["status"] = "error"
            _handsfree_status["message"] = str(exc)
        finally:
            _handsfree_status["active"] = False
            _handsfree_status["updated_at"] = datetime.now().isoformat(
                timespec="seconds"
            )
            _push_live_to_clients()
            _push_dashboard(highlight_mac="")

    _handsfree_task = asyncio.create_task(_runner())
    return {
        "ok": True,
        "message": f"Nipro hands-free started for {duration_s:.0f}s",
        "handsfree": handsfree_status(),
    }


async def job_nipro_handsfree_stop() -> Dict[str, Any]:
    global _handsfree_task
    # Cooperative stop not built into handsfree_wait — cancel task
    if _handsfree_task and not _handsfree_task.done():
        _handsfree_task.cancel()
        try:
            await _handsfree_task
        except (asyncio.CancelledError, Exception):  # noqa: BLE001
            pass
    _handsfree_status["active"] = False
    _handsfree_status["status"] = "stopped"
    _handsfree_status["message"] = "Hands-free stopped"
    _handsfree_status["updated_at"] = datetime.now().isoformat(timespec="seconds")
    return {"ok": True, "handsfree": handsfree_status()}

# ---------------------------------------------------------------------------
# Hub auto-sync — Tier-1 dedicated Pi collector
#   HUNT (scan) → SESSION (one device) → HUNT
#   NBP / NT / MightySat: connect on advertisement
#   Omron: editable poll (default 5 min) + opportunistic AD
#   Config: hub_config.json (repo root) or $MEDICAL_HUB_CONFIG
# ---------------------------------------------------------------------------

_daemon_task: Optional[asyncio.Task] = None
_daemon_hub: Any = None
_daemon_status: Dict[str, Any] = {
    "active": False,
    "status": "idle",
    "phase": "idle",
    "message": "",
    "updated_at": "",
    "last_mac": "",
    "last_brand": "",
    "last_result": "",
    "last_stored": 0,
    "last_reason": "",
    "scan_round": 0,
    "paired_count": 0,
    "omron_next_s": None,
    "config_omron_poll_s": 300.0,
    "concurrent_active": 0,
    "max_concurrent": 4,
    "active_sessions": [],
}


def daemon_status() -> Dict[str, Any]:
    return dict(_daemon_status)


def _daemon_now() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def _hub_roster() -> List[Dict[str, Any]]:
    """Paired Tier-1 devices from SQLite."""
    devices = db.list_devices()
    paired = [d for d in devices if d.get("paired")]
    if not paired:
        paired = [d for d in devices if d.get("mac") and d.get("brand")]
    return paired


async def _hub_run_session(target: Any) -> Dict[str, Any]:
    """
    Execute one brand session for the hub worker.

    exclusive_radio=False so multiple devices can hold GATT links at once
    (BlueZ multi-connect). Scan/Pair still pause the hub and take _ble_lock.
    """
    from medical_ble_toolkit.hub.config import load_hub_config
    from medical_ble_toolkit.hub.policy import is_stream

    cfg = load_hub_config()
    brand_id = (target.brand or "").lower()
    mac = (target.mac or "").upper()
    model = target.model or ""
    reason = getattr(target, "reason", "") or ""

    log.info(
        "[HUB] transfer brand=%s mac=%s model=%s reason=%s concurrent=1",
        brand_id,
        mac,
        model,
        reason,
    )
    _push_dashboard(highlight_mac=mac)

    try:
        if is_stream(brand_id) or brand_id == "masimo":
            # Duty-cycle: valid SpO2 up to good_hold_s, or "-" for invalid_exit_s → drop
            good_hold = float(
                getattr(cfg, "mightysat_good_hold_s", 20.0) or 20.0
            )
            inv_exit = float(
                getattr(cfg, "mightysat_invalid_exit_s", 5.0) or 5.0
            )
            grace = float(
                getattr(cfg, "mightysat_no_data_grace_s", 8.0) or 8.0
            )
            wall = min(
                float(cfg.mightysat_live_max_s),
                max(good_hold + inv_exit + 5.0, good_hold + 5.0),
            )
            log.info(
                "[HUB] MightySat duty-cycle mac=%s good_hold=%.0fs "
                "invalid_exit=%.0fs wall=%.0fs (finger must be in for values)",
                mac,
                good_hold,
                inv_exit,
                wall,
            )

            def _on_ms() -> None:
                _push_dashboard(highlight_mac=mac)

            result = await job_sync(
                brand_id="masimo",
                mac=mac,
                model=model or "MightySat",
                listen_s=wall,
                on_reading_cb=_on_ms,
                exclusive_radio=False,
                stream_good_hold_s=good_hold,
                stream_invalid_exit_s=inv_exit,
                stream_no_data_grace_s=grace,
            )
            stored = int(result.get("stored") or 0)
            had_valid = bool(result.get("had_valid_spo2"))
            end_reason = result.get("listen_end_reason") or ""
            if cfg.print_readings and result.get("latest"):
                log.info("[HUB][READING][live] %s", result.get("latest"))
            log.info(
                "[HUB] MightySat end reason=%s stored=%d had_valid=%s",
                end_reason or "unknown",
                stored,
                had_valid,
            )
            ok = bool(result.get("ok", True)) and (stored > 0 or had_valid)
            return {
                "ok": ok,
                "stored": stored,
                "readings": result.get("readings") or [],
                "latest": result.get("latest"),
                "error": None
                if ok
                else "no SpO2 samples (finger out / values were '-')",
                "session_id": result.get("session_id"),
                "listen_end_reason": end_reason,
                "had_valid_spo2": had_valid,
                # Signal hub daemon to hunt other devices next
                "prefer_others": True,
            }

        listen = cfg.receive_s(brand_id)

        def _on_reading() -> None:
            _push_dashboard(highlight_mac=mac)

        result = await job_sync(
            brand_id=brand_id,
            mac=mac,
            model=model,
            listen_s=listen,
            on_reading_cb=_on_reading,
            exclusive_radio=False,
        )
        if cfg.print_readings:
            for row in (result.get("readings") or [])[:30]:
                log.info("[HUB][READING] %s", row)
            if result.get("latest"):
                log.info("[HUB][LATEST] %s", result.get("latest"))
        return {
            "ok": bool(result.get("ok", True)),
            "stored": int(result.get("stored") or 0),
            "readings": result.get("readings") or [],
            "latest": result.get("latest"),
            "error": result.get("error"),
        }
    except Exception as exc:  # noqa: BLE001
        log.warning("[HUB] session error brand=%s mac=%s: %s", brand_id, mac, exc)
        return {"ok": False, "stored": 0, "error": str(exc), "readings": []}
    finally:
        _push_dashboard(highlight_mac="")


async def job_daemon_start(*, duration_s: float = 86400.0 * 7) -> Dict[str, Any]:
    """
    Tier-1 Pi hub auto-sync.

    Pair once on this hub (no phone) → leave running → measure.
    NBP / NT / MightySat: connect as soon as they advertise.
    Omron: every omron_poll_interval_s (default 300 = 5 min; edit hub_config.json).
    """
    global _daemon_task, _daemon_hub

    if _daemon_task and not _daemon_task.done():
        return {
            "ok": True,
            "message": "Hub auto-sync already running",
            "daemon": daemon_status(),
        }

    # One radio owner only
    if _handsfree_task and not _handsfree_task.done():
        await job_nipro_handsfree_stop()
    if _live_task and not _live_task.done():
        await job_live_stop()
    if _cycle_task and not _cycle_task.done():
        await job_cycle_stop()

    from medical_ble_toolkit.hub import HubDaemon, load_hub_config
    from medical_ble_toolkit.hub.policy import HUB_ONLY_PAIR_TIPS

    cfg = load_hub_config()

    def _on_status(st: Any) -> None:
        d = st.as_dict() if hasattr(st, "as_dict") else {}
        _daemon_status.update(
            {
                "active": bool(d.get("active")),
                "status": d.get("phase") or d.get("status") or "running",
                "phase": d.get("phase") or "",
                "message": d.get("message") or "",
                "updated_at": d.get("updated_at") or _daemon_now(),
                "last_mac": d.get("last_mac") or "",
                "last_brand": d.get("last_brand") or "",
                "last_result": d.get("last_result") or "",
                "last_stored": int(d.get("last_stored") or 0),
                "last_reason": d.get("last_reason") or "",
                "scan_round": int(d.get("scan_round") or 0),
                "paired_count": int(d.get("paired_count") or 0),
                "omron_next_s": d.get("omron_next_s"),
                "config_omron_poll_s": d.get("config_omron_poll_s")
                or cfg.omron_poll_interval_s,
                "concurrent_active": int(d.get("concurrent_active") or 0),
                "max_concurrent": int(
                    d.get("max_concurrent")
                    or getattr(cfg, "max_concurrent", 4)
                    or 4
                ),
                "active_sessions": list(d.get("active_sessions") or []),
            }
        )
        if d.get("last_mac"):
            _push_dashboard(highlight_mac=d.get("last_mac") or "")

    hub = HubDaemon(
        get_roster=_hub_roster,
        run_session=_hub_run_session,
        config=cfg,
        on_status=_on_status,
        reload_config_each_round=True,
        radio_lock=_ble_lock,  # share radio with UI Scan/Pair
    )
    _daemon_hub = hub

    max_c = int(getattr(cfg, "max_concurrent", 4) or 4)
    _daemon_status.update(
        {
            "active": True,
            "status": "running",
            "phase": "hunt",
            "message": (
                f"Hub ON — concurrent multi-connect (max {max_c}); "
                f"Omron every {cfg.omron_poll_interval_s:.0f}s; pair on hub only"
            ),
            "updated_at": _daemon_now(),
            "config_omron_poll_s": cfg.omron_poll_interval_s,
            "last_mac": "",
            "last_result": "",
            "last_stored": 0,
            "scan_round": 0,
            "concurrent_active": 0,
            "max_concurrent": max_c,
            "active_sessions": [],
        }
    )
    _push_dashboard(highlight_mac="")

    async def _runner() -> None:
        try:
            await hub.run(duration_s=duration_s)
        except asyncio.CancelledError:
            hub.request_stop()
            _daemon_status["status"] = "stopped"
            _daemon_status["phase"] = "stopped"
            _daemon_status["message"] = "Hub auto-sync stopped"
            raise
        except Exception as exc:
            log.exception("Hub daemon failed")
            _daemon_status["status"] = "error"
            _daemon_status["phase"] = "error"
            _daemon_status["message"] = str(exc)
        finally:
            _daemon_status["active"] = False
            _daemon_status["updated_at"] = _daemon_now()
            _push_dashboard(highlight_mac="")

    _daemon_task = asyncio.create_task(_runner())
    return {
        "ok": True,
        "message": (
            f"Hub auto-sync ON — concurrent multi-connect "
            f"(max {max_c} links); NBP/NT/MightySat on AD; "
            f"Omron every {cfg.omron_poll_interval_s:.0f}s "
            f"(edit hub_config.json)."
        ),
        "tips": list(HUB_ONLY_PAIR_TIPS),
        "daemon": daemon_status(),
        "config_path": "hub_config.json",
    }


async def job_daemon_stop() -> Dict[str, Any]:
    global _daemon_task, _daemon_hub
    if _daemon_hub is not None:
        try:
            _daemon_hub.request_stop()
        except Exception:
            pass
    if _daemon_task and not _daemon_task.done():
        _daemon_task.cancel()
        try:
            await _daemon_task
        except (asyncio.CancelledError, Exception):
            pass
    _daemon_task = None
    _daemon_hub = None
    _daemon_status["active"] = False
    _daemon_status["status"] = "stopped"
    _daemon_status["phase"] = "stopped"
    _daemon_status["message"] = "Hub auto-sync OFF"
    _daemon_status["updated_at"] = _daemon_now()
    return {"ok": True, "daemon": daemon_status()}

"""
BLE job adapters — call medical_ble_toolkit without modifying it.
"""

from __future__ import annotations

import asyncio
import logging
import re
import sys
from contextlib import asynccontextmanager
from datetime import datetime
from pathlib import Path
from typing import Any, Callable, Dict, List, Optional

# Parent experiments/ on path so medical_ble_toolkit imports work
_EXPERIMENTS = Path(__file__).resolve().parent.parent
if str(_EXPERIMENTS) not in sys.path:
    sys.path.insert(0, str(_EXPERIMENTS))

from brands import get_brand  # noqa: E402
import db  # noqa: E402
import medical_ble_toolkit.brands  # noqa: E402

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
    elif d.get("weight_kg") is not None or d.get("weight") is not None:
        row["reading_type"] = "weight"
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
    return row.get("reading_type") in ("bp", "spo2", "temp", "glucose", "weight")


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
    Yield radio to UI Scan/Pair: pause hub hunt, cancel workers, wait briefly
    for the shared radio lock, then run the manual op.

    Kept short on purpose — old path could block ~120s waiting for Mighty/Omron
    sessions, which made Pair feel broken.
    """
    hub = _daemon_hub
    # Scan needs radio fast; pair slightly longer for disconnect cleanup
    max_wait_s = 6.0 if reason == "scan" else 12.0
    if hub is not None:
        try:
            hub.request_manual_pause(cancel_workers=True)
        except TypeError:
            # Older hub without cancel_workers kw
            try:
                hub.request_manual_pause()
            except Exception as exc:  # noqa: BLE001
                log.debug("hub pause: %s", exc)
        except Exception as exc:  # noqa: BLE001
            log.debug("hub pause: %s", exc)
        deadline = asyncio.get_event_loop().time() + max_wait_s
        while asyncio.get_event_loop().time() < deadline:
            try:
                n = hub.conn_mgr.active_count if hasattr(hub, "conn_mgr") else 0
            except Exception:
                n = 0
            # Free as soon as no slots — do not key off phase (can be stale)
            if n <= 0:
                break
            await asyncio.sleep(0.15)
        await asyncio.sleep(0.15)
    try:
        # Unblock any stuck passkey agent from a previous failed pair
        try:
            from medical_ble_toolkit.brands.omron.ble.bluez_agent import (
                GLOBAL_PASSKEY_BROKER,
            )
            GLOBAL_PASSKEY_BROKER.cancel()
        except Exception:
            pass

        # Don't hang forever if another job holds the radio
        try:
            await asyncio.wait_for(_ble_lock.acquire(), timeout=max_wait_s)
        except asyncio.TimeoutError as exc:
            raise BleJobError(
                f"Radio busy after {max_wait_s:.0f}s — try again in a moment",
                code="RADIO_BUSY",
                tips=[
                    "Hub was mid-transfer; wait a few seconds and retry Scan/Pair.",
                    "Stop Auto-sync if it keeps holding the radio.",
                ],
                retryable=True,
            ) from exc
        try:
            log.info("[RADIO] manual op start (%s)", reason)
            yield
            log.info("[RADIO] manual op end (%s)", reason)
        finally:
            _ble_lock.release()
    finally:
        if hub is not None:
            try:
                hub.clear_manual_pause()
            except Exception as exc:  # noqa: BLE001
                log.debug("hub unpause: %s", exc)


# Substring hits that uniquely indicate medical meters (avoid "FT_Play" etc.)
_MEDICAL_NAME_SUBSTR = (
    "blesmart",
    "omron",
    "hem-",
    "nbp-1",
    "nbp_1",
    "nt-100",
    "nt100",
    "mightysat",
    "masimo",
    "beurer",
    "bm54",
    "po60",
    "gl50",
    "gl44",
    "gl48",
    "ft95",
    "ft85",
)

# Whole-name or prefix+digit patterns (Beurer BM54, GL50, FT95…)
_MEDICAL_SKU_RE = re.compile(
    r"^(BM|BC|GL|FT|PO|BF|ME|AS)\d",
    re.IGNORECASE,
)
_MEDICAL_OTHER_RE = re.compile(
    r"^(HEM[-_]?\d|NBP|NT[-_]?100|BLESmart)",
    re.IGNORECASE,
)


def _medical_rank(name: str) -> int:
    """
    Lower = higher priority in scan list.
      0 medical name match
      1 other named device
      2 anonymous / no name
    """
    n = (name or "").strip()
    if not n or n.lower() in ("(no name)", "-"):
        return 2
    n_l = n.lower()
    for h in _MEDICAL_NAME_SUBSTR:
        if h in n_l:
            return 0
    if _MEDICAL_SKU_RE.match(n) or _MEDICAL_OTHER_RE.match(n):
        return 0
    return 1


async def job_scan(
    *,
    brand_id: Optional[str] = None,
    timeout: float = 8.0,
) -> List[Dict[str, Any]]:
    """Scan nearby BLE devices; medical-looking names sorted first."""
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
    # One solid pass (timeout already 8–12s). Second full pass doubled latency.
    scan_timeout = max(4.0, min(float(timeout), 20.0))

    async with _pause_hub_for_manual("scan"):
        try:
            devices = await scan_devices(
                profile=use_profile,
                timeout=scan_timeout,
                retries=1,
            )
            last_err = None
        except Exception as exc:  # noqa: BLE001
            last_err = exc
            log.warning("job_scan: %s", exc)
            # Single short retry only on hard failure
            try:
                await asyncio.sleep(0.4)
                devices = await scan_devices(
                    profile=use_profile,
                    timeout=min(scan_timeout, 6.0),
                    retries=1,
                )
                last_err = None
            except Exception as exc2:  # noqa: BLE001
                last_err = exc2
                log.warning("job_scan retry: %s", exc2)

    out: List[Dict[str, Any]] = []
    for d in devices or []:
        mac = (getattr(d, "address", "") or "").upper()
        rssi = getattr(d, "rssi", None)
        if rssi is not None:
            try:
                rssi = int(rssi)
            except (TypeError, ValueError):
                rssi = None
        name = (getattr(d, "name", None) or "") or "(no name)"
        out.append(
            {
                "mac": mac,
                "address": mac,  # alias for older UI / clients
                "name": name,
                "rssi": rssi,
                "medical": _medical_rank(name) == 0,
            }
        )
    # Medical first, then named, then by strongest RSSI
    def _rank(row: Dict[str, Any]) -> tuple:
        return (
            _medical_rank(str(row.get("name") or "")),
            -(row.get("rssi") or -999),
        )

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


def _parse_passkey(value: Any) -> Optional[int]:
    if value is None or value == "":
        return None
    digits = "".join(c for c in str(value) if c.isdigit())
    if not digits:
        return None
    return int(digits[-6:]) if len(digits) > 6 else int(digits)


def pair_passkey_status() -> Dict[str, Any]:
    """UI poll: is BlueZ agent waiting for a 6-digit passkey?"""
    from medical_ble_toolkit.common.winrt_errors import is_windows
    if is_windows():
        return {
            "ok": True,
            "need_passkey": False,
            "passkey_via_ui": False,
            "message": "On Windows, enter the passkey in the Bluetooth pairing popup (check taskbar).",
        }
    try:
        from medical_ble_toolkit.brands.omron.ble.bluez_agent import GLOBAL_PASSKEY_BROKER

        st = GLOBAL_PASSKEY_BROKER.status()
        return {
            "ok": True,
            "need_passkey": bool(st.get("waiting")),
            "has_passkey": bool(st.get("has_passkey")),
            "device": st.get("device") or "",
            "passkey_via_ui": True,
            "message": (
                "Enter the 6-digit code shown on the cuff LCD"
                if st.get("waiting")
                else ""
            ),
        }
    except Exception as exc:  # noqa: BLE001
        return {"ok": False, "need_passkey": False, "error": str(exc)}


def provide_pair_passkey(passkey: Any) -> Dict[str, Any]:
    """UI submit: feed passkey to the in-flight BlueZ agent."""
    from medical_ble_toolkit.common.winrt_errors import is_windows
    if is_windows():
        raise RuntimeError(
            "Passkey entry via web UI is not supported on Windows. "
            "Enter the passkey in the Windows Bluetooth pairing popup (check your taskbar)."
        )
    pk = _parse_passkey(passkey)
    if pk is None:
        raise ValueError("Passkey must be 4–6 digits from the cuff display")
    from medical_ble_toolkit.brands.omron.ble.bluez_agent import GLOBAL_PASSKEY_BROKER

    GLOBAL_PASSKEY_BROKER.provide(pk)
    return {"ok": True, "passkey_set": True, "message": f"Passkey {pk:06d} sent to agent"}


async def job_pair(
    *,
    brand_id: str,
    mac: str,
    model: str = "",
    name: str = "",
    repair: bool = False,
    passkey: Any = None,
) -> Dict[str, Any]:
    # We kept the param name "brand_id" for backward compatibility with the HTTP route,
    # but the UI actually sends the `profile_id` (e.g. "nipro_nt100b", "mightysat").
    profile_id = brand_id
    from brands import get_brand
    brand_info = get_brand(profile_id)
    if not brand_info:
        raise ValueError(f"Unknown profile: {profile_id}")

    from medical_ble_toolkit.profiles import get_profile
    profile = get_profile(profile_id)

    pk = _parse_passkey(passkey)
    model = model or brand_info.get("default_model") or ""
    mac_u = mac.strip().upper()
    adv_name = (name or model or brand_info.get("default_model") or "").strip()

    # Remember if this MAC was already a real hub bond — failed pair must not
    # leave a ghost card on the dashboard (paired=0 stub from a prior attempt).
    prior = db.get_device_by_mac(mac_u)
    was_paired = bool(prior and prior.get("paired"))

    device = db.upsert_device(
        profile_id=profile_id,
        brand=brand_info.get("company", ""),
        mac=mac_u,
        model=model,
        name=adv_name or model,
    )
    sid = db.start_session(
        "repair" if repair else "pair",
        device_id=device.get("id"),
    )

    try:
        # Seed passkey broker early so UI can also POST /pair/passkey mid-flight
        if pk is not None or brand_info.get("is_beurer") or profile_id.startswith("beurer"):
            try:
                from medical_ble_toolkit.brands.omron.ble.bluez_agent import (
                    GLOBAL_PASSKEY_BROKER,
                )
                
                # First cancel any pending wait from a previous run
                GLOBAL_PASSKEY_BROKER.cancel()
                # Then reset to prime it for the new run
                GLOBAL_PASSKEY_BROKER.reset(preset=pk)
            except Exception as exc:  # noqa: BLE001
                log.debug("passkey broker reset: %s", exc)

        # Pause hub hunt so BlueZ is free for OS pair / GATT
        async with _pause_hub_for_manual("repair" if repair else "pair"):
            if brand_info.get("is_omron"):
                # Side-effect import registers OmronPlugin (see brands/__init__.py)
                import medical_ble_toolkit.brands  # noqa: F401
                from medical_ble_toolkit.core.registry import get_plugin

                res = await get_plugin(profile.brand).pair(mac_u, model, force_rebind=repair)
                if hasattr(res, "ok") and not res.ok:
                    raise BleJobError(
                        getattr(res, "error", "") or f"Omron pair failed for {mac_u}",
                        code="PAIR_FAILED",
                    )
            elif brand_info.get("is_beurer") or profile.brand == "beurer":
                import medical_ble_toolkit.brands  # noqa: F401
                from medical_ble_toolkit.core.registry import get_plugin

                res = await get_plugin("beurer").pair(
                    mac_u, model, force_rebind=repair, passkey=pk
                )
                if hasattr(res, "ok") and not res.ok:
                    raise BleJobError(
                        getattr(res, "error", "") or f"Beurer pair failed for {mac_u}",
                        code="PAIR_FAILED",
                    )
            else:
                await _generic_pair(
                    brand_id=profile.brand,
                    profile_id=profile_id,
                    mac=mac_u,
                    model=model,
                    force_rebind=repair,
                )
        db.upsert_device(
            profile_id=profile_id,
            brand=brand_info.get("company", ""),
            mac=mac_u,
            model=model,
            name=adv_name or model,
            paired=True,
        )
        # Nipro companion registry (exact name + CheckPairing id)
        if brand_info.get("is_nipro") or profile_id.startswith("nipro") or profile_id == "mightysat":
            try:
                if profile_id == "mightysat":
                    from medical_ble_toolkit.brands.nipro.registry import register_meter

                    register_meter(
                        device_id=mac_u,
                        name=adv_name or model,
                        profile_id=profile_id,
                        address=mac_u,
                        serial="",
                    )
                else:
                    from medical_ble_toolkit.core.registry import get_plugin
                    # The plugin registers the meter during pair()
                    await get_plugin(profile.brand).pair(mac_u, profile_id)
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
        if brand_info.get("is_omron"):
            out["next_steps"] = hub_tips + [
                "Omron: hub dumps history every omron_poll_interval_s "
                "(default 5 min — edit hub_config.json).",
                "If FE4A missing: RE-PAIR on hub with cuff flashing P.",
            ]
            await asyncio.sleep(1.5)
        elif brand_info.get("is_nipro") or profile_id.startswith("nipro") or profile_id == "thermo":
            out["next_steps"] = hub_tips + [
                "Measure on device — hub connects within ~1m05s BLE window.",
                "No phone; store exact advertised name at Pair.",
            ]
        elif profile_id == "mightysat":
            out["next_steps"] = hub_tips + [
                "Put finger in sensor — hub starts full live SpO2 stream on AD.",
            ]
        elif brand_info.get("is_beurer") or profile_id.startswith("beurer"):
            out["next_steps"] = hub_tips + [
                "Beurer: unpair phone Beurer app first (one bond).",
                "If the cuff shows a 6-digit code: type it in Passkey before/during Pair.",
                "Measure on the device — hub connects on advertisement and dumps history.",
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
        # Drop unpaired stub so failed Pair does not look like a hub device
        if not was_paired:
            try:
                db.delete_device(mac_u)
                log.info(
                    "[PAIR] removed unpaired stub mac=%s after fail: %s",
                    mac_u,
                    exc,
                )
            except Exception as del_exc:  # noqa: BLE001
                log.warning("delete unpaired stub %s: %s", mac_u, del_exc)
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
            from medical_ble_toolkit.omron_bridge import unpair_omron as unpair_address

            await unpair_address(mac)
            await asyncio.sleep(1.0)
        except Exception as exc:  # noqa: BLE001
            log.warning("unpair skip: %s", exc)

    if brand_id == "beurer":
        from medical_ble_toolkit.core.registry import get_plugin
        await get_plugin("beurer").pair(mac, model, force_rebind=force_rebind)
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
    mac: str,
    listen_s: float = 60.0,
    on_reading_cb: Optional[Callable[[], None]] = None,
    exclusive_radio: bool = True,
    stream_good_hold_s: float = 20.0,
    stream_invalid_exit_s: float = 8.0,
    stream_no_data_grace_s: float = 5.0,
    find_timeout: float = 12.0,
    # These legacy kwargs are ignored but kept so UI doesn't crash if it sends them
    brand_id: str = "",
    model: str = "",
) -> Dict[str, Any]:
    mac_u = mac.strip().upper()
    dev_row = db.get_device_by_mac(mac_u)
    if not dev_row:
        raise ValueError(f"Cannot sync unknown device {mac_u} — pair it first.")

    profile_id = dev_row["profile_id"]
    brand_id = dev_row["brand"]  # Display brand

    from medical_ble_toolkit.profiles import get_profile
    profile = get_profile(profile_id)
    plugin_brand = profile.brand

    model = dev_row.get("model") or ""
    already_paired = bool(dev_row.get("paired"))
    name_hint = (dev_row.get("name") or model).strip()
    
    device_id = dev_row.get("id")
    sid = db.start_session("sync", device_id=device_id)
    stored = 0
    collected: List[Dict[str, Any]] = []
    listen_end = ""

    try:
        async with _maybe_ble_lock(exclusive_radio):
            if plugin_brand == "omron":
                from medical_ble_toolkit.core.registry import get_plugin
                sync_result = await get_plugin("omron").run_session(mac_u, model)
                for r in sync_result.readings:
                    row = _reading_to_row(r, profile_id)
                    if not _is_clinical(row):
                        continue
                    rid = db.insert_reading(
                        device_id=device_id, session_id=sid, brand=profile_id,
                        reading_type=row["reading_type"], measured_at=row.get("measured_at"),
                        systolic=row.get("systolic"), diastolic=row.get("diastolic"),
                        pulse_rate=row.get("pulse_rate"), spo2=row.get("spo2"),
                        perfusion_index=row.get("perfusion_index"),
                        temperature=row.get("temperature"), glucose_mg_dl=row.get("glucose_mg_dl"),
                        payload=row.get("payload"), raw_hex=row.get("raw_hex") or "",
                        dedupe=True,
                    )
                    if rid is None:
                        collected.append(row)
                        continue
                    stored += 1
                    collected.append(row)
            elif plugin_brand == "beurer":
                from medical_ble_toolkit.core.registry import get_plugin
                sync_result = await get_plugin("beurer").run_session(mac_u, model)
                for r in sync_result.readings:
                    row = _reading_to_row(r, profile_id)
                    if row["reading_type"] == "waveform":
                        continue
                    if _is_clinical(row) or row.get("payload"):
                        rid = db.insert_reading(
                            device_id=device_id,
                            session_id=sid,
                            brand=profile_id,
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
                if not sync_result.ok and stored == 0:
                    raise RuntimeError(sync_result.detail.get("message") or "Beurer session failed")
            else:
                from medical_ble_toolkit.core.registry import get_plugin, has_plugin
                
                def _live_cb(r: Any) -> None:
                    nonlocal stored
                    row = _reading_to_row(r, profile_id)
                    if row["reading_type"] == "waveform":
                        return
                    if row["reading_type"] == "meta":
                        return
                    if _is_clinical(row) or row["reading_type"] == "raw":
                        rid = db.insert_reading(
                            device_id=device_id, session_id=sid, brand=profile_id,
                            reading_type=row["reading_type"], measured_at=row.get("measured_at"),
                            systolic=row.get("systolic"), diastolic=row.get("diastolic"),
                            pulse_rate=row.get("pulse_rate"), spo2=row.get("spo2"),
                            perfusion_index=row.get("perfusion_index"),
                            temperature=row.get("temperature"), glucose_mg_dl=row.get("glucose_mg_dl"),
                            payload=row.get("payload"), raw_hex=row.get("raw_hex") or "",
                            dedupe=_is_clinical(row),
                        )
                        if _is_clinical(row):
                            collected.append(row)
                            if rid is not None:
                                stored += 1
                                _push_dashboard(highlight_mac=mac_u)
                            if on_reading_cb:
                                on_reading_cb()

                if not has_plugin(plugin_brand):
                    raise BleJobError(
                        f"No plugin registered for brand '{plugin_brand}'. "
                        "Cannot sync — add a DevicePlugin for this brand.",
                        code="NO_PLUGIN",
                    )

                sync_result = await get_plugin(plugin_brand).run_session(
                    mac_u, model,
                    on_reading=_live_cb,
                    profile_id=profile_id,
                    listen_s=listen_s,
                    find_timeout=find_timeout,
                    already_paired=already_paired,
                    name_hint=name_hint,
                    stream_good_hold_s=stream_good_hold_s,
                    stream_invalid_exit_s=stream_invalid_exit_s,
                    stream_no_data_grace_s=stream_no_data_grace_s,
                )
                listen_end = sync_result.detail.get("listen_end", "")

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
        if profile_id in ("nipro_nt100b", "thermometer") and stored == 0:
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
        profile_id=brand.get("id") or brand_id,
        brand=brand.get("company") or brand_id,
        mac=mac_u,
        model=model,
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
    """All devices side-by-side with latest clinical."""
    board = db.dashboard_board(macs=macs)
    hm = (highlight_mac or "").strip().upper()
    for card in board:
        mac = (card.get("mac") or "").upper()
        card["active"] = bool(mac == hm and hm)
        card["slot_status"] = "reading" if card["active"] else "idle"
        card["online"] = card["active"]
        
        # Attach compact vitals for UI (Masimo: SpO2 + PR + PI + PVI + RRp)
        latest = card.get("latest") or {}
        if latest:
            payload: Dict[str, Any] = {}
            raw_payload = latest.get("payload_json")
            if isinstance(raw_payload, str) and raw_payload.strip():
                try:
                    import json as _json

                    parsed = _json.loads(raw_payload)
                    if isinstance(parsed, dict):
                        payload = parsed
                except Exception:  # noqa: BLE001
                    payload = {}
            elif isinstance(raw_payload, dict):
                payload = raw_payload
            card["vitals"] = {
                "reading_type": latest.get("reading_type"),
                "measured_at": latest.get("measured_at") or latest.get("created_at"),
                "systolic": latest.get("systolic"),
                "diastolic": latest.get("diastolic"),
                "pulse_rate": latest.get("pulse_rate")
                if latest.get("pulse_rate") is not None
                else _f(payload.get("pulse_rate")),
                "spo2": latest.get("spo2")
                if latest.get("spo2") is not None
                else _f(payload.get("spo2")),
                "perfusion_index": latest.get("perfusion_index")
                if latest.get("perfusion_index") is not None
                else _f(payload.get("perfusion_index")),
                "pvi": _f(payload.get("pvi")),
                "rrp": _f(payload.get("rrp")),
                "temperature": latest.get("temperature"),
                "glucose_mg_dl": latest.get("glucose_mg_dl"),
            }
        else:
            card["vitals"] = None
    return {
        "ok": True,
        "board": board,
        "count": len(board),
    }


def _push_dashboard(highlight_mac: str = "") -> None:
    """Fan-out multi-device board to all WS clients."""
    if not _live_queues:
        return
    payload = {
        "type": "dashboard",
        "dashboard": build_dashboard(highlight_mac=highlight_mac),
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


def _listen_for_brand(profile_id: str, slot_s: float) -> float:
    """How long to listen during a cycle slot (capped by slot)."""
    slot = max(5.0, float(slot_s))
    from medical_ble_toolkit.profiles import get_profile
    try:
        plugin_brand = get_profile(profile_id).brand
    except KeyError:
        return min(slot, 30.0)

    from medical_ble_toolkit.core.registry import has_plugin, get_plugin
    if has_plugin(plugin_brand):
        return get_plugin(plugin_brand).listen_s(slot)
    
    if plugin_brand == "masimo":
        return slot
    if plugin_brand == "and":
        return min(slot, 60.0)
    if plugin_brand == "thermo":
        return min(slot, 20.0)
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
        profile_id = (d.get("profile_id") or "").lower()
        if profile_id in ("re_generic", "fora6"):
            continue  # not useful for clinical cycle POC
        if not get_brand(profile_id):
            continue
        roster.append(
            {
                "mac": (d.get("mac") or "").upper(),
                "profile_id": profile_id,
                "brand": d.get("brand") or "",
                "model": d.get("model") or "",
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
                    profile_id = dev["profile_id"]
                    brand_id = dev["brand"]
                    model = dev["model"]
                    slot_start = _time.monotonic()

                    _cycle_status.update(
                        {
                            "status": "reading",
                            "index": idx + 1,
                            "current_mac": mac,
                            "current_brand": profile_id,
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
                        "Cycle slot profile=%s mac=%s slot=%.0fs round=%d",
                        profile_id,
                        mac,
                        slot,
                        round_i,
                    )

                    result_row: Dict[str, Any] = {
                        "mac": mac,
                        "brand": profile_id,
                        "model": model,
                        "ok": False,
                        "stored": 0,
                        "error": "",
                        "latest": None,
                    }
                    try:
                        listen = _listen_for_brand(profile_id, slot)
                        last_push = [0.0]
                        def _on_live(mac=mac):
                            now_t = _time.monotonic()
                            if now_t - last_push[0] > 0.5:
                                _push_dashboard(highlight_mac=mac)
                                last_push[0] = now_t
                            
                        # History dump / short stream for this device
                        sync_out = await job_sync(
                            mac=mac,
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
    from medical_ble_toolkit.brands.nipro.registry import list_meters

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
    from medical_ble_toolkit.brands.nipro.registry import register_meter

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
            profile_id=brand.get("id") or brand_id,
            brand=brand.get("company") or brand_id,
            mac=mac.strip().upper(),
            model=model or brand.get("default_model", ""),
            name=name.strip(),
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

    from medical_ble_toolkit.brands.nipro.handsfree import handsfree_wait
    from medical_ble_toolkit.brands.nipro.registry import list_meters

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
                # Best-effort device id by MAC
                mac = (row.get("payload", {}).get("address") or row.get("payload", {}).get("mac") or getattr(obj, "address", "") or "").upper()
                dev_id = None
                if mac:
                    dev_row = db.get_device_by_mac(mac)
                    if dev_row:
                        dev_id = dev_row.get("id")
                        
                rid = db.insert_reading(
                    device_id=dev_id,
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
#   Config: medical_ble_toolkit/hub_config.json (or cwd / $MEDICAL_HUB_CONFIG)
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


# profile_id (SQLite) → hub policy brand key (TIER1_BRANDS in hub/policy.py).
# Hub filters/matches on these ids — NOT the UI display brand ("Nipro", "Omron").
_PROFILE_TO_HUB_BRAND = {
    "omron": "omron",
    "hem7143t1": "omron",
    "nipro_nbp": "nipro_nbp",
    "nipro_nmbp": "nipro_nmbp",
    "nipro_nt100b": "nipro_nt100b",
    "thermometer": "thermo",
    "thermo": "thermo",
    "mightysat": "masimo",
    "masimo": "masimo",
    # Beurer family → single hub policy brand (plugin beurer / windowed)
    "beurer": "beurer",
    "beurer_bp": "beurer",
    "beurer_bm54": "beurer",
    "beurer_glucose": "beurer",
    "beurer_thermo": "beurer",
    "beurer_po60": "beurer",
    "beurer_scale": "beurer",
    "beurer_ecg": "beurer",
    "beurer_as87": "beurer",
    "beurer_as98": "beurer",
    "beurer_as99": "beurer",
    "beurer_tracker_legacy": "beurer",
    "beurer_hydration": "beurer",
}


def _hub_policy_brand(device: Dict[str, Any]) -> str:
    """
    Map a SQLite device row to the brand key hub/daemon expects.

    DB stores:
      profile_id = "nipro_nbp" | "omron" | "mightysat" | …
      brand      = display label ("Nipro", "Nipro / TaiDoc", "Omron")

    Hub TIER1_BRANDS is {omron, nipro_nbp, nipro_nt100b, masimo, …}.
    Using display brand drops every Nipro device (brand "nipro" ∉ TIER1).
    """
    pid = (device.get("profile_id") or "").strip().lower()
    if pid in _PROFILE_TO_HUB_BRAND:
        return _PROFILE_TO_HUB_BRAND[pid]
    # Any future beurer_* profile
    if pid.startswith("beurer"):
        return "beurer"
    if pid:
        return pid
    # Last resort: only works when display brand happens to match (e.g. "Omron")
    return (device.get("brand") or "").strip().lower()


def _hub_roster() -> List[Dict[str, Any]]:
    """Paired devices from SQLite only (never unpaired scan leftovers)."""
    devices = db.list_devices()
    out: List[Dict[str, Any]] = []
    for d in devices:
        if not d.get("paired") or not d.get("mac"):
            continue
        brand = _hub_policy_brand(d)
        if not brand:
            continue
        row = dict(d)
        row["brand"] = brand
        row["mac"] = (d.get("mac") or "").strip().upper()
        out.append(row)
    return out


async def _hub_run_session(target: Any) -> Dict[str, Any]:
    """
    Execute one brand session for the hub worker.

    exclusive_radio=False so multiple devices can hold GATT links at once
    (BlueZ multi-connect). Scan/Pair still pause the hub and take _ble_lock.
    """
    from medical_ble_toolkit.hub.config import load_hub_config
    from medical_ble_toolkit.core.registry import has_plugin, get_plugin
    from medical_ble_toolkit.core.device_plugin import DeviceClass

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
        _is_stream = has_plugin(brand_id) and get_plugin(brand_id).device_class == DeviceClass.STREAM
        if _is_stream:
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
                # Hub already matched AD — direct MAC connect (no nested scan)
                find_timeout=0.0,
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
            # Hub roster MAC already known — avoid parallel BlueZ discovery
            find_timeout=0.0,
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
        "config_path": "medical_ble_toolkit/hub_config.json",
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

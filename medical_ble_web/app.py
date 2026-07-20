"""
Medical BLE Web POC — local FastAPI backend.

  cd experiments/medical_ble_web
  pip install -r requirements.txt
  python app.py
  → http://127.0.0.1:8741

Does not modify medical_ble_toolkit CLI.
"""

from __future__ import annotations

import asyncio
import logging
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional

from fastapi import FastAPI, HTTPException, Query, WebSocket, WebSocketDisconnect
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

ROOT = Path(__file__).resolve().parent
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))
# Parent experiments for medical_ble_toolkit
_EXP = ROOT.parent
if str(_EXP) not in sys.path:
    sys.path.insert(0, str(_EXP))

import db  # noqa: E402
from brands import get_brand, list_brands  # noqa: E402
from ble_jobs import (  # noqa: E402
    BleJobError,
    build_dashboard,
    cycle_status,
    daemon_status,
    handsfree_status,
    job_cycle_start,
    job_cycle_stop,
    job_daemon_start,
    job_daemon_stop,
    job_live_start,
    job_live_stop,
    job_nipro_handsfree_start,
    job_nipro_handsfree_stop,
    job_nipro_list,
    job_nipro_register,
    job_pair,
    job_scan,
    job_sync,
    live_status,
    subscribe_live,
    unsubscribe_live,
)

logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] %(levelname)-7s %(name)s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger("medical_ble_web")

app = FastAPI(
    title="Medical BLE Web POC",
    description="Local-only proof of concept. CLI toolkit remains separate.",
    version="0.1.0",
)

STATIC = ROOT / "static"
if STATIC.is_dir():
    app.mount("/static", StaticFiles(directory=str(STATIC)), name="static")


@app.on_event("startup")
async def _startup() -> None:
    db.init_db()
    log.info("SQLite ready: %s", db.DB_PATH)
    log.info("Open http://127.0.0.1:8741")
    # MQTT cloud transfer (Android hub drop-in) — hard-coded hub/patient in mqtt_config.json
    try:
        import mqtt_bridge

        if mqtt_bridge.start():
            log.info("MQTT bridge started: %s", mqtt_bridge.status())
        else:
            log.info("MQTT bridge off or broker unreachable: %s", mqtt_bridge.status())
    except Exception as exc:  # noqa: BLE001
        log.warning("MQTT bridge start failed: %s", exc)
    # Hands-free: if any device is already paired, start auto-sync
    try:
        devices = db.list_devices()
        paired = [d for d in devices if d.get("paired")]
        if paired:
            await job_daemon_start()
            log.info(
                "Auto-sync started for %d paired device(s)",
                len(paired),
            )
        else:
            log.info("No paired devices yet — auto-sync idle until Pair")
    except Exception as exc:  # noqa: BLE001
        log.warning("Could not auto-start auto-sync: %s", exc)


@app.on_event("shutdown")
async def _shutdown() -> None:
    try:
        import mqtt_bridge

        mqtt_bridge.stop()
    except Exception:
        pass


# ---------------------------------------------------------------------------
# Models
# ---------------------------------------------------------------------------


class ScanBody(BaseModel):
    brand: Optional[str] = None
    timeout: float = Field(8.0, ge=2.0, le=60.0)


class DeviceBody(BaseModel):
    brand: str
    mac: str
    model: str = ""
    name: str = ""
    notes: str = ""


class PairBody(BaseModel):
    brand: str
    mac: str
    model: str = ""
    name: str = ""  # exact advertised BLE name (Nipro hands-free)
    repair: bool = False


class SyncBody(BaseModel):
    brand: str
    mac: str
    model: str = ""
    listen_s: float = Field(60.0, ge=5.0, le=180.0)


class NiproRegisterBody(BaseModel):
    mac: str
    name: str
    brand: str = "nipro_nbp"
    model: str = ""
    serial: str = ""


class NiproHandsfreeBody(BaseModel):
    duration_s: float = Field(3600.0, ge=60.0, le=28800.0)
    receive_timeout: float = Field(60.0, ge=15.0, le=180.0)
    categories: str = "bp,ht,gl,spo2"


class LiveBody(BaseModel):
    brand: str
    mac: str
    model: str = ""
    duration_s: float = Field(3600.0, ge=30.0, le=86400.0)
    auto_reconnect: bool = True
    max_reconnects: int = Field(30, ge=1, le=100)


class CycleBody(BaseModel):
    """Auto-cycle roster: spend slot_s on each device, all cards always shown."""
    macs: Optional[List[str]] = None  # default = all saved clinical devices
    slot_s: float = Field(30.0, ge=10.0, le=120.0)
    rounds: int = Field(0, ge=0, le=100)  # 0 = infinite until stop


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------


@app.get("/")
async def index() -> FileResponse:
    index_path = STATIC / "index.html"
    if not index_path.is_file():
        raise HTTPException(404, "static/index.html missing")
    return FileResponse(index_path)


@app.get("/health")
async def health() -> Dict[str, Any]:
    try:
        from medical_ble_toolkit.hub.config import load_hub_config, default_config_path

        cfg = load_hub_config()
        hub_cfg = {
            "omron_poll_interval_s": cfg.omron_poll_interval_s,
            "path": str(default_config_path()),
            "mqtt_enabled": cfg.mqtt_enabled,
        }
    except Exception as exc:  # noqa: BLE001
        hub_cfg = {"error": str(exc)}
    mqtt_st: Dict[str, Any] = {}
    try:
        import mqtt_bridge

        mqtt_st = mqtt_bridge.status()
    except Exception as exc:  # noqa: BLE001
        mqtt_st = {"error": str(exc)}
    return {
        "ok": True,
        "service": "medical_ble_web",
        "mode": "tier1_hub",
        "db": str(db.DB_PATH),
        "daemon": daemon_status(),
        "live": live_status(),
        "hub": hub_cfg,
        "mqtt": mqtt_st,
    }


@app.post("/admin/reset")
async def admin_reset() -> Dict[str, Any]:
    """Wipe devices/readings/sessions (fresh hub pairing). Does not touch OS bonds."""
    try:
        if daemon_status().get("active"):
            await job_daemon_stop()
        if live_status().get("active"):
            await job_live_stop()
    except Exception as exc:  # noqa: BLE001
        log.warning("stop before reset: %s", exc)
    db.reset_db()
    # Clear fixed Nipro registry (medical_ble_web/data/… or $MEDICAL_NIPRO_REGISTRY)
    try:
        from medical_ble_toolkit.nipro.registry import registry_path, save_registry

        save_registry({"meters": []})
        reg_path = str(registry_path())
    except Exception as exc:  # noqa: BLE001
        log.warning("registry clear: %s", exc)
        # Legacy fallback
        reg = ROOT / "data" / "nipro_paired_devices.json"
        try:
            reg.parent.mkdir(parents=True, exist_ok=True)
            reg.write_text('{"meters": []}\n', encoding="utf-8")
            reg_path = str(reg)
        except OSError as exc2:
            log.warning("registry clear fallback: %s", exc2)
            reg_path = ""
    try:
        db.export_paired_devices()
    except OSError as exc:
        log.warning("paired export clear: %s", exc)
    return {
        "ok": True,
        "message": "DB + nipro registry cleared. Re-pair devices on this hub only.",
        "db": str(db.DB_PATH),
        "nipro_registry": reg_path,
        "paired_export": str(db.PAIRED_EXPORT_PATH),
    }


@app.get("/brands")
async def brands(
    all: bool = Query(False, description="Include advanced/non Tier-1 brands"),
) -> Dict[str, Any]:
    """Default: Tier-1 hub brands only (Omron, NBP, NT-100B, MightySat)."""
    return {
        "brands": list_brands(include_advanced=bool(all)),
        "tier1_only": not bool(all),
    }


@app.post("/scan")
async def scan(body: ScanBody) -> Dict[str, Any]:
    try:
        devices = await job_scan(brand_id=body.brand, timeout=body.timeout)
        return {
            "ok": True,
            "count": len(devices),
            "devices": devices,
            "message": (
                f"Found {len(devices)} device(s)"
                if devices
                else "No devices — wake meters / move closer / try again"
            ),
        }
    except BleJobError as exc:
        raise HTTPException(status_code=409, detail=exc.as_dict()) from exc
    except Exception as exc:  # noqa: BLE001
        log.exception("scan failed")
        raise HTTPException(500, str(exc)) from exc


@app.get("/scan/cache")
async def scan_cache() -> Dict[str, Any]:
    return {"devices": db.list_scan_cache()}


@app.get("/devices")
async def devices() -> Dict[str, Any]:
    return {"devices": db.list_devices()}


@app.post("/devices")
async def save_device(body: DeviceBody) -> Dict[str, Any]:
    brand = get_brand(body.brand)
    if not brand:
        raise HTTPException(400, f"Unknown brand: {body.brand}")
    row = db.upsert_device(
        brand=body.brand,
        mac=body.mac,
        model=body.model or brand.get("default_model", ""),
        name=body.name or body.model or brand.get("default_model", ""),
        company=brand.get("company", ""),
        notes=body.notes,
    )
    return {"ok": True, "device": row}


@app.post("/pair")
async def pair(body: PairBody) -> Dict[str, Any]:
    if not get_brand(body.brand):
        raise HTTPException(400, f"Unknown brand: {body.brand}")
    try:
        result = await job_pair(
            brand_id=body.brand,
            mac=body.mac,
            model=body.model,
            name=body.name,
            repair=body.repair,
        )
        return result
    except BleJobError as exc:
        raise HTTPException(status_code=409, detail=exc.as_dict()) from exc
    except Exception as exc:  # noqa: BLE001
        log.exception("pair failed")
        raise HTTPException(500, str(exc)) from exc


@app.get("/nipro/meters")
async def nipro_meters() -> Dict[str, Any]:
    """Local Nipro companion registry (exact names for hands-free)."""
    try:
        return await job_nipro_list()
    except Exception as exc:  # noqa: BLE001
        raise HTTPException(500, str(exc)) from exc


@app.post("/nipro/register")
async def nipro_register(body: NiproRegisterBody) -> Dict[str, Any]:
    try:
        return await job_nipro_register(
            mac=body.mac,
            name=body.name,
            brand_id=body.brand,
            model=body.model,
            serial=body.serial,
        )
    except ValueError as exc:
        raise HTTPException(400, str(exc)) from exc
    except Exception as exc:  # noqa: BLE001
        raise HTTPException(500, str(exc)) from exc


@app.post("/nipro/handsfree/start")
async def nipro_handsfree_start(body: NiproHandsfreeBody) -> Dict[str, Any]:
    cats = [c.strip() for c in (body.categories or "").split(",") if c.strip()]
    try:
        return await job_nipro_handsfree_start(
            duration_s=body.duration_s,
            receive_timeout=body.receive_timeout,
            categories=cats or None,
        )
    except ValueError as exc:
        raise HTTPException(400, str(exc)) from exc
    except RuntimeError as exc:
        raise HTTPException(409, str(exc)) from exc
    except Exception as exc:  # noqa: BLE001
        log.exception("handsfree start failed")
        raise HTTPException(500, str(exc)) from exc


@app.post("/nipro/handsfree/stop")
async def nipro_handsfree_stop() -> Dict[str, Any]:
    return await job_nipro_handsfree_stop()


@app.get("/nipro/handsfree/status")
async def nipro_handsfree_status() -> Dict[str, Any]:
    return {"ok": True, "handsfree": handsfree_status()}


@app.post("/sync")
async def sync(body: SyncBody) -> Dict[str, Any]:
    if not get_brand(body.brand):
        raise HTTPException(400, f"Unknown brand: {body.brand}")
    try:
        result = await job_sync(
            brand_id=body.brand,
            mac=body.mac,
            model=body.model,
            listen_s=body.listen_s,
        )
        return result
    except BleJobError as exc:
        # FE4A / bond issues — structured tips for the UI (not a parse bug)
        raise HTTPException(status_code=409, detail=exc.as_dict()) from exc
    except Exception as exc:  # noqa: BLE001
        log.exception("sync failed")
        raise HTTPException(500, str(exc)) from exc


@app.post("/live/start")
async def live_start(body: LiveBody) -> Dict[str, Any]:
    if not get_brand(body.brand):
        raise HTTPException(400, f"Unknown brand: {body.brand}")
    try:
        return await job_live_start(
            brand_id=body.brand,
            mac=body.mac,
            model=body.model,
            duration_s=body.duration_s,
            auto_reconnect=body.auto_reconnect,
            max_reconnects=body.max_reconnects,
        )
    except Exception as exc:  # noqa: BLE001
        log.exception("live start failed")
        raise HTTPException(500, str(exc)) from exc


@app.post("/live/stop")
async def live_stop() -> Dict[str, Any]:
    return await job_live_stop()


@app.get("/live/latest")
async def live_latest() -> Dict[str, Any]:
    return {"ok": True, "live": live_status()}


@app.get("/dashboard")
async def dashboard() -> Dict[str, Any]:
    """All saved devices + latest vitals side-by-side (multi-device board)."""
    return build_dashboard()


@app.get("/cycle/status")
async def get_cycle_status() -> Dict[str, Any]:
    return {"ok": True, "cycle": cycle_status(), "dashboard": build_dashboard()}


@app.post("/cycle/start")
async def cycle_start(body: CycleBody) -> Dict[str, Any]:
    try:
        return await job_cycle_start(
            macs=body.macs,
            slot_s=body.slot_s,
            rounds=body.rounds,
        )
    except ValueError as exc:
        raise HTTPException(400, str(exc)) from exc
    except Exception as exc:  # noqa: BLE001
        log.exception("cycle start failed")
        raise HTTPException(500, str(exc)) from exc


@app.post("/cycle/stop")
async def cycle_stop() -> Dict[str, Any]:
    return await job_cycle_stop()


@app.get("/daemon/status")
async def daemon_status_route() -> Dict[str, Any]:
    """Background auto-sync daemon status (started after Pair if enabled)."""
    return {"ok": True, "daemon": daemon_status()}


@app.post("/daemon/start")
async def daemon_start() -> Dict[str, Any]:
    try:
        return await job_daemon_start()
    except Exception as exc:  # noqa: BLE001
        log.exception("daemon start failed")
        raise HTTPException(500, str(exc)) from exc


@app.post("/daemon/stop")
async def daemon_stop() -> Dict[str, Any]:
    return await job_daemon_stop()


@app.websocket("/ws/live")
async def ws_live(websocket: WebSocket) -> None:
    """
    Real-time push: live SpO2 vitals + multi-device dashboard/cycle updates.

    Queue items are either a live status dict, or
    ``{"type": "dashboard", "dashboard": {...}}``.
    """
    await websocket.accept()
    q = subscribe_live()
    log.info("WebSocket /ws/live client connected")
    try:
        await websocket.send_json(
            {
                "ok": True,
                "event": "hello",
                "live": live_status(),
                "dashboard": build_dashboard(),
            }
        )
        while True:
            try:
                item = await asyncio.wait_for(q.get(), timeout=0.5)
                if isinstance(item, dict) and item.get("type") == "dashboard":
                    await websocket.send_json(
                        {
                            "ok": True,
                            "event": "dashboard",
                            "dashboard": item.get("dashboard"),
                            "live": live_status(),
                        }
                    )
                else:
                    await websocket.send_json(
                        {
                            "ok": True,
                            "event": "vital",
                            "live": item,
                            "dashboard": build_dashboard(
                                highlight_mac=(item or {}).get("mac") or ""
                            )
                            if isinstance(item, dict) and item.get("active")
                            else None,
                        }
                    )
            except asyncio.TimeoutError:
                await websocket.send_json(
                    {
                        "ok": True,
                        "event": "tick",
                        "live": live_status(),
                        "dashboard": build_dashboard()
                        if cycle_status().get("active")
                        else None,
                    }
                )
    except WebSocketDisconnect:
        log.info("WebSocket /ws/live client disconnected")
    except Exception as exc:  # noqa: BLE001
        log.warning("WebSocket /ws/live error: %s", exc)
    finally:
        unsubscribe_live(q)


@app.get("/readings")
async def readings(
    mac: Optional[str] = None,
    brand: Optional[str] = None,
    limit: int = Query(50, ge=1, le=500),
) -> Dict[str, Any]:
    rows = db.list_readings(mac=mac, brand=brand, limit=limit)
    return {"count": len(rows), "readings": rows}


def main() -> None:
    import uvicorn

    uvicorn.run(
        "app:app",
        host="127.0.0.1",
        port=8741,
        reload=False,
        log_level="info",
    )


if __name__ == "__main__":
    main()

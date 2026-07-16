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
    job_live_start,
    job_live_stop,
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
def _startup() -> None:
    db.init_db()
    log.info("SQLite ready: %s", db.DB_PATH)
    log.info("Open http://127.0.0.1:8741")


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
    repair: bool = False


class SyncBody(BaseModel):
    brand: str
    mac: str
    model: str = ""
    listen_s: float = Field(30.0, ge=5.0, le=180.0)


class LiveBody(BaseModel):
    brand: str
    mac: str
    model: str = ""
    duration_s: float = Field(3600.0, ge=30.0, le=86400.0)
    auto_reconnect: bool = True
    max_reconnects: int = Field(30, ge=1, le=100)


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
    return {
        "ok": True,
        "service": "medical_ble_web",
        "db": str(db.DB_PATH),
        "live": live_status(),
    }


@app.get("/brands")
async def brands() -> Dict[str, Any]:
    return {"brands": list_brands()}


@app.post("/scan")
async def scan(body: ScanBody) -> Dict[str, Any]:
    try:
        devices = await job_scan(brand_id=body.brand, timeout=body.timeout)
        return {"ok": True, "count": len(devices), "devices": devices}
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
            repair=body.repair,
        )
        return result
    except BleJobError as exc:
        raise HTTPException(status_code=409, detail=exc.as_dict()) from exc
    except Exception as exc:  # noqa: BLE001
        log.exception("pair failed")
        raise HTTPException(500, str(exc)) from exc


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


@app.websocket("/ws/live")
async def ws_live(websocket: WebSocket) -> None:
    """
    Real-time push of live vitals to the browser.

    Server pushes a snapshot on every SpO2/PR packet, plus ~2 Hz keepalive ticks.
    This is the primary live UI path (not polling).
    """
    await websocket.accept()
    q = subscribe_live()
    log.info("WebSocket /ws/live client connected")
    try:
        await websocket.send_json(
            {"ok": True, "live": live_status(), "event": "hello"}
        )
        while True:
            try:
                snap = await asyncio.wait_for(q.get(), timeout=0.45)
                await websocket.send_json(
                    {"ok": True, "live": snap, "event": "vital"}
                )
            except asyncio.TimeoutError:
                await websocket.send_json(
                    {"ok": True, "live": live_status(), "event": "tick"}
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

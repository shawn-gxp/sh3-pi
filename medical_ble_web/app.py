"""
Medical BLE Hub — Daemon-driven FastAPI backend.

Boot → Daemon auto-starts → scans every 6s for paired MACs
     → device powers on → connects by MAC → reads data → SQLite + MQTT

First-time setup: POST /scan → POST /pair → daemon picks it up automatically.
"""

from __future__ import annotations

import asyncio
import logging
import os
import sys
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Any, Dict, List, Optional

from fastapi import FastAPI, HTTPException, Query, WebSocket, WebSocketDisconnect
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

ROOT = Path(__file__).resolve().parent
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))
_EXP = ROOT.parent  # sh3-pi package root
if str(_EXP) not in sys.path:
    sys.path.insert(0, str(_EXP))

import db  # noqa: E402
from brands import get_brand, list_brands  # noqa: E402
from ble_jobs import (  # noqa: E402
    BleJobError,
    build_dashboard,
    daemon_status,
    job_daemon_start,
    job_daemon_stop,
    job_pair,
    job_scan,
    pair_passkey_status,
    provide_pair_passkey,
    subscribe_live,
    unsubscribe_live,
)
from fall_import import (  # noqa: E402
    ensure_fall_detection,
    fall_detection_location,
    import_camera_loop,
    import_fall_config,
)

logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] %(levelname)-7s %(name)s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger("medical_ble_web")

@asynccontextmanager
async def lifespan(app: FastAPI):
    db.init_db()
    log.info("SQLite ready: %s", db.DB_PATH)
    log.info("Open http://0.0.0.0:8741")
    try:
        import mqtt_bridge

        if mqtt_bridge.start():
            log.info("MQTT bridge started: %s", mqtt_bridge.status())
        else:
            log.info("MQTT bridge off or broker unreachable: %s", mqtt_bridge.status())
    except Exception as exc:  # noqa: BLE001
        log.warning("MQTT bridge start failed: %s", exc)
    try:
        devices = db.list_devices()
        paired = [d for d in devices if d.get("paired")]
        if paired:
            await job_daemon_start()
            log.info("Auto-sync started for %d paired device(s)", len(paired))
        else:
            log.info("No paired devices yet — pair a device to begin.")
    except Exception as exc:  # noqa: BLE001
        log.warning("Could not auto-start daemon: %s", exc)
    # Fall detection is a *sibling* package fall_detection_pi (not inside sh3-pi)
    if ensure_fall_detection():
        try:
            import threading

            camera_loop = import_camera_loop()
            thread = threading.Thread(target=camera_loop.run, daemon=True)
            thread.start()
            log.info("Fall detection (fall_detection_pi) camera loop started")
        except Exception as exc:
            log.warning("Could not start fall detection: %s", exc)
    else:
        log.warning(
            "fall_detection_pi package not found — install sibling package "
            "(pip install -e ../fall_detection_pi) or set FALL_DETECTION_HOME. "
            "Hub BLE features still work; /api/fall/* will error until installed."
        )

    yield

    try:
        if ensure_fall_detection():
            import_camera_loop().stop()
    except Exception:
        pass

    try:
        import mqtt_bridge

        mqtt_bridge.stop()
    except Exception:
        pass


app = FastAPI(
    title="Medical BLE Hub",
    description="Daemon-driven hub: auto-collects from all paired BLE medical devices.",
    version="1.0.0",
    lifespan=lifespan,
)

STATIC = ROOT / "static"
if STATIC.is_dir():
    app.mount("/static", StaticFiles(directory=str(STATIC)), name="static")


# ---------------------------------------------------------------------------
# Request models
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
    name: str = ""
    repair: bool = False
    # Beurer BM54 etc. — 6-digit code from cuff LCD
    passkey: str = ""


class PasskeyBody(BaseModel):
    passkey: str


class PatientSetting(BaseModel):
    patient_id: str


# ---------------------------------------------------------------------------
# Fall Detection API Routes (optional sibling package)
# ---------------------------------------------------------------------------

from fastapi.responses import StreamingResponse
from fastapi import Request


def _require_fall():
    if not ensure_fall_detection():
        raise HTTPException(
            503,
            "fall_detection_pi package not installed. "
            "pip install -e ../fall_detection_pi or set FALL_DETECTION_HOME",
        )


@app.get("/api/fall/stream")
async def fall_stream():
    """Live MJPEG stream of the camera feed with pose overlay."""
    _require_fall()
    camera_loop = import_camera_loop()

    def generate():
        import time

        while True:
            jpeg = camera_loop.get_latest_jpeg()
            if jpeg:
                yield (
                    b"--frame\r\n"
                    b"Content-Type: image/jpeg\r\n\r\n" + jpeg + b"\r\n"
                )
            time.sleep(0.04)  # ~25 FPS

    return StreamingResponse(
        generate(), media_type="multipart/x-mixed-replace; boundary=frame"
    )


@app.get("/api/fall/roi")
async def fall_roi_get() -> Dict[str, Any]:
    """Get the current bed boundary polygon."""
    _require_fall()
    return {"ok": True, "polygon": import_camera_loop().get_active_polygon()}


class PolygonBody(BaseModel):
    polygon: List[Dict[str, float]]


@app.post("/api/fall/roi")
async def fall_roi_post(body: PolygonBody) -> Dict[str, Any]:
    """Set the bed boundary polygon (expects ≥3 points, normalized 0.0-1.0)."""
    _require_fall()
    if len(body.polygon) < 3:
        raise HTTPException(400, "Polygon must have at least 3 points")

    new_polygon = [(pt["x"], pt["y"]) for pt in body.polygon]
    import_camera_loop().update_polygon(new_polygon)

    try:
        import json

        cfg_path = import_fall_config().hub_config_path()
        cfg = json.loads(cfg_path.read_text(encoding="utf-8")) if cfg_path.is_file() else {}
        # hub_config key stays fall_detection for existing configs
        if "fall_detection" not in cfg:
            cfg["fall_detection"] = {}
        cfg["fall_detection"]["polygon"] = new_polygon
        cfg_path.parent.mkdir(parents=True, exist_ok=True)
        cfg_path.write_text(json.dumps(cfg, indent=2), encoding="utf-8")
    except Exception as exc:
        log.warning("Could not persist ROI to hub_config.json: %s", exc)

    return {"ok": True, "polygon": new_polygon}


@app.post("/api/fall/frame")
async def fall_frame_post(request: Request) -> Dict[str, Any]:
    """Receives a JPEG frame uploaded from browser/phone camera and runs fall detection."""
    _require_fall()
    data = await request.body()
    if not data:
        raise HTTPException(400, "Empty image body")
    return import_camera_loop().process_client_frame(data)


class LandmarksBody(BaseModel):
    landmarks: Any


@app.post("/api/fall/landmarks")
async def fall_landmarks_post(body: LandmarksBody) -> Dict[str, Any]:
    """Receives normalized skeletal landmark points and evaluates fall detection."""
    _require_fall()
    return import_camera_loop().process_normalized_landmarks(body.landmarks)


@app.get("/api/fall/status")
async def fall_status() -> Dict[str, Any]:
    """Whether the sibling fall_detection_pi package is available."""
    ok = ensure_fall_detection()
    p = fall_detection_location() if ok else None
    return {
        "ok": ok,
        "package": "fall_detection_pi",
        "package_path": str(p) if p else None,
        "separate_package": True,
    }

# ---------------------------------------------------------------------------
# Standard Web UI Routes
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
    from medical_ble_toolkit.common.winrt_errors import is_windows, is_linux
    return {
        "ok": True,
        "service": "medical_ble_hub",
        "mode": "daemon",
        "db": str(db.DB_PATH),
        "daemon": daemon_status(),
        "hub": hub_cfg,
        "mqtt": mqtt_st,
        "platform": {
            "os": "windows" if is_windows() else "linux" if is_linux() else "macos",
            "passkey_via_ui": is_linux(),
        },
    }


@app.post("/admin/reset")
async def admin_reset() -> Dict[str, Any]:
    """Wipe devices/readings/sessions for a fresh hub setup. Does not touch OS bonds."""
    try:
        if daemon_status().get("active"):
            await job_daemon_stop()
    except Exception as exc:  # noqa: BLE001
        log.warning("stop before reset: %s", exc)
    db.reset_db()
    try:
        from medical_ble_toolkit.brands.nipro.registry import registry_path, save_registry

        save_registry({"meters": []})
        reg_path = str(registry_path())
    except Exception as exc:  # noqa: BLE001
        log.warning("registry clear: %s", exc)
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
        "message": "DB + Nipro registry cleared. Re-pair all devices.",
        "db": str(db.DB_PATH),
        "nipro_registry": reg_path,
        "paired_export": str(db.PAIRED_EXPORT_PATH),
    }


@app.get("/brands")
async def brands(
    all: bool = Query(False, description="Include advanced/non Tier-1 brands"),
) -> Dict[str, Any]:
    """Tier-1 hub brands by default (Omron, Nipro, Masimo, Beurer). ?all=true for lab brands."""
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
                else "No devices found — wake the meter and move it closer, then try again"
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
        profile_id=brand.get("id") or body.brand,
        brand=brand.get("company") or body.brand,
        mac=body.mac,
        model=body.model or brand.get("default_model", ""),
        name=body.name or body.model or brand.get("default_model", ""),
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
            passkey=body.passkey,
        )
        return result
    except BleJobError as exc:
        raise HTTPException(status_code=409, detail=exc.as_dict()) from exc
    except Exception as exc:  # noqa: BLE001
        log.exception("pair failed")
        raise HTTPException(500, str(exc)) from exc


@app.get("/pair/passkey")
async def pair_passkey_get() -> Dict[str, Any]:
    """Poll while Pair is in flight — need_passkey=true when cuff shows a code."""
    return pair_passkey_status()


@app.post("/pair/passkey")
async def pair_passkey_post(body: PasskeyBody) -> Dict[str, Any]:
    """Submit 6-digit code from cuff LCD to the BlueZ agent (mid-pair)."""
    try:
        return provide_pair_passkey(body.passkey)
    except ValueError as exc:
        raise HTTPException(400, str(exc)) from exc


@app.get("/daemon/status")
async def daemon_status_route() -> Dict[str, Any]:
    """Hub daemon status — active, scan round, last device seen."""
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


@app.get("/dashboard")
async def dashboard() -> Dict[str, Any]:
    """All paired devices with their latest vitals."""
    return build_dashboard()


@app.websocket("/ws/live")
async def ws_live(websocket: WebSocket) -> None:
    """
    Real-time push: dashboard updates as daemon session results arrive.
    Sends a heartbeat tick every 5 seconds so the UI always has fresh data.
    """
    await websocket.accept()
    q = subscribe_live()
    log.info("WebSocket /ws/live client connected")
    try:
        await websocket.send_json(
            {
                "ok": True,
                "event": "hello",
                "daemon": daemon_status(),
                "dashboard": build_dashboard(),
            }
        )
        while True:
            try:
                item = await asyncio.wait_for(q.get(), timeout=5.0)
                # All events push a fresh dashboard + daemon status
                await websocket.send_json(
                    {
                        "ok": True,
                        "event": "dashboard",
                        "daemon": daemon_status(),
                        "dashboard": build_dashboard(
                            highlight_mac=(item or {}).get("mac", "")
                            if isinstance(item, dict)
                            else ""
                        ),
                    }
                )
            except asyncio.TimeoutError:
                # Regular heartbeat — always send current state
                await websocket.send_json(
                    {
                        "ok": True,
                        "event": "tick",
                        "daemon": daemon_status(),
                        "dashboard": build_dashboard(),
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


@app.get("/settings/patient")
async def get_patient_setting() -> Dict[str, Any]:
    from db import get_setting

    pid = get_setting("patient_id")
    source = "db"
    if not pid:
        from mqtt_bridge import _load_cfg

        pid = str(_load_cfg().get("patient_id") or "")
        source = "config"
    return {"patient_id": pid, "source": source}


@app.post("/settings/patient")
async def set_patient_setting(body: PatientSetting) -> Dict[str, Any]:
    from db import set_setting

    set_setting("patient_id", body.patient_id.strip())
    return {"ok": True, "patient_id": body.patient_id.strip()}


def generate_self_signed_cert(cert_path: str, key_path: str):
    import datetime
    from cryptography import x509
    from cryptography.x509.oid import NameOID
    from cryptography.hazmat.primitives import hashes, serialization
    from cryptography.hazmat.primitives.asymmetric import rsa

    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    subject = issuer = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "Medical BLE Hub")])
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.datetime.now(datetime.timezone.utc))
        .not_valid_after(datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=365))
        .sign(key, hashes.SHA256())
    )
    with open(key_path, "wb") as f:
        f.write(key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.TraditionalOpenSSL, serialization.NoEncryption()))
    with open(cert_path, "wb") as f:
        f.write(cert.public_bytes(serialization.Encoding.PEM))


def main() -> None:
    import uvicorn

    use_ssl = os.getenv("USE_SSL", "0").lower() in ("1", "true", "yes") or os.getenv("SSL", "0").lower() in ("1", "true", "yes")
    cert_file = ROOT / "cert.pem"
    key_file = ROOT / "key.pem"

    if use_ssl or "--ssl" in sys.argv:
        if not (cert_file.exists() and key_file.exists()):
            try:
                generate_self_signed_cert(str(cert_file), str(key_file))
                log.info("Auto-generated SSL certificate: %s", cert_file)
            except Exception as exc:
                log.warning("Could not auto-generate SSL cert: %s", exc)

        if cert_file.exists() and key_file.exists():
            log.info("HTTPS Enabled! Open https://0.0.0.0:8741")
            uvicorn.run(
                "app:app",
                host=os.getenv("HOST", "0.0.0.0"),
                port=int(os.getenv("PORT", "8741")),
                reload=False,
                log_level="info",
                ssl_certfile=str(cert_file),
                ssl_keyfile=str(key_file),
            )
            return

    uvicorn.run(
        "app:app",
        host=os.getenv("HOST", "0.0.0.0"),
        port=int(os.getenv("PORT", "8741")),
        reload=False,
        log_level="info",
    )


if __name__ == "__main__":
    main()

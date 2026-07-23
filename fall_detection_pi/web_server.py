"""
Independent Fall Detection HTTP server.

Runs separately from the BLE hub (medical_ble_web on :8741 by default).

From workspace root (folder that contains fall_detection_pi/):

  set PYTHONPATH=.
  python -m fall_detection_pi.web_server --ssl

Default port: 8742 (override with FALL_PORT).

Open: https://127.0.0.1:8742/  or  /static/fall.html
"""
from __future__ import annotations

import logging
import os
import sys
import threading
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Any, Dict, List

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import FileResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

_PKG = Path(__file__).resolve().parent
_PARENT = _PKG.parent
if str(_PARENT) not in sys.path:
    sys.path.insert(0, str(_PARENT))

from fall_detection_pi import camera_loop, config  # noqa: E402

logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] %(levelname)-7s %(name)s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger("fall_detection_pi.web")

STATIC = _PKG / "static"
DEFAULT_PORT = int(os.environ.get("FALL_PORT", os.environ.get("PORT", "8742")))
DEFAULT_HOST = os.environ.get("FALL_HOST", os.environ.get("HOST", "0.0.0.0"))


@asynccontextmanager
async def lifespan(_app: FastAPI):
    log.info("Fall detection service starting (independent of BLE hub)")
    log.info("Package: %s", _PKG)
    thread = threading.Thread(target=camera_loop.run, daemon=True, name="fall-camera")
    thread.start()
    log.info("Camera loop thread started (stays idle if no camera device)")
    yield
    try:
        camera_loop.stop()
    except Exception:
        pass
    log.info("Fall detection service stopped")


app = FastAPI(
    title="Fall Detection Pi",
    description="Standalone fall / bed-exit service (separate process from BLE hub).",
    version="0.2.0",
    lifespan=lifespan,
)

if STATIC.is_dir():
    app.mount("/static", StaticFiles(directory=str(STATIC)), name="static")


@app.get("/health")
async def health() -> Dict[str, Any]:
    return {
        "ok": True,
        "service": "fall_detection_pi",
        "package_path": str(_PKG),
        "have_cv2": camera_loop.HAVE_CV2,
        "have_pose": camera_loop.HAVE_POSE,
        "camera_source": config.CAMERA_SOURCE,
        "port": DEFAULT_PORT,
    }


@app.get("/")
async def index() -> FileResponse:
    index_path = STATIC / "fall.html"
    if not index_path.is_file():
        raise HTTPException(404, "static/fall.html missing")
    return FileResponse(index_path)


@app.get("/api/fall/stream")
async def fall_stream():
    def generate():
        import time

        while True:
            jpeg = camera_loop.get_latest_jpeg()
            if jpeg:
                yield (
                    b"--frame\r\n"
                    b"Content-Type: image/jpeg\r\n\r\n" + jpeg + b"\r\n"
                )
            time.sleep(0.04)

    return StreamingResponse(
        generate(), media_type="multipart/x-mixed-replace; boundary=frame"
    )


@app.get("/api/fall/roi")
async def fall_roi_get() -> Dict[str, Any]:
    return {"ok": True, "polygon": camera_loop.get_active_polygon()}


class PolygonBody(BaseModel):
    polygon: List[Dict[str, float]]


@app.post("/api/fall/roi")
async def fall_roi_post(body: PolygonBody) -> Dict[str, Any]:
    if len(body.polygon) < 3:
        raise HTTPException(400, "Polygon must have at least 3 points")
    new_polygon = [(pt["x"], pt["y"]) for pt in body.polygon]
    camera_loop.update_polygon(new_polygon)
    try:
        import json

        cfg_path = config.hub_config_path()
        cfg = json.loads(cfg_path.read_text(encoding="utf-8")) if cfg_path.is_file() else {}
        if "fall_detection" not in cfg:
            cfg["fall_detection"] = {}
        cfg["fall_detection"]["polygon"] = new_polygon
        cfg_path.parent.mkdir(parents=True, exist_ok=True)
        cfg_path.write_text(json.dumps(cfg, indent=2), encoding="utf-8")
    except Exception as exc:
        log.warning("Could not persist ROI: %s", exc)
    return {"ok": True, "polygon": new_polygon}


@app.post("/api/fall/frame")
async def fall_frame_post(request: Request) -> Dict[str, Any]:
    data = await request.body()
    if not data:
        raise HTTPException(400, "Empty image body")
    return camera_loop.process_client_frame(data)


class LandmarksBody(BaseModel):
    landmarks: Any


@app.post("/api/fall/landmarks")
async def fall_landmarks_post(body: LandmarksBody) -> Dict[str, Any]:
    return camera_loop.process_normalized_landmarks(body.landmarks)


@app.get("/api/fall/status")
async def fall_status() -> Dict[str, Any]:
    return {
        "ok": True,
        "service": "fall_detection_pi",
        "package": "fall_detection_pi",
        "package_path": str(_PKG),
        "separate_process": True,
        "port": DEFAULT_PORT,
        "have_cv2": camera_loop.HAVE_CV2,
        "have_pose": camera_loop.HAVE_POSE,
    }


def _generate_self_signed_cert(cert_path: Path, key_path: Path) -> None:
    import datetime
    from cryptography import x509
    from cryptography.hazmat.primitives import hashes, serialization
    from cryptography.hazmat.primitives.asymmetric import rsa
    from cryptography.x509.oid import NameOID

    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    subject = issuer = x509.Name(
        [x509.NameAttribute(NameOID.COMMON_NAME, "Fall Detection Pi")]
    )
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.datetime.now(datetime.timezone.utc))
        .not_valid_after(
            datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=365)
        )
        .sign(key, hashes.SHA256())
    )
    key_path.write_bytes(
        key.private_bytes(
            serialization.Encoding.PEM,
            serialization.PrivateFormat.TraditionalOpenSSL,
            serialization.NoEncryption(),
        )
    )
    cert_path.write_bytes(cert.public_bytes(serialization.Encoding.PEM))


def main() -> None:
    import uvicorn

    use_ssl = (
        os.getenv("USE_SSL", "0").lower() in ("1", "true", "yes")
        or os.getenv("SSL", "0").lower() in ("1", "true", "yes")
        or "--ssl" in sys.argv
    )
    cert_file = _PKG / "cert.pem"
    key_file = _PKG / "key.pem"
    host = DEFAULT_HOST
    port = DEFAULT_PORT

    if use_ssl:
        if not (cert_file.is_file() and key_file.is_file()):
            try:
                _generate_self_signed_cert(cert_file, key_file)
                log.info("Auto-generated SSL certificate: %s", cert_file)
            except Exception as exc:
                log.warning("Could not auto-generate SSL cert: %s", exc)
        if cert_file.is_file() and key_file.is_file():
            log.info("HTTPS fall service → https://%s:%s", host, port)
            uvicorn.run(
                app,
                host=host,
                port=port,
                reload=False,
                log_level="info",
                ssl_certfile=str(cert_file),
                ssl_keyfile=str(key_file),
            )
            return
        log.warning("SSL requested but cert/key missing — falling back to HTTP")

    log.info("HTTP fall service → http://%s:%s", host, port)
    uvicorn.run(app, host=host, port=port, reload=False, log_level="info")


if __name__ == "__main__":
    main()

# Complete API & Function Documentation

This document provides complete, exhaustive API reference and function-level documentation for both applications in this workspace:

1. **Medical BLE Hub (`sh3-pi / medical_ble_web`)** — Port `8741`
2. **Fall Detection Pi (`fall_detection_pi`)** — Port `8742`

---

## 1. Medical BLE Hub (`sh3-pi / medical_ble_web`)

The Medical BLE Hub is a daemon-driven FastAPI web application that manages Bluetooth Low Energy (BLE) medical devices (cuffs, blood glucose meters, pulse oximeters, thermometers), automatically collects vitals, stores data in SQLite, and bridges readings to MQTT.

* **Base URL**: `http://127.0.0.1:8741` (or `https://127.0.0.1:8741` if SSL enabled)
* **Entry Point**: `python -m medical_ble_web.app` or `scripts/dev/run_web.ps1`
* **Static Web UI**: [http://127.0.0.1:8741/](http://127.0.0.1:8741/)

### 1.1 REST Endpoints

#### `GET /`
* **Handler Function**: `index()`
* **Description**: Serves the main single-page web UI dashboard ([static/index.html](file:///c:/Users/Shawn%20A/Desktop/pi%20python/sh3-pi/medical_ble_web/static/index.html)).
* **Response**: `200 OK` (FileResponse: HTML).

---

#### `GET /health`
* **Handler Function**: `health()`
* **Description**: Returns detailed health and runtime metadata for the hub service, including background daemon status, SQLite DB path, MQTT bridge status, hub configuration, and OS platform capabilities.
* **Query Parameters**: None
* **Response Payload**:
```json
{
  "ok": true,
  "service": "medical_ble_hub",
  "mode": "daemon",
  "db": "c:\\Users\\Shawn A\\Desktop\\pi python\\sh3-pi\\medical_ble_web\\data\\poc.db",
  "daemon": {
    "active": true,
    "status": "hunt",
    "phase": "hunt",
    "scan_round": 6,
    "paired_count": 3
  },
  "hub": {
    "omron_poll_interval_s": 300,
    "path": "c:\\Users\\Shawn A\\Desktop\\pi python\\sh3-pi\\medical_ble_toolkit\\hub_config.json",
    "mqtt_enabled": true
  },
  "mqtt": {
    "enabled": true,
    "connected": false,
    "broker": "tcp://172.16.2.156:1883",
    "topic": "health/readings"
  },
  "platform": {
    "os": "windows",
    "passkey_via_ui": false
  }
}
```

---

#### `POST /admin/reset`
* **Handler Function**: `admin_reset()`
* **Description**: Resets the SQLite database tables (readings, devices, sessions, scan cache), clears the Nipro device registry file, and resets paired device exports. Does not remove operating system Bluetooth bonds.
* **Request Body**: None
* **Response Payload**:
```json
{
  "ok": true,
  "message": "DB + Nipro registry cleared. Re-pair all devices.",
  "db": "...",
  "nipro_registry": "...",
  "paired_export": "..."
}
```

---

#### `GET /brands`
* **Handler Function**: `brands(all: bool = Query(False))`
* **Description**: Retrieves list of supported medical device brands. By default returns Tier-1 consumer brands (Omron, Nipro, Masimo, Beurer).
* **Query Parameters**:
  * `all` (boolean, optional): Set to `true` to include advanced or lab test brands.
* **Response Payload**:
```json
{
  "brands": [
    {"id": "omron_bp", "company": "Omron", "default_model": "HEM-7143T1", ...},
    {"id": "nipro_4430", "company": "Nipro", "default_model": "4430", ...}
  ],
  "tier1_only": true
}
```

---

#### `POST /scan`
* **Handler Function**: `scan(body: ScanBody)`
* **Description**: Triggers an active BLE scan for nearby medical devices matching specified brand or across all supported profiles.
* **Request Body (`ScanBody`)**:
  * `brand` (string, optional): Specific brand ID filter.
  * `timeout` (float, default `8.0`, min `2.0`, max `60.0`): Scan duration in seconds.
* **Response Payload**:
```json
{
  "ok": true,
  "count": 2,
  "devices": [
    {
      "mac": "E1:99:7D:27:1C:0A",
      "name": "BLESmart_00000481E1997D271C0A",
      "brand": "Omron",
      "rssi": -55
    }
  ],
  "message": "Found 2 device(s)"
}
```

---

#### `GET /scan/cache`
* **Handler Function**: `scan_cache()`
* **Description**: Retrieves cached BLE scan results stored in SQLite from recent scan operations.
* **Response Payload**: `{"devices": [...]}`

---

#### `GET /devices`
* **Handler Function**: `devices()`
* **Description**: Returns all saved and paired devices in the SQLite database.
* **Response Payload**: `{"devices": [...]}`

---

#### `POST /devices`
* **Handler Function**: `save_device(body: DeviceBody)`
* **Description**: Manually creates or updates a saved device entry in the database.
* **Request Body (`DeviceBody`)**:
  * `brand` (string, required): Brand ID (e.g. `omron_bp`).
  * `mac` (string, required): MAC address of device.
  * `model` (string, optional): Device model string.
  * `name` (string, optional): User-friendly nickname.
  * `notes` (string, optional): Notes.
* **Response Payload**: `{"ok": true, "device": {...}}`

---

#### `POST /pair`
* **Handler Function**: `pair(body: PairBody)`
* **Description**: Initiates interactive BLE pairing and bonding workflow for a specific medical device.
* **Request Body (`PairBody`)**:
  * `brand` (string, required): Brand ID.
  * `mac` (string, required): Target MAC address.
  * `model` (string, optional): Model string.
  * `name` (string, optional): Display name.
  * `repair` (boolean, default `false`): Force re-pairing.
  * `passkey` (string, optional): Pre-supplied 6-digit PIN code.
* **Response Payload**: `{"ok": true, "mac": "...", "brand": "...", "status": "paired"}`

---

#### `GET /pair/passkey`
* **Handler Function**: `pair_passkey_get()`
* **Description**: Polls passkey prompt status while a pairing job is in flight. Used by UI to check if user input is needed for display passkeys (e.g. Beurer blood pressure cuff LCD code).
* **Response Payload**:
```json
{
  "in_flight": true,
  "need_passkey": true,
  "prompt": "Enter 6-digit code shown on cuff LCD"
}
```

---

#### `POST /pair/passkey`
* **Handler Function**: `pair_passkey_post(body: PasskeyBody)`
* **Description**: Submits the 6-digit passkey entered by user into BlueZ pairing agent during pairing process.
* **Request Body (`PasskeyBody`)**:
  * `passkey` (string, required): 6-digit passkey code string.
* **Response Payload**: `{"ok": true, "message": "Passkey provided"}`

---

#### `GET /daemon/status`
* **Handler Function**: `daemon_status_route()`
* **Description**: Retrieves status of the hub background collector daemon (whether active, scan round, active sessions, last seen MAC).
* **Response Payload**: `{"ok": true, "daemon": {...}}`

---

#### `POST /daemon/start`
* **Handler Function**: `daemon_start()`
* **Description**: Starts the background BLE auto-collection daemon.
* **Response Payload**: `{"ok": true, "status": "started"}`

---

#### `POST /daemon/stop`
* **Handler Function**: `daemon_stop()`
* **Description**: Stops the background BLE collector daemon.
* **Response Payload**: `{"ok": true, "status": "stopped"}`

---

#### `GET /dashboard`
* **Handler Function**: `dashboard()`
* **Description**: Retrieves complete dashboard state containing all paired devices alongside their latest recorded vital sign measurements.
* **Response Payload**:
```json
{
  "devices": [
    {
      "mac": "E1:99:7D:27:1C:0A",
      "brand": "Omron",
      "model": "HEM-7143T1",
      "latest_reading": {
        "sys": 120,
        "dia": 80,
        "pulse": 72,
        "timestamp": "2026-07-24 10:25:00"
      }
    }
  ]
}
```

---

#### `GET /readings`
* **Handler Function**: `readings(mac: Optional[str], brand: Optional[str], limit: int = 50)`
* **Description**: Retrieves historical vital sign readings with optional filtering by MAC address or brand ID.
* **Query Parameters**:
  * `mac` (string, optional): Filter by device MAC address.
  * `brand` (string, optional): Filter by brand ID.
  * `limit` (int, default `50`, range `1-500`): Maximum rows to return.
* **Response Payload**: `{"count": 10, "readings": [...]}`

---

#### `GET /settings/patient`
* **Handler Function**: `get_patient_setting()`
* **Description**: Fetches current `patient_id` setting from SQLite DB or fallback config file.
* **Response Payload**: `{"patient_id": "04d3030f-af86-44dd-9f7f-86f51cf08391", "source": "db"}`

---

#### `POST /settings/patient`
* **Handler Function**: `set_patient_setting(body: PatientSetting)`
* **Description**: Updates and persists target `patient_id` in SQLite DB settings table.
* **Request Body (`PatientSetting`)**:
  * `patient_id` (string, required): Target UUID or string ID for patient.
* **Response Payload**: `{"ok": true, "patient_id": "..."}`

---

### 1.2 WebSocket Endpoint

#### `WS /ws/live`
* **Handler Function**: `ws_live(websocket: WebSocket)`
* **Description**: Establishes real-time push socket connection for frontend UI clients.
* **Protocol Flow**:
  1. On connection: sends `event: "hello"` containing initial `daemon` status and `dashboard` snapshot.
  2. On new reading received: pushes `event: "dashboard"` with updated vitals and highlighted device MAC.
  3. Every 5 seconds (heartbeat tick): pushes `event: "tick"` with fresh daemon state and dashboard snapshot.

---

### 1.3 Internal Functions & Module Architecture

#### Module: `app.py`
* `lifespan(app: FastAPI)`: Async context manager managing database initialization, MQTT bridge startup, daemon auto-start, and graceful shutdown.
* `generate_self_signed_cert(cert_path: str, key_path: str)`: Auto-generates an RSA 2048-bit self-signed SSL certificate using Python `cryptography` library.
* `main()`: Script entry point executing `uvicorn.run("app:app", host=..., port=...)`.

#### Module: `ble_jobs.py`
* `job_scan(brand_id: Optional[str], timeout: float)`: Executes async BLE scan via `medical_ble_toolkit`.
* `job_pair(brand_id: str, mac: str, model: str, name: str, repair: bool, passkey: str)`: Executes device pairing job.
* `job_daemon_start()` / `job_daemon_stop()`: Starts/stops the background `medical_ble_toolkit.hub.daemon` instance.
* `daemon_status()`: Returns dictionary of current daemon state metrics.
* `build_dashboard(highlight_mac: str)`: Queries DB to format paired devices and latest vitals.
* `subscribe_live()` / `unsubscribe_live(q)`: Adds/removes WebSocket event queues for live broadcasts.
* `pair_passkey_status()` / `provide_pair_passkey(passkey)`: Gets/sets mid-pair passkey buffer.

#### Module: `db.py`
* `init_db()`: Initializes SQLite tables (`devices`, `readings`, `scan_cache`, `sessions`, `settings`).
* `reset_db()`: Clears all tables in SQLite database.
* `upsert_device(profile_id, brand, mac, model, name)`: Inserts/updates device metadata.
* `list_devices()` / `list_readings(mac, brand, limit)` / `list_scan_cache()`: Queries stored records.
* `insert_reading(mac, brand, model, reading_type, vitals, payload, raw_hex)`: Stores vital measurement record.
* `get_setting(key)` / `set_setting(key, value)`: Gets/sets key-value pair in `settings` table.
* `export_paired_devices()`: Exports active paired devices to JSON file for toolkit access.

#### Module: `mqtt_bridge.py`
* `start()` / `stop()` / `status()`: Initializes, terminates, and queries status of async MQTT bridge.
* `notify_reading_inserted(reading_id, ...)`: Outbox trigger that publishes new vital readings to MQTT broker topic (`health/readings`).

---

## 2. Fall Detection Pi (`fall_detection_pi`)

Fall Detection Pi is an independent computer-vision package and HTTP service running MediaPipe Tasks `PoseLandmarker` for real-time posture analysis, bed Region-Of-Interest (ROI) exit monitoring, and fall detection.

* **Base URL**: `http://127.0.0.1:8742` (or `https://127.0.0.1:8742` if SSL enabled)
* **Entry Point**: `python -m fall_detection_pi.web_server`
* **Static Web UI**: [http://127.0.0.1:8742/](http://127.0.0.1:8742/) or [static/fall.html](file:///c:/Users/Shawn%20A/Desktop/pi%20python/fall_detection_pi/static/fall.html)

---

### 2.1 REST Endpoints

#### `GET /`
* **Handler Function**: `index()`
* **Description**: Serves the standalone Fall Detection web application ([static/fall.html](file:///c:/Users/Shawn%20A/Desktop/pi%20python/fall_detection_pi/static/fall.html)) with live MJPEG stream viewer and interactive ROI polygon canvas.
* **Response**: `200 OK` (FileResponse: HTML).

---

#### `GET /health`
* **Handler Function**: `health()`
* **Description**: Returns quick health indicator showing status of OpenCV (`have_cv2`), MediaPipe Tasks (`have_pose`), camera source index/RTSP URL, and port.
* **Response Payload**:
```json
{
  "ok": true,
  "service": "fall_detection_pi",
  "package_path": "c:\\Users\\Shawn A\\Desktop\\pi python\\fall_detection_pi",
  "have_cv2": true,
  "have_pose": true,
  "camera_source": 0,
  "port": 8742
}
```

---

#### `GET /api/fall/stream`
* **Handler Function**: `fall_stream()`
* **Description**: Serves real-time video stream as HTTP `multipart/x-mixed-replace; boundary=frame` MJPEG stream with overlaid MediaPipe pose skeletons and bed ROI boundaries.
* **Response**: `200 OK` (StreamingResponse: MJPEG byte frames).

---

#### `GET /api/fall/roi`
* **Handler Function**: `fall_roi_get()`
* **Description**: Retrieves current active bed ROI polygon coordinates normalized between `0.0` and `1.0`.
* **Response Payload**:
```json
{
  "ok": true,
  "polygon": [
    {"x": 0.1, "y": 0.2},
    {"x": 0.9, "y": 0.2},
    {"x": 0.9, "y": 0.8},
    {"x": 0.1, "y": 0.8}
  ]
}
```

---

#### `POST /api/fall/roi`
* **Handler Function**: `fall_roi_post(body: PolygonBody)`
* **Description**: Updates active bed ROI polygon coordinates (requires at least 3 vertices) and persists the ROI to `hub_config.json`. Automatically resets temporal fall detection history to avoid false positives.
* **Request Body (`PolygonBody`)**:
  * `polygon` (list of `{"x": float, "y": float}` points, min length 3).
* **Response Payload**: `{"ok": true, "polygon": [...]}`

---

#### `POST /api/fall/frame`
* **Handler Function**: `fall_frame_post(request: Request)`
* **Description**: Accepts raw binary image (JPEG/PNG) in HTTP request body, executes pose detection and fall analysis on frame, and returns detection result. Useful for remote camera feeds or client-side frames.
* **Request Body**: Raw image binary data (`bytes`).
* **Response Payload**:
```json
{
  "event_type": "NORMAL",
  "in_bed": true,
  "near_edge": false,
  "landmarks_count": 33,
  "timestamp_ms": 1721814470000
}
```

---

#### `POST /api/fall/landmarks`
* **Handler Function**: `fall_landmarks_post(body: LandmarksBody)`
* **Description**: Accepts normalized 2D/3D pose landmark coordinates calculated on client (e.g. Android app or Web browser MediaPipe JS) and evaluates fall/bed exit rules.
* **Request Body (`LandmarksBody`)**:
  * `landmarks`: Object or list containing normalized pose landmarks.
* **Response Payload**: `{"event_type": "BED_EXIT", "in_bed": false, ...}`

---

#### `GET /api/fall/status`
* **Handler Function**: `fall_status()`
* **Description**: Returns detailed status parameters of the fall detection engine.
* **Response Payload**:
```json
{
  "ok": true,
  "service": "fall_detection_pi",
  "package": "fall_detection_pi",
  "package_path": "c:\\Users\\Shawn A\\Desktop\\pi python\\fall_detection_pi",
  "separate_process": true,
  "port": 8742,
  "have_cv2": true,
  "have_pose": true
}
```

---

### 2.2 Internal Functions & Module Architecture

#### Module: `web_server.py`
* `lifespan(app: FastAPI)`: Async context manager launching the background camera loop thread (`camera_loop.run`) on startup and stopping it (`camera_loop.stop`) on server shutdown.
* `_generate_self_signed_cert(cert_path: Path, key_path: Path)`: Auto-generates SSL certificate and key for HTTPS fall service.
* `main()`: Entry point parsing `--ssl` flag and starting Uvicorn server on port `8742`.

#### Module: `camera_loop.py`
* `run()`: Primary background camera processing loop thread. Continuously reads frames from physical camera or RTSP stream, performs MediaPipe `PoseLandmarker` inference, draws pose skeleton & ROI polygon overlay, updates thread-safe JPEG buffer, and evaluates fall rules.
* `stop()`: Signals camera loop thread to terminate and closes MediaPipe landmarker instances cleanly.
* `get_latest_jpeg()` -> `Optional[bytes]`: Retrieves latest thread-safe encoded JPEG frame.
* `update_polygon(polygon: List[Tuple[float, float]])`: Updates active bed ROI polygon coordinates and resets detector state machine.
* `get_active_polygon()` -> `List[Tuple[float, float]]`: Returns active bed ROI polygon.
* `get_current_detection_state()` -> `Optional[str]`: Returns string identifier of current detection state (`NORMAL`, `NEAR_BED_EDGE`, `BED_EXIT`, `FALL_DETECTED`).
* `process_client_frame(data: bytes)`: Decodes image bytes, executes pose estimation and fall detector evaluation, and returns detection results.
* `process_normalized_landmarks(landmarks: Any)`: Feeds landmark structure into fall detector state machine and returns result.
* `show_toast(message: str)`: Throttled logger for alert notifications.
* `play_alarm_sound_and_vibrate()` / `stop_alarm_sound_and_vibration()`: Hooks for audible/haptic alert triggers.
* `publish_event_if_needed(event_type: str, ...)`: Dispatches HTTP alert payload to remote backend via `alert_api.py` when state changes occur.

#### Module: `fall_detector.py`
* `FallDetector`: Core state machine class containing temporal posture analysis and bed boundary detection logic.
  * `update(landmarks: List[NormalizedPoint], roi: Optional[PolygonROI])` -> `DetectionState`: Evaluates bounding box center, aspect ratio, hip/shoulder position relative to ROI polygon, and temporal fall indicators. Returns state enum.
  * `reset()`: Resets temporal history counters, state buffers, and post-fall timers.
* `PolygonROI`: Bed ROI polygon class.
  * `contains_point(x: float, y: float)` -> `bool`: Ray-casting algorithm to test if point is inside ROI polygon.
  * `distance_to_edge(x: float, y: float)` -> `float`: Computes minimum distance from point to ROI boundary.
* `RectFNorm`: Normalized bounding box rectangle helper.
  * `inset(dx: float, dy: float)` -> `RectFNorm`: Expands or shrinks bounding box boundaries.
  * `clamp01()` -> `RectFNorm`: Clamps coordinates between `0.0` and `1.0`.
* `DetectionState` (Enum): `NORMAL`, `NEAR_BED_EDGE`, `BED_EXIT`, `FALL_DETECTED`.

#### Module: `alert_api.py`
* `send_fall_alert(event_type: str, patient_id: str, base_url: str)`: Sends HTTP POST request containing fall alert payload to central backend server (`/fall-events` endpoint).
* `send_fall_alert_async(...)`: Dispatches alert dispatch call to non-blocking background thread pool.

#### Module: `config.py`
* `hub_config_path()` -> `Path`: Resolves path to `hub_config.json` for persisting bed ROI coordinates.
* `CAMERA_SOURCE`: Resolves camera source (default `0` or RTSP URL from environment).
* `DEFAULT_POLYGON`: Default full-screen ROI polygon (`[(0.0, 0.0), (1.0, 0.0), (1.0, 1.0), (0.0, 1.0)]`).
* Threshold constants (`BED_EDGE_THRESHOLD`, `FALL_VELOCITY_THRESHOLD`, `LOG_COOLDOWN_MS`).

#### Module: `pose_model.py`
* `get_model_path(model_type: str)` -> `Path`: Resolves local path or automatically downloads MediaPipe `pose_landmarker_*.task` files (`lite`, `full`, `heavy`) from Google CDN.

---

## 3. Summary of Ports & Services

| Application | Process Name | Default Port | Static Frontend | Main Backend File |
| :--- | :--- | :--- | :--- | :--- |
| **Medical BLE Hub** | `medical_ble_web` | `8741` | [static/index.html](file:///c:/Users/Shawn%20A/Desktop/pi%20python/sh3-pi/medical_ble_web/static/index.html) | [sh3-pi/medical_ble_web/app.py](file:///c:/Users/Shawn%20A/Desktop/pi%20python/sh3-pi/medical_ble_web/app.py) |
| **Fall Detection Pi** | `fall_detection_pi` | `8742` | [static/fall.html](file:///c:/Users/Shawn%20A/Desktop/pi%20python/fall_detection_pi/static/fall.html) | [fall_detection_pi/web_server.py](file:///c:/Users/Shawn%20A/Desktop/pi%20python/fall_detection_pi/web_server.py) |

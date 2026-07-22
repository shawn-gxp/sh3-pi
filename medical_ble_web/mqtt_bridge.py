"""
MQTT bridge — Pi hub drop-in for Android EnhancedMqttManager publish path.

Reads clinical rows from SQLite (via callers) and publishes to SHHM topic
``health/readings`` in the same shape the APK used (plus full vitals fields).

Config: medical_ble_web/mqtt_config.json (same idea as android assets/mqtt_config.json).

Hard-coded in config (edit file, no code change):
  hub_id, patient_id
sensorId = device MAC (as Android often used device.address)
"""

from __future__ import annotations

import json
import logging
import threading
import time
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional

log = logging.getLogger("medical_ble_web.mqtt")

_CFG_PATH = Path(__file__).resolve().parent / "mqtt_config.json"
_client = None  # mqtt.Client | None
_cfg: Dict[str, Any] = {}
_lock = threading.Lock()
_start_mono = time.monotonic()
_hb_stop = threading.Event()
_hb_thread: Optional[threading.Thread] = None
_connected = False


def _load_cfg() -> Dict[str, Any]:
    defaults = {
        "enabled": False,
        "broker": "tcp://127.0.0.1:1883",
        "username": "",
        "password": "",
        "topic": "health/readings",
        "hub_id": "pi-hub-sh3-01",
        "patient_id": "00000000-0000-0000-0000-000000000001",
        "qos": 1,
        "heartbeat_s": 30,
        "clinical_types": ["bp", "temp", "spo2", "glucose"],
    }
    if not _CFG_PATH.is_file():
        return defaults
    try:
        data = json.loads(_CFG_PATH.read_text(encoding="utf-8"))
        if isinstance(data, dict):
            defaults.update({k: v for k, v in data.items() if not str(k).startswith("_")})
    except (OSError, json.JSONDecodeError) as exc:
        log.warning("mqtt_config load failed: %s", exc)
    return defaults


def _parse_broker(broker: str) -> tuple[str, int, str]:
    """
    android style: tcp://host:1883  →  (host, port, transport)
    """
    b = (broker or "").strip()
    transport = "tcp"
    if "://" in b:
        scheme, rest = b.split("://", 1)
        transport = scheme.lower()
        b = rest
    host = b
    port = 1883
    if ":" in b:
        host, p = b.rsplit(":", 1)
        try:
            port = int(p)
        except ValueError:
            port = 1883
    return host or "127.0.0.1", port, transport


def _utc_ts(measured_at: Optional[str] = None) -> str:
    """Android used yyyy-MM-dd'T'HH:mm:ss'Z' UTC."""
    if measured_at:
        s = measured_at.strip()
        # already ISO-ish — normalize trailing Z
        if s.endswith("Z"):
            return s[:19] + "Z" if len(s) >= 19 else s
        try:
            # try parse common local forms → UTC Z (best effort: keep as-is + Z)
            for fmt in (
                "%Y-%m-%dT%H:%M:%S.%f",
                "%Y-%m-%dT%H:%M:%S",
                "%Y-%m-%d %H:%M:%S.%f",
                "%Y-%m-%d %H:%M:%S",
            ):
                try:
                    dt = datetime.strptime(s[:26], fmt)
                    # treat naive as local wall clock; still emit Z-like stamp for APK parity
                    return dt.strftime("%Y-%m-%dT%H:%M:%SZ")
                except ValueError:
                    continue
        except Exception:
            pass
        return s
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def sensor_id_from_mac(mac: str) -> str:
    """APK used device.address; we keep full MAC upper-case for stability."""
    return (mac or "").strip().upper() or "unknown"


def is_enabled() -> bool:
    return bool(_load_cfg().get("enabled"))


def start() -> bool:
    """Connect to broker and start heartbeat (APK EnhancedMqttManager.connect)."""
    global _client, _cfg, _connected, _hb_thread

    _cfg = _load_cfg()
    if not _cfg.get("enabled"):
        log.info("MQTT disabled (mqtt_config.json enabled=false)")
        return False

    try:
        import paho.mqtt.client as mqtt
    except ImportError:
        log.error("paho-mqtt not installed — pip install paho-mqtt")
        return False

    host, port, _transport = _parse_broker(str(_cfg.get("broker") or ""))
    client_id = f"pi-hub-{_cfg.get('hub_id', 'sh3')}-{uuid.uuid4().hex[:8]}"

    with _lock:
        if _client is not None:
            try:
                _client.loop_stop()
                _client.disconnect()
            except Exception:
                pass
            _client = None

        # paho-mqtt 1.x vs 2.x Client() constructor
        try:
            client = mqtt.Client(
                mqtt.CallbackAPIVersion.VERSION1,  # type: ignore[attr-defined]
                client_id=client_id,
            )
        except (AttributeError, TypeError, ValueError):
            try:
                client = mqtt.Client(client_id=client_id)
            except TypeError:
                client = mqtt.Client(client_id)

        user = str(_cfg.get("username") or "")
        password = str(_cfg.get("password") or "")
        if user:
            client.username_pw_set(user, password or None)

        def _on_connect(c, userdata, flags, rc, *args):
            global _connected
            _connected = rc == 0
            if rc == 0:
                log.info(
                    "MQTT connected broker=%s:%s hub_id=%s topic=%s",
                    host,
                    port,
                    _cfg.get("hub_id"),
                    _cfg.get("topic"),
                )
                _drain_outbox_async()
            else:
                log.error("MQTT connect failed rc=%s", rc)

        def _on_disconnect(c, userdata, rc, *args):
            global _connected
            _connected = False
            log.warning("MQTT disconnected rc=%s", rc)

        client.on_connect = _on_connect
        client.on_disconnect = _on_disconnect

        try:
            # Non-blocking connect so hub HTTP/BLE still start if broker is down
            client.connect_async(host, port, keepalive=60)
            client.loop_start()
            _client = client
            log.info("MQTT connecting async to %s:%s …", host, port)
        except Exception as exc:
            log.error("MQTT connect error %s:%s — %s", host, port, exc)
            _client = None
            _connected = False
            return False
    # Heartbeat thread (30s like APK)
    _hb_stop.clear()
    if _hb_thread is None or not _hb_thread.is_alive():
        _hb_thread = threading.Thread(target=_heartbeat_loop, name="mqtt-hb", daemon=True)
        _hb_thread.start()

    return True


def stop() -> None:
    global _client, _connected
    _hb_stop.set()
    with _lock:
        if _client is not None:
            try:
                _client.loop_stop()
                _client.disconnect()
            except Exception as exc:
                log.debug("MQTT stop: %s", exc)
            _client = None
        _connected = False


def _heartbeat_loop() -> None:
    while not _hb_stop.wait(timeout=float(_cfg.get("heartbeat_s") or 30)):
        try:
            publish_heartbeat()
        except Exception as exc:
            log.debug("heartbeat: %s", exc)


def _drain_outbox_async():
    def _drain():
        try:
            from db import peek_mqtt_outbox, delete_mqtt_outbox_items  # type: ignore
            items = peek_mqtt_outbox(limit=20)
            if items:
                log.info("[MQTT] draining %d items from outbox", len(items))
            sent_ids: List[int] = []
            for item in items:
                try:
                    payload = json.loads(item["payload_json"])
                    ok = _publish_raw(item["topic"], payload, qos=item["qos"], bypass_outbox=True)
                    if ok:
                        sent_ids.append(int(item["id"]))
                    time.sleep(0.05)
                except Exception:
                    pass
            if sent_ids:
                delete_mqtt_outbox_items(sent_ids)
        except Exception as e:
            log.warning("MQTT outbox drain error: %s", e)
    threading.Thread(target=_drain, name="mqtt-outbox", daemon=True).start()


def _publish_raw(topic: str, payload: dict, qos: Optional[int] = None, bypass_outbox: bool = False) -> bool:
    global _client
    q = int(qos if qos is not None else _cfg.get("qos") or 1)
    body = json.dumps(payload, default=str)
    
    if _client is None or not _connected:
        log.warning("[MQTT] not connected, publish failed topic=%s", topic)
        if not bypass_outbox:
            try:
                from db import insert_mqtt_outbox # type: ignore
                insert_mqtt_outbox(topic, body, qos=q)
                log.info("[MQTT] queued message to outbox")
            except Exception as dbe:
                log.error("[MQTT] outbox insert failed: %s", dbe)
        return False

    try:
        info = _client.publish(topic, body, qos=q, retain=False)
        # paho returns MQTTMessageInfo
        ok = True
        try:
            ok = info.rc == 0  # type: ignore[attr-defined]
        except Exception:
            pass
        if ok:
            log.info("[MQTT] published topic=%s bytes=%d", topic, len(body))
        else:
            log.warning("[MQTT] publish rc issue topic=%s info=%s", topic, info)
            if not bypass_outbox:
                try:
                    from db import insert_mqtt_outbox # type: ignore
                    insert_mqtt_outbox(topic, body, qos=q)
                except Exception:
                    pass
        return bool(ok)
    except Exception as exc:
        log.warning("[MQTT] publish failed topic=%s: %s", topic, exc)
        if not bypass_outbox:
            try:
                from db import insert_mqtt_outbox # type: ignore
                insert_mqtt_outbox(topic, body, qos=q)
                log.info("[MQTT] queued message to outbox")
            except Exception as dbe:
                log.error("[MQTT] outbox insert failed: %s", dbe)
        return False


def publish_heartbeat() -> None:
    """Match EnhancedMqttManager.publishHeartbeat → hub/{hubId}/heartbeat."""
    cfg = _load_cfg()
    hub_id = str(cfg.get("hub_id") or "unknown")
    payload = {
        "hubId": hub_id,
        "timestamp": _utc_ts(),
        "status": "online",
        "uptime": int((time.monotonic() - _start_mono) * 1000),
        "messageType": "heartbeat",
    }
    _publish_raw(f"hub/{hub_id}/heartbeat", payload, qos=0)


def _device_lookup(device_id: Optional[int]) -> Dict[str, Any]:
    if not device_id:
        return {}
    try:
        from db import connect  # type: ignore

        with connect() as conn:
            row = conn.execute(
                "SELECT * FROM devices WHERE id = ?", (device_id,)
            ).fetchone()
            if row:
                return dict(row)
    except Exception:
        pass
    return {}


def publish_clinical_reading(
    *,
    reading_type: str,
    brand: str = "",
    measured_at: Optional[str] = None,
    systolic: Optional[float] = None,
    diastolic: Optional[float] = None,
    pulse_rate: Optional[float] = None,
    spo2: Optional[float] = None,
    temperature: Optional[float] = None,
    glucose_mg_dl: Optional[float] = None,
    perfusion_index: Optional[float] = None,
    device_id: Optional[int] = None,
    mac: str = "",
    device_name: str = "",
    reading_id: Optional[int] = None,
) -> int:
    """
    Publish one clinical SQLite row to health/readings (APK path).

    Emits 1–2 messages matching EnhancedMqttManager.publishSensorData style:
      type + value + unit (+ nested data for BP), plus full vitals for cloud.

    Returns number of MQTT messages sent.
    """
    cfg = _load_cfg()
    if not cfg.get("enabled"):
        return 0
    if _client is None:
        # lazy start
        if not start():
            return 0

    clinical = set(cfg.get("clinical_types") or ["bp", "temp", "spo2", "glucose"])
    rtype = (reading_type or "").lower().strip()
    if rtype not in clinical:
        return 0

    dev = _device_lookup(device_id)
    mac_u = sensor_id_from_mac(mac or dev.get("mac") or "")
    name = (
        device_name
        or dev.get("name")
        or dev.get("model")
        or brand
        or mac_u
    )
    hub_id = str(cfg.get("hub_id") or "unknown")
    topic = str(cfg.get("topic") or "health/readings")

    patient_id = ""
    try:
        from db import get_setting # type: ignore
        patient_id = get_setting("patient_id")
    except Exception:
        pass
    if not patient_id:
        patient_id = str(cfg.get("patient_id") or "")

    ts = _utc_ts(measured_at)

    messages: List[Dict[str, Any]] = []

    def base(**extra: Any) -> Dict[str, Any]:
        d: Dict[str, Any] = {
            "hubId": hub_id,
            "sensorId": mac_u,
            "patientId": patient_id,
            "deviceName": name,
            "timestamp": ts,
            "recorded_at": ts,
            "brand": brand or dev.get("brand") or "",
            "mac": mac_u,
            "dataType": "health_reading",
        }
        if reading_id is not None:
            d["localReadingId"] = reading_id
        # full vitals (SHHM docs + APK combined)
        if pulse_rate is not None:
            d["heartRate"] = pulse_rate
        if spo2 is not None:
            d["spo2"] = spo2
        if systolic is not None:
            d["bloodPressureSystolic"] = systolic
        if diastolic is not None:
            d["bloodPressureDiastolic"] = diastolic
        if temperature is not None:
            d["temperature"] = temperature
        if glucose_mg_dl is not None:
            d["glucose_mg_dl"] = glucose_mg_dl
        if perfusion_index is not None:
            d["perfusion_index"] = perfusion_index
        d.update(extra)
        return d

    # APK-style type/value messages (+ data blob for BP)
    if rtype == "bp":
        data_obj = {
            "systolic": systolic,
            "diastolic": diastolic,
            "pulse_rate": pulse_rate,
        }
        messages.append(
            base(
                type="BLOOD_PRESSURE",
                value=int(systolic) if systolic is not None else None,
                unit="mmHg",
                data=data_obj,
                dataType="BLOOD_PRESSURE",
            )
        )
        if pulse_rate is not None:
            messages.append(
                base(
                    type="HEART_RATE",
                    value=int(pulse_rate),
                    unit="bpm",
                    dataType="HEART_RATE",
                )
            )
    elif rtype == "spo2":
        messages.append(
            base(
                type="SPO2",
                value=int(spo2) if spo2 is not None else None,
                unit="%",
                dataType="SPO2",
            )
        )
        if pulse_rate is not None:
            messages.append(
                base(
                    type="HEART_RATE",
                    value=int(pulse_rate),
                    unit="bpm",
                    dataType="HEART_RATE",
                )
            )
    elif rtype == "temp":
        messages.append(
            base(
                type="TEMPERATURE",
                value=float(temperature) if temperature is not None else None,
                unit="C",
                dataType="TEMPERATURE",
            )
        )
    elif rtype == "glucose":
        messages.append(
            base(
                type="GLUCOSE",
                value=float(glucose_mg_dl) if glucose_mg_dl is not None else None,
                unit="mg/dL",
                dataType="GLUCOSE",
            )
        )
    else:
        return 0

    # Also publish structured patient topic (SHHM HUB_FEATURES) for drop-in routing
    patient_topic = f"patient/{patient_id}/sensor/{mac_u}/data"

    n = 0
    for msg in messages:
        if _publish_raw(topic, msg):
            n += 1
        # secondary structured topic (same payload) — ignore failures
        try:
            _publish_raw(patient_topic, msg)
        except Exception:
            pass
    return n


def notify_reading_inserted(
    *,
    reading_id: Optional[int],
    device_id: Optional[int],
    brand: str,
    reading_type: str,
    measured_at: Optional[str] = None,
    systolic: Optional[float] = None,
    diastolic: Optional[float] = None,
    pulse_rate: Optional[float] = None,
    spo2: Optional[float] = None,
    temperature: Optional[float] = None,
    glucose_mg_dl: Optional[float] = None,
    perfusion_index: Optional[float] = None,
) -> None:
    """Safe fire-and-forget from db.insert_reading (never raises)."""
    try:
        global _cfg
        _cfg = _load_cfg()
        if not _cfg.get("enabled"):
            return
        publish_clinical_reading(
            reading_id=reading_id,
            device_id=device_id,
            brand=brand,
            reading_type=reading_type,
            measured_at=measured_at,
            systolic=systolic,
            diastolic=diastolic,
            pulse_rate=pulse_rate,
            spo2=spo2,
            temperature=temperature,
            glucose_mg_dl=glucose_mg_dl,
            perfusion_index=perfusion_index,
        )
    except Exception as exc:  # noqa: BLE001
        log.warning("MQTT notify skip: %s", exc)


def status() -> Dict[str, Any]:
    cfg = _cfg or _load_cfg()
    return {
        "enabled": bool(cfg.get("enabled")),
        "connected": _connected,
        "broker": cfg.get("broker"),
        "topic": cfg.get("topic"),
        "hub_id": cfg.get("hub_id"),
        "patient_id": cfg.get("patient_id"),
        "config_path": str(_CFG_PATH),
    }

import logging
import threading
from datetime import datetime, timezone
from typing import Optional

import requests

from . import config

logger = logging.getLogger("FallAlertApi")


def _utc_now_iso() -> str:
    """Timezone-aware UTC ISO-8601 (datetime.utcnow is deprecated)."""
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def _post_event_task(
    patient_id: str,
    event_type: str,
    severity: str,
    confidence: Optional[float],
    near_edge: Optional[bool],
    in_safe_area: Optional[bool],
    device_id: str
):
    payload = {
        "patientId": patient_id,
        "eventType": event_type,
        "severity": severity,
        "fallDetected": (event_type == "FALL_DETECTED"),
        "detectedAt": _utc_now_iso(),
        "sourceApp": "EdgeAiFallDetection",
        "deviceId": device_id,
        "cooldownMs": config.COOLDOWN_MS
    }
    
    if confidence is not None:
        payload["confidence"] = confidence
    if near_edge is not None:
        payload["nearEdge"] = near_edge
    if in_safe_area is not None:
        payload["inSafeArea"] = in_safe_area

    headers = {"Content-Type": "application/json"}
    if config.INTEGRATION_KEY:
        headers["x-integration-key"] = config.INTEGRATION_KEY

    url = f"{config.BACKEND_BASE_URL}/fall-events"

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=10.0)
        if not response.ok:
            logger.error(f"Failed to post fall event: HTTP {response.status_code} - {response.text}")
            return
        
        logger.debug(f"Posted fall event successfully: {event_type}")
    except Exception as e:
        logger.error(f"Failed to post alert event: {e}")

def post_event(
    patient_id: str,
    event_type: str,
    severity: str,
    confidence: Optional[float] = None,
    near_edge: Optional[bool] = None,
    in_safe_area: Optional[bool] = None,
    device_id: str = config.DEVICE_ID
):
    if not patient_id or patient_id == "REPLACE_WITH_PATIENT_ID":
        logger.warning("Skipping alert publish because patientId is not configured")
        return

    # Run in background thread to avoid blocking camera loop
    thread = threading.Thread(
        target=_post_event_task,
        args=(patient_id, event_type, severity, confidence, near_edge, in_safe_area, device_id),
        daemon=True
    )
    thread.start()

"""
Omron-like post-measure support for Nipro devices.

Omron keeps history and often accepts connect after the display blanks.
Nipro windows are shorter — we approximate Omron UX by:

  1. find_timeout: keep scanning/retrying connect after a reading (device
     may only advertise for ~1–2 min, or wake briefly after measure)
  2. bulk_history: dump on-device storage (NT-100B TICD slots, NBP BLP stream)
  3. hands-free: long wait for ads, then immediate dump session

Windows (seconds) tuned from げんきノート + field logs.
"""

from __future__ import annotations

from typing import Dict

# How long after a measure we keep hunting the device (Omron-like "still get data")
FIND_WINDOW_S: Dict[str, float] = {
    "nipro_nbp": 120.0,  # cuff often still connectable briefly after reading
    "nipro_nmbp": 120.0,
    "nipro_nsm1": 90.0,
    "nipro_nt100b": 150.0,  # user log: ~1–2 min BLE after measure
    "nipro_cf": 120.0,
    "mightysat": 90.0,  # need sensor on; window for re-appear only
    "thermometer": 150.0,
}

# Per-session receive / listen after link is up
RECEIVE_S: Dict[str, float] = {
    "nipro_nbp": 90.0,
    "nipro_nmbp": 90.0,
    "nipro_nsm1": 60.0,
    "nipro_nt100b": 90.0,  # bulk TICD dump needs time
    "nipro_cf": 90.0,
    "mightysat": 120.0,  # live stream
    "thermometer": 90.0,
}

# Quiet end after last indication (BP multi-record spacing)
QUIET_S: Dict[str, float] = {
    "nipro_nbp": 10.0,
    "nipro_nmbp": 10.0,
    "nipro_nsm1": 6.0,
    "nipro_nt100b": 3.0,  # after bulk pull
    "nipro_cf": 12.0,
    "mightysat": 0.0,  # no quiet end for stream
    "thermometer": 3.0,
}

# NT-100B: how many TICD history slots to pull (Omron-like bulk)
# Device holds ~30; pull all for offline-style support
NT100B_HISTORY_MAX = 30

# Connect retries during find window
CONNECT_RETRIES_POST_MEASURE = 5
SCAN_CHUNK_S = 4.0
CONNECT_ATTEMPT_TIMEOUT_S = 12.0


def find_window_for(profile_id: str) -> float:
    return float(FIND_WINDOW_S.get(profile_id, 90.0))


def receive_s_for(profile_id: str) -> float:
    return float(RECEIVE_S.get(profile_id, 60.0))


def quiet_s_for(profile_id: str) -> float | None:
    q = QUIET_S.get(profile_id)
    if q is None:
        return None
    if q <= 0:
        return 0.0  # stream: disable quiet
    return float(q)

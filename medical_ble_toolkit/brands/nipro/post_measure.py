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
# Field baseline: NBP / NT-100B ~1m05s after measure (margin for scan+connect)
FIND_WINDOW_S: Dict[str, float] = {
    "nipro_nbp": 70.0,
    "nipro_nmbp": 70.0,
    "nipro_nsm1": 70.0,
    "nipro_nt100b": 70.0,
    "nipro_cf": 70.0,
    "mightysat": 90.0,  # only while measuring; reconnect budget
    "thermometer": 70.0,
}

# Per-session receive / listen after link is up
RECEIVE_S: Dict[str, float] = {
    "nipro_nbp": 65.0,
    "nipro_nmbp": 65.0,
    "nipro_nsm1": 60.0,
    "nipro_nt100b": 65.0,
    "nipro_cf": 65.0,
    "mightysat": 600.0,  # full live stream while finger on
    "thermometer": 65.0,
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

# NT-100B TICD slots to pull after measure.
# Hub needs *latest* fast (index 0). Full 30-slot dumps monopolize the radio
# for ~25s and starve MightySat's short live window.
NT100B_HISTORY_MAX = 1  # index 0 only (= latest). Raise for lab bulk dumps.

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

"""
Shared host→device helpers for Nipro げんきノート companion-like sessions.

Wire details: datasheets/nipro/EXACT_HW_SEQUENCES.md
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

# Standard SIG
DATE_TIME_UUID = "00002a08-0000-1000-8000-00805f9b34fb"
BP_SERVICE_UUID = "00001810-0000-1000-8000-00805f9b34fb"
BP_MEASUREMENT_UUID = "00002a35-0000-1000-8000-00805f9b34fb"
HTS_SERVICE_UUID = "00001809-0000-1000-8000-00805f9b34fb"
TEMP_MEASUREMENT_UUID = "00002a1c-0000-1000-8000-00805f9b34fb"
DIS_SERIAL_UUID = "00002a25-0000-1000-8000-00805f9b34fb"

# NSM-1BLE custom (A&D-style, short write — companion sends 3 bytes, not 20-pad)
NSM_CUSTOM_SERVICE = "233bf000-5a34-1b6d-975c-000d5690abe4"
NSM_CUSTOM_CHAR = "233bf001-5a34-1b6d-975c-000d5690abe4"
NSM_DISCONNECT = bytes([0x02, 0x01, 0x03])

# NT-100B TICD power-off (only custom write on companion receive teardown)
NT100B_CUSTOM_SERVICE = "00001523-1212-efde-1523-785feabcd123"
NT100B_CUSTOM_CHAR = "00001524-1212-efde-1523-785feabcd123"

# Timing (companion)
POST_CONNECT_CLOCK_DELAY_S = 1.0
POST_DISCONNECT_SETTLE_S = 0.1
NT100B_POST_POWEROFF_DELAY_S = 1.0
DEFAULT_RECEIVE_TIMEOUT_S = 60.0

# Name prefixes (DeviceName match substrings)
PREFIX_NBP = "NBP-1BLE"
PREFIX_NMBP = "NMBP"
PREFIX_NSM = "NSM-1BLE"
PREFIX_NT100B = "NT-100B"
PREFIX_CF = "NIPRO CF"
PREFIX_NBCM = "NBCM"
PREFIX_MIGHTY = "MightySat"


def encode_date_time_2a08(when: Optional[datetime] = None) -> bytes:
    """SIG Date Time 0x2A08 — same as companion BP/NSM clock write."""
    dt = when or datetime.now()
    y = int(dt.year)
    return bytes(
        [
            y & 0xFF,
            (y >> 8) & 0xFF,
            dt.month & 0xFF,
            dt.day & 0xFF,
            dt.hour & 0xFF,
            dt.minute & 0xFF,
            dt.second & 0xFF,
        ]
    )


def is_invalid_bp_companion(
    systolic: Optional[float],
    diastolic: Optional[float],
    pulse: Optional[float],
    measured_at: Optional[datetime],
) -> bool:
    """Companion reject: 2047 sentinels or missing timestamp."""
    if measured_at is None or measured_at == datetime.min:
        return True
    for v in (systolic, diastolic, pulse):
        if v is None:
            continue
        if v == 2047.0 or v == 2048.0:
            return True
    return False


def is_invalid_temp_companion(
    temperature: Optional[float],
    measured_at: Optional[datetime],
) -> bool:
    if temperature is None:
        return True
    if temperature == 65535.0 or temperature < 0:
        return True
    if measured_at is None or measured_at == datetime.min:
        # Companion allows DateTime.Now for NT path sometimes; only reject MinValue
        return False
    return False

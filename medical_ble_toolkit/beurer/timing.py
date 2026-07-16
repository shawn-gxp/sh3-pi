"""
Companion-app timing constants (HealthManager Pro 1.20.1).

Sources:
  - BloodPressureDeviceSyncRepoImpl (settle 3s, BTFlowTimer quiet ~4s)
  - Gl50SyncRepo (CCCD 50/300 ms, optional 2500 ms before RACP)
  - TemperatureSyncRepoImpl (indicate collect)
  - PulseOxyDeviceSyncRepoImpl (stream + request-more)

These delays are intentional: many Beurer meters reject or drop the session
if the host races CCCD / RACP / indicate setup.
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class SessionTiming:
    """Host-side delays for one protocol family."""

    # After BLE link up, before first subscribe (app marker `te` uses 3s)
    post_connect_settle_s: float = 0.0
    # Between pair() completing and subscribe
    post_pair_settle_s: float = 0.5
    # After each CCCD enable (bleak start_notify)
    after_cccd_s: float = 0.30
    # Between sequential CCCD operations (glucose multi-char)
    between_cccd_s: float = 0.05
    # After DIS / optional reads
    after_dis_s: float = 0.15
    # After set-time write
    after_set_time_s: float = 0.25
    # Before RACP / history query (glucose marker mg path uses 2.5s)
    before_history_query_s: float = 0.0
    # Quiet window: no new notify/indicate → sync done (BTFlowTimer ~4)
    quiet_timeout_s: float = 4.0
    # Absolute max listen even if packets keep coming
    max_listen_s: float = 90.0
    # Min listen before quiet can fire (avoid ending before first dump starts)
    min_listen_before_quiet_s: float = 1.5
    # Retry connect gap
    connect_retry_s: float = 1.5
    # Write-without-response pacing (PO60 request-more)
    write_pacing_s: float = 0.08


# --- Per-profile timings matching APK behavior ---

BP_SIG = SessionTiming(
    post_connect_settle_s=0.8,   # short default; use 3.0 if model needs settle
    post_pair_settle_s=0.8,
    after_cccd_s=0.35,
    after_dis_s=0.15,
    after_set_time_s=0.30,
    quiet_timeout_s=4.0,
    max_listen_s=120.0,
    min_listen_before_quiet_s=2.0,
)

# Models that implement app marker-style 3s settle before subscribe
BP_SETTLE_3S_MODELS = frozenset(
    {
        "BM59",
        "BM64",
        "BM69",
        "BM77",
        "BM81",
        "BM82",
        "BM85",
        "BM92",
        "BM93",
        "BM95",
        "BM96",
        "ME90",
        "ME95",
        "ELITE",
        "ELITE900",
        "ELITE950",
        "ELITEPLUS",
        "SERIES800",
        "SERIES700",
        "SERIES700W",
        "SERIES1000",
        "PREMIUM800",
        "DELUXE600",
        "DELUXE500",
    }
)

# Write Current Time 0x2A2B after connect (BM59 path + ECG combo)
BP_SET_TIME_MODELS = frozenset(
    {
        "BM59",
        "BM64",
        "BM93",
        "BM95",
        "BM96",
        "ME90",
        "ME95",
    }
)

GLUCOSE = SessionTiming(
    post_connect_settle_s=0.5,
    post_pair_settle_s=0.6,
    after_cccd_s=0.30,
    between_cccd_s=0.05,
    before_history_query_s=0.50,  # use 2.5 if marker mg (conservative 0.5 default)
    quiet_timeout_s=5.0,
    max_listen_s=90.0,
    min_listen_before_quiet_s=1.0,
)

# Some glucose models need longer pre-RACP delay (app marker mg)
GLUCOSE_LONG_RACP_DELAY_S = 2.5
GLUCOSE_LONG_RACP_MODELS = frozenset(
    {"GL50", "GL50EVO", "GL60", "GL48", "GL49"}
)

THERMO_FT = SessionTiming(
    post_connect_settle_s=0.4,
    post_pair_settle_s=0.5,
    after_cccd_s=0.30,
    quiet_timeout_s=4.0,
    max_listen_s=60.0,
    min_listen_before_quiet_s=1.0,
)

PO60 = SessionTiming(
    post_connect_settle_s=0.4,
    after_cccd_s=0.25,
    write_pacing_s=0.10,
    quiet_timeout_s=6.0,
    max_listen_s=120.0,
    min_listen_before_quiet_s=1.5,
)

SCALE = SessionTiming(
    post_connect_settle_s=0.6,
    after_cccd_s=0.30,
    after_set_time_s=0.35,
    write_pacing_s=0.12,
    quiet_timeout_s=5.0,
    max_listen_s=120.0,
)

TRACKER = SessionTiming(
    post_connect_settle_s=0.5,
    after_cccd_s=0.25,
    after_set_time_s=0.40,
    write_pacing_s=0.15,
    quiet_timeout_s=5.0,
    max_listen_s=90.0,
)

ECG_COMBO = SessionTiming(
    post_connect_settle_s=1.0,
    post_pair_settle_s=1.0,
    after_cccd_s=0.35,
    after_set_time_s=0.40,
    quiet_timeout_s=5.0,
    max_listen_s=180.0,
    min_listen_before_quiet_s=2.5,
)


def timing_for_profile(profile_id: str, model_id: str = "") -> SessionTiming:
    """Resolve timing preset for a protocol profile + optional model overrides."""
    pid = (profile_id or "").lower()
    mid = (model_id or "").upper().strip()

    if pid in ("beurer_bp", "beurer_bm54", "bp_sig", "beurer"):
        t = BP_SIG
        if mid in BP_SETTLE_3S_MODELS:
            return SessionTiming(
                **{
                    **t.__dict__,
                    "post_connect_settle_s": 3.0,
                    "post_pair_settle_s": 1.0,
                }
            )
        return t

    if pid in ("beurer_glucose", "glucose_sig", "glucose"):
        t = GLUCOSE
        if mid in GLUCOSE_LONG_RACP_MODELS:
            return SessionTiming(
                **{**t.__dict__, "before_history_query_s": GLUCOSE_LONG_RACP_DELAY_S}
            )
        return t

    if pid in ("beurer_thermo", "thermometer_sig", "beurer_ft"):
        return THERMO_FT
    if pid in ("beurer_po60", "pulse_ox", "po60"):
        return PO60
    if pid in ("beurer_scale", "scale_mixed", "bf700", "bf600"):
        return SCALE
    if pid.startswith("beurer_as") or pid in ("tracker_as87", "tracker_as98", "tracker_as99"):
        return TRACKER
    if pid in ("beurer_ecg", "ecg_custom"):
        return ECG_COMBO

    return SessionTiming()


def model_wants_set_time(model_id: str) -> bool:
    return (model_id or "").upper().strip() in BP_SET_TIME_MODELS

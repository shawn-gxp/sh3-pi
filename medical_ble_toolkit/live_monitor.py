"""
Live companion-style monitor for the interactive CLI.

Mirrors phone-app behaviour:
  - continuous cycle at a fixed interval (default 60s) for batch devices (BP etc.)
  - streaming brands (Masimo MightySat): stay connected and refresh the dashboard
    as SpO2/PR packets arrive — interval is UI refresh / reconnect gap (min 1s)
  - terminal dashboard auto-refreshes with the latest reading

  python -m medical_ble_toolkit          → wizard → LIVE MONITOR
  from medical_ble_toolkit.live_monitor import run_live_monitor

Ctrl+C stops the loop cleanly.
"""

from __future__ import annotations

import asyncio
import logging
import shutil
import sys
import time
from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, List, Optional

from .ble_client import MedicalBleClient, _brief, scan_devices, setup_logging
from .common.hexutil import ms_timestamp
from .common.winrt_errors import (
    is_windows,
    os_pair_supported,
    pairing_ui_hint,
    remove_bond_instructions,
)
from .profiles import DeviceProfile, get_profile

log = logging.getLogger("medical_ble.live")

DEFAULT_INTERVAL_S = 60.0
MIN_INTERVAL_S = 1.0  # user may set 1s for near-real-time UI
DEFAULT_SCAN_TIMEOUT_S = 8.0
DEFAULT_LISTEN_S = 45.0
MAX_HISTORY = 8
MAX_LOG_LINES = 12

# Brands / profiles that stream continuously (stay connected; paint on each vitals packet)
STREAMING_BRAND_IDS = frozenset({"masimo"})
STREAMING_PROFILE_IDS = frozenset({"mightysat"})


# ---------------------------------------------------------------------------
# Display helpers
# ---------------------------------------------------------------------------

def _term_width(default: int = 72) -> int:
    try:
        return max(60, min(100, shutil.get_terminal_size((default, 24)).columns))
    except Exception:  # noqa: BLE001
        return default


def _clear_screen() -> None:
    # ANSI home + clear; works in Windows Terminal / modern PowerShell / conhost
    sys.stdout.write("\033[H\033[J")
    sys.stdout.flush()


def _fmt_reading(r: Any) -> str:
    """One-line clinical summary for the dashboard hero panel."""
    if r is None:
        return "(none yet)"
    if hasattr(r, "to_dict"):
        d = r.to_dict()
    elif isinstance(r, dict):
        d = r
    else:
        return _brief(r)

    # Blood pressure
    if d.get("systolic") is not None:
        sys_v = d.get("systolic")
        dia = d.get("diastolic")
        pulse = d.get("pulse_rate")
        when = d.get("measured_at") or d.get("timestamp") or ""
        parts = [f"SYS {sys_v}  DIA {dia}  PULSE {pulse}"]
        if when:
            parts.append(f"@ {when}")
        return "  ".join(str(p) for p in parts if p is not None)

    # SpO2
    if d.get("spo2") is not None:
        return f"SpO2 {d.get('spo2')}%  PR {d.get('pulse_rate')}  PI {d.get('perfusion_index')}"

    # Temperature
    if d.get("object_temperature") is not None or d.get("temperature") is not None:
        t = d.get("object_temperature", d.get("temperature"))
        unit = d.get("unit") or "C"
        return f"Temp {t} {unit}"

    # Glucose
    if d.get("blood_glucose_mg_dl") is not None:
        return f"Glucose {d.get('blood_glucose_mg_dl')} mg/dL"

    return _brief(r)


def _reading_key(r: Any) -> str:
    """Clinical value key (ignores measured_at so same SpO2 is not log spam)."""
    if r is None:
        return ""
    if hasattr(r, "to_dict"):
        d = r.to_dict()
    elif isinstance(r, dict):
        d = r
    else:
        return repr(r)
    keys = (
        "systolic",
        "diastolic",
        "pulse_rate",
        "spo2",
        "perfusion_index",
        "pvi",
        "rrp",
        "object_temperature",
        "blood_glucose_mg_dl",
        "sequence",
    )
    return "|".join(f"{k}={d.get(k)}" for k in keys if k in d)


def _is_streaming_device(brand_id: str, profile: Optional[DeviceProfile]) -> bool:
    if (brand_id or "").lower() in STREAMING_BRAND_IDS:
        return True
    if profile is not None and profile.id in STREAMING_PROFILE_IDS:
        return True
    return False


def _is_live_vitals_reading(r: Any) -> bool:
    """True for SpO2/BP/temp/glucose — False for waveforms / acks / raw dicts."""
    if r is None:
        return False
    # PulseOximeterData and similar dataclasses
    if getattr(r, "spo2", None) is not None or getattr(r, "systolic", None) is not None:
        return True
    if getattr(r, "object_temperature", None) is not None:
        return True
    if getattr(r, "blood_glucose_mg_dl", None) is not None:
        return True
    if isinstance(r, dict):
        return any(
            r.get(k) is not None
            for k in ("spo2", "systolic", "object_temperature", "blood_glucose_mg_dl")
        )
    if hasattr(r, "to_dict"):
        d = r.to_dict()
        return any(
            d.get(k) is not None
            for k in ("spo2", "systolic", "object_temperature", "blood_glucose_mg_dl")
        )
    return False


# ---------------------------------------------------------------------------
# Error classification (operator-facing)
# ---------------------------------------------------------------------------

@dataclass(frozen=True)
class FailureHint:
    code: str
    short: str
    tips: tuple[str, ...]
    # If True, live loop should back off harder (pair required)
    needs_pair: bool = False
    # Extra wait multiplier on top of interval (e.g. 1.5)
    backoff_mult: float = 1.0


def classify_failure(exc: BaseException, *, brand_id: str = "") -> FailureHint:
    msg = str(exc).lower()
    raw = str(exc)

    if "fe4a" in msg or "parent service not found" in msg:
        return FailureHint(
            code="OMRON_FE4A_MISSING",
            short="FE4A missing after connect — OS bond incomplete (not a parser issue)",
            tips=(
                "1. Ctrl+C stop LIVE (PAIR is a one-time fix, not every cycle)",
                f"2. {remove_bond_instructions()}",
                "3. Phone OMRON Connect → forget this cuff (one host only)",
                "4. Cuff HOLD BT until flashing P → menu RE-PAIR → accept pairing prompt",
                "5. Then SHORT-press BT and run READ once (not LIVE) to verify",
                "6. Only after READ works: restart LIVE (connects immediately, no long scan)",
            ),
            needs_pair=True,
            backoff_mult=2.0,
        )

    if any(
        s in msg
        for s in (
            "access denied",
            "not permitted",
            "insufficient authentication",
            "0x80070005",
            "authentication required",
        )
    ):
        return FailureHint(
            code="AUTH_DENIED",
            short="Encrypted GATT denied — need OS bond",
            tips=(
                "Run PAIR (flashing P) then retry LIVE",
                remove_bond_instructions(),
            ),
            needs_pair=True,
            backoff_mult=1.5,
        )

    if any(s in msg for s in ("unreachable", "not found", "device not found", "timed out", "timeout")):
        return FailureHint(
            code="NOT_REACHABLE",
            short="Cuff not reachable / not advertising",
            tips=(
                "SHORT-press Bluetooth (transfer mode) just before a cycle",
                "Keep cuff < 1 m from PC; close phone Omron app",
                "Scan empty is normal when cuff is asleep — wake it",
            ),
            backoff_mult=1.0,
        )

    if any(
        s in msg
        for s in (
            "scanner",
            "watcher",
            "aborted",
            "stopping",
            "not ready",
            "in progress",
        )
    ):
        return FailureHint(
            code="SCANNER_BUSY",
            short="BLE scanner/adapter busy",
            tips=(
                "Toggle Bluetooth Off → wait 3s → On (or bluetoothctl power off/on)",
                "Close nRF Connect / other BLE apps / web UI if using CLI",
                "LIVE still connects by MAC when known — retry",
            ),
            backoff_mult=1.2,
        )

    return FailureHint(
        code="SYNC_FAILED",
        short=raw[:90] if raw else type(exc).__name__,
        tips=("See activity log; retry next cycle",),
        backoff_mult=1.0,
    )


# ---------------------------------------------------------------------------
# State
# ---------------------------------------------------------------------------

@dataclass
class LiveState:
    brand_id: str
    brand_label: str
    model: str
    address: str
    interval_s: float = DEFAULT_INTERVAL_S
    mode: str = "monitor"  # monitor | scan_only

    status: str = "starting"
    cycle: int = 0
    next_in_s: float = 0.0
    last_scan_at: Optional[str] = None
    last_sync_at: Optional[str] = None
    last_error: str = ""
    fail_code: str = ""
    tips: List[str] = field(default_factory=list)
    devices_found: List[str] = field(default_factory=list)
    consecutive_fails: int = 0
    # Extra sleep after hard failures (pair needed)
    extra_backoff_s: float = 0.0

    latest: Any = None
    history: List[Any] = field(default_factory=list)
    total_syncs_ok: int = 0
    total_readings: int = 0
    log_lines: List[str] = field(default_factory=list)

    def push_log(self, msg: str) -> None:
        ts = datetime.now().strftime("%H:%M:%S")
        # Keep log lines short enough for the dashboard width
        if len(msg) > 90:
            msg = msg[:87] + "…"
        self.log_lines.append(f"[{ts}] {msg}")
        if len(self.log_lines) > MAX_LOG_LINES:
            self.log_lines = self.log_lines[-MAX_LOG_LINES:]

    def apply_failure(self, exc: BaseException) -> FailureHint:
        hint = classify_failure(exc, brand_id=self.brand_id)
        self.last_error = hint.short
        self.fail_code = hint.code
        self.tips = list(hint.tips)
        self.consecutive_fails += 1
        self.extra_backoff_s = max(
            0.0,
            (self.interval_s * (hint.backoff_mult - 1.0))
            + (15.0 if hint.needs_pair and self.consecutive_fails >= 2 else 0.0),
        )
        self.push_log(f"{hint.code}: {hint.short}")
        if hint.needs_pair:
            self.status = "PAIR required — see tips below"
        else:
            self.status = f"sync failed ({hint.code}) — will retry"
        return hint

    def clear_failure(self) -> None:
        self.last_error = ""
        self.fail_code = ""
        self.tips = []
        self.consecutive_fails = 0
        self.extra_backoff_s = 0.0

    def set_latest(self, reading: Any) -> bool:
        """
        Update latest reading. Returns True if clinical values changed
        (so callers can log once). Same SpO2 still refreshes latest object
        for live timestamps / dashboard paint.
        """
        if reading is None:
            return False
        new_key = _reading_key(reading)
        old_key = _reading_key(self.latest)
        self.latest = reading
        if new_key and new_key == old_key:
            return False
        self.history.insert(0, reading)
        self.history = self.history[:MAX_HISTORY]
        return True


def _clip_row(text: str, width: int) -> str:
    """Fit text into dashboard inner width (w-4 printable chars)."""
    inner = max(20, width - 4)
    if len(text) <= inner:
        return text
    return text[: inner - 1] + "…"


def render_dashboard(state: LiveState) -> str:
    w = _term_width()
    bar = "═" * (w - 2)
    thin = "─" * (w - 2)
    lines: List[str] = []

    def row(text: str = "") -> None:
        text = _clip_row(text, w)
        pad = max(0, w - 4 - len(text))
        lines.append(f"║ {text}{' ' * pad} ║")

    lines.append(f"╔{bar}╗")
    title = (
        "Medical BLE — LIVE STREAM  (real-time SpO2/PR)"
        if state.mode == "stream"
        else "Medical BLE — LIVE MONITOR  (app-style auto sync)"
    )
    row(title)
    row(f"Brand: {state.brand_label}   Model: {state.model}")
    row(
        f"MAC: {state.address or '(scan any)'}   "
        f"Interval: {state.interval_s:.1f}s   Mode: {state.mode}"
    )
    row(
        f"Cycle: #{state.cycle}   Status: {state.status}   "
        f"Next in: {max(0, state.next_in_s):.1f}s"
    )
    lines.append(f"╠{thin}╣")
    row("LATEST READING  (live stream / sync updates)")
    row("")
    hero = _fmt_reading(state.latest)
    row(f"  ★  {hero}")
    if state.last_sync_at:
        row(f"  last sync: {state.last_sync_at}")
    row(
        f"  totals: ok_syncs={state.total_syncs_ok}  "
        f"readings_seen={state.total_readings}  "
        f"fails_in_a_row={state.consecutive_fails}"
    )
    lines.append(f"╠{thin}╣")
    row("RECENT HISTORY")
    if not state.history:
        row("  (waiting for first successful sync…)")
    else:
        for i, r in enumerate(state.history[:5]):
            row(f"  [{i}] {_fmt_reading(r)}")
    lines.append(f"╠{thin}╣")
    row("NEARBY / LAST SCAN")
    if state.last_scan_at:
        row(f"  at {state.last_scan_at}")
    if not state.devices_found:
        row("  (no devices listed — cuff may be asleep; short-press BT to wake)")
    else:
        for d in state.devices_found[:6]:
            row(f"  · {d}")
    if state.tips:
        lines.append(f"╠{thin}╣")
        row(f"WHAT TO DO  [{state.fail_code or 'HINT'}]")
        for t in state.tips[:6]:
            row(f"  {t}")
    lines.append(f"╠{thin}╣")
    row("ACTIVITY LOG")
    for ln in state.log_lines[-6:] or ["  (idle)"]:
        row(f"  {ln}")
    if state.last_error:
        row(f"  ! {state.last_error}")
    lines.append(f"╚{bar}╝")
    lines.append(
        "  Ctrl+C stop  ·  Omron: SHORT-press BT each sync  ·  PAIR only if FE4A/bond tips show"
    )
    return "\n".join(lines)


def paint(state: LiveState) -> None:
    _clear_screen()
    print(render_dashboard(state))


# ---------------------------------------------------------------------------
# Sync workers (one cycle)
# ---------------------------------------------------------------------------

async def _cycle_scan(
    state: LiveState,
    profile: Optional[DeviceProfile],
    *,
    timeout: float = DEFAULT_SCAN_TIMEOUT_S,
) -> List[Any]:
    state.status = "scanning…"
    paint(state)
    try:
        devices = await scan_devices(profile=profile, timeout=timeout, retries=2)
    except Exception as exc:  # noqa: BLE001
        state.last_error = f"{type(exc).__name__}: {exc}"
        state.push_log(f"scan failed: {exc}")
        return []
    state.last_scan_at = ms_timestamp()
    state.devices_found = [
        f"{d.address}  {d.name or '(no name)'}" for d in devices[:15]
    ]
    state.push_log(f"scan → {len(devices)} device(s)")
    return devices


def _mac_in_scan(devices: List[Any], address: str) -> bool:
    if not address:
        return False
    want = address.strip().upper().replace("-", ":")
    for d in devices:
        got = (getattr(d, "address", "") or "").strip().upper().replace("-", ":")
        if got == want:
            return True
    return False


async def _cycle_omron_read(
    state: LiveState,
    *,
    output_dir: Optional[str] = None,
    devices: Optional[List[Any]] = None,
    seen_in_scan: bool = False,
) -> None:
    from .omron_bridge import flatten_readings, read_omron

    # Hard bond failure: don't thrash every 60s — keep tips, try every 3rd cycle
    if state.fail_code == "OMRON_FE4A_MISSING" and state.consecutive_fails >= 2:
        if state.cycle % 3 != 0:
            state.status = "waiting — RE-PAIR required (skipping this cycle)"
            state.push_log("skip READ — fix Windows bond first (see tips)")
            state.tips = list(
                classify_failure(
                    OSError("fe4a parent service not found"),
                    brand_id="omron",
                ).tips
            )
            return

    if devices is not None and not seen_in_scan and not _mac_in_scan(devices, state.address):
        state.push_log("cuff not in last scan — connect by MAC; short-press BT if asleep")
        state.tips = [
            "SHORT-press Bluetooth (transfer, not P) right before/during this cycle",
            "LIVE connects immediately (no long pre-scan) to catch the transfer window",
            "If FE4A still missing after connect → RE-PAIR (bond), not more scanning",
        ]

    if seen_in_scan:
        state.push_log("cuff was advertising — connecting NOW (transfer window is short)")

    state.status = "syncing Omron…"
    paint(state)
    state.push_log("Omron READ — direct connect (no pair on read)")
    try:
        all_users = await read_omron(
            state.address,
            state.model,
            # find_timeout unused for scan-less connect; keep small
            find_timeout=15.0,
            session_retries=2,
            output_dir=output_dir,
        )
    except Exception as exc:  # noqa: BLE001
        state.apply_failure(exc)
        if seen_in_scan and state.fail_code == "OMRON_FE4A_MISSING":
            state.push_log(
                "NOTE: scan saw cuff but FE4A missing → bond is broken (not asleep)"
            )
            state.tips = [
                "Scan already proved transfer advertising works",
                "OS cannot open FE4A without a good bond",
                f"Ctrl+C → {remove_bond_instructions()} → RE-PAIR (flashing P)",
                "Unpair phone OMRON Connect first if it still owns the cuff",
                "Verify with: omron read … then restart LIVE",
            ]
        return

    flat = flatten_readings(all_users)
    state.last_sync_at = ms_timestamp()
    state.total_readings += len(flat)
    state.clear_failure()
    if flat:
        state.total_syncs_ok += 1
        newest = flat[0]
        if state.set_latest(newest):
            state.push_log(f"NEW latest: {_fmt_reading(newest)}")
        else:
            state.push_log(f"sync ok, same latest ({len(flat)} records)")
        # refresh history with up to MAX from this dump
        for r in flat[:MAX_HISTORY]:
            if r is not newest:
                key = _reading_key(r)
                if key not in {_reading_key(h) for h in state.history}:
                    state.history.append(r)
        state.history = state.history[:MAX_HISTORY]
        state.status = f"ok — {len(flat)} record(s)"
    else:
        state.push_log("sync ok but empty EEPROM / no records")
        state.status = "connected, no new data"
        state.total_syncs_ok += 1


async def _cycle_beurer(state: LiveState) -> None:
    from .beurer.session import BeurerCompanionSession

    state.status = "syncing Beurer…"
    paint(state)
    state.push_log("Beurer companion sync")
    sess = BeurerCompanionSession(
        state.address,
        model_id=state.model,
        pair=os_pair_supported(),
        duration=min(DEFAULT_LISTEN_S, state.interval_s * 0.6),
        connect_retries=2,
    )
    try:
        result = await sess.run()
    except Exception as exc:  # noqa: BLE001
        state.apply_failure(exc)
        return

    state.last_sync_at = ms_timestamp()
    state.total_readings += len(result.readings)
    if result.ok:
        state.total_syncs_ok += 1
        state.clear_failure()
    else:
        state.last_error = result.message
        state.fail_code = result.status.value if hasattr(result.status, "value") else "BEURER"
        state.tips = [result.message] if result.message else []
        state.consecutive_fails += 1

    if result.readings:
        newest = result.readings[0]
        if state.set_latest(newest):
            state.push_log(f"NEW latest: {_fmt_reading(newest)}")
        else:
            state.push_log(f"sync {result.status.value}: {len(result.readings)} reading(s)")
        for r in result.readings[1:MAX_HISTORY]:
            if _reading_key(r) not in {_reading_key(h) for h in state.history}:
                state.history.append(r)
        state.history = state.history[:MAX_HISTORY]
        state.status = f"{result.status.value} — {len(result.readings)} reading(s)"
    else:
        state.push_log(result.message or result.status.value)
        state.status = result.status.value


async def _cycle_generic(
    state: LiveState,
    profile: DeviceProfile,
    *,
    pair: bool = True,
    listen_s: float = DEFAULT_LISTEN_S,
) -> None:
    state.status = f"connecting {profile.id}…"
    paint(state)
    # Honor short intervals for simple sensors; thermometer needs longer session
    # (dual-wake + history poll ≈ several seconds of writes).
    if profile.id == "thermometer":
        listen = max(18.0, min(listen_s, 45.0))
    else:
        listen = min(listen_s, max(3.0, state.interval_s * 0.85))
        if state.interval_s >= 15.0:
            listen = min(listen_s, max(15.0, state.interval_s * 0.5))
    state.push_log(f"connect {profile.id} listen={listen:.1f}s")

    client = MedicalBleClient(
        address=state.address,
        profile=profile,
        pair=pair and os_pair_supported(),
        connect_retries=2,
        auto_dispatch=profile.id in ("re_generic", "fora6"),
    )
    try:
        await client.run(
            duration=listen,
            connect_timeout=30.0,
        )
    except Exception as exc:  # noqa: BLE001
        state.apply_failure(exc)
        return

    state.last_sync_at = ms_timestamp()
    n = len(client.readings)
    state.total_readings += n
    if n:
        state.total_syncs_ok += 1
        state.clear_failure()
        # Prefer clinical vitals over waveforms / acks
        candidate = None
        for r in client.readings:
            if _is_live_vitals_reading(r):
                candidate = r
                break
        if candidate is None:
            candidate = client.readings[-1]
        if state.set_latest(candidate):
            state.push_log(f"NEW latest: {_fmt_reading(candidate)}")
        else:
            state.push_log(f"sync ok, {n} reading(s)")
        state.status = f"ok — {n} reading(s)"
    elif client.raw_payloads:
        state.push_log(f"{len(client.raw_payloads)} raw packet(s), none parsed")
        state.status = "raw only (no parse)"
        state.consecutive_fails += 1
    else:
        state.push_log("no notifications this cycle")
        state.status = "no data — wake device / measure"
        if profile.id == "thermometer":
            state.fail_code = "THERMO_NO_DATA"
            state.tips = [
                "1. Confirm MAC is NT-100B (scan name should look like NT-100 / Thermometer)",
                "2. Take a forehead measurement, leave device on / advertising",
                f"3. Menu → PAIR once ({pairing_ui_hint()}), then LIVE again",
                "4. If [GATT] lacks 0x1524: note which chars have Write+Notify — may be OEM UUID",
                "5. Interval 5–15s is enough; 1s only reconnect thrash (needs ~18s session)",
            ]
            state.extra_backoff_s = max(state.extra_backoff_s, 4.0)
        else:
            state.tips = [
                "Trigger a measurement or wake the device before next cycle",
                "Confirm bond / --pair if encrypted characteristics",
            ]
        state.consecutive_fails += 1


async def _cycle_stream(
    state: LiveState,
    profile: DeviceProfile,
    *,
    pair: bool = True,
) -> None:
    """
    Real-time stream session (Masimo MightySat).

    Connect once, stay linked, paint dashboard when SpO2/PR packets arrive.
    `interval_s` controls minimum UI refresh spacing and post-drop reconnect wait
    — NOT a full disconnect/reconnect cycle for every tick.
    """
    state.status = "streaming live…"
    paint(state)
    state.push_log(
        f"stream session {profile.id} — UI refresh ≥{state.interval_s:.1f}s, stay connected"
    )

    last_paint_mono = 0.0
    vitals_count = 0
    # Long session; outer loop reconnects if the device drops
    session_s = 3600.0

    def on_reading(r: Any) -> None:
        nonlocal last_paint_mono, vitals_count
        if not _is_live_vitals_reading(r):
            return
        vitals_count += 1
        state.total_readings += 1
        state.last_sync_at = ms_timestamp()
        state.clear_failure()
        changed = state.set_latest(r)
        # Always refresh hero line with wall-clock freshness
        state.status = f"LIVE  {_fmt_reading(r)}"
        if changed:
            state.push_log(f"live: {_fmt_reading(r)}")
            state.total_syncs_ok += 1
        now = time.monotonic()
        # Paint at least every interval (1s default for user), always on value change
        min_gap = max(0.15, min(state.interval_s, 1.0))
        if changed or (now - last_paint_mono) >= min_gap:
            last_paint_mono = now
            state.next_in_s = 0.0
            try:
                paint(state)
            except Exception:  # noqa: BLE001
                pass

    client = MedicalBleClient(
        address=state.address,
        profile=profile,
        pair=pair and os_pair_supported(),
        connect_retries=2,
        on_reading=on_reading,
    )
    try:
        await client.run(duration=session_s, connect_timeout=30.0)
    except Exception as exc:  # noqa: BLE001
        state.apply_failure(exc)
        return

    if vitals_count:
        state.push_log(f"stream ended after {vitals_count} vitals update(s)")
        state.status = "stream ended — reconnecting…"
    else:
        state.push_log("stream ended with no SpO2/PR packets")
        state.status = "no vitals — check finger sensor / bond"
        state.consecutive_fails += 1
        state.tips = [
            "Keep finger in MightySat; sensor must be on",
            "If no packets: re-run PAIR once, then LIVE again",
        ]


# ---------------------------------------------------------------------------
# Main loop
# ---------------------------------------------------------------------------

async def run_live_monitor(
    *,
    brand_id: str,
    brand_label: str,
    model: str,
    address: str,
    connect_profile: Optional[str] = None,
    is_omron: bool = False,
    interval_s: float = DEFAULT_INTERVAL_S,
    mode: str = "monitor",  # monitor | scan_only
    output_dir: Optional[str] = None,
    pair: bool = True,
    max_cycles: Optional[int] = None,
) -> int:
    """
    Continuous companion-style loop.

    mode=monitor  : every interval, try sync (read/connect) and update latest
    mode=scan_only: every interval, scan and refresh nearby list only
    """
    setup_logging(verbose=False)
    # Quiet root a bit so dashboard stays readable; errors still show in log panel
    logging.getLogger("medical_ble").setLevel(logging.WARNING)
    logging.getLogger("bleak").setLevel(logging.WARNING)

    profile: Optional[DeviceProfile] = None
    if connect_profile:
        try:
            profile = get_profile(connect_profile)
        except KeyError:
            profile = None
    # Hard fallback so Masimo never falls into batch scan/listen by accident
    if profile is None and (brand_id or "").lower() in STREAMING_BRAND_IDS:
        try:
            profile = get_profile("mightysat")
        except KeyError:
            profile = None

    try:
        iv = float(interval_s)
    except (TypeError, ValueError):
        iv = DEFAULT_INTERVAL_S
    # Allow 1s (real-time UI). Old code forced 15s which made "interval=1" useless.
    iv = max(MIN_INTERVAL_S, iv)
    # Streaming devices: clamp long "batch-style" intervals down so UI feels live
    streaming = _is_streaming_device(brand_id, profile)
    if streaming and mode == "monitor" and iv > 5.0:
        # User left a 15/60s BP-style interval in config — still stream live;
        # use 1s UI cadence (reconnect gap stays short too).
        iv = 1.0

    state = LiveState(
        brand_id=brand_id,
        brand_label=brand_label,
        model=model,
        address=(address or "").strip().upper(),
        interval_s=iv,
        mode=("stream" if streaming and mode == "monitor" else mode),
    )
    state.push_log(
        f"started interval={state.interval_s:.1f}s "
        f"{'STREAM' if streaming else 'batch'} "
        f"platform={'win' if is_windows() else sys.platform}"
    )
    if streaming:
        state.push_log(
            "STREAM: stay connected · paint on every SpO2/PR · no 45s listen cycle"
        )
    if mode == "monitor" and not state.address:
        state.push_log("no MAC — will pick first scan match when possible")

    paint(state)

    try:
        while True:
            if max_cycles is not None and state.cycle >= max_cycles:
                state.status = "done (max cycles)"
                paint(state)
                return 0

            state.cycle += 1
            state.next_in_s = 0
            cycle_t0 = time.monotonic()
            devices: List[Any] = []

            if mode == "scan_only":
                devices = await _cycle_scan(state, profile)
                state.status = f"scan-only idle ({len(devices)} found)"
            elif is_omron and state.address:
                # Omron transfer window is short (~seconds). Burning 8s+ on scan
                # before connect often misses FE4A even when the cuff just advertised.
                state.push_log(
                    "Omron cycle: connect FIRST (skip pre-scan). Short-press BT now."
                )
                paint(state)
                await _cycle_omron_read(
                    state,
                    output_dir=output_dir,
                    devices=None,
                    seen_in_scan=False,
                )
            elif streaming and state.address:
                # Masimo etc.: no pre-scan, no per-second reconnect — live stream
                if profile is None:
                    state.apply_failure(
                        OSError("mightysat profile missing — cannot stream")
                    )
                else:
                    await _cycle_stream(state, profile, pair=pair)
            else:

                # Batch brands / unknown MAC: optional scan then connect
                if not state.address:
                    devices = await _cycle_scan(state, profile)
                    paint(state)
                    if devices:
                        state.address = devices[0].address
                        state.push_log(f"auto-selected MAC {state.address}")
                        paint(state)
                elif not streaming:
                    # Known MAC batch devices: skip scan for faster cycles
                    state.push_log("batch cycle: connect by MAC (skip pre-scan)")
                    paint(state)

                if not state.address:
                    state.status = "need MAC — no device found"
                    state.push_log("set address or wake device for scan")
                    # one discovery attempt
                    devices = await _cycle_scan(state, profile)
                    if devices:
                        state.address = devices[0].address
                        state.push_log(f"auto-selected MAC {state.address}")
                    else:
                        state.tips = [
                            "Wake the device so scan can capture its MAC",
                            "Or enter MAC manually before starting LIVE",
                        ]
                if state.address and is_omron:
                    await _cycle_omron_read(
                        state,
                        output_dir=output_dir,
                        devices=devices,
                        seen_in_scan=_mac_in_scan(devices, state.address),
                    )
                elif state.address and brand_id == "beurer":
                    await _cycle_beurer(state)
                elif state.address and streaming and profile is not None:
                    await _cycle_stream(state, profile, pair=pair)
                elif state.address and profile is not None:
                    await _cycle_generic(state, profile, pair=pair)
                elif state.address:
                    state.status = "no profile for this brand"
                    state.push_log("cannot sync without connect profile")

            paint(state)

            # --- wait until next interval (countdown on dashboard) ---------
            # Streaming: short reconnect gap only (session already painted live)
            elapsed = time.monotonic() - cycle_t0
            if streaming:
                wait = max(state.interval_s, 1.0) + state.extra_backoff_s
            else:
                wait = max(0.0, state.interval_s - elapsed) + state.extra_backoff_s
            prev_status = state.status
            state.status = f"sleeping until next cycle ({prev_status})"
            if wait >= 2.0:
                state.push_log(f"next cycle in {wait:.0f}s")
            end = time.monotonic() + wait
            while True:
                remaining = end - time.monotonic()
                if remaining <= 0:
                    break
                state.next_in_s = remaining
                paint(state)
                # Sub-second countdown when interval is 1s
                await asyncio.sleep(min(0.25 if state.interval_s <= 2.0 else 1.0, remaining))

    except asyncio.CancelledError:
        state.status = "cancelled"
        paint(state)
        return 130
    except KeyboardInterrupt:
        state.status = "stopped by user"
        paint(state)
        print("\n  Live monitor stopped.")
        return 130


async def run_continuous_scan(
    *,
    brand_id: str,
    brand_label: str,
    model: str,
    connect_profile: Optional[str] = None,
    interval_s: float = DEFAULT_INTERVAL_S,
) -> int:
    """Scan-only companion loop (no connect)."""
    return await run_live_monitor(
        brand_id=brand_id,
        brand_label=brand_label,
        model=model,
        address="",
        connect_profile=connect_profile,
        is_omron=False,
        interval_s=interval_s,
        mode="scan_only",
    )

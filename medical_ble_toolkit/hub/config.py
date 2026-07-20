"""
Editable hub timing / policy knobs.

Load order:
  1. path argument
  2. $MEDICAL_HUB_CONFIG
  3. ./hub_config.json  (cwd)
  4. medical_ble_toolkit/hub_config.json  (package default)
  5. built-in defaults

Edit medical_ble_toolkit/hub_config.json — no code change needed for Omron interval etc.
"""

from __future__ import annotations

import json
import logging
import os
from dataclasses import asdict, dataclass, fields
from pathlib import Path
from typing import Any, Dict, Optional

log = logging.getLogger("medical_ble.hub.config")

# Package root: medical_ble_toolkit/hub/config.py → ../
_PKG_ROOT = Path(__file__).resolve().parents[1]
_DEFAULT_NAME = "hub_config.json"


@dataclass
class HubConfig:
    """All knobs for the dedicated Pi hub. Times in seconds."""

    # Omron: scheduled dump (easily editable — default 5 min)
    omron_poll_interval_s: float = 300.0
    omron_success_cooldown_s: float = 300.0
    omron_fail_cooldown_s: float = 30.0

    # NBP after successful / failed session
    windowed_success_cooldown_s: float = 15.0
    windowed_fail_cooldown_s: float = 3.0
    # NT-100B: longer success cool-down so it cannot thrash-scan and block Masimo
    nt100b_success_cooldown_s: float = 45.0
    nt100b_fail_cooldown_s: float = 4.0

    # MightySat duty-cycle (multi-device friendly):
    #   while SpO2 valid ("not -"): hold up to good_hold_s, then release radio
    #   while SpO2 invalid ("-"/None) continuously for invalid_exit_s: disconnect
    #   then prefer other brands for others_window_s before re-grabbing Mighty
    mightysat_live_max_s: float = 25.0  # hard wall-clock cap (slightly > good_hold)
    mightysat_good_hold_s: float = 20.0  # stream while values valid, max this long
    mightysat_invalid_exit_s: float = 5.0  # continuous "-" / no valid SpO2 → drop link
    mightysat_others_window_s: float = 5.0  # hunt NBP/NT/Omron before Mighty again
    mightysat_no_data_grace_s: float = 8.0  # allow setup before counting no-data as "-"
    mightysat_success_cooldown_s: float = 5.0  # min gap before re-connect after good dump
    mightysat_fail_cooldown_s: float = 3.0

    # Radio / hunt / concurrency (dedicated Pi hub)
    # max_concurrent: simultaneous GATT sessions (Pi CYW43455: aim 4)
    max_concurrent: int = 4
    concurrent_enabled: bool = True
    # Serialize connect() establishment; workers then run in parallel
    connect_gap_s: float = 0.35
    scan_chunk_s: float = 2.5
    post_disconnect_settle_s: float = 0.15
    idle_sleep_s: float = 0.35
    health_log_every_s: float = 30.0
    # BlueZ: do not StartDiscovery while a worker is still connecting/setup
    scan_quiet_after_spawn_s: float = 12.0
    # After spawning a worker, pause before next hunt loop
    post_spawn_settle_s: float = 2.0

    # Session budgets (aligned to ~1m05s field windows)
    nbp_receive_s: float = 65.0
    nt100b_receive_s: float = 65.0

    # Debug / future cloud
    print_readings: bool = True
    store_full_mightysat_stream: bool = True
    mqtt_enabled: bool = False  # reserved — cloud via MQTT later
    mqtt_topic_prefix: str = "medical/hub"

    # Product locks
    hub_only_bond: bool = True
    tier1_brands_only: bool = True

    def cooldown_ok_s(self, brand_id: str) -> float:
        b = (brand_id or "").lower()
        if b == "omron":
            return float(self.omron_success_cooldown_s)
        if b == "masimo":
            return float(self.mightysat_success_cooldown_s)
        if b in ("nipro_nt100b", "thermo"):
            return float(self.nt100b_success_cooldown_s)
        if b in ("nipro_nbp", "nipro_nmbp", "nipro_nsm1"):
            return float(self.windowed_success_cooldown_s)
        return float(self.windowed_success_cooldown_s)

    def cooldown_fail_s(self, brand_id: str) -> float:
        b = (brand_id or "").lower()
        if b == "omron":
            return float(self.omron_fail_cooldown_s)
        if b == "masimo":
            return float(self.mightysat_fail_cooldown_s)
        if b in ("nipro_nt100b", "thermo"):
            return float(self.nt100b_fail_cooldown_s)
        return float(self.windowed_fail_cooldown_s)

    def receive_s(self, brand_id: str) -> float:
        b = (brand_id or "").lower()
        if b in ("nipro_nt100b", "thermo"):
            return float(self.nt100b_receive_s)
        if b in ("nipro_nbp", "nipro_nmbp"):
            return float(self.nbp_receive_s)
        if b == "masimo":
            return float(self.mightysat_live_max_s)
        if b == "omron":
            return 90.0
        return 60.0


def default_config_path() -> Path:
    env = (os.environ.get("MEDICAL_HUB_CONFIG") or "").strip()
    if env:
        return Path(env).expanduser().resolve()
    cwd = Path.cwd() / _DEFAULT_NAME
    if cwd.is_file():
        return cwd
    return _PKG_ROOT / _DEFAULT_NAME


def _from_dict(data: Dict[str, Any]) -> HubConfig:
    known = {f.name for f in fields(HubConfig)}
    kwargs: Dict[str, Any] = {}
    for k, v in data.items():
        if k.startswith("_"):
            continue
        if k not in known:
            log.debug("hub_config: ignore unknown key %s", k)
            continue
        kwargs[k] = v
    return HubConfig(**kwargs)


def load_hub_config(path: Optional[Path | str] = None) -> HubConfig:
    """Load config from JSON; missing file → defaults (and write template if package path)."""
    p = Path(path) if path else default_config_path()
    if not p.is_file():
        cfg = HubConfig()
        # Best-effort write so operators can edit without hunting defaults
        try:
            if p.parent.exists() or p == _PKG_ROOT / _DEFAULT_NAME:
                save_hub_config(cfg, p)
                log.info("Wrote default hub config → %s", p)
        except OSError as exc:
            log.debug("Could not write default hub config: %s", exc)
        return cfg
    try:
        raw = json.loads(p.read_text(encoding="utf-8"))
        if not isinstance(raw, dict):
            log.warning("hub_config not an object — using defaults")
            return HubConfig()
        cfg = _from_dict(raw)
        log.debug("Loaded hub config from %s (omron_poll=%.0fs)", p, cfg.omron_poll_interval_s)
        return cfg
    except (OSError, json.JSONDecodeError) as exc:
        log.warning("hub_config load failed (%s) — using defaults", exc)
        return HubConfig()


def save_hub_config(cfg: HubConfig, path: Optional[Path | str] = None) -> Path:
    p = Path(path) if path else default_config_path()
    p.parent.mkdir(parents=True, exist_ok=True)
    payload = asdict(cfg)
    payload["_comment"] = (
        "Pi medical BLE hub — edit freely. "
        "omron_poll_interval_s is the main Omron freshness knob (default 300 = 5 min)."
    )
    p.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    return p

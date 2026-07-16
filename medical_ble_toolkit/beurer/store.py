"""
Persistent sync state: glucose last sequence, recent BP keys for dedup.
"""

from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Any, Dict, Optional, Set

log = logging.getLogger("medical_ble.beurer.store")

STORE_NAME = "beurer_sync_state.json"


def _path() -> Path:
    return Path.cwd() / STORE_NAME


def load_state() -> dict:
    p = _path()
    if not p.is_file():
        return {"glucose_last_seq": {}, "bp_seen_keys": {}}
    try:
        return json.loads(p.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        log.warning("Could not read %s: %s", p, exc)
        return {"glucose_last_seq": {}, "bp_seen_keys": {}}


def save_state(data: dict) -> None:
    p = _path()
    try:
        p.write_text(json.dumps(data, indent=2), encoding="utf-8")
    except OSError as exc:
        log.warning("Could not write %s: %s", p, exc)


def glucose_last_seq(address: str, model_id: str = "") -> Optional[int]:
    st = load_state()
    key = _gkey(address, model_id)
    val = (st.get("glucose_last_seq") or {}).get(key)
    return int(val) if val is not None else None


def set_glucose_last_seq(address: str, seq: int, model_id: str = "") -> None:
    st = load_state()
    g = st.setdefault("glucose_last_seq", {})
    key = _gkey(address, model_id)
    prev = g.get(key)
    if prev is None or int(seq) > int(prev):
        g[key] = int(seq)
        save_state(st)
        log.info("[STORE] glucose last_seq %s → %s", key, seq)


def _gkey(address: str, model_id: str) -> str:
    return f"{(address or '').upper()}|{(model_id or '').upper()}"


def bp_seen_keyset(address: str, model_id: str = "", limit: int = 500) -> Set[str]:
    st = load_state()
    key = _gkey(address, model_id)
    lst = (st.get("bp_seen_keys") or {}).get(key) or []
    return set(lst[-limit:])


def remember_bp_keys(address: str, keys: list[str], model_id: str = "", limit: int = 500) -> None:
    if not keys:
        return
    st = load_state()
    b = st.setdefault("bp_seen_keys", {})
    k = _gkey(address, model_id)
    existing = list(b.get(k) or [])
    for x in keys:
        if x not in existing:
            existing.append(x)
    b[k] = existing[-limit:]
    save_state(st)

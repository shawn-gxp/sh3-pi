"""
Generic pairing workflow — will orchestrate the parts of "pair a device" that
are identical across every brand (persist to DB, handle repair flag), leaving
the brand-specific handshake to DevicePlugin.pair().

NOTE: only Omron is migrated as of Phase 1. This module is intentionally thin
until Nipro/Beurer/FORA are migrated and ble_jobs.py's job_pair() is updated
to call it for every brand instead of just Omron. Do not expand this file's
scope in Phase 1 — it exists now so the import path is stable for later phases.
"""
from __future__ import annotations

from .device_plugin import PairResult
from .registry import get_plugin


async def pair_device(brand_id: str, mac: str, model: str, *, repair: bool = False) -> PairResult:
    import medical_ble_toolkit.brands  # noqa: F401
    plugin = get_plugin(brand_id)
    return await plugin.pair(mac, model, force_rebind=repair)

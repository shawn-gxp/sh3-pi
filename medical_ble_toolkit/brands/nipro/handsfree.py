"""
Hands-free wait loop modeled on げんきノート MasterDetailRootViewModel:

  ReceiveWait(exact names, long timeout)
  → on AD: CheckPairing(id) → ReceiveStart(60s)
  → when quiet/complete → back to wait
  → restart on scan timeout

Windows bleak cannot hold a single 8h scan easily; we loop short scan chunks.
"""

from __future__ import annotations

import asyncio
import logging
import time
from typing import Any, Callable, List, Optional, Set

from bleak.exc import BleakError

from medical_ble_toolkit.ble_client import MedicalBleClient, _brief, scan_devices, setup_logging
from medical_ble_toolkit.common.winrt_errors import is_windows, os_pair_supported
from medical_ble_toolkit.profiles import get_profile
from .registry import (
    check_pairing,
    find_by_name,
    handsfree_name_list,
    list_meters,
)

log = logging.getLogger("medical_ble.nipro.handsfree")

# Companion constants
DEFAULT_SCAN_TOTAL_S = 28_800.0  # 8 hours
DEFAULT_RECEIVE_S = 60.0
DEFAULT_SCAN_CHUNK_S = 6.0  # faster reaction after measure (Omron-like)
HEALTH_CHECK_S = 3.0


async def handsfree_wait(
    *,
    duration: float = DEFAULT_SCAN_TOTAL_S,
    receive_timeout: float = DEFAULT_RECEIVE_S,
    scan_chunk: float = DEFAULT_SCAN_CHUNK_S,
    categories: Optional[List[str]] = None,
    pair_on_connect: Optional[bool] = None,
    on_reading: Optional[Callable[[Any], None]] = None,
    post_measure: bool = True,
) -> List[Any]:
    """
    Run companion-like hands-free loop until *duration* seconds elapse.

    post_measure=True (default): Omron-like — on AD, connect with find window
    and bulk dump (NT-100B full storage, NBP long indicate wait).

    Returns all readings collected across sessions.
    """
    from . import post_measure as pm

    meters = list_meters()
    names = handsfree_name_list(categories)
    if not names:
        log.error(
            "[NIPRO] hands-free: no paired meters. "
            "Register first:  python -m medical_ble_toolkit nipro pair -p nipro_nbp -a <MAC>"
        )
        return []

    log.info(
        "[NIPRO] hands-free START  post_measure=%s meters=%d names=%s  "
        "total=%.0fs receive=%.0fs chunk=%.0fs",
        post_measure,
        len(meters),
        names,
        duration,
        receive_timeout,
        scan_chunk,
    )
    for m in meters:
        log.info(
            "  • %s  category=%s profile=%s id=%s",
            m.name,
            m.category,
            m.profile_id,
            m.id_nodash,
        )

    all_readings: List[Any] = []
    busy: Set[str] = set()
    deadline = time.monotonic() + max(10.0, duration)
    sessions = 0

    while time.monotonic() < deadline:
        remaining = deadline - time.monotonic()
        if remaining <= 0:
            break
        chunk = min(scan_chunk, remaining)

        try:
            devices = await scan_devices(profile=None, timeout=chunk)
        except (BleakError, OSError, asyncio.TimeoutError) as exc:
            log.warning("[NIPRO] hands-free scan error: %s — retry in %.0fs", exc, HEALTH_CHECK_S)
            await asyncio.sleep(HEALTH_CHECK_S)
            continue

        # Filter: exact name + CheckPairing (companion ReceiveWaitHandler)
        candidates = []
        for d in devices:
            name = (getattr(d, "name", None) or "").strip()
            addr = getattr(d, "address", "") or ""
            if not name or name not in names:
                continue
            if name in busy:
                log.debug("[NIPRO] skip busy %s", name)
                continue
            if not check_pairing(addr):
                log.debug(
                    "[NIPRO] ignore unpaired ad name=%s addr=%s (not in registry)",
                    name,
                    addr,
                )
                continue
            meter = find_by_name(name)
            if meter is None:
                continue
            # Companion home ignores NBCM in MasterDetailRoot
            if meter.category == "bc":
                continue
            candidates.append((d, meter))

        if not candidates:
            # idle chunk complete — continue scanning (companion long wait)
            log.debug(
                "[NIPRO] hands-free idle (%.0fs left) — no paired ads this chunk",
                deadline - time.monotonic(),
            )
            continue

        for device, meter in candidates:
            if time.monotonic() >= deadline:
                break
            name = meter.name
            addr = device.address
            if name in busy:
                continue
            busy.add(name)
            sessions += 1
            log.info(
                "[NIPRO] hands-free SESSION #%d  name=%s profile=%s addr=%s",
                sessions,
                name,
                meter.profile_id,
                addr,
            )
            try:
                profile = get_profile(meter.profile_id)
            except KeyError:
                log.error("[NIPRO] unknown profile %s for %s", meter.profile_id, name)
                busy.discard(name)
                continue

            use_pair = pair_on_connect
            if use_pair is None:
                use_pair = os_pair_supported() and meter.profile_id in (
                    "nipro_nmbp",
                    "and_ua651",
                    "nipro_nbp",
                )

            find_to = (
                pm.find_window_for(meter.profile_id) if post_measure else 0.0
            )
            sess_dur = max(
                receive_timeout, pm.receive_s_for(meter.profile_id)
            )
            client = MedicalBleClient(
                profile=profile,
                address=addr,
                pair=bool(use_pair),
                on_reading=on_reading,
                find_timeout=find_to,
                name_hint=name,
                connect_retries=5 if post_measure else 2,
            )
            try:
                log.info(
                    "[POST-MEASURE] session find=%.0fs receive=%.0fs profile=%s",
                    find_to,
                    sess_dur,
                    meter.profile_id,
                )
                readings = await client.run(
                    duration=sess_dur,
                    connect_timeout=15.0,
                    quiet_timeout=None,  # post_measure defaults inside run()
                )
                for r in readings:
                    all_readings.append(r)
                    log.info("[NIPRO] reading: %s", _brief(r))
                log.info(
                    "[NIPRO] session done name=%s readings=%d",
                    name,
                    len(readings),
                )
            except Exception as exc:  # noqa: BLE001
                log.error(
                    "[NIPRO] session failed name=%s: %s: %s",
                    name,
                    type(exc).__name__,
                    exc,
                )
                # Companion encryption hint
                msg = str(exc).lower()
                if "encrypt" in msg or "auth" in msg or "bond" in msg:
                    log.error(
                        "データ受信できませんでした。エラーが続く場合は、"
                        "OSのBluetooth設定から測定器の登録を一度解除して、"
                        "測定器登録をやり直してください。"
                    )
            finally:
                busy.discard(name)
                await asyncio.sleep(0.1)  # companion post-disconnect settle

        # brief pause before next scan wave (health-check style)
        await asyncio.sleep(0.5)

    log.info(
        "[NIPRO] hands-free END  sessions=%d total_readings=%d",
        sessions,
        len(all_readings),
    )
    return all_readings

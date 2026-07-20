"""
Concurrent BLE connection slots for the dedicated Linux hub.

BlueZ-safe rules (from Bleak maintainers + field practice):
  - Serialize connect() (one at a time) with a short gap
  - Do not run an active discover() during connect
  - After connect, multiple BleakClients may stay up and notify in parallel
  - Cap concurrent links to controller budget (default 4 on Pi CYW43455)

This module tracks slots only — actual GATT work is in run_session callbacks.
"""

from __future__ import annotations

import asyncio
import logging
import time
from dataclasses import dataclass, field
from typing import Dict, List, Optional, Set

log = logging.getLogger("medical_ble.hub.conn_mgr")


@dataclass
class SlotInfo:
    mac: str
    brand: str
    started_mono: float
    reason: str = ""
    task_name: str = ""


class ConnectionManager:
    """
    Slot pool + connect mutex for concurrent medical BLE sessions.
    """

    def __init__(
        self,
        *,
        max_concurrent: int = 4,
        connect_gap_s: float = 0.35,
    ):
        self.max_concurrent = max(1, int(max_concurrent))
        self.connect_gap_s = max(0.0, float(connect_gap_s))
        self._active: Dict[str, SlotInfo] = {}
        self._connect_mutex = asyncio.Lock()
        self._last_connect_mono = 0.0
        self._guard = asyncio.Lock()  # protects _active

    @property
    def active_count(self) -> int:
        return len(self._active)

    @property
    def active_macs(self) -> Set[str]:
        return set(self._active.keys())

    def snapshot(self) -> List[dict]:
        return [
            {
                "mac": s.mac,
                "brand": s.brand,
                "reason": s.reason,
                "age_s": round(time.monotonic() - s.started_mono, 1),
            }
            for s in self._active.values()
        ]

    def has_slot(self) -> bool:
        return len(self._active) < self.max_concurrent

    def is_busy(self, mac: str) -> bool:
        return mac.strip().upper() in self._active

    def any_younger_than(self, window_s: float) -> bool:
        """
        True if any slot started less than window_s ago.

        Used to pause hub discovery while a worker is still in connect /
        GATT setup (BlueZ rejects parallel StartDiscovery → InProgress).
        """
        if window_s <= 0 or not self._active:
            return False
        now = time.monotonic()
        for s in self._active.values():
            if (now - s.started_mono) < window_s:
                return True
        return False

    async def try_acquire(self, mac: str, brand: str, reason: str = "") -> bool:
        """Reserve a slot for MAC. False if busy or pool full."""
        mac_u = mac.strip().upper()
        async with self._guard:
            if mac_u in self._active:
                return False
            if len(self._active) >= self.max_concurrent:
                return False
            self._active[mac_u] = SlotInfo(
                mac=mac_u,
                brand=brand,
                started_mono=time.monotonic(),
                reason=reason,
            )
            log.info(
                "[CONN] slot acquired mac=%s brand=%s active=%d/%d reason=%s",
                mac_u,
                brand,
                len(self._active),
                self.max_concurrent,
                reason,
            )
            return True

    async def release(self, mac: str) -> None:
        mac_u = mac.strip().upper()
        async with self._guard:
            gone = self._active.pop(mac_u, None)
            if gone:
                age = time.monotonic() - gone.started_mono
                log.info(
                    "[CONN] slot released mac=%s brand=%s held=%.1fs active=%d/%d",
                    mac_u,
                    gone.brand,
                    age,
                    len(self._active),
                    self.max_concurrent,
                )

    async def connect_gate(self):
        """
        Context manager: exclusive connect window + inter-connect gap.
        Usage: async with mgr.connect_gate(): await client.connect()
        """
        return _ConnectGate(self)


class _ConnectGate:
    def __init__(self, mgr: ConnectionManager):
        self._mgr = mgr

    async def __aenter__(self):
        await self._mgr._connect_mutex.acquire()
        gap = self._mgr.connect_gap_s
        if gap > 0 and self._mgr._last_connect_mono > 0:
            elapsed = time.monotonic() - self._mgr._last_connect_mono
            if elapsed < gap:
                await asyncio.sleep(gap - elapsed)
        return self

    async def __aexit__(self, exc_type, exc, tb):
        self._mgr._last_connect_mono = time.monotonic()
        self._mgr._connect_mutex.release()
        return False

"""
DevicePlugin — the contract every brand adapter implements.

This is the seam between the orchestrator (medical_ble_web/ble_jobs.py) and
brand-specific BLE logic (medical_ble_toolkit/brands/<brand>/). Adding a new
brand means implementing this interface and registering an instance in
brands/__init__.py — no changes to the orchestrator's dispatch logic required.

Do NOT put BLE protocol logic here. Implementations must be thin wrappers
that delegate to the brand's existing, field-proven modules.
"""
from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from enum import Enum
from typing import Any, Optional


class DeviceClass(str, Enum):
    """Mirrors medical_ble_toolkit.hub.policy's STREAM/WINDOWED/ALWAYS semantics."""
    STREAM = "stream"      # e.g. MightySat — live while measuring
    WINDOWED = "windowed"  # e.g. NBP / NT-100B — advertise then short dump
    ALWAYS = "always"      # e.g. Omron — scheduled / opportunistic history


@dataclass
class PairResult:
    ok: bool
    mac: str
    model: str
    detail: dict[str, Any] = field(default_factory=dict)


@dataclass
class SessionResult:
    ok: bool
    readings: list[Any] = field(default_factory=list)
    detail: dict[str, Any] = field(default_factory=dict)


class DevicePlugin(ABC):
    """One instance per brand. See brands/omron/plugin.py for the reference implementation."""

    brand_id: str
    device_class: DeviceClass
    priority_rank: int = 50

    @abstractmethod
    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        """One-time manual bonding/unlock. Called from the web UI pairing flow only."""
        ...

    @abstractmethod
    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        """Connect, read, and return parsed clinical readings for an already-paired device."""
        ...

    async def teardown(self, mac: str) -> None:
        """Optional brand-specific disconnect/power-off step. Default: no-op."""
        return None

    def matches_advertisement(self, name: str = "", mfg_ids: Optional[list[int]] = None) -> bool:
        """Optional: opportunistic advertisement match for hub discovery. Default: False (MAC-strict only)."""
        return False

    def listen_s(self, slot_s: float) -> float:
        """How many seconds to listen during a hub cycle slot. Override per brand."""
        return min(float(slot_s), 60.0)

    def quiet_timeout_s(self, profile_id: str) -> float:
        """Quiet-end timeout after last indication. 0.0 = stream/no quiet end."""
        return 0.0


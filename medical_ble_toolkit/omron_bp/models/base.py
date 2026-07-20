"""
Device profile contract — every Omron model (and later other brands) implements this.

WHAT BELONGS HERE
  - BLE UUIDs for this hardware family
  - Pairing mode (OS bond vs app unlock key)
  - EEPROM map (where records live, how many, block size)
  - Which parser turns raw bytes into vital signs

WHAT DOES NOT BELONG HERE
  - bleak / Windows BLE calls  → ble/
  - CLI menus                  → cli.py
  - CSV writing                → export/
"""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum
from typing import Any, Callable, Optional, Sequence


class PairingMode(str, Enum):
    """How the host becomes trusted by the cuff."""

    # Modern FE4A stack (e.g. HEM-7143T1): Windows/OS Just Works bond only
    OS_BONDING = "os_bonding"
    # Classic stack: program 16-byte unlock key into device EEPROM once
    UNLOCK_KEY = "unlock_key"
    # Device needs neither (rare / already open)
    NONE = "none"


class UnlockMode(str, Enum):
    """Post-connect memory unlock strategy (from hass-omron)."""

    NONE = "none"
    CLASSIC_KEY = "classic_key"
    # Stateless 0x11/0x91 nonce handshake (modern; not yet fully implemented in transport)
    TOKEN_KEY = "token_key"
    # Encrypted session (not implemented in this toolkit yet)
    SECURE_SESSION = "secure_session"


class Endianness(str, Enum):
    LITTLE = "little"
    BIG = "big"


class StackKind(str, Enum):
    """Which BLE parent service / channel layout to use."""

    CLASSIC = "classic"  # multi-channel legacy parent
    MODERN = "modern"  # FE4A single-channel


# Default legacy multi-channel Omron UUIDs (pre-FE4A family)
LEGACY_PARENT_SERVICE_UUID = "ecbe3980-c9a2-11e1-b1bd-0002a5d5c51b"
LEGACY_RX_UUIDS = [
    "49123040-aee8-11e1-a74d-0002a5d5c51b",
    "4d0bf320-aee8-11e1-a0d9-0002a5d5c51b",
    "5128ce60-aee8-11e1-b84b-0002a5d5c51b",
    "560f1420-aee8-11e1-8184-0002a5d5c51b",
]
LEGACY_TX_UUIDS = [
    "db5b55e0-aee7-11e1-965e-0002a5d5c51b",
    "e0b8a060-aee7-11e1-92f4-0002a5d5c51b",
    "0ae12b00-aee8-11e1-a192-0002a5d5c51b",
    "10e1ba60-aee8-11e1-89e5-0002a5d5c51b",
]
LEGACY_UNLOCK_UUID = "b305b680-aee7-11e1-a730-0002a5d5c51b"

# Modern single-channel FE4A stack
MODERN_PARENT_SERVICE_UUID = "0000fe4a-0000-1000-8000-00805f9b34fb"
MODERN_RX_UUIDS = ["49123040-aee8-11e1-a74d-0002a5d5c51b"]
MODERN_TX_UUIDS = ["db5b55e0-aee7-11e1-965e-0002a5d5c51b"]

# Default app-layer pairing key used by classic Omron tools (16 bytes)
DEFAULT_UNLOCK_KEY = bytes.fromhex("deadbeaf12341234deadbeaf12341234")


# Parser signature: raw record bytes + endianness → dict of vitals
RecordParserFn = Callable[[bytes, str], dict[str, Any]]


@dataclass(frozen=True)
class DeviceProfile:
    """
    Immutable description of one cuff family.

    `model_id` is the canonical key in the registry (e.g. "HEM-7143T1").
    `aliases` are sold-as / regional names that map to the same layout.
    """

    model_id: str
    display_name: str
    pairing_mode: PairingMode
    endianness: Endianness

    # BLE identity
    parent_service_uuid: str
    rx_channel_uuids: Sequence[str]
    tx_channel_uuids: Sequence[str]
    unlock_uuid: str = LEGACY_UNLOCK_UUID
    unlock_mode: UnlockMode = UnlockMode.NONE
    stack: StackKind = StackKind.CLASSIC

    # EEPROM record map
    user_start_addresses: Sequence[int] = field(default_factory=list)
    per_user_records_count: Sequence[int] = field(default_factory=list)
    record_byte_size: int = 0x0E
    transmission_block_size: int = 0x38

    # Settings region (None = feature not supported / not mapped yet)
    settings_read_address: Optional[int] = None
    settings_write_address: Optional[int] = None
    settings_unread_records_bytes: Optional[tuple[int, int]] = None
    settings_time_sync_bytes: Optional[tuple[int, int]] = None

    # How to decode one record
    parse_record: Optional[RecordParserFn] = None

    # Other marketed names that share this profile
    aliases: Sequence[str] = field(default_factory=tuple)

    # Free-text notes for operators / future maintainers
    notes: str = ""
    # Provenance: "hass-omron", "omblepy", "both", "lab"
    source: str = ""

    @property
    def requires_unlock(self) -> bool:
        """True when a pre-start unlock step is required (classic key or 0x11 token)."""
        return self.unlock_mode in (UnlockMode.CLASSIC_KEY, UnlockMode.TOKEN_KEY)

    @property
    def requires_os_bonding(self) -> bool:
        return self.pairing_mode == PairingMode.OS_BONDING

    @property
    def user_count(self) -> int:
        return len(self.user_start_addresses)

    def validate(self) -> None:
        if self.user_count != len(self.per_user_records_count):
            raise ValueError(
                f"{self.model_id}: user_start_addresses and per_user_records_count length mismatch"
            )
        if self.parse_record is None:
            raise ValueError(f"{self.model_id}: parse_record is required for readout")


def classic_profile(**kwargs) -> dict:
    """Default kwargs for classic multi-channel unlock-key devices."""
    return {
        "pairing_mode": PairingMode.UNLOCK_KEY,
        "unlock_mode": UnlockMode.CLASSIC_KEY,
        "stack": StackKind.CLASSIC,
        "parent_service_uuid": LEGACY_PARENT_SERVICE_UUID,
        "rx_channel_uuids": list(LEGACY_RX_UUIDS),
        "tx_channel_uuids": list(LEGACY_TX_UUIDS),
        "unlock_uuid": LEGACY_UNLOCK_UUID,
        **kwargs,
    }


def modern_profile(**kwargs) -> dict:
    """Default kwargs for modern FE4A OS-bonding devices."""
    return {
        "pairing_mode": PairingMode.OS_BONDING,
        "unlock_mode": UnlockMode.NONE,
        "stack": StackKind.MODERN,
        "parent_service_uuid": MODERN_PARENT_SERVICE_UUID,
        "rx_channel_uuids": list(MODERN_RX_UUIDS),
        "tx_channel_uuids": list(MODERN_TX_UUIDS),
        **kwargs,
    }

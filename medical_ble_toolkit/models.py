"""
Domain data models — strongly typed schema for parsed vitals.

PORT TO KOTLIN
--------------
Each @dataclass maps 1:1 to a Kotlin `data class`.
Keep field names stable across languages so serializers / DB columns match.

  BloodPressureReading  → data class BloodPressureReading(...)
  PulseOximeterData     → data class PulseOximeterData(...)
  ThermometerReading    → data class ThermometerReading(...)
  MultiParameterReading → data class MultiParameterReading(...)  # FORA scaffold

NO BLE / I/O imports here. Pure data only.
"""

from __future__ import annotations

from dataclasses import dataclass, field, asdict
from datetime import datetime
from enum import Enum, IntEnum
from typing import Any, Dict, List, Optional, Sequence


# ---------------------------------------------------------------------------
# Shared enums
# ---------------------------------------------------------------------------

class PressureUnit(str, Enum):
    MMHG = "mmHg"
    KPA = "kPa"


class TemperatureUnit(str, Enum):
    CELSIUS = "C"
    FAHRENHEIT = "F"


class TemperatureSite(IntEnum):
    """Thermometer object site (NT-100B Type1+Type2 encoding)."""
    EAR = 0
    FOREHEAD = 1
    RECTAL = 2
    ARMPIT = 3
    OBJECT_SURFACE = 4
    ROOM = 5
    CHILDREN = 6
    UNKNOWN = 255


class DeviceBrand(str, Enum):
    BEURER = "beurer"
    AND = "and"          # A&D / Nipro UA-651BLE
    OMRON = "omron"      # Omron HEM-* (proprietary EEPROM; see omron_bp)
    MASIMO = "masimo"    # MightySat SpO2
    THERMO = "thermo"    # NT-100B non-contact thermometer
    FORA = "fora"        # FORA 6 Connect (protocol TBD)
    UNKNOWN = "unknown"

@dataclass(frozen=True)
class DeviceProfile:
    id: str
    brand: str
    model: str
    parser_key: str
    # Name substrings for scan filter (case-insensitive)
    name_hints: Sequence[str] = ()
    # Manufacturer company IDs (Bluetooth SIG assigned numbers)
    company_ids: Sequence[int] = ()
    # Primary service UUID to subscribe after connect
    service_uuid: Optional[str] = None
    # Characteristics to enable notify/indicate (full or short form)
    notify_uuids: Sequence[str] = ()
    # Optional write char for command-driven devices
    write_uuid: Optional[str] = None
    notes: str = ""
    # If True, subscribe to ALL notify/indicate chars (RE mode)
    subscribe_all_notifiable: bool = False


# ---------------------------------------------------------------------------
# Exceptions
# ---------------------------------------------------------------------------

class ParseError(Exception):
    """Raised when a payload cannot be decoded.

    Always include:
      - payload_hex: zero-padded hex for forensic correlation
      - reason: human-readable cause (length, flags, CRC, …)
    """

    def __init__(self, reason: str, payload: bytes | bytearray | None = None):
        self.reason = reason
        self.payload_hex = (
            " ".join(f"0x{b:02X}" for b in payload) if payload is not None else None
        )
        msg = reason
        if self.payload_hex:
            msg = f"{reason} | payload=[{self.payload_hex}]"
        super().__init__(msg)


# ---------------------------------------------------------------------------
# Raw capture envelope (for RE / logging before parse)
# ---------------------------------------------------------------------------

@dataclass
class RawPayload:
    """Unparsed notification capture with forensic metadata."""
    received_at: datetime
    characteristic_uuid: str
    data: bytes
    device_address: str = ""
    device_name: str = ""

    @property
    def hex_dump(self) -> str:
        """Zero-padded hex: '0x0A 0x1B 0xFF' — use this in terminal RE logs."""
        return " ".join(f"0x{b:02X}" for b in self.data)

    @property
    def length(self) -> int:
        return len(self.data)


# ---------------------------------------------------------------------------
# Clinical vitals
# ---------------------------------------------------------------------------

@dataclass
class BloodPressureReading:
    """
    Bluetooth SIG Blood Pressure Measurement (characteristic 0x2A35).

    Used by:
      - Beurer BM54  (datasheets/beurer)
      - A&D UA-651BLE (datasheets/nipro/SH3 UA-651BLE SDK)

    Both devices share the same BLP binary layout; brand-specific extras
    (e.g. A&D custom service commands) live outside this model.
    """
    systolic: float
    diastolic: float
    mean_arterial_pressure: Optional[float]
    pulse_rate: Optional[float]
    unit: PressureUnit
    measured_at: Optional[datetime]
    user_id: Optional[int]
    # Measurement status flags (BLP / Beurer app mapper)
    body_movement: bool = False
    cuff_too_loose: bool = False
    irregular_pulse: bool = False      # IHB — status bit2 (app extracts commonly)
    pulse_rate_range: int = 0          # 0=in range, 1=high, 2=low, 3=reserved
    improper_position: bool = False
    # Extended status (Beurer HealthManager Pro extracts device-dependently)
    hsd: bool = False                  # status & 0x40 (common HSD path)
    afib: Optional[bool] = None        # model-specific; None = not decoded
    raw_status: int = 0                # full uint16 LE measurement status
    # Provenance
    brand: DeviceBrand = DeviceBrand.UNKNOWN
    model: str = ""
    raw_flags: int = 0
    raw_hex: str = ""

    @property
    def user_label(self) -> Optional[str]:
        """Beurer rev03 / app: 0=user1, 1=user2."""
        if self.user_id is None:
            return None
        if self.user_id == 0:
            return "user1"
        if self.user_id == 1:
            return "user2"
        return f"user{self.user_id}"

    def to_dict(self) -> Dict[str, Any]:
        d = asdict(self)
        d["unit"] = self.unit.value
        d["brand"] = self.brand.value
        d["user_label"] = self.user_label
        d["measured_at"] = self.measured_at.isoformat() if self.measured_at else None
        return d


@dataclass
class PulseOximeterData:
    """
    Masimo MightySat streaming parameters (proprietary framed protocol).

    Consumer variant: SpO2, PR, PI
    RX variant:       SpO2, PR, PI, PVi, RRp
    """
    spo2: Optional[int] = None                 # %
    pulse_rate: Optional[int] = None           # bpm
    perfusion_index: Optional[float] = None    # PI (stored x100 on wire)
    pvi: Optional[int] = None                  # pleth variability index
    rrp: Optional[int] = None                  # respiration rate
    # Quality / exception flags
    sensor_off: bool = False
    pulse_search: bool = False
    interference: bool = False
    low_perfusion: bool = False
    low_confidence: bool = False
    invalid: bool = False
    # Provenance
    brand: DeviceBrand = DeviceBrand.MASIMO
    model: str = "MightySat"
    measured_at: Optional[datetime] = None
    raw_hex: str = ""
    message_id: Optional[int] = None

    def to_dict(self) -> Dict[str, Any]:
        d = asdict(self)
        d["brand"] = self.brand.value
        d["measured_at"] = self.measured_at.isoformat() if self.measured_at else None
        return d


@dataclass
class WaveformSample:
    """MightySat pleth / SIQ sample pair (31.25 Hz stream)."""
    pleth: int
    siq: int
    siq_invalid: bool = False


@dataclass
class WaveformPacket:
    ordinal: int
    samples: List[WaveformSample] = field(default_factory=list)
    raw_hex: str = ""


@dataclass
class ThermometerReading:
    """
    Non-contact thermometer (NT-100B proprietary frame protocol).

    Temperature values are stored in 0.1 °C (or 0.1 °F) integer units on the wire.
    """
    object_temperature: float
    ambient_temperature: Optional[float]
    unit: TemperatureUnit
    site: TemperatureSite
    measured_at: Optional[datetime]
    index: Optional[int] = None
    brand: DeviceBrand = DeviceBrand.THERMO
    model: str = "NT-100B"
    raw_hex: str = ""

    def to_dict(self) -> Dict[str, Any]:
        d = asdict(self)
        d["unit"] = self.unit.value
        d["site"] = int(self.site)
        d["brand"] = self.brand.value
        d["measured_at"] = self.measured_at.isoformat() if self.measured_at else None
        return d


@dataclass
class MultiParameterReading:
    """
    FORA multi-parameter / glucose reading.

    Protocol: TaiDoc bus over GATT 1523/1524 (iFORA Smart). See
    datasheets/FORA/FORA_FIRST_PARTY_PROTOCOL.md. Multiparam fields beyond
    BG may remain None until HCI validates packing per project code.
    """
    blood_glucose_mg_dl: Optional[float] = None
    hematocrit_pct: Optional[float] = None
    hemoglobin_g_dl: Optional[float] = None
    beta_ketone_mmol_l: Optional[float] = None
    uric_acid_mg_dl: Optional[float] = None
    total_cholesterol_mg_dl: Optional[float] = None
    meal_tag: Optional[str] = None          # AC / PC / AutoQC
    measured_at: Optional[datetime] = None
    brand: DeviceBrand = DeviceBrand.FORA
    model: str = "FORA 6 Connect"
    raw_hex: str = ""
    notes: str = "Protocol not documented — populate via packet analysis"

    def to_dict(self) -> Dict[str, Any]:
        d = asdict(self)
        d["brand"] = self.brand.value
        d["measured_at"] = self.measured_at.isoformat() if self.measured_at else None
        return d

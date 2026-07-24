"""
Facade module: pure byte-array → dataclass conversion.

This is the portability boundary required by the architecture:

  ble_client.py  →  passes bytearray here  →  returns models.* dataclasses

Do NOT import bleak or any I/O library in this file (or parsers/*).

Kotlin mapping
--------------
  object / class ParserFacade {
      fun parse(profile: String, payload: ByteArray): Any
  }
  // or inject VitalParser implementations via DI
"""

from __future__ import annotations

from typing import Any, Callable, Dict, Optional

from .models import DeviceBrand, ParseError
from .parsers.blood_pressure import (
    BlpBloodPressureParser,
    and_ua651ble_parser,
    beurer_bm54_parser,
    parse_blood_pressure_measurement,
)
from .parsers.mightysat import MightySatParser
from .parsers.thermometer import ThermometerParser
from .parsers.fora import ForaParser
from .parsers.omron import OmronRecordParser, parse_omron_record
from .parsers.glucose import GlucoseParser
from .parsers.beurer_ft import BeurerFtParser
from .parsers.beurer_po60 import BeurerPo60Parser
from .parsers.beurer_scale import BeurerScaleParser
from .parsers.beurer_tracker import BeurerTrackerParser
from .parsers.beurer_ecg import BeurerEcgParser
from .parsers.htp import HtpTemperatureParser
from .parsers.nipro_cf import NiproCfParser
from .parsers.nipro_nc1 import NiproNc1Parser
from .parsers.nipro_nt100b import Nt100bCompanionParser
from .parsers.base import parse_dispatch


def _nipro_bp_parser():
    return BlpBloodPressureParser(brand=DeviceBrand.AND, model="Nipro BP")


def _nipro_htp_parser():
    return HtpTemperatureParser()


def _nipro_nt100b_parser():
    return Nt100bCompanionParser()


# Profile name → parser factory
_PROFILE_PARSERS: Dict[str, Callable[[], Any]] = {
    "beurer_bm54": beurer_bm54_parser,
    "beurer_bp": beurer_bm54_parser,
    "beurerbp": beurer_bm54_parser,
    "bm54": beurer_bm54_parser,
    "beurer_glucose": lambda: GlucoseParser(),
    "beurerglucose": lambda: GlucoseParser(),
    "beurer_thermo": lambda: BeurerFtParser(),
    "beurerthermo": lambda: BeurerFtParser(),
    "beurer_po60": lambda: BeurerPo60Parser(),
    "beurerpo60": lambda: BeurerPo60Parser(),
    "beurer_scale": lambda: BeurerScaleParser(),
    "beurerscale": lambda: BeurerScaleParser(),
    "beurer_as87": lambda: BeurerTrackerParser("AS87"),
    "beureras87": lambda: BeurerTrackerParser("AS87"),
    "beurer_as98": lambda: BeurerTrackerParser("AS98"),
    "beureras98": lambda: BeurerTrackerParser("AS98"),
    "beurer_as99": lambda: BeurerTrackerParser("AS99"),
    "beureras99": lambda: BeurerTrackerParser("AS99"),
    "beurer_tracker": lambda: BeurerTrackerParser(),
    "beurertracker": lambda: BeurerTrackerParser(),
    "beurer_tracker_legacy": lambda: BeurerTrackerParser("legacy"),
    "beurertrackerlegacy": lambda: BeurerTrackerParser("legacy"),
    "beurer_ecg": lambda: BeurerEcgParser(),
    "beurerecg": lambda: BeurerEcgParser(),
    "beurer_hydration": lambda: BeurerTrackerParser("DM20"),
    "beurerhydration": lambda: BeurerTrackerParser("DM20"),
    "and_ua651": and_ua651ble_parser,
    "ua651": and_ua651ble_parser,
    "ua-651ble": and_ua651ble_parser,
    "nipro_nbp": _nipro_bp_parser,
    "nipronbp": _nipro_bp_parser,
    "nipro_nmbp": _nipro_bp_parser,
    "nipronmbp": _nipro_bp_parser,
    "nipro_nsm1": _nipro_htp_parser,
    "nipronsm1": _nipro_htp_parser,
    "nipro_nt100b": _nipro_nt100b_parser,
    "nipront100b": _nipro_nt100b_parser,
    "nipro_cf": lambda: NiproCfParser(),
    "niprocf": lambda: NiproCfParser(),
    "nipro_nc1": lambda: NiproNc1Parser(),
    "nipronc1": lambda: NiproNc1Parser(),
    "nc1": lambda: NiproNc1Parser(),
    "nc1ble": lambda: NiproNc1Parser(),
    "cocoron_nc1": lambda: NiproNc1Parser(),
    "mightysat": lambda: MightySatParser(),
    "masimo": lambda: MightySatParser(),
    "thermometer": lambda: ThermometerParser(),
    "nt100b": _nipro_nt100b_parser,
    "fora": lambda: ForaParser(),
    "fora6": lambda: ForaParser(),
    # Omron EEPROM slot parsers (model-specific layout via omron_bp catalog)
    "omron": lambda: OmronRecordParser("HEM-7143T1"),
    "hem-7143t1": lambda: OmronRecordParser("HEM-7143T1"),
    "hem7143t1": lambda: OmronRecordParser("HEM-7143T1"),
    # Shared BLP without brand stamp
    "blp": lambda: BlpBloodPressureParser(),
    "blood_pressure": lambda: BlpBloodPressureParser(),
}


def get_parser(profile: str):
    """Return a configured parser instance for *profile* (case-insensitive)."""
    key = profile.strip().lower().replace(" ", "_").replace("-", "")
    # Normalize lookup keys that used hyphens
    factory = _PROFILE_PARSERS.get(key)
    if factory is None:
        # Also try original lower with underscores
        key2 = profile.strip().lower().replace(" ", "_")
        factory = _PROFILE_PARSERS.get(key2)
    if factory is not None:
        return factory()

    # Any Omron catalog model (HEM-7143T1, HEM-7322T, …) → EEPROM slot parser
    upper = profile.strip().upper()
    if upper.startswith("HEM") or upper.startswith("OMRON"):
        try:
            return OmronRecordParser(profile.strip())
        except Exception as exc:
            raise KeyError(
                f"Unknown Omron model '{profile}': {exc}"
            ) from exc

    known = ", ".join(sorted(_PROFILE_PARSERS))
    raise KeyError(f"Unknown profile '{profile}'. Known: {known} (+ any omron_bp HEM-*)")


def parse(
    payload: bytes | bytearray,
    profile: Optional[str] = None,
    characteristic_uuid: str = "",
) -> Any:
    """
    Parse raw notification bytes.

    Args:
        payload: GATT notification/indication value
        profile: device profile key (see _PROFILE_PARSERS); if None, auto-dispatch
        characteristic_uuid: optional UUID for heuristic routing

    Returns:
        BloodPressureReading | PulseOximeterData | ThermometerReading | dict

    Raises:
        ParseError on known protocol failures
        KeyError on unknown profile name
    """
    data = bytes(payload)
    if profile:
        parser = get_parser(profile)
        return parser.parse(data)
    return parse_dispatch(
        data, 
        characteristic_uuid=characteristic_uuid,
        parsers=[
            BlpBloodPressureParser(),
            MightySatParser(),
            ThermometerParser(),
        ]
    )


# Re-export for `from medical_ble_toolkit.parser import ...`
__all__ = [
    "parse",
    "get_parser",
    "parse_blood_pressure_measurement",
    "parse_omron_record",
    "BlpBloodPressureParser",
    "OmronRecordParser",
    "MightySatParser",
    "ThermometerParser",
    "ForaParser",
    "ParseError",
    "DeviceBrand",
]

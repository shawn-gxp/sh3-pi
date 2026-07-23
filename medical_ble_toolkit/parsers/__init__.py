"""
Pure parser package — bytes in, dataclasses out. NO bleak / I/O.

Kotlin port:
  interface VitalParser<T> { fun parse(payload: ByteArray): T }
  class BlpBloodPressureParser : VitalParser<BloodPressureReading>
  class MightySatParser : VitalParser<PulseOximeterData>
  ...
"""

from .base import VitalParser, parse_dispatch
from .blood_pressure import BlpBloodPressureParser, parse_blood_pressure_measurement
from .mightysat import MightySatParser
from .thermometer import ThermometerParser
from .fora import ForaParser
from .omron import OmronRecordParser, parse_omron_record
from .and_ua651 import (
    cmd_set_time as and_cmd_set_time,
    encode_date_time_2a08,
)
from .htp import HtpTemperatureParser, parse_temperature_measurement
from .nipro_cf import NiproCfParser, parse_cf_measurement
from .nipro_common import encode_date_time_2a08 as nipro_encode_date_time_2a08
from .nipro_nt100b import Nt100bCompanionParser, parse_htp_companion_style

__all__ = [
    "VitalParser",
    "parse_dispatch",
    "BlpBloodPressureParser",
    "parse_blood_pressure_measurement",
    "MightySatParser",
    "ThermometerParser",
    "ForaParser",
    "OmronRecordParser",
    "parse_omron_record",
    "and_cmd_set_time",
    "encode_date_time_2a08",
    "HtpTemperatureParser",
    "parse_temperature_measurement",
    "NiproCfParser",
    "parse_cf_measurement",
    "nipro_encode_date_time_2a08",
    "Nt100bCompanionParser",
    "parse_htp_companion_style",
]

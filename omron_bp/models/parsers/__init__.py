"""Record parsers — pure functions: bytes → vital-sign dicts."""

from omron_bp.models.parsers.classic_vital_14 import (
    parse_classic_vital_14,
    parse_classic_vital_14_6232_family,
    parse_classic_vital_14_bitpacked,
)
from omron_bp.models.parsers.vital_16 import (
    parse_classic_vital_16_6401_family,
    parse_vital_16_715x_bitpacked,
)

__all__ = [
    "parse_classic_vital_14",
    "parse_classic_vital_14_bitpacked",
    "parse_classic_vital_14_6232_family",
    "parse_vital_16_715x_bitpacked",
    "parse_classic_vital_16_6401_family",
]

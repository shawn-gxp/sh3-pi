"""
Nipro Cocoron NC-1BLE — pure BLE protocol + Huffman ECG decode.

Source of truth:
  datasheets/nipro/cocoron_nc1/NC1BLE_FIRST_PARTY_PROTOCOL.md
  Cocoron app jp.co.nipro.Cocoron 1.0.0 (jadx)

NO bleak / I/O here.
"""

from __future__ import annotations

import json
import logging
import uuid
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import Any, Dict, List, Optional, Sequence, Tuple

from ..common.hexutil import format_hex_dump
from ..models import DeviceBrand, ParseError

log = logging.getLogger("medical_ble.nipro_nc1")

# --- GATT (companion exact, upper-case constants in app) --------------------

SVC_COCORON = "c74d1000-457d-4194-8b37-e7188723aea9"
CHAR_ECG = "c74d2000-457d-4194-8b37-e7188723aea9"
CHAR_CONFIG = "c74d2001-457d-4194-8b37-e7188723aea9"
CHAR_RRT = "c74d2002-457d-4194-8b37-e7188723aea9"
CHAR_DATETIME = "00002a08-0000-1000-8000-00805f9b34fb"
CHAR_BATTERY = "00002a19-0000-1000-8000-00805f9b34fb"
CHAR_CCCD = "00002902-0000-1000-8000-00805f9b34fb"

# Flags
ECG_DATETIME = 0x40
ECG_DATASIZE = 0x3F
RRT_DATETIME = 0x10
RRT_LEADS_OFF = 0x20
RRT_ECG_FREE_RUN = 0x02
RRT_ILLEGAL_IPHONE = 0x40

# UI interval labels (prefs index 0..3) → seconds
INTERVAL_LABELS = ("60s", "30s", "10s", "5s")
INTERVAL_SECONDS = (60, 30, 10, 5)

BATTERY_WARNING_MV = 2300
SAMPLE_RATE_HZ = 125  # design PDF
MV_SCALE = 0.01
MV_OFFSET = 512.0

# Name hints (product); scan primary filter is service UUID
PREFIX_NC1 = "NC-1BLE"
NAME_HINTS = ("NC-1BLE", "NC-1", "Cocoron", "COCORON")

_HUFFMAN_TABLE: Optional[List[int]] = None
_END_OF_BUFFER = -1


def _norm_uuid(u: str) -> str:
    return (u or "").lower().replace("-", "")


def is_ecg_char(u: str) -> bool:
    return "c74d2000" in _norm_uuid(u)


def is_rrt_char(u: str) -> bool:
    return "c74d2002" in _norm_uuid(u)


def is_battery_char(u: str) -> bool:
    n = _norm_uuid(u)
    return n.endswith("2a19") or "00002a19" in n


def is_config_char(u: str) -> bool:
    return "c74d2001" in _norm_uuid(u)


def is_datetime_char(u: str) -> bool:
    n = _norm_uuid(u)
    return n.endswith("2a08") or "00002a08" in n


# --- Host id / config / datetime writers ------------------------------------


def make_host_id(seed: Optional[str] = None) -> str:
    """8-char lowercase id (companion stores first 8 of UUID)."""
    if seed:
        s = "".join(c for c in seed.lower() if c.isalnum())
        return (s + "00000000")[:8]
    return uuid.uuid4().hex[:8]


def prefs_to_wire_interval(prefs_index: int) -> int:
    """
    After CCCDs, companion sends:
      wire = (4 - prefs_index) - 1
    prefs 0=60s → wire 3; prefs 3=5s → wire 0.
    """
    i = max(0, min(3, int(prefs_index)))
    return (4 - i) - 1


def seconds_to_prefs_index(seconds: int) -> int:
    """Map desired HR period seconds → UI prefs index."""
    s = int(seconds)
    if s <= 5:
        return 3
    if s <= 10:
        return 2
    if s <= 30:
        return 1
    return 0


def encode_config(
    *,
    free_run: bool = False,
    interval_prefs: int = 0,
    host_id: Optional[str] = None,
    use_companion_interval_map: bool = True,
) -> bytes:
    """
    CONFIG write payload: mode_u8 | interval_u8 | host_id_ascii8

    free_run=True → mode 0x02 (abnormal continuous ECG path).
    interval_prefs: 0..3 UI index (0=60s default).
    """
    mode = RRT_ECG_FREE_RUN if free_run else 0
    if use_companion_interval_map:
        iv = prefs_to_wire_interval(interval_prefs)
    else:
        iv = max(0, min(3, int(interval_prefs)))
    hid = (host_id or make_host_id()).encode("utf-8")[:8]
    if len(hid) < 8:
        hid = hid + b"0" * (8 - len(hid))
    return bytes([mode & 0xFF, iv & 0xFF]) + hid


def encode_datetime(when: Optional[datetime] = None) -> bytes:
    """SIG Date Time 0x2A08 — year LE u16 + mon/day/h/m/s."""
    dt = when or datetime.now()
    y = int(dt.year)
    return bytes(
        [
            y & 0xFF,
            (y >> 8) & 0xFF,
            dt.month & 0xFF,
            dt.day & 0xFF,
            dt.hour & 0xFF,
            dt.minute & 0xFF,
            dt.second & 0xFF,
        ]
    )


# --- Huffman ----------------------------------------------------------------


def load_huffman_table(path: Optional[Path] = None) -> List[int]:
    """Load 4096-node signed index table (JSON array of ints)."""
    global _HUFFMAN_TABLE
    if _HUFFMAN_TABLE is not None and path is None:
        return _HUFFMAN_TABLE

    candidates: List[Path] = []
    if path is not None:
        candidates.append(Path(path))
    here = Path(__file__).resolve().parent
    candidates.append(here / "data" / "nc1_huffman_node_index.json")
    # Repo datasheets fallback
    candidates.append(
        here.parents[1]
        / "datasheets"
        / "nipro"
        / "cocoron_nc1"
        / "huffman_node_index.json"
    )

    for p in candidates:
        if p.is_file():
            data = json.loads(p.read_text(encoding="utf-8"))
            if not isinstance(data, list) or len(data) < 100:
                raise ParseError(f"Invalid Huffman table at {p}")
            table = [int(x) for x in data]
            if path is None:
                _HUFFMAN_TABLE = table
            log.debug("Loaded Huffman table %d nodes from %s", len(table), p)
            return table
    raise ParseError(
        "Huffman table not found. Expected medical_ble_toolkit/parsers/data/"
        "nc1_huffman_node_index.json"
    )


class HuffmanDecoder:
    """Port of companion Huffman.kt (bit-MSB-first tree walk)."""

    def __init__(self, node_index: Optional[Sequence[int]] = None) -> None:
        self.node_index: List[int] = list(node_index or load_huffman_table())
        self.num_nodes = len(self.node_index)
        self.bits_in_buffer = 0
        self.current_bit = 0
        self.eof_buffer = False

    def decode(self, buffer: bytes, size: int) -> List[int]:
        symbols = self.decode_bit_stream(buffer, size)
        if not symbols:
            return symbols
        out = list(symbols)
        out[0] = out[0] - 2048 if out[0] > 1023 else out[0]
        for i in range(1, len(out)):
            delta = out[i] - 2048 if out[i] > 1023 else out[i]
            out[i] = out[i - 1] + delta
        return out

    def decode_bit_stream(self, encoded: bytes, original_size: int) -> List[int]:
        out = [0] * original_size
        if original_size <= 0:
            return out
        s = self.node_index[self.num_nodes - 1]
        self.eof_buffer = False
        self.current_bit = 0
        self.bits_in_buffer = 0
        i = 0
        while True:
            bit = self.read_bit(encoded)
            if bit == _END_OF_BUFFER:
                break
            s = self.node_index[(s * 2) - bit]
            if s < 0:
                out[i] = (-s) - 1
                i += 1
                if i == original_size:
                    break
                s = self.node_index[self.num_nodes - 1]
        return out[:i] if i < original_size else out

    def read_bit(self, buffer: bytes) -> int:
        if self.current_bit == self.bits_in_buffer:
            if self.eof_buffer:
                return _END_OF_BUFFER
            self.eof_buffer = True
            self.bits_in_buffer = len(buffer) << 3
            self.current_bit = 0
        if self.bits_in_buffer == 0:
            return _END_OF_BUFFER
        i = self.current_bit
        bit = (buffer[i >> 3] >> (7 - (i % 8))) & 1
        self.current_bit = i + 1
        return bit


def samples_to_mv(samples: Sequence[int]) -> List[float]:
    """(raw - 512) * 0.01 → mV (companion scale, pre-Ntf filter)."""
    return [(float(s) - MV_OFFSET) * MV_SCALE for s in samples]


# --- Packet parsers ---------------------------------------------------------


@dataclass
class Nc1BatteryReading:
    level_mv: int
    major_ver: int
    minor_ver: int
    bugfix_ver: int
    low_battery: bool = False
    raw_hex: str = ""

    def to_dict(self) -> dict:
        return {
            "type": "battery",
            "level_mv": self.level_mv,
            "firmware": f"{self.major_ver}.{self.minor_ver}.{self.bugfix_ver}",
            "low_battery": self.low_battery,
            "raw_hex": self.raw_hex,
        }


@dataclass
class Nc1RriReading:
    rr_ms: int
    heart_rate_bpm: Optional[float]
    leads_off: bool
    free_run: bool
    illegal_host: bool
    has_datetime: bool
    measured_at: Optional[datetime]
    interval_echo: int
    raw_hex: str = ""

    def to_dict(self) -> dict:
        return {
            "type": "rri",
            "rr_ms": self.rr_ms,
            "heart_rate_bpm": self.heart_rate_bpm,
            "leads_off": self.leads_off,
            "free_run": self.free_run,
            "illegal_host": self.illegal_host,
            "measured_at": self.measured_at.isoformat() if self.measured_at else None,
            "interval_echo": self.interval_echo,
            "raw_hex": self.raw_hex,
        }


@dataclass
class Nc1EcgPacket:
    packet_counter: int
    data_size: int
    has_datetime: bool
    measured_at: Optional[datetime]
    trigger1: int
    trigger2: int
    samples_raw: List[int] = field(default_factory=list)
    samples_mv: List[float] = field(default_factory=list)
    raw_hex: str = ""
    decode_ok: bool = True

    def to_dict(self) -> dict:
        mv = self.samples_mv
        return {
            "type": "ecg",
            "packet_counter": self.packet_counter,
            "n_samples": len(mv),
            "data_size": self.data_size,
            "has_datetime": self.has_datetime,
            "measured_at": self.measured_at.isoformat() if self.measured_at else None,
            "trigger1": self.trigger1,
            "trigger2": self.trigger2,
            "mv_min": min(mv) if mv else None,
            "mv_max": max(mv) if mv else None,
            "mv_mean": (sum(mv) / len(mv)) if mv else None,
            "samples_mv": mv,
            "decode_ok": self.decode_ok,
            "raw_hex": self.raw_hex,
        }


def parse_battery(payload: bytes | bytearray) -> Nc1BatteryReading:
    data = bytes(payload)
    if len(data) < 5:
        raise ParseError(f"NC1 battery too short ({len(data)})")
    level = data[0] | (data[1] << 8)
    return Nc1BatteryReading(
        level_mv=level,
        major_ver=data[2],
        minor_ver=data[3],
        bugfix_ver=data[4],
        low_battery=level < BATTERY_WARNING_MV,
        raw_hex=format_hex_dump(data),
    )


def parse_rrt(payload: bytes | bytearray) -> Nc1RriReading:
    data = bytes(payload)
    if len(data) < 4:
        raise ParseError(f"NC1 RRT too short ({len(data)})")
    flags = data[0]
    has_dt = (flags & RRT_DATETIME) == RRT_DATETIME
    leads_off = (flags & RRT_LEADS_OFF) == RRT_LEADS_OFF
    free_run = (flags & RRT_ECG_FREE_RUN) == RRT_ECG_FREE_RUN
    illegal = (flags & RRT_ILLEGAL_IPHONE) == RRT_ILLEGAL_IPHONE
    i = 1
    measured_at: Optional[datetime] = None
    if has_dt:
        if len(data) < 10:
            raise ParseError("NC1 RRT datetime truncated")
        year = data[1] + 2000
        measured_at = datetime(
            year, data[2], data[3], data[4], data[5], data[6]
        )
        i = 7
    if i + 2 >= len(data):
        raise ParseError("NC1 RRT missing RR time")
    rr = data[i] | (data[i + 1] << 8)
    interval_echo = data[i + 2] if i + 2 < len(data) else 0
    hr: Optional[float] = None
    if rr > 0:
        hr = round(60000.0 / float(rr), 1)
    return Nc1RriReading(
        rr_ms=rr,
        heart_rate_bpm=hr,
        leads_off=leads_off,
        free_run=free_run,
        illegal_host=illegal,
        has_datetime=has_dt,
        measured_at=measured_at,
        interval_echo=interval_echo,
        raw_hex=format_hex_dump(data),
    )


def parse_ecg(
    payload: bytes | bytearray,
    *,
    decoder: Optional[HuffmanDecoder] = None,
) -> Nc1EcgPacket:
    data = bytes(payload)
    if len(data) < 5:
        raise ParseError(f"NC1 ECG too short ({len(data)})")
    flags = data[0]
    has_dt = (flags & ECG_DATETIME) == ECG_DATETIME
    data_size = flags & ECG_DATASIZE
    counter = data[1] | (data[2] << 8)
    i = 3
    measured_at: Optional[datetime] = None
    if has_dt:
        if len(data) < 11:
            raise ParseError("NC1 ECG datetime truncated")
        year = data[3] + 2000
        measured_at = datetime(
            year, data[4], data[5], data[6], data[7], data[8]
        )
        i = 9
    if i + 1 >= len(data):
        raise ParseError("NC1 ECG missing triggers")
    trigger1 = data[i]
    trigger2 = data[i + 1]
    compressed = data[i + 2 :]
    dec = decoder or HuffmanDecoder()
    raw_samples = dec.decode(compressed, data_size)
    ok = len(raw_samples) == data_size
    if not ok:
        log.warning(
            "NC1 ECG Huffman size mismatch: got %d want %d",
            len(raw_samples),
            data_size,
        )
        return Nc1EcgPacket(
            packet_counter=counter,
            data_size=data_size,
            has_datetime=has_dt,
            measured_at=measured_at,
            trigger1=trigger1,
            trigger2=trigger2,
            samples_raw=raw_samples,
            samples_mv=[],
            raw_hex=format_hex_dump(data),
            decode_ok=False,
        )
    mv = samples_to_mv(raw_samples)
    return Nc1EcgPacket(
        packet_counter=counter,
        data_size=data_size,
        has_datetime=has_dt,
        measured_at=measured_at,
        trigger1=trigger1,
        trigger2=trigger2,
        samples_raw=list(raw_samples),
        samples_mv=mv,
        raw_hex=format_hex_dump(data),
        decode_ok=True,
    )


class NiproNc1Parser:
    """
    Multi-characteristic parser for NC-1BLE stream session.
    Returns dicts via to_dict() for hub-friendly logging.
    """

    name = "nipro_nc1"
    brand = DeviceBrand.UNKNOWN

    def __init__(self) -> None:
        self.model = "NC-1BLE"
        self._huffman = HuffmanDecoder()
        self.ecg_packets: List[Nc1EcgPacket] = []
        self.rri_readings: List[Nc1RriReading] = []
        self.battery: Optional[Nc1BatteryReading] = None
        self.last_hr: Optional[float] = None
        self.leads_off: bool = False

    def can_parse(
        self, payload: bytes | bytearray, characteristic_uuid: str = ""
    ) -> bool:
        u = characteristic_uuid or ""
        if is_ecg_char(u) or is_rrt_char(u) or is_battery_char(u):
            return True
        # No UUID: accept plausible lengths
        n = len(payload)
        return n >= 4

    def parse(
        self, payload: bytes | bytearray, characteristic_uuid: str = ""
    ) -> Any:
        u = characteristic_uuid or ""
        if is_battery_char(u) or (
            not u and len(payload) == 5
        ):
            rec = parse_battery(payload)
            self.battery = rec
            return rec
        if is_rrt_char(u) or (not u and len(payload) <= 16 and len(payload) >= 4):
            # Prefer UUID; without UUID RRT is short, ECG is longer
            if is_rrt_char(u) or (not is_ecg_char(u) and len(payload) < 20):
                try:
                    rec = parse_rrt(payload)
                    self.rri_readings.append(rec)
                    self.last_hr = rec.heart_rate_bpm
                    self.leads_off = rec.leads_off
                    return rec
                except ParseError:
                    if is_rrt_char(u):
                        raise
        # ECG path
        rec = parse_ecg(payload, decoder=self._huffman)
        if rec.decode_ok:
            self.ecg_packets.append(rec)
        return rec


def summary_stats(parser: NiproNc1Parser) -> Dict[str, Any]:
    n_ecg = len(parser.ecg_packets)
    n_samp = sum(len(p.samples_mv) for p in parser.ecg_packets)
    return {
        "ecg_packets": n_ecg,
        "ecg_samples": n_samp,
        "rri_packets": len(parser.rri_readings),
        "last_hr": parser.last_hr,
        "leads_off": parser.leads_off,
        "battery_mv": parser.battery.level_mv if parser.battery else None,
    }

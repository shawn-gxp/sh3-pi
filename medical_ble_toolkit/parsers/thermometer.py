"""
NT-100B non-contact thermometer proprietary frame parser.

Source: datasheets/nipro/SH3 Communication Protocol TICD_Thermometer_v1.16

Frame (always 8 bytes for listed commands):
  [0] Start   = 0x51
  [1] CMD/ACK
  [2:6] Data0..Data3
  [6] Stop    = 0xA3 (GW→MD) or 0xA5 (MD→GW)
  [7] Checksum = sum(bytes[0..6]) & 0xFF

BLE v4 GATT:
  UUID base: 1212-efde-1523-785feabcd123
  Service 0x1523 / Characteristic 0x1524 (write + notify)

Temperature wire unit: 0.1 °C (or 0.1 °F if OutUnit bit set).
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional, Tuple, Union

from ..common.hexutil import format_hex_dump
from ..models import (
    DeviceBrand,
    ParseError,
    TemperatureSite,
    TemperatureUnit,
    ThermometerReading,
)

FRAME_START = 0x51
STOP_HOST = 0xA3
STOP_DEVICE = 0xA5

CMD_READ_CLOCK = 0x23
CMD_READ_MODEL = 0x24
CMD_READ_STORAGE_TIME = 0x25
CMD_READ_STORAGE_RESULT = 0x26
CMD_READ_SN_PART1 = 0x27
CMD_READ_SN_PART2 = 0x28
CMD_READ_STORAGE_COUNT = 0x2B
CMD_WRITE_CLOCK = 0x33
CMD_START_MEASURE = 0x41
CMD_POWER_OFF = 0x50
CMD_CLEAR_MEMORY = 0x52
CMD_ENTER_COMM = 0x54  # device → host notification

# BLE UUID fragments for auto-detection
THERMO_SERVICE_HINT = "1523"
THERMO_CHAR_HINT = "1524"


def checksum(frame7: bytes) -> int:
    """Sum of first 7 bytes, 8-bit."""
    return sum(frame7[:7]) & 0xFF


def build_frame(cmd: int, data: bytes = b"\x00\x00\x00\x00") -> bytes:
    """Build host→device 8-byte request frame."""
    if len(data) != 4:
        raise ValueError("data must be exactly 4 bytes")
    body = bytes([FRAME_START, cmd & 0xFF]) + data + bytes([STOP_HOST])
    return body + bytes([checksum(body)])


def encode_clock_data(when: Optional[datetime] = None) -> bytes:
    """
    Pack date/time into Data0..Data3 for cmds 0x23 (read rsp) / 0x33 (write).

    TICD § tables:
      word Data1||Data0: Year(7 from 2000) | Month(4) | Day(5)
      Data2: Type2(2)=0 | Minute(6)
      Data3: OutUnit(1)=0 | Reserved(1)=0 | Type1(1)=0 | Hour(5)
    """
    dt = when or datetime.now()
    year_field = max(0, min(127, dt.year - 2000))
    word = (year_field << 9) | ((dt.month & 0x0F) << 5) | (dt.day & 0x1F)
    data0 = word & 0xFF
    data1 = (word >> 8) & 0xFF
    data2 = dt.minute & 0x3F
    data3 = dt.hour & 0x1F
    return bytes([data0, data1, data2, data3])


def cmd_read_storage_count() -> bytes:
    return build_frame(CMD_READ_STORAGE_COUNT)


def cmd_read_storage_time(index: int = 0) -> bytes:
    """index 0 = latest reading."""
    idx = int(index).to_bytes(2, "little", signed=False)
    return build_frame(CMD_READ_STORAGE_TIME, idx + b"\x00\x00")


def cmd_read_storage_result(index: int = 0) -> bytes:
    idx = int(index).to_bytes(2, "little", signed=False)
    return build_frame(CMD_READ_STORAGE_RESULT, idx + b"\x00\x00")


def cmd_read_clock() -> bytes:
    return build_frame(CMD_READ_CLOCK)


def cmd_write_clock(when: Optional[datetime] = None) -> bytes:
    """0x33 — write system clock (same packing as 0x23)."""
    return build_frame(CMD_WRITE_CLOCK, encode_clock_data(when))


def cmd_read_model() -> bytes:
    return build_frame(CMD_READ_MODEL)


def cmd_read_serial_part1() -> bytes:
    return build_frame(CMD_READ_SN_PART1)


def cmd_read_serial_part2() -> bytes:
    return build_frame(CMD_READ_SN_PART2)


def cmd_start_measure() -> bytes:
    """0x41 — start IR measurement (not on TD1241)."""
    return build_frame(CMD_START_MEASURE)


def cmd_power_off() -> bytes:
    return build_frame(CMD_POWER_OFF)


def cmd_clear_memory() -> bytes:
    """0x52 — clear all memory (not on TD1241)."""
    return build_frame(CMD_CLEAR_MEMORY)


def cmd_wakeup_pair() -> bytes:
    """Send any command twice within 10s to enter communication mode (doc §1.1)."""
    return build_frame(CMD_READ_MODEL)


def history_pull_commands(
    count: int,
    *,
    max_records: int = 50,
) -> list[bytes]:
    """
    Build ordered host→device frames to download history after a 0x2B count.

    Index 0 = latest. Pulls min(count, max_records) entries as 0x25 then 0x26 pairs.
    """
    n = max(0, min(int(count), int(max_records)))
    frames: list[bytes] = []
    for index in range(n):
        frames.append(cmd_read_storage_time(index))
        frames.append(cmd_read_storage_result(index))
    return frames


def validate_frame(data: bytes | bytearray, expect_device: bool = True) -> bytes:
    raw = bytes(data)
    if len(raw) != 8:
        raise ParseError(f"Thermometer frame must be 8 bytes, got {len(raw)}", raw)
    if raw[0] != FRAME_START:
        raise ParseError(f"Bad start byte 0x{raw[0]:02X}, expected 0x51", raw)
    stop = STOP_DEVICE if expect_device else STOP_HOST
    # Accept either stop during RE
    if raw[6] not in (STOP_HOST, STOP_DEVICE):
        raise ParseError(f"Bad stop byte 0x{raw[6]:02X}", raw)
    calc = checksum(raw)
    if calc != raw[7]:
        raise ParseError(
            f"Checksum mismatch: wire=0x{raw[7]:02X} calc=0x{calc:02X}",
            raw,
        )
    return raw


def _decode_day_month_year(data0: int, data1: int) -> Tuple[int, int, int]:
    """
    Packed word (data1 << 8 | data0):
      day   bits 0..4
      month bits 5..8
      year  bits 9..15  (offset from 2000? doc says 7-bit year field)
    Doc Table A: Year(7) | Month(4) | Day(5) across Data_1 (MSB) + Data_0 (LSB).
    """
    word = (data1 << 8) | data0
    day = word & 0x1F
    month = (word >> 5) & 0x0F
    year = (word >> 9) & 0x7F
    # Protocol examples use absolute-ish years; many meters encode year-2000.
    full_year = 2000 + year if year < 100 else year
    return full_year, month, day


def _decode_minute_hour_type(data2: int, data3: int) -> Tuple[int, int, TemperatureSite, TemperatureUnit]:
    """
    Table C:
      Data_2: Type2(2) | Minute(6)
      Data_3: OutUnit(1) | Reserved(1) | Type1(1) | Hour(5)
    Type category = Type1*4 + Type2? Doc: Type1+Type2 mean temperature category (3 bits).
    We combine Type1 (1 bit in data3 bit5) and Type2 (2 bits in data2 bits 6-7).
    """
    minute = data2 & 0x3F
    type2 = (data2 >> 6) & 0x03
    hour = data3 & 0x1F
    type1 = (data3 >> 5) & 0x01
    out_unit = (data3 >> 7) & 0x01
    site_code = (type1 << 2) | type2
    try:
        site = TemperatureSite(site_code)
    except ValueError:
        site = TemperatureSite.UNKNOWN
    unit = TemperatureUnit.FAHRENHEIT if out_unit else TemperatureUnit.CELSIUS
    return minute, hour, site, unit


def parse_storage_time_response(frame: bytes, index: int = 0) -> dict:
    """Parse 0x25 response (date/time/site) — part 1 of a full reading."""
    raw = validate_frame(frame)
    if raw[1] != CMD_READ_STORAGE_TIME:
        raise ParseError(f"Expected ACK 0x25, got 0x{raw[1]:02X}", raw)
    year, month, day = _decode_day_month_year(raw[2], raw[3])
    minute, hour, site, unit = _decode_minute_hour_type(raw[4], raw[5])
    measured_at = None
    try:
        if year and month and day:
            measured_at = datetime(year, month, day, hour, minute, 0)
    except ValueError:
        measured_at = None
    return {
        "index": index,
        "measured_at": measured_at,
        "site": site,
        "unit": unit,
        "raw_hex": format_hex_dump(raw),
    }


def parse_storage_result_response(
    frame: bytes,
    meta: Optional[dict] = None,
    index: int = 0,
) -> ThermometerReading:
    """
    Parse 0x26 response (object + ambient temperature in 0.1° units).
    Combine with meta from 0x25 for a complete ThermometerReading.
    """
    raw = validate_frame(frame)
    if raw[1] != CMD_READ_STORAGE_RESULT:
        raise ParseError(f"Expected ACK 0x26, got 0x{raw[1]:02X}", raw)

    obj_raw = raw[2] | (raw[3] << 8)
    amb_raw = raw[4] | (raw[5] << 8)
    # Treat as unsigned 0.1 degree; values are typically 200–450 (20.0–45.0 °C)
    object_t = obj_raw / 10.0
    ambient_t = amb_raw / 10.0

    meta = meta or {}
    return ThermometerReading(
        object_temperature=object_t,
        ambient_temperature=ambient_t,
        unit=meta.get("unit", TemperatureUnit.CELSIUS),
        site=meta.get("site", TemperatureSite.UNKNOWN),
        measured_at=meta.get("measured_at"),
        index=index,
        brand=DeviceBrand.THERMO,
        model="NT-100B",
        raw_hex=format_hex_dump(raw),
    )


def parse_live_measure_response(frame: bytes) -> ThermometerReading:
    """Parse 0x41 start-measure response (object + ambient)."""
    raw = validate_frame(frame)
    if raw[1] != CMD_START_MEASURE:
        raise ParseError(f"Expected ACK 0x41, got 0x{raw[1]:02X}", raw)
    obj_raw = raw[2] | (raw[3] << 8)
    amb_raw = raw[4] | (raw[5] << 8)
    return ThermometerReading(
        object_temperature=obj_raw / 10.0,
        ambient_temperature=amb_raw / 10.0,
        unit=TemperatureUnit.CELSIUS,
        site=TemperatureSite.FOREHEAD,
        measured_at=datetime.now(),
        brand=DeviceBrand.THERMO,
        model="NT-100B",
        raw_hex=format_hex_dump(raw),
    )


ParseResult = Union[ThermometerReading, dict]


class ThermometerParser:
    """
    NT-100B frame parser. Maintains last 0x25 meta so a following 0x26
    can produce a full ThermometerReading automatically.
    """

    name = "thermometer_nt100b"
    brand = DeviceBrand.THERMO

    def __init__(self):
        self.model = "NT-100B"
        self._last_time_meta: Optional[dict] = None
        self._last_index: int = 0

    def set_history_index(self, index: int) -> None:
        """Host is about to poll 0x25/0x26 for this storage slot (0 = latest)."""
        self._last_index = max(0, int(index))

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        uuid = (characteristic_uuid or "").lower().replace("-", "")
        if THERMO_CHAR_HINT in uuid or THERMO_SERVICE_HINT in uuid:
            return len(payload) >= 3 and (len(payload) == 8 or payload[0] == FRAME_START)
        return len(payload) == 8 and payload[0] == FRAME_START

    def parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> ParseResult:
        raw = bytes(payload)
        uuid = (characteristic_uuid or "").lower().replace("-", "")

        # During RE we may see incomplete frames — validate soft
        if len(raw) != 8:
            raise ParseError(f"Expected 8-byte frame, got {len(raw)}", raw)
        try:
            validate_frame(raw)
        except ParseError:
            # Still try to surface CMD for RE even if checksum fails
            pass

        cmd = raw[1] if len(raw) > 1 else -1
        if cmd == CMD_READ_STORAGE_TIME:
            meta = parse_storage_time_response(raw, index=self._last_index)
            self._last_time_meta = meta
            # Keep index sticky until next 0x25 so following 0x26 pairs correctly
            return meta
        if cmd == CMD_READ_STORAGE_RESULT:
            reading = parse_storage_result_response(
                raw, meta=self._last_time_meta, index=self._last_index
            )
            return reading
        if cmd == CMD_START_MEASURE:
            return parse_live_measure_response(raw)
        if cmd == CMD_READ_STORAGE_COUNT:
            validate_frame(raw)
            count = raw[2] | (raw[3] << 8)
            return {"type": "storage_count", "count": count, "raw_hex": format_hex_dump(raw)}
        if cmd == CMD_ENTER_COMM:
            return {"type": "enter_comm_mode", "raw_hex": format_hex_dump(raw)}
        if cmd == CMD_READ_MODEL:
            validate_frame(raw)
            model = (raw[3] << 8) | raw[2]
            return {"type": "model", "project_code": model, "raw_hex": format_hex_dump(raw)}

        return {
            "type": "raw_frame",
            "cmd": cmd,
            "raw_hex": format_hex_dump(raw),
        }

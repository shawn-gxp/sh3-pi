"""
A&D UA-651BLE host command builders (pure bytes — no bleak).

Source: datasheets/nipro/SH3 sdk_ble_ua-651ble_V1.2
  - §2.1.4 Date and Time (0x2A08)
  - §5.1 Custom service 0xF000 / char 0xF001 (20-byte buffer)

Custom buffer layout:
  [0]     Size  = len(Type + Command + Value)
  [1]     Type  = 0 read | 1 write | 2 response
  [2]     Command ID
  [3..]   Value
  pad to 20 bytes with 0x00

Documented UA-651BLE commands:
  0x01 Set Time (value: YY_lo2, mon, day, hour, min, sec)
  0x03 Disconnect → stand-by
  0x10 Unpair → stand-by
  0x12 Delete all memory → stand-by
  0xA6 Set buffer size (0=none, 1=30 records)
  0xD6 Read buffer size
  0xE1 Request all memory data (sequence diagrams; no-value write)

SDK also requires, within ~5 s after encrypt:
  - CCCD Indicate enable on 0x2A35 (handled by bleak start_notify)
  - Date Time write on 0x2A08 (and/or custom 0x01 Set Time)
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

# GATT UUIDs from A&D profile mapping
AND_CUSTOM_SERVICE_UUID = "233bf000-5a34-1b6d-975c-000d5690abe4"
AND_CUSTOM_CHAR_UUID = "233bf001-5a34-1b6d-975c-000d5690abe4"
DATE_TIME_UUID = "00002a08-0000-1000-8000-00805f9b34fb"
BP_FEATURE_UUID = "00002a49-0000-1000-8000-00805f9b34fb"
BATTERY_LEVEL_UUID = "00002a19-0000-1000-8000-00805f9b34fb"

# Custom command IDs
CMD_SET_TIME = 0x01
CMD_DISCONNECT = 0x03
CMD_UNPAIR = 0x10
CMD_DELETE_ALL_MEMORY = 0x12
CMD_SET_BUFFER_SIZE = 0xA6
CMD_READ_BUFFER_SIZE = 0xD6
CMD_REQUEST_ALL_MEMORY = 0xE1

TYPE_READ = 0x00
TYPE_WRITE = 0x01
TYPE_RESPONSE = 0x02

CUSTOM_BUFFER_LEN = 20


def build_custom_command(
    command: int,
    *,
    type_: int = TYPE_WRITE,
    value: bytes = b"",
) -> bytes:
    """
    Build a 20-byte A&D custom characteristic write payload.

    Examples from SDK §5.1:
      Set Time 2014-03-11 19:12:34 → 08 01 01 0E 03 0B 13 0C 22 …
      Disconnect                  → 02 01 03 …
      Read buffer size            → 02 00 D6 …
    """
    if len(value) > CUSTOM_BUFFER_LEN - 3:
        raise ValueError(f"value too long for 20-byte custom buffer: {len(value)}")
    body = bytes([type_ & 0xFF, command & 0xFF]) + bytes(value)
    size = len(body)
    packet = bytes([size]) + body
    if len(packet) > CUSTOM_BUFFER_LEN:
        raise ValueError("custom command exceeds 20 bytes")
    return packet + bytes(CUSTOM_BUFFER_LEN - len(packet))


def encode_date_time_2a08(when: Optional[datetime] = None) -> bytes:
    """
    SIG Date Time characteristic (0x2A08) — 7 bytes little-endian year.

    Layout: year(u16 LE), month, day, hours, minutes, seconds.
    A&D rejects the write if any field is out of range (SDK §2.1.4).
    """
    dt = when or datetime.now()
    if not (1 <= dt.month <= 12 and 1 <= dt.day <= 31):
        raise ValueError(f"invalid calendar date for 0x2A08: {dt}")
    if not (0 <= dt.hour <= 24 and 0 <= dt.minute <= 59 and 0 <= dt.second <= 59):
        raise ValueError(f"invalid clock time for 0x2A08: {dt}")
    year = int(dt.year)
    if year < 1582 or year > 9999:
        raise ValueError(f"year out of SIG Date Time range: {year}")
    return bytes(
        [
            year & 0xFF,
            (year >> 8) & 0xFF,
            dt.month & 0xFF,
            dt.day & 0xFF,
            dt.hour & 0xFF,
            dt.minute & 0xFF,
            dt.second & 0xFF,
        ]
    )


def cmd_set_time(when: Optional[datetime] = None) -> bytes:
    """
    Custom 0x01 Set Time — year is *lower two decimal digits* only.

    SDK example: 2014/3/11 19:12:34 → 0E 03 0B 13 0C 22
    """
    dt = when or datetime.now()
    yy = dt.year % 100
    value = bytes(
        [
            yy & 0xFF,
            dt.month & 0xFF,
            dt.day & 0xFF,
            dt.hour & 0xFF,
            dt.minute & 0xFF,
            dt.second & 0xFF,
        ]
    )
    return build_custom_command(CMD_SET_TIME, type_=TYPE_WRITE, value=value)


def cmd_disconnect() -> bytes:
    """0x03 — device disconnects and enters stand-by."""
    return build_custom_command(CMD_DISCONNECT, type_=TYPE_WRITE)


def cmd_unpair() -> bytes:
    """0x10 — unpair bond and stand-by."""
    return build_custom_command(CMD_UNPAIR, type_=TYPE_WRITE)


def cmd_delete_all_memory() -> bytes:
    """0x12 — wipe measurement buffer and stand-by."""
    return build_custom_command(CMD_DELETE_ALL_MEMORY, type_=TYPE_WRITE)


def cmd_set_buffer_size(mode: int = 1) -> bytes:
    """
    0xA6 — set memory buffer size.

    mode 0 = no buffer, 1 = 30 records (SDK example 03 01 A6 01).
    """
    if mode not in (0, 1):
        raise ValueError("buffer mode must be 0 (none) or 1 (30 records)")
    return build_custom_command(
        CMD_SET_BUFFER_SIZE, type_=TYPE_WRITE, value=bytes([mode & 0xFF])
    )


def cmd_read_buffer_size() -> bytes:
    """0xD6 read request (Type=0). Response example: 03 02 D6 01."""
    return build_custom_command(CMD_READ_BUFFER_SIZE, type_=TYPE_READ)


def cmd_request_all_memory() -> bytes:
    """
    0xE1 — request all memory data (listed in pairing sequence diagrams).

    Exact value payload is not fully tabulated in §5.1; treated as no-value write
    like disconnect/unpair. Prefer the standard gated path (CCCD + Date Time)
    for normal history dumps.
    """
    return build_custom_command(CMD_REQUEST_ALL_MEMORY, type_=TYPE_WRITE)


def parse_custom_response(data: bytes | bytearray) -> dict:
    """
    Decode a 20-byte (or shorter) custom characteristic notification/read.

    Returns dict with size, type, command, value_hex — for RE / logging.
    """
    raw = bytes(data)
    if not raw:
        return {"type": "empty"}
    size = raw[0]
    if len(raw) < 3:
        return {"type": "short", "raw_hex": raw.hex(" ")}
    type_ = raw[1]
    command = raw[2]
    value = raw[3 : 1 + size] if size >= 2 else b""
    type_name = {0: "read", 1: "write", 2: "response"}.get(type_, f"type_{type_}")
    return {
        "size": size,
        "msg_type": type_name,
        "command": command,
        "value": value,
        "value_hex": value.hex(" ") if value else "",
        "raw_hex": raw.hex(" "),
    }

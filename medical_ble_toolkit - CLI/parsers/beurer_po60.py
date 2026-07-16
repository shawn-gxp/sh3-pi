"""
Beurer PO60 pulse oximeter proprietary frames (PulseOxyDeviceSyncRepoImpl).

Service 0xFF12 / notify 0xFF02 / write 0xFF01
Request more: 99 01 1A (WITHOUT_RESPONSE)

Records: 24-byte frames; stream may be buffered with header 0xE9.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import List, Optional

from ..common.hexutil import format_hex_dump
from ..models import DeviceBrand, ParseError

PO60_SERVICE = "0000ff12-0000-1000-8000-00805f9b34fb"
PO60_WRITE = "0000ff01-0000-1000-8000-00805f9b34fb"
PO60_NOTIFY = "0000ff02-0000-1000-8000-00805f9b34fb"

CMD_REQUEST_MORE = bytes([0x99, 0x01, 0x1A])


def cmd_request_more() -> bytes:
    return CMD_REQUEST_MORE


def _dt6(data: bytes, off: int) -> Optional[datetime]:
    if off + 6 > len(data):
        return None
    year = data[off] + 2000
    month, day = data[off + 1], data[off + 2]
    hour, minute, second = data[off + 3], data[off + 4], data[off + 5]
    try:
        return datetime(year, month, day, hour, minute, second)
    except ValueError:
        return None


@dataclass
class Po60Record:
    spo2_max: Optional[int]
    spo2_min: Optional[int]
    spo2_avg: Optional[int]
    pr_max: Optional[int]
    pr_min: Optional[int]
    pr_avg: Optional[int]
    start: Optional[datetime]
    end: Optional[datetime]
    last_on_device: bool
    raw_hex: str = ""
    brand: DeviceBrand = DeviceBrand.BEURER
    model: str = "PO60"

    def to_dict(self) -> dict:
        return {
            "type": "po60",
            "spo2_max": self.spo2_max,
            "spo2_min": self.spo2_min,
            "spo2_avg": self.spo2_avg,
            "pr_max": self.pr_max,
            "pr_min": self.pr_min,
            "pr_avg": self.pr_avg,
            "start": self.start.isoformat() if self.start else None,
            "end": self.end.isoformat() if self.end else None,
            "last_on_device": self.last_on_device,
            "raw_hex": self.raw_hex,
        }


def parse_po60_record(frame: bytes | bytearray) -> Po60Record:
    data = bytes(frame)
    if len(data) < 24:
        raise ParseError(f"PO60 record needs 24 bytes, got {len(data)}", data)
    # Prefer aligned frame starting at 0; if E9 header, shift
    off = 0
    if data[0] == 0xE9 and len(data) >= 25:
        off = 1
    body = data[off : off + 24]
    if len(body) < 24:
        body = data[:24]

    last_on = bool(body[1] & 0x40) if len(body) > 1 else False
    start = _dt6(body, 2)
    end = _dt6(body, 8)
    spo2_max = body[17]
    spo2_min = body[18]
    spo2_avg = body[19]
    # PR packing is approximate from decompile — store raw high bytes + simple fields
    pr_max = body[20]
    pr_min = body[21]
    pr_avg = body[22]

    return Po60Record(
        spo2_max=spo2_max,
        spo2_min=spo2_min,
        spo2_avg=spo2_avg,
        pr_max=pr_max,
        pr_min=pr_min,
        pr_avg=pr_avg,
        start=start,
        end=end,
        last_on_device=last_on,
        raw_hex=format_hex_dump(body),
    )


class Po60StreamBuffer:
    """Accumulate notify bytes and extract 24-byte records (E9-framed)."""

    def __init__(self) -> None:
        self.buf = bytearray()
        self.records: List[Po60Record] = []
        self.need_more = False

    def feed(self, chunk: bytes) -> List[Po60Record]:
        self.buf.extend(chunk)
        found: List[Po60Record] = []
        while True:
            try:
                idx = self.buf.index(0xE9)
            except ValueError:
                # No header — try fixed 24-byte chunks if long enough
                if len(self.buf) >= 24:
                    rec = parse_po60_record(bytes(self.buf[:24]))
                    found.append(rec)
                    self.records.append(rec)
                    del self.buf[:24]
                    continue
                break
            if len(self.buf) - idx < 24:
                # Meta: packetNumber & isLast at E9+1
                if len(self.buf) > idx + 1:
                    meta = self.buf[idx + 1]
                    self.need_more = (meta & 0x40) == 0
                break
            frame = bytes(self.buf[idx : idx + 24])
            meta = frame[1] if len(frame) > 1 else 0
            self.need_more = (meta & 0x40) == 0
            rec = parse_po60_record(frame)
            found.append(rec)
            self.records.append(rec)
            del self.buf[: idx + 24]
        return found


class BeurerPo60Parser:
    name = "beurer_po60"
    brand = DeviceBrand.BEURER

    def __init__(self) -> None:
        self.stream = Po60StreamBuffer()
        self.model = "PO60"

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        u = characteristic_uuid.lower()
        return "ff02" in u or "ff12" in u or len(payload) >= 8

    def parse(self, payload: bytes | bytearray):
        recs = self.stream.feed(bytes(payload))
        if recs:
            return recs[-1]
        return {
            "type": "po60_partial",
            "buffered": len(self.stream.buf),
            "need_more": self.stream.need_more,
            "raw_hex": format_hex_dump(payload),
        }

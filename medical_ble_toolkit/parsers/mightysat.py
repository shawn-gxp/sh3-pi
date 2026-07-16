"""
Masimo MightySat proprietary BLE protocol parser (CSD-1322B).

Framing:
  SOM (0x77) | LEN | PAYLOAD[N] | CRC8(payload)

LEN = sizeof(PAYLOAD + CRC)  i.e. len(payload)+1, does NOT include LEN itself.

GATT (base UUID pattern 54c2XXXX-a720-4b4f-11e4-9fe20002a5d5):
  Service  0x1000
  RX       0x1001  write-without-response  (host → device commands)
  TX       0x1002  notify                 (device → host)

Message IDs of interest:
  0x01 Get Device Info response
  0x05 Parameter streaming (1 Hz)
  0x04 Waveform streaming
  0xFE ACK / 0xFF NACK

NO bleak imports — pure framing + parameter decode.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import List, Optional, Union

from ..common.crc import crc8_ccitt
from ..common.hexutil import format_hex_dump
from ..models import (
    DeviceBrand,
    ParseError,
    PulseOximeterData,
    WaveformPacket,
    WaveformSample,
)

SOM = 0x77

# Characteristic short IDs (embedded in full UUID)
MIGHTYSAT_SERVICE_UUID = "54c21000-a720-4b4f-11e4-9fe20002a5d5"
MIGHTYSAT_RX_UUID = "54c21001-a720-4b4f-11e4-9fe20002a5d5"
MIGHTYSAT_TX_UUID = "54c21002-a720-4b4f-11e4-9fe20002a5d5"

# Parameter bitmask bits (LSB first streaming order)
PARAM_SPO2 = 0
PARAM_PR = 1
PARAM_PVI = 2
PARAM_PI = 3
PARAM_RRP = 4

# Streaming response IDs
MSG_WAVEFORM = 0x04
MSG_PARAMETERS = 0x05
MSG_ACK = 0xFE
MSG_NACK = 0xFF

# System exception bits (32-bit LE)
SYS_SENSOR_OFF = 21
SYS_PULSE_SEARCH = 22
SYS_INTERFERENCE = 23
SYS_LOW_PERFUSION = 24

# Parameter exception bits
PEX_LOW_CONFIDENCE = 0
PEX_INVALID = 2
PEX_STARTUP = 4


@dataclass
class MightySatFrame:
    """Deframed message before domain parse."""
    payload: bytes
    raw_hex: str
    crc_ok: bool


def build_command(payload: bytes) -> bytes:
    """
    Host → device framed command.
    Example Get Device Info: payload = [0x01] → [0x77, 0x02, 0x01, crc]
    """
    crc = crc8_ccitt(payload)
    length = len(payload) + 1  # PAYLOAD + CRC
    return bytes([SOM, length]) + payload + bytes([crc])


def cmd_get_device_info() -> bytes:
    return build_command(bytes([0x01]))


def cmd_set_clock(unix_seconds: int) -> bytes:
    ts = int(unix_seconds).to_bytes(4, "little", signed=False)
    return build_command(bytes([0x02]) + ts)


def cmd_configure_streaming(param_mask: int = 0x001F, wave_mask: int = 0x03) -> bytes:
    """
    Default: enable SpO2|PR|PVi|PI|RRp + Pleth|SIQ waveforms.
    param_mask: uint16 LE, wave_mask: uint8

    Doc example: [0x77, 0x05, 0x03, 0x1F, 0x00, 0x03, 0xD6]
    """
    body = bytes([
        0x03,
        param_mask & 0xFF,
        (param_mask >> 8) & 0xFF,
        wave_mask & 0xFF,
    ])
    return build_command(body)


def cmd_get_trend_record(session_id: int) -> bytes:
    """
    0x06 Get Trend Record — session_id uint32 LE.

    Doc example: [0x77, 0x06, 0x06, 0x9B, 0x00, 0x00, 0x00, 0x97] for id 0x9B.
    Do not request next record before ACK/response/NACK for this one.
    """
    sid = int(session_id).to_bytes(4, "little", signed=False)
    return build_command(bytes([0x06]) + sid)


def cmd_clear_trend_records() -> bytes:
    """0x07 Clear all trend records. Doc: [0x77, 0x02, 0x07, 0x15]."""
    return build_command(bytes([0x07]))


def parse_device_info(payload: bytes) -> dict:
    """
    Decode Get Device Information response payload (after deframe).

    Layout (19 bytes after msg id per CSD-1322B §3.4.1):
      [0]    0x01
      [1:3]  SW version
      [3:5]  available parameters
      [5]    available waveforms
      [6:8]  trend version
      [8:10] trended parameters
      [10]   number of trend records [0-100]
      [11:15] oldest trend session id
      [15:19] current trend session id
    """
    if not payload or payload[0] != 0x01:
        raise ParseError(
            f"Not device-info (id=0x{payload[0]:02X})" if payload else "empty",
            payload,
        )
    if len(payload) < 19:
        raise ParseError(
            f"Device info truncated: need 19 bytes, got {len(payload)}",
            payload,
        )
    return {
        "type": "device_info",
        "message_id": 0x01,
        "sw_version": int.from_bytes(payload[1:3], "little"),
        "available_parameters": int.from_bytes(payload[3:5], "little"),
        "available_waveforms": payload[5],
        "trend_version": int.from_bytes(payload[6:8], "little"),
        "trended_parameters": int.from_bytes(payload[8:10], "little"),
        "trend_record_count": payload[10],
        "oldest_trend_session_id": int.from_bytes(payload[11:15], "little"),
        "current_trend_session_id": int.from_bytes(payload[15:19], "little"),
        "raw_hex": format_hex_dump(payload),
    }


def _trend_stat_block(data: bytes, offset: int) -> tuple[dict, int]:
    """4-byte min/max/avg/last; 255 = invalid."""
    if offset + 4 > len(data):
        raise ParseError(f"Trend block truncated at offset {offset}", data)

    def _v(b: int) -> Optional[int]:
        return None if b == 255 else int(b)

    block = {
        "min": _v(data[offset]),
        "max": _v(data[offset + 1]),
        "avg": _v(data[offset + 2]),
        "last": _v(data[offset + 3]),
    }
    return block, offset + 4


def parse_trend_record(
    payload: bytes,
    *,
    trended_mask: int = 0x0017,
) -> dict:
    """
    Decode Get Trend Record response (msg id 0x06).

    Fixed header then SpO2, PR, optional PVi/RRp blocks when present in device
    info (PI is never stored as trend per CSD).
    Default mask 0x0017 = SpO2|PR|PVi|RRp (bits 0,1,2,4) — adjust from device info.
    """
    if not payload or payload[0] != 0x06:
        raise ParseError(
            f"Not trend record (id=0x{payload[0]:02X})" if payload else "empty",
            payload,
        )
    if len(payload) < 13:
        raise ParseError(
            f"Trend record truncated: need ≥13 header bytes, got {len(payload)}",
            payload,
        )
    session_id = int.from_bytes(payload[1:5], "little")
    duration_s = int.from_bytes(payload[5:9], "little")
    last_ts = int.from_bytes(payload[9:13], "little")
    offset = 13
    params: dict[str, dict] = {}
    # Order: SpO2, PR always; then PVi (bit2), RRp (bit4) if available — no PI
    order = [
        (PARAM_SPO2, "spo2"),
        (PARAM_PR, "pr"),
        (PARAM_PVI, "pvi"),
        (PARAM_RRP, "rrp"),
    ]
    for bit, name in order:
        # SpO2 + PR always present in response table; optional params only if bit set
        required = bit in (PARAM_SPO2, PARAM_PR)
        if not required and not (trended_mask & (1 << bit)):
            continue
        if offset + 4 > len(payload):
            if required:
                raise ParseError(f"Missing required trend block {name}", payload)
            break
        block, offset = _trend_stat_block(payload, offset)
        params[name] = block

    measured_at = None
    if last_ts:
        try:
            measured_at = datetime.fromtimestamp(last_ts)
        except (OSError, OverflowError, ValueError):
            measured_at = None

    return {
        "type": "trend_record",
        "message_id": 0x06,
        "session_id": session_id,
        "duration_seconds": duration_s,
        "last_sample_unix": last_ts,
        "measured_at": measured_at,
        "params": params,
        "raw_hex": format_hex_dump(payload),
    }


def deframe(data: bytes | bytearray) -> MightySatFrame:
    """
    Validate SOM/LEN/CRC and return payload (without CRC).

    Accepts a buffer that may be longer than one frame (extra trailing
    bytes are ignored — use FrameReassembler for multi-frame streams).
    """
    raw = bytes(data)
    hex_s = format_hex_dump(raw)
    if len(raw) < 3:
        raise ParseError(f"MightySat frame too short ({len(raw)} < 3)", raw)
    if raw[0] != SOM:
        raise ParseError(f"Bad SOM: expected 0x77, got 0x{raw[0]:02X}", raw)
    length = raw[1]
    # LEN covers payload+crc; total frame = 2 (SOM+LEN) + length
    expected_total = 2 + length
    if len(raw) < expected_total:
        raise ParseError(
            f"Truncated frame: LEN={length} expects total {expected_total}, got {len(raw)}",
            raw,
        )
    # Use only the first complete frame (ignore trailing start of next frame)
    frame_bytes = raw[:expected_total]
    body = frame_bytes[2:2 + length]  # payload + crc
    if len(body) < 1:
        raise ParseError("Empty body after LEN", raw)
    payload, crc_byte = body[:-1], body[-1]
    calc = crc8_ccitt(payload)
    crc_ok = calc == crc_byte
    if not crc_ok:
        raise ParseError(
            f"CRC mismatch: wire=0x{crc_byte:02X} calc=0x{calc:02X}",
            frame_bytes,
        )
    return MightySatFrame(
        payload=payload,
        raw_hex=format_hex_dump(frame_bytes),
        crc_ok=crc_ok,
    )


def _frame_crc_ok(raw: bytes) -> bool:
    """True if raw is a complete SOM|LEN|payload|CRC frame with matching CRC."""
    if len(raw) < 3 or raw[0] != SOM:
        return False
    length = raw[1]
    total = 2 + length
    if length < 1 or len(raw) < total:
        return False
    body = raw[2:total]
    payload, crc_byte = body[:-1], body[-1]
    return crc8_ccitt(payload) == crc_byte


class FrameReassembler:
    """
    Reassemble MightySat SOM|LEN|payload|CRC frames across BLE notifications.

    WinRT / ATT often splits or coalesces notifications:
      - GetDeviceInfo (22 B) → 20 + 2
      - Waveform (19 B) + start of next frame in same ATT payload
      - Continuation without SOM when previous notif held the 0x77

    feed() returns zero or more complete raw frames (still including SOM).
    Candidate frames are CRC-checked before accept (0x77 can appear in pleth data).
    """

    def __init__(self, max_buf: int = 512):
        self._buf = bytearray()
        self.max_buf = max(64, int(max_buf))
        # Once we have a provisional incomplete frame (SOM+LEN seen), do not
        # resync to a later 0x77 until that frame is complete or proven corrupt.
        self._locked: bool = False

    def reset(self) -> None:
        self._buf.clear()
        self._locked = False

    @property
    def pending(self) -> int:
        return len(self._buf)

    def feed(self, chunk: bytes | bytearray) -> List[bytes]:
        if not chunk:
            return []
        self._buf.extend(bytes(chunk))
        if len(self._buf) > self.max_buf:
            # Keep tail; unlock so we can find next SOM
            keep = self.max_buf // 2
            del self._buf[:-keep]
            self._locked = False

        frames: List[bytes] = []
        while True:
            if not self._buf:
                self._locked = False
                break

            if not self._locked:
                # Hunt for SOM (may discard mid-stream garbage / previous tail)
                try:
                    som_i = self._buf.index(SOM)
                except ValueError:
                    # No SOM yet — keep a short tail in case next chunk completes
                    # a split where SOM was the last byte of previous ATT (already
                    # consumed). If buffer is pure continuation with no 0x77 at
                    # all, drop it to avoid unbounded growth.
                    if len(self._buf) > 64:
                        self._buf.clear()
                    break
                if som_i > 0:
                    del self._buf[:som_i]
                if not self._buf:
                    break

            if len(self._buf) < 2:
                break

            if self._buf[0] != SOM:
                # Lock broken — resync
                self._locked = False
                del self._buf[0]
                continue

            length = self._buf[1]
            total = 2 + length
            # Protocol frames are small (device info ≈ 22 B, streams ≈ 19 B)
            if length < 1 or total > 128:
                self._locked = False
                del self._buf[0]
                continue

            if len(self._buf) < total:
                # Incomplete — stay locked so pleth 0x77 inside body cannot resync
                self._locked = True
                break

            candidate = bytes(self._buf[:total])
            if not _frame_crc_ok(candidate):
                # False SOM (e.g. pleth=0x77) or corrupt — skip this byte, unlock
                self._locked = False
                del self._buf[0]
                continue

            frames.append(candidate)
            del self._buf[:total]
            self._locked = False
        return frames


def _bit(value: int, bit: int) -> bool:
    return bool(value & (1 << bit))


def parse_parameter_stream(
    payload: bytes,
    param_mask: int = 0x001F,
    measured_at: Optional[datetime] = None,
    raw_hex: str = "",
) -> PulseOximeterData:
    """
    Decode message ID 0x05 parameter streaming response.

    Layout:
      [0]     0x05
      [1:5]   system exceptions uint32 LE
      then for each enabled param (LSB→MSB of mask):
        [1]   param exception bitmask
        [1|2] value  (PI is uint16 LE * 0.01; others uint8)
    """
    if not payload or payload[0] != MSG_PARAMETERS:
        raise ParseError(f"Not a parameter stream (id=0x{payload[0]:02X})", payload)
    if len(payload) < 5:
        raise ParseError("Parameter stream truncated before system exceptions", payload)

    sys_ex = int.from_bytes(payload[1:5], "little")
    offset = 5

    values: dict[int, tuple[int, Optional[float]]] = {}
    # param order LSB to MSB
    for bit in range(16):
        if not (param_mask & (1 << bit)):
            continue
        if offset >= len(payload):
            raise ParseError(
                f"Truncated while reading param bit {bit} at offset {offset}",
                payload,
            )
        pex = payload[offset]
        offset += 1
        # PI (bit 3) is uint16; others uint8
        if bit == PARAM_PI:
            if offset + 2 > len(payload):
                raise ParseError("Truncated PI value", payload)
            raw_val = payload[offset] | (payload[offset + 1] << 8)
            offset += 2
            val: Optional[float] = raw_val / 100.0
        else:
            if offset + 1 > len(payload):
                raise ParseError(f"Truncated value for param bit {bit}", payload)
            raw_val = payload[offset]
            offset += 1
            val = float(raw_val) if raw_val != 255 else None  # 255 = invalid in trends
        values[bit] = (pex, val)

    # Aggregate param exceptions from first present param (or any invalid)
    any_low_conf = any(_bit(pex, PEX_LOW_CONFIDENCE) for pex, _ in values.values())
    any_invalid = any(_bit(pex, PEX_INVALID) for pex, _ in values.values())

    def _int_param(bit: int) -> Optional[int]:
        if bit not in values:
            return None
        pex, v = values[bit]
        if v is None or _bit(pex, PEX_INVALID):
            return None
        return int(v)

    pi_val = None
    if PARAM_PI in values:
        pex, v = values[PARAM_PI]
        if v is not None and not _bit(pex, PEX_INVALID):
            pi_val = v

    return PulseOximeterData(
        spo2=_int_param(PARAM_SPO2),
        pulse_rate=_int_param(PARAM_PR),
        perfusion_index=pi_val,
        pvi=_int_param(PARAM_PVI),
        rrp=_int_param(PARAM_RRP),
        sensor_off=_bit(sys_ex, SYS_SENSOR_OFF),
        pulse_search=_bit(sys_ex, SYS_PULSE_SEARCH),
        interference=_bit(sys_ex, SYS_INTERFERENCE),
        low_perfusion=_bit(sys_ex, SYS_LOW_PERFUSION),
        low_confidence=any_low_conf,
        invalid=any_invalid,
        brand=DeviceBrand.MASIMO,
        model="MightySat",
        measured_at=measured_at,
        raw_hex=raw_hex or format_hex_dump(payload),
        message_id=MSG_PARAMETERS,
    )


def parse_waveform_stream(payload: bytes, raw_hex: str = "") -> WaveformPacket:
    """Decode message ID 0x04 waveform streaming response."""
    if not payload or payload[0] != MSG_WAVEFORM:
        raise ParseError(f"Not a waveform stream (id=0x{payload[0]:02X})", payload)
    if len(payload) < 2:
        raise ParseError("Waveform stream missing ordinal", payload)
    ordinal = payload[1]
    samples: List[WaveformSample] = []
    offset = 2
    while offset + 2 <= len(payload):
        pleth_raw = payload[offset]
        # int8 pleth
        pleth = pleth_raw - 256 if pleth_raw >= 128 else pleth_raw
        siq_raw = payload[offset + 1]
        siq_invalid = bool(siq_raw & 0x80)
        siq = siq_raw & 0x7F
        samples.append(WaveformSample(pleth=pleth, siq=siq, siq_invalid=siq_invalid))
        offset += 2
    return WaveformPacket(
        ordinal=ordinal,
        samples=samples,
        raw_hex=raw_hex or format_hex_dump(payload),
    )


ParseResult = Union[PulseOximeterData, WaveformPacket, dict]


class MightySatParser:
    """
    Standalone MightySat parser (pure logic).

    `param_mask` must match what was sent in Configure Streaming (0x03)
    so parameter field order is correct.

    Streaming path: use feed() so BLE split/coalesced notifications reassemble.
    Unit tests / complete frames: use parse() on one full SOM|LEN|… frame.
    """

    name = "mightysat"
    brand = DeviceBrand.MASIMO

    def __init__(self, param_mask: int = 0x001F):
        self.param_mask = param_mask
        self.model = "MightySat"
        self._reasm = FrameReassembler()

    def reset_reassembly(self) -> None:
        self._reasm.reset()

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        uuid = characteristic_uuid.lower()
        if "54c2" in uuid or "1002" in uuid:
            return True
        return len(payload) >= 3 and payload[0] == SOM

    def feed(self, chunk: bytes | bytearray) -> List[ParseResult]:
        """
        Accept one BLE notification (any size). Returns zero or more parsed
        messages once complete frames are available.

        Never raises for split ATT PDUs (0x11… / truncated 0x77 0x14…): those
        stay in the reassembly buffer until the next notification completes them.
        """
        results: List[ParseResult] = []
        for raw_frame in self._reasm.feed(chunk):
            try:
                results.append(self.parse(raw_frame))
            except ParseError:
                # CRC-locked reassembler should prevent this; skip bad frame
                continue
        return results

    def parse(self, payload: bytes | bytearray) -> ParseResult:
        frame = deframe(payload)
        if not frame.payload:
            raise ParseError("Empty MightySat payload after deframe", payload)
        msg_id = frame.payload[0]
        now = datetime.now()
        if msg_id == MSG_PARAMETERS:
            return parse_parameter_stream(
                frame.payload,
                param_mask=self.param_mask,
                measured_at=now,
                raw_hex=frame.raw_hex,
            )
        if msg_id == MSG_WAVEFORM:
            return parse_waveform_stream(frame.payload, raw_hex=frame.raw_hex)
        if msg_id == 0x01:
            return parse_device_info(frame.payload)
        if msg_id == 0x06:
            return parse_trend_record(
                frame.payload, trended_mask=getattr(self, "trended_mask", 0x0017)
            )
        if msg_id == MSG_ACK:
            return {"type": "ack", "command_id": frame.payload[1] if len(frame.payload) > 1 else None, "raw_hex": frame.raw_hex}
        if msg_id == MSG_NACK:
            return {
                "type": "nack",
                "command_id": frame.payload[1] if len(frame.payload) > 1 else None,
                "error_code": frame.payload[2] if len(frame.payload) > 2 else None,
                "raw_hex": frame.raw_hex,
            }
        # Unknown — structured dict for RE
        return {
            "type": "raw_message",
            "message_id": msg_id,
            "payload_hex": format_hex_dump(frame.payload),
            "raw_hex": frame.raw_hex,
        }

"""
FORA / TaiDoc bus protocol — first-party constants from iFORA Smart 1.5.9 (jadx).

Decompiled sources (authoritative):
  datasheets/FORA/extracted/decompiled/sources/com/foracare/tdlink/sm/
    constants/LibraryConstant.java, MeterCommand.java, TDLinkConst.java, LibraryEnum.java
    utils/ByteUtils.java, BluetoothUtils.java, MeterPairUtils.java
    inits/iMeterConnService.java

Wire frame (after appendOneByteCheckSumToCmd):
  [0]=0x51  [1]=cmd  [2..5]=msg  [6]=dir (TX 0xA3 / RX 0xA5)  [7]=sum[0..6]&0xFF

RX acceptance (dataReceived): len>=8, start==0x51, byte[len-2]==0xA5, last==checksum.
WS long frames: reassemble to 34 or 40 bytes; start 0x51|0x77; penultimate 0xA5.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from typing import Dict, List, Optional, Sequence, Tuple

# --- GATT ---
SERVICE_UUID = "00001523-1212-efde-1523-785feabcd123"
CHAR_UUID = "00001524-1212-efde-1523-785feabcd123"
WRITE_WITHOUT_RESPONSE = True  # BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE = 1

# Android BLE connect flags (iMeterConnService.connect)
# connectGatt(context, autoConnect=false, callback, transport=TRANSPORT_LE=2)
GATT_AUTO_CONNECT = False
GATT_TRANSPORT_LE = 2

# ScanSettings: SCAN_MODE_LOW_LATENCY=2, reportDelay=0
SCAN_MODE_LOW_LATENCY = 2
SCAN_REPORT_DELAY_MS = 0

# --- Pairing ---
METER_PIN_CODE = "111111"
NAME_PREFIXES = ("fora", "td-", "tng", "diamond", "taidoc", "sootheneb")
# Exact name contentEquals("FORA") gets extra log path in connect()
PAIRED_SLOTS_MAX = 12  # MeterPairUtils.maxCounts; getPairedPos scans 0..10
PAIRED_PREF_PREFIXES = (
    "BLE_PAIRED_METER_ADDR_",
    "BLE_PAIRED_METER_NAME_",
    "BLE_PAIRED_METER_INIT_",  # "0" means needRemoveRecord true for re-import wipe
)

# --- Frame ---
FRAME_START = 0x51
FRAME_START_WS_ALT = 0x77
DIR_OUT = 0xA3
DIR_IN = 0xA5

# Opcodes
CMD_PROJECT_CODE = 0x24
CMD_RECORD_DATETIME = 0x25
CMD_RECORD_VALUE = 0x26
CMD_SERIAL_2 = 0x27
CMD_SERIAL_1 = 0x28
CMD_FIRMWARE = 0x29
CMD_RECORD_NUMBER = 0x2B
CMD_SET_DATETIME = 0x33
CMD_SET_BLE_STATUS = 0x39
CMD_POWER_OFF = 0x50
CMD_CLEAR_MEMORY = 0x52
CMD_GET_NEW_SPO2 = 0x49
CMD_SET_MONITOR_SW_STOP = 0x47  # 71
CMD_WS_READ = 0x71  # 113
CMD_WS_READ2 = 0x77  # 119

# Clinical ranges
BG_LOW, BG_HIGH = 20.0, 600.0
BG_INVALID = 65535
YEAR_BASE = 2000  # app uses hTC_DEVICE_UNPAIR_AND_PAIRED_DELAY_TIME (2000) as year base

# Import type codes (BluetoothUtils + LibraryEnum.MeasurementType values)
TYPE_BG = 1
TYPE_BP = 2
TYPE_MP = 6
TYPE_SPO2 = 7
TYPE_WS = 8
TYPE_PEAKFLOW = 31
TYPE_NEBULIZER = 32
TYPE_TM = 60  # Ear thermometer family (project prefix "1")

MEASUREMENT_TYPE_VALUES = {
    "BG": 1,
    "BP": 2,
    "SpO2": 7,
    "BodyWeight": 8,
    "Step": 9,
    "PeakFlow": 31,
    "Nebulizer": 32,
    "Ear": 60,
    "ForHead": 61,
    "HEMATOCRIT": 90,
    "HB": 91,
    "KT": 92,
    "LACTATE": 93,
    "UA": 94,
    "CHOL": 95,
    "TG": 96,
}

# getDataType(projectNo): prefix → importType
# projectNo is 4 hex chars from response: sprintf("%02x%02x", data[3], data[2])
PROJECT_PREFIX_TO_IMPORT_TYPE: Dict[str, int] = {
    # first two hex chars
    "32": TYPE_MP,  # only when getDataType(status=true); later may flip BG/BP via bit
    "73": TYPE_PEAKFLOW,
    "70": TYPE_NEBULIZER,
    # first one hex char
    "1": TYPE_TM,  # 1xxx e.g. 1261, 1035
    "2": TYPE_WS,  # 2xxx scales
    "3": TYPE_BP,  # 3xxx BP (except 32xx multiparam/2-in-1)
    "4": TYPE_BG,  # 4xxx glucose
    "8": TYPE_SPO2,  # 8xxx
}

# Special project full codes (onGetCodeProject userNo / path)
SPECIAL_PROJECTS = {
    "3280": {"user_no": 1, "note": "force userNo=1 with 3128"},
    "3128": {"user_no": 1, "note": "force userNo=1 with 3280"},
    "3261": {"user_from_profile": True},
    "2560": {"user_from_profile": True, "ws_cmd": "ws_read2"},
    "3132": {"user_from_profile": True},
    "2555": {"user_from_profile": True},
    "7301": {"peakflow_type_from_data5": True},
    "4255": {"note": "special record-count path"},
    "3140": {"ihb_decode_variant": True},
    "2551": {"ws_note": True},
}

# All MeterCommand templates pre-checksum (MeterCommand.java)
# Values are decimal as in decompile; checksum appended at TX.
COMMAND_TEMPLATES: Dict[str, List[int]] = {
    "project_code": [81, 36, 0, 0, 0, 0, 163],
    "record_number": [81, 43, 0, 0, 0, 0, 163],
    "record_datetime": [81, 37, 0, 0, 0, 0, 163],
    "record_value": [81, 38, 0, 0, 0, 0, 163],
    "power_off": [81, 80, 0, 0, 0, 0, 163],
    "serial_1": [81, 40, 0, 0, 0, 0, 163],
    "serial_2": [81, 39, 0, 0, 0, 0, 163],
    "set_datetime": [81, 51, 0, 0, 0, 0, 163],
    # WS templates appear 6-element in decompile (missing pad) — pad to 7 with 0
    "ws_read": [81, 113, 2, 0, 0, 0, 163],
    "ws_read2": [81, 119, 2, 0, 0, 0, 163],
    "get_new_spo2": [81, 73, 0, 0, 0, 0, 163],
    "firmware": [81, 41, 0, 0, 0, 0, 163],
    "clear_memory": [81, 82, 0, 0, 0, 0, 163],
    "set_monitor_sw_stop": [81, 71, 0, 0, 0, 0, 163],
    "set_ble_status": [81, 57, 255, 255, 255, 255, 163],
    "peakflow_person_info1": [81, 106, 0, 0, 0, 0, 163],
    "peakflow_person_info2": [81, 107, 0, 0, 0, 0, 163],
    "set_peakflow_person_info1": [81, 58, 0, 0, 0, 0, 163],
    "set_peakflow_person_info2": [81, 59, 0, 0, 0, 0, 163],
    "peakflow_setting1": [81, 32, 0, 0, 0, 0, 163],
    "peakflow_setting2": [81, 18, 0, 0, 0, 0, 163],
    "peakflow_setting3": [81, 19, 0, 0, 0, 0, 163],
    "set_peakflow_setting1": [81, 64, 0, 0, 0, 0, 163],
    "set_peakflow_setting2": [81, 20, 0, 0, 0, 0, 163],
    "set_peakflow_setting3": [81, 17, 0, 0, 0, 0, 163],
    "peakflow_record1": [81, 21, 0, 0, 0, 0, 163],
    "peakflow_record2": [81, 22, 0, 0, 0, 0, 163],
    "peakflow_record3": [81, 23, 0, 0, 0, 0, 163],
    "peakflow_record4": [81, 24, 0, 0, 0, 0, 163],
    "peakflow_record5": [81, 25, 0, 0, 0, 0, 163],
    # cmdSetWsUserProfile uses DimensionsKt.LDPI (120) as cmd in decompile — long payload
    "set_ws_user_profile": [81, 120, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 163],
}

METER_MODELS = (
    "TD3223", "TD3252", "TD3258", "TD3261", "TD3250",
    "TD4256", "TD4257", "TD4272", "TD4127", "TD4282", "TD4283", "TD4279",
    "TD4255", "TD4240", "TD4230",
    "TD3132", "TD3128", "TD3129", "TD3140", "TD3280",
    "TD1261", "TD1035",
    "TD8002", "TD8201", "TD8255",
    "TD2500", "TD2501", "TD2551", "TD2552", "TD2555", "TD2599",
)

DISPLAY_DEVICES = (
    "FORA 6 CONNECT", "FORA D40", "FORA GD40", "FORA IR20", "FORA IR42",
    "FORA MD", "FORA MD6", "FORA O2", "FORA P20", "FORA P30 PLUS", "FORA P80",
    "FORA PREMIUM V10", "FORA Spiro10", "FORA Spiro20", "FORA W550", "FORA W600",
    "TNG", "TNG ADVANCE", "TNG BP", "TNG SCALE", "TNG SPO2",
    "TNG VITA", "TNG VITA VOICE", "TNG VOICE",
    "DIAMOND CUFF BP", "SootheNeb NBL300",
)

# Import / UI message codes (iMeterConnService)
MSG_CONNECT_FAIL = 1
MSG_DISCONNECTED = 2
MSG_RECONNECT = 3
MSG_DISCOVER_SERVICES = 4
MSG_DISCOVER_FAIL = 5
MSG_DISCOVER_SUCCESS = 6
MSG_CONNECT_OVER_TIME = 7
MSG_SCAN_ON = 8
MSG_SCAN_TIMEOUT = 9
MSG_SCAN_RESULT = 16
MSG_SCAN_FAIL = 17

MESSAGE_STATE_READY_START_IMPORT = 1
MESSAGE_STATE_NEED_TO_PAIR_DEVICE = 2
MESSAGE_STATE_UNKNOWN_EXCEPTION = 3
MESSAGE_STATE_NEED_TO_SELECT_ONE_METER_TO_IMPORT = 4
MESSAGE_STATE_CONNECTED_METER_NOT_CORRECT = 5
MESSAGE_STATE_CONNECTED_METER_NOT_PAIRED_WITH_ANDROID_PHONE = 6
MESSAGE_STATE_EXCEED_RETRY_TIMES = 7
MESSAGE_STATE_NOT_SUPPORT_METER = 8
MESSAGE_STATE_CONNECTING_METER = 9
MESSAGE_STATE_CONNECT_METER_SUCCESS = 10
MESSAGE_STATE_IMPORTING = 11
MESSAGE_STATE_IMPORT_SUCCESSFUL = 12
MESSAGE_STATE_NO_RECORD_TO_IMPORT = 13
MESSAGE_STATE_COMMUNICATION_TIMEOUT = 14
MESSAGE_STATE_NOT_SUPPORT_BLUETOOTH = 15
MESSAGE_STATE_FINISH_IMPORT = 16
MESSAGE_STATE_BT_NOT_ENABLED_EXCEPTION = 17
MESSAGE_STATE_TURN_OFF_METER = 18
MESSAGE_STATE_GET_METER_SN_FINISH = 19

# BG type codes used as i22 in multiparam decode (BloodGlucoseType ordinals / wire)
BG_TYPE_CODES = {
    0: "General",
    1: "AC",
    2: "PC",
    3: "QC",
    6: "HEMATOCRIT",  # switch cases in onGetRecord2
    7: "KETONE-ish",
    8: "HB-ish",
    9: "UA-ish",
    10: "CHOL-ish",
    12: "multiparam_combo",
    16: "multiparam",
    26: "multiparam",
    32: "multiparam",
    42: "multiparam",
    48: "multiparam",
    52: "multiparam",
    55: "multiparam",
    56: "multiparam",
    57: "multiparam",
    58: "multiparam",
    60: "multiparam",
}


@dataclass(frozen=True)
class ForaTimings:
    scan_window_ms: int = 3000
    connect_timeout_ms: int = 10000  # WorkRequest.MIN_BACKOFF_MILLIS
    after_connect_discover_delay_ms: int = 500
    after_cccd_goto_import_ms: int = 500
    cmd_response_timeout_ms: int = 5000
    set_datetime_timeout_ms: int = 15000
    spo2_poll_timeout_ms: int = 15000  # timeoutHandler onGetNewSpO2 empty
    cmd_max_retries: int = 12
    after_comm_timeout_finish_ms: int = 200
    htc_device_delay_ms: int = 3000
    htc_unpair_pair_delay_ms: int = 2000
    gatt_status_133: int = 133
    main_handler_delays_ms: Tuple[int, ...] = (1000, 2000, 3000)
    ws_frame_lengths: Tuple[int, ...] = (34, 40)


TIMINGS = ForaTimings()

# Companion import order after CCCD (sendStartToBle)
IMPORT_FSM_ORDER = (
    "set_datetime",      # → onSetDateTime → serial_1
    "serial_1",          # → serial_2
    "serial_2",          # → project_code
    "project_code",      # → getDataType; SpO2 may firmware/BLE status/newSpO2 first
    "firmware_optional",
    "record_number",
    "records_loop",      # type-specific: getRecord1/2 or WS or peakflow
    "power_off",
)


def checksum_one_byte(values: Sequence[int], start: int = 0, end_inclusive: Optional[int] = None) -> int:
    if end_inclusive is None:
        end_inclusive = len(values) - 1
    total = 0
    for i in range(start, end_inclusive + 1):
        total += int(values[i]) & 0xFF
    return total & 0xFF


def append_checksum(cmd: Sequence[int]) -> List[int]:
    body = [int(x) & 0xFF for x in cmd]
    body.append(checksum_one_byte(body, 0, len(body) - 1))
    return body


def frame_bytes(cmd: Sequence[int]) -> bytes:
    return bytes(append_checksum(cmd))


def template_named(name: str) -> List[int]:
    t = COMMAND_TEMPLATES[name]
    return list(t)


def cmd_from_template(name: str, **patches: int) -> bytes:
    t = template_named(name)
    for k, v in patches.items():
        if k.startswith("b") and k[1:].isdigit():
            t[int(k[1:])] = int(v) & 0xFF
    return frame_bytes(t)


def template(cmd: int, b2: int = 0, b3: int = 0, b4: int = 0, b5: int = 0, direction: int = DIR_OUT) -> List[int]:
    return [FRAME_START, cmd & 0xFF, b2 & 0xFF, b3 & 0xFF, b4 & 0xFF, b5 & 0xFF, direction & 0xFF]


def cmd_project_code() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["project_code"])


def cmd_record_number(user: int = 0) -> bytes:
    t = list(COMMAND_TEMPLATES["record_number"])
    t[2] = user & 0xFF
    return frame_bytes(t)


def cmd_record_datetime(index: int, user: int = 0) -> bytes:
    t = list(COMMAND_TEMPLATES["record_datetime"])
    t[2] = index & 0xFF
    t[3] = (index >> 8) & 0xFF
    t[5] = user & 0xFF
    return frame_bytes(t)


def cmd_record_value(index: int, user: int = 0) -> bytes:
    t = list(COMMAND_TEMPLATES["record_value"])
    t[2] = index & 0xFF
    t[3] = (index >> 8) & 0xFF
    t[5] = user & 0xFF
    return frame_bytes(t)


def cmd_serial_1() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["serial_1"])


def cmd_serial_2() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["serial_2"])


def cmd_firmware() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["firmware"])


def cmd_power_off() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["power_off"])


def cmd_clear_memory() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["clear_memory"])


def cmd_set_ble_status() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["set_ble_status"])


def cmd_get_new_spo2() -> bytes:
    return frame_bytes(COMMAND_TEMPLATES["get_new_spo2"])


def cmd_ws_read(index: int, project_no: str = "") -> bytes:
    name = "ws_read2" if project_no == "2560" else "ws_read"
    t = list(COMMAND_TEMPLATES[name])
    # readWSMeasureValue: if index>255 then hi in [4], lo in [3]
    if index > 255:
        t[3] = index & 0xFF
        t[4] = (index >> 8) & 0xFF
    else:
        t[3] = index & 0xFF
        t[4] = 0
    return frame_bytes(t)


def cmd_set_datetime(when: Optional[datetime] = None) -> bytes:
    dt = when or datetime.now()
    year, month, day = dt.year, dt.month, dt.day
    hour, minute = dt.hour, dt.minute
    t = list(COMMAND_TEMPLATES["set_datetime"])
    t[2] = (month * 32 + day) & 0xFF
    y = (year - YEAR_BASE) * 2
    if month > 7:
        y += 1
    t[3] = y & 0xFF
    t[4] = minute & 0xFF
    t[5] = hour & 0xFF
    return frame_bytes(t)


def parse_frame(data: bytes | bytearray, *, require_dir_in: bool = False) -> Optional[dict]:
    raw = bytes(data)
    if len(raw) < 8:
        return None
    frame = raw[:8]
    if frame[0] not in (FRAME_START, FRAME_START_WS_ALT):
        return None
    if require_dir_in and frame[6] != DIR_IN:
        return None
    expect = sum(frame[:7]) & 0xFF
    return {
        "start": frame[0],
        "command": frame[1],
        "message": frame[2:6],
        "direction": frame[6],
        "checksum": frame[7],
        "checksum_ok": frame[7] == expect,
        "raw": frame,
        "raw_hex": frame.hex(),
        "app_rx_ok": is_valid_app_rx(raw if len(raw) == 8 else frame),
    }


def is_valid_app_rx(data: bytes | bytearray) -> bool:
    """Match iMeterConnService.dataReceived short-frame predicate."""
    value = bytes(data)
    if len(value) < 8:
        return False
    if value[0] != FRAME_START:
        return False
    if (value[-2] & 0xFF) != DIR_IN:
        return False
    # calculateCheckSum sums all but last
    cs = 0
    for b in value[:-1]:
        cs = (cs + b) & 0xFF
    return (value[-1] & 0xFF) == cs


def project_code_from_response(data: bytes) -> Optional[str]:
    """onGetCodeProject: %02x%02x of (data[3], data[2])."""
    if len(data) < 4:
        return None
    return f"{data[3]:02x}{data[2]:02x}"


def serial_chunk_from_response(data: bytes) -> Optional[str]:
    """onGetSerialNumber1/2: %02x%02x%02x%02x of data[5],data[4],data[3],data[2]."""
    if len(data) < 6:
        return None
    return f"{data[5]:02x}{data[4]:02x}{data[3]:02x}{data[2]:02x}"


def import_type_from_project(project_no: str, *, status_true: bool = True) -> int:
    """Mirror getDataType(status)."""
    p = (project_no or "").lower()
    if len(p) >= 2 and p[:2] == "32":
        return TYPE_MP if status_true else TYPE_BG  # without status flip uses bit later
    if len(p) >= 2 and p[:2] == "73":
        return TYPE_PEAKFLOW
    if len(p) >= 2 and p[:2] == "70":
        return TYPE_NEBULIZER
    if p[:1] == "1":
        return TYPE_TM
    if p[:1] == "2":
        return TYPE_WS
    if p[:1] == "3":
        return TYPE_BP
    if p[:1] == "4":
        return TYPE_BG
    if p[:1] == "8":
        return TYPE_SPO2
    return TYPE_BG


def decode_record_datetime(tmp0: int, tmp1: int, tmp2: int, tmp3: int) -> dict:
    """
    onGetRecord2 datetime from record1 message (tmpByte[0..3]):
      year  = (tmp1 // 2) + 2000
      month = ((tmp0 & 0xE0) // 32) + ((tmp1 & 1) * 8)
      day   = tmp0 & 0x1F
      hour  = tmp3 & 0x1F
      minute= tmp2 & 0x3F
    """
    year = (tmp1 // 2) + YEAR_BASE
    month = ((tmp0 & 0xE0) // 32) + ((tmp1 & 1) * 8)
    day = tmp0 & 0x1F
    hour = tmp3 & 0x1F
    minute = tmp2 & 0x3F
    return {
        "year": year,
        "month": month,
        "day": day,
        "hour": hour,
        "minute": minute,
    }


def decode_bg_meal_flags(flags: int) -> Optional[str]:
    if flags & 0x40:
        return "AC"
    if flags & 0x80:
        return "PC"
    return None


def name_matches_series(name: str) -> bool:
    low = (name or "").lower()
    return any(p in low for p in NAME_PREFIXES)


def response_timeout_ms(status: str) -> int:
    if status in ("set_datetime", "E_Meter_SetDateTime"):
        return TIMINGS.set_datetime_timeout_ms
    if status in ("spo2", "E_Meter_GetNewSpO2"):
        return TIMINGS.spo2_poll_timeout_ms
    return TIMINGS.cmd_response_timeout_ms


def user_no_for_project(project_no: str, default: int = 0) -> int:
    """onGetCodeProject special userNo rules (when no CaseProfile available)."""
    p = (project_no or "").lower()
    if p in ("3280", "3128"):
        return 1
    if p in ("3261", "2560", "3132", "2555"):
        return default  # app uses CaseProfile; without profile keep caller default
    import_type = import_type_from_project(p, status_true=True)
    if import_type == TYPE_WS:
        return -1  # app uses -1 for some WS paths
    return default


def parse_record_count(msg: bytes, import_type: int) -> int:
    """onGetRecordsNumber — WS is LE u16; others decompiled as b0|b1 (no shift)."""
    if not msg or len(msg) < 2:
        return 0
    if import_type == TYPE_WS:
        return int(msg[0]) | (int(msg[1]) << 8)
    # Prefer LE u16 when hi byte looks like count high (safer); also accept OR form
    le = int(msg[0]) | (int(msg[1]) << 8)
    or_form = int(msg[0]) | int(msg[1])
    # Use LE if hi nibble set or value > 255; else both equal when hi=0
    return le if msg[1] != 0 else or_form


def decode_record_pair(
    msg1: bytes,
    msg2: bytes,
    *,
    project_no: str = "",
    import_type: int = TYPE_BG,
) -> dict:
    """
    Combine record1 (datetime msg) + record2 (value msg) into a structured record.
    Reliable from APK for BG and basic BP layout; multiparam type codes noted.
    """
    m1 = bytes(msg1) + b"\x00\x00\x00\x00"
    m2 = bytes(msg2) + b"\x00\x00\x00\x00"
    m1, m2 = m1[:4], m2[:4]
    dt = decode_record_datetime(m1[0], m1[1], m1[2], m1[3])
    itype = import_type
    if (project_no or "")[:2].lower() == "32":
        # after record1, bit7 of msg byte0 (tmpByte[2] in app is record1 data[4] = m1[2])
        # App: tmpByte[2] & 128 → BP vs BG. tmpByte[2] is filled from record1 data[4] = message[2]
        itype = TYPE_BP if (m1[2] & 0x80) else TYPE_BG

    out: dict = {
        "datetime": dt,
        "import_type": itype,
        "project_no": project_no,
        "msg1_hex": m1.hex(),
        "msg2_hex": m2.hex(),
    }

    if itype == TYPE_BP:
        # onGetRecord2 BP path: sys=tmp[4], dia=tmp[6], pulse=tmp[7] (= msg2[0], [2], [3])
        sys_v = float(m2[0])
        dia_v = float(m2[2])
        pulse_v = float(m2[3])
        map_v = float(m2[1]) if m2[1] else None
        # IHB flag: default (tmp[2]&0x40) which is m1[2]
        ihb = bool(m1[2] & 0x40)
        out.update(
            {
                "kind": "bp",
                "systolic": sys_v,
                "diastolic": dia_v,
                "map": map_v,
                "pulse": pulse_v,
                "irregular_pulse": ihb,
                "invalid": sys_v <= 0 or dia_v <= 0,
            }
        )
    else:
        # BG / multiparam primary: LE u16 in msg2[0:2]
        value = int(m2[0]) | (int(m2[1]) << 8)
        meal = int(m2[3])
        out.update(
            {
                "kind": "bg",
                "value_u16": value,
                "meal_flags": meal,
                "meal_tag": decode_bg_meal_flags(meal),
                "invalid": value == BG_INVALID,
                "blood_glucose_mg_dl": None if value == BG_INVALID else float(value),
            }
        )
        # type nibble hints for multiparam (wire type in high bits of some models)
        type_code = int(m2[2])
        out["type_code"] = type_code
        if type_code in (6, 7, 8, 9, 10) and value != BG_INVALID:
            # LibraryEnum-ish secondary: HCT/HB/KT/UA/CHOL ranges — store as note only
            out["secondary_type_hint"] = BG_TYPE_CODES.get(type_code, str(type_code))
    return out


def measured_at_from_dt(dt: dict) -> Optional[datetime]:
    try:
        return datetime(
            int(dt["year"]),
            max(1, min(12, int(dt["month"]))),
            max(1, min(31, int(dt["day"]))),
            max(0, min(23, int(dt["hour"]))),
            max(0, min(59, int(dt["minute"]))),
        )
    except (ValueError, KeyError, TypeError):
        return None


def firmware_version_from_response(data: bytes, import_type: int) -> dict:
    """onGetFirmwareVersion layout."""
    if len(data) < 6:
        return {}
    if import_type == TYPE_SPO2:
        return {
            "firmware": data[5] & 0xFF,
            "extra": data[4] & 0xFF,
        }
    return {"firmware": data[4] & 0xFF, "extra": 0}

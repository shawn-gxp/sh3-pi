"""
Unit tests for pure parsers — no BLE hardware required.

Run:
  python -m medical_ble_toolkit.tests.test_parsers
  # or: python -m pytest medical_ble_toolkit/tests/test_parsers.py
"""

from __future__ import annotations

import sys
import unittest
from pathlib import Path

# Allow running as script from repo root
ROOT = Path(__file__).resolve().parents[2]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from datetime import datetime

from medical_ble_toolkit.common.sfloat import decode_sfloat, encode_sfloat
from medical_ble_toolkit.common.crc import crc8_ccitt
from medical_ble_toolkit.parser import parse
from medical_ble_toolkit.parsers.blood_pressure import parse_blood_pressure_measurement
from medical_ble_toolkit.parsers.mightysat import (
    build_command,
    cmd_get_device_info,
    cmd_get_trend_record,
    cmd_clear_trend_records,
    cmd_configure_streaming,
    deframe,
    parse_device_info,
    parse_trend_record,
    MightySatParser,
)
from medical_ble_toolkit.parsers.thermometer import (
    build_frame,
    checksum,
    encode_clock_data,
    cmd_write_clock,
    cmd_read_storage_count,
    history_pull_commands,
    CMD_READ_STORAGE_RESULT,
    FRAME_START,
    STOP_DEVICE,
)
from medical_ble_toolkit.parsers.and_ua651 import (
    build_custom_command,
    cmd_set_time,
    cmd_disconnect,
    cmd_unpair,
    cmd_delete_all_memory,
    cmd_read_buffer_size,
    encode_date_time_2a08,
    parse_custom_response,
)
from medical_ble_toolkit.parsers.htp import parse_temperature_measurement, decode_float_11073
from medical_ble_toolkit.parsers.nipro_cf import (
    encode_cf_clock,
    racp_number_of_records_all,
    racp_report_all,
    racp_report_from_seq,
    parse_cf_measurement,
    NiproCfParser,
)
from medical_ble_toolkit.parsers.nipro_common import (
    encode_date_time_2a08 as nipro_dt,
    is_invalid_bp_companion,
)
from medical_ble_toolkit.parsers.mightysat import (
    cmd_set_clock_dotnet_ticks,
    cmd_enable_stream_from_device_info,
    cmd_get_device_info,
)
from medical_ble_toolkit.parsers.thermometer import cmd_power_off
from medical_ble_toolkit.profiles import get_profile
from medical_ble_toolkit.parser import get_parser
from medical_ble_toolkit.models import DeviceBrand, ParseError, PressureUnit


class TestSfloat(unittest.TestCase):
    def test_and_example_systolic_128(self):
        # A&D UA-651BLE doc: 128 mmHg → 0x80, 0x00 (SFLOAT, exp=0)
        self.assertEqual(decode_sfloat(bytes([0x80, 0x00])), 128.0)
        # BM54 wire example uses 0x70 0x00 (=112); narrative "117" is explanatory only
        self.assertEqual(decode_sfloat(bytes([0x70, 0x00])), 112.0)
        self.assertEqual(decode_sfloat(encode_sfloat(117.0)), 117.0)

    def test_and_pulse_60(self):
        # A&D: 60/min → 0x3C, 0x00
        self.assertEqual(decode_sfloat(bytes([0x3C, 0x00])), 60.0)

    def test_roundtrip(self):
        for v in (0, 1, 72, 120, 200, 255):
            enc = encode_sfloat(float(v), exponent=0)
            self.assertEqual(decode_sfloat(enc), float(v))


class TestBlpParser(unittest.TestCase):
    def test_beurer_doc_example(self):
        """
        BM54 golden wire (PDF + BLE_PROTOCOL_ANALYSIS): SYS=117 = 0x75 not 0x70.
        1E 75 00 4D 00 00 00 DF 07 01 0E 0A 37 00 48 00 01 00 00
        """
        payload = bytes.fromhex("1E75004D000000DF07010E0A37004800010000")
        r = parse_blood_pressure_measurement(
            payload, brand=DeviceBrand.BEURER, model="BM54"
        )
        self.assertEqual(r.systolic, 117.0)
        self.assertEqual(r.diastolic, 77.0)
        self.assertEqual(r.pulse_rate, 72.0)
        self.assertEqual(r.unit, PressureUnit.MMHG)
        self.assertEqual(r.user_id, 1)
        self.assertEqual(r.user_label, "user2")
        self.assertEqual(r.measured_at, datetime(2015, 1, 14, 10, 55, 0))
        self.assertFalse(r.irregular_pulse)
        self.assertFalse(r.hsd)

    def test_beurer_hsd_and_ihb_bits(self):
        payload = bytearray(bytes.fromhex("1E75004D000000DF07010E0A37004800010000"))
        payload[17] = 0x44  # IHB 0x04 + HSD 0x40
        r = parse_blood_pressure_measurement(payload, brand=DeviceBrand.BEURER)
        self.assertTrue(r.irregular_pulse)
        self.assertTrue(r.hsd)

    def test_short_payload_raises(self):
        with self.assertRaises(ParseError):
            parse_blood_pressure_measurement(b"\x1e\x70")

    def test_facade_profile(self):
        payload = (
            bytes([0x04])  # FLAG_PULSE only (no timestamp / user / status)
            + encode_sfloat(120)
            + encode_sfloat(80)
            + encode_sfloat(93)
            + encode_sfloat(70)
        )
        r = parse(payload, profile="blp")
        self.assertEqual(r.systolic, 120.0)
        self.assertEqual(r.diastolic, 80.0)
        self.assertEqual(r.pulse_rate, 70.0)


class TestMightySat(unittest.TestCase):
    def test_crc_get_device_info_example(self):
        # Doc: [0x77, 0x02, 0x01, 0x07]
        frame = cmd_get_device_info()
        self.assertEqual(frame[0], 0x77)
        self.assertEqual(frame[1], 0x02)
        self.assertEqual(frame[2], 0x01)
        self.assertEqual(frame[3], crc8_ccitt(bytes([0x01])))
        # Doc claims CRC 0x07
        self.assertEqual(frame[3], 0x07)

    def test_deframe_roundtrip(self):
        # Parameter stream stub: id 0x05 + sys_ex 0 + one SpO2 param
        # mask with only SpO2: we'll parse with mask 0x0001
        payload = bytes([
            0x05,
            0x00, 0x00, 0x00, 0x00,  # system exceptions
            0x00,  # param exception
            98,    # SpO2
        ])
        frame = build_command(payload)
        df = deframe(frame)
        self.assertEqual(df.payload, payload)

        parser = MightySatParser(param_mask=0x0001)
        result = parser.parse(frame)
        self.assertEqual(result.spo2, 98)

    def test_bad_crc(self):
        frame = bytearray(cmd_get_device_info())
        frame[-1] ^= 0xFF
        with self.assertRaises(ParseError):
            deframe(frame)


class TestOmronRecord(unittest.TestCase):
    """HEM-7143T1 classic_vital_14 — bytes from omron_bp PARSE_EXAMPLE doc."""

    def test_hem7143t1_example_record(self):
        # Record 0 from live capture: 51 42 51 1A 92 16 BB 14 80 00 5A 00 4F B0
        raw = bytes.fromhex("5142511a9216bb1480005a004fb0")
        from medical_ble_toolkit.parsers.omron import parse_omron_record

        r = parse_omron_record(raw, model="HEM-7143T1")
        self.assertEqual(r.systolic, 81 + 25)  # 106
        self.assertEqual(r.diastolic, 66)
        self.assertEqual(r.pulse_rate, 81)
        self.assertEqual(r.brand.value, "omron")
        self.assertEqual(r.model, "HEM-7143T1")
        self.assertIsNotNone(r.measured_at)

    def test_empty_slot_raises(self):
        from medical_ble_toolkit.parsers.omron import parse_omron_record
        from medical_ble_toolkit.models import ParseError

        with self.assertRaises(ParseError):
            parse_omron_record(b"\xff" * 14, model="HEM-7143T1")


class TestThermometer(unittest.TestCase):
    def test_checksum_and_frame(self):
        f = build_frame(0x24)
        self.assertEqual(len(f), 8)
        self.assertEqual(f[0], FRAME_START)
        self.assertEqual(f[7], checksum(f))

    def test_storage_result_parse(self):
        # Build a synthetic device response for 0x26: object 36.5°C, ambient 25.0°C
        obj = int(365).to_bytes(2, "little")  # 36.5
        amb = int(250).to_bytes(2, "little")  # 25.0
        body = bytes([FRAME_START, CMD_READ_STORAGE_RESULT]) + obj + amb + bytes([STOP_DEVICE])
        frame = body + bytes([checksum(body)])
        r = parse(frame, profile="thermometer")
        self.assertAlmostEqual(r.object_temperature, 36.5)
        self.assertAlmostEqual(r.ambient_temperature, 25.0)

    def test_write_clock_and_history_cmds(self):
        clock = cmd_write_clock(datetime(2026, 7, 15, 12, 30, 0))
        self.assertEqual(len(clock), 8)
        self.assertEqual(clock[1], 0x33)
        self.assertEqual(checksum(clock[:7]), clock[7])
        data = encode_clock_data(datetime(2026, 7, 15, 12, 30, 0))
        self.assertEqual(len(data), 4)
        # history: 3 records → 6 frames (time+result each)
        frames = history_pull_commands(3, max_records=10)
        self.assertEqual(len(frames), 6)
        self.assertEqual(cmd_read_storage_count()[1], 0x2B)


class TestAndUa651Custom(unittest.TestCase):
    """SDK §5.1 golden vectors."""

    def test_set_time_example_2014(self):
        # Doc: 2014/3/11 19:12:34 → 08 01 01 0E 03 0B 13 0C 22
        pkt = cmd_set_time(datetime(2014, 3, 11, 19, 12, 34))
        self.assertEqual(len(pkt), 20)
        self.assertEqual(pkt[:9], bytes([0x08, 0x01, 0x01, 0x0E, 0x03, 0x0B, 0x13, 0x0C, 0x22]))

    def test_disconnect_unpair_delete(self):
        self.assertEqual(cmd_disconnect()[:3], bytes([0x02, 0x01, 0x03]))
        self.assertEqual(cmd_unpair()[:3], bytes([0x02, 0x01, 0x10]))
        self.assertEqual(cmd_delete_all_memory()[:3], bytes([0x02, 0x01, 0x12]))
        self.assertEqual(cmd_read_buffer_size()[:3], bytes([0x02, 0x00, 0xD6]))

    def test_date_time_2a08(self):
        raw = encode_date_time_2a08(datetime(2015, 1, 14, 10, 55, 0))
        self.assertEqual(len(raw), 7)
        year = raw[0] | (raw[1] << 8)
        self.assertEqual(year, 2015)
        self.assertEqual(list(raw[2:]), [1, 14, 10, 55, 0])

    def test_parse_custom_response(self):
        # Response example: 03 02 D6 01
        resp = build_custom_command(0xD6, type_=0x02, value=bytes([0x01]))
        parsed = parse_custom_response(resp)
        self.assertEqual(parsed["command"], 0xD6)
        self.assertEqual(parsed["msg_type"], "response")


class TestMightySatExtended(unittest.TestCase):
    def test_reassemble_split_device_info(self):
        """Live log: GetDeviceInfo arrived as 20 + 2 bytes (ATT fragmentation)."""
        from medical_ble_toolkit.parsers.mightysat import FrameReassembler

        # Build a real framed device-info (payload 19 bytes after msg id layout)
        payload = bytes(
            [0x01]
            + [0x00, 0x10]
            + [0x1F, 0x00]
            + [0x03]
            + [0x00, 0x10]
            + [0x17, 0x00]
            + [0x02]
            + [0x01, 0x00, 0x00, 0x00]
            + [0x02, 0x00, 0x00, 0x00]
        )
        full = build_command(payload)
        self.assertGreater(len(full), 20)
        reasm = FrameReassembler()
        self.assertEqual(reasm.feed(full[:20]), [])
        frames = reasm.feed(full[20:])
        self.assertEqual(len(frames), 1)
        self.assertEqual(frames[0], full)
        info = MightySatParser().parse(frames[0])
        self.assertEqual(info["type"], "device_info")

    def test_reassemble_user_log_continuation_chunks(self):
        """Exact user log: mid-frame ATT then continuation with SpO2; device info 20+2."""
        # packet #2 / #3 style from live session (SOM left at end of prior ATT)
        p2 = bytes.fromhex(
            "11 04 28 BD 00 B8 64 B5 00 C4 00 DC 00 E8 00 E5 00 39 77 11"
        )
        p3 = bytes.fromhex(
            "05 00 00 00 00 00 61 00 52 00 1D 00 66 03 00 0F 27 77 11 04"
        )
        parser = MightySatParser(param_mask=0x001F)
        self.assertEqual(parser.feed(p2), [])  # hold 0x77 0x11 — no Bad SOM raise
        r = parser.feed(p3)
        self.assertTrue(any(getattr(x, "spo2", None) == 97 for x in r), r)

        p8 = bytes.fromhex(
            "77 14 01 44 10 1F 00 03 01 00 17 00 07 02 00 00 00 09 00 00"
        )
        p9 = bytes.fromhex("00 E8")
        parser2 = MightySatParser()
        self.assertEqual(parser2.feed(p8), [])
        info = parser2.feed(p9)
        self.assertEqual(len(info), 1)
        self.assertEqual(info[0]["type"], "device_info")

    def test_reassemble_coalesced_waveform_plus_params(self):
        """Live log: waveform ATT ends with next frame's SOM; next notif lacks 0x77."""
        # SpO2-only param frame for simple mask
        param_payload = bytes([0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 98])
        param_frame = build_command(param_payload)
        # Minimal waveform: id + ordinal + one sample (pleth, siq)
        wave_payload = bytes([0x04, 0x01, 0x10, 0x00])
        wave_frame = build_command(wave_payload)

        # Coalesce: full wave + first byte of param (SOM) in notif A
        part_a = wave_frame + param_frame[:1]
        part_b = param_frame[1:]

        parser = MightySatParser(param_mask=0x0001)
        r1 = parser.feed(part_a)
        self.assertEqual(len(r1), 1)
        self.assertEqual(r1[0].ordinal, 1)
        r2 = parser.feed(part_b)
        self.assertEqual(len(r2), 1)
        self.assertEqual(r2[0].spo2, 98)

    def test_configure_streaming_doc_example(self):
        # Doc: [0x77, 0x05, 0x03, 0x1F, 0x00, 0x03, 0xD6]
        frame = cmd_configure_streaming(0x001F, 0x03)
        self.assertEqual(list(frame), [0x77, 0x05, 0x03, 0x1F, 0x00, 0x03, 0xD6])

    def test_clear_trend_doc_example(self):
        # Doc: [0x77, 0x02, 0x07, 0x15]
        frame = cmd_clear_trend_records()
        self.assertEqual(list(frame), [0x77, 0x02, 0x07, 0x15])

    def test_get_trend_session_9b(self):
        # Doc example session 0x9B → [0x77, 0x06, 0x06, 0x9B, 0x00, 0x00, 0x00, 0x97]
        frame = cmd_get_trend_record(0x9B)
        self.assertEqual(list(frame), [0x77, 0x06, 0x06, 0x9B, 0x00, 0x00, 0x00, 0x97])

    def test_parse_device_info_and_trend(self):
        # Synthetic 19-byte device info payload
        payload = bytes(
            [0x01]
            + [0x00, 0x10]  # sw
            + [0x1F, 0x00]  # params
            + [0x03]  # waves
            + [0x00, 0x10]  # trend ver
            + [0x17, 0x00]  # trended
            + [0x02]  # 2 records
            + [0x01, 0x00, 0x00, 0x00]  # oldest
            + [0x02, 0x00, 0x00, 0x00]  # current
        )
        info = parse_device_info(payload)
        self.assertEqual(info["trend_record_count"], 2)
        self.assertEqual(info["oldest_trend_session_id"], 1)
        self.assertEqual(info["current_trend_session_id"], 2)

        # Trend: id + session + duration + ts + spo2 + pr blocks
        trend = bytes(
            [0x06]
            + [0x01, 0x00, 0x00, 0x00]
            + [0x3C, 0x00, 0x00, 0x00]  # 60s
            + [0x59, 0x98, 0x6A, 0x5F]  # unix
            + [90, 100, 95, 98]  # spo2 min/max/avg/last
            + [60, 80, 70, 72]  # pr
        )
        rec = parse_trend_record(trend, trended_mask=0x0003)
        self.assertEqual(rec["session_id"], 1)
        self.assertEqual(rec["params"]["spo2"]["last"], 98)
        self.assertEqual(rec["params"]["pr"]["avg"], 70)


class TestHtp(unittest.TestCase):
    def test_celsius_no_optional(self):
        # flags 0, FLOAT 36.5 °C → mantissa 365 exp -1
        mant = 365
        exp = (-1) & 0xFF
        float_le = (mant | (exp << 24)).to_bytes(4, "little")
        payload = bytes([0x00]) + float_le
        r = parse_temperature_measurement(payload)
        self.assertAlmostEqual(r.object_temperature, 36.5, places=2)
        self.assertEqual(r.model, "HTP")


class TestNiproCompanion(unittest.TestCase):
    """げんきノート BLELib parity helpers."""

    def test_profiles_resolve(self):
        for pid in (
            "nipro_nbp",
            "nipro_nmbp",
            "nipro_nsm1",
            "nipro_nt100b",
            "nipro_cf",
            "nt100b",
            "nbp",
            "cocoron",
        ):
            p = get_profile(pid)
            self.assertTrue(p.id.startswith("nipro_") or p.id == "nipro_nt100b")
            get_parser(p.parser_key)  # must construct

    def test_nt100b_alias_is_companion_not_ticd(self):
        p = get_profile("nt100b")
        self.assertEqual(p.id, "nipro_nt100b")
        self.assertEqual(p.parser_key, "nipro_nt100b")

    def test_bp_clock_and_power_off(self):
        raw = nipro_dt(datetime(2026, 7, 16, 14, 30, 5))
        self.assertEqual(raw, bytes([0xEA, 0x07, 0x07, 0x10, 0x0E, 0x1E, 0x05]))
        po = cmd_power_off()
        self.assertEqual(list(po), [0x51, 0x50, 0x00, 0x00, 0x00, 0x00, 0xA3, 0x44])

    def test_cf_racp_and_clock(self):
        self.assertEqual(list(racp_number_of_records_all()), [0x04, 0x01])
        self.assertEqual(list(racp_report_all()), [0x01, 0x01])
        self.assertEqual(list(racp_report_from_seq(12)), [0x01, 0x03, 0x01, 0x0C, 0x00])
        clk = encode_cf_clock(datetime(2026, 7, 16, 10, 0, 0))
        self.assertEqual(len(clk), 7)
        self.assertEqual(clk[0], 0xEA)
        self.assertEqual(clk[1], 0x07)

    def test_cf_measurement_parse(self):
        # seq=5, date 2026-07-16 08:00:00, sfloat 0.0012 kg/L → *1e5 = 120 mg/dL
        from medical_ble_toolkit.common.sfloat import encode_sfloat

        sfloat = encode_sfloat(0.0012, exponent=-4)  # mant 12, exp -4
        payload = bytearray(15)
        payload[1] = 5
        payload[2] = 0
        payload[3] = 0xEA
        payload[4] = 0x07
        payload[5] = 7
        payload[6] = 16
        payload[7] = 8
        payload[8] = 0
        payload[9] = 0
        payload[12] = sfloat[0]
        payload[13] = sfloat[1]
        payload[14] = 0x00
        rec = parse_cf_measurement(payload)
        self.assertEqual(rec.sequence, 5)
        self.assertEqual(rec.measured_at.year, 2026)
        self.assertFalse(rec.is_control_solution)
        self.assertAlmostEqual(rec.concentration_mg_dl or 0, 120.0, places=0)

    def test_cf_control_solution_skipped_flag(self):
        from medical_ble_toolkit.common.sfloat import encode_sfloat

        sfloat = encode_sfloat(0.0012, exponent=-4)
        payload = bytearray(15)
        payload[1] = 1
        payload[3] = 0xEA
        payload[4] = 0x07
        payload[5] = 1
        payload[6] = 1
        payload[12] = sfloat[0]
        payload[13] = sfloat[1]
        payload[14] = 0x0A
        rec = parse_cf_measurement(payload)
        self.assertTrue(rec.is_control_solution)

    def test_mightysat_companion_cmds(self):
        info = cmd_get_device_info()
        self.assertEqual(list(info), [0x77, 0x02, 0x01, 0x07])
        ticks = cmd_set_clock_dotnet_ticks(datetime(2020, 1, 1))
        self.assertEqual(ticks[0], 0x77)
        self.assertEqual(ticks[2], 0x02)
        # EnableStream from synthetic device info
        dev = bytes([0x01, 0, 0, 0x1F, 0x00, 0x03] + [0] * 13)
        stream = cmd_enable_stream_from_device_info(dev)
        self.assertEqual(stream[2], 0x03)
        self.assertEqual(list(stream[3:6]), [0x1F, 0x00, 0x03])

    def test_bp_sentinel(self):
        self.assertTrue(
            is_invalid_bp_companion(2047.0, 80.0, 70.0, datetime.now())
        )
        self.assertFalse(
            is_invalid_bp_companion(120.0, 80.0, 70.0, datetime.now())
        )

    def test_nt100b_htp_companion_style(self):
        from medical_ble_toolkit.parsers.nipro_nt100b import (
            parse_htp_companion_style,
            Nt100bCompanionParser,
        )

        # FLOAT 36.5°C: mant 365 exp -1 → 6D 01 00 FF
        # Companion: SFLOAT(6D,01)=365 * 10^(-1) = 36.5
        payload = bytes(
            [0x02]  # flags timestamp
            + [0x6D, 0x01, 0x00, 0xFF]
            + [0xEA, 0x07, 7, 16, 12, 0, 0]  # pad to >=12
        )
        r = parse_htp_companion_style(payload)
        self.assertAlmostEqual(r.object_temperature, 36.5, places=1)
        self.assertEqual(r.model, "NT-100B")

        p = Nt100bCompanionParser()
        self.assertTrue(
            p.can_parse(payload, "00002a1c-0000-1000-8000-00805f9b34fb")
        )
        r2 = p.parse(payload, "00002a1c-0000-1000-8000-00805f9b34fb")
        self.assertAlmostEqual(r2.object_temperature, 36.5, places=1)


if __name__ == "__main__":
    unittest.main()

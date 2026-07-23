"""
FORA / TaiDoc protocol unit tests — no BLE hardware.

Run:
  python -m pytest medical_ble_toolkit/tests/test_fora_protocol.py -q
  python -m medical_ble_toolkit.tests.test_fora_protocol
"""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from medical_ble_toolkit.brands.fora import protocol as P
from medical_ble_toolkit.parsers.fora import ForaParser


class TestForaProtocol(unittest.TestCase):
    def test_checksum_and_project_frame(self):
        tx = P.cmd_project_code()
        self.assertEqual(len(tx), 8)
        self.assertEqual(tx[0], 0x51)
        self.assertEqual(tx[1], 0x24)
        self.assertEqual(tx[6], 0xA3)
        self.assertEqual(tx[7], sum(tx[:7]) & 0xFF)

    def test_all_templates_frame(self):
        for name in P.COMMAND_TEMPLATES:
            if name == "set_ws_user_profile":
                # long template — still checksum appends one byte
                raw = P.frame_bytes(P.COMMAND_TEMPLATES[name])
                self.assertGreaterEqual(len(raw), 8)
                continue
            raw = P.cmd_from_template(name)
            self.assertEqual(raw[0], 0x51, name)
            self.assertEqual(raw[-1], sum(raw[:-1]) & 0xFF, name)

    def test_rx_validation_requires_a5(self):
        tx = P.cmd_project_code()
        bad = bytearray(tx)
        # still has A3 as dir — not valid app RX
        self.assertFalse(P.is_valid_app_rx(bad))
        good = bytearray(tx)
        good[6] = 0xA5
        good[7] = sum(good[:7]) & 0xFF
        self.assertTrue(P.is_valid_app_rx(good))

    def test_set_datetime_packing(self):
        from datetime import datetime

        dt = datetime(2024, 8, 15, 14, 30)
        frame = P.cmd_set_datetime(dt)
        # month 8 > 7 → year byte has +1
        self.assertEqual(frame[2], (8 * 32 + 15) & 0xFF)
        self.assertEqual(frame[4], 30)
        self.assertEqual(frame[5], 14)

    def test_record_index_packing(self):
        frame = P.cmd_record_value(0x123, user=1)
        self.assertEqual(frame[2], 0x23)
        self.assertEqual(frame[3], 0x01)
        self.assertEqual(frame[5], 1)

    def test_import_type_map(self):
        self.assertEqual(P.import_type_from_project("4272"), P.TYPE_BG)
        self.assertEqual(P.import_type_from_project("3140"), P.TYPE_BP)
        self.assertEqual(P.import_type_from_project("3250"), P.TYPE_MP)
        self.assertEqual(P.import_type_from_project("8201"), P.TYPE_SPO2)
        self.assertEqual(P.import_type_from_project("2555"), P.TYPE_WS)
        self.assertEqual(P.import_type_from_project("1261"), P.TYPE_TM)
        self.assertEqual(P.import_type_from_project("7301"), P.TYPE_PEAKFLOW)

    def test_user_no_special(self):
        self.assertEqual(P.user_no_for_project("3280"), 1)
        self.assertEqual(P.user_no_for_project("3128"), 1)

    def test_serial_and_project_parse(self):
        # craft fake 8-byte response
        body = [0x51, 0x28, 0x01, 0x02, 0x03, 0x04, 0xA5]
        body.append(sum(body) & 0xFF)
        raw = bytes(body)
        self.assertEqual(P.serial_chunk_from_response(raw), "04030201")
        body2 = [0x51, 0x24, 0x50, 0x32, 0x00, 0x00, 0xA5]
        body2.append(sum(body2) & 0xFF)
        self.assertEqual(P.project_code_from_response(bytes(body2)), "3250")

    def test_datetime_decode_roundtrip_bits(self):
        # year 2024 → (2024-2000)*2 = 48 = 0x30 if month<=7
        # month 3 day 10 → 3*32+10 = 106 = 0x6A
        dt = P.decode_record_datetime(0x6A, 0x30, 0x15, 0x0A)
        self.assertEqual(dt["year"], 2024)
        self.assertEqual(dt["month"], 3)
        self.assertEqual(dt["day"], 10)
        self.assertEqual(dt["hour"], 10)
        self.assertEqual(dt["minute"], 21)

    def test_decode_record_pair_bg(self):
        m1 = bytes([0x6A, 0x30, 0x15, 0x0A])
        # value 120 LE, meal AC 0x40
        m2 = bytes([120, 0, 0, 0x40])
        rec = P.decode_record_pair(m1, m2, project_no="4272", import_type=P.TYPE_BG)
        self.assertEqual(rec["kind"], "bg")
        self.assertEqual(rec["blood_glucose_mg_dl"], 120.0)
        self.assertEqual(rec["meal_tag"], "AC")
        self.assertFalse(rec["invalid"])

    def test_decode_record_pair_bp(self):
        m1 = bytes([0x6A, 0x30, 0x00, 0x0A])
        m2 = bytes([120, 90, 80, 72])  # sys, map?, dia, pulse
        rec = P.decode_record_pair(m1, m2, project_no="3140", import_type=P.TYPE_BP)
        self.assertEqual(rec["kind"], "bp")
        self.assertEqual(rec["systolic"], 120.0)
        self.assertEqual(rec["diastolic"], 80.0)
        self.assertEqual(rec["pulse"], 72.0)

    def test_parser_bg_frame(self):
        body = [0x51, 0x26, 120, 0, 0, 0x40, 0xA5]
        body.append(sum(body) & 0xFF)
        r = ForaParser().parse(bytes(body))
        self.assertEqual(r.blood_glucose_mg_dl, 120.0)
        self.assertEqual(r.meal_tag, "AC")

    def test_name_match(self):
        self.assertTrue(P.name_matches_series("FORA 6 CONNECT"))
        self.assertTrue(P.name_matches_series("TD-4277"))
        self.assertTrue(P.name_matches_series("TNG BP"))
        self.assertFalse(P.name_matches_series("BM54"))

    def test_plugin_registers(self):
        from medical_ble_toolkit.core.registry import get_plugin, has_plugin
        import medical_ble_toolkit.brands.fora.plugin  # noqa: F401

        self.assertTrue(has_plugin("fora"))
        p = get_plugin("fora")
        self.assertEqual(p.brand_id, "fora")
        self.assertTrue(p.matches_advertisement("fora d40"))


if __name__ == "__main__":
    unittest.main()

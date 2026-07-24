"""Offline tests for Nipro NC-1BLE protocol helpers (no BLE hardware)."""

from __future__ import annotations

import unittest
from datetime import datetime

from medical_ble_toolkit.parsers import nipro_nc1 as nc1


class TestNc1Codec(unittest.TestCase):
    def test_config_normal_60s(self) -> None:
        cfg = nc1.encode_config(
            free_run=False, interval_prefs=0, host_id="abcd1234"
        )
        self.assertEqual(len(cfg), 10)
        self.assertEqual(cfg[0], 0)
        self.assertEqual(cfg[1], 3)  # companion wire map
        self.assertEqual(cfg[2:10], b"abcd1234")

    def test_config_free_run_5s(self) -> None:
        cfg = nc1.encode_config(
            free_run=True, interval_prefs=3, host_id="xyz"
        )
        self.assertEqual(cfg[0], 0x02)
        self.assertEqual(cfg[1], 0)
        self.assertEqual(cfg[2:5], b"xyz")

    def test_datetime(self) -> None:
        dt = nc1.encode_datetime(datetime(2026, 7, 24, 12, 30, 45))
        self.assertEqual(len(dt), 7)
        self.assertEqual(dt[0] | (dt[1] << 8), 2026)
        self.assertEqual(list(dt[2:]), [7, 24, 12, 30, 45])

    def test_interval_maps(self) -> None:
        self.assertEqual(nc1.prefs_to_wire_interval(0), 3)
        self.assertEqual(nc1.prefs_to_wire_interval(3), 0)
        self.assertEqual(nc1.seconds_to_prefs_index(10), 2)
        self.assertEqual(nc1.seconds_to_prefs_index(60), 0)

    def test_battery(self) -> None:
        bat = nc1.parse_battery(bytes([0x04, 0x0B, 1, 2, 3]))
        self.assertEqual(bat.level_mv, 0x0B04)
        self.assertEqual(bat.major_ver, 1)
        self.assertFalse(bat.low_battery)

    def test_rrt_hr(self) -> None:
        # 1000 ms RR → 60 bpm
        r = nc1.parse_rrt(bytes([0x00, 0xE8, 0x03, 0x03]))
        self.assertEqual(r.rr_ms, 1000)
        self.assertEqual(r.heart_rate_bpm, 60.0)
        self.assertFalse(r.leads_off)

    def test_rrt_leads_off(self) -> None:
        r = nc1.parse_rrt(bytes([0x20, 0xE8, 0x03, 0x03]))
        self.assertTrue(r.leads_off)

    def test_huffman_table_loads(self) -> None:
        table = nc1.load_huffman_table()
        self.assertEqual(len(table), 4096)

    def test_parser_dispatches_by_uuid(self) -> None:
        p = nc1.NiproNc1Parser()
        bat = p.parse(
            bytes([0x04, 0x0B, 1, 0, 0]),
            characteristic_uuid=nc1.CHAR_BATTERY,
        )
        self.assertEqual(bat.level_mv, 0x0B04)
        rri = p.parse(
            bytes([0x00, 0xE8, 0x03, 0x03]),
            characteristic_uuid=nc1.CHAR_RRT,
        )
        self.assertEqual(rri.rr_ms, 1000)

    def test_profile_registered(self) -> None:
        from medical_ble_toolkit.profiles import get_profile
        from medical_ble_toolkit.parser import get_parser

        prof = get_profile("nipro_nc1")
        self.assertEqual(prof.service_uuid, nc1.SVC_COCORON)
        parser = get_parser("nipro_nc1")
        self.assertEqual(parser.name, "nipro_nc1")

    def test_infer_name(self) -> None:
        from medical_ble_toolkit.brands.nipro.registry import infer_profile_from_name

        self.assertEqual(infer_profile_from_name("NC-1BLE-1234"), "nipro_nc1")
        self.assertEqual(infer_profile_from_name("NIPRO CF 01"), "nipro_cf")


if __name__ == "__main__":
    unittest.main()

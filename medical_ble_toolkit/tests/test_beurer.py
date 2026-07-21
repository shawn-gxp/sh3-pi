"""Beurer multi-device catalog, timing, and parsers (no BLE)."""

from __future__ import annotations

import unittest

from medical_ble_toolkit.brands.beurer.catalog import (
    get_device,
    list_devices,
    match_advertisement_name,
    supported_summary,
)

from medical_ble_toolkit.parsers.beurer_ft import parse_beurer_ft_frame
from medical_ble_toolkit.parsers.beurer_po60 import cmd_request_more, parse_po60_record
from medical_ble_toolkit.parsers.glucose import racp_report_all, racp_report_ge_sequence
from medical_ble_toolkit.parsers.beurer_scale import cmd_get_measurement


class TestBeurerCatalog(unittest.TestCase):
    def test_loads_without_ocr_profile(self):
        devices = list_devices()
        self.assertGreaterEqual(len(devices), 100)
        self.assertIn("Beurer models", supported_summary())
        # OCR-only not a protocol profile entry
        self.assertTrue(all(d.protocol_profile != "ocr_fallback" for d in devices))

    def test_bp_and_glucose(self):
        bm = get_device("BM54")
        self.assertIsNotNone(bm)
        assert bm is not None
        self.assertEqual(bm.toolkit_profile, "beurer_bp")
        gl = get_device("GL50")
        self.assertIsNotNone(gl)
        assert gl is not None
        self.assertEqual(gl.toolkit_profile, "beurer_glucose")

    def test_match_adv_name(self):
        d = match_advertisement_name("BM54")
        self.assertIsNotNone(d)
        assert d is not None
        self.assertEqual(d.id, "BM54")


class TestBeurerTiming(unittest.TestCase):
    def test_bm59_pulse_swap_not_te(self):
        # APK: BM54 has te (3s settle); BM59 has t6 (pulse swap), not te
        from medical_ble_toolkit.brands.beurer.capabilities import get_capabilities

        c54 = get_capabilities("BM54")
        c59 = get_capabilities("BM59")
        self.assertTrue(c54.settle_3s)
        self.assertFalse(c59.settle_3s)
        self.assertTrue(c59.pulse_swapped)
        self.assertTrue(c59.set_time)
        self.assertEqual(c54.quiet_timeout_s, 4.0)

    def test_glucose_long_racp_mg_marker(self):
        from medical_ble_toolkit.brands.beurer.capabilities import get_capabilities

        # only GL49/GL60 have mg in APK extract
        self.assertTrue(get_capabilities("GL60").glucose_long_racp)
        self.assertFalse(get_capabilities("GL50").glucose_long_racp)
        self.assertEqual(get_capabilities("GL60").before_racp_s, 2.5)


class TestBeurerParsers(unittest.TestCase):
    def test_ft_13_byte(self):
        # Synthetic: 36.5 C, exp -1, mant 365, year 2024 BE, forehead
        mant = 365
        exp = (-1) & 0xFF
        year = 2024
        frame = bytes(
            [
                0x00,  # C
                mant & 0xFF,
                (mant >> 8) & 0xFF,
                (mant >> 16) & 0xFF,
                exp,
                (year >> 8) & 0xFF,
                year & 0xFF,
                7,
                15,
                12,
                30,
                0,
                2,  # forehead
            ]
        )
        r = parse_beurer_ft_frame(frame, model="FT95")
        self.assertAlmostEqual(r.object_temperature, 36.5, places=2)

    def test_racp_and_po60_cmds(self):
        self.assertEqual(list(racp_report_all()), [0x01, 0x01])
        self.assertEqual(list(racp_report_ge_sequence(0x1234)), [0x01, 0x03, 0x01, 0x34, 0x12])
        self.assertEqual(list(cmd_request_more()), [0x99, 0x01, 0x1A])
        self.assertEqual(cmd_get_measurement()[0], 0x41)

    def test_po60_record_len(self):
        frame = bytes([0x00, 0x00] + [0] * 22)
        # year 24 → 2024 at offset 2
        b = bytearray(frame)
        b[2] = 24
        b[3] = 7
        b[4] = 15
        b[17] = 98
        b[18] = 90
        b[19] = 95
        r = parse_po60_record(bytes(b))
        self.assertEqual(r.spo2_avg, 95)


class TestBeurerStability(unittest.TestCase):
    def test_dedup_bp(self):
        from medical_ble_toolkit.brands.beurer.dedup import dedupe_readings, bp_dedup_key
        from medical_ble_toolkit.parsers.blood_pressure import (
            parse_blood_pressure_measurement,
        )
        from medical_ble_toolkit.models import DeviceBrand

        payload = bytes.fromhex("1E75004D000000DF07010E0A37004800010000")
        a = parse_blood_pressure_measurement(payload, brand=DeviceBrand.BEURER)
        b = parse_blood_pressure_measurement(payload, brand=DeviceBrand.BEURER)
        kept, dropped, keys = dedupe_readings([a, b], key_fn=bp_dedup_key)
        self.assertEqual(len(kept), 1)
        self.assertEqual(dropped, 1)
        self.assertEqual(len(keys), 1)

    def test_sync_classify_pairing(self):
        from medical_ble_toolkit.brands.beurer.sync_result import classify_sync, SyncStatus

        r = classify_sync(
            model_id="BM54",
            address="AA:BB",
            readings=[],
            raw_count=0,
            paired_attempt=True,
            connect_ok=True,
            link_dropped=False,
            auth_error=True,
            passkey_hint=True,
        )
        self.assertEqual(r.status, SyncStatus.PAIRING_REQUIRED)

    def test_mfg_passkey(self):
        from medical_ble_toolkit.brands.beurer.capabilities import mfg_data_suggests_passkey

        self.assertTrue(mfg_data_suggests_passkey({0x0611: bytes([0x01, 0x03])}))
        self.assertFalse(mfg_data_suggests_passkey({0x0611: bytes([0x01])}))


if __name__ == "__main__":
    unittest.main()

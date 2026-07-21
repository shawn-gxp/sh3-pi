"""Unit tests for Nipro pair registry (no BLE hardware)."""

from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

ROOT = Path(__file__).resolve().parents[2]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from medical_ble_toolkit.brands.nipro import registry as reg


class TestNiproRegistry(unittest.TestCase):
    def setUp(self) -> None:
        self._td = tempfile.TemporaryDirectory()
        self._cwd = Path(self._td.name)
        self._patcher = mock.patch.object(reg, "_path", lambda: self._cwd / reg.STORE_NAME)
        self._patcher.start()

    def tearDown(self) -> None:
        self._patcher.stop()
        self._td.cleanup()

    def test_normalize_id(self):
        self.assertEqual(
            reg.normalize_device_id("AA:BB:CC:DD:EE:FF"),
            "aabbccddeeff",
        )
        self.assertEqual(
            reg.normalize_device_id("aabbccdd-eeff-0011-2233-445566778899"),
            "aabbccddeeff00112233445566778899",
        )

    def test_register_and_check_pairing(self):
        m = reg.register_meter(
            device_id="AA:BB:CC:DD:EE:FF",
            name="NBP-1BLE-1234",
            profile_id="nipro_nbp",
            serial="SN1",
        )
        self.assertEqual(m.category, "bp")
        self.assertTrue(reg.check_pairing("AA:BB:CC:DD:EE:FF"))
        self.assertTrue(reg.check_pairing("aabbccddeeff"))
        self.assertFalse(reg.check_pairing("11:22:33:44:55:66"))
        self.assertEqual(reg.find_by_name("NBP-1BLE-1234").serial, "SN1")

    def test_one_per_category_replace(self):
        reg.register_meter(
            device_id="AA:BB:CC:DD:EE:01",
            name="NBP-1BLE-A",
            profile_id="nipro_nbp",
        )
        reg.register_meter(
            device_id="AA:BB:CC:DD:EE:02",
            name="NBP-1BLE-B",
            profile_id="nipro_nbp",
        )
        meters = reg.list_meters()
        bp = [x for x in meters if x.category == "bp"]
        self.assertEqual(len(bp), 1)
        self.assertEqual(bp[0].name, "NBP-1BLE-B")

    def test_handsfree_names(self):
        reg.register_meter(
            device_id="AA:BB:CC:DD:EE:01",
            name="NBP-1BLE-X",
            profile_id="nipro_nbp",
        )
        reg.register_meter(
            device_id="AA:BB:CC:DD:EE:02",
            name="NT-100B-Y",
            profile_id="nipro_nt100b",
        )
        names = reg.handsfree_name_list(["bp", "ht"])
        self.assertEqual(set(names), {"NBP-1BLE-X", "NT-100B-Y"})
        self.assertEqual(reg.handsfree_name_list(["gl"]), [])

    def test_infer_profile(self):
        self.assertEqual(reg.infer_profile_from_name("NBP-1BLE-99"), "nipro_nbp")
        self.assertEqual(reg.infer_profile_from_name("NMBP001"), "nipro_nmbp")
        self.assertEqual(reg.infer_profile_from_name("NSM-1BLE"), "nipro_nsm1")
        self.assertEqual(reg.infer_profile_from_name("NT-100B"), "nipro_nt100b")
        self.assertEqual(reg.infer_profile_from_name("NIPRO CF 01"), "nipro_cf")
        self.assertEqual(reg.infer_profile_from_name("MightySatRx"), "mightysat")

    def test_delete(self):
        reg.register_meter(
            device_id="AA:BB:CC:DD:EE:01",
            name="NBP-1BLE-X",
            profile_id="nipro_nbp",
        )
        n = reg.delete_meter(category="bp")
        self.assertEqual(n, 1)
        self.assertEqual(reg.list_meters(), [])


if __name__ == "__main__":
    unittest.main()

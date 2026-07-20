"""Hub roster AD match — MAC-strict (no brand steal)."""

from __future__ import annotations

import sys
import types
import unittest
from pathlib import Path
from unittest.mock import MagicMock

ROOT = Path(__file__).resolve().parents[2]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from medical_ble_toolkit.hub.daemon import HubDaemon
from medical_ble_toolkit.hub.config import HubConfig


def _adv(mac: str, name: str = ""):
    d = types.SimpleNamespace()
    d.address = mac
    d.name = name
    return d


class TestMacStrictMatch(unittest.TestCase):
    def setUp(self) -> None:
        self.hub = HubDaemon(
            get_roster=lambda: [],
            run_session=MagicMock(),
            config=HubConfig(),
            reload_config_each_round=False,
        )

    def test_matches_own_mac_only(self):
        roster = [
            {
                "mac": "AA:BB:CC:DD:EE:01",
                "brand": "nipro_nbp",
                "model": "NBP-1BLE",
                "name": "NBP-1BLE_A",
                "paired": True,
            },
            {
                "mac": "AA:BB:CC:DD:EE:02",
                "brand": "nipro_nbp",
                "model": "NBP-1BLE",
                "name": "NBP-1BLE_B",
                "paired": True,
            },
        ]
        # Only second NBP advertising under first's name — must not steal
        scanned = [_adv("AA:BB:CC:DD:EE:02", "NBP-1BLE_A")]
        hits = self.hub._match_ads(roster, scanned, now=1000.0)
        self.assertEqual(len(hits), 1)
        self.assertEqual(hits[0].mac, "AA:BB:CC:DD:EE:02")

    def test_no_brand_hint_steal(self):
        roster = [
            {
                "mac": "AA:BB:CC:DD:EE:01",
                "brand": "nipro_nbp",
                "model": "NBP-1BLE",
                "name": "NBP-1BLE_A",
                "paired": True,
            },
        ]
        # Different MAC, brand-looking name — must not bind
        scanned = [_adv("FF:FF:FF:FF:FF:FF", "NBP-1BLE_OTHER")]
        hits = self.hub._match_ads(roster, scanned, now=1000.0)
        self.assertEqual(hits, [])

    def test_exact_mac_hit(self):
        roster = [
            {
                "mac": "C0:26:DA:1B:11:46",
                "brand": "nipro_nt100b",
                "model": "NT-100B",
                "name": "NT-100B",
                "paired": True,
            },
        ]
        scanned = [_adv("C0:26:DA:1B:11:46", "NT-100B")]
        hits = self.hub._match_ads(roster, scanned, now=1000.0)
        self.assertEqual(len(hits), 1)
        self.assertEqual(hits[0].mac, "C0:26:DA:1B:11:46")
        self.assertEqual(hits[0].brand, "nipro_nt100b")


class TestConnectQuietWindow(unittest.TestCase):
    def test_any_younger_than(self):
        from medical_ble_toolkit.hub.connection_manager import ConnectionManager
        import asyncio

        mgr = ConnectionManager(max_concurrent=4)

        async def _run() -> None:
            ok = await mgr.try_acquire("AA:BB:CC:DD:EE:01", "omron", "test")
            self.assertTrue(ok)
            self.assertTrue(mgr.any_younger_than(12.0))
            self.assertFalse(mgr.any_younger_than(0.0))
            # Force age past window
            for s in mgr._active.values():
                s.started_mono -= 20.0
            self.assertFalse(mgr.any_younger_than(12.0))
            await mgr.release("AA:BB:CC:DD:EE:01")

        asyncio.run(_run())


if __name__ == "__main__":
    unittest.main()


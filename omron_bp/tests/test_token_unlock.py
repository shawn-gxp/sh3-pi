"""Unit tests for TOKEN_KEY 0x11/0x91 framing (no BLE hardware)."""

from __future__ import annotations

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from omron_bp.models.registry import get_profile
from omron_bp.models.base import UnlockMode


def _is_token_unlock_ack(resp: bytes, token: bytes) -> bool:
    if resp is None or len(resp) < 6:
        return False
    return resp[0] == 0x91 and resp[1] == 0x00 and bytes(resp[2:6]) == token


def _build_token_packet(token: bytes) -> bytes:
    return b"\x11" + token + (b"\x00" * 15)


class TestTokenUnlockFraming(unittest.TestCase):
    def test_phone_capture_session1(self):
        # From omron_gatt_session.log session 1
        # Write:  11 cd 02 37 b7 + zeros
        # Notify: 91 00 cd 02 37 b7 …
        token = bytes.fromhex("cd0237b7")
        packet = _build_token_packet(token)
        self.assertEqual(len(packet), 20)
        self.assertEqual(packet[0], 0x11)
        self.assertEqual(packet[1:5], token)
        self.assertEqual(packet[5:], b"\x00" * 15)

        ack = bytes.fromhex("9100cd0237b7") + b"\x00" * 14
        self.assertTrue(_is_token_unlock_ack(ack, token))

    def test_phone_capture_session2(self):
        token = bytes.fromhex("b7a7fd9f")
        ack = bytes.fromhex("9100b7a7fd9f") + b"\x00" * 10
        self.assertTrue(_is_token_unlock_ack(ack, token))
        self.assertFalse(_is_token_unlock_ack(ack, bytes.fromhex("00000000")))

    def test_hem7143t1_profile_uses_token(self):
        p = get_profile("HEM-7143T1")
        self.assertEqual(p.unlock_mode, UnlockMode.TOKEN_KEY)
        self.assertTrue(p.requires_unlock)


if __name__ == "__main__":
    unittest.main()

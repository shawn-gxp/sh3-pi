"""
Omron BLE memory protocol (TX/RX + EEPROM read/write).

This is the shared "how bytes move" layer used by both classic multi-channel
devices and modern single-channel FE4A devices.

Packet sketch (PC → device read):
  len | type(0100) | address | size | pad | crc(xor-all==0)

Modern FE4A TOKEN unlock (phone HCI / hass-omron / HEM-7143T1):
  host → unlock char:  0x11 + 4-byte nonce + 15 zero pad  (20 bytes, write cmd)
  device → same char:  0x91 0x00 + echo of 4-byte nonce

See omblepy docs / README for full command table.
No model-specific EEPROM addresses live here — pass them in from DeviceProfile.
"""

from __future__ import annotations

import asyncio
import secrets
from typing import List, Optional, Sequence

from bleak import BleakClient

from omron_bp.logging_config import DBG_TAG, get_logger
from omron_bp.models.base import DEFAULT_UNLOCK_KEY, DeviceProfile, UnlockMode

logger = get_logger("ble.transport")


def _hex(data: bytes | bytearray) -> str:
    return bytes(data).hex()


class OmronTransport:
    """Low-level Omron EEPROM transport over an already-connected BleakClient."""

    def __init__(self, client: BleakClient, profile: DeviceProfile) -> None:
        self.client = client
        self.profile = profile
        self.rx_uuids: Sequence[str] = profile.rx_channel_uuids
        self.tx_uuids: Sequence[str] = profile.tx_channel_uuids
        self.unlock_uuid: str = profile.unlock_uuid

        self._notify_on = False
        self._rx_buffers: List[Optional[bytearray]] = [None] * 4
        self._rx_handle_map: dict = {}
        self._rx_done = False
        self._rx_packet_type: Optional[bytearray] = None
        self._rx_eeprom_addr: Optional[bytearray] = None
        self._rx_data: Optional[bytes] = None
        # Unlock channel uses a separate completion path
        self._unlock_done = False
        self._unlock_data: Optional[bytes] = None

        # DBG-LOG
        logger.debug(
            "%s transport init model=%s rx=%s tx=%s",
            DBG_TAG,
            profile.model_id,
            list(self.rx_uuids),
            list(self.tx_uuids),
        )

    # ------------------------------------------------------------------ notify
    def _build_rx_handle_map(self) -> None:
        self._rx_handle_map = {}
        for idx, uuid in enumerate(self.rx_uuids):
            char = self.client.services.get_characteristic(uuid)
            if char is not None:
                self._rx_handle_map[char.handle] = idx
                # DBG-LOG
                logger.debug("%s rx map handle=%s → ch%d uuid=%s", DBG_TAG, char.handle, idx, uuid)

    async def enable_rx_notify(self, *, retries: int = 5) -> None:
        """
        Subscribe to RX notifies (needed before start/read packets).

        Windows: CCCD write can race encryption / drop the link. Retry quickly
        while connected; if Unreachable, raise so the session layer reconnects.
        """
        if self._notify_on:
            return
        self._build_rx_handle_map()
        if not self._rx_handle_map and self.rx_uuids:
            # Characteristic missing from GATT tree (ACCESS_DENIED skip)
            raise ConnectionError(
                f"RX characteristic not in GATT map: {list(self.rx_uuids)}. "
                "WinRT often denied FE4A before encryption — reconnect/re-pair."
            )
        last_err: Exception | None = None
        for attempt in range(1, retries + 1):
            try:
                if not self.client.is_connected:
                    raise ConnectionError(
                        "Not connected when enabling notify. "
                        "Cuff left transfer mode or bond dropped — short-press BT and retry."
                    )
                for uuid in self.rx_uuids:
                    # UUID string path matches successful earlier PoC / omblepy
                    await self.client.start_notify(uuid, self._on_rx)
                    logger.debug("%s start_notify ok attempt=%d %s", DBG_TAG, attempt, uuid)
                self._notify_on = True
                logger.info("Notifications enabled.")
                return
            except Exception as exc:
                last_err = exc
                msg = str(exc).lower()
                logger.warning(
                    "start_notify failed (attempt %d/%d): %s: %s",
                    attempt,
                    retries,
                    type(exc).__name__,
                    exc,
                )
                # WinRT 0x800704C7 "canceled by the user" during CCCD almost always
                # means auth/bond abort or session CLOSED — not a real user click.
                bondish = any(
                    s in msg
                    for s in (
                        "canceled",
                        "cancelled",
                        "0x800704c7",
                        "-2147023673",
                        "access denied",
                        "not permitted",
                        "insufficient authentication",
                    )
                )
                if not self.client.is_connected or "unreachable" in msg or bondish:
                    # Link dead or encryption denied — outer layer must re-bond/reconnect
                    break
                # Brief settle for WinRT encryption race (keep short — transfer window)
                await asyncio.sleep(0.2 * attempt)
        assert last_err is not None
        msg = str(last_err).lower()
        if any(
            s in msg
            for s in ("canceled", "cancelled", "0x800704c7", "-2147023673", "not connected")
        ):
            raise ConnectionError(
                "start_notify aborted (WinRT canceled / link closed). "
                "Usually incomplete bond or cuff not in transfer mode. "
                "Short-press BT, or RE-PAIR with flashing P if this repeats. "
                f"raw={last_err}"
            ) from last_err
        raise last_err

    async def disable_rx_notify(self) -> None:
        if not self._notify_on:
            return
        for uuid in self.rx_uuids:
            try:
                await self.client.stop_notify(uuid)
            except Exception as exc:
                # DBG-LOG
                logger.debug("%s stop_notify fail %s: %s", DBG_TAG, uuid, exc)
        self._notify_on = False

    def _on_rx(self, char, data: bytearray) -> None:
        if len(self.rx_uuids) == 1:
            ch = 0
        elif isinstance(char, int):
            ch = self._rx_handle_map[char]
        else:
            ch = self._rx_handle_map[char.handle]

        self._rx_buffers[ch] = bytearray(data)
        # DBG-LOG: every RX chunk (very verbose — remove for release)
        logger.debug("%s rx ch%d < %s", DBG_TAG, ch, _hex(data))

        if not self._rx_buffers[0]:
            return

        if len(self.rx_uuids) == 1:
            combined = bytearray(self._rx_buffers[0])
            self._rx_buffers = [None] * 4
        else:
            packet_size = self._rx_buffers[0][0]
            needed = range((packet_size + 15) // 16)
            for i in needed:
                if self._rx_buffers[i] is None:
                    return
            combined = bytearray()
            for i in needed:
                combined += self._rx_buffers[i]
            combined = combined[:packet_size]
            self._rx_buffers = [None] * 4

        xor_crc = 0
        for b in combined:
            xor_crc ^= b
        if xor_crc:
            raise ValueError(
                f"RX CRC error xor={xor_crc} data={_hex(combined)}"
            )

        self._rx_packet_type = combined[1:3]
        self._rx_eeprom_addr = combined[3:5]
        expected = combined[5]
        if expected > (len(combined) - 8):
            self._rx_data = bytes(b"\xff") * expected
        else:
            if self._rx_packet_type == bytearray.fromhex("8f00"):
                self._rx_data = bytes(combined[6:7])
            else:
                self._rx_data = bytes(combined[6 : 6 + expected])
        self._rx_done = True

    async def _tx_and_wait(self, command: bytearray, timeout_s: float = 1.0) -> None:
        self._rx_done = False
        retries = 0
        while True:
            cmd = bytearray(command)
            width = 16
            if len(self.tx_uuids) == 1:
                width = max(width, len(cmd))
            channels = range((len(cmd) + width - 1) // width)
            for ch in channels:
                chunk = cmd[:width]
                # DBG-LOG: every TX chunk
                logger.debug("%s tx ch%d > %s", DBG_TAG, ch, _hex(chunk))
                if len(self.tx_uuids) == 1:
                    await self.client.write_gatt_char(self.tx_uuids[ch], chunk, response=False)
                else:
                    await self.client.write_gatt_char(self.tx_uuids[ch], chunk)
                cmd = cmd[width:]

            t = timeout_s
            while not self._rx_done and t > 0:
                await asyncio.sleep(0.1)
                t -= 0.1
            if self._rx_done:
                return
            retries += 1
            logger.warning("TX timeout, retry %d/5", retries)
            if retries >= 5:
                raise TimeoutError("Omron transport: same command failed 5 times")

    # ----------------------------------------------------------- session framing
    async def start_transmission(self) -> None:
        # Enable notify immediately — any delay after "link ready" often loses
        # the Windows link before CCCD is written.
        await self.enable_rx_notify()
        logger.info("Starting Omron data transmission...")
        await self._tx_and_wait(bytearray.fromhex("0800000000100018"))
        if self._rx_packet_type != bytearray.fromhex("8000"):
            raise ValueError(
                f"Invalid start response type={_hex(self._rx_packet_type or b'')}"
            )
        # DBG-LOG
        logger.debug("%s start_transmission OK", DBG_TAG)

    async def end_transmission(self) -> None:
        logger.info("Ending Omron data transmission...")
        await self._tx_and_wait(bytearray.fromhex("080f000000000007"))
        if self._rx_packet_type != bytearray.fromhex("8f00"):
            raise ValueError(
                f"Invalid end response type={_hex(self._rx_packet_type or b'')}"
            )
        if self._rx_data and self._rx_data[0]:
            raise ValueError(
                f"Device error status {self._rx_data[0]} on end_transmission"
            )
        await self.disable_rx_notify()
        # DBG-LOG
        logger.debug("%s end_transmission OK", DBG_TAG)

    # ---------------------------------------------------------------- EEPROM IO
    async def _read_block(self, address: int, blocksize: int) -> bytes:
        cmd = bytearray.fromhex("080100")
        cmd += address.to_bytes(2, "big")
        cmd += blocksize.to_bytes(1, "big")
        xor_crc = 0
        for b in cmd:
            xor_crc ^= b
        cmd += b"\x00"
        cmd.append(xor_crc)
        await self._tx_and_wait(cmd)
        if self._rx_eeprom_addr != address.to_bytes(2, "big"):
            raise ValueError(
                f"Address mismatch: got {_hex(self._rx_eeprom_addr or b'')} "
                f"expected {address.to_bytes(2, 'big').hex()}"
            )
        if self._rx_packet_type != bytearray.fromhex("8100"):
            raise ValueError("Invalid packet type for EEPROM read")
        return self._rx_data or b""

    async def _write_block(self, address: int, data: bytes | bytearray) -> None:
        cmd = bytearray()
        cmd += (len(data) + 8).to_bytes(1, "big")
        cmd += bytearray.fromhex("01c0")
        cmd += address.to_bytes(2, "big")
        cmd += len(data).to_bytes(1, "big")
        cmd += bytes(data)
        xor_crc = 0
        for b in cmd:
            xor_crc ^= b
        cmd += b"\x00"
        cmd.append(xor_crc)
        await self._tx_and_wait(cmd)
        if self._rx_eeprom_addr != address.to_bytes(2, "big"):
            raise ValueError("Address mismatch on EEPROM write")
        if self._rx_packet_type != bytearray.fromhex("81c0"):
            raise ValueError("Invalid packet type for EEPROM write")

    async def read_eeprom(
        self, start: int, nbytes: int, block_size: int | None = None
    ) -> bytearray:
        block_size = block_size or self.profile.transmission_block_size
        out = bytearray()
        addr = start
        remaining = nbytes
        while remaining > 0:
            n = min(remaining, block_size)
            # DBG-LOG: progress through large dumps
            logger.debug("%s eeprom read addr=0x%04X size=0x%02X", DBG_TAG, addr, n)
            out += await self._read_block(addr, n)
            addr += n
            remaining -= n
        return out

    async def write_eeprom(
        self, start: int, data: bytes | bytearray, block_size: int = 0x08
    ) -> None:
        addr = start
        buf = bytes(data)
        while buf:
            n = min(len(buf), block_size)
            # DBG-LOG
            logger.debug("%s eeprom write addr=0x%04X size=0x%02X", DBG_TAG, addr, n)
            await self._write_block(addr, buf[:n])
            buf = buf[n:]
            addr += n

    # --------------------------------------------------------------- unlock
    def _on_unlock(self, _char, data: bytearray) -> None:
        self._unlock_data = bytes(data)
        self._unlock_done = True
        # DBG-LOG
        logger.debug("%s unlock notify < %s", DBG_TAG, _hex(data))

    async def unlock_session(self, key: bytes = DEFAULT_UNLOCK_KEY) -> None:
        """
        Pre-start unlock dispatcher.

        CLASSIC_KEY → 16-byte app key auth (0x01 + key → 0x81 ack)
        TOKEN_KEY   → stateless 0x11/0x91 nonce (modern FE4A / OMRON Connect)
        NONE        → no-op
        """
        mode = self.profile.unlock_mode
        if mode == UnlockMode.NONE:
            logger.debug("%s unlock skipped (NONE)", DBG_TAG)
            return
        if mode == UnlockMode.CLASSIC_KEY:
            await self.unlock_with_key(key)
            return
        if mode == UnlockMode.TOKEN_KEY:
            await self.unlock_with_token()
            return
        if mode == UnlockMode.SECURE_SESSION:
            raise NotImplementedError(
                "SECURE_SESSION unlock is not implemented in this toolkit"
            )
        logger.warning("Unknown unlock_mode=%s — skipping unlock", mode)

    async def unlock_with_token(self, *, timeout_s: float = 5.0) -> None:
        """
        Modern FE4A token handshake (phone HCI + hass-omron).

        Host writes 20 bytes on unlock UUID (write-without-response preferred):
            0x11 | nonce[4] | 0x00 × 15
        Device notifies on the same UUID:
            0x91 | 0x00 | nonce[4] | …

        Confirmed on HEM-7143T1 phone capture (OMRON Connect):
          Write  handle 0x001B: 11 cd0237b7 00…
          Notify handle 0x001B: 91 00 cd0237b7 …
        then START on TX (0x001E) and data on RX (0x0020).

        Order matches official app: enable RX CCCD, then unlock CCCD, then 0x11.
        """
        token = secrets.token_bytes(4)
        packet = b"\x11" + token + (b"\x00" * 15)
        assert len(packet) == 20

        logger.info(
            "Token unlock (0x11/0x91) nonce=%s unlock_uuid=%s …",
            token.hex(),
            self.unlock_uuid,
        )

        # 1) RX CCCD first (phone order) — leave on for start_transmission
        if not self._notify_on:
            try:
                await self.enable_rx_notify(retries=3)
            except Exception as exc:
                logger.warning(
                    "Token unlock: RX notify prime failed (%s); continuing with unlock notify only",
                    exc,
                )

        # 2) Unlock characteristic notify
        self._unlock_done = False
        self._unlock_data = None
        expected = bytes([0x91, 0x00]) + token

        def _token_cb(_char, data: bytearray) -> None:
            raw = bytes(data)
            logger.debug("%s token unlock notify < %s", DBG_TAG, _hex(raw))
            # Accept prefix match (device may pad with zeros)
            if len(raw) >= 6 and raw[0] == 0x91 and raw[1] == 0x00 and raw[2:6] == token:
                self._unlock_data = raw
                self._unlock_done = True
            elif len(raw) >= 2 and raw[0] == 0x91:
                # Status non-zero or wrong echo — still record for error message
                self._unlock_data = raw
                self._unlock_done = True

        unlock_char = self.client.services.get_characteristic(self.unlock_uuid)
        if unlock_char is None:
            raise ConnectionError(
                f"Unlock characteristic not found: {self.unlock_uuid}. "
                "FE4A may still be ACCESS_DENIED — re-pair in P mode and retry."
            )

        await self.client.start_notify(self.unlock_uuid, _token_cb)
        await asyncio.sleep(0.12)

        try:
            # Prefer write-without-response (ATT Write Command) like OMRON Connect
            for use_response in (False, True):
                self._unlock_done = False
                self._unlock_data = None
                logger.debug(
                    "%s token write nonce=%s response=%s packet=%s",
                    DBG_TAG,
                    token.hex(),
                    use_response,
                    _hex(packet),
                )
                await self.client.write_gatt_char(
                    self.unlock_uuid, packet, response=use_response
                )
                deadline = timeout_s
                while not self._unlock_done and deadline > 0:
                    await asyncio.sleep(0.05)
                    deadline -= 0.05
                if self._unlock_done:
                    break
                if use_response:
                    raise TimeoutError(
                        f"Token unlock notify timeout after 0x11 write "
                        f"(nonce={token.hex()})"
                    )
                logger.debug(
                    "Token unlock: no notify with response=False; retrying with response=True"
                )

            resp = self._unlock_data or b""
            if len(resp) < 6 or resp[0] != 0x91 or resp[1] != 0x00 or resp[2:6] != token:
                raise ValueError(
                    f"Token unlock failed: expected 91 00 {token.hex()}… "
                    f"got {_hex(resp)}"
                )
            logger.info("Token unlock OK (nonce=%s echo matches).", token.hex())
        finally:
            try:
                await self.client.stop_notify(self.unlock_uuid)
            except Exception as exc:
                logger.debug("%s stop_notify unlock: %s", DBG_TAG, exc)

    async def unlock_with_key(self, key: bytes = DEFAULT_UNLOCK_KEY) -> None:
        """Classic stack: prove we know the programmed 16-byte key."""
        if self.profile.unlock_mode != UnlockMode.CLASSIC_KEY:
            # DBG-LOG
            logger.debug("%s unlock_with_key skipped (mode=%s)", DBG_TAG, self.profile.unlock_mode)
            return
        if len(key) != 16:
            raise ValueError("unlock key must be 16 bytes")

        logger.info("Unlocking device with app-layer key...")
        await self.client.start_notify(self.unlock_uuid, self._on_unlock)
        self._unlock_done = False
        await self.client.write_gatt_char(
            self.unlock_uuid, b"\x01" + key, response=True
        )
        while not self._unlock_done:
            await asyncio.sleep(0.1)
        resp = self._unlock_data or b""
        await self.client.stop_notify(self.unlock_uuid)
        if resp[:2] != bytes.fromhex("8100"):
            raise ValueError(
                f"Unlock failed — key mismatch? response={_hex(resp)}"
            )
        logger.info("Unlock OK.")

    async def program_unlock_key(self, key: bytes = DEFAULT_UNLOCK_KEY) -> None:
        """
        Classic stack pairing: enter key-programming mode and write a new key.
        Cuff must be in -P- pairing mode.
        """
        if len(key) != 16:
            raise ValueError("unlock key must be 16 bytes")

        logger.info("Programming unlock key (classic pairing)...")
        # Enabling RX notify can trigger SMP security request on some stacks
        await self.client.start_notify(self.rx_uuids[0], lambda _h, _d: None)
        await self.client.start_notify(self.unlock_uuid, self._on_unlock)

        max_retries = 10
        for attempt in range(max_retries):
            self._unlock_done = False
            await self.client.write_gatt_char(
                self.unlock_uuid, b"\x02" + b"\x00" * 16, response=True
            )
            while not self._unlock_done:
                await asyncio.sleep(0.1)
            resp = self._unlock_data or b""
            # DBG-LOG
            logger.debug(
                "%s key-program mode attempt=%d resp=%s",
                DBG_TAG,
                attempt + 1,
                _hex(resp[:4]),
            )
            if resp[:2] == bytes.fromhex("8200"):
                logger.info("Entered key programming mode (attempt %d).", attempt + 1)
                break
            await asyncio.sleep(1.0)
        else:
            raise ValueError(
                "Could not enter key programming mode. "
                "Is the cuff in pairing mode (flashing P)?"
            )

        self._unlock_done = False
        await self.client.write_gatt_char(
            self.unlock_uuid, b"\x00" + key, response=True
        )
        while not self._unlock_done:
            await asyncio.sleep(0.1)
        resp = self._unlock_data or b""
        if resp[:2] != bytes.fromhex("8000"):
            raise ValueError(f"Failure programming key: {_hex(resp)}")

        await self.client.stop_notify(self.unlock_uuid)
        await self.client.stop_notify(self.rx_uuids[0])
        logger.info("Unlock key programmed: %s", key.hex())

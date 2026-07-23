"""
FORA first-party BLE session — iFORA Smart 1.5.9 companion parity (APK-reliable path).

Implements from decompile (no HCI required for structure):
  connect → notify 1524 → [500ms] → setDateTime → SN1 → SN2 → project
  → firmware (non-SpO2) / SpO2 poll path → record count
  → getRecord1/2 loop (or WS long-frame attempt) → powerOff

Clinical multiparam secondary fields still HCI-gated; BG + basic BP decoded.
"""
from __future__ import annotations

import asyncio
import logging
from dataclasses import dataclass, field
from typing import Any, Callable, List, Optional

from bleak import BleakClient, BleakScanner
from bleak.backends.characteristic import BleakGATTCharacteristic

from . import protocol as P

log = logging.getLogger("medical_ble.fora.session")

OnFrame = Callable[[dict], None]
OnHex = Callable[[bytes], None]


@dataclass
class ForaSyncResult:
    ok: bool
    project_code: str = ""
    serial: str = ""
    record_count: int = 0
    import_type: int = 0
    firmware: Optional[int] = None
    frames: List[dict] = field(default_factory=list)
    raw_records: List[dict] = field(default_factory=list)
    error: str = ""
    detail: dict = field(default_factory=dict)


class ForaSession:
    def __init__(
        self,
        address: str,
        *,
        name_hint: str = "",
        find_timeout: float = 15.0,
        pair: bool = False,
        on_frame: Optional[OnFrame] = None,
        on_hex: Optional[OnHex] = None,
    ):
        self.address = address.strip().upper()
        self.name_hint = name_hint
        self.find_timeout = find_timeout
        self.pair = pair
        self.on_frame = on_frame
        self.on_hex = on_hex
        self._rx: asyncio.Queue[bytes] = asyncio.Queue()
        self._client: Optional[BleakClient] = None
        self._notify_uuid = P.CHAR_UUID
        self._ws_buf = bytearray()

    async def _resolve_device(self):
        if self.find_timeout and self.find_timeout > 0:
            dev = await BleakScanner.find_device_by_address(
                self.address, timeout=self.find_timeout
            )
            if dev is None:
                raise RuntimeError(f"FORA device {self.address} not found in scan")
            return dev
        return self.address

    def _on_notify(self, _char: BleakGATTCharacteristic, data: bytearray) -> None:
        raw = bytes(data)
        if self.on_hex:
            try:
                self.on_hex(raw)
            except Exception:
                pass
        fr = P.parse_frame(raw)
        if fr and self.on_frame:
            try:
                self.on_frame(fr)
            except Exception:
                pass
        try:
            self._rx.put_nowait(raw)
        except asyncio.QueueFull:
            pass

    async def _write_cmd(self, payload: bytes) -> None:
        assert self._client is not None
        log.debug("FORA TX %s", payload.hex())
        await self._client.write_gatt_char(
            self._notify_uuid,
            payload,
            response=not P.WRITE_WITHOUT_RESPONSE,
        )

    def _accept_rx(self, raw: bytes) -> bool:
        if P.is_valid_app_rx(raw):
            return True
        fr = P.parse_frame(raw)
        return bool(fr and fr.get("checksum_ok"))

    async def _transact(
        self,
        payload: bytes,
        *,
        expect_cmd: Optional[int] = None,
        timeout_ms: Optional[int] = None,
        retries: Optional[int] = None,
    ) -> bytes:
        timeout_s = (timeout_ms or P.TIMINGS.cmd_response_timeout_ms) / 1000.0
        max_try = retries if retries is not None else P.TIMINGS.cmd_max_retries
        last_err: Optional[Exception] = None
        for attempt in range(1, max_try + 1):
            while not self._rx.empty():
                try:
                    self._rx.get_nowait()
                except asyncio.QueueEmpty:
                    break
            try:
                await self._write_cmd(payload)
                raw = await asyncio.wait_for(self._rx.get(), timeout=timeout_s)
            except Exception as exc:
                last_err = exc
                log.warning("FORA cmd attempt %s/%s failed: %s", attempt, max_try, exc)
                continue
            if not self._accept_rx(raw):
                last_err = RuntimeError(f"invalid RX {raw.hex()}")
                continue
            fr = P.parse_frame(raw) or {}
            if expect_cmd is not None and fr.get("command") != expect_cmd:
                log.debug(
                    "FORA RX cmd=0x%02X expected=0x%02X (keeping)",
                    fr.get("command"),
                    expect_cmd,
                )
            return raw
        raise RuntimeError(f"FORA command failed after {max_try} tries: {last_err}")

    async def _power_off(self) -> None:
        try:
            await self._write_cmd(P.cmd_power_off())
            await asyncio.sleep(0.2)
        except Exception as exc:
            log.debug("power_off soft-fail: %s", exc)

    async def _spo2_pre_path(self, result: ForaSyncResult) -> None:
        """
        SpO2 (importType 7): firmware → maybe setBLEStatus → poll getNewSpO2
        until data[2]+data[3]>0 or give up (APK 15s timeout loop).
        """
        try:
            raw = await self._transact(P.cmd_firmware(), expect_cmd=P.CMD_FIRMWARE)
            result.frames.append(P.parse_frame(raw) or {})
            fw = P.firmware_version_from_response(raw, P.TYPE_SPO2)
            result.firmware = fw.get("firmware")
            # App: if fw==8 and extra==0 → setBLEStatus then NewSpO2; else NewSpO2
            if fw.get("firmware") == 8 and fw.get("extra") == 0:
                raw = await self._transact(
                    P.cmd_set_ble_status(), expect_cmd=P.CMD_SET_BLE_STATUS
                )
                result.frames.append(P.parse_frame(raw) or {})
        except Exception as exc:
            log.warning("FORA SpO2 firmware path soft-fail: %s", exc)

        deadline = asyncio.get_event_loop().time() + (
            P.TIMINGS.spo2_poll_timeout_ms / 1000.0
        )
        while asyncio.get_event_loop().time() < deadline:
            try:
                raw = await self._transact(
                    P.cmd_get_new_spo2(),
                    expect_cmd=P.CMD_GET_NEW_SPO2,
                    timeout_ms=3000,
                    retries=2,
                )
                result.frames.append(P.parse_frame(raw) or {})
                if len(raw) >= 4 and (raw[2] + raw[3]) > 0:
                    return
            except Exception:
                await asyncio.sleep(0.5)
        log.warning("FORA SpO2 poll timed out — continuing to history")

    async def _ws_read_index(self, index: int, project_no: str) -> Optional[bytes]:
        """
        Weight-scale long frame: companion reassembles until 34 or 40 bytes,
        start 0x51|0x77, penultimate 0xA5, checksum OK.
        """
        self._ws_buf.clear()
        payload = P.cmd_ws_read(index, project_no)
        await self._write_cmd(payload)
        deadline = asyncio.get_event_loop().time() + 8.0
        while asyncio.get_event_loop().time() < deadline:
            try:
                chunk = await asyncio.wait_for(self._rx.get(), timeout=2.0)
            except asyncio.TimeoutError:
                break
            self._ws_buf.extend(chunk)
            buf = bytes(self._ws_buf)
            for length in P.TIMINGS.ws_frame_lengths:
                if len(buf) >= length:
                    frame = buf[:length]
                    if frame[0] in (P.FRAME_START, P.FRAME_START_WS_ALT) and (
                        frame[-2] & 0xFF
                    ) == P.DIR_IN:
                        cs = sum(frame[:-1]) & 0xFF
                        if (frame[-1] & 0xFF) == cs:
                            return frame
            if len(self._ws_buf) > 64:
                break
        return bytes(self._ws_buf) if self._ws_buf else None

    async def run_history(
        self,
        *,
        user: Optional[int] = None,
        max_records: int = 500,
        set_time: bool = True,
        power_off: bool = True,
        connect_timeout: float = 35.0,
        do_firmware: bool = True,
    ) -> ForaSyncResult:
        result = ForaSyncResult(ok=False)
        device = await self._resolve_device()
        client = BleakClient(device, timeout=connect_timeout)
        self._client = client
        try:
            # Companion: after connect, 500ms before heavy GATT work is handled
            # by start_notify path; we also sleep after CCCD below.
            await client.connect()
            if self.pair:
                try:
                    pair_fn = getattr(client, "pair", None)
                    if callable(pair_fn):
                        await pair_fn()
                except Exception as exc:
                    log.warning("FORA pair() soft-fail: %s", exc)

            char_uuid = P.CHAR_UUID
            try:
                await client.start_notify(char_uuid, self._on_notify)
            except Exception:
                for svc in client.services:
                    if "1523" in str(svc.uuid).lower():
                        for ch in svc.characteristics:
                            props = ch.properties or []
                            if "notify" in props or "indicate" in props:
                                char_uuid = ch.uuid
                                self._notify_uuid = char_uuid
                                await client.start_notify(char_uuid, self._on_notify)
                                break
                        else:
                            continue
                        break
                else:
                    raise

            await asyncio.sleep(P.TIMINGS.after_cccd_goto_import_ms / 1000.0)

            # --- companion import sequence ---
            if set_time:
                raw = await self._transact(
                    P.cmd_set_datetime(),
                    expect_cmd=P.CMD_SET_DATETIME,
                    timeout_ms=P.TIMINGS.set_datetime_timeout_ms,
                )
                result.frames.append(P.parse_frame(raw) or {})

            sn = ""
            for cmd_fn, exp in (
                (P.cmd_serial_1, P.CMD_SERIAL_1),
                (P.cmd_serial_2, P.CMD_SERIAL_2),
            ):
                raw = await self._transact(cmd_fn(), expect_cmd=exp)
                result.frames.append(P.parse_frame(raw) or {})
                chunk = P.serial_chunk_from_response(raw)
                if chunk:
                    sn += chunk
            if len(sn) > 16:
                log.warning("FORA serial length %s > 16 (app error path)", len(sn))
                sn = sn[:16]
            result.serial = sn

            raw = await self._transact(P.cmd_project_code(), expect_cmd=P.CMD_PROJECT_CODE)
            result.frames.append(P.parse_frame(raw) or {})
            result.project_code = P.project_code_from_response(raw) or ""
            import_type = P.import_type_from_project(
                result.project_code, status_true=True
            )
            result.import_type = import_type

            use_user = (
                user
                if user is not None
                else P.user_no_for_project(result.project_code, default=0)
            )
            if use_user < 0:
                use_user = 0  # Bleak/session: negative user only meaningful in app profile path

            # SpO2 special pre-path; else firmware then records (APK)
            if import_type == P.TYPE_SPO2:
                await self._spo2_pre_path(result)
            elif do_firmware:
                try:
                    raw = await self._transact(
                        P.cmd_firmware(), expect_cmd=P.CMD_FIRMWARE, retries=3
                    )
                    result.frames.append(P.parse_frame(raw) or {})
                    fw = P.firmware_version_from_response(raw, import_type)
                    result.firmware = fw.get("firmware")
                except Exception as exc:
                    log.warning("FORA firmware soft-fail (continuing): %s", exc)

            raw = await self._transact(
                P.cmd_record_number(use_user), expect_cmd=P.CMD_RECORD_NUMBER
            )
            fr = P.parse_frame(raw) or {}
            result.frames.append(fr)
            msg = fr.get("message") or b"\x00\x00\x00\x00"
            count = P.parse_record_count(msg, import_type)
            result.record_count = count

            if count <= 0:
                # App: empty history → still success path + powerOff
                if power_off:
                    await self._power_off()
                result.ok = True
                result.detail = {
                    "char_uuid": str(self._notify_uuid),
                    "service_uuid": P.SERVICE_UUID,
                    "import_type": import_type,
                    "user_no": use_user,
                    "empty_history": True,
                    "name_hint": self.name_hint,
                    "timings": P.TIMINGS.__dict__,
                }
                return result

            count = min(count, max_records)

            if import_type == P.TYPE_WS:
                for i in range(count):
                    frame = await self._ws_read_index(i, result.project_code)
                    if not frame:
                        continue
                    result.frames.append(
                        {"raw_hex": frame.hex(), "kind": "ws_long", "index": i}
                    )
                    result.raw_records.append(
                        {
                            "index": i,
                            "kind": "ws",
                            "import_type": P.TYPE_WS,
                            "raw_hex": frame.hex(),
                            "length": len(frame),
                            # Full body map needs HCI; store raw for later
                            "invalid": False,
                        }
                    )
            else:
                for i in range(count):
                    raw1 = await self._transact(
                        P.cmd_record_datetime(i, use_user),
                        expect_cmd=P.CMD_RECORD_DATETIME,
                    )
                    raw2 = await self._transact(
                        P.cmd_record_value(i, use_user),
                        expect_cmd=P.CMD_RECORD_VALUE,
                    )
                    fr1 = P.parse_frame(raw1) or {}
                    fr2 = P.parse_frame(raw2) or {}
                    result.frames.extend([fr1, fr2])
                    m1 = fr1.get("message") or b"\x00\x00\x00\x00"
                    m2 = fr2.get("message") or b"\x00\x00\x00\x00"
                    rec = P.decode_record_pair(
                        m1,
                        m2,
                        project_no=result.project_code,
                        import_type=import_type,
                    )
                    rec["index"] = i
                    rec["datetime_frame"] = fr1.get("raw_hex")
                    rec["value_frame"] = fr2.get("raw_hex")
                    result.raw_records.append(rec)

            if power_off:
                await self._power_off()

            result.ok = True
            result.detail = {
                "char_uuid": str(self._notify_uuid),
                "service_uuid": P.SERVICE_UUID,
                "import_type": import_type,
                "user_no": use_user,
                "name_hint": self.name_hint,
                "firmware": result.firmware,
                "timings": P.TIMINGS.__dict__,
                "gatt_auto_connect": P.GATT_AUTO_CONNECT,
                "write_without_response": P.WRITE_WITHOUT_RESPONSE,
            }
            return result
        except Exception as exc:
            result.error = str(exc)
            log.exception("FORA session failed")
            return result
        finally:
            try:
                if client.is_connected:
                    await client.stop_notify(self._notify_uuid)
            except Exception:
                pass
            try:
                await client.disconnect()
            except Exception:
                pass
            self._client = None


async def run_fora_sync(
    address: str,
    *,
    name_hint: str = "",
    find_timeout: float = 15.0,
    already_paired: bool = True,
    **kwargs: Any,
) -> ForaSyncResult:
    sess = ForaSession(
        address,
        name_hint=name_hint,
        find_timeout=find_timeout,
        pair=not already_paired,
    )
    return await sess.run_history(**kwargs)

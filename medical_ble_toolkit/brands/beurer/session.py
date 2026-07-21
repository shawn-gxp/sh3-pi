"""
Beurer companion-parity BLE session.

Orchestrates connect → settle → pair → DIS → set-time → CCCD → history query
with HealthManager Pro delays, capability markers, sync results, dedup, and
glucose incremental RACP.
"""

from __future__ import annotations

import asyncio
import logging
import time
from datetime import datetime
from typing import Any, List, Optional

from bleak import BleakClient
from bleak.backends.device import BLEDevice
from bleak.exc import BleakError

from medical_ble_toolkit.common.hexutil import format_hex_dump, ms_timestamp
from medical_ble_toolkit.common.winrt_errors import (
    is_windows,
    os_pair_supported,
    pairing_ui_hint,
    remove_bond_instructions,
)
from medical_ble_toolkit.parser import get_parser
from medical_ble_toolkit.parsers.blood_pressure import (
    BlpBloodPressureParser,
    encode_current_time_2a2b,
    parse_blood_pressure_measurement,
)
from medical_ble_toolkit.parsers import glucose as glucose_mod
from medical_ble_toolkit.parsers import beurer_po60 as po60_mod
from medical_ble_toolkit.parsers import beurer_scale as scale_mod
from medical_ble_toolkit.models import DeviceBrand
from medical_ble_toolkit.profiles import DeviceProfile, get_profile
from .capabilities import DeviceCapabilities, get_capabilities, mfg_data_suggests_passkey
from .catalog import BeurerDevice, get_device
from .dedup import bp_dedup_key, dedupe_readings, glucose_dedup_key
from .store import (
    bp_seen_keyset,
    glucose_last_seq,
    remember_bp_keys,
    set_glucose_last_seq,
)
from .sync_result import SyncResult, SyncStatus, classify_sync
from .timing import SessionTiming, timing_for_profile

log = logging.getLogger("medical_ble.beurer.session")


def _ble_device(address: str, name: str = "Beurer") -> BLEDevice:
    return BLEDevice(address.strip().upper(), name, None)


def _is_connection_lost(exc: BaseException) -> bool:
    msg = str(exc).lower()
    return any(
        s in msg
        for s in (
            "connection lost",
            "disconnected",
            "unreachable",
            "peer removed",
            "connection was aborted",
            "device not found",
        )
    )


def _is_auth_error(exc: BaseException) -> bool:
    msg = str(exc).lower()
    return any(
        s in msg
        for s in (
            "auth",
            "bond",
            "pair",
            "gatt_auth",
            "not permitted",
            "access denied",
            "insufficient authentication",
            "encryption",
        )
    )


class BeurerCompanionSession:
    """
    Full multi-device Beurer session with companion-app timing + stability.

    Usage:
      sess = BeurerCompanionSession(address, model_id=\"BM54\")
      result = await sess.run()  # SyncResult
    """

    def __init__(
        self,
        address: str,
        model_id: str = "BM54",
        *,
        pair: bool = True,
        duration: Optional[float] = None,
        connect_timeout: float = 30.0,
        connect_retries: int = 3,
        mfg_data: Optional[dict] = None,
        force_full_glucose_history: bool = False,
    ):
        self.address = address.strip().upper()
        self.device_meta: Optional[BeurerDevice] = get_device(model_id)
        self.model_id = (
            self.device_meta.id if self.device_meta else (model_id or "BM54").upper()
        )
        self.toolkit_profile = (
            self.device_meta.toolkit_profile if self.device_meta else "beurer_bp"
        )
        self.profile: DeviceProfile = get_profile(self.toolkit_profile)
        self.caps: DeviceCapabilities = get_capabilities(self.model_id)
        self.timing: SessionTiming = timing_for_profile(
            self.toolkit_profile, self.model_id
        )
        # Override timing from APK markers
        self.timing = SessionTiming(
            **{
                **self.timing.__dict__,
                "post_connect_settle_s": self.caps.post_connect_settle_s,
                "quiet_timeout_s": self.caps.quiet_timeout_s,
                "before_history_query_s": (
                    self.caps.before_racp_s
                    if self.toolkit_profile == "beurer_glucose"
                    else self.timing.before_history_query_s
                ),
            }
        )

        # OS bonding needed on Windows and Linux for encrypted BLP indications
        self.pair = bool(pair) and os_pair_supported()
        self.duration = duration if duration is not None else self.timing.max_listen_s
        self.connect_timeout = connect_timeout
        self.connect_retries = max(1, connect_retries)
        self.mfg_data = mfg_data
        self.force_full_glucose_history = force_full_glucose_history

        adv_passkey = mfg_data_suggests_passkey(mfg_data or {})
        self.passkey_hint = bool(
            self.caps.passkey_likely
            if adv_passkey is None
            else adv_passkey or self.caps.passkey_likely
        )

        self._client: Optional[BleakClient] = None
        self._parser = get_parser(self.profile.parser_key)
        if hasattr(self._parser, "model"):
            try:
                self._parser.model = self.model_id
            except Exception:
                pass
        # pulse_swapped on BLP parser
        if isinstance(self._parser, BlpBloodPressureParser):
            self._parser.pulse_swapped = self.caps.pulse_swapped
        elif self.toolkit_profile in ("beurer_bp", "beurer_bm54", "beurer_ecg"):
            # re-wrap with swapped flag for parse path
            self._pulse_swapped = self.caps.pulse_swapped
        else:
            self._pulse_swapped = False
        self._pulse_swapped = self.caps.pulse_swapped

        self.readings: List[Any] = []
        self.raw: List[dict] = []
        self._notif_seq = 0
        self._last_notif_mono: Optional[float] = None
        self._subscribed: List[str] = []
        self._session_start_mono = 0.0
        self._po60_need_more = False
        self._connect_ok = False
        self._link_dropped = False
        self._auth_error = False
        self._paired_attempt = False
        self._last_error: Optional[BaseException] = None
        self.last_result: Optional[SyncResult] = None

        log.info(
            "[CAPS] %s settle_3s=%s pulse_swapped=%s set_time=%s "
            "passkey_likely=%s glucose_long_racp=%s afib=%s markers=%s",
            self.model_id,
            self.caps.settle_3s,
            self.caps.pulse_swapped,
            self.caps.set_time,
            self.caps.passkey_likely,
            self.caps.glucose_long_racp,
            self.caps.afib_variant,
            self.caps.markers[:12],
        )
        if self.passkey_hint:
            log.info(
                "[PAIR] Passkey likely for %s — %s",
                self.model_id,
                pairing_ui_hint(),
            )

    # ----- notify ----------------------------------------------------------

    def _on_notify(self, characteristic: Any, data: bytearray) -> None:
        self._last_notif_mono = time.monotonic()
        self._notif_seq += 1
        uuid = str(getattr(characteristic, "uuid", characteristic))
        payload = bytes(data) if data is not None else b""
        hex_dump = format_hex_dump(payload)
        log.info(
            "[NOTIF] #%d  %s  len=%d  ts=%s",
            self._notif_seq,
            uuid,
            len(payload),
            ms_timestamp(),
        )
        log.info("[HEX]   %s", hex_dump)
        self.raw.append(
            {"seq": self._notif_seq, "uuid": uuid, "data": payload, "hex": hex_dump}
        )

        try:
            result = self._parse_payload(payload, uuid)
            if result is not None:
                if isinstance(result, list):
                    self.readings.extend(result)
                    for r in result:
                        log.info("[PARSE] OK  →  %s", _brief(r))
                else:
                    self.readings.append(result)
                    log.info("[PARSE] OK  →  %s", _brief(result))
                    # track glucose sequence
                    seq = getattr(result, "sequence", None)
                    if seq is not None:
                        set_glucose_last_seq(self.address, int(seq), self.model_id)
        except Exception as exc:  # noqa: BLE001
            log.error("[PARSE] %s: %s | hex=%s", type(exc).__name__, exc, hex_dump)

        if self.toolkit_profile == "beurer_po60":
            stream = getattr(self._parser, "stream", None)
            if stream is not None and getattr(stream, "need_more", False):
                self._po60_need_more = True

    def _parse_payload(self, payload: bytes, uuid: str) -> Any:
        ul = uuid.lower().replace("-", "")
        # BP measurement with pulse_swapped from capabilities
        if "2a35" in ul and self.toolkit_profile in (
            "beurer_bp",
            "beurer_bm54",
            "beurer_ecg",
        ):
            return parse_blood_pressure_measurement(
                payload,
                brand=DeviceBrand.BEURER,
                model=self.model_id,
                pulse_swapped=self._pulse_swapped,
            )
        if hasattr(self._parser, "parse"):
            try:
                return self._parser.parse(payload, characteristic_uuid=uuid)
            except TypeError:
                return self._parser.parse(payload)
        return None

    async def _po60_request_more(self) -> None:
        if not self._client or not self._client.is_connected:
            return
        try:
            await asyncio.sleep(self.timing.write_pacing_s)
            await self._client.write_gatt_char(
                po60_mod.PO60_WRITE, po60_mod.cmd_request_more(), response=False
            )
            log.info("[PO60] request-more 99 01 1A")
        except Exception as exc:  # noqa: BLE001
            log.warning("[PO60] request-more failed: %s", exc)

    # ----- connect ---------------------------------------------------------

    async def connect(self) -> None:
        last: Optional[BaseException] = None
        target = _ble_device(self.address, name=self.model_id)
        for attempt in range(1, self.connect_retries + 1):
            log.info(
                "[CONNECT] Beurer %s attempt %d/%d …",
                self.model_id,
                attempt,
                self.connect_retries,
            )
            client = BleakClient(
                target,
                timeout=self.connect_timeout,
                disconnected_callback=self._on_disconnect,
            )
            try:
                await client.connect()
                if client.is_connected:
                    self._client = client
                    self._connect_ok = True
                    log.info("[CONNECT] OK  ts=%s", ms_timestamp())
                    return
            except Exception as exc:  # noqa: BLE001
                last = exc
                self._last_error = exc
                if _is_auth_error(exc):
                    self._auth_error = True
                lost = _is_connection_lost(exc)
                log.warning(
                    "[CONNECT] failed%s: %s: %s",
                    " (connection lost — retry)" if lost else "",
                    type(exc).__name__,
                    exc,
                )
                try:
                    await client.disconnect()
                except Exception:
                    pass
                # App retries ConnectionLostException
                delay = self.timing.connect_retry_s * attempt
                if lost:
                    delay = max(delay, 2.0)
                await asyncio.sleep(delay)
        raise last or ConnectionError("Beurer connect failed")

    def _on_disconnect(self, client: BleakClient) -> None:
        self._link_dropped = True
        log.warning("[DISCONNECT] unexpected  ts=%s", ms_timestamp())

    async def _pair_if_needed(self) -> None:
        if not self.pair or not self._client:
            return
        self._paired_attempt = True
        log.info("[PAIR] OS bond (companion pairing)…")
        if self.passkey_hint:
            log.info("[PAIR] Expect passkey UI for this model/generation.")
        try:
            try:
                await self._client.pair(protection_level=2)
            except TypeError:
                await self._client.pair()
            log.info("[PAIR] OK")
            # Bond-wait settle (GATT_AUTH recovery window)
            await asyncio.sleep(max(self.timing.post_pair_settle_s, 0.8))
        except BleakError as exc:
            self._last_error = exc
            msg = str(exc).lower()
            if "already" in msg:
                log.info("[PAIR] already bonded")
            elif _is_auth_error(exc):
                self._auth_error = True
                log.warning("[PAIR] auth/bond issue: %s", exc)
            else:
                log.warning("[PAIR] %s — continuing if link still up", exc)
            await asyncio.sleep(self.timing.post_pair_settle_s)

    async def _settle(self) -> None:
        s = self.timing.post_connect_settle_s
        if s > 0:
            log.info(
                "[TIMING] post-connect settle %.1fs (marker te=%s)",
                s,
                self.caps.settle_3s,
            )
            await asyncio.sleep(s)

    async def _read_dis(self) -> None:
        if not self._client:
            return
        chars = {
            "model": "00002a24-0000-1000-8000-00805f9b34fb",
            "serial": "00002a25-0000-1000-8000-00805f9b34fb",
            "firmware": "00002a26-0000-1000-8000-00805f9b34fb",
            "manufacturer": "00002a29-0000-1000-8000-00805f9b34fb",
        }
        log.info("[DIS] reading identity…")
        for label, uuid in chars.items():
            try:
                raw = await self._client.read_gatt_char(uuid)
                text = bytes(raw).decode("utf-8", errors="replace").strip("\x00 ").strip()
                log.info("[DIS] %s=%r", label, text)
            except Exception:
                pass
        await asyncio.sleep(self.timing.after_dis_s)

    async def _set_time_if_needed(self) -> None:
        if not self._client:
            return
        want = self.caps.set_time or self.toolkit_profile in (
            "beurer_ecg",
            "beurer_glucose",
        )
        if self.toolkit_profile == "beurer_bp" and not self.caps.set_time:
            return
        if not want and self.toolkit_profile not in ("beurer_ecg", "beurer_glucose"):
            return
        uuid = "00002a2b-0000-1000-8000-00805f9b34fb"
        payload = encode_current_time_2a2b(datetime.now())
        with_resp = self.caps.set_time_with_response
        log.info(
            "[TIME] Current Time 0x2A2B response=%s  %s",
            with_resp,
            format_hex_dump(payload),
        )
        try:
            await self._client.write_gatt_char(uuid, payload, response=with_resp)
            log.info("[TIME] OK")
        except Exception as exc:  # noqa: BLE001
            try:
                await self._client.write_gatt_char(
                    uuid, payload, response=not with_resp
                )
                log.info("[TIME] OK (flipped response mode)")
            except Exception as exc2:  # noqa: BLE001
                log.warning("[TIME] skip: %s / %s", exc, exc2)
        await asyncio.sleep(self.timing.after_set_time_s)

    async def _subscribe(self) -> None:
        if not self._client:
            return
        targets = list(self.profile.notify_uuids)
        if self.profile.subscribe_all_notifiable:
            for svc in self._client.services:
                for ch in svc.characteristics:
                    props = [p.lower() for p in (ch.properties or [])]
                    if "notify" in props or "indicate" in props:
                        targets.append(str(ch.uuid))
        seen = set()
        unique = []
        for u in targets:
            ul = u.lower()
            if ul not in seen:
                seen.add(ul)
                unique.append(u)

        # Glucose: app toggles CCCD off then on with 50/300 ms pacing
        glucose_order = self.toolkit_profile == "beurer_glucose"

        for uuid in unique:
            try:
                if glucose_order:
                    # stop first if already on (reset path)
                    try:
                        await self._client.stop_notify(uuid)
                    except Exception:
                        pass
                    await asyncio.sleep(self.timing.between_cccd_s)  # ~50ms class
                await self._client.start_notify(uuid, self._on_notify)
                self._subscribed.append(uuid)
                log.info("[CCCD] enabled %s", uuid)
                await asyncio.sleep(self.timing.after_cccd_s)  # ~300ms
            except Exception as exc:  # noqa: BLE001
                if _is_auth_error(exc):
                    self._auth_error = True
                log.warning("[CCCD] %s failed: %s", uuid, exc)
                await asyncio.sleep(self.timing.between_cccd_s)

    async def _post_subscribe_commands(self) -> None:
        if not self._client:
            return
        pid = self.toolkit_profile

        if pid == "beurer_glucose":
            delay = self.timing.before_history_query_s
            if delay:
                log.info(
                    "[TIMING] pre-RACP settle %.1fs (mg=%s)",
                    delay,
                    self.caps.glucose_long_racp,
                )
                await asyncio.sleep(delay)
            last = None
            if not self.force_full_glucose_history:
                last = glucose_last_seq(self.address, self.model_id)
            try:
                if last is not None and last >= 0:
                    # incremental: report records with sequence ≥ last+1
                    nxt = last + 1
                    cmd = glucose_mod.racp_report_ge_sequence(nxt)
                    log.info(
                        "[RACP] incremental ≥ seq %d (last stored %d)  %s",
                        nxt,
                        last,
                        format_hex_dump(cmd),
                    )
                else:
                    cmd = glucose_mod.racp_report_all()
                    log.info("[RACP] report all stored records  %s", format_hex_dump(cmd))
                await self._client.write_gatt_char(
                    glucose_mod.RACP_UUID, cmd, response=True
                )
            except Exception as exc:  # noqa: BLE001
                log.error("[RACP] write failed: %s", exc)
                if _is_auth_error(exc):
                    self._auth_error = True

        if pid == "beurer_scale":
            w = self.profile.write_uuid
            if w:
                try:
                    await asyncio.sleep(self.timing.write_pacing_s)
                    await self._client.write_gatt_char(
                        w, scale_mod.cmd_get_measurement(), response=True
                    )
                    log.info("[SCALE] GET_MEASUREMENT 0x41")
                except Exception as exc:  # noqa: BLE001
                    log.warning("[SCALE] GET_MEASUREMENT: %s", exc)

        if pid == "beurer_po60":
            await asyncio.sleep(0.5)
            await self._po60_request_more()

    async def listen(self) -> None:
        quiet = self.timing.quiet_timeout_s
        max_s = self.duration
        min_before = self.timing.min_listen_before_quiet_s
        self._session_start_mono = time.monotonic()
        deadline = self._session_start_mono + max_s
        log.info(
            "[LISTEN] max=%.0fs quiet=%.1fs min_before_quiet=%.1fs",
            max_s,
            quiet,
            min_before,
        )
        while True:
            now = time.monotonic()
            if now >= deadline:
                log.info("[LISTEN] max duration reached")
                break
            # If link dropped and we already have data, end (partial success)
            if self._link_dropped and (self.readings or self.raw):
                log.info("[LISTEN] link dropped with data — ending partial sync")
                break
            if not self._client or not self._client.is_connected:
                if self.readings or self.raw:
                    self._link_dropped = True
                    break
                # wait a bit for reconnect? app does not auto-reconnect mid BP dump
                log.warning("[LISTEN] not connected")
                break
            elapsed = now - self._session_start_mono
            if (
                quiet > 0
                and self._last_notif_mono is not None
                and elapsed >= min_before
                and (now - self._last_notif_mono) >= quiet
                and (self.readings or self.raw)
            ):
                log.info(
                    "[LISTEN] quiet %.1fs — sync complete (%d readings, %d raw)",
                    quiet,
                    len(self.readings),
                    len(self.raw),
                )
                break
            if self._po60_need_more:
                self._po60_need_more = False
                await self._po60_request_more()
            await asyncio.sleep(0.2)

    async def disconnect(self) -> None:
        if not self._client:
            return
        for uuid in list(self._subscribed):
            try:
                await self._client.stop_notify(uuid)
            except Exception:
                pass
        self._subscribed.clear()
        try:
            if self._client.is_connected:
                await self._client.disconnect()
                log.info("[DISCONNECT] clean  ts=%s", ms_timestamp())
        except Exception as exc:  # noqa: BLE001
            log.warning("[DISCONNECT] %s", exc)
        self._client = None

    def _finalize_readings(self) -> int:
        """Dedup BP/glucose; persist BP keys. Returns dropped count."""
        pid = self.toolkit_profile
        dropped = 0
        if pid in ("beurer_bp", "beurer_bm54", "beurer_ecg"):
            prior = bp_seen_keyset(self.address, self.model_id)
            kept, dropped, new_keys = dedupe_readings(
                self.readings, prior_keys=prior, key_fn=bp_dedup_key
            )
            if dropped:
                log.info("[DEDUP] dropped %d duplicate BP reading(s)", dropped)
            self.readings = kept
            remember_bp_keys(self.address, new_keys, self.model_id)
        elif pid == "beurer_glucose":
            kept, dropped, _ = dedupe_readings(
                self.readings, prior_keys=None, key_fn=glucose_dedup_key
            )
            if dropped:
                log.info("[DEDUP] dropped %d duplicate glucose reading(s)", dropped)
            self.readings = kept
            # max sequence already stored on each parse
        return dropped

    async def run(self) -> SyncResult:
        log.info(
            "=" * 60
            + "\n  BEURER companion session\n"
            + f"  model={self.model_id}  profile={self.toolkit_profile}\n"
            + f"  address={self.address}  pair={self.pair}  passkey_hint={self.passkey_hint}\n"
            + f"  settle={self.timing.post_connect_settle_s}s  "
            + f"quiet={self.timing.quiet_timeout_s}s\n"
            + "=" * 60
        )
        try:
            await self.connect()
            await self._settle()
            await self._pair_if_needed()
            await self._read_dis()
            await self._set_time_if_needed()
            await self._subscribe()
            await self._post_subscribe_commands()
            await self.listen()
        except Exception as exc:  # noqa: BLE001
            self._last_error = exc
            if _is_auth_error(exc):
                self._auth_error = True
            if not self._connect_ok:
                log.error("[SESSION] connect aborted: %s: %s", type(exc).__name__, exc)
            else:
                log.error("[SESSION] aborted: %s: %s", type(exc).__name__, exc)
        finally:
            await self.disconnect()

        dropped = self._finalize_readings()
        result = classify_sync(
            model_id=self.model_id,
            address=self.address,
            readings=self.readings,
            raw_count=len(self.raw),
            paired_attempt=self._paired_attempt,
            connect_ok=self._connect_ok,
            link_dropped=self._link_dropped,
            auth_error=self._auth_error,
            passkey_hint=self.passkey_hint,
            deduped_dropped=dropped,
            error=self._last_error,
        )
        self.last_result = result
        log.info("[SYNC] %s", result.summary())
        if result.status == SyncStatus.PAIRING_REQUIRED and self.passkey_hint:
            log.info(
                "[SYNC] Tip: %s Then re-run and enter 6-digit passkey from cuff LCD.",
                remove_bond_instructions(),
            )
        return result


def _brief(result: Any) -> str:
    if hasattr(result, "to_dict"):
        d = result.to_dict()
        keys = (
            "systolic",
            "diastolic",
            "pulse_rate",
            "concentration",
            "object_temperature",
            "spo2_avg",
            "type",
            "sequence",
        )
        parts = [f"{k}={d[k]}" for k in keys if k in d and d[k] is not None]
        return type(result).__name__ + "{" + ", ".join(parts[:6]) + "}"
    if isinstance(result, dict):
        return "dict{" + ", ".join(f"{k}={v}" for k, v in list(result.items())[:5]) + "}"
    return repr(result)[:120]


async def run_beurer_sync(
    address: str,
    model_id: str = "BM54",
    **kwargs: Any,
) -> SyncResult:
    sess = BeurerCompanionSession(address, model_id=model_id, **kwargs)
    return await sess.run()

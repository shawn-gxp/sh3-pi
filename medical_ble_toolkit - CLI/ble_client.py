"""
BLE client (bleak) — connection lifecycle ONLY.

Responsibility boundary
-----------------------
  THIS MODULE: scan, connect, GATT map, subscribe, disconnect, hex logging
  parser.py  : bytearray → dataclasses  (no bleak)

Windows 11 / WinRT notes
------------------------
  bleak on Windows uses WinRT (BluetoothLEDevice, DeviceInformation.Pairing).
  Common failure modes are classified in common/winrt_errors.py:
    - Pairing dialog dismissed / cancelled by user
    - Pairing timeout while waiting for OS popup
    - GATT Unreachable mid service-discovery
    - Access denied when encrypted chars are read without a bond

  ALWAYS log [HEX] before parse, [WINRT] on OS-level failures, and
  millisecond timestamps on every notification for physical-action correlation.

Kotlin port:
  class MedicalBleClient(
      private val parser: VitalParser<*>,
      private val gatt: BluetoothGatt
  ) {
      fun onCharacteristicChanged(uuid: UUID, value: ByteArray) {
          logHex(value)          // forensic FIRST
          val reading = parser.parse(value)
          listener.onReading(reading)
      }
  }
"""

from __future__ import annotations

import argparse
import asyncio
import logging
import platform
import sys
import traceback
from datetime import datetime
from typing import Any, Callable, List, Optional, Set

from bleak import BleakClient, BleakScanner
from bleak.backends.device import BLEDevice
from bleak.exc import BleakError, BleakDeviceNotFoundError

from .common.gatt_map import format_gatt_tree
from .common.hexutil import format_hex_dump, ms_timestamp
from .common.winrt_errors import (
    classify_ble_error,
    format_diagnosis,
    is_windows,
)
from .models import ParseError, RawPayload
from .parser import get_parser, parse as parse_payload
from .profiles import DeviceProfile, get_profile, list_profiles

# Optional post-connect command builders (pure bytes — still no circular bleak deps)
from .parsers import mightysat as mightysat_mod
from .parsers import thermometer as thermo_mod
from .parsers import and_ua651 as and_mod

log = logging.getLogger("medical_ble")


# ---------------------------------------------------------------------------
# Logging setup — forensic millisecond timestamps on EVERY line
# ---------------------------------------------------------------------------

class _MsFormatter(logging.Formatter):
    """
    Force millisecond precision into the log line prefix.

    Terminal appearance:
      [2026-07-15 14:32:01.123] INFO    [HEX]   0x1E 0x70 0x00 ...

    HOW TO USE TIMESTAMPS DURING RE
    --------------------------------
    1. Start a phone stopwatch / screen recording when you begin the session.
    2. Note wall-clock time of each physical action (cuff inflate, M1 press, …).
    3. Grep the terminal for that second, then use .mmm to order packets.
    4. Diff consecutive [HEX] lines that share the same action window.
    """

    def formatTime(self, record, datefmt=None):  # noqa: N802
        dt = datetime.fromtimestamp(record.created)
        return ms_timestamp(dt)


def setup_logging(verbose: bool = True) -> None:
    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(
        _MsFormatter("[%(asctime)s] %(levelname)-7s %(message)s")
    )
    root = logging.getLogger()
    root.handlers.clear()
    root.addHandler(handler)
    root.setLevel(logging.DEBUG if verbose else logging.INFO)


def _log_winrt(exc: BaseException, operation: str) -> None:
    """Emit a multi-line [WINRT] diagnosis block for Windows-specific failures."""
    block = format_diagnosis(exc, operation=operation)
    for line in block.splitlines():
        log.error("%s", line)


# ---------------------------------------------------------------------------
# Scan
# ---------------------------------------------------------------------------

def _is_scanner_busy(exc: BaseException) -> bool:
    msg = str(exc).lower()
    return any(
        s in msg
        for s in (
            "failed to start scanner",
            "watcher status",
            "stopping",
            "aborted",
            "bluetooth turned on",
            "radio",
        )
    )


async def _discover_once(timeout: float) -> Any:
    """
    One BleakScanner.discover attempt.

    Prefer active scanning on Windows (more reliable for medical meters that
    only answer active scans); fall back if the backend rejects the kwarg.
    """
    try:
        return await BleakScanner.discover(
            timeout=timeout, return_adv=True, scanning_mode="active"
        )
    except TypeError:
        return await BleakScanner.discover(timeout=timeout, return_adv=True)


async def scan_devices(
    profile: Optional[DeviceProfile] = None,
    timeout: float = 8.0,
    *,
    retries: int = 3,
) -> List[BLEDevice]:
    """
    Discover peripherals; optionally filter by profile name hints / company ID.

    On Windows the advertisement watcher often starts ABORTED when another app
    held the radio or a previous scan did not release cleanly. We retry a few
    times with a short pause instead of failing in <100ms.

    Returns an empty list on total scanner failure (never crashes the CLI);
    callers can still prompt for a MAC — connect uses BLEDevice (no scan).
    """
    log.info(
        "Scanning for BLE devices (timeout=%.1fs, platform=%s, retries=%d)…",
        timeout,
        platform.system(),
        retries,
    )
    if is_windows():
        log.info(
            "[WINRT] Using Windows Bluetooth stack. Ensure Bluetooth is ON "
            "in Quick Settings and no other app holds exclusive access."
        )

    devices: Any = None
    last_exc: Optional[BaseException] = None
    attempts = max(1, retries)
    for attempt in range(1, attempts + 1):
        try:
            log.info("[SCAN] attempt %d/%d …", attempt, attempts)
            devices = await _discover_once(timeout=timeout)
            last_exc = None
            break
        except (asyncio.TimeoutError, BleakError, OSError) as exc:
            last_exc = exc
            busy = _is_scanner_busy(exc)
            log.warning(
                "[SCAN] attempt %d failed: %s: %s",
                attempt,
                type(exc).__name__,
                exc,
            )
            _log_winrt(exc, operation=f"scan(attempt={attempt})")
            if attempt < attempts:
                delay = 2.0 * attempt
                reason = "scanner/radio busy" if busy else "transient scan error"
                log.info(
                    "[SCAN] %s — waiting %.1fs before retry…",
                    reason,
                    delay,
                )
                await asyncio.sleep(delay)
                continue
            break
        except Exception as exc:  # noqa: BLE001
            last_exc = exc
            log.error("[SCAN] unexpected %s: %s", type(exc).__name__, exc)
            _log_winrt(exc, operation=f"scan(attempt={attempt})")
            break

    if devices is None:
        log.error(
            "Scan failed after %d attempt(s) — adapter off / permissions / "
            "WinRT scanner busy (ABORTED).",
            attempts,
        )
        if last_exc is not None:
            _log_winrt(last_exc, operation="scan")
        if is_windows():
            log.error(
                "[WINRT] Connect still works without scan: pass the MAC address "
                "(toolkit uses BLEDevice → FromBluetoothAddressAsync)."
            )
        return []

    results: List[BLEDevice] = []
    # devices is dict[str, tuple[BLEDevice, AdvertisementData]] when return_adv=True
    if isinstance(devices, dict):
        items = devices.items()
    else:
        # older bleak: list[BLEDevice]
        items = ((getattr(d, "address", str(d)), (d, None)) for d in devices)

    for addr, pair in items:
        if isinstance(pair, tuple) and len(pair) == 2:
            dev, adv = pair
        else:
            dev, adv = pair, None
        name = dev.name or (getattr(adv, "local_name", None) if adv else None) or ""
        mfg = (getattr(adv, "manufacturer_data", {}) or {}) if adv else {}
        rssi = getattr(adv, "rssi", None) if adv else None

        if profile is None:
            results.append(dev)
            log.info("  FOUND  %s  rssi=%s  name=%r", addr, rssi, name)
            continue

        name_ok = False
        if profile.name_hints:
            name_l = name.lower()
            name_ok = any(h.lower() in name_l for h in profile.name_hints)
        company_ok = False
        if profile.company_ids:
            company_ok = any(cid in mfg for cid in profile.company_ids)

        if name_ok or company_ok or (not profile.name_hints and not profile.company_ids):
            results.append(dev)
            log.info(
                "  MATCH  %s  rssi=%s  name=%r  mfg_ids=%s",
                addr,
                rssi,
                name,
                list(mfg.keys()),
            )
        else:
            log.debug("  skip   %s  name=%r", addr, name)

    log.info("Scan complete: %d device(s) listed", len(results))
    return results


# ---------------------------------------------------------------------------
# Client
# ---------------------------------------------------------------------------

class MedicalBleClient:
    """
    Connects to one device, prints GATT map, subscribes, feeds parser.

    Parsing is ALWAYS delegated to parser.py — keep this class free of
    field offsets / SFLOAT / CRC logic so Kotlin can reimplement I/O only.
    """

    def __init__(
        self,
        address: str,
        profile: DeviceProfile,
        on_reading: Optional[Callable[[Any], None]] = None,
        auto_dispatch: bool = False,
        pair: bool = False,
        connect_retries: int = 2,
    ):
        self.address = address
        self.profile = profile
        self.on_reading = on_reading
        self.auto_dispatch = auto_dispatch
        # Windows: call client.pair() after connect (shows OS pairing dialog)
        self.pair = pair
        self.connect_retries = max(1, connect_retries)
        self._parser = get_parser(profile.parser_key)
        self._client: Optional[BleakClient] = None
        self._subscribed: Set[str] = set()
        self.readings: List[Any] = []
        self.raw_payloads: List[RawPayload] = []
        # Monotonic counter so RE logs can refer to "packet #N"
        self._notif_seq: int = 0
        # Quiet-timeout sync (Beurer app BTFlowTimer-style)
        self._last_notif_mono: Optional[float] = None
        # Resolved after GATT map (profile UUIDs may differ per OEM firmware)
        self._resolved_write_uuid: Optional[str] = None
        self._resolved_notify_uuids: List[str] = []

    # -- notification path --------------------------------------------------

    def _notification_handler(self, characteristic: Any, data: bytearray) -> None:
        """
        CRITICAL RE PATH — every notification hits here first.

        LOOK IN THE TERMINAL FOR (in order):
          [TS]    explicit ms timestamp of arrival (also in log prefix)
          [NOTIF] sequence #, char UUID, length
          [HEX]   zero-padded raw bytes BEFORE any parse attempt
          [IDX]   index-aligned dump (debug level) for field-offset hunting
          [PARSE] structured result or exception with payload echo
        """
        # Capture arrival time FIRST — before any other work — so physical
        # action correlation is not skewed by parse cost.
        received_at = datetime.now()
        ts = ms_timestamp(received_at)
        self._notif_seq += 1
        seq = self._notif_seq
        # Restart quiet timer (Beurer: each indication restarts idle window)
        import time as _time

        self._last_notif_mono = _time.monotonic()

        uuid = str(getattr(characteristic, "uuid", characteristic))
        payload = bytes(data) if data is not None else b""
        raw = RawPayload(
            received_at=received_at,
            characteristic_uuid=uuid,
            data=payload,
            device_address=self.address,
            device_name=self.profile.model,
        )
        self.raw_payloads.append(raw)

        # ------------------------------------------------------------------
        # FORENSIC HEX DUMP — ALWAYS, BEFORE PARSING
        # ------------------------------------------------------------------
        # Format: "0x0A 0x1B 0xFF"  (zero-padded, space-separated)
        #
        # HOW TO USE THIS LOG DURING REVERSE ENGINEERING
        # -----------------------------------------------
        # 1. Leave the medical device idle; note any baseline [HEX] traffic.
        # 2. Perform ONE physical action (finish BP, press M1, insert strip,
        #    attach finger oximeter, etc.).
        # 3. Find the new [HEX] line(s) whose [TS] matches that action.
        # 4. Diff against the previous packet — changed offsets = candidate
        #    clinical fields for the Kotlin/parser port.
        # 5. If length changes, expect ParseError until parsers/* is updated.
        # 6. Empty payload (len=0) on Windows can mean a CCCD/security issue —
        #    check [WINRT] logs and pairing state.
        #
        # Grep examples (PowerShell):
        #   Select-String -Path session.log -Pattern '\[HEX\]'
        #   Select-String -Path session.log -Pattern '\[TS\].*14:32'
        # ------------------------------------------------------------------
        hex_dump = format_hex_dump(payload) if payload else "(empty)"

        log.info("[TS]    %s   (packet #%d)", ts, seq)
        log.info(
            "[NOTIF] #=%d char=%s len=%d device=%s",
            seq,
            uuid,
            len(payload),
            self.address,
        )
        log.info("[HEX]   %s", hex_dump)

        # Index-aligned dump: "00:1E 01:70 02:00 …" — pin field offsets
        if payload:
            indexed = " ".join(f"{i:02d}:{b:02X}" for i, b in enumerate(payload))
            log.debug("[IDX]   %s", indexed)
        else:
            log.warning(
                "[HEX]   empty notification — on WinRT this can follow a failed "
                "CCCD write or an encrypted char without bonding. Try --pair."
            )

        # ---- PARSE (isolated try/except — never crash the notify callback) -
        # MightySat (and similar framed protocols) may split one logical frame
        # across multiple BLE notifications, or glue two frames into one ATT PDU.
        # ALWAYS use feed() reassembly when available — never parse raw ATT
        # fragments (Bad SOM 0x11 / truncated 0x77 0x14… are normal wire splits).
        try:
            results: List[Any] = []
            use_feed = callable(getattr(self._parser, "feed", None))
            # Force feed for known stream framers even if duck-type fails oddly
            if not use_feed and getattr(self.profile, "parser_key", "") in (
                "mightysat",
                "masimo",
            ):
                use_feed = callable(getattr(self._parser, "feed", None))

            if self.auto_dispatch and not use_feed:
                results = [
                    parse_payload(
                        payload,
                        profile=None,
                        characteristic_uuid=uuid,
                    )
                ]
            elif use_feed:
                results = list(self._parser.feed(payload) or [])
                if not results:
                    pending = getattr(
                        getattr(self._parser, "_reasm", None), "pending", 0
                    ) or 0
                    if pending:
                        # Not an error — waiting for rest of frame (e.g. 20+2 split)
                        log.debug(
                            "[PARSE] packet=#%d reassembly pending %d byte(s)",
                            seq,
                            pending,
                        )
                    return
            else:
                results = [self._parser.parse(payload)]

            for result in results:
                self.readings.append(result)
                log.info("[PARSE] OK  packet=#%d  →  %s", seq, _brief(result))
                if self.on_reading:
                    try:
                        self.on_reading(result)
                    except Exception as cb_exc:  # noqa: BLE001
                        log.error("[PARSE] on_reading callback failed: %s", cb_exc)
        except ParseError as exc:
            # Should be rare with feed()+CRC reassembly; still log for RE
            msg = str(exc)
            if "Bad SOM" in msg or "Truncated frame" in msg or "too short" in msg:
                log.warning(
                    "[PARSE] packet=#%d framing noise (should reassemble next notif): %s",
                    seq,
                    exc,
                )
            else:
                log.error("[PARSE] ParseError packet=#%d: %s", seq, exc)
                log.error("[PARSE] payload was: %s", hex_dump)
        except IndexError as exc:
            log.error(
                "[PARSE] IndexError packet=#%d (payload length changed?): %s | hex=%s",
                seq,
                exc,
                hex_dump,
            )
        except Exception as exc:  # noqa: BLE001
            log.exception(
                "[PARSE] Unexpected %s packet=#%d: %s | hex=%s",
                type(exc).__name__,
                seq,
                exc,
                hex_dump,
            )

    # -- connection lifecycle (Windows-hardened) ----------------------------

    async def connect(self, timeout: float = 30.0) -> None:
        """
        Connect with WinRT-aware error handling and optional OS pairing.

        Retries are useful on Windows because the first attempt often races
        the device's short advertising window or a delayed pairing dialog.
        """
        log.info(
            "Connecting to %s (profile=%s, timeout=%.0fs, pair=%s, retries=%d)…",
            self.address,
            self.profile.id,
            timeout,
            self.pair,
            self.connect_retries,
        )
        if is_windows():
            log.info(
                "[WINRT] bleak → Windows Runtime Bluetooth APIs. "
                "If a pairing popup appears, ACCEPT it — dismissing it raises BleakError."
            )
            log.info(
                "[WINRT] Tip: Settings → Bluetooth & devices — remove stale entries "
                "for this address if connect loops fail."
            )

        last_exc: Optional[BaseException] = None
        for attempt in range(1, self.connect_retries + 1):
            log.info("[CONNECT] attempt %d/%d …", attempt, self.connect_retries)
            try:
                await self._connect_once(timeout=timeout)
                log.info("[CONNECT] success on attempt %d", attempt)
                return
            except Exception as exc:  # classified below; re-raised after retries
                last_exc = exc
                diag = classify_ble_error(exc)
                log.error(
                    "[CONNECT] attempt %d failed: %s: %s",
                    attempt,
                    type(exc).__name__,
                    exc,
                )
                _log_winrt(exc, operation=f"connect(attempt={attempt})")
                if attempt < self.connect_retries and diag.is_retryable:
                    # Brief pause so WinRT can release the device handle
                    delay = 1.5 * attempt
                    log.info(
                        "[CONNECT] retryable (%s) — waiting %.1fs before next attempt…",
                        diag.category,
                        delay,
                    )
                    await asyncio.sleep(delay)
                    continue
                break

        assert last_exc is not None
        raise last_exc

    def _ble_device(self) -> BLEDevice:
        """
        Wrap MAC as BLEDevice so WinRT uses FromBluetoothAddressAsync.

        BleakClient("AA:BB:…") always starts BluetoothLEAdvertisementWatcher
        first; when the watcher is ABORTED that fails even though the cuff is
        reachable. Omron pair/read already use this path — multi-brand must too.
        """
        return BLEDevice(self.address.strip().upper(), self.profile.model or "device", None)

    async def _connect_once(self, timeout: float) -> None:
        """
        Single connect attempt with explicit WinRT exception branches.

        Exception matrix (Windows):
          asyncio.TimeoutError     — connect() exceeded timeout (dialog slow / not adv)
          BleakDeviceNotFoundError — address not in radio range / wrong MAC
          BleakError               — dialog dismissed, unreachable, access denied, …
          OSError                  — adapter/stack issues
          Exception                — unexpected; log full traceback for RE
        """
        # Always prefer BLEDevice target — skips WinRT scan on connect.
        target: Any = self._ble_device() if is_windows() else self.address
        if is_windows():
            log.info(
                "[WINRT] Connecting via BLEDevice(%s) — no advertisement scan",
                self.address,
            )

        self._client = BleakClient(
            target,
            timeout=timeout,
            disconnected_callback=self._on_disconnect,
        )

        # ---- 1) TCP/BLE link connect --------------------------------------
        try:
            await self._client.connect()
        except asyncio.TimeoutError as exc:
            # WinRT: FromBluetoothAddressAsync / connect can hang until timeout
            # when the device stopped advertising or pairing UI is waiting.
            log.error(
                "[CONNECT] TIMEOUT after %.0fs — device not advertising, out of "
                "range, or Windows pairing dialog left unanswered.",
                timeout,
            )
            _log_winrt(exc, operation="connect/timeout")
            log.error(
                "[WINRT] If a pairing popup was open: accept it next time, or "
                "dismiss then re-run with a longer --connect-timeout."
            )
            raise
        except BleakDeviceNotFoundError as exc:
            log.error("[CONNECT] Device not found: %s", self.address)
            _log_winrt(exc, operation="connect/not_found")
            raise
        except BleakError as exc:
            # Most common path for "user cancelled pairing", Unreachable, etc.
            log.error("[CONNECT] BleakError: %s", exc)
            _log_winrt(exc, operation="connect")
            # Explicit callout when the OS dialog was dismissed
            diag = classify_ble_error(exc)
            if diag.category == "PAIRING_DIALOG_DISMISSED":
                log.error(
                    "[WINRT] ★ Pairing dialog was dismissed. Re-run, watch the "
                    "taskbar for the Bluetooth popup, and click Connect/Yes."
                )
            raise
        except OSError as exc:
            log.error("[CONNECT] OSError (adapter/stack): %s", exc)
            _log_winrt(exc, operation="connect/os")
            raise
        except Exception as exc:  # noqa: BLE001
            log.error(
                "[CONNECT] Unexpected %s: %s",
                type(exc).__name__,
                exc,
            )
            _log_winrt(exc, operation="connect/unexpected")
            log.debug("[CONNECT] traceback:\n%s", traceback.format_exc())
            raise

        if not self._client.is_connected:
            # Defensive: some WinRT builds return without raising
            exc = BleakError(
                f"connect() returned but is_connected=False for {self.address}"
            )
            _log_winrt(exc, operation="connect/not_connected")
            raise exc

        log.info("[CONNECT] link up  is_connected=%s", self._client.is_connected)

        # ---- 2) Optional OS-level pairing / bonding -----------------------
        # Windows shows a system dialog; dismissing it → BleakError.
        if self.pair:
            await self._pair_winrt()

        # ---- 3) GATT discovery map (forensic) -----------------------------
        await self._print_gatt_map()

    async def _pair_winrt(self) -> None:
        """
        Call BleakClient.pair() with WinRT-specific guards.

        protection_level=2 ≈ encryption + authentication (Windows).
        If the user dismisses the OS dialog, bleak raises BleakError — we
        catch it, log remediation, and re-raise so the session aborts cleanly
        rather than continuing half-bonded.

        Common WinRT race (seen on MightySat): connect succeeds, then pair()
        tears down the GATT session and bleak hits
        AttributeError: 'NoneType' object has no attribute 'device_information'.
        That is retryable — the next connect often reports already paired.
        """
        if not self._client:
            return
        if not hasattr(self._client, "pair"):
            log.warning(
                "[PAIR] BleakClient.pair() not available on this backend — "
                "pair manually in Windows Settings → Bluetooth & devices."
            )
            return

        # Already bonded? Skip pair() to avoid the mid-pair disconnect race.
        try:
            if getattr(self._client, "is_paired", False):
                log.info("[PAIR] Device already paired — skipping pair().")
                return
        except Exception:  # noqa: BLE001
            pass

        log.info(
            "[PAIR] Requesting OS pairing (Windows may show a popup — ACCEPT it)…"
        )
        log.info(
            "[PAIR] If you dismiss/cancel the dialog, expect BleakError "
            "PAIRING_DIALOG_DISMISSED and a failed session."
        )
        try:
            # protection_level is honored on Windows; ignored or rejected elsewhere
            try:
                await self._client.pair(protection_level=2)
            except TypeError:
                # Older bleak: pair() takes no kwargs
                await self._client.pair()
            log.info("[PAIR] pair() completed OK")
        except asyncio.TimeoutError as exc:
            log.error(
                "[PAIR] TIMEOUT — pairing dialog not confirmed in time, or "
                "device left pairing mode."
            )
            _log_winrt(exc, operation="pair/timeout")
            raise
        except BleakError as exc:
            diag = classify_ble_error(exc)
            log.error("[PAIR] BleakError during pair(): %s", exc)
            _log_winrt(exc, operation="pair")
            if diag.category == "PAIRING_DIALOG_DISMISSED":
                log.error(
                    "[WINRT] ★ You (or a timeout) dismissed the Windows pairing "
                    "dialog. Re-run with --pair and accept the popup."
                )
            # Some stacks raise if already paired — treat as soft success
            msg = str(exc).lower()
            if "already" in msg and "pair" in msg:
                log.warning("[PAIR] Already paired — continuing.")
                return
            raise
        except AttributeError as exc:
            # WinRT: session closed during pair → _bleak_backend fields are None
            msg = str(exc)
            if "device_information" in msg or not getattr(
                self._client, "is_connected", False
            ):
                log.warning(
                    "[PAIR] Link dropped mid-pair (WinRT race: %s). "
                    "Often the bond still completed — retry will skip if already paired.",
                    msg,
                )
                raise BleakError(
                    "Pair interrupted: GATT session closed mid-pair (retryable)"
                ) from exc
            log.error("[PAIR] Unexpected AttributeError: %s", exc)
            _log_winrt(exc, operation="pair/unexpected")
            raise
        except Exception as exc:  # noqa: BLE001
            log.error("[PAIR] Unexpected %s: %s", type(exc).__name__, exc)
            _log_winrt(exc, operation="pair/unexpected")
            raise

    def _iter_gatt_chars(self) -> List[tuple[Any, Any]]:
        """List of (service, characteristic) from the live client."""
        out: List[tuple[Any, Any]] = []
        if not self._client:
            return out
        try:
            services = self._client.services
        except Exception:  # noqa: BLE001
            return out
        if not services:
            return out
        for svc in services:
            for ch in getattr(svc, "characteristics", None) or []:
                out.append((svc, ch))
        return out

    @staticmethod
    def _uuid_close(a: str, b: str) -> bool:
        """Match full UUIDs or 16-bit fragments (e.g. 1524)."""
        al = (a or "").lower().replace("-", "")
        bl = (b or "").lower().replace("-", "")
        if not al or not bl:
            return False
        if al == bl:
            return True
        # 16-bit assigned numbers appear as 0000XXXX…
        if len(al) >= 8 and len(bl) >= 8 and al[4:8] == bl[4:8]:
            # only treat as match when the 16-bit nibble is non-generic
            nibble = al[4:8]
            if nibble not in ("0000", "1800", "1801", "180a"):
                return nibble in al and nibble in bl
        # fragment hints from datasheets (1523/1524, 2a1c, …)
        for frag in ("1523", "1524", "2a1c", "2a1d", "2a1e"):
            if frag in al and frag in bl:
                return True
        return False

    def _resolve_io_uuids(self) -> None:
        """
        Map profile write/notify UUIDs onto characteristics actually present.

        NT-100B TICD docs say 0x1523/0x1524, but some meters expose a different
        base UUID or only a generic serial-over-BLE write+notify pair. If the
        exact UUID is missing, fall back so LIVE does not thrash on CharNotFound.
        """
        chars = self._iter_gatt_chars()
        if not chars:
            log.warning(
                "[GATT] No characteristics discovered — cannot resolve write/notify UUIDs. "
                "Link may have dropped mid-discovery; re-advertise and retry."
            )
            self._resolved_write_uuid = self.profile.write_uuid
            self._resolved_notify_uuids = list(self.profile.notify_uuids)
            return

        wanted_n = [str(u).lower() for u in self.profile.notify_uuids]
        wanted_w = (self.profile.write_uuid or "").lower()

        notify_hits: List[str] = []
        write_hit: Optional[str] = None
        serial_candidates: List[str] = []  # write+notify on same char
        any_notify: List[str] = []
        any_write: List[str] = []

        for svc, ch in chars:
            uid = str(ch.uuid)
            ul = uid.lower()
            props = [p.lower() for p in (ch.properties or [])]
            can_n = "notify" in props or "indicate" in props
            can_w = "write" in props or "write-without-response" in props
            if can_n:
                any_notify.append(uid)
            if can_w:
                any_write.append(uid)
            if can_n and can_w:
                serial_candidates.append(uid)

            for w in wanted_n:
                if self._uuid_close(ul, w) and can_n and uid not in notify_hits:
                    notify_hits.append(uid)
            if wanted_w and self._uuid_close(ul, wanted_w) and can_w:
                write_hit = uid

            # Datasheet fragment match even if full base UUID differs
            svc_u = str(getattr(svc, "uuid", "")).lower()
            if ("1524" in ul or "1523" in svc_u) and can_n and uid not in notify_hits:
                notify_hits.append(uid)
            if ("1524" in ul or "1523" in svc_u) and can_w and not write_hit:
                write_hit = uid

        if not notify_hits and serial_candidates:
            notify_hits = list(serial_candidates)
            log.warning(
                "[GATT] Profile notify UUID not present — using write+notify serial char(s): %s",
                ", ".join(notify_hits[:4]),
            )
        if not notify_hits and any_notify:
            notify_hits = list(any_notify)
            log.warning(
                "[GATT] Falling back to ALL notifiable characteristics (%d)",
                len(notify_hits),
            )
        if not write_hit:
            if serial_candidates:
                write_hit = serial_candidates[0]
                log.warning(
                    "[GATT] Profile write UUID not present — using serial char %s",
                    write_hit,
                )
            elif any_write:
                write_hit = any_write[0]
                log.warning(
                    "[GATT] Profile write UUID not present — using first writable %s",
                    write_hit,
                )
            else:
                write_hit = self.profile.write_uuid

        if not notify_hits:
            notify_hits = list(self.profile.notify_uuids)

        self._resolved_notify_uuids = notify_hits
        self._resolved_write_uuid = write_hit
        log.info(
            "[GATT] Resolved I/O  write=%s  notify=%s",
            self._resolved_write_uuid,
            self._resolved_notify_uuids,
        )

    async def _print_gatt_map(self) -> None:
        """
        Iterate all services/characteristics and print the visual property tree.

        On Windows, an empty tree right after connect is a strong signal that
        GetGattServicesAsync failed silently or the link dropped (Unreachable).
        """
        if not self._client:
            return
        log.info("[GATT] Enumerating services / characteristics / properties…")
        try:
            # Prefer async get_services on older bleak; .services is cached on newer
            services = self._client.services
            if services is None or (hasattr(services, "__len__") and len(list(services)) == 0):
                # Force refresh if the cache looks empty (WinRT race)
                get_services = getattr(self._client, "get_services", None)
                if callable(get_services):
                    log.info("[GATT] services cache empty — calling get_services()…")
                    try:
                        services = await get_services()
                    except BleakError as exc:
                        log.error("[GATT] get_services() BleakError: %s", exc)
                        _log_winrt(exc, operation="get_services")
                        raise
            tree = format_gatt_tree(services)
            for line in tree.splitlines():
                log.info("%s", line)
            # Map profile UUIDs → what is actually on the radio
            self._resolve_io_uuids()
        except BleakError as exc:
            log.error("[GATT] Failed to build discovery map: %s", exc)
            _log_winrt(exc, operation="gatt_discovery")
            raise
        except Exception as exc:  # noqa: BLE001
            log.error(
                "[GATT] Unexpected failure building discovery map: %s: %s",
                type(exc).__name__,
                exc,
            )
            _log_winrt(exc, operation="gatt_discovery/unexpected")

    def _on_disconnect(self, client: BleakClient) -> None:
        """
        bleak disconnected_callback — may run on a different thread/loop tick.

        Common on Windows when:
          - Device sleep timer expires after measurement upload
          - Link drops mid-transfer (Unreachable follow-up)
          - User turns Bluetooth off
        """
        log.warning(
            "[DISCONNECT] Device %s disconnected (was_connected=%s)  ts=%s",
            self.address,
            getattr(client, "is_connected", "?"),
            ms_timestamp(),
        )
        if is_windows():
            log.warning(
                "[WINRT] Unexpected disconnect: check device sleep, range, or "
                "pairing. Re-advertise and re-run if mid-session."
            )

    async def subscribe(self) -> None:
        if not self._client or not self._client.is_connected:
            raise RuntimeError("Not connected")

        # Prefer UUIDs resolved from the live GATT tree (handles NT-100B OEM variants)
        if not self._resolved_notify_uuids:
            self._resolve_io_uuids()

        targets: List[str] = list(
            self._resolved_notify_uuids or self.profile.notify_uuids
        )

        if self.profile.subscribe_all_notifiable:
            log.info("RE mode: discovering all notify/indicate characteristics…")
            for svc in self._client.services:
                for ch in svc.characteristics:
                    props = [p.lower() for p in (ch.properties or [])]
                    if "notify" in props or "indicate" in props:
                        targets.append(str(ch.uuid))

        # De-dupe preserving order
        seen: Set[str] = set()
        unique_targets: List[str] = []
        for u in targets:
            ul = u.lower()
            if ul not in seen:
                seen.add(ul)
                unique_targets.append(u)

        if not unique_targets:
            log.warning(
                "No notify/indicate UUIDs configured. "
                "Use profile 're_generic' or set subscribe_all_notifiable."
            )
            return

        for uuid in unique_targets:
            # start_notify → WinRT writes CCCD (0x2902). Failures often mean
            # missing bond / link already dead / char not notifiable.
            try:
                await self._client.start_notify(uuid, self._notification_handler)
                self._subscribed.add(uuid)
                log.info("[SUBSCRIBE] OK  %s", uuid)
            except BleakError as exc:
                msg = str(exc).lower()
                if "not found" in msg or "characteristic" in msg:
                    log.error(
                        "[SUBSCRIBE] FAIL %s → characteristic not on this device. "
                        "Check [GATT] map above — MAC may not be NT-100B TICD, "
                        "or firmware uses a different UUID.",
                        uuid,
                    )
                else:
                    log.error("[SUBSCRIBE] FAIL %s → BleakError: %s", uuid, exc)
                _log_winrt(exc, operation=f"start_notify({uuid})")
            except Exception as exc:  # noqa: BLE001
                log.error(
                    "[SUBSCRIBE] FAIL %s → %s: %s",
                    uuid,
                    type(exc).__name__,
                    exc,
                )
                _log_winrt(exc, operation=f"start_notify({uuid})")

        # Last-chance: nothing subscribed → try every notifiable char
        if not self._subscribed:
            log.warning(
                "[SUBSCRIBE] No CCCD enabled yet — trying all notifiable characteristics…"
            )
            for svc, ch in self._iter_gatt_chars():
                props = [p.lower() for p in (ch.properties or [])]
                if "notify" not in props and "indicate" not in props:
                    continue
                uid = str(ch.uuid)
                if uid.lower() in {s.lower() for s in self._subscribed}:
                    continue
                try:
                    await self._client.start_notify(uid, self._notification_handler)
                    self._subscribed.add(uid)
                    log.info("[SUBSCRIBE] OK (fallback) %s", uid)
                except Exception as exc:  # noqa: BLE001
                    log.debug("[SUBSCRIBE] fallback skip %s: %s", uid, exc)

        if not self._subscribed:
            log.error(
                "[SUBSCRIBE] Still no notifications enabled. "
                "Device GATT does not match profile %s. "
                "Run RE mode or SCAN once and verify the MAC is the NT-100B.",
                self.profile.id,
            )

    async def _write_bytes(
        self,
        uuid: str,
        data: bytes,
        *,
        response: bool = False,
        label: str = "write",
    ) -> None:
        assert self._client is not None
        log.debug(
            "[WRITE] %s → %s  hex=%s",
            label,
            uuid,
            " ".join(f"{b:02X}" for b in data[:24]),
        )
        await self._client.write_gatt_char(uuid, data, response=response)

    async def run_post_connect_setup(self) -> None:
        """
        Device-specific host→peripheral commands (still pure bytes from parsers).

        Sequences follow vendor datasheets so companion-app-like arming works
        without RE guesswork (A&D 5s gate, thermo dual-wake, MightySat stream).
        """
        if not self._client or not self._client.is_connected:
            return
        pid = self.profile.id

        if pid == "mightysat" and self.profile.write_uuid:
            log.info(
                "MightySat: SetClock + GetDeviceInfo + ConfigureStreaming "
                "(CSD-1322B)…"
            )
            try:
                import time as _time

                await self._write_bytes(
                    self.profile.write_uuid,
                    mightysat_mod.cmd_set_clock(int(_time.time())),
                    response=False,
                    label="mightysat_set_clock",
                )
                await asyncio.sleep(0.15)
                await self._write_bytes(
                    self.profile.write_uuid,
                    mightysat_mod.cmd_get_device_info(),
                    response=False,
                    label="mightysat_get_info",
                )
                await asyncio.sleep(0.2)
                await self._write_bytes(
                    self.profile.write_uuid,
                    mightysat_mod.cmd_configure_streaming(),
                    response=False,
                    label="mightysat_stream",
                )
                log.info(
                    "MightySat streaming armed. History: use cmd_get_trend_record "
                    "after device_info session ids (parser returns trend_record dicts)."
                )
            except BleakError as exc:
                log.error("MightySat setup write failed: %s", exc)
                _log_winrt(exc, operation="mightysat_write")

        if pid == "thermometer":
            wu = self._resolved_write_uuid or self.profile.write_uuid
            if not wu:
                log.error(
                    "Thermometer: no writable characteristic on GATT map — "
                    "cannot send TICD dual-wake / history. Wrong MAC or not NT-100B BLE."
                )
            elif not self._subscribed:
                log.error(
                    "Thermometer: no notify CCCD enabled — responses would be lost. "
                    "Check [GATT] map; try PAIR then LIVE, or RE mode for UUID discovery."
                )
            else:
                log.info(
                    "Thermometer NT-100B: dual wake → write clock → storage count → "
                    "history poll (TICD v1.16) via %s …",
                    wu,
                )
                try:
                    # 1-2: any two cmds within 10s enter communication mode
                    frame = thermo_mod.cmd_wakeup_pair()
                    await self._write_bytes(
                        wu, frame, response=False, label="thermo_wake1"
                    )
                    await asyncio.sleep(0.3)
                    await self._write_bytes(
                        wu, frame, response=False, label="thermo_wake2"
                    )
                    await asyncio.sleep(0.3)
                    # 3: sync clock so storage timestamps are meaningful
                    await self._write_bytes(
                        wu,
                        thermo_mod.cmd_write_clock(),
                        response=False,
                        label="thermo_write_clock",
                    )
                    await asyncio.sleep(0.25)
                    # 4: identity helpers (model + SN) — responses via notify
                    await self._write_bytes(
                        wu,
                        thermo_mod.cmd_read_model(),
                        response=False,
                        label="thermo_model",
                    )
                    await asyncio.sleep(0.15)
                    await self._write_bytes(
                        wu,
                        thermo_mod.cmd_read_serial_part1(),
                        response=False,
                        label="thermo_sn1",
                    )
                    await asyncio.sleep(0.15)
                    await self._write_bytes(
                        wu,
                        thermo_mod.cmd_read_serial_part2(),
                        response=False,
                        label="thermo_sn2",
                    )
                    await asyncio.sleep(0.2)
                    # 5: storage count then latest N index pairs (0 = newest)
                    await self._write_bytes(
                        wu,
                        thermo_mod.cmd_read_storage_count(),
                        response=False,
                        label="thermo_count",
                    )
                    await asyncio.sleep(0.35)
                    # Pull latest 15 slots as 0x25/0x26 pairs
                    max_hist = 15
                    for index in range(max_hist):
                        await self._write_bytes(
                            wu,
                            thermo_mod.cmd_read_storage_time(index),
                            response=False,
                            label=f"thermo_time[{index}]",
                        )
                        await asyncio.sleep(0.12)
                        await self._write_bytes(
                            wu,
                            thermo_mod.cmd_read_storage_result(index),
                            response=False,
                            label=f"thermo_result[{index}]",
                        )
                        await asyncio.sleep(0.12)
                    log.info(
                        "Thermometer history poll queued (up to %d latest records). "
                        "Watch [PARSE] for storage_count / ThermometerReading.",
                        max_hist,
                    )
                except BleakError as exc:
                    log.error("Thermometer setup write failed: %s", exc)
                    _log_winrt(exc, operation="thermometer_write")

        if pid == "and_ua651":
            # SDK §3: after encrypt, within 5s — CCCD Indicate (done in subscribe)
            # AND Date Time write → device immediately dumps memory oldest-first.
            log.info(
                "UA-651BLE: writing Date Time (0x2A08) + custom Set Time (0x01) "
                "within 5s gate (CCCD already enabled)…"
            )
            if is_windows() and not self.pair:
                log.warning(
                    "[WINRT] UA-651BLE usually needs OS bonding — re-run with --pair "
                    "if indications never arrive."
                )
            try:
                # Prefer SIG Date Time characteristic
                await self._write_bytes(
                    and_mod.DATE_TIME_UUID,
                    and_mod.encode_date_time_2a08(),
                    response=True,
                    label="and_2a08_date_time",
                )
            except BleakError as exc:
                log.warning(
                    "UA-651BLE 0x2A08 Date Time write failed: %s — trying custom 0x01",
                    exc,
                )
                _log_winrt(exc, operation="and_date_time_2a08")
            try:
                custom_uuid = self.profile.write_uuid or and_mod.AND_CUSTOM_CHAR_UUID
                await self._write_bytes(
                    custom_uuid,
                    and_mod.cmd_set_time(),
                    response=True,
                    label="and_custom_set_time",
                )
                log.info(
                    "UA-651BLE time armed — device should Indicate stored BP "
                    "(oldest first) on 0x2A35. Optional cmds: disconnect/unpair/"
                    "clear mem in parsers.and_ua651."
                )
            except BleakError as exc:
                log.error("UA-651BLE custom Set Time failed: %s", exc)
                _log_winrt(exc, operation="and_custom_set_time")

        if pid == "beurer_bm54":
            log.info(
                "Beurer BP: CCCD Indicate already on — device auto-dumps stored "
                "measurements (no download command; APK BloodPressureDeviceSyncRepoImpl)."
            )
            if is_windows() and not self.pair:
                log.info(
                    "[WINRT] Newer BM54 uses 6-digit passkey — if connect fails, "
                    "retry with --pair and enter the code from the cuff LCD."
                )
            # Optional DIS reads (app path) — best-effort, non-fatal
            await self._read_device_information()

    async def _read_device_information(self) -> None:
        """Best-effort DIS 0x180A string reads (Beurer app metadata path)."""
        if not self._client or not self._client.is_connected:
            return
        dis_chars = {
            "model": "00002a24-0000-1000-8000-00805f9b34fb",
            "serial": "00002a25-0000-1000-8000-00805f9b34fb",
            "firmware": "00002a26-0000-1000-8000-00805f9b34fb",
            "hardware": "00002a27-0000-1000-8000-00805f9b34fb",
            "manufacturer": "00002a29-0000-1000-8000-00805f9b34fb",
        }
        log.info("[DIS] Reading Device Information Service strings (optional)…")
        for label, uuid in dis_chars.items():
            try:
                raw = await self._client.read_gatt_char(uuid)
                text = bytes(raw).decode("utf-8", errors="replace").strip("\x00").strip()
                log.info("[DIS] %s = %r", label, text)
            except Exception as exc:  # noqa: BLE001
                log.debug("[DIS] %s unavailable: %s", label, exc)

    async def listen(
        self,
        duration: float = 60.0,
        *,
        quiet_timeout: Optional[float] = None,
    ) -> None:
        """
        Wait for notifications.

        quiet_timeout: if set (or defaulted for Beurer/A&D BP), end early after
        this many seconds with no new indications once at least one packet
        arrived — matches HealthManager Pro BTFlowTimer quiet-end behavior.
        """
        import time as _time

        # Beurer app restarts idle timer on each packet; constructor arg ~4s quiet
        if quiet_timeout is None and self.profile.id in ("beurer_bm54", "and_ua651"):
            quiet_timeout = 4.0

        log.info(
            "Listening up to %.0fs (quiet_timeout=%s) — device may auto-send history.",
            duration,
            f"{quiet_timeout:.1f}s" if quiet_timeout else "off",
        )
        log.info(
            "RE TIP: Watch for [HEX] lines. Correlate [TS] ms stamps with button presses."
        )
        log.info(
            "RE TIP: Every notification logs [TS] then [HEX] BEFORE [PARSE]."
        )
        deadline = _time.monotonic() + max(0.5, duration)
        try:
            while True:
                now = _time.monotonic()
                if now >= deadline:
                    log.info("Listen max duration reached")
                    break
                if (
                    quiet_timeout is not None
                    and self._last_notif_mono is not None
                    and (now - self._last_notif_mono) >= quiet_timeout
                    and (self.readings or self.raw_payloads)
                ):
                    log.info(
                        "Quiet timeout %.1fs after last indication "
                        "(%d readings, %d raw) — sync complete (Beurer-style).",
                        quiet_timeout,
                        len(self.readings),
                        len(self.raw_payloads),
                    )
                    break
                await asyncio.sleep(0.25)
        except asyncio.CancelledError:
            log.info("Listen cancelled")
            raise

    async def disconnect(self) -> None:
        if not self._client:
            return
        for uuid in list(self._subscribed):
            try:
                await self._client.stop_notify(uuid)
            except Exception as exc:  # noqa: BLE001
                log.debug("stop_notify %s: %s", uuid, exc)
        self._subscribed.clear()
        try:
            if self._client.is_connected:
                await self._client.disconnect()
                log.info("Disconnected cleanly from %s  ts=%s", self.address, ms_timestamp())
        except BleakError as exc:
            log.error("Error during disconnect: %s", exc)
            _log_winrt(exc, operation="disconnect")
        except Exception as exc:  # noqa: BLE001
            log.error("Error during disconnect: %s: %s", type(exc).__name__, exc)

    async def run(
        self,
        duration: float = 60.0,
        connect_timeout: float = 30.0,
    ) -> List[Any]:
        try:
            await self.connect(timeout=connect_timeout)
            await self.subscribe()
            await self.run_post_connect_setup()
            await self.listen(duration=duration)
        except (BleakError, asyncio.TimeoutError, OSError) as exc:
            log.error(
                "Session aborted: %s: %s",
                type(exc).__name__,
                exc,
            )
            _log_winrt(exc, operation="session")
        except Exception as exc:  # noqa: BLE001
            log.error(
                "Session aborted (unexpected): %s: %s",
                type(exc).__name__,
                exc,
            )
            _log_winrt(exc, operation="session/unexpected")
            log.debug("traceback:\n%s", traceback.format_exc())
        finally:
            await self.disconnect()
        log.info(
            "Session done. readings=%d raw_payloads=%d  ts=%s",
            len(self.readings),
            len(self.raw_payloads),
            ms_timestamp(),
        )
        return self.readings


def _brief(result: Any) -> str:
    if hasattr(result, "to_dict"):
        d = result.to_dict()
        # Compact clinical summary
        keys = (
            "systolic", "diastolic", "pulse_rate", "spo2",
            "object_temperature", "blood_glucose_mg_dl", "type", "message_id",
        )
        parts = [f"{k}={d[k]}" for k in keys if k in d and d[k] is not None]
        if not parts:
            parts = [f"{k}={v}" for k, v in list(d.items())[:6]]
        return type(result).__name__ + "{" + ", ".join(parts) + "}"
    if isinstance(result, dict):
        return "dict{" + ", ".join(f"{k}={v}" for k, v in list(result.items())[:6]) + "}"
    return repr(result)


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

async def _async_connect_main(args: argparse.Namespace) -> int:
    """Generic notify-listener path (Beurer / A&D / MightySat / RE)."""
    if args.list_profiles:
        for p in list_profiles():
            print(f"  {p.id:16s}  {p.brand}/{p.model}  parser={p.parser_key}")
            if p.notes:
                print(f"    {p.notes[:100]}…")
        return 0

    profile = get_profile(args.profile) if args.profile else None

    # Omron history is not a passive notify stream — steer operators
    if profile and profile.brand == "omron":
        log.warning(
            "Omron uses a proprietary EEPROM protocol, not passive BLP notify. "
            "Use:  python -m medical_ble_toolkit omron pair|read -d HEM-7143T1 -a <MAC>"
        )

    if args.scan or not args.address:
        try:
            devices = await scan_devices(profile=profile, timeout=args.scan_timeout)
        except (BleakError, OSError, asyncio.TimeoutError) as exc:
            _log_winrt(exc, operation="scan")
            return 2
        if not args.address:
            if not devices:
                log.error(
                    "No devices found. Is Bluetooth on? Is the meter advertising?"
                )
                if is_windows():
                    log.error(
                        "[WINRT] Quick Settings → Bluetooth ON; then wake the device "
                        "and re-scan within its advertising window."
                    )
                return 1
            if profile and devices:
                args.address = devices[0].address
                log.info("Auto-selected %s", args.address)
            else:
                log.info("Pass --address <MAC> to connect (or re-run with --profile).")
                return 0

    if not profile:
        profile = get_profile("re_generic")
        log.warning("No --profile given; using re_generic (subscribe-all)")

    pair = args.pair
    if args.pair is None:
        pair = profile.id in ("beurer_bm54", "and_ua651", "hem7143t1", "omron") and is_windows()
        if pair:
            log.info(
                "[WINRT] Auto-enabling --pair for profile %s on Windows "
                "(pass --no-pair to skip OS bonding).",
                profile.id,
            )

    client = MedicalBleClient(
        address=args.address,
        profile=profile,
        auto_dispatch=args.auto_parse,
        pair=bool(pair),
        connect_retries=args.retries,
    )
    await client.run(duration=args.duration, connect_timeout=args.connect_timeout)

    if client.readings:
        log.info("--- PARSED READINGS SUMMARY ---")
        for i, r in enumerate(client.readings):
            log.info("  [%d] %s", i, _brief(r))
    elif client.raw_payloads:
        log.warning(
            "Received %d raw notification(s) but 0 parsed readings — "
            "check [PARSE] errors and [HEX] dumps above.",
            len(client.raw_payloads),
        )
    else:
        log.warning(
            "No notifications received. Device may need a physical action, "
            "bonding (--pair), or a different characteristic UUID."
        )
    return 0


async def _async_omron_main(args: argparse.Namespace) -> int:
    """Omron pair / read / list-models (backed by omron_bp)."""
    from .omron_bridge import (
        flatten_readings,
        list_omron_models,
        pair_omron,
        read_omron,
    )

    cmd = args.omron_cmd
    if cmd == "list-models":
        rows = list_omron_models()
        print(f"{'MODEL':16s}  {'STACK':8s}  {'PAIRING':12s}  USERS  NOTES")
        print("-" * 88)
        for r in rows:
            print(
                f"{r['model_id']:16s}  {r['stack']:8s}  {r['pairing']:12s}  "
                f"{r['users']:5d}  {r['notes']}"
            )
        print(f"\n{len(rows)} Omron model(s). Use -d <MODEL> with pair/read.")
        return 0

    if not args.device:
        log.error("Omron %s requires -d/--device (e.g. HEM-7143T1)", cmd)
        return 2
    if not args.address:
        log.error("Omron %s requires -a/--address (BLE MAC)", cmd)
        return 2

    if cmd == "pair":
        try:
            await pair_omron(args.address, args.device)
        except Exception as exc:  # noqa: BLE001
            log.error("[OMRON] pair failed: %s: %s", type(exc).__name__, exc)
            _log_winrt(exc, operation="omron_pair")
            return 1
        return 0

    if cmd == "read":
        try:
            all_users = await read_omron(
                args.address,
                args.device,
                find_timeout=args.find_timeout,
                session_retries=args.retries,
                output_dir=args.output,
            )
        except Exception as exc:  # noqa: BLE001
            log.error("[OMRON] read failed: %s: %s", type(exc).__name__, exc)
            _log_winrt(exc, operation="omron_read")
            return 1

        flat = flatten_readings(all_users)
        log.info("--- OMRON READINGS (newest first, all users) ---")
        for i, r in enumerate(flat[:30]):
            log.info("  [%d] %s", i, _brief(r))
        if len(flat) > 30:
            log.info("  … %d more", len(flat) - 30)
        if not flat:
            log.warning("No records decoded — empty EEPROM or wrong model map?")
        return 0

    log.error("Unknown omron command: %s", cmd)
    return 2


async def _async_main(args: argparse.Namespace) -> int:
    setup_logging(verbose=not args.quiet)

    log.info(
        "medical_ble_toolkit starting  platform=%s  python=%s",
        platform.platform(),
        sys.version.split()[0],
    )
    if is_windows():
        log.info(
            "[WINRT] Windows detected — pairing dialogs, Unreachable, and "
            "dialog-dismiss errors will be classified under [WINRT] log tags."
        )

    if getattr(args, "command", None) == "omron":
        return await _async_omron_main(args)
    return await _async_connect_main(args)


def build_arg_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description=(
            "Medical BLE reverse-engineering toolkit (multi-brand). "
            "Brands: Beurer/A&D (BLP), Omron (EEPROM via omron_bp), "
            "MightySat, thermometer, FORA scaffold. "
            "Windows 11: [WINRT] diagnostics for pairing dialog failures."
        ),
    )
    p.add_argument("-v", "--verbose", action="store_true", default=True)
    p.add_argument("-q", "--quiet", action="store_true")

    sub = p.add_subparsers(dest="command")

    sub.add_parser(
        "interactive",
        aliases=["i", "menu", "wizard"],
        help="Interactive prompts (brand/model/action). Default brand: Omron",
    )

    # ---- default-style connect (also as explicit "connect" subcommand) ----
    def _add_connect_args(sp: argparse.ArgumentParser) -> None:
        sp.add_argument(
            "--profile",
            default=None,
            help="beurer_bm54 | and_ua651 | mightysat | thermometer | fora6 | omron | re_generic",
        )
        sp.add_argument("--address", "-a", default=None, help="BLE MAC / address")
        sp.add_argument("--scan", action="store_true")
        sp.add_argument("--scan-timeout", type=float, default=8.0)
        sp.add_argument("--connect-timeout", type=float, default=30.0)
        sp.add_argument("--retries", type=int, default=2)
        sp.add_argument("--duration", "-t", type=float, default=60.0)
        sp.add_argument("--pair", dest="pair", action="store_true", default=None)
        sp.add_argument("--no-pair", dest="pair", action="store_false")
        sp.add_argument("--auto-parse", action="store_true")
        sp.add_argument("--list-profiles", action="store_true")

    connect_p = sub.add_parser(
        "connect",
        help="Scan/connect and listen for GATT notifications (BLP, SpO2, RE)",
    )
    _add_connect_args(connect_p)

    # ---- Omron (reuses earlier omron_bp project) ----
    omron_p = sub.add_parser(
        "omron",
        help="Omron HEM-* pair / read (proprietary EEPROM; uses omron_bp)",
    )
    omron_sub = omron_p.add_subparsers(dest="omron_cmd", required=True)

    omron_sub.add_parser("list-models", help="List ~23 Omron profiles + aliases")

    pair_p = omron_sub.add_parser("pair", help="Pair once (cuff flashing P)")
    pair_p.add_argument("-d", "--device", required=True, help="Model e.g. HEM-7143T1")
    pair_p.add_argument("-a", "--address", required=True, help="BLE MAC")

    read_p = omron_sub.add_parser("read", help="Download history (transfer mode)")
    read_p.add_argument("-d", "--device", required=True, help="Model e.g. HEM-7143T1")
    read_p.add_argument("-a", "--address", required=True, help="BLE MAC")
    read_p.add_argument(
        "-o",
        "--output",
        default=None,
        help="Directory for userN.csv export (optional)",
    )
    read_p.add_argument(
        "--find-timeout",
        type=float,
        default=60.0,
        help="Seconds to wait for live advertising",
    )
    read_p.add_argument("--retries", type=int, default=3)

    # Flat flags for backward compatibility when no subcommand is used
    _add_connect_args(p)
    return p


def _has_direct_flags(argv: List[str]) -> bool:
    """True if user passed flags that imply non-interactive connect mode."""
    markers = (
        "--profile",
        "--address",
        "-a",
        "--scan",
        "--list-profiles",
        "--pair",
        "--no-pair",
        "--auto-parse",
        "--duration",
        "-t",
        "connect",
        "omron",
    )
    return any(a in markers or a.startswith("--profile=") for a in argv)


def main(argv: Optional[List[str]] = None) -> int:
    argv = list(sys.argv[1:] if argv is None else argv)

    # No args (or only -v/-q) → interactive wizard (default brand: Omron)
    if not argv or argv in (["-v"], ["--verbose"], ["-q"], ["--quiet"]):
        from .interactive import main as interactive_main

        return interactive_main()

    if argv and argv[0] in ("interactive", "i", "menu", "wizard"):
        from .interactive import main as interactive_main

        return interactive_main()

    parser = build_arg_parser()
    args = parser.parse_args(argv)

    if getattr(args, "command", None) in ("interactive", "i", "menu", "wizard"):
        from .interactive import main as interactive_main

        return interactive_main()

    # If user ran without subcommand, treat as "connect" when flags present
    if args.command is None:
        if _has_direct_flags(argv):
            args.command = "connect"
        else:
            from .interactive import main as interactive_main

            return interactive_main()

    if args.quiet:
        args.verbose = False
    try:
        return asyncio.run(_async_main(args))
    except KeyboardInterrupt:
        log.warning("Interrupted by user  ts=%s", ms_timestamp())
        return 130


if __name__ == "__main__":
    sys.exit(main())

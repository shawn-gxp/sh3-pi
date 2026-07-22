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
    ble_log_tag,
    classify_ble_error,
    format_diagnosis,
    is_linux,
    is_windows,
    os_pair_supported,
    pairing_ui_hint,
    remove_bond_instructions,
)
from .models import ParseError, RawPayload
from .parser import get_parser, parse as parse_payload
from .profiles import DeviceProfile, get_profile, list_profiles

# Optional post-connect command builders (pure bytes — still no circular bleak deps)
from .parsers import mightysat as mightysat_mod
from .parsers import thermometer as thermo_mod
from .parsers import and_ua651 as and_mod
from .parsers import nipro_common as nipro_mod
from .parsers import nipro_cf as nipro_cf_mod

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
            "not ready",
            "in progress",
            "org.bluez.error.inprogress",
            "org.bluez.error.notready",
            "resource busy",
            "no default bluetooth adapter",
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


class _ScannedBLEDevice(BLEDevice):
    """BLEDevice plus advertisement RSSI (Bleak 0.21+ dropped Device.rssi)."""

    __slots__ = ("rssi",)

    def __init__(
        self,
        address: str,
        name: Optional[str],
        details: Any,
        rssi: Optional[int] = None,
    ) -> None:
        super().__init__(address, name, details)
        self.rssi = rssi


async def scan_devices(
    profile: Optional[DeviceProfile] = None,
    timeout: float = 8.0,
    *,
    retries: int = 3,
    quiet_busy: bool = False,
) -> List[BLEDevice]:
    """
    Discover peripherals; optionally filter by profile name hints / company ID.

    On Windows the advertisement watcher often starts ABORTED when another app
    held the radio or a previous scan did not release cleanly. We retry a few
    times with a short pause instead of failing in <100ms.

    quiet_busy: hub mode — BlueZ InProgress is expected during connect; log at
    debug/warning only (no multi-line ERROR diagnosis spam).

    Returns an empty list on total scanner failure (never crashes the CLI);
    callers can still prompt for a MAC — connect uses BLEDevice (no scan).

    Each result is a BLEDevice with ``.rssi`` set from AdvertisementData when
    available (native Bleak BLEDevice no longer carries RSSI).
    """
    log.info(
        "Scanning for BLE devices (timeout=%.1fs, platform=%s, retries=%d)…",
        timeout,
        platform.system(),
        retries,
    )
    tag = ble_log_tag()
    if is_windows():
        log.info(
            "[%s] Using Windows Bluetooth stack. Ensure Bluetooth is ON "
            "in Quick Settings and no other app holds exclusive access.",
            tag,
        )
    elif is_linux() and not quiet_busy:
        log.info(
            "[%s] Using BlueZ. Adapter must be powered "
            "(bluetoothctl power on). Close other scanners if scan fails.",
            tag,
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
            if quiet_busy and busy:
                log.warning(
                    "[SCAN] attempt %d/%d busy (BlueZ InProgress) — %s",
                    attempt,
                    attempts,
                    exc,
                )
            else:
                log.warning(
                    "[SCAN] attempt %d failed: %s: %s",
                    attempt,
                    type(exc).__name__,
                    exc,
                )
                if not (quiet_busy and busy):
                    _log_winrt(exc, operation=f"scan(attempt={attempt})")
            if attempt < attempts:
                delay = (1.0 * attempt) if (quiet_busy and busy) else (2.0 * attempt)
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
            if not quiet_busy:
                _log_winrt(exc, operation=f"scan(attempt={attempt})")
            break

    if devices is None:
        if quiet_busy and last_exc is not None and _is_scanner_busy(last_exc):
            log.warning(
                "[SCAN] skipped — radio busy (connect in progress). "
                "Will retry next hunt round."
            )
        else:
            log.error(
                "Scan failed after %d attempt(s) — adapter off / permissions / "
                "scanner busy.",
                attempts,
            )
            if last_exc is not None:
                _log_winrt(last_exc, operation="scan")
            log.error(
                "[%s] Connect still works without scan: pass the MAC address directly.",
                ble_log_tag(),
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
        if rssi is None:
            # Fallbacks: older bleak on device, or OS details dict
            rssi = getattr(dev, "rssi", None)
        if rssi is not None:
            try:
                rssi = int(rssi)
            except (TypeError, ValueError):
                rssi = None

        hit = _ScannedBLEDevice(
            address=getattr(dev, "address", None) or str(addr),
            name=name or None,
            details=getattr(dev, "details", None),
            rssi=rssi,
        )

        if profile is None:
            results.append(hit)
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
            results.append(hit)
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
        find_timeout: float = 0.0,
        name_hint: str = "",
    ):
        self.address = address
        self.profile = profile
        self.on_reading = on_reading
        self.auto_dispatch = auto_dispatch
        # OS bond after connect (WinRT dialog / BlueZ agent) when True
        self.pair = pair
        self.connect_retries = max(1, connect_retries)
        # Omron-like: hunt device after measure while Nipro still advertises
        self.find_timeout = max(0.0, float(find_timeout))
        self.name_hint = (name_hint or "").strip()
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

            # A&D UA-651BLE (Nipro pack): custom 0xF001 traffic is NOT BLP 0x2A35
            uuid_l = uuid.lower().replace("-", "")
            if self.profile.id == "and_ua651" and (
                "f001" in uuid_l or "f000" in uuid_l or "233bf00" in uuid_l
            ):
                custom = and_mod.parse_custom_response(payload)
                log.info(
                    "[PARSE] packet=#%d A&D custom (not BP): cmd=0x%02X type=%s",
                    seq,
                    int(custom.get("command") or 0),
                    custom.get("msg_type") or custom.get("type"),
                )
                # Do not store as clinical reading
                return

            # Gate BLP / other parsers on characteristic when can_parse exists
            if (
                not use_feed
                and not self.auto_dispatch
                and hasattr(self._parser, "can_parse")
                and not self._parser.can_parse(payload, uuid)
            ):
                log.debug(
                    "[PARSE] packet=#%d skip %s (can_parse=False for char %s)",
                    seq,
                    getattr(self._parser, "name", type(self._parser).__name__),
                    uuid,
                )
                return

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
                # Pass characteristic UUID when parser supports it (NIPRO CF, etc.)
                try:
                    results = [
                        self._parser.parse(payload, characteristic_uuid=uuid)
                    ]
                except TypeError:
                    results = [self._parser.parse(payload)]

            for result in results:
                if self._should_drop_companion_invalid(result):
                    log.info(
                        "[PARSE] drop invalid/sentinel packet=#%d → %s",
                        seq,
                        _brief(result),
                    )
                    continue
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

        If find_timeout > 0 (Omron-like post-measure), keep trying connect +
        short scans until the device advertises again after a reading.
        """
        import time as _time

        find_budget = self.find_timeout
        total_deadline = (
            _time.monotonic() + find_budget if find_budget > 0 else None
        )
        # During find window use many short attempts (Nipro advertises briefly)
        if find_budget > 0:
            retries = max(self.connect_retries, 8)
            attempt_timeout = min(timeout, 12.0)
        else:
            retries = self.connect_retries
            attempt_timeout = timeout

        log.info(
            "Connecting to %s (profile=%s, timeout=%.0fs, pair=%s, retries=%d, "
            "find_window=%.0fs)…",
            self.address,
            self.profile.id,
            attempt_timeout,
            self.pair,
            retries,
            find_budget,
        )
        if find_budget > 0:
            log.info(
                "[POST-MEASURE] Omron-like hunt: will retry connect/scan for "
                "%.0fs after measure (Nipro BLE window). Keep meter nearby.",
                find_budget,
            )
        tag = ble_log_tag()
        if self.pair:
            log.info("[%s] %s", tag, pairing_ui_hint())
        log.info(
            "[%s] If connect loops fail: %s",
            tag,
            remove_bond_instructions(self.address),
        )

        last_exc: Optional[BaseException] = None
        attempt = 0
        while True:
            attempt += 1
            if total_deadline is not None and _time.monotonic() >= total_deadline:
                log.error(
                    "[POST-MEASURE] find window %.0fs exhausted — device never "
                    "connected. Take a new reading and Sync within the BLE window.",
                    find_budget,
                )
                break
            if total_deadline is None and attempt > retries:
                break
            if total_deadline is not None and attempt > 40:
                break  # safety

            log.info(
                "[CONNECT] attempt %d%s …",
                attempt,
                f"/{retries}" if total_deadline is None else f" (find left {max(0, total_deadline - _time.monotonic()):.0f}s)",
            )
            try:
                # Prefer reconnect when we just saw it advertising
                if attempt > 1 and (find_budget > 0 or attempt <= retries):
                    await self._scan_for_self(timeout=4.0)
                await self._connect_once(timeout=attempt_timeout)
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
                if total_deadline is not None:
                    # Keep hunting through the post-measure window
                    await asyncio.sleep(1.2)
                    continue
                if attempt < retries and diag.is_retryable:
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

    async def _scan_for_self(self, timeout: float = 4.0) -> bool:
        """
        Brief scan to confirm target is advertising (post-measure wake).
        Returns True if address or name_hint seen.
        """
        addr_u = self.address.strip().upper()
        hint = self.name_hint.lower()
        try:
            devices = await scan_devices(profile=None, timeout=timeout, retries=1)
        except Exception as exc:  # noqa: BLE001
            log.debug("[POST-MEASURE] scan probe failed: %s", exc)
            return False
        for d in devices:
            daddr = (getattr(d, "address", "") or "").upper()
            dname = (getattr(d, "name", None) or "") or ""
            if daddr == addr_u:
                log.info(
                    "[POST-MEASURE] target advertising: %s name=%r",
                    daddr,
                    dname,
                )
                return True
            if hint and hint in dname.lower():
                log.info(
                    "[POST-MEASURE] name match %r → %s (expected %s)",
                    dname,
                    daddr,
                    addr_u,
                )
                # Update address if OS reports different form
                if daddr:
                    self.address = daddr
                return True
        log.debug("[POST-MEASURE] not advertising yet (scanned %d)", len(devices))
        return False

    def _connect_target(self) -> Any:
        """
        Platform-correct BleakClient target.

        Windows: BLEDevice(mac) → FromBluetoothAddressAsync (no scanner).
                 BleakClient("AA:BB:…") always starts the advertisement watcher.

        Linux BlueZ: plain MAC string. Fake BLEDevice(..., details=None) crashes:
                 TypeError: 'NoneType' object is not subscriptable
                 (backend does details["path"]).
        """
        mac = self.address.strip().upper()
        if is_windows():
            return BLEDevice(mac, self.profile.model or "device", None)
        return mac

    async def _connect_once(self, timeout: float) -> None:
        """
        Single connect attempt with platform-aware exception handling.

        Exception matrix:
          asyncio.TimeoutError     — connect() exceeded timeout (dialog slow / not adv)
          BleakDeviceNotFoundError — address not in radio range / wrong MAC
          BleakError               — dialog dismissed, unreachable, access denied, …
          OSError                  — adapter/stack issues
          Exception                — unexpected; log full traceback for RE
        """
        target: Any = self._connect_target()
        log.info(
            "[%s] Connecting via %s(%s)",
            ble_log_tag(),
            "BLEDevice" if is_windows() else "MAC string",
            self.address,
        )

        self._client = BleakClient(
            target,
            timeout=timeout,
            disconnected_callback=self._on_disconnect,
        )

        # ---- 1) BLE link connect ------------------------------------------
        try:
            await self._client.connect()
        except asyncio.TimeoutError as exc:
            log.error(
                "[CONNECT] TIMEOUT after %.0fs — device not advertising, out of "
                "range, or pairing prompt left unanswered.",
                timeout,
            )
            _log_winrt(exc, operation="connect/timeout")
            log.error("[%s] %s", ble_log_tag(), pairing_ui_hint())
            raise
        except BleakDeviceNotFoundError as exc:
            log.error("[CONNECT] Device not found: %s", self.address)
            _log_winrt(exc, operation="connect/not_found")
            raise
        except BleakError as exc:
            log.error("[CONNECT] BleakError: %s", exc)
            _log_winrt(exc, operation="connect")
            diag = classify_ble_error(exc)
            if diag.category == "PAIRING_DIALOG_DISMISSED":
                log.error(
                    "[%s] ★ Pairing was dismissed/rejected. Re-run and accept the prompt. %s",
                    ble_log_tag(),
                    pairing_ui_hint(),
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
            # Defensive: some backends return without raising
            exc = BleakError(
                f"connect() returned but is_connected=False for {self.address}"
            )
            _log_winrt(exc, operation="connect/not_connected")
            raise exc

        log.info("[CONNECT] link up  is_connected=%s", self._client.is_connected)

        # ---- 2) Optional OS-level pairing / bonding -----------------------
        # Windows: system dialog; Linux BlueZ: agent / Just-Works.
        if self.pair:
            await self._pair_os()

        # ---- 3) GATT discovery map (forensic) -----------------------------
        await self._print_gatt_map()

    async def _pair_os(self) -> None:
        """
        Call BleakClient.pair() with platform-aware guards.

        Windows: protection_level=2 ≈ encryption + authentication.
        Linux BlueZ: plain pair() / Just-Works; passkey via desktop agent.

        If the user dismisses the OS dialog/agent, bleak raises BleakError — we
        catch it, log remediation, and re-raise so the session aborts cleanly
        rather than continuing half-bonded.
        """
        if not self._client:
            return
        if not hasattr(self._client, "pair"):
            log.warning(
                "[PAIR] BleakClient.pair() not available on this backend — "
                "pair manually via OS Bluetooth settings. %s",
                remove_bond_instructions(self.address),
            )
            return

        # Already bonded? Skip pair() to avoid the mid-pair disconnect race.
        try:
            if getattr(self._client, "is_paired", False):
                log.info("[PAIR] Device already paired — skipping pair().")
                return
        except Exception:  # noqa: BLE001
            pass

        log.info("[PAIR] Requesting OS pairing… %s", pairing_ui_hint())
        # Nipro NBP / A&D / Beurer: companion often still dumps after connect even if
        # OS Just-Works pair() returns AuthenticationFailed (stale bond / agent).
        # Omron FE4A needs a real bond — keep hard-fail there.
        soft_ok_ids = (
            "nipro_nbp",
            "nipro_nmbp",
            "nipro_nsm1",
            "nipro_cf",
            "and_ua651",
            "beurer_bp",
            "beurer_bm54",
            "beurer_glucose",
        )
        try:
            if is_linux():
                # BlueZ needs a pair agent or AuthenticationFailed/Canceled
                from .brands.omron.ble.connection import pair_client as _omron_pair

                await _omron_pair(self._client)
            else:
                # protection_level is honored on Windows
                try:
                    await self._client.pair(protection_level=2)
                except TypeError:
                    await self._client.pair()
            log.info("[PAIR] pair() completed OK")
        except asyncio.TimeoutError as exc:
            log.error(
                "[PAIR] TIMEOUT — pairing prompt not confirmed in time, or "
                "device left pairing mode."
            )
            _log_winrt(exc, operation="pair/timeout")
            if self._soft_pair_continue(soft_ok_ids, exc):
                return
            raise
        except BleakError as exc:
            diag = classify_ble_error(exc)
            log.error("[PAIR] BleakError during pair(): %s", exc)
            _log_winrt(exc, operation="pair")
            if diag.category == "PAIRING_DIALOG_DISMISSED":
                log.error(
                    "[%s] ★ Pairing dismissed/rejected. Re-run with --pair. %s",
                    ble_log_tag(),
                    pairing_ui_hint(),
                )
            msg = str(exc).lower()
            if "already" in msg and "pair" in msg:
                log.warning("[PAIR] Already paired — continuing.")
                return
            if self._soft_pair_continue(soft_ok_ids, exc):
                return
            raise
        except AttributeError as exc:
            # WinRT race: session closed mid-pair
            msg = str(exc)
            if "device_information" in msg or not getattr(
                self._client, "is_connected", False
            ):
                log.warning(
                    "[PAIR] Link dropped mid-pair (%s). "
                    "Often the bond still completed — retry will skip if already paired.",
                    msg,
                )
                if self._soft_pair_continue(soft_ok_ids, exc):
                    return
                raise BleakError(
                    "Pair interrupted: GATT session closed mid-pair (retryable)"
                ) from exc
            log.error("[PAIR] Unexpected AttributeError: %s", exc)
            _log_winrt(exc, operation="pair/unexpected")
            raise
        except Exception as exc:  # noqa: BLE001
            log.error("[PAIR] Unexpected %s: %s", type(exc).__name__, exc)
            _log_winrt(exc, operation="pair/unexpected")
            if self._soft_pair_continue(soft_ok_ids, exc):
                return
            raise

    def _soft_pair_continue(self, soft_ok_ids: tuple, exc: BaseException) -> bool:
        """True = keep session (Nipro/A&D/Beurer companion-style)."""
        pid = getattr(self.profile, "id", "") or ""
        if pid not in soft_ok_ids:
            return False
        if not self._client or not getattr(self._client, "is_connected", False):
            return False
        log.warning(
            "[PAIR] OS pair failed (%s) but link still up for profile=%s — "
            "continuing (companion clock+indicate may work without perfect bond). "
            "If Sync gets no data: %s",
            type(exc).__name__,
            pid,
            remove_bond_instructions(self.address),
        )
        return True

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
        log.warning(
            "[%s] Unexpected disconnect: check device sleep, range, or pairing. "
            "Re-advertise and re-run if mid-session.",
            ble_log_tag(),
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

    def _should_drop_companion_invalid(self, result: Any) -> bool:
        """Drop companion-rejected sentinels (BP 2047, temp 65535, CF control)."""
        pid = self.profile.id
        if getattr(result, "is_control_solution", False):
            return True
        if pid in ("nipro_nbp", "nipro_nmbp", "and_ua651", "beurer_bp", "beurer_bm54"):
            if hasattr(result, "systolic"):
                return nipro_mod.is_invalid_bp_companion(
                    getattr(result, "systolic", None),
                    getattr(result, "diastolic", None),
                    getattr(result, "pulse_rate", None),
                    getattr(result, "measured_at", None),
                )
        if pid in ("nipro_nt100b", "nipro_nsm1"):
            if hasattr(result, "object_temperature"):
                # Companion rejects 65535 / <0; allow missing timestamp (uses now)
                t = getattr(result, "object_temperature", None)
                if t is not None and (t == 65535.0 or t < 0):
                    return True
        return False

    async def run_post_connect_setup(self) -> None:
        """
        Device-specific host→peripheral commands (still pure bytes from parsers).

        Sequences follow vendor datasheets + Nipro げんきノート companion BLELib.
        """
        if not self._client or not self._client.is_connected:
            return
        pid = self.profile.id

        # --- Nipro companion BP (NBP-1BLE / NMBP): 1s → 0x2A08 only ----------
        if pid in ("nipro_nbp", "nipro_nmbp"):
            log.info(
                "Nipro BP (%s): companion path — settle %.0fs then DateTime 0x2A08",
                pid,
                nipro_mod.POST_CONNECT_CLOCK_DELAY_S,
            )
            if pid == "nipro_nmbp" and os_pair_supported() and not self.pair:
                log.warning(
                    "[%s] NMBP usually needs OS bonding — re-run with --pair "
                    "if serial/indications fail.",
                    ble_log_tag(),
                )
            await asyncio.sleep(nipro_mod.POST_CONNECT_CLOCK_DELAY_S)
            try:
                await self._write_bytes(
                    nipro_mod.DATE_TIME_UUID,
                    nipro_mod.encode_date_time_2a08(),
                    response=True,
                    label="nipro_bp_2a08_clock",
                )
                try:
                    back = await self._client.read_gatt_char(nipro_mod.DATE_TIME_UUID)
                    log.info(
                        "Nipro BP clock readback: %s",
                        " ".join(f"{b:02X}" for b in bytes(back)[:8]),
                    )
                except Exception as exc:  # noqa: BLE001
                    log.debug("Nipro BP clock readback skipped: %s", exc)
            except BleakError as exc:
                log.error("Nipro BP 0x2A08 write failed: %s", exc)
                _log_winrt(exc, operation="nipro_bp_clock")
            await self._read_device_information()
            return

        # --- Nipro NSM-1BLE: 1s → HTS 0x2A08 + HTP indicate (already subscribed)
        if pid == "nipro_nsm1":
            log.info(
                "Nipro NSM-1BLE: companion path — settle %.0fs then HTS DateTime 0x2A08",
                nipro_mod.POST_CONNECT_CLOCK_DELAY_S,
            )
            await asyncio.sleep(nipro_mod.POST_CONNECT_CLOCK_DELAY_S)
            try:
                await self._write_bytes(
                    nipro_mod.DATE_TIME_UUID,
                    nipro_mod.encode_date_time_2a08(),
                    response=True,
                    label="nipro_nsm1_2a08_clock",
                )
            except BleakError as exc:
                log.error("Nipro NSM-1BLE clock write failed: %s", exc)
                _log_winrt(exc, operation="nipro_nsm1_clock")
            return

        # --- Nipro NT-100B: HTP listen + TICD pull latest (post-measure recovery)
        if pid == "nipro_nt100b":
            # Problem: companion only enables HTP notify, but the indication often
            # fires at measure time *before* we connect — so HTP-only sync gets 0.
            # Fix: after CCCD, actively pull TICD storage index 0 (latest) while
            # still accepting HTP if it arrives. Then listen() can quiet-end early.
            log.info(
                "Nipro NT-100B: HTP notify already on + TICD pull latest stored "
                "reading (post-measure recovery). Power-off on disconnect."
            )
            wu = self._resolved_write_uuid or self.profile.write_uuid
            if not wu:
                # resolve any writeable serial-like char
                for svc, ch in self._iter_gatt_chars():
                    props = [p.lower() for p in (ch.properties or [])]
                    if "write" in props or "write-without-response" in props:
                        uid = str(ch.uuid).lower().replace("-", "")
                        if "1524" in uid or "fff" in uid:
                            wu = str(ch.uuid)
                            break
            if not wu:
                log.warning(
                    "Nipro NT-100B: no TICD write char — HTP-only mode "
                    "(must measure *while* connected for a reading)"
                )
                return

            async def _tw(data: bytes, label: str) -> None:
                try:
                    await self._write_bytes(wu, data, response=False, label=label)
                except BleakError:
                    await self._write_bytes(
                        wu, data, response=True, label=label + "_rsp"
                    )

            try:
                from .nipro import post_measure as pm

                # Dual-wake into communication mode (TICD §1.1)
                wake = thermo_mod.cmd_wakeup_pair()
                log.info("[NT100B] >>> WAKE1: %s", wake.hex())
                await _tw(wake, "nt100b_wake1")
                await asyncio.sleep(0.45)
                log.info("[NT100B] >>> WAKE2: %s", wake.hex())
                await _tw(wake, "nt100b_wake2")
                await asyncio.sleep(0.55)
                count_cmd = thermo_mod.cmd_read_storage_count()
                log.info("[NT100B] >>> COUNT: %s", count_cmd.hex())
                await _tw(count_cmd, "nt100b_count")
                await asyncio.sleep(0.45)
                count = 1
                for r in self.readings:
                    if isinstance(r, dict) and r.get("type") == "storage_count":
                        try:
                            count = max(1, int(r.get("count") or 1))
                            log.info("[NT100B] storage_count=%d", count)
                        except (TypeError, ValueError):
                            count = 1
                        break

                async def _pull_slot(index: int) -> None:
                    if not self._client or not self._client.is_connected:
                        return
                    if hasattr(self._parser, "set_history_index"):
                        self._parser.set_history_index(index)
                    time_cmd = thermo_mod.cmd_read_storage_time(index)
                    log.info("[NT100B] >>> TIME[%d]: %s", index, time_cmd.hex())
                    await _tw(time_cmd, f"nt100b_time[{index}]")
                    await asyncio.sleep(0.30)
                    result_cmd = thermo_mod.cmd_read_storage_result(index)
                    log.info("[NT100B] >>> RESULT[%d]: %s", index, result_cmd.hex())
                    await _tw(result_cmd, f"nt100b_result[{index}]")
                    await asyncio.sleep(0.30)

                # Index 0 = latest on device (TICD). Pull FIRST so the hub
                # always gets the newest measure even if we later dump history.
                log.info(
                    "[POST-MEASURE] NT-100B TICD pull index 0 (latest) first "
                    "(device count=%s)",
                    count,
                )
                await _pull_slot(0)
                n_latest = sum(
                    1
                    for r in self.readings
                    if getattr(r, "object_temperature", None) is not None
                )
                log.info(
                    "[POST-MEASURE] NT-100B latest slot done — %d temp reading(s) so far",
                    n_latest,
                )

                # Optional older history only if NT100B_HISTORY_MAX > 1
                max_hist = min(count, pm.NT100B_HISTORY_MAX)
                if max_hist > 1:
                    log.info(
                        "[POST-MEASURE] NT-100B TICD history dump slots 1..%d",
                        max_hist - 1,
                    )
                    for index in range(1, max_hist):
                        if not self._client or not self._client.is_connected:
                            log.warning(
                                "NT-100B disconnected mid-history at index %d", index
                            )
                            break
                        await _pull_slot(index)
                else:
                    log.info(
                        "[POST-MEASURE] NT-100B latest-only mode "
                        "(NT100B_HISTORY_MAX=%s) — frees radio for MightySat",
                        pm.NT100B_HISTORY_MAX,
                    )

                n_temp = sum(
                    1
                    for r in self.readings
                    if getattr(r, "object_temperature", None) is not None
                )
                log.info(
                    "[POST-MEASURE] NT-100B dump done — %d temperature(s) total",
                    n_temp,
                )
            except BleakError as exc:
                log.warning(
                    "Nipro NT-100B TICD pull failed (will still listen HTP): %s",
                    exc,
                )
                _log_winrt(exc, operation="nt100b_ticd_pull")
            return

        # --- Nipro CF / Cocoron glucose ---------------------------------------
        if pid == "nipro_cf":
            log.info(
                "Nipro CF: companion path — clock + RACP number/report (All mode)"
            )
            try:
                await self._write_bytes(
                    nipro_cf_mod.CHAR_CURRENT_TIME,
                    nipro_cf_mod.encode_cf_clock(),
                    response=True,
                    label="nipro_cf_clock",
                )
            except BleakError as exc:
                log.warning("Nipro CF clock write failed (continuing): %s", exc)
                _log_winrt(exc, operation="nipro_cf_clock")
            await asyncio.sleep(0.2)
            racp = self.profile.write_uuid or nipro_cf_mod.CHAR_RACP
            try:
                await self._write_bytes(
                    racp,
                    nipro_cf_mod.racp_number_of_records_all(),
                    response=True,
                    label="nipro_cf_racp_count",
                )
                await asyncio.sleep(0.4)
                await self._write_bytes(
                    racp,
                    nipro_cf_mod.racp_report_all(),
                    response=True,
                    label="nipro_cf_racp_report_all",
                )
            except BleakError as exc:
                log.error("Nipro CF RACP failed: %s", exc)
                _log_winrt(exc, operation="nipro_cf_racp")
            return

        if pid == "mightysat" and self.profile.write_uuid:
            # Companion: notify already on → GetInfo → (on rsp) SetClock ticks →
            # (on ACK) EnableStream from device-info[3:6]
            log.info(
                "MightySat: companion order GetInfo → SetClock(ticks) → "
                "EnableStream(from info)…"
            )
            try:
                self._mightysat_device_info: Optional[bytes] = None
                await self._write_bytes(
                    self.profile.write_uuid,
                    mightysat_mod.cmd_get_device_info(),
                    response=False,
                    label="mightysat_get_info",
                )
                # Wait briefly for device_info notify into parser/readings
                await asyncio.sleep(0.45)
                info_payload: Optional[bytes] = None
                for r in reversed(self.readings):
                    if isinstance(r, dict) and r.get("type") == "device_info":
                        # reconstruct minimal payload from parse_device_info fields
                        # Prefer raw from raw_payloads last matching 0x01
                        break
                for rp in reversed(self.raw_payloads):
                    raw = bytes(getattr(rp, "data", b"") or b"")
                    # deframe if needed
                    try:
                        if raw and raw[0] == 0x77:
                            fr = mightysat_mod.deframe(raw)
                            if fr.payload and fr.payload[0] == 0x01:
                                info_payload = fr.payload
                                break
                        elif raw and raw[0] == 0x01:
                            info_payload = raw
                            break
                    except Exception:  # noqa: BLE001
                        continue

                await self._write_bytes(
                    self.profile.write_uuid,
                    mightysat_mod.cmd_set_clock_dotnet_ticks(),
                    response=False,
                    label="mightysat_set_clock_ticks",
                )
                await asyncio.sleep(0.25)
                if info_payload:
                    stream_cmd = mightysat_mod.cmd_enable_stream_from_device_info(
                        info_payload
                    )
                    label = "mightysat_enable_stream_from_info"
                else:
                    stream_cmd = mightysat_mod.cmd_configure_streaming()
                    label = "mightysat_configure_streaming_fallback"
                    log.warning(
                        "MightySat: no device_info yet — fallback ConfigureStreaming"
                    )
                await self._write_bytes(
                    self.profile.write_uuid,
                    stream_cmd,
                    response=False,
                    label=label,
                )
                log.info("MightySat streaming armed (companion-like).")
            except BleakError as exc:
                log.error("MightySat setup write failed: %s", exc)
                _log_winrt(exc, operation="mightysat_write")

        if pid == "thermometer":
            # Lab full TICD history. Prefer profile nipro_nt100b for normal Sync
            # (latest slot only + HTP). Kept for RE / bulk dump.
            wu = self._resolved_write_uuid or self.profile.write_uuid
            if not wu:
                log.error(
                    "Thermometer: no writable characteristic on GATT map — "
                    "cannot send TICD dual-wake / history. Wrong MAC or not NT-100B BLE."
                )
            elif not self._subscribed:
                log.error(
                    "Thermometer: no notify CCCD enabled — responses would be lost. "
                    "Take a reading first (BLE on ~2 min), then PAIR once + SYNC quickly."
                )
            else:
                log.info(
                    "Thermometer LAB: full TICD history via %s "
                    "(for normal Sync use profile nipro_nt100b)",
                    wu,
                )

                async def _tw(data: bytes, label: str) -> None:
                    """WriteWithoutResponse first; fall back to Write with response."""
                    try:
                        await self._write_bytes(
                            wu, data, response=False, label=label
                        )
                    except BleakError:
                        await self._write_bytes(
                            wu, data, response=True, label=label + "_rsp"
                        )

                try:
                    wake = thermo_mod.cmd_wakeup_pair()
                    await _tw(wake, "thermo_wake1")
                    await asyncio.sleep(0.55)
                    await _tw(wake, "thermo_wake2")
                    await asyncio.sleep(0.7)

                    await _tw(thermo_mod.cmd_write_clock(), "thermo_write_clock")
                    await asyncio.sleep(0.35)

                    await _tw(thermo_mod.cmd_read_storage_count(), "thermo_count")
                    await asyncio.sleep(0.6)

                    count = 1
                    for r in self.readings:
                        if isinstance(r, dict) and r.get("type") == "storage_count":
                            try:
                                count = max(0, int(r.get("count") or 0))
                            except (TypeError, ValueError):
                                count = 1
                            break
                    if count <= 0:
                        count = 1
                    # Lab default: latest 5 only (was 20 — slow and floods UI)
                    max_hist = min(count, 5)
                    log.info(
                        "Thermometer LAB: pulling %d history slot(s) (count=%s)",
                        max_hist,
                        count,
                    )

                    for index in range(max_hist):
                        if not self._client or not self._client.is_connected:
                            break
                        if hasattr(self._parser, "set_history_index"):
                            self._parser.set_history_index(index)  # type: ignore[attr-defined]
                        await _tw(
                            thermo_mod.cmd_read_storage_time(index),
                            f"thermo_time[{index}]",
                        )
                        await asyncio.sleep(0.35)
                        await _tw(
                            thermo_mod.cmd_read_storage_result(index),
                            f"thermo_result[{index}]",
                        )
                        await asyncio.sleep(0.35)

                    log.info(
                        "Thermometer LAB poll done: readings_buf=%d",
                        len(self.readings),
                    )
                except BleakError as exc:
                    log.error("Thermometer setup write failed: %s", exc)
                    _log_winrt(exc, operation="thermometer_write")

        if pid == "and_ua651":
            # A&D UA-651BLE (Nipro pack) SDK sequence after encrypt (~5s gate):
            #   1) CCCD Indicate on 0x2A35 (already done in subscribe)
            #   2) Optional: custom 0xA6 buffer mode=1 → up to 30 stored records
            #   3) Date Time 0x2A08 and/or custom 0x01 Set Time
            #   4) Optional 0xE1 request-all — then device Indicates oldest-first
            # Device only sends what is *actually stored* (not empty slots).
            log.info(
                "UA-651BLE (Nipro): buffer=30 + Date Time + Set Time + 0xE1 "
                "within 5s gate (CCCD Indicate already on)…"
            )
            if os_pair_supported() and not self.pair:
                log.warning(
                    "[%s] UA-651BLE usually needs OS bonding — re-run with --pair "
                    "if indications never arrive.",
                    ble_log_tag(),
                )
            custom_uuid = self.profile.write_uuid or and_mod.AND_CUSTOM_CHAR_UUID
            # 0xA6 mode 1 = 30-record buffer (SDK); mode 0 = no buffer (only latest)
            try:
                await self._write_bytes(
                    custom_uuid,
                    and_mod.cmd_set_buffer_size(1),
                    response=True,
                    label="and_set_buffer_30",
                )
                await asyncio.sleep(0.15)
            except BleakError as exc:
                log.warning(
                    "UA-651BLE 0xA6 set buffer(30) failed (may still dump): %s",
                    exc,
                )
                _log_winrt(exc, operation="and_set_buffer_a6")
            try:
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
                await self._write_bytes(
                    custom_uuid,
                    and_mod.cmd_set_time(),
                    response=True,
                    label="and_custom_set_time",
                )
            except BleakError as exc:
                log.error("UA-651BLE custom Set Time failed: %s", exc)
                _log_winrt(exc, operation="and_custom_set_time")
            try:
                await asyncio.sleep(0.2)
                await self._write_bytes(
                    custom_uuid,
                    and_mod.cmd_request_all_memory(),
                    response=True,
                    label="and_request_all_memory_e1",
                )
                log.info(
                    "UA-651BLE armed — expect up to 30 BP Indications on 0x2A35 "
                    "(oldest first; only stored measurements, not empty slots)."
                )
            except BleakError as exc:
                log.warning(
                    "UA-651BLE 0xE1 request-all failed (may still dump after time): %s",
                    exc,
                )
                _log_winrt(exc, operation="and_request_all_e1")

        if pid == "beurer_bm54":
            log.info(
                "Beurer BP: CCCD Indicate already on — device auto-dumps stored "
                "measurements (no download command; APK BloodPressureDeviceSyncRepoImpl)."
            )
            if os_pair_supported() and not self.pair:
                log.info(
                    "[%s] Newer BM54 uses 6-digit passkey — if connect fails, "
                    "retry with --pair and enter the code from the cuff LCD.",
                    ble_log_tag(),
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

    def _latest_spo2_valid(self) -> Optional[bool]:
        """
        MightySat / SpO2 validity for duty-cycle.

        True  — last clinical SpO2 is a real number (UI would show a value)
        False — last SpO2 packet is invalid / sensor-off / None ("-")
        None  — no SpO2-class reading yet
        """
        for r in reversed(self.readings or []):
            spo2 = None
            sensor_off = False
            invalid = False
            if isinstance(r, dict):
                spo2 = r.get("spo2")
                sensor_off = bool(r.get("sensor_off"))
                invalid = bool(r.get("invalid"))
                rtype = r.get("type") or r.get("reading_type") or ""
                # skip non-clinical stream frames
                if rtype in ("waveform", "ack", "nack", "device_info", "raw_message"):
                    continue
            else:
                spo2 = getattr(r, "spo2", None)
                sensor_off = bool(getattr(r, "sensor_off", False))
                invalid = bool(getattr(r, "invalid", False))
            # Only treat objects that look like pulse-ox rows
            if spo2 is None and not sensor_off and not invalid:
                # Could be BP / temp / unrelated — skip if no spo2 field at all
                if not hasattr(r, "spo2") and not (
                    isinstance(r, dict) and "spo2" in r
                ):
                    continue
            if sensor_off or invalid:
                return False
            if spo2 is None:
                return False
            try:
                v = float(spo2)
            except (TypeError, ValueError):
                return False
            # 0 / nonsense treated as dash
            if v <= 0 or v > 100:
                return False
            return True
        return None

    async def listen(
        self,
        duration: float = 60.0,
        *,
        quiet_timeout: Optional[float] = None,
        stream_good_hold_s: Optional[float] = None,
        stream_invalid_exit_s: Optional[float] = None,
        stream_no_data_grace_s: float = 8.0,
    ) -> None:
        """
        Wait for notifications.

        quiet_timeout: if set (or defaulted for Beurer/A&D BP), end early after
        this many seconds with no new indications once at least one packet
        arrived — matches HealthManager Pro BTFlowTimer quiet-end behavior.

        stream_good_hold_s / stream_invalid_exit_s (MightySat hub duty-cycle):
          - accumulate time while SpO2 is valid; exit when good hold reached
          - continuous invalid ("-") for stream_invalid_exit_s → exit
          - no SpO2 yet counts as invalid after stream_no_data_grace_s
        """
        import time as _time

        # Beurer app restarts idle timer on each packet; constructor arg ~4s quiet
        if quiet_timeout is None and self.profile.id == "beurer_bm54":
            quiet_timeout = 4.0
        # A&D can space multi-record Indications; 4s cut dump to 1 reading often
        if quiet_timeout is None and self.profile.id == "and_ua651":
            quiet_timeout = 12.0
        # Nipro companion BP: multi-record dump; allow spacing like A&D
        if quiet_timeout is None and self.profile.id in ("nipro_nbp", "nipro_nmbp"):
            quiet_timeout = 8.0
        # Nipro HT: single reading; NT-100B may already have temp from TICD pull
        if quiet_timeout is None and self.profile.id == "nipro_nsm1":
            quiet_timeout = 6.0
        if quiet_timeout is None and self.profile.id == "nipro_nt100b":
            # After TICD latest pull we already have temps — short quiet is OK.
            # Keep a little room for a late HTP indication if it still arrives.
            has_temp = any(
                getattr(r, "object_temperature", None) is not None
                for r in self.readings
            )
            quiet_timeout = 4.0 if has_temp else 15.0
        # Nipro CF: history may stream then go quiet
        if quiet_timeout is None and self.profile.id == "nipro_cf":
            quiet_timeout = 10.0
        # MightySat / streams: never quiet-end (0.0 forced from run())
        if quiet_timeout == 0.0:
            quiet_timeout = None

        duty = (
            stream_good_hold_s is not None or stream_invalid_exit_s is not None
        )
        log.info(
            "Listening up to %.0fs (quiet_timeout=%s)%s",
            duration,
            f"{quiet_timeout:.1f}s" if quiet_timeout else "off",
            (
                f" — DUTY good={stream_good_hold_s}s invalid_exit={stream_invalid_exit_s}s"
                if duty
                else (
                    " — STREAMING (no early quiet-end)"
                    if self.profile.id in ("mightysat",)
                    else " — device may auto-send history."
                )
            ),
        )
        log.info(
            "RE TIP: Watch for [HEX] lines. Correlate [TS] ms stamps with button presses."
        )
        log.info(
            "RE TIP: Every notification logs [TS] then [HEX] BEFORE [PARSE]."
        )
        listen_start = _time.monotonic()
        deadline = listen_start + max(0.5, duration)
        good_accum = 0.0
        good_slice_start: Optional[float] = None
        invalid_slice_start: Optional[float] = None
        end_reason = ""
        try:
            while True:
                now = _time.monotonic()
                if now >= deadline:
                    end_reason = "max_duration"
                    log.info("Listen max duration reached")
                    break
                # --- MightySat duty-cycle (hub multi-device) ---
                if duty and self.profile.id in ("mightysat",):
                    valid = self._latest_spo2_valid()
                    # After grace, "no packet yet" counts as dash/invalid
                    if valid is None and (now - listen_start) >= float(
                        stream_no_data_grace_s or 8.0
                    ):
                        valid = False
                    if valid is True:
                        invalid_slice_start = None
                        if good_slice_start is None:
                            good_slice_start = now
                        good_accum = now - good_slice_start
                        if (
                            stream_good_hold_s is not None
                            and good_accum >= float(stream_good_hold_s)
                        ):
                            end_reason = "good_hold"
                            log.info(
                                "MightySat duty: %.1fs valid SpO2 — releasing radio",
                                good_accum,
                            )
                            break
                    elif valid is False:
                        good_slice_start = None  # reset continuous good window
                        if invalid_slice_start is None:
                            invalid_slice_start = now
                        inv_age = now - invalid_slice_start
                        if (
                            stream_invalid_exit_s is not None
                            and inv_age >= float(stream_invalid_exit_s)
                        ):
                            end_reason = "invalid_exit"
                            log.info(
                                "MightySat duty: SpO2 '-' / invalid for %.1fs — disconnect",
                                inv_age,
                            )
                            break
                    # valid is None within grace: wait for first sample
                # NT-100B: if post-connect TICD already delivered a temp, finish
                if (
                    self.profile.id == "nipro_nt100b"
                    and any(
                        getattr(r, "object_temperature", None) is not None
                        for r in self.readings
                    )
                    and self._last_notif_mono is not None
                    and (now - self._last_notif_mono) >= (quiet_timeout or 2.5)
                ):
                    log.info(
                        "Nipro NT-100B: have temperature + quiet %.1fs — ending listen",
                        quiet_timeout or 2.5,
                    )
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
            if end_reason:
                self._listen_end_reason = end_reason  # type: ignore[attr-defined]
        except asyncio.CancelledError:
            # Web/HTTP cancel often aborts listen mid-session. Keep any data
            # already collected (TICD dump / HTP) instead of failing the job.
            n = sum(
                1
                for r in self.readings
                if getattr(r, "object_temperature", None) is not None
                or getattr(r, "systolic", None) is not None
            )
            log.info(
                "Listen cancelled — keeping %d reading(s) / %d raw already collected",
                len(self.readings),
                len(self.raw_payloads),
            )
            if n > 0 or self.readings or self.raw_payloads:
                return
            raise

    async def run_pre_disconnect_teardown(self) -> None:
        """Companion teardown writes (e.g. NT-100B power-off) before GATT drop."""
        if not self._client:
            return
        try:
            still_up = bool(self._client.is_connected)
        except Exception:  # noqa: BLE001
            still_up = False
        if not still_up:
            log.debug("teardown skip — link already down")
            return
        pid = self.profile.id
        if pid in ("nipro_nt100b", "thermometer"):
            wu = self._resolved_write_uuid
            if not wu:
                # Re-resolve from live GATT (profile UUID may not match after rediscovery)
                for _svc, ch in self._iter_gatt_chars():
                    props = [p.lower() for p in (ch.properties or [])]
                    uid = str(ch.uuid).lower().replace("-", "")
                    if "1524" in uid and (
                        "write" in props or "write-without-response" in props
                    ):
                        wu = str(ch.uuid)
                        break
            if not wu:
                wu = self.profile.write_uuid or nipro_mod.NT100B_CUSTOM_CHAR
            try:
                log.info(
                    "Nipro NT-100B: TICD power-off via %s (best-effort)",
                    wu,
                )
                # Prefer write-without-response — less likely to fail if link is dying
                try:
                    await self._write_bytes(
                        wu,
                        thermo_mod.cmd_power_off(),
                        response=False,
                        label="nipro_nt100b_power_off",
                    )
                except BleakError:
                    await self._write_bytes(
                        wu,
                        thermo_mod.cmd_power_off(),
                        response=True,
                        label="nipro_nt100b_power_off_rsp",
                    )
                await asyncio.sleep(min(0.4, nipro_mod.NT100B_POST_POWEROFF_DELAY_S))
            except Exception as exc:  # noqa: BLE001
                # Non-fatal: device often disconnects itself after dump
                log.info(
                    "Nipro NT-100B power-off skipped (%s) — data already collected is kept",
                    exc,
                )
        if pid == "nipro_cf" and hasattr(self._parser, "flush_pending"):
            try:
                flushed = self._parser.flush_pending()  # type: ignore[attr-defined]
                for rec in flushed or []:
                    if rec not in self.readings:
                        self.readings.append(rec)
                        log.info("[NIPRO CF] flushed pending: %s", _brief(rec))
            except Exception as exc:  # noqa: BLE001
                log.debug("Nipro CF flush_pending: %s", exc)
        # brief settle for Nipro BP/HT like companion
        if pid.startswith("nipro_"):
            await asyncio.sleep(nipro_mod.POST_DISCONNECT_SETTLE_S)

    async def disconnect(self) -> None:
        if not self._client:
            return
        try:
            await self.run_pre_disconnect_teardown()
        except Exception as exc:  # noqa: BLE001
            log.debug("pre-disconnect teardown: %s", exc)
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
        *,
        quiet_timeout: Optional[float] = None,
        raise_on_error: bool = False,
        stream_good_hold_s: Optional[float] = None,
        stream_invalid_exit_s: Optional[float] = None,
        stream_no_data_grace_s: float = 8.0,
    ) -> List[Any]:
        """
        Full session: connect → subscribe → device setup → listen → disconnect.

        quiet_timeout: pass through to listen(); None = use profile defaults
          (Beurer/A&D early quiet-end). For streaming devices (MightySat) pass
          quiet_timeout=None and a long duration — do NOT use BP quiet-end.
        raise_on_error: if True, re-raise BLE failures (web live needs this).
        stream_*: MightySat hub duty-cycle (good hold / invalid dash exit).
        """
        self._listen_end_reason = ""  # type: ignore[attr-defined]
        # Streaming profiles must not inherit BP quiet-end from a prior default
        if quiet_timeout is None and self.profile.id in ("mightysat",):
            quiet_timeout = 0.0  # sentinel: listen() treats 0 as "off"
        # Post-measure defaults (Omron-like windows for Nipro)
        try:
            from .nipro import post_measure as pm

            pid = self.profile.id
            # find_timeout is caller-controlled (hub sets short/zero after AD;
            # manual Sync may pass a positive window). Do not override here.
            if quiet_timeout is None:
                q = pm.quiet_s_for(pid)
                if q is not None:
                    quiet_timeout = q
            # Prefer longer receive when caller left default 60
            if duration <= 60.0 and pid.startswith("nipro_"):
                duration = max(duration, pm.receive_s_for(pid))
            # Do NOT auto-force a long find_window when caller left find_timeout=0
            # (hub already saw the AD — long hunt eats the BLE window).
            # Auto-hunt only when find_timeout was left at default and caller
            # set a positive value via constructor / web.
        except Exception:  # noqa: BLE001
            pass

        err: Optional[BaseException] = None
        try:
            await self.connect(timeout=connect_timeout)
            # げんきノート NBP/NMBP: settle+clock BEFORE StartUpdates on 0x2A35.
            # (NT-100B / streams keep subscribe-first.)
            clock_before_cccd = self.profile.id in ("nipro_nbp", "nipro_nmbp")
            if clock_before_cccd:
                await self.run_post_connect_setup()
                await asyncio.sleep(0.15)
                await self.subscribe()
            else:
                await self.subscribe()
                await self.run_post_connect_setup()
            # 0.0 means disable quiet-end (continuous stream)
            qt = None if quiet_timeout == 0.0 else quiet_timeout
            await self.listen(
                duration=duration,
                quiet_timeout=qt,
                stream_good_hold_s=stream_good_hold_s,
                stream_invalid_exit_s=stream_invalid_exit_s,
                stream_no_data_grace_s=stream_no_data_grace_s,
            )
        except asyncio.CancelledError:
            # UI/HTTP cancelled mid-listen — still return dump already done in setup
            log.warning(
                "Session cancelled — returning %d reading(s) collected so far",
                len(self.readings),
            )
            err = None  # treat as soft cancel if we have data
            if raise_on_error and not self.readings:
                raise
        except (BleakError, asyncio.TimeoutError, OSError) as exc:
            err = exc
            log.error(
                "Session aborted: %s: %s",
                type(exc).__name__,
                exc,
            )
            _log_winrt(exc, operation="session")
        except Exception as exc:  # noqa: BLE001
            err = exc
            log.error(
                "Session aborted (unexpected): %s: %s",
                type(exc).__name__,
                exc,
            )
            _log_winrt(exc, operation="session/unexpected")
            log.debug("traceback:\n%s", traceback.format_exc())
        finally:
            try:
                await self.disconnect()
            except Exception as disc_exc:  # noqa: BLE001
                log.debug("disconnect after session: %s", disc_exc)
        log.info(
            "Session done. readings=%d raw_payloads=%d  ts=%s",
            len(self.readings),
            len(self.raw_payloads),
            ms_timestamp(),
        )
        if raise_on_error and err is not None:
            raise err
        return self.readings


def _brief(result: Any) -> str:
    if hasattr(result, "to_dict"):
        d = result.to_dict()
        # Compact clinical summary
        keys = (
            "systolic",
            "diastolic",
            "pulse_rate",
            "spo2",
            "object_temperature",
            "blood_glucose_mg_dl",
            "concentration",
            "sequence",
            "type",
            "message_id",
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
                log.error("[%s] Wake the device and re-scan within its advertising window.", ble_log_tag())
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
        pair = profile.id in (
            "beurer_bm54",
            "and_ua651",
            "hem7143t1",
            "omron",
            "nipro_nmbp",
            "beurer_bp",
        ) and os_pair_supported()
        if pair:
            log.info(
                "[%s] Auto-enabling --pair for profile %s "
                "(pass --no-pair to skip OS bonding).",
                ble_log_tag(),
                profile.id,
            )

    # Omron-like post-measure hunt for Nipro (disable with --find-timeout 0)
    find_to = float(getattr(args, "find_timeout", -1))
    if find_to < 0:
        # default: auto window for Nipro profiles
        if profile.id.startswith("nipro_") or profile.id in ("mightysat", "thermometer"):
            from .nipro import post_measure as pm

            find_to = pm.find_window_for(profile.id)
        else:
            find_to = 0.0

    client = MedicalBleClient(
        address=args.address,
        profile=profile,
        auto_dispatch=args.auto_parse,
        pair=bool(pair),
        connect_retries=max(args.retries, 4 if find_to > 0 else 2),
        find_timeout=find_to,
        name_hint=str(getattr(args, "device_name", "") or ""),
    )
    dur = float(args.duration)
    if profile.id.startswith("nipro_") and dur <= 60:
        from .nipro import post_measure as pm

        dur = max(dur, pm.receive_s_for(profile.id))
    await client.run(duration=dur, connect_timeout=args.connect_timeout)

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
    log.info(
        "[%s] Stack diagnostics use [%s] tags (Windows WinRT / Linux BlueZ).",
        ble_log_tag(),
        ble_log_tag(),
    )

    if getattr(args, "command", None) == "omron":
        return await _async_omron_main(args)
    if getattr(args, "command", None) == "nipro":
        return await _async_nipro_main(args)
    return await _async_connect_main(args)


def build_arg_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description=(
            "Medical BLE reverse-engineering toolkit (multi-brand). "
            "Brands: Beurer/A&D (BLP), Omron (EEPROM via omron_bp), "
            "MightySat, thermometer, FORA scaffold. "
            "Windows (WinRT) and Linux (BlueZ) supported."
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
            help=(
                "beurer_bm54 | and_ua651 | nipro_nbp | nipro_nmbp | nipro_nsm1 | "
                "nipro_nt100b | nipro_cf | mightysat | thermometer | fora6 | omron"
            ),
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
        sp.add_argument(
            "--find-timeout",
            type=float,
            default=-1.0,
            help=(
                "Post-measure hunt seconds (Omron-like). Default auto for Nipro "
                "(~90–150s). Use 0 to disable (connect once only)."
            ),
        )
        sp.add_argument(
            "--device-name",
            default="",
            help="Advertised name hint while hunting (optional)",
        )

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

    # ---- Nipro げんきノート (pair registry + hands-free) ----
    nipro_p = sub.add_parser(
        "nipro",
        help="Nipro げんきノート: pair registry + hands-free wait (companion-like)",
    )
    nipro_sub = nipro_p.add_subparsers(dest="nipro_cmd", required=True)

    nipro_sub.add_parser("list", help="List paired Nipro meters (local registry)")

    n_pair = nipro_sub.add_parser(
        "pair",
        help="Register a meter (optionally scan/connect to fill name+serial)",
    )
    n_pair.add_argument(
        "-p",
        "--profile",
        default=None,
        help="nipro_nbp | nipro_nmbp | nipro_nsm1 | nipro_nt100b | nipro_cf | mightysat",
    )
    n_pair.add_argument("-a", "--address", default=None, help="BLE MAC / address")
    n_pair.add_argument("--name", default=None, help="Exact advertised name (required if offline)")
    n_pair.add_argument("--serial", default="", help="Serial number (optional)")
    n_pair.add_argument("--scan", action="store_true", help="Scan and pick by address/name")
    n_pair.add_argument("--scan-timeout", type=float, default=12.0)
    n_pair.add_argument(
        "--connect-serial",
        action="store_true",
        help="Connect once and try DIS serial read before saving",
    )

    n_unpair = nipro_sub.add_parser("unpair", help="Remove from local registry")
    n_unpair.add_argument("-a", "--address", default=None)
    n_unpair.add_argument("--name", default=None)
    n_unpair.add_argument(
        "--category",
        default=None,
        help="bp | ht | gl | spo2 | bc",
    )

    n_wait = nipro_sub.add_parser(
        "wait",
        help="Hands-free wait: scan paired names → sync 60s → loop (like companion)",
    )
    n_wait.add_argument(
        "-t",
        "--duration",
        type=float,
        default=3600.0,
        help="Total wait seconds (default 3600; companion home uses ~8h)",
    )
    n_wait.add_argument(
        "--receive-timeout",
        type=float,
        default=60.0,
        help="Per-session receive timeout (companion 60s)",
    )
    n_wait.add_argument(
        "--scan-chunk",
        type=float,
        default=12.0,
        help="Scan window seconds per chunk",
    )
    n_wait.add_argument(
        "--categories",
        default="bp,ht,gl,spo2",
        help="Comma categories to wait for (default bp,ht,gl,spo2; home app omits bc)",
    )
    n_wait.add_argument("--pair", dest="pair", action="store_true", default=None)
    n_wait.add_argument("--no-pair", dest="pair", action="store_false")

    # Flat flags for backward compatibility when no subcommand is used
    _add_connect_args(p)
    return p


async def _async_nipro_main(args: argparse.Namespace) -> int:
    """Nipro pair registry + hands-free wait."""
    from .nipro import registry as reg
    from .nipro.handsfree import handsfree_wait

    cmd = getattr(args, "nipro_cmd", None)

    if cmd == "list":
        meters = reg.list_meters()
        if not meters:
            log.info("[NIPRO] no paired meters (cwd: nipro_paired_devices.json)")
            return 0
        log.info("[NIPRO] %d paired meter(s):", len(meters))
        for m in meters:
            log.info(
                "  %-16s  %-12s  profile=%-14s  id=%s  serial=%s  addr=%s",
                m.name,
                m.category,
                m.profile_id,
                m.id_nodash,
                m.serial or "-",
                m.address or "-",
            )
        return 0

    if cmd == "unpair":
        n = reg.delete_meter(
            device_id=args.address,
            category=args.category,
            name=args.name,
        )
        log.info("[NIPRO] removed %d entr(y/ies)", n)
        return 0 if n else 1

    if cmd == "pair":
        address = args.address
        name = args.name
        profile_id = args.profile
        serial = args.serial or ""

        if args.scan or not address or not name:
            log.info("[NIPRO] scanning for meters…")
            try:
                devices = await scan_devices(profile=None, timeout=args.scan_timeout)
            except (BleakError, OSError, asyncio.TimeoutError) as exc:
                _log_winrt(exc, operation="nipro_scan")
                return 2
            # Prefer address match, else name prefix / profile hints
            picked = None
            for d in devices:
                dname = (d.name or "").strip()
                daddr = d.address or ""
                if address and daddr.upper() == address.upper():
                    picked = d
                    break
                if name and dname == name.strip():
                    picked = d
                    break
            if picked is None and profile_id:
                try:
                    prof = get_profile(profile_id)
                    for d in devices:
                        dname = (d.name or "").lower()
                        if any(h.lower() in dname for h in prof.name_hints):
                            picked = d
                            break
                except KeyError:
                    pass
            if picked is None and devices:
                # first device that maps to a Nipro profile
                for d in devices:
                    if reg.infer_profile_from_name(d.name or ""):
                        picked = d
                        break
            if picked is None:
                log.error(
                    "[NIPRO] no matching device found. Advertise the meter and retry "
                    "with -a <MAC> and --name '<exact adv name>'."
                )
                for d in devices[:15]:
                    log.info("  seen: %s  %s", d.address, d.name)
                return 2
            address = picked.address
            name = (picked.name or name or "").strip()
            log.info("[NIPRO] selected %s  %s", address, name)

        if not name:
            log.error("[NIPRO] pair requires --name (exact advertised name)")
            return 2
        if not address:
            log.error("[NIPRO] pair requires -a/--address")
            return 2
        if not profile_id:
            profile_id = reg.infer_profile_from_name(name)
        if not profile_id:
            log.error(
                "[NIPRO] could not infer profile from name %r — pass -p nipro_nbp|…",
                name,
            )
            return 2

        if args.connect_serial:
            try:
                prof = get_profile(profile_id)
                client = MedicalBleClient(
                    profile=prof,
                    address=address,
                    pair=os_pair_supported()
                    and profile_id in ("nipro_nmbp", "and_ua651"),
                )
                await client.connect(timeout=30.0)
                await client._read_device_information()
                # serial may only be in logs; optional future: capture DIS
                await client.disconnect()
            except Exception as exc:  # noqa: BLE001
                log.warning("[NIPRO] connect-serial optional path failed: %s", exc)

        try:
            meter = reg.register_meter(
                device_id=address,
                name=name,
                profile_id=profile_id,
                serial=serial,
                address=address,
            )
        except ValueError as exc:
            log.error("[NIPRO] pair failed: %s", exc)
            return 2
        log.info(
            "[NIPRO] paired OK — hands-free: python -m medical_ble_toolkit nipro wait"
        )
        log.info(
            "  name=%s category=%s profile=%s id=%s",
            meter.name,
            meter.category,
            meter.profile_id,
            meter.id_nodash,
        )
        return 0

    if cmd == "wait":
        cats = [c.strip() for c in (args.categories or "").split(",") if c.strip()]
        readings = await handsfree_wait(
            duration=float(args.duration),
            receive_timeout=float(args.receive_timeout),
            scan_chunk=float(args.scan_chunk),
            categories=cats or None,
            pair_on_connect=args.pair,
        )
        log.info("[NIPRO] hands-free collected %d reading(s)", len(readings))
        return 0

    log.error("Unknown nipro command: %s", cmd)
    return 2


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
        "nipro",
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

"""
Windows 11 / WinRT BLE error classification for bleak.

On Windows, bleak uses the WinRT Bluetooth APIs (BluetoothLEDevice,
GattDeviceService, DeviceInformation.Pairing). Failures often surface as
generic BleakError strings that are easy to miss during reverse engineering.

This module maps those strings to operator-actionable diagnostics.
NO bleak import required — pure string/exception classification.
"""

from __future__ import annotations

import platform
import sys
from dataclasses import dataclass
from typing import Optional


def is_windows() -> bool:
    return sys.platform == "win32" or platform.system().lower() == "windows"


@dataclass(frozen=True)
class WinRtDiagnosis:
    """Structured diagnosis for a Windows BLE failure."""
    category: str
    summary: str
    remediation: tuple[str, ...]
    is_pairing_related: bool = False
    is_retryable: bool = False


# Substring → diagnosis (first match wins; order matters — more specific first)
_RULES: tuple[tuple[tuple[str, ...], WinRtDiagnosis], ...] = (
    (
        # Explicit dialog dismissal only (0x800704C7 alone is often CCCD/auth, not dialog)
        ("user canceled the pairing", "user cancelled the pairing",
         "pairing was canceled", "pairing was cancelled",
         "pairing dialog"),
        WinRtDiagnosis(
            category="PAIRING_DIALOG_DISMISSED",
            summary=(
                "Windows pairing dialog was dismissed/cancelled "
                "(or timed out waiting for user confirmation)."
            ),
            remediation=(
                "Watch for the Windows 11 Bluetooth pairing popup (may be behind other windows).",
                "Accept the passkey / confirm pairing when the dialog appears.",
                "Settings → Bluetooth & devices → remove old entries for this device, then retry.",
                "For Beurer BM54 newer HW: enter the 6-digit passkey shown on the cuff.",
                "Do not click Cancel on the OS dialog while this client is connecting.",
            ),
            is_pairing_related=True,
            is_retryable=True,
        ),
    ),
    (
        # WinRT PairingResult.Status = Failed (common on Omron re-pair)
        ("could not pair", "pair with device: failed", "could not pair with device",
         "pairing failed", "pair failed"),
        WinRtDiagnosis(
            category="PAIRING_FAILED",
            summary=(
                "Windows OS pairing returned FAILED. Common with Omron when the "
                "cuff is not in flashing-P mode, a stale bond exists, or the phone "
                "still holds the exclusive bond."
            ),
            remediation=(
                "Cuff: HOLD Bluetooth until LCD shows flashing P / -P- (not a short press).",
                "Settings → Bluetooth & devices → remove any Omron / BLESmart / this MAC.",
                "Unpair the cuff from phone OMRON Connect (only one host can bond).",
                "Use RE-PAIR in the toolkit (auto-unpair + pair), accept any Windows popup.",
                "Keep cuff < 1 m; if pair fails in <0.1s again, reboot Bluetooth (Off/On).",
                "After pair OK: short-press BT for READ/LIVE (transfer mode).",
            ),
            is_pairing_related=True,
            is_retryable=True,
        ),
    ),
    (
        # ERROR_CANCELLED 0x800704C7 — on BLE CCCD often means auth/session abort
        ("canceled by the user", "cancelled by the user", "operation was canceled",
         "operation was cancelled", "winerror -2147023673", "-2147023673",
         "bond incomplete", "encrypted notify probe", "start_notify aborted"),
        WinRtDiagnosis(
            category="GATT_CANCELED_OR_BOND",
            summary=(
                "WinRT canceled a GATT operation (often start_notify/CCCD). "
                "On Omron this usually means the bond is incomplete or the cuff "
                "closed the session — not that you clicked Cancel."
            ),
            remediation=(
                "SHORT-press Bluetooth (transfer mode) and retry READ immediately.",
                "If pair() was FAILED: remove cuff in Windows Bluetooth, RE-PAIR with flashing P.",
                "Unpair phone OMRON Connect so only this PC holds the bond.",
                "Toggle Bluetooth Off/On if GattSession keeps going ACTIVE→CLOSED instantly.",
            ),
            is_pairing_related=True,
            is_retryable=True,
        ),
    ),
    (
        ("not connected", "not connected when"),
        WinRtDiagnosis(
            category="LINK_DROPPED",
            summary=(
                "BLE link dropped mid-session (GattSession CLOSED). Common right "
                "after a failed encrypted notify on Omron."
            ),
            remediation=(
                "Short-press BT for transfer mode and retry.",
                "If this follows pair FAILED / FE4A-only connect: RE-PAIR first.",
                "Keep cuff awake and < 1 m from the PC during the whole READ.",
            ),
            is_retryable=True,
        ),
    ),
    (
        ("not paired", "device is not paired", "requires pairing",
         "encryption", "insufficient authentication", "auth", "access denied",
         "0x80070005", "e_accessdenied"),
        WinRtDiagnosis(
            category="PAIRING_REQUIRED_OR_DENIED",
            summary=(
                "WinRT refused the GATT operation — device may need OS-level "
                "pairing/bonding, or access was denied."
            ),
            remediation=(
                "Re-run with --pair so the client calls BleakClient.pair() after connect.",
                "Put the medical device into pairing mode (see device manual / profile notes).",
                "Settings → Bluetooth & devices → Add device, or accept the OS pairing dialog.",
                "Remove stale pairings for this MAC, then pair fresh with only this PC as host.",
                "Unpair the device from any phone app that may hold an exclusive bond.",
            ),
            is_pairing_related=True,
            is_retryable=True,
        ),
    ),
    (
        ("timeout", "timed out", "operation aborted", "0x80070102"),
        WinRtDiagnosis(
            category="TIMEOUT",
            summary="BLE operation timed out (common on WinRT during connect/pair/service discovery).",
            remediation=(
                "Confirm the device is advertising (LCD/BT icon) within ~1 m of the PC.",
                "Increase --connect-timeout (e.g. 45–60s) for slow pairing dialogs.",
                "Windows Bluetooth radio: toggle off/on; avoid CSR dongles known to flake.",
                "Close other BLE apps (phones nearby, manufacturer apps, other scanners).",
                "Retry immediately — short advertising windows on medical devices are common.",
            ),
            is_retryable=True,
        ),
    ),
    (
        ("unreachable", "could not get gatt services", "device unreachable",
         "the device is unreachable", "0x8007273c"),
        WinRtDiagnosis(
            category="GATT_UNREACHABLE",
            summary=(
                "Link dropped during GATT service discovery or notify setup "
                "(classic Windows 'Unreachable' after a cached or flaky connect)."
            ),
            remediation=(
                "Do not rely on Windows device cache alone — scan for a live advertisement first.",
                "Wake the device again (new measurement / BT button) and reconnect within the window.",
                "Keep cuff/meter < 1 m from the adapter; avoid USB3 interference near dongles.",
                "Remove the device from Windows Bluetooth settings, re-pair, then reconnect.",
            ),
            is_retryable=True,
        ),
    ),
    (
        ("already connected", "in use", "resource in use", "0x800700aa"),
        WinRtDiagnosis(
            category="RESOURCE_IN_USE",
            summary="Another process or a stale WinRT handle still holds the device.",
            remediation=(
                "Close other Python/bleak sessions and manufacturer companion apps.",
                "Remove the device in Windows Bluetooth settings and re-add if stuck.",
                "Restart Bluetooth support service or reboot if handles leak after crashes.",
            ),
            is_retryable=True,
        ),
    ),
    (
        (
            "characteristic",
            "was not found",
            "characteristicnotfound",
            "bleakcharacteristicnotfound",
        ),
        WinRtDiagnosis(
            category="GATT_CHAR_MISSING",
            summary=(
                "Characteristic UUID is not on this device's GATT map "
                "(wrong profile/MAC, or OEM uses a different serial UUID)."
            ),
            remediation=(
                "Read the [GATT] discovery map in the log — note services that have Notify/Write.",
                "Confirm the MAC is the NT-100B (name NT-100 / Thermometer), not another BLE gadget.",
                "Toolkit will try write+notify fallback chars; if still failing use RE mode.",
                "Power-cycle the thermometer, take a new measurement, then PAIR once and LIVE.",
            ),
            is_retryable=True,
        ),
    ),
    (
        ("not found", "device not found", "no such device", "element not found"),
        WinRtDiagnosis(
            category="DEVICE_NOT_FOUND",
            summary="Address not visible to WinRT (not advertising, wrong MAC, or radio off).",
            remediation=(
                "Run --scan while the device is advertising; copy the address from MATCH lines.",
                "Confirm Bluetooth is On in Windows Quick Settings.",
                "For BM54: press M1/M2 or finish a measurement to start advertising.",
            ),
            is_retryable=True,
        ),
    ),
    (
        ("failed to start scanner", "watcher status", "stopping", "aborted",
         "radio", "bluetooth is off", "adapter", "not powered", "no bluetooth"),
        WinRtDiagnosis(
            category="SCANNER_OR_ADAPTER",
            summary=(
                "WinRT could not start the BLE scanner (often ABORTED: radio busy, "
                "another app holding the watcher, or Bluetooth toggled off)."
            ),
            remediation=(
                "Confirm Bluetooth is On in Quick Settings (toggle Off then On).",
                "Close other BLE apps (nRF Connect, manufacturer apps, other Python scans).",
                "Wait 2–3 seconds and retry — Windows often holds a lock after a failed scan.",
                "Device Manager → Bluetooth → disable/enable the adapter if it keeps aborting.",
                "If you already know the MAC, the toolkit will try connect-by-address without scan.",
            ),
            is_retryable=True,
        ),
    ),
)


_DEFAULT = WinRtDiagnosis(
    category="BLEAK_ERROR",
    summary="Unclassified Bleak/WinRT error — see raw exception text.",
    remediation=(
        "Re-run with -v and capture the full stack for analysis.",
        "Check Windows Bluetooth settings and device advertising state.",
        "Retry after removing stale pairings for this device address.",
    ),
    is_retryable=True,
)


def classify_ble_error(exc: BaseException) -> WinRtDiagnosis:
    """Map an exception (or its message) to a WinRtDiagnosis."""
    text = f"{type(exc).__name__}: {exc}".lower()
    # Also fold __cause__ / __context__ — WinRT often nests HRESULT messages
    for attr in ("__cause__", "__context__"):
        nested = getattr(exc, attr, None)
        if nested is not None:
            text += f" | {type(nested).__name__}: {nested}".lower()

    for needles, diagnosis in _RULES:
        if any(n in text for n in needles):
            return diagnosis
    return _DEFAULT


def format_diagnosis(exc: BaseException, operation: str = "BLE operation") -> str:
    """Multi-line human-readable block for logs."""
    diag = classify_ble_error(exc)
    lines = [
        f"[WINRT] operation={operation}",
        f"[WINRT] category={diag.category}",
        f"[WINRT] {diag.summary}",
        f"[WINRT] raw={type(exc).__name__}: {exc}",
        f"[WINRT] pairing_related={diag.is_pairing_related}  retryable={diag.is_retryable}",
        "[WINRT] remediation:",
    ]
    for i, step in enumerate(diag.remediation, 1):
        lines.append(f"[WINRT]   {i}. {step}")
    if is_windows():
        lines.append(
            "[WINRT] platform=Windows — bleak uses WinRT "
            "(BluetoothLEDevice / DeviceInformation.Pairing)."
        )
    else:
        lines.append(
            f"[WINRT] platform={platform.system()} — WinRT-specific notes may not apply."
        )
    return "\n".join(lines)

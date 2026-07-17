"""
Cross-platform BLE error classification for bleak (Windows WinRT + Linux BlueZ).

Historically named winrt_errors.py (Windows-first). Now classifies failures on
all platforms and returns operator-actionable diagnostics.

  - Windows: bleak uses WinRT (BluetoothLEDevice, DeviceInformation.Pairing)
  - Linux:   bleak uses BlueZ over D-Bus (org.bluez)
  - macOS:   CoreBluetooth (limited pairing control)

NO bleak import required — pure string/exception classification.
"""

from __future__ import annotations

import platform
import sys
from dataclasses import dataclass
from typing import Optional


def is_windows() -> bool:
    return sys.platform == "win32" or platform.system().lower() == "windows"


def is_linux() -> bool:
    return sys.platform.startswith("linux") or platform.system().lower() == "linux"


def is_macos() -> bool:
    return sys.platform == "darwin" or platform.system().lower() == "darwin"


def os_pair_supported() -> bool:
    """
    True when BleakClient.pair() / OS bonding is meaningful.

    Windows and Linux medical meters almost always need OS bonding for
    encrypted Indications (BLP 0x2A35, Omron FE4A, etc.).
    """
    return is_windows() or is_linux()


def ble_log_tag() -> str:
    """Log prefix for stack-specific diagnostics."""
    if is_windows():
        return "WINRT"
    if is_linux():
        return "BLUEZ"
    if is_macos():
        return "CBLE"
    return "BLE"


def remove_bond_instructions(address: str = "AA:BB:CC:DD:EE:FF") -> str:
    """How to delete a stale OS bond for this platform."""
    mac = (address or "AA:BB:CC:DD:EE:FF").strip().upper()
    if is_windows():
        return (
            "Settings → Bluetooth & devices → remove Omron/BLESmart / this MAC, "
            "then re-pair with the toolkit."
        )
    if is_linux():
        return (
            f"bluetoothctl remove {mac}   "
            "(or Settings → Bluetooth → forget device), then re-pair. "
            "If stuck: bluetoothctl power off; sleep 1; power on"
        )
    return "Remove the device bond in your OS Bluetooth settings, then re-pair."


def pairing_ui_hint() -> str:
    """What the operator should expect during OS pair()."""
    if is_windows():
        return (
            "Windows may show a Bluetooth pairing popup — ACCEPT it "
            "(check the taskbar). Enter a 6-digit passkey from the device LCD if asked."
        )
    if is_linux():
        return (
            "BlueZ may prompt via a Bluetooth agent (desktop notification or "
            "bluetoothctl). For Just-Works devices (most Omron) no PIN is needed. "
            "Passkey devices (some Beurer): enter the 6-digit code from the LCD. "
            "If pair hangs: run  bluetoothctl  then  agent on  /  default-agent"
        )
    return "Accept any OS Bluetooth pairing prompt if shown."


def bluetooth_on_hint() -> str:
    if is_windows():
        return "Confirm Bluetooth is On in Windows Quick Settings (toggle Off/On)."
    if is_linux():
        return (
            "Confirm adapter is powered:  bluetoothctl show | grep Powered  "
            "or  bluetoothctl power on. Also: rfkill unblock bluetooth"
        )
    return "Confirm Bluetooth is enabled on this machine."


@dataclass(frozen=True)
class WinRtDiagnosis:
    """Structured diagnosis for a BLE stack failure (name kept for API stability)."""
    category: str
    summary: str
    remediation: tuple[str, ...]
    is_pairing_related: bool = False
    is_retryable: bool = False


def _remediation_pair_failed() -> tuple[str, ...]:
    return (
        "Cuff/meter: HOLD Bluetooth until LCD shows flashing P / pairing mode "
        "(not a short press).",
        remove_bond_instructions(),
        "Unpair the device from any phone companion app (only one host can bond).",
        "Use RE-PAIR in the toolkit (auto-unpair + pair). " + pairing_ui_hint(),
        "Keep device < 1 m; if pair fails instantly, toggle Bluetooth Off/On.",
        "After pair OK: short-press BT for READ/LIVE (transfer mode), not P mode.",
    )


def _remediation_auth() -> tuple[str, ...]:
    return (
        "Re-run with --pair so the client calls BleakClient.pair() after connect.",
        "Put the medical device into pairing mode (see device manual / profile notes).",
        pairing_ui_hint(),
        remove_bond_instructions(),
        "Unpair the device from any phone app that may hold an exclusive bond.",
    )


def _remediation_scanner() -> tuple[str, ...]:
    base = [
        bluetooth_on_hint(),
        "Close other BLE apps (nRF Connect, manufacturer apps, other Python scans).",
        "Wait 2–3 seconds and retry — stacks often hold a lock after a failed scan.",
        "If you already know the MAC, pass --address / paste MAC (connect without scan).",
    ]
    if is_linux():
        base.append(
            "Linux: ensure user can use BlueZ (polkit/seat). "
            "Optional: sudo usermod -aG bluetooth $USER  then re-login. "
            "Check: journalctl -u bluetooth -n 30"
        )
        base.append(
            "If hci0 is DOWN:  sudo hciconfig hci0 up  or  bluetoothctl power on"
        )
    if is_windows():
        base.append(
            "Device Manager → Bluetooth → disable/enable the adapter if it keeps aborting."
        )
    return tuple(base)


# Substring → diagnosis (first match wins; order matters — more specific first)
_RULES: tuple[tuple[tuple[str, ...], WinRtDiagnosis], ...] = (
    (
        (
            "user canceled the pairing",
            "user cancelled the pairing",
            "pairing was canceled",
            "pairing was cancelled",
            "pairing dialog",
            "authentication canceled",
            "authentication cancelled",
            "org.bluez.error.authenticationcanceled",
            "org.bluez.error.authenticationrejected",
        ),
        WinRtDiagnosis(
            category="PAIRING_DIALOG_DISMISSED",
            summary=(
                "Pairing was cancelled / rejected (OS dialog, agent, or timeout)."
            ),
            remediation=(
                pairing_ui_hint(),
                "Accept the passkey / confirm pairing when prompted.",
                remove_bond_instructions(),
                "For Beurer BM54 newer HW: enter the 6-digit passkey shown on the cuff.",
                "Do not cancel the OS pairing prompt while this client is connecting.",
            ),
            is_pairing_related=True,
            is_retryable=True,
        ),
    ),
    (
        (
            "could not pair",
            "pair with device: failed",
            "could not pair with device",
            "pairing failed",
            "pair failed",
            "org.bluez.error.authenticationfailed",
            "org.bluez.error.failed",
            "authentication failed",
        ),
        WinRtDiagnosis(
            category="PAIRING_FAILED",
            summary=(
                "OS pairing returned FAILED. Common when the device is not in "
                "pairing mode, a stale bond exists, or a phone still holds the bond."
            ),
            remediation=_remediation_pair_failed(),
            is_pairing_related=True,
            is_retryable=True,
        ),
    ),
    (
        (
            "canceled by the user",
            "cancelled by the user",
            "operation was canceled",
            "operation was cancelled",
            "winerror -2147023673",
            "-2147023673",
            "bond incomplete",
            "encrypted notify probe",
            "start_notify aborted",
            "org.bluez.error.notpermitted",
            "not permitted",
        ),
        WinRtDiagnosis(
            category="GATT_CANCELED_OR_BOND",
            summary=(
                "GATT operation canceled (often start_notify/CCCD). Usually means "
                "bond incomplete, wrong encryption, or the device closed the session."
            ),
            remediation=(
                "SHORT-press Bluetooth (transfer mode) and retry READ immediately.",
                "If pair() failed: " + remove_bond_instructions(),
                "Unpair phone companion apps so only this PC holds the bond.",
                "Toggle Bluetooth Off/On if the link keeps dropping instantly.",
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
                "BLE link dropped mid-session. Common right after a failed "
                "encrypted notify or when the meter sleeps."
            ),
            remediation=(
                "Short-press BT for transfer mode and retry.",
                "If this follows pair FAILED: RE-PAIR first.",
                "Keep device awake and < 1 m from the adapter during the whole READ.",
            ),
            is_retryable=True,
        ),
    ),
    (
        (
            "not paired",
            "device is not paired",
            "requires pairing",
            "encryption",
            "insufficient authentication",
            "insufficient encryption",
            "auth",
            "access denied",
            "0x80070005",
            "e_accessdenied",
            "org.bluez.error.authenticationrequired",
            "authentication required",
        ),
        WinRtDiagnosis(
            category="PAIRING_REQUIRED_OR_DENIED",
            summary=(
                "Stack refused the GATT operation — device needs OS-level "
                "pairing/bonding, or access was denied."
            ),
            remediation=_remediation_auth(),
            is_pairing_related=True,
            is_retryable=True,
        ),
    ),
    (
        (
            "timeout",
            "timed out",
            "operation aborted",
            "0x80070102",
            "org.bluez.error.timedout",
            "le connection attempt failed",
        ),
        WinRtDiagnosis(
            category="TIMEOUT",
            summary="BLE operation timed out (connect/pair/service discovery).",
            remediation=(
                "Confirm the device is advertising (LCD/BT icon) within ~1 m of the PC.",
                "Increase --connect-timeout (e.g. 45–60s) for slow pairing prompts.",
                bluetooth_on_hint(),
                "Close other BLE apps (phones nearby, manufacturer apps, other scanners).",
                "Retry immediately — short advertising windows on medical devices are common.",
            ),
            is_retryable=True,
        ),
    ),
    (
        (
            "unreachable",
            "could not get gatt services",
            "device unreachable",
            "the device is unreachable",
            "0x8007273c",
            "org.bluez.error.doesnotexist",
        ),
        WinRtDiagnosis(
            category="GATT_UNREACHABLE",
            summary=(
                "Link dropped during GATT service discovery or notify setup."
            ),
            remediation=(
                "Scan for a live advertisement first; do not rely on a stale cache.",
                "Wake the device again (new measurement / BT button) and reconnect quickly.",
                "Keep cuff/meter < 1 m from the adapter.",
                remove_bond_instructions(),
            ),
            is_retryable=True,
        ),
    ),
    (
        (
            "already connected",
            "in use",
            "resource in use",
            "0x800700aa",
            "org.bluez.error.inprogress",
            "in progress",
            "resource busy",
            "device or resource busy",
        ),
        WinRtDiagnosis(
            category="RESOURCE_IN_USE",
            summary="Another process or a stale handle still holds the device/radio.",
            remediation=(
                "Close other Python/bleak sessions and manufacturer companion apps.",
                "Stop the web UI if the CLI needs the radio (or vice versa).",
                remove_bond_instructions(),
                bluetooth_on_hint(),
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
                "Read the [GATT] discovery map in the log — note Notify/Write chars.",
                "Confirm the MAC is the expected medical device.",
                "Toolkit will try write+notify fallback chars; if still failing use RE mode.",
                "Power-cycle the device, take a new measurement, then PAIR once and retry.",
            ),
            is_retryable=True,
        ),
    ),
    (
        (
            "not found",
            "device not found",
            "no such device",
            "element not found",
            "org.bluez.error.does not exist",
        ),
        WinRtDiagnosis(
            category="DEVICE_NOT_FOUND",
            summary="Address not visible (not advertising, wrong MAC, or radio off).",
            remediation=(
                "Run --scan while the device is advertising; copy the address from MATCH lines.",
                bluetooth_on_hint(),
                "For BM54: press M1/M2 or finish a measurement to start advertising.",
            ),
            is_retryable=True,
        ),
    ),
    (
        (
            "failed to start scanner",
            "watcher status",
            "stopping",
            "aborted",
            "radio",
            "bluetooth is off",
            "adapter",
            "not powered",
            "no bluetooth",
            "org.bluez.error.notready",
            "not ready",
            "no default bluetooth adapter",
            "bluetooth adapter not found",
        ),
        WinRtDiagnosis(
            category="SCANNER_OR_ADAPTER",
            summary=(
                "Could not start the BLE scanner (radio busy, adapter off, or "
                "another app holding the adapter)."
            ),
            remediation=_remediation_scanner(),
            is_retryable=True,
        ),
    ),
)


_DEFAULT = WinRtDiagnosis(
    category="BLEAK_ERROR",
    summary="Unclassified Bleak/BLE stack error — see raw exception text.",
    remediation=(
        "Re-run with -v and capture the full stack for analysis.",
        bluetooth_on_hint(),
        "Retry after removing stale pairings for this device address.",
        remove_bond_instructions(),
    ),
    is_retryable=True,
)


def classify_ble_error(exc: BaseException) -> WinRtDiagnosis:
    """Map an exception (or its message) to a WinRtDiagnosis."""
    text = f"{type(exc).__name__}: {exc}".lower()
    # Nested causes — WinRT HRESULTs and BlueZ D-Bus errors often nest
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
    tag = ble_log_tag()
    lines = [
        f"[{tag}] operation={operation}",
        f"[{tag}] category={diag.category}",
        f"[{tag}] {diag.summary}",
        f"[{tag}] raw={type(exc).__name__}: {exc}",
        f"[{tag}] pairing_related={diag.is_pairing_related}  retryable={diag.is_retryable}",
        f"[{tag}] remediation:",
    ]
    for i, step in enumerate(diag.remediation, 1):
        lines.append(f"[{tag}]   {i}. {step}")
    if is_windows():
        lines.append(
            f"[{tag}] platform=Windows — bleak uses WinRT "
            "(BluetoothLEDevice / DeviceInformation.Pairing)."
        )
    elif is_linux():
        lines.append(
            f"[{tag}] platform=Linux — bleak uses BlueZ D-Bus. "
            "Pair/unpair via BlueZ agent; remove bonds with bluetoothctl."
        )
    else:
        lines.append(
            f"[{tag}] platform={platform.system()} — stack-specific notes may vary."
        )
    return "\n".join(lines)

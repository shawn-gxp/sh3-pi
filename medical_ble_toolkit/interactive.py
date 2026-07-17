"""
Interactive multi-brand CLI wizard.

  python -m medical_ble_toolkit
  python -m medical_ble_toolkit interactive

Prompts for brand → model → action → address.
Empty brand defaults to Omron; empty model defaults to HEM-7143T1 (lab default).
PAIR / RE-PAIR always scan nearby devices and list MAC + name (no saved-MAC default).
READ / LIVE may reuse last address from medical_ble_device.json.
"""

from __future__ import annotations

import asyncio
import json
import logging
import pathlib
import sys
from dataclasses import dataclass, field
from typing import Any, Callable, List, Optional, Sequence

from .ble_client import (
    MedicalBleClient,
    _brief,
    _log_winrt,
    scan_devices,
    setup_logging,
)
from .common.hexutil import ms_timestamp
from .common.winrt_errors import is_windows
from .profiles import DeviceProfile, get_profile, list_profiles

log = logging.getLogger("medical_ble.interactive")

CONFIG_NAME = "medical_ble_device.json"
DEFAULT_BRAND = "omron"
DEFAULT_OMRON_MODEL = "HEM-7143T1"
DEFAULT_LISTEN_SEC = 90.0
DEFAULT_OUTPUT = "data"


# ---------------------------------------------------------------------------
# Brand catalog (interactive menu)
# ---------------------------------------------------------------------------

@dataclass(frozen=True)
class BrandChoice:
    id: str
    label: str
    # connect profile id in profiles.py (None for omron-only path)
    connect_profile: Optional[str] = None
    # True → use omron_bridge pair/read
    is_omron: bool = False
    default_model: str = ""
    notes: str = ""


BRANDS: List[BrandChoice] = [
    BrandChoice(
        id="omron",
        label="Omron (HEM-* BP, EEPROM history)",
        is_omron=True,
        connect_profile="omron",
        default_model=DEFAULT_OMRON_MODEL,
        notes="Pair once (flashing P), then read in transfer mode (short-press BT).",
    ),
    BrandChoice(
        id="beurer",
        label="Beurer multi-device (BP / glucose / FT / PO60 / scale / ECG / trackers)",
        connect_profile="beurer_bp",
        default_model="BM54",
        notes=(
            "Companion-app timing + APK catalog (~100 models, OCR excluded). "
            "BP auto-Indicate dump; other families use documented UUIDs/RACP."
        ),
    ),
    BrandChoice(
        id="and",
        label="A&D UA-651BLE full SDK (not げんきノート-simple)",
        connect_profile="and_ua651",
        default_model="UA-651BLE",
        notes="Custom 0xA6/0xE1 path. For Nipro companion meters use brand 'nipro'.",
    ),
    BrandChoice(
        id="nipro",
        label="Nipro げんきノート (NBP / NMBP / NSM / NT-100B / CF)",
        connect_profile="nipro_nbp",
        default_model="NBP-1BLE",
        notes=(
            "Companion-like sessions. Pair registry: "
            "python -m medical_ble_toolkit nipro pair|list|wait"
        ),
    ),
    BrandChoice(
        id="masimo",
        label="Masimo MightySat (SpO2 stream)",
        connect_profile="mightysat",
        default_model="MightySat",
        notes="Companion order: GetInfo → SetClock(ticks) → EnableStream.",
    ),
    BrandChoice(
        id="thermo",
        label="NT-100B companion (HTP + power-off)",
        connect_profile="nipro_nt100b",
        default_model="NT-100B",
        notes="げんきノート path. TICD history lab: profile thermometer.",
    )
    BrandChoice(
        id="fora",
        label="FORA 6 Connect (RE scaffold)",
        connect_profile="fora6",
        default_model="FORA 6 Connect",
        notes="No wire protocol yet — hex dump / reverse-engineering mode.",
    ),
    BrandChoice(
        id="re",
        label="Unknown / reverse-engineering (subscribe all)",
        connect_profile="re_generic",
        default_model="generic",
        notes="GATT tree + all notify/indicate + hex dumps.",
    ),
]


# ---------------------------------------------------------------------------
# Config persistence
# ---------------------------------------------------------------------------

def _config_path() -> pathlib.Path:
    return pathlib.Path.cwd() / CONFIG_NAME


def load_config() -> dict[str, Any]:
    p = _config_path()
    if not p.is_file():
        return {}
    try:
        return json.loads(p.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        log.warning("Could not read %s: %s", p, exc)
        return {}


def save_config(**kwargs: Any) -> None:
    p = _config_path()
    data = load_config()
    data.update({k: v for k, v in kwargs.items() if v is not None})
    data["updated_at"] = ms_timestamp()
    try:
        p.write_text(json.dumps(data, indent=2), encoding="utf-8")
        log.info("Saved preferences → %s", p.name)
    except OSError as exc:
        log.warning("Could not save config: %s", exc)


# ---------------------------------------------------------------------------
# Prompts
# ---------------------------------------------------------------------------

def _prompt(msg: str, default: str = "") -> str:
    """Read a line; empty → default. Ctrl+C raises KeyboardInterrupt."""
    if default:
        shown = f"{msg} [{default}]: "
    else:
        shown = f"{msg}: "
    try:
        raw = input(shown)
    except EOFError:
        return default
    text = (raw or "").strip()
    return text if text else default


def _prompt_choice(
    title: str,
    options: Sequence[tuple[str, str]],
    default_key: str,
) -> str:
    """
    options: list of (key, label). User may type number (1-based) or key.
    Empty → default_key.
    """
    print()
    print(title)
    print("-" * 60)
    keys = []
    for i, (key, label) in enumerate(options, 1):
        mark = "  (default)" if key == default_key else ""
        print(f"  {i}. {label}{mark}")
        keys.append(key)
    print("-" * 60)
    raw = _prompt("Choice (number or id, Enter = default)", default_key)
    # numeric?
    if raw.isdigit():
        idx = int(raw)
        if 1 <= idx <= len(keys):
            return keys[idx - 1]
        print(f"  Invalid number {idx}; using default {default_key!r}.")
        return default_key
    # match key case-insensitive
    low = raw.lower()
    for key in keys:
        if key.lower() == low:
            return key
    # partial match on label
    for key, label in options:
        if low in label.lower() or low in key.lower():
            return key
    print(f"  Unrecognized {raw!r}; using default {default_key!r}.")
    return default_key


def _pick_brand(cfg: dict) -> BrandChoice:
    """
    Enter always selects Omron (lab default).

    Last session brand is only shown in the banner — it must NOT become the
    Enter default, or a RE session sticky-saves and every subsequent run
    starts on brand 7 (re) instead of Omron.
    """
    # Always Omron on bare Enter (ignore sticky config brand)
    default = DEFAULT_BRAND

    last = (cfg.get("brand") or "").strip().lower()
    if last and last != default:
        print(f"  (Last session brand was {last!r} — type its number/id to reuse.)")

    opts = [(b.id, f"{b.label}") for b in BRANDS]
    chosen = _prompt_choice(
        "Select BRAND  (press Enter for Omron)",
        opts,
        default,
    )
    for b in BRANDS:
        if b.id == chosen:
            if b.notes:
                print(f"  → {b.notes}")
            return b
    return BRANDS[0]


def _list_omron_model_ids() -> List[str]:
    try:
        from .omron_bridge import list_omron_models

        return [r["model_id"] for r in list_omron_models()]
    except Exception as exc:  # noqa: BLE001
        log.warning("Could not load omron_bp catalog: %s", exc)
        return [DEFAULT_OMRON_MODEL]


def _pick_beurer_model(cfg: dict) -> str:
    """Category → model from APK device_registry (OCR excluded)."""
    try:
        from .beurer.catalog import (
            CATEGORY_LABELS,
            devices_in_category,
            get_device,
            list_categories,
            supported_summary,
        )
    except Exception as exc:  # noqa: BLE001
        log.warning("Beurer catalog unavailable: %s — default BM54", exc)
        return cfg.get("model") if cfg.get("brand") == "beurer" else "BM54"

    print()
    print(f"  {supported_summary()}")
    cats = list_categories()
    opts = [(c, CATEGORY_LABELS.get(c, c)) for c in cats]
    default_cat = "blood_pressure"
    if cfg.get("brand") == "beurer" and cfg.get("model"):
        d = get_device(str(cfg["model"]))
        if d:
            default_cat = d.primary_category
    cat = _prompt_choice("Select Beurer CATEGORY", opts, default_cat)
    devices = devices_in_category(cat)
    if not devices:
        return "BM54"
    print()
    print(f"  Models in {CATEGORY_LABELS.get(cat, cat)} ({len(devices)}):")
    show = devices[:20]
    default_model = "BM54"
    if cfg.get("brand") == "beurer" and cfg.get("model"):
        if any(d.id == cfg["model"] for d in devices):
            default_model = str(cfg["model"])
        elif devices:
            default_model = devices[0].id
    elif devices:
        # Prefer BM54 in BP list
        default_model = next((d.id for d in devices if d.id == "BM54"), devices[0].id)
    for i, d in enumerate(show, 1):
        mark = "  ← default" if d.id == default_model else ""
        print(f"  {i:2d}. {d.label}{mark}")
    if len(devices) > 20:
        print(f"  … +{len(devices) - 20} more (type full model id)")
    raw = _prompt("Model id (Enter = default)", default_model)
    if raw.isdigit():
        idx = int(raw)
        if 1 <= idx <= len(show):
            return show[idx - 1].id
    hit = get_device(raw)
    if hit:
        print(f"  → protocol={hit.protocol_profile}  toolkit={hit.toolkit_profile}")
        return hit.id
    print(f"  Unknown {raw!r}; using {default_model!r}")
    return default_model


def _pick_model(brand: BrandChoice, cfg: dict) -> str:
    if brand.id == "beurer":
        return _pick_beurer_model(cfg)

    if brand.is_omron:
        models = _list_omron_model_ids()
        default = (
            cfg.get("model")
            if cfg.get("brand") == "omron" and cfg.get("model") in models
            else brand.default_model or DEFAULT_OMRON_MODEL
        )
        if default not in models:
            default = DEFAULT_OMRON_MODEL if DEFAULT_OMRON_MODEL in models else models[0]

        print()
        print(f"Omron models ({len(models)}). Common: HEM-7143T1, HEM-7322T, HEM-7155T …")
        show = models[:12]
        for i, m in enumerate(show, 1):
            mark = "  ← default" if m == default else ""
            print(f"  {i:2d}. {m}{mark}")
        if len(models) > 12:
            print(f"  … +{len(models) - 12} more (type full id)")
        raw = _prompt("Model id (Enter = default)", default)
        # number from short list?
        if raw.isdigit():
            idx = int(raw)
            if 1 <= idx <= len(show):
                return show[idx - 1]
        # resolve via omron registry (aliases work)
        try:
            from .omron_bridge import resolve_omron_model

            return resolve_omron_model(raw).model_id
        except Exception:
            if raw.upper() in {m.upper() for m in models}:
                for m in models:
                    if m.upper() == raw.upper():
                        return m
            print(f"  Unknown model {raw!r}; using {default!r}.")
            return default

    # Non-Omron: free text with brand default
    default = brand.default_model
    if cfg.get("brand") == brand.id and cfg.get("model"):
        default = str(cfg["model"])
    return _prompt(f"Model name for {brand.label}", default)


def _pick_action(brand: BrandChoice) -> str:
    if brand.is_omron:
        print()
        print("  Omron actions:")
        print("  1 READ    = one-shot history download (short-press BT)")
        print("  2 LIVE    = app-style loop every 60s — auto-update latest")
        print("  3 PAIR    = first-time bond (cuff flashing P)")
        print("  4 REPAIR  = remove Windows bond, then pair again")
        print("  5 SCAN    = continuous nearby scan (60s interval)")
        print()
        opts = [
            ("read", "READ once — short-press BT (transfer mode)"),
            ("live", "LIVE MONITOR — sync every 60s, latest reading auto-updates"),
            ("pair", "PAIR once — cuff flashing P (first time on this PC)"),
            ("repair", "RE-PAIR — remove Windows bond, then pair (flashing P)"),
            ("scan", "SCAN continuous — refresh nearby list every 60s"),
            ("scan_once", "SCAN once — single 8s discovery"),
            ("quit", "Quit"),
        ]
        return _prompt_choice("Select ACTION", opts, "read")

    # All other brands: pairing is first-class (scan → pick MAC+name → OS bond)
    print()
    print(f"  {brand.label} actions:")
    print("  1 PAIR    = scan nearby → pick MAC+name → Windows bond")
    print("  2 REPAIR  = remove Windows bond, then pair again")
    print("  3 LIVE    = app-style loop every 60s")
    print("  4 CONNECT = one-shot connect + listen")
    print("  5 SCAN    = continuous nearby scan")
    print()
    opts = [
        ("pair", "PAIR once — scan surroundings, pick device MAC+name, bond"),
        ("repair", "RE-PAIR — remove Windows bond, scan, then pair again"),
        ("live", "LIVE MONITOR — connect/sync every 60s, latest auto-updates"),
        ("connect", "Connect once + listen (hex + parse)"),
        ("scan", "SCAN continuous — refresh nearby list every 60s"),
        ("scan_once", "SCAN once — single 8s discovery"),
        ("quit", "Quit"),
    ]
    return _prompt_choice("Select ACTION", opts, "pair")


def _resolve_pick_from_scan(pick: str, devices: Sequence[Any], *, max_list: int = 30) -> Optional[str]:
    """
    Map user input to a MAC from a scan list.
    - digits in 1..listed range → that device
    - other non-empty text → treated as pasted MAC
    - empty → None
    """
    text = (pick or "").strip()
    if not text:
        return None
    listed = min(len(devices), max_list)
    if text.isdigit():
        idx = int(text)
        if 1 <= idx <= listed:
            chosen = devices[idx - 1]
            name = (getattr(chosen, "name", None) or "").strip() or "(no name)"
            print(f"  Selected: {chosen.address}  {name}")
            return chosen.address
        print(f"  Invalid list number {idx} (valid 1–{listed}). Paste a MAC instead.")
        return None
    return text


async def _scan_and_list_devices(
    brand: BrandChoice,
    *,
    timeout: float = 8.0,
    filter_by_profile: bool = True,
    max_list: int = 30,
) -> List[Any]:
    """
    Scan surroundings and print MAC + advertised name for each hit.
    When filter_by_profile is False (pairing discovery), list ALL nearby BLE
    devices so a new cuff can be found even if its name is unexpected.
    """
    profile: Optional[DeviceProfile] = None
    if filter_by_profile and brand.connect_profile:
        try:
            profile = get_profile(brand.connect_profile)
        except KeyError:
            profile = None

    print("  Scanning surroundings ~%.0fs — wake the device / put it in P mode…" % timeout)
    if is_windows():
        print(
            "  Windows tip: if scan aborts, type the MAC — connect uses "
            "direct address (no scanner)."
        )
    try:
        devices = await scan_devices(profile=profile, timeout=timeout)
    except Exception as exc:  # noqa: BLE001
        log.error("Scan failed: %s", exc)
        _log_winrt(exc, operation="interactive_scan")
        devices = []

    if devices:
        print()
        print(f"  Found {len(devices)} device(s)  (MAC + name):")
        print("  " + "-" * 56)
        for i, d in enumerate(devices[:max_list], 1):
            name = (d.name or "").strip() or "(no name)"
            print(f"    {i:2d}.  {d.address}   {name}")
        if len(devices) > max_list:
            print(f"    … and {len(devices) - max_list} more (not listed)")
        print("  " + "-" * 56)
    else:
        print("  No devices found (scanner busy / BT off / nothing advertising).")
    return devices


async def _pick_address(
    brand: BrandChoice,
    cfg: dict,
    *,
    do_scan: bool = False,
    for_pairing: bool = False,
) -> str:
    """
    Resolve a BLE MAC for the chosen action.

    Pairing / re-pair (for_pairing=True):
      - NEVER pre-fill the last saved MAC or a default name
      - Always scan surroundings and show MAC + advertised name
      - User must pick a listed device or paste a MAC (no silent default)

    Read / live / connect:
      - May reuse last saved MAC; optional scan when none saved or do_scan
    """
    # Pairing must discover a real nearby device — do not trust last session MAC.
    default = ""
    if not for_pairing:
        if cfg.get("brand") == brand.id and cfg.get("address"):
            default = str(cfg["address"])
        elif cfg.get("address"):
            default = str(cfg["address"])

    if for_pairing:
        print()
        print("  PAIRING: scanning nearby devices (no saved MAC / no default name).")
        print("  Put the new device in pairing mode, then pick it from the list.")
        devices = await _scan_and_list_devices(
            brand,
            timeout=8.0,
            filter_by_profile=False,  # show all nearby so new devices are visible
        )
        if devices:
            pick = _prompt("Pick number from list, or paste MAC (no default)", "")
            resolved = _resolve_pick_from_scan(pick, devices)
            if resolved:
                return resolved
            # Empty / invalid: re-scan once, then require a real choice
            print("  No valid selection — scanning again…")
            devices = await _scan_and_list_devices(
                brand, timeout=8.0, filter_by_profile=False
            )
            if devices:
                pick = _prompt("Pick number or paste MAC", "")
                resolved = _resolve_pick_from_scan(pick, devices)
                if resolved:
                    return resolved
        else:
            print("  Type the MAC manually only if you already know it.")
        addr = _prompt("Device address (MAC) — no default", "")
        if not addr:
            raise SystemExit("No address provided — pairing aborted (scan required).")
        return addr

    if do_scan or not default:
        print()
        scan_now = _prompt(
            "Scan for devices now? (Y/n)",
            "Y" if not default else "n",
        ).lower()
        if scan_now in ("", "y", "yes"):
            devices = await _scan_and_list_devices(brand, timeout=8.0, filter_by_profile=True)
            if devices:
                pick = _prompt(
                    "Pick number or paste MAC",
                    "1" if devices else default,
                )
                resolved = _resolve_pick_from_scan(pick, devices)
                if resolved:
                    return resolved
            else:
                print("  Type the MAC manually — connect does not require a scan.")

    addr = _prompt("Device address (MAC)", default)
    if not addr:
        raise SystemExit("No address provided — aborting.")
    return addr


# ---------------------------------------------------------------------------
# Actions
# ---------------------------------------------------------------------------

async def _run_omron_pair(
    address: str,
    model: str,
    *,
    force_rebind: bool = False,
) -> int:
    from .omron_bridge import pair_omron

    print()
    print("=" * 60)
    print(f"  OMRON PAIR  model={model}  address={address}")
    print("  Device chosen from scan (MAC above). Bond uses direct connect.")
    print("  Cuff: hold Bluetooth until flashing  P  / -P-  (or already bonded)")
    if is_windows():
        print("  Windows: ACCEPT pairing dialog if shown.")
        if force_rebind:
            print("  Will try to remove old Windows bond first.")
    print("=" * 60)
    print()
    _prompt("Press Enter to start pair (cuff near PC)", "")

    try:
        await pair_omron(address, model, force_rebind=force_rebind)
        print("\n*** PAIR OK ***")
        print("Next: READ once, or LIVE MONITOR (auto every 60s).")
        print("Cuff: short-press BT for transfer mode (not flashing P).")
        return 0
    except Exception as exc:  # noqa: BLE001
        log.error("Pair failed: %s: %s", type(exc).__name__, exc)
        _log_winrt(exc, operation="interactive_omron_pair")
        msg = str(exc).lower()
        print()
        print("── Pair failed ──")
        print("  Windows said: Could not pair / FAILED  (or bond incomplete)")
        print()
        print("  Do this in order:")
        print("  1. Phone: open OMRON Connect → forget/unpair this cuff")
        print("  2. Windows: Settings → Bluetooth & devices → remove Omron/BLESmart")
        print("  3. Cuff: HOLD Bluetooth 3–5s until flashing  P  / -P-")
        print("  4. Here: choose RE-PAIR (not plain PAIR)")
        print("  5. Accept any Windows pairing popup (check taskbar)")
        print("  6. After OK: short-press BT → READ or LIVE")
        print()
        if "failed" in msg or "could not pair" in msg:
            print("  Note: pair() failed in ~0.1s usually means stale bond or not in P mode.")
            print("  Connect can still succeed without a good bond — that is normal on WinRT.")
        print("  If you paired successfully before on this PC, try READ first.")
        print("────────────────")
        return 1


async def _run_omron_read(address: str, model: str, output: str) -> int:
    from .omron_bridge import flatten_readings, read_omron

    print()
    print("=" * 60)
    print(f"  OMRON READ  model={model}  address={address}")
    print("  Short-press Bluetooth (transfer mode) — NOT flashing P.")
    print(f"  CSV output dir: {output}")
    print("=" * 60)
    try:
        all_users = await read_omron(
            address,
            model,
            find_timeout=90.0,
            session_retries=3,
            output_dir=output,
        )
    except Exception as exc:  # noqa: BLE001
        log.error("Read failed: %s: %s", type(exc).__name__, exc)
        _log_winrt(exc, operation="interactive_omron_read")
        msg = str(exc).lower()
        print()
        print("── Troubleshooting ──")
        bondish = any(
            s in msg
            for s in (
                "bond",
                "pair",
                "canceled",
                "cancelled",
                "0x800704c7",
                "-2147023673",
                "fe4a",
                "encrypt",
                "notify probe",
                "start_notify aborted",
            )
        )
        if bondish:
            print("  Bond / encrypted notify failed (FE4A up but CCCD denied):")
            print("  1. SHORT-press BT now (transfer mode — not flashing P)")
            print("  2. Retry READ immediately while cuff is awake")
            print("  3. If still fails: Windows Bluetooth → remove this cuff")
            print("  4. Phone OMRON Connect → forget cuff (one host only)")
            print("  5. Toolkit → RE-PAIR with cuff flashing P → accept popup")
            print("  6. Then short-press BT → READ again")
        elif "scanner" in msg or "watcher" in msg or "stopping" in msg or "aborted" in msg:
            print("  Scanner/radio busy (WinRT STOPPING/ABORTED):")
            print("  1. Quick Settings → Bluetooth Off → wait 3s → On")
            print("  2. Close other BLE apps; wait 2s; retry READ")
            print("  3. Toolkit will also try connect-by-MAC without scan")
        else:
            print("  1. Cuff < 1 m; short-press BT (transfer mode, not P)")
            print("  2. No phone Omron Connect connected")
            print("  3. If FE4A access denied: Remove cuff in Windows → PAIR → READ")
        if "unreachable" in msg or "access" in msg or "parent" in msg:
            print("  → Link/encryption race; retry after short-press BT often works.")
        print("─────────────────────")
        return 1

    flat = flatten_readings(all_users)
    print()
    print(f"--- Latest readings ({min(10, len(flat))} of {len(flat)}) ---")
    for i, r in enumerate(flat[:10]):
        print(f"  [{i}] {_brief(r)}")
    if not flat:
        print("  (no records — empty memory or wrong model map?)")
    return 0


async def _run_beurer(address: str, model: str) -> int:
    from .beurer.session import BeurerCompanionSession
    from .beurer.catalog import get_device

    meta = get_device(model)
    print()
    print("=" * 60)
    print(f"  BEURER companion sync  model={model}")
    if meta:
        print(f"  category={meta.primary_category}  protocol={meta.protocol_profile}")
        print(f"  toolkit_profile={meta.toolkit_profile}")
        print(f"  adv_names={list(meta.advertisement_names)}")
    print("  Timing: settle → pair → DIS → set-time → CCCD → quiet-end")
    print("  (HealthManager Pro–aligned; OCR not used)")
    if is_windows():
        print("  Windows: accept pairing dialog if shown.")
    print("=" * 60)
    print()
    _prompt("Press Enter to start (device advertising / ready)", "")

    from .beurer.capabilities import get_capabilities

    caps = get_capabilities(model)
    if caps.passkey_likely:
        print("  Note: this model often uses a 6-digit passkey on first pair.")
    if caps.settle_3s:
        print("  Note: 3s post-connect settle (APK marker te).")
    if caps.pulse_swapped:
        print("  Note: pulse SFLOAT byte-swap enabled (APK marker t6).")

    sess = BeurerCompanionSession(address, model_id=model, pair=True)
    try:
        result = await sess.run()
    except Exception as exc:  # noqa: BLE001
        log.error("Beurer session failed: %s", exc)
        _log_winrt(exc, operation="beurer_session")
        return 2
    print()
    print(f"*** SYNC {result.status.value.upper()} ***")
    print(f"  {result.message}")
    print(
        f"  readings={len(result.readings)}  raw={result.raw_count}  "
        f"dedup_drop={result.deduped_dropped}"
    )
    if result.passkey_hint and result.status.value.startswith("pair"):
        print("  → Remove Windows bond, re-run, enter 6-digit code from LCD.")
    for i, r in enumerate(result.readings[:20]):
        print(f"  [{i}] {_brief(r)}")
    return 0 if result.ok else 2


async def _run_live(
    brand: BrandChoice,
    address: str,
    *,
    model: str,
    interval: float = 60.0,
    output: Optional[str] = None,
) -> int:
    """Companion-style loop: scan/sync every interval, auto-update latest."""
    from .live_monitor import run_live_monitor

    print()
    print("=" * 60)
    print("  LIVE MONITOR — companion-style auto sync")
    print(f"  brand={brand.id}  model={model}  mac={address}")
    if brand.id in ("masimo",) or (brand.connect_profile or "") == "mightysat":
        print(
            f"  STREAMING mode: stay connected; dashboard refreshes on SpO2/PR "
            f"(UI ≥ {interval:.1f}s)"
        )
        print("  Keep finger in sensor. Ctrl+C stops.")
    else:
        print(f"  every {interval:.1f}s: connect → sync → update latest → sleep")
        print("  Leave device in transfer / advertise mode after measure.")
        print("  Ctrl+C stops and returns to menu.")
    print("=" * 60)
    _prompt("Press Enter to start live loop", "")
    try:
        return await run_live_monitor(
            brand_id=brand.id,
            brand_label=brand.label,
            model=model,
            address=address,
            connect_profile=brand.connect_profile,
            is_omron=brand.is_omron,
            interval_s=interval,
            mode="monitor",
            output_dir=output,
            pair=True,
        )
    except KeyboardInterrupt:
        print("\n  Live monitor stopped.")
        return 130


async def _run_generic_pair(
    brand: BrandChoice,
    address: str,
    *,
    model: str = "",
    force_rebind: bool = False,
) -> int:
    """
    Multi-brand OS pair (Masimo, Beurer, A&D, thermo, FORA, RE, …).

    Discovery already happened in _pick_address(for_pairing=True).
    Here we bond to the chosen MAC (+ optional unpair for RE-PAIR).
    """
    label = "RE-PAIR" if force_rebind else "PAIR"
    print()
    print("=" * 60)
    print(f"  {label}  brand={brand.id}  model={model or brand.default_model}")
    print(f"  address={address}")
    print("  Device chosen from scan. Connecting + OS bond…")
    if is_windows():
        print("  Windows: ACCEPT pairing dialog if shown.")
        if force_rebind:
            print("  Will try to remove old Windows bond first.")
    print("=" * 60)
    print()
    _prompt("Press Enter to start pair (device advertising / near PC)", "")

    if force_rebind and is_windows():
        try:
            from omron_bp.ble.connection import unpair_address

            print("  Removing previous Windows bond (best effort)…")
            await unpair_address(address)
            await asyncio.sleep(1.0)
        except Exception as exc:  # noqa: BLE001
            log.warning("Unpair skip: %s — continue with pair", exc)
            print(
                "  Note: auto-unpair failed. Remove the device in "
                "Settings → Bluetooth & devices if pair fails."
            )

    # Beurer has its own companion session (pair + protocol timing)
    if brand.id == "beurer":
        return await _run_beurer(address, model or brand.default_model or "BM54")

    assert brand.connect_profile
    profile = get_profile(brand.connect_profile)
    # Short listen after bond so we can confirm notifications if device streams
    duration = 15.0 if brand.id in ("masimo", "mightysat", "re", "fora") else 8.0

    client = MedicalBleClient(
        address=address,
        profile=profile,
        pair=is_windows(),
        connect_retries=2,
        auto_dispatch=(brand.id in ("re", "fora")),
    )
    try:
        await client.run(duration=duration, connect_timeout=35.0)
        print()
        print(f"*** {label} / CONNECT OK ***")
        print(f"  address={address}  brand={brand.id}")
        print("  Next: LIVE MONITOR or CONNECT to stream/read data.")
        if client.readings:
            print("--- Parsed samples during pair window ---")
            for i, r in enumerate(client.readings[:10]):
                print(f"  [{i}] {_brief(r)}")
        return 0
    except Exception as exc:  # noqa: BLE001
        log.error("%s failed: %s: %s", label, type(exc).__name__, exc)
        _log_winrt(exc, operation=f"interactive_{brand.id}_pair")
        print()
        print(f"── {label} failed ──")
        print(f"  {type(exc).__name__}: {exc}")
        print()
        print("  Do this in order:")
        print("  1. Device: put in pairing / advertising mode")
        print("  2. Windows: Settings → Bluetooth → remove this device if listed")
        print("  3. Here: choose RE-PAIR (scan → pick MAC+name again)")
        print("  4. Accept any Windows pairing popup (check taskbar)")
        print("────────────────")
        return 1


async def _run_connect(
    brand: BrandChoice,
    address: str,
    *,
    duration: float = DEFAULT_LISTEN_SEC,
    do_pair: bool = True,
    model: str = "",
) -> int:
    if brand.id == "beurer":
        return await _run_beurer(address, model or brand.default_model or "BM54")

    assert brand.connect_profile
    profile = get_profile(brand.connect_profile)
    print()
    print("=" * 60)
    print(f"  CONNECT  brand={brand.id}  profile={profile.id}")
    print(f"  address={address}  listen={duration:.0f}s  pair={do_pair}")
    print("  Watch [HEX] / [PARSE] lines. Trigger one action on the device.")
    print("=" * 60)

    client = MedicalBleClient(
        address=address,
        profile=profile,
        pair=do_pair and is_windows(),
        connect_retries=2,
        auto_dispatch=(brand.id in ("re", "fora")),
    )
    await client.run(duration=duration, connect_timeout=35.0)

    if client.readings:
        print()
        print("--- Parsed readings ---")
        for i, r in enumerate(client.readings[:20]):
            print(f"  [{i}] {_brief(r)}")
    elif client.raw_payloads:
        print(f"\n{len(client.raw_payloads)} raw notification(s); none parsed.")
    else:
        print("\nNo notifications. Check advertising, bonding, and action timing.")
    return 0


# ---------------------------------------------------------------------------
# Main wizard loop
# ---------------------------------------------------------------------------

async def run_interactive() -> int:
    setup_logging(verbose=True)
    cfg = load_config()

    print()
    print("=" * 60)
    print("  Medical BLE Toolkit — Interactive Mode")
    print("  Multi-brand HAL  |  default brand: Omron")
    print("  NEW: LIVE MONITOR = app-style sync every 60s + latest UI")
    print("=" * 60)
    if cfg:
        print(
            f"  Last session: brand={cfg.get('brand')}  "
            f"model={cfg.get('model')}  address={cfg.get('address')}"
        )
        if cfg.get("live_interval"):
            print(f"  Live interval: {cfg.get('live_interval')}s")
    print("  Press Enter at any prompt to accept the [default] value.")
    print("  Ctrl+C to quit (or stop a live loop).")

    while True:
        try:
            brand = _pick_brand(cfg)
            model = _pick_model(brand, cfg)
            action = _pick_action(brand)

            if action == "quit":
                print("Bye.")
                return 0

            # --- continuous scan (60s) or single scan ---------------------------
            if action in ("scan", "scan_once"):
                profile: Optional[DeviceProfile] = None
                if brand.connect_profile:
                    try:
                        profile = get_profile(brand.connect_profile)
                    except KeyError:
                        profile = None

                if action == "scan":
                    # App-style: keep scanning on a fixed interval
                    from .live_monitor import DEFAULT_INTERVAL_S, run_continuous_scan

                    iv_raw = _prompt(
                        "Scan interval seconds",
                        str(int(cfg.get("live_interval") or DEFAULT_INTERVAL_S)),
                    )
                    try:
                        interval = float(iv_raw)
                    except ValueError:
                        interval = DEFAULT_INTERVAL_S
                    save_config(
                        brand=brand.id,
                        model=model,
                        action=action,
                        live_interval=interval,
                    )
                    print()
                    print("  Continuous SCAN — dashboard refreshes each interval.")
                    print("  Ctrl+C to stop and return to menu.")
                    try:
                        await run_continuous_scan(
                            brand_id=brand.id,
                            brand_label=brand.label,
                            model=model,
                            connect_profile=brand.connect_profile,
                            interval_s=interval,
                        )
                    except KeyboardInterrupt:
                        print("\n  Scan loop stopped.")
                    cfg = load_config()
                    again = _prompt("Another action? (Y/n)", "Y").lower()
                    if again in ("n", "no"):
                        return 0
                    continue

                # scan_once — original one-shot discovery
                print()
                print("  Scanning ~8s — wake the device…")
                if is_windows():
                    print(
                        "  If WinRT aborts the scanner: toggle Bluetooth Off/On, "
                        "close nRF Connect, wait 2s, retry — or skip scan and "
                        "use known MAC on Connect/Pair/Read."
                    )
                try:
                    devices = await scan_devices(profile=profile, timeout=8.0)
                except Exception as exc:  # noqa: BLE001
                    log.error("Scan failed: %s", exc)
                    _log_winrt(exc, operation="interactive_scan")
                    devices = []
                if devices:
                    print()
                    print(f"  Found {len(devices)} device(s):")
                    for i, d in enumerate(devices[:20], 1):
                        print(f"    {i:2d}. {d.address}  {d.name or '(no name)'}")
                    if cfg.get("address"):
                        print(f"  Saved MAC still: {cfg.get('address')}")
                else:
                    print("  No devices listed (scanner failed or nothing advertising).")
                    if cfg.get("address"):
                        print(
                            f"  You can still Connect/Pair/Read with saved MAC "
                            f"{cfg.get('address')} (no scan needed)."
                        )
                again = _prompt("Another action? (Y/n)", "Y").lower()
                if again in ("n", "no"):
                    return 0
                continue

            # PAIR / RE-PAIR: always scan surroundings; never reuse saved MAC default.
            # READ / LIVE / CONNECT: may reuse last known address.
            address = await _pick_address(
                brand,
                cfg,
                do_scan=False,
                for_pairing=(action in ("pair", "repair")),
            )

            # Extra options for connect / live / read paths
            duration = DEFAULT_LISTEN_SEC
            output = cfg.get("output_dir") or DEFAULT_OUTPUT
            interval = float(cfg.get("live_interval") or 60.0)
            if brand.is_omron and action in ("read", "live"):
                output = _prompt("CSV output directory", str(output))
            if action == "live":
                is_stream_brand = brand.id in ("masimo",) or (
                    brand.connect_profile or ""
                ) == "mightysat"
                is_thermo = brand.id in ("thermo",) or (
                    brand.connect_profile or ""
                ) == "thermometer"
                # Masimo: 1s stream UI. Thermometer: 10s (needs dual-wake session).
                if is_stream_brand:
                    default_iv = "1"
                elif is_thermo:
                    default_iv = "10"
                else:
                    default_iv = str(int(interval) if interval >= 1 else 60)
                iv_raw = _prompt(
                    "Live interval seconds"
                    + (
                        " (Masimo STREAM: 1)"
                        if is_stream_brand
                        else " (NT-100B: 10+ recommended)"
                        if is_thermo
                        else ""
                    ),
                    default_iv,
                )
                try:
                    interval = float(iv_raw)
                except ValueError:
                    interval = (
                        1.0 if is_stream_brand else 10.0 if is_thermo else 60.0
                    )
                if interval < 1.0:
                    print("  Interval minimum is 1s — using 1.")
                    interval = 1.0
                if is_stream_brand and interval > 5.0:
                    print(
                        f"  Note: Masimo streams continuously; "
                        f"UI refresh capped to 1s (you entered {interval:.0f}s)."
                    )
                    interval = 1.0
                if is_thermo and interval < 5.0:
                    print(
                        "  Note: NT-100B needs ~18s per poll (dual-wake + history). "
                        "Using interval 10s to avoid reconnect thrash."
                    )
                    interval = 10.0
            if not brand.is_omron and action == "connect":
                d_raw = _prompt("Listen duration (seconds)", str(int(DEFAULT_LISTEN_SEC)))
                try:
                    duration = float(d_raw)
                except ValueError:
                    duration = DEFAULT_LISTEN_SEC

            # Persist choices before long BLE work
            save_config(
                brand=brand.id,
                model=model,
                address=address,
                action=action,
                output_dir=output if brand.is_omron else cfg.get("output_dir"),
                live_interval=interval if action == "live" else cfg.get("live_interval"),
            )
            cfg = load_config()

            if action == "live":
                code = await _run_live(
                    brand,
                    address,
                    model=model,
                    interval=interval,
                    output=str(output) if brand.is_omron else None,
                )
            elif action in ("pair", "repair"):
                force = action == "repair"
                if brand.is_omron:
                    code = await _run_omron_pair(
                        address, model, force_rebind=force
                    )
                else:
                    code = await _run_generic_pair(
                        brand,
                        address,
                        model=model,
                        force_rebind=force,
                    )
                if code == 0:
                    start_live = _prompt(
                        "Start LIVE MONITOR now (sync every 60s)? (Y/n)", "Y"
                    ).lower()
                    if start_live not in ("n", "no"):
                        code = await _run_live(
                            brand,
                            address,
                            model=model,
                            interval=interval,
                            output=str(output) if brand.is_omron else None,
                        )
            elif brand.is_omron:
                if action == "read":
                    code = await _run_omron_read(address, model, str(output))
                else:
                    print(f"Unknown action {action}")
                    code = 2
            elif action == "connect":
                code = await _run_connect(
                    brand, address, duration=duration, model=model
                )
            else:
                print(f"Unknown action {action}")
                code = 2

            print()
            again = _prompt("Run another action? (Y/n)", "Y").lower()
            if again in ("n", "no"):
                return code
            # refresh config for next loop defaults
            cfg = load_config()
        except KeyboardInterrupt:
            print("\nInterrupted.")
            return 130


def main() -> int:
    try:
        return asyncio.run(run_interactive())
    except KeyboardInterrupt:
        print("\nInterrupted.")
        return 130


if __name__ == "__main__":
    raise SystemExit(main())

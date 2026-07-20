"""
Model registry — resolve model_id / alias → DeviceProfile.

Usage:
  from medical_ble_toolkit.omron_bp.models.registry import get_profile, list_models
  profile = get_profile("HEM-7143T1")
"""

from __future__ import annotations

from typing import Dict, List

from medical_ble_toolkit.omron_bp.logging_config import DBG_TAG, get_logger
from medical_ble_toolkit.omron_bp.models.base import DeviceProfile

logger = get_logger("models.registry")

_PROFILES: Dict[str, DeviceProfile] = {}
_ALIASES: Dict[str, str] = {}
_LOADED = False


def register(profile: DeviceProfile) -> None:
    """Register a profile and all of its aliases."""
    profile.validate()
    key = profile.model_id.upper()
    if key in _PROFILES:
        raise ValueError(f"Duplicate model registration: {profile.model_id}")
    _PROFILES[key] = profile
    _ALIASES[key.lower()] = key
    for alias in profile.aliases:
        alias_l = alias.strip().lower()
        if not alias_l:
            continue
        # First registration wins for alias collisions
        if alias_l not in _ALIASES:
            _ALIASES[alias_l] = key
        else:
            # DBG-LOG: alias already claimed
            logger.debug(
                "%s alias skip %r already → %s (wanted %s)",
                DBG_TAG,
                alias,
                _ALIASES[alias_l],
                key,
            )
    # DBG-LOG
    logger.debug(
        "%s registered model=%s stack=%s pairing=%s aliases=%d",
        DBG_TAG,
        profile.model_id,
        profile.stack.value,
        profile.pairing_mode.value,
        len(profile.aliases),
    )


def _load_builtin_profiles() -> None:
    global _LOADED
    if _LOADED:
        return
    from medical_ble_toolkit.omron_bp.models.profiles.catalog import build_all_profiles

    for profile in build_all_profiles():
        register(profile)

    _LOADED = True
    logger.info(
        "Loaded %d Omron model profile(s) (%d name aliases)",
        len(_PROFILES),
        len(_ALIASES),
    )
    # DBG-LOG
    logger.debug("%s models=%s", DBG_TAG, sorted(_PROFILES.keys()))


def ensure_loaded() -> None:
    _load_builtin_profiles()


def get_profile(model: str) -> DeviceProfile:
    """Resolve model name or alias (case-insensitive) to a DeviceProfile."""
    ensure_loaded()
    if not model or not str(model).strip():
        raise ValueError("model name is empty")
    canonical = _ALIASES.get(model.strip().lower())
    if canonical is None:
        known = ", ".join(sorted(_PROFILES.keys()))
        raise KeyError(f"Unknown model '{model}'. Known canonical ids: {known}")
    return _PROFILES[canonical]


def list_models() -> List[DeviceProfile]:
    ensure_loaded()
    return [_PROFILES[k] for k in sorted(_PROFILES.keys())]


def list_model_ids() -> List[str]:
    ensure_loaded()
    return sorted(_PROFILES.keys())


def resolve_canonical_id(model: str) -> str:
    """Return canonical model_id for a name or alias."""
    return get_profile(model).model_id

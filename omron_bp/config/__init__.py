"""Optional persisted device preference (last MAC / model)."""

from omron_bp.config.store import load_device_config, save_device_config

__all__ = ["load_device_config", "save_device_config"]

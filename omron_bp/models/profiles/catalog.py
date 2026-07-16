"""
Unified Omron device profile catalog.

Sources merged (prefer omblepy parsers where they were hardware-validated):
  - hass-omron custom_components/omron/omron_ble/device_catalog.py
  - omblepy deviceSpecific/*.py

Each entry is a DeviceProfile registered by models.registry.
Regional / sold-as names live in `aliases` so CLI `-d` accepts them.

TOKEN_KEY unlock (0x11/0x91) is implemented in OmronTransport.unlock_with_token
(phone HCI + hass-omron). HEM-7143T1 requires it (OMRON Connect capture).
"""

from __future__ import annotations

from typing import List

from omron_bp.models.base import (
    DeviceProfile,
    Endianness,
    UnlockMode,
    classic_profile,
    modern_profile,
)
from omron_bp.models.parsers import (
    parse_classic_vital_14,
    parse_classic_vital_14_6232_family,
    parse_classic_vital_14_bitpacked,
    parse_classic_vital_16_6401_family,
    parse_vital_16_715x_bitpacked,
)


def build_all_profiles() -> List[DeviceProfile]:
    """Return every built-in profile (canonical models + their aliases)."""
    profiles: List[DeviceProfile] = []

    def add(**kwargs) -> None:
        profiles.append(DeviceProfile(**kwargs))

    # =====================================================================
    # CLASSIC multi-channel stack (unlock key)
    # =====================================================================

    add(
        **classic_profile(
            model_id="HEM-6320T",
            display_name="Omron HEM-6320T (wrist)",
            endianness=Endianness.BIG,
            user_start_addresses=[0x0370],
            per_user_records_count=[100],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0F74,
            settings_write_address=0x0F9A,
            settings_unread_records_bytes=(0x00, 0x08),
            settings_time_sync_bytes=(0x14, 0x1E),
            parse_record=parse_classic_vital_14_bitpacked,
            aliases=("HEM-6320T-Z",),
            source="hass-omron",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-6321T",
            display_name="Omron HEM-6321T (wrist, dual user)",
            endianness=Endianness.BIG,
            user_start_addresses=[0x0370, 0x08E8],
            per_user_records_count=[100, 100],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0F74,
            settings_write_address=0x0F9A,
            settings_unread_records_bytes=(0x00, 0x08),
            settings_time_sync_bytes=(0x14, 0x1E),
            parse_record=parse_classic_vital_14_bitpacked,
            aliases=("HEM-6321T-Z",),
            source="hass-omron",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-6401T",
            display_name="Omron HEM-6401T (wrist family)",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x1350],
            per_user_records_count=[100],
            record_byte_size=0x10,
            transmission_block_size=0x10,
            settings_read_address=0x0100,
            settings_write_address=0x0160,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x10, 0x20),
            parse_record=parse_classic_vital_16_6401_family,
            aliases=("HEM-6401T-Z", "HEM-6402T-Z", "HEM-6410T-Z"),
            source="hass-omron",
            notes="Only BP data_5 area mapped.",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7320T",
            display_name="Omron HEM-7320T",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02AC, 0x05F4],
            per_user_records_count=[60, 60],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x0286,
            settings_unread_records_bytes=(0x00, 0x08),
            settings_time_sync_bytes=(0x14, 0x1E),
            parse_record=parse_classic_vital_14_bitpacked,
            aliases=(
                "HEM-7320T-CA",
                "HEM-7320T-CACS",
                "HEM-7320T-ZV",
                "HEM-7320T_TI-CA",
                "HEM-7320T_TI-Z",
                "HEM-8725T-WM",
            ),
            source="hass-omron",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7322T",
            display_name="Omron HEM-7322T / M700 Intelli IT",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02AC, 0x0824],
            per_user_records_count=[100, 100],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x0286,
            settings_unread_records_bytes=(0x00, 0x08),
            settings_time_sync_bytes=(0x14, 0x1E),
            parse_record=parse_classic_vital_14_bitpacked,
            aliases=(
                "M700 Intelli IT",
                "HEM-7321T-CA",
                "HEM-7321T_TI-CA",
                "HEM-7321T_TI-Z",
                "HEM-7280T-AP",
                "HEM-7280T-E",
                "HEM-7280T_TI-D",
                "HEM-7280T_TI-E",
                "HEM-7281T",
                "HEM-7282T",
                "HEM-7321T-ZV",
                "HEM-7322T-D",
                "HEM-7322T-E",
                "HEM-7511T",
                "HEM-8732K-SH",
                "HEM-8732T-SH",
            ),
            source="both",
            notes="Reference classic dual-user profile from omblepy + hass-omron.",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7600T",
            display_name="Omron HEM-7600T / Evolv",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02AC],
            per_user_records_count=[100],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x0286,
            settings_unread_records_bytes=(0x00, 0x08),
            settings_time_sync_bytes=(0x14, 0x1E),
            parse_record=parse_classic_vital_14_bitpacked,
            aliases=(
                "Omron Evolv",
                "HEM-7270C",
                "HEM-7271T",
                "HEM-7325T",
                "HEM-7600T-E",
                "HEM-7600T-Z",
                "HEM-7600T-ZCD6BK",
                "HEM-7600T-SH3BK",
                "HEM-7600T2-JF",
                "HEM-7600T_W",
                "HEM-7600T_W-SH3W",
                "HEM-7600T_W-Z",
                "HEM-9601T-J3",
                "HEM-9601T2-BR3",
                "HEM-9601T_E3",
                "HEM-9700T",
            ),
            source="both",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-6232T",
            display_name="Omron HEM-6232T / RS7 Intelli IT",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02E8, 0x0860],
            per_user_records_count=[100, 100],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=(0x00, 0x08),
            # omblepy notes time-sync offsets may be wrong; hass uses 0x2C-0x3C
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14_6232_family,
            aliases=(
                "RS7 Intelli IT",
                "HEM-1026T2-AJC",
                "HEM-1026T2-AJE",
                "HEM-1026T2-AKA",
                "HEM-6232T-AP",
                "HEM-6232T-D",
                "HEM-6232T-E",
                "HEM-6232T-Z",
                "HEM-6233T",
                "HEM-6320T-SH",
                "HEM-6322T-SH",
                "HEM-6323T",
                "HEM-6324T",
                "HEM-6325T",
            ),
            source="both",
            notes="Year packed at bits 18..23 (6232 family parser).",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7530T",
            display_name="Omron HEM-7530T / Complete (BP only, no EKG)",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02E8],
            per_user_records_count=[90],
            record_byte_size=0x0E,
            transmission_block_size=0x10,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            # omblepy uses year bits 18..23 → 6232 family parser
            parse_record=parse_classic_vital_14_6232_family,
            aliases=(
                "Omron Complete",
                "HEM-6231T2-JC",
                "HEM-6231T2-JE",
                "HEM-6231T2-JT3",
                "HEM-7271P-SH3",
                "HEM-7271T_SH3",
                "HEM-7530T-Z",
                "HEM-7530T1-BR3",
                "HEM-7530T_AP3",
                "HEM-7530T_E3",
                "HEM-7530T_J3",
                "HEM-7530T_JT3",
                "HEM-8630T-SH",
            ),
            source="both",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-6161T",
            display_name="Omron HEM-6161T (30-slot 7530 family)",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02E8],
            per_user_records_count=[30],
            record_byte_size=0x0E,
            transmission_block_size=0x10,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14_6232_family,
            aliases=(
                "HEM-6161T-E",
                "HEM-6161T-RU",
                "HEM-6161T2-BR",
                "HEM-7271L-SH3",
            ),
            source="hass-omron",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7136T",
            display_name="Omron HEM-7136T (60-slot 7530 family)",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02E8],
            per_user_records_count=[60],
            record_byte_size=0x0E,
            transmission_block_size=0x10,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14_6232_family,
            aliases=(
                "HEM-7136T-SH3",
                "HEM-7138JT-SH",
                "HEM-7138T-SH",
                "HEM-7139T-SH3",
            ),
            source="hass-omron",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-6231T",
            display_name="Omron HEM-6231T (100-slot 7530 family)",
            endianness=Endianness.BIG,
            user_start_addresses=[0x02E8],
            per_user_records_count=[100],
            record_byte_size=0x0E,
            transmission_block_size=0x10,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14_6232_family,
            aliases=("HEM-6231T-SH", "HEM-6231T_Z"),
            source="hass-omron",
        )
    )

    # Classic-stack little-endian 16-byte records (omblepy-validated bitfields)
    add(
        **classic_profile(
            model_id="HEM-7150T",
            display_name="Omron HEM-7150T / BP7250",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x0098],
            per_user_records_count=[60],
            record_byte_size=0x10,
            transmission_block_size=0x10,
            settings_read_address=0x0010,
            settings_write_address=0x0054,
            settings_unread_records_bytes=(0x00, 0x10),
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_vital_16_715x_bitpacked,
            aliases=(
                "BP7250",
                "HEM-7150T-CA",
                "HEM-7150T-Z",
                "HEM-7153JT_ASH",
                "HEM-7153T_ASH",
                "HEM-7156T-BR",
                "HEM-7156T-LA",
                "HEM-7156T_AAP",
                "HEM-7156T_AP",
                "HEM-7157T-AP",
                "HEM-7158T-JC",
                "HEM-7158T_AP3",
            ),
            source="both",
            notes="Parser from omblepy (16-byte LE bitfields), not hass byte-aligned.",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7151T",
            display_name="Omron HEM-7151T (80 slots, 7150 family)",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x0098],
            per_user_records_count=[80],
            record_byte_size=0x10,
            transmission_block_size=0x10,
            settings_read_address=0x0010,
            settings_write_address=0x0054,
            settings_unread_records_bytes=(0x00, 0x10),
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_vital_16_715x_bitpacked,
            aliases=("HEM-7151T-Z",),
            source="hass-omron+omblepy-parser",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7155T",
            display_name="Omron HEM-7155T / M4 / X4 Smart (classic stack)",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x0098, 0x0458],
            per_user_records_count=[60, 60],
            record_byte_size=0x10,
            transmission_block_size=0x10,
            settings_read_address=0x0010,
            settings_write_address=0x0054,
            settings_unread_records_bytes=(0x00, 0x10),
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_vital_16_715x_bitpacked,
            aliases=(
                "M400",
                "M4 Smart",
                "X4 Smart",
                "HEM-7155T-ALRU",
                "HEM-7155T-D",
                "HEM-7155T-EBK",
                "HEM-7155T-EBL",
                "HEM-7155T_AP",
                "HEM-7155T_ASH3BK",
                "HEM-7155T_ASH3SL",
                "HEM-7155T_ESL",
                "HEM-7340T-CA",
                "HEM-7340T-Z",
                "HEM-7341T-Z",
            ),
            source="both",
            notes="Classic-stack X4. Modern firmware → HEM-7155T-MW / MW3 / K4.",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7342T",
            display_name="Omron HEM-7342T / BP7450",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x0098, 0x06D8],
            per_user_records_count=[100, 100],
            record_byte_size=0x10,
            transmission_block_size=0x10,
            settings_read_address=0x0010,
            settings_write_address=0x0054,
            settings_unread_records_bytes=(0x00, 0x10),
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_vital_16_715x_bitpacked,
            aliases=(
                "BP7450",
                "HEM-7159T_AP3",
                "HEM-7342T-CA",
                "HEM-7342T-Z",
                "HEM-7342T1-ACACD6",
                "HEM-7343T-Z",
                "HEM-7344JT_ASH3",
                "HEM-7344T_ASH3BK",
                "HEM-7344T_ASH3SL",
                "HEM-7346T-AJC3",
                "HEM-7346T-AJE3",
                "HEM-7346T2-AJC32",
                "HEM-7346T2-AJE32",
                "HEM-7346T_ABR3",
                "HEM-7346T_AP3",
                "HEM-7347T-AJC3",
                "HEM-7347T-AJE3",
                "HEM-7347T2-AJC32",
                "HEM-7347T2-AJE32",
                "HEM-7349T_ABR",
            ),
            source="both",
        )
    )

    add(
        **classic_profile(
            model_id="HEM-7361T",
            display_name="Omron HEM-7361T / M500 / M7 Intelli IT",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x0098, 0x06D8],
            per_user_records_count=[100, 100],
            record_byte_size=0x10,
            transmission_block_size=0x10,
            settings_read_address=0x0010,
            settings_write_address=0x0054,
            settings_unread_records_bytes=(0x00, 0x10),
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_vital_16_715x_bitpacked,
            aliases=(
                "M500 Intelli IT",
                "M7 Intelli IT",
                "HEM-7361T-ALRU",
                "HEM-7361T-AP",
                "HEM-7361T-D",
                "HEM-7361T-EBK",
                "HEM-7361T1-BS",
                "HEM-7361T_ESL",
            ),
            source="both",
        )
    )

    # =====================================================================
    # MODERN FE4A stack (OS bonding)
    # =====================================================================

    # Lab primary — same EEPROM as hass HEM-7146T
    # Phone HCI (OMRON Connect): TOKEN 0x11/0x91 on unlock char before START.
    add(
        **modern_profile(
            model_id="HEM-7143T1",
            display_name="Omron HEM-7143T1 (lab cuff / modern FE4A)",
            endianness=Endianness.LITTLE,
            unlock_mode=UnlockMode.TOKEN_KEY,
            user_start_addresses=[0x02E8],
            per_user_records_count=[30],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14,
            aliases=(
                "HEM-7146T",
                "HEM-7143T1-AIN",
                "HEM-7143T1-AP",
                "HEM-7143T1-D",
                "HEM-7143T1-E",
                "HEM-7143T1_D",
                "HEM-7143T1_EBK",
                "HEM-7143T2-E",
                "HEM-7143T2_ESL",
                "HEM-7144T1-AU",
                "HEM-7144T2-BR",
                "HEM-7144T2-LA",
                "HEM-7146T2-EBK",
                "HEM-7146T2-ESL",
                "HEM-7146T2-JD",
                "HEM-7146T2-JF",
                "HEM-716DT2-LA",
            ),
            source="lab+hass-omron+phone-hci",
            notes=(
                "OS bond (flashing P) once. Each read: TOKEN 0x11/0x91 then START. "
                "Short-press BT for transfer mode."
            ),
        )
    )

    add(
        **modern_profile(
            model_id="HEM-7142T2",
            display_name="Omron HEM-7142T2 (modern, small buffer)",
            endianness=Endianness.LITTLE,
            unlock_mode=UnlockMode.TOKEN_KEY,
            user_start_addresses=[0x02E8],
            per_user_records_count=[14],
            record_byte_size=0x0E,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14,
            aliases=(
                "HEM-7138K-SH",
                "HEM-7140T1-AP",
                "HEM-7141T1-AP",
                "HEM-7142T1-AP",
                "HEM-7142T2-AP",
                "HEM-7142T2-Z",
                "HEM-7142T2-ZAZ",
                "HEM-7142T2_JAZ",
                "HEM-716BT2-ZAZ",
                "HEM-716CT2-Z",
            ),
            source="hass-omron",
            notes="TOKEN_KEY 0x11/0x91 before START (implemented).",
        )
    )

    add(
        **modern_profile(
            model_id="HEM-7155T-MW",
            display_name="Omron HEM-7155T modern V2 (OS bond)",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x0098, 0x0458],
            per_user_records_count=[60, 60],
            record_byte_size=0x10,
            transmission_block_size=0x38,
            settings_read_address=0x0010,
            settings_write_address=0x0054,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            # hass uses byte-aligned classic_vital_14 on modern firmware
            parse_record=parse_classic_vital_14,
            source="hass-omron",
        )
    )

    add(
        **modern_profile(
            model_id="HEM-7155T-K4",
            display_name="Omron HEM-7155T-K4 modern",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x02E8, 0x06A8],
            per_user_records_count=[60, 60],
            record_byte_size=0x10,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14,
            aliases=(
                "HEM-7155T_K4-D",
                "HEM-7155T_K4-EBK",
                "HEM-7155T_K4-ESL",
                "HEM-7340T_K4-CA",
                "HEM-7340T_K4-Z",
                "HEM-7341T_K4-Z",
            ),
            source="hass-omron",
        )
    )

    add(
        **modern_profile(
            model_id="HEM-7155T-MW3",
            display_name="Omron HEM-7155T modern V3 / X4 Smart FE4A",
            endianness=Endianness.LITTLE,
            unlock_mode=UnlockMode.TOKEN_KEY,
            user_start_addresses=[0x02E8, 0x06A8],
            per_user_records_count=[60, 60],
            record_byte_size=0x10,
            transmission_block_size=0x38,
            settings_read_address=0x0260,
            settings_write_address=0x02A4,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14,
            aliases=("HEM-7155T_ESL1",),
            source="hass-omron",
            notes="TOKEN_KEY (0x11/0x91) may be required outside pairing grace window.",
        )
    )

    add(
        **modern_profile(
            model_id="HEM-7380T1",
            display_name="Omron HEM-7380T1 / X7 Smart AFib",
            endianness=Endianness.LITTLE,
            unlock_mode=UnlockMode.TOKEN_KEY,
            user_start_addresses=[0x01C4, 0x0804],
            per_user_records_count=[100, 100],
            record_byte_size=0x10,
            transmission_block_size=0x38,
            settings_read_address=0x0010,
            settings_write_address=0x0054,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=(0x2C, 0x3C),
            parse_record=parse_classic_vital_14,
            aliases=(
                "X7 Smart AFib",
                "M7 Intelli IT AFib",
                "HEM-7183T1-AP",
                "HEM-7183T1-CAP",
                "HEM-7183T1_FLBIN",
                "HEM-7183T1_FLIN",
                "HEM-7183T1_LAP",
                "HEM-7188T1-LE",
                "HEM-7188T1-LEO",
                "HEM-7194T1-FLAP",
                "HEM-7194T1-FLCAP",
                "HEM-7194T1_FLBIN",
                "HEM-7194T1_FLIN",
                "HEM-7196T1-FLE",
                "HEM-7196T1-FLEO",
                "HEM-7376T1-ACACD6",
                "HEM-7376T1-Z",
                "HEM-7377T1-ZAZ",
                "HEM-7380T",
                "HEM-7380T1-EBK",
                "HEM-7380T1-EOSL",
                "HEM-7381T1-AZ",
                "HEM-7382T1",
                "HEM-7382T1-AZAZ",
                "HEM-7383T1-AP",
                "HEM-7384T1-NBBR",
                "HEM-7385T1-AJAZ3",
                "HEM-7386T1-AJF3",
                "HEM-7387T1-AJAZ3",
                "HEM-7388T1-AJF3",
                "HEM-7389T1-JM3",
            ),
            source="both",
            notes=(
                "omblepy has no settings map; hass maps 0x0010/0x0054. "
                "TOKEN_KEY unlock may apply. Bond once (avoid re-pair churn)."
            ),
        )
    )

    # omblepy-specific BP5360 layout (slightly different addresses than 7380)
    add(
        **modern_profile(
            model_id="HEM-7377T1",
            display_name="Omron HEM-7377T1 / BP5360",
            endianness=Endianness.LITTLE,
            user_start_addresses=[0x01CC, 0x080C],
            per_user_records_count=[100, 100],
            record_byte_size=0x10,
            transmission_block_size=0x38,
            settings_read_address=0x0040,
            settings_write_address=0x0088,
            settings_unread_records_bytes=None,
            settings_time_sync_bytes=None,
            parse_record=parse_classic_vital_14,
            aliases=("BP5360",),
            source="omblepy",
            notes=(
                "Addresses offset +8 vs 7380T1 (header before records). "
                "User1/User2 may share slot metadata (see omblepy driver). "
                "Time sync not supported here."
            ),
        )
    )

    return profiles

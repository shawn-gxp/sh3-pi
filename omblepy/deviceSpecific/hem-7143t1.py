"""Driver for Omron HEM-7143T1 (and close relatives).

Profile derived from hass-omron HEM-7146T, which lists HEM-7143T1-* as
equivalent models (modern FE4A stack, OS bonding, no app-layer unlock key).

Sold-as names vary by region; hardware BLE name is typically
BLESmart_... with Omron manufacturer ID 0x020E.

Pairing: OS-managed BLE bond only (supportsOsBondingOnly).
  1. Put cuff in -P- mode (hold Bluetooth button 3-5s).
  2. python omblepy.py -p -d HEM-7143T1 -m <MAC>
  3. Accept Windows pairing prompt if shown.
  4. Later reads (transfer mode, not -P-):
     python omblepy.py -d HEM-7143T1 -m <MAC>

EEPROM layout / record format are best-effort from hass-omron and may need
tweaking if sys/dia/dates look wrong. Prefer --loggerDebug when debugging.
"""
import datetime
import logging
import sys

logger = logging.getLogger("omblepy")

sys.path.append("..")
from sharedDriver import sharedDeviceDriverCode


class deviceSpecificDriver(sharedDeviceDriverCode):
    # Modern Omron stack (same as HEM-7380T1 / HEM-7146T in hass-omron)
    parentService_UUID = "0000fe4a-0000-1000-8000-00805f9b34fb"
    deviceRxChannelUUIDs = ["49123040-aee8-11e1-a74d-0002a5d5c51b"]
    deviceTxChannelUUIDs = ["db5b55e0-aee7-11e1-965e-0002a5d5c51b"]
    requiresUnlock = False
    supportsPairing = False
    supportsOsBondingOnly = True

    deviceEndianess = "little"
    # hass-omron HEM-7146T: single user, 30 x 0x0E records starting 0x02E8
    userStartAdressesList = [0x02E8]
    perUserRecordsCountList = [30]
    recordByteSize = 0x0E
    transmissionBlockSize = 0x38

    settingsReadAddress = 0x0260
    settingsWriteAddress = 0x02A4
    # Unread counter not mapped for this family in hass-omron
    settingsUnreadRecordsBytes = None
    settingsTimeSyncBytes = [0x2C, 0x3C]

    def deviceSpecific_ParseRecordFormat(self, singleRecordAsByteArray):
        """Classic 14-byte vital record (byte-aligned, little-endian flags).

        Matches hass-omron parse_classic_vital_14 used by HEM-7146T.
        """
        data = singleRecordAsByteArray
        raw_sys = data[0]
        if raw_sys > 0xE1:
            raise ValueError("record slot is empty")

        recordDict = dict()
        recordDict["sys"] = raw_sys + 25
        recordDict["dia"] = data[1]
        recordDict["bpm"] = data[2]

        year = 2000 + (data[3] & 0x3F)
        flags1 = data[4] | (data[5] << 8)
        flags2 = data[6] | (data[7] << 8)

        hour = flags1 & 0x1F
        day = (flags1 >> 5) & 0x1F
        month = (flags1 >> 10) & 0x0F
        recordDict["ihb"] = (flags1 >> 14) & 0x01
        recordDict["mov"] = (flags1 >> 15) & 0x01
        second = min(flags2 & 0x3F, 59)
        minute = min((flags2 >> 6) & 0x3F, 59)

        if (
            data[1] == 0
            and data[2] == 0
            and (data[3] & 0x3F) == 0
            and flags1 == 0
            and flags2 == 0
        ):
            raise ValueError("record slot is empty")

        if not (1 <= month <= 12) or not (1 <= day <= 31) or hour > 23:
            raise ValueError("record slot is empty / invalid date")

        recordDict["datetime"] = datetime.datetime(
            year, month, day, hour, minute, second
        )
        return recordDict

    def deviceSpecific_syncWithSystemTime(self):
        """Modern offset-8 time layout at settingsTimeSyncBytes [0x2C, 0x3C]."""
        if self.settingsTimeSyncBytes is None:
            raise ValueError("time sync not configured for this device")

        timeSyncSettingsCopy = self.cachedSettingsBytes[
            slice(*self.settingsTimeSyncBytes)
        ]
        try:
            year, month, day, hour, minute, second = [
                int(b) for b in timeSyncSettingsCopy[8:14]
            ]
            logger.info(
                "device is set to date: %s",
                datetime.datetime(
                    year + 2000, month, day, hour, minute, second
                ).strftime("%Y-%m-%d %H:%M:%S"),
            )
        except Exception:
            logger.warning("device is set to an invalid date")

        currentTime = datetime.datetime.now()
        setNewTimeDataBytes = bytearray(timeSyncSettingsCopy[0:8])
        setNewTimeDataBytes += bytes(
            [
                currentTime.year - 2000,
                currentTime.month,
                currentTime.day,
                currentTime.hour,
                currentTime.minute,
                currentTime.second,
            ]
        )
        setNewTimeDataBytes += bytes([sum(setNewTimeDataBytes) & 0xFF, 0x00])
        self.cachedSettingsBytes[slice(*self.settingsTimeSyncBytes)] = (
            setNewTimeDataBytes
        )
        logger.info(
            "settings updated to new date %s",
            currentTime.strftime("%Y-%m-%d %H:%M:%S"),
        )

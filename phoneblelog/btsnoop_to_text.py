#!/usr/bin/env python3
"""Convert a binary BTSnoop HCI capture (.cfa / .log / .btsnoop) to a readable text log."""

from __future__ import annotations

import argparse
import struct
import sys
from datetime import datetime, timezone
from pathlib import Path

# BTSnoop timestamps are microseconds since 0 AD (Gregorian), not Unix epoch.
# Epoch offset: 0 AD -> 1970-01-01 is 0x00dcddb30f2f8000 microseconds.
BTSNOOP_EPOCH_DELTA_US = 0x00DCDDB30F2F8000

H4_TYPES = {
    0x01: "HCI_CMD",
    0x02: "ACL_DATA",
    0x03: "SCO_DATA",
    0x04: "HCI_EVT",
    0x05: "ISO_DATA",
}

# HCI status codes (common)
STATUS = {
    0x00: "Success",
    0x01: "Unknown HCI Command",
    0x02: "Unknown Connection Identifier",
    0x03: "Hardware Failure",
    0x04: "Page Timeout",
    0x05: "Authentication Failure",
    0x06: "PIN or Key Missing",
    0x07: "Memory Capacity Exceeded",
    0x08: "Connection Timeout",
    0x09: "Connection Limit Exceeded",
    0x0A: "Synchronous Connection Limit Exceeded",
    0x0B: "Connection Already Exists",
    0x0C: "Command Disallowed",
    0x0D: "Connection Rejected Limited Resources",
    0x0E: "Connection Rejected Security Reasons",
    0x0F: "Connection Rejected Unacceptable BD_ADDR",
    0x10: "Connection Accept Timeout Exceeded",
    0x11: "Unsupported Feature or Parameter Value",
    0x12: "Invalid HCI Command Parameters",
    0x13: "Remote User Terminated Connection",
    0x14: "Remote Device Terminated Connection Low Resources",
    0x15: "Remote Device Terminated Connection Power Off",
    0x16: "Connection Terminated By Local Host",
    0x17: "Repeated Attempts",
    0x18: "Pairing Not Allowed",
    0x19: "Unknown LMP PDU",
    0x1A: "Unsupported Remote Feature",
    0x1B: "SCO Offset Rejected",
    0x1C: "SCO Interval Rejected",
    0x1D: "SCO Air Mode Rejected",
    0x1E: "Invalid LMP Parameters / Invalid LL Parameters",
    0x1F: "Unspecified Error",
    0x20: "Unsupported LMP Parameter Value / Unsupported LL Parameter Value",
    0x21: "Role Change Not Allowed",
    0x22: "LMP Response Timeout / LL Response Timeout",
    0x23: "LMP Error Transaction Collision / LL Procedure Collision",
    0x24: "LMP PDU Not Allowed",
    0x25: "Encryption Mode Not Acceptable",
    0x26: "Link Key Cannot Be Changed",
    0x27: "Requested QoS Not Supported",
    0x28: "Instant Passed",
    0x29: "Pairing With Unit Key Not Supported",
    0x2A: "Different Transaction Collision",
    0x2C: "QoS Unacceptable Parameter",
    0x2D: "QoS Rejected",
    0x2E: "Channel Classification Not Supported",
    0x2F: "Insufficient Security",
    0x30: "Parameter Out Of Mandatory Range",
    0x32: "Role Switch Pending",
    0x34: "Reserved Slot Violation",
    0x35: "Role Switch Failed",
    0x36: "Extended Inquiry Response Too Large",
    0x37: "Secure Simple Pairing Not Supported By Host",
    0x38: "Host Busy Pairing",
    0x39: "Connection Rejected No Suitable Channel Found",
    0x3A: "Controller Busy",
    0x3B: "Unacceptable Connection Parameters",
    0x3C: "Advertising Timeout",
    0x3D: "Connection Terminated MIC Failure",
    0x3E: "Connection Failed To Be Established / Synchronization Timeout",
    0x3F: "MAC Connection Failed",
    0x40: "Coarse Clock Adjustment Rejected",
    0x41: "Type0 Submap Not Defined",
    0x42: "Unknown Advertising Identifier",
    0x43: "Limit Reached",
    0x44: "Operation Cancelled by Host",
    0x45: "Packet Too Long",
}

# OGF names
OGF = {
    0x01: "Link Control",
    0x02: "Link Policy",
    0x03: "Controller & Baseband",
    0x04: "Informational",
    0x05: "Status",
    0x06: "Testing",
    0x08: "LE Controller",
    0x3F: "Vendor Specific",
}

# Common HCI opcodes: (OCF | (OGF << 10))
OPCODES = {
    0x0401: "Inquiry",
    0x0402: "Inquiry Cancel",
    0x0405: "Create Connection",
    0x0406: "Disconnect",
    0x040D: "Remote Name Request",
    0x0411: "Authentication Requested",
    0x0413: "Set Connection Encryption",
    0x0419: "Remote Name Request Cancel",
    0x041B: "Read Remote Supported Features",
    0x041C: "Read Remote Extended Features",
    0x041D: "Read Remote Version Information",
    0x041F: "Read Clock Offset",
    0x0429: "Read LMP Handle",
    0x043F: "Enhanced Setup Synchronous Connection",
    0x0807: "Write Link Policy Settings",
    0x080F: "Write Default Link Policy Settings",
    0x0C01: "Set Event Mask",
    0x0C03: "Reset",
    0x0C05: "Set Event Filter",
    0x0C0D: "Read Stored Link Key",
    0x0C12: "Delete Stored Link Key",
    0x0C13: "Write Local Name",
    0x0C14: "Read Local Name",
    0x0C16: "Write Connection Accept Timeout",
    0x0C18: "Write Page Timeout",
    0x0C1A: "Write Scan Enable",
    0x0C1C: "Write Page Scan Activity",
    0x0C1E: "Write Inquiry Scan Activity",
    0x0C20: "Write Authentication Enable",
    0x0C24: "Write Class of Device",
    0x0C26: "Write Voice Setting",
    0x0C2A: "Write Automatic Flush Timeout",
    0x0C2F: "Write Number Of Supported IAC",
    0x0C33: "Host Buffer Size",
    0x0C3A: "Write Inquiry Scan Type",
    0x0C3C: "Write Inquiry Mode",
    0x0C3E: "Write Page Scan Type",
    0x0C45: "Write Extended Inquiry Response",
    0x0C52: "Write Simple Pairing Mode",
    0x0C56: "Write Inquiry Transmit Power Level",
    0x0C58: "Write Default Erroneous Data Reporting",
    0x0C5B: "Write LE Host Support",
    0x0C63: "Write Secure Connections Host Support",
    0x0C6D: "Write Authenticated Payload Timeout",
    0x0C7A: "Set Event Mask Page 2",
    0x1001: "Read Local Version Information",
    0x1002: "Read Local Supported Commands",
    0x1003: "Read Local Supported Features",
    0x1004: "Read Local Extended Features",
    0x1005: "Read Buffer Size",
    0x1009: "Read BD_ADDR",
    0x100B: "Read Local Supported Codecs",
    0x1405: "Read RSSI",
    0x1406: "Read AFH Channel Map",
    0x1407: "Read Clock",
    0x1408: "Read Encryption Key Size",
    0x1409: "Read Local AMP Info",
    0x2001: "LE Set Event Mask",
    0x2002: "LE Read Buffer Size [v1]",
    0x2003: "LE Read Local Supported Features",
    0x2005: "LE Set Random Address",
    0x2006: "LE Set Advertising Parameters",
    0x2007: "LE Read Advertising Channel Tx Power",
    0x2008: "LE Set Advertising Data",
    0x2009: "LE Set Scan Response Data",
    0x200A: "LE Set Advertising Enable",
    0x200B: "LE Set Scan Parameters",
    0x200C: "LE Set Scan Enable",
    0x200D: "LE Create Connection",
    0x200E: "LE Create Connection Cancel",
    0x200F: "LE Read Filter Accept List Size",
    0x2010: "LE Clear Filter Accept List",
    0x2011: "LE Add Device To Filter Accept List",
    0x2012: "LE Remove Device From Filter Accept List",
    0x2013: "LE Connection Update",
    0x2014: "LE Set Host Channel Classification",
    0x2015: "LE Read Channel Map",
    0x2016: "LE Read Remote Features",
    0x2017: "LE Encrypt",
    0x2018: "LE Rand",
    0x2019: "LE Enable Encryption",
    0x201A: "LE Long Term Key Request Reply",
    0x201B: "LE Long Term Key Request Negative Reply",
    0x201C: "LE Read Supported States",
    0x201D: "LE Receiver Test [v1]",
    0x201E: "LE Transmitter Test [v1]",
    0x201F: "LE Test End",
    0x2020: "LE Remote Connection Parameter Request Reply",
    0x2021: "LE Remote Connection Parameter Request Negative Reply",
    0x2022: "LE Set Data Length",
    0x2023: "LE Read Suggested Default Data Length",
    0x2024: "LE Write Suggested Default Data Length",
    0x2025: "LE Read Local P-256 Public Key",
    0x2026: "LE Generate DHKey [v1]",
    0x2027: "LE Add Device To Resolving List",
    0x2028: "LE Remove Device From Resolving List",
    0x2029: "LE Clear Resolving List",
    0x202A: "LE Read Resolving List Size",
    0x202B: "LE Read Peer Resolvable Address",
    0x202C: "LE Read Local Resolvable Address",
    0x202D: "LE Set Address Resolution Enable",
    0x202E: "LE Set Resolvable Private Address Timeout",
    0x202F: "LE Read Maximum Data Length",
    0x2030: "LE Read PHY",
    0x2031: "LE Set Default PHY",
    0x2032: "LE Set PHY",
    0x2033: "LE Receiver Test [v2]",
    0x2034: "LE Transmitter Test [v2]",
    0x2035: "LE Set Advertising Set Random Address",
    0x2036: "LE Set Extended Advertising Parameters [v1]",
    0x2037: "LE Set Extended Advertising Data",
    0x2038: "LE Set Extended Scan Response Data",
    0x2039: "LE Set Extended Advertising Enable",
    0x203A: "LE Read Maximum Advertising Data Length",
    0x203B: "LE Read Number of Supported Advertising Sets",
    0x203C: "LE Remove Advertising Set",
    0x203D: "LE Clear Advertising Sets",
    0x203E: "LE Set Periodic Advertising Parameters [v1]",
    0x203F: "LE Set Periodic Advertising Data",
    0x2040: "LE Set Periodic Advertising Enable",
    0x2041: "LE Set Extended Scan Parameters",
    0x2042: "LE Set Extended Scan Enable",
    0x2043: "LE Extended Create Connection [v1]",
    0x2044: "LE Periodic Advertising Create Sync",
    0x2045: "LE Periodic Advertising Create Sync Cancel",
    0x2046: "LE Periodic Advertising Terminate Sync",
    0x2047: "LE Add Device To Periodic Advertiser List",
    0x2048: "LE Remove Device From Periodic Advertiser List",
    0x2049: "LE Clear Periodic Advertiser List",
    0x204A: "LE Read Periodic Advertiser List Size",
    0x204B: "LE Read Transmit Power",
    0x204C: "LE Read RF Path Compensation",
    0x204D: "LE Write RF Path Compensation",
    0x204E: "LE Set Privacy Mode",
    0x204F: "LE Receiver Test [v3]",
    0x2050: "LE Transmitter Test [v3]",
    0x2051: "LE Set Connectionless CTE Transmit Parameters",
    0x2052: "LE Set Connectionless CTE Transmit Enable",
    0x2053: "LE Set Connection CTE Receive Parameters",
    0x2054: "LE Set Connection CTE Transmit Parameters",
    0x2055: "LE Connection CTE Request Enable",
    0x2056: "LE Connection CTE Response Enable",
    0x2057: "LE Read Antenna Information",
    0x2058: "LE Set Periodic Advertising Receive Enable",
    0x2059: "LE Periodic Advertising Sync Transfer",
    0x205A: "LE Periodic Advertising Set Info Transfer",
    0x205B: "LE Set Periodic Advertising Sync Transfer Parameters",
    0x205C: "LE Set Default Periodic Advertising Sync Transfer Parameters",
    0x205D: "LE Generate DHKey [v2]",
    0x205E: "LE Modify Sleep Clock Accuracy",
    0x205F: "LE Read Buffer Size [v2]",
    0x2060: "LE Read ISO TX Sync",
    0x2061: "LE Set CIG Parameters",
    0x2062: "LE Set CIG Parameters Test",
    0x2063: "LE Create CIS",
    0x2064: "LE Remove CIG",
    0x2065: "LE Accept CIS Request",
    0x2066: "LE Reject CIS Request",
    0x2067: "LE Create BIG",
    0x2068: "LE Create BIG Test",
    0x2069: "LE Terminate BIG",
    0x206A: "LE BIG Create Sync",
    0x206B: "LE BIG Terminate Sync",
    0x206C: "LE Request Peer SCA",
    0x206D: "LE Setup ISO Data Path",
    0x206E: "LE Remove ISO Data Path",
    0x206F: "LE ISO Transmit Test",
    0x2070: "LE ISO Receive Test",
    0x2071: "LE ISO Read Test Counters",
    0x2072: "LE ISO Test End",
    0x2073: "LE Set Host Feature",
    0x2074: "LE Read ISO Link Quality",
    0x2075: "LE Enhanced Read Transmit Power Level",
    0x2076: "LE Read Remote Transmit Power Level",
    0x2077: "LE Set Path Loss Reporting Parameters",
    0x2078: "LE Set Path Loss Reporting Enable",
    0x2079: "LE Set Transmit Power Reporting Enable",
    0x207A: "LE Transmitter Test [v4]",
    0x207B: "LE Set Data Related Address Changes",
    0x207C: "LE Set Default Subrate",
    0x207D: "LE Subrate Request",
    0x207E: "LE Set Extended Advertising Parameters [v2]",
}

EVENTS = {
    0x01: "Inquiry Complete",
    0x02: "Inquiry Result",
    0x03: "Connection Complete",
    0x04: "Connection Request",
    0x05: "Disconnection Complete",
    0x06: "Authentication Complete",
    0x07: "Remote Name Request Complete",
    0x08: "Encryption Change",
    0x0C: "Read Remote Supported Features Complete",
    0x0B: "Read Remote Version Information Complete",
    0x0E: "Command Complete",
    0x0F: "Command Status",
    0x10: "Hardware Error",
    0x12: "Role Change",
    0x13: "Number Of Completed Packets",
    0x14: "Mode Change",
    0x17: "Link Key Notification",
    0x1B: "Max Slots Change",
    0x1C: "Read Clock Offset Complete",
    0x1D: "Connection Packet Type Changed",
    0x20: "Page Scan Repetition Mode Change",
    0x23: "Extended Inquiry Result",
    0x2F: "Encryption Key Refresh Complete",
    0x30: "IO Capability Request",
    0x31: "IO Capability Response",
    0x32: "User Confirmation Request",
    0x33: "User Passkey Request",
    0x34: "Remote OOB Data Request",
    0x35: "Simple Pairing Complete",
    0x36: "Link Supervision Timeout Changed",
    0x3B: "Remote Host Supported Features Notification",
    0x3E: "LE Meta",
    0x48: "Authentication Payload Timeout Expired",
    0x59: "Number Of Completed Data Blocks",
    0xFF: "Vendor Specific",
}

LE_META_SUBEVENTS = {
    0x01: "LE Connection Complete",
    0x02: "LE Advertising Report",
    0x03: "LE Connection Update Complete",
    0x04: "LE Read Remote Features Complete",
    0x05: "LE Long Term Key Request",
    0x06: "LE Remote Connection Parameter Request",
    0x07: "LE Data Length Change",
    0x08: "LE Read Local P-256 Public Key Complete",
    0x09: "LE Generate DHKey Complete",
    0x0A: "LE Enhanced Connection Complete [v1]",
    0x0B: "LE Directed Advertising Report",
    0x0C: "LE PHY Update Complete",
    0x0D: "LE Extended Advertising Report",
    0x0E: "LE Periodic Advertising Sync Established [v1]",
    0x0F: "LE Periodic Advertising Report [v1]",
    0x10: "LE Periodic Advertising Sync Lost",
    0x11: "LE Scan Timeout",
    0x12: "LE Advertising Set Terminated",
    0x13: "LE Scan Request Received",
    0x14: "LE Channel Selection Algorithm",
    0x15: "LE Connectionless IQ Report",
    0x16: "LE Connection IQ Report",
    0x17: "LE CTE Request Failed",
    0x18: "LE Periodic Advertising Sync Transfer Received [v1]",
    0x19: "LE CIS Established [v1]",
    0x1A: "LE CIS Request",
    0x1B: "LE Create BIG Complete",
    0x1C: "LE Terminate BIG Complete",
    0x1D: "LE BIG Sync Established",
    0x1E: "LE BIG Sync Lost",
    0x1F: "LE Request Peer SCA Complete",
    0x20: "LE Path Loss Threshold",
    0x21: "LE Transmit Power Reporting",
    0x22: "LE BIGInfo Advertising Report",
    0x23: "LE Subrate Change",
    0x24: "LE Periodic Advertising Sync Established [v2]",
    0x25: "LE Periodic Advertising Report [v2]",
    0x26: "LE Periodic Advertising Sync Transfer Received [v2]",
    0x29: "LE Enhanced Connection Complete [v2]",
    0x2A: "LE CIS Established [v2]",
    0x2B: "LE Read All Remote Features Complete",
    0x2F: "LE CS Subevent Result",
    0x30: "LE CS Subevent Result Continue",
}

ADDR_TYPES = {
    0x00: "Public",
    0x01: "Random",
    0x02: "Public Identity",
    0x03: "Random Identity",
}

ADV_EVENT_TYPES = {
    0x00: "Connectable undirected",
    0x01: "Connectable directed",
    0x02: "Scannable undirected",
    0x03: "Non-connectable undirected",
    0x04: "Scan response",
}

# GATT / ATT opcodes (on L2CAP CID 0x0004)
ATT_OPS = {
    0x01: "Error Response",
    0x02: "Exchange MTU Request",
    0x03: "Exchange MTU Response",
    0x04: "Find Information Request",
    0x05: "Find Information Response",
    0x06: "Find By Type Value Request",
    0x07: "Find By Type Value Response",
    0x08: "Read By Type Request",
    0x09: "Read By Type Response",
    0x0A: "Read Request",
    0x0B: "Read Response",
    0x0C: "Read Blob Request",
    0x0D: "Read Blob Response",
    0x0E: "Read Multiple Request",
    0x0F: "Read Multiple Response",
    0x10: "Read By Group Type Request",
    0x11: "Read By Group Type Response",
    0x12: "Write Request",
    0x13: "Write Response",
    0x16: "Prepare Write Request",
    0x17: "Prepare Write Response",
    0x18: "Execute Write Request",
    0x19: "Execute Write Response",
    0x1B: "Handle Value Notification",
    0x1D: "Handle Value Indication",
    0x1E: "Handle Value Confirmation",
    0x52: "Write Command",
    0xD2: "Signed Write Command",
}

L2CAP_CIDS = {
    0x0001: "Signaling",
    0x0004: "ATT",
    0x0005: "LE Signaling",
    0x0006: "SMP",
    0x0007: "BR/EDR Security Manager",
}


def status_name(code: int) -> str:
    return STATUS.get(code, f"Unknown(0x{code:02X})")


def bd_addr(data: bytes) -> str:
    if len(data) < 6:
        return data.hex()
    return ":".join(f"{b:02X}" for b in reversed(data[:6]))


def ts_to_str(ts_us: int) -> str:
    """Convert BTSnoop absolute timestamp to local-looking ISO string + raw."""
    try:
        unix_us = ts_us - BTSNOOP_EPOCH_DELTA_US
        sec = unix_us // 1_000_000
        usec = abs(unix_us % 1_000_000)
        dt = datetime.fromtimestamp(sec, tz=timezone.utc)
        return dt.strftime("%Y-%m-%d %H:%M:%S.") + f"{usec:06d} UTC"
    except (OSError, OverflowError, ValueError):
        return f"raw_ts={ts_us}"


def hexdump(data: bytes, max_len: int = 64) -> str:
    if not data:
        return ""
    shown = data[:max_len]
    hx = " ".join(f"{b:02X}" for b in shown)
    if len(data) > max_len:
        hx += f" ... (+{len(data) - max_len} bytes)"
    return hx


def opcode_name(opcode: int) -> str:
    if opcode in OPCODES:
        return OPCODES[opcode]
    ogf = (opcode >> 10) & 0x3F
    ocf = opcode & 0x03FF
    ogf_name = OGF.get(ogf, f"OGF=0x{ogf:02X}")
    if ogf == 0x3F:
        return f"Vendor Specific (OCF=0x{ocf:03X})"
    return f"Unknown ({ogf_name}, OCF=0x{ocf:03X})"


def decode_cmd_params(opcode: int, params: bytes) -> str:
    parts: list[str] = []
    # Disconnect
    if opcode == 0x0406 and len(params) >= 3:
        handle = params[0] | (params[1] << 8)
        reason = params[2]
        parts.append(f"handle=0x{handle:04X} reason={status_name(reason)}")
    # LE Set Scan Enable
    elif opcode == 0x200C and len(params) >= 2:
        parts.append(f"enable={params[0]} filter_duplicates={params[1]}")
    # LE Set Extended Scan Enable
    elif opcode == 0x2042 and len(params) >= 2:
        parts.append(
            f"enable={params[0]} filter_duplicates={params[1]}"
            + (f" duration={params[2] | (params[3] << 8)}" if len(params) >= 4 else "")
        )
    # LE Set Advertising Enable
    elif opcode == 0x200A and len(params) >= 1:
        parts.append(f"enable={params[0]}")
    # LE Create Connection
    elif opcode == 0x200D and len(params) >= 25:
        peer_type = params[6]
        peer = bd_addr(params[7:13])
        parts.append(f"peer={peer} ({ADDR_TYPES.get(peer_type, peer_type)})")
    # LE Extended Create Connection
    elif opcode == 0x2043 and len(params) >= 10:
        # simplified: initiating filter policy, own addr type, peer addr type, peer addr
        peer_type = params[2]
        peer = bd_addr(params[3:9])
        parts.append(f"peer={peer} ({ADDR_TYPES.get(peer_type, peer_type)})")
    # LE Add Device To Filter Accept List
    elif opcode in (0x2011, 0x2012) and len(params) >= 7:
        parts.append(f"addr={bd_addr(params[1:7])} type={ADDR_TYPES.get(params[0], params[0])}")
    # Write Scan Enable
    elif opcode == 0x0C1A and len(params) >= 1:
        parts.append(f"scan_enable=0x{params[0]:02X}")
    # Set Event Mask
    elif opcode == 0x0C01 and len(params) >= 8:
        mask = int.from_bytes(params[:8], "little")
        parts.append(f"mask=0x{mask:016X}")
    # LE Set Event Mask
    elif opcode == 0x2001 and len(params) >= 8:
        mask = int.from_bytes(params[:8], "little")
        parts.append(f"mask=0x{mask:016X}")
    # Read RSSI
    elif opcode == 0x1405 and len(params) >= 2:
        handle = params[0] | (params[1] << 8)
        parts.append(f"handle=0x{handle:04X}")
    # LE Connection Update
    elif opcode == 0x2013 and len(params) >= 14:
        handle = params[0] | (params[1] << 8)
        parts.append(f"handle=0x{handle:04X}")
    # LE Set Data Length
    elif opcode == 0x2022 and len(params) >= 6:
        handle = params[0] | (params[1] << 8)
        tx_octets = params[2] | (params[3] << 8)
        tx_time = params[4] | (params[5] << 8)
        parts.append(f"handle=0x{handle:04X} tx_octets={tx_octets} tx_time={tx_time}")
    # LE Set PHY
    elif opcode == 0x2032 and len(params) >= 7:
        handle = params[0] | (params[1] << 8)
        parts.append(
            f"handle=0x{handle:04X} all_phys=0x{params[2]:02X} "
            f"tx=0x{params[3]:02X} rx=0x{params[4]:02X}"
        )
    # Host Buffer Size
    elif opcode == 0x0C33 and len(params) >= 7:
        acl_len = params[0] | (params[1] << 8)
        sco_len = params[2]
        acl_pkts = params[3] | (params[4] << 8)
        sco_pkts = params[5] | (params[6] << 8)
        parts.append(f"acl_len={acl_len} sco_len={sco_len} acl_pkts={acl_pkts} sco_pkts={sco_pkts}")
    # Write LE Host Support
    elif opcode == 0x0C5B and len(params) >= 2:
        parts.append(f"le_supported_host={params[0]} simultaneous={params[1]}")

    if not parts and params:
        parts.append(f"params[{len(params)}]={hexdump(params, 48)}")
    elif not params:
        parts.append("(no parameters)")
    return " ".join(parts)


def decode_le_meta(payload: bytes) -> str:
    if not payload:
        return "LE Meta (empty)"
    sub = payload[0]
    name = LE_META_SUBEVENTS.get(sub, f"Unknown Subevent 0x{sub:02X}")
    p = payload[1:]
    extra = []

    if sub == 0x01 and len(p) >= 18:  # Connection Complete
        status = p[0]
        handle = p[1] | (p[2] << 8)
        role = "Central" if p[3] == 0 else "Peripheral"
        peer_type = p[4]
        peer = bd_addr(p[5:11])
        interval = p[11] | (p[12] << 8)
        latency = p[13] | (p[14] << 8)
        timeout = p[15] | (p[16] << 8)
        extra.append(
            f"status={status_name(status)} handle=0x{handle:04X} role={role} "
            f"peer={peer} ({ADDR_TYPES.get(peer_type, peer_type)}) "
            f"interval={interval * 1.25:g}ms latency={latency} timeout={timeout * 10}ms"
        )
    elif sub in (0x0A, 0x29) and len(p) >= 30:  # Enhanced Connection Complete
        status = p[0]
        handle = p[1] | (p[2] << 8)
        role = "Central" if p[3] == 0 else "Peripheral"
        peer_type = p[4]
        peer = bd_addr(p[5:11])
        local_rpa = bd_addr(p[11:17])
        peer_rpa = bd_addr(p[17:23])
        interval = p[23] | (p[24] << 8)
        latency = p[25] | (p[26] << 8)
        timeout = p[27] | (p[28] << 8)
        extra.append(
            f"status={status_name(status)} handle=0x{handle:04X} role={role} "
            f"peer={peer} ({ADDR_TYPES.get(peer_type, peer_type)}) "
            f"local_rpa={local_rpa} peer_rpa={peer_rpa} "
            f"interval={interval * 1.25:g}ms latency={latency} timeout={timeout * 10}ms"
        )
    elif sub == 0x02 and len(p) >= 1:  # Advertising Report
        num = p[0]
        extra.append(f"num_reports={num}")
        off = 1
        for i in range(num):
            if off + 8 > len(p):
                break
            evt_type = p[off]
            addr_type = p[off + 1]
            addr = bd_addr(p[off + 2 : off + 8])
            off += 8
            if off >= len(p):
                break
            dlen = p[off]
            off += 1
            adv = p[off : off + dlen]
            off += dlen
            rssi = None
            if off < len(p):
                rssi = struct.unpack("b", bytes([p[off]]))[0]
                off += 1
            extra.append(
                f"  [{i}] {ADV_EVENT_TYPES.get(evt_type, f'type=0x{evt_type:02X}')} "
                f"{addr} ({ADDR_TYPES.get(addr_type, addr_type)}) "
                f"rssi={rssi}dBm data={hexdump(adv, 32)}"
            )
    elif sub == 0x0D and len(p) >= 1:  # Extended Advertising Report
        num = p[0]
        extra.append(f"num_reports={num}")
        off = 1
        for i in range(min(num, 3)):  # cap detail for huge dumps
            if off + 24 > len(p):
                break
            evt_type = p[off] | (p[off + 1] << 8)
            addr_type = p[off + 2]
            addr = bd_addr(p[off + 3 : off + 9])
            prim_phy = p[off + 9]
            sec_phy = p[off + 10]
            sid = p[off + 11]
            tx_power = struct.unpack("b", bytes([p[off + 12]]))[0]
            rssi = struct.unpack("b", bytes([p[off + 13]]))[0]
            # periodic interval at 14-15, direct addr type 16, direct addr 17-22, data_len 23
            dlen = p[off + 23]
            data = p[off + 24 : off + 24 + dlen]
            extra.append(
                f"  [{i}] evt=0x{evt_type:04X} {addr} ({ADDR_TYPES.get(addr_type, addr_type)}) "
                f"phy={prim_phy}/{sec_phy} sid={sid} tx={tx_power}dBm rssi={rssi}dBm "
                f"data={hexdump(data, 32)}"
            )
            off += 24 + dlen
    elif sub == 0x03 and len(p) >= 9:  # Connection Update Complete
        status = p[0]
        handle = p[1] | (p[2] << 8)
        interval = p[3] | (p[4] << 8)
        latency = p[5] | (p[6] << 8)
        timeout = p[7] | (p[8] << 8)
        extra.append(
            f"status={status_name(status)} handle=0x{handle:04X} "
            f"interval={interval * 1.25:g}ms latency={latency} timeout={timeout * 10}ms"
        )
    elif sub == 0x04 and len(p) >= 11:  # Read Remote Features Complete
        status = p[0]
        handle = p[1] | (p[2] << 8)
        feats = p[3:11].hex()
        extra.append(f"status={status_name(status)} handle=0x{handle:04X} features={feats}")
    elif sub == 0x05 and len(p) >= 12:  # LTK Request
        handle = p[0] | (p[1] << 8)
        extra.append(f"handle=0x{handle:04X} rand={p[2:10].hex()} ediv={p[10] | (p[11] << 8):04X}")
    elif sub == 0x07 and len(p) >= 8:  # Data Length Change
        handle = p[0] | (p[1] << 8)
        max_tx = p[2] | (p[3] << 8)
        max_rx = p[6] | (p[7] << 8)
        extra.append(f"handle=0x{handle:04X} max_tx_octets={max_tx} max_rx_octets={max_rx}")
    elif sub == 0x0C and len(p) >= 5:  # PHY Update Complete
        status = p[0]
        handle = p[1] | (p[2] << 8)
        extra.append(f"status={status_name(status)} handle=0x{handle:04X} tx_phy={p[3]} rx_phy={p[4]}")
    elif sub == 0x14 and len(p) >= 3:  # Channel Selection Algorithm
        handle = p[0] | (p[1] << 8)
        extra.append(f"handle=0x{handle:04X} algorithm={p[2]}")
    else:
        if p:
            extra.append(f"raw={hexdump(p, 48)}")

    body = " | ".join(extra) if extra else ""
    return f"LE Meta: {name}" + (f" - {body}" if body else "")


def decode_event(event_code: int, params: bytes) -> str:
    name = EVENTS.get(event_code, f"Unknown Event 0x{event_code:02X}")

    if event_code == 0x0E:  # Command Complete
        if len(params) >= 3:
            ncmd = params[0]
            opcode = params[1] | (params[2] << 8)
            rest = params[3:]
            status = status_name(rest[0]) if rest else ""
            detail = ""
            # Read BD_ADDR
            if opcode == 0x1009 and len(rest) >= 7:
                detail = f" bd_addr={bd_addr(rest[1:7])}"
            # Read Local Version
            elif opcode == 0x1001 and len(rest) >= 9:
                detail = (
                    f" hci_ver=0x{rest[1]:02X} hci_rev=0x{rest[2] | (rest[3] << 8):04X} "
                    f"lmp_ver=0x{rest[4]:02X} mfr=0x{rest[5] | (rest[6] << 8):04X} "
                    f"lmp_sub=0x{rest[7] | (rest[8] << 8):04X}"
                )
            # Read Buffer Size
            elif opcode == 0x1005 and len(rest) >= 8:
                detail = (
                    f" acl_len={rest[1] | (rest[2] << 8)} sco_len={rest[3]} "
                    f"acl_pkts={rest[4] | (rest[5] << 8)} sco_pkts={rest[6] | (rest[7] << 8)}"
                )
            # LE Read Buffer Size
            elif opcode == 0x2002 and len(rest) >= 4:
                detail = f" le_acl_len={rest[1] | (rest[2] << 8)} le_acl_pkts={rest[3]}"
            # Read RSSI
            elif opcode == 0x1405 and len(rest) >= 4:
                handle = rest[1] | (rest[2] << 8)
                rssi = struct.unpack("b", bytes([rest[3]]))[0]
                detail = f" handle=0x{handle:04X} rssi={rssi}dBm"
            # Encryption Key Size
            elif opcode == 0x1408 and len(rest) >= 4:
                handle = rest[1] | (rest[2] << 8)
                detail = f" handle=0x{handle:04X} key_size={rest[3]}"
            elif rest and len(rest) > 1:
                detail = f" return={hexdump(rest[1:], 32)}"
            return (
                f"Command Complete: {opcode_name(opcode)} (0x{opcode:04X}) "
                f"ncmd={ncmd} status={status}{detail}"
            )
        return f"{name} raw={hexdump(params)}"

    if event_code == 0x0F:  # Command Status
        if len(params) >= 4:
            status = status_name(params[0])
            ncmd = params[1]
            opcode = params[2] | (params[3] << 8)
            return (
                f"Command Status: {opcode_name(opcode)} (0x{opcode:04X}) "
                f"status={status} ncmd={ncmd}"
            )
        return f"{name} raw={hexdump(params)}"

    if event_code == 0x05 and len(params) >= 4:  # Disconnection Complete
        status = status_name(params[0])
        handle = params[1] | (params[2] << 8)
        reason = status_name(params[3])
        return f"Disconnection Complete: status={status} handle=0x{handle:04X} reason={reason}"

    if event_code == 0x13 and len(params) >= 1:  # Number Of Completed Packets
        num = params[0]
        bits = []
        off = 1
        for i in range(num):
            if off + 4 > len(params):
                break
            handle = params[off] | (params[off + 1] << 8)
            count = params[off + 2] | (params[off + 3] << 8)
            bits.append(f"handle=0x{handle:04X} completed={count}")
            off += 4
        return "Number Of Completed Packets: " + ("; ".join(bits) if bits else f"num={num}")

    if event_code == 0x3E:
        return decode_le_meta(params)

    if event_code == 0x08 and len(params) >= 4:  # Encryption Change
        status = status_name(params[0])
        handle = params[1] | (params[2] << 8)
        enc = {0: "OFF", 1: "ON (E0/AES-CCM)", 2: "ON (AES-CCM)"}.get(params[3], str(params[3]))
        return f"Encryption Change: status={status} handle=0x{handle:04X} encryption={enc}"

    if event_code == 0x2F and len(params) >= 3:  # Encryption Key Refresh Complete
        status = status_name(params[0])
        handle = params[1] | (params[2] << 8)
        return f"Encryption Key Refresh Complete: status={status} handle=0x{handle:04X}"

    if event_code == 0x03 and len(params) >= 11:  # Connection Complete (BR/EDR)
        status = status_name(params[0])
        handle = params[1] | (params[2] << 8)
        addr = bd_addr(params[3:9])
        return f"Connection Complete: status={status} handle=0x{handle:04X} bd_addr={addr}"

    if event_code == 0xFF:
        return f"Vendor Specific Event: {hexdump(params, 64)}"

    return f"{name}: {hexdump(params, 64)}"


def decode_att(att: bytes, direction: str) -> str:
    if not att:
        return "ATT empty"
    op = att[0]
    name = ATT_OPS.get(op, f"ATT Opcode 0x{op:02X}")
    p = att[1:]
    if op == 0x01 and len(p) >= 4:  # Error Response
        req = p[0]
        handle = p[1] | (p[2] << 8)
        err = p[3]
        return f"ATT Error Response: req=0x{req:02X} handle=0x{handle:04X} err=0x{err:02X}"
    if op in (0x02, 0x03) and len(p) >= 2:  # MTU
        mtu = p[0] | (p[1] << 8)
        return f"ATT {name}: mtu={mtu}"
    if op in (0x0A, 0x0C) and len(p) >= 2:  # Read / Read Blob
        handle = p[0] | (p[1] << 8)
        extra = ""
        if op == 0x0C and len(p) >= 4:
            extra = f" offset={p[2] | (p[3] << 8)}"
        return f"ATT {name}: handle=0x{handle:04X}{extra}"
    if op in (0x0B, 0x0D):  # Read Response / Blob Response
        return f"ATT {name}: value={hexdump(p, 48)}"
    if op in (0x12, 0x52) and len(p) >= 2:  # Write Request / Command
        handle = p[0] | (p[1] << 8)
        return f"ATT {name}: handle=0x{handle:04X} value={hexdump(p[2:], 40)}"
    if op == 0x13:
        return f"ATT {name}"
    if op in (0x1B, 0x1D) and len(p) >= 2:  # Notification / Indication
        handle = p[0] | (p[1] << 8)
        return f"ATT {name}: handle=0x{handle:04X} value={hexdump(p[2:], 48)}"
    if op in (0x08, 0x10) and len(p) >= 6:  # Read By Type / Group
        start = p[0] | (p[1] << 8)
        end = p[2] | (p[3] << 8)
        uuid = p[4:].hex()
        return f"ATT {name}: start=0x{start:04X} end=0x{end:04X} uuid={uuid}"
    if op in (0x09, 0x11):
        return f"ATT {name}: {hexdump(p, 48)}"
    if op == 0x04 and len(p) >= 4:
        start = p[0] | (p[1] << 8)
        end = p[2] | (p[3] << 8)
        return f"ATT {name}: start=0x{start:04X} end=0x{end:04X}"
    return f"ATT {name}: {hexdump(p, 40)}"


def decode_acl(data: bytes) -> str:
    if len(data) < 4:
        return f"ACL short: {hexdump(data)}"
    handle_flags = data[0] | (data[1] << 8)
    handle = handle_flags & 0x0FFF
    pb = (handle_flags >> 12) & 0x3
    bc = (handle_flags >> 14) & 0x3
    length = data[2] | (data[3] << 8)
    payload = data[4 : 4 + length]
    pb_names = {
        0: "start_nonflush",
        1: "cont",
        2: "start_flush",
        3: "complete",
    }
    header = f"ACL handle=0x{handle:03X} pb={pb_names.get(pb, pb)} bc={bc} len={length}"

    # Only parse L2CAP on start packets with enough data
    if pb in (0, 2) and len(payload) >= 4:
        l2_len = payload[0] | (payload[1] << 8)
        cid = payload[2] | (payload[3] << 8)
        l2_payload = payload[4 : 4 + l2_len]
        cid_name = L2CAP_CIDS.get(cid, f"CID 0x{cid:04X}")
        if cid == 0x0004:  # ATT
            return f"{header} L2CAP[{cid_name}] {decode_att(l2_payload, '')}"
        if cid in (0x0001, 0x0005) and l2_payload:
            code = l2_payload[0]
            return f"{header} L2CAP[{cid_name}] code=0x{code:02X} {hexdump(l2_payload, 32)}"
        if cid == 0x0006 and l2_payload:
            smp_codes = {
                0x01: "Pairing Request",
                0x02: "Pairing Response",
                0x03: "Pairing Confirm",
                0x04: "Pairing Random",
                0x05: "Pairing Failed",
                0x06: "Encryption Information",
                0x07: "Central Identification",
                0x08: "Identity Information",
                0x09: "Identity Address Information",
                0x0A: "Signing Information",
                0x0B: "Security Request",
                0x0C: "Pairing Public Key",
                0x0D: "Pairing DHKey Check",
                0x0E: "Pairing Keypress Notification",
            }
            code = l2_payload[0]
            return (
                f"{header} L2CAP[SMP] {smp_codes.get(code, f'code=0x{code:02X}')} "
                f"{hexdump(l2_payload[1:], 32)}"
            )
        return f"{header} L2CAP[{cid_name}] {hexdump(l2_payload, 40)}"
    if pb == 1:
        return f"{header} cont_data={hexdump(payload, 40)}"
    return f"{header} data={hexdump(payload, 40)}"


def decode_packet(data: bytes, flags: int) -> str:
    if not data:
        return "(empty packet)"

    # flags: bit0 direction (0=host->controller/sent, 1=controller->host/recv)
    #        bit1 command/event vs ACL (legacy); H4 type byte is authoritative
    direction = "RX" if (flags & 0x01) else "TX"
    pkt_type = data[0]
    body = data[1:]
    type_name = H4_TYPES.get(pkt_type, f"TYPE_0x{pkt_type:02X}")

    if pkt_type == 0x01:  # Command
        if len(body) < 3:
            return f"{direction} {type_name} truncated {hexdump(data)}"
        opcode = body[0] | (body[1] << 8)
        plen = body[2]
        params = body[3 : 3 + plen]
        return (
            f"{direction} {type_name} {opcode_name(opcode)} (0x{opcode:04X}) "
            f"{decode_cmd_params(opcode, params)}"
        )

    if pkt_type == 0x04:  # Event
        if len(body) < 2:
            return f"{direction} {type_name} truncated {hexdump(data)}"
        event_code = body[0]
        plen = body[1]
        params = body[2 : 2 + plen]
        return f"{direction} {type_name} {decode_event(event_code, params)}"

    if pkt_type == 0x02:  # ACL
        return f"{direction} {type_name} {decode_acl(body)}"

    if pkt_type == 0x03:
        return f"{direction} {type_name} {hexdump(body, 48)}"

    if pkt_type == 0x05:
        return f"{direction} {type_name} {hexdump(body, 48)}"

    return f"{direction} {type_name} {hexdump(data, 64)}"


def parse_btsnoop(path: Path):
    with path.open("rb") as f:
        header = f.read(16)
        if len(header) < 16 or header[:8] != b"btsnoop\x00":
            raise ValueError(f"Not a BTSnoop file (magic={header[:8]!r})")
        version, datalink = struct.unpack(">II", header[8:16])
        packets = []
        while True:
            rec = f.read(24)
            if len(rec) < 24:
                break
            orig_len, incl_len, flags, drops, ts = struct.unpack(">IIIIq", rec)
            data = f.read(incl_len)
            if len(data) < incl_len:
                break
            packets.append(
                {
                    "orig_len": orig_len,
                    "incl_len": incl_len,
                    "flags": flags,
                    "drops": drops,
                    "ts": ts,
                    "data": data,
                }
            )
    return version, datalink, packets


def main() -> int:
    ap = argparse.ArgumentParser(description="Convert BTSnoop HCI capture to readable text")
    ap.add_argument("input", type=Path, help="Input .cfa / .btsnoop / binary HCI log")
    ap.add_argument(
        "-o",
        "--output",
        type=Path,
        default=None,
        help="Output text file (default: <input>_readable.log)",
    )
    ap.add_argument(
        "--max-hex",
        type=int,
        default=64,
        help="Max payload bytes to show as hex (default 64)",
    )
    ap.add_argument(
        "--skip-num-completed",
        action="store_true",
        help="Omit noisy Number Of Completed Packets events",
    )
    args = ap.parse_args()

    if args.output is None:
        args.output = args.input.with_name(args.input.stem + "_readable.log")

    version, datalink, packets = parse_btsnoop(args.input)
    dlt_names = {
        1001: "H1",
        1002: "HCI UART (H4)",
        1003: "HCI BCSP",
        1004: "HCI Serial (H5)",
        2001: "Unencapsulated HCI",
    }

    counts: dict[str, int] = {}
    lines: list[str] = []
    lines.append("=" * 88)
    lines.append("BTSnoop HCI capture - human-readable decode")
    lines.append(f"Source     : {args.input.name}")
    lines.append(f"Version    : {version}")
    lines.append(f"Datalink   : {datalink} ({dlt_names.get(datalink, 'unknown')})")
    lines.append(f"Packets    : {len(packets)}")
    if packets:
        lines.append(f"First time : {ts_to_str(packets[0]['ts'])}")
        lines.append(f"Last time  : {ts_to_str(packets[-1]['ts'])}")
    lines.append("=" * 88)
    lines.append("")

    t0 = packets[0]["ts"] if packets else 0
    written = 0
    for i, pkt in enumerate(packets, 1):
        data = pkt["data"]
        # Optional filter for noisy events
        if args.skip_num_completed and data[:2] == b"\x04\x13":
            counts["skipped_num_completed"] = counts.get("skipped_num_completed", 0) + 1
            continue

        rel_ms = (pkt["ts"] - t0) / 1000.0
        summary = decode_packet(data, pkt["flags"])
        kind = summary.split()[1] if len(summary.split()) > 1 else "?"
        counts[kind] = counts.get(kind, 0) + 1

        lines.append(
            f"[{i:05d}] +{rel_ms:12.3f}ms  {ts_to_str(pkt['ts'])}  "
            f"flags=0x{pkt['flags']:X} len={pkt['incl_len']}"
        )
        lines.append(f"        {summary}")
        # Always include raw hex for auditability (truncated)
        lines.append(f"        RAW: {hexdump(data, args.max_hex)}")
        lines.append("")
        written += 1

    lines.append("=" * 88)
    lines.append("Summary")
    lines.append("-" * 88)
    for k, v in sorted(counts.items(), key=lambda x: (-x[1], x[0])):
        lines.append(f"  {k:24s} {v:6d}")
    lines.append(f"  {'written':24s} {written:6d}")
    lines.append("=" * 88)

    text = "\n".join(lines) + "\n"
    args.output.write_text(text, encoding="utf-8")
    print(f"Wrote {written} packets -> {args.output}")
    print(f"Size: {args.output.stat().st_size:,} bytes")
    return 0


if __name__ == "__main__":
    sys.exit(main())

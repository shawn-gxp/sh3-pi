"""Decompress Xamarin XALZ-packed assemblies from APK assemblies/ folder."""
import struct
from pathlib import Path

import lz4.block

SRC = Path(__file__).resolve().parent / "extracted" / "assemblies"
DST = Path(__file__).resolve().parent / "extracted" / "assemblies_decompressed"


def decompress_file(data: bytes, name: str) -> bytes:
    if data[:4] == b"XALZ":
        index = struct.unpack_from("<I", data, 4)[0]
        unc_size = struct.unpack_from("<I", data, 8)[0]
        payload = data[12:]
        try:
            out = lz4.block.decompress(payload, uncompressed_size=unc_size)
        except Exception:
            out = lz4.block.decompress(payload)
        return out, index, unc_size
    if data[:4] == b"XABA":
        # Uncompressed assembly blob: magic + index + payload
        if data[8:10] == b"MZ":
            return data[8:], struct.unpack_from("<I", data, 4)[0], len(data) - 8
        if data[12:14] == b"MZ":
            return data[12:], struct.unpack_from("<I", data, 4)[0], len(data) - 12
        return data[8:], 0, len(data) - 8
    return data, None, len(data)


def main() -> None:
    DST.mkdir(parents=True, exist_ok=True)
    ok = fail = 0
    interesting = {
        "NHL.dll",
        "NHL.Android.dll",
        "BLELib.dll",
        "BLELib.Android.dll",
        "Plugin.BLE.dll",
        "Plugin.BLE.Abstractions.dll",
    }
    for p in sorted(SRC.iterdir()):
        if not p.is_file():
            continue
        data = p.read_bytes()
        try:
            out, index, unc_size = decompress_file(data, p.name)
        except Exception as e:
            print(f"FAIL {p.name}: {e}")
            fail += 1
            continue
        (DST / p.name).write_bytes(out)
        ok += 1
        if p.name in interesting or (out[:2] != b"MZ" and p.suffix.lower() == ".dll"):
            print(
                f"OK {p.name}: index={index} out={len(out)} hdr_unc={unc_size} PE={out[:2] == b'MZ'}"
            )
    print(f"\nDone: ok={ok} fail={fail}\nOutput: {DST}")


if __name__ == "__main__":
    main()

using System;
using Android.Bluetooth.LE;
using Android.OS;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;

namespace Plugin.BLE.Extensions;

internal static class ScanModeExtension
{
	public static global::Android.Bluetooth.LE.ScanMode ToNative(this Plugin.BLE.Abstractions.Contracts.ScanMode scanMode)
	{
		if (Build.VERSION.SdkInt < BuildVersionCodes.Lollipop)
		{
			throw new InvalidOperationException("Scan modes are not implemented in API lvl < 21.");
		}
		switch (scanMode)
		{
		case Plugin.BLE.Abstractions.Contracts.ScanMode.Passive:
			if (Build.VERSION.SdkInt < BuildVersionCodes.M)
			{
				Trace.Message("Scanmode Passive is not supported on API lvl < 23. Falling back to LowPower.");
				return global::Android.Bluetooth.LE.ScanMode.LowPower;
			}
			return global::Android.Bluetooth.LE.ScanMode.Opportunistic;
		case Plugin.BLE.Abstractions.Contracts.ScanMode.LowPower:
			return global::Android.Bluetooth.LE.ScanMode.LowPower;
		case Plugin.BLE.Abstractions.Contracts.ScanMode.Balanced:
			return global::Android.Bluetooth.LE.ScanMode.Balanced;
		case Plugin.BLE.Abstractions.Contracts.ScanMode.LowLatency:
			return global::Android.Bluetooth.LE.ScanMode.LowLatency;
		default:
			throw new ArgumentOutOfRangeException("scanMode", scanMode, null);
		}
	}
}

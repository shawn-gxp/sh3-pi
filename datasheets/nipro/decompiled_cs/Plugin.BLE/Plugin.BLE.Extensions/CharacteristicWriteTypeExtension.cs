using System;
using Android.Bluetooth;
using Plugin.BLE.Abstractions;

namespace Plugin.BLE.Extensions;

internal static class CharacteristicWriteTypeExtension
{
	public static GattWriteType ToNative(this CharacteristicWriteType writeType)
	{
		return writeType switch
		{
			CharacteristicWriteType.WithResponse => GattWriteType.Default, 
			CharacteristicWriteType.WithoutResponse => GattWriteType.NoResponse, 
			_ => throw new NotImplementedException(), 
		};
	}
}

using System;

namespace Plugin.BLE.Abstractions;

[Flags]
public enum CharacteristicPropertyType
{
	Broadcast = 1,
	Read = 2,
	WriteWithoutResponse = 4,
	Write = 8,
	Notify = 0x10,
	Indicate = 0x20,
	AuthenticatedSignedWrites = 0x40,
	ExtendedProperties = 0x80
}

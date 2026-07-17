using System;

namespace Plugin.BLE.Abstractions.Exceptions;

public class DeviceConnectionException : Exception
{
	public Guid DeviceId { get; }

	public string DeviceName { get; }

	public DeviceConnectionException(Guid deviceId, string deviceName, string message)
		: base(message)
	{
		DeviceId = deviceId;
		DeviceName = deviceName;
	}
}

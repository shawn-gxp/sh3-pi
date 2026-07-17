namespace Plugin.BLE.Abstractions;

public struct ConnectParameters
{
	public bool AutoConnect { get; }

	public bool ForceBleTransport { get; }

	public static ConnectParameters None { get; }

	public ConnectParameters(bool autoConnect = false, bool forceBleTransport = false)
	{
		AutoConnect = autoConnect;
		ForceBleTransport = forceBleTransport;
	}
}

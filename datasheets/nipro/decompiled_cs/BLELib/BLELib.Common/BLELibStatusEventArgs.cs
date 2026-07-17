using System;

namespace BLELib.Common;

public class BLELibStatusEventArgs : EventArgs
{
	public string DeviceName { get; set; }

	public BLELibStatus Status { get; set; }

	public string Message { get; set; }

	public BLELibStatusEventArgs(string deviceName, BLELibStatus status, string message = "")
	{
		DeviceName = deviceName;
		Status = status;
		Message = message;
	}
}

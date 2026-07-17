using System;
using Plugin.BLE.Abstractions.Contracts;

namespace Plugin.BLE.Abstractions.EventArgs;

public class BluetoothStateChangedArgs : System.EventArgs
{
	public BluetoothState OldState { get; }

	public BluetoothState NewState { get; }

	public BluetoothStateChangedArgs(BluetoothState oldState, BluetoothState newState)
	{
		OldState = oldState;
		NewState = newState;
	}
}

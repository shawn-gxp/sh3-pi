using System;
using Plugin.BLE.Abstractions.EventArgs;

namespace Plugin.BLE.Abstractions.Contracts;

public interface IBluetoothLE
{
	BluetoothState State { get; }

	bool IsAvailable { get; }

	bool IsOn { get; }

	IAdapter Adapter { get; }

	event EventHandler<BluetoothStateChangedArgs> StateChanged;
}

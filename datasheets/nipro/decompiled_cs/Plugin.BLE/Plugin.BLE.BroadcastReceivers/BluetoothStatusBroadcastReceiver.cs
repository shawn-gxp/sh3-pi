using System;
using Android.Bluetooth;
using Android.Content;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Extensions;

namespace Plugin.BLE.BroadcastReceivers;

public class BluetoothStatusBroadcastReceiver : BroadcastReceiver
{
	private readonly Action<BluetoothState> _stateChangedHandler;

	public BluetoothStatusBroadcastReceiver(Action<BluetoothState> stateChangedHandler)
	{
		_stateChangedHandler = stateChangedHandler;
	}

	public override void OnReceive(Context context, Intent intent)
	{
		if (!(intent.Action != "android.bluetooth.adapter.action.STATE_CHANGED"))
		{
			int intExtra = intent.GetIntExtra("android.bluetooth.adapter.extra.STATE", -1);
			if (intExtra == -1)
			{
				_stateChangedHandler?.Invoke(BluetoothState.Unknown);
				return;
			}
			State state = (State)intExtra;
			_stateChangedHandler?.Invoke(state.ToBluetoothState());
		}
	}
}

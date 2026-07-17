using Android.App;
using Android.Bluetooth;
using Android.Content;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Android;
using Plugin.BLE.BroadcastReceivers;
using Plugin.BLE.Extensions;

namespace Plugin.BLE;

internal class BleImplementation : BleImplementationBase
{
	private BluetoothManager _bluetoothManager;

	protected override void InitializeNative()
	{
		Context context = Application.Context;
		if (context.PackageManager.HasSystemFeature("android.hardware.bluetooth_le"))
		{
			BluetoothStatusBroadcastReceiver receiver = new BluetoothStatusBroadcastReceiver(UpdateState);
			context.RegisterReceiver(receiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
			_bluetoothManager = (BluetoothManager)context.GetSystemService("bluetooth");
		}
	}

	protected override BluetoothState GetInitialStateNative()
	{
		if (_bluetoothManager == null)
		{
			return BluetoothState.Unavailable;
		}
		return _bluetoothManager.Adapter.State.ToBluetoothState();
	}

	protected override IAdapter CreateNativeAdapter()
	{
		return new Adapter(_bluetoothManager);
	}

	private void UpdateState(BluetoothState state)
	{
		base.State = state;
	}
}

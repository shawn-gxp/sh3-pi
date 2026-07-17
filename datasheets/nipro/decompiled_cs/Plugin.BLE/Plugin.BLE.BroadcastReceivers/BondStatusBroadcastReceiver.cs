using System;
using Android.Bluetooth;
using Android.Content;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.EventArgs;
using Plugin.BLE.Android;

namespace Plugin.BLE.BroadcastReceivers;

public class BondStatusBroadcastReceiver : BroadcastReceiver
{
	public event EventHandler<DeviceBondStateChangedEventArgs> BondStateChanged;

	public override void OnReceive(Context context, Intent intent)
	{
		Bond intExtra = (Bond)intent.GetIntExtra("android.bluetooth.device.extra.BOND_STATE", 10);
		Device device = new Device(null, (BluetoothDevice)intent.GetParcelableExtra("android.bluetooth.device.extra.DEVICE"), null, 0);
		Console.WriteLine(intExtra.ToString());
		if (this.BondStateChanged != null)
		{
			switch (intExtra)
			{
			case Bond.None:
				this.BondStateChanged(this, new DeviceBondStateChangedEventArgs
				{
					Device = device,
					State = DeviceBondState.NotBonded
				});
				break;
			case Bond.Bonding:
				this.BondStateChanged(this, new DeviceBondStateChangedEventArgs
				{
					Device = device,
					State = DeviceBondState.Bonding
				});
				break;
			case Bond.Bonded:
				this.BondStateChanged(this, new DeviceBondStateChangedEventArgs
				{
					Device = device,
					State = DeviceBondState.Bonded
				});
				break;
			}
		}
	}
}

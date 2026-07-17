using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using BLELib.Common;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;

namespace BLELib.BLEDevice;

public interface IBLEDevice
{
	string DeviceName { get; }

	IDevice Device { get; set; }

	event EventHandler<BLELibStatusEventArgs> StateChanged;

	object CreateDevice(IDevice device);

	Task<IList<string>> Pairing(IList<string> param = null);

	Task<object> ReciveStart(Action<int, int> handler, int timeout, IList<string> param = null);

	Task ReceiveStop();

	void Cancel();

	void BondStatusBroadcastReceiver_BondStateChanged(object sender, DeviceBondStateChangedEventArgs e);

	void InitializeDeviceInfo(IDevice device);

	Task<bool> DeleteUserByParam(int timeout, IList<string> param);

	Task<IList<object>> GetFreeColor(int timeout, IList<string> param);

	Task<bool> RegisterUserColor(int timeout, IList<string> param);
}

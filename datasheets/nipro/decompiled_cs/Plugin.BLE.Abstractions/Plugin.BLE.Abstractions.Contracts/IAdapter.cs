using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Plugin.BLE.Abstractions.EventArgs;

namespace Plugin.BLE.Abstractions.Contracts;

public interface IAdapter
{
	bool IsScanning { get; }

	int ScanTimeout { get; set; }

	ScanMode ScanMode { get; set; }

	IList<IDevice> DiscoveredDevices { get; }

	IList<IDevice> ConnectedDevices { get; }

	event EventHandler<DeviceEventArgs> DeviceAdvertised;

	event EventHandler<DeviceEventArgs> DeviceDiscovered;

	event EventHandler<DeviceEventArgs> DeviceConnected;

	event EventHandler<DeviceEventArgs> DeviceDisconnected;

	event EventHandler<DeviceErrorEventArgs> DeviceConnectionLost;

	event EventHandler ScanTimeoutElapsed;

	Task StartScanningForDevicesAsync(Guid[] serviceUuids = null, Func<IDevice, bool> deviceFilter = null, bool allowDuplicatesKey = false, CancellationToken cancellationToken = default(CancellationToken));

	Task StopScanningForDevicesAsync();

	Task ConnectToDeviceAsync(IDevice device, ConnectParameters connectParameters = default(ConnectParameters), CancellationToken cancellationToken = default(CancellationToken));

	Task DisconnectDeviceAsync(IDevice device);

	Task<IDevice> ConnectToKnownDeviceAsync(Guid deviceGuid, ConnectParameters connectParameters = default(ConnectParameters), CancellationToken cancellationToken = default(CancellationToken));

	List<IDevice> GetSystemConnectedOrPairedDevices(Guid[] services = null);
}

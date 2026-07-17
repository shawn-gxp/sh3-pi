using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Plugin.BLE.Abstractions.Exceptions;
using Plugin.BLE.Abstractions.Utils;

namespace Plugin.BLE.Abstractions.Extensions;

public static class AdapterExtenstion
{
	public static Task StartScanningForDevicesAsync(this IAdapter adapter, CancellationToken cancellationToken)
	{
		return adapter.StartScanningForDevicesAsync(null, null, allowDuplicatesKey: false, cancellationToken);
	}

	public static Task StartScanningForDevicesAsync(this IAdapter adapter, Guid[] serviceUuids, CancellationToken cancellationToken = default(CancellationToken))
	{
		return adapter.StartScanningForDevicesAsync(serviceUuids, null, allowDuplicatesKey: false, cancellationToken);
	}

	public static Task StartScanningForDevicesAsync(this IAdapter adapter, Func<IDevice, bool> deviceFilter, CancellationToken cancellationToken = default(CancellationToken))
	{
		return adapter.StartScanningForDevicesAsync(null, deviceFilter, allowDuplicatesKey: false, cancellationToken);
	}

	public static Task<IDevice> DiscoverDeviceAsync(this IAdapter adapter, Guid deviceId, CancellationToken cancellationToken = default(CancellationToken))
	{
		return adapter.DiscoverDeviceAsync((IDevice device) => device.Id == deviceId, cancellationToken);
	}

	public static async Task<IDevice> DiscoverDeviceAsync(this IAdapter adapter, Func<IDevice, bool> deviceFilter, CancellationToken cancellationToken = default(CancellationToken))
	{
		IDevice device = adapter.DiscoveredDevices.FirstOrDefault(deviceFilter);
		if (device != null)
		{
			return device;
		}
		if (adapter.IsScanning)
		{
			await adapter.StopScanningForDevicesAsync();
		}
		return await TaskBuilder.FromEvent<IDevice, EventHandler<DeviceEventArgs>, EventHandler>(delegate
		{
			adapter.StartScanningForDevicesAsync(deviceFilter, cancellationToken);
		}, (Action<IDevice> complete, Action<Exception> reject) => delegate(object sender, DeviceEventArgs args)
		{
			complete(args.Device);
			adapter.StopScanningForDevicesAsync();
		}, delegate(EventHandler<DeviceEventArgs> handler)
		{
			adapter.DeviceDiscovered += handler;
		}, delegate(EventHandler<DeviceEventArgs> handler)
		{
			adapter.DeviceDiscovered -= handler;
		}, (Action<Exception> reject) => delegate
		{
			reject(new DeviceDiscoverException());
		}, delegate(EventHandler handler)
		{
			adapter.ScanTimeoutElapsed += handler;
		}, delegate(EventHandler handler)
		{
			adapter.ScanTimeoutElapsed -= handler;
		}, cancellationToken);
	}

	public static Task ConnectToDeviceAsync(this IAdapter adapter, IDevice device, ConnectParameters connectParameters, CancellationToken cancellationToken)
	{
		return adapter.ConnectToDeviceAsync(device, connectParameters, cancellationToken);
	}
}

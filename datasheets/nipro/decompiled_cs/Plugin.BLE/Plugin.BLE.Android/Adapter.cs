using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Android.Bluetooth;
using Android.Bluetooth.LE;
using Android.OS;
using Android.Runtime;
using Java.Lang;
using Java.Util;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Extensions;

namespace Plugin.BLE.Android;

public class Adapter : AdapterBase
{
	private class DeviceComparer : IEqualityComparer<BluetoothDevice>
	{
		public bool Equals(BluetoothDevice x, BluetoothDevice y)
		{
			return x.Address == y.Address;
		}

		public int GetHashCode(BluetoothDevice obj)
		{
			return obj.GetHashCode();
		}
	}

	public class Api18BleScanCallback : Java.Lang.Object, BluetoothAdapter.ILeScanCallback, IJavaObject, IDisposable
	{
		private readonly Adapter _adapter;

		public Api18BleScanCallback(Adapter adapter)
		{
			_adapter = adapter;
		}

		public void OnLeScan(BluetoothDevice bleDevice, int rssi, byte[] scanRecord)
		{
			Trace.Message("Adapter.LeScanCallback: " + bleDevice.Name);
			_adapter.HandleDiscoveredDevice(new Device(_adapter, bleDevice, null, rssi, scanRecord));
		}
	}

	public class Api21BleScanCallback : ScanCallback
	{
		private readonly Adapter _adapter;

		public Api21BleScanCallback(Adapter adapter)
		{
			_adapter = adapter;
		}

		public override void OnScanFailed(ScanFailure errorCode)
		{
			Trace.Message("Adapter: Scan failed with code {0}", errorCode);
			base.OnScanFailed(errorCode);
		}

		public override void OnScanResult(ScanCallbackType callbackType, ScanResult result)
		{
			base.OnScanResult(callbackType, result);
			Device device = new Device(_adapter, result.Device, null, result.Rssi, result.ScanRecord.GetBytes());
			_adapter.HandleDiscoveredDevice(device);
		}
	}

	private readonly BluetoothManager _bluetoothManager;

	private readonly BluetoothAdapter _bluetoothAdapter;

	private readonly Api18BleScanCallback _api18ScanCallback;

	private readonly Api21BleScanCallback _api21ScanCallback;

	public override IList<IDevice> ConnectedDevices => ConnectedDeviceRegistry.Values.ToList();

	public Dictionary<string, IDevice> ConnectedDeviceRegistry { get; }

	public Adapter(BluetoothManager bluetoothManager)
	{
		_bluetoothManager = bluetoothManager;
		_bluetoothAdapter = bluetoothManager.Adapter;
		ConnectedDeviceRegistry = new Dictionary<string, IDevice>();
		if (Build.VERSION.SdkInt >= BuildVersionCodes.Lollipop)
		{
			_api21ScanCallback = new Api21BleScanCallback(this);
		}
		else
		{
			_api18ScanCallback = new Api18BleScanCallback(this);
		}
	}

	protected override Task StartScanningForDevicesNativeAsync(Guid[] serviceUuids, bool allowDuplicatesKey, CancellationToken scanCancellationToken)
	{
		DiscoveredDevices.Clear();
		if (Build.VERSION.SdkInt < BuildVersionCodes.Lollipop)
		{
			StartScanningOld(serviceUuids);
		}
		else
		{
			StartScanningNew(serviceUuids);
		}
		return Task.FromResult(result: true);
	}

	private void StartScanningOld(Guid[] serviceUuids)
	{
		bool num = serviceUuids?.Any() ?? false;
		UUID[] serviceUuids2 = null;
		if (num)
		{
			serviceUuids2 = serviceUuids.Select((Guid u) => UUID.FromString(u.ToString())).ToArray();
		}
		Trace.Message("Adapter < 21: Starting a scan for devices.");
		_bluetoothAdapter.StartLeScan(serviceUuids2, _api18ScanCallback);
	}

	private void StartScanningNew(Guid[] serviceUuids)
	{
		bool flag = serviceUuids?.Any() ?? false;
		List<ScanFilter> list = null;
		if (flag)
		{
			list = new List<ScanFilter>();
			for (int i = 0; i < serviceUuids.Length; i++)
			{
				Guid guid = serviceUuids[i];
				ScanFilter.Builder builder = new ScanFilter.Builder();
				builder.SetServiceUuid(ParcelUuid.FromString(guid.ToString()));
				list.Add(builder.Build());
			}
		}
		ScanSettings.Builder builder2 = new ScanSettings.Builder();
		builder2.SetScanMode(base.ScanMode.ToNative());
		if (_bluetoothAdapter.BluetoothLeScanner != null)
		{
			Trace.Message($"Adapter >=21: Starting a scan for devices. ScanMode: {base.ScanMode}");
			if (flag)
			{
				Trace.Message(string.Format("ScanFilters: {0}", string.Join(", ", serviceUuids)));
			}
			_bluetoothAdapter.BluetoothLeScanner.StartScan(list, builder2.Build(), _api21ScanCallback);
		}
		else
		{
			Trace.Message("Adapter >= 21: Scan failed. Bluetooth is probably off");
		}
	}

	protected override void StopScanNative()
	{
		if (Build.VERSION.SdkInt < BuildVersionCodes.Lollipop)
		{
			Trace.Message("Adapter < 21: Stopping the scan for devices.");
			_bluetoothAdapter.StopLeScan(_api18ScanCallback);
		}
		else
		{
			Trace.Message("Adapter >= 21: Stopping the scan for devices.");
			_bluetoothAdapter.BluetoothLeScanner?.StopScan(_api21ScanCallback);
		}
	}

	protected override Task ConnectToDeviceNativeAsync(IDevice device, ConnectParameters connectParameters, CancellationToken cancellationToken)
	{
		((Device)device).Connect(connectParameters);
		return Task.CompletedTask;
	}

	protected override void DisconnectDeviceNative(IDevice device)
	{
		((Device)device).Disconnect();
	}

	public override async Task<IDevice> ConnectToKnownDeviceAsync(Guid deviceGuid, ConnectParameters connectParameters = default(ConnectParameters), CancellationToken cancellationToken = default(CancellationToken))
	{
		byte[] address = deviceGuid.ToByteArray().Skip(10).Take(6)
			.ToArray();
		BluetoothDevice remoteDevice = _bluetoothAdapter.GetRemoteDevice(address);
		Device device = new Device(this, remoteDevice, null, 0, new byte[0]);
		await ConnectToDeviceAsync(device, connectParameters, cancellationToken);
		return device;
	}

	public override List<IDevice> GetSystemConnectedOrPairedDevices(Guid[] services = null)
	{
		if (services != null)
		{
			Trace.Message("Caution: GetSystemConnectedDevices does not take into account the 'services' parameter on Android.");
		}
		IEnumerable<BluetoothDevice> first = from d in _bluetoothManager.GetConnectedDevices(ProfileType.Gatt)
			where d.Type == BluetoothDeviceType.Le || d.Type == BluetoothDeviceType.Dual
			select d;
		IEnumerable<BluetoothDevice> second = _bluetoothAdapter.BondedDevices.Where((BluetoothDevice d) => d.Type == BluetoothDeviceType.Le || d.Type == BluetoothDeviceType.Dual);
		return (from d in first.Union(second, new DeviceComparer())
			select new Device(this, d, null, 0)).Cast<IDevice>().ToList();
	}
}

using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Plugin.BLE.Abstractions.Exceptions;
using Plugin.BLE.Abstractions.Utils;

namespace Plugin.BLE.Abstractions;

public abstract class AdapterBase : IAdapter
{
	private CancellationTokenSource _scanCancellationTokenSource;

	private readonly IList<IDevice> _discoveredDevices;

	private volatile bool _isScanning;

	private Func<IDevice, bool> _currentScanDeviceFilter;

	public bool IsScanning
	{
		get
		{
			return _isScanning;
		}
		private set
		{
			_isScanning = value;
		}
	}

	public int ScanTimeout { get; set; } = 10000;

	public ScanMode ScanMode { get; set; } = ScanMode.LowPower;

	public virtual IList<IDevice> DiscoveredDevices => _discoveredDevices;

	public abstract IList<IDevice> ConnectedDevices { get; }

	public event EventHandler<DeviceEventArgs> DeviceAdvertised = delegate
	{
	};

	public event EventHandler<DeviceEventArgs> DeviceDiscovered = delegate
	{
	};

	public event EventHandler<DeviceEventArgs> DeviceConnected = delegate
	{
	};

	public event EventHandler<DeviceEventArgs> DeviceDisconnected = delegate
	{
	};

	public event EventHandler<DeviceErrorEventArgs> DeviceConnectionLost = delegate
	{
	};

	public event EventHandler<DeviceErrorEventArgs> DeviceConnectionError = delegate
	{
	};

	public event EventHandler ScanTimeoutElapsed = delegate
	{
	};

	protected AdapterBase()
	{
		_discoveredDevices = new List<IDevice>();
	}

	public async Task StartScanningForDevicesAsync(Guid[] serviceUuids = null, Func<IDevice, bool> deviceFilter = null, bool allowDuplicatesKey = false, CancellationToken cancellationToken = default(CancellationToken))
	{
		if (IsScanning)
		{
			Trace.Message("Adapter: Already scanning!");
			return;
		}
		IsScanning = true;
		serviceUuids = serviceUuids ?? new Guid[0];
		_currentScanDeviceFilter = deviceFilter ?? ((Func<IDevice, bool>)((IDevice d) => true));
		_scanCancellationTokenSource = new CancellationTokenSource();
		try
		{
			using (cancellationToken.Register(delegate
			{
				_scanCancellationTokenSource?.Cancel();
			}))
			{
				await StartScanningForDevicesNativeAsync(serviceUuids, allowDuplicatesKey, _scanCancellationTokenSource.Token);
				await Task.Delay(ScanTimeout, _scanCancellationTokenSource.Token);
				Trace.Message("Adapter: Scan timeout has elapsed.");
				CleanupScan();
				this.ScanTimeoutElapsed(this, new System.EventArgs());
			}
		}
		catch (TaskCanceledException)
		{
			CleanupScan();
			Trace.Message("Adapter: Scan was cancelled.");
		}
	}

	public Task StopScanningForDevicesAsync()
	{
		if (_scanCancellationTokenSource != null && !_scanCancellationTokenSource.IsCancellationRequested)
		{
			_scanCancellationTokenSource.Cancel();
		}
		else
		{
			Trace.Message("Adapter: Already cancelled scan.");
		}
		return Task.FromResult(0);
	}

	public async Task ConnectToDeviceAsync(IDevice device, ConnectParameters connectParameters = default(ConnectParameters), CancellationToken cancellationToken = default(CancellationToken))
	{
		if (device == null)
		{
			throw new ArgumentNullException("device");
		}
		if (device.State == DeviceState.Connected)
		{
			return;
		}
		CancellationTokenSource cts = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
		try
		{
			await TaskBuilder.FromEvent<bool, EventHandler<DeviceEventArgs>, EventHandler<DeviceErrorEventArgs>>(delegate
			{
				ConnectToDeviceNativeAsync(device, connectParameters, cts.Token);
			}, (Action<bool> complete, Action<Exception> reject) => delegate(object sender, DeviceEventArgs args)
			{
				if (args.Device.Id == device.Id)
				{
					Trace.Message("ConnectToDeviceAsync Connected: {0} {1}", args.Device.Id, args.Device.Name);
					complete(obj: true);
				}
			}, delegate(EventHandler<DeviceEventArgs> handler)
			{
				DeviceConnected += handler;
			}, delegate(EventHandler<DeviceEventArgs> handler)
			{
				DeviceConnected -= handler;
			}, (Action<Exception> reject) => delegate(object sender, DeviceErrorEventArgs args)
			{
				if (args.Device?.Id == device.Id)
				{
					Trace.Message("ConnectAsync Error: {0} {1}", args.Device?.Id, args.Device?.Name);
					reject(new DeviceConnectionException((args.Device?.Id).Value, args.Device?.Name, args.ErrorMessage));
				}
			}, delegate(EventHandler<DeviceErrorEventArgs> handler)
			{
				DeviceConnectionError += handler;
			}, delegate(EventHandler<DeviceErrorEventArgs> handler)
			{
				DeviceConnectionError -= handler;
			}, cts.Token);
		}
		finally
		{
			if (cts != null)
			{
				((IDisposable)cts).Dispose();
			}
		}
	}

	public Task DisconnectDeviceAsync(IDevice device)
	{
		if (!ConnectedDevices.Contains(device))
		{
			Trace.Message("Disconnect async: device {0} not in the list of connected devices.", device.Name);
			return Task.FromResult(result: false);
		}
		return TaskBuilder.FromEvent<bool, EventHandler<DeviceEventArgs>, EventHandler<DeviceErrorEventArgs>>(delegate
		{
			DisconnectDeviceNative(device);
		}, (Action<bool> complete, Action<Exception> reject) => delegate(object sender, DeviceEventArgs args)
		{
			if (args.Device.Id == device.Id)
			{
				Trace.Message("DisconnectAsync Disconnected: {0} {1}", args.Device.Id, args.Device.Name);
				complete(obj: true);
			}
		}, delegate(EventHandler<DeviceEventArgs> handler)
		{
			DeviceDisconnected += handler;
		}, delegate(EventHandler<DeviceEventArgs> handler)
		{
			DeviceDisconnected -= handler;
		}, (Action<Exception> reject) => delegate(object sender, DeviceErrorEventArgs args)
		{
			if (args.Device.Id == device.Id)
			{
				Trace.Message("DisconnectAsync", "Disconnect Error: {0} {1}", args.Device?.Id, args.Device?.Name);
				reject(new Exception("Disconnect operation exception"));
			}
		}, delegate(EventHandler<DeviceErrorEventArgs> handler)
		{
			DeviceConnectionError += handler;
		}, delegate(EventHandler<DeviceErrorEventArgs> handler)
		{
			DeviceConnectionError -= handler;
		});
	}

	private void CleanupScan()
	{
		Trace.Message("Adapter: Stopping the scan for devices.");
		StopScanNative();
		if (_scanCancellationTokenSource != null)
		{
			_scanCancellationTokenSource.Dispose();
			_scanCancellationTokenSource = null;
		}
		IsScanning = false;
	}

	public void HandleDiscoveredDevice(IDevice device)
	{
		if (_currentScanDeviceFilter(device))
		{
			this.DeviceAdvertised(this, new DeviceEventArgs
			{
				Device = device
			});
			if (!_discoveredDevices.Contains(device))
			{
				_discoveredDevices.Add(device);
				this.DeviceDiscovered(this, new DeviceEventArgs
				{
					Device = device
				});
			}
		}
	}

	public void HandleConnectedDevice(IDevice device)
	{
		this.DeviceConnected(this, new DeviceEventArgs
		{
			Device = device
		});
	}

	public void HandleDisconnectedDevice(bool disconnectRequested, IDevice device)
	{
		if (disconnectRequested)
		{
			Trace.Message("DisconnectedPeripheral by user: {0}", device.Name);
			this.DeviceDisconnected(this, new DeviceEventArgs
			{
				Device = device
			});
			return;
		}
		Trace.Message("DisconnectedPeripheral by lost signal: {0}", device.Name);
		this.DeviceConnectionLost(this, new DeviceErrorEventArgs
		{
			Device = device
		});
		if (DiscoveredDevices.Contains(device))
		{
			DiscoveredDevices.Remove(device);
		}
	}

	public void HandleConnectionFail(IDevice device, string errorMessage)
	{
		Trace.Message("Failed to connect peripheral {0}: {1}", device.Id, device.Name);
		this.DeviceConnectionError(this, new DeviceErrorEventArgs
		{
			Device = device,
			ErrorMessage = errorMessage
		});
	}

	protected abstract Task StartScanningForDevicesNativeAsync(Guid[] serviceUuids, bool allowDuplicatesKey, CancellationToken scanCancellationToken);

	protected abstract void StopScanNative();

	protected abstract Task ConnectToDeviceNativeAsync(IDevice device, ConnectParameters connectParameters, CancellationToken cancellationToken);

	protected abstract void DisconnectDeviceNative(IDevice device);

	public abstract Task<IDevice> ConnectToKnownDeviceAsync(Guid deviceGuid, ConnectParameters connectParameters = default(ConnectParameters), CancellationToken cancellationToken = default(CancellationToken));

	public abstract List<IDevice> GetSystemConnectedOrPairedDevices(Guid[] services = null);
}

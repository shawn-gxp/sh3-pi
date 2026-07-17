using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Android.App;
using Android.Content;
using BLELib.Common;
using BLELib.Helper;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Plugin.BLE.BroadcastReceivers;

namespace BLELib.Android;

public class BLELibrary : BLELib
{
	private BondStatusBroadcastReceiver _BondStatusBroadcastReceiver = new BondStatusBroadcastReceiver();

	private EventHandler<DeviceBondStateChangedEventArgs> _BondStateChanged;

	public override void Initialize()
	{
		base.Initialize();
		Application.Context.RegisterReceiver(_BondStatusBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
		_BondStatusBroadcastReceiver.BondStateChanged += _BondStatusBroadcastReceiver_BondStateChanged;
	}

	protected override void StartBondingCheck(EventHandler<DeviceBondStateChangedEventArgs> handler, IDevice device = null)
	{
		_BondStateChanged = (EventHandler<DeviceBondStateChangedEventArgs>)Delegate.Combine(_BondStateChanged, handler);
	}

	private void _BondStatusBroadcastReceiver_BondStateChanged(object sender, DeviceBondStateChangedEventArgs e)
	{
		if (_BondStateChanged != null)
		{
			_BondStateChanged(sender, e);
		}
	}

	protected override void StopBondingCheck(EventHandler<DeviceBondStateChangedEventArgs> handler)
	{
		if (handler != null)
		{
			_BondStateChanged = (EventHandler<DeviceBondStateChangedEventArgs>)Delegate.Remove(_BondStateChanged, handler);
		}
	}

	public override async Task ScanPause()
	{
		Log.Debug("【BLELib】【Android】【BLELibrary】【ScanPause】ScanPause start");
		if (_IAdapter.IsScanning)
		{
			await _IAdapter.StopScanningForDevicesAsync();
		}
	}

	public override async Task ScanPauseRestart(int msec)
	{
		Log.Debug("【BLELib】【Android】【BLELibrary】【ScanPauseRestart】ScanPauseRestart start");
		if (_CancellationTokenSource != null)
		{
			_CancellationTokenSource.Cancel();
			while (_CancellationTokenSource != null)
			{
				await Task.Delay(100);
			}
		}
		_CancellationTokenSource = new CancellationTokenSource();
		try
		{
			await Task.Run(async delegate
			{
				await Task.Delay(msec);
				if (!_IAdapter.IsScanning)
				{
					base.ScanDeviceList.Clear();
					await _IAdapter.StartScanningForDevicesAsync(null, delegate(IDevice device)
					{
						if (_ScanTargetPrefixs == null || _ScanTargetPrefixs.Count == 0)
						{
							return true;
						}
						return _ScanTargetPrefixs.Any((string x) => !string.IsNullOrEmpty(device?.Name) && device.Name.TrimStart().StartsWith(x)) ? true : false;
					});
				}
			}, _CancellationTokenSource.Token);
		}
		catch (TaskCanceledException arg)
		{
			Log.Warn($"【BLELib】【BLEDeviceNSM1】【ReciveStart】TaskCanceledException発生：{arg}");
		}
		catch (OperationCanceledException arg2)
		{
			Log.Warn($"【BLELib】【BLEDeviceNSM1】【ReciveStart】OperationCanceledException発生：{arg2}");
		}
		catch (Exception arg3)
		{
			Log.Error($"【BLELib】【Android】【BLELibrary】【ScanPauseRestart】例外発生：{arg3}");
		}
		finally
		{
			if (_CancellationTokenSource != null)
			{
				_CancellationTokenSource.Dispose();
				_CancellationTokenSource = null;
			}
		}
		Log.Debug("【BLELib】【Android】【BLELibrary】【ScanPauseRestart】ScanPauseRestart finish");
	}

	public override async Task ReceiveWait(IList<string> names, int timeout, Action<IList<string>> handler)
	{
		Log.Debug("【BLELib】【Android】【BLELibrary】【ReceiveWait】ReceiveWait start device names : " + names?.JoinString(","));
		_IAdapter.DeviceDiscovered += _IAdapter_DeviceDiscovered;
		base.IsReceiveWait = true;
		base.ScanDeviceList = new List<ScanDevice>();
		ScanMode result = ScanMode.Balanced;
		Enum.TryParse<ScanMode>(base.Mode.ToString(), out result);
		_IAdapter.ScanMode = result;
		_IAdapter.ScanTimeout = timeout;
		_ScanHandler = handler;
		_ScanTargetPrefixs = names;
		string arg = ((handler == null) ? "(null)" : "(not null)");
		Log.Trace(string.Format("【BLELib】【Android】【BLELibrary】【ReceiveWait】names={0}, timeout={1}, handler={2}", names?.JoinString(","), timeout, arg));
		StatusChange("", BLELibStatus.RCV_WAIT_START);
		if (_CancellationTokenSource == null)
		{
			_CancellationTokenSource = new CancellationTokenSource();
		}
		await _IAdapter.StartScanningForDevicesAsync(null, delegate(IDevice device)
		{
			if (_ScanTargetPrefixs == null || _ScanTargetPrefixs.Count == 0)
			{
				return true;
			}
			return _ScanTargetPrefixs.Any((string x) => !string.IsNullOrEmpty(device?.Name) && device.Name.Trim() == x) ? true : false;
		}, allowDuplicatesKey: false, _CancellationTokenSource.Token);
		Log.Debug("【BLELib】【Android】【BLELibrary】【ReceiveWait】ReceiveWait finish device names : " + names?.JoinString(","));
	}

	public override async Task ReceiveStop()
	{
		Log.Debug("【BLELib】【Android】【BLELibrary】【ReceiveStop】ReceiveStop start");
		_IAdapter.DeviceDiscovered -= _IAdapter_DeviceDiscovered;
		base.IsReceiveWait = false;
		for (int i = base.ReceiveDeviceList.Count - 1; i >= 0; i--)
		{
			if (base.ReceiveDeviceList[i] != null)
			{
				StatusChange(base.ReceiveDeviceList[i].DeviceName, BLELibStatus.RCV_STOP);
				await base.ReceiveDeviceList[i].ReceiveStop().ConfigureAwait(continueOnCapturedContext: false);
			}
		}
		Log.Trace("【BLELib】【Android】【BLELibrary】【ReceiveStop】ReceiveStop");
		StatusChange("", BLELibStatus.RCV_WAIT_STOP);
		await _IAdapter.StopScanningForDevicesAsync();
		_ScanHandler = null;
		Log.Debug("【BLELib】【Android】【BLELibrary】【ReceiveStop】ReceiveStop finish");
	}

	protected override void _IAdapter_DeviceAdvertised(object sender, DeviceEventArgs e)
	{
		try
		{
			DeviceBase device = e.Device as DeviceBase;
			if (device == null)
			{
				return;
			}
			string text = "";
			AdvertisementRecord advertisementRecord = device.AdvertisementRecords.Where((AdvertisementRecord x) => x.Type == AdvertisementRecordType.ManufacturerSpecificData).FirstOrDefault();
			if (advertisementRecord != null && advertisementRecord.Data.Length >= 4)
			{
				text = Convert.ToInt64(BitConverter.ToString(advertisementRecord.Data.Skip(advertisementRecord.Data.Length - 4).ToArray().Reverse()
					.ToArray()).Replace("-", ""), 16).ToString();
			}
			foreach (AdvertisementRecord advertisementRecord2 in device.AdvertisementRecords)
			{
				_ = advertisementRecord2.Type;
				_ = 9;
			}
			if (string.IsNullOrEmpty(device.Name))
			{
				return;
			}
			if (base.ScanDeviceList.All((ScanDevice x) => x.Device.Id != device.Id))
			{
				base.ScanDeviceList.Add(new ScanDevice
				{
					Device = e.Device,
					Name = e.Device.Name,
					SerialNumber = text
				});
			}
			if (_ScanHandler != null)
			{
				_ScanHandler(new string[5]
				{
					device.Name,
					device.Id.ToString(),
					e.Device.Rssi.ToString(),
					e.Device.State.ToString(),
					text
				});
				Task.Run(async delegate
				{
					await ScanPause();
					await ScanPauseRestart(5000);
				});
			}
		}
		catch (Exception arg)
		{
			Log.Error($"【BLELib】【Android】【BLELibrary】【_IAdapter_DeviceAdvertised】例外発生：{arg}");
		}
	}

	protected override void _IAdapter_DeviceDiscovered(object sender, DeviceEventArgs e)
	{
		Task.Run(delegate
		{
			DeviceBase device = e.Device as DeviceBase;
			if (device != null)
			{
				string text = "";
				AdvertisementRecord advertisementRecord = device.AdvertisementRecords.Where((AdvertisementRecord x) => x.Type == AdvertisementRecordType.ManufacturerSpecificData).FirstOrDefault();
				if (advertisementRecord != null && advertisementRecord.Data.Length >= 4)
				{
					text = Convert.ToInt64(BitConverter.ToString(advertisementRecord.Data.Skip(advertisementRecord.Data.Length - 4).ToArray().Reverse()
						.ToArray()).Replace("-", ""), 16).ToString();
				}
				foreach (AdvertisementRecord advertisementRecord2 in device.AdvertisementRecords)
				{
					_ = advertisementRecord2.Type;
					_ = 9;
				}
				if (!string.IsNullOrEmpty(device.Name))
				{
					ScanDevice scanDevice = base.ScanDeviceList.FirstOrDefault((ScanDevice x) => x.Device.Id == device.Id);
					if (scanDevice == null)
					{
						base.ScanDeviceList.Add(new ScanDevice
						{
							Device = e.Device,
							Name = e.Device.Name,
							SerialNumber = text
						});
					}
					else
					{
						scanDevice.Device = e.Device;
					}
					if (_ScanHandler != null)
					{
						_ScanHandler(new string[5]
						{
							device.Name,
							device.Id.ToString(),
							e.Device.Rssi.ToString(),
							e.Device.State.ToString(),
							text
						});
					}
				}
			}
		}).ConfigureAwait(continueOnCapturedContext: false);
	}
}

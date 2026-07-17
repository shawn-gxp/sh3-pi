using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using BLELib.BLEDevice;
using BLELib.Common;
using BLELib.Helper;
using Plugin.BLE;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Plugin.BLE.Abstractions.Exceptions;

namespace BLELib;

public abstract class BLELib : IDisposable, IBLELib
{
	public class ScanDevice
	{
		public string Name { get; set; }

		public IDevice Device { get; set; }

		public string SerialNumber { get; set; }
	}

	protected ILoggingService Log;

	private object _ILoggingService;

	protected BLEManager _BLEManager;

	protected IBluetoothLE _CrossBluetoothLE;

	protected IAdapter _IAdapter;

	protected Action<IList<string>> _ScanHandler;

	protected IList<string> _ScanTargetPrefixs;

	protected CancellationTokenSource _CancellationTokenSource;

	private bool disposedValue;

	public bool IsScanning
	{
		get
		{
			if (_IAdapter == null)
			{
				return false;
			}
			return _IAdapter.IsScanning;
		}
	}

	protected List<ScanDevice> ScanDeviceList { get; set; } = new List<ScanDevice>();

	protected List<IBLEDevice> ReceiveDeviceList { get; set; } = new List<IBLEDevice>();

	public int Mode { get; set; } = 2;

	public bool IsReceiveWait { get; set; }

	public object ILoggingService
	{
		set
		{
			_ILoggingService = value;
		}
	}

	public event EventHandler<BLELibStatusEventArgs> StateChanged;

	public virtual void Initialize()
	{
		LogManager.Initialize(_ILoggingService);
		Log = LogManager.GetLogger();
		Log.Debug("【BLELib】【BLELib】【Initialize】Initialize start");
		_CrossBluetoothLE = CrossBluetoothLE.Current;
		_IAdapter = CrossBluetoothLE.Current.Adapter;
		_CrossBluetoothLE.StateChanged += _CrossBluetoothLE_StateChanged;
		_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
		_IAdapter.DeviceConnectionLost += _IAdapter_DeviceConnectionLost;
		_IAdapter.DeviceDisconnected += _IAdapter_DeviceDisconnected;
		_IAdapter.ScanTimeoutElapsed += _IAdapter_ScanTimeoutElapsed;
		Trace.TraceImplementation = delegate(string message, object[] args)
		{
			if (!message.StartsWith("Adv rec"))
			{
				Log.Trace("【Plugin.BLE】" + string.Format(message, args));
			}
		};
		_BLEManager = new BLEManager();
		_BLEManager.Initialize();
		Log.Debug("【BLELib】【BLELib】【Initialize】Initialize end");
	}

	protected virtual void _IAdapter_DeviceDiscovered(object sender, DeviceEventArgs e)
	{
		Log.Debug("【BLELib】【BLELib】【_IAdapter_DeviceDiscovered】e.Device.Name=" + e?.Device.Name + ", e.Device.State=" + e?.Device.State.ToString());
	}

	protected void StatusChange(string deviceName, BLELibStatus status, string message = "")
	{
		if (this.StateChanged != null)
		{
			this.StateChanged(this, new BLELibStatusEventArgs(deviceName, status, message));
		}
	}

	protected void RemoveDevice(string name)
	{
		Log.Debug(string.Format("【BLELib】【BLELib】【RemoveDevice】name={0}:IsReceiveWait={1}:ReceiveDeviceList.Count={2}", new object[3] { name, IsReceiveWait, ReceiveDeviceList.Count }));
		for (int num = ReceiveDeviceList.Count - 1; num >= 0; num--)
		{
			if (ReceiveDeviceList[num]?.Device?.Name == name)
			{
				ReceiveDeviceList[num].StateChanged -= _BleDevice_StateChanged;
				ReceiveDeviceList.Remove(ReceiveDeviceList[num]);
			}
		}
	}

	protected void ClearReceiveDeviceList()
	{
		for (int num = ReceiveDeviceList.Count - 1; num >= 0; num--)
		{
			ReceiveDeviceList[num].StateChanged -= _BleDevice_StateChanged;
			ReceiveDeviceList.Remove(ReceiveDeviceList[num]);
		}
	}

	public async Task ScanStart(IList<string> names, int timeout, Action<IList<string>> handler)
	{
		Log.Debug("【BLELib】【BLELib】【ScanStart】ScanStart target names : " + names?.JoinString(","));
		_IAdapter.DeviceAdvertised += _IAdapter_DeviceAdvertised;
		ScanDeviceList = new List<ScanDevice>();
		ScanMode result = ScanMode.Balanced;
		Enum.TryParse<ScanMode>(Mode.ToString(), out result);
		_IAdapter.ScanMode = result;
		_IAdapter.ScanTimeout = timeout;
		_ScanHandler = handler;
		_ScanTargetPrefixs = names;
		string text = ((handler == null) ? "(null)" : "(not null)");
		Log.Trace(string.Format("【BLELib】【BLELib】【ScanStart】names={0}, timeout={1}, handler={2}", new object[3]
		{
			names?.JoinString(","),
			timeout,
			text
		}));
		StatusChange("", BLELibStatus.SCAN_START);
		if (_CancellationTokenSource == null)
		{
			_CancellationTokenSource = new CancellationTokenSource();
		}
		await _IAdapter.StartScanningForDevicesAsync(null, (IDevice device) => _ScanTargetPrefixs == null || _ScanTargetPrefixs.Count == 0 || _ScanTargetPrefixs.Any((string x) => !string.IsNullOrEmpty(device?.Name) && device.Name.TrimStart(new char[0]).StartsWith(x)), allowDuplicatesKey: false, _CancellationTokenSource.Token);
		Log.Trace("【BLELib】【BLELib】【ScanStart】★ScanStart");
		Log.Trace($"【BLELib】【BLELib】【ScanStart】_IAdapter.IsScanning={_IAdapter.IsScanning}");
		Log.Trace($"【BLELib】【BLELib】【ScanStart】_CrossBluetoothLE.IsOn={_CrossBluetoothLE.IsOn}");
		Log.Trace($"【BLELib】【BLELib】【ScanStart】_CrossBluetoothLE.IsAvailable={_CrossBluetoothLE.IsAvailable}");
		Log.Trace($"【BLELib】【BLELib】【ScanStart】_CrossBluetoothLE.State={_CrossBluetoothLE.State}");
	}

	public async Task ScanStop()
	{
		Log.Debug("【BLELib】【BLELib】【ScanStop】ScanStop start");
		_IAdapter.DeviceAdvertised -= _IAdapter_DeviceAdvertised;
		if (_CancellationTokenSource != null)
		{
			_CancellationTokenSource.Cancel();
		}
		foreach (IBLEDevice receiveDevice in ReceiveDeviceList)
		{
			receiveDevice.Cancel();
		}
		StatusChange("", BLELibStatus.SCAN_STOP);
		await _IAdapter.StopScanningForDevicesAsync();
		_ScanHandler = null;
		Log.Debug("【BLELib】【BLELib】【ScanStop】ScanStop finish");
	}

	public async Task<IList<string>> Pairing(Guid id, string name, IList<string> param = null)
	{
		Log.Debug("【BLELib】【BLELib】【Pairing】start pairing");
		IList<string> res = null;
		IBLEDevice device = _BLEManager.GetDevice(name);
		if (device != null)
		{
			IDevice device2 = (from x in ScanDeviceList
				where x.Device.Id == id
				select x.Device).FirstOrDefault();
			if (device2 != null && device.CreateDevice(device2) is IBLEDevice device3)
			{
				try
				{
					device3.Cancel();
					StatusChange(name, BLELibStatus.PAIR_START);
					device3.StateChanged += _BleDevice_StateChanged;
					StartBondingCheck(device3.BondStatusBroadcastReceiver_BondStateChanged, device2);
					res = await device3.Pairing(param);
					Log.Trace("【BLELib】【BLELib】【Pairing】names=" + name + ", param=" + param?.JoinString(","));
					StatusChange(name, BLELibStatus.PAIR_END);
				}
				catch (DeviceConnectionException ex)
				{
					Log.Error($"【BLELib】【BLELib】【Pairing】DeviceConnectionException:{ex}");
					StatusChange(name, BLELibStatus.PAIR_ERR);
				}
				catch (Exception ex2)
				{
					Log.Error($"【BLELib】【BLELib】【Pairing】Exception:{ex2}");
					StatusChange(name, BLELibStatus.PAIR_ERR);
				}
				finally
				{
					StopBondingCheck(device3.BondStatusBroadcastReceiver_BondStateChanged);
					device3.StateChanged -= _BleDevice_StateChanged;
				}
			}
		}
		Log.Debug("【BLELib】【BLELib】【Pairing】finish pairing");
		return res;
	}

	public virtual async Task ReceiveWait(IList<string> names, int timeout, Action<IList<string>> handler)
	{
		Log.Debug("【BLELib】【BLELib】【ReceiveWait】ReceiveWait start names=" + names?.JoinString(","));
		_IAdapter.DeviceAdvertised += _IAdapter_DeviceAdvertised;
		IsReceiveWait = true;
		ScanDeviceList = new List<ScanDevice>();
		ScanMode result = ScanMode.Balanced;
		Enum.TryParse<ScanMode>(Mode.ToString(), out result);
		_IAdapter.ScanMode = result;
		_IAdapter.ScanTimeout = timeout;
		_ScanHandler = handler;
		_ScanTargetPrefixs = names;
		string text = ((handler == null) ? "(null)" : "(not null)");
		Log.Trace(string.Format("【BLELib】【BLELib】【ReceiveWait】names={0}, timeout={1}, handler={2}", new object[3]
		{
			names?.JoinString(","),
			timeout,
			text
		}));
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
		Log.Debug("【BLELib】【BLELib】【ReceiveWait】ReceiveWait finish names=" + names?.JoinString(","));
	}

	public virtual async Task<object> ReceiveStart(string name, Action<int, int> handler, int timeout, IList<string> param = null, string serialNumber = "")
	{
		Log.Debug("【BLELib】【BLELib】【ReceiveStart】start receive device name : " + name);
		IBLEDevice deviceBuilder = _BLEManager.GetDevice(name);
		if (deviceBuilder == null)
		{
			return null;
		}
		object res = null;
		try
		{
			IDevice device = ((!string.IsNullOrEmpty(serialNumber)) ? (from x in ScanDeviceList
				where x.Name == name && x.SerialNumber == serialNumber
				select x.Device).FirstOrDefault() : (from x in ScanDeviceList
				where x.Name == name
				select x.Device).FirstOrDefault());
			if (device == null)
			{
				return null;
			}
			string text = ((handler == null) ? "(null)" : "not null");
			Log.Trace(string.Format("【BLELib】【BLELib】【ReceiveStart】name={0}, handler={1}, timout={2}, param={3}", name, text, timeout, param?.JoinString(",")));
			if (ReceiveDeviceList.Any((IBLEDevice x) => x.DeviceName == deviceBuilder.DeviceName))
			{
				return null;
			}
			deviceBuilder.InitializeDeviceInfo(device);
			ReceiveDeviceList.Add(deviceBuilder);
			deviceBuilder.StateChanged -= _BleDevice_StateChanged;
			deviceBuilder.StateChanged += _BleDevice_StateChanged;
			try
			{
				StatusChange(name, BLELibStatus.RCV_START);
				res = await deviceBuilder.ReciveStart(handler, timeout, param);
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLELib】【ReceiveStart】ステータス通知 or 受信開始 例外発生: {ex}");
				throw;
			}
			RemoveDevice(name);
		}
		catch (Exception ex2)
		{
			IBLEDevice iBLEDevice = ReceiveDeviceList.FirstOrDefault((IBLEDevice x) => x.DeviceName == deviceBuilder.DeviceName);
			if (iBLEDevice != null)
			{
				ReceiveDeviceList.Remove(iBLEDevice);
			}
			Log.Error($"【BLELib】【BLELib】【ReceiveStart】例外発生：{ex2}");
			StatusChange(name, BLELibStatus.RCV_ERR, ex2.Message);
		}
		Log.Debug("【BLELib】【BLELib】【ReceiveStart】finish receive device name : " + name);
		return res;
	}

	public virtual async Task ReceiveStop()
	{
		Log.Debug("【BLELib】【BLELib】【ReceiveStop】ReceiveStop start");
		_IAdapter.DeviceAdvertised -= _IAdapter_DeviceAdvertised;
		IsReceiveWait = false;
		for (int i = ReceiveDeviceList.Count - 1; i >= 0; i--)
		{
			if (ReceiveDeviceList[i] != null)
			{
				StatusChange(ReceiveDeviceList[i].DeviceName, BLELibStatus.RCV_STOP);
				await ReceiveDeviceList[i].ReceiveStop().ConfigureAwait(continueOnCapturedContext: false);
			}
		}
		StatusChange("", BLELibStatus.RCV_WAIT_STOP);
		await _IAdapter.StopScanningForDevicesAsync();
		_ScanHandler = null;
		Log.Debug("【BLELib】【BLELib】【ReceiveStop】ReceiveStop finish");
	}

	public async Task<bool> DeleteUser(string name, int timeout, IList<string> param)
	{
		Log.Debug("【BLELib】【BLELib】【DeleteUser】start delete user device name : " + name);
		if (param == null || param.Count == 0)
		{
			Log.Error($"【BLELib】【BLELib】【DeleteUser】param：{param?.Count}");
			return false;
		}
		IBLEDevice deviceBuilder = _BLEManager.GetDevice(name);
		if (deviceBuilder == null)
		{
			return false;
		}
		try
		{
			IDevice device = (from x in ScanDeviceList
				where x.Name == name
				select x.Device).FirstOrDefault();
			if (device == null)
			{
				return false;
			}
			Log.Trace(string.Format("【BLELib】【BLELib】【DeleteUser】name={0}, timout={1}, param={2}", new object[3]
			{
				name,
				timeout,
				param?.JoinString(",")
			}));
			if (ReceiveDeviceList.Any((IBLEDevice x) => x.DeviceName == deviceBuilder.DeviceName))
			{
				return false;
			}
			deviceBuilder.InitializeDeviceInfo(device);
			deviceBuilder.StateChanged += _BleDevice_StateChanged;
			ReceiveDeviceList.Add(deviceBuilder);
			StatusChange(name, BLELibStatus.USER_DELETE_START);
			return await deviceBuilder.DeleteUserByParam(timeout, param);
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLELib】【DeleteUser】例外発生：{ex}");
			StatusChange(name, BLELibStatus.USER_DELETE_ERR, ex.Message);
			return false;
		}
		finally
		{
			RemoveDevice(name);
		}
	}

	public async Task<IList<object>> GetFreeColor(string name, int timeout, IList<string> param)
	{
		Log.Debug("【BLELib】【BLELib】【GetFreeColor】start get free color device name : " + name);
		IBLEDevice deviceBuilder = _BLEManager.GetDevice(name);
		if (deviceBuilder == null)
		{
			return new List<object>();
		}
		try
		{
			IDevice device = (from x in ScanDeviceList
				where x.Name == name
				select x.Device).FirstOrDefault();
			if (device == null)
			{
				return new List<object>();
			}
			Log.Trace(string.Format("【BLELib】【BLELib】【GetFreeColor】name={0}, timout={1}, param={2}", new object[3]
			{
				name,
				timeout,
				param?.JoinString(",")
			}));
			if (ReceiveDeviceList.Any((IBLEDevice x) => x.DeviceName == deviceBuilder.DeviceName))
			{
				return new List<object>();
			}
			deviceBuilder.InitializeDeviceInfo(device);
			deviceBuilder.StateChanged += _BleDevice_StateChanged;
			ReceiveDeviceList.Add(deviceBuilder);
			StatusChange(name, BLELibStatus.GET_FREE_COLOR_START);
			return await deviceBuilder.GetFreeColor(timeout, param);
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLELib】【GetFreeColor】例外発生：{ex}");
			StatusChange(name, BLELibStatus.GET_FREE_COLOR_ERR, ex.Message);
			return new List<object>();
		}
		finally
		{
			RemoveDevice(name);
		}
	}

	public async Task<bool> RegisterUserColor(string name, int timeout, IList<string> param)
	{
		Log.Debug("【BLELib】【BLELib】【RegisterUserColor】start register user device name : " + name);
		IBLEDevice deviceBuilder = _BLEManager.GetDevice(name);
		if (deviceBuilder == null)
		{
			return false;
		}
		try
		{
			IDevice device = (from x in ScanDeviceList
				where x.Name == name
				select x.Device).FirstOrDefault();
			if (device == null)
			{
				return false;
			}
			Log.Trace(string.Format("【BLELib】【BLELib】【RegisterUserColor】name={0}, timout={1}, param={2}", new object[3]
			{
				name,
				timeout,
				param?.JoinString(",")
			}));
			if (ReceiveDeviceList.Any((IBLEDevice x) => x.DeviceName == deviceBuilder.DeviceName))
			{
				return false;
			}
			deviceBuilder.InitializeDeviceInfo(device);
			deviceBuilder.StateChanged += _BleDevice_StateChanged;
			ReceiveDeviceList.Add(deviceBuilder);
			StatusChange(name, BLELibStatus.REGISTER_USER_START);
			return await deviceBuilder.RegisterUserColor(timeout, param);
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLELib】【RegisterUserColor】例外発生：{ex}");
			StatusChange(name, BLELibStatus.REGISTER_USER_ERR, ex.Message);
			return false;
		}
		finally
		{
			RemoveDevice(name);
		}
	}

	protected void _BleDevice_StateChanged(object sender, BLELibStatusEventArgs e)
	{
		Log.Debug(string.Format("【BLELib】【BLELib】【_BleDevice_StateChanged】DeviceName={0}, Status={1}, Message={2}", new object[3] { e.DeviceName, e.Status, e.Message }));
		StatusChange(e.DeviceName, e.Status, e.Message);
		if ((e.Status == BLELibStatus.RCV_END || e.Status == BLELibStatus.PAIR_TIMEOUT || e.Status == BLELibStatus.RCV_TIMEOUT || e.Status == BLELibStatus.RCV_ERR) && (e.DeviceName.StartsWith("NBP-1BLE") || e.DeviceName.StartsWith("NMBP") || e.DeviceName.StartsWith("NSM-1BLE") || e.DeviceName.StartsWith("NT-100B")))
		{
			RemoveDevice(e.DeviceName);
		}
	}

	private void _CrossBluetoothLE_StateChanged(object sender, BluetoothStateChangedArgs e)
	{
		try
		{
			Log.Debug("【BLELib】【BLELib】【_CrossBluetoothLE_StateChanged】NewState=" + e?.NewState.ToString());
			switch (e?.NewState)
			{
			case BluetoothState.Unknown:
				StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED_UNKNOWN);
				break;
			case BluetoothState.Unavailable:
				StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED_UNAVAILABLE);
				break;
			case BluetoothState.Unauthorized:
				StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED_UNAUTHORIZED);
				break;
			case BluetoothState.TurningOn:
				StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED_TURNINGON);
				break;
			case BluetoothState.On:
				StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED_ON);
				break;
			case BluetoothState.TurningOff:
				StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED_TURNINGOFF);
				break;
			case BluetoothState.Off:
				StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED_OFF);
				break;
			}
			StatusChange("", BLELibStatus.ADAPTER_BLE_STATECHANGED);
		}
		catch (Exception ex)
		{
			Log.Error(ex, $"【BLELib】【BLELib】【_CrossBluetoothLE_StateChanged】例外発生 {ex}");
			throw;
		}
	}

	private void _IAdapter_DeviceDisconnected(object sender, DeviceEventArgs e)
	{
		Log.Debug("【BLELib】【BLELib】【_IAdapter_DeviceDisconnected】Name=" + e.Device.Name);
		StatusChange(e.Device.Name, BLELibStatus.ADAPTER_DEVICE_DISCONNECTED);
		RemoveDevice(e.Device.Name);
	}

	private void _IAdapter_DeviceConnectionLost(object sender, DeviceErrorEventArgs e)
	{
		Log.Debug("【BLELib】【BLELib】【_IAdapter_DeviceConnectionLost】Name=" + e.Device.Name);
		StatusChange(e.Device.Name, BLELibStatus.ADAPTER_DEVICE_CONNECTION_LOST);
		if (!e.Device.Name.StartsWith("NBP-1BLE") && !e.Device.Name.StartsWith("NMBP") && !e.Device.Name.StartsWith("NSM-1BLE") && !e.Device.Name.StartsWith("NT-100B") && !e.Device.Name.StartsWith("NBCM"))
		{
			RemoveDevice(e.Device.Name);
		}
	}

	private void _IAdapter_DeviceConnected(object sender, DeviceEventArgs e)
	{
		Log.Debug("【BLELib】【BLELib】【_IAdapter_DeviceConnected】Name=" + e.Device.Name);
		StatusChange(e.Device.Name, BLELibStatus.ADAPTER_DEVICE_CONNECTED);
	}

	private void _IAdapter_ScanTimeoutElapsed(object sender, EventArgs e)
	{
		Log.Debug("【BLELib】【BLELib】【_IAdapter_ScanTimeoutElapsed】ScanTimeout");
		ScanStop().ContinueWith(delegate
		{
		});
		if (IsReceiveWait)
		{
			StatusChange("", BLELibStatus.RCV_WAIT_TIMEOUT);
		}
		else
		{
			StatusChange("", BLELibStatus.SCAN_TIMEOUT);
		}
	}

	protected virtual void _IAdapter_DeviceAdvertised(object sender, DeviceEventArgs e)
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
			Log.Debug("【BLELib】【BLELib】【_IAdapter_DeviceAdvertised】item.Type = " + advertisementRecord2.Type.ToString() + ", item.Data = " + advertisementRecord2.Data.JoinString(","));
			if (advertisementRecord2.Type == AdvertisementRecordType.CompleteLocalName)
			{
				Log.Debug("【BLELib】【BLELib】【_IAdapter_DeviceAdvertised】CompleteLocalName=" + ToAsciiStringFromBytes(advertisementRecord2.Data));
			}
		}
		if (!string.IsNullOrEmpty(device.Name))
		{
			if (ScanDeviceList.All((ScanDevice x) => x.Device.Id != device.Id))
			{
				Log.Debug($"【BLELib】【BLELib】【_IAdapter_DeviceAdvertised】Name = {e.Device.Name}, Id = {e.Device.Id}, Rssi = {e.Device.Rssi}, State = {e.Device.State.ToString()}, SerialNumber={text}");
				ScanDeviceList.Add(new ScanDevice
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
			}
		}
	}

	protected string ToAsciiStringFromBytes(byte[] bytes)
	{
		string text = string.Empty;
		if (bytes != null)
		{
			for (int i = 0; i < bytes.Length; i++)
			{
				string text2 = text;
				char c = (char)bytes[i];
				text = text2 + c;
			}
		}
		return text;
	}

	protected virtual void Dispose(bool disposing)
	{
		if (!disposedValue)
		{
			if (disposing)
			{
				ClearReceiveDeviceList();
				_CrossBluetoothLE.StateChanged -= _CrossBluetoothLE_StateChanged;
				_IAdapter.DeviceAdvertised -= _IAdapter_DeviceAdvertised;
				_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
				_IAdapter.DeviceConnectionLost -= _IAdapter_DeviceConnectionLost;
				_IAdapter.DeviceDisconnected -= _IAdapter_DeviceDisconnected;
				_IAdapter.ScanTimeoutElapsed -= _IAdapter_ScanTimeoutElapsed;
			}
			disposedValue = true;
		}
	}

	public void Dispose()
	{
		Dispose(disposing: true);
	}

	protected virtual void StartBondingCheck(EventHandler<DeviceBondStateChangedEventArgs> handler, IDevice device = null)
	{
		handler?.Invoke(new object(), new DeviceBondStateChangedEventArgs
		{
			State = DeviceBondState.Bonded,
			Device = device
		});
	}

	protected virtual void StopBondingCheck(EventHandler<DeviceBondStateChangedEventArgs> handler)
	{
	}

	public virtual async Task ScanPause()
	{
		await Task.Delay(1);
	}

	public virtual async Task ScanPauseRestart(int msec)
	{
		await Task.Delay(1);
	}
}

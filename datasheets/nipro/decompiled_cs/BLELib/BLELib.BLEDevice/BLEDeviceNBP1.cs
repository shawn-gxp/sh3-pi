using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using BLELib.BLEDevice.Record;
using BLELib.Common;
using Plugin.BLE;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Xamarin.Essentials;

namespace BLELib.BLEDevice;

public class BLEDeviceNBP1 : IBLEDevice
{
	public static class BloodPressureServiceConstants
	{
		public const string SERVICE_UUID = "00001810-0000-1000-8000-00805F9B34FB";

		public const string BLOOD_PRESSURE_MEASUREMENT = "00002A35-0000-1000-8000-00805F9B34FB";

		public const string DATE_TIME = "00002A08-0000-1000-8000-00805F9B34FB";
	}

	public static class DeviceInformationServiceConstants
	{
		public const string SERVICE_UUID = "0000180A-0000-1000-8000-00805F9B34FB";

		public const string SYSTEM_ID = "00002A23-0000-1000-8000-00805F9B34FB";

		public const string MODEL_NUMBER = "00002A24-0000-1000-8000-00805F9B34FB";

		public const string SERIAL_NUMBER = "00002A25-0000-1000-8000-00805F9B34FB";

		public const string FIRMWARE_REVISION = "00002A26-0000-1000-8000-00805F9B34FB";

		public const string HARDWARE_REVISION = "00002A27-0000-1000-8000-00805F9B34FB";

		public const string SOFTWARE_REVISION = "00002A28-0000-1000-8000-00805F9B34FB";

		public const string MANUFACTURE_NAME = "00002A29-0000-1000-8000-00805F9B34FB";

		public const string REGISTRATION_CERTIFICATION_DATA = "00002A2A-0000-1000-8000-00805F9B34FB";
	}

	public static class BatteryServiceConstants
	{
		public const string SERVICE_UUID = "0000180F-0000-1000-8000-00805F9B34FB";

		public const string BATTERY_LEVEL = "00002A19-0000-1000-8000-00805F9B34FB";
	}

	private ILoggingService Log = LogManager.GetLogger();

	private static readonly int PAIRING_REQUEST_TIMEOUT = 30000;

	public const string COMMON_UUID_TAIL = "-0000-1000-8000-00805F9B34FB";

	public const string PRIMARY_SERVICE_UUID = "00001810-0000-1000-8000-00805F9B34FB";

	private static IAdapter _IAdapter = CrossBluetoothLE.Current.Adapter;

	private CancellationTokenSource _CancelToken;

	private BloodPressureResult _Result;

	public string DeviceName => "NBP-1BLE";

	protected ManualResetEventSlim DataReceiveCompleteEvent { get; set; }

	protected ManualResetEventSlim DeviceConnectedEvent { get; set; }

	protected ManualResetEventSlim DeviceBondedEvent { get; set; }

	public IDevice Device { get; set; }

	public event EventHandler<BLELibStatusEventArgs> StateChanged;

	public object CreateDevice(IDevice device)
	{
		return new BLEDeviceNBP1
		{
			Device = device,
			DataReceiveCompleteEvent = new ManualResetEventSlim(initialState: false),
			DeviceConnectedEvent = new ManualResetEventSlim(initialState: false),
			DeviceBondedEvent = new ManualResetEventSlim(initialState: false)
		};
	}

	public void InitializeDeviceInfo(IDevice device)
	{
		Device = device;
		DataReceiveCompleteEvent = new ManualResetEventSlim(initialState: false);
		DeviceConnectedEvent = new ManualResetEventSlim(initialState: false);
		DeviceBondedEvent = new ManualResetEventSlim(initialState: false);
	}

	private void _IAdapter_DeviceConnectionLost(object sender, DeviceErrorEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBP1】【_IAdapter_DeviceConnectionLost】Device.Name={0}, Device.Id={1}", new object[2]
		{
			e.Device?.Name,
			e.Device?.Id
		}));
		if (!(Device.Name != e.Device.Name) && DataReceiveCompleteEvent != null)
		{
			DataReceiveCompleteEvent.Set();
			_IAdapter.DeviceConnectionLost -= _IAdapter_DeviceConnectionLost;
		}
	}

	public async Task<IList<string>> Pairing(IList<string> param = null)
	{
		Log.Info("【BLELib】【BLEDeviceNBP1】【Pairing】start pairing");
		string serialNumber = "";
		IList<string> result;
		try
		{
			_CancelToken = new CancellationTokenSource();
			DeviceConnectedEvent.Reset();
			_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
			Log.Info(string.Format("【BLELib】【BLEDeviceNBP1】【Pairing】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			if (_IAdapter.IsScanning)
			{
				Log.Info("【BLELib】【BLEDeviceNBP1】【Pairing】scan already processing, stop scan.");
				await _IAdapter.StopScanningForDevicesAsync();
			}
			ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
			await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _CancelToken.Token);
			Task pairingTask = Task.Run(async delegate
			{
				DeviceConnectedEvent.Wait();
				if (_CancelToken.IsCancellationRequested)
				{
					Log.Info("【BLELib】【BLEDeviceNBP1】【Pairing】CancelToken was cancelled, finish pairing.");
				}
				else
				{
					using IService service = await Device.GetServiceAsync(Guid.Parse("0000180A-0000-1000-8000-00805F9B34FB"));
					ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A25-0000-1000-8000-00805F9B34FB"));
					Log.Info("【BLELib】【BLEDeviceNBP1】【Pairing】serial number ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
					DeviceBondedEvent.Wait();
					byte[] array = await characteristic.ReadAsync();
					serialNumber = ToAsciiStringFromBytes(array);
					Log.Info("【BLELib】【BLEDeviceNBP1】【Pairing】serial number ReadAsync:Result=" + BitConverter.ToString(array) + ":SerialNumber=" + serialNumber);
				}
			}, _CancelToken.Token);
			if (await Task.WhenAny(pairingTask, Task.Delay(PAIRING_REQUEST_TIMEOUT)) != pairingTask)
			{
				Log.Info("【BLELib】【BLEDeviceNBP1】【Pairing】ペアリング失敗：PAIR_TIMEOUT");
				await StatusChange(Device.Name, BLELibStatus.PAIR_TIMEOUT);
				result = new string[3] { "", "", "" };
			}
			else
			{
				result = new string[3]
				{
					Device.Name,
					Device.Id.ToString(),
					serialNumber
				};
			}
		}
		catch (TaskCanceledException ex)
		{
			Log.Warn($"【BLELib】【BLEDeviceNBP1】【Pairing】TaskCanceledException発生：{ex}");
			goto IL_05ab;
		}
		catch (OperationCanceledException ex2)
		{
			Log.Warn($"【BLELib】【BLEDeviceNBP1】【Pairing】OperationCanceledException発生：{ex2}");
			goto IL_05ab;
		}
		catch (Exception ex3)
		{
			Log.Error($"【BLELib】【BLEDeviceNBP1】【Pairing】例外発生：{ex3}");
			await StatusChange(Device.Name, BLELibStatus.PAIR_ERR, ex3.Message);
			goto IL_05ab;
		}
		finally
		{
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
			_IAdapter.DisconnectDeviceAsync(Device);
			await Task.Delay(100);
		}
		return result;
		IL_05ab:
		Log.Info("【BLELib】【BLEDeviceNBP1】【Pairing】finish pairing");
		return new string[3] { "", "", "" };
	}

	public async Task<object> ReciveStart(Action<int, int> handler, int timeout, IList<string> param = null)
	{
		ICharacteristic characteristicMeasurement = null;
		object result;
		try
		{
			_Result = new BloodPressureResult();
			_Result.Result = new List<BloodPressureResult.BloodPressureMeasureRecord>();
			DeviceConnectedEvent.Reset();
			DataReceiveCompleteEvent.Reset();
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
			_CancelToken = new CancellationTokenSource();
			Log.Info(string.Format("【BLELib】【BLEDeviceNBP1】【ReciveStart】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
			await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _CancelToken.Token);
			Task receiveTask = Task.Run(async delegate
			{
				DeviceConnectedEvent.Wait();
				if (_CancelToken.IsCancellationRequested)
				{
					Log.Info("【BLELib】【BLEDeviceNBP1】【ReciveStart】CancelToken was cancelled, finish pairing.");
				}
				else
				{
					using IService service = await Device.GetServiceAsync(Guid.Parse("00001810-0000-1000-8000-00805F9B34FB"));
					ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A08-0000-1000-8000-00805F9B34FB"));
					byte[] datetimeArray = GetDateTimeByteArray();
					Log.Info("【BLELib】【BLEDeviceNBP1】【ReciveStart】メーター日時 WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString() + ", data=" + BitConverter.ToString(datetimeArray));
					await Task.Delay(1000);
					await MainThread.InvokeOnMainThreadAsync(async () => await characteristic.WriteAsync(datetimeArray));
					Log.Info("【BLELib】【BLEDeviceNBP1】【ReciveStart】メーター日時 ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
					byte[] array = await characteristic.ReadAsync();
					Log.Info("【BLELib】【BLEDeviceNBP1】【ReciveStart】メーター日時 ReadAsync:Result=" + BitConverter.ToString(array));
				}
			}, _CancelToken.Token);
			if (await Task.WhenAny(receiveTask, Task.Delay(timeout)) != receiveTask)
			{
				Log.Info("【BLELib】【BLEDeviceNBP1】【RceiveStart】ReceiveStart失敗：RCV_TIMEOUT");
				await StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
				result = new string[3] { "", "", "" };
			}
			else
			{
				characteristicMeasurement = await CharacteristicAsyncOf(Device, "00001810-0000-1000-8000-00805F9B34FB", "00002A35-0000-1000-8000-00805F9B34FB");
				characteristicMeasurement.ValueUpdated -= BloodPressureMeasurement_CharacteristicValueUpdateHandler;
				characteristicMeasurement.ValueUpdated += BloodPressureMeasurement_CharacteristicValueUpdateHandler;
				await characteristicMeasurement.StartUpdatesAsync();
				Task.Run(delegate
				{
					_IAdapter.DeviceConnectionLost -= _IAdapter_DeviceConnectionLost;
					_IAdapter.DeviceConnectionLost += _IAdapter_DeviceConnectionLost;
				});
				result = await Task.Run((Func<Task<object>>)async delegate
				{
					if (DataReceiveCompleteEvent.Wait(timeout))
					{
						await StatusChange(Device.Name, BLELibStatus.RCV_END);
					}
					else
					{
						await StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
					}
					return _Result;
				});
			}
		}
		catch (TaskCanceledException ex)
		{
			Log.Warn($"【BLELib】【BLEDeviceNBP1】【ReciveStart】TaskCanceledException発生：{ex}");
			goto IL_0742;
		}
		catch (OperationCanceledException ex2)
		{
			Log.Warn($"【BLELib】【BLEDeviceNBP1】【ReciveStart】OperationCanceledException発生：{ex2}");
			goto IL_0742;
		}
		catch (Exception ex3)
		{
			Log.Error($"【BLELib】【BLEDeviceNBP1】【ReciveStart】例外発生：{ex3}");
			await StatusChange(Device.Name, BLELibStatus.RCV_ERR);
			goto IL_0742;
		}
		finally
		{
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			if (characteristicMeasurement != null)
			{
				characteristicMeasurement.ValueUpdated -= BloodPressureMeasurement_CharacteristicValueUpdateHandler;
			}
			if (Device != null)
			{
				Log.Info(string.Format("【BLELib】【BLEDeviceNBP1】【ReciveStart】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				_IAdapter.DisconnectDeviceAsync(Device);
				await Task.Delay(100);
			}
		}
		return result;
		IL_0742:
		return null;
	}

	private void _IAdapter_DeviceConnected(object sender, DeviceEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBP1】【_IAdapter_DeviceConnected】Device.Name={0}, Device.Id={1}", new object[2]
		{
			e.Device?.Name,
			e.Device?.Id
		}));
		if (Device == null || !(Device.Name != e.Device?.Name))
		{
			DeviceConnectedEvent.Set();
		}
	}

	public async Task ReceiveStop()
	{
		if (_CancelToken != null)
		{
			_CancelToken.Cancel();
		}
		if (Device != null)
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceNBP1】【ReceiveStop】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			await _IAdapter.DisconnectDeviceAsync(Device);
		}
	}

	public void Cancel()
	{
		if (_CancelToken != null)
		{
			Log.Info("【BLELib】【BLEDeviceNBP1】【Cancel】Cancel all tasks.");
			_CancelToken.Cancel();
		}
	}

	public Task<bool> DeleteUserByParam(int timeout, IList<string> param)
	{
		throw new NotImplementedException();
	}

	public Task<IList<object>> GetFreeColor(int timeout, IList<string> param)
	{
		throw new NotImplementedException();
	}

	public Task<bool> RegisterUserColor(int timeout, IList<string> param)
	{
		throw new NotImplementedException();
	}

	private void BloodPressureMeasurement_CharacteristicValueUpdateHandler(object sender, CharacteristicUpdatedEventArgs e)
	{
		try
		{
			ICharacteristic characteristic = e.Characteristic;
			byte[] value = characteristic.Value;
			characteristic.Uuid.ToUpper();
			Log.Info("【BLELib】【BLEDeviceNBP1】【BloodPressureMeasurement_CharacteristicValueUpdateHandler】CharacteristicValueUpdateHandle:sender=" + (sender as ICharacteristic).Uuid + ", buffer=" + BitConverter.ToString(e.Characteristic.Value));
			if (value != null && value.Length != 0)
			{
				BloodPressureResult.BloodPressureMeasureRecord bloodPressureMeasureRecord = CreateBloodPressureMeasureRecordWithReceivedDataBytes(value);
				Log.Info(string.Format("【BLELib】【BLEDeviceNBP1】【BloodPressureMeasurement_CharacteristicValueUpdateHandler】CharacteristicValueUpdateHandle:{0}:TimeStamp={1}, SBP={2}, DBP={3}, MAP={4}, PulseRate={5}", Device.Name, bloodPressureMeasureRecord.TimeStamp.ToString("yyyy/MM/dd H:mm:ss"), bloodPressureMeasureRecord.SBP, bloodPressureMeasureRecord.DBP, bloodPressureMeasureRecord.MAP, bloodPressureMeasureRecord.PulseRate));
				_Result.Result.Add(bloodPressureMeasureRecord);
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLEDeviceNBP1】【BloodPressureMeasurement_CharacteristicValueUpdateHandler】例外発生：{ex}");
		}
	}

	public void BondStatusBroadcastReceiver_BondStateChanged(object sender, DeviceBondStateChangedEventArgs e)
	{
		Log.Info("【BLELib】【BLEDeviceNBP1】【BondStatusBroadcastReceiver_BondStateChanged】sender=" + sender?.ToString() + ",e.Device=" + e.Device.Name + ", e.State=" + e.State);
		if (e.Device.Name.StartsWith(Device.Name.Trim()) && e.State == DeviceBondState.Bonded)
		{
			DeviceBondedEvent.Set();
		}
	}

	private async Task StatusChange(string deviceName, BLELibStatus status, string message = "")
	{
		if (this.StateChanged != null)
		{
			await Task.Run(delegate
			{
				this.StateChanged(this, new BLELibStatusEventArgs(deviceName, status));
			});
		}
	}

	protected async Task<ICharacteristic> CharacteristicAsyncOf(IDevice device, string serviceUuid, string characteristicUuid)
	{
		ICharacteristic characteristic = null;
		int retryCount = 0;
		while (retryCount < 3)
		{
			retryCount++;
			try
			{
				IService service = await device.GetServiceAsync(Guid.Parse(serviceUuid));
				if (service == null)
				{
					continue;
				}
				characteristic = await service.GetCharacteristicAsync(Guid.Parse(characteristicUuid));
				goto IL_0132;
			}
			catch (Exception)
			{
				goto IL_0132;
			}
			IL_0132:
			if (characteristic != null)
			{
				break;
			}
		}
		return characteristic;
	}

	private BloodPressureResult.BloodPressureMeasureRecord CreateBloodPressureMeasureRecordWithReceivedDataBytes(byte[] data)
	{
		double pulseRate = 0.0;
		int num = 0;
		int num2 = 7;
		bool flag = false;
		bool flag2 = false;
		bool flag3 = false;
		bool flag4 = false;
		bool flag5 = false;
		flag = (data[num] & 1) == 0;
		if ((data[num] & 2) != 0)
		{
			flag2 = true;
		}
		if ((data[num] & 4) != 0)
		{
			flag3 = true;
		}
		if ((data[num] & 8) != 0)
		{
			flag4 = true;
		}
		if ((data[num] & 0x10) != 0)
		{
			flag5 = true;
		}
		double sBP;
		double dBP;
		double mAP;
		if (flag)
		{
			num++;
			sBP = ToSFloat(new byte[2]
			{
				data[num],
				data[num + 1]
			});
			num += 2;
			dBP = ToSFloat(new byte[2]
			{
				data[num],
				data[num + 1]
			});
			num += 2;
			mAP = ToSFloat(new byte[2]
			{
				data[num],
				data[num + 1]
			});
			num += 2;
		}
		else
		{
			num += 6;
			sBP = ToSFloat(new byte[2]
			{
				data[num],
				data[num + 1]
			});
			num += 2;
			dBP = ToSFloat(new byte[2]
			{
				data[num],
				data[num + 1]
			});
			num += 2;
			mAP = ToSFloat(new byte[2]
			{
				data[num],
				data[num + 1]
			});
			num += 2;
		}
		DateTime timeStamp;
		if (flag2)
		{
			byte[] array = new byte[num2];
			Array.Copy(data, num, array, 0, num2);
			timeStamp = ToDateTime(array);
			num += 7;
		}
		else
		{
			timeStamp = DateTime.MinValue;
		}
		if (flag3)
		{
			pulseRate = ToSFloat(new byte[2]
			{
				data[num],
				data[num + 1]
			});
			num += 2;
		}
		if (flag4)
		{
			num++;
		}
		return new BloodPressureResult.BloodPressureMeasureRecord
		{
			TimeStamp = timeStamp,
			SBP = sBP,
			DBP = dBP,
			MAP = mAP,
			PulseRate = pulseRate
		};
	}

	private DateTime ToDateTime(byte[] value)
	{
		try
		{
			short year = BitConverter.ToInt16(value, 0);
			return new DateTime(year, value[2], value[3], value[4], value[5], value[6]);
		}
		catch (Exception)
		{
			return DateTime.MinValue;
		}
	}

	private float ToSFloat(byte[] value)
	{
		if (value.Length != 2)
		{
			throw new ArgumentException();
		}
		byte value2 = value[0];
		byte value3 = value[1];
		int num = unsignedToSigned(ToInt(value2) + ((ToInt(value3) & 0xF) << 8), 12);
		int num2 = unsignedToSigned(ToInt(value3) >> 4, 4);
		return (float)((double)num * Math.Pow(10.0, num2));
	}

	private int ToInt(byte value)
	{
		return value & 0xFF;
	}

	private int unsignedToSigned(int unsigned, int size)
	{
		if ((unsigned & (1 << size - 1)) != 0)
		{
			unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
		}
		return unsigned;
	}

	private static byte[] GetDateTimeByteArray()
	{
		DateTime now = DateTime.Now;
		short value = (short)now.Year;
		byte b = (byte)now.Month;
		byte b2 = (byte)now.Day;
		byte b3 = (byte)now.Hour;
		byte b4 = (byte)now.Minute;
		byte b5 = (byte)now.Second;
		byte[] bytes = BitConverter.GetBytes(value);
		return new byte[7]
		{
			bytes[0],
			bytes[1],
			b,
			b2,
			b3,
			b4,
			b5
		};
	}

	protected string ToStringFromBytes(byte[] bytes)
	{
		if (bytes != null)
		{
			return BitConverter.ToString(bytes);
		}
		return "";
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
}

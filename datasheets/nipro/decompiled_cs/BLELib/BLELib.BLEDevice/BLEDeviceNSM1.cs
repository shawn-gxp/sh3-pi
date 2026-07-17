using System;
using System.Collections.Generic;
using System.Globalization;
using System.Threading;
using System.Threading.Tasks;
using BLELib.Common;
using Plugin.BLE;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Xamarin.Essentials;

namespace BLELib.BLEDevice;

public class BLEDeviceNSM1 : IBLEDevice
{
	public class BodyTemperatureResult
	{
		public class BodyTemperatureMeasureRecord
		{
			public DateTime TimeStamp { get; set; }

			public double Temperture { get; set; }

			public override string ToString()
			{
				return string.Format(TimeStamp.ToString() + ", " + Temperture);
			}
		}

		public IList<BodyTemperatureMeasureRecord> Result { get; set; }
	}

	public class ThermometrServiceConstants
	{
		public const string SERVICE_UUID = "00001809-0000-1000-8000-00805F9B34FB";

		public const string TEMPERATURE_MEASUREMENT = "00002A1C-0000-1000-8000-00805F9B34FB";

		public const string TEMPERATURE_TYPE = "00002A1D-0000-1000-8000-00805F9B34FB";

		public const string DATE_TIME = "00002A08-0000-1000-8000-00805F9B34FB";
	}

	public class DeviceInformationServiceConstants
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

	public class BatteryServiceConstants
	{
		public const string SERVICE_UUID = "0000180F-0000-1000-8000-00805F9B34FB";

		public const string BATTERY_LEVEL = "00002A19-0000-1000-8000-00805F9B34FB";
	}

	public class CustomServiceConstants
	{
		public const string SERVICE_UUID = "233BF000-5A34-1B6D-975C-000D5690ABE4";

		public const string CUSTOM_CHARACTERISTIC = "233BF001-5A34-1B6D-975C-000D5690ABE4";
	}

	private ILoggingService Log = LogManager.GetLogger();

	private static readonly int PAIRING_REQUEST_TIMEOUT = 30000;

	protected const string COMMON_UUID_TAIL = "-0000-1000-8000-00805F9B34FB";

	public const string PRIMARY_SERVICE_UUID = "00001809-0000-1000-8000-00805F9B34FB";

	public static readonly byte[] SET_TIME_AS_HEADER = new byte[3] { 8, 1, 1 };

	public static readonly byte[] DISCONNECT = new byte[3] { 2, 1, 3 };

	public static readonly byte[] READ_SET_TIME_AND_DATE = new byte[3] { 2, 0, 4 };

	public static readonly byte[] UNPAIR = new byte[3] { 2, 1, 16 };

	public static readonly byte[] DELETE_ALL_MEMORY = new byte[3] { 2, 1, 18 };

	public static readonly byte[] SET_BUFFER_SIZE_AS_HEADER = new byte[3] { 3, 1, 166 };

	public static readonly byte[] SET_BUFFER_SIZE_AS_VALUE_FOR_ZERO = new byte[1];

	public static readonly byte[] SET_BUFFER_SIZE_AS_VALUE_FOR_NINETY = new byte[1] { 1 };

	public static readonly byte[] READ_DEVICE_SETTINGS = new byte[3] { 2, 0, 219 };

	public static readonly byte[] REQUEST_TO_SEND_DATA_IN_BUFFER = new byte[3] { 2, 0, 225 };

	private static IAdapter _IAdapter = CrossBluetoothLE.Current.Adapter;

	private CancellationTokenSource _CancelToken;

	private BodyTemperatureResult _Temperature;

	private static readonly Dictionary<int, float> reservedValues = new Dictionary<int, float>
	{
		{
			2046,
			1f / 0f
		},
		{
			2047,
			0f / 0f
		},
		{
			2048,
			0f / 0f
		},
		{
			2049,
			0f / 0f
		},
		{
			2050,
			-1f / 0f
		}
	};

	protected ManualResetEventSlim DataReceiveCompleteEvent { get; set; }

	protected ManualResetEventSlim DeviceConnectedEvent { get; set; }

	protected ManualResetEventSlim DeviceBondedEvent { get; set; }

	public IDevice Device { get; set; }

	public string DeviceName => "NSM-1BLE";

	public event EventHandler<BLELibStatusEventArgs> StateChanged;

	public object CreateDevice(IDevice device)
	{
		return new BLEDeviceNSM1
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

	public async Task<IList<string>> Pairing(IList<string> param = null)
	{
		Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】start pairing");
		try
		{
			_ = 5;
			IList<string> result;
			try
			{
				string serialNumber = "";
				_CancelToken = new CancellationTokenSource();
				Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【Pairing】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
				DeviceConnectedEvent.Reset();
				if (_IAdapter.IsScanning)
				{
					Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】scan already processing, stop scan.");
					await _IAdapter.StopScanningForDevicesAsync();
				}
				ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
				await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _CancelToken.Token);
				Log.Info($"【BLELib】【BLEDeviceNSM1】【Pairing】ConnectToDeviceAsync:Device.State={Device.State}");
				Task pairingTask = Task.Run(async delegate
				{
					DeviceConnectedEvent.Wait();
					if (_CancelToken.IsCancellationRequested)
					{
						Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】CancelToken was cancelled, finish pairing.");
					}
					else
					{
						using IService service2 = await Device.GetServiceAsync(Guid.Parse("0000180A-0000-1000-8000-00805F9B34FB"));
						ICharacteristic characteristic2 = await service2.GetCharacteristicAsync(Guid.Parse("00002A25-0000-1000-8000-00805F9B34FB"));
						Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】Serial Number ReadAsync:Service=" + service2.Id.ToString() + ", Characteristic=" + characteristic2.Id.ToString());
						DeviceBondedEvent.Wait();
						byte[] array = await characteristic2.ReadAsync(_CancelToken.Token);
						serialNumber = ToAsciiStringFromBytes(array);
						Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】Serial Number ReadAsync:Result=" + BitConverter.ToString(array) + ":SerialNumber=" + serialNumber);
					}
				}, _CancelToken.Token);
				if (await Task.WhenAny(pairingTask, Task.Delay(PAIRING_REQUEST_TIMEOUT)) != pairingTask)
				{
					Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】ペアリング失敗：PAIR_TIMEOUT");
					await StatusChange(Device.Name, BLELibStatus.PAIR_TIMEOUT);
					result = new string[3] { "", "", "" };
				}
				else
				{
					using (IService service = await Device.GetServiceAsync(Guid.Parse("233BF000-5A34-1B6D-975C-000D5690ABE4")))
					{
						ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("233BF001-5A34-1B6D-975C-000D5690ABE4"));
						Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】disconnect WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString() + ", data=" + BitConverter.ToString(DISCONNECT));
						MainThread.InvokeOnMainThreadAsync(() => characteristic.WriteAsync(DISCONNECT));
					}
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
				Log.Warn($"【BLELib】【BLEDeviceNSM1】【Pairing】TaskCanceledException発生：{ex}");
				goto end_IL_0054;
			}
			catch (OperationCanceledException ex2)
			{
				Log.Warn($"【BLELib】【BLEDeviceNSM1】【Pairing】OperationCanceledException発生：{ex2}");
				goto end_IL_0054;
			}
			catch (Exception ex3)
			{
				Log.Error($"【BLELib】【BLEDeviceNSM1】【Pairing】例外発生：{ex3}");
				StatusChange(Device.Name, BLELibStatus.PAIR_ERR, ex3.Message);
				goto end_IL_0054;
			}
			return result;
			end_IL_0054:;
		}
		finally
		{
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
			Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【Pairing】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			_IAdapter.DisconnectDeviceAsync(Device);
			await Task.Delay(100);
		}
		Log.Info("【BLELib】【BLEDeviceNSM1】【Pairing】finish pairing");
		return new string[3] { "", "", "" };
	}

	private void _CrossBluetoothLE_StateChanged(object sender, BluetoothStateChangedArgs e)
	{
		throw new NotImplementedException();
	}

	public async Task<object> ReciveStart(Action<int, int> handler, int timeout, IList<string> param = null)
	{
		ICharacteristic characteristicTmperature = null;
		object result;
		try
		{
			_Temperature = new BodyTemperatureResult();
			_Temperature.Result = new List<BodyTemperatureResult.BodyTemperatureMeasureRecord>();
			DeviceConnectedEvent.Reset();
			DataReceiveCompleteEvent.Reset();
			_CancelToken = new CancellationTokenSource();
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
			Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【ReciveStart】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
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
					Log.Info("【BLELib】【BLEDeviceNSM1】【ReciveStart】CancelToken was cancelled, finish pairing.");
				}
				else
				{
					using IService service = await Device.GetServiceAsync(Guid.Parse("00001809-0000-1000-8000-00805F9B34FB"));
					ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A08-0000-1000-8000-00805F9B34FB"));
					byte[] datetimeArray = GetDateTimeByteArray();
					Log.Info("【BLELib】【BLEDeviceNSM1】【ReciveStart】メーター日時 WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString() + ", data=" + BitConverter.ToString(datetimeArray));
					await Task.Delay(1000);
					await MainThread.InvokeOnMainThreadAsync(async () => await characteristic.WriteAsync(datetimeArray));
					Log.Info("【BLELib】【BLEDeviceNSM1】【ReciveStart】メーター日時 ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
					byte[] array = await characteristic.ReadAsync();
					Log.Info("【BLELib】【BLEDeviceNSM1】【ReciveStart】メーター日時 ReadAsync:Result=" + BitConverter.ToString(array));
				}
			}, _CancelToken.Token);
			if (await Task.WhenAny(receiveTask, Task.Delay(timeout)) != receiveTask)
			{
				Log.Info("【BLELib】【BLEDeviceNSM1】【RceiveStart】ReceiveStart失敗：RCV_TIMEOUT");
				await StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
				result = new string[3] { "", "", "" };
			}
			else
			{
				characteristicTmperature = await CharacteristicAsyncOf(Device, "00001809-0000-1000-8000-00805F9B34FB", "00002A1C-0000-1000-8000-00805F9B34FB");
				characteristicTmperature.ValueUpdated -= ThermometerMeasurement_CharacteristicValueUpdateHandler;
				characteristicTmperature.ValueUpdated += ThermometerMeasurement_CharacteristicValueUpdateHandler;
				await characteristicTmperature.StartUpdatesAsync();
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
					return _Temperature;
				});
			}
		}
		catch (TaskCanceledException ex)
		{
			Log.Warn($"【BLELib】【BLEDeviceNSM1】【ReciveStart】TaskCanceledException発生：{ex}");
			goto IL_0739;
		}
		catch (OperationCanceledException ex2)
		{
			Log.Warn($"【BLELib】【BLEDeviceNSM1】【ReciveStart】OperationCanceledException発生：{ex2}");
			goto IL_0739;
		}
		catch (Exception ex3)
		{
			Log.Error($"【BLELib】【BLEDeviceNSM1】【ReciveStart】例外発生：{ex3}");
			await StatusChange(Device.Name, BLELibStatus.RCV_ERR, ex3.Message);
			goto IL_0739;
		}
		finally
		{
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			if (characteristicTmperature != null)
			{
				characteristicTmperature.ValueUpdated -= ThermometerMeasurement_CharacteristicValueUpdateHandler;
			}
			Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【ReciveStart】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			_IAdapter.DisconnectDeviceAsync(Device);
			await Task.Delay(100);
		}
		return result;
		IL_0739:
		return null;
	}

	public async Task ReceiveStop()
	{
		if (_CancelToken != null)
		{
			_CancelToken.Cancel();
		}
		if (Device != null)
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【ReceiveStop】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
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
			Log.Info("【BLELib】【BLEDeviceNSM1】【Cancel】Cancel all tasks.");
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

	private void _IAdapter_DeviceConnected(object sender, DeviceEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【_IAdapter_DeviceConnected】DeviceConnected:Device.Name={0}, Device.Id={1}", new object[2]
		{
			e.Device?.Name,
			e.Device?.Id
		}));
		if (Device == null || !(Device.Name != e.Device?.Name))
		{
			DeviceConnectedEvent.Set();
		}
	}

	private void _IAdapter_DeviceConnectionLost(object sender, DeviceErrorEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【_IAdapter_DeviceConnectionLost】DeviceConnectionLost:Device.Name={0}, Device.Id={1}", new object[2]
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

	public void BondStatusBroadcastReceiver_BondStateChanged(object sender, DeviceBondStateChangedEventArgs e)
	{
		Log.Info("【BLELib】【BLEDeviceNSM1】【BondStatusBroadcastReceiver_BondStateChanged】sender=" + sender?.ToString() + ",e.Device=" + e.Device.Name + ", e.State=" + e.State);
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
				this.StateChanged(this, new BLELibStatusEventArgs(deviceName, status, message));
			});
		}
	}

	private void ThermometerMeasurement_CharacteristicValueUpdateHandler(object sender, CharacteristicUpdatedEventArgs e)
	{
		try
		{
			ICharacteristic characteristic = e.Characteristic;
			byte[] value = characteristic.Value;
			string text = characteristic.Uuid.ToUpper();
			Log.Info("【BLELib】【BLEDeviceNSM1】【ThermometerMeasurement_CharacteristicValueUpdateHandler】sender=" + (sender as ICharacteristic).Uuid + ", buffer=" + BitConverter.ToString(e.Characteristic.Value));
			int num = 0;
			string[] array = text.Split(new char[1] { '-' });
			if (array != null && array.Length != 0)
			{
				num = int.Parse(array[0], NumberStyles.AllowHexSpecifier);
			}
			if (num == 10780 && value != null && value.Length >= 12)
			{
				sbyte b = (sbyte)(value[4] & 0xFF);
				float num2 = Ieee11073ToSingle(new byte[2]
				{
					value[1],
					value[2]
				}) * (float)Math.Pow(10.0, b);
				DateTime timeStamp = DateTime.MinValue;
				if ((value[0] & 2) == 2)
				{
					timeStamp = new DateTime(BitConverter.ToInt16(new byte[2]
					{
						value[5],
						value[6]
					}, 0), value[7], value[8], value[9], value[10], value[11]);
				}
				_Temperature.Result.Add(new BodyTemperatureResult.BodyTemperatureMeasureRecord
				{
					Temperture = num2,
					TimeStamp = timeStamp
				});
				Log.Info(string.Format("【BLELib】【BLEDeviceNSM1】【ThermometerMeasurement_CharacteristicValueUpdateHandler】CharacteristicValueUpdateHandle:{0}:TimeStamp={1}, 体温={2}", new object[3]
				{
					Device.Name,
					timeStamp.ToString("yyyy/MM/dd H:mm:ss"),
					num2
				}));
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLEDeviceNSM1】【ThermometerMeasurement_CharacteristicValueUpdateHandler】例外発生：{ex}");
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
				goto IL_015d;
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNSM1】【CharacteristicAsyncOf】例外発生：{ex}");
				goto IL_015d;
			}
			IL_015d:
			if (characteristic != null)
			{
				break;
			}
		}
		return characteristic;
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

	public static float Ieee11073ToSingle(byte[] bytes)
	{
		ushort num = (ushort)(bytes[0] + 256 * bytes[1]);
		int num2 = num & 0xFFF;
		if (reservedValues.ContainsKey(num2))
		{
			return reservedValues[num2];
		}
		if (num2 >= 2048)
		{
			num2 = -(4096 - num2);
		}
		int num3 = num >> 12;
		if (num3 >= 8)
		{
			num3 = -(16 - num3);
		}
		double num4 = Math.Pow(10.0, num3);
		return (float)((double)num2 * num4);
	}
}

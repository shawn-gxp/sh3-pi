using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using BLELib.Common;
using Plugin.BLE;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;

namespace BLELib.BLEDevice;

public class BLEDeviceNT100B : IBLEDevice
{
	public class BodyTemperatureResult
	{
		public class BodyTemperatureMeasureRecord
		{
			public DateTime TimeStamp { get; set; }

			public double Temperature { get; set; }

			public override string ToString()
			{
				return string.Format(TimeStamp.ToString() + ", " + Temperature);
			}
		}

		public IList<BodyTemperatureMeasureRecord> Result { get; set; }
	}

	private static class ThermometerServiceConstants
	{
		public const string SERVICE_UUID = "00001809-0000-1000-8000-00805F9B34FB";

		public const string TEMPERATURE_MEASUREMENT = "00002A1C-0000-1000-8000-00805F9B34FB";
	}

	private static class DeviceInformationServiceConstants
	{
		public const string SERVICE_UUID = "0000180A-0000-1000-8000-00805F9B34FB";

		public const string SYSTEM_ID = "00002A23-0000-1000-8000-00805F9B34FB";

		public const string MODEL_NUMBER = "00002A24-0000-1000-8000-00805F9B34FB";

		public const string SERIAL_NUMBER = "00002A25-0000-1000-8000-00805F9B34FB";

		public const string FIRMWARE_REVISION = "00002A26-0000-1000-8000-00805F9B34FB";

		public const string HARDWARE_REVISION = "00002A27-0000-1000-8000-00805F9B34FB";

		public const string SOFTWARE_REVISION = "00002A28-0000-1000-8000-00805F9B34FB";

		public const string MANUFACTURE_NAME = "00002A29-0000-1000-8000-00805F9B34FB";

		public const string PnP_ID = "00002A50-0000-1000-8000-00805F9B34FB";

		public const string REGISTRATION_CERTIFICATION_DATA = "00002A2A-0000-1000-8000-00805F9B34FB";
	}

	private static class CustomServiceConstants
	{
		public const string SERVICE_UUID = "00001523-1212-EFDE-1523-785FEABCD123";

		public const string WRITE_REQ = "00001524-1212-EFDE-1523-785FEABCD123";

		public const string NOTIFICATION = "00001524-1212-EFDE-1523-785FEABCD123";
	}

	private static class CommandMessages
	{
		public const byte ReadDeviceClockTime = 35;

		public const byte ReadDeviceModel = 36;

		public const byte ReadTheStorageDataWithIndexTime = 37;

		public const byte ReadTheStorageDataWithIndexResult = 38;

		public const byte ReadDeviceSerialNumber1 = 39;

		public const byte ReadDeviceSerialNumber2 = 40;

		public const byte ReadStorageNumberOfData = 43;

		public const byte WriteSystemClockTime = 51;

		public const byte StartAnInfraRedTemperatureMeasurement = 65;

		public const byte TurnOffTheDevice = 80;

		public const byte ClearDeleteAllMemory = 82;

		public const byte NotificationForEnteringCommunicationMode = 84;
	}

	private static ILoggingService Log = LogManager.GetLogger();

	private static readonly int PAIRING_REQUEST_TIMEOUT = 30000;

	protected const string COMMON_UUID_TAIL = "-0000-1000-8000-00805F9B34FB";

	public const string PRIMARY_SERVICE_UUID = "00001809-0000-1000-8000-00805F9B34FB";

	private static IAdapter _IAdapter = CrossBluetoothLE.Current.Adapter;

	private CancellationTokenSource _CancelToken;

	private BodyTemperatureResult _Temperature;

	private bool isDeviceConnectionLost;

	private static readonly Dictionary<int, float> RESERVED_VALUES = new Dictionary<int, float>
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

	public IDevice Device { get; set; }

	public string DeviceName => "NT-100B";

	public event EventHandler<BLELibStatusEventArgs> StateChanged;

	public object CreateDevice(IDevice device)
	{
		return new BLEDeviceNT100B
		{
			Device = device,
			DataReceiveCompleteEvent = new ManualResetEventSlim(initialState: false),
			DeviceConnectedEvent = new ManualResetEventSlim(initialState: false)
		};
	}

	public void InitializeDeviceInfo(IDevice device)
	{
		Device = device;
		DataReceiveCompleteEvent = new ManualResetEventSlim(initialState: false);
		DeviceConnectedEvent = new ManualResetEventSlim(initialState: false);
	}

	public async Task<IList<string>> Pairing(IList<string> param = null)
	{
		Log.Info("【BLELib】【BLEDeviceNT100B】【Pairing】start pairing");
		try
		{
			IList<string> result;
			try
			{
				string text = "";
				_CancelToken = new CancellationTokenSource();
				Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【Pairing】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				AdvertisementRecord advertisementRecord = Device?.AdvertisementRecords.FirstOrDefault((AdvertisementRecord x) => x.Type == AdvertisementRecordType.ManufacturerSpecificData);
				if (advertisementRecord != null && advertisementRecord.Data.Length >= 4)
				{
					text = Convert.ToInt64(BitConverter.ToString(advertisementRecord.Data.Skip(advertisementRecord.Data.Length - 4).ToArray().Reverse()
						.ToArray()).Replace("-", ""), 16).ToString();
				}
				Log.Info("【BLELib】【BLEDeviceNT100B】【Pairing】manufactureSpecific=" + BitConverter.ToString(advertisementRecord?.Data) + ":SerialNumber=" + text);
				result = new string[3]
				{
					Device?.Name,
					Device?.Id.ToString(),
					text
				};
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNT100B】【Pairing】例外発生：{ex}");
				StatusChange(Device?.Name, BLELibStatus.PAIR_ERR, ex.Message);
				goto end_IL_0031;
			}
			return result;
			end_IL_0031:;
		}
		finally
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【Pairing】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			_IAdapter.DisconnectDeviceAsync(Device);
			await Task.Delay(100);
		}
		Log.Info("【BLELib】【BLEDeviceNT100B】【Pairing】finish pairing");
		return new string[3] { "", "", "" };
	}

	public async Task<object> ReciveStart(Action<int, int> handler, int timeout, IList<string> param = null)
	{
		ICharacteristic characteristicTemperature = null;
		try
		{
			object result;
			try
			{
				_Temperature = new BodyTemperatureResult();
				_Temperature.Result = new List<BodyTemperatureResult.BodyTemperatureMeasureRecord>();
				DataReceiveCompleteEvent.Reset();
				if (!(await DeviceConnect(timeout)))
				{
					result = new string[3] { "", "", "" };
				}
				else
				{
					characteristicTemperature = await CharacteristicAsyncOf(Device, "00001809-0000-1000-8000-00805F9B34FB", "00002A1C-0000-1000-8000-00805F9B34FB");
					characteristicTemperature.ValueUpdated -= ThermometerMeasurement_CharacteristicValueUpdateHandler;
					characteristicTemperature.ValueUpdated += ThermometerMeasurement_CharacteristicValueUpdateHandler;
					await characteristicTemperature.StartUpdatesAsync();
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
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNT100B】【ReciveStart】例外発生：{ex}");
				await StatusChange(Device?.Name, BLELibStatus.RCV_ERR, ex.Message);
				goto end_IL_0082;
			}
			return result;
			end_IL_0082:;
		}
		finally
		{
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			if (characteristicTemperature != null)
			{
				characteristicTemperature.ValueUpdated -= ThermometerMeasurement_CharacteristicValueUpdateHandler;
			}
			TurnOffTheDevice().ConfigureAwait(continueOnCapturedContext: false);
			await Task.Delay(1000);
			Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【ReciveStart】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			_IAdapter.DisconnectDeviceAsync(Device);
			await Task.Delay(100);
		}
		return null;
	}

	public async Task ReceiveStop()
	{
		_CancelToken?.Cancel();
		if (Device != null)
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【ReceiveStop】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
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
			Log.Info("【BLELib】【BLEDeviceNT100B】【Cancel】Cancel all tasks.");
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
		Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【_IAdapter_DeviceConnected】Device.Name={0}, Device.Id={1}", new object[2]
		{
			e?.Device?.Name,
			e?.Device?.Id
		}));
		if (Device == null || !(Device.Name != e?.Device?.Name))
		{
			isDeviceConnectionLost = false;
			DeviceConnectedEvent.Set();
		}
	}

	private void _IAdapter_DeviceConnectionLost(object sender, DeviceEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【_IAdapter_DeviceConnectionLost】Device.Name={0}, Device.Id={1}", new object[2]
		{
			e?.Device?.Name,
			e?.Device?.Id
		}));
		isDeviceConnectionLost = true;
	}

	public void BondStatusBroadcastReceiver_BondStateChanged(object sender, DeviceBondStateChangedEventArgs e)
	{
	}

	private async Task<bool> DeviceConnect(int timeoutMillisec = 60000)
	{
		DeviceConnectedEvent.Reset();
		_CancelToken = new CancellationTokenSource();
		_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
		_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
		_IAdapter.DeviceConnectionLost -= _IAdapter_DeviceConnectionLost;
		_IAdapter.DeviceConnectionLost += _IAdapter_DeviceConnectionLost;
		if (_IAdapter.IsScanning)
		{
			Log.Info("【BLELib】【BLEDeviceNT100B】【DeviceConnect】scan already processing, stop scan.");
			await _IAdapter.StopScanningForDevicesAsync();
		}
		try
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【DeviceConnect】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
			await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _CancelToken.Token);
			Task receiveTask = Task.Run(async delegate
			{
				DeviceConnectedEvent.Wait();
			}, _CancelToken.Token);
			if (await Task.WhenAny(receiveTask, Task.Delay(timeoutMillisec)) != receiveTask)
			{
				Log.Info("【BLELib】【BLEDeviceNT100B】【RDeviceConnect】ReceiveStart失敗：RCV_TIMEOUT");
				await StatusChange(Device?.Name, BLELibStatus.RCV_TIMEOUT);
				return false;
			}
		}
		catch (TaskCanceledException ex)
		{
			Log.Warn($"【BLELib】【BLEDeviceNT100B】【DeviceConnect】TaskCanceledException発生：{ex}");
			throw;
		}
		catch (OperationCanceledException ex2)
		{
			Log.Warn($"【BLELib】【BLEDeviceNT100B】【DeviceConnect】OperationCanceledException発生：{ex2}");
			throw;
		}
		return true;
	}

	private static byte[] MakeCommand(byte command, byte[] data = null)
	{
		List<byte> list = new List<byte> { 81, command };
		if (data != null)
		{
			list.AddRange(data);
		}
		list.Add(163);
		byte item = list.Aggregate<byte, byte>(0, (byte current, byte b) => (byte)(current + b));
		list.Add(item);
		return list.ToArray();
	}

	private async Task TurnOffTheDevice()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("00001523-1212-EFDE-1523-785FEABCD123"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00001524-1212-EFDE-1523-785FEABCD123"));
		Log.Info("【BLELib】【BLEDeviceNT100B】【TurnOffTheDevice】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] request = MakeCommand(80, new byte[4]);
		bool flag = await characteristic.WriteAsync(request);
		Log.Info($"【BLELib】【BLEDeviceNT100B】【TurnOffTheDevice】Custom :Service={service.Id.ToString()}, Characteristic={characteristic.Id.ToString()}, request={BitConverter.ToString(request)}, :WriteAsync={flag}");
	}

	private async Task<ICharacteristic> CharacteristicAsyncOf(IDevice device, string serviceUuid, string characteristicUuid)
	{
		ICharacteristic characteristic = null;
		int retryCount = 0;
		while (retryCount < 3)
		{
			Log.Debug($"【BLELib】【BLEDeviceNT100B】【CharacteristicAsyncOf】retryCount: {retryCount + 1}");
			retryCount++;
			if (isDeviceConnectionLost)
			{
				Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【CharacteristicAsyncOf】非接触体温計と再接続します Device.Name={0}, Device.Id={1}", new object[2]
				{
					device.Name,
					device?.Id
				}));
				if (!(await DeviceConnect()))
				{
					continue;
				}
			}
			try
			{
				IService service = await device.GetServiceAsync(Guid.Parse(serviceUuid));
				if (service == null)
				{
					continue;
				}
				characteristic = await service.GetCharacteristicAsync(Guid.Parse(characteristicUuid));
				goto IL_0255;
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNT100B】【CharacteristicAsyncOf】例外発生：{ex}");
				goto IL_0255;
			}
			IL_0255:
			if (characteristic != null)
			{
				break;
			}
		}
		return characteristic;
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

	private async void ThermometerMeasurement_CharacteristicValueUpdateHandler(object sender, CharacteristicUpdatedEventArgs e)
	{
		try
		{
			ICharacteristic characteristic = e.Characteristic;
			byte[] value = characteristic.Value;
			string text = characteristic.Uuid.ToUpper();
			Log.Info("【BLELib】【BLEDeviceNT100B】【ThermometerMeasurement_CharacteristicValueUpdateHandler】sender=" + (sender as ICharacteristic)?.Uuid + ", buffer=" + BitConverter.ToString(e.Characteristic.Value));
			int num = 0;
			string[] array = text.Split(new char[1] { '-' });
			if (array.Length != 0)
			{
				num = int.Parse(array[0], NumberStyles.AllowHexSpecifier);
			}
			if (num == 10780 && value != null && value.Length >= 12)
			{
				sbyte b = (sbyte)(value[4] & 0xFF);
				float num2 = Ieee11073ToFloat(new byte[2]
				{
					value[1],
					value[2]
				}) * (float)Math.Pow(10.0, b);
				DateTime now = DateTime.Now;
				_Temperature.Result.Add(new BodyTemperatureResult.BodyTemperatureMeasureRecord
				{
					Temperature = num2,
					TimeStamp = now
				});
				Log.Info(string.Format("【BLELib】【BLEDeviceNT100B】【ThermometerMeasurement_CharacteristicValueUpdateHandler】CharacteristicValueUpdateHandle:{0}:TimeStamp={1:yyyy/MM/dd H:mm:ss}, 体温={2}", new object[3] { Device.Name, now, num2 }));
				DataReceiveCompleteEvent?.Set();
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLEDeviceNT100B】【ThermometerMeasurement_CharacteristicValueUpdateHandler】例外発生：{ex}");
		}
	}

	public static float Ieee11073ToFloat(byte[] bytes)
	{
		ushort num = (ushort)(bytes[0] + 256 * bytes[1]);
		int num2 = num & 0xFFF;
		if (RESERVED_VALUES.ContainsKey(num2))
		{
			return RESERVED_VALUES[num2];
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

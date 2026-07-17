using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using BLELib.Common;
using Plugin.BLE;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Xamarin.Essentials;

namespace BLELib.BLEDevice;

public class BLEDeviceMightySat : IBLEDevice
{
	public class MightySatResult
	{
		public class MightySatMeasureRecord
		{
			public DateTime TimeStamp { get; set; }

			public int SpO2 { get; set; }

			public int PR { get; set; }

			public double PI { get; set; }

			public override string ToString()
			{
				return string.Format(TimeStamp.ToString() + ", " + SpO2 + ", " + PR + ", " + PI);
			}
		}

		public IList<MightySatMeasureRecord> Result { get; set; }
	}

	private static class CommandMessages
	{
		public const byte GetDeviceInformation = 1;

		public const byte SetClock = 2;

		public const byte EnableStream = 3;

		public const byte Waveforms = 4;

		public const byte Parameters = 5;

		public const byte GetTrendRecord = 6;

		public const byte ClearallTrend = 7;

		public const byte Ack = 254;

		public const byte Nack = 255;
	}

	private enum RecieveStateType
	{
		success,
		error
	}

	public enum CRC8_POLY
	{
		CRC8 = 213,
		CRC8_CCITT = 7,
		CRC8_DALLAS_MAXIM = 49,
		CRC8_SAE_J1850 = 29,
		CRC_8_WCDMA = 155
	}

	public class CRC8Calc
	{
		private byte[] table = new byte[256];

		public byte[] Table
		{
			get
			{
				return table;
			}
			set
			{
				table = value;
			}
		}

		public byte Checksum(params byte[] val)
		{
			if (val == null)
			{
				throw new ArgumentNullException("val");
			}
			byte b = 0;
			foreach (byte b2 in val)
			{
				b = table[b ^ b2];
			}
			return b;
		}

		public byte[] GenerateTable(CRC8_POLY polynomial)
		{
			byte[] array = new byte[256];
			for (int i = 0; i < 256; i++)
			{
				int num = i;
				for (int j = 0; j < 8; j++)
				{
					num = (((num & 0x80) == 0) ? (num << 1) : ((num << 1) ^ (int)polynomial));
				}
				array[i] = (byte)num;
			}
			return array;
		}

		public CRC8Calc(CRC8_POLY polynomial)
		{
			table = GenerateTable(polynomial);
		}
	}

	private ILoggingService Log = LogManager.GetLogger();

	public const string SERVICE_UUID = "54c21000-a720-4b4f-11e4-9fe20002a5d5";

	public const string INCOMING_UUID = "54c21001-a720-4b4f-11e4-9fe20002a5d5";

	public const string GOINGOUT_UUID = "54c21002-a720-4b4f-11e4-9fe20002a5d5";

	public const byte SOM = 119;

	private const int PULSE_SEARCH_BIT = 64;

	private RecieveStateType _RecieveState = RecieveStateType.error;

	private static IAdapter _IAdapter = CrossBluetoothLE.Current.Adapter;

	private CancellationTokenSource _CancelToken;

	private MightySatResult _MightySatResult;

	private byte[] _DeviceInformation;

	private List<IList<byte>> _MessageBuffer;

	private List<byte> _RecieveBuffer;

	private int _Numberoftrendrecord;

	private int _OldestTrendSessionId;

	private int _CurrentTrendSessionId;

	private int _LastSpO2;

	private int _LastPR;

	private double _LastPI;

	private DateTime _LastDateTime;

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

	public IDevice Device { get; set; }

	public string DeviceName => "MightySat";

	public event EventHandler<BLELibStatusEventArgs> StateChanged;

	public object CreateDevice(IDevice device)
	{
		return new BLEDeviceMightySat
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
		Log.Info("【BLELib】【BLEDeviceMightySat】【Pairing】start pairing");
		try
		{
			IList<string> result;
			try
			{
				string text = "";
				AdvertisementRecord advertisementRecord = Device.AdvertisementRecords.Where((AdvertisementRecord x) => x.Type == AdvertisementRecordType.ManufacturerSpecificData).FirstOrDefault();
				if (advertisementRecord != null && advertisementRecord.Data.Length >= 4)
				{
					text = Convert.ToInt64(BitConverter.ToString(advertisementRecord.Data.Skip(advertisementRecord.Data.Length - 4).ToArray().Reverse()
						.ToArray()).Replace("-", ""), 16).ToString();
				}
				Log.Info("【BLELib】【BLEDeviceMightySat】【Pairing】manufactureSpecific=" + BitConverter.ToString(advertisementRecord?.Data) + ":SerialNumber=" + text);
				result = new string[3]
				{
					Device.Name,
					Device.Id.ToString(),
					text
				};
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceMightySat】【Pairing】例外発生：{ex}");
				StatusChange(Device.Name, BLELibStatus.PAIR_ERR, ex.Message);
				goto end_IL_0032;
			}
			return result;
			end_IL_0032:;
		}
		finally
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceMightySat】【Pairing】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			_IAdapter.DisconnectDeviceAsync(Device);
			await Task.Delay(100);
		}
		Log.Info("【BLELib】【BLEDeviceMightySat】【Pairing】finish pairing");
		return new string[3] { "", "", "" };
	}

	public async Task<object> ReciveStart(Action<int, int> handler, int timeout, IList<string> param = null)
	{
		_RecieveBuffer = new List<byte>();
		_MessageBuffer = new List<IList<byte>>();
		ICharacteristic characteristicGoingout = null;
		try
		{
			object result;
			try
			{
				_LastSpO2 = 0;
				_LastPR = 0;
				_LastPI = 0.0;
				_MightySatResult = new MightySatResult();
				_MightySatResult.Result = new List<MightySatResult.MightySatMeasureRecord>();
				DeviceConnectedEvent.Reset();
				DataReceiveCompleteEvent.Reset();
				_CancelToken = new CancellationTokenSource();
				_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
				_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
				Log.Info(string.Format("【BLELib】【BLEDeviceMightySat】【ReciveStart】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				if (_IAdapter.IsScanning)
				{
					Log.Info("【BLELib】【BLEDeviceMightySat】【ReciveStart】scan already processing, stop scan.");
					await _IAdapter.StopScanningForDevicesAsync();
				}
				ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
				await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _CancelToken.Token);
				if (!DeviceConnectedEvent.Wait(timeout))
				{
					await StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
					result = _MightySatResult;
				}
				else
				{
					using (IService service = await Device.GetServiceAsync(Guid.Parse("54c21000-a720-4b4f-11e4-9fe20002a5d5")))
					{
						characteristicGoingout = await service.GetCharacteristicAsync(Guid.Parse("54c21002-a720-4b4f-11e4-9fe20002a5d5"));
						characteristicGoingout.ValueUpdated -= CharacteristicIncoming_ValueUpdated;
						characteristicGoingout.ValueUpdated += CharacteristicIncoming_ValueUpdated;
						await characteristicGoingout.StartUpdatesAsync();
					}
					if (!(await SendCommand(MakeCommand(1))))
					{
						await StatusChange(Device.Name, BLELibStatus.RCV_ERR, "Get Device Information");
						result = _MightySatResult;
					}
					else
					{
						result = await Task.Run((Func<Task<object>>)async delegate
						{
							if (!DataReceiveCompleteEvent.Wait(timeout))
							{
								await StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
							}
							else if (_RecieveState == RecieveStateType.success)
							{
								await StatusChange(Device.Name, BLELibStatus.RCV_END);
							}
							else
							{
								await StatusChange(Device.Name, BLELibStatus.RCV_ERR);
							}
							return _MightySatResult;
						});
					}
				}
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceMightySat】【ReciveStart】例外発生：{ex}");
				await StatusChange(Device.Name, BLELibStatus.RCV_ERR, ex.Message);
				goto end_IL_0084;
			}
			return result;
			end_IL_0084:;
		}
		finally
		{
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			if (characteristicGoingout != null)
			{
				characteristicGoingout.ValueUpdated -= CharacteristicIncoming_ValueUpdated;
			}
			Log.Info(string.Format("【BLELib】【BLEDeviceMightySat】【ReciveStart】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
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
		if (_CancelToken != null)
		{
			_CancelToken.Cancel();
		}
		if (Device != null)
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceMightySat】【ReceiveStop】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			await _IAdapter.DisconnectDeviceAsync(Device);
		}
	}

	private void CharacteristicIncoming_ValueUpdated(object sender, CharacteristicUpdatedEventArgs e)
	{
		try
		{
			ICharacteristic characteristic = e.Characteristic;
			byte[] value = characteristic.Value;
			characteristic.Uuid.ToUpper();
			Log.Info("【BLELib】【BLEDeviceMightySat】【CharacteristicIncoming_ValueUpdated】sender=" + (sender as ICharacteristic).Uuid + ", buffer=" + BitConverter.ToString(e.Characteristic.Value));
			_RecieveBuffer.AddRange(value);
			while (_RecieveBuffer.Count >= 2)
			{
				int count = _RecieveBuffer.Count;
				if (_RecieveBuffer[0] == 119)
				{
					int num = _RecieveBuffer[1];
					if (count >= num + 2)
					{
						IEnumerable<byte> source = _RecieveBuffer.Take(num + 2);
						ResponseMessageCommand(source.ToArray());
						_RecieveBuffer.RemoveRange(0, num + 2);
						continue;
					}
					break;
				}
				break;
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLEDeviceMightySat】【CharacteristicIncoming_ValueUpdated】{ex}");
		}
	}

	public void Cancel()
	{
		if (_CancelToken != null)
		{
			Log.Info("【BLELib】【BLEDeviceMightySat】【Cancel】Cancel all tasks.");
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
		try
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceMightySat】【_IAdapter_DeviceConnected】Device.Name={0}, Device.Id={1}", new object[2]
			{
				e?.Device?.Name,
				e?.Device?.Id
			}));
			if (Device == null || !(Device.Name != e?.Device?.Name))
			{
				DeviceConnectedEvent.Set();
			}
		}
		catch (Exception ex)
		{
			Log.Error(ex, $"【BLELib】【BLEDeviceMightySat】【_IAdapter_DeviceConnected】例外発生: {ex}");
			throw;
		}
	}

	private void _IAdapter_DeviceConnectionLost(object sender, DeviceErrorEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceMightySat】【_IAdapter_DeviceConnectionLost】Device.Name={0}, Device.Id={1}", new object[2]
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

	private byte[] MakeCommand(byte command, byte[] data = null)
	{
		List<byte> list = new List<byte> { command };
		if (data != null)
		{
			list.AddRange(data);
		}
		byte item = new CRC8Calc(CRC8_POLY.CRC8_CCITT).Checksum(list.ToArray());
		list.Add(item);
		int count = list.Count;
		List<byte> list2 = new List<byte>();
		list2.Add(119);
		list2.Add((byte)count);
		list2.AddRange(list);
		return list2.ToArray();
	}

	private async Task<bool> SendCommand(byte[] command)
	{
		bool flag = false;
		using (IService service = await Device.GetServiceAsync(Guid.Parse("54c21000-a720-4b4f-11e4-9fe20002a5d5")))
		{
			ICharacteristic characteristicIncoming = await service.GetCharacteristicAsync(Guid.Parse("54c21001-a720-4b4f-11e4-9fe20002a5d5"));
			characteristicIncoming.WriteType = CharacteristicWriteType.WithoutResponse;
			Log.Info("【BLELib】【BLEDeviceMightySat】【SendCommand】WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristicIncoming.Id.ToString() + ", data=" + BitConverter.ToString(command));
			flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristicIncoming.WriteAsync(command));
			Log.Info($"【BLELib】【BLEDeviceMightySat】【SendCommand】WriteAsync:Result={flag}");
		}
		return flag;
	}

	private void ResponseMessageCommand(byte[] message)
	{
		Log.Info("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】message=" + BitConverter.ToString(message));
		switch (message[2])
		{
		case 1:
		{
			_DeviceInformation = message.Skip(2).ToArray();
			_Numberoftrendrecord = _DeviceInformation[10];
			_OldestTrendSessionId = BitConverter.ToInt32(_DeviceInformation, 11);
			_CurrentTrendSessionId = BitConverter.ToInt32(_DeviceInformation, 15);
			Log.Debug(string.Format("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】Numberoftrendrecord={0}, OldestTrendSessionId={1}, CurrentTrendSessionId={2}", new object[3] { _Numberoftrendrecord, _OldestTrendSessionId, _CurrentTrendSessionId }));
			long datetime = DateTime.UtcNow.Ticks;
			Task.Run(async delegate
			{
				if (!(await SendCommand(MakeCommand(2, BitConverter.GetBytes(datetime)))))
				{
					_RecieveState = RecieveStateType.error;
					DataReceiveCompleteEvent.Set();
				}
			});
			break;
		}
		case 254:
			if (message.Length < 4)
			{
				break;
			}
			switch (message[3])
			{
			case 2:
				Task.Run(async delegate
				{
					byte[] data = new byte[3]
					{
						_DeviceInformation[3],
						_DeviceInformation[4],
						_DeviceInformation[5]
					};
					if (!(await SendCommand(MakeCommand(3, data))))
					{
						_RecieveState = RecieveStateType.error;
						DataReceiveCompleteEvent.Set();
					}
				});
				break;
			default:
				_ = 6;
				break;
			case 3:
				break;
			}
			break;
		case 255:
			_RecieveState = RecieveStateType.error;
			break;
		case 5:
		{
			BitArray bitArray = new BitArray(new byte[4]
			{
				message[3],
				message[4],
				message[5],
				message[6]
			});
			if (bitArray.Get(21))
			{
				Log.Debug("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】Sensor Off Patient");
				_MightySatResult.Result.Add(new MightySatResult.MightySatMeasureRecord
				{
					TimeStamp = _LastDateTime,
					SpO2 = _LastSpO2,
					PR = _LastPR,
					PI = _LastPI
				});
				DataReceiveCompleteEvent.Set();
				break;
			}
			if (bitArray.Get(22))
			{
				Log.Debug("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】Pulse Search");
			}
			else if (bitArray.Get(23))
			{
				Log.Debug("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】Interference Detected");
			}
			else if (bitArray.Get(24))
			{
				Log.Debug("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】Low Perfusion");
			}
			byte bit = message[7];
			byte b = message[8];
			Log.Debug("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】CheckParameterExceptionBit:SpO2");
			CheckParameterExceptionBit(bit);
			byte bit2 = message[9];
			byte b2 = message[10];
			Log.Debug("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】CheckParameterExceptionBit:PR");
			CheckParameterExceptionBit(bit2);
			byte bit3 = message[11];
			double num = (double)BitConverter.ToInt16(message, 12) / 100.0;
			Log.Debug("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】CheckParameterExceptionBit:PI");
			CheckParameterExceptionBit(bit3);
			Log.Debug(string.Format("【BLELib】【BLEDeviceMightySat】【ResponseMessageCommand】SpO2={0}, PR={1}, PI={2:#.00}", new object[3] { b, b2, num }));
			_LastDateTime = DateTime.Now;
			_LastSpO2 = b;
			_LastPR = b2;
			_LastPI = num;
			break;
		}
		default:
			_ = 4;
			break;
		}
	}

	private bool CheckParameterExceptionBit(byte bit)
	{
		bool result = true;
		if ((bit | 1) == 1)
		{
			Log.Info("【BLELib】【BLEDeviceMightySat】【CheckParameterExceptionBit】Low Confidence");
		}
		else if ((bit | 4) == 4)
		{
			Log.Info("【BLELib】【BLEDeviceMightySat】【CheckParameterExceptionBit】Invalid");
			result = false;
		}
		else if ((bit | 0x10) == 16)
		{
			Log.Info("【BLELib】【BLEDeviceMightySat】【CheckParameterExceptionBit】Startup State");
		}
		return result;
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

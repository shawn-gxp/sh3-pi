using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using BLELib.Common;
using BLELib.Helper;
using Plugin.BLE;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Xamarin.Essentials;

namespace BLELib.BLEDevice;

public class BLEDeviceCFL : IBLEDevice
{
	private class RecvData
	{
		public string SeqNo { get; set; }

		public DateTime Date { get; set; }

		public int Data { get; set; }

		public int Meal { get; set; }

		public string Type { get; set; }
	}

	public enum ReceiveMode
	{
		All,
		Diff,
		Last
	}

	public class PairingParameter
	{
		public CliantType Type { get; set; }

		public AutoTransferMode Mode { get; set; }
	}

	public enum CliantType
	{
		Own,
		Family,
		Hospital
	}

	public enum AutoTransferMode
	{
		Off,
		On
	}

	public class GlucoseResult
	{
		public class GlucoseMeasureRecord
		{
			public enum MealFlagEnum
			{
				NONE,
				BEFORE,
				AFTER
			}

			public string SeqNo { get; set; }

			public DateTime TimeStamp { get; set; }

			public int Glu { get; set; }

			public int Meal { get; set; }

			public string Type { get; set; }

			public override string ToString()
			{
				return string.Format(SeqNo + ", " + TimeStamp.ToString() + ", " + Glu + ", " + Meal);
			}
		}

		public IList<string> Deviceinfo { get; set; }

		public IList<GlucoseMeasureRecord> Result { get; set; }
	}

	private ILoggingService Log = LogManager.GetLogger();

	private static readonly int PAIRING_REQUEST_TIMEOUT = 30000;

	private static readonly Dictionary<string, string> _UuidsS = new Dictionary<string, string>
	{
		{ "Glucose Meter Service", "5D87A4A0-E42D-11E5-BEEF-0002A5D5C51B" },
		{ "Glucose Meter Extension Service", "7A1A0001-8D7F-1727-A23F-DEDB5BF5DF46" },
		{ "Time and Alarm Setting Service", "87F60001-A469-1EF4-637F-78B96A6F358B" },
		{ "Device Information Service", "8E5996E0-E42F-11E5-AF97-0002A5D5C51B" },
		{ "Battery Service", "74D4C620-E431-11E5-B5F8-0002A5D5C51B" }
	};

	private static readonly Dictionary<string, string> _Uuids1 = new Dictionary<string, string>
	{
		{ "Glucose Measurement", "5D87A4A1-E42D-11E5-BEEF-0002A5D5C51B" },
		{ "Glucose Measurement Context", "5D87A4A2-E42D-11E5-BEEF-0002A5D5C51B" },
		{ "Record Access Control Point", "5D87A4A3-E42D-11E5-BEEF-0002A5D5C51B" },
		{ "Glucose Feature", "5D87A4A4-E42D-11E5-BEEF-0002A5D5C51B" }
	};

	private static readonly Dictionary<string, string> _Uuids2 = new Dictionary<string, string>
	{
		{ "Target Glucose Concentration", "7A1A0002-8D7F-1727-A23F-DEDB5BF5DF46" },
		{ "Remote Device Information", "7A1A0003-8D7F-1727-A23F-DEDB5BF5DF46" },
		{ "Fixed Message", "7A1A0005-8D7F-1727-A23F-DEDB5BF5DF46" },
		{ "Image Data Transfer", "7A1A0006-8D7F-1727-A23F-DEDB5BF5DF46" },
		{ "Transfer Access Control Point", "7A1A0007-8D7F-1727-A23F-DEDB5BF5DF46" }
	};

	private static readonly Dictionary<string, string> _Uuids3 = new Dictionary<string, string> { { "Current Time", "87F60002-A469-1EF4-637F-78B96A6F358B" } };

	private static readonly Dictionary<string, string> _Uuids4 = new Dictionary<string, string>
	{
		{ "Manufacturer Name String", "8E5996E1-E42F-11E5-AF97-0002A5D5C51B" },
		{ "Model Number String", "8E5996E2-E42F-11E5-AF97-0002A5D5C51B" },
		{ "Serial Number String", "8E5996E3-E42F-11E5-AF97-0002A5D5C51B" },
		{ "Hardware Revision String", "8E5996E4-E42F-11E5-AF97-0002A5D5C51B" },
		{ "Firmware Revision String", "8E5996E5-E42F-11E5-AF97-0002A5D5C51B" },
		{ "Software Revision String", "8E5996E6-E42F-11E5-AF97-0002A5D5C51B" }
	};

	private static IAdapter _IAdapter = CrossBluetoothLE.Current.Adapter;

	private Dictionary<string, RecvData> _RecvBuffer;

	private GlucoseResult _ReturnBuffer;

	private int _MaxSeqNo;

	private Action<int, int> _Handler;

	private int _Timeout;

	private CancellationTokenSource _CancelToken;

	private ReceiveMode _Mode;

	private int _LastId;

	private bool _IsPairingSequance;

	private PairingParameter _PairingParameter;

	private static Dictionary<int, float> reservedValues = new Dictionary<int, float>
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

	public string DeviceName => "NIPRO CF";

	protected Dictionary<string, IService> GattDeviceServiceList { get; set; }

	protected Dictionary<string, ICharacteristic> GattCharacteristicList1 { get; set; }

	protected Dictionary<string, ICharacteristic> GattCharacteristicList2 { get; set; }

	protected Dictionary<string, ICharacteristic> GattCharacteristicList3 { get; set; }

	protected Dictionary<string, ICharacteristic> GattCharacteristicList4 { get; set; }

	protected object RecvBufferLock { get; set; }

	protected ManualResetEventSlim DeviceConnectedEvent { get; set; }

	protected ManualResetEventSlim DataReceiveCompleteEvent { get; set; }

	public IDevice Device { get; set; }

	public event EventHandler<BLELibStatusEventArgs> StateChanged;

	public object CreateDevice(IDevice device)
	{
		return new BLEDeviceCFL
		{
			Device = device,
			GattDeviceServiceList = new Dictionary<string, IService>(),
			GattCharacteristicList1 = new Dictionary<string, ICharacteristic>(),
			GattCharacteristicList2 = new Dictionary<string, ICharacteristic>(),
			GattCharacteristicList4 = new Dictionary<string, ICharacteristic>(),
			RecvBufferLock = new object(),
			DataReceiveCompleteEvent = new ManualResetEventSlim(initialState: false),
			DeviceConnectedEvent = new ManualResetEventSlim(initialState: false)
		};
	}

	public void InitializeDeviceInfo(IDevice device)
	{
		Device = device;
		GattDeviceServiceList = new Dictionary<string, IService>();
		GattCharacteristicList1 = new Dictionary<string, ICharacteristic>();
		GattCharacteristicList2 = new Dictionary<string, ICharacteristic>();
		GattCharacteristicList4 = new Dictionary<string, ICharacteristic>();
		RecvBufferLock = new object();
		DataReceiveCompleteEvent = new ManualResetEventSlim(initialState: false);
		DeviceConnectedEvent = new ManualResetEventSlim(initialState: false);
	}

	public async Task<IList<string>> Pairing(IList<string> param = null)
	{
		Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】start pairing");
		try
		{
			_IsPairingSequance = true;
			string serialNumber = "";
			_CancelToken = new CancellationTokenSource();
			_PairingParameter = new PairingParameter();
			if (param != null)
			{
				int result = 0;
				int result2 = 0;
				if (param.Count > 0)
				{
					int.TryParse(param[0], out result);
				}
				if (param.Count > 1)
				{
					int.TryParse(param[1], out result2);
				}
				_PairingParameter.Type = (CliantType)result;
				_PairingParameter.Mode = (AutoTransferMode)result2;
			}
			else
			{
				_PairingParameter.Type = CliantType.Own;
				_PairingParameter.Mode = AutoTransferMode.On;
			}
			Log.Info(string.Format("【BLELib】【BLEDeviceCFL】【Pairing】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			if (_IAdapter.IsScanning)
			{
				Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】scan already processing, stop scan.");
				await _IAdapter.StopScanningForDevicesAsync();
			}
			ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
			await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _CancelToken.Token);
			Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】State = " + Device?.State.ToString());
			using (IService service = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Device Information Service"])))
			{
				ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse(_Uuids4["Serial Number String"]));
				Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】Serial Number ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
				byte[] array = await characteristic.ReadAsync();
				serialNumber = Encoding.UTF8.GetString(array, 0, array.Length);
				Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】Serial Number ReadAsync:Result=" + BitConverter.ToString(array) + ":SerialNumber=" + serialNumber);
			}
			if (!(await TimeSetting()))
			{
				throw new Exception("ペアリング時の時刻設定に失敗しました。");
			}
			Task pairingTask = Task.Run(async delegate
			{
				using IService service2 = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Glucose Meter Service"]));
				ICharacteristic characteristic2 = await service2.GetCharacteristicAsync(Guid.Parse(_Uuids1["Glucose Feature"]));
				Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】Glucose Feature ReadAsync:Service=" + service2.Id.ToString() + ", Characteristic=" + characteristic2.Id.ToString());
				byte[] array2 = await characteristic2.ReadAsync();
				BitArray values = new BitArray(array2);
				Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】Glucose Feature ReadAsync:Result=" + BitConverter.ToString(array2) + ":feature=" + values.JoinString(","));
			});
			if (await Task.WhenAny(pairingTask, Task.Delay(PAIRING_REQUEST_TIMEOUT)) != pairingTask)
			{
				Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】ペアリング失敗：PAIR_TIMEOUT");
				await StatusChange(Device.Name, BLELibStatus.PAIR_TIMEOUT);
				return new string[3] { "", "", "" };
			}
			Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】State = " + Device.State);
			return await Task.Run((Func<Task<IList<string>>>)async delegate
			{
				ReciveStart(delegate
				{
				}, 30000);
				return new string[3]
				{
					Device.Name,
					Device.Id.ToString(),
					serialNumber
				};
			});
		}
		catch (TaskCanceledException ex)
		{
			Log.Warn($"【BLELib】【BLEDeviceCFL】【Pairing】TaskCanceledException発生：{ex}");
		}
		catch (OperationCanceledException ex2)
		{
			Log.Warn($"【BLELib】【BLEDeviceCFL】【Pairing】OperationCanceledException発生：{ex2}");
		}
		catch (Exception ex3)
		{
			Log.Error($"【BLELib】【BLEDeviceCFL】【Pairing】例外発生：{ex3}");
			await StatusChange(Device?.Name, BLELibStatus.PAIR_ERR, ex3.Message);
		}
		finally
		{
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
		}
		Log.Info("【BLELib】【BLEDeviceCFL】【Pairing】finish pairing");
		return new string[3] { "", "", "" };
	}

	public async Task<object> ReciveStart(Action<int, int> handler, int timeout, IList<string> param = null)
	{
		Stopwatch stopwatch = new Stopwatch();
		try
		{
			object result3;
			try
			{
				stopwatch.Start();
				DeviceConnectedEvent.Reset();
				ReceiveMode mode = ReceiveMode.All;
				if (param != null)
				{
					if (param.Count >= 1 && Enum.TryParse<ReceiveMode>(param[0], out var result))
					{
						mode = result;
					}
					if (param.Count >= 2 && int.TryParse(param[1], out var result2))
					{
						_LastId = result2;
					}
				}
				_RecvBuffer = new Dictionary<string, RecvData>();
				_ReturnBuffer = new GlucoseResult();
				_ReturnBuffer.Result = new List<GlucoseResult.GlucoseMeasureRecord>();
				_MaxSeqNo = 0;
				DataReceiveCompleteEvent.Reset();
				_Mode = mode;
				_Handler = handler;
				_Timeout = timeout;
				ClearGlucoseMeterService();
				_CancelToken = new CancellationTokenSource();
				bool bDeffNoData = false;
				bool bRes = false;
				if (_IsPairingSequance)
				{
					goto IL_0891;
				}
				_IsPairingSequance = false;
				_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
				_IAdapter.DeviceConnected += _IAdapter_DeviceConnected;
				Log.Info(string.Format("【BLELib】【BLEDeviceCFL】【ReciveStart】ConnectToDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
				await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _CancelToken.Token);
				if (!DeviceConnectedEvent.Wait(_Timeout))
				{
					await StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
					result3 = _ReturnBuffer;
				}
				else
				{
					DeviceConnectedEvent.Reset();
					_Timeout = timeout - (int)stopwatch.ElapsedMilliseconds;
					string text = "";
					using (IService serviceSerialNumber = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Device Information Service"])))
					{
						ICharacteristic characteristic = await serviceSerialNumber.GetCharacteristicAsync(Guid.Parse(_Uuids4["Serial Number String"]));
						Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】Serial Number ReadAsync:Service=" + serviceSerialNumber.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
						byte[] array = await characteristic.ReadAsync();
						text = Encoding.UTF8.GetString(array, 0, array.Length);
						Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】Serial Number ReadAsync:Result=" + BitConverter.ToString(array) + ": serialNumber=" + text);
					}
					_ReturnBuffer.Deviceinfo = new string[3]
					{
						Device.Name,
						Device.Id.ToString(),
						text
					};
					if (await TimeSetting())
					{
						if (param.Count == 9)
						{
							IList<string> list = await GetConcentration();
							if (list.Count == 7 && (param[3] != list[1] || param[4] != list[2] || param[5] != list[3] || param[6] != list[4]))
							{
								Log.Debug("【BLELib】【BLEDeviceCFL】【ReciveStart】目標値の設定");
								bRes = await SetConcentration(list[0], param[3], param[4], param[5], param[6], list[5], list[6]);
							}
						}
						goto IL_0891;
					}
					await StatusChange(Device.Name, BLELibStatus.RCV_ERR);
					result3 = _ReturnBuffer;
				}
				goto end_IL_00cc;
				IL_1284:
				if (_MaxSeqNo != 0)
				{
					goto IL_1404;
				}
				await StatusChange(Device.Name, BLELibStatus.RCV_NODATA);
				if (mode == ReceiveMode.Diff && _LastId > 1)
				{
					bDeffNoData = true;
					int num = await GetLastSEQNo();
					if (num == 0)
					{
						_LastId = 1;
					}
					else
					{
						_LastId = num;
					}
					goto IL_1404;
				}
				await PairingAllRecieve();
				result3 = _ReturnBuffer;
				goto end_IL_00cc;
				IL_0891:
				IService service = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Glucose Meter Service"]));
				GattDeviceServiceList["Glucose Meter Service"] = service;
				foreach (KeyValuePair<string, string> s in _Uuids1)
				{
					ICharacteristic cr = await GattDeviceServiceList["Glucose Meter Service"].GetCharacteristicAsync(Guid.Parse(s.Value));
					if (s.Key == "Glucose Measurement")
					{
						cr.ValueUpdated -= Cr_ValueUpdated;
						cr.ValueUpdated += Cr_ValueUpdated;
						await cr.StartUpdatesAsync();
					}
					else if (s.Key == "Glucose Measurement Context")
					{
						cr.ValueUpdated -= Cr_ValueUpdated;
						cr.ValueUpdated += Cr_ValueUpdated;
						await cr.StartUpdatesAsync();
					}
					else if (s.Key == "Record Access Control Point")
					{
						cr.ValueUpdated -= Cr_ValueUpdated;
						cr.ValueUpdated += Cr_ValueUpdated;
						await cr.StartUpdatesAsync();
					}
					Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】" + cr.Id.ToString() + ":Connected!");
					GattCharacteristicList1[s.Key] = cr;
				}
				ICharacteristic characteristic2;
				bool flag;
				if (_IsPairingSequance)
				{
					await PairingResponse();
					await PairingAllRecieve();
					result3 = _ReturnBuffer;
				}
				else
				{
					characteristic2 = GattCharacteristicList1["Record Access Control Point"];
					if (characteristic2 == null)
					{
						goto IL_1284;
					}
					characteristic2.WriteType = CharacteristicWriteType.WithResponse;
					DataReceiveCompleteEvent.Reset();
					Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】" + Device.Name + ":CanWrite = " + characteristic2.CanWrite);
					byte[] data = null;
					switch (mode)
					{
					case ReceiveMode.Last:
					{
						byte[] bytes2 = BitConverter.GetBytes((short)1);
						byte[] obj2 = new byte[5] { 4, 3, 1, 0, 0 };
						obj2[3] = bytes2[0];
						obj2[4] = bytes2[1];
						data = obj2;
						break;
					}
					case ReceiveMode.Diff:
					{
						byte[] bytes = BitConverter.GetBytes((short)_LastId);
						byte[] obj = new byte[5] { 4, 3, 1, 0, 0 };
						obj[3] = bytes[0];
						obj[4] = bytes[1];
						data = obj;
						break;
					}
					default:
						data = new byte[2] { 4, 1 };
						break;
					}
					Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】データ件数取得 WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic2.Id.ToString() + ", data=" + BitConverter.ToString(data));
					flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristic2.WriteAsync(data));
					bRes = flag;
					Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】データ件数取得コマンド送信 WriteAsync:Result=" + bRes);
					if (bRes)
					{
						Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】" + Device.Name + ":データ件数取得:" + characteristic2.Id.ToString() + ":mode=" + mode.ToString() + ":param=" + data.AsEnumerable().JoinString(",") + ":Write Success");
						bool bWaitRes = false;
						await Task.Run(delegate
						{
							bWaitRes = DataReceiveCompleteEvent.Wait(_Timeout);
							if (!bWaitRes)
							{
								StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
							}
						});
						if (bWaitRes)
						{
							goto IL_1284;
						}
						result3 = _ReturnBuffer;
					}
					else
					{
						Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】" + Device.Name + ":データ件数取得:" + characteristic2.Id.ToString() + ":mode=" + mode.ToString() + ":param=" + data.AsEnumerable().JoinString(",") + ":Write Failed");
						await StatusChange(Device.Name, BLELibStatus.RCV_ERR);
						result3 = _ReturnBuffer;
					}
				}
				goto end_IL_00cc;
				IL_1404:
				_Timeout = timeout - (int)stopwatch.ElapsedMilliseconds;
				DataReceiveCompleteEvent.Reset();
				characteristic2 = GattCharacteristicList1["Record Access Control Point"];
				if (characteristic2 == null)
				{
					goto IL_18c6;
				}
				byte[] data2;
				switch (mode)
				{
				case ReceiveMode.Last:
				{
					byte[] bytes4 = BitConverter.GetBytes((short)_MaxSeqNo);
					byte[] obj4 = new byte[5] { 1, 3, 1, 0, 0 };
					obj4[3] = bytes4[0];
					obj4[4] = bytes4[1];
					data2 = obj4;
					break;
				}
				case ReceiveMode.Diff:
				{
					byte[] bytes3 = BitConverter.GetBytes((short)_LastId);
					byte[] obj3 = new byte[5] { 1, 3, 1, 0, 0 };
					obj3[3] = bytes3[0];
					obj3[4] = bytes3[1];
					data2 = obj3;
					break;
				}
				default:
					data2 = new byte[2] { 1, 1 };
					break;
				}
				characteristic2.WriteType = CharacteristicWriteType.WithResponse;
				Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】データ取得 WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic2.Id.ToString() + ", data=" + BitConverter.ToString(data2));
				flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristic2.WriteAsync(data2));
				bRes = flag;
				Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】データ取得コマンド送信 WriteAsync:Result=" + bRes);
				if (bRes)
				{
					Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】" + Device.Name + ":データ取得:" + characteristic2.Id.ToString() + ":mode=" + mode.ToString() + ":param=" + data2.AsEnumerable().JoinString(",") + ":Write Success");
					if (!bDeffNoData)
					{
						goto IL_18c6;
					}
					result3 = _ReturnBuffer;
				}
				else
				{
					Log.Info("【BLELib】【BLEDeviceCFL】【ReciveStart】" + Device.Name + ":データ取得:" + characteristic2.Id.ToString() + ":mode=" + mode.ToString() + ":param=" + data2.AsEnumerable().JoinString(",") + ":Write Failed");
					await StatusChange(Device.Name, BLELibStatus.RCV_ERR);
					result3 = _ReturnBuffer;
				}
				goto end_IL_00cc;
				IL_18c6:
				result3 = await Task.Run((Func<Task<object>>)async delegate
				{
					bRes = DataReceiveCompleteEvent.Wait(_Timeout);
					if (bRes)
					{
						await StatusChange(Device.Name, BLELibStatus.RCV_END);
					}
					else
					{
						await StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
					}
					return _ReturnBuffer;
				});
				end_IL_00cc:;
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceCFL】【ReciveStart】例外発生：{ex}");
				await StatusChange(Device.Name, BLELibStatus.RCV_ERR, ex.Message);
				goto end_IL_0062;
			}
			return result3;
			end_IL_0062:;
		}
		finally
		{
			if (_CancelToken != null)
			{
				_CancelToken.Dispose();
				_CancelToken = null;
			}
			stopwatch?.Stop();
			ClearGlucoseMeterService();
			_IAdapter.DeviceConnected -= _IAdapter_DeviceConnected;
			Log.Info(string.Format("【BLELib】【BLEDeviceCFL】【ReciveStart】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
			{
				Device?.Name,
				Device?.Id
			}));
			_IAdapter.DisconnectDeviceAsync(Device);
			await Task.Delay(100);
			Device = null;
			_Handler = null;
		}
		return _ReturnBuffer;
	}

	public async Task ReceiveStop()
	{
		if (_CancelToken != null)
		{
			_CancelToken.Cancel();
		}
		if (Device != null)
		{
			Log.Info(string.Format("【BLELib】【BLEDeviceCFL】【ReceiveStop】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
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
			Log.Info("【BLELib】【BLEDeviceCFL】【Cancel】Cancel all tasks.");
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

	public async Task<bool> ConnectionWait()
	{
		int counter = 0;
		while (Device == null || Device.State != DeviceState.Connected)
		{
			Log.Debug($"【BLELib】【BLEDeviceCFL】【ConnectionWait】counter = {counter}");
			Log.Debug($"【BLELib】【BLEDeviceCFL】【ConnectionWait】Device.State = {Device?.State}");
			await Task.Delay(1);
		}
		DataReceiveCompleteEvent.Set();
		return true;
	}

	public async Task PairingResponse()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Glucose Meter Extension Service"]));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse(_Uuids2["Remote Device Information"]));
		if (characteristic != null)
		{
			byte[] data = null;
			if (_PairingParameter == null)
			{
				data = new byte[2] { 0, 1 };
			}
			else
			{
				data = new byte[2]
				{
					(byte)_PairingParameter.Type,
					(byte)_PairingParameter.Mode
				};
			}
			Log.Info("【BLELib】【BLEDeviceCFL】【PairingResponse】WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString() + ", data=" + BitConverter.ToString(data));
			bool flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristic.WriteAsync(data));
			Log.Info("【BLELib】【BLEDeviceCFL】【PairingResponse】WriteAsync:Result=" + flag);
			if (flag)
			{
				Log.Trace("【BLELib】【BLEDeviceCFL】【PairingResponse】" + characteristic.Id.ToString() + " :Write Success");
				return;
			}
			Log.Trace("【BLELib】【BLEDeviceCFL】【PairingResponse】" + characteristic.Id.ToString() + " :Write Failed");
			await StatusChange(Device.Name, BLELibStatus.PAIR_ERR);
		}
	}

	public async Task PairingAllRecieve()
	{
		IService service = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Glucose Meter Service"]));
		try
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse(_Uuids1["Record Access Control Point"]));
			if (characteristic == null)
			{
				return;
			}
			characteristic.ValueUpdated -= Cr_ValueUpdated;
			characteristic.ValueUpdated += Cr_ValueUpdated;
			await characteristic.StartUpdatesAsync();
			characteristic.WriteType = CharacteristicWriteType.WithResponse;
			byte[] data = null;
			DataReceiveCompleteEvent.Reset();
			data = new byte[2] { 4, 1 };
			Log.Info("【BLELib】【BLEDeviceCFL】【PairingAllRecieve】件数取得 WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString() + ", data=" + BitConverter.ToString(data));
			bool flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristic.WriteAsync(data));
			Log.Info("【BLELib】【BLEDeviceCFL】【PairingAllRecieve】件数取得コマンド送信 WriteAsync:Result=" + flag);
			Log.Info(Device.Name + ":データ件数取得:" + characteristic.Id.ToString() + ":param=" + data.AsEnumerable().JoinString(",") + (flag ? ":Write Success" : ":Write Failed"));
			bool bWaitRes = false;
			await Task.Run(delegate
			{
				bWaitRes = DataReceiveCompleteEvent.Wait(_Timeout);
				if (!bWaitRes)
				{
					StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
				}
			});
			DataReceiveCompleteEvent.Reset();
			data = new byte[2] { 1, 1 };
			Log.Info("【BLELib】【BLEDeviceCFL】【PairingAllRecieve】全件取得 WriteAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString() + ", data=" + BitConverter.ToString(data));
			flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristic.WriteAsync(data));
			Log.Info("【BLELib】【BLEDeviceCFL】【PairingAllRecieve】全件取得コマンド送信 WriteAsync:Result=" + flag);
			Log.Info(Device.Name + ":全件取得:" + characteristic.Id.ToString() + ":param=" + data.AsEnumerable().JoinString(",") + (flag ? ":Write Success" : ":Write Failed"));
			characteristic.ValueUpdated -= Cr_ValueUpdated;
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLEDeviceCFL】【PairingAllRecieve】例外発生：{ex}");
		}
		finally
		{
			service.Dispose();
		}
	}

	private void ClearGlucoseMeterService()
	{
		if (GattDeviceServiceList.ContainsKey("Glucose Meter Service"))
		{
			GattDeviceServiceList["Glucose Meter Service"].Dispose();
			GattDeviceServiceList["Glucose Meter Service"] = null;
			GattDeviceServiceList.Remove("Glucose Meter Service");
		}
		if (GattCharacteristicList1.ContainsKey("Glucose Measurement"))
		{
			GattCharacteristicList1["Glucose Measurement"].ValueUpdated -= Cr_ValueUpdated;
			GattCharacteristicList1["Glucose Measurement"] = null;
			GattCharacteristicList1.Remove("Glucose Measurement");
		}
		if (GattCharacteristicList1.ContainsKey("Glucose Measurement Context"))
		{
			GattCharacteristicList1["Glucose Measurement Context"].ValueUpdated -= Cr_ValueUpdated;
			GattCharacteristicList1["Glucose Measurement Context"] = null;
			GattCharacteristicList1.Remove("Glucose Measurement Context");
		}
		if (GattCharacteristicList1.ContainsKey("Record Access Control Point"))
		{
			GattCharacteristicList1["Record Access Control Point"].ValueUpdated -= Cr_ValueUpdated;
			GattCharacteristicList1["Record Access Control Point"] = null;
			GattCharacteristicList1.Remove("Record Access Control Point");
		}
	}

	private async Task<IList<string>> GetConcentration()
	{
		List<string> list = new List<string>();
		using (IService serviceConcentration = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Glucose Meter Extension Service"])))
		{
			ICharacteristic characteristic = await serviceConcentration.GetCharacteristicAsync(Guid.Parse(_Uuids2["Target Glucose Concentration"]));
			if (characteristic != null)
			{
				Log.Info("【BLELib】【BLEDeviceCFL】【GetConcentration】ReadAsync:Service=Glucose Meter Extension Service, Characteristic=" + characteristic.Id.ToString());
				byte[] array = await characteristic.ReadAsync();
				if (array != null)
				{
					Log.Info("【BLELib】【BLEDeviceCFL】【GetConcentration】ReadAsync:Result(byte)=" + array.JoinString(","));
					if (array.Length == 13)
					{
						list.Add(BitConverter.ToInt16(new byte[2]
						{
							array[0],
							array[1]
						}, 0).ToString());
						list.Add(BitConverter.ToInt16(new byte[2]
						{
							array[2],
							array[3]
						}, 0).ToString());
						list.Add(BitConverter.ToInt16(new byte[2]
						{
							array[4],
							array[5]
						}, 0).ToString());
						list.Add(BitConverter.ToInt16(new byte[2]
						{
							array[6],
							array[7]
						}, 0).ToString());
						list.Add(BitConverter.ToInt16(new byte[2]
						{
							array[8],
							array[9]
						}, 0).ToString());
						list.Add(BitConverter.ToInt16(new byte[2]
						{
							array[10],
							array[11]
						}, 0).ToString());
						int num = array[12];
						list.Add(num.ToString());
					}
					Log.Info("【BLELib】【BLEDeviceCFL】【GetConcentration】ReadAsync:Result(string)=" + list.JoinString(","));
				}
			}
		}
		return list;
	}

	private async Task<bool> SetConcentration(string emergencyHigh, string high, string aboveNormal, string belowNormal, string low, string emergencyLow, string accessAuthorityAtaProperty)
	{
		bool bRes = false;
		byte[] bytes = BitConverter.GetBytes(ParseStringToInt16(emergencyLow));
		byte[] bytes2 = BitConverter.GetBytes(ParseStringToInt16(high));
		byte[] bytes3 = BitConverter.GetBytes(ParseStringToInt16(aboveNormal));
		byte[] bytes4 = BitConverter.GetBytes(ParseStringToInt16(belowNormal));
		byte[] bytes5 = BitConverter.GetBytes(ParseStringToInt16(low));
		byte[] bytes6 = BitConverter.GetBytes(ParseStringToInt16(emergencyLow));
		int result = 0;
		int.TryParse(accessAuthorityAtaProperty, out result);
		byte[] data = new byte[13]
		{
			bytes[0],
			bytes[1],
			bytes2[0],
			bytes2[1],
			bytes3[0],
			bytes3[1],
			bytes4[0],
			bytes4[1],
			bytes5[0],
			bytes5[1],
			bytes6[0],
			bytes6[1],
			(byte)result
		};
		using (IService serviceConcentration = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Glucose Meter Extension Service"])))
		{
			ICharacteristic characteristicConcentration = await serviceConcentration.GetCharacteristicAsync(Guid.Parse(_Uuids2["Target Glucose Concentration"]));
			if (characteristicConcentration != null)
			{
				Log.Info("【BLELib】【BLEDeviceCFL】【SetConcentration】WriteAsync:Service=Glucose Meter Extension Service, Characteristic=" + characteristicConcentration.Id.ToString() + ", data=" + BitConverter.ToString(data));
				bRes = await MainThread.InvokeOnMainThreadAsync(async () => await characteristicConcentration.WriteAsync(data));
				Log.Info("【BLELib】【BLEDeviceCFL】【SetConcentration】WriteAsync:Result=" + bRes);
			}
		}
		return bRes;
	}

	private short ParseStringToInt16(string s)
	{
		short result = 0;
		short.TryParse(s, out result);
		return result;
	}

	private async Task<int> GetLastSEQNo()
	{
		ICharacteristic characteristic = GattCharacteristicList1["Record Access Control Point"];
		if (characteristic != null)
		{
			characteristic.WriteType = CharacteristicWriteType.WithResponse;
			DataReceiveCompleteEvent.Reset();
			Log.Info("【BLELib】【BLEDeviceCFL】【GetLastSEQNo】" + Device.Name + ":CanWrite = " + characteristic.CanWrite);
			byte[] data = null;
			byte[] bytes = BitConverter.GetBytes((short)1);
			byte[] obj = new byte[5] { 4, 3, 1, 0, 0 };
			obj[3] = bytes[0];
			obj[4] = bytes[1];
			data = obj;
			Log.Info("【BLELib】【BLEDeviceCFL】【GetLastSEQNo】WriteAsync:Service=Glucose Meter Service, Characteristic=" + characteristic.Id.ToString() + ", data=" + BitConverter.ToString(data));
			bool flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristic.WriteAsync(data));
			Log.Info("【BLELib】【BLEDeviceCFL】【GetLastSEQNo】WriteAsync:Result=" + flag);
			if (!flag)
			{
				Log.Trace("【BLELib】【BLEDeviceCFL】【GetLastSEQNo】" + Device.Name + ":データ件数取得:" + characteristic.Id.ToString() + ":param=" + data.AsEnumerable().JoinString(",") + ":Write Failed");
				return 0;
			}
			Log.Trace("【BLELib】【BLEDeviceCFL】【GetLastSEQNo】" + Device.Name + ":データ件数取得:" + characteristic.Id.ToString() + ":param=" + data.AsEnumerable().JoinString(",") + ":Write Success");
			bool bWaitRes = false;
			await Task.Run(delegate
			{
				bWaitRes = DataReceiveCompleteEvent.Wait(_Timeout);
				if (!bWaitRes)
				{
					StatusChange(Device.Name, BLELibStatus.RCV_TIMEOUT);
				}
			});
			if (!bWaitRes)
			{
				return 0;
			}
		}
		return _MaxSeqNo;
	}

	private async Task<bool> TimeSetting()
	{
		using IService serviceTimeandAlarm = await Device.GetServiceAsync(Guid.Parse(_UuidsS["Time and Alarm Setting Service"]));
		ICharacteristic characteristicTimeandAlarm = await serviceTimeandAlarm.GetCharacteristicAsync(Guid.Parse(_Uuids3["Current Time"]));
		if (characteristicTimeandAlarm == null)
		{
			return false;
		}
		DateTime now = DateTime.Now;
		string text = now.Year.ToString("x4");
		string s = text.Substring(0, 2);
		string s2 = text.Substring(2, 2);
		int num = int.Parse(s, NumberStyles.HexNumber);
		int num2 = int.Parse(s2, NumberStyles.HexNumber);
		byte[] data = new byte[7]
		{
			(byte)num2,
			(byte)num,
			(byte)now.Month,
			(byte)now.Day,
			(byte)now.Hour,
			(byte)now.Minute,
			(byte)now.Second
		};
		characteristicTimeandAlarm.WriteType = CharacteristicWriteType.WithResponse;
		Log.Info("【BLELib】【BLEDeviceCFL】【TimeSetting】WriteAsync:Service=" + serviceTimeandAlarm.Id.ToString() + ", Characteristic=" + characteristicTimeandAlarm.Id.ToString() + ", data=" + BitConverter.ToString(data));
		bool flag = await MainThread.InvokeOnMainThreadAsync(async () => await characteristicTimeandAlarm.WriteAsync(data));
		Log.Info("【BLELib】【BLEDeviceCFL】【TimeSetting】WriteAsync:Result=" + flag);
		Log.Debug(string.Format("{0}:時刻の設定:{1}:Write {2}", new object[3]
		{
			Device.Name,
			characteristicTimeandAlarm.Id.ToString(),
			flag
		}));
		return flag;
	}

	private void _IAdapter_DeviceConnected(object sender, DeviceEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceCFL】【_IAdapter_DeviceConnected】DeviceConnected:Device.Name={0}, Device.Id={1}", new object[2]
		{
			e.Device?.Name,
			e.Device?.Id
		}));
		if (Device == null || !(Device.Name != e.Device?.Name))
		{
			DeviceConnectedEvent.Set();
		}
	}

	private void Cr_ValueUpdated(object sender, CharacteristicUpdatedEventArgs e)
	{
		try
		{
			byte[] array = e.Characteristic.Value.ToArray();
			KeyValuePair<string, ICharacteristic> keyValuePair = GattCharacteristicList1.Where((KeyValuePair<string, ICharacteristic> x) => x.Value == sender).FirstOrDefault();
			Log.Info("【BLELib】【BLEDeviceCFL】【Cr_ValueUpdated】Cr_ValueUpdated:sender=" + keyValuePair.Key + "(" + (sender as ICharacteristic).Uuid + "), buffer=" + BitConverter.ToString(e.Characteristic.Value));
			lock (RecvBufferLock)
			{
				if (keyValuePair.Key == "Glucose Measurement")
				{
					if (_IsPairingSequance || array.Length <= 13)
					{
						return;
					}
					short num = BitConverter.ToInt16(new byte[2]
					{
						array[1],
						array[2]
					}, 0);
					float num2 = Ieee11073ToSingle(new byte[2]
					{
						array[12],
						array[13]
					});
					string type = BitConverter.ToString(new byte[1] { array[14] });
					DateTime date = new DateTime(BitConverter.ToInt16(new byte[2]
					{
						array[3],
						array[4]
					}, 0), array[5], array[6], array[7], array[8], array[9]);
					RecvData recvData = null;
					if (!_RecvBuffer.ContainsKey(num.ToString()))
					{
						recvData = new RecvData();
						_RecvBuffer[num.ToString()] = recvData;
					}
					else
					{
						recvData = _RecvBuffer[num.ToString()];
						GlucoseResult.GlucoseMeasureRecord glucoseMeasureRecord = new GlucoseResult.GlucoseMeasureRecord
						{
							SeqNo = recvData.SeqNo,
							TimeStamp = recvData.Date,
							Glu = recvData.Data,
							Type = recvData.Type
						};
						glucoseMeasureRecord.Meal = recvData.Meal;
						_ReturnBuffer.Result.Add(glucoseMeasureRecord);
						Log.Info("【BLELib】【BLEDeviceCFL】【Cr_ValueUpdated】血糖値取得 Cr_ValueUpdated:" + Device.Name + ":SeqNo=" + glucoseMeasureRecord.SeqNo + ", TimeStamp=" + glucoseMeasureRecord.TimeStamp.ToString("yyyy/MM/dd H:mm:ss") + ", Glu=" + glucoseMeasureRecord.Glu + ", Meal=" + glucoseMeasureRecord.Meal + ", Type=" + glucoseMeasureRecord.Type);
						if (_Handler != null)
						{
							_Handler(_MaxSeqNo, _ReturnBuffer.Result.Count);
						}
					}
					recvData.SeqNo = num.ToString();
					recvData.Date = date;
					recvData.Data = (int)((decimal)num2 * 100000m);
					recvData.Type = type;
					if ((_Mode == ReceiveMode.All && _ReturnBuffer.Result.Count == _MaxSeqNo) || (_Mode == ReceiveMode.Diff && _ReturnBuffer.Result.Count >= _MaxSeqNo) || (_Mode == ReceiveMode.Last && _ReturnBuffer.Result.Count == 1))
					{
						DataReceiveCompleteEvent.Set();
					}
				}
				else if (keyValuePair.Key == "Glucose Measurement Context")
				{
					if (array[0] == 2 && array.Length == 4)
					{
						short num3 = BitConverter.ToInt16(new byte[2]
						{
							array[1],
							array[2]
						}, 0);
						int meal = array[3];
						RecvData recvData2 = null;
						if (!_RecvBuffer.ContainsKey(num3.ToString()))
						{
							recvData2 = new RecvData();
							recvData2.Meal = meal;
							_RecvBuffer[num3.ToString()] = recvData2;
						}
						else
						{
							recvData2 = _RecvBuffer[num3.ToString()];
							GlucoseResult.GlucoseMeasureRecord glucoseMeasureRecord2 = new GlucoseResult.GlucoseMeasureRecord
							{
								SeqNo = recvData2.SeqNo,
								TimeStamp = recvData2.Date,
								Glu = recvData2.Data,
								Type = recvData2.Type
							};
							glucoseMeasureRecord2.Meal = meal;
							_ReturnBuffer.Result.Add(glucoseMeasureRecord2);
							Log.Info("【BLELib】【BLEDeviceCFL】【Cr_ValueUpdated】食前食後取得 Cr_ValueUpdated:" + Device.Name + ":SeqNo=" + glucoseMeasureRecord2.SeqNo + ", TimeStamp=" + glucoseMeasureRecord2.TimeStamp.ToString("yyyy/MM/dd H:mm:ss") + ", Glu=" + glucoseMeasureRecord2.Glu + ", Meal=" + glucoseMeasureRecord2.Meal + ", Type=" + glucoseMeasureRecord2.Type);
							if (_Handler != null)
							{
								_Handler(_MaxSeqNo, _ReturnBuffer.Result.Count);
							}
						}
					}
					if ((_Mode == ReceiveMode.All && _ReturnBuffer.Result.Count == _MaxSeqNo) || (_Mode == ReceiveMode.Diff && _ReturnBuffer.Result.Count >= _MaxSeqNo) || (_Mode == ReceiveMode.Last && _ReturnBuffer.Result.Count == 1))
					{
						DataReceiveCompleteEvent.Set();
					}
				}
				else if (keyValuePair.Key == "Record Access Control Point" && array.Length == 4 && array[0] == 5)
				{
					_MaxSeqNo = BitConverter.ToInt32(new byte[4]
					{
						array[2],
						array[3],
						0,
						0
					}, 0);
					Log.Info("【BLELib】【BLEDeviceCFL】【Cr_ValueUpdated】件数取得 Cr_ValueUpdated:" + Device.Name + ":データ件数=" + _MaxSeqNo);
					DataReceiveCompleteEvent.Set();
				}
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【BLELib】【BLEDeviceCFL】【Cr_ValueUpdated】例外発生：{ex}");
		}
	}

	public void BondStatusBroadcastReceiver_BondStateChanged(object sender, DeviceBondStateChangedEventArgs e)
	{
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

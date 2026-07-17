using System;
using System.Collections.Generic;
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
using Plugin.BLE.Abstractions.Exceptions;
using Xamarin.Essentials;
using Xamarin.Forms;

namespace BLELib.BLEDevice;

public class BLEDeviceNBCM : IBLEDevice
{
	public class BodyCompositionResult
	{
		public class BodyCompositionRecord
		{
			public bool MeasurementUnit { get; set; }

			public double? WeightScale { get; set; }

			public DateTime? TimeStamp { get; set; }

			public double? BMI { get; set; }

			public double? BodyFatPercentage { get; set; }

			public double? BasalMetabolism { get; set; }

			public double? MuscleMass { get; set; }

			public double? BodyWaterMass { get; set; }
		}

		public IList<BodyCompositionRecord> Result { get; set; }
	}

	public class RegisterNewUserResult
	{
		public bool IsSuccess { get; set; }

		public int? UserNo { get; set; }

		public int? Error { get; set; }
	}

	public class UserAuthenticationResult
	{
		public bool IsSuccess { get; set; }

		public int Error { get; set; }
	}

	public enum OperationModeType
	{
		MeasurementMode = 2,
		SettingMode
	}

	private static ILoggingService Log = LogManager.GetLogger();

	private IAdapter _IAdapter;

	private CancellationTokenSource _cancelToken;

	private RegisterNewUserResult _registerNewUserResult;

	private bool _deleteUserResult;

	private UserAuthenticationResult _userAuthenticationResult;

	private bool _statusChangeResult;

	private bool _productionModeChangedResult;

	private bool _factoryShippingModeChangedEventResult;

	private bool _userNoAvailabilityCheckEventResult;

	private byte _freeColorResult;

	private bool _setColorResult;

	protected ManualResetEventSlim DeviceConnectedEvent { get; set; }

	protected ManualResetEventSlim DeviceBondedEvent { get; set; }

	protected ManualResetEventSlim RegisterNewUserEvent { get; set; }

	protected ManualResetEventSlim DeleteUserEvent { get; set; }

	protected ManualResetEventSlim UserAuthenticationEvent { get; set; }

	protected ManualResetEventSlim WeightScaleMeasurementEvent { get; set; } = new ManualResetEventSlim(initialState: false);

	protected ManualResetEventSlim BodyCompositionMeasurementEvent { get; set; } = new ManualResetEventSlim(initialState: false);

	protected ManualResetEventSlim StatusChangeEvent { get; set; }

	protected bool MeasurementCompletedFlag { get; set; }

	protected ManualResetEventSlim ProductionModeChangedEvent { get; set; }

	protected ManualResetEventSlim FactoryShippingModeChangedEvent { get; set; }

	protected ManualResetEventSlim UserNoAvailabilityCheckEvent { get; set; }

	protected ManualResetEventSlim GetFreeColorEvent { get; set; }

	protected ManualResetEventSlim SetColorEvent { get; set; }

	protected BodyCompositionResult BodyComposition { get; set; } = new BodyCompositionResult
	{
		Result = new List<BodyCompositionResult.BodyCompositionRecord>()
	};

	protected BodyCompositionResult.BodyCompositionRecord BodyCompositionRecord { get; set; } = new BodyCompositionResult.BodyCompositionRecord();

	public string DeviceName => "NBCM";

	public IDevice Device { get; set; }

	public event EventHandler<BLELibStatusEventArgs> StateChanged;

	public BLEDeviceNBCM()
	{
		_IAdapter = CrossBluetoothLE.Current.Adapter;
	}

	public BLEDeviceNBCM(IAdapter iAdapter = null)
	{
		_IAdapter = iAdapter ?? CrossBluetoothLE.Current.Adapter;
	}

	public void BondStatusBroadcastReceiver_BondStateChanged(object sender, DeviceBondStateChangedEventArgs e)
	{
		Log.Info("【BLELib】【BLEDeviceNBCM】【BondStatusBroadcastReceiver_BondStateChanged】sender=" + sender?.ToString() + ",e.Device=" + e.Device?.Name + ", e.State=" + e.State);
		if (e.Device.Name.StartsWith(Device.Name.Trim()) && e.State == DeviceBondState.Bonded)
		{
			DeviceBondedEvent.Set();
		}
	}

	public object CreateDevice(IDevice device)
	{
		BLEDeviceNBCM bLEDeviceNBCM = new BLEDeviceNBCM();
		bLEDeviceNBCM.Device = device;
		bLEDeviceNBCM.DeviceConnectedEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.DeviceBondedEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.RegisterNewUserEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.DeleteUserEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.UserAuthenticationEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.WeightScaleMeasurementEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.BodyCompositionMeasurementEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.StatusChangeEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.ProductionModeChangedEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.FactoryShippingModeChangedEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.DeleteUserEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.UserNoAvailabilityCheckEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.GetFreeColorEvent = new ManualResetEventSlim(initialState: false);
		bLEDeviceNBCM.SetColorEvent = new ManualResetEventSlim(initialState: false);
		MeasurementCompletedFlag = false;
		return bLEDeviceNBCM;
	}

	public void InitializeDeviceInfo(IDevice device)
	{
		Device = device;
		DeviceConnectedEvent = new ManualResetEventSlim(initialState: false);
		DeviceBondedEvent = new ManualResetEventSlim(initialState: false);
		RegisterNewUserEvent = new ManualResetEventSlim(initialState: false);
		DeleteUserEvent = new ManualResetEventSlim(initialState: false);
		UserAuthenticationEvent = new ManualResetEventSlim(initialState: false);
		WeightScaleMeasurementEvent = new ManualResetEventSlim(initialState: false);
		BodyCompositionMeasurementEvent = new ManualResetEventSlim(initialState: false);
		StatusChangeEvent = new ManualResetEventSlim(initialState: false);
		ProductionModeChangedEvent = new ManualResetEventSlim(initialState: false);
		FactoryShippingModeChangedEvent = new ManualResetEventSlim(initialState: false);
		DeleteUserEvent = new ManualResetEventSlim(initialState: false);
		UserNoAvailabilityCheckEvent = new ManualResetEventSlim(initialState: false);
		GetFreeColorEvent = new ManualResetEventSlim(initialState: false);
		SetColorEvent = new ManualResetEventSlim(initialState: false);
		_deleteUserResult = false;
		MeasurementCompletedFlag = false;
	}

	public async Task<IList<string>> Pairing(IList<string> param = null)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【Pairing】Device.Name={0}, Device.Id={1}", new object[2]
		{
			Device?.Name,
			Device?.Id
		}));
		IList<string> result;
		try
		{
			_ = 1;
			try
			{
				_registerNewUserResult = new RegisterNewUserResult();
				await DeviceConnect();
				byte? userNo = null;
				string serialNumber = string.Empty;
				await Task.Run(async delegate
				{
					DeviceConnectedEvent.Wait();
					serialNumber = await GetSerialNumber();
					await StatusNotificationLog();
					if (!(await UserNoAvailabilityCheck()))
					{
						Log.Info("【BLELib】【BLEDeviceNBCM】ユーザーNo空き無し");
						if (!(await DeleteUser(224)))
						{
							throw new Exception("ユーザー削除に失敗しました。");
						}
					}
					if (!(await RegisterNewUser()))
					{
						throw new Exception("ユーザー登録に失敗しました。");
					}
					userNo = (byte)_registerNewUserResult.UserNo.Value;
					if (!(await UserAuthentication(userNo.Value)))
					{
						throw new Exception("ユーザー認証に失敗しました。");
					}
					await UpdateUserPersonalInformation(param);
					if (!(await SetOperationMode(OperationModeType.SettingMode)))
					{
						throw new Exception("設定モードへの遷移に失敗しました。");
					}
					if (!(await SetDateTime()))
					{
						throw new Exception("時計の設定に失敗しました。");
					}
					if (!(await MeasurementResultDisplaySetting()))
					{
						throw new Exception("測定結果表示の設定に失敗しました。");
					}
					if (!(await BodyFatPercentageMeasurement()))
					{
						throw new Exception("体脂肪測定設定に失敗しました。");
					}
					if (!(await MedicalExaminationModeDisabled()))
					{
						throw new Exception("検診モードを無効にできませんでした。");
					}
					if (!(await AirplaneModeDisabled()))
					{
						throw new Exception("機内モードを無効にできませんでした。");
					}
				}, _cancelToken.Token);
				Log.Info($"【BLELib】【BLEDeviceNBCM】【Pairing】Device.Name={Device?.Name}, Device.Id={Device?.Id} SerialNumber={serialNumber} UserNo={userNo.Value.ToString()}");
				result = new string[4]
				{
					Device.Name,
					Device.Id.ToString(),
					serialNumber,
					userNo.Value.ToString()
				};
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNBCM】【Pairing】例外発生：{ex}");
				StatusChange(Device.Name, BLELibStatus.PAIR_ERR, ex.Message);
				result = new string[4]
				{
					string.Empty,
					string.Empty,
					string.Empty,
					string.Empty
				};
			}
		}
		finally
		{
			_cancelToken?.Dispose();
			_cancelToken = null;
			_IAdapter.DeviceConnected -= OnDeviceConnected;
			await _IAdapter.DisconnectDeviceAsync(Device);
		}
		return result;
	}

	public async Task<object> ReciveStart(Action<int, int> handler, int timeout, IList<string> param = null)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【ReciveStart】Device.Name={0}, Device.Id={1}", new object[2]
		{
			Device?.Name,
			Device?.Id
		}));
		try
		{
			_ = 1;
			try
			{
				BodyComposition = new BodyCompositionResult
				{
					Result = new List<BodyCompositionResult.BodyCompositionRecord>()
				};
				await DeviceConnect();
				await Task.Run(async delegate
				{
					DeviceConnectedEvent.Wait();
					if (!(await UserAuthentication(Convert.ToByte(param[4]))))
					{
						throw new Exception("ユーザー認証に失敗しました。");
					}
					if (DeviceInfo.Platform == DevicePlatform.iOS)
					{
						DependencyService.Get<IBLEDeviceNBCMDependencyService>().EnableBodyComposition(Device);
					}
					await StatusNotificationLog();
					await UpdateUserPersonalInformation(param);
					if (!(await SetDateTime()))
					{
						throw new Exception("時計の設定に失敗しました。");
					}
					await SetOperationMode(OperationModeType.MeasurementMode);
					await WeightScaleMeasurement();
					await BodyCompositionMeasurement();
					await WaitUntilMeasurementCompleted();
					WeightScaleMeasurementEvent.Wait();
					BodyCompositionMeasurementEvent.Wait();
				}, _cancelToken.Token);
				StatusChange(Device.Name, BLELibStatus.RCV_END);
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNBCM】【ReciveStart】例外発生：{ex}");
				StatusChange(Device.Name, BLELibStatus.RCV_ERR, ex.Message);
				return null;
			}
		}
		finally
		{
			_cancelToken?.Dispose();
			_cancelToken = null;
			_IAdapter.DeviceConnected -= OnDeviceConnected;
			await _IAdapter.DisconnectDeviceAsync(Device);
		}
		Log.Debug($"【BLELib】【BLEDeviceNBCM】【ReciveStart】BodyComposition.Result count : {BodyComposition.Result.Count}");
		return BodyComposition;
	}

	public async Task ReceiveStop()
	{
		_cancelToken?.Cancel();
		if (Device == null)
		{
			Log.Info("【BLELib】【BLEDeviceNBCM】【ReceiveStop】受信停止対象の器機情報がnullです。");
			return;
		}
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【ReceiveStop】DisconnectDeviceAsync:Device.Name={0}, Device.Id={1}", new object[2]
		{
			Device?.Name,
			Device?.Id
		}));
		_IAdapter.DeviceConnected -= OnDeviceConnected;
		await _IAdapter.DisconnectDeviceAsync(Device);
	}

	public void Cancel()
	{
		Log.Info("【BLELib】【BLEDeviceNBCM】【Cancel】Cancel all tasks.");
		_cancelToken?.Cancel();
	}

	public async Task<bool> DeleteUserByParam(int timeout, IList<string> param)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【DeleteUser】Device.Name={0}, Device.Id={1}, timeout={2}, param={3}", Device?.Name, Device?.Id, timeout, param.JoinString(",")));
		bool result;
		try
		{
			_ = 1;
			try
			{
				await DeviceConnect();
				await Task.Run(async delegate
				{
					DeviceConnectedEvent.Wait();
					await StatusNotificationLog();
					if (!(await DeleteUser(byte.Parse(param[0]))))
					{
						throw new Exception("ユーザー削除に失敗しました。");
					}
				}, _cancelToken.Token);
				StatusChange(Device.Name, BLELibStatus.USER_DELETE_END);
				Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【DeleteUser】USER_DELETE_END Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				result = true;
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNBCM】【DeleteUser】例外発生：{ex}");
				throw;
			}
		}
		finally
		{
			_cancelToken?.Dispose();
			_cancelToken = null;
			_IAdapter.DeviceConnected -= OnDeviceConnected;
			await _IAdapter.DisconnectDeviceAsync(Device);
		}
		return result;
	}

	public async Task<IList<object>> GetFreeColor(int timeout, IList<string> param)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【GetFreeColor】Device.Name={0}, Device.Id={1}, timeout={2}, param={3}", Device?.Name, Device?.Id, timeout, param.JoinString(",")));
		List<object> result = new List<object>();
		IList<object> result2;
		try
		{
			_ = 1;
			try
			{
				await DeviceConnect();
				await Task.Run(async delegate
				{
					DeviceConnectedEvent.Wait();
					await StatusNotificationLog();
					result = await FreeColor();
				}, _cancelToken.Token);
				if (result == null || result.Count == 0)
				{
					throw new Exception("空き色の取得に失敗しました。");
				}
				StatusChange(Device.Name, BLELibStatus.GET_FREE_COLOR_END);
				Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【GetFreeColor】USER_DELETE_END Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				result2 = result;
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNBCM】【GetFreeColor】例外発生：{ex}");
				throw;
			}
		}
		finally
		{
			_cancelToken?.Dispose();
			_cancelToken = null;
			_IAdapter.DeviceConnected -= OnDeviceConnected;
			await _IAdapter.DisconnectDeviceAsync(Device);
		}
		return result2;
	}

	public async Task<bool> RegisterUserColor(int timeout, IList<string> param)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【RegisterUserColor】Device.Name={0}, Device.Id={1}, timeout={2}, param={3}", Device?.Name, Device?.Id, timeout, param.JoinString(",")));
		bool result;
		try
		{
			_ = 1;
			try
			{
				await DeviceConnect();
				await Task.Run(async delegate
				{
					DeviceConnectedEvent.Wait();
					await StatusNotificationLog();
					if (!(await UserAuthentication(byte.Parse(param[4]))))
					{
						throw new Exception("ユーザー認証に失敗しました。");
					}
					if (!(await SetColor(byte.Parse(param[5]))))
					{
						throw new Exception("色設定に失敗しました。");
					}
				}, _cancelToken.Token);
				StatusChange(Device.Name, BLELibStatus.REGISTER_USER_END);
				Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【RegisterUserColor】REGISTER_USER_END Device.Name={0}, Device.Id={1}", new object[2]
				{
					Device?.Name,
					Device?.Id
				}));
				result = true;
			}
			catch (Exception ex)
			{
				Log.Error($"【BLELib】【BLEDeviceNBCM】【RegisterUserColor】例外発生：{ex}");
				throw;
			}
		}
		finally
		{
			_cancelToken?.Dispose();
			_cancelToken = null;
			_IAdapter.DeviceConnected -= OnDeviceConnected;
			await _IAdapter.DisconnectDeviceAsync(Device);
		}
		return result;
	}

	private void StatusChange(string deviceName, BLELibStatus status, string message = "")
	{
		this.StateChanged?.Invoke(this, new BLELibStatusEventArgs(deviceName, status, message));
	}

	private byte[] GetDateTime()
	{
		DateTime now = DateTime.Now;
		short value = (short)now.Year;
		byte b = (byte)now.Month;
		byte b2 = (byte)now.Day;
		byte b3 = (byte)now.Hour;
		byte b4 = (byte)now.Minute;
		byte b5 = (byte)now.Second;
		byte[] bytes = BitConverter.GetBytes(value);
		return new byte[10]
		{
			bytes[0],
			bytes[1],
			b,
			b2,
			b3,
			b4,
			b5,
			0,
			0,
			0
		};
	}

	private async Task DeviceConnect()
	{
		DeviceConnectedEvent.Reset();
		_cancelToken = new CancellationTokenSource();
		_IAdapter.DeviceConnected -= OnDeviceConnected;
		_IAdapter.DeviceConnected += OnDeviceConnected;
		if (_IAdapter.IsScanning)
		{
			Log.Info("【BLELib】【BLEDeviceNBCM】【DeviceConnect】scan already processing, stop scan.");
			await _IAdapter.StopScanningForDevicesAsync();
		}
		if (DeviceInfo.Platform == DevicePlatform.Android && DeviceInfo.Manufacturer == "FUJITSU" && (DeviceInfo.Model == "F-03K" || DeviceInfo.Model == "F-42A" || DeviceInfo.Model == "F-01L"))
		{
			ConnectParameters parameters = new ConnectParameters(autoConnect: false, forceBleTransport: true);
			await RetryUtil.ExecuteAsync(async delegate
			{
				await _IAdapter.ConnectToDeviceAsync(Device, parameters, _cancelToken.Token);
			}, 10, 100, typeof(DeviceConnectionException));
		}
		else
		{
			ConnectParameters connectParameters = new ConnectParameters(autoConnect: true, forceBleTransport: true);
			await _IAdapter.ConnectToDeviceAsync(Device, connectParameters, _cancelToken.Token);
		}
		Log.Info($"【BLELib】【BLEDeviceNBCM】【DeviceConnect】ConnectToDeviceAsync:Device.State={Device.State}");
	}

	private async Task<string> GetSerialNumber()
	{
		if (DeviceInfo.Platform == DevicePlatform.iOS)
		{
			await Task.Delay(2000);
		}
		string text = string.Empty;
		using (IService service = await Device.GetServiceAsync(Guid.Parse("0000180A-0000-1000-8000-00805F9B34FB")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A25-0000-1000-8000-00805F9B34FB"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【Pairing】Serial Number ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			DeviceBondedEvent.Wait();
			if (DeviceInfo.Platform == DevicePlatform.iOS)
			{
				await Task.Delay(1000);
			}
			byte[] array = await characteristic.ReadAsync();
			text = StringUtils.ToStringFromBytes(array);
			Log.Info("【BLELib】【BLEDeviceNBCM】【Pairing】Serial Number ReadAsync:Result=" + BitConverter.ToString(array) + ":SerialNumber=" + text);
		}
		return text;
	}

	private async Task StatusNotificationLog()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66"));
		ICharacteristic obj = await service.GetCharacteristicAsync(Guid.Parse("11127002-B364-11E4-AB27-0800200C9A66"));
		obj.ValueUpdated += delegate(object s, CharacteristicUpdatedEventArgs e)
		{
			byte[] value = e.Characteristic.Value;
			Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】[" + BitConverter.ToString(value) + "]");
			if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 84 && value[3] == 32)
			{
				if (value[4] == 0)
				{
					StatusChange(Device.Name, BLELibStatus.NBCM_STEP_ON);
				}
				else if (value[4] == 1)
				{
					StatusChange(Device.Name, BLELibStatus.NBCM_RCV_START);
				}
				else if (value[4] == 2)
				{
					StatusChange(Device.Name, BLELibStatus.NBCM_STEP_OFF);
				}
				else if (value[4] == 3)
				{
					StatusChange(Device.Name, BLELibStatus.NBCM_RCV_CANCELED);
				}
				else if (value[4] == 4)
				{
					MeasurementCompletedFlag = true;
					StatusChange(Device.Name, BLELibStatus.NBCM_RCV_END);
				}
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 5 && value[3] == 10)
			{
				Log.Info($"【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】動作モード設定成否：{value[4] == 0}");
				_statusChangeResult = value[4] == 0;
				StatusChangeEvent.Set();
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 20 && value[3] == 24)
			{
				Log.Info($"【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】身長設定成否：{value[4] == 0}");
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 5 && value[3] == 32)
			{
				Log.Info($"【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】表示設定成否：{value[4] == 0}");
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 5 && value[3] == 34)
			{
				Log.Info($"【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】体脂肪率測定設定：{value[4] == 0}");
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 5 && value[3] == 40)
			{
				Log.Info($"【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】検診モード無効設定：{value[4] == 0}");
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 5 && value[3] == 44)
			{
				Log.Info($"【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】機内モードOFF設定：{value[4] == 0}");
			}
			else if (value.Length == 6 && value[0] == 5 && value[1] == 0 && value[2] == 20 && value[3] == 25)
			{
				Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】身長読出要求：" + BitConverter.ToString(value));
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 176 && value[3] == 0)
			{
				Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】生産用モード移行要求：" + BitConverter.ToString(value));
				_productionModeChangedResult = value[4] == 0;
				ProductionModeChangedEvent.Set();
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 175 && value[3] == 0)
			{
				Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】工場出荷設定要求：" + BitConverter.ToString(value));
				_factoryShippingModeChangedEventResult = value[4] == 0;
				FactoryShippingModeChangedEvent.Set();
			}
			else if ((value.Length == 5 || value.Length == 6) && value[1] == 0 && value[2] == 20 && value[3] == 19)
			{
				Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】ユーザーNo削除要求：" + BitConverter.ToString(value));
				_deleteUserResult = value[value.Length - 1] == 0;
				DeleteUserEvent.Set();
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 20 && value[3] == 18)
			{
				Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】ユーザーNo空き状況要求：" + BitConverter.ToString(value));
				_userNoAvailabilityCheckEventResult = value[4] == 0;
				UserNoAvailabilityCheckEvent.Set();
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 65 && value[3] == 2)
			{
				Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】空き色取得要求：" + BitConverter.ToString(value));
				_freeColorResult = value[4];
				GetFreeColorEvent.Set();
			}
			else if (value.Length == 5 && value[0] == 4 && value[1] == 0 && value[2] == 65 && value[3] == 0)
			{
				Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】色設定要求：" + BitConverter.ToString(value));
				_setColorResult = value[4] == 0;
				SetColorEvent.Set();
			}
		};
		await obj.StartUpdatesAsync();
		Log.Info("【BLELib】【BLEDeviceNBCM】【StatusNotificationLog】CCCD");
	}

	private async Task<bool> SetOperationMode(OperationModeType operationMode)
	{
		bool result = false;
		StatusChangeEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【SetOperationMode】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			byte[] obj = new byte[5] { 4, 1, 5, 10, 0 };
			obj[4] = (byte)operationMode;
			byte[] request = obj;
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【SetOperationMode】Custom :WriteAsync {0} request:{1}", new object[2]
			{
				result,
				BitConverter.ToString(request)
			}));
			StatusChangeEvent.Wait();
		}
		return _statusChangeResult;
	}

	private async Task<bool> RegisterNewUser()
	{
		_registerNewUserResult = new RegisterNewUserResult();
		RegisterNewUserEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("0000181C-0000-1000-8000-00805F9B34FB")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A9F-0000-1000-8000-00805F9B34FB"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【RegisterNewUser】User Control ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			characteristic.ValueUpdated -= UserControlOnValueUpdated;
			characteristic.ValueUpdated += UserControlOnValueUpdated;
			await characteristic.StartUpdatesAsync();
			bool result = false;
			byte[] request = new byte[3] { 1, 226, 7 };
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【RegisterNewUser】WriteAsync:{0} request:{1}", new object[2]
			{
				result,
				BitConverter.ToString(request)
			}));
			if (!result)
			{
				characteristic.ValueUpdated -= UserControlOnValueUpdated;
				return result;
			}
			RegisterNewUserEvent.Wait();
		}
		return _registerNewUserResult.IsSuccess;
	}

	private async Task<bool> UserNoAvailabilityCheck()
	{
		_userNoAvailabilityCheckEventResult = false;
		UserNoAvailabilityCheckEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【UserNoAvailabilityCheck】Custom ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			bool result = false;
			byte[] request = new byte[4] { 3, 1, 20, 18 };
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
			Log.Info("【BLELib】【BLEDeviceNBCM】【UserNoAvailabilityCheck】ユーザーNo空き状況要求 WriteAsync:[" + BitConverter.ToString(request) + "]");
			UserNoAvailabilityCheckEvent.Wait();
		}
		return _userNoAvailabilityCheckEventResult;
	}

	private async Task<bool> DeleteSettings()
	{
		ProductionModeChangedEvent.Reset();
		FactoryShippingModeChangedEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【DeleteSettings】Custom ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			bool result = false;
			byte[] request = new byte[4] { 3, 1, 176, 0 };
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
			Log.Info("【BLELib】【BLEDeviceNBCM】【DeleteSettings】生産用モード移行要求 WriteAsync:[" + BitConverter.ToString(request) + "] ");
			ProductionModeChangedEvent.Wait();
			if (!_productionModeChangedResult)
			{
				return false;
			}
			byte[] factorySettingRequest = new byte[4] { 3, 1, 175, 0 };
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(factorySettingRequest));
			Log.Info("【BLELib】【BLEDeviceNBCM】【DeleteSettings】工場出荷設定要求 WriteAsync:[" + BitConverter.ToString(factorySettingRequest) + "]");
			FactoryShippingModeChangedEvent.Wait();
		}
		return _factoryShippingModeChangedEventResult;
	}

	private async Task<bool> DeleteUser(byte userNo)
	{
		_deleteUserResult = false;
		DeleteUserEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【DeleteUser({0})】Custom ReadAsync:Service={1}, Characteristic={2}", new object[3]
			{
				userNo,
				service.Id.ToString(),
				characteristic.Id.ToString()
			}));
			bool result = false;
			byte[] obj = new byte[5] { 4, 1, 20, 19, 0 };
			obj[4] = userNo;
			byte[] request = obj;
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【DeleteUser({0})】Custom WriteAsync:{1} request:{2}", new object[3]
			{
				userNo,
				result,
				BitConverter.ToString(request)
			}));
			DeleteUserEvent.Wait();
		}
		return _deleteUserResult;
	}

	private async Task<bool> DeleteUser()
	{
		_deleteUserResult = false;
		DeleteUserEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("0000181C-0000-1000-8000-00805F9B34FB")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A9F-0000-1000-8000-00805F9B34FB"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【DeleteUser】User Control ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			characteristic.ValueUpdated -= UserDeleteOnValueUpdated;
			characteristic.ValueUpdated += UserDeleteOnValueUpdated;
			await characteristic.StartUpdatesAsync();
			bool result = false;
			byte[] request = new byte[1] { 3 };
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【DeleteUser】WriteAsync:{0} characteristic:{1}", new object[2]
			{
				result,
				BitConverter.ToString(characteristic.Value)
			}));
			DeleteUserEvent.Wait();
		}
		return _deleteUserResult;
	}

	private async Task<bool> UserAuthentication(byte userNo)
	{
		_userAuthenticationResult = new UserAuthenticationResult();
		UserAuthenticationEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("0000181C-0000-1000-8000-00805F9B34FB")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A9F-0000-1000-8000-00805F9B34FB"));
			if (DeviceInfo.Platform == DevicePlatform.iOS)
			{
				await Task.Delay(1000);
			}
			Log.Info("【BLELib】【BLEDeviceNBCM】【UserAuthentication】User Control ReadAsync:Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			characteristic.ValueUpdated -= UserAuthenticationOnValueUpdated;
			characteristic.ValueUpdated += UserAuthenticationOnValueUpdated;
			await characteristic.StartUpdatesAsync();
			bool result = false;
			byte[] obj = new byte[4] { 2, 0, 226, 7 };
			obj[1] = userNo;
			byte[] request = obj;
			await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【UserAuthentication】WriteAsync:{0} request:{1}", new object[2]
			{
				result,
				BitConverter.ToString(request)
			}));
			if (!result)
			{
				characteristic.ValueUpdated -= UserAuthenticationOnValueUpdated;
				return result;
			}
			UserAuthenticationEvent.Wait();
			characteristic.ValueUpdated -= UserAuthenticationOnValueUpdated;
		}
		return _userAuthenticationResult.IsSuccess;
	}

	private async Task<bool> UpdateUserPersonalInformation(IList<string> param)
	{
		if (param.Count < 4)
		{
			return false;
		}
		string name = param[0];
		string sex = param[1];
		string s = param[2];
		string s2 = param[3];
		if (!int.TryParse(s, out var height))
		{
			height = 0;
		}
		DateTime? birthday = null;
		if (DateTime.TryParse(s2, out var result))
		{
			birthday = result;
		}
		IService service = await Device.GetServiceAsync(Guid.Parse("0000181C-0000-1000-8000-00805F9B34FB"));
		try
		{
			ICharacteristic firstNameCharacteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A8A-0000-1000-8000-00805F9B34FB"));
			ICharacteristic heightCharacteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A8E-0000-1000-8000-00805F9B34FB"));
			ICharacteristic birthdayCharacteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A85-0000-1000-8000-00805F9B34FB"));
			await Task.Delay(1000);
			Log.Info("【BLELib】【BLEDeviceNBCM】【UpdateUserPersonalInformation】User Data ReadAsync:Service=" + service.Id.ToString());
			bool result2 = false;
			await MainThread.InvokeOnMainThreadAsync(async delegate
			{
				if (!string.IsNullOrEmpty(name))
				{
					byte[] nameRequest = Encoding.UTF8.GetBytes(name);
					result2 = await firstNameCharacteristic.WriteAsync(nameRequest);
					Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【UpdateUserPersonalInformation】firstname書き込み結果:{0} byte:{1} name:{2}", new object[3]
					{
						result2,
						BitConverter.ToString(nameRequest),
						name
					}));
					if (!result2)
					{
						Log.Error("氏名の設定に失敗しました。");
					}
				}
				int sexRequest = 0;
				if (sex == "2")
				{
					sexRequest = 1;
				}
				else if (sex != "1" && sex != "2")
				{
					sexRequest = 2;
				}
				ICharacteristic sexCharacteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A8C-0000-1000-8000-00805F9B34FB"));
				await Task.Delay(1000);
				result2 = await sexCharacteristic.WriteAsync(new byte[1] { (byte)sexRequest });
				Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【UpdateUserPersonalInformation】性別書き込み結果:{0} [{1}]", new object[2]
				{
					result2,
					BitConverter.ToString(new byte[1] { (byte)sexRequest })
				}));
				if (!result2 && DeviceInfo.Platform != DevicePlatform.iOS)
				{
					throw new Exception("性別の設定に失敗しました。");
				}
				if (!result2 && DeviceInfo.Platform == DevicePlatform.iOS)
				{
					Log.Error("[iOS]性別の設定に失敗しました。");
				}
				byte[] heightBytes = BitConverter.GetBytes((short)height);
				result2 = await heightCharacteristic.WriteAsync(heightBytes.ToArray());
				Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【UpdateUserPersonalInformation】身長書き込み結果:{0} [{1}]", new object[2]
				{
					result2,
					BitConverter.ToString(heightBytes.ToArray())
				}));
				if (!result2 && DeviceInfo.Platform != DevicePlatform.iOS)
				{
					throw new Exception("身長の設定に失敗しました。");
				}
				if (!result2 && DeviceInfo.Platform == DevicePlatform.iOS)
				{
					Log.Error("[iOS]身長の設定に失敗しました。");
				}
				if (birthday.HasValue)
				{
					byte[] bytes = BitConverter.GetBytes((short)birthday.Value.Year);
					byte b = (byte)birthday.Value.Month;
					byte b2 = (byte)birthday.Value.Day;
					byte[] nameRequest = new byte[4]
					{
						bytes[0],
						bytes[1],
						b,
						b2
					};
					result2 = await birthdayCharacteristic.WriteAsync(nameRequest);
					Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【UpdateUserPersonalInformation】誕生日書き込み結果:{0} [{1}]", new object[2]
					{
						result2,
						BitConverter.ToString(nameRequest)
					}));
					if (!result2 && DeviceInfo.Platform != DevicePlatform.iOS)
					{
						throw new Exception("誕生日の設定に失敗しました。");
					}
					if (!result2 && DeviceInfo.Platform == DevicePlatform.iOS)
					{
						Log.Error("[iOS]誕生日の設定に失敗しました。");
					}
				}
			});
		}
		finally
		{
			if (service != null)
			{
				service.Dispose();
			}
		}
		Log.Info("【BLELib】【BLEDeviceNBCM】【UpdateUserPersonalInformation】正常に設定されました。");
		return true;
	}

	private async Task<bool> SetHeight(IList<string> param)
	{
		string text = param[2];
		if (string.IsNullOrEmpty(text))
		{
			return false;
		}
		if (!double.TryParse(text, out var result))
		{
			Log.Error("【BLELib】【BLEDeviceNBCM】【SetHeight】身長に不正な値が設定されています。" + text);
			return false;
		}
		short value = (short)(result * 10.0);
		byte[] bytes = BitConverter.GetBytes(value);
		using IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【SetHeight】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] obj = new byte[6] { 5, 1, 20, 24, 0, 0 };
		obj[4] = bytes[1];
		obj[5] = bytes[0];
		byte[] request = obj;
		bool result2 = false;
		await MainThread.InvokeOnMainThreadAsync(async () => result2 = await characteristic.WriteAsync(request));
		Log.Info($"【BLELib】【BLEDeviceNBCM】【SetHeight】身長書き込み成否：{result2} WriteAsync:Service={service.Id.ToString()}, Characteristic={characteristic.Id.ToString()} request={request}|");
		return result2;
	}

	private async Task<bool> MeasurementResultDisplaySetting()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【MeasurementResultDisplaySetting】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] request = new byte[5] { 4, 1, 5, 32, 1 };
		bool result = false;
		await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
		Log.Info($"【BLELib】【BLEDeviceNBCM】【MeasurementResultDisplaySetting】表示設定書き込み成否：{result} WriteAsync:Service={service.Id.ToString()}, Characteristic={characteristic.Id.ToString()} request={request}|");
		return result;
	}

	private async Task<bool> BodyFatPercentageMeasurement()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【BodyFatPercentageMeasurement】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] request = new byte[5] { 4, 1, 5, 34, 1 };
		bool result = false;
		await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
		Log.Info($"【BLELib】【BLEDeviceNBCM】【BodyFatPercentageMeasurement】体脂肪率測定設定書き込み成否：{result} WriteAsync:Service={service.Id.ToString()}, Characteristic={characteristic.Id.ToString()} request={request}|");
		return result;
	}

	private async Task<bool> MedicalExaminationModeDisabled()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【MedicalExaminationModeDisabled】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] request = new byte[5] { 4, 1, 5, 40, 0 };
		bool result = false;
		await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
		Log.Info($"【BLELib】【BLEDeviceNBCM】【MedicalExaminationModeDisabled】検診モード無効設定書き込み成否：{result} WriteAsync:Service={service.Id.ToString()}, Characteristic={characteristic.Id.ToString()} request={request}|");
		return result;
	}

	private async Task<bool> AirplaneModeDisabled()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【AirplaneModeDisabled】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] request = new byte[5] { 4, 1, 5, 44, 0 };
		bool result = false;
		await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
		Log.Info($"【BLELib】【BLEDeviceNBCM】【AirplaneModeDisabled】機内モードOFF書き込み成否：{result} WriteAsync:Service={service.Id.ToString()}, Characteristic={characteristic.Id.ToString()} request={request}|");
		return result;
	}

	private async Task<bool> SetDateTime()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("00001805-0000-1000-8000-00805F9B34FB"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A2B-0000-1000-8000-00805F9B34FB"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【SetDateTime】CurrentTime :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] datetime = GetDateTime();
		bool result = false;
		await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(datetime));
		Log.Info($"【BLELib】【BLEDeviceNBCM】【SetDateTime】時計設定 成否：{result} WriteAsync:Service={service.Id.ToString()}, Characteristic={characteristic.Id.ToString()}, data={BitConverter.ToString(datetime)}");
		return result;
	}

	private async Task DeviceDisconnect()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【DeviceDisconnect】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		byte[] request = new byte[4] { 3, 1, 19, 0 };
		bool result = false;
		await MainThread.InvokeOnMainThreadAsync(async () => result = await characteristic.WriteAsync(request));
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【DeviceDisconnect】切断 成否：{0} WriteAsync:Service={1}, Characteristic={2}", new object[3]
		{
			result,
			service.Id.ToString(),
			characteristic.Id.ToString()
		}));
	}

	private async Task WaitUntilMeasurementCompleted()
	{
		int count = 0;
		while (!MeasurementCompletedFlag)
		{
			await Task.Delay(1000);
			count++;
			if (count >= 120)
			{
				_cancelToken.Cancel();
				_cancelToken.Token.ThrowIfCancellationRequested();
			}
		}
	}

	private async Task WeightScaleMeasurement()
	{
		using IService service = await Device.GetServiceAsync(Guid.Parse("0000181D-0000-1000-8000-00805F9B34FB"));
		ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A9D-0000-1000-8000-00805F9B34FB"));
		Log.Info("【BLELib】【BLEDeviceNBCM】【WeightScaleMeasurement】Weight Scale :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
		characteristic.ValueUpdated -= WeightScaleMeasurementOnValueUpdated;
		characteristic.ValueUpdated += WeightScaleMeasurementOnValueUpdated;
		await characteristic.StartUpdatesAsync();
		Log.Info("【BLELib】【BLEDeviceNBCM】【WeightScaleMeasurement】CCCD");
	}

	private async Task BodyCompositionMeasurement()
	{
		IService service = null;
		try
		{
			await RetryUtil.ExecuteAsync(async delegate
			{
				service = await Device.GetServiceAsync(Guid.Parse("0000181B-0000-1000-8000-00805F9B34FB"));
				await Task.Delay(1000);
				Log.Info($"【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurement】Body Composition :Service={service}");
				ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("00002A9C-0000-1000-8000-00805F9B34FB"));
				Log.Info($"【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurement】Body Composition :Characteristic={characteristic}");
				Log.Info("【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurement】Body Composition :ServiceId=" + service.Id.ToString() + ", CharacteristicId=" + characteristic.Id.ToString());
				characteristic.ValueUpdated -= BodyCompositionMeasurementOnValueUpdated;
				characteristic.ValueUpdated += BodyCompositionMeasurementOnValueUpdated;
				await characteristic.StartUpdatesAsync();
				Log.Info("【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurement】CCCD");
			}, 10, 1000, typeof(NullReferenceException));
		}
		finally
		{
			if (service != null)
			{
				service.Dispose();
			}
		}
	}

	private async Task<List<object>> FreeColor()
	{
		List<object> result = new List<object>();
		_freeColorResult = 0;
		GetFreeColorEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【FreeColor】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			byte[] request = new byte[4] { 3, 1, 65, 2 };
			bool writeResult = false;
			await MainThread.InvokeOnMainThreadAsync(async () => writeResult = await characteristic.WriteAsync(request));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【FreeColor】Custom :WriteAsync {0} request:{1}", new object[2]
			{
				writeResult,
				BitConverter.ToString(request)
			}));
			if (!writeResult)
			{
				Log.Error("【BLELib】【BLEDeviceNBCM】【FreeColor】要求書き込みに失敗しました。");
				return result;
			}
			GetFreeColorEvent.Wait();
			result.Add((_freeColorResult & 1) == 0);
			result.Add((_freeColorResult & 2) == 0);
			result.Add((_freeColorResult & 4) == 0);
			result.Add((_freeColorResult & 8) == 0);
			result.Add((_freeColorResult & 0x10) == 0);
		}
		return result;
	}

	private async Task<bool> SetColor(byte color)
	{
		_setColorResult = false;
		SetColorEvent.Reset();
		using (IService service = await Device.GetServiceAsync(Guid.Parse("11127000-B364-11E4-AB27-0800200C9A66")))
		{
			ICharacteristic characteristic = await service.GetCharacteristicAsync(Guid.Parse("11127001-B364-11E4-AB27-0800200C9A66"));
			Log.Info("【BLELib】【BLEDeviceNBCM】【SetColor】Custom :Service=" + service.Id.ToString() + ", Characteristic=" + characteristic.Id.ToString());
			byte[] obj = new byte[5] { 4, 1, 65, 0, 0 };
			obj[4] = color;
			byte[] request = obj;
			bool writeResult = false;
			await MainThread.InvokeOnMainThreadAsync(async () => writeResult = await characteristic.WriteAsync(request));
			Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【SetColor】Custom :WriteAsync {0} request:{1}", new object[2]
			{
				writeResult,
				BitConverter.ToString(request)
			}));
			if (!writeResult)
			{
				Log.Error("【BLELib】【BLEDeviceNBCM】【SetColor】要求書き込みに失敗しました。");
				return false;
			}
			SetColorEvent.Wait();
		}
		return _setColorResult;
	}

	private void OnDeviceConnected(object sender, DeviceEventArgs e)
	{
		Log.Info(string.Format("【BLELib】【BLEDeviceNBCM】【_IAdapter_DeviceConnected】DeviceConnected:Device.Name={0}, Device.Id={1}", new object[2]
		{
			e.Device?.Name,
			e.Device?.Id
		}));
		if (Device == null || !(Device.Name != e.Device?.Name))
		{
			DeviceConnectedEvent.Set();
		}
	}

	private void UserControlOnValueUpdated(object sender, CharacteristicUpdatedEventArgs e)
	{
		byte[] value = e.Characteristic.Value;
		string text = e.Characteristic.Uuid.ToUpper();
		Log.Info("【BLELib】【BLEDeviceNBCM】【UserControlOnValueUpdated】User Control ValueUpdated:sender=" + (sender as ICharacteristic).Uuid + ", buffer=" + BitConverter.ToString(value) + ", uuid=" + text);
		_registerNewUserResult = new RegisterNewUserResult
		{
			IsSuccess = (value.Length == 4)
		};
		if (_registerNewUserResult.IsSuccess)
		{
			_registerNewUserResult.UserNo = value[3];
		}
		else
		{
			_registerNewUserResult.Error = value[2];
		}
		RegisterNewUserEvent.Set();
	}

	private void UserDeleteOnValueUpdated(object sender, CharacteristicUpdatedEventArgs e)
	{
		byte[] value = e.Characteristic.Value;
		string text = e.Characteristic.Uuid.ToUpper();
		Log.Info("【BLELib】【BLEDeviceNBCM】【UserDeleteOnValueUpdated】User Control ValueUpdated:sender=" + (sender as ICharacteristic).Uuid + ", buffer=" + BitConverter.ToString(value) + ", uuid=" + text);
		_deleteUserResult = value[1] == 1;
		DeleteUserEvent.Set();
	}

	private void UserAuthenticationOnValueUpdated(object sender, CharacteristicUpdatedEventArgs e)
	{
		byte[] value = e.Characteristic.Value;
		string text = e.Characteristic.Uuid.ToUpper();
		Log.Info("【BLELib】【BLEDeviceNBCM】【UserAuthenticationOnValueUpdated】User Control ValueUpdated:sender=" + (sender as ICharacteristic).Uuid + ", buffer=" + BitConverter.ToString(value) + ", uuid=" + text);
		if (value.Length != 3)
		{
			_userAuthenticationResult.IsSuccess = false;
		}
		else
		{
			_userAuthenticationResult.IsSuccess = value[2] == 1;
			_userAuthenticationResult.Error = value[2];
		}
		UserAuthenticationEvent.Set();
	}

	private void WeightScaleMeasurementOnValueUpdated(object sender, CharacteristicUpdatedEventArgs e)
	{
		byte[] value = e.Characteristic.Value;
		string text = e.Characteristic.Uuid.ToUpper();
		Log.Info("【BLELib】【BLEDeviceNBCM】【WeightScaleMeasurementOnValueUpdated】Weight Scale ValueUpdated:sender=" + (sender as ICharacteristic)?.Uuid + ", buffer=" + BitConverter.ToString(value) + ", uuid=" + text);
		if (value.Length < 11)
		{
			Log.Info("【BLELib】【BLEDeviceNBCM】【WeightScaleMeasurementOnValueUpdated】buffer is invalid.");
			return;
		}
		DateTime? timeStamp;
		if ((value[0] & 2) == 2)
		{
			timeStamp = new DateTime(BitConverter.ToInt16(new byte[2]
			{
				value[3],
				value[4]
			}, 0), value[5], value[6], value[7], value[8], value[9]);
		}
		else
		{
			timeStamp = null;
		}
		lock (BodyComposition)
		{
			if (BodyComposition.Result.All(delegate(BodyCompositionResult.BodyCompositionRecord x)
			{
				DateTime? timeStamp2 = x.TimeStamp;
				DateTime? dateTime = timeStamp;
				if (timeStamp2.HasValue != dateTime.HasValue)
				{
					return true;
				}
				return timeStamp2.HasValue && timeStamp2.GetValueOrDefault() != dateTime.GetValueOrDefault();
			}))
			{
				Log.Trace($"【BLELib】【BLEDeviceNBCM】【WeightScaleMeasurementOnValueUpdated】create BodyCompositionRecord. timeStamp: {timeStamp}");
				BodyCompositionRecord = new BodyCompositionResult.BodyCompositionRecord
				{
					TimeStamp = timeStamp
				};
				BodyComposition.Result.Add(BodyCompositionRecord);
			}
			else
			{
				Log.Trace($"【BLELib】【BLEDeviceNBCM】【WeightScaleMeasurementOnValueUpdated】modify BodyCompositionRecord. timeStamp: {timeStamp}");
				BodyCompositionRecord = BodyComposition.Result.First(delegate(BodyCompositionResult.BodyCompositionRecord x)
				{
					DateTime? timeStamp2 = x.TimeStamp;
					DateTime? dateTime = timeStamp;
					if (timeStamp2.HasValue != dateTime.HasValue)
					{
						return false;
					}
					return !timeStamp2.HasValue || timeStamp2.GetValueOrDefault() == dateTime.GetValueOrDefault();
				});
			}
		}
		BodyCompositionRecord.MeasurementUnit = (value[0] & 1) == 0;
		bool flag = (value[0] & 8) == 8;
		double num = (int)BitConverter.ToUInt16(new byte[2]
		{
			value[1],
			value[2]
		}, 0);
		if (value[1] == 255 && value[2] == 255)
		{
			BodyCompositionRecord.WeightScale = null;
		}
		else
		{
			double num2 = (BodyCompositionRecord.MeasurementUnit ? 0.005 : 0.01);
			BodyCompositionRecord.WeightScale = num * num2;
		}
		if (!flag || (value[11] == 255 && value[12] == 255))
		{
			BodyCompositionRecord.BMI = null;
		}
		else
		{
			double num3 = 0.1;
			double num4 = (int)BitConverter.ToUInt16(new byte[2]
			{
				value[11],
				value[12]
			}, 0);
			BodyCompositionRecord.BMI = num4 * num3;
		}
		if (MeasurementCompletedFlag)
		{
			WeightScaleMeasurementEvent.Set();
		}
	}

	private void BodyCompositionMeasurementOnValueUpdated(object sender, CharacteristicUpdatedEventArgs e)
	{
		byte[] value = e.Characteristic.Value;
		string text = e.Characteristic.Uuid.ToUpper();
		Log.Info("【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurementOnValueUpdated】Body Composition ValueUpdated:sender=" + (sender as ICharacteristic)?.Uuid + ", buffer=" + BitConverter.ToString(value) + ", uuid=" + text);
		if (value.Length < 16)
		{
			Log.Info("【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurementOnValueUpdated】buffer is invalid.");
			return;
		}
		ushort num = BitConverter.ToUInt16(new byte[2]
		{
			value[0],
			value[1]
		}, 0);
		DateTime? timeStamp;
		if ((num & 2) == 2)
		{
			timeStamp = new DateTime(BitConverter.ToInt16(new byte[2]
			{
				value[4],
				value[5]
			}, 0), value[6], value[7], value[8], value[9], value[10]);
		}
		else
		{
			timeStamp = null;
		}
		lock (BodyComposition)
		{
			if (BodyComposition.Result.All(delegate(BodyCompositionResult.BodyCompositionRecord x)
			{
				DateTime? timeStamp2 = x.TimeStamp;
				DateTime? dateTime = timeStamp;
				if (timeStamp2.HasValue != dateTime.HasValue)
				{
					return true;
				}
				return timeStamp2.HasValue && timeStamp2.GetValueOrDefault() != dateTime.GetValueOrDefault();
			}))
			{
				Log.Trace($"【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurementOnValueUpdated】create BodyCompositionRecord. timeStamp: {timeStamp}");
				BodyCompositionRecord = new BodyCompositionResult.BodyCompositionRecord
				{
					TimeStamp = timeStamp
				};
				BodyComposition.Result.Add(BodyCompositionRecord);
			}
			else
			{
				Log.Trace($"【BLELib】【BLEDeviceNBCM】【BodyCompositionMeasurementOnValueUpdated】modify BodyCompositionRecord. timeStamp: {timeStamp}");
				BodyCompositionRecord = BodyComposition.Result.First(delegate(BodyCompositionResult.BodyCompositionRecord x)
				{
					DateTime? timeStamp2 = x.TimeStamp;
					DateTime? dateTime = timeStamp;
					if (timeStamp2.HasValue != dateTime.HasValue)
					{
						return false;
					}
					return !timeStamp2.HasValue || timeStamp2.GetValueOrDefault() == dateTime.GetValueOrDefault();
				});
			}
		}
		bool flag = (num & 1) == 0;
		bool flag2 = (num & 8) == 8;
		bool flag3 = (num & 0x20) == 32;
		bool flag4 = (num & 0x100) == 256;
		if (value[2] == 255 && value[3] == 255)
		{
			BodyCompositionRecord.BodyFatPercentage = null;
		}
		else
		{
			double num2 = 0.1;
			ushort num3 = BitConverter.ToUInt16(new byte[2]
			{
				value[2],
				value[3]
			}, 0);
			BodyCompositionRecord.BodyFatPercentage = (double)(int)num3 * num2;
		}
		if (!flag2 || (value[12] == 255 && value[13] == 255))
		{
			BodyCompositionRecord.BasalMetabolism = null;
		}
		else
		{
			ushort num4 = BitConverter.ToUInt16(new byte[2]
			{
				value[12],
				value[13]
			}, 0);
			BodyCompositionRecord.BasalMetabolism = (int)num4;
		}
		if (!flag3 || (value[14] == 255 && value[15] == 255))
		{
			BodyCompositionRecord.MuscleMass = null;
		}
		else
		{
			double num5 = (flag ? 0.005 : 0.01);
			ushort num6 = BitConverter.ToUInt16(new byte[2]
			{
				value[14],
				value[15]
			}, 0);
			BodyCompositionRecord.MuscleMass = (double)(int)num6 * num5;
		}
		if (!flag4 || (value[16] == 255 && value[17] == 255))
		{
			BodyCompositionRecord.BodyWaterMass = null;
		}
		else
		{
			double num7 = (flag ? 0.005 : 0.01);
			ushort num8 = BitConverter.ToUInt16(new byte[2]
			{
				value[16],
				value[17]
			}, 0);
			BodyCompositionRecord.BodyWaterMass = (double)(int)num8 * num7;
		}
		if (MeasurementCompletedFlag)
		{
			BodyCompositionMeasurementEvent.Set();
		}
	}
}

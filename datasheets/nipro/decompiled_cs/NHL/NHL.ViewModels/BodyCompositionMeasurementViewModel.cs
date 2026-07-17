using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using BLELib;
using BLELib.BLEDevice;
using BLELib.Common;
using Caliburn.Micro;
using NHL.Common;
using NHL.Models;
using NHL.Models.Entity;
using NHL.Models.Types;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class BodyCompositionMeasurementViewModel : ViewModelBase, IHandle<LifecycleEvent>, IHandle
{
	private NHL.Services.DependencyService.ILoggingService Log = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();

	private readonly int SCAN_TIMEOUT = 28800000;

	private readonly int RECEIVE_TIMEOUT = 60000;

	private readonly string RECEIVE_ERROR_MESSAGE = "データ受信できませんでした。エラーが続く場合は、OSのBluetooth設定から測定器の登録を一度解除して、測定器登録をやり直してください。";

	private static readonly BLELibStatus INITIAL_DEVICE_STATUS = BLELibStatus.RCV_WAIT_START;

	private DateTime? latestDateOfReceivedData;

	private bool _IsSleep;

	private object _ReceiveListLock = new object();

	private static object _ScanLock = new object();

	private List<ReceiveItem> _ReceiveList;

	private int _NotifyDataCount;

	private bool _isScanning;

	private BLELibStatus _deviceStatus = INITIAL_DEVICE_STATUS;

	public IMeasurementService MeasurementService { get; set; }

	public IBLELib BLELib { get; set; }

	public MeterContext MeterContext { get; set; }

	public string RecvMessage => $"測定器からのデータを{_NotifyDataCount}件受信しました。";

	public bool IsScanning
	{
		get
		{
			lock (_ScanLock)
			{
				return _isScanning;
			}
		}
		set
		{
			lock (_ScanLock)
			{
				_isScanning = value;
				NotifyOfPropertyChange(() => IsScanning);
			}
		}
	}

	public BLELibStatus DeviceStatus
	{
		get
		{
			return _deviceStatus;
		}
		set
		{
			_deviceStatus = value;
			NotifyOfPropertyChange(() => DeviceStatus);
		}
	}

	public bool IsiPad => NHL.Common.Common.IsiPad();

	public MasterDetailRootViewModel MasterDetailRootViewModel { get; set; }

	public HomeViewModel HomeViewModel { get; set; }

	public MeasurementResultViewModel MeasurementResultViewModel { get; set; }

	public async void GoBackHome()
	{
		await base.NavigationService.GoBackAsync();
	}

	public BodyCompositionMeasurementViewModel()
	{
		MeterContext = IoC.Get<MeterContext>();
		MasterDetailRootViewModel = IoC.Get<MasterDetailRootViewModel>();
		HomeViewModel = IoC.Get<HomeViewModel>();
		MeasurementResultViewModel = IoC.Get<MeasurementResultViewModel>();
	}

	~BodyCompositionMeasurementViewModel()
	{
	}

	protected override void OnInitialize()
	{
		Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【OnInitialize】OnInitialize start");
		base.OnInitialize();
		Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【OnInitialize】OnInitialize finish");
	}

	protected override async void OnActivate()
	{
		Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【OnActivate】OnActivate start");
		try
		{
			base.EventAggregator.Subscribe(this);
			IsScanning = false;
			await StartScan();
			DeviceStatus = INITIAL_DEVICE_STATUS;
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【BodyCompositionMeasurementViewModel】【OnActivate】例外発生：{ex}");
		}
		base.OnActivate();
		Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【OnActivate】OnActivate finish");
	}

	protected override async void OnDeactivate(bool close)
	{
		Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【OnDeactivate】OnDeactivate start");
		base.EventAggregator.Unsubscribe(this);
		await ReceiveStop();
		base.OnDeactivate(close);
		Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【OnDeactivate】OnDeactivate finish");
	}

	private async Task StartScan()
	{
		Log.Debug(string.Format("【IG】【BodyCompositionMeasurementViewModel】【StartScan】StartScan start: IsActive={0}, MeterContext.IsReady={1}, IsScanning={2}", new object[3] { IsActive, MeterContext.IsReady, IsScanning }));
		if (IsActive && MeterContext.IsReady && !IsScanning)
		{
			if (BLELib != null)
			{
				BLELib.StateChanged -= BLELib_StateChanged;
				BLELib.Dispose();
				BLELib = null;
			}
			BLELib = DependencyService.Get<IBLELibService>().GetBLELibrary();
			BLELib.ILoggingService = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();
			BLELib.Initialize();
			_ReceiveList = new List<ReceiveItem>();
			BLELib.StateChanged += BLELib_StateChanged;
			IsScanning = true;
			List<string> names = new List<string> { MeterContext.BCMeter.Name };
			await BLELib.ReceiveWait(names, SCAN_TIMEOUT, ReceiveWaitHandler);
		}
	}

	private void BLELib_StateChanged(object sender, BLELibStatusEventArgs e)
	{
		Log.Info(string.Format("【IG】【BodyCompositionMeasurementViewModel】【BLELib_StateChanged】sender={0}, DeviceName={1}, BLELibStatus={2}", new object[3]
		{
			sender,
			e.DeviceName,
			e.Status.ToString()
		}));
		if (e.Status == BLELibStatus.SCAN_TIMEOUT)
		{
			RestartReceiveWait().ConfigureAwait(continueOnCapturedContext: false);
		}
		else if (e.Status == BLELibStatus.RCV_WAIT_TIMEOUT)
		{
			RestartReceiveWait().ConfigureAwait(continueOnCapturedContext: false);
		}
		else if (e.Status == BLELibStatus.RCV_TIMEOUT)
		{
			ReceiveItem receiveItem = _ReceiveList.Where((ReceiveItem x) => x.Name == e.DeviceName).FirstOrDefault();
			if (receiveItem != null)
			{
				receiveItem.Status = ReceiveItem.ReceiveStatus.Timeout;
			}
			Execute.OnUIThread(async delegate
			{
				await base.DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
			});
		}
		else if (e.Status == BLELibStatus.RCV_ERR)
		{
			if (!e.Message.Contains("Encryption is insufficient"))
			{
				return;
			}
			ReceiveItem receiveItem2 = _ReceiveList.Where((ReceiveItem x) => x.Name == e.DeviceName && x.Status == ReceiveItem.ReceiveStatus.Start).FirstOrDefault();
			if (receiveItem2 == null)
			{
				return;
			}
			receiveItem2.Status = ReceiveItem.ReceiveStatus.Error;
			if (receiveItem2.ResultString.Count == 0)
			{
				Execute.OnUIThread(async delegate
				{
					await base.DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
				});
			}
			else
			{
				Execute.OnUIThread(async delegate
				{
					await base.DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
				});
			}
		}
		else if (e.Status == BLELibStatus.ADAPTER_DEVICE_CONNECTION_LOST)
		{
			ReceiveItem receiveItem3 = _ReceiveList.Where((ReceiveItem x) => x.Name.StartsWith("NIPRO CF") && x.Name == e.DeviceName && x.Status == ReceiveItem.ReceiveStatus.Start).FirstOrDefault();
			if (receiveItem3 != null && receiveItem3.ResultString.Count == 0)
			{
				Execute.OnUIThread(async delegate
				{
					await base.DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
				});
			}
			ReceiveItem receiveItem4 = _ReceiveList.Where((ReceiveItem x) => x.Name == e.DeviceName).FirstOrDefault();
			if (receiveItem4 != null)
			{
				receiveItem4.Status = ReceiveItem.ReceiveStatus.ConnectionLost;
			}
		}
		else if (e.Status != BLELibStatus.ADAPTER_DEVICE_DISCONNECTED && e.Status != BLELibStatus.RCV_NODATA)
		{
			if (e.Status == BLELibStatus.RCV_WAIT_START)
			{
				DeviceStatus = e.Status;
			}
			else if (e.Status == BLELibStatus.NBCM_STEP_ON)
			{
				DeviceStatus = e.Status;
			}
			else if (e.Status == BLELibStatus.NBCM_STEP_OFF)
			{
				DeviceStatus = e.Status;
			}
			else if (e.Status == BLELibStatus.NBCM_RCV_END)
			{
				DeviceStatus = e.Status;
			}
		}
	}

	private async void ReceiveWaitHandler(IList<string> s)
	{
		if (!IsActive || s == null)
		{
			return;
		}
		string name = ((s.Count >= 1) ? s[0] : "");
		string id = ((s.Count >= 2) ? s[1] : "");
		string text = ((s.Count >= 3) ? s[2] : "");
		string text2 = ((s.Count >= 4) ? s[3] : "");
		Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】name=" + name + ", Id=" + id + ", Rssi=" + text + ", State=" + text2);
		if (!(await MeterContext.CheckPairing("USERNAME", id)))
		{
			Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】name=" + name + ", Id=" + id + " is not pairing, do nothing.");
			return;
		}
		if (!name.StartsWith("NBCM"))
		{
			Log.Debug("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】name=" + name + ", Id=" + id + " is not body composition meter, do nothing.");
			return;
		}
		ReceiveItem receiveItem = null;
		lock (_ReceiveListLock)
		{
			if (!_ReceiveList.Any((ReceiveItem x) => x.Name == name && x.Status == ReceiveItem.ReceiveStatus.Start))
			{
				receiveItem = _ReceiveList.Where((ReceiveItem x) => x.Name == name).FirstOrDefault();
				if (receiveItem == null)
				{
					receiveItem = new ReceiveItem
					{
						Name = name,
						Status = ReceiveItem.ReceiveStatus.Start
					};
				}
				receiveItem.ResultString = new List<string>();
				_ReceiveList.Add(receiveItem);
			}
		}
		if (receiveItem == null)
		{
			return;
		}
		if (string.IsNullOrEmpty(MeterContext?.BCMeter?.UserNo) || string.IsNullOrEmpty(MeterContext?.BCMeter?.ColorCode) || ColorType.GetInstance(MeterContext.BCMeter.ColorCode) == ColorType.UNKNOWN)
		{
			ReceiveItem receiveItem2 = _ReceiveList.FirstOrDefault((ReceiveItem x) => x.Name == name);
			if (receiveItem2 != null)
			{
				_ReceiveList.Remove(receiveItem2);
			}
			return;
		}
		List<string> list = new List<string>
		{
			base.UserManager.IgUser.Name,
			base.UserManager.IgUser.Sex,
			base.UserManager.IgUser.Height?.ToString(),
			base.UserManager.IgUser.Birthday?.ToString(),
			MeterContext.BCMeter.UserNo
		};
		Log.Info(string.Format("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】受信開始:name={0}, timeout={1}, param={2}", new object[3]
		{
			name,
			RECEIVE_TIMEOUT,
			list?.JoinString(",")
		}));
		object obj = await BLELib.ReceiveStart(name, null, RECEIVE_TIMEOUT, list);
		Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】受信完了:response=" + obj);
		if (obj != null)
		{
			if (await DoAfterAllDataReceived(receiveItem, obj))
			{
				receiveItem.Status = ReceiveItem.ReceiveStatus.Complete;
				if (!_ReceiveList.Any((ReceiveItem x) => x.Status == ReceiveItem.ReceiveStatus.Start))
				{
					await DependencyService.Get<IMediaPlayerService>().PlayAsync("decision8");
					Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】SnackBar - " + RecvMessage);
					ShowSnackBarResult();
					_NotifyDataCount = 0;
				}
			}
			else
			{
				Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】不正なデータを受信 SnackBar - " + RecvMessage);
				NHL.Common.Common.ShowSnackBar("不正なデータを受信しました。", 5000);
				if (_NotifyDataCount > 0)
				{
					ShowSnackBarResult();
				}
				_NotifyDataCount = 0;
			}
		}
		else
		{
			receiveItem.Status = ReceiveItem.ReceiveStatus.NoData;
		}
		if (!_ReceiveList.Any((ReceiveItem x) => x.Status == ReceiveItem.ReceiveStatus.Start))
		{
			if (_IsSleep)
			{
				await ReceiveStop();
			}
			else
			{
				await RestartReceiveWait().ConfigureAwait(continueOnCapturedContext: false);
			}
		}
		else
		{
			Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ReceiveWaitHandler】複数受信中");
		}
	}

	private async Task<bool> DoAfterAllDataReceived(ReceiveItem receiveItem, object response)
	{
		_ = 3;
		try
		{
			int _AppendDataCount = 0;
			IList<Measurement> measureResultModelList = new List<Measurement>();
			if (response != null)
			{
				if (MeterContext.BCMeter != null && receiveItem.Name.Trim() == MeterContext.BCMeter.Name && response is BLEDeviceNBCM.BodyCompositionResult bodyCompositionResult)
				{
					measureResultModelList = CreateMeasurementListByBodyCompositionResult(bodyCompositionResult);
				}
				receiveItem.Status = ReceiveItem.ReceiveStatus.Complete;
			}
			else
			{
				receiveItem.Status = ReceiveItem.ReceiveStatus.NoData;
			}
			if (measureResultModelList.Count == 0)
			{
				return true;
			}
			BindableCollection<Measurement> dbDataList = await MeasurementService.GetAllMeasurement();
			Log.Info($"【IG】【BodyCompositionMeasurementViewModel】【DoAfterAllDataReceived】内部DBデータ取得:DB件数={dbDataList.Count}");
			foreach (Measurement measureResult in measureResultModelList)
			{
				if (!((!(measureResult.MeasurementType == "01")) ? dbDataList.Any((Measurement x) => x.MeasurementType != "01" && x.MeasurementType == measureResult.MeasurementType && x.MeasurementAt == measureResult.MeasurementAt && x.MeasurementValue == measureResult.MeasurementValue) : dbDataList.Any((Measurement x) => x.MeasurementType == "01" && x.MeasuringEquipmentMeasurementId == measureResult.MeasuringEquipmentMeasurementId && x.MeasuringEquipmentId == MeterContext.GLMeter.SerialNumber)))
				{
					measureResult.IgUserId = base.UserManager.IgUser.Id;
					await SetTimezone(measureResult);
					await MeasurementService.RegisterMeasurement(measureResult);
					if (measureResult.MeasurementType == "01" || measureResult.MeasurementType == "02" || measureResult.MeasurementType == "03" || measureResult.MeasurementType == "06")
					{
						_AppendDataCount++;
						_NotifyDataCount++;
					}
				}
			}
			Log.Info($"【IG】【BodyCompositionMeasurementViewModel】【DoAfterAllDataReceived】内部DB登録完了:登録件数={_AppendDataCount}");
			await Task.Run(async delegate
			{
				Log.Info("【IG】【BodyCompositionMeasurementViewModel】【DoAfterAllDataReceived】Sync start");
				await MeasurementService.Sync();
			}).ConfigureAwait(continueOnCapturedContext: false);
			return true;
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【BodyCompositionMeasurementViewModel】【DoAfterAllDataReceived】受信完了後のデータ処理の例外:{ex}");
			return false;
		}
	}

	private IList<Measurement> CreateMeasurementListByBodyCompositionResult(BLEDeviceNBCM.BodyCompositionResult bodyCompositionResult)
	{
		List<Measurement> list = new List<Measurement>();
		foreach (BLEDeviceNBCM.BodyCompositionResult.BodyCompositionRecord item7 in bodyCompositionResult.Result)
		{
			if (item7 == null)
			{
				continue;
			}
			if (item7.WeightScale.HasValue && Convert.ToDecimal(item7.WeightScale) != 65535m)
			{
				Measurement item = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.WeightScale, "06");
				list.Add(item);
			}
			if (item7.BMI.HasValue)
			{
				Measurement item2 = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.BMI, "07");
				list.Add(item2);
			}
			if (item7.BodyFatPercentage.HasValue && Convert.ToDecimal(item7.BodyFatPercentage) != 65535m)
			{
				Measurement item3 = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.BodyFatPercentage, "08");
				list.Add(item3);
			}
			if (item7.BasalMetabolism.HasValue && Convert.ToDecimal(item7.BasalMetabolism) != 65535m)
			{
				Measurement item4 = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.BasalMetabolism, "10");
				list.Add(item4);
			}
			if (item7.MuscleMass.HasValue && Convert.ToDecimal(item7.MuscleMass) != 65535m)
			{
				Measurement item5 = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.MuscleMass, "09");
				list.Add(item5);
			}
			if (item7.BodyWaterMass.HasValue && Convert.ToDecimal(item7.BodyWaterMass) != 65535m)
			{
				Measurement item6 = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.BodyWaterMass, "11");
				list.Add(item6);
			}
			if (item7.WeightScale.HasValue && Convert.ToDecimal(item7.WeightScale) != 65535m && item7.WeightScale != 0.0)
			{
				if (item7.BodyFatPercentage.HasValue && Convert.ToDecimal(item7.BodyFatPercentage) != 65535m && item7.BodyFatPercentage != 0.0)
				{
					Measurement measurement = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.BodyFatPercentage, "12");
					double? num = measurement.MeasurementValue * item7.WeightScale / 100.0;
					measurement.MeasurementValue = (double)Math.Round(Convert.ToDecimal(num), 2, MidpointRounding.AwayFromZero);
					list.Add(measurement);
				}
				if (item7.MuscleMass.HasValue && Convert.ToDecimal(item7.MuscleMass) != 65535m && item7.MuscleMass != 0.0)
				{
					Measurement measurement2 = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.MuscleMass, "13");
					double? num2 = measurement2.MeasurementValue / item7.WeightScale * 100.0;
					measurement2.MeasurementValue = (double)Math.Round(Convert.ToDecimal(num2), 1, MidpointRounding.AwayFromZero);
					list.Add(measurement2);
				}
				if (item7.BodyWaterMass.HasValue && Convert.ToDecimal(item7.BodyWaterMass) != 65535m && item7.BodyWaterMass != 0.0)
				{
					Measurement measurement3 = CreateMeasurement(MeterContext.BCMeter, item7.TimeStamp ?? DateTime.Now, item7.BodyWaterMass, "14");
					double? num3 = measurement3.MeasurementValue / item7.WeightScale * 100.0;
					measurement3.MeasurementValue = (double)Math.Round(Convert.ToDecimal(num3), 1, MidpointRounding.AwayFromZero);
					list.Add(measurement3);
				}
			}
		}
		return list;
	}

	private Measurement CreateMeasurement(MeterModel meter, DateTime? timeStamp, double? measurementValue, string measurementType)
	{
		return new Measurement
		{
			MeasurementAt = timeStamp,
			MeasurementValue = measurementValue,
			MeasurementType = measurementType,
			MeasuringEquipmentId = meter.SerialNumber,
			MeasuringEquipmentName = meter.Name,
			ExceedLimitType = "0"
		};
	}

	private async Task SetTimezone(Measurement measurement)
	{
		DateTime value = measurement.MeasurementAt.Value;
		DateTime value2 = new DateTime(value.Year, value.Month, value.Day);
		TimeSpan value3 = new TimeSpan(value.Hour, value.Minute, value.Second);
		TimeSpan? timezoneOther = base.UserManager.IgUser.TimezoneOther;
		if (value3 < timezoneOther)
		{
			measurement.TimezoneDate = value2.AddDays(-1.0);
		}
		else
		{
			measurement.TimezoneDate = value2;
		}
		if (!latestDateOfReceivedData.HasValue || (measurement.TimezoneDate.HasValue && latestDateOfReceivedData < measurement.TimezoneDate))
		{
			latestDateOfReceivedData = measurement.TimezoneDate;
		}
		await Task.Delay(1);
	}

	private void ShowSnackBarResult()
	{
		HomeViewModel.RefreshMeasurementResult();
		NHL.Common.Common.ShowSnackBar(RecvMessage, 10000, "表示", delegate
		{
			Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ShowSnackBarResult】SnackBar - ボタンクリック");
			MeasurementResult();
		});
	}

	private void MeasurementResult()
	{
		Log.Info("【IG】【BodyCompositionMeasurementViewModel】【MeasurementResult】MeasurementResult - 測定結果表示");
		MeasurementResultViewModel.RefreshMeasurementResult();
		MasterDetailRootViewModel.MeasurementResult();
		MasterDetailRootViewModel.DisplayName = "測定結果";
	}

	private async Task ReceiveStop()
	{
		Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ReceiveStop】待ち受けストップ");
		if (!IsScanning)
		{
			return;
		}
		try
		{
			await BLELib.ReceiveStop();
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【BodyCompositionMeasurementViewModel】【ReceiveStop】例外発生: {ex}");
		}
		finally
		{
			if (BLELib != null)
			{
				BLELib.StateChanged -= BLELib_StateChanged;
				BLELib.Dispose();
				BLELib = null;
			}
			IsScanning = false;
		}
		Log.Info("【IG】【BodyCompositionMeasurementViewModel】【ReceiveStop】待ち受けストップ完了");
	}

	private void RestartBLELibReceiveWait()
	{
		Task.Run(async delegate
		{
			Log.Info("【IG】【BodyCompositionMeasurementViewModel】【RestartBLELibReceiveWait】待受け(BLELib)の再起動 - Scan停止");
			await ReceiveStop();
			await Task.Delay(10);
			Log.Info("【IG】【BodyCompositionMeasurementViewModel】【RestartBLELibReceiveWait】待受け(BLELib)の再起動 - Scan開始");
			await StartScan();
		}).ConfigureAwait(continueOnCapturedContext: false);
	}

	private async Task RestartReceiveWait()
	{
		_ = 2;
		try
		{
			Log.Info("【IG】【BodyCompositionMeasurementViewModel】【RestartReceiveWait】待受けの再起動");
			await BLELib.ReceiveStop();
			await Task.Delay(10);
			BLELib.StateChanged -= BLELib_StateChanged;
			BLELib.StateChanged += BLELib_StateChanged;
			IsScanning = true;
			List<string> list = new List<string> { MeterContext.BCMeter.Name };
			Log.Info("【IG】【BodyCompositionMeasurementViewModel】【RestartReceiveWait】待受けの再起動:filter=" + list.JoinString(","));
			await BLELib.ReceiveWait(list, SCAN_TIMEOUT, ReceiveWaitHandler);
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【BodyCompositionMeasurementViewModel】【RestartReceiveWait】例外発生：{ex}");
			RestartBLELibReceiveWait();
		}
	}

	async void IHandle<LifecycleEvent>.Handle(LifecycleEvent message)
	{
		Log.Info("【IG】【BodyCompositionMeasurementViewModel】【IHandle<LifecycleEvent>.Handle】event: " + message.State);
		if (!IsActive)
		{
			return;
		}
		_IsSleep = message.State == LifecycleEvent.Status.Sleep;
		if (IsScanning && message.State == LifecycleEvent.Status.Sleep)
		{
			if (!_ReceiveList.Any((ReceiveItem x) => x.Status == ReceiveItem.ReceiveStatus.Start))
			{
				await ReceiveStop();
			}
		}
		else if (!IsScanning && message.State == LifecycleEvent.Status.Resume)
		{
			await StartScan();
		}
	}
}

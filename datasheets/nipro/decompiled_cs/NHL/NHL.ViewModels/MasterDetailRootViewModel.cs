using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using BLELib;
using BLELib.BLEDevice;
using BLELib.BLEDevice.Record;
using BLELib.Common;
using Caliburn.Micro;
using NHL.Common;
using NHL.Models.Entity;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.Services.Support;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using NHL.Views;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class MasterDetailRootViewModel : ViewModelBase, IHandle<LifecycleEvent>, IHandle, IHandle<CentralManagerEvent>, IHandle<TabChangeEvent>, IHandle<VersionEvent>, IHandle<MenuNavigatedEvent>, IHandle<ScanControlEvent>, IHandle<UpdateTimezoneRangeEvent>, IHandle<ShowVersionUpMessageEvent>, IHandle<UpdateTimezoneOtherRangeEvent>, IHandle<LocationRequestEvent>
{
	public class TimezoneRangeItem
	{
		public TimeSpan? Min { get; set; }

		public TimeSpan? Max { get; set; }

		public string Code { get; set; }
	}

	private NHL.Services.DependencyService.ILoggingService Log = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();

	private readonly int SCAN_TIMEOUT = 28800000;

	private readonly int RECEIVE_TIMEOUT = 60000;

	private readonly string RECEIVE_ERROR_MESSAGE = "データ受信できませんでした。エラーが続く場合は、OSのBluetooth設定から測定器の登録を一度解除して、測定器登録をやり直してください。";

	private readonly string CONTROL_SOLUTION = "0A";

	private bool _IsInitStartScan;

	private bool isScanning;

	private string _MeterKey = string.Empty;

	private int _AppendDataCount;

	private bool isPresentedMenu;

	private DateTime? requestDate;

	private DateTime? latestDateOfReceivedData;

	private IList<TimezoneRangeItem> _TimezoneRangeGlu;

	private IList<TimezoneRangeItem> _TimezoneRangeOther;

	private string scanMessage;

	private bool _IsSleep;

	private List<ReceiveItem> _ReceiveList;

	private object _ReceiveListLock = new object();

	private static object _ScanLock = new object();

	private ManualResetEventSlim CentralManagerReadyEvent = new ManualResetEventSlim(initialState: false);

	private ManualResetEventSlim LocationRequestCompleteEvent = new ManualResetEventSlim(initialState: false);

	private CancellationTokenSource _HelthCheckCancelTokenSource = new CancellationTokenSource();

	private bool isTabVisible = true;

	private int _NotifyDataCount;

	private bool _IsBLEAlert;

	public IMeasurementService MeasurementService { get; set; }

	public IBLELib BLELib { get; set; }

	public MeterContext MeterContext { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public string OldDisplayName { get; set; }

	public SettingViewModel SettingViewModel { get; set; }

	public HomeViewModel HomeViewModel { get; set; }

	public MeasurementResultViewModel MeasurementResultViewModel { get; set; }

	public TakePhotoViewModel TakePhotoViewModel { get; set; }

	public PickPhotoViewModel PickPhotoViewModel { get; set; }

	public RecordViewModel RecordViewModel { get; set; }

	public string RecvMessage => $"測定器からのデータを{_NotifyDataCount}件受信しました。";

	public bool IsScanning
	{
		get
		{
			lock (_ScanLock)
			{
				return isScanning;
			}
		}
		set
		{
			lock (_ScanLock)
			{
				isScanning = value;
				NotifyOfPropertyChange(() => IsScanning);
			}
		}
	}

	public bool IsPresentedMenu
	{
		get
		{
			return isPresentedMenu;
		}
		set
		{
			isPresentedMenu = value;
			NotifyOfPropertyChange(() => IsPresentedMenu);
			base.EventAggregator.PublishOnUIThread(new PresentedMenuEvent
			{
				IsPresented = isPresentedMenu
			});
		}
	}

	public bool IsBLEAlert
	{
		get
		{
			return _IsBLEAlert;
		}
		set
		{
			_IsBLEAlert = value;
			NotifyOfPropertyChange(() => IsBLEAlert);
		}
	}

	public bool IsTabVisible
	{
		get
		{
			return isTabVisible;
		}
		set
		{
			isTabVisible = value;
			NotifyOfPropertyChange(() => IsTabVisible);
		}
	}

	public MasterDetailRootViewModel()
	{
		DisplayName = "げんきノート";
		SettingViewModel = IoC.Get<SettingViewModel>();
		HomeViewModel = IoC.Get<HomeViewModel>();
		MeasurementResultViewModel = IoC.Get<MeasurementResultViewModel>();
		TakePhotoViewModel = IoC.Get<TakePhotoViewModel>();
		PickPhotoViewModel = IoC.Get<PickPhotoViewModel>();
		RecordViewModel = IoC.Get<RecordViewModel>();
		MeterContext = IoC.Get<MeterContext>();
	}

	~MasterDetailRootViewModel()
	{
		if (MeasurementResultViewModel != null)
		{
			base.EventAggregator.Unsubscribe(MeasurementResultViewModel);
		}
		if (HomeViewModel != null)
		{
			base.EventAggregator.Unsubscribe(HomeViewModel);
		}
		if (RecordViewModel != null)
		{
			base.EventAggregator.Unsubscribe(RecordViewModel);
		}
	}

	protected override void OnInitialize()
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【OnInitialize】OnInitialize start");
		base.EventAggregator.Subscribe(this);
		HomeViewModel.Initialize();
		MeasurementResultViewModel.Initialize();
		RecordViewModel.Initialize();
		TakePhotoViewModel.Initialize();
		PickPhotoViewModel.Initialize();
		base.OnInitialize();
		Log.Debug("【IG】【MasterDetailRootViewModel】【OnInitialize】OnInitialize finish");
	}

	protected override async void OnActivate()
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【OnActivate】OnActivate start");
		try
		{
			IoC.Get<MeterContext>().Initialize("USERNAME");
			if (((GetView() as MasterDetailRootView)?.Detail as NavigationPage)?.CurrentPage is TabbedPage tabbedPage)
			{
				tabbedPage.CurrentPageChanged += OnTabCurrentPageChanged;
			}
			base.EventAggregator.PublishOnUIThread(new MasterDetailRootOnActivateEvent());
			base.EventAggregator.PublishOnUIThread(new HomeMasterDetailRootOnActivateEvent());
			if (!_IsInitStartScan)
			{
				_IsInitStartScan = true;
				MakeTimezoneRange();
				MakeTimezoneOtherRange();
			}
			await StartScan();
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【MasterDetailRootViewModel】【OnActivate】例外発生：{ex}");
		}
		base.OnActivate();
		Log.Debug("【IG】【MasterDetailRootViewModel】【OnActivate】OnActivate finish");
	}

	protected override async void OnDeactivate(bool close)
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【OnDeactivate】OnDeactivate start");
		if (((GetView() as MasterDetailRootView)?.Detail as NavigationPage)?.CurrentPage is TabbedPage tabbedPage)
		{
			tabbedPage.CurrentPageChanged -= OnTabCurrentPageChanged;
		}
		await ReceiveStop();
		base.OnDeactivate(close);
		Log.Debug("【IG】【MasterDetailRootViewModel】【OnDeactivate】OnDeactivate finish");
	}

	public void MakeTimezoneRange()
	{
		_TimezoneRangeGlu = new List<TimezoneRangeItem>
		{
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone1,
				Max = base.UserManager.IgUser.Timezone2,
				Code = "01"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone2,
				Max = base.UserManager.IgUser.Timezone3,
				Code = "02"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone3,
				Max = base.UserManager.IgUser.Timezone4,
				Code = "03"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone4,
				Max = base.UserManager.IgUser.Timezone5,
				Code = "04"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone5,
				Max = base.UserManager.IgUser.Timezone6,
				Code = "05"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone6,
				Max = base.UserManager.IgUser.Timezone7,
				Code = "06"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone7,
				Max = base.UserManager.IgUser.Timezone8,
				Code = "07"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.Timezone8,
				Max = base.UserManager.IgUser.Timezone1 + new TimeSpan(24, 0, 0, 0),
				Code = "08"
			}
		};
	}

	public void MakeTimezoneOtherRange()
	{
		_TimezoneRangeOther = new List<TimezoneRangeItem>
		{
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.TimezoneOther,
				Max = base.UserManager.IgUser.TimezoneNight,
				Code = "01"
			},
			new TimezoneRangeItem
			{
				Min = base.UserManager.IgUser.TimezoneNight,
				Max = base.UserManager.IgUser.TimezoneOther + new TimeSpan(24, 0, 0, 0),
				Code = "02"
			}
		};
	}

	private void StartHelthCheck()
	{
		_HelthCheckCancelTokenSource = new CancellationTokenSource();
		Task.Run(async delegate
		{
			Log.Debug("【IG】【MasterDetailRootViewModel】【StartHelthCheck】ヘルスチェックタスク - 開始");
			try
			{
				while (_HelthCheckCancelTokenSource != null && !_HelthCheckCancelTokenSource.IsCancellationRequested)
				{
					await Task.Delay(5000, _HelthCheckCancelTokenSource.Token);
					Log.Debug($"【IG】【MasterDetailRootViewModel】【StartHelthCheck】ヘルスチェックタスク - BLELib.IsScanning={BLELib.IsScanning}");
					if (!_ReceiveList.Any((ReceiveItem x) => x.Status == ReceiveItem.ReceiveStatus.Start))
					{
						if (!BLELib.IsScanning)
						{
							Log.Debug("【IG】【MasterDetailRootViewModel】【StartHelthCheck】ヘルスチェックタスク - 待受けの再起動");
							RestartReceiveWait();
						}
						else if (_ReceiveList.Count > 0 && _ReceiveList.All((ReceiveItem x) => x.Status == ReceiveItem.ReceiveStatus.Complete))
						{
							Log.Debug("【IG】【MasterDetailRootViewModel】【StartHelthCheck】ヘルスチェックタスク - 待受けの再起動 [All Complete]");
							_ReceiveList = new List<ReceiveItem>();
							RestartReceiveWait();
						}
					}
				}
			}
			catch (TaskCanceledException ex)
			{
				Log.Debug("【IG】【MasterDetailRootViewModel】【StartHelthCheck】TaskCanceledException発生：" + ex.Message);
			}
			catch (Exception ex2)
			{
				Log.Error($"【IG】【MasterDetailRootViewModel】【StartHelthCheck】例外発生：{ex2}");
			}
			Log.Debug("【IG】【MasterDetailRootViewModel】【StartHelthCheck】ヘルスチェックタスク - 終了");
		}, _HelthCheckCancelTokenSource.Token).ContinueWith((Func<Task, Task>)async delegate
		{
			if (_HelthCheckCancelTokenSource != null)
			{
				_HelthCheckCancelTokenSource.Dispose();
			}
			_HelthCheckCancelTokenSource = null;
		}).ConfigureAwait(continueOnCapturedContext: false);
	}

	private void StopHelthCheck()
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【StopHelthCheck】ヘルスチェックタスク - 停止");
		if (_HelthCheckCancelTokenSource != null)
		{
			_HelthCheckCancelTokenSource.Cancel();
			_HelthCheckCancelTokenSource = null;
		}
	}

	public void MeasurementResult()
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【MeasurementResult】MeasurementResult - 測定結果表示");
		IsPresentedMenu = false;
		if (Application.Current.MainPage.Navigation.NavigationStack.Count > 1)
		{
			base.NavigationService.GoBackAsync();
			if (!requestDate.HasValue)
			{
				MeasurementResultViewModel.RefreshMeasurementResult();
			}
			else
			{
				MeasurementResultViewModel.RefreshMeasurementResult(requestDate.Value);
			}
		}
		if (!(((GetView() as MasterDetailRootView)?.Detail as NavigationPage)?.CurrentPage is TabbedPage tabbedPage))
		{
			return;
		}
		if (tabbedPage.CurrentPage is MeasurementResultView)
		{
			if (!requestDate.HasValue)
			{
				MeasurementResultViewModel.SelectDate(DateTime.Now);
			}
			else
			{
				MeasurementResultViewModel.SelectDate(requestDate.Value);
			}
		}
		else
		{
			tabbedPage.CurrentPage = tabbedPage.Children[1];
		}
		latestDateOfReceivedData = null;
		requestDate = null;
	}

	public void MeasurementResult(DateTime? requestDate)
	{
		this.requestDate = requestDate;
		MeasurementResult();
	}

	private IList<string> LoadTargetMeterNameList()
	{
		List<string> list = new List<string>();
		if (MeterContext.GLMeter != null && MeterContext.GLMeter.Name != MeterContext.NON_REGIST_NAME)
		{
			list.Add(MeterContext.GLMeter.Name);
		}
		if (MeterContext.HTMeter != null && MeterContext.HTMeter.Name != MeterContext.NON_REGIST_NAME)
		{
			list.Add(MeterContext.HTMeter.Name);
		}
		if (MeterContext.BPMeter != null && MeterContext.BPMeter.Name != MeterContext.NON_REGIST_NAME)
		{
			list.Add(MeterContext.BPMeter.Name);
		}
		return list;
	}

	private async Task<bool> DoAfterAllDataReceived(ReceiveItem receiveItem, object response)
	{
		_ = 3;
		try
		{
			_AppendDataCount = 0;
			List<Measurement> measureResultModelList = new List<Measurement>();
			if (response != null)
			{
				if (MeterContext.GLMeter != null && receiveItem.Name.Trim() == MeterContext.GLMeter.Name)
				{
					if (response is BLEDeviceCFL.GlucoseResult glucoseResult)
					{
						glucoseResult.Deviceinfo?.JoinString(",");
						Log.Info(string.Format("【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】血糖値受信:Deviceinfo={0}, {1}件", new object[2]
						{
							glucoseResult.Deviceinfo?.JoinString(","),
							glucoseResult?.Result.Count
						}));
						foreach (BLEDeviceCFL.GlucoseResult.GlucoseMeasureRecord item in glucoseResult.Result)
						{
							Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】血糖値:TimeStamp={item.TimeStamp.ToString()}, SeqNo={item.SeqNo}, Glu={item.Glu}, MealFlg={item.Meal.ToString()}, Type={item.Type}");
							if (!item.Type.Equals(CONTROL_SOLUTION) && !measureResultModelList.Any((Measurement x) => x.MeasuringEquipmentMeasurementId == item.SeqNo && x.MeasurementType == "01" && x.MeasuringEquipmentId == MeterContext.GLMeter.SerialNumber))
							{
								string exceedLimitType;
								double value;
								if (item.Glu > 600)
								{
									exceedLimitType = "1";
									value = 600.0;
								}
								else if (item.Glu < 20)
								{
									exceedLimitType = "2";
									value = 20.0;
								}
								else
								{
									exceedLimitType = "0";
									value = item.Glu;
								}
								measureResultModelList.Add(new Measurement
								{
									MeasurementAt = item.TimeStamp,
									MeasurementValue = value,
									MeasurementType = "01",
									MeasuringEquipmentId = MeterContext.GLMeter.SerialNumber,
									MeasuringEquipmentName = MeterContext.GLMeter.Name,
									MeasuringEquipmentMeasurementId = item.SeqNo,
									ExceedLimitType = exceedLimitType
								});
							}
						}
					}
				}
				else if (MeterContext.HTMeter != null && receiveItem.Name.Trim() == MeterContext.HTMeter.Name)
				{
					List<BLEDeviceNT100B.BodyTemperatureResult.BodyTemperatureMeasureRecord> list = new List<BLEDeviceNT100B.BodyTemperatureResult.BodyTemperatureMeasureRecord>();
					if (response is BLEDeviceNSM1.BodyTemperatureResult bodyTemperatureResult)
					{
						Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】【BLEDeviceNSM1】体温受信:{bodyTemperatureResult.Result.Count}件");
						list = bodyTemperatureResult.Result.Select((BLEDeviceNSM1.BodyTemperatureResult.BodyTemperatureMeasureRecord x) => new BLEDeviceNT100B.BodyTemperatureResult.BodyTemperatureMeasureRecord
						{
							Temperature = x.Temperture,
							TimeStamp = x.TimeStamp
						}).ToList();
					}
					else if (response is BLEDeviceNT100B.BodyTemperatureResult bodyTemperatureResult2)
					{
						Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】【BLEDeviceNT100B】体温受信:{bodyTemperatureResult2.Result.Count}件");
						list = bodyTemperatureResult2.Result.ToList();
					}
					if (list.Count > 0)
					{
						foreach (BLEDeviceNT100B.BodyTemperatureResult.BodyTemperatureMeasureRecord item2 in list)
						{
							Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】体温:{item2}");
						}
						BLEDeviceNT100B.BodyTemperatureResult.BodyTemperatureMeasureRecord bodyTemperatureMeasureRecord = list.OrderByDescending((BLEDeviceNT100B.BodyTemperatureResult.BodyTemperatureMeasureRecord x) => x.TimeStamp).FirstOrDefault();
						if (bodyTemperatureMeasureRecord.Temperature == 65535.0 || bodyTemperatureMeasureRecord.Temperature < 0.0 || bodyTemperatureMeasureRecord.TimeStamp == DateTime.MinValue)
						{
							return false;
						}
						measureResultModelList.Add(new Measurement
						{
							MeasurementAt = bodyTemperatureMeasureRecord.TimeStamp,
							MeasurementValue = bodyTemperatureMeasureRecord.Temperature,
							MeasurementType = "02",
							MeasuringEquipmentId = MeterContext.HTMeter.SerialNumber,
							MeasuringEquipmentName = MeterContext.HTMeter.Name,
							ExceedLimitType = "0"
						});
					}
				}
				else if (MeterContext.BPMeter != null && receiveItem.Name.Trim() == MeterContext.BPMeter.Name && response is BloodPressureResult bloodPressureResult)
				{
					Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】血圧受信:{bloodPressureResult.Result.Count}件");
					if (bloodPressureResult.Result.Count > 0)
					{
						foreach (BloodPressureResult.BloodPressureMeasureRecord item3 in bloodPressureResult.Result)
						{
							Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】血圧:{item3}");
						}
						BloodPressureResult.BloodPressureMeasureRecord bloodPressureMeasureRecord = bloodPressureResult.Result.OrderByDescending((BloodPressureResult.BloodPressureMeasureRecord x) => x.TimeStamp).FirstOrDefault();
						if (bloodPressureMeasureRecord.SBP == 2047.0 || bloodPressureMeasureRecord.DBP == 2047.0 || bloodPressureMeasureRecord.PulseRate == 2047.0 || bloodPressureMeasureRecord.PulseRate == 2048.0 || bloodPressureMeasureRecord.TimeStamp == DateTime.MinValue)
						{
							return false;
						}
						measureResultModelList.Add(new Measurement
						{
							MeasurementAt = bloodPressureMeasureRecord.TimeStamp,
							MeasurementValue = bloodPressureMeasureRecord.SBP,
							MeasurementType = "03",
							ExceedLimitType = "0",
							MeasuringEquipmentId = MeterContext.BPMeter.SerialNumber,
							MeasuringEquipmentName = MeterContext.BPMeter.Name
						});
						measureResultModelList.Add(new Measurement
						{
							MeasurementAt = bloodPressureMeasureRecord.TimeStamp,
							MeasurementValue = bloodPressureMeasureRecord.DBP,
							MeasurementType = "04",
							ExceedLimitType = "0",
							MeasuringEquipmentId = MeterContext.BPMeter.SerialNumber,
							MeasuringEquipmentName = MeterContext.BPMeter.Name
						});
						measureResultModelList.Add(new Measurement
						{
							MeasurementAt = bloodPressureMeasureRecord.TimeStamp,
							MeasurementValue = bloodPressureMeasureRecord.PulseRate,
							MeasurementType = "05",
							ExceedLimitType = "0",
							MeasuringEquipmentId = MeterContext.BPMeter.SerialNumber,
							MeasuringEquipmentName = MeterContext.BPMeter.Name
						});
					}
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
			Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】内部DBデータ取得:DB件数={dbDataList.Count}");
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
			Log.Info($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】内部DB登録完了:登録件数={_AppendDataCount}");
			await Task.Run(async delegate
			{
				Log.Info("【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】Sync start");
				await MeasurementService.Sync();
			}).ConfigureAwait(continueOnCapturedContext: false);
			return true;
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【MasterDetailRootViewModel】【DoAfterAllDataReceived】受信完了後のデータ処理の例外:{ex}");
			return false;
		}
	}

	private async Task SetTimezone(Measurement measurement)
	{
		DateTime value = measurement.MeasurementAt.Value;
		TimeSpan timeZone = new TimeSpan(value.Hour, value.Minute, value.Second);
		DateTime value2 = new DateTime(value.Year, value.Month, value.Day);
		if (measurement.MeasurementType == "01")
		{
			string text = _TimezoneRangeGlu.FirstOrDefault(delegate(TimezoneRangeItem x)
			{
				if (x.Min <= timeZone)
				{
					TimeSpan value4 = timeZone;
					TimeSpan? max = x.Max;
					return value4 < max;
				}
				return false;
			})?.Code;
			if (text != null)
			{
				measurement.Timezone = text;
				measurement.TimezoneDate = value2;
			}
			else
			{
				TimeSpan timeZone24 = timeZone.Add(new TimeSpan(24, 0, 0, 0));
				string text2 = _TimezoneRangeGlu.FirstOrDefault(delegate(TimezoneRangeItem x)
				{
					if (x.Min <= timeZone24)
					{
						TimeSpan value4 = timeZone24;
						TimeSpan? max = x.Max;
						return value4 < max;
					}
					return false;
				})?.Code;
				if (text2 != null)
				{
					measurement.Timezone = text2;
					measurement.TimezoneDate = value2.AddDays(-1.0);
				}
			}
		}
		else if (measurement.MeasurementType == "02")
		{
			TimeSpan value3 = timeZone;
			TimeSpan? timezoneOther = base.UserManager.IgUser.TimezoneOther;
			if (value3 < timezoneOther)
			{
				measurement.TimezoneDate = value2.AddDays(-1.0);
			}
			else
			{
				measurement.TimezoneDate = value2;
			}
		}
		else if (measurement.MeasurementType == "03" || measurement.MeasurementType == "04" || measurement.MeasurementType == "05")
		{
			string text3 = _TimezoneRangeOther.FirstOrDefault(delegate(TimezoneRangeItem x)
			{
				if (x.Min <= timeZone)
				{
					TimeSpan value4 = timeZone;
					TimeSpan? max = x.Max;
					return value4 < max;
				}
				return false;
			})?.Code;
			if (text3 != null)
			{
				measurement.Timezone = text3;
				measurement.TimezoneDate = value2;
			}
			else
			{
				TimeSpan timeZone25 = timeZone.Add(new TimeSpan(24, 0, 0, 0));
				string text4 = _TimezoneRangeOther.FirstOrDefault(delegate(TimezoneRangeItem x)
				{
					if (x.Min <= timeZone25)
					{
						TimeSpan value4 = timeZone25;
						TimeSpan? max = x.Max;
						return value4 < max;
					}
					return false;
				})?.Code;
				if (text4 != null)
				{
					measurement.Timezone = text4;
					measurement.TimezoneDate = value2.AddDays(-1.0);
				}
			}
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

	private async Task StartScan()
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【StartScan】StartScan start");
		if (!IsActive || !MeterContext.IsReady || IsScanning)
		{
			return;
		}
		LocationRequestCompleteEvent.Reset();
		CentralManagerReadyEvent.Reset();
		await Task.Run(async delegate
		{
			IRequestLocation requestLocation = DependencyService.Get<IRequestLocation>();
			if (!requestLocation.IsLocationServiceEnabled())
			{
				requestLocation.Request();
				LocationRequestCompleteEvent.Wait();
			}
			DependencyService.Get<IRequestBluetooth>().Request();
			await Task.Delay(1000);
			if (!CentralManagerReadyEvent.Wait(10000))
			{
				Log.Debug("【IG】【MasterDetailRootViewModel】【StartScan】CentralManagerReadyEvent timeout");
				Execute.OnUIThread(async delegate
				{
					await DialogProvider.ShowAlert("", "Bluetoothが無効です");
				});
			}
		});
	}

	private async Task StartScanEvent()
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【StartScanEvent】StartScanEvent start");
		BLELib = DependencyService.Get<IBLELibService>().GetBLELibrary();
		BLELib.ILoggingService = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();
		BLELib.Initialize();
		_ReceiveList = new List<ReceiveItem>();
		BLELib.StateChanged += BLELib_StateChanged;
		IsScanning = true;
		List<string> list = (from x in LoadTargetMeterNameList()
			select x.Trim()).ToList();
		Log.Info("【IG】【MasterDetailRootViewModel】【StartScanEvent】StartScan:filter=" + list.JoinString(","));
		await BLELib.ReceiveWait(list, SCAN_TIMEOUT, ReceiveWaitHandler);
		StartHelthCheck();
	}

	private void BLELib_StateChanged(object sender, BLELibStatusEventArgs e)
	{
		Log.Info(string.Format("【IG】【MasterDetailRootViewModel】【BLELib_StateChanged】sender={0}, DeviceName={1}, BLELibStatus={2}", new object[3]
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
				await DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
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
					await DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
				});
			}
			else
			{
				Execute.OnUIThread(async delegate
				{
					await DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
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
					await DialogProvider.ShowAlert("", RECEIVE_ERROR_MESSAGE);
				});
			}
			ReceiveItem receiveItem4 = _ReceiveList.Where((ReceiveItem x) => x.Name == e.DeviceName).FirstOrDefault();
			if (receiveItem4 != null)
			{
				receiveItem4.Status = ReceiveItem.ReceiveStatus.ConnectionLost;
			}
		}
		else if (e.Status != BLELibStatus.ADAPTER_DEVICE_DISCONNECTED)
		{
			_ = e.Status;
			_ = 12;
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
		Log.Info("【IG】【MasterDetailRootViewModel】【ReceiveWaitHandler】name=" + name + ", Id=" + id + ", Rssi=" + text + ", State=" + text2);
		if (!(await MeterContext.CheckPairing("USERNAME", id)))
		{
			Log.Debug("【IG】【MasterDetailRootViewModel】【ReceiveWaitHandler】name=" + name + ", Id=" + id + " is not pairing, do nothing.");
		}
		else
		{
			if (name.StartsWith("NBCM"))
			{
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
			Task.Run(delegate
			{
				StopHelthCheck();
			}).ConfigureAwait(continueOnCapturedContext: false);
			IList<string> param = null;
			if (name.StartsWith("NIPRO CF"))
			{
				Task<List<Measurement>> task = Task.Run(async () => await SyncManager.GetInstance().MeasurementTable.Where((Measurement x) => x.MeasurementType == "01" && x.IgUserId == UserManager.IgUser.Id && x.MeasuringEquipmentId == MeterContext.GLMeter.SerialNumber && x.MeasuringEquipmentMeasurementId != null).ToListAsync());
				param = new List<string>();
				if (task != null && task.Result != null && task.Result.Count > 0)
				{
					Measurement measurement = task.Result.OrderByDescending(delegate(Measurement x)
					{
						int result2 = 0;
						int.TryParse(x.MeasuringEquipmentMeasurementId, out result2);
						return result2;
					}).FirstOrDefault();
					int result = 0;
					if (int.TryParse(measurement.MeasuringEquipmentMeasurementId, out result))
					{
						param.Add(BLEDeviceCFL.ReceiveMode.Diff.ToString());
						param.Add((result + 1).ToString());
					}
					else
					{
						param.Add(BLEDeviceCFL.ReceiveMode.All.ToString());
					}
				}
				else
				{
					param.Add(BLEDeviceCFL.ReceiveMode.All.ToString());
				}
				if (base.UserManager.IgUser.GlucoseHighValue.HasValue)
				{
					if (param[0] != BLEDeviceCFL.ReceiveMode.Diff.ToString())
					{
						param.Add("");
					}
					param.Add("700");
					param.Add(base.UserManager.IgUser.GlucoseHighValue.ToString());
					param.Add(base.UserManager.IgUser.GlucoseLittleHighValue.ToString());
					param.Add(base.UserManager.IgUser.GlucoseLittleLowValue.ToString());
					param.Add(base.UserManager.IgUser.GlucoseLowValue.ToString());
					param.Add("0");
					param.Add("0");
				}
			}
			Task.Run(async delegate
			{
				Log.Info(string.Format("【IG】【MasterDetailRootViewModel】【ReceiveWaitHandler】受信開始:name={0}, timeout={1}, param={2}", new object[3]
				{
					name,
					RECEIVE_TIMEOUT,
					param?.JoinString(",")
				}));
				object obj = await BLELib.ReceiveStart(name, null, RECEIVE_TIMEOUT, param);
				Log.Info("【IG】【MasterDetailRootViewModel】【ReceiveWaitHandler】受信完了:response=" + obj);
				if (obj != null)
				{
					if (await DoAfterAllDataReceived(receiveItem, obj))
					{
						receiveItem.Status = ReceiveItem.ReceiveStatus.Complete;
						if (!_ReceiveList.Any((ReceiveItem x) => x.Status == ReceiveItem.ReceiveStatus.Start))
						{
							await DependencyService.Get<IMediaPlayerService>().PlayAsync("decision8");
							await ReceiveStop();
							Log.Info("【IG】【MasterDetailRootViewModel】【ReceiveWaitHandler】SnackBar - " + RecvMessage);
							if (string.IsNullOrEmpty(DisplayName))
							{
								NHL.Common.Common.ShowSnackBar(RecvMessage, 5000);
								_NotifyDataCount = 0;
							}
							else
							{
								ShowSnackBarResult();
								_NotifyDataCount = 0;
							}
						}
					}
					else
					{
						Log.Info("【IG】【MasterDetailRootViewModel】【ReceiveWaitHandler】不正なデータを受信 SnackBar - " + RecvMessage);
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
					StartHelthCheck();
				}
			}).ConfigureAwait(continueOnCapturedContext: false);
		}
	}

	private void ShowSnackBarResult()
	{
		TabbedPage tabbedPage = ((GetView() as MasterDetailRootView)?.Detail as NavigationPage)?.CurrentPage as TabbedPage;
		if (tabbedPage.CurrentPage is MeasurementResultView)
		{
			MeasurementResultViewModel.RefreshMeasurementResult(MeasurementResultViewModel.SelectedDate, isRefreshFoodPhotoList: false);
		}
		else if (tabbedPage.CurrentPage is HomeView)
		{
			HomeViewModel.RefreshMeasurementResult();
		}
		NHL.Common.Common.ShowSnackBar(RecvMessage, 10000, "表示", delegate
		{
			Log.Info("【IG】【MasterDetailRootViewModel】【ShowSnackBarResult】SnackBar - ボタンクリック");
			if (latestDateOfReceivedData.HasValue)
			{
				requestDate = new DateTime(latestDateOfReceivedData.Value.Ticks);
			}
			MeasurementResult();
		});
	}

	public async Task ReceiveStop()
	{
		Log.Info($"【IG】【MasterDetailRootViewModel】【ReceiveStop】待ち受けストップ IsScanning: {IsScanning}");
		if (IsScanning)
		{
			DependencyService.Get<IRequestBluetooth>().ResetCentralManager();
			StopHelthCheck();
			if (BLELib != null)
			{
				Log.Info("【IG】【MasterDetailRootViewModel】【ReceiveStop】BLELib.ReceiveStop");
				await BLELib.ReceiveStop();
				BLELib.StateChanged -= BLELib_StateChanged;
				BLELib.Dispose();
			}
			IsScanning = false;
		}
	}

	private void RestartBLELibReceiveWait()
	{
		Task.Run(async delegate
		{
			await ReceiveStop();
			await Task.Delay(10);
			await StartScan();
		}).ConfigureAwait(continueOnCapturedContext: false);
	}

	private async Task RestartReceiveWait()
	{
		_ = 2;
		try
		{
			await BLELib.ReceiveStop();
			await Task.Delay(10);
			List<string> list = (from x in LoadTargetMeterNameList()
				select x.Trim()).ToList();
			Log.Info("【IG】【MasterDetailRootViewModel】【RestartReceiveWait】待受けの再起動:filter=" + list.JoinString(","));
			IsScanning = true;
			await BLELib.ReceiveWait(list, SCAN_TIMEOUT, ReceiveWaitHandler);
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【MasterDetailRootViewModel】【RestartReceiveWait】例外発生：{ex}");
			RestartBLELibReceiveWait();
		}
	}

	private void OnTabCurrentPageChanged(object sender, EventArgs e)
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【OnTabCurrentPageChanged】sender=" + sender.GetType().ToString());
		if (!(sender is TabbedPage tabbedPage))
		{
			return;
		}
		OldDisplayName = DisplayName;
		IsTabVisible = true;
		if (tabbedPage.CurrentPage is HomeView)
		{
			DisplayName = "げんきノート";
			HomeViewModel.RefreshMeasurementResult();
		}
		else if (tabbedPage.CurrentPage is MeasurementResultView)
		{
			DisplayName = "測定結果";
			if (!requestDate.HasValue)
			{
				MeasurementResultViewModel.RefreshMeasurementResult();
				return;
			}
			MeasurementResultViewModel.RefreshMeasurementResult(requestDate.Value);
			requestDate = null;
		}
		else if (tabbedPage.CurrentPage is RecordView)
		{
			DisplayName = "記録";
			RecordViewModel.RefreshMeasurementResult();
		}
		else if (tabbedPage.CurrentPage is TakePhotoView)
		{
			IsTabVisible = false;
			DisplayName = string.Empty;
			TakePhotoViewModel.Start();
		}
		else if (tabbedPage.CurrentPage is PickPhotoView)
		{
			DisplayName = string.Empty;
			PickPhotoViewModel.Start();
		}
	}

	void IHandle<TabChangeEvent>.Handle(TabChangeEvent message)
	{
		Log.Debug($"【IG】【MasterDetailRootViewModel】【IHandle<TabChangeEvent>.Handle】message.RequestDate={message.RequestDate}");
		requestDate = message.RequestDate;
		if (!(((GetView() as MasterDetailRootView)?.Detail as NavigationPage)?.CurrentPage is TabbedPage tabbedPage))
		{
			return;
		}
		if (!requestDate.HasValue)
		{
			if (OldDisplayName == "げんきノート")
			{
				tabbedPage.CurrentPage = tabbedPage.Children[0];
			}
			else
			{
				tabbedPage.CurrentPage = tabbedPage.Children[1];
			}
		}
		else if (tabbedPage.Children.Count <= message.TabIndex)
		{
			tabbedPage.CurrentPage = tabbedPage.Children[1];
		}
		else
		{
			tabbedPage.CurrentPage = tabbedPage.Children[message.TabIndex];
		}
	}

	async void IHandle<VersionEvent>.Handle(VersionEvent message)
	{
		IAssemblyService assemblyService = DependencyService.Get<IAssemblyService>();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.Append("アプリケーション:").Append(assemblyService.GetPackageName()).Append(Environment.NewLine);
		stringBuilder.Append("バージョン:").Append(assemblyService.GetVersionCode() + "." + assemblyService.GetVersionName());
		await ShowAlert("バージョン", stringBuilder.ToString());
	}

	void IHandle<MenuNavigatedEvent>.Handle(MenuNavigatedEvent message)
	{
		IsPresentedMenu = false;
	}

	async void IHandle<LifecycleEvent>.Handle(LifecycleEvent message)
	{
		Log.Info("【IG】【MasterDetailRootViewModel】【IHandle<LifecycleEvent>.Handle】event: " + message.State);
		if (!IsActive)
		{
			return;
		}
		if (message.State == LifecycleEvent.Status.Sleep)
		{
			_IsSleep = true;
			StopHelthCheck();
		}
		else
		{
			_IsSleep = false;
		}
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

	async void IHandle<CentralManagerEvent>.Handle(CentralManagerEvent message)
	{
		Log.Info("【IG】【MasterDetailRootViewModel】【IHandle<CentralManagerEvent>.Handle】event: " + message.Status);
		if (!IsActive)
		{
			return;
		}
		if (message.Status == CentralManagerEvent.CentralManagerState.PoweredOn)
		{
			CentralManagerReadyEvent.Set();
			await StartScanEvent();
			return;
		}
		CentralManagerReadyEvent.Reset();
		if (isScanning)
		{
			RestartBLELibReceiveWait();
		}
	}

	public void Handle(ScanControlEvent message)
	{
		Log.Debug("【IG】【MasterDetailRootViewModel】【IHandle<ScanControlEvent>.Handle】event: " + message.Command);
		switch (message.Command)
		{
		case ScanControlEvent.CommandType.StartScan:
			StartScan();
			break;
		case ScanControlEvent.CommandType.StopScan:
			ReceiveStop();
			break;
		case ScanControlEvent.CommandType.Restart:
			RestartBLELibReceiveWait();
			break;
		}
	}

	public void Handle(UpdateTimezoneRangeEvent message)
	{
		MakeTimezoneRange();
	}

	public void Handle(UpdateTimezoneOtherRangeEvent message)
	{
		MakeTimezoneOtherRange();
	}

	async void IHandle<ShowVersionUpMessageEvent>.Handle(ShowVersionUpMessageEvent message)
	{
		if (IsActive)
		{
			await ShowVersionUpMessage();
		}
	}

	void IHandle<LocationRequestEvent>.Handle(LocationRequestEvent message)
	{
		Log.Info("【IG】【MasterDetailRootViewModel】【IHandle<LocationRequestEvent>.Handle】event: " + message.Status);
		if (IsActive)
		{
			LocationRequestCompleteEvent.Set();
		}
	}
}

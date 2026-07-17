using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using BLELib;
using BLELib.Common;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Common;
using NHL.Models;
using NHL.Models.Types;
using NHL.Services.DependencyService;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class MeterScanViewModel : ViewModelBase, IHandle<LifecycleEvent>, IHandle, IHandle<CentralManagerEvent>, IHandle<LocationRequestEvent>
{
	private class ScanFilterItem
	{
		public string Name { get; set; }

		public MeterCategory MeterCategory { get; set; }
	}

	private NHL.Services.DependencyService.ILoggingService Log = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();

	private readonly IList<ScanFilterItem> SCAN_FILTER_LIST = new List<ScanFilterItem>
	{
		new ScanFilterItem
		{
			Name = "NIPRO CF",
			MeterCategory = MeterCategory.GL
		},
		new ScanFilterItem
		{
			Name = "NSM-1BLE",
			MeterCategory = MeterCategory.HT
		},
		new ScanFilterItem
		{
			Name = "NT-100B",
			MeterCategory = MeterCategory.HT
		},
		new ScanFilterItem
		{
			Name = "NBP-1BLE",
			MeterCategory = MeterCategory.BP
		},
		new ScanFilterItem
		{
			Name = "NBCM",
			MeterCategory = MeterCategory.BC
		}
	};

	private readonly int SCAN_TIMEOUT = 30000;

	private static readonly string PIARING_ERROR_MESSAGE = "ペアリングに失敗しました。OSのBluetooth設定から測定器の登録を一度解除して、再度やりなおしてください。";

	private BindableCollection<MeterModel> meterList;

	private MeterModel selectedMeterModel;

	private bool isScanning;

	private bool isPairing;

	private static object _ScanLock = new object();

	private static object _PairingLock = new object();

	private ManualResetEventSlim CentralManagerReadyEvent = new ManualResetEventSlim(initialState: false);

	private ManualResetEventSlim LocationRequestCompleteEvent = new ManualResetEventSlim(initialState: false);

	private static object _MeterListLock = new object();

	private bool _scanBCMeter;

	private MeterCategory _meterCategory;

	public MeterContext MeterContext { get; set; }

	public BindableCollection<MeterModel> MeterList
	{
		get
		{
			return meterList;
		}
		set
		{
			if (meterList != value)
			{
				meterList = value;
				NotifyOfPropertyChange(() => MeterList);
			}
		}
	}

	public MeterModel SelectedMeterModel
	{
		get
		{
			return selectedMeterModel;
		}
		set
		{
			if (selectedMeterModel == value)
			{
				return;
			}
			if (selectedMeterModel != value)
			{
				selectedMeterModel = value;
				NotifyOfPropertyChange(() => SelectedMeterModel);
			}
			if (selectedMeterModel != null)
			{
				Pair();
			}
		}
	}

	public MeterCategory MeterCategory
	{
		get
		{
			return _meterCategory;
		}
		set
		{
			if (_meterCategory != value)
			{
				_meterCategory = value;
				NotifyOfPropertyChange(() => MeterCategory);
			}
		}
	}

	public IBLELib BLELib { get; set; }

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

	public bool IsPairing
	{
		get
		{
			lock (_PairingLock)
			{
				return isPairing;
			}
		}
		set
		{
			lock (_PairingLock)
			{
				isPairing = value;
				NotifyOfPropertyChange(() => IsPairing);
			}
		}
	}

	public bool ScanBCMeter
	{
		get
		{
			return _scanBCMeter;
		}
		set
		{
			Set(ref _scanBCMeter, value, "ScanBCMeter");
		}
	}

	public MeterScanViewModel()
	{
		MeterContext = IoC.Get<MeterContext>();
	}

	protected override async void OnActivate()
	{
		Log.Debug("【IG】【MeterScanViewModel】【OnActivate】OnActivate start");
		ScanBCMeter = MeterCategory == MeterCategory.BC;
		MeterContext = IoC.Get<MeterContext>();
		if (MeterContext.ReturnedFromColorSelection)
		{
			MeterContext.Initialize("USERNAME");
			await base.NavigationService.GoBackAsync();
			Execute.BeginOnUIThread(async delegate
			{
				await Task.Delay(1000);
				base.EventAggregator.BeginPublishOnUIThread(new ScanControlEvent
				{
					Command = ScanControlEvent.CommandType.Restart
				});
			});
			return;
		}
		base.EventAggregator.Subscribe(this);
		MeterList = new BindableCollection<MeterModel>();
		SelectedMeterModel = null;
		base.EventAggregator.PublishOnUIThread(new ScanControlEvent
		{
			Command = ScanControlEvent.CommandType.StopScan
		});
		try
		{
			await StartScan();
		}
		catch (OperationCanceledException ex)
		{
			Log.Error($"【IG】【MeterScanViewModel】【OnActivate】スキャン時の例外:{ex}");
		}
		catch (Exception ex2)
		{
			Log.Error($"【IG】【MeterScanViewModel】【OnActivate】OnActivate時の例外:{ex2}");
		}
		base.OnActivate();
		Log.Debug("【IG】【MeterScanViewModel】【OnActivate】OnActivate finish");
	}

	protected override async void OnDeactivate(bool close)
	{
		Log.Debug("【IG】【MeterScanViewModel】【OnDeactivate】OnDeactivate start");
		try
		{
			await StopScan();
			base.EventAggregator.BeginPublishOnUIThread(new ScanControlEvent
			{
				Command = ScanControlEvent.CommandType.StartScan
			});
			base.EventAggregator.Unsubscribe(this);
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【MeterScanViewModel】【OnDeactivate】例外発生：{ex}");
		}
		base.OnDeactivate(close);
		Log.Debug("【IG】【MeterScanViewModel】【OnDeactivate】OnDeactivate finish");
	}

	async void IHandle<LifecycleEvent>.Handle(LifecycleEvent message)
	{
		Log.Info(string.Format("【IG】【MeterScanViewModel】【IHandle<LifecycleEvent>.Handle】event={0}, IsScanning={1}, IsPairing={2}", new object[3]
		{
			message.State.ToString(),
			IsScanning,
			IsPairing
		}));
		if (!IsActive)
		{
			return;
		}
		if (IsScanning && message.State == LifecycleEvent.Status.Sleep)
		{
			if (!IsPairing)
			{
				await StopScan();
			}
		}
		else if (!IsScanning && message.State == LifecycleEvent.Status.Resume)
		{
			await StartScan();
		}
	}

	async void IHandle<CentralManagerEvent>.Handle(CentralManagerEvent message)
	{
		Log.Info("【IG】【MeterScanViewModel】【IHandle<CentralManagerEvent>.Handle】event: " + message.Status);
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
		if (IsScanning)
		{
			RestartBLELibScan();
		}
	}

	void IHandle<LocationRequestEvent>.Handle(LocationRequestEvent message)
	{
		Log.Info("【IG】【MeterScanViewModel】【IHandle<LocationRequestEvent>.Handle】event: " + message.Status);
		if (IsActive)
		{
			LocationRequestCompleteEvent.Set();
		}
	}

	private async Task Pair()
	{
		Log.Info("【IG】【MeterScanViewModel】【Pair】ペアリング");
		await StopScan();
		try
		{
			Guid id = SelectedMeterModel.Id;
			string name = SelectedMeterModel.Name;
			IsPairing = true;
			Log.Info("【IG】【MeterScanViewModel】【Pair】ペアリング開始:meterName=" + name);
			IList<string> list;
			if (SelectedMeterModel != null && SelectedMeterModel.Name.StartsWith("NBCM"))
			{
				IList<string> param = new List<string>
				{
					base.UserManager.IgUser.Name,
					base.UserManager.IgUser.Sex,
					base.UserManager.IgUser.Height?.ToString(),
					base.UserManager.IgUser.Birthday?.ToString()
				};
				list = await BLELib.Pairing(id, name, param);
			}
			else
			{
				list = await BLELib.Pairing(id, name);
			}
			Log.Info($"【IG】【MeterScanViewModel】【Pair】res : {list}");
			if (list != null && (list.Count == 3 || list.Count == 4) && !string.IsNullOrEmpty(list[2]))
			{
				string serialNumber = list[2];
				string text = ((list.Count >= 4) ? list[3] : string.Empty);
				MeterModel meter = new MeterModel
				{
					Id = SelectedMeterModel.Id,
					Name = SelectedMeterModel.Name.Trim(),
					SerialNumber = serialNumber,
					DeviceType = (int)MeterCategory,
					UserNo = text,
					ColorCode = ColorType.UNKNOWN.Code
				};
				Log.Info($"【IG】【MeterScanViewModel】【Pair】ペアリング終了:Id={meter.Id}, Name={meter.Name}, SerialNumber={meter.SerialNumber}, DeviceType={meter.DeviceType}(MeterCategory={MeterCategory.ToString()}) userNo={text}");
				if (SelectedMeterModel.Name.StartsWith("NBCM") && string.IsNullOrEmpty(text))
				{
					Log.Info("【IG】【MeterScanViewModel】【Pair】ユーザーNo取得失敗");
					await ShowAlert("ユーザー登録に失敗", PIARING_ERROR_MESSAGE);
					await base.NavigationService.GoBackAsync();
					return;
				}
				SelectedMeterModel.SerialNumber = serialNumber;
				await MeterContext.Pair(meter, "USERNAME", MeterCategory);
				await ShowAlert("", "ペアリングに成功しました。");
				if (SelectedMeterModel.Name.StartsWith("NBCM"))
				{
					base.NavigationService.For<ColorSelectionViewModel>().WithParam((ColorSelectionViewModel x) => x.Meter, meter).Navigate();
					return;
				}
			}
			else
			{
				Log.Info("【IG】【MeterScanViewModel】【Pair】ペアリング終了:エラー");
				await ShowAlert("", PIARING_ERROR_MESSAGE);
			}
			await base.NavigationService.GoBackAsync();
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【MeterScanViewModel】【Pair】ペアリング時の例外:{ex}");
			await ShowAlert("例外", PIARING_ERROR_MESSAGE);
		}
		finally
		{
			SelectedMeterModel = null;
			IsPairing = false;
		}
	}

	private async Task StartScan()
	{
		Log.Debug("【IG】【MeterScanViewModel】【StartScan】StartScan");
		if (BLELib != null)
		{
			await StopScan();
		}
		if (IsScanning)
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
				Execute.OnUIThread(async delegate
				{
					Log.Debug("【IG】【MeterScanViewModel】【StartScan】CentralManagerReadyEvent timeout");
					await base.DialogProvider.ShowAlert("", "Bluetoothが無効です");
				});
			}
		});
	}

	private async Task StartScanEvent()
	{
		Log.Debug("【IG】【MeterScanViewModel】【StartScanEvent】StartScanEvent start");
		if (!base.IsActive)
		{
			Log.Trace("【IG】【MeterScanViewModel】【StartScan】screen isnot active, do nothing.");
			return;
		}
		await Task.Run(async delegate
		{
			BLELib = DependencyService.Get<IBLELibService>().GetBLELibrary();
			BLELib.ILoggingService = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();
			BLELib.Initialize();
			BLELib.StateChanged += BLELib_StateChanged;
			IsScanning = true;
			List<string> list = (from x in SCAN_FILTER_LIST
				where x.MeterCategory == MeterCategory
				select x.Name).ToList();
			Log.Info("【IG】【MeterScanViewModel】【StartScanEvent】StartScan:filter=" + list.JoinString(","));
			await BLELib.ScanStart(list, SCAN_TIMEOUT, ScanHandler);
		});
	}

	private async Task StopScan()
	{
		Log.Trace("【IG】【MeterScanViewModel】【StopScan】");
		if (BLELib == null || (!IsScanning && !BLELib.IsScanning))
		{
			return;
		}
		DependencyService.Get<IRequestBluetooth>().ResetCentralManager();
		await Task.Run(async delegate
		{
			if (BLELib != null)
			{
				Log.Info("【IG】【MeterScanViewModel】【StopScan】StopScan開始");
				await BLELib.ScanStop();
				BLELib.StateChanged -= BLELib_StateChanged;
				BLELib.Dispose();
				IsScanning = false;
			}
		});
	}

	private void RestartBLELibScan()
	{
		Task.Run(async delegate
		{
			Log.Debug("【IG】【MeterScanViewModel】【RestartBLELibScan】待受け(BLELib)の再起動 - Scan停止");
			await StopScan();
			await Task.Delay(10);
			Log.Debug("【IG】【MeterScanViewModel】【RestartBLELibScan】待受け(BLELib)の再起動 - Scan開始");
			await StartScan();
		}).ConfigureAwait(continueOnCapturedContext: false);
	}

	private void ScanHandler(IList<string> s)
	{
		if (!IsActive || s == null)
		{
			return;
		}
		string text = ((s.Count >= 1) ? s[0] : "");
		string id = ((s.Count >= 2) ? s[1] : "");
		string text2 = ((s.Count >= 3) ? s[2] : "");
		string text3 = ((s.Count >= 4) ? s[3] : "");
		Log.Info("【IG】【MeterScanViewModel】【ScanHandler】name=" + text + ", Id=" + id + ", Rssi=" + text2 + ", State=" + text3);
		lock (_MeterListLock)
		{
			MeterModel meterModel = MeterList.FirstOrDefault((MeterModel mm) => mm.Id == Guid.Parse(id));
			if (meterModel == null)
			{
				MeterModel item = new MeterModel
				{
					Id = Guid.Parse(id),
					Name = text,
					SerialNumber = "",
					DeviceType = (int)MeterCategory,
					IsConnected = (text3 == "Connected")
				};
				MeterList.Add(item);
			}
			else
			{
				meterModel.Id = Guid.Parse(id);
				meterModel.IsConnected = text3 == "Connected";
			}
		}
	}

	private void BLELib_StateChanged(object sender, BLELibStatusEventArgs e)
	{
		Log.Info(string.Format("【IG】【MeterScanViewModel】【BLELib_StateChanged】sender={0}, DeviceName={1}, BLELibStatus={2}", new object[3]
		{
			sender,
			e.DeviceName,
			e.Status.ToString()
		}));
		if (e.Status == BLELibStatus.SCAN_TIMEOUT)
		{
			StopScan();
			Execute.OnUIThread(async delegate
			{
				await ShowAlert("", PIARING_ERROR_MESSAGE);
				await base.NavigationService.GoBackAsync();
			});
		}
		else if (e.Status == BLELibStatus.ADAPTER_DEVICE_CONNECTION_LOST)
		{
			if (SelectedMeterModel != null && SelectedMeterModel.Name.StartsWith("NIPRO CF"))
			{
				Execute.OnUIThread(async delegate
				{
					await ShowAlert("", PIARING_ERROR_MESSAGE);
					await base.NavigationService.GoBackAsync();
				});
			}
		}
		else if (e.Status == BLELibStatus.PAIR_ERR)
		{
			Execute.OnUIThread(async delegate
			{
				await ShowAlert("エラー", PIARING_ERROR_MESSAGE);
				await base.NavigationService.GoBackAsync();
			});
		}
		else if (e.Status == BLELibStatus.PAIR_TIMEOUT)
		{
			Execute.OnUIThread(async delegate
			{
				await ShowAlert("エラー", PIARING_ERROR_MESSAGE);
				await base.NavigationService.GoBackAsync();
			});
		}
	}
}

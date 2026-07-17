using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using BLELib;
using BLELib.Common;
using Caliburn.Micro;
using NHL.Common;
using NHL.Models;
using NHL.Models.Types;
using NHL.Properties;
using NHL.Services.DependencyService;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class ColorSelectionViewModel : ViewModelBase, IHandle<LifecycleEvent>, IHandle
{
	public const string LOG_PREFIX = "【IG】【ColorSelectionViewModel】";

	public const int SCAN_TIMEOUT = 30000;

	public const int GET_FREE_COLOR_PROCESSING_TIMEOUT = 60000;

	private static object _ScanLock = new object();

	private static object _MeterListLock = new object();

	private NHL.Services.DependencyService.ILoggingService Log = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();

	private bool _isScanning;

	private BindableCollection<ColorInfoModel> _colorList;

	private ColorInfoModel _selectedColor;

	private BindableCollection<MeterModel> _meterList;

	private bool _scanToGetFreeColor;

	private bool _bleProcessingEnabled;

	public MeterContext MeterContext { get; set; }

	public IBLELib BLELib { get; set; }

	public MeterModel Meter { get; set; }

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

	public BindableCollection<ColorInfoModel> ColorList
	{
		get
		{
			return _colorList;
		}
		set
		{
			Set(ref _colorList, value, "ColorList");
		}
	}

	public ColorInfoModel SelectedColor
	{
		get
		{
			return _selectedColor;
		}
		set
		{
			Set(ref _selectedColor, value, "SelectedColor");
			NotifyOfPropertyChange(() => CanDecision);
		}
	}

	public bool CanUpdateColorList => !_bleProcessingEnabled;

	public bool CanDecision
	{
		get
		{
			if (!_bleProcessingEnabled && SelectedColor != null)
			{
				return !string.IsNullOrEmpty(SelectedColor.ColorCode);
			}
			return false;
		}
	}

	public ColorSelectionViewModel()
	{
		MeterContext = IoC.Get<MeterContext>();
	}

	async void IHandle<LifecycleEvent>.Handle(LifecycleEvent message)
	{
		Log.Info(string.Format("{0}【IHandle<LifecycleEvent>.Handle】event={1}, IsScanning={2}", new object[3]
		{
			"【IG】【ColorSelectionViewModel】",
			message.State.ToString(),
			IsScanning
		}));
		if (IsScanning && message.State == LifecycleEvent.Status.Sleep)
		{
			await StopScan();
		}
		else if (!IsScanning)
		{
			_ = message.State;
			_ = 1;
		}
	}

	public async Task Decision()
	{
		_bleProcessingEnabled = true;
		_scanToGetFreeColor = false;
		await StartScan();
	}

	public async Task UpdateColorList()
	{
		_bleProcessingEnabled = true;
		_scanToGetFreeColor = true;
		await StartScan();
	}

	protected override void OnInitialize()
	{
		base.EventAggregator.Subscribe(this);
		base.OnInitialize();
	}

	protected override async void OnActivate()
	{
		MeterContext.ReturnedFromColorSelection = true;
		ColorList = new BindableCollection<ColorInfoModel>();
		_bleProcessingEnabled = true;
		_scanToGetFreeColor = true;
		await ExecAsync(async delegate
		{
			await Task.Delay(3000);
		});
		await StartScan();
		base.OnActivate();
	}

	protected override async void OnDeactivate(bool close)
	{
		await StopScan();
		base.OnDeactivate(close);
	}

	private async Task StartScan()
	{
		Log.Debug("【IG】【ColorSelectionViewModel】【StartScan】start");
		if (!IsActive)
		{
			Log.Trace("【IG】【ColorSelectionViewModel】【StartScan】画面がアクティブではない");
			return;
		}
		if (BLELib != null)
		{
			BLELib.StateChanged -= OnStatusChanged;
			BLELib.Dispose();
			BLELib = null;
		}
		_meterList = new BindableCollection<MeterModel>();
		BLELib = DependencyService.Get<IBLELibService>().GetBLELibrary();
		BLELib.ILoggingService = DependencyService.Get<NHL.Services.DependencyService.ILoggingService>();
		BLELib.Initialize();
		BLELib.StateChanged += OnStatusChanged;
		IsScanning = true;
		NotifyOfCanCommand();
		await BLELib.ScanStart(new List<string> { "NBCM" }, 30000, ScanHandler);
	}

	private async Task StopScan()
	{
		Log.Debug("【IG】【ColorSelectionViewModel】【StopScan】start");
		if (BLELib == null || !BLELib.IsScanning || !IsScanning)
		{
			Log.Trace("【IG】【ColorSelectionViewModel】【StartScan】スキャン中ではない");
			return;
		}
		try
		{
			Log.Info("【IG】【ColorSelectionViewModel】【StopScan】BLELibのScanStopを開始");
			await BLELib.ScanStop();
		}
		catch (Exception ex)
		{
			Log.Error(string.Format("{0}【StopScan】BLELibのScanStopで例外発生：{1}", new object[2] { "【IG】【ColorSelectionViewModel】", ex }));
		}
		finally
		{
			if (BLELib != null)
			{
				BLELib.StateChanged -= OnStatusChanged;
				BLELib.Dispose();
				BLELib = null;
			}
			IsScanning = false;
			NotifyOfCanCommand();
		}
	}

	private async void OnStatusChanged(object sender, BLELibStatusEventArgs e)
	{
		Log.Info("【IG】【ColorSelectionViewModel】【OnStateChanged】DeviceName=" + e.DeviceName + ", BLELibStatus=" + e.Status);
		switch (e.Status)
		{
		case BLELibStatus.SCAN_TIMEOUT:
			Log.Error("【IG】【ColorSelectionViewModel】【OnStateChanged】スキャンタイムアウト");
			await base.DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_011);
			_bleProcessingEnabled = false;
			break;
		case BLELibStatus.GET_FREE_COLOR_ERR:
			Log.Error("【IG】【ColorSelectionViewModel】【OnStateChanged】空き色取得失敗を検知。");
			await base.DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_011);
			_bleProcessingEnabled = false;
			break;
		case BLELibStatus.REGISTER_USER_ERR:
			Log.Error("【IG】【ColorSelectionViewModel】【OnStateChanged】ユーザー登録失敗を検知。");
			_bleProcessingEnabled = false;
			break;
		}
		NotifyOfCanCommand();
	}

	private void ScanHandler(IList<string> deviceInfoList)
	{
		if (!_bleProcessingEnabled || deviceInfoList == null || deviceInfoList.Count == 0)
		{
			return;
		}
		string name = ((deviceInfoList.Count >= 1) ? deviceInfoList[0] : string.Empty);
		string text = ((deviceInfoList.Count >= 2) ? deviceInfoList[1] : string.Empty);
		string text2 = ((deviceInfoList.Count >= 3) ? deviceInfoList[2] : string.Empty);
		string text3 = ((deviceInfoList.Count >= 4) ? deviceInfoList[3] : string.Empty);
		Log.Info("【IG】【MeterScanViewModel】【ScanHandler】name=" + name + ", Id=" + text + ", Rssi=" + text2 + ", State=" + text3);
		lock (_MeterListLock)
		{
			MeterModel meterModel = _meterList.FirstOrDefault((MeterModel x) => x.Name == name);
			if (meterModel != null)
			{
				meterModel.Id = Guid.Parse(text);
				meterModel.IsConnected = text3 == "Connected";
			}
			else
			{
				MeterModel item = new MeterModel
				{
					Id = Guid.Parse(text),
					Name = name,
					SerialNumber = string.Empty,
					DeviceType = 1,
					IsConnected = (text3 == "Connected")
				};
				_meterList.Add(item);
			}
		}
		MeterModel meterModel2 = _meterList.FirstOrDefault((MeterModel x) => x.Name == name);
		if (meterModel2 == null || meterModel2.Name != MeterContext.BCMeter.Name)
		{
			return;
		}
		_bleProcessingEnabled = false;
		Execute.BeginOnUIThread(async delegate
		{
			bool result = false;
			await ExecAsync(async delegate
			{
				if (_scanToGetFreeColor)
				{
					List<string> param = new List<string>();
					IList<object> colorInfoList = await BLELib.GetFreeColor(MeterContext.BCMeter.Name, 60000, param);
					SetColorInfoList(colorInfoList);
				}
				else
				{
					List<string> param2 = new List<string>
					{
						base.UserManager.IgUser.Name,
						base.UserManager.IgUser.Sex,
						base.UserManager.IgUser.Height?.ToString(),
						base.UserManager.IgUser.Birthday?.ToString(),
						Meter.UserNo,
						ColorType.GetInstance(SelectedColor.ColorCode).ColorValue.ToString()
					};
					result = await BLELib.RegisterUserColor(MeterContext.BCMeter.Name, 60000, param2);
				}
				await StopScan();
			});
			NotifyOfCanCommand();
			if (!_scanToGetFreeColor)
			{
				if (!result)
				{
					await base.DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_010);
				}
				else
				{
					ColorType instance = ColorType.GetInstance(SelectedColor.ColorCode);
					Meter.ColorCode = instance.Code;
					await MeterContext.Unregister(MeterCategory.BC, "USERNAME");
					await MeterContext.Pair(Meter, "USERNAME", MeterCategory.BC);
					await base.NavigationService.GoBackAsync();
				}
			}
		});
	}

	private ColorInfoModel CreateColorInfoModel(ColorType colorType)
	{
		return new ColorInfoModel
		{
			ColorCode = colorType.Code,
			Name = colorType.Name
		};
	}

	private void SetColorInfoList(IList<object> colorAvailabilityList)
	{
		if (colorAvailabilityList == null || colorAvailabilityList.Count == 0)
		{
			ColorList = new BindableCollection<ColorInfoModel>();
			return;
		}
		BindableCollection<ColorInfoModel> bindableCollection = new BindableCollection<ColorInfoModel>();
		if ((bool)colorAvailabilityList[0])
		{
			bindableCollection.Add(CreateColorInfoModel(ColorType.GREEN));
		}
		if ((bool)colorAvailabilityList[1])
		{
			bindableCollection.Add(CreateColorInfoModel(ColorType.LIGHT_BLUE));
		}
		if ((bool)colorAvailabilityList[2])
		{
			bindableCollection.Add(CreateColorInfoModel(ColorType.YELLOW));
		}
		if ((bool)colorAvailabilityList[3])
		{
			bindableCollection.Add(CreateColorInfoModel(ColorType.ORANGE));
		}
		if ((bool)colorAvailabilityList[4])
		{
			bindableCollection.Add(CreateColorInfoModel(ColorType.PURPLE));
		}
		ColorList = bindableCollection;
	}

	private void NotifyOfCanCommand()
	{
		NotifyOfPropertyChange(() => CanUpdateColorList);
		NotifyOfPropertyChange(() => CanDecision);
	}
}

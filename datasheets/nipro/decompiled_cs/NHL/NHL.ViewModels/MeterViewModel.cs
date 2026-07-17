using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Common;
using NHL.Models;
using NHL.Models.Types;
using NHL.Properties;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class MeterViewModel : ViewModelBase
{
	private MeterModel thermometer;

	private MeterModel sphygmomanometer;

	private MeterModel bloodGlucoseMeter;

	private MeterModel bodyCompositionMeter;

	private string _bodyCompositionMeterUserInfo;

	public new IDialogProvider DialogProvider { get; set; }

	public MeterContext MeterContext { get; set; }

	public MeterModel Thermometer
	{
		get
		{
			return thermometer;
		}
		set
		{
			if (thermometer != value)
			{
				thermometer = value;
				NotifyOfPropertyChange(() => Thermometer);
			}
		}
	}

	public MeterModel Sphygmomanometer
	{
		get
		{
			return sphygmomanometer;
		}
		set
		{
			if (sphygmomanometer != value)
			{
				sphygmomanometer = value;
				NotifyOfPropertyChange(() => Sphygmomanometer);
			}
		}
	}

	public MeterModel BloodGlucoseMeter
	{
		get
		{
			return bloodGlucoseMeter;
		}
		set
		{
			if (bloodGlucoseMeter != value)
			{
				bloodGlucoseMeter = value;
				NotifyOfPropertyChange(() => BloodGlucoseMeter);
			}
		}
	}

	public MeterModel BodyCompositionMeter
	{
		get
		{
			return bodyCompositionMeter;
		}
		set
		{
			Set(ref bodyCompositionMeter, value, "BodyCompositionMeter");
		}
	}

	public string BodyCompositionMeterUserInfo
	{
		get
		{
			return _bodyCompositionMeterUserInfo;
		}
		set
		{
			Set(ref _bodyCompositionMeterUserInfo, value, "BodyCompositionMeterUserInfo");
		}
	}

	public bool HasBodyCompositionCalcInfo
	{
		get
		{
			if (!string.IsNullOrEmpty(base.UserManager.IgUser.Sex) && base.UserManager.IgUser.Height.HasValue)
			{
				return base.UserManager.IgUser.Birthday.HasValue;
			}
			return false;
		}
	}

	public MeterViewModel()
	{
		MeterContext = IoC.Get<MeterContext>();
	}

	public void ScanHT()
	{
		if (Thermometer.Name == "未登録")
		{
			base.NavigationService.For<MeterScanViewModel>().WithParam((MeterScanViewModel x) => x.MeterCategory, MeterCategory.HT).Navigate();
		}
		else
		{
			base.NavigationService.For<MeterDetailViewModel>().WithParam((MeterDetailViewModel x) => x.Meter, Thermometer).Navigate();
		}
	}

	public void ScanBP()
	{
		if (Sphygmomanometer.Name == "未登録")
		{
			base.NavigationService.For<MeterScanViewModel>().WithParam((MeterScanViewModel x) => x.MeterCategory, MeterCategory.BP).Navigate();
		}
		else
		{
			base.NavigationService.For<MeterDetailViewModel>().WithParam((MeterDetailViewModel x) => x.Meter, Sphygmomanometer).Navigate();
		}
	}

	public void ScanGL()
	{
		if (BloodGlucoseMeter.Name == "未登録")
		{
			base.NavigationService.For<MeterScanViewModel>().WithParam((MeterScanViewModel x) => x.MeterCategory, MeterCategory.GL).Navigate();
		}
		else
		{
			base.NavigationService.For<MeterDetailViewModel>().WithParam((MeterDetailViewModel x) => x.Meter, BloodGlucoseMeter).Navigate();
		}
	}

	public async Task ScanBC()
	{
		if (!HasBodyCompositionCalcInfo && string.IsNullOrEmpty(BodyCompositionMeter?.SerialNumber))
		{
			await DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_007);
		}
		if (base.UserManager.IgUser.Height < 90.0 || base.UserManager.IgUser.Height > 220.0 || base.UserManager.IgUser.Height - Math.Floor(base.UserManager.IgUser.Height.Value) != 0.0)
		{
			await DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_014);
		}
		ChangeContentToMeterScan(BodyCompositionMeter, MeterCategory.BC);
	}

	protected override void OnInitialize()
	{
		base.OnInitialize();
		Thermometer = new MeterModel
		{
			Name = "未登録",
			DeviceType = 5
		};
		Sphygmomanometer = new MeterModel
		{
			Name = "未登録",
			DeviceType = 3
		};
		BloodGlucoseMeter = new MeterModel
		{
			Name = "未登録",
			DeviceType = 4
		};
		BodyCompositionMeter = new MeterModel
		{
			Name = "未登録",
			DeviceType = 1
		};
	}

	protected override void OnActivate()
	{
		Thermometer = ((MeterContext.HTMeter != null) ? MeterContext.HTMeter : Thermometer);
		Sphygmomanometer = ((MeterContext.BPMeter != null) ? MeterContext.BPMeter : Sphygmomanometer);
		BloodGlucoseMeter = ((MeterContext.GLMeter != null) ? MeterContext.GLMeter : BloodGlucoseMeter);
		BodyCompositionMeter = ((MeterContext.BCMeter != null) ? MeterContext.BCMeter : BodyCompositionMeter);
		if (BodyCompositionMeter == null || BodyCompositionMeter.Name == MeterContext.NON_REGIST_NAME)
		{
			BodyCompositionMeterUserInfo = string.Empty;
		}
		else
		{
			BodyCompositionMeterUserInfo = "ユーザーNo" + BodyCompositionMeter.UserNo + "：";
			ColorType instance = ColorType.GetInstance(BodyCompositionMeter.ColorCode);
			BodyCompositionMeterUserInfo += ((instance == ColorType.UNKNOWN) ? "色情報なし" : instance.Name);
		}
		if (Device.RuntimePlatform == "Android")
		{
			DependencyService.Get<ICheckPermissionService>().CheckLocationPermission();
		}
		base.OnActivate();
	}

	private void ChangeContentToMeterScan(MeterModel meter, MeterCategory category)
	{
		if (meter.Name == MeterContext.NON_REGIST_NAME)
		{
			base.NavigationService.For<MeterScanViewModel>().WithParam((MeterScanViewModel x) => x.MeterCategory, category).Navigate();
		}
		else
		{
			base.NavigationService.For<MeterDetailViewModel>().WithParam((MeterDetailViewModel x) => x.Meter, meter).Navigate();
		}
	}
}

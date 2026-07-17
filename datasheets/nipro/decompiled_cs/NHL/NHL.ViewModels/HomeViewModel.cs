using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Common;
using NHL.Models;
using NHL.Models.Entity;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class HomeViewModel : ViewModelBase, IHandle<HomeMasterDetailRootOnActivateEvent>, IHandle, IHandle<PresentedMenuEvent>, IHandle<HomeRegistDateSaveEvent>, IHandle<HomeDeletePhotographEvent>, IHandle<HomeUpdateMeasurementEvent>, IHandle<HomeSelectPhotographEvent>
{
	private BindableCollection<MeasurementResultModel> measurementResultModelList;

	private BindableCollection<Measurement> allMeasurementResultModelList;

	private BindableCollection<MeasurementResultModel> gluMeasurementList;

	private BindableCollection<NoticeDetailResponseModel> noticeList;

	private AuthenticationModel authenticatedUser;

	private string displayGluMeasurement;

	private string displayTempMeasurement;

	private string displayPressureMeasurement;

	private string displayPulseMeasurement;

	private string displayStepMeasurement;

	private string displayWeightMeasurement;

	private string displayFatPercentageMeasurement;

	private string displayBmiMeasurement;

	private DateTime selectedDate;

	private int selectedPosition;

	private BindableCollection<CarouselPhotographModel> foodPhotoList;

	private BindableCollection<DailyComments> dailyCommentModelList;

	private DailyComments dailyComment;

	private string displayDailyComment;

	private bool displayDailyCommentFlag;

	private DateTime? minTimezone;

	private bool demoPhotoFlag = true;

	private List<string> photoList;

	private bool isShowNotice;

	private bool isNetworkEnabled;

	private bool isPresentedMenu;

	private readonly ILoggingService log = DependencyService.Get<ILoggingService>();

	private bool _IsiPad;

	private bool _IsShowScanNBCMButton;

	public IMeasurementService MeasurementService { get; set; }

	public IPhotographService PhotographService { get; set; }

	public IDailyCommentService DailyCommentService { get; set; }

	private IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public IHospitalInfoService HospitalInfoService { get; set; }

	private INoticeService NoticeService { get; set; }

	public AuthenticationModel AuthenticatedUser
	{
		get
		{
			return authenticatedUser;
		}
		set
		{
			authenticatedUser = value;
			NotifyOfPropertyChange(() => AuthenticatedUser);
		}
	}

	public BindableCollection<MeasurementResultModel> MeasurementResultModelList
	{
		get
		{
			return measurementResultModelList;
		}
		set
		{
			measurementResultModelList = value;
			NotifyOfPropertyChange(() => MeasurementResultModelList);
		}
	}

	public BindableCollection<Measurement> AllMeasurementResultModelList
	{
		get
		{
			return allMeasurementResultModelList;
		}
		set
		{
			allMeasurementResultModelList = value;
			NotifyOfPropertyChange(() => AllMeasurementResultModelList);
		}
	}

	public BindableCollection<MeasurementResultModel> GluMeasurementList
	{
		get
		{
			return gluMeasurementList;
		}
		set
		{
			gluMeasurementList = value;
			NotifyOfPropertyChange(() => GluMeasurementList);
		}
	}

	public BindableCollection<NoticeDetailResponseModel> NoticeList
	{
		get
		{
			return noticeList;
		}
		set
		{
			noticeList = value;
			NotifyOfPropertyChange(() => NoticeList);
		}
	}

	public string DisplayGluMeasurement
	{
		get
		{
			return displayGluMeasurement;
		}
		set
		{
			displayGluMeasurement = value;
			NotifyOfPropertyChange(() => DisplayGluMeasurement);
		}
	}

	public string DisplayTempMeasurement
	{
		get
		{
			return displayTempMeasurement;
		}
		set
		{
			displayTempMeasurement = value;
			NotifyOfPropertyChange(() => DisplayTempMeasurement);
		}
	}

	public string DisplayPressureMeasurement
	{
		get
		{
			return displayPressureMeasurement;
		}
		set
		{
			displayPressureMeasurement = value;
			NotifyOfPropertyChange(() => DisplayPressureMeasurement);
		}
	}

	public string DisplayPulseMeasurement
	{
		get
		{
			return displayPulseMeasurement;
		}
		set
		{
			displayPulseMeasurement = value;
			NotifyOfPropertyChange(() => DisplayPulseMeasurement);
		}
	}

	public string DisplayStepMeasurement
	{
		get
		{
			if (int.TryParse(displayStepMeasurement, out var result))
			{
				return $"{result:#,0}";
			}
			return displayStepMeasurement;
		}
		set
		{
			displayStepMeasurement = value;
			NotifyOfPropertyChange(() => DisplayStepMeasurement);
		}
	}

	public string DisplayWeightMeasurement
	{
		get
		{
			return displayWeightMeasurement;
		}
		set
		{
			displayWeightMeasurement = value;
			NotifyOfPropertyChange(() => DisplayWeightMeasurement);
		}
	}

	public string DisplayFatPercentageMeasurement
	{
		get
		{
			return displayFatPercentageMeasurement;
		}
		set
		{
			displayFatPercentageMeasurement = value;
			NotifyOfPropertyChange(() => DisplayFatPercentageMeasurement);
		}
	}

	public string DisplayBmiMeasurement
	{
		get
		{
			return displayBmiMeasurement;
		}
		set
		{
			displayBmiMeasurement = value;
			NotifyOfPropertyChange(() => DisplayBmiMeasurement);
		}
	}

	public DateTime SelectedDate
	{
		get
		{
			return selectedDate;
		}
		set
		{
			selectedDate = value;
			NotifyOfPropertyChange(() => SelectedDate);
		}
	}

	public int SelectedPosition
	{
		get
		{
			return selectedPosition;
		}
		set
		{
			selectedPosition = value;
			NotifyOfPropertyChange(() => SelectedPosition);
		}
	}

	public BindableCollection<CarouselPhotographModel> FoodPhotoList
	{
		get
		{
			return foodPhotoList;
		}
		set
		{
			foodPhotoList = value;
			NotifyOfPropertyChange(() => FoodPhotoList);
		}
	}

	public BindableCollection<CarouselPhotographModel> DisplayFoodPhotoList => foodPhotoList;

	public bool DemoPhotoFlag
	{
		get
		{
			return demoPhotoFlag;
		}
		set
		{
			if (demoPhotoFlag != value)
			{
				demoPhotoFlag = value;
				NotifyOfPropertyChange(() => DemoPhotoFlag);
			}
		}
	}

	public BindableCollection<DailyComments> DailyCommentModelList
	{
		get
		{
			return dailyCommentModelList;
		}
		set
		{
			dailyCommentModelList = value;
			NotifyOfPropertyChange(() => DailyCommentModelList);
		}
	}

	public DailyComments DailyComment
	{
		get
		{
			return dailyComment;
		}
		set
		{
			dailyComment = value;
			NotifyOfPropertyChange(() => DailyComment);
		}
	}

	public string DisplayDailyComment
	{
		get
		{
			return displayDailyComment;
		}
		set
		{
			if (displayDailyComment != value)
			{
				displayDailyComment = value;
				NotifyOfPropertyChange(() => DisplayDailyComment);
			}
			DisplayDailyCommentFlag = string.IsNullOrEmpty(displayDailyComment);
		}
	}

	public bool DisplayDailyCommentFlag
	{
		get
		{
			return displayDailyCommentFlag;
		}
		set
		{
			displayDailyCommentFlag = value;
			NotifyOfPropertyChange(() => DisplayDailyCommentFlag);
		}
	}

	public DateTime? MinTimezone
	{
		get
		{
			return minTimezone;
		}
		set
		{
			minTimezone = value;
			NotifyOfPropertyChange(() => MinTimezone);
		}
	}

	public List<string> PhotoList
	{
		get
		{
			return photoList;
		}
		set
		{
			photoList = value;
			NotifyOfPropertyChange(() => PhotoList);
		}
	}

	public bool IsShowNotice
	{
		get
		{
			return isShowNotice;
		}
		set
		{
			isShowNotice = value;
			NotifyOfPropertyChange(() => IsShowNotice);
		}
	}

	public bool IsNetworkEnabled
	{
		get
		{
			return isNetworkEnabled;
		}
		set
		{
			isNetworkEnabled = value;
			NotifyOfPropertyChange(() => IsNetworkEnabled);
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
		}
	}

	public bool IsiPad
	{
		get
		{
			return _IsiPad;
		}
		set
		{
			_IsiPad = value;
			NotifyOfPropertyChange(() => IsiPad);
		}
	}

	public bool IsShowScanNBCMButton
	{
		get
		{
			return _IsShowScanNBCMButton;
		}
		set
		{
			_IsShowScanNBCMButton = value;
			NotifyOfPropertyChange(() => IsShowScanNBCMButton);
		}
	}

	public HomeViewModel()
	{
		IsiPad = NHL.Common.Common.IsiPad();
	}

	public async void Initialize()
	{
		base.EventAggregator.Unsubscribe(this);
		base.EventAggregator.Subscribe(this);
		IsShowNotice = false;
		MeasurementResultModelList = new BindableCollection<MeasurementResultModel>();
		FoodPhotoList = new BindableCollection<CarouselPhotographModel>();
		DailyCommentModelList = new BindableCollection<DailyComments>();
		await RefreshMeasurementResult();
	}

	public async void SelectToday()
	{
		SelectedDate = DateTime.Now;
		await RefreshMeasurementResult();
	}

	public async Task RefreshMeasurementResult()
	{
		await RefreshMeasurementResult(DateTime.Now);
	}

	private async Task RefreshMeasurementResult(DateTime date, bool isRefreshFoodPhotoList = true, bool isRefreshDailyCommentList = true)
	{
		await ExecAsync(async delegate
		{
			MeasurementResultModelList?.Clear();
			SelectedDate = date;
			BindableCollection<Measurement> bindableCollection = await MeasurementService.GetAllMeasurement();
			DisplayGluMeasurement = "--";
			DisplayTempMeasurement = "--";
			DisplayPressureMeasurement = "--";
			DisplayPulseMeasurement = "--";
			DisplayStepMeasurement = "--";
			DisplayWeightMeasurement = "--";
			DisplayFatPercentageMeasurement = "--";
			DisplayBmiMeasurement = "--";
			if (bindableCollection != null && bindableCollection.Count > 0)
			{
				AllMeasurementResultModelList = bindableCollection;
				MeasurementResultModelList = Filter(CreateMeasurementResultList(bindableCollection));
				if (MeasurementResultModelList.Count > 0)
				{
					GluMeasurementList = new BindableCollection<MeasurementResultModel>();
					BindableCollection<MeasurementResultModel> bindableCollection2 = new BindableCollection<MeasurementResultModel>();
					BindableCollection<MeasurementResultModel> bindableCollection3 = new BindableCollection<MeasurementResultModel>();
					BindableCollection<MeasurementResultModel> bindableCollection4 = new BindableCollection<MeasurementResultModel>();
					BindableCollection<MeasurementResultModel> bindableCollection5 = new BindableCollection<MeasurementResultModel>();
					BindableCollection<MeasurementResultModel> bindableCollection6 = new BindableCollection<MeasurementResultModel>();
					BindableCollection<MeasurementResultModel> bindableCollection7 = new BindableCollection<MeasurementResultModel>();
					BindableCollection<MeasurementResultModel> bindableCollection8 = new BindableCollection<MeasurementResultModel>();
					GluMeasurementList.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "血糖"));
					bindableCollection2.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "体温"));
					bindableCollection3.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "血圧"));
					bindableCollection4.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "脈拍"));
					bindableCollection5.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "歩数"));
					bindableCollection6.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "体重"));
					bindableCollection7.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "BMI"));
					bindableCollection8.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "体脂肪率"));
					if (GluMeasurementList != null && GluMeasurementList.Count > 0)
					{
						DisplayGluMeasurement = GluMeasurementList.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
					if (bindableCollection2.Count > 0)
					{
						DisplayTempMeasurement = bindableCollection2.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
					if (bindableCollection3.Count > 0)
					{
						DisplayPressureMeasurement = bindableCollection3.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
					if (bindableCollection4.Count > 0)
					{
						DisplayPulseMeasurement = bindableCollection4.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
					if (bindableCollection5.Count > 0)
					{
						DisplayStepMeasurement = bindableCollection5.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
					if (bindableCollection6.Count > 0)
					{
						DisplayWeightMeasurement = bindableCollection6.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
					if (bindableCollection7.Count > 0)
					{
						DisplayBmiMeasurement = bindableCollection7.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
					if (bindableCollection8.Count > 0)
					{
						DisplayFatPercentageMeasurement = bindableCollection8.OrderBy((MeasurementResultModel x) => x.RegistDate).Last().DisplayMeasurementValue;
					}
				}
			}
			if (isRefreshFoodPhotoList)
			{
				await RefreshFoodPhotoList();
			}
			if (isRefreshDailyCommentList)
			{
				await RefreshDailyCommentList();
			}
			DateTime? dateTime = await CalcMinTimezone();
			MinTimezone = dateTime;
		});
	}

	public async Task RefreshFoodPhotoList()
	{
		FoodPhotoList = new BindableCollection<CarouselPhotographModel>();
		DemoPhotoFlag = true;
		BindableCollection<Photograph> bindableCollection = await PhotographService.GetAllPhotograph();
		if (bindableCollection == null || bindableCollection.Count == 0)
		{
			return;
		}
		bool flag = true;
		bool flag2 = true;
		foreach (Photograph item in from x in bindableCollection
			where x.Image != null && !x.Deleted && x.TimezoneDate.Value.ToString("yyyyMMdd") == SelectedDate.ToString("yyyyMMdd")
			orderby x.ShootingAt
			select x)
		{
			if (flag)
			{
				FoodPhotoList.Add(new CarouselPhotographModel
				{
					LeftPhoto = item,
					IsVisibleBack = !flag2
				});
				flag = false;
				flag2 = false;
			}
			else
			{
				FoodPhotoList.Last().RightPhoto = item;
				FoodPhotoList.Last().IsVisibleNext = true;
				flag = true;
			}
		}
		if (FoodPhotoList.Count > 0)
		{
			FoodPhotoList.Last().IsVisibleNext = false;
			DemoPhotoFlag = false;
		}
		FoodPhotoList.Refresh();
	}

	public async Task RefreshDailyCommentList()
	{
		DailyCommentModelList = new BindableCollection<DailyComments>();
		BindableCollection<DailyComments> bindableCollection = await DailyCommentService.GetAllDailyComment();
		if (bindableCollection == null || bindableCollection.Count == 0)
		{
			DailyComment = new DailyComments
			{
				IgUserId = base.UserManager.IgUser.Id,
				TimezoneDate = new DateTime(SelectedDate.Year, SelectedDate.Month, SelectedDate.Day, 9, 0, 0),
				Deleted = false
			};
		}
		DailyComment = bindableCollection.FirstOrDefault((DailyComments x) => x.Comment != null && !x.Deleted && x.TimezoneDate.Value.ToString("yyyyMMdd") == SelectedDate.ToString("yyyyMMdd"));
		if (DailyComment == null)
		{
			DailyComment = new DailyComments
			{
				IgUserId = base.UserManager.IgUser.Id,
				TimezoneDate = new DateTime(SelectedDate.Year, SelectedDate.Month, SelectedDate.Day, 9, 0, 0),
				Deleted = false
			};
			DisplayDailyComment = null;
		}
		else
		{
			DisplayDailyComment = DailyComment.Comment;
		}
	}

	public void EditDailyCommentOnTapped()
	{
		base.NavigationService.For<EditDailyCommentViewModel>().WithParam((EditDailyCommentViewModel x) => x.DailyComments, DailyComment).Navigate();
	}

	public async void NoticeLabelOnTapped()
	{
		await Navigate<HospitalRegistrationViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async void GlucoseChartOnTapped()
	{
		await ExecAsync(async delegate
		{
			if (AllMeasurementResultModelList != null && AllMeasurementResultModelList.Count > 0)
			{
				MeasurementResultModelList = CreateMeasurementResultList(AllMeasurementResultModelList);
				if (MeasurementResultModelList.Count > 0)
				{
					GluMeasurementList = new BindableCollection<MeasurementResultModel>();
					GluMeasurementList.AddRange(MeasurementResultModelList.Where((MeasurementResultModel x) => x.DisplayCategory == "血糖"));
				}
			}
			BindableCollection<GlucoseChartDataModel> bindableCollection = new BindableCollection<GlucoseChartDataModel>();
			if (GluMeasurementList != null && GluMeasurementList.Count > 0)
			{
				foreach (MeasurementResultModel gluMeasurement in GluMeasurementList)
				{
					bindableCollection.Add(new GlucoseChartDataModel
					{
						MeasurementValue = gluMeasurement.MeasurementValue,
						TimezoneDate = gluMeasurement.TimezoneDate.ToString(),
						TimezoneType = gluMeasurement.TimezoneType
					});
				}
			}
			ChartContext chartContext = IoC.Get<ChartContext>();
			chartContext.Initialize();
			chartContext.GlucoseChartData = bindableCollection;
			await Navigate<GlucoseChartViewModel>();
		});
	}

	public async void TemperatureChartOnTapped()
	{
		await ExecAsync(async delegate
		{
			BindableCollection<TemperatureChartDataModel> bindableCollection = new BindableCollection<TemperatureChartDataModel>();
			if (AllMeasurementResultModelList != null && AllMeasurementResultModelList.Count > 0)
			{
				foreach (Measurement item in from x in AllMeasurementResultModelList
					where x.MeasurementType == "02"
					orderby x.MeasurementAt
					select x)
				{
					bindableCollection.Add(new TemperatureChartDataModel
					{
						MeasurementAt = item.MeasurementAt,
						Temperature = item.MeasurementValue,
						TimezoneDate = item.TimezoneDate.ToString()
					});
				}
			}
			ChartContext chartContext = IoC.Get<ChartContext>();
			chartContext.Initialize();
			chartContext.TemperatureChartData = bindableCollection;
			await Navigate<TemperatureChartViewModel>();
		});
	}

	public async void SphygChartOnTapped()
	{
		await ExecAsync(async delegate
		{
			BindableCollection<SphygmomanometerChartDataModel> bindableCollection = new BindableCollection<SphygmomanometerChartDataModel>();
			if (AllMeasurementResultModelList != null && AllMeasurementResultModelList.Count > 0)
			{
				foreach (DateTime? measurementAt in from x in AllMeasurementResultModelList
					where x.MeasurementType == "03"
					select x.MeasurementAt into x
					orderby x
					select x)
				{
					Measurement measurement = AllMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementAt == measurementAt && x.MeasurementType == "03");
					Measurement measurement2 = AllMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementAt == measurementAt && x.MeasurementType == "04");
					Measurement measurement3 = AllMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementAt == measurementAt && x.MeasurementType == "05");
					bindableCollection.Add(new SphygmomanometerChartDataModel
					{
						MeasurementAt = measurementAt,
						MaxPressure = measurement?.MeasurementValue,
						MinPressure = measurement2?.MeasurementValue,
						Pulse = measurement3?.MeasurementValue,
						TimezoneType = measurement?.Timezone,
						TimezoneDate = measurement?.TimezoneDate.ToString()
					});
				}
			}
			ChartContext chartContext = IoC.Get<ChartContext>();
			chartContext.Initialize();
			chartContext.SphygmomanometerChartData = bindableCollection;
			await Navigate<SphygmomanometerChartViewModel>();
		});
	}

	public async void StepChartOnTapped()
	{
		await ExecAsync(async delegate
		{
			BindableCollection<StepChartDataModel> bindableCollection = new BindableCollection<StepChartDataModel>();
			if (AllMeasurementResultModelList != null && AllMeasurementResultModelList.Count > 0)
			{
				foreach (Measurement item in from x in AllMeasurementResultModelList
					where x.MeasurementType == "15"
					orderby x.MeasurementAt
					select x)
				{
					bindableCollection.Add(new StepChartDataModel
					{
						MeasurementAt = item.MeasurementAt,
						Step = Convert.ToDecimal(item.MeasurementValue)
					});
				}
			}
			ChartContext chartContext = IoC.Get<ChartContext>();
			chartContext.Initialize();
			chartContext.StepChartData = bindableCollection;
			await Navigate<StepChartViewModel>();
		});
	}

	public async void CompChartOnTapped()
	{
		await ExecAsync(async delegate
		{
			BindableCollection<CompositionMeterChartDataModel> bindableCollection = new BindableCollection<CompositionMeterChartDataModel>();
			if (AllMeasurementResultModelList != null && AllMeasurementResultModelList.Count > 0)
			{
				foreach (DateTime? measurementAt in from x in AllMeasurementResultModelList
					where x.MeasurementType == "06"
					select x.MeasurementAt into x
					orderby x
					select x)
				{
					Measurement measurement = AllMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementAt == measurementAt && x.MeasurementType == "06");
					Measurement measurement2 = AllMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementAt == measurementAt && x.MeasurementType == "07");
					Measurement measurement3 = AllMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementAt == measurementAt && x.MeasurementType == "08");
					bindableCollection.Add(new CompositionMeterChartDataModel
					{
						MeasurementAt = measurementAt,
						Weight = measurement?.MeasurementValue,
						Bmi = measurement2?.MeasurementValue,
						FatPercentage = measurement3?.MeasurementValue,
						TimezoneDate = measurement.TimezoneDate.ToString()
					});
				}
			}
			ChartContext chartContext = IoC.Get<ChartContext>();
			chartContext.Initialize();
			chartContext.CompositionMeterChartData = bindableCollection;
			await Navigate<CompositionMeterChartViewModel>();
		});
	}

	public async void ScanNBCM()
	{
		await ExecAsync(async delegate
		{
			await Navigate<BodyCompositionMeasurementViewModel>();
		});
	}

	protected override async void OnActivate()
	{
		NoticeList = new BindableCollection<NoticeDetailResponseModel>();
		await ExecAsync(async delegate
		{
			IsShowNotice = false;
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				NoticeListResponseModel noticeListResponseModel = await NoticeService.GetNoticeList(new NoticeListRequestModel
				{
					IgUserId = AuthenticatedUser.IgUser.Id
				});
				if (noticeListResponseModel?.Notices?.Any() == true)
				{
					NoticeList = new BindableCollection<NoticeDetailResponseModel>(noticeListResponseModel.Notices.ToArray());
				}
				foreach (HospitalModel hospital in base.UserManager.Hospitals)
				{
					NoticeDetailResponseModel noticeDetailResponseModel = NoticeList.FirstOrDefault((NoticeDetailResponseModel x) => x.Hospital.Id == hospital.Id && x.Notice.TransitionType == 1);
					if (noticeDetailResponseModel != null)
					{
						string id = Convert.ToBase64String(Encoding.UTF8.GetBytes(noticeDetailResponseModel.Hospital.Id));
						HospitalInfoResponseModel info = await HospitalInfoService.GetHospitalInfo(id);
						if (info != null)
						{
							HospitalShareSettingModel shareItemSettings = info.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 0);
							IgUserHospitalShareItemModel igUserHospitalShareItemModel = info.IgUserHospitalShareItems.FirstOrDefault((IgUserHospitalShareItemModel x) => x.ItemId == shareItemSettings?.Id);
							bool valueOrDefault = shareItemSettings?.SelectItems?.Contains("glucose") == true;
							bool valueOrDefault2 = shareItemSettings?.SelectItems?.Contains("sphygmomanometer") == true;
							bool valueOrDefault3 = shareItemSettings?.SelectItems?.Contains("temperature") == true;
							bool valueOrDefault4 = shareItemSettings?.SelectItems?.Contains("compositionMeter") == true;
							bool valueOrDefault5 = shareItemSettings?.SelectItems?.Contains("stepMeter") == true;
							bool valueOrDefault6 = shareItemSettings?.SelectItems?.Contains("photograph") == true;
							bool valueOrDefault7 = shareItemSettings?.SelectItems?.Contains("comment") == true;
							bool flag = igUserHospitalShareItemModel?.Value.Contains("glucose") ?? true;
							bool flag2 = igUserHospitalShareItemModel?.Value.Contains("sphygmomanometer") ?? true;
							bool flag3 = igUserHospitalShareItemModel?.Value.Contains("temperature") ?? true;
							bool flag4 = igUserHospitalShareItemModel?.Value.Contains("compositionMeter") ?? true;
							bool flag5 = igUserHospitalShareItemModel?.Value.Contains("stepMeter") ?? true;
							bool flag6 = igUserHospitalShareItemModel?.Value.Contains("photograph") ?? true;
							bool flag7 = igUserHospitalShareItemModel?.Value.Contains("comment") ?? true;
							IsShowNotice = (valueOrDefault && !flag) || (valueOrDefault2 && !flag2) || (valueOrDefault3 && !flag3) || (valueOrDefault4 && !flag4) || (valueOrDefault5 && !flag5) || (valueOrDefault6 && !flag6) || (valueOrDefault7 && !flag7) || info.HospitalShareSettings.Any(delegate(HospitalShareSettingModel setting)
							{
								IgUserHospitalShareItemModel igUserHospitalShareItemModel2 = info.IgUserHospitalShareItems.FirstOrDefault((IgUserHospitalShareItemModel x) => x.ItemId == setting?.Id);
								return igUserHospitalShareItemModel2 == null || (setting.RequiredFlg && string.IsNullOrEmpty(igUserHospitalShareItemModel2.Value));
							});
							if (IsShowNotice)
							{
								break;
							}
						}
					}
				}
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				log.Error($"【IG】【HomeViewModel】【OnActivate】例外発生：{ex}");
			}
		});
		MeterContext meterContext = IoC.Get<MeterContext>();
		IsShowScanNBCMButton = meterContext.BCMeter != null && meterContext.BCMeter.Name != MeterContext.NON_REGIST_NAME;
	}

	private async Task<DateTime?> CalcMinTimezone()
	{
		MeasurementResultModel minMeasurement = MeasurementResultModelList.OrderBy((MeasurementResultModel x) => x.TimezoneDate).FirstOrDefault((MeasurementResultModel x) => x.TimezoneDate.HasValue);
		Photograph photograph = (from x in (await PhotographService.GetAllPhotograph()).ToList()
			orderby x.TimezoneDate
			select x).FirstOrDefault((Photograph x) => x.TimezoneDate.HasValue && !x.Deleted && x.Image != null);
		if (minMeasurement == null && photograph == null)
		{
			return null;
		}
		if (minMeasurement == null)
		{
			return photograph.TimezoneDate.Value;
		}
		if (photograph == null)
		{
			return minMeasurement.TimezoneDate.Value;
		}
		if (minMeasurement.TimezoneDate.Value >= photograph.TimezoneDate.Value)
		{
			return photograph.TimezoneDate.Value;
		}
		return minMeasurement.TimezoneDate.Value;
	}

	private BindableCollection<MeasurementResultModel> CreateMeasurementResultList(BindableCollection<Measurement> list)
	{
		MeasurementResultModelList.Clear();
		foreach (Measurement item in list.OrderByDescending((Measurement x) => x.MeasurementAt))
		{
			MeasurementResultModel measurementResultModel = CreateMeasurementResult(list, item);
			if (measurementResultModel != null)
			{
				MeasurementResultModelList.Add(measurementResultModel);
			}
		}
		return MeasurementResultModelList;
	}

	private MeasurementResultModel CreateMeasurementResult(IEnumerable<Measurement> list, Measurement item)
	{
		switch (item.MeasurementType)
		{
		case "01":
		{
			string text3 = item.MeasurementValue.ToString();
			text3 += FormatUtils.ConvertExceedLimitTypeToDisplaySymbol(item.ExceedLimitType);
			Color textColor = FontColorOfGlucoseMeasurementValue(Convert.ToDecimal(item.MeasurementValue), base.UserManager.IgUser);
			return CreateMeasurementResultCore(item, "血糖", "血糖", text3, textColor);
		}
		case "02":
		{
			string displayMeasurementValue6 = item.MeasurementValue?.ToString("F1");
			return CreateMeasurementResultCore(item, "体温", "体温", displayMeasurementValue6, Color.Transparent);
		}
		case "03":
		{
			MeasurementResultModel measurementResultModel = CreateMeasurementResultCore(item, "最高血圧", "血圧", null, Color.Transparent);
			string text = Convert.ToInt32(measurementResultModel.MeasurementValue).ToString();
			Measurement measurement = list.FirstOrDefault((Measurement x) => x.MeasurementAt == item.MeasurementAt && x.MeasurementType == "04");
			string text2 = "0";
			if (measurement != null)
			{
				text2 = Convert.ToInt32(measurement.MeasurementValue).ToString();
			}
			measurementResultModel.DisplayMeasurementValue = text + "/" + text2;
			return measurementResultModel;
		}
		case "05":
		{
			string displayMeasurementValue4 = Convert.ToInt32(item.MeasurementValue).ToString();
			return CreateMeasurementResultCore(item, "脈拍", "脈拍", displayMeasurementValue4, Color.Transparent);
		}
		case "06":
		{
			string displayMeasurementValue3 = item.MeasurementValue?.ToString((item.MeasurementValue >= 100.0) ? "F1" : "F2");
			return CreateMeasurementResultCore(item, "体重", "体重", displayMeasurementValue3, Color.Transparent);
		}
		case "07":
		{
			string displayMeasurementValue5 = item.MeasurementValue?.ToString("F1");
			return CreateMeasurementResultCore(item, "BMI", "BMI", displayMeasurementValue5, Color.Transparent);
		}
		case "08":
		{
			string displayMeasurementValue2 = item.MeasurementValue?.ToString("F1");
			return CreateMeasurementResultCore(item, "体脂肪率", "体脂肪率", displayMeasurementValue2, Color.Transparent);
		}
		case "15":
		{
			string displayMeasurementValue = Convert.ToInt32(item.MeasurementValue).ToString();
			return CreateMeasurementResultCore(item, "歩数", "歩数", displayMeasurementValue, Color.Transparent);
		}
		default:
			return null;
		}
	}

	private MeasurementResultModel CreateMeasurementResultCore(Measurement item, string category, string displayCategory, string displayMeasurementValue, Color textColor)
	{
		MeasurementResultModel measurementResultModel = new MeasurementResultModel
		{
			Source = item,
			RegistDate = item.MeasurementAt.Value,
			TimezoneDate = item.TimezoneDate,
			TimezoneType = item.Timezone,
			MeasurementValue = (decimal)item.MeasurementValue.Value,
			Category = category,
			DisplayCategory = displayCategory,
			DisplayMeasurementValue = displayMeasurementValue,
			TextColor = textColor
		};
		if (item.MeasurementType == "01")
		{
			measurementResultModel.DisplayTimezoneType = ConvertMeasurementTypeGluToName(measurementResultModel.TimezoneType);
		}
		if (item.MeasurementType == "03" || item.MeasurementType == "04" || item.MeasurementType == "05")
		{
			measurementResultModel.DisplayTimezoneType = ConvertMeasurementTypeOtherToName(measurementResultModel.TimezoneType);
		}
		return measurementResultModel;
	}

	private string ConvertMeasurementTypeGluToName(string timezoneType)
	{
		return timezoneType switch
		{
			"01" => "朝食前", 
			"02" => "朝食後", 
			"03" => "昼食前", 
			"04" => "昼食後", 
			"05" => "夕食前", 
			"06" => "夕食後", 
			"07" => "就寝前", 
			"08" => "深夜", 
			_ => string.Empty, 
		};
	}

	private string ConvertMeasurementTypeOtherToName(string timezoneType)
	{
		return timezoneType switch
		{
			"01" => "朝", 
			"02" => "晩", 
			_ => string.Empty, 
		};
	}

	private BindableCollection<MeasurementResultModel> Filter(ICollection<MeasurementResultModel> measurementResultList)
	{
		if (measurementResultList == null || measurementResultList.Count == 0)
		{
			return new BindableCollection<MeasurementResultModel>();
		}
		List<MeasurementResultModel> list = measurementResultList.Where((MeasurementResultModel x) => x.TimezoneDate.HasValue && x.TimezoneDate.Value.ToString("yyyyMMdd") == SelectedDate.ToString("yyyyMMdd")).ToList();
		if (list.Count == 0)
		{
			return new BindableCollection<MeasurementResultModel>();
		}
		BindableCollection<MeasurementResultModel> bindableCollection = new BindableCollection<MeasurementResultModel>();
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "血糖"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "体温"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "血圧"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "脈拍"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "体重"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "BMI"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "体脂肪率"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "歩数"));
		return new BindableCollection<MeasurementResultModel>(bindableCollection.OrderBy((MeasurementResultModel x) => x.RegistDate));
	}

	private static Color FontColorOfGlucoseMeasurementValue(decimal measurementValue, IgUserModel igUser)
	{
		if (igUser.GlucoseHighValue.HasValue)
		{
			decimal? num = igUser.GlucoseHighValue;
			if ((measurementValue >= num.GetValueOrDefault()) & num.HasValue)
			{
				return Color.FromHex("F8BBD0");
			}
		}
		if (igUser.GlucoseLowValue.HasValue)
		{
			decimal? num = igUser.GlucoseLowValue;
			if ((measurementValue <= num.GetValueOrDefault()) & num.HasValue)
			{
				return Color.FromHex("BBDEFB");
			}
		}
		return Color.Transparent;
	}

	void IHandle<HomeSelectPhotographEvent>.Handle(HomeSelectPhotographEvent message)
	{
		Photograph selectedPhotograph = message.SelectedPhotograph;
		if (selectedPhotograph.ShootingAt.HasValue)
		{
			TimeSpan value = new TimeSpan(selectedPhotograph.ShootingAt.Value.Hour, selectedPhotograph.ShootingAt.Value.Minute, selectedPhotograph.ShootingAt.Value.Second);
			base.NavigationService.For<HomeImageTimezoneChangeViewModel>().WithParam((HomeImageTimezoneChangeViewModel x) => x.Photograph, selectedPhotograph).WithParam((HomeImageTimezoneChangeViewModel x) => x.SelectedPhotographRegistDateOfDate, selectedPhotograph.ShootingAt.Value)
				.WithParam((HomeImageTimezoneChangeViewModel x) => x.SelectedPhotographRegistDateOfTime, value)
				.Navigate();
		}
		else
		{
			base.NavigationService.For<HomeImageTimezoneChangeViewModel>().WithParam((HomeImageTimezoneChangeViewModel x) => x.Photograph, selectedPhotograph).Navigate();
		}
	}

	void IHandle<PresentedMenuEvent>.Handle(PresentedMenuEvent message)
	{
		IsPresentedMenu = message.IsPresented;
	}

	void IHandle<HomeMasterDetailRootOnActivateEvent>.Handle(HomeMasterDetailRootOnActivateEvent message)
	{
		OnActivate();
	}

	async void IHandle<HomeRegistDateSaveEvent>.Handle(HomeRegistDateSaveEvent message)
	{
		await RefreshMeasurementResult(SelectedDate);
	}

	async void IHandle<HomeDeletePhotographEvent>.Handle(HomeDeletePhotographEvent message)
	{
		await RefreshMeasurementResult(SelectedDate);
	}

	async void IHandle<HomeUpdateMeasurementEvent>.Handle(HomeUpdateMeasurementEvent message)
	{
		await RefreshMeasurementResult(SelectedDate);
	}
}

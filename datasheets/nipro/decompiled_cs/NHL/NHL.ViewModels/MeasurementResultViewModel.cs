using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Models;
using NHL.Models.Entity;
using NHL.Services;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class MeasurementResultViewModel : ViewModelBase, IHandle<SelectPhotographEvent>, IHandle, IHandle<MasterDetailRootOnActivateEvent>, IHandle<RegistDateSaveEvent>, IHandle<DeletePhotographEvent>, IHandle<UpdateMeasurementEvent>, IHandle<PresentedMenuEvent>, IHandle<GlucoseTargetThresholdChanged>, IHandle<SelectedDateEvent>
{
	private BindableCollection<MeasurementResultModel> measurementResultModelList;

	private BindableCollection<Measurement> allMeasurementResultModelList;

	private BindableCollection<Measurement> gluMeasurementList;

	private BindableCollection<Measurement> tempMeasurementList;

	private BindableCollection<Measurement> pressureMeasurementList;

	private BindableCollection<Measurement> compMeasurementList;

	private BindableCollection<Measurement> stepMeasurementList;

	private MeasurementResultModel selectedMeasurementResult;

	private DateTime selectedDate;

	private int selectedPosition;

	private BindableCollection<CarouselPhotographModel> foodPhotoList;

	private BindableCollection<DailyComments> dailyCommentModelList;

	private DailyComments dailyComment;

	private string displayDailyComment;

	private bool displayDailyCommentFlag;

	private DateTime? minTimezone;

	private bool isPresentedMenu;

	private bool demoPhotoFlag = true;

	public IMeasurementService MeasurementService { get; set; }

	public IPhotographService PhotographService { get; set; }

	public IDailyCommentService DailyCommentService { get; set; }

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
			NotifyOfPropertyChange(() => DisplayMeasurementResultModelList);
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

	public BindableCollection<Measurement> GluMeasurementList
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

	public BindableCollection<Measurement> TempMeasurementList
	{
		get
		{
			return tempMeasurementList;
		}
		set
		{
			tempMeasurementList = value;
			NotifyOfPropertyChange(() => TempMeasurementList);
		}
	}

	public BindableCollection<Measurement> PressureMeasurementList
	{
		get
		{
			return pressureMeasurementList;
		}
		set
		{
			pressureMeasurementList = value;
			NotifyOfPropertyChange(() => PressureMeasurementList);
		}
	}

	public BindableCollection<Measurement> CompMeasurementList
	{
		get
		{
			return compMeasurementList;
		}
		set
		{
			compMeasurementList = value;
			NotifyOfPropertyChange(() => CompMeasurementList);
		}
	}

	public BindableCollection<Measurement> StepMeasurementList
	{
		get
		{
			return stepMeasurementList;
		}
		set
		{
			stepMeasurementList = value;
			NotifyOfPropertyChange(() => StepMeasurementList);
		}
	}

	public BindableCollection<MeasurementResultModel> DisplayMeasurementResultModelList => GetSelectDateMeasurementResultModel();

	public MeasurementResultModel SelectedMeasurementResult
	{
		get
		{
			return selectedMeasurementResult;
		}
		set
		{
			if (selectedMeasurementResult == value)
			{
				return;
			}
			selectedMeasurementResult = value;
			NotifyOfPropertyChange(() => SelectedMeasurementResult);
			if (selectedMeasurementResult == null)
			{
				return;
			}
			switch (selectedMeasurementResult.DisplayCategory)
			{
			case "血糖":
				base.NavigationService.For<ChangeTimeZoneViewModel>().WithParam((ChangeTimeZoneViewModel x) => x.MeasurementResult, selectedMeasurementResult).Navigate();
				break;
			case "血圧":
				base.NavigationService.For<ChangeTimeZoneViewModel>().WithParam((ChangeTimeZoneViewModel x) => x.MeasurementResult, selectedMeasurementResult).Navigate();
				break;
			case "体重":
			{
				BindableCollection<Measurement> bindableCollection = new BindableCollection<Measurement>();
				bindableCollection.AddRange(AllMeasurementResultModelList.Where(delegate(Measurement x)
				{
					DateTime? measurementAt = x.MeasurementAt;
					DateTime registDate = selectedMeasurementResult.RegistDate;
					if (!measurementAt.HasValue)
					{
						return false;
					}
					return !measurementAt.HasValue || measurementAt.GetValueOrDefault() == registDate;
				}));
				base.NavigationService.For<CompositionMeterDetailViewModel>().WithParam((CompositionMeterDetailViewModel x) => x.CompMeasurementResultModelList, bindableCollection).Navigate();
				break;
			}
			case "歩数":
				base.NavigationService.For<RecordEditViewModel>().WithParam((RecordEditViewModel x) => x.AllMeasurementList, AllMeasurementResultModelList).WithParam((RecordEditViewModel x) => x.SelectedMeasurementAt, selectedMeasurementResult.RegistDate)
					.WithParam((RecordEditViewModel x) => x.IsEnableMeasurementAt, value: false)
					.Navigate();
				break;
			}
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
			if (selectedDate != value)
			{
				selectedDate = value;
				NotifyOfPropertyChange(() => SelectedDate);
			}
			NotifyOfPropertyChange(() => IsVisibleSelectTomorrow);
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
				value = value?.Replace("\r", "\u3000").Replace("\n", "\u3000");
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
			if (minTimezone != value)
			{
				minTimezone = value;
				NotifyOfPropertyChange(() => MinTimezone);
			}
			NotifyOfPropertyChange(() => IsVisibleSelectTomorrow);
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

	public bool DemoPhotoFlag
	{
		get
		{
			return demoPhotoFlag;
		}
		set
		{
			demoPhotoFlag = value;
			NotifyOfPropertyChange(() => DemoPhotoFlag);
		}
	}

	public bool IsVisibleSelectTomorrow => (SelectedDate.Date - DateTime.Today).Days < 0;

	public void Initialize()
	{
		base.EventAggregator.Unsubscribe(this);
		base.EventAggregator.Subscribe(this);
		MeasurementResultModelList = new BindableCollection<MeasurementResultModel>();
		FoodPhotoList = new BindableCollection<CarouselPhotographModel>();
		DailyCommentModelList = new BindableCollection<DailyComments>();
		SelectedMeasurementResult = null;
	}

	public async void SelectYesterday()
	{
		SelectedDate = SelectedDate.AddDays(-1.0);
		await ExecAsync(async delegate
		{
			await RefreshFoodPhotoList();
			await RefreshDailyCommentList();
			NotifyOfPropertyChange(() => DisplayMeasurementResultModelList);
		});
	}

	public async void SelectToday()
	{
		SelectedDate = DateTime.Now;
		await ExecAsync(async delegate
		{
			await RefreshFoodPhotoList();
			await RefreshDailyCommentList();
			NotifyOfPropertyChange(() => DisplayMeasurementResultModelList);
		});
	}

	public async void SelectTomorrow()
	{
		SelectedDate = SelectedDate.AddDays(1.0);
		await ExecAsync(async delegate
		{
			await RefreshFoodPhotoList();
			await RefreshDailyCommentList();
			NotifyOfPropertyChange(() => DisplayMeasurementResultModelList);
		});
	}

	public async void SelectDate(DateTime date)
	{
		SelectedDate = date;
		await ExecAsync(async delegate
		{
			await RefreshFoodPhotoList();
			NotifyOfPropertyChange(() => DisplayMeasurementResultModelList);
		});
	}

	public async Task RefreshMeasurementResult()
	{
		await RefreshMeasurementResult(DateTime.Now);
	}

	public async Task RefreshMeasurementResult(DateTime date, bool isRefreshFoodPhotoList = true, bool isRefreshDailyCommentList = true)
	{
		await ExecAsync(async delegate
		{
			MeasurementResultModelList.Clear();
			SelectedDate = date;
			AllMeasurementResultModelList = new BindableCollection<Measurement>();
			BindableCollection<Measurement> bindableCollection = await MeasurementService.GetAllMeasurement();
			if (bindableCollection != null && bindableCollection.Count > 0)
			{
				AllMeasurementResultModelList = bindableCollection;
				CreateMeasurementVisibleList();
				CreateMeasurementResultList(bindableCollection);
			}
			NotifyOfPropertyChange(() => DisplayMeasurementResultModelList);
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
			FoodPhotoList.First().LeftPhoto.Refresh();
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

	public async void Calendar()
	{
		await ExecAsync(async delegate
		{
			MeasurementContext measurementContext = IoC.Get<MeasurementContext>();
			measurementContext.Initialize();
			measurementContext.GluMeasurementList = GluMeasurementList;
			measurementContext.TempMeasurementList = TempMeasurementList;
			measurementContext.PressureMeasurementList = PressureMeasurementList;
			measurementContext.CompMeasurementList = CompMeasurementList;
			measurementContext.StepMeasurementList = StepMeasurementList;
			measurementContext.CalendarDisplayDate = SelectedDate;
			await Navigate<MeasurementCalendarViewModel>();
		});
	}

	protected override void OnInitialize()
	{
		base.OnInitialize();
	}

	protected override void OnActivate()
	{
		SelectedMeasurementResult = null;
	}

	public void CreateMeasurementVisibleList()
	{
		GluMeasurementList = new BindableCollection<Measurement>();
		TempMeasurementList = new BindableCollection<Measurement>();
		PressureMeasurementList = new BindableCollection<Measurement>();
		CompMeasurementList = new BindableCollection<Measurement>();
		StepMeasurementList = new BindableCollection<Measurement>();
		GluMeasurementList.AddRange(AllMeasurementResultModelList.Where((Measurement x) => x.MeasurementType == "01"));
		TempMeasurementList.AddRange(AllMeasurementResultModelList.Where((Measurement x) => x.MeasurementType == "02"));
		PressureMeasurementList.AddRange(AllMeasurementResultModelList.Where((Measurement x) => x.MeasurementType == "04" || x.MeasurementType == "03" || x.MeasurementType == "05"));
		CompMeasurementList.AddRange(AllMeasurementResultModelList.Where((Measurement x) => x.MeasurementType == "06" || x.MeasurementType == "08"));
		StepMeasurementList.AddRange(AllMeasurementResultModelList.Where((Measurement x) => x.MeasurementType == "15"));
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

	private void CreateMeasurementResultList(BindableCollection<Measurement> list)
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
	}

	private MeasurementResultModel CreateMeasurementResult(BindableCollection<Measurement> list, Measurement item)
	{
		switch (item.MeasurementType)
		{
		case "01":
		{
			string text5 = item.MeasurementValue.ToString();
			text5 += FormatUtils.ConvertExceedLimitTypeToDisplaySymbol(item.ExceedLimitType);
			Color textColor = FontColorOfGlucoseMeasurementValue(Convert.ToDecimal(item.MeasurementValue), base.UserManager.IgUser);
			return CreateMeasurementResultCore(item, "血糖", "血糖", text5, textColor);
		}
		case "02":
		{
			string text6 = item.MeasurementValue?.ToString("F1");
			return CreateMeasurementResultCore(item, "体温", "体温", text6 + "℃", Color.Transparent);
		}
		case "03":
		{
			MeasurementResultModel measurementResultModel = CreateMeasurementResultCore(item, "最高血圧", "血圧", null, Color.Transparent);
			string text2 = measurementResultModel.MeasurementValue.ToString();
			Measurement measurement = list.FirstOrDefault((Measurement x) => x.MeasurementAt == item.MeasurementAt && x.MeasurementType == "04");
			string text3 = "0";
			if (measurement != null)
			{
				text3 = ((int)measurement.MeasurementValue.Value).ToString();
			}
			Measurement measurement2 = list.FirstOrDefault((Measurement x) => x.MeasurementAt == item.MeasurementAt && x.MeasurementType == "05");
			string text4 = "0";
			if (measurement2 != null)
			{
				text4 = ((int)measurement2.MeasurementValue.Value).ToString();
			}
			measurementResultModel.DisplayMeasurementValue = text2 + "/" + text3 + " (脈拍 " + text4 + ")";
			return measurementResultModel;
		}
		case "06":
		{
			MeasurementResultModel measurementResultModel2 = CreateMeasurementResultCore(item, "体重", "体重", null, Color.Transparent);
			string text7 = measurementResultModel2.MeasurementValue.ToString((measurementResultModel2.MeasurementValue >= 100m) ? "F1" : "F2");
			text7 += "kg";
			measurementResultModel2.DisplayMeasurementValue = text7 ?? "";
			return measurementResultModel2;
		}
		case "15":
		{
			string text = item.MeasurementValue?.ToString("#,##0");
			return CreateMeasurementResultCore(item, "歩数", "歩数", text + "歩", Color.Transparent);
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
			ThresholdExclusion = false,
			PhysicianConfirmed = false,
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
			null => "未", 
			_ => string.Empty, 
		};
	}

	private BindableCollection<MeasurementResultModel> GetSelectDateMeasurementResultModel()
	{
		BindableCollection<MeasurementResultModel> bindableCollection = Filter();
		if (bindableCollection.Where((MeasurementResultModel x) => x.DisplayCategory == "歩数").ToList().Count != 0)
		{
			return bindableCollection;
		}
		bindableCollection.Add(new MeasurementResultModel
		{
			Source = new Measurement
			{
				IgUserId = base.UserManager.IgUser.Id,
				MeasurementType = "15",
				MeasurementAt = new DateTime(selectedDate.Year, selectedDate.Month, selectedDate.Day, 0, 0, 0),
				TimezoneDate = selectedDate,
				ExceedLimitType = "0"
			},
			Category = "歩数",
			DisplayCategory = "歩数",
			DisplayMeasurementValue = "未入力",
			RegistDate = new DateTime(selectedDate.Year, selectedDate.Month, selectedDate.Day, 0, 0, 0)
		});
		return new BindableCollection<MeasurementResultModel>(bindableCollection.OrderBy((MeasurementResultModel x) => x.RegistDate));
	}

	private BindableCollection<MeasurementResultModel> Filter()
	{
		if (MeasurementResultModelList == null || MeasurementResultModelList.Count == 0)
		{
			return new BindableCollection<MeasurementResultModel>();
		}
		List<MeasurementResultModel> list = MeasurementResultModelList.Where((MeasurementResultModel x) => x.TimezoneDate.HasValue && x.TimezoneDate.Value.ToString("yyyyMMdd") == SelectedDate.ToString("yyyyMMdd")).ToList();
		if (list.Count == 0)
		{
			return new BindableCollection<MeasurementResultModel>();
		}
		BindableCollection<MeasurementResultModel> bindableCollection = new BindableCollection<MeasurementResultModel>();
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "血糖"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "体温"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "血圧" || x.DisplayCategory == "脈拍"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "体重" || x.DisplayCategory == "体脂肪率"));
		bindableCollection.AddRange(list.Where((MeasurementResultModel x) => x.DisplayCategory == "歩数"));
		return new BindableCollection<MeasurementResultModel>(bindableCollection.OrderBy((MeasurementResultModel x) => x.RegistDate));
	}

	private Color FontColorOfGlucoseMeasurementValue(decimal measurementValue, IgUserModel igUser)
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

	void IHandle<SelectPhotographEvent>.Handle(SelectPhotographEvent message)
	{
		Photograph selectedPhotograph = message.SelectedPhotograph;
		if (selectedPhotograph.ShootingAt.HasValue)
		{
			TimeSpan value = new TimeSpan(selectedPhotograph.ShootingAt.Value.Hour, selectedPhotograph.ShootingAt.Value.Minute, selectedPhotograph.ShootingAt.Value.Second);
			base.NavigationService.For<ImageTimezoneChangeViewModel>().WithParam((ImageTimezoneChangeViewModel x) => x.Photograph, selectedPhotograph).WithParam((ImageTimezoneChangeViewModel x) => x.SelectedPhotographRegistDateOfDate, selectedPhotograph.ShootingAt.Value)
				.WithParam((ImageTimezoneChangeViewModel x) => x.SelectedPhotographRegistDateOfTime, value)
				.Navigate();
		}
		else
		{
			base.NavigationService.For<ImageTimezoneChangeViewModel>().WithParam((ImageTimezoneChangeViewModel x) => x.Photograph, selectedPhotograph).Navigate();
		}
	}

	void IHandle<MasterDetailRootOnActivateEvent>.Handle(MasterDetailRootOnActivateEvent message)
	{
		OnActivate();
	}

	async void IHandle<RegistDateSaveEvent>.Handle(RegistDateSaveEvent message)
	{
		await RefreshMeasurementResult(SelectedDate);
	}

	async void IHandle<DeletePhotographEvent>.Handle(DeletePhotographEvent message)
	{
		await RefreshMeasurementResult(SelectedDate);
	}

	async void IHandle<UpdateMeasurementEvent>.Handle(UpdateMeasurementEvent message)
	{
		await RefreshMeasurementResult(SelectedDate);
	}

	async void IHandle<SelectedDateEvent>.Handle(SelectedDateEvent message)
	{
		await RefreshMeasurementResult(message.SelectDate);
	}

	void IHandle<PresentedMenuEvent>.Handle(PresentedMenuEvent message)
	{
		IsPresentedMenu = message.IsPresented;
	}

	public void Handle(GlucoseTargetThresholdChanged message)
	{
		foreach (MeasurementResultModel measurementResultModel in MeasurementResultModelList)
		{
			if (measurementResultModel.DisplayCategory == "血糖")
			{
				measurementResultModel.TextColor = FontColorOfGlucoseMeasurementValue(measurementResultModel.MeasurementValue, base.UserManager.IgUser);
			}
		}
	}
}

using System;
using System.Collections.Generic;
using System.Linq;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Models;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Support;
using NHL.Views;

namespace NHL.ViewModels;

public class GlucoseChartViewModel : ViewModelBase
{
	private BindableCollection<GlucoseChartDataModel> glucoseChartData;

	private BindableCollection<GlucoseChartDataModel> selectedGlucoseChartData;

	private BindableCollection<DisplayGluChartDataModel> dispGluChartData;

	private BindableCollection<GlucoseChartDataModel> beforeGluChartAverageData;

	private BindableCollection<GlucoseChartDataModel> afterGluChartAverageData;

	private BindableCollection<ChartSettingModel> chartSettings;

	private List<DateTime> selectedDates;

	private string displaySelectedDates;

	private string beforeMealsAverage;

	private string afterMealsAverage;

	private decimal displayChartMax;

	public BindableCollection<GlucoseChartDataModel> GlucoseChartData
	{
		get
		{
			return glucoseChartData;
		}
		set
		{
			if (glucoseChartData != value)
			{
				glucoseChartData = value;
				NotifyOfPropertyChange(() => GlucoseChartData);
			}
		}
	}

	public BindableCollection<GlucoseChartDataModel> SelectedGlucoseChartData
	{
		get
		{
			return selectedGlucoseChartData;
		}
		set
		{
			if (selectedGlucoseChartData != value)
			{
				selectedGlucoseChartData = value;
				NotifyOfPropertyChange(() => SelectedGlucoseChartData);
			}
		}
	}

	public BindableCollection<DisplayGluChartDataModel> DispGluChartData
	{
		get
		{
			return dispGluChartData;
		}
		set
		{
			if (dispGluChartData != value)
			{
				dispGluChartData = value;
				NotifyOfPropertyChange(() => DispGluChartData);
			}
		}
	}

	public BindableCollection<GlucoseChartDataModel> BeforeGluChartAverageData
	{
		get
		{
			return beforeGluChartAverageData;
		}
		set
		{
			if (beforeGluChartAverageData != value)
			{
				beforeGluChartAverageData = value;
				NotifyOfPropertyChange(() => BeforeGluChartAverageData);
			}
		}
	}

	public BindableCollection<GlucoseChartDataModel> AfterGluChartAverageData
	{
		get
		{
			return afterGluChartAverageData;
		}
		set
		{
			if (afterGluChartAverageData != value)
			{
				afterGluChartAverageData = value;
				NotifyOfPropertyChange(() => AfterGluChartAverageData);
			}
		}
	}

	public BindableCollection<ChartSettingModel> ChartSettings
	{
		get
		{
			return chartSettings;
		}
		set
		{
			if (chartSettings != value)
			{
				chartSettings = value;
				NotifyOfPropertyChange(() => ChartSettings);
			}
		}
	}

	public List<DateTime> SelectedDates
	{
		get
		{
			return selectedDates;
		}
		set
		{
			if (selectedDates != value)
			{
				selectedDates = value;
				NotifyOfPropertyChange(() => SelectedDates);
			}
		}
	}

	public string DisplaySelectedDates
	{
		get
		{
			return displaySelectedDates;
		}
		set
		{
			if (displaySelectedDates != value)
			{
				displaySelectedDates = value;
				NotifyOfPropertyChange(() => DisplaySelectedDates);
			}
		}
	}

	public string BeforeMealsAverage
	{
		get
		{
			return beforeMealsAverage;
		}
		set
		{
			if (beforeMealsAverage != value)
			{
				beforeMealsAverage = value;
				NotifyOfPropertyChange(() => BeforeMealsAverage);
			}
		}
	}

	public string AfterMealsAverage
	{
		get
		{
			return afterMealsAverage;
		}
		set
		{
			if (afterMealsAverage != value)
			{
				afterMealsAverage = value;
				NotifyOfPropertyChange(() => AfterMealsAverage);
			}
		}
	}

	public decimal DisplayChartMax
	{
		get
		{
			return displayChartMax;
		}
		set
		{
			if (displayChartMax != value)
			{
				displayChartMax = value;
				NotifyOfPropertyChange(() => DisplayChartMax);
			}
		}
	}

	protected override void OnActivate()
	{
		base.OnActivate();
		ChartContext chartContext = IoC.Get<ChartContext>();
		SelectedDates = chartContext.SelectedDates;
		GlucoseChartData = chartContext.GlucoseChartData;
		ChartSettings = chartContext.ChartSettings;
		if (SelectedDates == null || SelectedDates.Count < 1)
		{
			SelectedDates = new List<DateTime>();
			DateTime now = DateTime.Now;
			DateTime dateTime = now.AddDays(-7.0);
			while (dateTime <= now)
			{
				SelectedDates.Add(dateTime);
				dateTime = dateTime.AddDays(1.0);
			}
		}
		CreateGluChartData();
		GlucoseChartView obj = GetView() as GlucoseChartView;
		obj.ChartView(DispGluChartData, DisplayChartMax);
		DisplaySelectedDates = selectedDates.First().ToString("yyyy/M/d ～") + selectedDates.Last().ToString(" yyyy/M/d");
		CreateGluChartAverageData();
		if (ChartSettings == null)
		{
			ChartSettings = new BindableCollection<ChartSettingModel>();
		}
		obj.ChartVisible(ChartSettings);
	}

	public void CreateGluChartData()
	{
		DispGluChartData = new BindableCollection<DisplayGluChartDataModel>();
		DisplayChartMax = 100m;
		DispGluChartData.Add(new DisplayGluChartDataModel());
		foreach (DateTime selectedDate in SelectedDates)
		{
			DisplayGluChartDataModel displayGluChartDataModel = new DisplayGluChartDataModel();
			displayGluChartDataModel.Date = selectedDate.Date.ToString("M/d");
			if (GlucoseChartData != null)
			{
				foreach (GlucoseChartDataModel glucoseChartDatum in GlucoseChartData)
				{
					if (glucoseChartDatum.TimezoneDate == selectedDate.Date.ToString())
					{
						if (glucoseChartDatum.MeasurementValue > DisplayChartMax)
						{
							DisplayChartMax = glucoseChartDatum.MeasurementValue + 30m;
						}
						switch (glucoseChartDatum.TimezoneType)
						{
						case "01":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.BeforeBreakFast = glucoseChartDatum.MeasurementValue;
							break;
						case "02":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.AfterBreakFast = glucoseChartDatum.MeasurementValue;
							break;
						case "03":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.BeforeLunch = glucoseChartDatum.MeasurementValue;
							break;
						case "04":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.AfterLunch = glucoseChartDatum.MeasurementValue;
							break;
						case "05":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.BeforeDinner = glucoseChartDatum.MeasurementValue;
							break;
						case "06":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.AfterDinner = glucoseChartDatum.MeasurementValue;
							break;
						case "07":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.BedTime = glucoseChartDatum.MeasurementValue;
							break;
						case "08":
							displayGluChartDataModel.Date = DateTime.Parse(glucoseChartDatum.TimezoneDate).ToString("M/d");
							displayGluChartDataModel.Night = glucoseChartDatum.MeasurementValue;
							break;
						}
					}
				}
			}
			if (displayGluChartDataModel != null && displayGluChartDataModel.Date != null)
			{
				DispGluChartData.Add(displayGluChartDataModel);
			}
		}
	}

	public void CreateGluChartAverageData()
	{
		BeforeGluChartAverageData = new BindableCollection<GlucoseChartDataModel>();
		AfterGluChartAverageData = new BindableCollection<GlucoseChartDataModel>();
		SelectedGlucoseChartData = new BindableCollection<GlucoseChartDataModel>();
		foreach (DateTime selectedDate in SelectedDates)
		{
			if (GlucoseChartData == null || SelectedDates == null)
			{
				continue;
			}
			foreach (GlucoseChartDataModel glucoseChartDatum in GlucoseChartData)
			{
				if (glucoseChartDatum.TimezoneDate == selectedDate.Date.ToString())
				{
					SelectedGlucoseChartData.Add(glucoseChartDatum);
				}
			}
		}
		if (SelectedGlucoseChartData != null && SelectedGlucoseChartData.Count > 0)
		{
			BeforeGluChartAverageData.AddRange(SelectedGlucoseChartData.Where((GlucoseChartDataModel x) => x.TimezoneType == "01" || x.TimezoneType == "03" || x.TimezoneType == "05" || x.TimezoneType == "07"));
			AfterGluChartAverageData.AddRange(SelectedGlucoseChartData.Where((GlucoseChartDataModel x) => x.TimezoneType == "02" || x.TimezoneType == "04" || x.TimezoneType == "06"));
		}
		if (BeforeGluChartAverageData != null && BeforeGluChartAverageData.Count > 0)
		{
			BeforeMealsAverage = BeforeGluChartAverageData.Average((GlucoseChartDataModel x) => x.MeasurementValue).ToString("F1");
		}
		else
		{
			BeforeMealsAverage = "--";
		}
		if (AfterGluChartAverageData != null && AfterGluChartAverageData.Count > 0)
		{
			AfterMealsAverage = AfterGluChartAverageData.Average((GlucoseChartDataModel x) => x.MeasurementValue).ToString("F1");
		}
		else
		{
			AfterMealsAverage = "--";
		}
	}

	private void SetChartContext()
	{
		ChartContext chartContext = IoC.Get<ChartContext>();
		chartContext.GlucoseChartData = GlucoseChartData;
		chartContext.ChartSettings = ChartSettings;
		chartContext.SelectedDates = SelectedDates;
	}

	public async void OnCalendarTapped()
	{
		await ExecAsync(async delegate
		{
			await Navigate<ChartCalendarViewModel>();
		});
	}

	public void OnGluSettingTapped()
	{
		base.NavigationService.For<ChartSettingViewModel>().WithParam((ChartSettingViewModel x) => x.ChartSettings, ChartSettings).Navigate();
	}

	public void OnGluDataTapped()
	{
		base.NavigationService.For<GlucoseDataViewModel>().WithParam((GlucoseDataViewModel x) => x.SelectedGlucoseChartData, SelectedGlucoseChartData).Navigate();
	}

	public void Initialize()
	{
		base.OnInitialize();
	}

	protected override void OnDeactivate(bool close)
	{
		SetChartContext();
		base.OnDeactivate(close);
	}
}

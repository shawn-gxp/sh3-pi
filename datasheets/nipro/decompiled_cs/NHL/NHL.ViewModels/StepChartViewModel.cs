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

public class StepChartViewModel : ViewModelBase
{
	private BindableCollection<StepChartDataModel> _stepChartData;

	private BindableCollection<StepChartDataModel> _selectedStepChartData;

	private BindableCollection<DisplayStepChartDataModel> _displayStepChartData;

	private BindableCollection<ChartSettingModel> _chartSettings;

	private List<DateTime> _selectedDates;

	private string _displaySelectedDates;

	private string _stepAverage;

	private string _stepTotal;

	private string _viewModeType;

	private const string VIEW_MODE_TYPE_WEEK = "01";

	private const string VIEW_MODE_TYPE_MONTH = "02";

	private double DisplayChartMax { get; set; }

	public bool IsVisibleSelectNext => ChangeAbleToNextTerm();

	public string DisplaySelectedDates
	{
		get
		{
			return _displaySelectedDates;
		}
		set
		{
			_displaySelectedDates = value;
			NotifyOfPropertyChange(() => DisplaySelectedDates);
			NotifyOfPropertyChange(() => IsVisibleSelectNext);
		}
	}

	public BindableCollection<StepChartDataModel> SelectedStepChartData
	{
		get
		{
			return _selectedStepChartData;
		}
		set
		{
			if (_selectedStepChartData != value)
			{
				_selectedStepChartData = value;
				NotifyOfPropertyChange(() => SelectedStepChartData);
			}
		}
	}

	public string StepAverage
	{
		get
		{
			if (int.TryParse(_stepAverage, out var result))
			{
				return $"{result:#,0}";
			}
			return _stepAverage;
		}
		set
		{
			_stepAverage = value;
			NotifyOfPropertyChange(() => StepAverage);
		}
	}

	public string StepTotal
	{
		get
		{
			if (int.TryParse(_stepTotal, out var result))
			{
				return $"{result:#,0}";
			}
			return _stepTotal;
		}
		set
		{
			_stepTotal = value;
			NotifyOfPropertyChange(() => StepTotal);
		}
	}

	protected override void OnActivate()
	{
		base.OnActivate();
		ChartContext chartContext = IoC.Get<ChartContext>();
		_selectedDates = chartContext.SelectedDates;
		_stepChartData = chartContext.StepChartData;
		_chartSettings = chartContext.ChartSettings;
		if (_selectedDates == null || _selectedDates.Count < 1)
		{
			_selectedDates = new List<DateTime>();
			DateTime dateTime = DateTime.Now.AddDays(0 - DateTime.Now.DayOfWeek);
			DateTime dateTime2 = dateTime.AddDays(6.0);
			for (int i = 0; i <= (dateTime2 - dateTime).Days; i++)
			{
				_selectedDates.Add(dateTime.AddDays(i));
			}
			_viewModeType = "01";
		}
		ChangeChartTerm(_selectedDates.First(), _selectedDates.Last());
	}

	private void ChangeChartTerm(DateTime startDay, DateTime endDay)
	{
		_selectedDates.Clear();
		int majorUnit = 1;
		for (int i = 0; i <= (endDay - startDay).Days; i++)
		{
			_selectedDates.Add(startDay.AddDays(i));
		}
		if (GetView() is StepChartView stepChartView)
		{
			if (_viewModeType == "01")
			{
				DisplaySelectedDates = _selectedDates.First().ToString("M/d ～") + _selectedDates.Last().ToString("M/d");
				majorUnit = 1;
			}
			else if (_viewModeType == "02")
			{
				DisplaySelectedDates = _selectedDates.First().ToString("yyyy/MM");
				majorUnit = 7;
			}
			CreateStepChartData();
			stepChartView.ChartView(_displayStepChartData, DisplayChartMax, majorUnit);
			CreateStepChartAverageData();
			CreateStepChartTotal();
			_chartSettings = stepChartView.ChartVisible(_chartSettings);
		}
	}

	private void CreateStepChartData()
	{
		DisplayChartMax = 2200.0;
		_displayStepChartData = new BindableCollection<DisplayStepChartDataModel>();
		_selectedStepChartData = new BindableCollection<StepChartDataModel>();
		if (_stepChartData == null)
		{
			return;
		}
		foreach (DateTime selectedDate in _selectedDates)
		{
			BindableCollection<DisplayStepChartDataModel> bindableCollection = new BindableCollection<DisplayStepChartDataModel>();
			string date = selectedDate.Date.ToString("M/d");
			foreach (StepChartDataModel stepChartDatum in _stepChartData)
			{
				if (!(stepChartDatum.MeasurementAt?.Date != selectedDate.Date))
				{
					_selectedStepChartData.Add(stepChartDatum);
					double? num = (double?)stepChartDatum.Step;
					if (num > DisplayChartMax)
					{
						DisplayChartMax = num.Value + 1000.0;
					}
					bindableCollection.Add(new DisplayStepChartDataModel
					{
						Date = date,
						Step = stepChartDatum.Step
					});
				}
			}
			if (bindableCollection.Count == 0)
			{
				_displayStepChartData.Add(new DisplayStepChartDataModel
				{
					Date = date,
					Step = default(decimal)
				});
			}
			else
			{
				_displayStepChartData.AddRange(bindableCollection);
			}
		}
	}

	private void CreateStepChartAverageData()
	{
		StepAverage = "--";
		List<decimal?> list = (from x in _selectedStepChartData
			where x.Step.HasValue
			select x.Step).ToList();
		if (list.Count > 0)
		{
			StepAverage = list.Average((decimal? x) => x)?.ToString("F0");
		}
	}

	private void CreateStepChartTotal()
	{
		StepTotal = "--";
		List<decimal?> list = (from x in _selectedStepChartData
			where x.Step.HasValue
			select x.Step).ToList();
		if (list.Count > 0)
		{
			StepTotal = list.Sum((decimal? x) => x)?.ToString("F0");
		}
	}

	private void SetChartContext()
	{
		ChartContext chartContext = IoC.Get<ChartContext>();
		chartContext.StepChartData = _stepChartData;
		chartContext.ChartSettings = _chartSettings;
		chartContext.SelectedDates = _selectedDates;
	}

	public async void OnCalendarTapped()
	{
		await ExecAsync(async delegate
		{
			await Navigate<ChartCalendarViewModel>();
		});
	}

	public void WeekButtonTapped()
	{
		if (!(_viewModeType == "01"))
		{
			_viewModeType = "01";
			DateTime dateTime = new DateTime(_selectedDates.First().Year, _selectedDates.First().Month, 1, 0, 0, 0);
			DateTime startDay = dateTime.AddDays(0 - dateTime.DayOfWeek);
			DateTime endDay = startDay.AddDays(6.0);
			ChangeChartTerm(startDay, endDay);
		}
	}

	public void MonthButtonTapped()
	{
		if (!(_viewModeType == "02"))
		{
			_viewModeType = "02";
			DateTime dateTime = _selectedDates.Last();
			DateTime startDay = new DateTime(dateTime.Year, dateTime.Month, 1);
			DateTime endDay = new DateTime(dateTime.Year, dateTime.Month, DateTime.DaysInMonth(dateTime.Year, dateTime.Month));
			ChangeChartTerm(startDay, endDay);
		}
	}

	public void SelectPrevTermTapped()
	{
		DateTime startDay = DateTime.Now;
		DateTime endDay = DateTime.Now;
		if (_viewModeType == "01")
		{
			startDay = _selectedDates.First().AddDays(-7.0);
			endDay = startDay.AddDays(6.0);
		}
		else if (_viewModeType == "02")
		{
			DateTime dateTime = _selectedDates.First().AddMonths(-1);
			startDay = new DateTime(dateTime.Year, dateTime.Month, 1);
			endDay = new DateTime(dateTime.Year, dateTime.Month, DateTime.DaysInMonth(dateTime.Year, dateTime.Month));
		}
		ChangeChartTerm(startDay, endDay);
	}

	private bool ChangeAbleToNextTerm()
	{
		DateTime dateTime = DateTime.Now;
		if (_viewModeType == "01")
		{
			dateTime = _selectedDates.First().AddDays(7.0);
		}
		else if (_viewModeType == "02")
		{
			DateTime dateTime2 = _selectedDates.First().AddMonths(1);
			dateTime = new DateTime(dateTime2.Year, dateTime2.Month, 1);
		}
		return dateTime <= DateTime.Now;
	}

	public void SelectNextTermTapped()
	{
		DateTime startDay = DateTime.Now;
		DateTime endDay = DateTime.Now;
		if (_viewModeType == "01")
		{
			startDay = _selectedDates.First().AddDays(7.0);
			endDay = startDay.AddDays(6.0);
		}
		else if (_viewModeType == "02")
		{
			DateTime dateTime = _selectedDates.First().AddMonths(1);
			startDay = new DateTime(dateTime.Year, dateTime.Month, 1);
			endDay = new DateTime(dateTime.Year, dateTime.Month, DateTime.DaysInMonth(dateTime.Year, dateTime.Month));
		}
		ChangeChartTerm(startDay, endDay);
	}

	public void OnSteDataTapped()
	{
		base.NavigationService.For<StepDataViewModel>().WithParam((StepDataViewModel x) => x.SelectedStepChartData, SelectedStepChartData).Navigate();
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

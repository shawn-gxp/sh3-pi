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

public class CompositionMeterChartViewModel : ViewModelBase
{
	private BindableCollection<CompositionMeterChartDataModel> _compositionMeterChartData;

	private BindableCollection<CompositionMeterChartDataModel> _selectedCompositionMeterChartData;

	private BindableCollection<DisplayCompChartDataModel> _displayCompChartData;

	private BindableCollection<ChartSettingModel> _chartSettings;

	private List<DateTime> _selectedDates;

	private string _displaySelectedDates;

	private List<double> _valueList;

	private string _weightAverage;

	private string _bmiAverage;

	private string _fatPercentageAverage;

	private double _displayChartMin;

	private double _displayChartMax;

	public BindableCollection<DisplayCompChartDataModel> DisplayCompChartData
	{
		get
		{
			return _displayCompChartData;
		}
		set
		{
			_displayCompChartData = value;
			NotifyOfPropertyChange(() => DisplayCompChartData);
		}
	}

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
		}
	}

	public string WeightAverage
	{
		get
		{
			return _weightAverage;
		}
		set
		{
			_weightAverage = value;
			NotifyOfPropertyChange(() => WeightAverage);
		}
	}

	public string BmiAverage
	{
		get
		{
			return _bmiAverage;
		}
		set
		{
			_bmiAverage = value;
			NotifyOfPropertyChange(() => BmiAverage);
		}
	}

	public string FatPercentageAverage
	{
		get
		{
			return _fatPercentageAverage;
		}
		set
		{
			_fatPercentageAverage = value;
			NotifyOfPropertyChange(() => FatPercentageAverage);
		}
	}

	protected override void OnActivate()
	{
		base.OnActivate();
		ChartContext chartContext = IoC.Get<ChartContext>();
		_selectedDates = chartContext.SelectedDates;
		_compositionMeterChartData = chartContext.CompositionMeterChartData;
		_chartSettings = chartContext.ChartSettings;
		if (_selectedDates == null || _selectedDates.Count < 1)
		{
			_selectedDates = new List<DateTime>();
			DateTime now = DateTime.Now;
			DateTime dateTime = now.AddDays(-7.0);
			while (dateTime <= now)
			{
				_selectedDates.Add(dateTime);
				dateTime = dateTime.AddDays(1.0);
			}
		}
		if (_chartSettings == null || _chartSettings.Count < 1)
		{
			_chartSettings = new BindableCollection<ChartSettingModel>();
			string[] array = new string[3] { "体重", "BMI", "体脂肪率" };
			foreach (string setting in array)
			{
				ChartSettingModel item = new ChartSettingModel
				{
					Setting = setting,
					Active = true
				};
				_chartSettings.Add(item);
			}
		}
		_valueList = new List<double>();
		foreach (CompositionMeterChartDataModel item2 in from _003C_003Eh__TransparentIdentifier0 in (from selectedDate in _selectedDates
				from compData in _compositionMeterChartData
				select new { selectedDate, compData }).Where(_003C_003Eh__TransparentIdentifier0 =>
			{
				DateTime? dateTime2 = _003C_003Eh__TransparentIdentifier0.compData.MeasurementAt?.Date;
				DateTime date = _003C_003Eh__TransparentIdentifier0.selectedDate.Date;
				if (!dateTime2.HasValue)
				{
					return false;
				}
				return !dateTime2.HasValue || dateTime2.GetValueOrDefault() == date;
			})
			select _003C_003Eh__TransparentIdentifier0.compData)
		{
			_valueList.AddRange(GetCompositionMeterValueList(item2));
		}
		if (GetView() is CompositionMeterChartView compositionMeterChartView)
		{
			DisplaySelectedDates = _selectedDates.First().ToString("yyyy/M/d ～") + _selectedDates.Last().ToString(" yyyy/M/d");
			CreateCompChartData();
			compositionMeterChartView.ChartView(_displayChartMin, _displayChartMax);
			CreateCompChartAverageData();
			_chartSettings = compositionMeterChartView.ChartVisible(_chartSettings);
		}
	}

	private void CreateCompChartData()
	{
		double defaultValue = ((_valueList.Count == 0) ? 0.01 : _valueList.Min());
		DisplayCompChartData = new BindableCollection<DisplayCompChartDataModel>();
		_selectedCompositionMeterChartData = new BindableCollection<CompositionMeterChartDataModel>();
		if (_compositionMeterChartData == null)
		{
			return;
		}
		foreach (DateTime selectedDate in _selectedDates)
		{
			BindableCollection<DisplayCompChartDataModel> bindableCollection = new BindableCollection<DisplayCompChartDataModel>();
			string date = selectedDate.Date.ToString("M/d");
			foreach (CompositionMeterChartDataModel compositionMeterChartDatum in _compositionMeterChartData)
			{
				if (compositionMeterChartDatum.TimezoneDate == selectedDate.Date.ToString())
				{
					_selectedCompositionMeterChartData.Add(compositionMeterChartDatum);
					bindableCollection.Add(new DisplayCompChartDataModel
					{
						Date = DateTime.Parse(compositionMeterChartDatum.TimezoneDate).ToString("M/d"),
						Weight = compositionMeterChartDatum.Weight,
						Bmi = compositionMeterChartDatum.Bmi,
						FatPercentage = compositionMeterChartDatum.FatPercentage,
						DefaultValue = defaultValue
					});
				}
			}
			if (bindableCollection.Count == 0)
			{
				DisplayCompChartData.Add(new DisplayCompChartDataModel
				{
					Date = date,
					DefaultValue = defaultValue
				});
			}
			else
			{
				DisplayCompChartData.AddRange(bindableCollection);
			}
		}
		if (_valueList.Count == 0)
		{
			_displayChartMin = 0.0;
		}
		else
		{
			_displayChartMin = Math.Floor(_valueList.Min() - 1.0);
		}
		if (_valueList.Count == 0)
		{
			_displayChartMax = 10.0;
		}
		else
		{
			_displayChartMax = Math.Ceiling(_valueList.Max() + 1.0);
		}
	}

	private IEnumerable<double> GetCompositionMeterValueList(CompositionMeterChartDataModel compData)
	{
		List<double> list = new List<double>();
		foreach (ChartSettingModel chartSetting in _chartSettings)
		{
			switch (chartSetting.Setting)
			{
			case "体重":
				if (chartSetting.Active && compData.Weight.HasValue)
				{
					list.Add(compData.Weight.Value);
				}
				break;
			case "BMI":
				if (chartSetting.Active && compData.Bmi.HasValue)
				{
					list.Add(compData.Bmi.Value);
				}
				break;
			case "体脂肪率":
				if (chartSetting.Active && compData.FatPercentage.HasValue)
				{
					list.Add(compData.FatPercentage.Value);
				}
				break;
			}
		}
		return list;
	}

	private void CreateCompChartAverageData()
	{
		WeightAverage = "--";
		BmiAverage = "--";
		FatPercentageAverage = "--";
		if (_selectedCompositionMeterChartData == null || _selectedCompositionMeterChartData.Count <= 0)
		{
			return;
		}
		List<double?> list = (from x in _selectedCompositionMeterChartData
			where x.Weight.HasValue
			select x.Weight).ToList();
		if (list.Count > 0)
		{
			double? num = list.Average((double? x) => x);
			WeightAverage = num?.ToString((num >= 100.0) ? "F1" : "F2");
		}
		List<double?> list2 = (from x in _selectedCompositionMeterChartData
			where x.Bmi.HasValue
			select x.Bmi).ToList();
		if (list2.Count > 0)
		{
			BmiAverage = list2.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list3 = (from x in _selectedCompositionMeterChartData
			where x.FatPercentage.HasValue
			select x.FatPercentage).ToList();
		if (list3.Count > 0)
		{
			FatPercentageAverage = list3.Average((double? x) => x)?.ToString("F1");
		}
	}

	private void SetChartContext()
	{
		ChartContext chartContext = IoC.Get<ChartContext>();
		chartContext.CompositionMeterChartData = _compositionMeterChartData;
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

	public void OnChartSettingTapped()
	{
		base.NavigationService.For<ChartSettingViewModel>().WithParam((ChartSettingViewModel x) => x.ChartSettings, _chartSettings).Navigate();
	}

	public void OnCompDataTapped()
	{
		base.NavigationService.For<CompositionMeterDataViewModel>().WithParam((CompositionMeterDataViewModel x) => x.SelectedCompChartData, _selectedCompositionMeterChartData).Navigate();
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

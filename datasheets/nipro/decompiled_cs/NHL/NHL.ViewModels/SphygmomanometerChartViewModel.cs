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

public class SphygmomanometerChartViewModel : ViewModelBase
{
	private BindableCollection<SphygmomanometerChartDataModel> _sphygmomanometerChartData;

	private BindableCollection<SphygmomanometerChartDataModel> _selectedSphygmomanometerChartData;

	private BindableCollection<DisplaySphygChartDataModel> _displaySphygChartData;

	private BindableCollection<ChartSettingModel> _chartSettings;

	private List<DateTime> _selectedDates;

	private string _displaySelectedDates;

	private string _maxPressureAverage;

	private string _minPressureAverage;

	private string _pulseAverage;

	private string _morningMaxPressureAverage;

	private string _morningMinPressureAverage;

	private string _morningPulseAverage;

	private string _nightMaxPressureAverage;

	private string _nightMinPressureAverage;

	private string _nightPulseAverage;

	private bool _isAverageData;

	private double DisplayChartMax { get; set; }

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

	public string MaxPressureAverage
	{
		get
		{
			return _maxPressureAverage;
		}
		set
		{
			_maxPressureAverage = value;
			NotifyOfPropertyChange(() => MaxPressureAverage);
		}
	}

	public string MinPressureAverage
	{
		get
		{
			return _minPressureAverage;
		}
		set
		{
			_minPressureAverage = value;
			NotifyOfPropertyChange(() => MinPressureAverage);
		}
	}

	public string PulseAverage
	{
		get
		{
			return _pulseAverage;
		}
		set
		{
			_pulseAverage = value;
			NotifyOfPropertyChange(() => PulseAverage);
		}
	}

	public string MorningMaxPressureAverage
	{
		get
		{
			return _morningMaxPressureAverage;
		}
		set
		{
			_morningMaxPressureAverage = value;
			NotifyOfPropertyChange(() => MorningMaxPressureAverage);
		}
	}

	public string MorningMinPressureAverage
	{
		get
		{
			return _morningMinPressureAverage;
		}
		set
		{
			_morningMinPressureAverage = value;
			NotifyOfPropertyChange(() => MorningMinPressureAverage);
		}
	}

	public string MorningPulseAverage
	{
		get
		{
			return _morningPulseAverage;
		}
		set
		{
			_morningPulseAverage = value;
			NotifyOfPropertyChange(() => MorningPulseAverage);
		}
	}

	public string NightMaxPressureAverage
	{
		get
		{
			return _nightMaxPressureAverage;
		}
		set
		{
			_nightMaxPressureAverage = value;
			NotifyOfPropertyChange(() => NightMaxPressureAverage);
		}
	}

	public string NightMinPressureAverage
	{
		get
		{
			return _nightMinPressureAverage;
		}
		set
		{
			_nightMinPressureAverage = value;
			NotifyOfPropertyChange(() => NightMinPressureAverage);
		}
	}

	public string NightPulseAverage
	{
		get
		{
			return _nightPulseAverage;
		}
		set
		{
			_nightPulseAverage = value;
			NotifyOfPropertyChange(() => NightPulseAverage);
		}
	}

	public bool IsAverageData
	{
		get
		{
			return _isAverageData;
		}
		set
		{
			_isAverageData = value;
			NotifyOfPropertyChange(() => IsAverageData);
		}
	}

	protected override void OnActivate()
	{
		base.OnActivate();
		ChartContext chartContext = IoC.Get<ChartContext>();
		_selectedDates = chartContext.SelectedDates;
		_sphygmomanometerChartData = chartContext.SphygmomanometerChartData;
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
		if (GetView() is SphygmomanometerChartView sphygmomanometerChartView)
		{
			DisplaySelectedDates = _selectedDates.First().ToString("yyyy/M/d ～") + _selectedDates.Last().ToString(" yyyy/M/d");
			CreateSphygChartData();
			sphygmomanometerChartView.ChartView(_displaySphygChartData, DisplayChartMax);
			CreateGluChartAverageData();
			_chartSettings = sphygmomanometerChartView.ChartVisible(_chartSettings);
		}
	}

	private void CreateSphygChartData()
	{
		DisplayChartMax = 100.0;
		_displaySphygChartData = new BindableCollection<DisplaySphygChartDataModel>();
		_selectedSphygmomanometerChartData = new BindableCollection<SphygmomanometerChartDataModel>();
		if (_sphygmomanometerChartData == null)
		{
			return;
		}
		foreach (DateTime selectedDate in _selectedDates)
		{
			BindableCollection<DisplaySphygChartDataModel> bindableCollection = new BindableCollection<DisplaySphygChartDataModel>();
			string date = selectedDate.Date.ToString("M/d");
			foreach (SphygmomanometerChartDataModel sphygmomanometerChartDatum in _sphygmomanometerChartData)
			{
				if (sphygmomanometerChartDatum.TimezoneDate == selectedDate.Date.ToString())
				{
					_selectedSphygmomanometerChartData.Add(sphygmomanometerChartDatum);
					double? num = ((Convert.ToDecimal(sphygmomanometerChartDatum.MaxPressure) > Convert.ToDecimal(sphygmomanometerChartDatum.Pulse)) ? sphygmomanometerChartDatum.MaxPressure : sphygmomanometerChartDatum.Pulse);
					if (num > DisplayChartMax)
					{
						DisplayChartMax = num.Value + 30.0;
					}
					switch (sphygmomanometerChartDatum.TimezoneType)
					{
					case null:
						bindableCollection.Add(new DisplaySphygChartDataModel
						{
							Date = DateTime.Parse(sphygmomanometerChartDatum.TimezoneDate).ToString("M/d"),
							MaxPressure = sphygmomanometerChartDatum.MaxPressure,
							MinPressure = sphygmomanometerChartDatum.MinPressure,
							Pulse = sphygmomanometerChartDatum.Pulse
						});
						break;
					case "01":
						bindableCollection.Add(new DisplaySphygChartDataModel
						{
							Date = DateTime.Parse(sphygmomanometerChartDatum.TimezoneDate).ToString("M/d"),
							MorningMaxPressure = sphygmomanometerChartDatum.MaxPressure,
							MorningMinPressure = sphygmomanometerChartDatum.MinPressure,
							MorningPulse = sphygmomanometerChartDatum.Pulse
						});
						break;
					case "02":
						bindableCollection.Add(new DisplaySphygChartDataModel
						{
							Date = DateTime.Parse(sphygmomanometerChartDatum.TimezoneDate).ToString("M/d"),
							NightMaxPressure = sphygmomanometerChartDatum.MaxPressure,
							NightMinPressure = sphygmomanometerChartDatum.MinPressure,
							NightPulse = sphygmomanometerChartDatum.Pulse
						});
						break;
					}
				}
			}
			if (bindableCollection.Count == 0)
			{
				_displaySphygChartData.Add(new DisplaySphygChartDataModel
				{
					Date = date
				});
			}
			else
			{
				_displaySphygChartData.AddRange(bindableCollection);
			}
		}
	}

	private void CreateGluChartAverageData()
	{
		MorningMaxPressureAverage = "--";
		MorningMinPressureAverage = "--";
		MorningPulseAverage = "--";
		NightMaxPressureAverage = "--";
		NightMinPressureAverage = "--";
		NightPulseAverage = "--";
		if (_selectedSphygmomanometerChartData == null || _selectedSphygmomanometerChartData.Count <= 0)
		{
			IsAverageData = false;
			return;
		}
		List<double?> list = (from x in _selectedSphygmomanometerChartData
			where x.MaxPressure.HasValue && x.TimezoneType == null
			select x.MaxPressure).ToList();
		if (list.Count > 0)
		{
			IsAverageData = true;
		}
		MaxPressureAverage = list.Average((double? x) => x)?.ToString("F1");
		List<double?> list2 = (from x in _selectedSphygmomanometerChartData
			where x.MinPressure.HasValue && x.TimezoneType == null
			select x.MinPressure).ToList();
		if (list2.Count > 0)
		{
			MinPressureAverage = list2.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list3 = (from x in _selectedSphygmomanometerChartData
			where x.Pulse.HasValue && x.TimezoneType == null
			select x.Pulse).ToList();
		if (list3.Count > 0)
		{
			PulseAverage = list3.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list4 = (from x in _selectedSphygmomanometerChartData
			where x.MaxPressure.HasValue && x.TimezoneType == "01"
			select x.MaxPressure).ToList();
		if (list4.Count > 0)
		{
			MorningMaxPressureAverage = list4.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list5 = (from x in _selectedSphygmomanometerChartData
			where x.MinPressure.HasValue && x.TimezoneType == "01"
			select x.MinPressure).ToList();
		if (list5.Count > 0)
		{
			MorningMinPressureAverage = list5.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list6 = (from x in _selectedSphygmomanometerChartData
			where x.Pulse.HasValue && x.TimezoneType == "01"
			select x.Pulse).ToList();
		if (list6.Count > 0)
		{
			MorningPulseAverage = list6.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list7 = (from x in _selectedSphygmomanometerChartData
			where x.MaxPressure.HasValue && x.TimezoneType == "02"
			select x.MaxPressure).ToList();
		if (list7.Count > 0)
		{
			NightMaxPressureAverage = list7.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list8 = (from x in _selectedSphygmomanometerChartData
			where x.MinPressure.HasValue && x.TimezoneType == "02"
			select x.MinPressure).ToList();
		if (list8.Count > 0)
		{
			NightMinPressureAverage = list8.Average((double? x) => x)?.ToString("F1");
		}
		List<double?> list9 = (from x in _selectedSphygmomanometerChartData
			where x.Pulse.HasValue && x.TimezoneType == "02"
			select x.Pulse).ToList();
		if (list9.Count > 0)
		{
			NightPulseAverage = list9.Average((double? x) => x)?.ToString("F1");
		}
	}

	private void SetChartContext()
	{
		ChartContext chartContext = IoC.Get<ChartContext>();
		chartContext.SphygmomanometerChartData = _sphygmomanometerChartData;
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

	public void OnGluSettingTapped()
	{
		base.NavigationService.For<ChartSettingViewModel>().WithParam((ChartSettingViewModel x) => x.ChartSettings, _chartSettings).Navigate();
	}

	public void OnTempDataTapped()
	{
		base.NavigationService.For<SphygmomanometerDataViewModel>().WithParam((SphygmomanometerDataViewModel x) => x.SelectedSpyhgChartData, _selectedSphygmomanometerChartData).Navigate();
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

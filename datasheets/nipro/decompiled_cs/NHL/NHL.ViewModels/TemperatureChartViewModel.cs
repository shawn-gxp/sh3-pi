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

public class TemperatureChartViewModel : ViewModelBase
{
	private BindableCollection<TemperatureChartDataModel> _temperatureChartData;

	private BindableCollection<TemperatureChartDataModel> _selectedTemperatureChartData;

	private BindableCollection<DisplayTemperatureChartDataModel> _displayTemperatureChartData;

	private List<DateTime> _selectedDates;

	private string _displaySelectedDates;

	private string _temperatureAverage;

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

	public string TemperatureAverage
	{
		get
		{
			return _temperatureAverage;
		}
		set
		{
			_temperatureAverage = value;
			NotifyOfPropertyChange(() => TemperatureAverage);
		}
	}

	protected override void OnActivate()
	{
		base.OnActivate();
		ChartContext chartContext = IoC.Get<ChartContext>();
		_selectedDates = chartContext.SelectedDates;
		_temperatureChartData = chartContext.TemperatureChartData;
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
		if (GetView() is TemperatureChartView temperatureChartView)
		{
			DisplaySelectedDates = _selectedDates.First().ToString("yyyy/M/d ～") + _selectedDates.Last().ToString(" yyyy/M/d");
			CreateTemperatureChartData();
			temperatureChartView.ChartView(_displayTemperatureChartData, DisplayChartMax);
			CreateGluChartAverageData();
		}
	}

	private void CreateTemperatureChartData()
	{
		DisplayChartMax = 37.0;
		_displayTemperatureChartData = new BindableCollection<DisplayTemperatureChartDataModel>();
		_selectedTemperatureChartData = new BindableCollection<TemperatureChartDataModel>();
		if (_temperatureChartData == null)
		{
			return;
		}
		foreach (DateTime selectedDate in _selectedDates)
		{
			BindableCollection<DisplayTemperatureChartDataModel> bindableCollection = new BindableCollection<DisplayTemperatureChartDataModel>();
			string date = selectedDate.Date.ToString("M/d");
			foreach (TemperatureChartDataModel temperatureChartDatum in _temperatureChartData)
			{
				if (temperatureChartDatum.TimezoneDate == selectedDate.Date.ToString())
				{
					_selectedTemperatureChartData.Add(temperatureChartDatum);
					if (temperatureChartDatum.Temperature >= DisplayChartMax)
					{
						DisplayChartMax = (temperatureChartDatum.Temperature + 0.5).Value;
					}
					bindableCollection.Add(new DisplayTemperatureChartDataModel
					{
						Date = DateTime.Parse(temperatureChartDatum.TimezoneDate).ToString("M/d"),
						Temperature = temperatureChartDatum.Temperature
					});
				}
			}
			if (bindableCollection.Count == 0)
			{
				_displayTemperatureChartData.Add(new DisplayTemperatureChartDataModel
				{
					Date = date
				});
			}
			else
			{
				_displayTemperatureChartData.AddRange(bindableCollection);
			}
		}
	}

	private void CreateGluChartAverageData()
	{
		TemperatureAverage = "--";
		if (_selectedTemperatureChartData == null || _selectedTemperatureChartData.Count <= 0)
		{
			return;
		}
		List<double?> list = (from x in _selectedTemperatureChartData
			where x.Temperature.HasValue
			select x.Temperature).ToList();
		if (list.Count > 0)
		{
			TemperatureAverage = list.Average((double? x) => x)?.ToString("F1");
		}
	}

	private void SetChartContext()
	{
		ChartContext chartContext = IoC.Get<ChartContext>();
		chartContext.TemperatureChartData = _temperatureChartData;
		chartContext.SelectedDates = _selectedDates;
	}

	public async void OnCalendarTapped()
	{
		await ExecAsync(async delegate
		{
			await Navigate<ChartCalendarViewModel>();
		});
	}

	public void OnTempDataTapped()
	{
		base.NavigationService.For<TemperatureDataViewModel>().WithParam((TemperatureDataViewModel x) => x.SelectedTemperatureChartData, _selectedTemperatureChartData).Navigate();
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

using System;
using Caliburn.Micro;
using NHL.Models;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class TemperatureDataViewModel : ViewModelBase
{
	private BindableCollection<TemperatureChartDataModel> _selectedTemperatureChartData;

	private BindableCollection<TemperatureChartDataModel> _displayTemperatureChartData;

	public BindableCollection<TemperatureChartDataModel> SelectedTemperatureChartData
	{
		get
		{
			return _selectedTemperatureChartData;
		}
		set
		{
			_selectedTemperatureChartData = value;
			NotifyOfPropertyChange(() => SelectedTemperatureChartData);
		}
	}

	public BindableCollection<TemperatureChartDataModel> DisplayTemperatureChartData
	{
		get
		{
			return _displayTemperatureChartData;
		}
		set
		{
			_displayTemperatureChartData = value;
			NotifyOfPropertyChange(() => DisplayTemperatureChartData);
		}
	}

	protected override void OnActivate()
	{
		CreateData();
		base.OnActivate();
	}

	private void CreateData()
	{
		DisplayTemperatureChartData = new BindableCollection<TemperatureChartDataModel>();
		if (SelectedTemperatureChartData == null)
		{
			return;
		}
		foreach (TemperatureChartDataModel selectedTemperatureChartDatum in SelectedTemperatureChartData)
		{
			TemperatureChartDataModel temperatureChartDataModel = new TemperatureChartDataModel();
			temperatureChartDataModel.MeasurementAt = DateTime.Parse(selectedTemperatureChartDatum.TimezoneDate);
			temperatureChartDataModel.Temperature = selectedTemperatureChartDatum.Temperature;
			DisplayTemperatureChartData.Add(temperatureChartDataModel);
		}
	}
}

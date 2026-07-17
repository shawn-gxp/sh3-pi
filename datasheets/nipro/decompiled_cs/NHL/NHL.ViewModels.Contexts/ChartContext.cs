using System;
using System.Collections.Generic;
using Caliburn.Micro;
using NHL.Models;

namespace NHL.ViewModels.Contexts;

public class ChartContext
{
	public BindableCollection<GlucoseChartDataModel> GlucoseChartData { get; set; }

	public BindableCollection<TemperatureChartDataModel> TemperatureChartData { get; set; }

	public BindableCollection<SphygmomanometerChartDataModel> SphygmomanometerChartData { get; set; }

	public BindableCollection<StepChartDataModel> StepChartData { get; set; }

	public BindableCollection<CompositionMeterChartDataModel> CompositionMeterChartData { get; set; }

	public BindableCollection<ChartSettingModel> ChartSettings { get; set; }

	public List<DateTime> SelectedDates { get; set; }

	public void Initialize()
	{
		GlucoseChartData = new BindableCollection<GlucoseChartDataModel>();
		TemperatureChartData = new BindableCollection<TemperatureChartDataModel>();
		SphygmomanometerChartData = new BindableCollection<SphygmomanometerChartDataModel>();
		StepChartData = new BindableCollection<StepChartDataModel>();
		CompositionMeterChartData = new BindableCollection<CompositionMeterChartDataModel>();
		ChartSettings = new BindableCollection<ChartSettingModel>();
		SelectedDates = new List<DateTime>();
	}
}

using System;
using Caliburn.Micro;
using NHL.Models;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class CompositionMeterDataViewModel : ViewModelBase
{
	private BindableCollection<CompositionMeterChartDataModel> _selectedCompChartData;

	private BindableCollection<CompositionMeterChartDataModel> _displayCompChartData;

	public BindableCollection<CompositionMeterChartDataModel> SelectedCompChartData
	{
		get
		{
			return _selectedCompChartData;
		}
		set
		{
			_selectedCompChartData = value;
			NotifyOfPropertyChange(() => SelectedCompChartData);
		}
	}

	public BindableCollection<CompositionMeterChartDataModel> DisplayCompChartData
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

	protected override void OnActivate()
	{
		CreateData();
		base.OnActivate();
	}

	private void CreateData()
	{
		DisplayCompChartData = new BindableCollection<CompositionMeterChartDataModel>();
		if (SelectedCompChartData == null)
		{
			return;
		}
		foreach (CompositionMeterChartDataModel selectedCompChartDatum in SelectedCompChartData)
		{
			CompositionMeterChartDataModel compositionMeterChartDataModel = new CompositionMeterChartDataModel();
			compositionMeterChartDataModel.MeasurementAt = DateTime.Parse(selectedCompChartDatum.TimezoneDate);
			compositionMeterChartDataModel.Weight = selectedCompChartDatum.Weight;
			compositionMeterChartDataModel.Bmi = selectedCompChartDatum.Bmi;
			compositionMeterChartDataModel.FatPercentage = selectedCompChartDatum.FatPercentage;
			DisplayCompChartData.Add(compositionMeterChartDataModel);
		}
	}
}

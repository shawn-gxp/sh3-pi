using Caliburn.Micro;
using NHL.Models;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class StepDataViewModel : ViewModelBase
{
	public BindableCollection<StepChartDataModel> _selectedStepChartData;

	public BindableCollection<StepChartDataModel> _displayStepData;

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

	public BindableCollection<StepChartDataModel> DisplayStepData
	{
		get
		{
			return _displayStepData;
		}
		set
		{
			if (_displayStepData != value)
			{
				_displayStepData = value;
				NotifyOfPropertyChange(() => DisplayStepData);
			}
		}
	}

	protected override void OnActivate()
	{
		CreateData();
		base.OnActivate();
	}

	private void CreateData()
	{
		DisplayStepData = new BindableCollection<StepChartDataModel>();
		if (SelectedStepChartData == null)
		{
			return;
		}
		foreach (StepChartDataModel selectedStepChartDatum in SelectedStepChartData)
		{
			StepChartDataModel stepChartDataModel = new StepChartDataModel();
			stepChartDataModel.Step = selectedStepChartDatum.Step;
			stepChartDataModel.MeasurementAt = selectedStepChartDatum.MeasurementAt;
			DisplayStepData.Add(stepChartDataModel);
		}
	}
}

using System;
using Caliburn.Micro;
using NHL.Models;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class GlucoseDataViewModel : ViewModelBase
{
	public BindableCollection<GlucoseChartDataModel> selectedGlucoseChartData;

	public BindableCollection<GlucoseChartDataModel> displayGlucoseData;

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

	public BindableCollection<GlucoseChartDataModel> DisplayGlucoseData
	{
		get
		{
			return displayGlucoseData;
		}
		set
		{
			if (displayGlucoseData != value)
			{
				displayGlucoseData = value;
				NotifyOfPropertyChange(() => DisplayGlucoseData);
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
		DisplayGlucoseData = new BindableCollection<GlucoseChartDataModel>();
		if (SelectedGlucoseChartData == null)
		{
			return;
		}
		foreach (GlucoseChartDataModel selectedGlucoseChartDatum in SelectedGlucoseChartData)
		{
			GlucoseChartDataModel glucoseChartDataModel = new GlucoseChartDataModel();
			glucoseChartDataModel.TimezoneDate = DateTime.Parse(selectedGlucoseChartDatum.TimezoneDate).ToString("M/d");
			glucoseChartDataModel.MeasurementValue = selectedGlucoseChartDatum.MeasurementValue;
			switch (selectedGlucoseChartDatum.TimezoneType)
			{
			case "01":
				glucoseChartDataModel.TimezoneType = "朝食前";
				break;
			case "02":
				glucoseChartDataModel.TimezoneType = "朝食後";
				break;
			case "03":
				glucoseChartDataModel.TimezoneType = "昼食前";
				break;
			case "04":
				glucoseChartDataModel.TimezoneType = "昼食後";
				break;
			case "05":
				glucoseChartDataModel.TimezoneType = "夕食前";
				break;
			case "06":
				glucoseChartDataModel.TimezoneType = "夕食後";
				break;
			case "07":
				glucoseChartDataModel.TimezoneType = "就寝前";
				break;
			case "08":
				glucoseChartDataModel.TimezoneType = "深夜";
				break;
			}
			DisplayGlucoseData.Add(glucoseChartDataModel);
		}
	}
}

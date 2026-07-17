using System;
using Caliburn.Micro;
using NHL.Models;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class SphygmomanometerDataViewModel : ViewModelBase
{
	private BindableCollection<SphygmomanometerChartDataModel> _selectedSphygChartData;

	private BindableCollection<SphygmomanometerChartDataModel> _displaySphygChartData;

	public BindableCollection<SphygmomanometerChartDataModel> SelectedSpyhgChartData
	{
		get
		{
			return _selectedSphygChartData;
		}
		set
		{
			if (_selectedSphygChartData != value)
			{
				_selectedSphygChartData = value;
				NotifyOfPropertyChange(() => SelectedSpyhgChartData);
			}
		}
	}

	public BindableCollection<SphygmomanometerChartDataModel> DisplaySphygChartData
	{
		get
		{
			return _displaySphygChartData;
		}
		set
		{
			if (_displaySphygChartData != value)
			{
				_displaySphygChartData = value;
				NotifyOfPropertyChange(() => DisplaySphygChartData);
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
		DisplaySphygChartData = new BindableCollection<SphygmomanometerChartDataModel>();
		if (SelectedSpyhgChartData == null)
		{
			return;
		}
		foreach (SphygmomanometerChartDataModel selectedSpyhgChartDatum in SelectedSpyhgChartData)
		{
			SphygmomanometerChartDataModel sphygmomanometerChartDataModel = new SphygmomanometerChartDataModel();
			sphygmomanometerChartDataModel.MeasurementAt = DateTime.Parse(selectedSpyhgChartDatum.TimezoneDate);
			sphygmomanometerChartDataModel.MaxPressure = selectedSpyhgChartDatum.MaxPressure;
			sphygmomanometerChartDataModel.MinPressure = selectedSpyhgChartDatum.MinPressure;
			sphygmomanometerChartDataModel.Pulse = selectedSpyhgChartDatum.Pulse;
			switch (selectedSpyhgChartDatum.TimezoneType)
			{
			case "01":
				sphygmomanometerChartDataModel.TimezoneType = "朝";
				break;
			case "02":
				sphygmomanometerChartDataModel.TimezoneType = "晩";
				break;
			default:
				sphygmomanometerChartDataModel.TimezoneType = "未判定";
				break;
			}
			DisplaySphygChartData.Add(sphygmomanometerChartDataModel);
		}
	}
}

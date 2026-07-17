using System;
using Caliburn.Micro;
using NHL.Models.Entity;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class MeasurementCalendarViewModel : ViewModelBase
{
	private BindableCollection<Measurement> gluMeasurementList;

	private BindableCollection<Measurement> pressureMeasurementList;

	private BindableCollection<Measurement> tempMeasurementList;

	private BindableCollection<Measurement> stepMeasurementList;

	private DateTime calendarDisplayDate;

	private bool isPresentedMenu;

	public BindableCollection<Measurement> PressureMeasurementList
	{
		get
		{
			return pressureMeasurementList;
		}
		set
		{
			if (pressureMeasurementList != value)
			{
				pressureMeasurementList = value;
				NotifyOfPropertyChange(() => PressureMeasurementList);
			}
		}
	}

	public BindableCollection<Measurement> TempMeasurementList
	{
		get
		{
			return tempMeasurementList;
		}
		set
		{
			if (tempMeasurementList != value)
			{
				tempMeasurementList = value;
				NotifyOfPropertyChange(() => TempMeasurementList);
			}
		}
	}

	public BindableCollection<Measurement> GluMeasurementList
	{
		get
		{
			return gluMeasurementList;
		}
		set
		{
			if (gluMeasurementList != value)
			{
				gluMeasurementList = value;
				NotifyOfPropertyChange(() => GluMeasurementList);
			}
		}
	}

	public BindableCollection<Measurement> StepMeasurementList
	{
		get
		{
			return stepMeasurementList;
		}
		set
		{
			if (stepMeasurementList != value)
			{
				stepMeasurementList = value;
				NotifyOfPropertyChange(() => StepMeasurementList);
			}
		}
	}

	public DateTime CalendarDisplayDate
	{
		get
		{
			return calendarDisplayDate;
		}
		set
		{
			if (calendarDisplayDate != value)
			{
				calendarDisplayDate = value;
				NotifyOfPropertyChange(() => CalendarDisplayDate);
			}
		}
	}

	public bool IsPresentedMenu
	{
		get
		{
			return isPresentedMenu;
		}
		set
		{
			isPresentedMenu = value;
			NotifyOfPropertyChange(() => IsPresentedMenu);
		}
	}

	protected override void OnInitialize()
	{
		base.OnInitialize();
	}

	public void Initialize()
	{
		base.EventAggregator.Unsubscribe(this);
		base.EventAggregator.Subscribe(this);
		base.OnInitialize();
	}

	protected override void OnDeactivate(bool close)
	{
		base.OnDeactivate(close);
	}
}

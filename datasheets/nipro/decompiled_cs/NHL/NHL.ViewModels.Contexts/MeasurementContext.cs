using System;
using Caliburn.Micro;
using NHL.Models.Entity;

namespace NHL.ViewModels.Contexts;

public class MeasurementContext
{
	public BindableCollection<Measurement> GluMeasurementList { get; set; }

	public BindableCollection<Measurement> PressureMeasurementList { get; set; }

	public BindableCollection<Measurement> TempMeasurementList { get; set; }

	public BindableCollection<Measurement> CompMeasurementList { get; set; }

	public BindableCollection<Measurement> StepMeasurementList { get; set; }

	public DateTime CalendarDisplayDate { get; set; }

	public void Initialize()
	{
	}
}

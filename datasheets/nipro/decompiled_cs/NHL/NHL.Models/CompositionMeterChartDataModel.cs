using System;
using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class CompositionMeterChartDataModel : ModelBase
{
	public DateTime? MeasurementAt { get; set; }

	public double? Weight { get; set; }

	public double? Bmi { get; set; }

	public double? FatPercentage { get; set; }

	public string TimezoneDate { get; set; }
}

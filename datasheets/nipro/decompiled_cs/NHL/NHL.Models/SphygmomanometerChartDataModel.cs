using System;
using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class SphygmomanometerChartDataModel : ModelBase
{
	public DateTime? MeasurementAt { get; set; }

	public double? MaxPressure { get; set; }

	public double? MinPressure { get; set; }

	public double? Pulse { get; set; }

	public string TimezoneType { get; set; }

	public string TimezoneDate { get; set; }
}

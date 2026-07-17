using System;
using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class TemperatureChartDataModel : ModelBase
{
	public DateTime? MeasurementAt { get; set; }

	public double? Temperature { get; set; }

	public string TimezoneDate { get; set; }
}

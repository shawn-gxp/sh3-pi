using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class DisplayTemperatureChartDataModel : ModelBase
{
	public string Date { get; set; }

	public double? Temperature { get; set; }

	public double DefaultValue { get; set; } = 32.01;
}

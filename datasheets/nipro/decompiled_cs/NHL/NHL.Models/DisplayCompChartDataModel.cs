using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class DisplayCompChartDataModel : ModelBase
{
	public string Date { get; set; }

	public double? Weight { get; set; }

	public double? Bmi { get; set; }

	public double? FatPercentage { get; set; }

	public double DefaultValue { get; set; }
}

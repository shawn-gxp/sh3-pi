using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class DisplaySphygChartDataModel : ModelBase
{
	public string Date { get; set; }

	public double? MaxPressure { get; set; }

	public double? MinPressure { get; set; }

	public double? Pulse { get; set; }

	public double? MorningMaxPressure { get; set; }

	public double? MorningMinPressure { get; set; }

	public double? MorningPulse { get; set; }

	public double? NightMaxPressure { get; set; }

	public double? NightMinPressure { get; set; }

	public double? NightPulse { get; set; }

	public double DefaultValue { get; set; } = 0.01;
}

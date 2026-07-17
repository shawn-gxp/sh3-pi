using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class DisplayStepChartDataModel : ModelBase
{
	public string Date { get; set; }

	public decimal? Step { get; set; }
}

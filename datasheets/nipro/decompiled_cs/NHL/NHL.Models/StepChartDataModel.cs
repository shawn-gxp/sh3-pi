using System;
using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class StepChartDataModel : ModelBase
{
	private DateTime? _measurementAt { get; set; }

	private decimal? _step { get; set; }

	[DataMember]
	public DateTime? MeasurementAt
	{
		get
		{
			return _measurementAt;
		}
		set
		{
			if (_measurementAt != value)
			{
				_measurementAt = value;
				NotifyOfPropertyChange(() => _measurementAt);
			}
		}
	}

	public decimal? Step
	{
		get
		{
			return _step;
		}
		set
		{
			if (!(_step == value))
			{
				_step = value;
				NotifyOfPropertyChange(() => _step);
			}
		}
	}
}

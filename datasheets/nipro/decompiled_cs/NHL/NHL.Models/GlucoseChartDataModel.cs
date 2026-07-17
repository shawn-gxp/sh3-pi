using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class GlucoseChartDataModel : ModelBase
{
	private decimal measurementValue;

	private string timezoneDate;

	private string timezoneType;

	[DataMember]
	public decimal MeasurementValue
	{
		get
		{
			return measurementValue;
		}
		set
		{
			if (measurementValue != value)
			{
				measurementValue = value;
				NotifyOfPropertyChange(() => MeasurementValue);
			}
		}
	}

	public string TimezoneDate
	{
		get
		{
			return timezoneDate;
		}
		set
		{
			if (timezoneDate != value)
			{
				timezoneDate = value;
				NotifyOfPropertyChange(() => TimezoneDate);
			}
		}
	}

	public string TimezoneType
	{
		get
		{
			return timezoneType;
		}
		set
		{
			if (timezoneType != value)
			{
				timezoneType = value;
				NotifyOfPropertyChange(() => TimezoneType);
			}
		}
	}
}

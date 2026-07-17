using System;
using System.Runtime.Serialization;
using NHL.Models.Entity;
using NHL.Models.Support;
using Xamarin.Forms;

namespace NHL.Models;

[DataContract]
public class MeasurementResultModel : ModelBase
{
	private string displayMeasurementValue;

	private string displayCategory;

	private string displayTimezoneType;

	private Measurement source;

	private Color _TextColor;

	private DateTime registDate;

	private string category;

	private decimal measurementValue;

	private bool thresholdExclusion;

	private bool physicianConfirmed;

	private DateTime? timezoneDate;

	private string timezoneType;

	public string DisplayMeasurementValue
	{
		get
		{
			return displayMeasurementValue;
		}
		set
		{
			if (displayMeasurementValue != value)
			{
				displayMeasurementValue = value;
				NotifyOfPropertyChange(() => DisplayMeasurementValue);
			}
		}
	}

	public string DisplayCategory
	{
		get
		{
			return displayCategory;
		}
		set
		{
			if (displayCategory != value)
			{
				displayCategory = value;
				NotifyOfPropertyChange(() => DisplayCategory);
			}
		}
	}

	public string DisplayTimezoneType
	{
		get
		{
			return displayTimezoneType;
		}
		set
		{
			if (displayTimezoneType != value)
			{
				displayTimezoneType = value;
				NotifyOfPropertyChange(() => DisplayTimezoneType);
			}
		}
	}

	public Measurement Source
	{
		get
		{
			return source;
		}
		set
		{
			if (source != value)
			{
				source = value;
				NotifyOfPropertyChange(() => Source);
			}
		}
	}

	public Color TextColor
	{
		get
		{
			return _TextColor;
		}
		set
		{
			_TextColor = value;
			NotifyOfPropertyChange(() => TextColor);
		}
	}

	[DataMember]
	public DateTime RegistDate
	{
		get
		{
			return registDate;
		}
		set
		{
			if (registDate != value)
			{
				registDate = value;
				NotifyOfPropertyChange(() => RegistDate);
			}
		}
	}

	[DataMember]
	public string Category
	{
		get
		{
			return category;
		}
		set
		{
			if (category != value)
			{
				category = value;
				NotifyOfPropertyChange(() => Category);
			}
		}
	}

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

	[DataMember]
	public bool ThresholdExclusion
	{
		get
		{
			return thresholdExclusion;
		}
		set
		{
			if (thresholdExclusion != value)
			{
				thresholdExclusion = value;
				NotifyOfPropertyChange(() => ThresholdExclusion);
			}
		}
	}

	[DataMember]
	public bool PhysicianConfirmed
	{
		get
		{
			return physicianConfirmed;
		}
		set
		{
			if (physicianConfirmed != value)
			{
				physicianConfirmed = value;
				NotifyOfPropertyChange(() => PhysicianConfirmed);
			}
		}
	}

	public DateTime? TimezoneDate
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

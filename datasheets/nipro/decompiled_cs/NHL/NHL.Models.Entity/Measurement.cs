using System;
using System.Runtime.Serialization;
using Microsoft.WindowsAzure.MobileServices;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models.Entity;

[DataContract]
public class Measurement : ModelBase
{
	private string id;

	private string igUserId;

	private string measurementType;

	private DateTime? measurementAt;

	private DateTime? timezoneDate;

	private string timezone;

	private double? measurementValue;

	private string exceedLimitType;

	private string measuringEquipmentMeasurementId;

	private string measuringEquipmentName;

	private string measuringEquipmentId;

	private DateTime? createdAt;

	private DateTime? updatedAt;

	private bool deleted;

	private string version;

	[JsonProperty("id")]
	public string Id
	{
		get
		{
			return id;
		}
		set
		{
			if (id != value)
			{
				id = value;
				NotifyOfPropertyChange(() => Id);
			}
		}
	}

	[JsonProperty("igUserId")]
	public string IgUserId
	{
		get
		{
			return igUserId;
		}
		set
		{
			if (igUserId != value)
			{
				igUserId = value;
				NotifyOfPropertyChange(() => IgUserId);
			}
		}
	}

	[JsonProperty("measurementType")]
	public string MeasurementType
	{
		get
		{
			return measurementType;
		}
		set
		{
			if (measurementType != value)
			{
				measurementType = value;
				NotifyOfPropertyChange(() => MeasurementType);
			}
		}
	}

	[JsonProperty("measurementAt")]
	public DateTime? MeasurementAt
	{
		get
		{
			return measurementAt;
		}
		set
		{
			if (measurementAt != value)
			{
				measurementAt = value;
				NotifyOfPropertyChange(() => MeasurementAt);
			}
		}
	}

	[JsonProperty("timezoneDate")]
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

	[JsonProperty("timezone")]
	public string Timezone
	{
		get
		{
			return timezone;
		}
		set
		{
			if (timezone != value)
			{
				timezone = value;
				NotifyOfPropertyChange(() => Timezone);
			}
		}
	}

	[JsonProperty("measurementValue")]
	public double? MeasurementValue
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

	[JsonProperty("exceedLimitType")]
	public string ExceedLimitType
	{
		get
		{
			return exceedLimitType;
		}
		set
		{
			if (exceedLimitType != value)
			{
				exceedLimitType = value;
				NotifyOfPropertyChange(() => ExceedLimitType);
			}
		}
	}

	[JsonProperty("measuringEquipmentMeasurementId")]
	public string MeasuringEquipmentMeasurementId
	{
		get
		{
			return measuringEquipmentMeasurementId;
		}
		set
		{
			if (measuringEquipmentMeasurementId != value)
			{
				measuringEquipmentMeasurementId = value;
				NotifyOfPropertyChange(() => MeasuringEquipmentMeasurementId);
			}
		}
	}

	[JsonProperty("measuringEquipmentName")]
	public string MeasuringEquipmentName
	{
		get
		{
			return measuringEquipmentName;
		}
		set
		{
			if (measuringEquipmentName != value)
			{
				measuringEquipmentName = value;
				NotifyOfPropertyChange(() => MeasuringEquipmentName);
			}
		}
	}

	[JsonProperty("measuringEquipmentId")]
	public string MeasuringEquipmentId
	{
		get
		{
			return measuringEquipmentId;
		}
		set
		{
			if (measuringEquipmentId != value)
			{
				measuringEquipmentId = value;
				NotifyOfPropertyChange(() => MeasuringEquipmentId);
			}
		}
	}

	[JsonProperty("createdAt")]
	public DateTime? CreatedAt
	{
		get
		{
			return createdAt;
		}
		set
		{
			if (createdAt != value)
			{
				createdAt = value;
				NotifyOfPropertyChange(() => CreatedAt);
			}
		}
	}

	[JsonProperty("updatedAt")]
	public DateTime? UpdatedAt
	{
		get
		{
			return updatedAt;
		}
		set
		{
			if (updatedAt != value)
			{
				updatedAt = value;
				NotifyOfPropertyChange(() => UpdatedAt);
			}
		}
	}

	[JsonProperty("deleted")]
	public bool Deleted
	{
		get
		{
			return deleted;
		}
		set
		{
			if (deleted != value)
			{
				deleted = value;
				NotifyOfPropertyChange(() => Deleted);
			}
		}
	}

	[Version]
	public string Version
	{
		get
		{
			return version;
		}
		set
		{
			if (version != value)
			{
				version = value;
				NotifyOfPropertyChange(() => Version);
			}
		}
	}
}

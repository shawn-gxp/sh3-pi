using System;
using System.Runtime.Serialization;
using Microsoft.WindowsAzure.MobileServices;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

[DataContract]
public class Photograph : ModelBase
{
	private string id;

	private string iGUserId;

	private string image;

	private DateTime? shootingAt;

	private DateTime? timezoneDate;

	private bool deleted;

	private DateTime? updatedAt;

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
	public string IGUserId
	{
		get
		{
			return iGUserId;
		}
		set
		{
			if (iGUserId != value)
			{
				iGUserId = value;
				NotifyOfPropertyChange(() => IGUserId);
			}
		}
	}

	[JsonProperty("image")]
	public string Image
	{
		get
		{
			return image;
		}
		set
		{
			if (image != value)
			{
				image = value;
				NotifyOfPropertyChange(() => Image);
			}
		}
	}

	[JsonProperty("shootingAt")]
	public DateTime? ShootingAt
	{
		get
		{
			return shootingAt;
		}
		set
		{
			if (shootingAt != value)
			{
				shootingAt = value;
				NotifyOfPropertyChange(() => ShootingAt);
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
	public string Version { get; set; }

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
}

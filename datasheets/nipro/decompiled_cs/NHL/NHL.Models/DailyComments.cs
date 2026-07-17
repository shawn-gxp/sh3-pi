using System;
using System.Runtime.Serialization;
using Microsoft.WindowsAzure.MobileServices;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

[DataContract]
public class DailyComments : ModelBase
{
	private string id;

	private string igUserId;

	private string comment;

	private DateTime? timezoneDate;

	private bool deleted;

	private string version;

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

	[JsonProperty("comment")]
	public string Comment
	{
		get
		{
			return comment;
		}
		set
		{
			if (comment != value)
			{
				comment = value;
				NotifyOfPropertyChange(() => Comment);
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

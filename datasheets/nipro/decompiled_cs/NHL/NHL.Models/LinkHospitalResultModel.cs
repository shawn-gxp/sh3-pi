using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class LinkHospitalResultModel : ModelBase
{
	private string igUserId;

	private string hospitalid;

	private string id;

	private DateTime? createdAt;

	private DateTime? updatedAt;

	private bool deleted;

	private string version;

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

	public string Hospitalid
	{
		get
		{
			return hospitalid;
		}
		set
		{
			if (hospitalid != value)
			{
				hospitalid = value;
				NotifyOfPropertyChange(() => Hospitalid);
			}
		}
	}

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

	[JsonProperty("version")]
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

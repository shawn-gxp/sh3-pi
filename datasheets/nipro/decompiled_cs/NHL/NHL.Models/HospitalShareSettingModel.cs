using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class HospitalShareSettingModel : ModelBase
{
	private string id;

	private string hospitalId;

	private int itemNo;

	private string itemName;

	private string type;

	private string selectItems;

	private bool requiredFlg;

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
			if (!(id == value))
			{
				id = value;
				NotifyOfPropertyChange(() => Id);
			}
		}
	}

	[JsonProperty("hospitalId")]
	public string HospitalId
	{
		get
		{
			return hospitalId;
		}
		set
		{
			if (!(hospitalId == value))
			{
				hospitalId = value;
				NotifyOfPropertyChange(() => HospitalId);
			}
		}
	}

	[JsonProperty("itemNo")]
	public int ItemNo
	{
		get
		{
			return itemNo;
		}
		set
		{
			if (itemNo != value)
			{
				itemNo = value;
				NotifyOfPropertyChange(() => ItemNo);
			}
		}
	}

	[JsonProperty("itemName")]
	public string ItemName
	{
		get
		{
			return itemName;
		}
		set
		{
			if (!(itemName == value))
			{
				itemName = value;
				NotifyOfPropertyChange(() => ItemName);
			}
		}
	}

	[JsonProperty("type")]
	public string Type
	{
		get
		{
			return type;
		}
		set
		{
			if (!(type == value))
			{
				type = value;
				NotifyOfPropertyChange(() => Type);
			}
		}
	}

	[JsonProperty("selectItems")]
	public string SelectItems
	{
		get
		{
			return selectItems;
		}
		set
		{
			if (!(selectItems == value))
			{
				selectItems = value;
				NotifyOfPropertyChange(() => SelectItems);
			}
		}
	}

	[JsonProperty("requiredFlg")]
	public bool RequiredFlg
	{
		get
		{
			return requiredFlg;
		}
		set
		{
			if (requiredFlg != value)
			{
				requiredFlg = value;
				NotifyOfPropertyChange(() => RequiredFlg);
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
			if (!(createdAt == value))
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
			if (!(updatedAt == value))
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
			if (!(version == value))
			{
				version = value;
				NotifyOfPropertyChange(() => Version);
			}
		}
	}
}

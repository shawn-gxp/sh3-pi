using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class HospitalModel : ModelBase
{
	private string id;

	private string hospitalCode;

	private string facilitiesNumber;

	private string dcfFacilitiesCode;

	private string facilitiesName;

	private string facilitiesAbbreviatedName;

	private string facilitiesNameKana;

	private string facilitiesAbbreviatedNameKana;

	private int? numberOfSickbedsCapacity;

	private string address;

	private string telephoneNumber;

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

	[JsonProperty("hospitalCode")]
	public string HospitalCode
	{
		get
		{
			return hospitalCode;
		}
		set
		{
			if (hospitalCode != value)
			{
				hospitalCode = value;
				NotifyOfPropertyChange(() => HospitalCode);
			}
		}
	}

	[JsonProperty("facilitiesNumber")]
	public string FacilitiesNumber
	{
		get
		{
			return facilitiesNumber;
		}
		set
		{
			if (facilitiesNumber != value)
			{
				facilitiesNumber = value;
				NotifyOfPropertyChange(() => FacilitiesNumber);
			}
		}
	}

	[JsonProperty("dcfFacilitiesCode")]
	public string DcfFacilitiesCode
	{
		get
		{
			return dcfFacilitiesCode;
		}
		set
		{
			if (dcfFacilitiesCode != value)
			{
				dcfFacilitiesCode = value;
				NotifyOfPropertyChange(() => DcfFacilitiesCode);
			}
		}
	}

	[JsonProperty("facilitiesName")]
	public string FacilitiesName
	{
		get
		{
			return facilitiesName;
		}
		set
		{
			if (facilitiesName != value)
			{
				facilitiesName = value;
				NotifyOfPropertyChange(() => FacilitiesName);
			}
		}
	}

	[JsonProperty("facilitiesAbbreviatedName")]
	public string FacilitiesAbbreviatedName
	{
		get
		{
			return facilitiesAbbreviatedName;
		}
		set
		{
			if (facilitiesAbbreviatedName != value)
			{
				facilitiesAbbreviatedName = value;
				NotifyOfPropertyChange(() => FacilitiesAbbreviatedName);
			}
		}
	}

	[JsonProperty("facilitiesNameKana")]
	public string FacilitiesNameKana
	{
		get
		{
			return facilitiesNameKana;
		}
		set
		{
			if (facilitiesNameKana != value)
			{
				facilitiesNameKana = value;
				NotifyOfPropertyChange(() => FacilitiesNameKana);
			}
		}
	}

	[JsonProperty("facilitiesAbbreviatedNameKana")]
	public string FacilitiesAbbreviatedNameKana
	{
		get
		{
			return facilitiesAbbreviatedNameKana;
		}
		set
		{
			if (facilitiesAbbreviatedNameKana != value)
			{
				facilitiesAbbreviatedNameKana = value;
				NotifyOfPropertyChange(() => FacilitiesAbbreviatedNameKana);
			}
		}
	}

	[JsonProperty("numberOfSickbedsCapacity")]
	public int? NumberOfSickbedsCapacity
	{
		get
		{
			return numberOfSickbedsCapacity;
		}
		set
		{
			if (numberOfSickbedsCapacity != value)
			{
				numberOfSickbedsCapacity = value;
				NotifyOfPropertyChange(() => NumberOfSickbedsCapacity);
			}
		}
	}

	[JsonProperty("address")]
	public string Address
	{
		get
		{
			return address;
		}
		set
		{
			if (address != value)
			{
				address = value;
				NotifyOfPropertyChange(() => Address);
			}
		}
	}

	[JsonProperty("telephoneNumber")]
	public string TelephoneNumber
	{
		get
		{
			return telephoneNumber;
		}
		set
		{
			if (telephoneNumber != value)
			{
				telephoneNumber = value;
				NotifyOfPropertyChange(() => TelephoneNumber);
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

using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class AuthenticatedUserModel : ModelBase
{
	private string id;

	private string name;

	private string nameKana;

	private DateTime? birthday;

	private string sex;

	private double? height;

	private double? weight;

	private string timezone1;

	private string timezone2;

	private string timezone3;

	private string timezone4;

	private string timezone5;

	private string timezone6;

	private string timezone7;

	private string timezone8;

	private string backupCode;

	private string hospitalid;

	private string facilitiesName;

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

	[JsonProperty("name")]
	public string Name
	{
		get
		{
			return name;
		}
		set
		{
			if (name != value)
			{
				name = value;
				NotifyOfPropertyChange(() => Name);
			}
		}
	}

	[JsonProperty("nameKana")]
	public string NameKana
	{
		get
		{
			return nameKana;
		}
		set
		{
			if (nameKana != value)
			{
				nameKana = value;
				NotifyOfPropertyChange(() => NameKana);
			}
		}
	}

	[JsonProperty("birthday")]
	public DateTime? Birthday
	{
		get
		{
			return birthday;
		}
		set
		{
			if (birthday != value)
			{
				birthday = value;
				NotifyOfPropertyChange(() => Birthday);
			}
		}
	}

	[JsonProperty("sex")]
	public string Sex
	{
		get
		{
			return sex;
		}
		set
		{
			if (sex != value)
			{
				sex = value;
				NotifyOfPropertyChange(() => Sex);
			}
		}
	}

	[JsonProperty("height")]
	public double? Height
	{
		get
		{
			return height;
		}
		set
		{
			if (height != value)
			{
				height = value;
				NotifyOfPropertyChange(() => Height);
			}
		}
	}

	[JsonProperty("weight")]
	public double? Weight
	{
		get
		{
			return weight;
		}
		set
		{
			if (weight != value)
			{
				weight = value;
				NotifyOfPropertyChange(() => Weight);
			}
		}
	}

	[JsonProperty("timezone1")]
	public string Timezone1
	{
		get
		{
			return timezone1;
		}
		set
		{
			if (timezone1 != value)
			{
				timezone1 = value;
				NotifyOfPropertyChange(() => Timezone1);
			}
		}
	}

	[JsonProperty("timezone2")]
	public string Timezone2
	{
		get
		{
			return timezone2;
		}
		set
		{
			if (timezone2 != value)
			{
				timezone2 = value;
				NotifyOfPropertyChange(() => Timezone2);
			}
		}
	}

	[JsonProperty("timezone3")]
	public string Timezone3
	{
		get
		{
			return timezone3;
		}
		set
		{
			if (timezone3 != value)
			{
				timezone3 = value;
				NotifyOfPropertyChange(() => Timezone3);
			}
		}
	}

	[JsonProperty("timezone4")]
	public string Timezone4
	{
		get
		{
			return timezone4;
		}
		set
		{
			if (timezone4 != value)
			{
				timezone4 = value;
				NotifyOfPropertyChange(() => Timezone4);
			}
		}
	}

	[JsonProperty("timezone5")]
	public string Timezone5
	{
		get
		{
			return timezone5;
		}
		set
		{
			if (timezone5 != value)
			{
				timezone5 = value;
				NotifyOfPropertyChange(() => Timezone5);
			}
		}
	}

	[JsonProperty("timezone6")]
	public string Timezone6
	{
		get
		{
			return timezone6;
		}
		set
		{
			if (timezone6 != value)
			{
				timezone6 = value;
				NotifyOfPropertyChange(() => Timezone6);
			}
		}
	}

	[JsonProperty("timezone7")]
	public string Timezone7
	{
		get
		{
			return timezone7;
		}
		set
		{
			if (timezone7 != value)
			{
				timezone7 = value;
				NotifyOfPropertyChange(() => Timezone7);
			}
		}
	}

	[JsonProperty("timezone8")]
	public string Timezone8
	{
		get
		{
			return timezone8;
		}
		set
		{
			if (timezone8 != value)
			{
				timezone8 = value;
				NotifyOfPropertyChange(() => Timezone8);
			}
		}
	}

	[JsonProperty("backupCode")]
	public string BackupCode
	{
		get
		{
			return backupCode;
		}
		set
		{
			if (backupCode != value)
			{
				backupCode = value;
				NotifyOfPropertyChange(() => BackupCode);
			}
		}
	}

	[JsonProperty("hospitalid")]
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
}

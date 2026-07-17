using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class UpdateIgUserModel : ModelBase
{
	private string name;

	private string nameKana;

	private DateTime? birthday;

	private string sex;

	private double? height;

	private double? weight;

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
}

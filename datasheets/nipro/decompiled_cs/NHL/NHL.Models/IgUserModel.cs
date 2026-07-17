using System;
using System.Linq;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class IgUserModel : ModelBase
{
	private static readonly TimeSpan TIMEZONE_A = new TimeSpan(6, 0, 0);

	private static readonly TimeSpan TIMEZONE_B = new TimeSpan(7, 0, 0);

	private static readonly TimeSpan TIMEZONE_C = new TimeSpan(12, 0, 0);

	private static readonly TimeSpan TIMEZONE_D = new TimeSpan(18, 0, 0);

	private static readonly TimeSpan TIMEZONE_E = new TimeSpan(23, 0, 0);

	private static readonly TimeSpan TIMEZONE_OTHER_A = new TimeSpan(0, 0, 0);

	private static readonly TimeSpan TIMEZONE_NIGHT = new TimeSpan(12, 0, 0);

	private string id;

	private string name;

	private string nameKana;

	private DateTime? birthday;

	private string sex;

	private double? height;

	private double? weight;

	private TimeSpan? timezone1;

	private TimeSpan? timezone2;

	private TimeSpan? timezone3;

	private TimeSpan? timezone4;

	private TimeSpan? timezone5;

	private TimeSpan? timezone6;

	private TimeSpan? timezone7;

	private TimeSpan? timezone8;

	private int? glucoseHighValue;

	private int? glucoseLittleHighValue;

	private int? glucoseLittleLowValue;

	private int? glucoseLowValue;

	private string backupCode;

	private string backupPassword;

	private DateTime? createdAt;

	private DateTime? updatedAt;

	private bool deleted;

	private string version;

	private TimeSpan? timezoneOther;

	private TimeSpan? timezoneNight;

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
	public TimeSpan? Timezone1
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
	public TimeSpan? Timezone2
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
	public TimeSpan? Timezone3
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
	public TimeSpan? Timezone4
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
	public TimeSpan? Timezone5
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
	public TimeSpan? Timezone6
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
	public TimeSpan? Timezone7
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
	public TimeSpan? Timezone8
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

	[JsonProperty("glucoseHighValue")]
	public int? GlucoseHighValue
	{
		get
		{
			return glucoseHighValue;
		}
		set
		{
			glucoseHighValue = value;
			NotifyOfPropertyChange(() => GlucoseHighValue);
		}
	}

	[JsonProperty("glucoseLittleHighValue")]
	public int? GlucoseLittleHighValue
	{
		get
		{
			return glucoseLittleHighValue;
		}
		set
		{
			glucoseLittleHighValue = value;
			NotifyOfPropertyChange(() => GlucoseLittleHighValue);
		}
	}

	[JsonProperty("glucoseLittleLowValue")]
	public int? GlucoseLittleLowValue
	{
		get
		{
			return glucoseLittleLowValue;
		}
		set
		{
			glucoseLittleLowValue = value;
			NotifyOfPropertyChange(() => GlucoseLittleLowValue);
		}
	}

	[JsonProperty("glucoseLowValue")]
	public int? GlucoseLowValue
	{
		get
		{
			return glucoseLowValue;
		}
		set
		{
			glucoseLowValue = value;
			NotifyOfPropertyChange(() => GlucoseLowValue);
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

	[JsonProperty("backupPassword")]
	public string BackupPassword
	{
		get
		{
			return backupPassword;
		}
		set
		{
			if (backupPassword != value)
			{
				backupPassword = value;
				NotifyOfPropertyChange(() => BackupPassword);
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

	[JsonProperty("timezoneOther")]
	public TimeSpan? TimezoneOther
	{
		get
		{
			return timezoneOther;
		}
		set
		{
			if (timezoneOther != value)
			{
				timezoneOther = value;
				NotifyOfPropertyChange(() => TimezoneOther);
			}
		}
	}

	[JsonProperty("timezoneNight")]
	public TimeSpan? TimezoneNight
	{
		get
		{
			return timezoneNight;
		}
		set
		{
			if (timezoneNight != value)
			{
				timezoneNight = value;
				NotifyOfPropertyChange(() => TimezoneNight);
			}
		}
	}

	public IgUserModel()
	{
		Name = string.Empty;
		NameKana = string.Empty;
		Birthday = null;
		Sex = string.Empty;
		Height = null;
		Weight = null;
		TimeSpan tIMEZONE_A = TIMEZONE_A;
		Timezone1 = new TimeSpan(tIMEZONE_A.Ticks);
		tIMEZONE_A = TIMEZONE_B;
		Timezone2 = new TimeSpan(tIMEZONE_A.Ticks);
		tIMEZONE_A = TIMEZONE_C;
		Timezone4 = new TimeSpan(tIMEZONE_A.Ticks);
		tIMEZONE_A = TIMEZONE_D;
		Timezone6 = new TimeSpan(tIMEZONE_A.Ticks);
		tIMEZONE_A = TIMEZONE_E;
		Timezone8 = new TimeSpan(tIMEZONE_A.Ticks);
		tIMEZONE_A = TIMEZONE_OTHER_A;
		TimezoneOther = new TimeSpan(tIMEZONE_A.Ticks);
		tIMEZONE_A = TIMEZONE_NIGHT;
		TimezoneNight = new TimeSpan(tIMEZONE_A.Ticks);
		TimeSpan?[] source = new TimeSpan?[2]
		{
			Timezone2?.Add(new TimeSpan(2, 0, 0)),
			Timezone4
		};
		Timezone3 = source.Min();
		TimeSpan?[] source2 = new TimeSpan?[2]
		{
			Timezone4?.Add(new TimeSpan(2, 0, 0)),
			Timezone6
		};
		Timezone5 = source2.Min();
		TimeSpan?[] source3 = new TimeSpan?[2]
		{
			Timezone6?.Add(new TimeSpan(2, 0, 0)),
			Timezone8
		};
		Timezone7 = source3.Min();
		GlucoseHighValue = null;
		GlucoseLittleHighValue = null;
		GlucoseLittleLowValue = null;
		GlucoseLowValue = null;
	}
}

using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class IgUserRequestModel : ModelBase
{
	private string _Name;

	private string _NameKane;

	private DateTime? _Birthday;

	private string _Sex;

	private double? _Height;

	private double? _Weight;

	private TimeSpan? _Timezone1;

	private TimeSpan? _Timezone2;

	private TimeSpan? _Timezone3;

	private TimeSpan? _Timezone4;

	private TimeSpan? _Timezone5;

	private TimeSpan? _Timezone6;

	private TimeSpan? _Timezone7;

	private TimeSpan? _Timezone8;

	private int? _GlucoseHighValue;

	private int? _GlucoseLittleHighValue;

	private int? _GlucoseLittleLowValue;

	private int? _GlucoseLowValue;

	private string _BackupCode;

	private string _BackupPassword;

	private TimeSpan? _TimezoneOther;

	private TimeSpan? _TimezoneNight;

	[JsonProperty("name")]
	public string Name
	{
		get
		{
			return _Name;
		}
		set
		{
			_Name = value;
			NotifyOfPropertyChange(() => Name);
		}
	}

	[JsonProperty("nameKana")]
	public string NameKane
	{
		get
		{
			return _NameKane;
		}
		set
		{
			_NameKane = value;
			NotifyOfPropertyChange(() => NameKane);
		}
	}

	[JsonProperty("birthday")]
	public DateTime? Birthday
	{
		get
		{
			return _Birthday;
		}
		set
		{
			_Birthday = value;
			NotifyOfPropertyChange(() => Birthday);
		}
	}

	[JsonProperty("sex")]
	public string Sex
	{
		get
		{
			return _Sex;
		}
		set
		{
			_Sex = value;
			NotifyOfPropertyChange(() => Sex);
		}
	}

	[JsonProperty("height")]
	public double? Height
	{
		get
		{
			return _Height;
		}
		set
		{
			_Height = value;
			NotifyOfPropertyChange(() => Height);
		}
	}

	[JsonProperty("weight")]
	public double? Weight
	{
		get
		{
			return _Weight;
		}
		set
		{
			_Weight = value;
			NotifyOfPropertyChange(() => Weight);
		}
	}

	[JsonProperty("timezone1")]
	public TimeSpan? Timezone1
	{
		get
		{
			return _Timezone1;
		}
		set
		{
			_Timezone1 = value;
			NotifyOfPropertyChange(() => Timezone1);
		}
	}

	[JsonProperty("timezone2")]
	public TimeSpan? Timezone2
	{
		get
		{
			return _Timezone2;
		}
		set
		{
			_Timezone2 = value;
			NotifyOfPropertyChange(() => Timezone2);
		}
	}

	[JsonProperty("timezone3")]
	public TimeSpan? Timezone3
	{
		get
		{
			return _Timezone3;
		}
		set
		{
			_Timezone3 = value;
			NotifyOfPropertyChange(() => Timezone3);
		}
	}

	[JsonProperty("timezone4")]
	public TimeSpan? Timezone4
	{
		get
		{
			return _Timezone4;
		}
		set
		{
			_Timezone4 = value;
			NotifyOfPropertyChange(() => Timezone4);
		}
	}

	[JsonProperty("timezone5")]
	public TimeSpan? Timezone5
	{
		get
		{
			return _Timezone5;
		}
		set
		{
			_Timezone5 = value;
			NotifyOfPropertyChange(() => Timezone5);
		}
	}

	[JsonProperty("timezone6")]
	public TimeSpan? Timezone6
	{
		get
		{
			return _Timezone6;
		}
		set
		{
			_Timezone6 = value;
			NotifyOfPropertyChange(() => Timezone6);
		}
	}

	[JsonProperty("timezone7")]
	public TimeSpan? Timezone7
	{
		get
		{
			return _Timezone7;
		}
		set
		{
			_Timezone7 = value;
			NotifyOfPropertyChange(() => Timezone7);
		}
	}

	[JsonProperty("timezone8")]
	public TimeSpan? Timezone8
	{
		get
		{
			return _Timezone8;
		}
		set
		{
			_Timezone8 = value;
			NotifyOfPropertyChange(() => Timezone8);
		}
	}

	[JsonProperty("glucoseHighValue")]
	public int? GlucoseHighValue
	{
		get
		{
			return _GlucoseHighValue;
		}
		set
		{
			_GlucoseHighValue = value;
			NotifyOfPropertyChange(() => GlucoseHighValue);
		}
	}

	[JsonProperty("glucoseLittleHighValue")]
	public int? GlucoseLittleHighValue
	{
		get
		{
			return _GlucoseLittleHighValue;
		}
		set
		{
			_GlucoseLittleHighValue = value;
			NotifyOfPropertyChange(() => GlucoseLittleHighValue);
		}
	}

	[JsonProperty("glucoseLittleLowValue")]
	public int? GlucoseLittleLowValue
	{
		get
		{
			return _GlucoseLittleLowValue;
		}
		set
		{
			_GlucoseLittleLowValue = value;
			NotifyOfPropertyChange(() => GlucoseLittleLowValue);
		}
	}

	[JsonProperty("glucoseLowValue")]
	public int? GlucoseLowValue
	{
		get
		{
			return _GlucoseLowValue;
		}
		set
		{
			_GlucoseLowValue = value;
			NotifyOfPropertyChange(() => GlucoseLowValue);
		}
	}

	[JsonProperty("backupCode")]
	public string BackupCode
	{
		get
		{
			return _BackupCode;
		}
		set
		{
			_BackupCode = value;
			NotifyOfPropertyChange(() => BackupCode);
		}
	}

	[JsonProperty("backupPassword")]
	public string BackupPassword
	{
		get
		{
			return _BackupCode;
		}
		set
		{
			_BackupPassword = value;
			NotifyOfPropertyChange(() => BackupPassword);
		}
	}

	[JsonProperty("timezoneOther")]
	public TimeSpan? TimezoneOther
	{
		get
		{
			return _TimezoneOther;
		}
		set
		{
			_TimezoneOther = value;
			NotifyOfPropertyChange(() => TimezoneOther);
		}
	}

	[JsonProperty("timezoneNight")]
	public TimeSpan? TimezoneNight
	{
		get
		{
			return _TimezoneNight;
		}
		set
		{
			_TimezoneNight = value;
			NotifyOfPropertyChange(() => TimezoneNight);
		}
	}
}

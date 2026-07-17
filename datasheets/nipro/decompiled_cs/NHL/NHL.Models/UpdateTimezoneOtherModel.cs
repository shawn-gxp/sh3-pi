using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class UpdateTimezoneOtherModel : ModelBase
{
	private TimeSpan? timezoneOther;

	private TimeSpan? timezoneNight;

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
}

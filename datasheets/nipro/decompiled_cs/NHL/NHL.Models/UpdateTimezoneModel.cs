using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class UpdateTimezoneModel : ModelBase
{
	private TimeSpan? timezone1;

	private TimeSpan? timezone2;

	private TimeSpan? timezone3;

	private TimeSpan? timezone4;

	private TimeSpan? timezone5;

	private TimeSpan? timezone6;

	private TimeSpan? timezone7;

	private TimeSpan? timezone8;

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
}

using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class DisplayGluChartDataModel : ModelBase
{
	private string date;

	private decimal beforeBreakFast;

	private decimal afterBreakFast;

	private decimal beforeLunch;

	private decimal afterLunch;

	private decimal beforeDinner;

	private decimal afterDinner;

	private decimal bedTime;

	private decimal night;

	private decimal defaultValue = 100m;

	[DataMember]
	public string Date
	{
		get
		{
			return date;
		}
		set
		{
			if (date != value)
			{
				date = value;
				NotifyOfPropertyChange(() => Date);
			}
		}
	}

	[DataMember]
	public decimal BeforeBreakFast
	{
		get
		{
			return beforeBreakFast;
		}
		set
		{
			if (beforeBreakFast != value)
			{
				beforeBreakFast = value;
				NotifyOfPropertyChange(() => BeforeBreakFast);
			}
		}
	}

	[DataMember]
	public decimal AfterBreakFast
	{
		get
		{
			return afterBreakFast;
		}
		set
		{
			if (afterBreakFast != value)
			{
				afterBreakFast = value;
				NotifyOfPropertyChange(() => AfterBreakFast);
			}
		}
	}

	[DataMember]
	public decimal BeforeLunch
	{
		get
		{
			return beforeLunch;
		}
		set
		{
			if (beforeLunch != value)
			{
				beforeLunch = value;
				NotifyOfPropertyChange(() => BeforeLunch);
			}
		}
	}

	[DataMember]
	public decimal AfterLunch
	{
		get
		{
			return afterLunch;
		}
		set
		{
			if (afterLunch != value)
			{
				afterLunch = value;
				NotifyOfPropertyChange(() => AfterLunch);
			}
		}
	}

	[DataMember]
	public decimal BeforeDinner
	{
		get
		{
			return beforeDinner;
		}
		set
		{
			if (beforeDinner != value)
			{
				beforeDinner = value;
				NotifyOfPropertyChange(() => BeforeDinner);
			}
		}
	}

	public decimal AfterDinner
	{
		get
		{
			return afterDinner;
		}
		set
		{
			if (afterDinner != value)
			{
				afterDinner = value;
				NotifyOfPropertyChange(() => AfterDinner);
			}
		}
	}

	public decimal BedTime
	{
		get
		{
			return bedTime;
		}
		set
		{
			if (bedTime != value)
			{
				bedTime = value;
				NotifyOfPropertyChange(() => BedTime);
			}
		}
	}

	public decimal Night
	{
		get
		{
			return night;
		}
		set
		{
			if (night != value)
			{
				night = value;
				NotifyOfPropertyChange(() => Night);
			}
		}
	}

	public decimal DefaultValue
	{
		get
		{
			return defaultValue;
		}
		set
		{
			if (defaultValue != value)
			{
				defaultValue = value;
				NotifyOfPropertyChange(() => DefaultValue);
			}
		}
	}
}

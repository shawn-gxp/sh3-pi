using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class GlucoseTargetThresholdModel : ModelBase
{
	private int? _HighValue;

	private int? _LittleHighValue;

	private int? _LittleLowValue;

	private int? _LowValue;

	[JsonProperty("highValue")]
	public int? HighValue
	{
		get
		{
			return _HighValue;
		}
		set
		{
			_HighValue = value;
			NotifyOfPropertyChange(() => HighValue);
		}
	}

	[JsonProperty("littleHighValue")]
	public int? LittleHighValue
	{
		get
		{
			return _LittleHighValue;
		}
		set
		{
			_LittleHighValue = value;
			NotifyOfPropertyChange(() => LittleHighValue);
		}
	}

	[JsonProperty("littleLowValue")]
	public int? LittleLowValue
	{
		get
		{
			return _LittleLowValue;
		}
		set
		{
			_LittleLowValue = value;
			NotifyOfPropertyChange(() => LittleLowValue);
		}
	}

	[JsonProperty("lowValue")]
	public int? LowValue
	{
		get
		{
			return _LowValue;
		}
		set
		{
			_LowValue = value;
			NotifyOfPropertyChange(() => LowValue);
		}
	}
}

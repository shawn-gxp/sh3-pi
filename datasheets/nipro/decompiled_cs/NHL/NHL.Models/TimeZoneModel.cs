using NHL.Models.Support;

namespace NHL.Models;

public class TimeZoneModel : ModelBase
{
	private string code;

	private string name;

	public string Code
	{
		get
		{
			return code;
		}
		set
		{
			if (code != value)
			{
				code = value;
				NotifyOfPropertyChange(() => Code);
			}
		}
	}

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
}

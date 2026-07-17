using NHL.Models.Support;

namespace NHL.Models;

public class ColorInfoModel : ModelBase
{
	private string _colorCode;

	private string _name;

	public string ColorCode
	{
		get
		{
			return _colorCode;
		}
		set
		{
			Set(ref _colorCode, value, "ColorCode");
		}
	}

	public string Name
	{
		get
		{
			return _name;
		}
		set
		{
			Set(ref _name, value, "Name");
		}
	}
}

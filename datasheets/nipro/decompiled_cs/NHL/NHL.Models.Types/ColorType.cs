using System.Collections.Generic;
using NHL.Models.Types.Support;

namespace NHL.Models.Types;

public class ColorType : CodeNameEnumBase
{
	private static ColorType _unknown = new ColorType("0", "白", 0);

	private static ColorType _green = new ColorType("1", "緑", 1);

	private static ColorType _lightBlue = new ColorType("2", "水色", 2);

	private static ColorType _yellow = new ColorType("3", "黄", 3);

	private static ColorType _orange = new ColorType("4", "橙", 4);

	private static ColorType _purple = new ColorType("5", "紫", 5);

	private static Dictionary<string, ColorType> _instances = new Dictionary<string, ColorType>
	{
		{ UNKNOWN.Code, UNKNOWN },
		{ GREEN.Code, GREEN },
		{ LIGHT_BLUE.Code, LIGHT_BLUE },
		{ YELLOW.Code, YELLOW },
		{ ORANGE.Code, ORANGE },
		{ PURPLE.Code, PURPLE }
	};

	public static ColorType UNKNOWN => _unknown;

	public static ColorType GREEN => _green;

	public static ColorType LIGHT_BLUE => _lightBlue;

	public static ColorType YELLOW => _yellow;

	public static ColorType ORANGE => _orange;

	public static ColorType PURPLE => _purple;

	public int ColorValue { get; set; }

	private ColorType(string newCode, string newName, int colorValue)
		: base(newCode, newName)
	{
		ColorValue = colorValue;
	}

	public static ColorType GetInstance(string code)
	{
		if (!_instances.ContainsKey(code))
		{
			return UNKNOWN;
		}
		return _instances[code];
	}
}

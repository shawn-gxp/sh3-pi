using System.Collections.Generic;
using NHL.Models.Types.Support;

namespace NHL.Models.Types;

public class ShareSettingType : CodeNameEnumBase
{
	private static readonly ShareSettingType Unknown = new ShareSettingType("", "");

	private static readonly ShareSettingType String = new ShareSettingType("string", "文字列");

	private static readonly ShareSettingType Number = new ShareSettingType("number", "数値");

	private static readonly ShareSettingType Date = new ShareSettingType("date", "日付");

	private static readonly ShareSettingType Selected = new ShareSettingType("selected", "選択");

	private static readonly ShareSettingType SelectedMultiple = new ShareSettingType("selectedMultiple", "複数選択");

	private static Dictionary<string, ShareSettingType> _instances = new Dictionary<string, ShareSettingType>
	{
		{ UNKNOWN.Code, UNKNOWN },
		{ STRING.Code, STRING },
		{ NUMBER.Code, NUMBER },
		{ DATE.Code, DATE },
		{ SELECTED.Code, SELECTED },
		{ SELECTED_MULTIPLE.Code, SELECTED_MULTIPLE }
	};

	public static ShareSettingType UNKNOWN => Unknown;

	public static ShareSettingType STRING => String;

	public static ShareSettingType NUMBER => Number;

	public static ShareSettingType DATE => Date;

	public static ShareSettingType SELECTED => Selected;

	public static ShareSettingType SELECTED_MULTIPLE => SelectedMultiple;

	private ShareSettingType(string newCode, string newName)
		: base(newCode, newName)
	{
	}

	public static ShareSettingType GetInstance(string code)
	{
		if (code == null || !_instances.ContainsKey(code))
		{
			return UNKNOWN;
		}
		return _instances[code];
	}
}

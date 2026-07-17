using System.Linq;

namespace BLELib.Common;

public sealed class StringUtils
{
	public static string ToStringFromBytes(byte[] bytes)
	{
		string text = string.Empty;
		if (bytes == null || bytes.Count() == 0)
		{
			return text;
		}
		for (int i = 0; i < bytes.Length; i++)
		{
			string text2 = text;
			char c = (char)bytes[i];
			text = text2 + c;
		}
		return text;
	}
}

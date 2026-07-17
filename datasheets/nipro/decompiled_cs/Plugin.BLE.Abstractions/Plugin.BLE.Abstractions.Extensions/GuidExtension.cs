using System;

namespace Plugin.BLE.Abstractions.Extensions;

public static class GuidExtension
{
	public static Guid UuidFromPartial(this int partial)
	{
		string text = partial.ToString("X").PadRight(4, '0');
		if (text.Length == 4)
		{
			text = "0000" + text + "-0000-1000-8000-00805f9b34fb";
		}
		return Guid.ParseExact(text, "d");
	}

	public static string PartialFromUuid(this Guid uuid)
	{
		string text = uuid.ToString();
		if (text.Length > 8)
		{
			text = text.Substring(4, 4);
		}
		return "0x" + text;
	}

	public static string ToHexString(this byte[] bytes)
	{
		if (bytes == null)
		{
			return string.Empty;
		}
		return BitConverter.ToString(bytes);
	}
}

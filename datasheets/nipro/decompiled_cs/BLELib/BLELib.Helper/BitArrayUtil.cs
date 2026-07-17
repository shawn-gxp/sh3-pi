using System;
using System.Collections;
using System.Collections.Generic;

namespace BLELib.Helper;

public static class BitArrayUtil
{
	public static string JoinString(this BitArray values, string separator)
	{
		List<string> list = new List<string>();
		foreach (object value in values)
		{
			list.Add(Convert.ToInt32(value).ToString());
		}
		return string.Join(separator, list);
	}
}

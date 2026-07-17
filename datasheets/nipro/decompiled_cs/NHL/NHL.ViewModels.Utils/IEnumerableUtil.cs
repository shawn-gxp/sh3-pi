using System;
using System.Collections.Generic;

namespace NHL.ViewModels.Utils;

public static class IEnumerableUtil
{
	public static string JoinString<T>(this IEnumerable<T> values, string separator, Func<T, string> converter = null)
	{
		if (converter != null)
		{
			List<string> list = new List<string>();
			foreach (T value in values)
			{
				list.Add(converter(value));
			}
			return string.Join(separator, list);
		}
		return string.Join(separator, values);
	}
}

using System;

namespace NHL.ViewModels.Utils;

public class DateTimeUtils
{
	public static DateTime CreateUtcTime(DateTime dateTime)
	{
		return new DateTime(dateTime.Year, dateTime.Month, dateTime.Day, dateTime.Hour, dateTime.Minute, dateTime.Second, DateTimeKind.Utc);
	}
}

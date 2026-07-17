using System;
using System.Globalization;
using Xamarin.Forms;

namespace NHL.Views.Converters;

public class BirthDayToBisibleConverter : IValueConverter
{
	public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
	{
		DateTime? dateTime = value as DateTime?;
		if (!dateTime.HasValue || dateTime == DateTime.MinValue)
		{
			return true;
		}
		return false;
	}

	public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
	{
		throw new NotImplementedException();
	}
}

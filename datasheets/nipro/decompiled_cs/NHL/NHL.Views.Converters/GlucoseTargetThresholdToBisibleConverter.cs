using System;
using System.Globalization;
using Xamarin.Forms;

namespace NHL.Views.Converters;

public class GlucoseTargetThresholdToBisibleConverter : IValueConverter
{
	public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
	{
		if (value == null)
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

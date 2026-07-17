using System;
using System.Globalization;
using NHL.Common;
using Xamarin.Forms;

namespace NHL.Views.Converters;

public class PresentedMenuOpenColorConverter : IValueConverter
{
	public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
	{
		bool? flag = value as bool?;
		if (flag == true && Device.RuntimePlatform == "iOS" && Device.Idiom == TargetIdiom.Tablet && !NHL.Common.Common.GetCurrentOrientationIsPortrait())
		{
			return false;
		}
		return flag;
	}

	public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
	{
		throw new NotImplementedException();
	}
}

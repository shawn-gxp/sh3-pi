using System;
using System.Globalization;
using System.IO;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Views.Converters;

public class Base64ToImageSourceConverter : IValueConverter
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
	{
		if (value == null || string.IsNullOrEmpty(value.ToString().Trim()))
		{
			return null;
		}
		try
		{
			byte[] bytes = System.Convert.FromBase64String(value.ToString().Trim());
			return ImageSource.FromStream(() => new MemoryStream(bytes));
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【Base64ToImageSourceConverter】【Convert】例外発生：{ex}");
			return null;
		}
	}

	public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
	{
		throw new NotSupportedException();
	}
}

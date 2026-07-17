using Android.App;
using Android.Util;
using NHL.Services.DependencyService;

namespace NHL.Droid.DependencyService;

public class CurrentOrientationService : ICurrentOrientationService
{
	public bool GetCurrentOrientationIsPortrait()
	{
		double num = 0.0;
		DisplayMetrics displayMetrics = Android.App.Application.Context.Resources.DisplayMetrics;
		num = (float)displayMetrics.HeightPixels / displayMetrics.Density;
		if ((double)((float)displayMetrics.WidthPixels / displayMetrics.Density) < num)
		{
			return true;
		}
		return false;
	}
}

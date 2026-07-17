using Android.App;
using Android.Content.PM;
using AndroidX.Core.App;
using AndroidX.Core.Content;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class CheckPermissionService : ICheckPermissionService
{
	public static readonly int REQUEST_LOCATION_ID;

	public void CheckLocationPermission()
	{
		bool num = ContextCompat.CheckSelfPermission(Android.App.Application.Context, "android.permission.ACCESS_COARSE_LOCATION") == Permission.Granted;
		bool flag = ContextCompat.CheckSelfPermission(Android.App.Application.Context, "android.permission.ACCESS_FINE_LOCATION") == Permission.Granted;
		if (!num || !flag)
		{
			RequestLocationPermission();
		}
	}

	public void RequestLocationPermission()
	{
		if (Forms.Context is Activity activity)
		{
			ActivityCompat.RequestPermissions(activity, new string[2] { "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION" }, REQUEST_LOCATION_ID);
		}
	}
}

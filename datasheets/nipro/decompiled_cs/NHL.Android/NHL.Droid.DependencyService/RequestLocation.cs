using Android.App;
using Android.Gms.Location;
using Android.Locations;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class RequestLocation : IRequestLocation
{
	public bool IsLocationServiceEnabled()
	{
		LocationManager obj = (LocationManager)Forms.Context.GetSystemService("location");
		bool flag = obj?.IsProviderEnabled("gps") ?? false;
		bool flag2 = obj?.IsProviderEnabled("network") ?? false;
		return flag || flag2;
	}

	public void Request()
	{
		if (!IsLocationServiceEnabled())
		{
			LocationRequest locationRequest = LocationRequest.Create();
			locationRequest.SetInterval(10L);
			locationRequest.SetSmallestDisplacement(10f);
			locationRequest.SetFastestInterval(10L);
			locationRequest.SetPriority(100);
			LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
			builder.AddLocationRequest(locationRequest);
			builder.SetAlwaysShow(p0: true);
			LocationServices.GetSettingsClient(Android.App.Application.Context).CheckLocationSettings(builder.Build()).AddOnCompleteListener(new OnCompleteListener());
		}
	}
}

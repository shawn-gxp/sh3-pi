using Android.App;
using Android.Provider;
using NHL.Services.DependencyService;

namespace NHL.Droid.DependencyService;

public class IdentifierForVendorService : IIdentifierForVendorService
{
	public string GetIdentifierForVendor()
	{
		return Settings.Secure.GetString(Android.App.Application.Context.ContentResolver, "android_id");
	}
}

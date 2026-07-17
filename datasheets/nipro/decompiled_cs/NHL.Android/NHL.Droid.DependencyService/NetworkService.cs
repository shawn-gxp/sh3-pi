using Android.Net;
using Android.OS;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class NetworkService : INetworkService
{
	public bool IsWifiUsed()
	{
		ConnectivityManager connectivityManager = Forms.Context.GetSystemService("connectivity") as ConnectivityManager;
		if (Build.VERSION.SdkInt >= BuildVersionCodes.M)
		{
			Network activeNetwork = connectivityManager.ActiveNetwork;
			NetworkCapabilities networkCapabilities = connectivityManager.GetNetworkCapabilities(activeNetwork);
			if (networkCapabilities != null && networkCapabilities.HasTransport(TransportType.Wifi))
			{
				return true;
			}
		}
		else
		{
			NetworkInfo activeNetworkInfo = connectivityManager.ActiveNetworkInfo;
			if (activeNetworkInfo != null && activeNetworkInfo.IsConnected && connectivityManager.ActiveNetworkInfo.Type == ConnectivityType.Wifi)
			{
				return true;
			}
		}
		return false;
	}
}

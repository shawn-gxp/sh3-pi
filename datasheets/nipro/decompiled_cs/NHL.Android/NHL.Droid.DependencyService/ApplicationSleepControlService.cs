using Android.App;
using Android.Views;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class ApplicationSleepControlService : IApplicationSleepControlService
{
	public void SleepDisabled(bool disabled = true)
	{
		Window window = (Forms.Context as Activity).Window;
		if (disabled)
		{
			window.ClearFlags(WindowManagerFlags.ShowWhenLocked);
			window.AddFlags(WindowManagerFlags.KeepScreenOn);
		}
		else
		{
			window.ClearFlags(WindowManagerFlags.KeepScreenOn);
			window.AddFlags(WindowManagerFlags.ShowWhenLocked);
		}
	}
}

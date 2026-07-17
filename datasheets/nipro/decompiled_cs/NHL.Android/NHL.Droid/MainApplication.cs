using System;
using Android.App;
using Android.OS;
using Android.Runtime;
using Java.Interop;
using Plugin.CurrentActivity;

namespace NHL.Droid;

[Application(Debuggable = false)]
public class MainApplication : Application, Android.App.Application.IActivityLifecycleCallbacks, IJavaObject, IDisposable, IJavaPeerable
{
	public MainApplication(IntPtr handle, JniHandleOwnership transer)
		: base(handle, transer)
	{
	}

	public override void OnCreate()
	{
		base.OnCreate();
		RegisterActivityLifecycleCallbacks(this);
	}

	public override void OnTerminate()
	{
		base.OnTerminate();
		UnregisterActivityLifecycleCallbacks(this);
	}

	public void OnActivityCreated(Activity activity, Bundle savedInstanceState)
	{
		CrossCurrentActivity.Current.Activity = activity;
	}

	public void OnActivityDestroyed(Activity activity)
	{
	}

	public void OnActivityPaused(Activity activity)
	{
	}

	public void OnActivityResumed(Activity activity)
	{
		CrossCurrentActivity.Current.Activity = activity;
	}

	public void OnActivitySaveInstanceState(Activity activity, Bundle outState)
	{
	}

	public void OnActivityStarted(Activity activity)
	{
		CrossCurrentActivity.Current.Activity = activity;
	}

	public void OnActivityStopped(Activity activity)
	{
	}
}

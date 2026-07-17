using System;
using Android.App;
using Android.Content;
using Android.Gms.Common.Apis;
using Android.Gms.Tasks;
using Android.Runtime;
using Caliburn.Micro;
using Java.Interop;
using Java.Lang;
using NHL.ViewModels.Event;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

internal class OnCompleteListener : Java.Lang.Object, IOnCompleteListener, IJavaObject, IDisposable, IJavaPeerable
{
	private const int REQUEST_LOCATION_CODE = 1;

	public void OnComplete(Task task)
	{
		try
		{
			task.GetResult(Class.FromType(typeof(ApiException)));
		}
		catch (ApiException ex)
		{
			switch (ex.StatusCode)
			{
			case 6:
				try
				{
					ResolvableApiException obj = (ResolvableApiException)ex;
					MainActivity mainActivity = (MainActivity)(object)Forms.Context;
					mainActivity.ActivityResult += OnActivityResult;
					obj.StartResolutionForResult((Activity)(object)mainActivity, 1);
					break;
				}
				catch (IntentSender.SendIntentException)
				{
					break;
				}
				catch (ClassCastException)
				{
					break;
				}
			}
		}
	}

	private void OnActivityResult(int requestCode, Result resultCode, Intent data)
	{
		if (requestCode == 1)
		{
			((MainActivity)(object)Forms.Context).ActivityResult -= OnActivityResult;
			if (resultCode.Equals(Result.Canceled))
			{
				IoC.Get<IEventAggregator>().PublishOnUIThread(new LocationRequestEvent
				{
					Status = LocationRequestEvent.LocationRequestStatus.Canceled
				});
			}
			else
			{
				IoC.Get<IEventAggregator>().PublishOnUIThread(new LocationRequestEvent
				{
					Status = LocationRequestEvent.LocationRequestStatus.Completed
				});
			}
		}
	}
}

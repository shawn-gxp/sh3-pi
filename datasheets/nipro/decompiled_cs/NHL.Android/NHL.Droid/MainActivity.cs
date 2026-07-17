using System;
using System.IO;
using Android.App;
using Android.Content;
using Android.Content.PM;
using Android.Database;
using Android.OS;
using Android.Views;
using C1.Android.Core;
using Caliburn.Micro;
using Microsoft.AppCenter;
using Microsoft.AppCenter.Analytics;
using Microsoft.AppCenter.Crashes;
using Microsoft.WindowsAzure.MobileServices;
using NHL.ViewModels.Event;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.BroadcastReceivers;
using Plugin.Permissions;
using Xamarin.Essentials;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;
using ZXing.Net.Mobile.Android;

namespace NHL.Droid;

[Activity(Label = "げんきノート", Icon = "@mipmap/ic_launcher", RoundIcon = "@mipmap/ic_launcher_round", Theme = "@style/splashscreen", MainLauncher = true, ConfigurationChanges = (ConfigChanges.Orientation | ConfigChanges.ScreenSize), ScreenOrientation = ScreenOrientation.Portrait)]
public class MainActivity : FormsAppCompatActivity
{
	private BluetoothStatusBroadcastReceiver statusChangeReceiver = new BluetoothStatusBroadcastReceiver(UpdateState);

	public const int REQUEST_IMAGE_GALLERY_CODE = 100;

	public const string APPCENTER_APPID = "fdfb26d7-6bff-45a0-935d-6bb63cdeda94";

	public event Action<int, Result, Intent> ActivityResult;

	public override void OnRequestPermissionsResult(int requestCode, string[] permissions, Permission[] grantResults)
	{
		PermissionsHandler.OnRequestPermissionsResult(requestCode, permissions, grantResults);
		PermissionsImplementation.Current.OnRequestPermissionsResult(requestCode, permissions, grantResults);
		Xamarin.Essentials.Platform.OnRequestPermissionsResult(requestCode, permissions, grantResults);
		((Activity)(object)this).OnRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	protected override void OnCreate(Bundle bundle)
	{
		((Activity)(object)this).Window.RequestFeature(WindowFeatures.ActionBar);
		((ContextWrapper)(object)this).SetTheme(2131755247);
		FormsAppCompatActivity.TabLayoutResource = 2131427460;
		FormsAppCompatActivity.ToolbarResource = 2131427475;
		base.OnCreate(bundle);
		Forms.Init((Context)(object)this, bundle);
		Xamarin.Essentials.Platform.Init((Activity)(object)this, bundle);
		LicenseManager.Key = "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";
		((Context)(object)this).RegisterReceiver((BroadcastReceiver)statusChangeReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
		CurrentPlatform.Init();
		AppCenter.Start("fdfb26d7-6bff-45a0-935d-6bb63cdeda94", typeof(Analytics), typeof(Crashes));
		LoadApplication((Xamarin.Forms.Application)(object)IoC.Get<App>());
	}

	protected override void OnResume()
	{
		base.OnResume();
	}

	protected override void OnActivityResult(int requestCode, Result resultCode, Intent data)
	{
		try
		{
			this.ActivityResult(requestCode, resultCode, data);
			base.OnActivityResult(requestCode, resultCode, data);
			if (requestCode == 100 && resultCode == Result.Ok)
			{
				string selectedFilePath = GetSelectedFilePath(data);
				if (!string.IsNullOrEmpty(selectedFilePath))
				{
					FileInfo fileInfo = new FileInfo(selectedFilePath);
					ImageSource imageSource = ImageSource.FromFile(selectedFilePath);
					IoC.Get<IEventAggregator>().PublishOnUIThread(new SelectImageEvent
					{
						ImageSource = imageSource,
						Date = fileInfo.CreationTime
					});
				}
			}
		}
		catch (Exception)
		{
		}
	}

	private string GetSelectedFilePath(Intent data)
	{
		string result = string.Empty;
		ICursor cursor = null;
		try
		{
			string[] projection = new string[1] { "_data" };
			cursor = ((Context)(object)this).ContentResolver.Query(data.Data, projection, null, null, null);
			if (cursor != null && cursor.MoveToFirst())
			{
				result = cursor.GetString(0);
			}
			return result;
		}
		finally
		{
			cursor?.Close();
		}
	}

	protected override void OnPause()
	{
		base.OnPause();
		IoC.Get<App>().EnterBackground();
	}

	protected override void OnDestroy()
	{
		((Context)(object)this).UnregisterReceiver((BroadcastReceiver)statusChangeReceiver);
		base.OnDestroy();
	}

	protected override void OnRestart()
	{
		base.OnRestart();
		IoC.Get<App>().EnterForeground();
	}

	private static void UpdateState(BluetoothState state)
	{
		CentralManagerEvent.CentralManagerState status;
		switch (state)
		{
		case BluetoothState.Unavailable:
			status = CentralManagerEvent.CentralManagerState.Resetting;
			break;
		case BluetoothState.Unauthorized:
			status = CentralManagerEvent.CentralManagerState.Unauthorized;
			break;
		case BluetoothState.TurningOn:
		case BluetoothState.On:
			status = CentralManagerEvent.CentralManagerState.PoweredOn;
			break;
		case BluetoothState.TurningOff:
		case BluetoothState.Off:
			status = CentralManagerEvent.CentralManagerState.PoweredOff;
			break;
		default:
			status = CentralManagerEvent.CentralManagerState.Unknown;
			break;
		}
		IoC.Get<IEventAggregator>().PublishOnUIThread(new CentralManagerEvent
		{
			Status = status
		});
	}
}

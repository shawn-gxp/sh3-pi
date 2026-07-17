using Android.App;
using Android.Bluetooth;
using Android.Content;
using Caliburn.Micro;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class RequestBluetooth : IRequestBluetooth
{
	public const int REQUEST_ENABLE_BT = 2;

	private void OnActivityResult(int requestCode, Result resultCode, Intent data)
	{
		if (requestCode == 2)
		{
			((MainActivity)(object)Forms.Context).ActivityResult -= OnActivityResult;
			if (resultCode != Result.Ok)
			{
				IoC.Get<IEventAggregator>().PublishOnUIThread(new CentralManagerEvent
				{
					Status = CentralManagerEvent.CentralManagerState.PoweredOff
				});
			}
			else
			{
				IoC.Get<IEventAggregator>().PublishOnUIThread(new CentralManagerEvent
				{
					Status = CentralManagerEvent.CentralManagerState.PoweredOn
				});
			}
		}
	}

	public CentralManagerEvent.CentralManagerState GetState()
	{
		if (BluetoothAdapter.DefaultAdapter.IsEnabled)
		{
			return CentralManagerEvent.CentralManagerState.PoweredOn;
		}
		return CentralManagerEvent.CentralManagerState.PoweredOff;
	}

	public void Request()
	{
		if (BluetoothAdapter.DefaultAdapter.IsEnabled)
		{
			IoC.Get<IEventAggregator>().PublishOnUIThread(new CentralManagerEvent
			{
				Status = CentralManagerEvent.CentralManagerState.PoweredOn
			});
			return;
		}
		Intent intent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
		MainActivity obj = (MainActivity)(object)Forms.Context;
		obj.ActivityResult += OnActivityResult;
		((Activity)(object)obj).StartActivityForResult(intent, 2);
	}

	public void ResetCentralManager()
	{
		((MainActivity)(object)Forms.Context).ActivityResult -= OnActivityResult;
	}
}

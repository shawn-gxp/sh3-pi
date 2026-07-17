using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Essentials;

namespace NHL.ViewModels;

public class SharerIntroductionViewModel : ViewModelBase
{
	private bool _isNetworkEnabled;

	public bool IsNetworkEnabled
	{
		get
		{
			return _isNetworkEnabled;
		}
		set
		{
			_isNetworkEnabled = value;
			NotifyOfPropertyChange(() => IsNetworkEnabled);
		}
	}

	protected override async void OnActivate()
	{
		IsNetworkEnabled = Connectivity.NetworkAccess == NetworkAccess.Internet;
		if (!IsNetworkEnabled)
		{
			await Execute.OnUIThreadAsync(async delegate
			{
				await base.DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
			});
		}
		base.OnActivate();
	}

	public async Task NextView()
	{
		await Navigate<SharerRegisterViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}
}

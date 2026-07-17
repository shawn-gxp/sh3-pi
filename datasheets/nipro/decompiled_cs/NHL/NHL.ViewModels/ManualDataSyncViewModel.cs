using System;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.Services.Support;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class ManualDataSyncViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private AuthenticationModel _authenticatedUser;

	private string displayUpdatedAt;

	private const string NO_UPDATED_AT = "----";

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public AuthenticationModel AuthenticatedUser
	{
		get
		{
			return _authenticatedUser;
		}
		set
		{
			if (_authenticatedUser != value)
			{
				_authenticatedUser = value;
				NotifyOfPropertyChange(() => AuthenticatedUser);
			}
		}
	}

	public string DisplayUpdatedAt
	{
		get
		{
			return displayUpdatedAt;
		}
		set
		{
			if (displayUpdatedAt != value)
			{
				displayUpdatedAt = value;
				NotifyOfPropertyChange(() => DisplayUpdatedAt);
			}
		}
	}

	public async void ManualDataSync()
	{
		Log.Debug("【IG】【ManualDataSyncViewModel】【ManualDataSync】data sync start");
		await ExecAsync(async delegate
		{
			try
			{
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				if (AuthenticatedUser != null)
				{
					await SyncManager.GetInstance().Sync();
					await Execute.OnUIThreadAsync(async delegate
					{
						await base.DialogProvider.ShowAlert("", "同期が完了しました。");
					});
					DisplayUpdatedAt = (string)Application.Current.Properties["latestUpdatedAt"];
				}
			}
			catch (Exception ex)
			{
				await Execute.OnUIThreadAsync(async delegate
				{
					await base.DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
				Log.Error($"【IG】【ManualDataSyncViewModel】【ManualDatasync】例外発生：{ex}");
			}
		});
		Log.Debug("【IG】【ManualDataSyncViewModel】【ManualDataSync】data sync finish");
	}

	protected override void OnActivate()
	{
		Log.Debug("【IG】【ManualDataSyncViewModel】【OnActivate】OnActivate start");
		if (Application.Current.Properties.ContainsKey("latestUpdatedAt"))
		{
			DisplayUpdatedAt = (string)Application.Current.Properties["latestUpdatedAt"];
			return;
		}
		DisplayUpdatedAt = "----";
		Log.Debug("【IG】【ManualDataSyncViewModel】【OnActivate】OnActivate finish");
	}
}

using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class RestoreViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private string _BackupCode;

	private string _BackupPassword;

	private bool canRestoration;

	public new IDialogProvider DialogProvider { get; set; }

	public string BackupCode
	{
		get
		{
			return _BackupCode;
		}
		set
		{
			_BackupCode = value;
			NotifyOfPropertyChange(() => BackupCode);
		}
	}

	public string BackupPassword
	{
		get
		{
			return _BackupPassword;
		}
		set
		{
			if (_BackupPassword != value)
			{
				_BackupPassword = value;
				NotifyOfPropertyChange(() => BackupPassword);
			}
		}
	}

	public IMeasurementService MeasurementService { get; set; }

	public IPhotographService PhotographService { get; set; }

	public IDailyCommentService DailyCommentService { get; set; }

	public IUpdateIgPasswordService UpdateIgPasswordService { get; set; }

	public bool CanRestoration => canRestoration;

	public async void NextButton()
	{
		await RestorationHelper(async delegate
		{
			if (await IsValid() && await RestorationConfirmIfNoWifi())
			{
				if (!(await RestorationCore()))
				{
					await DialogProvider.ShowError("エラー", "復元でエラーが発生しました", "OK", null);
				}
				else
				{
					await Navigate<MasterDetailRootViewModel>();
					for (int num = Application.Current.MainPage.Navigation.NavigationStack.Count - 1; num >= 0; num--)
					{
						Page page = Application.Current.MainPage.Navigation.NavigationStack[num];
						if ((object)page.GetType() != typeof(MasterDetailRootViewModel))
						{
							Application.Current.MainPage.Navigation.RemovePage(page);
						}
					}
				}
			}
		});
	}

	protected async Task<bool> IsValid()
	{
		if (string.IsNullOrEmpty(BackupCode))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "バックアップコードを入力してください");
			return false;
		}
		if (string.IsNullOrEmpty(BackupPassword))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "パスワードを入力してください");
			return false;
		}
		return true;
	}

	private async Task<bool> RestorationCore()
	{
		IBusyIndicator busyIndecator = DependencyService.Get<IBusyIndicator>();
		return await Task.Run(async delegate
		{
			_ = 6;
			try
			{
				busyIndecator.IsBusy = true;
				bool num = await base.UserManager.RestoreUser(BackupCode, BackupPassword);
				busyIndecator.Message = "15%";
				if (!num)
				{
					return false;
				}
				bool num2 = await base.UserManager.Authenticate();
				busyIndecator.Message = "30%";
				if (!num2)
				{
					return false;
				}
				bool num3 = await base.UserManager.SaveUserModel();
				busyIndecator.Message = "45%";
				if (!num3)
				{
					return false;
				}
				Log.Info("【IG】【RestoreViewModel】【RestorationCore】MeasurementService.PushAsync start");
				await MeasurementService.PushAsync();
				busyIndecator.Message = "55%";
				Log.Info("【IG】【RestoreViewModel】【RestorationCore】MeasurementService PullAsync start");
				await MeasurementService.PullAsync();
				busyIndecator.Message = "70%";
				Log.Info("【IG】【RestoreViewModel】【RestorationCore】PhotographService PullAsync start");
				await PhotographService.PullAsync();
				busyIndecator.Message = "85%";
				Log.Info("【IG】【RestoreViewModel】【RestorationCore】DailyCommentService PullAsync start");
				await DailyCommentService.PullAsync();
				busyIndecator.Message = "100%";
				Log.Info("【IG】【RestoreViewModel】【RestorationCore】complete");
				return true;
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【RestoreViewModel】【RestorationCore】例外発生：{ex}");
				return false;
			}
			finally
			{
				busyIndecator.Initialize();
			}
		});
	}

	private async Task RestorationHelper(Func<Task> method)
	{
		try
		{
			ChangeCanRestoration(value: true);
			await method();
		}
		finally
		{
			ChangeCanRestoration(value: false);
		}
	}

	private async Task<bool> RestorationConfirmIfNoWifi()
	{
		if (DependencyService.Get<INetworkService>().IsWifiUsed())
		{
			return true;
		}
		return await DialogProvider.ShowAlert("確認", "データのダウンロードを行います。" + Environment.NewLine + "Wi-Fiでのダウンロードを推奨します。" + Environment.NewLine + "ダウンロードを開始しますか？", "OK", "Cancel", null);
	}

	protected override void OnActivate()
	{
		ChangeCanRestoration(value: true);
		Execute.BeginOnUIThread(async delegate
		{
			await Task.Delay(10);
			BackupPassword = null;
		});
		base.OnActivate();
	}

	private void ChangeCanRestoration(bool value)
	{
		canRestoration = false;
		NotifyOfPropertyChange(() => CanRestoration);
	}
}

using System;
using System.Threading;
using System.Threading.Tasks;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Properties;
using NHL.Services;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.ViewModels.Support;

public abstract class ViewModelBase : Conductor<IScreen>.Collection.OneActive
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private static object lockForWorkerThread = new object();

	private static SemaphoreSlim lockForWorkerThreadSemaphore = new SemaphoreSlim(1, 1);

	public INavigationService NavigationService { get; set; }

	public IEventAggregator EventAggregator { get; set; }

	public IUserManager UserManager { get; set; }

	public IDialogProvider DialogProvider { get; set; }

	public async Task Reset()
	{
		await UserManager.ResetUser();
	}

	protected virtual void RefreshCanCommand()
	{
	}

	protected async Task Navigate<T>() where T : ViewModelBase
	{
		await Execute.OnUIThreadAsync(async delegate
		{
			await NavigationService.NavigateToViewModelAsync<T>();
		});
	}

	protected async Task ExecAsync(System.Action action, bool isShowBusy = true, string indicatorMessage = null)
	{
		IBusyIndicator busyIndecator = DependencyService.Get<IBusyIndicator>();
		if (string.IsNullOrEmpty(indicatorMessage))
		{
			busyIndecator.Message = Resources.DEFAULT_WAIT_MESSAGE;
		}
		else
		{
			busyIndecator.Message = indicatorMessage;
		}
		try
		{
			System.Action action2 = delegate
			{
				lock (lockForWorkerThread)
				{
					action();
				}
			};
			busyIndecator.IsBusy = isShowBusy;
			await Task.Run(action2);
			busyIndecator.IsBusy = false;
		}
		catch (Exception ex)
		{
			busyIndecator.IsBusy = false;
			Log.Error($"【IG】【ViewModelBase】【ExecAsync】ExecAsync error: {ex}");
		}
		finally
		{
			busyIndecator.IsBusy = false;
			busyIndecator.Message = Resources.DEFAULT_WAIT_MESSAGE;
		}
	}

	protected async Task ExecAsync(Func<Task> action, bool isShowBusy = true, string indicatorMessage = null)
	{
		IBusyIndicator busyIndecator = DependencyService.Get<IBusyIndicator>();
		if (string.IsNullOrEmpty(indicatorMessage))
		{
			busyIndecator.Message = Resources.DEFAULT_WAIT_MESSAGE;
		}
		else
		{
			busyIndecator.Message = indicatorMessage;
		}
		try
		{
			Func<Task> function = async delegate
			{
				await lockForWorkerThreadSemaphore.WaitAsync().ConfigureAwait(continueOnCapturedContext: false);
				try
				{
					await action();
				}
				finally
				{
					lockForWorkerThreadSemaphore.Release();
				}
			};
			busyIndecator.IsBusy = isShowBusy;
			await Task.Run(function);
		}
		catch (Exception ex)
		{
			busyIndecator.IsBusy = false;
			Log.Error($"【IG】【ViewModelBase】【ExecAsync】ExecAsync error: {ex}");
		}
		finally
		{
			busyIndecator.IsBusy = false;
			busyIndecator.Message = Resources.DEFAULT_WAIT_MESSAGE;
		}
	}

	private async Task HandleException(Exception e)
	{
		await ShowAlert(Resources.ALERT_DIALOG_TITLE, Resources.MESSAGE_001);
	}

	protected async Task ShowAlert(string title, string message, string button = "OK")
	{
		if (GetView() is Page page)
		{
			await page.DisplayAlert(title, message, button);
		}
	}

	protected async Task ShowVersionUpMessage()
	{
		string text = "アプリのバージョンアップが可能です。" + Environment.NewLine + "最新版にアップデートしてご利用ください。";
		string message = text + Environment.NewLine + "Wi-Fiでのダウンロードを推奨します。";
		IDialogProvider dialogProvider = IoC.Get<IDialogProvider>();
		if (DependencyService.Get<INetworkService>().IsWifiUsed())
		{
			await dialogProvider.ShowAlert("情報", text, "OK", null);
		}
		else
		{
			await dialogProvider.ShowAlert("情報", message, "OK", null);
		}
	}

	protected void ShowConfirmationBeginOnUIThreadAsync(string title, string message, Action<bool> callback, string buttonText = "はい", string cancelText = "いいえ")
	{
		Execute.BeginOnUIThread(async delegate
		{
			await DialogProvider.ShowAlert(title, message, buttonText, cancelText, callback);
		});
	}
}

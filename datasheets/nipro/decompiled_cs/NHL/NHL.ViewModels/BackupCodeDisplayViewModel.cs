using System;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class BackupCodeDisplayViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private static readonly string COPIED_MESSAGE = "コピーしました";

	private static readonly string COMPLETE_MESSAGE = "登録が完了しました";

	private static readonly string PASSWORD_COMPLETE_MESSAGE = "データ復元時にはバックアップコードとパスワードの両方が必要になります。";

	private static readonly string BACKUP_CODE_MESSAGE = "データ復元時に使うパスワードを登録してください。(再発行可)";

	private static readonly string BACKUP_PASSWORD_MESSAGE = "パスワードと一緒に以下のバックアップコードを手元に保存しておいてください。";

	private static readonly string BACKUP_CODE_REGIST_MESSAGE = "ユーザー情報を登録すると、機種変更時などにデータを復元するのに利用するコードが発行できます。\n\n利用したい場合は、後ほどユーザー情報をご登録ください。";

	private string _NavigateFrom;

	private bool _IsVisibleNextButton = true;

	private bool _HasBackButton;

	private string _BackupPassword;

	public new IDialogProvider DialogProvider { get; set; }

	public IUpdateIgPasswordService UpdateIgPasswordService { get; set; }

	public string BackupCodeText
	{
		get
		{
			if (!string.IsNullOrEmpty(base.UserManager.IgUser.Name))
			{
				return BACKUP_CODE_MESSAGE;
			}
			return BACKUP_CODE_REGIST_MESSAGE;
		}
	}

	public string BackupPasswordText => BACKUP_PASSWORD_MESSAGE;

	public string PasswordCompleteText => PASSWORD_COMPLETE_MESSAGE;

	public string NavigateFrom
	{
		get
		{
			return _NavigateFrom;
		}
		set
		{
			_NavigateFrom = value;
			NotifyOfPropertyChange(() => NavigateFrom);
			if (!(_NavigateFrom != "menu"))
			{
				HasBackButton = true;
				IsVisibleNextButton = false;
			}
		}
	}

	public bool IsVisibleNextButton
	{
		get
		{
			return _IsVisibleNextButton;
		}
		set
		{
			_IsVisibleNextButton = value;
			NotifyOfPropertyChange(() => IsVisibleNextButton);
		}
	}

	public bool HasBackButton
	{
		get
		{
			return _HasBackButton;
		}
		set
		{
			_HasBackButton = value;
			NotifyOfPropertyChange(() => HasBackButton);
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
			_BackupPassword = value;
			NotifyOfPropertyChange(() => BackupPassword);
		}
	}

	public bool IsVisibleBackupCode => !string.IsNullOrEmpty(base.UserManager.IgUser.Name);

	public bool IsVisibleBackupPassword => base.UserManager.IgUser.BackupPassword != null;

	public async void NextButton()
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

	public async void PasswordButton()
	{
		if (!(await IsValid()))
		{
			BackupPassword = null;
			return;
		}
		await ExecAsync(async delegate
		{
			try
			{
				IgUserModel igUserModel = await UpdateIgPasswordService.UpdatePassword(new IgUserPasswordModel
				{
					BackupPassword = BackupPassword
				});
				base.UserManager.IgUser = igUserModel ?? throw new NullReferenceException("レスポンスがnull");
				await base.UserManager.SaveUserModel();
				BackupPassword = null;
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowAlert("", COMPLETE_MESSAGE);
				});
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【backupCodeDisplayViewModel】【Update】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
				return;
			}
			base.Refresh();
		});
	}

	protected async Task<bool> IsValid()
	{
		if (string.IsNullOrEmpty(BackupPassword))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "パスワードを入力してください");
			return false;
		}
		if (BackupPassword.Length < 4 || BackupPassword.Length > 8)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "4～8文字以内で入力してください");
			return false;
		}
		if (Regex.IsMatch(BackupPassword, "^[０-９ａ-ｚＡ-Ｚ]+$"))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "半角で入力してください");
			return false;
		}
		if (!Regex.IsMatch(BackupPassword, "^[0-9a-zA-Z]+$"))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "英数字で入力してください");
			return false;
		}
		return true;
	}

	public async void CopyButton()
	{
		DependencyService.Get<IClipBoard>().Copy(base.UserManager.IgUser.BackupCode);
		await DialogProvider.ShowAlert("", COPIED_MESSAGE);
	}
}

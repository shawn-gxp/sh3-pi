using System;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using BCrypt.Net;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class SharerRegisterViewModel : ViewModelBase
{
	private AuthenticationModel _authenticatedUser;

	private string _sharerLoginId;

	private string _sharerPassword;

	private string _checkSharerPassword;

	private string _email;

	private bool _isNetworkEnabled;

	private readonly ILoggingService _log = DependencyService.Get<ILoggingService>();

	public AuthenticationModel AuthenticatedUser
	{
		get
		{
			return _authenticatedUser;
		}
		set
		{
			_authenticatedUser = value;
			NotifyOfPropertyChange(() => AuthenticatedUser);
		}
	}

	public string SharerLoginId
	{
		get
		{
			return _sharerLoginId;
		}
		set
		{
			_sharerLoginId = value;
			NotifyOfPropertyChange(() => SharerLoginId);
		}
	}

	public string SharerPassword
	{
		get
		{
			return _sharerPassword;
		}
		set
		{
			_sharerPassword = value;
			NotifyOfPropertyChange(() => SharerPassword);
		}
	}

	public string CheckSharerPassword
	{
		get
		{
			return _checkSharerPassword;
		}
		set
		{
			_checkSharerPassword = value;
			NotifyOfPropertyChange(() => CheckSharerPassword);
		}
	}

	public string Email
	{
		get
		{
			return _email;
		}
		set
		{
			_email = value;
			NotifyOfPropertyChange(() => Email);
		}
	}

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

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public ISharerRegisterService SharerRegisterService { get; set; }

	public ISharerUpdateService SharerUpdateService { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public bool IsSharerRegistered => base.UserManager?.SharerLogin != null;

	protected override async void OnActivate()
	{
		SharerLoginId = string.Empty;
		SharerPassword = string.Empty;
		CheckSharerPassword = string.Empty;
		Email = string.Empty;
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				if (!string.IsNullOrEmpty(AuthenticatedUser?.SharerLogin?.LoginId))
				{
					SharerLoginId = AuthenticatedUser?.SharerLogin?.LoginId;
					Email = AuthenticatedUser?.SharerLogin?.Email;
				}
				base.UserManager.SharerHospital = AuthenticatedUser?.SharerHospital;
				base.UserManager.SharerLogin = AuthenticatedUser?.SharerLogin;
				NotifyOfPropertyChange(() => IsSharerRegistered);
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				SharerLoginId = base.UserManager.SharerLogin?.LoginId;
				Email = base.UserManager.SharerLogin?.Email;
				_log.Error($"【IG】【SharerInformationViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
	}

	public async void Register()
	{
		if (!(await IsValid()))
		{
			return;
		}
		SharerRegisterRequestModel req = new SharerRegisterRequestModel
		{
			SharerLoginId = SharerLoginId,
			SharerPassword = BCrypt.Net.BCrypt.HashPassword(SharerPassword),
			Email = Email
		};
		await ExecAsync(async delegate
		{
			try
			{
				SharerRegisterResponseModel sharerRegisterResponseModel = (await SharerRegisterService.RegisterSharer(req)) ?? throw new NullReferenceException("レスポンスがnull");
				base.UserManager.IgUser = sharerRegisterResponseModel.IgUser;
				base.UserManager.SharerLogin = sharerRegisterResponseModel.SharerLogin;
				base.UserManager.SharerHospital = sharerRegisterResponseModel.SharerHospital;
				await base.UserManager.SaveUserModel();
			}
			catch (Exception ex)
			{
				_log.Error($"【IG】【SharerRegisterViewModel】【Register】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
				return;
			}
			await Execute.OnUIThreadAsync(async delegate
			{
				await Navigate<SharerInformationViewModel>();
				RemoveNavigationPage(2);
			});
		});
	}

	public async void Update()
	{
		if (!(await IsValid()))
		{
			return;
		}
		SharerRegisterRequestModel req = new SharerRegisterRequestModel
		{
			SharerLoginId = SharerLoginId,
			SharerPassword = BCrypt.Net.BCrypt.HashPassword(SharerPassword),
			Email = Email
		};
		await ExecAsync(async delegate
		{
			try
			{
				SharerRegisterResponseModel sharerRegisterResponseModel = (await SharerUpdateService.UpdateSharer(req)) ?? throw new NullReferenceException("レスポンスがnull");
				base.UserManager.IgUser = sharerRegisterResponseModel.IgUser;
				base.UserManager.SharerLogin = sharerRegisterResponseModel.SharerLogin;
				base.UserManager.SharerHospital = sharerRegisterResponseModel.SharerHospital;
				await base.UserManager.SaveUserModel();
			}
			catch (Exception ex)
			{
				_log.Error($"【IG】【SharerRegisterViewModel】【Update】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
				return;
			}
			await Execute.OnUIThreadAsync(async delegate
			{
				await Navigate<SharerInformationViewModel>();
				RemoveNavigationPage(1);
			});
		});
	}

	protected async Task<bool> IsValid()
	{
		if (string.IsNullOrEmpty(SharerLoginId))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "ユーザーIDを入力してください");
			return false;
		}
		if (SharerLoginId.Length < 5 || SharerLoginId.Length > 128)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "ユーザーIDは5～128文字以内で入力してください");
			return false;
		}
		if (string.IsNullOrEmpty(SharerPassword))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "パスワードを入力してください");
			return false;
		}
		if (SharerPassword.Length < 5 || SharerPassword.Length > 128)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "パスワードは5～128文字以内で入力してください");
			return false;
		}
		if (Regex.IsMatch(SharerPassword, "^[０-９ａ-ｚＡ-Ｚ]+$"))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "パスワードは半角で入力してください");
			return false;
		}
		if (!Regex.IsMatch(SharerPassword, "^[0-9a-zA-Z]+$"))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "パスワードは英数字で入力してください");
			return false;
		}
		if (string.IsNullOrEmpty(CheckSharerPassword))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "確認用パスワードを入力してください");
			return false;
		}
		if (SharerPassword != CheckSharerPassword)
		{
			await DialogProvider.ShowAlert("入力エラー", "入力されたパスワードが違います");
			return false;
		}
		if (string.IsNullOrEmpty(Email))
		{
			return true;
		}
		if (!Regex.IsMatch(Email, "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$", RegexOptions.IgnoreCase))
		{
			await DialogProvider.ShowAlert("入力エラー", "メールアドレスを正しく入力してください");
			return false;
		}
		if (Email.Length > 100)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "メールアドレスは100文字以内で入力してください");
			return false;
		}
		return true;
	}

	private static void RemoveNavigationPage(int removePage)
	{
		int num = Application.Current.MainPage.Navigation.NavigationStack.Count - 1;
		int num2 = 0;
		while (num >= 0)
		{
			Page page = Application.Current.MainPage.Navigation.NavigationStack[num];
			if ((object)page.GetType() != typeof(MasterDetailRootViewModel))
			{
				Application.Current.MainPage.Navigation.RemovePage(page);
			}
			if (num2 < removePage)
			{
				num--;
				num2++;
				continue;
			}
			break;
		}
	}
}

using System;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class SharerInformationViewModel : ViewModelBase
{
	private static readonly string COPIED_MESSAGE = "コピーしました";

	private AuthenticationModel _authenticatedUser;

	private string _sharerHospitalCode;

	private string _sharerLoginId;

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

	public string SharerHospitalCode
	{
		get
		{
			return _sharerHospitalCode;
		}
		set
		{
			_sharerHospitalCode = value;
			NotifyOfPropertyChange(() => SharerHospitalCode);
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

	public string IgWebSharerLoginUrl => "https://note.niprogenkinote.jp/sharer/login" + ((!string.IsNullOrEmpty(SharerHospitalCode) && !string.IsNullOrEmpty(SharerLoginId)) ? ("?hospitalCode=" + SharerHospitalCode + "&username=" + Uri.EscapeDataString(SharerLoginId)) : "");

	public bool IsSharerRegistered => base.UserManager?.SharerHospital != null;

	protected override async void OnActivate()
	{
		SharerHospitalCode = string.Empty;
		SharerLoginId = string.Empty;
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				if (!string.IsNullOrEmpty(AuthenticatedUser?.SharerHospital?.HospitalCode))
				{
					SharerHospitalCode = AuthenticatedUser?.SharerHospital?.HospitalCode;
					SharerLoginId = AuthenticatedUser?.SharerLogin?.LoginId;
				}
				base.UserManager.SharerHospital = AuthenticatedUser?.SharerHospital;
				base.UserManager.SharerLogin = AuthenticatedUser?.SharerLogin;
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				SharerHospitalCode = base.UserManager.SharerHospital?.HospitalCode;
				SharerLoginId = base.UserManager.SharerLogin?.LoginId;
				_log.Error($"【IG】【SharerInformationViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await base.DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		NotifyOfPropertyChange(() => IsSharerRegistered);
		NotifyOfPropertyChange(() => IgWebSharerLoginUrl);
		base.OnActivate();
	}

	public async void Introduction()
	{
		await ExecAsync(async delegate
		{
			await Navigate<SharerIntroductionViewModel>();
		});
	}

	public async void Update()
	{
		await ExecAsync(async delegate
		{
			await Navigate<SharerRegisterViewModel>();
		});
	}

	public void OpenIgWebSharerLoginUrl()
	{
		Device.OpenUri(new Uri(IgWebSharerLoginUrl));
	}

	public async void CopyButton()
	{
		DependencyService.Get<IClipBoard>().Copy(IgWebSharerLoginUrl);
		await base.DialogProvider.ShowAlert("", COPIED_MESSAGE);
	}
}

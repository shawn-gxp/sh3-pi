using System;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class NoticeDetailViewModel : ViewModelBase
{
	private AuthenticationModel _authenticatedUser;

	private string _selectedNoticeId;

	private NoticeDetailResponseModel _noticeDetail;

	private bool _isNetworkEnabled;

	private readonly ILoggingService _log = DependencyService.Get<ILoggingService>();

	private IAuthenticatedUserService AuthenticatedUserService { get; set; }

	private INoticeService NoticeService { get; set; }

	private new IDialogProvider DialogProvider { get; set; }

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

	public string SelectedNoticeId
	{
		get
		{
			return _selectedNoticeId;
		}
		set
		{
			_selectedNoticeId = value;
			NotifyOfPropertyChange(() => SelectedNoticeId);
		}
	}

	public NoticeDetailResponseModel NoticeDetail
	{
		get
		{
			return _noticeDetail;
		}
		set
		{
			_noticeDetail = value;
			NotifyOfPropertyChange(() => NoticeDetail);
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

	public bool IsTransitionableToRegisterHospitalView
	{
		get
		{
			NoticeDetailResponseModel noticeDetail = NoticeDetail;
			if (noticeDetail != null && noticeDetail.Notice?.TransitionType == 1)
			{
				return !string.IsNullOrEmpty(NoticeDetail?.Hospital?.Id);
			}
			return false;
		}
	}

	protected override async void OnActivate()
	{
		NoticeDetail = new NoticeDetailResponseModel();
		IsNetworkEnabled = false;
		await ExecAsync(async delegate
		{
			try
			{
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				NoticeDetail = (await NoticeService.GetNoticeDetail(new NoticeDetailRequestModel
				{
					NoticeId = SelectedNoticeId
				})) ?? throw new NullReferenceException("レスポンスがnull");
				NotifyOfPropertyChange(() => IsTransitionableToRegisterHospitalView);
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				_log.Error($"【IG】【NoticeDetailViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
	}

	public void OnInputButtonTapped()
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
			if (num2 >= 1)
			{
				break;
			}
			num--;
			num2++;
		}
		base.NavigationService.For<HospitalRegistrationViewModel>().Navigate();
		base.NavigationService.For<UnregisterHospitalViewModel>().WithParam((UnregisterHospitalViewModel x) => x.Hospital, NoticeDetail.Hospital).Navigate();
	}
}

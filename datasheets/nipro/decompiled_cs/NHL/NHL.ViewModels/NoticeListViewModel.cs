using System;
using System.Linq;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class NoticeListViewModel : ViewModelBase
{
	private AuthenticationModel _authenticatedUser;

	private BindableCollection<NoticeDetailResponseModel> _noticeList;

	private bool _isNetworkEnabled;

	private NoticeDetailResponseModel _selectedNotice;

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

	public BindableCollection<NoticeDetailResponseModel> NoticeList
	{
		get
		{
			return _noticeList;
		}
		set
		{
			_noticeList = value;
			NotifyOfPropertyChange(() => NoticeList);
			NotifyOfPropertyChange(() => IsNoticeNothing);
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
			NotifyOfPropertyChange(() => IsNoticeNothing);
		}
	}

	public bool IsNoticeNothing
	{
		get
		{
			if (IsNetworkEnabled)
			{
				BindableCollection<NoticeDetailResponseModel> noticeList = NoticeList;
				if (noticeList == null)
				{
					return true;
				}
				return !noticeList.Any();
			}
			return false;
		}
	}

	public NoticeDetailResponseModel SelectedNotice
	{
		get
		{
			return _selectedNotice;
		}
		set
		{
			if (_selectedNotice == value)
			{
				return;
			}
			_selectedNotice = value;
			NotifyOfPropertyChange(() => SelectedNotice);
			if (_selectedNotice != null && !string.IsNullOrEmpty(_selectedNotice.Notice.Id))
			{
				base.NavigationService.For<NoticeDetailViewModel>().WithParam((NoticeDetailViewModel x) => x.SelectedNoticeId, _selectedNotice.Notice.Id).Navigate();
			}
		}
	}

	protected override async void OnActivate()
	{
		NoticeList = new BindableCollection<NoticeDetailResponseModel>();
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				NoticeListResponseModel noticeListResponseModel = await NoticeService.GetNoticeList(new NoticeListRequestModel
				{
					IgUserId = AuthenticatedUser.IgUser.Id,
					Count = 15
				});
				if (noticeListResponseModel?.Notices?.Any() == true)
				{
					NoticeList = new BindableCollection<NoticeDetailResponseModel>(noticeListResponseModel.Notices.ToArray());
				}
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				_log.Error($"【IG】【NoticeListViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
	}
}

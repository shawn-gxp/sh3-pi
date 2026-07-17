using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class TimeZoneOtherUpdateViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private TimeSpan? _TimezoneOther;

	private TimeSpan? _TimezoneNight;

	private string _SelectedTimezoneOther;

	private string _SelectedTimezoneNight;

	private bool _isNetworkEnabled;

	private AuthenticationModel _authenticatedUser;

	public IUpdateTimezoneOtherService UpdateTimezoneOtherService { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public string SelectedTimezoneOther
	{
		get
		{
			return _SelectedTimezoneOther;
		}
		set
		{
			_SelectedTimezoneOther = value;
			NotifyOfPropertyChange(() => SelectedTimezoneOther);
		}
	}

	public string SelectedTimezoneNight
	{
		get
		{
			return _SelectedTimezoneNight;
		}
		set
		{
			_SelectedTimezoneNight = value;
			NotifyOfPropertyChange(() => SelectedTimezoneNight);
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

	private static string MakeTimezoneString(double? hour, double? minute)
	{
		if (hour.HasValue && minute.HasValue)
		{
			return string.Format(string.Format("{0}:{1:00}", new object[2]
			{
				(int)hour.Value,
				(int)minute.Value
			}));
		}
		return string.Empty;
	}

	private TimeSpan MakeTimezoneTimeSpan(string timezone)
	{
		int result = 0;
		int result2 = 0;
		string[] array = timezone.Split(new char[1] { ':' });
		if (array != null && array.Length >= 1)
		{
			int.TryParse(array[0], out result);
		}
		if (array != null && array.Length >= 2)
		{
			int.TryParse(array[1], out result2);
		}
		return new TimeSpan(result, result2, 0);
	}

	protected override async void OnActivate()
	{
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				AuthenticationModel authenticatedUser = AuthenticatedUser;
				if (authenticatedUser != null && (authenticatedUser.IgUser?.TimezoneOther).HasValue)
				{
					SelectedTimezoneOther = MakeTimezoneString(AuthenticatedUser.IgUser.TimezoneOther?.TotalHours, AuthenticatedUser.IgUser.TimezoneOther?.Minutes);
					SelectedTimezoneNight = MakeTimezoneString(AuthenticatedUser.IgUser.TimezoneNight?.TotalHours, AuthenticatedUser.IgUser.TimezoneNight?.Minutes);
				}
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				SelectedTimezoneOther = MakeTimezoneString(base.UserManager.IgUser.TimezoneOther?.TotalHours, base.UserManager.IgUser.TimezoneOther?.Minutes);
				SelectedTimezoneNight = MakeTimezoneString(base.UserManager.IgUser.TimezoneNight?.TotalHours, base.UserManager.IgUser.TimezoneNight?.Minutes);
				Log.Error($"【IG】【TimeZoneOtherUpdateViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
	}

	public async void Update()
	{
		if (!(await IsValid()))
		{
			return;
		}
		UpdateTimezoneOtherModel request = new UpdateTimezoneOtherModel
		{
			TimezoneOther = _TimezoneOther,
			TimezoneNight = _TimezoneNight
		};
		await ExecAsync(async delegate
		{
			try
			{
				IgUserModel igUserModel = await UpdateTimezoneOtherService.UpdateTimezoneOther(request);
				base.UserManager.IgUser = igUserModel ?? throw new NullReferenceException("レスポンスがnull");
				base.UserManager.IgUser = igUserModel;
				await base.UserManager.SaveUserModel();
				base.EventAggregator.PublishOnBackgroundThread(new UpdateTimezoneOtherRangeEvent());
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【TimeZoneOtherUpdateViewModel】【Update】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
				return;
			}
			await Execute.OnUIThreadAsync(async delegate
			{
				await base.NavigationService.GoBackAsync();
			});
		});
	}

	protected async Task<bool> IsValid()
	{
		_TimezoneOther = MakeTimezoneTimeSpan(SelectedTimezoneOther);
		_TimezoneNight = MakeTimezoneTimeSpan(SelectedTimezoneNight);
		if (_TimezoneOther >= _TimezoneNight)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "朝/晩切替時刻は日付変更基準時刻より後に設定してください。");
			return false;
		}
		return true;
	}
}

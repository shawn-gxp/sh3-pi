using System;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class TimeZoneUpdateViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private TimeSpan? _Timezone1;

	private TimeSpan? _Timezone2;

	private TimeSpan? _Timezone3;

	private TimeSpan? _Timezone4;

	private TimeSpan? _Timezone5;

	private TimeSpan? _Timezone6;

	private TimeSpan? _Timezone7;

	private TimeSpan? _Timezone8;

	private string _SelectedTimezone1;

	private string _SelectedTimezone2;

	private string _SelectedTimezone4;

	private string _SelectedTimezone6;

	private string _SelectedTimezone8;

	private AuthenticationModel _authenticatedUser;

	private bool _isNetworkEnabled;

	public IUpdateTimezoneService UpdateTimezoneService { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public string SelectedTimezone1
	{
		get
		{
			return _SelectedTimezone1;
		}
		set
		{
			_SelectedTimezone1 = value;
			NotifyOfPropertyChange(() => SelectedTimezone1);
		}
	}

	public string SelectedTimezone2
	{
		get
		{
			return _SelectedTimezone2;
		}
		set
		{
			_SelectedTimezone2 = value;
			NotifyOfPropertyChange(() => SelectedTimezone2);
		}
	}

	public string SelectedTimezone4
	{
		get
		{
			return _SelectedTimezone4;
		}
		set
		{
			_SelectedTimezone4 = value;
			NotifyOfPropertyChange(() => SelectedTimezone4);
		}
	}

	public string SelectedTimezone6
	{
		get
		{
			return _SelectedTimezone6;
		}
		set
		{
			_SelectedTimezone6 = value;
			NotifyOfPropertyChange(() => SelectedTimezone6);
		}
	}

	public string SelectedTimezone8
	{
		get
		{
			return _SelectedTimezone8;
		}
		set
		{
			_SelectedTimezone8 = value;
			NotifyOfPropertyChange(() => SelectedTimezone8);
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
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				SelectedTimezone1 = MakeTimezoneString(AuthenticatedUser.IgUser.Timezone1?.TotalHours, AuthenticatedUser.IgUser.Timezone1?.Minutes);
				SelectedTimezone2 = MakeTimezoneString(AuthenticatedUser.IgUser.Timezone2?.TotalHours, AuthenticatedUser.IgUser.Timezone2?.Minutes);
				SelectedTimezone4 = MakeTimezoneString(AuthenticatedUser.IgUser.Timezone4?.TotalHours, AuthenticatedUser.IgUser.Timezone4?.Minutes);
				SelectedTimezone6 = MakeTimezoneString(AuthenticatedUser.IgUser.Timezone6?.TotalHours, AuthenticatedUser.IgUser.Timezone6?.Minutes);
				SelectedTimezone8 = MakeTimezoneString(AuthenticatedUser.IgUser.Timezone8?.TotalHours, AuthenticatedUser.IgUser.Timezone8?.Minutes);
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				SelectedTimezone1 = MakeTimezoneString(base.UserManager.IgUser.Timezone1?.TotalHours, base.UserManager.IgUser.Timezone1?.Minutes);
				SelectedTimezone2 = MakeTimezoneString(base.UserManager.IgUser.Timezone2?.TotalHours, base.UserManager.IgUser.Timezone2?.Minutes);
				SelectedTimezone4 = MakeTimezoneString(base.UserManager.IgUser.Timezone4?.TotalHours, base.UserManager.IgUser.Timezone4?.Minutes);
				SelectedTimezone6 = MakeTimezoneString(base.UserManager.IgUser.Timezone6?.TotalHours, base.UserManager.IgUser.Timezone6?.Minutes);
				SelectedTimezone8 = MakeTimezoneString(base.UserManager.IgUser.Timezone8?.TotalHours, base.UserManager.IgUser.Timezone8?.Minutes);
				Log.Error($"【IG】【TimeZoneUpdateViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
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

	public async void Update()
	{
		if (!(await IsValid()))
		{
			return;
		}
		UpdateTimezoneModel request = new UpdateTimezoneModel
		{
			Timezone1 = _Timezone1,
			Timezone2 = _Timezone2,
			Timezone3 = _Timezone3,
			Timezone4 = _Timezone4,
			Timezone5 = _Timezone5,
			Timezone6 = _Timezone6,
			Timezone7 = _Timezone7,
			Timezone8 = _Timezone8
		};
		await ExecAsync(async delegate
		{
			try
			{
				IgUserModel igUserModel = await UpdateTimezoneService.UpdateTimezone(request);
				base.UserManager.IgUser = igUserModel ?? throw new NullReferenceException("レスポンスがnull");
				base.UserManager.IgUser = igUserModel;
				await base.UserManager.SaveUserModel();
				base.EventAggregator.PublishOnBackgroundThread(new UpdateTimezoneRangeEvent());
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【TimeZoneUpdateViewModel】【Update】例外発生：{ex}");
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
		_Timezone1 = MakeTimezoneTimeSpan(SelectedTimezone1);
		_Timezone2 = MakeTimezoneTimeSpan(SelectedTimezone2);
		_Timezone4 = MakeTimezoneTimeSpan(SelectedTimezone4);
		_Timezone6 = MakeTimezoneTimeSpan(SelectedTimezone6);
		_Timezone8 = MakeTimezoneTimeSpan(SelectedTimezone8);
		TimeSpan?[] source = new TimeSpan?[2]
		{
			_Timezone2?.Add(new TimeSpan(2, 0, 0)),
			_Timezone4
		};
		_Timezone3 = source.Min();
		TimeSpan?[] source2 = new TimeSpan?[2]
		{
			_Timezone4?.Add(new TimeSpan(2, 0, 0)),
			_Timezone6
		};
		_Timezone5 = source2.Min();
		TimeSpan?[] source3 = new TimeSpan?[2]
		{
			_Timezone6?.Add(new TimeSpan(2, 0, 0)),
			_Timezone8
		};
		_Timezone7 = source3.Min();
		if (_Timezone1 > new TimeSpan(24, 0, 0))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "起床は24時以前で入力してください");
			return false;
		}
		if (_Timezone8 - _Timezone1 > new TimeSpan(24, 0, 0))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "起床～就寝までが24時間以内で入力してください");
			return false;
		}
		if (_Timezone1 > _Timezone2)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n起床～朝食");
			return false;
		}
		if (_Timezone2 > _Timezone4)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n朝食～昼食");
			return false;
		}
		if (_Timezone4 > _Timezone6)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n昼食～夕食");
			return false;
		}
		if (_Timezone6 > _Timezone8)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n夕食～就寝");
			return false;
		}
		return true;
	}
}

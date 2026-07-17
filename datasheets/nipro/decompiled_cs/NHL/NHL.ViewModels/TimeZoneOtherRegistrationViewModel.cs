using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class TimeZoneOtherRegistrationViewModel : ViewModelBase
{
	private bool _RegistResult;

	private string _SelectedTimezoneOther;

	private string _SelectedTimezoneNight;

	public new IDialogProvider DialogProvider { get; set; }

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

	public TimeZoneOtherRegistrationViewModel(IUserManager userManager)
	{
		base.UserManager = userManager;
		SelectedTimezoneOther = MakeTimezoneString((int)(base.UserManager.IgUser.TimezoneOther?.TotalHours).Value, (base.UserManager.IgUser.TimezoneOther?.Minutes).Value);
		SelectedTimezoneNight = MakeTimezoneString((int)(base.UserManager.IgUser.TimezoneNight?.TotalHours).Value, (base.UserManager.IgUser.TimezoneNight?.Minutes).Value);
	}

	protected override async void OnActivate()
	{
		if ((bool)Application.Current.Properties["firstMeterSelectSphygmomanometer"])
		{
			await DialogProvider.ShowAlert(null, "血圧計の測定結果は切替時刻を基準として朝/晩に自動振り分けされます。", "閉じる", null);
		}
		base.OnActivate();
	}

	protected override void OnDeactivate(bool close)
	{
		base.OnDeactivate(close);
		base.UserManager.IgUser.TimezoneOther = MakeTimezoneTimeSpan(SelectedTimezoneOther);
		base.UserManager.IgUser.TimezoneNight = MakeTimezoneTimeSpan(SelectedTimezoneNight);
	}

	private string MakeTimezoneString(int hour, int minute)
	{
		return string.Format(string.Format("{0}:{1:00}", new object[2] { hour, minute }));
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

	private async Task IgUserRegister()
	{
		IgUserRequestModel igUser = new IgUserRequestModel
		{
			Name = base.UserManager.IgUser.Name,
			NameKane = base.UserManager.IgUser.NameKana,
			Birthday = base.UserManager.IgUser.Birthday,
			Sex = base.UserManager.IgUser.Sex,
			Height = base.UserManager.IgUser.Height,
			Weight = base.UserManager.IgUser.Weight,
			Timezone1 = base.UserManager.IgUser.Timezone1,
			Timezone2 = base.UserManager.IgUser.Timezone2,
			Timezone3 = base.UserManager.IgUser.Timezone3,
			Timezone4 = base.UserManager.IgUser.Timezone4,
			Timezone5 = base.UserManager.IgUser.Timezone5,
			Timezone6 = base.UserManager.IgUser.Timezone6,
			Timezone7 = base.UserManager.IgUser.Timezone7,
			Timezone8 = base.UserManager.IgUser.Timezone8,
			GlucoseHighValue = base.UserManager.IgUser.GlucoseHighValue,
			GlucoseLittleHighValue = base.UserManager.IgUser.GlucoseLittleHighValue,
			GlucoseLittleLowValue = base.UserManager.IgUser.GlucoseLittleLowValue,
			GlucoseLowValue = base.UserManager.IgUser.GlucoseLowValue,
			TimezoneOther = base.UserManager.IgUser.TimezoneOther,
			TimezoneNight = base.UserManager.IgUser.TimezoneNight
		};
		await ExecAsync(async delegate
		{
			if (await base.UserManager.RegistUser(igUser))
			{
				await base.UserManager.Authenticate();
				await base.UserManager.SaveUserModel();
			}
			else
			{
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("エラー", "ユーザー登録でエラーが発生しました", "OK", null);
				});
			}
		});
	}

	public async void NextButton()
	{
		if (await IsValid())
		{
			if ((bool)Application.Current.Properties["firstMeterSelectGlucose"])
			{
				await Navigate<TimeZoneRegistViewModel>();
				return;
			}
			await IgUserRegister();
			await Navigate<BackupCodeDisplayViewModel>();
		}
	}

	protected async Task<bool> IsValid()
	{
		base.UserManager.IgUser.TimezoneOther = MakeTimezoneTimeSpan(SelectedTimezoneOther);
		base.UserManager.IgUser.TimezoneNight = MakeTimezoneTimeSpan(SelectedTimezoneNight);
		if (base.UserManager.IgUser.TimezoneOther >= base.UserManager.IgUser.TimezoneNight)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "朝/晩切替時刻は日付変更基準時刻より後に設定してください。");
			return false;
		}
		return true;
	}
}

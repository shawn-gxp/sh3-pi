using System;
using System.Linq;
using System.Threading.Tasks;
using NHL.Services;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class TimeZoneRegistViewModel : ViewModelBase
{
	private bool _RegistResult;

	private string _SelectedTimezone1;

	private string _SelectedTimezone2;

	private string _SelectedTimezone4;

	private string _SelectedTimezone6;

	private string _SelectedTimezone8;

	public new IDialogProvider DialogProvider { get; set; }

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

	public TimeZoneRegistViewModel(IUserManager userManager)
	{
		base.UserManager = userManager;
		SelectedTimezone1 = MakeTimezoneString((int)(base.UserManager.IgUser.Timezone1?.TotalHours).Value, (base.UserManager.IgUser.Timezone1?.Minutes).Value);
		SelectedTimezone2 = MakeTimezoneString((int)(base.UserManager.IgUser.Timezone2?.TotalHours).Value, (base.UserManager.IgUser.Timezone2?.Minutes).Value);
		SelectedTimezone4 = MakeTimezoneString((int)(base.UserManager.IgUser.Timezone4?.TotalHours).Value, (base.UserManager.IgUser.Timezone4?.Minutes).Value);
		SelectedTimezone6 = MakeTimezoneString((int)(base.UserManager.IgUser.Timezone6?.TotalHours).Value, (base.UserManager.IgUser.Timezone6?.Minutes).Value);
		SelectedTimezone8 = MakeTimezoneString((int)(base.UserManager.IgUser.Timezone8?.TotalHours).Value, (base.UserManager.IgUser.Timezone8?.Minutes).Value);
	}

	protected override async void OnActivate()
	{
		if ((bool)Application.Current.Properties["firstMeterSelectGlucose"])
		{
			await DialogProvider.ShowAlert(null, "血糖測定器の測定結果はタイムゾーンごとに自動振り分けされます。", "閉じる", null);
		}
		base.OnActivate();
	}

	protected override void OnDeactivate(bool close)
	{
		base.OnDeactivate(close);
		base.UserManager.IgUser.Timezone1 = MakeTimezoneTimeSpan(SelectedTimezone1);
		base.UserManager.IgUser.Timezone2 = MakeTimezoneTimeSpan(SelectedTimezone2);
		base.UserManager.IgUser.Timezone4 = MakeTimezoneTimeSpan(SelectedTimezone4);
		base.UserManager.IgUser.Timezone6 = MakeTimezoneTimeSpan(SelectedTimezone6);
		base.UserManager.IgUser.Timezone8 = MakeTimezoneTimeSpan(SelectedTimezone8);
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

	public async void NextButton()
	{
		if (await IsValid())
		{
			await Navigate<GlucoseTargetRegistViewModel>();
		}
	}

	protected async Task<bool> IsValid()
	{
		base.UserManager.IgUser.Timezone1 = MakeTimezoneTimeSpan(SelectedTimezone1);
		base.UserManager.IgUser.Timezone2 = MakeTimezoneTimeSpan(SelectedTimezone2);
		base.UserManager.IgUser.Timezone4 = MakeTimezoneTimeSpan(SelectedTimezone4);
		base.UserManager.IgUser.Timezone6 = MakeTimezoneTimeSpan(SelectedTimezone6);
		base.UserManager.IgUser.Timezone8 = MakeTimezoneTimeSpan(SelectedTimezone8);
		TimeSpan?[] source = new TimeSpan?[2]
		{
			base.UserManager.IgUser.Timezone2?.Add(new TimeSpan(2, 0, 0)),
			base.UserManager.IgUser.Timezone4
		};
		base.UserManager.IgUser.Timezone3 = source.Min();
		TimeSpan?[] source2 = new TimeSpan?[2]
		{
			base.UserManager.IgUser.Timezone4?.Add(new TimeSpan(2, 0, 0)),
			base.UserManager.IgUser.Timezone6
		};
		base.UserManager.IgUser.Timezone5 = source2.Min();
		TimeSpan?[] source3 = new TimeSpan?[2]
		{
			base.UserManager.IgUser.Timezone6?.Add(new TimeSpan(2, 0, 0)),
			base.UserManager.IgUser.Timezone8
		};
		base.UserManager.IgUser.Timezone7 = source3.Min();
		if (base.UserManager.IgUser.Timezone1 > new TimeSpan(24, 0, 0))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "起床は24時以前で入力してください");
			return false;
		}
		if (base.UserManager.IgUser.Timezone8 - base.UserManager.IgUser.Timezone1 > new TimeSpan(24, 0, 0))
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "起床～就寝までが24時間以内で入力してください");
			return false;
		}
		if (base.UserManager.IgUser.Timezone1 > base.UserManager.IgUser.Timezone2)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n起床～朝食");
			return false;
		}
		if (base.UserManager.IgUser.Timezone2 > base.UserManager.IgUser.Timezone4)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n朝食～昼食");
			return false;
		}
		if (base.UserManager.IgUser.Timezone4 > base.UserManager.IgUser.Timezone6)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n昼食～夕食");
			return false;
		}
		if (base.UserManager.IgUser.Timezone6 > base.UserManager.IgUser.Timezone8)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "時刻の前後が入れ替わっています\n夕食～就寝");
			return false;
		}
		return true;
	}
}

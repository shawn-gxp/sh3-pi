using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class GlucoseTargetRegistViewModel : ViewModelBase
{
	public class ThresholdItem
	{
		public int? Value { get; set; }

		public string Name { get; set; }
	}

	private IList<ThresholdItem> _ThresholdList = (from x in Enumerable.Range(20, 581)
		select new ThresholdItem
		{
			Value = x,
			Name = x.ToString()
		}).ToList();

	private ThresholdItem _GlucoseHighValue;

	private ThresholdItem _GlucoseLittleHighValue;

	private ThresholdItem _GlucoseLittleLowValue;

	private ThresholdItem _GlucoseLowValue;

	public IList<ThresholdItem> ThresholdList => _ThresholdList;

	public ThresholdItem GlucoseHighValue
	{
		get
		{
			return _GlucoseHighValue;
		}
		set
		{
			_GlucoseHighValue = value;
			NotifyOfPropertyChange(() => GlucoseHighValue);
			if (_GlucoseHighValue != null)
			{
				base.UserManager.IgUser.GlucoseHighValue = _GlucoseHighValue.Value;
			}
		}
	}

	public ThresholdItem GlucoseLittleHighValue
	{
		get
		{
			return _GlucoseLittleHighValue;
		}
		set
		{
			_GlucoseLittleHighValue = value;
			NotifyOfPropertyChange(() => GlucoseLittleHighValue);
			if (_GlucoseLittleHighValue != null)
			{
				base.UserManager.IgUser.GlucoseLittleHighValue = _GlucoseLittleHighValue.Value;
			}
		}
	}

	public ThresholdItem GlucoseLittleLowValue
	{
		get
		{
			return _GlucoseLittleLowValue;
		}
		set
		{
			_GlucoseLittleLowValue = value;
			NotifyOfPropertyChange(() => GlucoseLittleLowValue);
			if (_GlucoseLittleLowValue != null)
			{
				base.UserManager.IgUser.GlucoseLittleLowValue = _GlucoseLittleLowValue.Value;
			}
		}
	}

	public ThresholdItem GlucoseLowValue
	{
		get
		{
			return _GlucoseLowValue;
		}
		set
		{
			_GlucoseLowValue = value;
			NotifyOfPropertyChange(() => GlucoseLowValue);
			if (_GlucoseLowValue != null)
			{
				base.UserManager.IgUser.GlucoseLowValue = _GlucoseLowValue.Value;
			}
		}
	}

	public new IDialogProvider DialogProvider { get; set; }

	public GlucoseTargetRegistViewModel(IUserManager userManager)
	{
		base.UserManager = userManager;
	}

	public async void NextButton()
	{
		if (await IsValid())
		{
			await IgUserRegister();
		}
	}

	public async void LaterButton()
	{
		await IgUserRegister();
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
				await Navigate<BackupCodeDisplayViewModel>();
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

	protected async Task<bool> IsValid()
	{
		if (!base.UserManager.IgUser.GlucoseHighValue.HasValue)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「高い」を入力してください");
			return false;
		}
		if (!base.UserManager.IgUser.GlucoseLittleHighValue.HasValue)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや高い」を入力してください");
			return false;
		}
		if (!base.UserManager.IgUser.GlucoseLittleLowValue.HasValue)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや低い」を入力してください");
			return false;
		}
		if (!base.UserManager.IgUser.GlucoseLowValue.HasValue)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「低い」を入力してください");
			return false;
		}
		if (base.UserManager.IgUser.GlucoseHighValue < 20 || base.UserManager.IgUser.GlucoseHighValue > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「高い」は20～600を入力してください");
			return false;
		}
		if (base.UserManager.IgUser.GlucoseLittleHighValue < 20 || base.UserManager.IgUser.GlucoseLittleHighValue > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや高い」は20～600を入力してください");
			return false;
		}
		if (base.UserManager.IgUser.GlucoseLittleLowValue < 20 || base.UserManager.IgUser.GlucoseLittleLowValue > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや低い」は20～600を入力してください");
			return false;
		}
		if (base.UserManager.IgUser.GlucoseLowValue < 20 || base.UserManager.IgUser.GlucoseLowValue > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「低い」は20～600を入力してください");
			return false;
		}
		if (base.UserManager.IgUser.GlucoseHighValue <= base.UserManager.IgUser.GlucoseLittleHighValue)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや高い」は「高い」未満を入力してください");
			return false;
		}
		if (base.UserManager.IgUser.GlucoseLittleHighValue <= base.UserManager.IgUser.GlucoseLittleLowValue)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや低い」は「やや高い」未満を入力してください");
			return false;
		}
		if (base.UserManager.IgUser.GlucoseLittleLowValue <= base.UserManager.IgUser.GlucoseLowValue)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「低い」は「やや低い」未満を入力してください");
			return false;
		}
		return true;
	}
}

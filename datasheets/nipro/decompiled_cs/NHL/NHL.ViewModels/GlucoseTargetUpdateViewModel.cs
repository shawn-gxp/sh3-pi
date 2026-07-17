using System;
using System.Collections.Generic;
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

public class GlucoseTargetUpdateViewModel : ViewModelBase
{
	public class ThresholdItem
	{
		public int? Value { get; set; }

		public string Name { get; set; }
	}

	private ILoggingService Log = DependencyService.Get<ILoggingService>();

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

	private AuthenticationModel _authenticatedUser;

	private bool _isNetworkEnabled;

	public new IDialogProvider DialogProvider { get; set; }

	public IUpdateIgGlucoseTargetService UpdateIgGlucoseTargetService { get; set; }

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public IList<ThresholdItem> ThresholdList
	{
		get
		{
			return _ThresholdList;
		}
		set
		{
			_ThresholdList = value;
			NotifyOfPropertyChange(() => ThresholdList);
		}
	}

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

	public async void Update()
	{
		if (!(await IsValid()))
		{
			return;
		}
		GlucoseTargetThresholdModel request = new GlucoseTargetThresholdModel
		{
			HighValue = GlucoseHighValue.Value,
			LittleHighValue = GlucoseLittleHighValue.Value,
			LittleLowValue = GlucoseLittleLowValue.Value,
			LowValue = GlucoseLowValue.Value
		};
		await ExecAsync(async delegate
		{
			try
			{
				IgUserModel igUserModel = await UpdateIgGlucoseTargetService.UpdateGlucoseTargetThreshold(request);
				base.UserManager.IgUser = igUserModel ?? throw new NullReferenceException("レスポンスがnull");
				await base.UserManager.SaveUserModel();
				await base.EventAggregator.PublishOnUIThreadAsync(new GlucoseTargetThresholdChanged());
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【GlucoseTargetUpdateViewModel】【Update】例外発生：{ex}");
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

	protected override async void OnActivate()
	{
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				GlucoseHighValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == AuthenticatedUser.IgUser.GlucoseHighValue);
				GlucoseLittleHighValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == AuthenticatedUser.IgUser.GlucoseLittleHighValue);
				GlucoseLittleLowValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == AuthenticatedUser.IgUser.GlucoseLittleLowValue);
				GlucoseLowValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == AuthenticatedUser.IgUser.GlucoseLowValue);
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				GlucoseHighValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == base.UserManager.IgUser.GlucoseHighValue);
				GlucoseLittleHighValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == base.UserManager.IgUser.GlucoseLittleHighValue);
				GlucoseLittleLowValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == base.UserManager.IgUser.GlucoseLittleLowValue);
				GlucoseLowValue = ThresholdList.FirstOrDefault((ThresholdItem x) => x.Value == base.UserManager.IgUser.GlucoseLowValue);
				Log.Error($"【IG】【GlucoseTargetUpdateViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
	}

	private async Task<bool> IsValid()
	{
		if (GlucoseHighValue == null)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「高い」を入力してください");
			return false;
		}
		if (GlucoseLittleHighValue == null)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや高い」を入力してください");
			return false;
		}
		if (GlucoseLittleLowValue == null)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや低い」を入力してください");
			return false;
		}
		if (GlucoseLowValue == null)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「低い」を入力してください");
			return false;
		}
		if (GlucoseHighValue.Value < 20 || GlucoseHighValue.Value > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「高い」は20～600を入力してください");
			return false;
		}
		if (GlucoseLittleHighValue.Value < 20 || GlucoseLittleHighValue.Value > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや高い」は20～600を入力してください");
			return false;
		}
		if (GlucoseLittleLowValue.Value < 20 || GlucoseLittleLowValue.Value > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや低い」は20～600を入力してください");
			return false;
		}
		if (GlucoseLowValue.Value < 20 || GlucoseLowValue.Value > 600)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「低い」は20～600を入力してください");
			return false;
		}
		if (GlucoseHighValue.Value <= GlucoseLittleHighValue.Value)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや高い」は「高い」未満を入力してください");
			return false;
		}
		if (GlucoseLittleHighValue.Value <= GlucoseLittleLowValue.Value)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「やや低い」は「やや高い」未満を入力してください");
			return false;
		}
		if (GlucoseLittleLowValue.Value <= GlucoseLowValue.Value)
		{
			await DialogProvider.ShowAlert("入力チェックエラー", "「低い」は「やや低い」未満を入力してください");
			return false;
		}
		return true;
	}
}

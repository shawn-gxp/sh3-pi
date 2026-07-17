using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class UserInfoUpdateViewModel : ViewModelBase
{
	public class SexType
	{
		public string Name { get; set; }

		public string Value { get; set; }
	}

	public class HeightItem
	{
		public string Name { get; set; }

		public int Value { get; set; }
	}

	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private static readonly int MAX_NAME_BYTE = 128;

	private static readonly int MAX_NAME_KANA_BYTE = 128;

	private static readonly DateTime MAX_DATE = new DateTime(9999, 12, 31, 23, 59, 59);

	private static readonly DateTime MIN_DATE = new DateTime(1753, 1, 1, 0, 0, 0);

	private string _Name;

	private string _NameKana;

	private DateTime? _Birthday;

	private string _Weight;

	private double? _RegisterWeight;

	private AuthenticationModel _authenticatedUser;

	private IList<SexType> _SexTypes = new List<SexType>
	{
		new SexType
		{
			Name = "男性",
			Value = "1"
		},
		new SexType
		{
			Name = "女性",
			Value = "2"
		}
	};

	private SexType _SelectedSex;

	private IList<HeightItem> _HeightList = (from x in Enumerable.Range(90, 131)
		select new HeightItem
		{
			Value = x,
			Name = x.ToString()
		}).ToList();

	private HeightItem _SelectedHeight;

	private bool _BirthdayVisible;

	private bool _isNetworkEnabled;

	public new IDialogProvider DialogProvider { get; set; }

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public AuthenticationModel AuthenticatedUser
	{
		get
		{
			return _authenticatedUser;
		}
		set
		{
			if (_authenticatedUser != value)
			{
				_authenticatedUser = value;
				NotifyOfPropertyChange(() => AuthenticatedUser);
			}
		}
	}

	public IList<SexType> SexTypes
	{
		get
		{
			return _SexTypes;
		}
		set
		{
			_SexTypes = value;
			NotifyOfPropertyChange(() => SexTypes);
		}
	}

	public SexType SelectedSex
	{
		get
		{
			return _SelectedSex;
		}
		set
		{
			_SelectedSex = value;
			NotifyOfPropertyChange(() => SelectedSex);
		}
	}

	public IList<HeightItem> HeightList
	{
		get
		{
			return _HeightList;
		}
		set
		{
			_HeightList = value;
			NotifyOfPropertyChange(() => HeightList);
		}
	}

	public HeightItem SelectedHeight
	{
		get
		{
			return _SelectedHeight;
		}
		set
		{
			_SelectedHeight = value;
			NotifyOfPropertyChange(() => SelectedHeight);
		}
	}

	public string Name
	{
		get
		{
			return _Name;
		}
		set
		{
			_Name = value;
			NotifyOfPropertyChange(() => Name);
		}
	}

	public string NameKana
	{
		get
		{
			return _NameKana;
		}
		set
		{
			_NameKana = value;
			NotifyOfPropertyChange(() => NameKana);
		}
	}

	public DateTime? Birthday
	{
		get
		{
			return _Birthday;
		}
		set
		{
			_Birthday = value;
			NotifyOfPropertyChange(() => Birthday);
		}
	}

	public bool BirthdayVisible
	{
		get
		{
			return _BirthdayVisible;
		}
		set
		{
			_BirthdayVisible = value;
			NotifyOfPropertyChange(() => BirthdayVisible);
		}
	}

	public string Weight
	{
		get
		{
			return _Weight;
		}
		set
		{
			_Weight = value;
			NotifyOfPropertyChange(() => Weight);
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

	public IUpdateIgUserService UpdateIgUserService { get; set; }

	protected override async void OnActivate()
	{
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				Birthday = DateTime.Now;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				if (AuthenticatedUser != null)
				{
					decimal? num = (decimal?)AuthenticatedUser.IgUser.Height;
					decimal? num2 = num;
					decimal num3 = 90;
					if ((num2.GetValueOrDefault() < num3) & num2.HasValue)
					{
						SelectedHeight = HeightList.FirstOrDefault((HeightItem x) => x.Value == 90);
					}
					else
					{
						num2 = num;
						num3 = 220;
						if ((num2.GetValueOrDefault() > num3) & num2.HasValue)
						{
							SelectedHeight = HeightList.FirstOrDefault((HeightItem x) => x.Value == 220);
						}
						else
						{
							if (num.HasValue)
							{
								num2 = num - (decimal?)Math.Floor(num.Value);
								if (!((num2.GetValueOrDefault() == default(decimal)) & num2.HasValue))
								{
									int height = (int)Math.Floor(num.Value);
									SelectedHeight = HeightList.FirstOrDefault((HeightItem x) => x.Value == height);
									goto IL_028f;
								}
							}
							if (num.HasValue)
							{
								int height2 = (int)num.Value;
								SelectedHeight = HeightList.FirstOrDefault((HeightItem x) => x.Value == height2);
							}
							else
							{
								SelectedHeight = null;
							}
						}
					}
					goto IL_028f;
				}
				goto IL_0378;
				IL_028f:
				BirthdayVisible = false;
				Name = AuthenticatedUser.IgUser.Name;
				NameKana = AuthenticatedUser.IgUser.NameKana;
				SelectedSex = SexTypes.FirstOrDefault((SexType x) => x.Value == AuthenticatedUser.IgUser.Sex);
				Birthday = AuthenticatedUser.IgUser.Birthday;
				Weight = AuthenticatedUser.IgUser.Weight?.ToString();
				if (AuthenticatedUser.IgUser.Birthday.HasValue)
				{
					Execute.BeginOnUIThread(async delegate
					{
						await Task.Delay(1);
						Birthday = AuthenticatedUser.IgUser.Birthday;
						BirthdayVisible = true;
					});
				}
				else
				{
					Birthday = DateTime.Now;
					Execute.BeginOnUIThread(async delegate
					{
						await Task.Delay(10);
						Birthday = null;
						BirthdayVisible = true;
					});
				}
				goto IL_0378;
				IL_0378:
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				Name = base.UserManager.IgUser.Name;
				NameKana = base.UserManager.IgUser.NameKana;
				SelectedSex = SexTypes.FirstOrDefault((SexType x) => x.Value == base.UserManager.IgUser.Sex);
				Birthday = base.UserManager.IgUser.Birthday;
				int height3 = (int)base.UserManager.IgUser.Height.Value;
				SelectedHeight = HeightList.FirstOrDefault((HeightItem x) => x.Value == height3);
				Weight = base.UserManager.IgUser.Weight?.ToString();
				Log.Error($"【IG】【UserInfoUpdateViewModel】【OnActivate】例外発生：{ex}");
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
		UpdateIgUserModel request = new UpdateIgUserModel
		{
			Name = Name,
			NameKana = NameKana,
			Sex = SelectedSex?.Value,
			Height = SelectedHeight?.Value,
			Weight = _RegisterWeight
		};
		if (Birthday.HasValue)
		{
			request.Birthday = DateTimeUtils.CreateUtcTime(Birthday.Value);
		}
		await ExecAsync(async delegate
		{
			try
			{
				IgUserModel igUserModel = await UpdateIgUserService.UpdateIgUser(request);
				base.UserManager.IgUser = igUserModel ?? throw new NullReferenceException("レスポンスがnull");
				await base.UserManager.SaveUserModel();
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【UserInfoUpdateViewModel】【Update】例外発生：{ex}");
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
		if (string.IsNullOrEmpty(Name))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "氏名を入力してください");
			return false;
		}
		if (string.IsNullOrEmpty(NameKana))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "氏名（カナ）を入力してください");
			return false;
		}
		Encoding uTF = Encoding.UTF8;
		if (uTF.GetByteCount(Name) > MAX_NAME_BYTE)
		{
			await DialogProvider.ShowAlert("書式チェックエラー", "氏名が長すぎます");
			return false;
		}
		if (uTF.GetByteCount(Name) > MAX_NAME_KANA_BYTE)
		{
			await DialogProvider.ShowAlert("書式チェックエラー", "氏名（カナ）が長すぎます");
			return false;
		}
		if (!string.IsNullOrEmpty(Weight))
		{
			if (!double.TryParse(Weight, out var result) || result > 1.7976931348623157E+308 || result < 0.0)
			{
				await DialogProvider.ShowAlert("書式チェックエラー", "体重が無効です");
				return false;
			}
			_RegisterWeight = result;
		}
		if (!Birthday.HasValue)
		{
			return true;
		}
		if (Birthday == DateTime.MinValue || (!(Birthday > MAX_DATE) && !(Birthday < MIN_DATE)))
		{
			return true;
		}
		await DialogProvider.ShowAlert("書式チェックエラー", "生年月日が無効です");
		return false;
	}
}

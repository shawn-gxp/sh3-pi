using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Services;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class UserInfoRegistViewModel : ViewModelBase
{
	public class SexType
	{
		public string Name { get; set; }

		public string Value { get; set; }
	}

	public class HeightItem
	{
		public string Name { get; set; }

		public string Value { get; set; }
	}

	private static readonly int MAX_NAME_BYTE = 128;

	private static readonly int MAX_NAME_KANA_BYTE = 128;

	private static readonly DateTime MAX_DATE = new DateTime(9999, 12, 31, 23, 59, 59);

	private static readonly DateTime MIN_DATE = new DateTime(1753, 1, 1, 0, 0, 0);

	private string _name;

	private string _nameKana;

	private SexType _selectedSexType;

	private DateTime? _birthday;

	private HeightItem _selectedHeightItem;

	private string _weight;

	private readonly IList<SexType> _sexTypes = new List<SexType>
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

	private readonly IList<HeightItem> _heightList = (from x in Enumerable.Range(90, 131)
		select new HeightItem
		{
			Value = x.ToString(),
			Name = x.ToString()
		}).ToList();

	public new IDialogProvider DialogProvider { get; set; }

	public IList<SexType> SexTypes => _sexTypes;

	public IList<HeightItem> HeightList => _heightList;

	public string Name
	{
		get
		{
			return _name;
		}
		set
		{
			_name = value;
			NotifyOfPropertyChange(() => Name);
		}
	}

	public string NameKana
	{
		get
		{
			return _nameKana;
		}
		set
		{
			_nameKana = value;
			NotifyOfPropertyChange(() => NameKana);
		}
	}

	public SexType SelectedSexType
	{
		get
		{
			return _selectedSexType;
		}
		set
		{
			_selectedSexType = value;
			NotifyOfPropertyChange(() => SelectedSexType);
		}
	}

	public DateTime? Birthday
	{
		get
		{
			return _birthday;
		}
		set
		{
			_birthday = value;
			NotifyOfPropertyChange(() => Birthday);
		}
	}

	public HeightItem SelectedHeightItem
	{
		get
		{
			return _selectedHeightItem;
		}
		set
		{
			_selectedHeightItem = value;
			NotifyOfPropertyChange(() => SelectedHeightItem);
		}
	}

	public string Weight
	{
		get
		{
			return _weight;
		}
		set
		{
			_weight = value;
			NotifyOfPropertyChange(() => Weight);
		}
	}

	protected override async void OnActivate()
	{
		Name = base.UserManager.IgUser.Name;
		NameKana = base.UserManager.IgUser.NameKana;
		SelectedSexType = SexTypes.FirstOrDefault((SexType x) => x.Value == base.UserManager.IgUser.Sex);
		Birthday = null;
		SelectedHeightItem = HeightList.FirstOrDefault((HeightItem x) => x.Value == base.UserManager.IgUser.Height.ToString());
		Weight = base.UserManager.IgUser.Weight?.ToString();
		if (base.UserManager.IgUser.Birthday.HasValue)
		{
			Birthday = base.UserManager.IgUser.Birthday;
		}
		else
		{
			Birthday = DateTime.Now;
			Execute.BeginOnUIThread(async delegate
			{
				await Task.Delay(10);
				Birthday = null;
			});
		}
		await IsValidComposition();
		base.OnActivate();
	}

	public async void NextButton()
	{
		bool flag = !(await IsValid());
		if (!flag)
		{
			flag = !(await IsValidComposition());
		}
		if (!flag)
		{
			await Navigate<TimeZoneOtherRegistrationViewModel>();
		}
	}

	public async void LaterButton()
	{
		Name = string.Empty;
		NameKana = string.Empty;
		Birthday = null;
		SelectedSexType = null;
		SelectedHeightItem = null;
		Weight = null;
		base.UserManager.IgUser.Name = string.Empty;
		base.UserManager.IgUser.NameKana = string.Empty;
		base.UserManager.IgUser.Birthday = null;
		base.UserManager.IgUser.Sex = string.Empty;
		base.UserManager.IgUser.Height = null;
		base.UserManager.IgUser.Weight = null;
		await Navigate<TimeZoneOtherRegistrationViewModel>();
	}

	private async Task<bool> IsValid()
	{
		base.UserManager.IgUser.Name = string.Empty;
		base.UserManager.IgUser.NameKana = string.Empty;
		base.UserManager.IgUser.Birthday = null;
		base.UserManager.IgUser.Sex = string.Empty;
		base.UserManager.IgUser.Height = null;
		base.UserManager.IgUser.Weight = null;
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
		base.UserManager.IgUser.Name = Name;
		base.UserManager.IgUser.NameKana = NameKana;
		if (SelectedSexType != null)
		{
			base.UserManager.IgUser.Sex = SelectedSexType.Value;
		}
		if (Birthday.HasValue)
		{
			if (Birthday != DateTime.MinValue && (Birthday > MAX_DATE || Birthday < MIN_DATE))
			{
				await DialogProvider.ShowAlert("書式チェックエラー", "生年月日が無効です");
				return false;
			}
			base.UserManager.IgUser.Birthday = DateTimeUtils.CreateUtcTime(Birthday.Value);
		}
		if (SelectedHeightItem != null)
		{
			base.UserManager.IgUser.Height = int.Parse(SelectedHeightItem.Value);
		}
		if (!string.IsNullOrEmpty(Weight))
		{
			if (!double.TryParse(Weight, out var result) || result > 1.7976931348623157E+308 || result < 0.0)
			{
				await DialogProvider.ShowAlert("書式チェックエラー", "体重が無効です");
				return false;
			}
			base.UserManager.IgUser.Weight = result;
		}
		return true;
	}

	private async Task<bool> IsValidComposition()
	{
		if (!(bool)Application.Current.Properties["firstMeterSelectCompositionMeter"])
		{
			return true;
		}
		if (base.UserManager.IgUser.Birthday.HasValue && base.UserManager.IgUser.Height.HasValue)
		{
			return true;
		}
		await DialogProvider.ShowAlert(null, "体組成計の利用には「生年月日」と「身長」の登録が必要です。", "閉じる", null);
		return false;
	}
}

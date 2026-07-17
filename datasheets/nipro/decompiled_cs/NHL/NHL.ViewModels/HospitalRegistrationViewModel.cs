using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Models;
using NHL.Properties;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class HospitalRegistrationViewModel : ViewModelBase, IHandle<HospitalScannedEvent>, IHandle
{
	public const int MAX_REGISTERED_HOSPITAL = 10;

	private AuthenticationModel authenticatedUser;

	private string facilitiesName;

	private bool _IsNetworkEnabled;

	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private bool[] _IsHospitalsRegistered;

	private string[] _HospitalInfoWarn;

	private string[] _RegisteredFacilitiesNames;

	private bool _IsRegisterable = true;

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public IHospitalInfoService HospitalInfoService { get; set; }

	public AuthenticationModel AuthenticatedUser
	{
		get
		{
			return authenticatedUser;
		}
		set
		{
			authenticatedUser = value;
			NotifyOfPropertyChange(() => AuthenticatedUser);
		}
	}

	public string FacilitiesName
	{
		get
		{
			return facilitiesName;
		}
		set
		{
			if (facilitiesName != value)
			{
				facilitiesName = value;
				NotifyOfPropertyChange(() => FacilitiesName);
			}
		}
	}

	public bool IsNetworkEnabled
	{
		get
		{
			return _IsNetworkEnabled;
		}
		set
		{
			_IsNetworkEnabled = value;
			NotifyOfPropertyChange(() => IsNetworkEnabled);
		}
	}

	public bool[] IsHospitalsRegistered
	{
		get
		{
			return _IsHospitalsRegistered;
		}
		set
		{
			_IsHospitalsRegistered = value;
			NotifyOfPropertyChange(() => IsHospitalsRegistered);
		}
	}

	public string[] HospitalInfoWarn
	{
		get
		{
			return _HospitalInfoWarn;
		}
		set
		{
			_HospitalInfoWarn = value;
			NotifyOfPropertyChange(() => HospitalInfoWarn);
		}
	}

	public string[] RegisteredFacilitiesNames
	{
		get
		{
			return _RegisteredFacilitiesNames;
		}
		set
		{
			_RegisteredFacilitiesNames = value;
			NotifyOfPropertyChange(() => RegisteredFacilitiesNames);
		}
	}

	public bool IsRegisterable
	{
		get
		{
			return _IsRegisterable;
		}
		set
		{
			_IsRegisterable = value;
			NotifyOfPropertyChange(() => IsRegisterable);
		}
	}

	public void Unregist(int i)
	{
		HospitalModel[] array = AuthenticatedUser.Hospitals.ToArray();
		base.NavigationService.For<UnregisterHospitalViewModel>().WithParam((UnregisterHospitalViewModel x) => x.Hospital, array[i]).Navigate();
	}

	protected override void OnInitialize()
	{
		base.OnInitialize();
		base.EventAggregator.Subscribe(this);
	}

	protected override async void OnActivate()
	{
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				AuthenticatedUser = await AuthenticatedUserService.GetAuthenticatedUser();
				List<HospitalInfoResponseModel> hospitalInfoList = new List<HospitalInfoResponseModel>();
				foreach (HospitalModel hospital in AuthenticatedUser.Hospitals)
				{
					string id = Convert.ToBase64String(Encoding.UTF8.GetBytes(hospital.Id));
					List<HospitalInfoResponseModel> list = hospitalInfoList;
					list.Add(await HospitalInfoService.GetHospitalInfo(id));
				}
				SetHospitalsRegistered(AuthenticatedUser.Hospitals, hospitalInfoList);
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【HospitalRegistrationViewModel】【OnActivate】例外発生：{ex}");
				SetHospitalsRegistered(base.UserManager.Hospitals, new List<HospitalInfoResponseModel>());
				await Execute.OnUIThreadAsync(async delegate
				{
					await base.DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
	}

	private void SetHospitalsRegistered(IEnumerable<HospitalModel> hospitals, List<HospitalInfoResponseModel> hospitalInfoList)
	{
		List<bool> list = new List<bool>();
		List<string> list2 = new List<string>();
		List<string> list3 = new List<string>();
		if (hospitals == null)
		{
			for (int i = 0; i < 10; i++)
			{
				list.Add(item: false);
				list2.Add(string.Empty);
				list3.Add(string.Empty);
			}
			IsRegisterable = true;
		}
		else
		{
			HospitalModel[] array = hospitals.ToArray();
			for (int j = 0; j < 10; j++)
			{
				string empty = string.Empty;
				bool flag = false;
				bool flag2 = array.Length > j;
				if (flag2)
				{
					HospitalModel hospital = array[j];
					empty = hospital.FacilitiesName;
					HospitalInfoResponseModel info = hospitalInfoList.FirstOrDefault((HospitalInfoResponseModel x) => x.Hospital.Id == hospital.Id);
					if (info != null)
					{
						HospitalShareSettingModel shareItemSettings = info.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 0);
						IgUserHospitalShareItemModel igUserHospitalShareItemModel = info.IgUserHospitalShareItems.FirstOrDefault((IgUserHospitalShareItemModel x) => x.ItemId == shareItemSettings?.Id);
						bool valueOrDefault = shareItemSettings?.SelectItems?.Contains("glucose") == true;
						bool valueOrDefault2 = shareItemSettings?.SelectItems?.Contains("sphygmomanometer") == true;
						bool valueOrDefault3 = shareItemSettings?.SelectItems?.Contains("temperature") == true;
						bool valueOrDefault4 = shareItemSettings?.SelectItems?.Contains("compositionMeter") == true;
						bool valueOrDefault5 = shareItemSettings?.SelectItems?.Contains("stepMeter") == true;
						bool valueOrDefault6 = shareItemSettings?.SelectItems?.Contains("photograph") == true;
						bool valueOrDefault7 = shareItemSettings?.SelectItems?.Contains("comment") == true;
						bool flag3 = igUserHospitalShareItemModel?.Value.Contains("glucose") ?? true;
						bool flag4 = igUserHospitalShareItemModel?.Value.Contains("sphygmomanometer") ?? true;
						bool flag5 = igUserHospitalShareItemModel?.Value.Contains("temperature") ?? true;
						bool flag6 = igUserHospitalShareItemModel?.Value.Contains("compositionMeter") ?? true;
						bool flag7 = igUserHospitalShareItemModel?.Value.Contains("stepMeter") ?? true;
						bool flag8 = igUserHospitalShareItemModel?.Value.Contains("photograph") ?? true;
						bool flag9 = igUserHospitalShareItemModel?.Value.Contains("comment") ?? true;
						flag = (valueOrDefault && !flag3) || (valueOrDefault2 && !flag4) || (valueOrDefault3 && !flag5) || (valueOrDefault4 && !flag6) || (valueOrDefault5 && !flag7) || (valueOrDefault6 && !flag8) || (valueOrDefault7 && !flag9) || info.HospitalShareSettings.Any(delegate(HospitalShareSettingModel setting)
						{
							IgUserHospitalShareItemModel igUserHospitalShareItemModel2 = info.IgUserHospitalShareItems.FirstOrDefault((IgUserHospitalShareItemModel x) => x.ItemId == setting?.Id);
							return igUserHospitalShareItemModel2 == null || (setting.RequiredFlg && string.IsNullOrEmpty(igUserHospitalShareItemModel2.Value));
						});
					}
				}
				list.Add(flag2);
				list2.Add(flag ? "⚠" : string.Empty);
				list3.Add(empty);
			}
			IsRegisterable = array.Length < 10;
		}
		IsHospitalsRegistered = list.ToArray();
		HospitalInfoWarn = list2.ToArray();
		RegisteredFacilitiesNames = list3.ToArray();
	}

	async void IHandle<HospitalScannedEvent>.Handle(HospitalScannedEvent message)
	{
		await ExecAsync(async delegate
		{
			HospitalInfoResponseModel hospitalInfo = await HospitalInfoService.GetHospitalInfo(message?.Text?.Trim());
			Execute.OnUIThread(async delegate
			{
				if (string.IsNullOrEmpty(hospitalInfo.Hospital.FacilitiesName))
				{
					await base.DialogProvider.ShowAlert(Resources.ALERT_DIALOG_TITLE, Resources.MESSAGE_003);
				}
				else if (AuthenticatedUser.Hospitals != null && AuthenticatedUser.Hospitals.ToArray().Any((HospitalModel t) => t.Id == hospitalInfo.Hospital.Id))
				{
					await base.DialogProvider.ShowAlert(Resources.ALERT_DIALOG_TITLE, Resources.MESSAGE_012);
				}
				else
				{
					base.NavigationService.For<HospitalDetailViewModel>().WithParam((HospitalDetailViewModel x) => x.HospitalInfo, hospitalInfo).Navigate();
				}
			});
		});
	}
}

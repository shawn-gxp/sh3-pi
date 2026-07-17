using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Properties;
using NHL.Services;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class SettingViewModel : ViewModelBase
{
	public new IDialogProvider DialogProvider { get; set; }

	public bool IsRegisteredIgUser => !string.IsNullOrEmpty(base.UserManager.IgUser.Name);

	public async Task NoticeList()
	{
		await Navigate<NoticeListViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task UserInfo()
	{
		await Navigate<UserInfoUpdateViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task Timezone()
	{
		await Navigate<TimeZoneUpdateViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task TimezoneOther()
	{
		await Navigate<TimeZoneOtherUpdateViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task GlucoseTarget()
	{
		await Navigate<GlucoseTargetUpdateViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task Meter()
	{
		await Navigate<MeterViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task Hospital()
	{
		await Navigate<HospitalRegistrationViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task SharerInfo()
	{
		if (!IsRegisteredIgUser)
		{
			await DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_013);
			return;
		}
		await Navigate<SharerInformationViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task BackupCode()
	{
		await Execute.OnUIThreadAsync(async delegate
		{
			base.NavigationService.For<BackupCodeDisplayViewModel>().WithParam((BackupCodeDisplayViewModel x) => x.NavigateFrom, "menu").Navigate();
		});
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task Information()
	{
		await Navigate<InformationViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task ManualDataSync()
	{
		await Navigate<ManualDataSyncViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public void TermsOfService()
	{
		Device.OpenUri(new Uri("https://api.niprogenkinote.jp/TermsOfService.html"));
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async Task ContactUs()
	{
		await Navigate<ContactUsViewModel>();
		base.EventAggregator.PublishOnUIThread(new MenuNavigatedEvent());
	}

	public async void LogFileView()
	{
		await Navigate<LogViewModel>();
	}

	protected override void OnInitialize()
	{
		base.OnInitialize();
	}

	protected override void OnActivate()
	{
		base.OnActivate();
	}
}

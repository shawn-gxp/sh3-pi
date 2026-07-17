using System;
using System.Collections.Generic;
using NHL.Properties;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class ContactUsViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	public const string CONTACT_US_EMAIL = "support@niprogenkinote.jp";

	private string email;

	public new IDialogProvider DialogProvider { get; set; }

	public string Email
	{
		get
		{
			return email;
		}
		set
		{
			email = value;
			NotifyOfPropertyChange(() => Email);
		}
	}

	public async void StartMailer()
	{
		try
		{
			IMailService mailService = DependencyService.Get<IMailService>();
			if (mailService == null || !mailService.StartMailer(new List<string> { Email }))
			{
				await DialogProvider.ShowAlert(Resources.ALERT_DIALOG_TITLE, Resources.MESSAGE_006);
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【ContactUsViewModel】【StartMailer】例外発生：{ex}");
			await DialogProvider.ShowAlert(Resources.ALERT_DIALOG_TITLE, Resources.MESSAGE_006);
		}
	}

	protected override void OnInitialize()
	{
		Email = "support@niprogenkinote.jp";
		base.OnInitialize();
	}
}

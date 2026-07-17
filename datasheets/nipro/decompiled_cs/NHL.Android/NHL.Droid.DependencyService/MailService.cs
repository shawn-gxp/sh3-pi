using System.Collections.Generic;
using Android.Content;
using Android.Net;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class MailService : IMailService
{
	public bool StartMailer(List<string> to)
	{
		Intent intent = new Intent();
		intent.SetAction("android.intent.action.SENDTO");
		intent.AddFlags(ActivityFlags.NewTask);
		intent.SetType("text/plain");
		intent.SetData(Uri.Parse("mailto:"));
		intent.PutExtra("android.intent.extra.EMAIL", to.ToArray());
		Forms.Context.StartActivity(intent);
		return true;
	}
}

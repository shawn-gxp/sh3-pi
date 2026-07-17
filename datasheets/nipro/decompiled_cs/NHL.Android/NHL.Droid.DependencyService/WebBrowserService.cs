using System;
using Android.Content;
using Android.Net;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class WebBrowserService : IWebBrowserService
{
	public void Open(System.Uri uri)
	{
		Forms.Context.StartActivity(new Intent("android.intent.action.VIEW", Android.Net.Uri.Parse(uri.AbsoluteUri)));
	}
}

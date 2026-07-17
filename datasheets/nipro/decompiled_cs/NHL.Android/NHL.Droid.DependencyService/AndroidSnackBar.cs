using System;
using Android.App;
using Android.Graphics;
using Android.Views;
using Google.Android.Material.Snackbar;
using NHL.Services.DependencyService;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.DependencyService;

public class AndroidSnackBar : ISnackBar
{
	public static Android.Graphics.Color ActionColor { get; set; } = new Xamarin.Forms.Color(0.949999988079071, 0.7599999904632568, 0.20000000298023224).ToAndroid();

	public void Show(string text, int duration, string actionText, Action action)
	{
		Android.Views.View view = ((Activity)Forms.Context).FindViewById(16908290);
		Snackbar snackbar = Snackbar.Make(view, text, duration);
		Action<Android.Views.View> clickHandler = delegate
		{
			action();
			snackbar.SetDuration(0);
		};
		snackbar.SetAction(actionText, clickHandler);
		snackbar.SetActionTextColor(ActionColor);
		snackbar.Show();
	}
}

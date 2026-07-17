using System;
using Android.Content;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class ClipBoard : IClipBoard
{
	public bool Copy(string text)
	{
		try
		{
			ClipboardManager obj = (ClipboardManager)Forms.Context.GetSystemService("clipboard");
			ClipData primaryClip = ClipData.NewPlainText(string.Empty, text);
			obj.PrimaryClip = primaryClip;
		}
		catch (Exception)
		{
			return false;
		}
		return true;
	}
}

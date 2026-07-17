using Android.Graphics;
using Android.Widget;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Effect;

public class UnderlineEffect : PlatformEffect
{
	protected override void OnAttached()
	{
		if (base.Control is TextView textView)
		{
			textView.PaintFlags |= PaintFlags.UnderlineText;
		}
	}

	protected override void OnDetached()
	{
	}
}

using Android.OS;
using Android.Widget;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Effect;

public class FlatEntryEffect : PlatformEffect
{
	protected override void OnAttached()
	{
		if (base.Control is TextView textView && Build.VERSION.SdkInt != BuildVersionCodes.Lollipop)
		{
			textView.StateListAnimator = null;
			textView.SetBackground(null);
		}
	}

	protected override void OnDetached()
	{
	}
}

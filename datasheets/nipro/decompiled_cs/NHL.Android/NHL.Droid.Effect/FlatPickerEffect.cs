using Android.OS;
using Android.Widget;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Effect;

public class FlatPickerEffect : PlatformEffect
{
	protected override void OnAttached()
	{
		if (base.Control is NumberPicker numberPicker)
		{
			if (Build.VERSION.SdkInt != BuildVersionCodes.Lollipop)
			{
				numberPicker.StateListAnimator = null;
			}
			numberPicker.SetBackground(null);
		}
	}

	protected override void OnDetached()
	{
	}
}

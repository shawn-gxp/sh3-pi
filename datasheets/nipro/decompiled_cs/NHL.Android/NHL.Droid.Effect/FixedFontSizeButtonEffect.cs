using Android.App;
using Android.Util;
using Android.Widget;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Effect;

public class FixedFontSizeButtonEffect : PlatformEffect
{
	private int fixedSize = 14;

	protected override void OnAttached()
	{
		if (!(base.Control is Android.Widget.Button button))
		{
			return;
		}
		DisplayMetrics displayMetrics = Android.App.Application.Context.Resources.DisplayMetrics;
		float density = displayMetrics.Density;
		float scaledDensity = displayMetrics.ScaledDensity;
		float num = scaledDensity / density;
		if (num == 1f && density == scaledDensity)
		{
			return;
		}
		foreach (Xamarin.Forms.Effect effect in base.Element.Effects)
		{
			if (effect is NHL.Views.Effect.FixedFontSizeButtonEffect fixedFontSizeButtonEffect)
			{
				if (!fixedFontSizeButtonEffect.IsScaleEnabled)
				{
					(base.Element as Xamarin.Forms.Button).FontSize /= num;
					return;
				}
				fixedSize = fixedFontSizeButtonEffect.FixedSize;
				break;
			}
		}
		int num2 = (int)((float)fixedSize * num);
		button.SetTextSize(ComplexUnitType.Dip, num2);
	}

	protected override void OnDetached()
	{
	}
}

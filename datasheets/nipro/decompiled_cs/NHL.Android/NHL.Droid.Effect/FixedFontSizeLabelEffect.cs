using Android.App;
using Android.Util;
using Android.Widget;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Effect;

public class FixedFontSizeLabelEffect : PlatformEffect
{
	private int fixedSize = 14;

	protected override void OnAttached()
	{
		if (!(base.Control is TextView textView))
		{
			return;
		}
		DisplayMetrics displayMetrics = Android.App.Application.Context.Resources.DisplayMetrics;
		float density = displayMetrics.Density;
		float scaledDensity = displayMetrics.ScaledDensity;
		float num = scaledDensity / density;
		if (num == 1f && density == scaledDensity && scaledDensity == (float)(int)scaledDensity)
		{
			return;
		}
		foreach (Xamarin.Forms.Effect effect in base.Element.Effects)
		{
			if (effect is NHL.Views.Effect.FixedFontSizeLabelEffect fixedFontSizeLabelEffect)
			{
				if (!fixedFontSizeLabelEffect.IsScaleEnabled)
				{
					(base.Element as Label).FontSize /= num;
					return;
				}
				fixedSize = fixedFontSizeLabelEffect.FixedSize;
				break;
			}
		}
		int num2 = (int)((float)fixedSize * num);
		textView.SetTextSize(ComplexUnitType.Dip, num2);
	}

	protected override void OnDetached()
	{
	}
}

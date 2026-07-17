using Xamarin.Forms;

namespace NHL.Views.Effect;

public class FixedFontSizeButtonEffect : RoutingEffect
{
	public int FixedSize { get; set; } = 14;

	public bool IsScaleEnabled { get; set; }

	public FixedFontSizeButtonEffect()
		: base("GxP.FixedFontSizeButtonEffect")
	{
	}
}

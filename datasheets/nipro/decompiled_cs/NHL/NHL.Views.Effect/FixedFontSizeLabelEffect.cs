using Xamarin.Forms;

namespace NHL.Views.Effect;

public class FixedFontSizeLabelEffect : RoutingEffect
{
	public int FixedSize { get; set; } = 14;

	public bool IsScaleEnabled { get; set; }

	public FixedFontSizeLabelEffect()
		: base("GxP.FixedFontSizeLabelEffect")
	{
	}
}

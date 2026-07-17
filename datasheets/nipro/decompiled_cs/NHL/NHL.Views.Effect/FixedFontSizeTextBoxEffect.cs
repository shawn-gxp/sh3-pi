using Xamarin.Forms;

namespace NHL.Views.Effect;

public class FixedFontSizeTextBoxEffect : RoutingEffect
{
	public int FixedSize { get; set; } = 14;

	public bool IsScaleEnabled { get; set; } = true;

	public FixedFontSizeTextBoxEffect()
		: base("GxP.FixedFontSizeTextBoxEffect")
	{
	}
}

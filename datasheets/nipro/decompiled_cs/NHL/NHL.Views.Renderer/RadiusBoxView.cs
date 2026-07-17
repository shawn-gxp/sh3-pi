using Xamarin.Forms;

namespace NHL.Views.Renderer;

public class RadiusBoxView : BoxView
{
	public float Radius { get; set; }

	public int LineWidth { get; set; }

	public Color StrokeColor { get; set; }

	public Color FillColor { get; set; }
}

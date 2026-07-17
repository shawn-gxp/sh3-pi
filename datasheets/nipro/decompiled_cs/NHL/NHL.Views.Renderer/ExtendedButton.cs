using Xamarin.Forms;

namespace NHL.Views.Renderer;

public class ExtendedButton : Button
{
	public static BindableProperty HorizontalTextAlignmentProperty = BindableProperty.Create((ExtendedButton x) => x.HorizontalTextAlignment, TextAlignment.Center);

	public TextAlignment HorizontalTextAlignment
	{
		get
		{
			return (TextAlignment)GetValue(HorizontalTextAlignmentProperty);
		}
		set
		{
			SetValue(HorizontalTextAlignmentProperty, value);
		}
	}
}

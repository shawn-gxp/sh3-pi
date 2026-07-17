using System.ComponentModel;
using NHL.Views.Renderer;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;
using Xamarin.Forms.Platform.Android.AppCompat;

namespace NHL.Droid.Renderer;

public class ExtendedButtonRenderer : Xamarin.Forms.Platform.Android.AppCompat.ButtonRenderer
{
	public new ExtendedButton Element => (ExtendedButton)base.Element;

	protected override void OnElementChanged(ElementChangedEventArgs<Button> e)
	{
		base.OnElementChanged(e);
		if (e.NewElement != null)
		{
			SetTextAlignment();
		}
	}

	protected override void OnElementPropertyChanged(object sender, PropertyChangedEventArgs e)
	{
		base.OnElementPropertyChanged(sender, e);
		if (e.PropertyName == ExtendedButton.HorizontalTextAlignmentProperty.PropertyName)
		{
			SetTextAlignment();
		}
	}

	public void SetTextAlignment()
	{
		base.Control.Gravity = Element.HorizontalTextAlignment.ToHorizontalGravityFlags();
	}
}

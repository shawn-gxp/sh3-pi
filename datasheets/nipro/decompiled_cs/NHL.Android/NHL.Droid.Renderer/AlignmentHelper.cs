using Android.Views;
using Xamarin.Forms;

namespace NHL.Droid.Renderer;

public static class AlignmentHelper
{
	public static GravityFlags ToHorizontalGravityFlags(this Xamarin.Forms.TextAlignment alignment)
	{
		return alignment switch
		{
			Xamarin.Forms.TextAlignment.Center => GravityFlags.AxisSpecified, 
			Xamarin.Forms.TextAlignment.End => GravityFlags.Right, 
			_ => GravityFlags.Left, 
		};
	}
}

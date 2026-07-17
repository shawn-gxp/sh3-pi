using Android.Views;

namespace NHL.Droid.Renderer.Utils;

internal static class MeasureSpecFactory
{
	public static int GetSize(int measureSpec)
	{
		return measureSpec & 0x3FFFFFFF;
	}

	public static int MakeMeasureSpec(int size, MeasureSpecMode mode)
	{
		return (int)(size + mode);
	}
}

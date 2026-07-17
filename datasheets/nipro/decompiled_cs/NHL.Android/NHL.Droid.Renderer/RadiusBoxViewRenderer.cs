using Android.Graphics;
using Android.Views;
using NHL.Views.Renderer;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Renderer;

public class RadiusBoxViewRenderer : BoxRenderer
{
	public override void Draw(Canvas canvas)
	{
		RadiusBoxView radiusBoxView = (RadiusBoxView)base.Element;
		using Paint paint = new Paint();
		RectF rect = new RectF(radiusBoxView.LineWidth, radiusBoxView.LineWidth, ((View)(object)this).Width - radiusBoxView.LineWidth, ((View)(object)this).Height - radiusBoxView.LineWidth);
		float num = (float)(((View)(object)this).Width / 2) * (radiusBoxView.Radius / 50f);
		paint.Color = radiusBoxView.FillColor.ToAndroid();
		canvas.DrawRoundRect(rect, num, num, paint);
		paint.SetStyle(Paint.Style.Stroke);
		paint.StrokeWidth = radiusBoxView.LineWidth;
		paint.Color = radiusBoxView.StrokeColor.ToAndroid();
		paint.AntiAlias = true;
		canvas.DrawRoundRect(rect, num, num, paint);
	}
}

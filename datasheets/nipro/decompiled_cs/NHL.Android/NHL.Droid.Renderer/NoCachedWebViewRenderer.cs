using Android.Webkit;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Renderer;

internal class NoCachedWebViewRenderer : WebViewRenderer
{
	protected override void OnElementChanged(ElementChangedEventArgs<Xamarin.Forms.WebView> e)
	{
		base.OnElementChanged(e);
		if (base.Control != null)
		{
			base.Control.ClearCache(includeDiskFiles: true);
			base.Control.Settings.SetAppCacheEnabled(flag: false);
			base.Control.Settings.CacheMode = CacheModes.NoCache;
		}
	}
}

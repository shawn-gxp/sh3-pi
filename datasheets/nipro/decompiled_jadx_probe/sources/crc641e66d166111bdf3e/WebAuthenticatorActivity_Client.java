package crc641e66d166111bdf3e;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class WebAuthenticatorActivity_Client extends WebViewClient implements IGCUserPeer {
    public static final String __md_methods = "n_shouldOverrideUrlLoading:(Landroid/webkit/WebView;Ljava/lang/String;)Z:GetShouldOverrideUrlLoading_Landroid_webkit_WebView_Ljava_lang_String_Handler\nn_onPageStarted:(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V:GetOnPageStarted_Landroid_webkit_WebView_Ljava_lang_String_Landroid_graphics_Bitmap_Handler\nn_onPageFinished:(Landroid/webkit/WebView;Ljava/lang/String;)V:GetOnPageFinished_Landroid_webkit_WebView_Ljava_lang_String_Handler\nn_onReceivedSslError:(Landroid/webkit/WebView;Landroid/webkit/SslErrorHandler;Landroid/net/http/SslError;)V:GetOnReceivedSslError_Landroid_webkit_WebView_Landroid_webkit_SslErrorHandler_Landroid_net_http_SslError_Handler\n";
    private ArrayList refList;

    private native void n_onPageFinished(WebView webView, String str);

    private native void n_onPageStarted(WebView webView, String str, Bitmap bitmap);

    private native void n_onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError);

    private native boolean n_shouldOverrideUrlLoading(WebView webView, String str);

    static {
        Runtime.register("Xamarin.Auth._MobileServices.WebAuthenticatorActivity+Client, Microsoft.Azure.Mobile.Client", WebAuthenticatorActivity_Client.class, __md_methods);
    }

    public WebAuthenticatorActivity_Client() {
        if (WebAuthenticatorActivity_Client.class == WebAuthenticatorActivity_Client.class) {
            TypeManager.Activate("Xamarin.Auth._MobileServices.WebAuthenticatorActivity+Client, Microsoft.Azure.Mobile.Client", "", this, new Object[0]);
        }
    }

    public WebAuthenticatorActivity_Client(WebAuthenticatorActivity webAuthenticatorActivity) {
        if (WebAuthenticatorActivity_Client.class == WebAuthenticatorActivity_Client.class) {
            TypeManager.Activate("Xamarin.Auth._MobileServices.WebAuthenticatorActivity+Client, Microsoft.Azure.Mobile.Client", "Xamarin.Auth._MobileServices.WebAuthenticatorActivity, Microsoft.Azure.Mobile.Client", this, new Object[]{webAuthenticatorActivity});
        }
    }

    @Override // android.webkit.WebViewClient
    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        return n_shouldOverrideUrlLoading(webView, str);
    }

    @Override // android.webkit.WebViewClient
    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        n_onPageStarted(webView, str, bitmap);
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView webView, String str) {
        n_onPageFinished(webView, str);
    }

    @Override // android.webkit.WebViewClient
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        n_onReceivedSslError(webView, sslErrorHandler, sslError);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

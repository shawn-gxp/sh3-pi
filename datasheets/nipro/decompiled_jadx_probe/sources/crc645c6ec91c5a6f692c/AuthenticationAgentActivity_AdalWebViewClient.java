package crc645c6ec91c5a6f692c;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AuthenticationAgentActivity_AdalWebViewClient extends WebViewClient implements IGCUserPeer {
    public static final String __md_methods = "n_onLoadResource:(Landroid/webkit/WebView;Ljava/lang/String;)V:GetOnLoadResource_Landroid_webkit_WebView_Ljava_lang_String_Handler\nn_shouldOverrideUrlLoading:(Landroid/webkit/WebView;Ljava/lang/String;)Z:GetShouldOverrideUrlLoading_Landroid_webkit_WebView_Ljava_lang_String_Handler\nn_onPageFinished:(Landroid/webkit/WebView;Ljava/lang/String;)V:GetOnPageFinished_Landroid_webkit_WebView_Ljava_lang_String_Handler\nn_onPageStarted:(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V:GetOnPageStarted_Landroid_webkit_WebView_Ljava_lang_String_Landroid_graphics_Bitmap_Handler\n";
    private ArrayList refList;

    private native void n_onLoadResource(WebView webView, String str);

    private native void n_onPageFinished(WebView webView, String str);

    private native void n_onPageStarted(WebView webView, String str, Bitmap bitmap);

    private native boolean n_shouldOverrideUrlLoading(WebView webView, String str);

    static {
        Runtime.register("Microsoft.IdentityModel.Clients.ActiveDirectory.AuthenticationAgentActivity+AdalWebViewClient, Microsoft.IdentityModel.Clients.ActiveDirectory", AuthenticationAgentActivity_AdalWebViewClient.class, __md_methods);
    }

    public AuthenticationAgentActivity_AdalWebViewClient() {
        if (AuthenticationAgentActivity_AdalWebViewClient.class == AuthenticationAgentActivity_AdalWebViewClient.class) {
            TypeManager.Activate("Microsoft.IdentityModel.Clients.ActiveDirectory.AuthenticationAgentActivity+AdalWebViewClient, Microsoft.IdentityModel.Clients.ActiveDirectory", "", this, new Object[0]);
        }
    }

    public AuthenticationAgentActivity_AdalWebViewClient(String str) {
        if (AuthenticationAgentActivity_AdalWebViewClient.class == AuthenticationAgentActivity_AdalWebViewClient.class) {
            TypeManager.Activate("Microsoft.IdentityModel.Clients.ActiveDirectory.AuthenticationAgentActivity+AdalWebViewClient, Microsoft.IdentityModel.Clients.ActiveDirectory", "System.String, mscorlib", this, new Object[]{str});
        }
    }

    @Override // android.webkit.WebViewClient
    public void onLoadResource(WebView webView, String str) {
        n_onLoadResource(webView, str);
    }

    @Override // android.webkit.WebViewClient
    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        return n_shouldOverrideUrlLoading(webView, str);
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView webView, String str) {
        n_onPageFinished(webView, str);
    }

    @Override // android.webkit.WebViewClient
    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        n_onPageStarted(webView, str, bitmap);
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

package crc64396a3fe5f8138e3f;

import android.content.ComponentName;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CustomTabsServiceConnectionImpl extends CustomTabsServiceConnection implements IGCUserPeer {
    public static final String __md_methods = "n_onCustomTabsServiceConnected:(Landroid/content/ComponentName;Landroidx/browser/customtabs/CustomTabsClient;)V:GetOnCustomTabsServiceConnected_Landroid_content_ComponentName_Landroidx_browser_customtabs_CustomTabsClient_Handler\nn_onServiceDisconnected:(Landroid/content/ComponentName;)V:GetOnServiceDisconnected_Landroid_content_ComponentName_Handler\n";
    private ArrayList refList;

    private native void n_onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient);

    private native void n_onServiceDisconnected(ComponentName componentName);

    static {
        Runtime.register("AndroidX.Browser.CustomTabs.CustomTabsServiceConnectionImpl, Xamarin.AndroidX.Browser", CustomTabsServiceConnectionImpl.class, __md_methods);
    }

    public CustomTabsServiceConnectionImpl() {
        if (CustomTabsServiceConnectionImpl.class == CustomTabsServiceConnectionImpl.class) {
            TypeManager.Activate("AndroidX.Browser.CustomTabs.CustomTabsServiceConnectionImpl, Xamarin.AndroidX.Browser", "", this, new Object[0]);
        }
    }

    @Override // androidx.browser.customtabs.CustomTabsServiceConnection
    public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
        n_onCustomTabsServiceConnected(componentName, customTabsClient);
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName componentName) {
        n_onServiceDisconnected(componentName);
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

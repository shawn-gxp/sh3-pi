package androidx.browser.customtabs;

import android.os.Bundle;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CustomTabsClient_CustomTabsCallbackImpl extends CustomTabsCallback implements IGCUserPeer {
    public static final String __md_methods = "n_onNavigationEvent:(ILandroid/os/Bundle;)V:GetOnNavigationEvent_ILandroid_os_Bundle_Handler\nn_extraCallback:(Ljava/lang/String;Landroid/os/Bundle;)V:GetExtraCallback_Ljava_lang_String_Landroid_os_Bundle_Handler\n";
    private ArrayList refList;

    private native void n_extraCallback(String str, Bundle bundle);

    private native void n_onNavigationEvent(int i, Bundle bundle);

    static {
        Runtime.register("AndroidX.Browser.CustomTabs.CustomTabsClient+CustomTabsCallbackImpl, Xamarin.AndroidX.Browser", CustomTabsClient_CustomTabsCallbackImpl.class, __md_methods);
    }

    public CustomTabsClient_CustomTabsCallbackImpl() {
        if (CustomTabsClient_CustomTabsCallbackImpl.class == CustomTabsClient_CustomTabsCallbackImpl.class) {
            TypeManager.Activate("AndroidX.Browser.CustomTabs.CustomTabsClient+CustomTabsCallbackImpl, Xamarin.AndroidX.Browser", "", this, new Object[0]);
        }
    }

    @Override // androidx.browser.customtabs.CustomTabsCallback
    public void onNavigationEvent(int i, Bundle bundle) {
        n_onNavigationEvent(i, bundle);
    }

    @Override // androidx.browser.customtabs.CustomTabsCallback
    public void extraCallback(String str, Bundle bundle) {
        n_extraCallback(str, bundle);
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

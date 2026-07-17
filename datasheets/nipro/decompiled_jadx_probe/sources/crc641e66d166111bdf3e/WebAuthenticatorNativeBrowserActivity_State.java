package crc641e66d166111bdf3e;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class WebAuthenticatorNativeBrowserActivity_State implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("Xamarin.Auth._MobileServices.WebAuthenticatorNativeBrowserActivity+State, Microsoft.Azure.Mobile.Client", WebAuthenticatorNativeBrowserActivity_State.class, "");
    }

    public WebAuthenticatorNativeBrowserActivity_State() {
        if (WebAuthenticatorNativeBrowserActivity_State.class == WebAuthenticatorNativeBrowserActivity_State.class) {
            TypeManager.Activate("Xamarin.Auth._MobileServices.WebAuthenticatorNativeBrowserActivity+State, Microsoft.Azure.Mobile.Client", "", this, new Object[0]);
        }
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

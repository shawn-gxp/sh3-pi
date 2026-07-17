package mono.com.microsoft.appcenter.utils;

import com.microsoft.appcenter.utils.NetworkStateHelper;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class NetworkStateHelper_ListenerImplementor implements IGCUserPeer, NetworkStateHelper.Listener {
    public static final String __md_methods = "n_onNetworkStateUpdated:(Z)V:GetOnNetworkStateUpdated_ZHandler:Com.Microsoft.Appcenter.Utils.NetworkStateHelper/IListenerInvoker, Microsoft.AppCenter.Android.Bindings\n";
    private ArrayList refList;

    private native void n_onNetworkStateUpdated(boolean z);

    static {
        Runtime.register("Com.Microsoft.Appcenter.Utils.NetworkStateHelper+IListenerImplementor, Microsoft.AppCenter.Android.Bindings", NetworkStateHelper_ListenerImplementor.class, __md_methods);
    }

    public NetworkStateHelper_ListenerImplementor() {
        if (NetworkStateHelper_ListenerImplementor.class == NetworkStateHelper_ListenerImplementor.class) {
            TypeManager.Activate("Com.Microsoft.Appcenter.Utils.NetworkStateHelper+IListenerImplementor, Microsoft.AppCenter.Android.Bindings", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.appcenter.utils.NetworkStateHelper.Listener
    public void onNetworkStateUpdated(boolean z) {
        n_onNetworkStateUpdated(z);
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

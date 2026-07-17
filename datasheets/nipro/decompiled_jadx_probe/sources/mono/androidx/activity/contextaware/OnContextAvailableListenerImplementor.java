package mono.androidx.activity.contextaware;

import android.content.Context;
import androidx.activity.contextaware.OnContextAvailableListener;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class OnContextAvailableListenerImplementor implements IGCUserPeer, OnContextAvailableListener {
    public static final String __md_methods = "n_onContextAvailable:(Landroid/content/Context;)V:GetOnContextAvailable_Landroid_content_Context_Handler:AndroidX.Activity.ContextAware.IOnContextAvailableListenerInvoker, Xamarin.AndroidX.Activity\n";
    private ArrayList refList;

    private native void n_onContextAvailable(Context context);

    static {
        Runtime.register("AndroidX.Activity.ContextAware.IOnContextAvailableListenerImplementor, Xamarin.AndroidX.Activity", OnContextAvailableListenerImplementor.class, __md_methods);
    }

    public OnContextAvailableListenerImplementor() {
        if (OnContextAvailableListenerImplementor.class == OnContextAvailableListenerImplementor.class) {
            TypeManager.Activate("AndroidX.Activity.ContextAware.IOnContextAvailableListenerImplementor, Xamarin.AndroidX.Activity", "", this, new Object[0]);
        }
    }

    @Override // androidx.activity.contextaware.OnContextAvailableListener
    public void onContextAvailable(Context context) {
        n_onContextAvailable(context);
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

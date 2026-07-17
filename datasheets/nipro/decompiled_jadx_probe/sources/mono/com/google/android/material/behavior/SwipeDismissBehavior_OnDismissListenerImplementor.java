package mono.com.google.android.material.behavior;

import android.view.View;
import com.google.android.material.behavior.SwipeDismissBehavior;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class SwipeDismissBehavior_OnDismissListenerImplementor implements IGCUserPeer, SwipeDismissBehavior.OnDismissListener {
    public static final String __md_methods = "n_onDismiss:(Landroid/view/View;)V:GetOnDismiss_Landroid_view_View_Handler:Google.Android.Material.Behavior.SwipeDismissBehavior/IOnDismissListenerInvoker, Xamarin.Google.Android.Material\nn_onDragStateChanged:(I)V:GetOnDragStateChanged_IHandler:Google.Android.Material.Behavior.SwipeDismissBehavior/IOnDismissListenerInvoker, Xamarin.Google.Android.Material\n";
    private ArrayList refList;

    private native void n_onDismiss(View view);

    private native void n_onDragStateChanged(int i);

    static {
        Runtime.register("Google.Android.Material.Behavior.SwipeDismissBehavior+IOnDismissListenerImplementor, Xamarin.Google.Android.Material", SwipeDismissBehavior_OnDismissListenerImplementor.class, __md_methods);
    }

    public SwipeDismissBehavior_OnDismissListenerImplementor() {
        if (SwipeDismissBehavior_OnDismissListenerImplementor.class == SwipeDismissBehavior_OnDismissListenerImplementor.class) {
            TypeManager.Activate("Google.Android.Material.Behavior.SwipeDismissBehavior+IOnDismissListenerImplementor, Xamarin.Google.Android.Material", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.material.behavior.SwipeDismissBehavior.OnDismissListener
    public void onDismiss(View view) {
        n_onDismiss(view);
    }

    @Override // com.google.android.material.behavior.SwipeDismissBehavior.OnDismissListener
    public void onDragStateChanged(int i) {
        n_onDragStateChanged(i);
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

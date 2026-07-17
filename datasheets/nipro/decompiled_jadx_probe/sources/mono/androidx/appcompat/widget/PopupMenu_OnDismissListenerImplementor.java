package mono.androidx.appcompat.widget;

import androidx.appcompat.widget.PopupMenu;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class PopupMenu_OnDismissListenerImplementor implements IGCUserPeer, PopupMenu.OnDismissListener {
    public static final String __md_methods = "n_onDismiss:(Landroidx/appcompat/widget/PopupMenu;)V:GetOnDismiss_Landroidx_appcompat_widget_PopupMenu_Handler:AndroidX.AppCompat.Widget.PopupMenu/IOnDismissListenerInvoker, Xamarin.AndroidX.AppCompat\n";
    private ArrayList refList;

    private native void n_onDismiss(PopupMenu popupMenu);

    static {
        Runtime.register("AndroidX.AppCompat.Widget.PopupMenu+IOnDismissListenerImplementor, Xamarin.AndroidX.AppCompat", PopupMenu_OnDismissListenerImplementor.class, __md_methods);
    }

    public PopupMenu_OnDismissListenerImplementor() {
        if (PopupMenu_OnDismissListenerImplementor.class == PopupMenu_OnDismissListenerImplementor.class) {
            TypeManager.Activate("AndroidX.AppCompat.Widget.PopupMenu+IOnDismissListenerImplementor, Xamarin.AndroidX.AppCompat", "", this, new Object[0]);
        }
    }

    @Override // androidx.appcompat.widget.PopupMenu.OnDismissListener
    public void onDismiss(PopupMenu popupMenu) {
        n_onDismiss(popupMenu);
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

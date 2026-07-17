package mono.androidx.appcompat.widget;

import android.view.MenuItem;
import androidx.appcompat.widget.PopupMenu;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class PopupMenu_OnMenuItemClickListenerImplementor implements IGCUserPeer, PopupMenu.OnMenuItemClickListener {
    public static final String __md_methods = "n_onMenuItemClick:(Landroid/view/MenuItem;)Z:GetOnMenuItemClick_Landroid_view_MenuItem_Handler:AndroidX.AppCompat.Widget.PopupMenu/IOnMenuItemClickListenerInvoker, Xamarin.AndroidX.AppCompat\n";
    private ArrayList refList;

    private native boolean n_onMenuItemClick(MenuItem menuItem);

    static {
        Runtime.register("AndroidX.AppCompat.Widget.PopupMenu+IOnMenuItemClickListenerImplementor, Xamarin.AndroidX.AppCompat", PopupMenu_OnMenuItemClickListenerImplementor.class, __md_methods);
    }

    public PopupMenu_OnMenuItemClickListenerImplementor() {
        if (PopupMenu_OnMenuItemClickListenerImplementor.class == PopupMenu_OnMenuItemClickListenerImplementor.class) {
            TypeManager.Activate("AndroidX.AppCompat.Widget.PopupMenu+IOnMenuItemClickListenerImplementor, Xamarin.AndroidX.AppCompat", "", this, new Object[0]);
        }
    }

    @Override // androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        return n_onMenuItemClick(menuItem);
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

package crc64c24f44e46c13f9f8;

import android.view.View;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CustomViewPropertyAnimatorListenerAdapter extends ViewPropertyAnimatorListenerAdapter implements IGCUserPeer {
    public static final String __md_methods = "n_onAnimationEnd:(Landroid/view/View;)V:GetOnAnimationEnd_Landroid_view_View_Handler\nn_onAnimationCancel:(Landroid/view/View;)V:GetOnAnimationCancel_Landroid_view_View_Handler\n";
    private ArrayList refList;

    private native void n_onAnimationCancel(View view);

    private native void n_onAnimationEnd(View view);

    static {
        Runtime.register("BottomNavigationBar.Adapters.CustomViewPropertyAnimatorListenerAdapter, BottomNavigationBar", CustomViewPropertyAnimatorListenerAdapter.class, __md_methods);
    }

    public CustomViewPropertyAnimatorListenerAdapter() {
        if (CustomViewPropertyAnimatorListenerAdapter.class == CustomViewPropertyAnimatorListenerAdapter.class) {
            TypeManager.Activate("BottomNavigationBar.Adapters.CustomViewPropertyAnimatorListenerAdapter, BottomNavigationBar", "", this, new Object[0]);
        }
    }

    public CustomViewPropertyAnimatorListenerAdapter(View view, int i, View view2) {
        if (CustomViewPropertyAnimatorListenerAdapter.class == CustomViewPropertyAnimatorListenerAdapter.class) {
            TypeManager.Activate("BottomNavigationBar.Adapters.CustomViewPropertyAnimatorListenerAdapter, BottomNavigationBar", "Android.Views.View, Mono.Android:System.Int32, mscorlib:Android.Views.View, Mono.Android", this, new Object[]{view, Integer.valueOf(i), view2});
        }
    }

    @Override // androidx.core.view.ViewPropertyAnimatorListenerAdapter, androidx.core.view.ViewPropertyAnimatorListener
    public void onAnimationEnd(View view) {
        n_onAnimationEnd(view);
    }

    @Override // androidx.core.view.ViewPropertyAnimatorListenerAdapter, androidx.core.view.ViewPropertyAnimatorListener
    public void onAnimationCancel(View view) {
        n_onAnimationCancel(view);
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

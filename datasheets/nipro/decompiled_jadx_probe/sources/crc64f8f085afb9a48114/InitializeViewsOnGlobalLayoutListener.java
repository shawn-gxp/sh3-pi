package crc64f8f085afb9a48114;

import android.view.View;
import android.view.ViewTreeObserver;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class InitializeViewsOnGlobalLayoutListener implements IGCUserPeer, ViewTreeObserver.OnGlobalLayoutListener {
    public static final String __md_methods = "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onGlobalLayout();

    static {
        Runtime.register("BottomNavigationBar.Listeners.InitializeViewsOnGlobalLayoutListener, BottomNavigationBar", InitializeViewsOnGlobalLayoutListener.class, "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public InitializeViewsOnGlobalLayoutListener() {
        if (InitializeViewsOnGlobalLayoutListener.class == InitializeViewsOnGlobalLayoutListener.class) {
            TypeManager.Activate("BottomNavigationBar.Listeners.InitializeViewsOnGlobalLayoutListener, BottomNavigationBar", "", this, new Object[0]);
        }
    }

    public InitializeViewsOnGlobalLayoutListener(boolean z, CoordinatorLayout.LayoutParams layoutParams, View view, ViewTreeObserver viewTreeObserver, boolean z2, boolean z3) {
        if (InitializeViewsOnGlobalLayoutListener.class == InitializeViewsOnGlobalLayoutListener.class) {
            TypeManager.Activate("BottomNavigationBar.Listeners.InitializeViewsOnGlobalLayoutListener, BottomNavigationBar", "System.Boolean, mscorlib:AndroidX.CoordinatorLayout.Widget.CoordinatorLayout+LayoutParams, Xamarin.AndroidX.CoordinatorLayout:Android.Views.View, Mono.Android:Android.Views.ViewTreeObserver, Mono.Android:System.Boolean, mscorlib:System.Boolean, mscorlib", this, new Object[]{Boolean.valueOf(z), layoutParams, view, viewTreeObserver, Boolean.valueOf(z2), Boolean.valueOf(z3)});
        }
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
        n_onGlobalLayout();
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

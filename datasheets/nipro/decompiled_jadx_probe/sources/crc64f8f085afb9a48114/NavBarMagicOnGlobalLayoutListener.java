package crc64f8f085afb9a48114;

import android.view.View;
import android.view.ViewTreeObserver;
import crc64949d522a3c2cb8a0.BottomBar;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class NavBarMagicOnGlobalLayoutListener implements IGCUserPeer, ViewTreeObserver.OnGlobalLayoutListener {
    public static final String __md_methods = "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onGlobalLayout();

    static {
        Runtime.register("BottomNavigationBar.Listeners.NavBarMagicOnGlobalLayoutListener, BottomNavigationBar", NavBarMagicOnGlobalLayoutListener.class, "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public NavBarMagicOnGlobalLayoutListener() {
        if (NavBarMagicOnGlobalLayoutListener.class == NavBarMagicOnGlobalLayoutListener.class) {
            TypeManager.Activate("BottomNavigationBar.Listeners.NavBarMagicOnGlobalLayoutListener, BottomNavigationBar", "", this, new Object[0]);
        }
    }

    public NavBarMagicOnGlobalLayoutListener(BottomBar bottomBar, View view, int i, boolean z, boolean z2) {
        if (NavBarMagicOnGlobalLayoutListener.class == NavBarMagicOnGlobalLayoutListener.class) {
            TypeManager.Activate("BottomNavigationBar.Listeners.NavBarMagicOnGlobalLayoutListener, BottomNavigationBar", "BottomNavigationBar.BottomBar, BottomNavigationBar:Android.Views.View, Mono.Android:System.Int32, mscorlib:System.Boolean, mscorlib:System.Boolean, mscorlib", this, new Object[]{bottomBar, view, Integer.valueOf(i), Boolean.valueOf(z), Boolean.valueOf(z2)});
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

package crc64949d522a3c2cb8a0;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class BottomBarBadge extends TextView implements IGCUserPeer, ViewTreeObserver.OnGlobalLayoutListener {
    public static final String __md_methods = "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onGlobalLayout();

    static {
        Runtime.register("BottomNavigationBar.BottomBarBadge, BottomNavigationBar", BottomBarBadge.class, "n_onGlobalLayout:()V:GetOnGlobalLayoutHandler:Android.Views.ViewTreeObserver/IOnGlobalLayoutListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public BottomBarBadge(Context context) {
        super(context);
        if (BottomBarBadge.class == BottomBarBadge.class) {
            TypeManager.Activate("BottomNavigationBar.BottomBarBadge, BottomNavigationBar", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public BottomBarBadge(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (BottomBarBadge.class == BottomBarBadge.class) {
            TypeManager.Activate("BottomNavigationBar.BottomBarBadge, BottomNavigationBar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public BottomBarBadge(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (BottomBarBadge.class == BottomBarBadge.class) {
            TypeManager.Activate("BottomNavigationBar.BottomBarBadge, BottomNavigationBar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
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

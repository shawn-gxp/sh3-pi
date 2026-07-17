package crc64949d522a3c2cb8a0;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class BottomBar extends RelativeLayout implements IGCUserPeer, View.OnClickListener, View.OnLongClickListener {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_onClick:(Landroid/view/View;)V:GetOnClick_Landroid_view_View_Handler:Android.Views.View/IOnClickListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onLongClick:(Landroid/view/View;)Z:GetOnLongClick_Landroid_view_View_Handler:Android.Views.View/IOnLongClickListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onClick(View view);

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native boolean n_onLongClick(View view);

    static {
        Runtime.register("BottomNavigationBar.BottomBar, BottomNavigationBar", BottomBar.class, __md_methods);
    }

    public BottomBar(Context context) {
        super(context);
        if (BottomBar.class == BottomBar.class) {
            TypeManager.Activate("BottomNavigationBar.BottomBar, BottomNavigationBar", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public BottomBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (BottomBar.class == BottomBar.class) {
            TypeManager.Activate("BottomNavigationBar.BottomBar, BottomNavigationBar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public BottomBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (BottomBar.class == BottomBar.class) {
            TypeManager.Activate("BottomNavigationBar.BottomBar, BottomNavigationBar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public BottomBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (BottomBar.class == BottomBar.class) {
            TypeManager.Activate("BottomNavigationBar.BottomBar, BottomNavigationBar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // android.widget.RelativeLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        n_onClick(view);
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        return n_onLongClick(view);
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

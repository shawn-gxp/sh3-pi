package crc645e9136c190843a3c;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class GridFreezingLayer extends FrameLayout implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    static {
        Runtime.register("C1.Android.Grid.GridFreezingLayer, C1.Android.Grid", GridFreezingLayer.class, "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public GridFreezingLayer(Context context) {
        super(context);
        if (GridFreezingLayer.class == GridFreezingLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridFreezingLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public GridFreezingLayer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (GridFreezingLayer.class == GridFreezingLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridFreezingLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public GridFreezingLayer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (GridFreezingLayer.class == GridFreezingLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridFreezingLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public GridFreezingLayer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (GridFreezingLayer.class == GridFreezingLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridFreezingLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
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

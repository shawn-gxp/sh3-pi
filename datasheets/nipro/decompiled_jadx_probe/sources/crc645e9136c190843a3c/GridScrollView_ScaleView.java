package crc645e9136c190843a3c;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class GridScrollView_ScaleView extends ViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("C1.Android.Grid.GridScrollView+ScaleView, C1.Android.Grid", GridScrollView_ScaleView.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public GridScrollView_ScaleView(Context context) {
        super(context);
        if (GridScrollView_ScaleView.class == GridScrollView_ScaleView.class) {
            TypeManager.Activate("C1.Android.Grid.GridScrollView+ScaleView, C1.Android.Grid", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public GridScrollView_ScaleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (GridScrollView_ScaleView.class == GridScrollView_ScaleView.class) {
            TypeManager.Activate("C1.Android.Grid.GridScrollView+ScaleView, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public GridScrollView_ScaleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (GridScrollView_ScaleView.class == GridScrollView_ScaleView.class) {
            TypeManager.Activate("C1.Android.Grid.GridScrollView+ScaleView, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // android.view.ViewGroup, android.view.View
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

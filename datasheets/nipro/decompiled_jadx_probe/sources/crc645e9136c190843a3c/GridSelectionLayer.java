package crc645e9136c190843a3c;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class GridSelectionLayer extends FrameLayout implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("C1.Android.Grid.GridSelectionLayer, C1.Android.Grid", GridSelectionLayer.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public GridSelectionLayer(Context context) {
        super(context);
        if (GridSelectionLayer.class == GridSelectionLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridSelectionLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public GridSelectionLayer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (GridSelectionLayer.class == GridSelectionLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridSelectionLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public GridSelectionLayer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (GridSelectionLayer.class == GridSelectionLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridSelectionLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public GridSelectionLayer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (GridSelectionLayer.class == GridSelectionLayer.class) {
            TypeManager.Activate("C1.Android.Grid.GridSelectionLayer, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
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

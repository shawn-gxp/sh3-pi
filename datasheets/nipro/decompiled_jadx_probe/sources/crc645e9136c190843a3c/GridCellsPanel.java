package crc645e9136c190843a3c;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class GridCellsPanel extends ViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("C1.Android.Grid.GridCellsPanel, C1.Android.Grid", GridCellsPanel.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public GridCellsPanel(Context context) {
        super(context);
        if (GridCellsPanel.class == GridCellsPanel.class) {
            TypeManager.Activate("C1.Android.Grid.GridCellsPanel, C1.Android.Grid", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public GridCellsPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (GridCellsPanel.class == GridCellsPanel.class) {
            TypeManager.Activate("C1.Android.Grid.GridCellsPanel, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public GridCellsPanel(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (GridCellsPanel.class == GridCellsPanel.class) {
            TypeManager.Activate("C1.Android.Grid.GridCellsPanel, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
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

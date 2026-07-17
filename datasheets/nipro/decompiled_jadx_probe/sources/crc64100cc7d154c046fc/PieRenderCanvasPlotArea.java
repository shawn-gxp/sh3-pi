package crc64100cc7d154c046fc;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class PieRenderCanvasPlotArea extends RenderCanvasPlotArea implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("C1.Android.Chart.PieRenderCanvasPlotArea, C1.Android.Chart", PieRenderCanvasPlotArea.class, "");
    }

    public PieRenderCanvasPlotArea(Context context) {
        super(context);
        if (PieRenderCanvasPlotArea.class == PieRenderCanvasPlotArea.class) {
            TypeManager.Activate("C1.Android.Chart.PieRenderCanvasPlotArea, C1.Android.Chart", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public PieRenderCanvasPlotArea(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (PieRenderCanvasPlotArea.class == PieRenderCanvasPlotArea.class) {
            TypeManager.Activate("C1.Android.Chart.PieRenderCanvasPlotArea, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public PieRenderCanvasPlotArea(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (PieRenderCanvasPlotArea.class == PieRenderCanvasPlotArea.class) {
            TypeManager.Activate("C1.Android.Chart.PieRenderCanvasPlotArea, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public PieRenderCanvasPlotArea(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (PieRenderCanvasPlotArea.class == PieRenderCanvasPlotArea.class) {
            TypeManager.Activate("C1.Android.Chart.PieRenderCanvasPlotArea, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // crc64100cc7d154c046fc.RenderCanvasPlotArea, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc64100cc7d154c046fc.RenderCanvasPlotArea, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

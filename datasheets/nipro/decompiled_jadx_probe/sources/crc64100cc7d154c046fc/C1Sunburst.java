package crc64100cc7d154c046fc;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class C1Sunburst extends FlexPie implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    static {
        Runtime.register("C1.Android.Chart.C1Sunburst, C1.Android.Chart", C1Sunburst.class, "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public C1Sunburst(Context context) {
        super(context);
        if (C1Sunburst.class == C1Sunburst.class) {
            TypeManager.Activate("C1.Android.Chart.C1Sunburst, C1.Android.Chart", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public C1Sunburst(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (C1Sunburst.class == C1Sunburst.class) {
            TypeManager.Activate("C1.Android.Chart.C1Sunburst, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public C1Sunburst(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (C1Sunburst.class == C1Sunburst.class) {
            TypeManager.Activate("C1.Android.Chart.C1Sunburst, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public C1Sunburst(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (C1Sunburst.class == C1Sunburst.class) {
            TypeManager.Activate("C1.Android.Chart.C1Sunburst, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // crc64100cc7d154c046fc.FlexPie, crc64100cc7d154c046fc.ChartBase, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // crc64100cc7d154c046fc.FlexPie, crc64100cc7d154c046fc.ChartBase, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc64100cc7d154c046fc.FlexPie, crc64100cc7d154c046fc.ChartBase, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

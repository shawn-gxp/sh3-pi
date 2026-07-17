package crc64f204578e57a29fff;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class C1ToggleButton extends ViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("C1.Android.Core.C1ToggleButton, C1.Android.Core", C1ToggleButton.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public C1ToggleButton(Context context) {
        super(context);
        if (getClass() == C1ToggleButton.class) {
            TypeManager.Activate("C1.Android.Core.C1ToggleButton, C1.Android.Core", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public C1ToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == C1ToggleButton.class) {
            TypeManager.Activate("C1.Android.Core.C1ToggleButton, C1.Android.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public C1ToggleButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == C1ToggleButton.class) {
            TypeManager.Activate("C1.Android.Core.C1ToggleButton, C1.Android.Core", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
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

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

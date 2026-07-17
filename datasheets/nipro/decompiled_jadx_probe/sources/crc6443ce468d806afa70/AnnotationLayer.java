package crc6443ce468d806afa70;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AnnotationLayer extends FrameLayout implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("C1.Android.Chart.Annotation.AnnotationLayer, C1.Android.Chart", AnnotationLayer.class, "");
    }

    public AnnotationLayer(Context context) {
        super(context);
        if (AnnotationLayer.class == AnnotationLayer.class) {
            TypeManager.Activate("C1.Android.Chart.Annotation.AnnotationLayer, C1.Android.Chart", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public AnnotationLayer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (AnnotationLayer.class == AnnotationLayer.class) {
            TypeManager.Activate("C1.Android.Chart.Annotation.AnnotationLayer, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public AnnotationLayer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (AnnotationLayer.class == AnnotationLayer.class) {
            TypeManager.Activate("C1.Android.Chart.Annotation.AnnotationLayer, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public AnnotationLayer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (AnnotationLayer.class == AnnotationLayer.class) {
            TypeManager.Activate("C1.Android.Chart.Annotation.AnnotationLayer, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
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

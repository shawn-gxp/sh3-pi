package crc6498c6b4d776f4c9b6;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class XView_C1ViewRendererWrapper extends ViewGroup implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n";
    private ArrayList refList;

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("C1.Xamarin.Forms.Core.Platform.Android.XView+C1ViewRendererWrapper, C1.Xamarin.Forms.Core.Platform.Android", XView_C1ViewRendererWrapper.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\n");
    }

    public XView_C1ViewRendererWrapper(Context context) {
        super(context);
        if (XView_C1ViewRendererWrapper.class == XView_C1ViewRendererWrapper.class) {
            TypeManager.Activate("C1.Xamarin.Forms.Core.Platform.Android.XView+C1ViewRendererWrapper, C1.Xamarin.Forms.Core.Platform.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public XView_C1ViewRendererWrapper(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (XView_C1ViewRendererWrapper.class == XView_C1ViewRendererWrapper.class) {
            TypeManager.Activate("C1.Xamarin.Forms.Core.Platform.Android.XView+C1ViewRendererWrapper, C1.Xamarin.Forms.Core.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public XView_C1ViewRendererWrapper(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (XView_C1ViewRendererWrapper.class == XView_C1ViewRendererWrapper.class) {
            TypeManager.Activate("C1.Xamarin.Forms.Core.Platform.Android.XView+C1ViewRendererWrapper, C1.Xamarin.Forms.Core.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
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

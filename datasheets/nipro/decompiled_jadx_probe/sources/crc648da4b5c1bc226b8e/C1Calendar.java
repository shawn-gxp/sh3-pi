package crc648da4b5c1bc226b8e;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class C1Calendar extends FrameLayout implements IGCUserPeer {
    public static final String __md_methods = "n_onAttachedToWindow:()V:GetOnAttachedToWindowHandler\nn_onDetachedFromWindow:()V:GetOnDetachedFromWindowHandler\nn_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_setBackgroundColor:(I)V:GetSetBackgroundColor_IHandler\n";
    private ArrayList refList;

    private native void n_onAttachedToWindow();

    private native void n_onDetachedFromWindow();

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    private native void n_onMeasure(int i, int i2);

    private native void n_setBackgroundColor(int i);

    static {
        Runtime.register("C1.Android.Calendar.C1Calendar, C1.Android.Calendar", C1Calendar.class, __md_methods);
    }

    public C1Calendar(Context context) {
        super(context);
        if (C1Calendar.class == C1Calendar.class) {
            TypeManager.Activate("C1.Android.Calendar.C1Calendar, C1.Android.Calendar", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public C1Calendar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (C1Calendar.class == C1Calendar.class) {
            TypeManager.Activate("C1.Android.Calendar.C1Calendar, C1.Android.Calendar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public C1Calendar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (C1Calendar.class == C1Calendar.class) {
            TypeManager.Activate("C1.Android.Calendar.C1Calendar, C1.Android.Calendar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public C1Calendar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (C1Calendar.class == C1Calendar.class) {
            TypeManager.Activate("C1.Android.Calendar.C1Calendar, C1.Android.Calendar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        n_onAttachedToWindow();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        n_onDetachedFromWindow();
    }

    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.View
    public void setBackgroundColor(int i) {
        n_setBackgroundColor(i);
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

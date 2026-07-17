package crc64100cc7d154c046fc;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class PieCanvas extends FrameLayout implements IGCUserPeer {
    public static final String __md_methods = "n_onDraw:(Landroid/graphics/Canvas;)V:GetOnDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native void n_onDraw(Canvas canvas);

    static {
        Runtime.register("C1.Android.Chart.PieCanvas, C1.Android.Chart", PieCanvas.class, "n_onDraw:(Landroid/graphics/Canvas;)V:GetOnDraw_Landroid_graphics_Canvas_Handler\n");
    }

    public PieCanvas(Context context) {
        super(context);
        if (PieCanvas.class == PieCanvas.class) {
            TypeManager.Activate("C1.Android.Chart.PieCanvas, C1.Android.Chart", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public PieCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (PieCanvas.class == PieCanvas.class) {
            TypeManager.Activate("C1.Android.Chart.PieCanvas, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public PieCanvas(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (PieCanvas.class == PieCanvas.class) {
            TypeManager.Activate("C1.Android.Chart.PieCanvas, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public PieCanvas(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        if (PieCanvas.class == PieCanvas.class) {
            TypeManager.Activate("C1.Android.Chart.PieCanvas, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i), Integer.valueOf(i2)});
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        n_onDraw(canvas);
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

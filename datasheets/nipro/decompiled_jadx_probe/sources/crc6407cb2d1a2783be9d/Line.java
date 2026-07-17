package crc6407cb2d1a2783be9d;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class Line extends View implements IGCUserPeer {
    public static final String __md_methods = "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onDraw:(Landroid/graphics/Canvas;)V:GetOnDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native void n_onDraw(Canvas canvas);

    private native void n_onMeasure(int i, int i2);

    static {
        Runtime.register("C1.Android.Chart.Interaction.Line, C1.Android.Chart", Line.class, "n_onMeasure:(II)V:GetOnMeasure_IIHandler\nn_onDraw:(Landroid/graphics/Canvas;)V:GetOnDraw_Landroid_graphics_Canvas_Handler\n");
    }

    public Line(Context context) {
        super(context);
        if (Line.class == Line.class) {
            TypeManager.Activate("C1.Android.Chart.Interaction.Line, C1.Android.Chart", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public Line(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (Line.class == Line.class) {
            TypeManager.Activate("C1.Android.Chart.Interaction.Line, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public Line(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (Line.class == Line.class) {
            TypeManager.Activate("C1.Android.Chart.Interaction.Line, C1.Android.Chart", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        n_onMeasure(i, i2);
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

package crc645e9136c190843a3c;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class GridSelectionAdornerView extends View implements IGCUserPeer {
    public static final String __md_methods = "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native void n_draw(Canvas canvas);

    static {
        Runtime.register("C1.Android.Grid.GridSelectionAdornerView, C1.Android.Grid", GridSelectionAdornerView.class, "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n");
    }

    public GridSelectionAdornerView(Context context) {
        super(context);
        if (GridSelectionAdornerView.class == GridSelectionAdornerView.class) {
            TypeManager.Activate("C1.Android.Grid.GridSelectionAdornerView, C1.Android.Grid", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public GridSelectionAdornerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (GridSelectionAdornerView.class == GridSelectionAdornerView.class) {
            TypeManager.Activate("C1.Android.Grid.GridSelectionAdornerView, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public GridSelectionAdornerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (GridSelectionAdornerView.class == GridSelectionAdornerView.class) {
            TypeManager.Activate("C1.Android.Grid.GridSelectionAdornerView, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        n_draw(canvas);
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

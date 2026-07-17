package crc64e1465336b1298e5b;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import crc643f46942d9dd1fff9.BoxRenderer;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class RadiusBoxViewRenderer extends BoxRenderer implements IGCUserPeer {
    public static final String __md_methods = "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native void n_draw(Canvas canvas);

    static {
        Runtime.register("NHL.Droid.Renderer.RadiusBoxViewRenderer, NHL.Android", RadiusBoxViewRenderer.class, "n_draw:(Landroid/graphics/Canvas;)V:GetDraw_Landroid_graphics_Canvas_Handler\n");
    }

    public RadiusBoxViewRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (RadiusBoxViewRenderer.class == RadiusBoxViewRenderer.class) {
            TypeManager.Activate("NHL.Droid.Renderer.RadiusBoxViewRenderer, NHL.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public RadiusBoxViewRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (RadiusBoxViewRenderer.class == RadiusBoxViewRenderer.class) {
            TypeManager.Activate("NHL.Droid.Renderer.RadiusBoxViewRenderer, NHL.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public RadiusBoxViewRenderer(Context context) {
        super(context);
        if (RadiusBoxViewRenderer.class == RadiusBoxViewRenderer.class) {
            TypeManager.Activate("NHL.Droid.Renderer.RadiusBoxViewRenderer, NHL.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        n_draw(canvas);
    }

    @Override // crc643f46942d9dd1fff9.BoxRenderer, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc643f46942d9dd1fff9.BoxRenderer, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

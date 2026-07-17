package crc6480997b3ef81bf9b2;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class ZxingOverlayView extends View implements IGCUserPeer {
    public static final String __md_methods = "n_onDraw:(Landroid/graphics/Canvas;)V:GetOnDraw_Landroid_graphics_Canvas_Handler\n";
    private ArrayList refList;

    private native void n_onDraw(Canvas canvas);

    static {
        Runtime.register("ZXing.Mobile.ZxingOverlayView, ZXingNetMobile", ZxingOverlayView.class, "n_onDraw:(Landroid/graphics/Canvas;)V:GetOnDraw_Landroid_graphics_Canvas_Handler\n");
    }

    public ZxingOverlayView(Context context) {
        super(context);
        if (ZxingOverlayView.class == ZxingOverlayView.class) {
            TypeManager.Activate("ZXing.Mobile.ZxingOverlayView, ZXingNetMobile", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public ZxingOverlayView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (ZxingOverlayView.class == ZxingOverlayView.class) {
            TypeManager.Activate("ZXing.Mobile.ZxingOverlayView, ZXingNetMobile", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ZxingOverlayView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (ZxingOverlayView.class == ZxingOverlayView.class) {
            TypeManager.Activate("ZXing.Mobile.ZxingOverlayView, ZXingNetMobile", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
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

package crc6427ea3917517e908b;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import crc643f46942d9dd1fff9.ViewRenderer_2;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class ZXingScannerViewRenderer extends ViewRenderer_2 implements IGCUserPeer {
    public static final String __md_methods = "n_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\n";
    private ArrayList refList;

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    static {
        Runtime.register("ZXing.Net.Mobile.Forms.Android.ZXingScannerViewRenderer, ZXing.Net.Mobile.Forms.Android", ZXingScannerViewRenderer.class, "n_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\n");
    }

    public ZXingScannerViewRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (ZXingScannerViewRenderer.class == ZXingScannerViewRenderer.class) {
            TypeManager.Activate("ZXing.Net.Mobile.Forms.Android.ZXingScannerViewRenderer, ZXing.Net.Mobile.Forms.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public ZXingScannerViewRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (ZXingScannerViewRenderer.class == ZXingScannerViewRenderer.class) {
            TypeManager.Activate("ZXing.Net.Mobile.Forms.Android.ZXingScannerViewRenderer, ZXing.Net.Mobile.Forms.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ZXingScannerViewRenderer(Context context) {
        super(context);
        if (ZXingScannerViewRenderer.class == ZXingScannerViewRenderer.class) {
            TypeManager.Activate("ZXing.Net.Mobile.Forms.Android.ZXingScannerViewRenderer, ZXing.Net.Mobile.Forms.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc643f46942d9dd1fff9.VisualElementRenderer_1, com.xamarin.forms.platform.android.FormsViewGroup, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
    }

    @Override // crc643f46942d9dd1fff9.ViewRenderer_2, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc643f46942d9dd1fff9.ViewRenderer_2, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

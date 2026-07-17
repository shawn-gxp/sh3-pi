package crc64ee486da937c010f4;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class ImageRenderer extends ImageView implements IGCUserPeer {
    public static final String __md_methods = "n_invalidate:()V:GetInvalidateHandler\nn_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\n";
    private ArrayList refList;

    private native void n_invalidate();

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    static {
        Runtime.register("Xamarin.Forms.Platform.Android.FastRenderers.ImageRenderer, Xamarin.Forms.Platform.Android", ImageRenderer.class, __md_methods);
    }

    public ImageRenderer(Context context) {
        super(context);
        if (ImageRenderer.class == ImageRenderer.class) {
            TypeManager.Activate("Xamarin.Forms.Platform.Android.FastRenderers.ImageRenderer, Xamarin.Forms.Platform.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public ImageRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (ImageRenderer.class == ImageRenderer.class) {
            TypeManager.Activate("Xamarin.Forms.Platform.Android.FastRenderers.ImageRenderer, Xamarin.Forms.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public ImageRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (ImageRenderer.class == ImageRenderer.class) {
            TypeManager.Activate("Xamarin.Forms.Platform.Android.FastRenderers.ImageRenderer, Xamarin.Forms.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // android.view.View
    public void invalidate() {
        n_invalidate();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
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

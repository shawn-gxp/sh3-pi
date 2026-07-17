package crc64e1465336b1298e5b;

import android.content.Context;
import android.util.AttributeSet;
import crc643f46942d9dd1fff9.WebViewRenderer;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class NoCachedWebViewRenderer extends WebViewRenderer implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("NHL.Droid.Renderer.NoCachedWebViewRenderer, NHL.Android", NoCachedWebViewRenderer.class, "");
    }

    public NoCachedWebViewRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (NoCachedWebViewRenderer.class == NoCachedWebViewRenderer.class) {
            TypeManager.Activate("NHL.Droid.Renderer.NoCachedWebViewRenderer, NHL.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public NoCachedWebViewRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (NoCachedWebViewRenderer.class == NoCachedWebViewRenderer.class) {
            TypeManager.Activate("NHL.Droid.Renderer.NoCachedWebViewRenderer, NHL.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public NoCachedWebViewRenderer(Context context) {
        super(context);
        if (NoCachedWebViewRenderer.class == NoCachedWebViewRenderer.class) {
            TypeManager.Activate("NHL.Droid.Renderer.NoCachedWebViewRenderer, NHL.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc643f46942d9dd1fff9.WebViewRenderer, crc643f46942d9dd1fff9.ViewRenderer_2, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc643f46942d9dd1fff9.WebViewRenderer, crc643f46942d9dd1fff9.ViewRenderer_2, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

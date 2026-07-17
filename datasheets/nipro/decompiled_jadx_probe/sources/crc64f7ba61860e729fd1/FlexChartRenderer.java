package crc64f7ba61860e729fd1;

import android.content.Context;
import android.util.AttributeSet;
import crc6498c6b4d776f4c9b6.C1ViewRenderer_2;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class FlexChartRenderer extends C1ViewRenderer_2 implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("C1.Xamarin.Forms.Chart.Platform.Android.FlexChartRenderer, C1.Xamarin.Forms.Chart.Platform.Android", FlexChartRenderer.class, "");
    }

    public FlexChartRenderer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (FlexChartRenderer.class == FlexChartRenderer.class) {
            TypeManager.Activate("C1.Xamarin.Forms.Chart.Platform.Android.FlexChartRenderer, C1.Xamarin.Forms.Chart.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    public FlexChartRenderer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (FlexChartRenderer.class == FlexChartRenderer.class) {
            TypeManager.Activate("C1.Xamarin.Forms.Chart.Platform.Android.FlexChartRenderer, C1.Xamarin.Forms.Chart.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public FlexChartRenderer(Context context) {
        super(context);
        if (FlexChartRenderer.class == FlexChartRenderer.class) {
            TypeManager.Activate("C1.Xamarin.Forms.Chart.Platform.Android.FlexChartRenderer, C1.Xamarin.Forms.Chart.Platform.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    @Override // crc6498c6b4d776f4c9b6.C1ViewRenderer_2, crc643f46942d9dd1fff9.ViewRenderer_2, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc6498c6b4d776f4c9b6.C1ViewRenderer_2, crc643f46942d9dd1fff9.ViewRenderer_2, crc643f46942d9dd1fff9.VisualElementRenderer_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

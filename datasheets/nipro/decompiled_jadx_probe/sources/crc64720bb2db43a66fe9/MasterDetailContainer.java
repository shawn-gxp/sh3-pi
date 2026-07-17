package crc64720bb2db43a66fe9;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class MasterDetailContainer extends crc643f46942d9dd1fff9.MasterDetailContainer implements IGCUserPeer {
    public static final String __md_methods = "n_onLayout:(ZIIII)V:GetOnLayout_ZIIIIHandler\nn_onAttachedToWindow:()V:GetOnAttachedToWindowHandler\n";
    private ArrayList refList;

    private native void n_onAttachedToWindow();

    private native void n_onLayout(boolean z, int i, int i2, int i3, int i4);

    static {
        Runtime.register("Xamarin.Forms.Platform.Android.AppCompat.MasterDetailContainer, Xamarin.Forms.Platform.Android", MasterDetailContainer.class, __md_methods);
    }

    public MasterDetailContainer(Context context) {
        super(context);
        if (MasterDetailContainer.class == MasterDetailContainer.class) {
            TypeManager.Activate("Xamarin.Forms.Platform.Android.AppCompat.MasterDetailContainer, Xamarin.Forms.Platform.Android", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MasterDetailContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (MasterDetailContainer.class == MasterDetailContainer.class) {
            TypeManager.Activate("Xamarin.Forms.Platform.Android.AppCompat.MasterDetailContainer, Xamarin.Forms.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MasterDetailContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (MasterDetailContainer.class == MasterDetailContainer.class) {
            TypeManager.Activate("Xamarin.Forms.Platform.Android.AppCompat.MasterDetailContainer, Xamarin.Forms.Platform.Android", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // crc643f46942d9dd1fff9.MasterDetailContainer, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        n_onLayout(z, i, i2, i3, i4);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        n_onAttachedToWindow();
    }

    @Override // crc643f46942d9dd1fff9.MasterDetailContainer, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc643f46942d9dd1fff9.MasterDetailContainer, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

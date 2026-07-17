package crc64f204578e57a29fff;

import android.animation.TimeInterpolator;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class C1Easing_EasingFunc implements IGCUserPeer, TimeInterpolator {
    public static final String __md_methods = "n_getInterpolation:(F)F:GetGetInterpolation_FHandler:Android.Animation.ITimeInterpolatorInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native float n_getInterpolation(float f);

    static {
        Runtime.register("C1.Android.Core.C1Easing+EasingFunc, C1.Android.Core", C1Easing_EasingFunc.class, __md_methods);
    }

    public C1Easing_EasingFunc() {
        if (C1Easing_EasingFunc.class == C1Easing_EasingFunc.class) {
            TypeManager.Activate("C1.Android.Core.C1Easing+EasingFunc, C1.Android.Core", "", this, new Object[0]);
        }
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float f) {
        return n_getInterpolation(f);
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

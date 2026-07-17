package crc64f204578e57a29fff;

import android.view.Choreographer;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class FrameCallback implements IGCUserPeer, Choreographer.FrameCallback {
    public static final String __md_methods = "n_doFrame:(J)V:GetDoFrame_JHandler:Android.Views.Choreographer/IFrameCallbackInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_doFrame(long j);

    static {
        Runtime.register("C1.Android.Core.FrameCallback, C1.Android.Core", FrameCallback.class, __md_methods);
    }

    public FrameCallback() {
        if (FrameCallback.class == FrameCallback.class) {
            TypeManager.Activate("C1.Android.Core.FrameCallback, C1.Android.Core", "", this, new Object[0]);
        }
    }

    @Override // android.view.Choreographer.FrameCallback
    public void doFrame(long j) {
        n_doFrame(j);
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

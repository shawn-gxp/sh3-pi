package crc64357c0806a054c747;

import crc648c15711fce523d6b.CaliburnApplication;
import java.util.ArrayList;
import mono.MonoPackageManager;
import mono.android.IGCUserPeer;

/* loaded from: classes.dex */
public class Application extends CaliburnApplication implements IGCUserPeer {
    public static final String __md_methods = "n_onCreate:()V:GetOnCreateHandler\n";
    private ArrayList refList;

    private native void n_onCreate();

    public Application() {
        MonoPackageManager.setContext(this);
    }

    @Override // android.app.Application
    public void onCreate() {
        n_onCreate();
    }

    @Override // crc648c15711fce523d6b.CaliburnApplication, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc648c15711fce523d6b.CaliburnApplication, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

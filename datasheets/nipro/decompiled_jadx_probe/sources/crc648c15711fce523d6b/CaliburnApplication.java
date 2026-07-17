package crc648c15711fce523d6b;

import androidx.multidex.MultiDexApplication;
import java.util.ArrayList;
import mono.MonoPackageManager;
import mono.android.IGCUserPeer;

/* loaded from: classes.dex */
public class CaliburnApplication extends MultiDexApplication implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    public CaliburnApplication() {
        MonoPackageManager.setContext(this);
    }

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

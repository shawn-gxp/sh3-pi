package crc64396a3fe5f8138e3f;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class KeepAliveService extends Service implements IGCUserPeer {
    public static final String __md_methods = "n_onBind:(Landroid/content/Intent;)Landroid/os/IBinder;:GetOnBind_Landroid_content_Intent_Handler\n";
    private ArrayList refList;

    private native IBinder n_onBind(Intent intent);

    static {
        Runtime.register("AndroidX.Browser.CustomTabs.KeepAliveService, Xamarin.AndroidX.Browser", KeepAliveService.class, __md_methods);
    }

    public KeepAliveService() {
        if (KeepAliveService.class == KeepAliveService.class) {
            TypeManager.Activate("AndroidX.Browser.CustomTabs.KeepAliveService, Xamarin.AndroidX.Browser", "", this, new Object[0]);
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return n_onBind(intent);
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

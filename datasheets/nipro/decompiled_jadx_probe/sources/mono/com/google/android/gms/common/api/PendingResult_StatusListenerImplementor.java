package mono.com.google.android.gms.common.api;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class PendingResult_StatusListenerImplementor implements IGCUserPeer, PendingResult.StatusListener {
    public static final String __md_methods = "n_onComplete:(Lcom/google/android/gms/common/api/Status;)V:GetOnComplete_Lcom_google_android_gms_common_api_Status_Handler:Android.Gms.Common.Apis.PendingResult/IStatusListenerInvoker, Xamarin.GooglePlayServices.Base\n";
    private ArrayList refList;

    private native void n_onComplete(Status status);

    static {
        Runtime.register("Android.Gms.Common.Apis.PendingResult+IStatusListenerImplementor, Xamarin.GooglePlayServices.Base", PendingResult_StatusListenerImplementor.class, __md_methods);
    }

    public PendingResult_StatusListenerImplementor() {
        if (PendingResult_StatusListenerImplementor.class == PendingResult_StatusListenerImplementor.class) {
            TypeManager.Activate("Android.Gms.Common.Apis.PendingResult+IStatusListenerImplementor, Xamarin.GooglePlayServices.Base", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.gms.common.api.PendingResult.StatusListener
    public void onComplete(Status status) {
        n_onComplete(status);
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

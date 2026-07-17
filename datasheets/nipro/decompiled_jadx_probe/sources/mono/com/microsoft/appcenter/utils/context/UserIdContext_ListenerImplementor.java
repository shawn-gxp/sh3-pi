package mono.com.microsoft.appcenter.utils.context;

import com.microsoft.appcenter.utils.context.UserIdContext;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class UserIdContext_ListenerImplementor implements IGCUserPeer, UserIdContext.Listener {
    public static final String __md_methods = "n_onNewUserId:(Ljava/lang/String;)V:GetOnNewUserId_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Utils.Context.UserIdContext/IListenerInvoker, Microsoft.AppCenter.Android.Bindings\n";
    private ArrayList refList;

    private native void n_onNewUserId(String str);

    static {
        Runtime.register("Com.Microsoft.Appcenter.Utils.Context.UserIdContext+IListenerImplementor, Microsoft.AppCenter.Android.Bindings", UserIdContext_ListenerImplementor.class, __md_methods);
    }

    public UserIdContext_ListenerImplementor() {
        if (UserIdContext_ListenerImplementor.class == UserIdContext_ListenerImplementor.class) {
            TypeManager.Activate("Com.Microsoft.Appcenter.Utils.Context.UserIdContext+IListenerImplementor, Microsoft.AppCenter.Android.Bindings", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.appcenter.utils.context.UserIdContext.Listener
    public void onNewUserId(String str) {
        n_onNewUserId(str);
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

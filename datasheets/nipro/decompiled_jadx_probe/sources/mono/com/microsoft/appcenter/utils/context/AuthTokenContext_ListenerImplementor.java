package mono.com.microsoft.appcenter.utils.context;

import com.microsoft.appcenter.utils.context.AuthTokenContext;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AuthTokenContext_ListenerImplementor implements IGCUserPeer, AuthTokenContext.Listener {
    public static final String __md_methods = "n_onNewAuthToken:(Ljava/lang/String;)V:GetOnNewAuthToken_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Utils.Context.AuthTokenContext/IListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onNewUser:(Ljava/lang/String;)V:GetOnNewUser_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Utils.Context.AuthTokenContext/IListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onTokenRequiresRefresh:(Ljava/lang/String;)V:GetOnTokenRequiresRefresh_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Utils.Context.AuthTokenContext/IListenerInvoker, Microsoft.AppCenter.Android.Bindings\n";
    private ArrayList refList;

    private native void n_onNewAuthToken(String str);

    private native void n_onNewUser(String str);

    private native void n_onTokenRequiresRefresh(String str);

    static {
        Runtime.register("Com.Microsoft.Appcenter.Utils.Context.AuthTokenContext+IListenerImplementor, Microsoft.AppCenter.Android.Bindings", AuthTokenContext_ListenerImplementor.class, __md_methods);
    }

    public AuthTokenContext_ListenerImplementor() {
        if (AuthTokenContext_ListenerImplementor.class == AuthTokenContext_ListenerImplementor.class) {
            TypeManager.Activate("Com.Microsoft.Appcenter.Utils.Context.AuthTokenContext+IListenerImplementor, Microsoft.AppCenter.Android.Bindings", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.appcenter.utils.context.AuthTokenContext.Listener
    public void onNewAuthToken(String str) {
        n_onNewAuthToken(str);
    }

    @Override // com.microsoft.appcenter.utils.context.AuthTokenContext.Listener
    public void onNewUser(String str) {
        n_onNewUser(str);
    }

    @Override // com.microsoft.appcenter.utils.context.AuthTokenContext.Listener
    public void onTokenRequiresRefresh(String str) {
        n_onTokenRequiresRefresh(str);
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

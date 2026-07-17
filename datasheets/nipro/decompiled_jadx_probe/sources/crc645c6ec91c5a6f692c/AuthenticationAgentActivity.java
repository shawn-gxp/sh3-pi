package crc645c6ec91c5a6f692c;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AuthenticationAgentActivity extends Activity implements IGCUserPeer {
    public static final String __md_methods = "n_onCreate:(Landroid/os/Bundle;)V:GetOnCreate_Landroid_os_Bundle_Handler\nn_finish:()V:GetFinishHandler\n";
    private ArrayList refList;

    private native void n_finish();

    private native void n_onCreate(Bundle bundle);

    static {
        Runtime.register("Microsoft.IdentityModel.Clients.ActiveDirectory.AuthenticationAgentActivity, Microsoft.IdentityModel.Clients.ActiveDirectory", AuthenticationAgentActivity.class, __md_methods);
    }

    public AuthenticationAgentActivity() {
        if (AuthenticationAgentActivity.class == AuthenticationAgentActivity.class) {
            TypeManager.Activate("Microsoft.IdentityModel.Clients.ActiveDirectory.AuthenticationAgentActivity, Microsoft.IdentityModel.Clients.ActiveDirectory", "", this, new Object[0]);
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        n_onCreate(bundle);
    }

    @Override // android.app.Activity
    public void finish() {
        n_finish();
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

package crc645c6ec91c5a6f692c;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CallBackHandler implements IGCUserPeer, AccountManagerCallback {
    public static final String __md_methods = "n_run:(Landroid/accounts/AccountManagerFuture;)V:GetRun_Landroid_accounts_AccountManagerFuture_Handler:Android.Accounts.IAccountManagerCallbackInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_run(AccountManagerFuture accountManagerFuture);

    static {
        Runtime.register("Microsoft.IdentityModel.Clients.ActiveDirectory.CallBackHandler, Microsoft.IdentityModel.Clients.ActiveDirectory", CallBackHandler.class, __md_methods);
    }

    public CallBackHandler() {
        if (CallBackHandler.class == CallBackHandler.class) {
            TypeManager.Activate("Microsoft.IdentityModel.Clients.ActiveDirectory.CallBackHandler, Microsoft.IdentityModel.Clients.ActiveDirectory", "", this, new Object[0]);
        }
    }

    @Override // android.accounts.AccountManagerCallback
    public void run(AccountManagerFuture accountManagerFuture) {
        n_run(accountManagerFuture);
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

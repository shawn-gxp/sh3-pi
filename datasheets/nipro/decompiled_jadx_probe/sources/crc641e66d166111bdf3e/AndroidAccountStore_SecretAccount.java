package crc641e66d166111bdf3e;

import java.io.Serializable;
import java.security.Key;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AndroidAccountStore_SecretAccount implements IGCUserPeer, SecretKey, Key, Serializable {
    public static final String __md_methods = "n_getAlgorithm:()Ljava/lang/String;:GetGetAlgorithmHandler:Java.Security.IKeyInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getFormat:()Ljava/lang/String;:GetGetFormatHandler:Java.Security.IKeyInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_getEncoded:()[B:GetGetEncodedHandler:Java.Security.IKeyInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native String n_getAlgorithm();

    private native byte[] n_getEncoded();

    private native String n_getFormat();

    static {
        Runtime.register("Xamarin.Auth._MobileServices.AndroidAccountStore+SecretAccount, Microsoft.Azure.Mobile.Client", AndroidAccountStore_SecretAccount.class, __md_methods);
    }

    public AndroidAccountStore_SecretAccount() {
        if (AndroidAccountStore_SecretAccount.class == AndroidAccountStore_SecretAccount.class) {
            TypeManager.Activate("Xamarin.Auth._MobileServices.AndroidAccountStore+SecretAccount, Microsoft.Azure.Mobile.Client", "", this, new Object[0]);
        }
    }

    @Override // java.security.Key
    public String getAlgorithm() {
        return n_getAlgorithm();
    }

    @Override // java.security.Key
    public String getFormat() {
        return n_getFormat();
    }

    @Override // java.security.Key
    public byte[] getEncoded() {
        return n_getEncoded();
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

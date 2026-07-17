package crc641e66d166111bdf3e;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class FormAuthenticatorActivity extends Activity implements IGCUserPeer {
    public static final String __md_methods = "n_onCreate:(Landroid/os/Bundle;)V:GetOnCreate_Landroid_os_Bundle_Handler\nn_onResume:()V:GetOnResumeHandler\nn_onBackPressed:()V:GetOnBackPressedHandler\nn_onRetainNonConfigurationInstance:()Ljava/lang/Object;:GetOnRetainNonConfigurationInstanceHandler\nn_onSaveInstanceState:(Landroid/os/Bundle;)V:GetOnSaveInstanceState_Landroid_os_Bundle_Handler\n";
    private ArrayList refList;

    private native void n_onBackPressed();

    private native void n_onCreate(Bundle bundle);

    private native void n_onResume();

    private native Object n_onRetainNonConfigurationInstance();

    private native void n_onSaveInstanceState(Bundle bundle);

    static {
        Runtime.register("Xamarin.Auth._MobileServices.FormAuthenticatorActivity, Microsoft.Azure.Mobile.Client", FormAuthenticatorActivity.class, "n_onCreate:(Landroid/os/Bundle;)V:GetOnCreate_Landroid_os_Bundle_Handler\nn_onResume:()V:GetOnResumeHandler\nn_onBackPressed:()V:GetOnBackPressedHandler\nn_onRetainNonConfigurationInstance:()Ljava/lang/Object;:GetOnRetainNonConfigurationInstanceHandler\nn_onSaveInstanceState:(Landroid/os/Bundle;)V:GetOnSaveInstanceState_Landroid_os_Bundle_Handler\n");
    }

    public FormAuthenticatorActivity() {
        if (FormAuthenticatorActivity.class == FormAuthenticatorActivity.class) {
            TypeManager.Activate("Xamarin.Auth._MobileServices.FormAuthenticatorActivity, Microsoft.Azure.Mobile.Client", "", this, new Object[0]);
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        n_onCreate(bundle);
    }

    @Override // android.app.Activity
    public void onResume() {
        n_onResume();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        n_onBackPressed();
    }

    @Override // android.app.Activity
    public Object onRetainNonConfigurationInstance() {
        return n_onRetainNonConfigurationInstance();
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        n_onSaveInstanceState(bundle);
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

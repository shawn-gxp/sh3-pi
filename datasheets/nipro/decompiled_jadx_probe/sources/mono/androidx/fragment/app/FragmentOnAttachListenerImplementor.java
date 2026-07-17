package mono.androidx.fragment.app;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class FragmentOnAttachListenerImplementor implements IGCUserPeer, FragmentOnAttachListener {
    public static final String __md_methods = "n_onAttachFragment:(Landroidx/fragment/app/FragmentManager;Landroidx/fragment/app/Fragment;)V:GetOnAttachFragment_Landroidx_fragment_app_FragmentManager_Landroidx_fragment_app_Fragment_Handler:AndroidX.Fragment.App.IFragmentOnAttachListenerInvoker, Xamarin.AndroidX.Fragment\n";
    private ArrayList refList;

    private native void n_onAttachFragment(FragmentManager fragmentManager, Fragment fragment);

    static {
        Runtime.register("AndroidX.Fragment.App.IFragmentOnAttachListenerImplementor, Xamarin.AndroidX.Fragment", FragmentOnAttachListenerImplementor.class, __md_methods);
    }

    public FragmentOnAttachListenerImplementor() {
        if (FragmentOnAttachListenerImplementor.class == FragmentOnAttachListenerImplementor.class) {
            TypeManager.Activate("AndroidX.Fragment.App.IFragmentOnAttachListenerImplementor, Xamarin.AndroidX.Fragment", "", this, new Object[0]);
        }
    }

    @Override // androidx.fragment.app.FragmentOnAttachListener
    public void onAttachFragment(FragmentManager fragmentManager, Fragment fragment) {
        n_onAttachFragment(fragmentManager, fragment);
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

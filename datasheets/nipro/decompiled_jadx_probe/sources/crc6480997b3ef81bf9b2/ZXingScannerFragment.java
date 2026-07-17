package crc6480997b3ef81bf9b2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class ZXingScannerFragment extends Fragment implements IGCUserPeer {
    public static final String __md_methods = "n_onCreateView:(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;:GetOnCreateView_Landroid_view_LayoutInflater_Landroid_view_ViewGroup_Landroid_os_Bundle_Handler\nn_onStart:()V:GetOnStartHandler\nn_onStop:()V:GetOnStopHandler\n";
    private ArrayList refList;

    private native View n_onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    private native void n_onStart();

    private native void n_onStop();

    static {
        Runtime.register("ZXing.Mobile.ZXingScannerFragment, ZXingNetMobile", ZXingScannerFragment.class, __md_methods);
    }

    public ZXingScannerFragment() {
        if (ZXingScannerFragment.class == ZXingScannerFragment.class) {
            TypeManager.Activate("ZXing.Mobile.ZXingScannerFragment, ZXingNetMobile", "", this, new Object[0]);
        }
    }

    public ZXingScannerFragment(int i) {
        super(i);
        if (ZXingScannerFragment.class == ZXingScannerFragment.class) {
            TypeManager.Activate("ZXing.Mobile.ZXingScannerFragment, ZXingNetMobile", "System.Int32, mscorlib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return n_onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public void onStart() {
        n_onStart();
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        n_onStop();
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

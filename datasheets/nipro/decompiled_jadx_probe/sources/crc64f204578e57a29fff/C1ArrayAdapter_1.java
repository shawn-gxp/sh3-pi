package crc64f204578e57a29fff;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class C1ArrayAdapter_1 extends ArrayAdapter implements IGCUserPeer {
    public static final String __md_methods = "n_getDropDownView:(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;:GetGetDropDownView_ILandroid_view_View_Landroid_view_ViewGroup_Handler\nn_getView:(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;:GetGetView_ILandroid_view_View_Landroid_view_ViewGroup_Handler\n";
    private ArrayList refList;

    private native View n_getDropDownView(int i, View view, ViewGroup viewGroup);

    private native View n_getView(int i, View view, ViewGroup viewGroup);

    static {
        Runtime.register("C1.Android.Core.C1ArrayAdapter`1, C1.Android.Core", C1ArrayAdapter_1.class, __md_methods);
    }

    public C1ArrayAdapter_1(Context context, int i, List list) {
        super(context, i, list);
        if (C1ArrayAdapter_1.class == C1ArrayAdapter_1.class) {
            TypeManager.Activate("C1.Android.Core.C1ArrayAdapter`1, C1.Android.Core", "Android.Content.Context, Mono.Android:System.Int32, mscorlib:System.Collections.Generic.IList`1<T>, mscorlib", this, new Object[]{context, Integer.valueOf(i), list});
        }
    }

    @Override // android.widget.ArrayAdapter, android.widget.BaseAdapter, android.widget.SpinnerAdapter
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        return n_getDropDownView(i, view, viewGroup);
    }

    @Override // android.widget.ArrayAdapter, android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        return n_getView(i, view, viewGroup);
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

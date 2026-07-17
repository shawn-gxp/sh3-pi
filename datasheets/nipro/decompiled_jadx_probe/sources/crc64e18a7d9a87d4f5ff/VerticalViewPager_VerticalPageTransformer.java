package crc64e18a7d9a87d4f5ff;

import android.view.View;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class VerticalViewPager_VerticalPageTransformer implements IGCUserPeer, ViewPager.PageTransformer {
    public static final String __md_methods = "n_transformPage:(Landroid/view/View;F)V:GetTransformPage_Landroid_view_View_FHandler:AndroidX.ViewPager.Widget.ViewPager/IPageTransformerInvoker, Xamarin.AndroidX.ViewPager\n";
    private ArrayList refList;

    private native void n_transformPage(View view, float f);

    static {
        Runtime.register("Com.Android.DeskClock.VerticalViewPager+VerticalPageTransformer, Com.Android.DeskClock", VerticalViewPager_VerticalPageTransformer.class, __md_methods);
    }

    public VerticalViewPager_VerticalPageTransformer() {
        if (VerticalViewPager_VerticalPageTransformer.class == VerticalViewPager_VerticalPageTransformer.class) {
            TypeManager.Activate("Com.Android.DeskClock.VerticalViewPager+VerticalPageTransformer, Com.Android.DeskClock", "", this, new Object[0]);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager.PageTransformer
    public void transformPage(View view, float f) {
        n_transformPage(view, f);
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

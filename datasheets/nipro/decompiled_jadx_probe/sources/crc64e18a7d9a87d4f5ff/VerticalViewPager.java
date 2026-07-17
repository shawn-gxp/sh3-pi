package crc64e18a7d9a87d4f5ff;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class VerticalViewPager extends ViewPager implements IGCUserPeer {
    public static final String __md_methods = "n_canScrollHorizontally:(I)Z:GetCanScrollHorizontally_IHandler\nn_canScrollVertically:(I)Z:GetCanScrollVertically_IHandler\nn_onInterceptTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnInterceptTouchEvent_Landroid_view_MotionEvent_Handler\nn_onTouchEvent:(Landroid/view/MotionEvent;)Z:GetOnTouchEvent_Landroid_view_MotionEvent_Handler\n";
    private ArrayList refList;

    private native boolean n_canScrollHorizontally(int i);

    private native boolean n_canScrollVertically(int i);

    private native boolean n_onInterceptTouchEvent(MotionEvent motionEvent);

    private native boolean n_onTouchEvent(MotionEvent motionEvent);

    static {
        Runtime.register("Com.Android.DeskClock.VerticalViewPager, Com.Android.DeskClock", VerticalViewPager.class, __md_methods);
    }

    public VerticalViewPager(Context context) {
        super(context);
        if (VerticalViewPager.class == VerticalViewPager.class) {
            TypeManager.Activate("Com.Android.DeskClock.VerticalViewPager, Com.Android.DeskClock", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public VerticalViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (VerticalViewPager.class == VerticalViewPager.class) {
            TypeManager.Activate("Com.Android.DeskClock.VerticalViewPager, Com.Android.DeskClock", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public boolean canScrollHorizontally(int i) {
        return n_canScrollHorizontally(i);
    }

    @Override // android.view.View
    public boolean canScrollVertically(int i) {
        return n_canScrollVertically(i);
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return n_onInterceptTouchEvent(motionEvent);
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return n_onTouchEvent(motionEvent);
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

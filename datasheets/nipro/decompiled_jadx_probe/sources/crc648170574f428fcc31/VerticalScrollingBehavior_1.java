package crc648170574f428fcc31;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public abstract class VerticalScrollingBehavior_1 extends CoordinatorLayout.Behavior implements IGCUserPeer {
    public static final String __md_methods = "n_onStartNestedScroll:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;Landroid/view/View;I)Z:GetOnStartNestedScroll_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_Landroid_view_View_IHandler\nn_onNestedScrollAccepted:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;Landroid/view/View;I)V:GetOnNestedScrollAccepted_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_Landroid_view_View_IHandler\nn_onStopNestedScroll:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;)V:GetOnStopNestedScroll_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_Handler\nn_onNestedScroll:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;IIII)V:GetOnNestedScroll_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_IIIIHandler\nn_onNestedPreScroll:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;II[I)V:GetOnNestedPreScroll_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_IIarrayIHandler\nn_onNestedFling:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;FFZ)Z:GetOnNestedFling_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_FFZHandler\nn_onNestedPreFling:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;FF)Z:GetOnNestedPreFling_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_FFHandler\nn_onApplyWindowInsets:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroidx/core/view/WindowInsetsCompat;)Landroidx/core/view/WindowInsetsCompat;:GetOnApplyWindowInsets_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroidx_core_view_WindowInsetsCompat_Handler\nn_onSaveInstanceState:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;)Landroid/os/Parcelable;:GetOnSaveInstanceState_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Handler\n";
    private ArrayList refList;

    private native WindowInsetsCompat n_onApplyWindowInsets(CoordinatorLayout coordinatorLayout, View view, WindowInsetsCompat windowInsetsCompat);

    private native boolean n_onNestedFling(CoordinatorLayout coordinatorLayout, View view, View view2, float f, float f2, boolean z);

    private native boolean n_onNestedPreFling(CoordinatorLayout coordinatorLayout, View view, View view2, float f, float f2);

    private native void n_onNestedPreScroll(CoordinatorLayout coordinatorLayout, View view, View view2, int i, int i2, int[] iArr);

    private native void n_onNestedScroll(CoordinatorLayout coordinatorLayout, View view, View view2, int i, int i2, int i3, int i4);

    private native void n_onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, View view, View view2, View view3, int i);

    private native Parcelable n_onSaveInstanceState(CoordinatorLayout coordinatorLayout, View view);

    private native boolean n_onStartNestedScroll(CoordinatorLayout coordinatorLayout, View view, View view2, View view3, int i);

    private native void n_onStopNestedScroll(CoordinatorLayout coordinatorLayout, View view, View view2);

    static {
        Runtime.register("BottomNavigationBar.Scrollswetness.VerticalScrollingBehavior`1, BottomNavigationBar", VerticalScrollingBehavior_1.class, __md_methods);
    }

    public VerticalScrollingBehavior_1() {
        if (getClass() == VerticalScrollingBehavior_1.class) {
            TypeManager.Activate("BottomNavigationBar.Scrollswetness.VerticalScrollingBehavior`1, BottomNavigationBar", "", this, new Object[0]);
        }
    }

    public VerticalScrollingBehavior_1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == VerticalScrollingBehavior_1.class) {
            TypeManager.Activate("BottomNavigationBar.Scrollswetness.VerticalScrollingBehavior`1, BottomNavigationBar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View view, View view2, View view3, int i) {
        return n_onStartNestedScroll(coordinatorLayout, view, view2, view3, i);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, View view, View view2, View view3, int i) {
        n_onNestedScrollAccepted(coordinatorLayout, view, view2, view3, i);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View view, View view2) {
        n_onStopNestedScroll(coordinatorLayout, view, view2);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View view, View view2, int i, int i2, int i3, int i4) {
        n_onNestedScroll(coordinatorLayout, view, view2, i, i2, i3, i4);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View view, View view2, int i, int i2, int[] iArr) {
        n_onNestedPreScroll(coordinatorLayout, view, view2, i, i2, iArr);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, View view, View view2, float f, float f2, boolean z) {
        return n_onNestedFling(coordinatorLayout, view, view2, f, f2, z);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View view, View view2, float f, float f2) {
        return n_onNestedPreFling(coordinatorLayout, view, view2, f, f2);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, View view, WindowInsetsCompat windowInsetsCompat) {
        return n_onApplyWindowInsets(coordinatorLayout, view, windowInsetsCompat);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public Parcelable onSaveInstanceState(CoordinatorLayout coordinatorLayout, View view) {
        return n_onSaveInstanceState(coordinatorLayout, view);
    }

    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

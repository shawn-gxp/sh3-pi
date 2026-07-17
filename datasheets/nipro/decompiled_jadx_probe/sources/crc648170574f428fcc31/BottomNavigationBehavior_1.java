package crc648170574f428fcc31;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class BottomNavigationBehavior_1 extends VerticalScrollingBehavior_1 implements IGCUserPeer {
    public static final String __md_methods = "n_layoutDependsOn:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;)Z:GetLayoutDependsOn_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_Handler\nn_onDependentViewRemoved:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;)V:GetOnDependentViewRemoved_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_Handler\nn_onDependentViewChanged:(Landroidx/coordinatorlayout/widget/CoordinatorLayout;Landroid/view/View;Landroid/view/View;)Z:GetOnDependentViewChanged_Landroidx_coordinatorlayout_widget_CoordinatorLayout_Landroid_view_View_Landroid_view_View_Handler\n";
    private ArrayList refList;

    private native boolean n_layoutDependsOn(CoordinatorLayout coordinatorLayout, View view, View view2);

    private native boolean n_onDependentViewChanged(CoordinatorLayout coordinatorLayout, View view, View view2);

    private native void n_onDependentViewRemoved(CoordinatorLayout coordinatorLayout, View view, View view2);

    static {
        Runtime.register("BottomNavigationBar.Scrollswetness.BottomNavigationBehavior`1, BottomNavigationBar", BottomNavigationBehavior_1.class, __md_methods);
    }

    public BottomNavigationBehavior_1() {
        if (BottomNavigationBehavior_1.class == BottomNavigationBehavior_1.class) {
            TypeManager.Activate("BottomNavigationBar.Scrollswetness.BottomNavigationBehavior`1, BottomNavigationBar", "", this, new Object[0]);
        }
    }

    public BottomNavigationBehavior_1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (BottomNavigationBehavior_1.class == BottomNavigationBehavior_1.class) {
            TypeManager.Activate("BottomNavigationBar.Scrollswetness.BottomNavigationBehavior`1, BottomNavigationBar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public boolean layoutDependsOn(CoordinatorLayout coordinatorLayout, View view, View view2) {
        return n_layoutDependsOn(coordinatorLayout, view, view2);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public void onDependentViewRemoved(CoordinatorLayout coordinatorLayout, View view, View view2) {
        n_onDependentViewRemoved(coordinatorLayout, view, view2);
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior
    public boolean onDependentViewChanged(CoordinatorLayout coordinatorLayout, View view, View view2) {
        return n_onDependentViewChanged(coordinatorLayout, view, view2);
    }

    @Override // crc648170574f428fcc31.VerticalScrollingBehavior_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc648170574f428fcc31.VerticalScrollingBehavior_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}

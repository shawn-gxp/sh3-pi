package crc64c24f44e46c13f9f8;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CustomAnimatorListenerAdapter extends AnimatorListenerAdapter implements IGCUserPeer {
    public static final String __md_methods = "n_onAnimationEnd:(Landroid/animation/Animator;)V:GetOnAnimationEnd_Landroid_animation_Animator_Handler\nn_onAnimationCancel:(Landroid/animation/Animator;)V:GetOnAnimationCancel_Landroid_animation_Animator_Handler\n";
    private ArrayList refList;

    private native void n_onAnimationCancel(Animator animator);

    private native void n_onAnimationEnd(Animator animator);

    static {
        Runtime.register("BottomNavigationBar.Adapters.CustomAnimatorListenerAdapter, BottomNavigationBar", CustomAnimatorListenerAdapter.class, __md_methods);
    }

    public CustomAnimatorListenerAdapter() {
        if (CustomAnimatorListenerAdapter.class == CustomAnimatorListenerAdapter.class) {
            TypeManager.Activate("BottomNavigationBar.Adapters.CustomAnimatorListenerAdapter, BottomNavigationBar", "", this, new Object[0]);
        }
    }

    public CustomAnimatorListenerAdapter(View view, int i, View view2) {
        if (CustomAnimatorListenerAdapter.class == CustomAnimatorListenerAdapter.class) {
            TypeManager.Activate("BottomNavigationBar.Adapters.CustomAnimatorListenerAdapter, BottomNavigationBar", "Android.Views.View, Mono.Android:System.Int32, mscorlib:Android.Views.View, Mono.Android", this, new Object[]{view, Integer.valueOf(i), view2});
        }
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animator) {
        n_onAnimationEnd(animator);
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationCancel(Animator animator) {
        n_onAnimationCancel(animator);
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

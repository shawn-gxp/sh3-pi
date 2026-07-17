package crc64f8f085afb9a48114;

import android.animation.ValueAnimator;
import android.view.View;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class ResizePaddingTopAnimatorUpdateListener implements IGCUserPeer, ValueAnimator.AnimatorUpdateListener {
    public static final String __md_methods = "n_onAnimationUpdate:(Landroid/animation/ValueAnimator;)V:GetOnAnimationUpdate_Landroid_animation_ValueAnimator_Handler:Android.Animation.ValueAnimator/IAnimatorUpdateListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onAnimationUpdate(ValueAnimator valueAnimator);

    static {
        Runtime.register("BottomNavigationBar.Listeners.ResizePaddingTopAnimatorUpdateListener, BottomNavigationBar", ResizePaddingTopAnimatorUpdateListener.class, "n_onAnimationUpdate:(Landroid/animation/ValueAnimator;)V:GetOnAnimationUpdate_Landroid_animation_ValueAnimator_Handler:Android.Animation.ValueAnimator/IAnimatorUpdateListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public ResizePaddingTopAnimatorUpdateListener() {
        if (ResizePaddingTopAnimatorUpdateListener.class == ResizePaddingTopAnimatorUpdateListener.class) {
            TypeManager.Activate("BottomNavigationBar.Listeners.ResizePaddingTopAnimatorUpdateListener, BottomNavigationBar", "", this, new Object[0]);
        }
    }

    public ResizePaddingTopAnimatorUpdateListener(View view) {
        if (ResizePaddingTopAnimatorUpdateListener.class == ResizePaddingTopAnimatorUpdateListener.class) {
            TypeManager.Activate("BottomNavigationBar.Listeners.ResizePaddingTopAnimatorUpdateListener, BottomNavigationBar", "Android.Views.View, Mono.Android", this, new Object[]{view});
        }
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        n_onAnimationUpdate(valueAnimator);
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

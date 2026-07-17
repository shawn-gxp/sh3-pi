package crc64f4908b7feabfb550;

import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AddTextPlatformEffect_FastRendererOnLayoutChangeListener implements IGCUserPeer, View.OnLayoutChangeListener {
    public static final String __md_methods = "n_onLayoutChange:(Landroid/view/View;IIIIIIII)V:GetOnLayoutChange_Landroid_view_View_IIIIIIIIHandler:Android.Views.View/IOnLayoutChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    static {
        Runtime.register("AiForms.Effects.Droid.AddTextPlatformEffect+FastRendererOnLayoutChangeListener, AiForms.Effects.Droid", AddTextPlatformEffect_FastRendererOnLayoutChangeListener.class, "n_onLayoutChange:(Landroid/view/View;IIIIIIII)V:GetOnLayoutChange_Landroid_view_View_IIIIIIIIHandler:Android.Views.View/IOnLayoutChangeListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public AddTextPlatformEffect_FastRendererOnLayoutChangeListener() {
        if (AddTextPlatformEffect_FastRendererOnLayoutChangeListener.class == AddTextPlatformEffect_FastRendererOnLayoutChangeListener.class) {
            TypeManager.Activate("AiForms.Effects.Droid.AddTextPlatformEffect+FastRendererOnLayoutChangeListener, AiForms.Effects.Droid", "", this, new Object[0]);
        }
    }

    public AddTextPlatformEffect_FastRendererOnLayoutChangeListener(View view, ViewGroup viewGroup) {
        if (AddTextPlatformEffect_FastRendererOnLayoutChangeListener.class == AddTextPlatformEffect_FastRendererOnLayoutChangeListener.class) {
            TypeManager.Activate("AiForms.Effects.Droid.AddTextPlatformEffect+FastRendererOnLayoutChangeListener, AiForms.Effects.Droid", "Android.Views.View, Mono.Android:Android.Views.ViewGroup, Mono.Android", this, new Object[]{view, viewGroup});
        }
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        n_onLayoutChange(view, i, i2, i3, i4, i5, i6, i7, i8);
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

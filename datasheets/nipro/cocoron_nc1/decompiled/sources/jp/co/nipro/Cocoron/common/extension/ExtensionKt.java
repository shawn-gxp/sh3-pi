package jp.co.nipro.cocoron.common.extension;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import com.github.mikephil.charting.data.Entry;
import kotlin.Metadata;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.MutablePropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;

/* compiled from: Extension.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\u001a\n\u0010\u0015\u001a\u00020\u0016*\u00020\u0003\"/\u0010\u0002\u001a\u00020\u0001*\u00020\u00032\u0006\u0010\u0000\u001a\u00020\u00018F@FX\u0086\u008e\u0002¢\u0006\u0012\n\u0004\b\b\u0010\t\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007\"/\u0010\n\u001a\u00020\u0001*\u00020\u00032\u0006\u0010\u0000\u001a\u00020\u00018F@FX\u0086\u008e\u0002¢\u0006\u0012\n\u0004\b\r\u0010\t\u001a\u0004\b\u000b\u0010\u0005\"\u0004\b\f\u0010\u0007\"/\u0010\u000e\u001a\u00020\u0001*\u00020\u000f2\u0006\u0010\u0000\u001a\u00020\u00018F@FX\u0086\u008e\u0002¢\u0006\u0012\n\u0004\b\u0014\u0010\t\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013¨\u0006\u0017"}, d2 = {"<set-?>", "", "animating", "Landroid/view/View;", "getAnimating", "(Landroid/view/View;)Z", "setAnimating", "(Landroid/view/View;Z)V", "animating$delegate", "Ljp/co/nipro/cocoron/common/extension/FieldProperty;", "flashing", "getFlashing", "setFlashing", "flashing$delegate", "visible", "Lcom/github/mikephil/charting/data/Entry;", "getVisible", "(Lcom/github/mikephil/charting/data/Entry;)Z", "setVisible", "(Lcom/github/mikephil/charting/data/Entry;Z)V", "visible$delegate", "refresh", "", "app_release"}, k = 2, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class ExtensionKt {
    static final /* synthetic */ KProperty[] $$delegatedProperties = {Reflection.mutableProperty1(new MutablePropertyReference1Impl(ExtensionKt.class, "visible", "getVisible(Lcom/github/mikephil/charting/data/Entry;)Z", 1)), Reflection.mutableProperty1(new MutablePropertyReference1Impl(ExtensionKt.class, "flashing", "getFlashing(Landroid/view/View;)Z", 1)), Reflection.mutableProperty1(new MutablePropertyReference1Impl(ExtensionKt.class, "animating", "getAnimating(Landroid/view/View;)Z", 1))};
    private static final FieldProperty visible$delegate = new FieldProperty(new Function1<Entry, Boolean>() { // from class: jp.co.nipro.cocoron.common.extension.ExtensionKt$visible$2
        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Boolean invoke(Entry entry) {
            return Boolean.valueOf(invoke2(entry));
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final boolean invoke2(Entry it) {
            Intrinsics.checkNotNullParameter(it, "it");
            return false;
        }
    });
    private static final FieldProperty flashing$delegate = new FieldProperty(new Function1<View, Boolean>() { // from class: jp.co.nipro.cocoron.common.extension.ExtensionKt$flashing$2
        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Boolean invoke(View view) {
            return Boolean.valueOf(invoke2(view));
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final boolean invoke2(View it) {
            Intrinsics.checkNotNullParameter(it, "it");
            return false;
        }
    });
    private static final FieldProperty animating$delegate = new FieldProperty(new Function1<View, Boolean>() { // from class: jp.co.nipro.cocoron.common.extension.ExtensionKt$animating$2
        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Boolean invoke(View view) {
            return Boolean.valueOf(invoke2(view));
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final boolean invoke2(View it) {
            Intrinsics.checkNotNullParameter(it, "it");
            return false;
        }
    });

    public static final boolean getAnimating(View animating) {
        Intrinsics.checkNotNullParameter(animating, "$this$animating");
        return ((Boolean) animating$delegate.getValue(animating, $$delegatedProperties[2])).booleanValue();
    }

    public static final boolean getFlashing(View flashing) {
        Intrinsics.checkNotNullParameter(flashing, "$this$flashing");
        return ((Boolean) flashing$delegate.getValue(flashing, $$delegatedProperties[1])).booleanValue();
    }

    public static final boolean getVisible(Entry visible) {
        Intrinsics.checkNotNullParameter(visible, "$this$visible");
        return ((Boolean) visible$delegate.getValue(visible, $$delegatedProperties[0])).booleanValue();
    }

    public static final void setAnimating(View animating, boolean z) {
        Intrinsics.checkNotNullParameter(animating, "$this$animating");
        animating$delegate.setValue(animating, $$delegatedProperties[2], Boolean.valueOf(z));
    }

    public static final void setFlashing(View flashing, boolean z) {
        Intrinsics.checkNotNullParameter(flashing, "$this$flashing");
        flashing$delegate.setValue(flashing, $$delegatedProperties[1], Boolean.valueOf(z));
    }

    public static final void setVisible(Entry visible, boolean z) {
        Intrinsics.checkNotNullParameter(visible, "$this$visible");
        visible$delegate.setValue(visible, $$delegatedProperties[0], Boolean.valueOf(z));
    }

    public static final void refresh(final View refresh) {
        Intrinsics.checkNotNullParameter(refresh, "$this$refresh");
        if (getFlashing(refresh) && !getAnimating(refresh)) {
            setAnimating(refresh, true);
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(refresh, "alpha", 0.3f);
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(refresh, "alpha", 1.0f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(ofFloat, ofFloat2);
            animatorSet.setDuration(1000L);
            animatorSet.addListener(new Animator.AnimatorListener() { // from class: jp.co.nipro.cocoron.common.extension.ExtensionKt$refresh$$inlined$addListener$1
                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    Intrinsics.checkParameterIsNotNull(animator, "animator");
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                    Intrinsics.checkParameterIsNotNull(animator, "animator");
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    Intrinsics.checkParameterIsNotNull(animator, "animator");
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    Intrinsics.checkParameterIsNotNull(animator, "animator");
                    ExtensionKt.setAnimating(refresh, false);
                    ExtensionKt.refresh(refresh);
                }
            });
            animatorSet.start();
            return;
        }
        refresh.setAlpha(1.0f);
    }
}

package jp.co.nipro.cocoron.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import java.util.Objects;
import jp.co.nipro.Cocoron.C0009R;

/* loaded from: classes.dex */
public final class ActivityMainBinding implements ViewBinding {
    public final ConstraintLayout container;
    private final ConstraintLayout rootView;

    private ActivityMainBinding(ConstraintLayout rootView, ConstraintLayout container) {
        this.rootView = rootView;
        this.container = container;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static ActivityMainBinding inflate(LayoutInflater inflater) {
        return inflate(inflater, null, false);
    }

    public static ActivityMainBinding inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
        View inflate = inflater.inflate(C0009R.layout.activity_main, parent, false);
        if (attachToParent) {
            parent.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityMainBinding bind(View rootView) {
        Objects.requireNonNull(rootView, "rootView");
        ConstraintLayout constraintLayout = (ConstraintLayout) rootView;
        return new ActivityMainBinding(constraintLayout, constraintLayout);
    }
}

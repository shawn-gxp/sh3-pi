package com.github.mikephil.charting.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import com.github.mikephil.charting.animation.Easing;

/* loaded from: classes.dex */
public class ChartAnimator {
    private ValueAnimator.AnimatorUpdateListener mListener;
    protected float mPhaseY = 1.0f;
    protected float mPhaseX = 1.0f;

    public ChartAnimator() {
    }

    public ChartAnimator(ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        this.mListener = animatorUpdateListener;
    }

    private ObjectAnimator xAnimator(int i, Easing.EasingFunction easingFunction) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "phaseX", 0.0f, 1.0f);
        ofFloat.setInterpolator(easingFunction);
        ofFloat.setDuration(i);
        return ofFloat;
    }

    private ObjectAnimator yAnimator(int i, Easing.EasingFunction easingFunction) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "phaseY", 0.0f, 1.0f);
        ofFloat.setInterpolator(easingFunction);
        ofFloat.setDuration(i);
        return ofFloat;
    }

    public void animateX(int i) {
        animateX(i, Easing.Linear);
    }

    public void animateX(int i, Easing.EasingFunction easingFunction) {
        ObjectAnimator xAnimator = xAnimator(i, easingFunction);
        xAnimator.addUpdateListener(this.mListener);
        xAnimator.start();
    }

    public void animateXY(int i, int i2) {
        animateXY(i, i2, Easing.Linear, Easing.Linear);
    }

    public void animateXY(int i, int i2, Easing.EasingFunction easingFunction) {
        ObjectAnimator xAnimator = xAnimator(i, easingFunction);
        ObjectAnimator yAnimator = yAnimator(i2, easingFunction);
        if (i > i2) {
            xAnimator.addUpdateListener(this.mListener);
        } else {
            yAnimator.addUpdateListener(this.mListener);
        }
        xAnimator.start();
        yAnimator.start();
    }

    public void animateXY(int i, int i2, Easing.EasingFunction easingFunction, Easing.EasingFunction easingFunction2) {
        ObjectAnimator xAnimator = xAnimator(i, easingFunction);
        ObjectAnimator yAnimator = yAnimator(i2, easingFunction2);
        if (i > i2) {
            xAnimator.addUpdateListener(this.mListener);
        } else {
            yAnimator.addUpdateListener(this.mListener);
        }
        xAnimator.start();
        yAnimator.start();
    }

    public void animateY(int i) {
        animateY(i, Easing.Linear);
    }

    public void animateY(int i, Easing.EasingFunction easingFunction) {
        ObjectAnimator yAnimator = yAnimator(i, easingFunction);
        yAnimator.addUpdateListener(this.mListener);
        yAnimator.start();
    }

    public float getPhaseY() {
        return this.mPhaseY;
    }

    public void setPhaseY(float f) {
        if (f > 1.0f) {
            f = 1.0f;
        } else if (f < 0.0f) {
            f = 0.0f;
        }
        this.mPhaseY = f;
    }

    public float getPhaseX() {
        return this.mPhaseX;
    }

    public void setPhaseX(float f) {
        if (f > 1.0f) {
            f = 1.0f;
        } else if (f < 0.0f) {
            f = 0.0f;
        }
        this.mPhaseX = f;
    }
}

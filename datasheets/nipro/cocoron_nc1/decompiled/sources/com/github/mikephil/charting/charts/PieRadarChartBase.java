package com.github.mikephil.charting.charts;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.PieRadarChartTouchListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

/* loaded from: classes.dex */
public abstract class PieRadarChartBase<T extends ChartData<? extends IDataSet<? extends Entry>>> extends Chart<T> {
    protected float mMinOffset;
    private float mRawRotationAngle;
    protected boolean mRotateEnabled;
    private float mRotationAngle;

    @Override // com.github.mikephil.charting.charts.Chart
    protected void calcMinMax() {
    }

    public abstract int getIndexForAngle(float f);

    public abstract float getRadius();

    protected abstract float getRequiredBaseOffset();

    protected abstract float getRequiredLegendOffset();

    @Override // com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
    public float getYChartMax() {
        return 0.0f;
    }

    @Override // com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
    public float getYChartMin() {
        return 0.0f;
    }

    public PieRadarChartBase(Context context) {
        super(context);
        this.mRotationAngle = 270.0f;
        this.mRawRotationAngle = 270.0f;
        this.mRotateEnabled = true;
        this.mMinOffset = 0.0f;
    }

    public PieRadarChartBase(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRotationAngle = 270.0f;
        this.mRawRotationAngle = 270.0f;
        this.mRotateEnabled = true;
        this.mMinOffset = 0.0f;
    }

    public PieRadarChartBase(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mRotationAngle = 270.0f;
        this.mRawRotationAngle = 270.0f;
        this.mRotateEnabled = true;
        this.mMinOffset = 0.0f;
    }

    @Override // com.github.mikephil.charting.charts.Chart
    protected void init() {
        super.init();
        this.mChartTouchListener = new PieRadarChartTouchListener(this);
    }

    @Override // com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
    public int getMaxVisibleCount() {
        return this.mData.getEntryCount();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mTouchEnabled && this.mChartTouchListener != null) {
            return this.mChartTouchListener.onTouch(this, motionEvent);
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public void computeScroll() {
        if (this.mChartTouchListener instanceof PieRadarChartTouchListener) {
            ((PieRadarChartTouchListener) this.mChartTouchListener).computeScroll();
        }
    }

    @Override // com.github.mikephil.charting.charts.Chart
    public void notifyDataSetChanged() {
        if (this.mData == null) {
            return;
        }
        calcMinMax();
        if (this.mLegend != null) {
            this.mLegendRenderer.computeLegend(this.mData);
        }
        calculateOffsets();
    }

    /* JADX WARN: Code restructure failed: missing block: B:16:0x007d, code lost:
    
        if (r2 != 2) goto L19;
     */
    @Override // com.github.mikephil.charting.charts.Chart
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void calculateOffsets() {
        float f;
        float f2;
        float f3;
        float convertDpToPixel;
        float f4;
        float f5;
        float min;
        float f6 = 0.0f;
        if (this.mLegend == null || !this.mLegend.isEnabled() || this.mLegend.isDrawInsideEnabled()) {
            f = 0.0f;
            f2 = 0.0f;
            f3 = 0.0f;
        } else {
            float min2 = Math.min(this.mLegend.mNeededWidth, this.mViewPortHandler.getChartWidth() * this.mLegend.getMaxSizePercent());
            int i = AnonymousClass2.$SwitchMap$com$github$mikephil$charting$components$Legend$LegendOrientation[this.mLegend.getOrientation().ordinal()];
            if (i == 1) {
                if (this.mLegend.getHorizontalAlignment() != Legend.LegendHorizontalAlignment.LEFT && this.mLegend.getHorizontalAlignment() != Legend.LegendHorizontalAlignment.RIGHT) {
                    convertDpToPixel = 0.0f;
                } else if (this.mLegend.getVerticalAlignment() == Legend.LegendVerticalAlignment.CENTER) {
                    convertDpToPixel = min2 + Utils.convertDpToPixel(13.0f);
                } else {
                    convertDpToPixel = min2 + Utils.convertDpToPixel(8.0f);
                    float f7 = this.mLegend.mNeededHeight + this.mLegend.mTextHeightMax;
                    MPPointF center = getCenter();
                    float width = this.mLegend.getHorizontalAlignment() == Legend.LegendHorizontalAlignment.RIGHT ? (getWidth() - convertDpToPixel) + 15.0f : convertDpToPixel - 15.0f;
                    float f8 = f7 + 15.0f;
                    float distanceToCenter = distanceToCenter(width, f8);
                    MPPointF position = getPosition(center, getRadius(), getAngleForPoint(width, f8));
                    float distanceToCenter2 = distanceToCenter(position.x, position.y);
                    float convertDpToPixel2 = Utils.convertDpToPixel(5.0f);
                    if (f8 < center.y || getHeight() - convertDpToPixel <= getWidth()) {
                        convertDpToPixel = distanceToCenter < distanceToCenter2 ? convertDpToPixel2 + (distanceToCenter2 - distanceToCenter) : 0.0f;
                    }
                    MPPointF.recycleInstance(center);
                    MPPointF.recycleInstance(position);
                }
                int i2 = AnonymousClass2.$SwitchMap$com$github$mikephil$charting$components$Legend$LegendHorizontalAlignment[this.mLegend.getHorizontalAlignment().ordinal()];
                if (i2 == 1) {
                    f4 = 0.0f;
                    f5 = 0.0f;
                    f6 = convertDpToPixel;
                    convertDpToPixel = 0.0f;
                    f6 += getRequiredBaseOffset();
                    f = convertDpToPixel + getRequiredBaseOffset();
                    f3 = f5 + getRequiredBaseOffset();
                    f2 = f4 + getRequiredBaseOffset();
                } else if (i2 != 2) {
                    if (i2 == 3) {
                        int i3 = AnonymousClass2.$SwitchMap$com$github$mikephil$charting$components$Legend$LegendVerticalAlignment[this.mLegend.getVerticalAlignment().ordinal()];
                        if (i3 == 1) {
                            min = Math.min(this.mLegend.mNeededHeight, this.mViewPortHandler.getChartHeight() * this.mLegend.getMaxSizePercent());
                            f5 = min;
                            convertDpToPixel = 0.0f;
                            f4 = 0.0f;
                            f6 += getRequiredBaseOffset();
                            f = convertDpToPixel + getRequiredBaseOffset();
                            f3 = f5 + getRequiredBaseOffset();
                            f2 = f4 + getRequiredBaseOffset();
                        } else if (i3 == 2) {
                            min = Math.min(this.mLegend.mNeededHeight, this.mViewPortHandler.getChartHeight() * this.mLegend.getMaxSizePercent());
                            f4 = min;
                            convertDpToPixel = 0.0f;
                            f5 = 0.0f;
                            f6 += getRequiredBaseOffset();
                            f = convertDpToPixel + getRequiredBaseOffset();
                            f3 = f5 + getRequiredBaseOffset();
                            f2 = f4 + getRequiredBaseOffset();
                        }
                    }
                    convertDpToPixel = 0.0f;
                    f4 = 0.0f;
                    f5 = f4;
                    f6 += getRequiredBaseOffset();
                    f = convertDpToPixel + getRequiredBaseOffset();
                    f3 = f5 + getRequiredBaseOffset();
                    f2 = f4 + getRequiredBaseOffset();
                } else {
                    f4 = 0.0f;
                    f5 = f4;
                    f6 += getRequiredBaseOffset();
                    f = convertDpToPixel + getRequiredBaseOffset();
                    f3 = f5 + getRequiredBaseOffset();
                    f2 = f4 + getRequiredBaseOffset();
                }
            } else {
                if (i == 2 && (this.mLegend.getVerticalAlignment() == Legend.LegendVerticalAlignment.TOP || this.mLegend.getVerticalAlignment() == Legend.LegendVerticalAlignment.BOTTOM)) {
                    min = Math.min(this.mLegend.mNeededHeight + getRequiredLegendOffset(), this.mViewPortHandler.getChartHeight() * this.mLegend.getMaxSizePercent());
                    int i4 = AnonymousClass2.$SwitchMap$com$github$mikephil$charting$components$Legend$LegendVerticalAlignment[this.mLegend.getVerticalAlignment().ordinal()];
                    if (i4 != 1) {
                    }
                    f5 = min;
                    convertDpToPixel = 0.0f;
                    f4 = 0.0f;
                    f6 += getRequiredBaseOffset();
                    f = convertDpToPixel + getRequiredBaseOffset();
                    f3 = f5 + getRequiredBaseOffset();
                    f2 = f4 + getRequiredBaseOffset();
                }
                convertDpToPixel = 0.0f;
                f4 = 0.0f;
                f5 = f4;
                f6 += getRequiredBaseOffset();
                f = convertDpToPixel + getRequiredBaseOffset();
                f3 = f5 + getRequiredBaseOffset();
                f2 = f4 + getRequiredBaseOffset();
            }
        }
        float convertDpToPixel3 = Utils.convertDpToPixel(this.mMinOffset);
        if (this instanceof RadarChart) {
            XAxis xAxis = getXAxis();
            if (xAxis.isEnabled() && xAxis.isDrawLabelsEnabled()) {
                convertDpToPixel3 = Math.max(convertDpToPixel3, xAxis.mLabelRotatedWidth);
            }
        }
        float extraTopOffset = f3 + getExtraTopOffset();
        float extraRightOffset = f + getExtraRightOffset();
        float extraBottomOffset = f2 + getExtraBottomOffset();
        float max = Math.max(convertDpToPixel3, f6 + getExtraLeftOffset());
        float max2 = Math.max(convertDpToPixel3, extraTopOffset);
        float max3 = Math.max(convertDpToPixel3, extraRightOffset);
        float max4 = Math.max(convertDpToPixel3, Math.max(getRequiredBaseOffset(), extraBottomOffset));
        this.mViewPortHandler.restrainViewPort(max, max2, max3, max4);
        if (this.mLogEnabled) {
            Log.i(Chart.LOG_TAG, "offsetLeft: " + max + ", offsetTop: " + max2 + ", offsetRight: " + max3 + ", offsetBottom: " + max4);
        }
    }

    /* renamed from: com.github.mikephil.charting.charts.PieRadarChartBase$2, reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$github$mikephil$charting$components$Legend$LegendHorizontalAlignment;
        static final /* synthetic */ int[] $SwitchMap$com$github$mikephil$charting$components$Legend$LegendOrientation;
        static final /* synthetic */ int[] $SwitchMap$com$github$mikephil$charting$components$Legend$LegendVerticalAlignment;

        static {
            int[] iArr = new int[Legend.LegendOrientation.values().length];
            $SwitchMap$com$github$mikephil$charting$components$Legend$LegendOrientation = iArr;
            try {
                iArr[Legend.LegendOrientation.VERTICAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$github$mikephil$charting$components$Legend$LegendOrientation[Legend.LegendOrientation.HORIZONTAL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            int[] iArr2 = new int[Legend.LegendHorizontalAlignment.values().length];
            $SwitchMap$com$github$mikephil$charting$components$Legend$LegendHorizontalAlignment = iArr2;
            try {
                iArr2[Legend.LegendHorizontalAlignment.LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$github$mikephil$charting$components$Legend$LegendHorizontalAlignment[Legend.LegendHorizontalAlignment.RIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$github$mikephil$charting$components$Legend$LegendHorizontalAlignment[Legend.LegendHorizontalAlignment.CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError unused5) {
            }
            int[] iArr3 = new int[Legend.LegendVerticalAlignment.values().length];
            $SwitchMap$com$github$mikephil$charting$components$Legend$LegendVerticalAlignment = iArr3;
            try {
                iArr3[Legend.LegendVerticalAlignment.TOP.ordinal()] = 1;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$github$mikephil$charting$components$Legend$LegendVerticalAlignment[Legend.LegendVerticalAlignment.BOTTOM.ordinal()] = 2;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    public float getAngleForPoint(float f, float f2) {
        MPPointF centerOffsets = getCenterOffsets();
        double d = f - centerOffsets.x;
        double d2 = f2 - centerOffsets.y;
        float degrees = (float) Math.toDegrees(Math.acos(d2 / Math.sqrt((d * d) + (d2 * d2))));
        if (f > centerOffsets.x) {
            degrees = 360.0f - degrees;
        }
        float f3 = degrees + 90.0f;
        if (f3 > 360.0f) {
            f3 -= 360.0f;
        }
        MPPointF.recycleInstance(centerOffsets);
        return f3;
    }

    public MPPointF getPosition(MPPointF mPPointF, float f, float f2) {
        MPPointF mPPointF2 = MPPointF.getInstance(0.0f, 0.0f);
        getPosition(mPPointF, f, f2, mPPointF2);
        return mPPointF2;
    }

    public void getPosition(MPPointF mPPointF, float f, float f2, MPPointF mPPointF2) {
        double d = f;
        double d2 = f2;
        mPPointF2.x = (float) (mPPointF.x + (Math.cos(Math.toRadians(d2)) * d));
        mPPointF2.y = (float) (mPPointF.y + (d * Math.sin(Math.toRadians(d2))));
    }

    public float distanceToCenter(float f, float f2) {
        float f3;
        float f4;
        MPPointF centerOffsets = getCenterOffsets();
        if (f > centerOffsets.x) {
            f3 = f - centerOffsets.x;
        } else {
            f3 = centerOffsets.x - f;
        }
        if (f2 > centerOffsets.y) {
            f4 = f2 - centerOffsets.y;
        } else {
            f4 = centerOffsets.y - f2;
        }
        float sqrt = (float) Math.sqrt(Math.pow(f3, 2.0d) + Math.pow(f4, 2.0d));
        MPPointF.recycleInstance(centerOffsets);
        return sqrt;
    }

    public void setRotationAngle(float f) {
        this.mRawRotationAngle = f;
        this.mRotationAngle = Utils.getNormalizedAngle(f);
    }

    public float getRawRotationAngle() {
        return this.mRawRotationAngle;
    }

    public float getRotationAngle() {
        return this.mRotationAngle;
    }

    public void setRotationEnabled(boolean z) {
        this.mRotateEnabled = z;
    }

    public boolean isRotationEnabled() {
        return this.mRotateEnabled;
    }

    public float getMinOffset() {
        return this.mMinOffset;
    }

    public void setMinOffset(float f) {
        this.mMinOffset = f;
    }

    public float getDiameter() {
        RectF contentRect = this.mViewPortHandler.getContentRect();
        contentRect.left += getExtraLeftOffset();
        contentRect.top += getExtraTopOffset();
        contentRect.right -= getExtraRightOffset();
        contentRect.bottom -= getExtraBottomOffset();
        return Math.min(contentRect.width(), contentRect.height());
    }

    public void spin(int i, float f, float f2, Easing.EasingFunction easingFunction) {
        setRotationAngle(f);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "rotationAngle", f, f2);
        ofFloat.setDuration(i);
        ofFloat.setInterpolator(easingFunction);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.github.mikephil.charting.charts.PieRadarChartBase.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                PieRadarChartBase.this.postInvalidate();
            }
        });
        ofFloat.start();
    }
}

package com.github.mikephil.charting.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import androidx.core.view.ViewCompat;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.lang.ref.WeakReference;
import java.util.List;

/* loaded from: classes.dex */
public class PieChartRenderer extends DataRenderer {
    protected Canvas mBitmapCanvas;
    private RectF mCenterTextLastBounds;
    private CharSequence mCenterTextLastValue;
    private StaticLayout mCenterTextLayout;
    private TextPaint mCenterTextPaint;
    protected PieChart mChart;
    protected WeakReference<Bitmap> mDrawBitmap;
    protected Path mDrawCenterTextPathBuffer;
    protected RectF mDrawHighlightedRectF;
    private Paint mEntryLabelsPaint;
    private Path mHoleCirclePath;
    protected Paint mHolePaint;
    private RectF mInnerRectBuffer;
    private Path mPathBuffer;
    private RectF[] mRectBuffer;
    protected Paint mTransparentCirclePaint;
    protected Paint mValueLinePaint;

    @Override // com.github.mikephil.charting.renderer.DataRenderer
    public void initBuffers() {
    }

    public PieChartRenderer(PieChart pieChart, ChartAnimator chartAnimator, ViewPortHandler viewPortHandler) {
        super(chartAnimator, viewPortHandler);
        this.mCenterTextLastBounds = new RectF();
        this.mRectBuffer = new RectF[]{new RectF(), new RectF(), new RectF()};
        this.mPathBuffer = new Path();
        this.mInnerRectBuffer = new RectF();
        this.mHoleCirclePath = new Path();
        this.mDrawCenterTextPathBuffer = new Path();
        this.mDrawHighlightedRectF = new RectF();
        this.mChart = pieChart;
        Paint paint = new Paint(1);
        this.mHolePaint = paint;
        paint.setColor(-1);
        this.mHolePaint.setStyle(Paint.Style.FILL);
        Paint paint2 = new Paint(1);
        this.mTransparentCirclePaint = paint2;
        paint2.setColor(-1);
        this.mTransparentCirclePaint.setStyle(Paint.Style.FILL);
        this.mTransparentCirclePaint.setAlpha(105);
        TextPaint textPaint = new TextPaint(1);
        this.mCenterTextPaint = textPaint;
        textPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mCenterTextPaint.setTextSize(Utils.convertDpToPixel(12.0f));
        this.mValuePaint.setTextSize(Utils.convertDpToPixel(13.0f));
        this.mValuePaint.setColor(-1);
        this.mValuePaint.setTextAlign(Paint.Align.CENTER);
        Paint paint3 = new Paint(1);
        this.mEntryLabelsPaint = paint3;
        paint3.setColor(-1);
        this.mEntryLabelsPaint.setTextAlign(Paint.Align.CENTER);
        this.mEntryLabelsPaint.setTextSize(Utils.convertDpToPixel(13.0f));
        Paint paint4 = new Paint(1);
        this.mValueLinePaint = paint4;
        paint4.setStyle(Paint.Style.STROKE);
    }

    public Paint getPaintHole() {
        return this.mHolePaint;
    }

    public Paint getPaintTransparentCircle() {
        return this.mTransparentCirclePaint;
    }

    public TextPaint getPaintCenterText() {
        return this.mCenterTextPaint;
    }

    public Paint getPaintEntryLabels() {
        return this.mEntryLabelsPaint;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.github.mikephil.charting.renderer.DataRenderer
    public void drawData(Canvas canvas) {
        int chartWidth = (int) this.mViewPortHandler.getChartWidth();
        int chartHeight = (int) this.mViewPortHandler.getChartHeight();
        WeakReference<Bitmap> weakReference = this.mDrawBitmap;
        Bitmap bitmap = weakReference == null ? null : weakReference.get();
        if (bitmap == null || bitmap.getWidth() != chartWidth || bitmap.getHeight() != chartHeight) {
            if (chartWidth <= 0 || chartHeight <= 0) {
                return;
            }
            bitmap = Bitmap.createBitmap(chartWidth, chartHeight, Bitmap.Config.ARGB_4444);
            this.mDrawBitmap = new WeakReference<>(bitmap);
            this.mBitmapCanvas = new Canvas(bitmap);
        }
        bitmap.eraseColor(0);
        for (IPieDataSet iPieDataSet : ((PieData) this.mChart.getData()).getDataSets()) {
            if (iPieDataSet.isVisible() && iPieDataSet.getEntryCount() > 0) {
                drawDataSet(canvas, iPieDataSet);
            }
        }
    }

    protected float calculateMinimumRadiusForSpacedSlice(MPPointF mPPointF, float f, float f2, float f3, float f4, float f5, float f6) {
        double d = (f5 + f6) * 0.017453292f;
        float cos = mPPointF.x + (((float) Math.cos(d)) * f);
        float sin = mPPointF.y + (((float) Math.sin(d)) * f);
        double d2 = (f5 + (f6 / 2.0f)) * 0.017453292f;
        return (float) ((f - ((float) ((Math.sqrt(Math.pow(cos - f3, 2.0d) + Math.pow(sin - f4, 2.0d)) / 2.0d) * Math.tan(((180.0d - f2) / 2.0d) * 0.017453292519943295d)))) - Math.sqrt(Math.pow((mPPointF.x + (((float) Math.cos(d2)) * f)) - ((cos + f3) / 2.0f), 2.0d) + Math.pow((mPPointF.y + (((float) Math.sin(d2)) * f)) - ((sin + f4) / 2.0f), 2.0d)));
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected float getSliceSpace(IPieDataSet iPieDataSet) {
        if (!iPieDataSet.isAutomaticallyDisableSliceSpacingEnabled()) {
            return iPieDataSet.getSliceSpace();
        }
        if (iPieDataSet.getSliceSpace() / this.mViewPortHandler.getSmallestContentExtension() > (iPieDataSet.getYMin() / ((PieData) this.mChart.getData()).getYValueSum()) * 2.0f) {
            return 0.0f;
        }
        return iPieDataSet.getSliceSpace();
    }

    protected void drawDataSet(Canvas canvas, IPieDataSet iPieDataSet) {
        int i;
        int i2;
        int i3;
        float f;
        float f2;
        float[] fArr;
        float f3;
        float f4;
        int i4;
        RectF rectF;
        RectF rectF2;
        MPPointF mPPointF;
        float f5;
        MPPointF mPPointF2;
        int i5;
        float f6;
        MPPointF mPPointF3;
        IPieDataSet iPieDataSet2 = iPieDataSet;
        float rotationAngle = this.mChart.getRotationAngle();
        float phaseX = this.mAnimator.getPhaseX();
        float phaseY = this.mAnimator.getPhaseY();
        RectF circleBox = this.mChart.getCircleBox();
        int entryCount = iPieDataSet.getEntryCount();
        float[] drawAngles = this.mChart.getDrawAngles();
        MPPointF centerCircleBox = this.mChart.getCenterCircleBox();
        float radius = this.mChart.getRadius();
        boolean z = this.mChart.isDrawHoleEnabled() && !this.mChart.isDrawSlicesUnderHoleEnabled();
        float holeRadius = z ? (this.mChart.getHoleRadius() / 100.0f) * radius : 0.0f;
        float holeRadius2 = (radius - ((this.mChart.getHoleRadius() * radius) / 100.0f)) / 2.0f;
        RectF rectF3 = new RectF();
        boolean z2 = z && this.mChart.isDrawRoundedSlicesEnabled();
        int i6 = 0;
        for (int i7 = 0; i7 < entryCount; i7++) {
            if (Math.abs(iPieDataSet2.getEntryForIndex(i7).getY()) > Utils.FLOAT_EPSILON) {
                i6++;
            }
        }
        float sliceSpace = i6 <= 1 ? 0.0f : getSliceSpace(iPieDataSet2);
        int i8 = 0;
        float f7 = 0.0f;
        while (i8 < entryCount) {
            float f8 = drawAngles[i8];
            if (Math.abs(iPieDataSet2.getEntryForIndex(i8).getY()) > Utils.FLOAT_EPSILON && (!this.mChart.needsHighlight(i8) || z2)) {
                boolean z3 = sliceSpace > 0.0f && f8 <= 180.0f;
                i = entryCount;
                this.mRenderPaint.setColor(iPieDataSet2.getColor(i8));
                float f9 = i6 == 1 ? 0.0f : sliceSpace / (radius * 0.017453292f);
                float f10 = rotationAngle + ((f7 + (f9 / 2.0f)) * phaseY);
                float f11 = (f8 - f9) * phaseY;
                float f12 = f11 < 0.0f ? 0.0f : f11;
                this.mPathBuffer.reset();
                if (z2) {
                    float f13 = radius - holeRadius2;
                    i2 = i8;
                    i3 = i6;
                    double d = f10 * 0.017453292f;
                    f = rotationAngle;
                    f2 = phaseX;
                    float cos = centerCircleBox.x + (((float) Math.cos(d)) * f13);
                    float sin = centerCircleBox.y + (f13 * ((float) Math.sin(d)));
                    rectF3.set(cos - holeRadius2, sin - holeRadius2, cos + holeRadius2, sin + holeRadius2);
                } else {
                    i2 = i8;
                    i3 = i6;
                    f = rotationAngle;
                    f2 = phaseX;
                }
                double d2 = f10 * 0.017453292f;
                float f14 = holeRadius;
                float cos2 = centerCircleBox.x + (((float) Math.cos(d2)) * radius);
                float sin2 = centerCircleBox.y + (((float) Math.sin(d2)) * radius);
                if (f12 >= 360.0f && f12 % 360.0f <= Utils.FLOAT_EPSILON) {
                    fArr = drawAngles;
                    this.mPathBuffer.addCircle(centerCircleBox.x, centerCircleBox.y, radius, Path.Direction.CW);
                } else {
                    fArr = drawAngles;
                    if (z2) {
                        this.mPathBuffer.arcTo(rectF3, f10 + 180.0f, -180.0f);
                    }
                    this.mPathBuffer.arcTo(circleBox, f10, f12);
                }
                RectF rectF4 = rectF3;
                this.mInnerRectBuffer.set(centerCircleBox.x - f14, centerCircleBox.y - f14, centerCircleBox.x + f14, centerCircleBox.y + f14);
                if (!z) {
                    f3 = radius;
                    f4 = f14;
                    i4 = i3;
                    rectF = rectF4;
                    rectF2 = circleBox;
                    mPPointF = centerCircleBox;
                    f5 = 360.0f;
                } else if (f14 > 0.0f || z3) {
                    if (z3) {
                        i4 = i3;
                        rectF2 = circleBox;
                        f4 = f14;
                        i5 = 1;
                        f3 = radius;
                        mPPointF2 = centerCircleBox;
                        float calculateMinimumRadiusForSpacedSlice = calculateMinimumRadiusForSpacedSlice(centerCircleBox, radius, f8 * phaseY, cos2, sin2, f10, f12);
                        if (calculateMinimumRadiusForSpacedSlice < 0.0f) {
                            calculateMinimumRadiusForSpacedSlice = -calculateMinimumRadiusForSpacedSlice;
                        }
                        f6 = Math.max(f4, calculateMinimumRadiusForSpacedSlice);
                    } else {
                        f3 = radius;
                        mPPointF2 = centerCircleBox;
                        f4 = f14;
                        i4 = i3;
                        rectF2 = circleBox;
                        i5 = 1;
                        f6 = f4;
                    }
                    float f15 = (i4 == i5 || f6 == 0.0f) ? 0.0f : sliceSpace / (f6 * 0.017453292f);
                    float f16 = f + ((f7 + (f15 / 2.0f)) * phaseY);
                    float f17 = (f8 - f15) * phaseY;
                    if (f17 < 0.0f) {
                        f17 = 0.0f;
                    }
                    float f18 = f16 + f17;
                    if (f12 >= 360.0f && f12 % 360.0f <= Utils.FLOAT_EPSILON) {
                        this.mPathBuffer.addCircle(mPPointF2.x, mPPointF2.y, f6, Path.Direction.CCW);
                        mPPointF3 = mPPointF2;
                        rectF = rectF4;
                    } else {
                        if (z2) {
                            float f19 = f3 - holeRadius2;
                            double d3 = 0.017453292f * f18;
                            mPPointF3 = mPPointF2;
                            float cos3 = mPPointF2.x + (((float) Math.cos(d3)) * f19);
                            float sin3 = mPPointF3.y + (f19 * ((float) Math.sin(d3)));
                            rectF = rectF4;
                            rectF.set(cos3 - holeRadius2, sin3 - holeRadius2, cos3 + holeRadius2, sin3 + holeRadius2);
                            this.mPathBuffer.arcTo(rectF, f18, 180.0f);
                        } else {
                            mPPointF3 = mPPointF2;
                            rectF = rectF4;
                            double d4 = f18 * 0.017453292f;
                            this.mPathBuffer.lineTo(mPPointF3.x + (((float) Math.cos(d4)) * f6), mPPointF3.y + (f6 * ((float) Math.sin(d4))));
                        }
                        this.mPathBuffer.arcTo(this.mInnerRectBuffer, f18, -f17);
                    }
                    mPPointF = mPPointF3;
                    this.mPathBuffer.close();
                    this.mBitmapCanvas.drawPath(this.mPathBuffer, this.mRenderPaint);
                    f7 += f8 * f2;
                } else {
                    f3 = radius;
                    f4 = f14;
                    i4 = i3;
                    rectF = rectF4;
                    f5 = 360.0f;
                    rectF2 = circleBox;
                    mPPointF = centerCircleBox;
                }
                if (f12 % f5 > Utils.FLOAT_EPSILON) {
                    if (z3) {
                        float calculateMinimumRadiusForSpacedSlice2 = calculateMinimumRadiusForSpacedSlice(mPPointF, f3, f8 * phaseY, cos2, sin2, f10, f12);
                        double d5 = 0.017453292f * (f10 + (f12 / 2.0f));
                        this.mPathBuffer.lineTo(mPPointF.x + (((float) Math.cos(d5)) * calculateMinimumRadiusForSpacedSlice2), mPPointF.y + (calculateMinimumRadiusForSpacedSlice2 * ((float) Math.sin(d5))));
                    } else {
                        this.mPathBuffer.lineTo(mPPointF.x, mPPointF.y);
                    }
                }
                this.mPathBuffer.close();
                this.mBitmapCanvas.drawPath(this.mPathBuffer, this.mRenderPaint);
                f7 += f8 * f2;
            } else {
                f7 += f8 * phaseX;
                i2 = i8;
                f3 = radius;
                f = rotationAngle;
                f2 = phaseX;
                rectF2 = circleBox;
                i = entryCount;
                fArr = drawAngles;
                i4 = i6;
                rectF = rectF3;
                f4 = holeRadius;
                mPPointF = centerCircleBox;
            }
            i8 = i2 + 1;
            iPieDataSet2 = iPieDataSet;
            holeRadius = f4;
            rectF3 = rectF;
            centerCircleBox = mPPointF;
            i6 = i4;
            radius = f3;
            entryCount = i;
            circleBox = rectF2;
            rotationAngle = f;
            phaseX = f2;
            drawAngles = fArr;
        }
        MPPointF.recycleInstance(centerCircleBox);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.github.mikephil.charting.renderer.DataRenderer
    public void drawValues(Canvas canvas) {
        int i;
        List<IPieDataSet> list;
        float f;
        float[] fArr;
        float[] fArr2;
        float f2;
        float f3;
        float f4;
        float f5;
        MPPointF mPPointF;
        Canvas canvas2;
        PieDataSet.ValuePosition valuePosition;
        float f6;
        float f7;
        float f8;
        boolean z;
        float f9;
        float f10;
        float f11;
        MPPointF mPPointF2;
        MPPointF mPPointF3;
        PieEntry pieEntry;
        ValueFormatter valueFormatter;
        float f12;
        IPieDataSet iPieDataSet;
        Canvas canvas3;
        String str;
        MPPointF mPPointF4;
        MPPointF mPPointF5;
        Canvas canvas4 = canvas;
        MPPointF centerCircleBox = this.mChart.getCenterCircleBox();
        float radius = this.mChart.getRadius();
        float rotationAngle = this.mChart.getRotationAngle();
        float[] drawAngles = this.mChart.getDrawAngles();
        float[] absoluteAngles = this.mChart.getAbsoluteAngles();
        float phaseX = this.mAnimator.getPhaseX();
        float phaseY = this.mAnimator.getPhaseY();
        float holeRadius = (radius - ((this.mChart.getHoleRadius() * radius) / 100.0f)) / 2.0f;
        float holeRadius2 = this.mChart.getHoleRadius() / 100.0f;
        float f13 = (radius / 10.0f) * 3.6f;
        if (this.mChart.isDrawHoleEnabled()) {
            f13 = (radius - (radius * holeRadius2)) / 2.0f;
            if (!this.mChart.isDrawSlicesUnderHoleEnabled() && this.mChart.isDrawRoundedSlicesEnabled()) {
                rotationAngle = (float) (rotationAngle + ((holeRadius * 360.0f) / (radius * 6.283185307179586d)));
            }
        }
        float f14 = rotationAngle;
        float f15 = radius - f13;
        PieData pieData = (PieData) this.mChart.getData();
        List<IPieDataSet> dataSets = pieData.getDataSets();
        float yValueSum = pieData.getYValueSum();
        boolean isDrawEntryLabelsEnabled = this.mChart.isDrawEntryLabelsEnabled();
        canvas.save();
        float convertDpToPixel = Utils.convertDpToPixel(5.0f);
        int i2 = 0;
        int i3 = 0;
        while (i3 < dataSets.size()) {
            IPieDataSet iPieDataSet2 = dataSets.get(i3);
            boolean isDrawValuesEnabled = iPieDataSet2.isDrawValuesEnabled();
            if (isDrawValuesEnabled || isDrawEntryLabelsEnabled) {
                PieDataSet.ValuePosition xValuePosition = iPieDataSet2.getXValuePosition();
                PieDataSet.ValuePosition yValuePosition = iPieDataSet2.getYValuePosition();
                applyValueTextStyle(iPieDataSet2);
                int i4 = i2;
                i = i3;
                float calcTextHeight = Utils.calcTextHeight(this.mValuePaint, "Q") + Utils.convertDpToPixel(4.0f);
                ValueFormatter valueFormatter2 = iPieDataSet2.getValueFormatter();
                int entryCount = iPieDataSet2.getEntryCount();
                list = dataSets;
                this.mValueLinePaint.setColor(iPieDataSet2.getValueLineColor());
                this.mValueLinePaint.setStrokeWidth(Utils.convertDpToPixel(iPieDataSet2.getValueLineWidth()));
                float sliceSpace = getSliceSpace(iPieDataSet2);
                MPPointF mPPointF6 = MPPointF.getInstance(iPieDataSet2.getIconsOffset());
                MPPointF mPPointF7 = centerCircleBox;
                mPPointF6.x = Utils.convertDpToPixel(mPPointF6.x);
                mPPointF6.y = Utils.convertDpToPixel(mPPointF6.y);
                int i5 = 0;
                while (i5 < entryCount) {
                    MPPointF mPPointF8 = mPPointF6;
                    PieEntry entryForIndex = iPieDataSet2.getEntryForIndex(i5);
                    int i6 = entryCount;
                    float f16 = f14 + (((i4 == 0 ? 0.0f : absoluteAngles[i4 - 1] * phaseX) + ((drawAngles[i4] - ((sliceSpace / (f15 * 0.017453292f)) / 2.0f)) / 2.0f)) * phaseY);
                    float f17 = sliceSpace;
                    String pieLabel = valueFormatter2.getPieLabel(this.mChart.isUsePercentValuesEnabled() ? (entryForIndex.getY() / yValueSum) * 100.0f : entryForIndex.getY(), entryForIndex);
                    float[] fArr3 = drawAngles;
                    String label = entryForIndex.getLabel();
                    ValueFormatter valueFormatter3 = valueFormatter2;
                    double d = f16 * 0.017453292f;
                    float[] fArr4 = absoluteAngles;
                    float f18 = phaseX;
                    float cos = (float) Math.cos(d);
                    float f19 = phaseY;
                    float sin = (float) Math.sin(d);
                    boolean z2 = isDrawEntryLabelsEnabled && xValuePosition == PieDataSet.ValuePosition.OUTSIDE_SLICE;
                    float f20 = f14;
                    boolean z3 = isDrawValuesEnabled && yValuePosition == PieDataSet.ValuePosition.OUTSIDE_SLICE;
                    boolean z4 = isDrawEntryLabelsEnabled && xValuePosition == PieDataSet.ValuePosition.INSIDE_SLICE;
                    PieDataSet.ValuePosition valuePosition2 = xValuePosition;
                    boolean z5 = isDrawValuesEnabled && yValuePosition == PieDataSet.ValuePosition.INSIDE_SLICE;
                    if (z2 || z3) {
                        float valueLinePart1Length = iPieDataSet2.getValueLinePart1Length();
                        float valueLinePart2Length = iPieDataSet2.getValueLinePart2Length();
                        float valueLinePart1OffsetPercentage = iPieDataSet2.getValueLinePart1OffsetPercentage() / 100.0f;
                        valuePosition = yValuePosition;
                        if (this.mChart.isDrawHoleEnabled()) {
                            float f21 = radius * holeRadius2;
                            f6 = ((radius - f21) * valueLinePart1OffsetPercentage) + f21;
                        } else {
                            f6 = radius * valueLinePart1OffsetPercentage;
                        }
                        float abs = iPieDataSet2.isValueLineVariableLength() ? valueLinePart2Length * f15 * ((float) Math.abs(Math.sin(d))) : valueLinePart2Length * f15;
                        MPPointF mPPointF9 = mPPointF7;
                        float f22 = (f6 * cos) + mPPointF9.x;
                        float f23 = (f6 * sin) + mPPointF9.y;
                        float f24 = (valueLinePart1Length + 1.0f) * f15;
                        f7 = radius;
                        float f25 = (f24 * cos) + mPPointF9.x;
                        f8 = sin;
                        float f26 = mPPointF9.y + (f24 * sin);
                        z = z4;
                        f9 = cos;
                        double d2 = f16 % 360.0d;
                        if (d2 >= 90.0d && d2 <= 270.0d) {
                            float f27 = f25 - abs;
                            this.mValuePaint.setTextAlign(Paint.Align.RIGHT);
                            if (z2) {
                                this.mEntryLabelsPaint.setTextAlign(Paint.Align.RIGHT);
                            }
                            f10 = f27;
                            f11 = f27 - convertDpToPixel;
                        } else {
                            f10 = f25 + abs;
                            this.mValuePaint.setTextAlign(Paint.Align.LEFT);
                            if (z2) {
                                this.mEntryLabelsPaint.setTextAlign(Paint.Align.LEFT);
                            }
                            f11 = f10 + convertDpToPixel;
                        }
                        if (iPieDataSet2.getValueLineColor() != 1122867) {
                            if (iPieDataSet2.isUsingSliceColorAsValueLineColor()) {
                                this.mValueLinePaint.setColor(iPieDataSet2.getColor(i5));
                            }
                            valueFormatter = valueFormatter3;
                            f12 = f15;
                            iPieDataSet = iPieDataSet2;
                            mPPointF2 = mPPointF9;
                            mPPointF3 = mPPointF8;
                            pieEntry = entryForIndex;
                            canvas.drawLine(f22, f23, f25, f26, this.mValueLinePaint);
                            canvas.drawLine(f25, f26, f10, f26, this.mValueLinePaint);
                        } else {
                            mPPointF2 = mPPointF9;
                            mPPointF3 = mPPointF8;
                            pieEntry = entryForIndex;
                            valueFormatter = valueFormatter3;
                            f12 = f15;
                            iPieDataSet = iPieDataSet2;
                        }
                        if (z2 && z3) {
                            drawValue(canvas, pieLabel, f11, f26, iPieDataSet.getValueTextColor(i5));
                            if (i5 >= pieData.getEntryCount() || label == null) {
                                canvas3 = canvas;
                                str = label;
                            } else {
                                canvas3 = canvas;
                                str = label;
                                drawEntryLabel(canvas3, str, f11, f26 + calcTextHeight);
                            }
                        } else {
                            canvas3 = canvas;
                            str = label;
                            if (z2) {
                                if (i5 < pieData.getEntryCount() && str != null) {
                                    drawEntryLabel(canvas3, str, f11, f26 + (calcTextHeight / 2.0f));
                                }
                            } else if (z3) {
                                drawValue(canvas, pieLabel, f11, f26 + (calcTextHeight / 2.0f), iPieDataSet.getValueTextColor(i5));
                            }
                        }
                    } else {
                        valuePosition = yValuePosition;
                        f7 = radius;
                        z = z4;
                        f9 = cos;
                        mPPointF2 = mPPointF7;
                        mPPointF3 = mPPointF8;
                        pieEntry = entryForIndex;
                        valueFormatter = valueFormatter3;
                        str = label;
                        canvas3 = canvas;
                        f8 = sin;
                        f12 = f15;
                        iPieDataSet = iPieDataSet2;
                    }
                    if (z || z5) {
                        mPPointF4 = mPPointF2;
                        float f28 = (f12 * f9) + mPPointF4.x;
                        float f29 = (f12 * f8) + mPPointF4.y;
                        this.mValuePaint.setTextAlign(Paint.Align.CENTER);
                        if (z && z5) {
                            drawValue(canvas, pieLabel, f28, f29, iPieDataSet.getValueTextColor(i5));
                            if (i5 < pieData.getEntryCount() && str != null) {
                                drawEntryLabel(canvas3, str, f28, f29 + calcTextHeight);
                            }
                        } else {
                            if (z) {
                                if (i5 < pieData.getEntryCount() && str != null) {
                                    drawEntryLabel(canvas3, str, f28, f29 + (calcTextHeight / 2.0f));
                                }
                            } else if (z5) {
                                drawValue(canvas, pieLabel, f28, f29 + (calcTextHeight / 2.0f), iPieDataSet.getValueTextColor(i5));
                            }
                            if (pieEntry.getIcon() == null && iPieDataSet.isDrawIconsEnabled()) {
                                Drawable icon = pieEntry.getIcon();
                                mPPointF5 = mPPointF3;
                                Utils.drawImage(canvas, icon, (int) (((f12 + mPPointF5.y) * f9) + mPPointF4.x), (int) (((f12 + mPPointF5.y) * f8) + mPPointF4.y + mPPointF5.x), icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                            } else {
                                mPPointF5 = mPPointF3;
                            }
                            i4++;
                            i5++;
                            mPPointF6 = mPPointF5;
                            mPPointF7 = mPPointF4;
                            iPieDataSet2 = iPieDataSet;
                            sliceSpace = f17;
                            entryCount = i6;
                            drawAngles = fArr3;
                            f15 = f12;
                            absoluteAngles = fArr4;
                            phaseX = f18;
                            phaseY = f19;
                            f14 = f20;
                            xValuePosition = valuePosition2;
                            radius = f7;
                            yValuePosition = valuePosition;
                            valueFormatter2 = valueFormatter;
                        }
                    } else {
                        mPPointF4 = mPPointF2;
                    }
                    if (pieEntry.getIcon() == null) {
                    }
                    mPPointF5 = mPPointF3;
                    i4++;
                    i5++;
                    mPPointF6 = mPPointF5;
                    mPPointF7 = mPPointF4;
                    iPieDataSet2 = iPieDataSet;
                    sliceSpace = f17;
                    entryCount = i6;
                    drawAngles = fArr3;
                    f15 = f12;
                    absoluteAngles = fArr4;
                    phaseX = f18;
                    phaseY = f19;
                    f14 = f20;
                    xValuePosition = valuePosition2;
                    radius = f7;
                    yValuePosition = valuePosition;
                    valueFormatter2 = valueFormatter;
                }
                f = radius;
                fArr = drawAngles;
                fArr2 = absoluteAngles;
                f2 = phaseX;
                f3 = phaseY;
                f4 = f14;
                f5 = f15;
                mPPointF = mPPointF7;
                canvas2 = canvas;
                MPPointF.recycleInstance(mPPointF6);
                i2 = i4;
            } else {
                i = i3;
                list = dataSets;
                f = radius;
                fArr = drawAngles;
                fArr2 = absoluteAngles;
                f2 = phaseX;
                f3 = phaseY;
                f4 = f14;
                f5 = f15;
                canvas2 = canvas4;
                mPPointF = centerCircleBox;
            }
            i3 = i + 1;
            canvas4 = canvas2;
            centerCircleBox = mPPointF;
            dataSets = list;
            drawAngles = fArr;
            f15 = f5;
            absoluteAngles = fArr2;
            phaseX = f2;
            phaseY = f3;
            f14 = f4;
            radius = f;
        }
        MPPointF.recycleInstance(centerCircleBox);
        canvas.restore();
    }

    @Override // com.github.mikephil.charting.renderer.DataRenderer
    public void drawValue(Canvas canvas, String str, float f, float f2, int i) {
        this.mValuePaint.setColor(i);
        canvas.drawText(str, f, f2, this.mValuePaint);
    }

    protected void drawEntryLabel(Canvas canvas, String str, float f, float f2) {
        canvas.drawText(str, f, f2, this.mEntryLabelsPaint);
    }

    @Override // com.github.mikephil.charting.renderer.DataRenderer
    public void drawExtras(Canvas canvas) {
        drawHole(canvas);
        canvas.drawBitmap(this.mDrawBitmap.get(), 0.0f, 0.0f, (Paint) null);
        drawCenterText(canvas);
    }

    protected void drawHole(Canvas canvas) {
        if (!this.mChart.isDrawHoleEnabled() || this.mBitmapCanvas == null) {
            return;
        }
        float radius = this.mChart.getRadius();
        float holeRadius = (this.mChart.getHoleRadius() / 100.0f) * radius;
        MPPointF centerCircleBox = this.mChart.getCenterCircleBox();
        if (Color.alpha(this.mHolePaint.getColor()) > 0) {
            this.mBitmapCanvas.drawCircle(centerCircleBox.x, centerCircleBox.y, holeRadius, this.mHolePaint);
        }
        if (Color.alpha(this.mTransparentCirclePaint.getColor()) > 0 && this.mChart.getTransparentCircleRadius() > this.mChart.getHoleRadius()) {
            int alpha = this.mTransparentCirclePaint.getAlpha();
            float transparentCircleRadius = radius * (this.mChart.getTransparentCircleRadius() / 100.0f);
            this.mTransparentCirclePaint.setAlpha((int) (alpha * this.mAnimator.getPhaseX() * this.mAnimator.getPhaseY()));
            this.mHoleCirclePath.reset();
            this.mHoleCirclePath.addCircle(centerCircleBox.x, centerCircleBox.y, transparentCircleRadius, Path.Direction.CW);
            this.mHoleCirclePath.addCircle(centerCircleBox.x, centerCircleBox.y, holeRadius, Path.Direction.CCW);
            this.mBitmapCanvas.drawPath(this.mHoleCirclePath, this.mTransparentCirclePaint);
            this.mTransparentCirclePaint.setAlpha(alpha);
        }
        MPPointF.recycleInstance(centerCircleBox);
    }

    protected void drawCenterText(Canvas canvas) {
        float radius;
        MPPointF mPPointF;
        CharSequence centerText = this.mChart.getCenterText();
        if (!this.mChart.isDrawCenterTextEnabled() || centerText == null) {
            return;
        }
        MPPointF centerCircleBox = this.mChart.getCenterCircleBox();
        MPPointF centerTextOffset = this.mChart.getCenterTextOffset();
        float f = centerCircleBox.x + centerTextOffset.x;
        float f2 = centerCircleBox.y + centerTextOffset.y;
        if (this.mChart.isDrawHoleEnabled() && !this.mChart.isDrawSlicesUnderHoleEnabled()) {
            radius = this.mChart.getRadius() * (this.mChart.getHoleRadius() / 100.0f);
        } else {
            radius = this.mChart.getRadius();
        }
        RectF rectF = this.mRectBuffer[0];
        rectF.left = f - radius;
        rectF.top = f2 - radius;
        rectF.right = f + radius;
        rectF.bottom = f2 + radius;
        RectF rectF2 = this.mRectBuffer[1];
        rectF2.set(rectF);
        float centerTextRadiusPercent = this.mChart.getCenterTextRadiusPercent() / 100.0f;
        if (centerTextRadiusPercent > Utils.DOUBLE_EPSILON) {
            rectF2.inset((rectF2.width() - (rectF2.width() * centerTextRadiusPercent)) / 2.0f, (rectF2.height() - (rectF2.height() * centerTextRadiusPercent)) / 2.0f);
        }
        if (centerText.equals(this.mCenterTextLastValue) && rectF2.equals(this.mCenterTextLastBounds)) {
            mPPointF = centerTextOffset;
        } else {
            this.mCenterTextLastBounds.set(rectF2);
            this.mCenterTextLastValue = centerText;
            mPPointF = centerTextOffset;
            this.mCenterTextLayout = new StaticLayout(centerText, 0, centerText.length(), this.mCenterTextPaint, (int) Math.max(Math.ceil(this.mCenterTextLastBounds.width()), 1.0d), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        }
        float height = this.mCenterTextLayout.getHeight();
        canvas.save();
        if (Build.VERSION.SDK_INT >= 18) {
            Path path = this.mDrawCenterTextPathBuffer;
            path.reset();
            path.addOval(rectF, Path.Direction.CW);
            canvas.clipPath(path);
        }
        canvas.translate(rectF2.left, rectF2.top + ((rectF2.height() - height) / 2.0f));
        this.mCenterTextLayout.draw(canvas);
        canvas.restore();
        MPPointF.recycleInstance(centerCircleBox);
        MPPointF.recycleInstance(mPPointF);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.github.mikephil.charting.renderer.DataRenderer
    public void drawHighlighted(Canvas canvas, Highlight[] highlightArr) {
        int i;
        RectF rectF;
        float f;
        float[] fArr;
        boolean z;
        float f2;
        float f3;
        MPPointF mPPointF;
        IPieDataSet dataSetByIndex;
        float f4;
        int i2;
        float[] fArr2;
        float f5;
        int i3;
        float f6;
        float f7;
        Highlight[] highlightArr2 = highlightArr;
        boolean z2 = this.mChart.isDrawHoleEnabled() && !this.mChart.isDrawSlicesUnderHoleEnabled();
        if (z2 && this.mChart.isDrawRoundedSlicesEnabled()) {
            return;
        }
        float phaseX = this.mAnimator.getPhaseX();
        float phaseY = this.mAnimator.getPhaseY();
        float rotationAngle = this.mChart.getRotationAngle();
        float[] drawAngles = this.mChart.getDrawAngles();
        float[] absoluteAngles = this.mChart.getAbsoluteAngles();
        MPPointF centerCircleBox = this.mChart.getCenterCircleBox();
        float radius = this.mChart.getRadius();
        float holeRadius = z2 ? (this.mChart.getHoleRadius() / 100.0f) * radius : 0.0f;
        RectF rectF2 = this.mDrawHighlightedRectF;
        rectF2.set(0.0f, 0.0f, 0.0f, 0.0f);
        int i4 = 0;
        while (i4 < highlightArr2.length) {
            int x = (int) highlightArr2[i4].getX();
            if (x < drawAngles.length && (dataSetByIndex = ((PieData) this.mChart.getData()).getDataSetByIndex(highlightArr2[i4].getDataSetIndex())) != null && dataSetByIndex.isHighlightEnabled()) {
                int entryCount = dataSetByIndex.getEntryCount();
                int i5 = 0;
                for (int i6 = 0; i6 < entryCount; i6++) {
                    if (Math.abs(dataSetByIndex.getEntryForIndex(i6).getY()) > Utils.FLOAT_EPSILON) {
                        i5++;
                    }
                }
                if (x == 0) {
                    i2 = 1;
                    f4 = 0.0f;
                } else {
                    f4 = absoluteAngles[x - 1] * phaseX;
                    i2 = 1;
                }
                float sliceSpace = i5 <= i2 ? 0.0f : dataSetByIndex.getSliceSpace();
                float f8 = drawAngles[x];
                float selectionShift = dataSetByIndex.getSelectionShift();
                int i7 = i4;
                float f9 = radius + selectionShift;
                float f10 = holeRadius;
                rectF2.set(this.mChart.getCircleBox());
                float f11 = -selectionShift;
                rectF2.inset(f11, f11);
                boolean z3 = sliceSpace > 0.0f && f8 <= 180.0f;
                this.mRenderPaint.setColor(dataSetByIndex.getColor(x));
                float f12 = i5 == 1 ? 0.0f : sliceSpace / (radius * 0.017453292f);
                float f13 = i5 == 1 ? 0.0f : sliceSpace / (f9 * 0.017453292f);
                float f14 = rotationAngle + (((f12 / 2.0f) + f4) * phaseY);
                float f15 = (f8 - f12) * phaseY;
                float f16 = f15 < 0.0f ? 0.0f : f15;
                float f17 = (((f13 / 2.0f) + f4) * phaseY) + rotationAngle;
                float f18 = (f8 - f13) * phaseY;
                if (f18 < 0.0f) {
                    f18 = 0.0f;
                }
                this.mPathBuffer.reset();
                if (f16 >= 360.0f && f16 % 360.0f <= Utils.FLOAT_EPSILON) {
                    this.mPathBuffer.addCircle(centerCircleBox.x, centerCircleBox.y, f9, Path.Direction.CW);
                    fArr2 = drawAngles;
                    f5 = f4;
                    i3 = i5;
                    z = z2;
                } else {
                    fArr2 = drawAngles;
                    f5 = f4;
                    double d = f17 * 0.017453292f;
                    i3 = i5;
                    z = z2;
                    this.mPathBuffer.moveTo(centerCircleBox.x + (((float) Math.cos(d)) * f9), centerCircleBox.y + (f9 * ((float) Math.sin(d))));
                    this.mPathBuffer.arcTo(rectF2, f17, f18);
                }
                if (z3) {
                    double d2 = f14 * 0.017453292f;
                    i = i7;
                    rectF = rectF2;
                    f = f10;
                    mPPointF = centerCircleBox;
                    fArr = fArr2;
                    f6 = calculateMinimumRadiusForSpacedSlice(centerCircleBox, radius, f8 * phaseY, (((float) Math.cos(d2)) * radius) + centerCircleBox.x, centerCircleBox.y + (((float) Math.sin(d2)) * radius), f14, f16);
                } else {
                    rectF = rectF2;
                    mPPointF = centerCircleBox;
                    i = i7;
                    f = f10;
                    fArr = fArr2;
                    f6 = 0.0f;
                }
                this.mInnerRectBuffer.set(mPPointF.x - f, mPPointF.y - f, mPPointF.x + f, mPPointF.y + f);
                if (!z || (f <= 0.0f && !z3)) {
                    f2 = phaseX;
                    f3 = phaseY;
                    if (f16 % 360.0f > Utils.FLOAT_EPSILON) {
                        if (z3) {
                            double d3 = (f14 + (f16 / 2.0f)) * 0.017453292f;
                            this.mPathBuffer.lineTo(mPPointF.x + (((float) Math.cos(d3)) * f6), mPPointF.y + (f6 * ((float) Math.sin(d3))));
                        } else {
                            this.mPathBuffer.lineTo(mPPointF.x, mPPointF.y);
                        }
                    }
                } else {
                    if (z3) {
                        if (f6 < 0.0f) {
                            f6 = -f6;
                        }
                        f7 = Math.max(f, f6);
                    } else {
                        f7 = f;
                    }
                    float f19 = (i3 == 1 || f7 == 0.0f) ? 0.0f : sliceSpace / (f7 * 0.017453292f);
                    float f20 = ((f5 + (f19 / 2.0f)) * phaseY) + rotationAngle;
                    float f21 = (f8 - f19) * phaseY;
                    if (f21 < 0.0f) {
                        f21 = 0.0f;
                    }
                    float f22 = f20 + f21;
                    if (f16 >= 360.0f && f16 % 360.0f <= Utils.FLOAT_EPSILON) {
                        this.mPathBuffer.addCircle(mPPointF.x, mPPointF.y, f7, Path.Direction.CCW);
                        f2 = phaseX;
                        f3 = phaseY;
                    } else {
                        double d4 = f22 * 0.017453292f;
                        f2 = phaseX;
                        f3 = phaseY;
                        this.mPathBuffer.lineTo(mPPointF.x + (((float) Math.cos(d4)) * f7), mPPointF.y + (f7 * ((float) Math.sin(d4))));
                        this.mPathBuffer.arcTo(this.mInnerRectBuffer, f22, -f21);
                    }
                }
                this.mPathBuffer.close();
                this.mBitmapCanvas.drawPath(this.mPathBuffer, this.mRenderPaint);
            } else {
                i = i4;
                rectF = rectF2;
                f = holeRadius;
                fArr = drawAngles;
                z = z2;
                f2 = phaseX;
                f3 = phaseY;
                mPPointF = centerCircleBox;
            }
            i4 = i + 1;
            phaseX = f2;
            rectF2 = rectF;
            holeRadius = f;
            centerCircleBox = mPPointF;
            phaseY = f3;
            drawAngles = fArr;
            z2 = z;
            highlightArr2 = highlightArr;
        }
        MPPointF.recycleInstance(centerCircleBox);
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void drawRoundedSlices(Canvas canvas) {
        float f;
        float[] fArr;
        float f2;
        if (this.mChart.isDrawRoundedSlicesEnabled()) {
            IPieDataSet dataSet = ((PieData) this.mChart.getData()).getDataSet();
            if (dataSet.isVisible()) {
                float phaseX = this.mAnimator.getPhaseX();
                float phaseY = this.mAnimator.getPhaseY();
                MPPointF centerCircleBox = this.mChart.getCenterCircleBox();
                float radius = this.mChart.getRadius();
                float holeRadius = (radius - ((this.mChart.getHoleRadius() * radius) / 100.0f)) / 2.0f;
                float[] drawAngles = this.mChart.getDrawAngles();
                float rotationAngle = this.mChart.getRotationAngle();
                int i = 0;
                while (i < dataSet.getEntryCount()) {
                    float f3 = drawAngles[i];
                    if (Math.abs(dataSet.getEntryForIndex(i).getY()) > Utils.FLOAT_EPSILON) {
                        double d = radius - holeRadius;
                        double d2 = (rotationAngle + f3) * phaseY;
                        f = phaseY;
                        fArr = drawAngles;
                        f2 = rotationAngle;
                        float cos = (float) (centerCircleBox.x + (Math.cos(Math.toRadians(d2)) * d));
                        float sin = (float) ((d * Math.sin(Math.toRadians(d2))) + centerCircleBox.y);
                        this.mRenderPaint.setColor(dataSet.getColor(i));
                        this.mBitmapCanvas.drawCircle(cos, sin, holeRadius, this.mRenderPaint);
                    } else {
                        f = phaseY;
                        fArr = drawAngles;
                        f2 = rotationAngle;
                    }
                    rotationAngle = f2 + (f3 * phaseX);
                    i++;
                    phaseY = f;
                    drawAngles = fArr;
                }
                MPPointF.recycleInstance(centerCircleBox);
            }
        }
    }

    public void releaseBitmap() {
        Canvas canvas = this.mBitmapCanvas;
        if (canvas != null) {
            canvas.setBitmap(null);
            this.mBitmapCanvas = null;
        }
        WeakReference<Bitmap> weakReference = this.mDrawBitmap;
        if (weakReference != null) {
            Bitmap bitmap = weakReference.get();
            if (bitmap != null) {
                bitmap.recycle();
            }
            this.mDrawBitmap.clear();
            this.mDrawBitmap = null;
        }
    }
}

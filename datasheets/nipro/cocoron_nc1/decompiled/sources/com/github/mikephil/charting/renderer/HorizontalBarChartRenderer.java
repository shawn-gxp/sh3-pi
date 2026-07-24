package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.buffer.HorizontalBarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.util.List;

/* loaded from: classes.dex */
public class HorizontalBarChartRenderer extends BarChartRenderer {
    private RectF mBarShadowRectBuffer;

    public HorizontalBarChartRenderer(BarDataProvider barDataProvider, ChartAnimator chartAnimator, ViewPortHandler viewPortHandler) {
        super(barDataProvider, chartAnimator, viewPortHandler);
        this.mBarShadowRectBuffer = new RectF();
        this.mValuePaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override // com.github.mikephil.charting.renderer.BarChartRenderer, com.github.mikephil.charting.renderer.DataRenderer
    public void initBuffers() {
        BarData barData = this.mChart.getBarData();
        this.mBarBuffers = new HorizontalBarBuffer[barData.getDataSetCount()];
        for (int i = 0; i < this.mBarBuffers.length; i++) {
            IBarDataSet iBarDataSet = (IBarDataSet) barData.getDataSetByIndex(i);
            this.mBarBuffers[i] = new HorizontalBarBuffer(iBarDataSet.getEntryCount() * 4 * (iBarDataSet.isStacked() ? iBarDataSet.getStackSize() : 1), barData.getDataSetCount(), iBarDataSet.isStacked());
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.github.mikephil.charting.renderer.BarChartRenderer
    protected void drawDataSet(Canvas canvas, IBarDataSet iBarDataSet, int i) {
        Transformer transformer = this.mChart.getTransformer(iBarDataSet.getAxisDependency());
        this.mBarBorderPaint.setColor(iBarDataSet.getBarBorderColor());
        this.mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(iBarDataSet.getBarBorderWidth()));
        boolean z = iBarDataSet.getBarBorderWidth() > 0.0f;
        float phaseX = this.mAnimator.getPhaseX();
        float phaseY = this.mAnimator.getPhaseY();
        if (this.mChart.isDrawBarShadowEnabled()) {
            this.mShadowPaint.setColor(iBarDataSet.getBarShadowColor());
            float barWidth = this.mChart.getBarData().getBarWidth() / 2.0f;
            int min = Math.min((int) Math.ceil(iBarDataSet.getEntryCount() * phaseX), iBarDataSet.getEntryCount());
            for (int i2 = 0; i2 < min; i2++) {
                float x = ((BarEntry) iBarDataSet.getEntryForIndex(i2)).getX();
                this.mBarShadowRectBuffer.top = x - barWidth;
                this.mBarShadowRectBuffer.bottom = x + barWidth;
                transformer.rectValueToPixel(this.mBarShadowRectBuffer);
                if (this.mViewPortHandler.isInBoundsTop(this.mBarShadowRectBuffer.bottom)) {
                    if (!this.mViewPortHandler.isInBoundsBottom(this.mBarShadowRectBuffer.top)) {
                        break;
                    }
                    this.mBarShadowRectBuffer.left = this.mViewPortHandler.contentLeft();
                    this.mBarShadowRectBuffer.right = this.mViewPortHandler.contentRight();
                    canvas.drawRect(this.mBarShadowRectBuffer, this.mShadowPaint);
                }
            }
        }
        BarBuffer barBuffer = this.mBarBuffers[i];
        barBuffer.setPhases(phaseX, phaseY);
        barBuffer.setDataSet(i);
        barBuffer.setInverted(this.mChart.isInverted(iBarDataSet.getAxisDependency()));
        barBuffer.setBarWidth(this.mChart.getBarData().getBarWidth());
        barBuffer.feed(iBarDataSet);
        transformer.pointValuesToPixel(barBuffer.buffer);
        boolean z2 = iBarDataSet.getColors().size() == 1;
        if (z2) {
            this.mRenderPaint.setColor(iBarDataSet.getColor());
        }
        for (int i3 = 0; i3 < barBuffer.size(); i3 += 4) {
            int i4 = i3 + 3;
            if (!this.mViewPortHandler.isInBoundsTop(barBuffer.buffer[i4])) {
                return;
            }
            int i5 = i3 + 1;
            if (this.mViewPortHandler.isInBoundsBottom(barBuffer.buffer[i5])) {
                if (!z2) {
                    this.mRenderPaint.setColor(iBarDataSet.getColor(i3 / 4));
                }
                int i6 = i3 + 2;
                canvas.drawRect(barBuffer.buffer[i3], barBuffer.buffer[i5], barBuffer.buffer[i6], barBuffer.buffer[i4], this.mRenderPaint);
                if (z) {
                    canvas.drawRect(barBuffer.buffer[i3], barBuffer.buffer[i5], barBuffer.buffer[i6], barBuffer.buffer[i4], this.mBarBorderPaint);
                }
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.github.mikephil.charting.renderer.BarChartRenderer, com.github.mikephil.charting.renderer.DataRenderer
    public void drawValues(Canvas canvas) {
        List list;
        int i;
        MPPointF mPPointF;
        int i2;
        float[] fArr;
        float f;
        int i3;
        float[] fArr2;
        float f2;
        float f3;
        BarEntry barEntry;
        int i4;
        List list2;
        float f4;
        MPPointF mPPointF2;
        ValueFormatter valueFormatter;
        int i5;
        BarBuffer barBuffer;
        if (isDrawingValuesAllowed(this.mChart)) {
            List dataSets = this.mChart.getBarData().getDataSets();
            float convertDpToPixel = Utils.convertDpToPixel(5.0f);
            boolean isDrawValueAboveBarEnabled = this.mChart.isDrawValueAboveBarEnabled();
            int i6 = 0;
            while (i6 < this.mChart.getBarData().getDataSetCount()) {
                IBarDataSet iBarDataSet = (IBarDataSet) dataSets.get(i6);
                if (shouldDrawValues(iBarDataSet)) {
                    boolean isInverted = this.mChart.isInverted(iBarDataSet.getAxisDependency());
                    applyValueTextStyle(iBarDataSet);
                    float f5 = 2.0f;
                    float calcTextHeight = Utils.calcTextHeight(this.mValuePaint, "10") / 2.0f;
                    ValueFormatter valueFormatter2 = iBarDataSet.getValueFormatter();
                    BarBuffer barBuffer2 = this.mBarBuffers[i6];
                    float phaseY = this.mAnimator.getPhaseY();
                    MPPointF mPPointF3 = MPPointF.getInstance(iBarDataSet.getIconsOffset());
                    mPPointF3.x = Utils.convertDpToPixel(mPPointF3.x);
                    mPPointF3.y = Utils.convertDpToPixel(mPPointF3.y);
                    if (iBarDataSet.isStacked()) {
                        list = dataSets;
                        i = i6;
                        mPPointF = mPPointF3;
                        Transformer transformer = this.mChart.getTransformer(iBarDataSet.getAxisDependency());
                        int i7 = 0;
                        int i8 = 0;
                        while (i7 < iBarDataSet.getEntryCount() * this.mAnimator.getPhaseX()) {
                            BarEntry barEntry2 = (BarEntry) iBarDataSet.getEntryForIndex(i7);
                            int valueTextColor = iBarDataSet.getValueTextColor(i7);
                            float[] yVals = barEntry2.getYVals();
                            if (yVals == null) {
                                int i9 = i8 + 1;
                                if (!this.mViewPortHandler.isInBoundsTop(barBuffer2.buffer[i9])) {
                                    break;
                                }
                                if (this.mViewPortHandler.isInBoundsX(barBuffer2.buffer[i8]) && this.mViewPortHandler.isInBoundsBottom(barBuffer2.buffer[i9])) {
                                    String barLabel = valueFormatter2.getBarLabel(barEntry2);
                                    float calcTextWidth = Utils.calcTextWidth(this.mValuePaint, barLabel);
                                    float f6 = isDrawValueAboveBarEnabled ? convertDpToPixel : -(calcTextWidth + convertDpToPixel);
                                    float f7 = isDrawValueAboveBarEnabled ? -(calcTextWidth + convertDpToPixel) : convertDpToPixel;
                                    if (isInverted) {
                                        f6 = (-f6) - calcTextWidth;
                                        f7 = (-f7) - calcTextWidth;
                                    }
                                    float f8 = f6;
                                    float f9 = f7;
                                    if (iBarDataSet.isDrawValuesEnabled()) {
                                        i2 = i7;
                                        fArr = yVals;
                                        barEntry = barEntry2;
                                        drawValue(canvas, barLabel, barBuffer2.buffer[i8 + 2] + (barEntry2.getY() >= 0.0f ? f8 : f9), barBuffer2.buffer[i9] + calcTextHeight, valueTextColor);
                                    } else {
                                        barEntry = barEntry2;
                                        i2 = i7;
                                        fArr = yVals;
                                    }
                                    if (barEntry.getIcon() != null && iBarDataSet.isDrawIconsEnabled()) {
                                        Drawable icon = barEntry.getIcon();
                                        float f10 = barBuffer2.buffer[i8 + 2];
                                        if (barEntry.getY() < 0.0f) {
                                            f8 = f9;
                                        }
                                        Utils.drawImage(canvas, icon, (int) (f10 + f8 + mPPointF.x), (int) (barBuffer2.buffer[i9] + mPPointF.y), icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                                    }
                                }
                            } else {
                                i2 = i7;
                                fArr = yVals;
                                int length = fArr.length * 2;
                                float[] fArr3 = new float[length];
                                float f11 = -barEntry2.getNegativeSum();
                                float f12 = 0.0f;
                                int i10 = 0;
                                int i11 = 0;
                                while (i10 < length) {
                                    float f13 = fArr[i11];
                                    if (f13 == 0.0f && (f12 == 0.0f || f11 == 0.0f)) {
                                        float f14 = f11;
                                        f11 = f13;
                                        f3 = f14;
                                    } else if (f13 >= 0.0f) {
                                        f12 += f13;
                                        f3 = f11;
                                        f11 = f12;
                                    } else {
                                        f3 = f11 - f13;
                                    }
                                    fArr3[i10] = f11 * phaseY;
                                    i10 += 2;
                                    i11++;
                                    f11 = f3;
                                }
                                transformer.pointValuesToPixel(fArr3);
                                int i12 = 0;
                                while (i12 < length) {
                                    float f15 = fArr[i12 / 2];
                                    String barStackedLabel = valueFormatter2.getBarStackedLabel(f15, barEntry2);
                                    float calcTextWidth2 = Utils.calcTextWidth(this.mValuePaint, barStackedLabel);
                                    float f16 = isDrawValueAboveBarEnabled ? convertDpToPixel : -(calcTextWidth2 + convertDpToPixel);
                                    int i13 = length;
                                    float f17 = isDrawValueAboveBarEnabled ? -(calcTextWidth2 + convertDpToPixel) : convertDpToPixel;
                                    if (isInverted) {
                                        f16 = (-f16) - calcTextWidth2;
                                        f17 = (-f17) - calcTextWidth2;
                                    }
                                    boolean z = (f15 == 0.0f && f11 == 0.0f && f12 > 0.0f) || f15 < 0.0f;
                                    float f18 = fArr3[i12];
                                    if (z) {
                                        f16 = f17;
                                    }
                                    float f19 = f18 + f16;
                                    float f20 = (barBuffer2.buffer[i8 + 1] + barBuffer2.buffer[i8 + 3]) / 2.0f;
                                    if (!this.mViewPortHandler.isInBoundsTop(f20)) {
                                        break;
                                    }
                                    if (this.mViewPortHandler.isInBoundsX(f19) && this.mViewPortHandler.isInBoundsBottom(f20)) {
                                        if (iBarDataSet.isDrawValuesEnabled()) {
                                            f = f20;
                                            i3 = i12;
                                            fArr2 = fArr3;
                                            f2 = f19;
                                            drawValue(canvas, barStackedLabel, f19, f20 + calcTextHeight, valueTextColor);
                                        } else {
                                            f = f20;
                                            i3 = i12;
                                            fArr2 = fArr3;
                                            f2 = f19;
                                        }
                                        if (barEntry2.getIcon() != null && iBarDataSet.isDrawIconsEnabled()) {
                                            Drawable icon2 = barEntry2.getIcon();
                                            Utils.drawImage(canvas, icon2, (int) (f2 + mPPointF.x), (int) (f + mPPointF.y), icon2.getIntrinsicWidth(), icon2.getIntrinsicHeight());
                                        }
                                    } else {
                                        i3 = i12;
                                        fArr2 = fArr3;
                                    }
                                    i12 = i3 + 2;
                                    length = i13;
                                    fArr3 = fArr2;
                                }
                            }
                            i8 = fArr == null ? i8 + 4 : i8 + (fArr.length * 4);
                            i7 = i2 + 1;
                        }
                    } else {
                        int i14 = 0;
                        while (i14 < barBuffer2.buffer.length * this.mAnimator.getPhaseX()) {
                            int i15 = i14 + 1;
                            float f21 = (barBuffer2.buffer[i15] + barBuffer2.buffer[i14 + 3]) / f5;
                            if (!this.mViewPortHandler.isInBoundsTop(barBuffer2.buffer[i15])) {
                                break;
                            }
                            if (this.mViewPortHandler.isInBoundsX(barBuffer2.buffer[i14]) && this.mViewPortHandler.isInBoundsBottom(barBuffer2.buffer[i15])) {
                                BarEntry barEntry3 = (BarEntry) iBarDataSet.getEntryForIndex(i14 / 4);
                                float y = barEntry3.getY();
                                String barLabel2 = valueFormatter2.getBarLabel(barEntry3);
                                float calcTextWidth3 = Utils.calcTextWidth(this.mValuePaint, barLabel2);
                                float f22 = isDrawValueAboveBarEnabled ? convertDpToPixel : -(calcTextWidth3 + convertDpToPixel);
                                MPPointF mPPointF4 = mPPointF3;
                                float f23 = isDrawValueAboveBarEnabled ? -(calcTextWidth3 + convertDpToPixel) : convertDpToPixel;
                                if (isInverted) {
                                    f22 = (-f22) - calcTextWidth3;
                                    f23 = (-f23) - calcTextWidth3;
                                }
                                float f24 = f22;
                                float f25 = f23;
                                if (iBarDataSet.isDrawValuesEnabled()) {
                                    i4 = i14;
                                    list2 = dataSets;
                                    mPPointF2 = mPPointF4;
                                    i5 = i6;
                                    barBuffer = barBuffer2;
                                    f4 = calcTextHeight;
                                    valueFormatter = valueFormatter2;
                                    drawValue(canvas, barLabel2, barBuffer2.buffer[i14 + 2] + (y >= 0.0f ? f24 : f25), f21 + calcTextHeight, iBarDataSet.getValueTextColor(i14 / 2));
                                } else {
                                    i4 = i14;
                                    list2 = dataSets;
                                    f4 = calcTextHeight;
                                    mPPointF2 = mPPointF4;
                                    valueFormatter = valueFormatter2;
                                    i5 = i6;
                                    barBuffer = barBuffer2;
                                }
                                if (barEntry3.getIcon() != null && iBarDataSet.isDrawIconsEnabled()) {
                                    Drawable icon3 = barEntry3.getIcon();
                                    float f26 = barBuffer.buffer[i4 + 2];
                                    if (y < 0.0f) {
                                        f24 = f25;
                                    }
                                    Utils.drawImage(canvas, icon3, (int) (f26 + f24 + mPPointF2.x), (int) (f21 + mPPointF2.y), icon3.getIntrinsicWidth(), icon3.getIntrinsicHeight());
                                }
                            } else {
                                i4 = i14;
                                list2 = dataSets;
                                i5 = i6;
                                f4 = calcTextHeight;
                                mPPointF2 = mPPointF3;
                                barBuffer = barBuffer2;
                                valueFormatter = valueFormatter2;
                            }
                            i14 = i4 + 4;
                            mPPointF3 = mPPointF2;
                            barBuffer2 = barBuffer;
                            valueFormatter2 = valueFormatter;
                            dataSets = list2;
                            i6 = i5;
                            calcTextHeight = f4;
                            f5 = 2.0f;
                        }
                        list = dataSets;
                        i = i6;
                        mPPointF = mPPointF3;
                    }
                    MPPointF.recycleInstance(mPPointF);
                } else {
                    list = dataSets;
                    i = i6;
                }
                i6 = i + 1;
                dataSets = list;
            }
        }
    }

    @Override // com.github.mikephil.charting.renderer.BarChartRenderer, com.github.mikephil.charting.renderer.DataRenderer
    public void drawValue(Canvas canvas, String str, float f, float f2, int i) {
        this.mValuePaint.setColor(i);
        canvas.drawText(str, f, f2, this.mValuePaint);
    }

    @Override // com.github.mikephil.charting.renderer.BarChartRenderer
    protected void prepareBarHighlight(float f, float f2, float f3, float f4, Transformer transformer) {
        this.mBarRect.set(f2, f - f4, f3, f + f4);
        transformer.rectToPixelPhaseHorizontal(this.mBarRect, this.mAnimator.getPhaseY());
    }

    @Override // com.github.mikephil.charting.renderer.BarChartRenderer
    protected void setHighlightDrawPos(Highlight highlight, RectF rectF) {
        highlight.setDraw(rectF.centerY(), rectF.right);
    }

    @Override // com.github.mikephil.charting.renderer.DataRenderer
    protected boolean isDrawingValuesAllowed(ChartInterface chartInterface) {
        return ((float) chartInterface.getData().getEntryCount()) < ((float) chartInterface.getMaxVisibleCount()) * this.mViewPortHandler.getScaleY();
    }
}

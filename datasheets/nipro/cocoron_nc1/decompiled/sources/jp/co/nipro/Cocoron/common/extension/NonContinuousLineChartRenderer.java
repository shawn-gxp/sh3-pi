package jp.co.nipro.cocoron.common.extension;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PathEffect;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NonContinuousLineChartRenderer.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0014\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B#\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007¢\u0006\u0002\u0010\bJ\u001a\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0014R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u0011"}, d2 = {"Ljp/co/nipro/cocoron/common/extension/NonContinuousLineChartRenderer;", "Lcom/github/mikephil/charting/renderer/LineChartRenderer;", "chart", "Lcom/github/mikephil/charting/interfaces/dataprovider/LineDataProvider;", "animator", "Lcom/github/mikephil/charting/animation/ChartAnimator;", "viewPortHandler", "Lcom/github/mikephil/charting/utils/ViewPortHandler;", "(Lcom/github/mikephil/charting/interfaces/dataprovider/LineDataProvider;Lcom/github/mikephil/charting/animation/ChartAnimator;Lcom/github/mikephil/charting/utils/ViewPortHandler;)V", "mLineBuffer", "", "drawLinear", "", "c", "Landroid/graphics/Canvas;", "dataSet", "Lcom/github/mikephil/charting/interfaces/datasets/ILineDataSet;", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class NonContinuousLineChartRenderer extends LineChartRenderer {
    private float[] mLineBuffer;

    public NonContinuousLineChartRenderer(LineDataProvider lineDataProvider, ChartAnimator chartAnimator, ViewPortHandler viewPortHandler) {
        super(lineDataProvider, chartAnimator, viewPortHandler);
        this.mLineBuffer = new float[4];
    }

    /* JADX WARN: Type inference failed for: r11v8, types: [com.github.mikephil.charting.data.Entry] */
    /* JADX WARN: Type inference failed for: r14v16, types: [com.github.mikephil.charting.data.Entry] */
    /* JADX WARN: Type inference failed for: r14v25, types: [com.github.mikephil.charting.data.Entry, java.lang.Object] */
    /* JADX WARN: Type inference failed for: r5v7, types: [com.github.mikephil.charting.data.Entry] */
    @Override // com.github.mikephil.charting.renderer.LineChartRenderer
    protected void drawLinear(Canvas c, ILineDataSet dataSet) {
        int i;
        char c2;
        Intrinsics.checkNotNullParameter(dataSet, "dataSet");
        int entryCount = dataSet.getEntryCount();
        char c3 = 0;
        boolean z = dataSet.getMode() == LineDataSet.Mode.STEPPED;
        char c4 = 4;
        int i2 = z ? 4 : 2;
        Transformer transformer = this.mChart.getTransformer(dataSet.getAxisDependency());
        ChartAnimator mAnimator = this.mAnimator;
        Intrinsics.checkNotNullExpressionValue(mAnimator, "mAnimator");
        float phaseY = mAnimator.getPhaseY();
        Paint mRenderPaint = this.mRenderPaint;
        Intrinsics.checkNotNullExpressionValue(mRenderPaint, "mRenderPaint");
        mRenderPaint.setStyle(Paint.Style.STROKE);
        Canvas canvas = dataSet.isDashedLineEnabled() ? this.mBitmapCanvas : c;
        this.mXBounds.set(this.mChart, dataSet);
        if (dataSet.isDrawFilledEnabled() && entryCount > 0) {
            drawLinearFill(c, dataSet, transformer, this.mXBounds);
        }
        if (dataSet.getColors().size() > 1) {
            int i3 = i2 * 2;
            if (this.mLineBuffer.length <= i3) {
                this.mLineBuffer = new float[i2 * 4];
            }
            int i4 = this.mXBounds.min;
            int i5 = this.mXBounds.range + this.mXBounds.min;
            if (i4 <= i5) {
                while (true) {
                    ?? entryForIndex = dataSet.getEntryForIndex(i4);
                    if (entryForIndex != 0) {
                        this.mLineBuffer[c3] = entryForIndex.getX();
                        this.mLineBuffer[1] = entryForIndex.getY() * phaseY;
                        if (i4 < this.mXBounds.max) {
                            ?? entryForIndex2 = dataSet.getEntryForIndex(i4 + 1);
                            Intrinsics.checkNotNullExpressionValue(entryForIndex2, "dataSet.getEntryForIndex(j + 1)");
                            if (entryForIndex2 == 0) {
                                break;
                            }
                            if (z) {
                                this.mLineBuffer[2] = entryForIndex2.getX();
                                float[] fArr = this.mLineBuffer;
                                fArr[3] = fArr[1];
                                fArr[c4] = fArr[2];
                                fArr[5] = fArr[3];
                                fArr[6] = entryForIndex2.getX();
                                this.mLineBuffer[7] = entryForIndex2.getY() * phaseY;
                            } else {
                                this.mLineBuffer[2] = entryForIndex2.getX();
                                this.mLineBuffer[3] = entryForIndex2.getY() * phaseY;
                            }
                            c2 = 0;
                        } else {
                            float[] fArr2 = this.mLineBuffer;
                            c2 = 0;
                            fArr2[2] = fArr2[0];
                            fArr2[3] = fArr2[1];
                        }
                        transformer.pointValuesToPixel(this.mLineBuffer);
                        if (!this.mViewPortHandler.isInBoundsRight(this.mLineBuffer[c2])) {
                            break;
                        }
                        if (this.mViewPortHandler.isInBoundsLeft(this.mLineBuffer[2]) && (this.mViewPortHandler.isInBoundsTop(this.mLineBuffer[1]) || this.mViewPortHandler.isInBoundsBottom(this.mLineBuffer[3]))) {
                            Paint mRenderPaint2 = this.mRenderPaint;
                            Intrinsics.checkNotNullExpressionValue(mRenderPaint2, "mRenderPaint");
                            mRenderPaint2.setColor(dataSet.getColor(i4));
                            Intrinsics.checkNotNull(canvas);
                            canvas.drawLines(this.mLineBuffer, 0, i3, this.mRenderPaint);
                        }
                    }
                    if (i4 == i5) {
                        break;
                    }
                    i4++;
                    c4 = 4;
                    c3 = 0;
                }
            }
        } else {
            int i6 = entryCount * i2;
            if (this.mLineBuffer.length < Math.max(i6, i2) * 2) {
                this.mLineBuffer = new float[Math.max(i6, i2) * 4];
            }
            if (dataSet.getEntryForIndex(this.mXBounds.min) != 0) {
                int i7 = this.mXBounds.min;
                int i8 = this.mXBounds.range + this.mXBounds.min;
                int i9 = 0;
                if (i7 <= i8) {
                    while (true) {
                        ?? entryForIndex3 = dataSet.getEntryForIndex(i7 == 0 ? 0 : i7 - 1);
                        ?? entryForIndex4 = dataSet.getEntryForIndex(i7);
                        if (entryForIndex3 != 0 && entryForIndex4 != 0) {
                            if (!ExtensionKt.getVisible(entryForIndex3) || !ExtensionKt.getVisible(entryForIndex4)) {
                                int i10 = i9 + 1;
                                this.mLineBuffer[i9] = entryForIndex3.getX();
                                int i11 = i10 + 1;
                                this.mLineBuffer[i10] = (entryForIndex3.getY() * phaseY) - 100000.0f;
                                int i12 = i11 + 1;
                                this.mLineBuffer[i11] = entryForIndex4.getX();
                                i = i12 + 1;
                                this.mLineBuffer[i12] = (entryForIndex4.getY() * phaseY) - 100000.0f;
                            } else {
                                int i13 = i9 + 1;
                                this.mLineBuffer[i9] = entryForIndex3.getX();
                                int i14 = i13 + 1;
                                this.mLineBuffer[i13] = entryForIndex3.getY() * phaseY;
                                if (z) {
                                    int i15 = i14 + 1;
                                    this.mLineBuffer[i14] = entryForIndex4.getX();
                                    int i16 = i15 + 1;
                                    this.mLineBuffer[i15] = entryForIndex3.getY() * phaseY;
                                    int i17 = i16 + 1;
                                    this.mLineBuffer[i16] = entryForIndex4.getX();
                                    i14 = i17 + 1;
                                    this.mLineBuffer[i17] = entryForIndex3.getY() * phaseY;
                                }
                                int i18 = i14 + 1;
                                this.mLineBuffer[i14] = entryForIndex4.getX();
                                i = i18 + 1;
                                this.mLineBuffer[i18] = entryForIndex4.getY() * phaseY;
                            }
                            i9 = i;
                        }
                        if (i7 == i8) {
                            break;
                        } else {
                            i7++;
                        }
                    }
                }
                if (i9 > 0) {
                    transformer.pointValuesToPixel(this.mLineBuffer);
                    int max = Math.max((this.mXBounds.range + 1) * i2, i2) * 2;
                    Paint mRenderPaint3 = this.mRenderPaint;
                    Intrinsics.checkNotNullExpressionValue(mRenderPaint3, "mRenderPaint");
                    mRenderPaint3.setColor(dataSet.getColor());
                    Intrinsics.checkNotNull(canvas);
                    canvas.drawLines(this.mLineBuffer, 0, max, this.mRenderPaint);
                }
            }
        }
        Paint mRenderPaint4 = this.mRenderPaint;
        Intrinsics.checkNotNullExpressionValue(mRenderPaint4, "mRenderPaint");
        mRenderPaint4.setPathEffect((PathEffect) null);
    }
}

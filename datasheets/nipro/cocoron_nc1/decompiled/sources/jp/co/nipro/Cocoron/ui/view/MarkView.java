package jp.co.nipro.cocoron.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import jp.co.nipro.Cocoron.C0009R;
import jp.co.nipro.cocoron.common.Config;
import jp.co.nipro.cocoron.ui.view.FlickListener;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MarkView.kt */
@InverseBindingMethods({@InverseBindingMethod(attribute = "barEntry", type = MarkView.class)})
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0011\b\u0007\u0018\u0000 _2\u00020\u0001:\u0002_`B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004B\u0019\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006¢\u0006\u0002\u0010\u0007B!\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\u0006\u0010\b\u001a\u00020\t¢\u0006\u0002\u0010\nJ\b\u0010K\u001a\u0004\u0018\u00010\u0012J\u0006\u0010L\u001a\u00020MJ0\u0010N\u001a\u00020M2\u0006\u0010O\u001a\u00020P2\u0006\u0010Q\u001a\u00020\t2\u0006\u0010R\u001a\u00020\t2\u0006\u0010S\u001a\u00020\t2\u0006\u0010T\u001a\u00020\tH\u0014J\u0018\u0010U\u001a\u00020M2\u0006\u0010V\u001a\u00020\t2\u0006\u0010W\u001a\u00020\tH\u0014J\u0010\u0010X\u001a\u00020M2\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012J\u000e\u0010Y\u001a\u00020M2\u0006\u0010Z\u001a\u00020\fJ\u000e\u0010[\u001a\u00020M2\u0006\u0010\\\u001a\u00020\u0018J\u000e\u0010]\u001a\u00020M2\u0006\u0010\\\u001a\u00020=J\u000e\u0010^\u001a\u00020M2\u0006\u0010\\\u001a\u00020=R\u001a\u0010\u000b\u001a\u00020\fX\u0086.¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001c\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001c\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u001a\u0010\u001d\u001a\u00020\u001eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u001a\u0010#\u001a\u00020$X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b%\u0010&\"\u0004\b'\u0010(R\u001c\u0010)\u001a\u0004\u0018\u00010*X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b+\u0010,\"\u0004\b-\u0010.R\u001a\u0010/\u001a\u00020\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b/\u00100\"\u0004\b1\u00102R\u001a\u00103\u001a\u000204X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b5\u00106\"\u0004\b7\u00108R\u001a\u00109\u001a\u000204X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b:\u00106\"\u0004\b;\u00108R\u001c\u0010<\u001a\u0004\u0018\u00010=X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b>\u0010?\"\u0004\b@\u0010AR\u001c\u0010B\u001a\u0004\u0018\u00010=X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bC\u0010?\"\u0004\bD\u0010AR\u001a\u0010E\u001a\u00020FX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bG\u0010H\"\u0004\bI\u0010J¨\u0006a"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/MarkView;", "Landroid/view/ViewGroup;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "_chartView", "Lcom/github/mikephil/charting/charts/CombinedChart;", "get_chartView", "()Lcom/github/mikephil/charting/charts/CombinedChart;", "set_chartView", "(Lcom/github/mikephil/charting/charts/CombinedChart;)V", "entry", "Lcom/github/mikephil/charting/data/BarEntry;", "getEntry", "()Lcom/github/mikephil/charting/data/BarEntry;", "setEntry", "(Lcom/github/mikephil/charting/data/BarEntry;)V", "flickListener", "Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;", "getFlickListener", "()Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;", "setFlickListener", "(Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;)V", "handleView", "Landroid/widget/ImageView;", "getHandleView", "()Landroid/widget/ImageView;", "setHandleView", "(Landroid/widget/ImageView;)V", "holderView", "Landroid/view/View;", "getHolderView", "()Landroid/view/View;", "setHolderView", "(Landroid/view/View;)V", "inverseBindingListener", "Landroidx/databinding/InverseBindingListener;", "getInverseBindingListener", "()Landroidx/databinding/InverseBindingListener;", "setInverseBindingListener", "(Landroidx/databinding/InverseBindingListener;)V", "isDrag", "()I", "setDrag", "(I)V", "max", "Landroid/widget/TextView;", "getMax", "()Landroid/widget/TextView;", "setMax", "(Landroid/widget/TextView;)V", "min", "getMin", "setMin", "selectedistener", "Ljp/co/nipro/cocoron/ui/view/MarkView$MarkViewListener;", "getSelectedistener", "()Ljp/co/nipro/cocoron/ui/view/MarkView$MarkViewListener;", "setSelectedistener", "(Ljp/co/nipro/cocoron/ui/view/MarkView$MarkViewListener;)V", "selectingListener", "getSelectingListener", "setSelectingListener", "startTouchX", "", "getStartTouchX", "()F", "setStartTouchX", "(F)V", "getBarEntry", "layoutSubView", "", "onLayout", "changed", "", "l", "t", "r", "b", "onMeasure", "widthMeasureSpec", "heightMeasureSpec", "setBarEntry", "setChartView", "chartView", "setOnFlick", "listener", "setOnSelected", "setOnSelecting", "Companion", "MarkViewListener", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class MarkView extends ViewGroup {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    public CombinedChart _chartView;
    private BarEntry entry;
    private FlickListener.Listener flickListener;
    private ImageView handleView;
    private View holderView;
    private InverseBindingListener inverseBindingListener;
    private int isDrag;
    private TextView max;
    private TextView min;
    private MarkViewListener selectedistener;
    private MarkViewListener selectingListener;
    private float startTouchX;

    /* compiled from: MarkView.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&¨\u0006\b"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/MarkView$MarkViewListener;", "", "onChange", "", "view", "Ljp/co/nipro/cocoron/ui/view/MarkView;", "entry", "Lcom/github/mikephil/charting/data/BarEntry;", "app_release"}, k = 1, mv = {1, 4, 2})
    public interface MarkViewListener {
        void onChange(MarkView view, BarEntry entry);
    }

    @BindingAdapter({"app:barEntryAttrChanged"})
    @JvmStatic
    public static final void setListeners(MarkView markView, InverseBindingListener inverseBindingListener) {
        INSTANCE.setListeners(markView, inverseBindingListener);
    }

    public final View getHolderView() {
        return this.holderView;
    }

    public final void setHolderView(View view) {
        Intrinsics.checkNotNullParameter(view, "<set-?>");
        this.holderView = view;
    }

    public final ImageView getHandleView() {
        return this.handleView;
    }

    public final void setHandleView(ImageView imageView) {
        Intrinsics.checkNotNullParameter(imageView, "<set-?>");
        this.handleView = imageView;
    }

    public final TextView getMax() {
        return this.max;
    }

    public final void setMax(TextView textView) {
        Intrinsics.checkNotNullParameter(textView, "<set-?>");
        this.max = textView;
    }

    public final TextView getMin() {
        return this.min;
    }

    public final void setMin(TextView textView) {
        Intrinsics.checkNotNullParameter(textView, "<set-?>");
        this.min = textView;
    }

    public final CombinedChart get_chartView() {
        CombinedChart combinedChart = this._chartView;
        if (combinedChart == null) {
            Intrinsics.throwUninitializedPropertyAccessException("_chartView");
        }
        return combinedChart;
    }

    public final void set_chartView(CombinedChart combinedChart) {
        Intrinsics.checkNotNullParameter(combinedChart, "<set-?>");
        this._chartView = combinedChart;
    }

    public final BarEntry getEntry() {
        return this.entry;
    }

    public final void setEntry(BarEntry barEntry) {
        this.entry = barEntry;
    }

    /* renamed from: isDrag, reason: from getter */
    public final int getIsDrag() {
        return this.isDrag;
    }

    public final void setDrag(int i) {
        this.isDrag = i;
    }

    public final float getStartTouchX() {
        return this.startTouchX;
    }

    public final void setStartTouchX(float f) {
        this.startTouchX = f;
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public MarkView(Context context) {
        this(context, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public MarkView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MarkView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkNotNullParameter(context, "context");
        this.holderView = new View(getContext());
        this.handleView = new ImageView(getContext());
        this.max = new TextView(getContext());
        this.min = new TextView(getContext());
        this.holderView.setBackgroundColor(Config.INSTANCE.getMARK_LINE_COLOR());
        addView(this.holderView);
        this.max.layout(0, 0, 60, 60);
        this.max.setBackgroundResource(C0009R.drawable.round_max_bg);
        this.max.setGravity(17);
        this.max.setPadding(0, 15, 0, 0);
        this.max.setTextColor(Config.INSTANCE.getMARK_TEXT_COLOR());
        this.max.setTextSize(7.0f);
        addView(this.max);
        this.min.layout(0, 0, 60, 60);
        this.min.setBackgroundResource(C0009R.drawable.round_min_bg);
        this.min.setGravity(17);
        this.min.setPadding(0, 15, 0, 0);
        this.min.setTextColor(Config.INSTANCE.getMARK_TEXT_COLOR());
        this.min.setTextSize(7.0f);
        addView(this.min);
        this.handleView.setImageResource(C0009R.drawable.handle);
        this.handleView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.handleView.layout(0, 0, 50, 50);
        addView(this.handleView);
        setOnTouchListener(new View.OnTouchListener() { // from class: jp.co.nipro.cocoron.ui.view.MarkView.1
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent event) {
                Object obj;
                FlickListener.Listener flickListener;
                CombinedChart combinedChart = MarkView.this.get_chartView();
                int i2 = 0;
                if (combinedChart != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("event.action:");
                    Intrinsics.checkNotNullExpressionValue(event, "event");
                    sb.append(event.getAction());
                    sb.append(" isDrag:");
                    sb.append(MarkView.this.getIsDrag());
                    Log.d("OnTouchListener", sb.toString());
                    Object obj2 = null;
                    if (event.getAction() == 1) {
                        if (MarkView.this.getIsDrag() != 3) {
                            double d = combinedChart.getValuesByTouchPoint(event.getX(), event.getY(), YAxis.AxisDependency.LEFT).x;
                            BarEntry entry = MarkView.this.getEntry();
                            CombinedData combinedData = (CombinedData) combinedChart.getData();
                            Intrinsics.checkNotNullExpressionValue(combinedData, "chartView.data");
                            Iterable dataSets = combinedData.getDataSets();
                            Intrinsics.checkNotNullExpressionValue(dataSets, "chartView.data.dataSets");
                            Iterator it = dataSets.iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                Object next = it.next();
                                if (((IBarLineScatterCandleBubbleDataSet) next) instanceof BarDataSet) {
                                    obj2 = next;
                                    break;
                                }
                            }
                            Objects.requireNonNull(obj2, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
                            Iterable<BarEntry> values = ((BarDataSet) obj2).getValues();
                            Intrinsics.checkNotNullExpressionValue(values, "(chartView.data.dataSets…t } as BarDataSet).values");
                            double d2 = Double.MAX_VALUE;
                            for (BarEntry it2 : values) {
                                Intrinsics.checkNotNullExpressionValue(it2, "it");
                                if (Math.abs(it2.getX() - d) < d2) {
                                    entry = it2;
                                    d2 = Math.abs(it2.getX() - d);
                                }
                            }
                            MarkView.this.setEntry(entry);
                            MarkView.this.layoutSubView();
                            InverseBindingListener inverseBindingListener = MarkView.this.getInverseBindingListener();
                            if (inverseBindingListener != null) {
                                inverseBindingListener.onChange();
                            }
                            MarkViewListener selectingListener = MarkView.this.getSelectingListener();
                            if (selectingListener != null) {
                                MarkView markView = MarkView.this;
                                BarEntry entry2 = markView.getEntry();
                                Intrinsics.checkNotNull(entry2);
                                selectingListener.onChange(markView, entry2);
                            }
                            MarkViewListener selectedistener = MarkView.this.getSelectedistener();
                            if (selectedistener != null) {
                                MarkView markView2 = MarkView.this;
                                BarEntry entry3 = markView2.getEntry();
                                Intrinsics.checkNotNull(entry3);
                                selectedistener.onChange(markView2, entry3);
                            }
                        } else if (MarkView.this.getStartTouchX() - event.getX() > 100 && (flickListener = MarkView.this.getFlickListener()) != null) {
                            flickListener.onFlickToLeft();
                        }
                        return true;
                    }
                    if (event.getAction() == 2) {
                        if (MarkView.this.getIsDrag() == 1) {
                            double d3 = combinedChart.getValuesByTouchPoint(event.getX(), event.getY(), YAxis.AxisDependency.LEFT).x;
                            BarEntry entry4 = MarkView.this.getEntry();
                            CombinedData combinedData2 = (CombinedData) combinedChart.getData();
                            Intrinsics.checkNotNullExpressionValue(combinedData2, "chartView.data");
                            Iterable dataSets2 = combinedData2.getDataSets();
                            Intrinsics.checkNotNullExpressionValue(dataSets2, "chartView.data.dataSets");
                            Iterator it3 = dataSets2.iterator();
                            while (true) {
                                if (!it3.hasNext()) {
                                    break;
                                }
                                Object next2 = it3.next();
                                if (((IBarLineScatterCandleBubbleDataSet) next2) instanceof BarDataSet) {
                                    obj2 = next2;
                                    break;
                                }
                            }
                            Objects.requireNonNull(obj2, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
                            Iterable<BarEntry> values2 = ((BarDataSet) obj2).getValues();
                            Intrinsics.checkNotNullExpressionValue(values2, "(chartView.data.dataSets…t } as BarDataSet).values");
                            double d4 = Double.MAX_VALUE;
                            for (BarEntry it4 : values2) {
                                Intrinsics.checkNotNullExpressionValue(it4, "it");
                                if (Math.abs(it4.getX() - d3) < d4) {
                                    entry4 = it4;
                                    d4 = Math.abs(it4.getX() - d3);
                                }
                            }
                            MarkView.this.setEntry(entry4);
                            MarkView.this.layoutSubView();
                            InverseBindingListener inverseBindingListener2 = MarkView.this.getInverseBindingListener();
                            if (inverseBindingListener2 != null) {
                                inverseBindingListener2.onChange();
                            }
                            MarkViewListener selectingListener2 = MarkView.this.getSelectingListener();
                            if (selectingListener2 != null) {
                                MarkView markView3 = MarkView.this;
                                BarEntry entry5 = markView3.getEntry();
                                Intrinsics.checkNotNull(entry5);
                                selectingListener2.onChange(markView3, entry5);
                            }
                        } else if (Math.abs(MarkView.this.getStartTouchX() - event.getX()) > 20) {
                            MarkView.this.setDrag(3);
                        }
                        return true;
                    }
                    if (event.getAction() == 0) {
                        double d5 = combinedChart.getValuesByTouchPoint(event.getX(), event.getY(), YAxis.AxisDependency.LEFT).x;
                        CombinedData combinedData3 = (CombinedData) combinedChart.getData();
                        Intrinsics.checkNotNullExpressionValue(combinedData3, "chartView.data");
                        Iterable dataSets3 = combinedData3.getDataSets();
                        Intrinsics.checkNotNullExpressionValue(dataSets3, "chartView.data.dataSets");
                        Iterator it5 = dataSets3.iterator();
                        while (true) {
                            if (!it5.hasNext()) {
                                obj = null;
                                break;
                            }
                            Object next3 = it5.next();
                            if (((IBarLineScatterCandleBubbleDataSet) next3) instanceof BarDataSet) {
                                obj = next3;
                                break;
                            }
                        }
                        Objects.requireNonNull(obj, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
                        int indexOf = ((BarDataSet) obj).getValues().indexOf(MarkView.this.getEntry());
                        CombinedData combinedData4 = (CombinedData) combinedChart.getData();
                        Intrinsics.checkNotNullExpressionValue(combinedData4, "chartView.data");
                        Iterable dataSets4 = combinedData4.getDataSets();
                        Intrinsics.checkNotNullExpressionValue(dataSets4, "chartView.data.dataSets");
                        Iterator it6 = dataSets4.iterator();
                        while (true) {
                            if (!it6.hasNext()) {
                                break;
                            }
                            Object next4 = it6.next();
                            if (((IBarLineScatterCandleBubbleDataSet) next4) instanceof BarDataSet) {
                                obj2 = next4;
                                break;
                            }
                        }
                        Objects.requireNonNull(obj2, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
                        Iterable values3 = ((BarDataSet) obj2).getValues();
                        Intrinsics.checkNotNullExpressionValue(values3, "(chartView.data.dataSets…t } as BarDataSet).values");
                        int i3 = indexOf;
                        double d6 = Double.MAX_VALUE;
                        for (Object obj3 : values3) {
                            int i4 = i2 + 1;
                            if (i2 < 0) {
                                CollectionsKt.throwIndexOverflow();
                            }
                            BarEntry barEntry = (BarEntry) obj3;
                            Intrinsics.checkNotNullExpressionValue(barEntry, "barEntry");
                            if (Math.abs(barEntry.getX() - d5) < d6) {
                                d6 = Math.abs(barEntry.getX() - d5);
                                i3 = i2;
                            }
                            i2 = i4;
                        }
                        if (Math.abs(i3 - indexOf) < 4) {
                            MarkView.this.setDrag(1);
                        } else {
                            MarkView.this.setDrag(2);
                        }
                        MarkView.this.setStartTouchX(event.getX());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutSubView();
    }

    public final void setChartView(CombinedChart chartView) {
        Intrinsics.checkNotNullParameter(chartView, "chartView");
        this._chartView = chartView;
        layoutSubView();
    }

    public final void setBarEntry(BarEntry entry) {
        this.entry = entry;
        layoutSubView();
    }

    /* renamed from: getBarEntry, reason: from getter */
    public final BarEntry getEntry() {
        return this.entry;
    }

    public final void layoutSubView() {
        float[] yVals;
        float[] yVals2;
        float[] yVals3;
        int height = getHeight();
        CombinedChart combinedChart = this._chartView;
        if (combinedChart == null) {
            Intrinsics.throwUninitializedPropertyAccessException("_chartView");
        }
        if (combinedChart != null && this.entry != null) {
            CombinedChart combinedChart2 = this._chartView;
            if (combinedChart2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("_chartView");
            }
            int i = (int) combinedChart2.getPosition(this.entry, YAxis.AxisDependency.LEFT).x;
            this.holderView.layout(i - 1, 0, i + 1, height);
            BarEntry barEntry = this.entry;
            float sum = (barEntry == null || (yVals3 = barEntry.getYVals()) == null) ? 0.0f : ArraysKt.sum(yVals3);
            BarEntry barEntry2 = this.entry;
            float f = (barEntry2 == null || (yVals2 = barEntry2.getYVals()) == null) ? 0.0f : yVals2[0];
            CombinedChart combinedChart3 = this._chartView;
            if (combinedChart3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("_chartView");
            }
            BarEntry barEntry3 = this.entry;
            Intrinsics.checkNotNull(barEntry3);
            int i2 = (int) combinedChart3.getPosition(new Entry(barEntry3.getX(), sum), YAxis.AxisDependency.LEFT).y;
            CombinedChart combinedChart4 = this._chartView;
            if (combinedChart4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("_chartView");
            }
            BarEntry barEntry4 = this.entry;
            Intrinsics.checkNotNull(barEntry4);
            int i3 = (int) combinedChart4.getPosition(new Entry(barEntry4.getX(), f), YAxis.AxisDependency.LEFT).y;
            TextView textView = this.max;
            textView.layout(i - (textView.getWidth() / 2), (i2 - this.max.getHeight()) - 10, (this.max.getWidth() / 2) + i, i2 - 10);
            this.max.setText(String.valueOf((int) sum));
            if (this.min.getHeight() + i3 + 10 > height) {
                TextView textView2 = this.min;
                textView2.layout(i - (textView2.getWidth() / 2), (height - this.min.getHeight()) - 5, (this.min.getWidth() / 2) + i, height - 5);
            } else {
                TextView textView3 = this.min;
                textView3.layout(i - (textView3.getWidth() / 2), i3 + 10, (this.min.getWidth() / 2) + i, i3 + this.min.getHeight() + 10);
            }
            this.min.setText(String.valueOf((int) f));
            BarEntry barEntry5 = this.entry;
            if (barEntry5 != null && (yVals = barEntry5.getYVals()) != null && ArraysKt.sum(yVals) == 0.0f) {
                this.max.setVisibility(4);
                this.min.setVisibility(4);
            } else {
                this.max.setVisibility(0);
                this.min.setVisibility(0);
            }
            ImageView imageView = this.handleView;
            imageView.layout(i - (imageView.getWidth() / 2), (height - this.handleView.getHeight()) - 20, i + (this.handleView.getWidth() / 2), height - 20);
            BarEntry barEntry6 = this.entry;
            if (barEntry6 != null) {
                float x = barEntry6.getX();
                CombinedChart combinedChart5 = this._chartView;
                if (combinedChart5 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("_chartView");
                }
                CombinedData combinedData = (CombinedData) combinedChart5.getData();
                Intrinsics.checkNotNullExpressionValue(combinedData, "_chartView.data");
                BarData barData = combinedData.getBarData();
                Intrinsics.checkNotNullExpressionValue(barData, "_chartView.data.barData");
                Object obj = barData.getDataSets().get(0);
                Objects.requireNonNull(obj, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
                List<T> values = ((BarDataSet) obj).getValues();
                Intrinsics.checkNotNullExpressionValue(values, "(_chartView.data.barData…[0] as BarDataSet).values");
                Object last = CollectionsKt.last((List<? extends Object>) values);
                Intrinsics.checkNotNullExpressionValue(last, "(_chartView.data.barData…BarDataSet).values.last()");
                if (x == ((BarEntry) last).getX()) {
                    this.max.setVisibility(4);
                    this.min.setVisibility(4);
                    this.handleView.setVisibility(4);
                    this.holderView.setVisibility(4);
                    return;
                }
            }
            this.handleView.setVisibility(0);
            this.holderView.setVisibility(0);
            return;
        }
        this.holderView.layout(-100, 0, -99, height);
        ImageView imageView2 = this.handleView;
        imageView2.layout(-100, height - imageView2.getHeight(), this.handleView.getWidth() - 100, height);
        TextView textView4 = this.max;
        textView4.layout(-100, height - textView4.getHeight(), this.max.getWidth() - 100, height);
        TextView textView5 = this.min;
        textView5.layout(-100, height - textView5.getHeight(), this.min.getWidth() - 100, height);
        this.max.setVisibility(4);
        this.min.setVisibility(4);
        this.handleView.setVisibility(4);
        this.holderView.setVisibility(4);
    }

    public final MarkViewListener getSelectedistener() {
        return this.selectedistener;
    }

    public final void setSelectedistener(MarkViewListener markViewListener) {
        this.selectedistener = markViewListener;
    }

    public final MarkViewListener getSelectingListener() {
        return this.selectingListener;
    }

    public final void setSelectingListener(MarkViewListener markViewListener) {
        this.selectingListener = markViewListener;
    }

    public final FlickListener.Listener getFlickListener() {
        return this.flickListener;
    }

    public final void setFlickListener(FlickListener.Listener listener) {
        this.flickListener = listener;
    }

    public final void setOnSelecting(MarkViewListener listener) {
        Intrinsics.checkNotNullParameter(listener, "listener");
        this.selectingListener = listener;
    }

    public final void setOnSelected(MarkViewListener listener) {
        Intrinsics.checkNotNullParameter(listener, "listener");
        this.selectedistener = listener;
    }

    public final void setOnFlick(FlickListener.Listener listener) {
        Intrinsics.checkNotNullParameter(listener, "listener");
        this.flickListener = listener;
    }

    /* compiled from: MarkView.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007¨\u0006\t"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/MarkView$Companion;", "", "()V", "setListeners", "", "view", "Ljp/co/nipro/cocoron/ui/view/MarkView;", "attrChange", "Landroidx/databinding/InverseBindingListener;", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @BindingAdapter({"app:barEntryAttrChanged"})
        @JvmStatic
        public final void setListeners(MarkView view, InverseBindingListener attrChange) {
            Intrinsics.checkNotNullParameter(view, "view");
            Intrinsics.checkNotNullParameter(attrChange, "attrChange");
            view.setInverseBindingListener(attrChange);
        }
    }

    public final InverseBindingListener getInverseBindingListener() {
        return this.inverseBindingListener;
    }

    public final void setInverseBindingListener(InverseBindingListener inverseBindingListener) {
        this.inverseBindingListener = inverseBindingListener;
    }
}

package jp.co.nipro.cocoron.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jp.co.nipro.Cocoron.C0009R;
import jp.co.nipro.cocoron.common.Config;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: HolderView.kt */
@InverseBindingMethods({@InverseBindingMethod(attribute = "selected", type = HolderView.class)})
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u0000 >2\u00020\u0001:\u0001>B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004B\u0019\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006¢\u0006\u0002\u0010\u0007B!\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\u0006\u0010\b\u001a\u00020\t¢\u0006\u0002\u0010\nJ\u0006\u0010*\u001a\u00020\tJ0\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\t2\u0006\u00100\u001a\u00020\t2\u0006\u00101\u001a\u00020\t2\u0006\u00102\u001a\u00020\tH\u0014J\u0018\u00103\u001a\u00020,2\u0006\u00104\u001a\u00020\t2\u0006\u00105\u001a\u00020\tH\u0014J\u000e\u00106\u001a\u00020,2\u0006\u00107\u001a\u00020\tJ\u000e\u00108\u001a\u00020,2\u0006\u00109\u001a\u00020\u0012J\u0014\u0010:\u001a\u00020,2\f\u0010;\u001a\b\u0012\u0004\u0012\u00020=0<R\u001a\u0010\u000b\u001a\u00020\fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001c\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001c\u0010\u0017\u001a\u0004\u0018\u00010\u0012X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0014\"\u0004\b\u0019\u0010\u0016R\u001a\u0010\u001a\u001a\u00020\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u001a\u0010\u001f\u001a\u00020\fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b \u0010\u000e\"\u0004\b!\u0010\u0010R*\u0010\"\u001a\u0012\u0012\u0004\u0012\u00020$0#j\b\u0012\u0004\u0012\u00020$`%X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b&\u0010'\"\u0004\b(\u0010)¨\u0006?"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/HolderView;", "Landroid/view/ViewGroup;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "backLine", "Landroid/widget/ImageView;", "getBackLine", "()Landroid/widget/ImageView;", "setBackLine", "(Landroid/widget/ImageView;)V", "inverseBindingListener", "Landroidx/databinding/InverseBindingListener;", "getInverseBindingListener", "()Landroidx/databinding/InverseBindingListener;", "setInverseBindingListener", "(Landroidx/databinding/InverseBindingListener;)V", "publicListener", "getPublicListener", "setPublicListener", "select", "getSelect", "()I", "setSelect", "(I)V", "selectedView", "getSelectedView", "setSelectedView", "tagViews", "Ljava/util/ArrayList;", "Landroid/widget/TextView;", "Lkotlin/collections/ArrayList;", "getTagViews", "()Ljava/util/ArrayList;", "setTagViews", "(Ljava/util/ArrayList;)V", "getSelected", "onLayout", "", "changed", "", "l", "t", "r", "b", "onMeasure", "widthMeasureSpec", "heightMeasureSpec", "setSelected", "sel", "setSelectedChanged", "attrChange", "setTags", "tags", "", "", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class HolderView extends ViewGroup {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private ImageView backLine;
    private InverseBindingListener inverseBindingListener;
    private InverseBindingListener publicListener;
    private int select;
    private ImageView selectedView;
    private ArrayList<TextView> tagViews;

    @BindingAdapter({"app:selectedAttrChanged"})
    @JvmStatic
    public static final void setListeners(HolderView holderView, InverseBindingListener inverseBindingListener) {
        INSTANCE.setListeners(holderView, inverseBindingListener);
    }

    public final ArrayList<TextView> getTagViews() {
        return this.tagViews;
    }

    public final void setTagViews(ArrayList<TextView> arrayList) {
        Intrinsics.checkNotNullParameter(arrayList, "<set-?>");
        this.tagViews = arrayList;
    }

    public final ImageView getBackLine() {
        return this.backLine;
    }

    public final void setBackLine(ImageView imageView) {
        Intrinsics.checkNotNullParameter(imageView, "<set-?>");
        this.backLine = imageView;
    }

    public final ImageView getSelectedView() {
        return this.selectedView;
    }

    public final void setSelectedView(ImageView imageView) {
        Intrinsics.checkNotNullParameter(imageView, "<set-?>");
        this.selectedView = imageView;
    }

    public final int getSelect() {
        return this.select;
    }

    public final void setSelect(int i) {
        this.select = i;
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public HolderView(Context context) {
        this(context, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public HolderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public HolderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkNotNullParameter(context, "context");
        this.tagViews = new ArrayList<>();
        this.backLine = new ImageView(getContext());
        this.selectedView = new ImageView(getContext());
        this.select = -1;
        this.backLine.setImageResource(C0009R.drawable.holderbk);
        this.backLine.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(this.backLine);
        this.selectedView.setImageResource(C0009R.drawable.holder);
        this.selectedView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(this.selectedView);
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int measuredWidth = getMeasuredWidth() / (this.tagViews.size() + 1);
        int i = measuredWidth;
        for (TextView textView : this.tagViews) {
            int measuredWidth2 = textView.getMeasuredWidth();
            int measuredHeight = textView.getMeasuredHeight();
            int i2 = measuredWidth2 / 2;
            textView.layout(i - i2, (getMeasuredHeight() - measuredHeight) / 2, i2 + i, (getMeasuredHeight() + measuredHeight) / 2);
            i += measuredWidth;
        }
        Resources resources = getResources();
        Intrinsics.checkNotNullExpressionValue(resources, "resources");
        int applyDimension = (int) TypedValue.applyDimension(1, 1.0f, resources.getDisplayMetrics());
        this.backLine.layout(0, (getMeasuredHeight() - applyDimension) / 2, getMeasuredWidth(), (getMeasuredHeight() + applyDimension) / 2);
        int measuredWidth3 = this.selectedView.getMeasuredWidth();
        int measuredHeight2 = this.selectedView.getMeasuredHeight();
        int i3 = measuredWidth3 / 2;
        this.selectedView.layout(((this.select + 1) * measuredWidth) - i3, (getMeasuredHeight() - measuredHeight2) / 2, (measuredWidth * (this.select + 1)) + i3, (getMeasuredHeight() + measuredHeight2) / 2);
        this.selectedView.setTranslationX(0.0f);
    }

    public final void setTags(List<String> tags) {
        Intrinsics.checkNotNullParameter(tags, "tags");
        Iterator<T> it = this.tagViews.iterator();
        while (it.hasNext()) {
            removeView((TextView) it.next());
        }
        final int i = 0;
        for (Object obj : tags) {
            int i2 = i + 1;
            if (i < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            String str = (String) obj;
            TextView textView = new TextView(getContext());
            if (Build.VERSION.SDK_INT >= 24) {
                textView.setText(Html.fromHtml(str, 0));
            } else {
                textView.setText(Html.fromHtml(str));
            }
            textView.setTextSize(20.0f);
            textView.setTextColor(Config.INSTANCE.getHOLDER_TEXT_COLOR());
            textView.setPadding(20, 0, 20, 10);
            textView.setOnClickListener(new View.OnClickListener() { // from class: jp.co.nipro.cocoron.ui.view.HolderView$setTags$$inlined$forEachIndexed$lambda$1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.setSelected(i);
                    InverseBindingListener inverseBindingListener = this.getInverseBindingListener();
                    if (inverseBindingListener != null) {
                        inverseBindingListener.onChange();
                    }
                    InverseBindingListener publicListener = this.getPublicListener();
                    if (publicListener != null) {
                        publicListener.onChange();
                    }
                }
            });
            addView(textView);
            this.tagViews.add(textView);
            i = i2;
        }
    }

    public final void setSelected(int sel) {
        if (sel == this.select) {
            return;
        }
        this.select = sel;
        int measuredWidth = getMeasuredWidth() / (this.tagViews.size() + 1);
        float measuredWidth2 = (((this.select + 1) * measuredWidth) - (this.selectedView.getMeasuredWidth() / 2)) - this.selectedView.getX();
        int i = 0;
        if (measuredWidth2 != 0.0f) {
            ImageView imageView = this.selectedView;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(imageView, "x", imageView.getX() + measuredWidth2);
            ofFloat.setDuration(Math.abs((long) (measuredWidth2 / measuredWidth)) * 100);
            ofFloat.start();
        }
        for (Object obj : this.tagViews) {
            int i2 = i + 1;
            if (i < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            TextView textView = (TextView) obj;
            if (i == this.select) {
                textView.setTextColor(Config.INSTANCE.getHOLDER_SELECT_TEXT_COLOR());
            } else {
                textView.setTextColor(Config.INSTANCE.getHOLDER_TEXT_COLOR());
            }
            i = i2;
        }
    }

    public final int getSelected() {
        return this.select;
    }

    public final InverseBindingListener getInverseBindingListener() {
        return this.inverseBindingListener;
    }

    public final void setInverseBindingListener(InverseBindingListener inverseBindingListener) {
        this.inverseBindingListener = inverseBindingListener;
    }

    public final InverseBindingListener getPublicListener() {
        return this.publicListener;
    }

    public final void setPublicListener(InverseBindingListener inverseBindingListener) {
        this.publicListener = inverseBindingListener;
    }

    public final void setSelectedChanged(InverseBindingListener attrChange) {
        Intrinsics.checkNotNullParameter(attrChange, "attrChange");
        this.publicListener = attrChange;
    }

    /* compiled from: HolderView.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007¨\u0006\t"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/HolderView$Companion;", "", "()V", "setListeners", "", "view", "Ljp/co/nipro/cocoron/ui/view/HolderView;", "attrChange", "Landroidx/databinding/InverseBindingListener;", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @BindingAdapter({"app:selectedAttrChanged"})
        @JvmStatic
        public final void setListeners(HolderView view, InverseBindingListener attrChange) {
            Intrinsics.checkNotNullParameter(view, "view");
            Intrinsics.checkNotNullParameter(attrChange, "attrChange");
            view.setInverseBindingListener(attrChange);
        }
    }
}

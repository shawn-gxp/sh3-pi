package jp.co.nipro.cocoron.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import kotlin.Metadata;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: PickerView.kt */
@InverseBindingMethods({@InverseBindingMethod(attribute = "currentItem", type = PickerView.class)})
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0019\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005¢\u0006\u0002\u0010\u0006J\b\u0010\u0010\u001a\u00020\u0011H\u0016J\u000e\u0010\u0012\u001a\u00020\u00112\u0006\u0010\r\u001a\u00020\bR\u001c\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001c\u0010\r\u001a\u0004\u0018\u00010\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\n\"\u0004\b\u000f\u0010\f¨\u0006\u0014"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/PickerView;", "Lcom/contrarywind/view/WheelView;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "itemListener", "Landroidx/databinding/InverseBindingListener;", "getItemListener", "()Landroidx/databinding/InverseBindingListener;", "setItemListener", "(Landroidx/databinding/InverseBindingListener;)V", "selectingListener", "getSelectingListener", "setSelectingListener", "cancelFuture", "", "setSelecting", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class PickerView extends WheelView {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private InverseBindingListener itemListener;
    private InverseBindingListener selectingListener;

    @BindingAdapter({"app:currentItemAttrChanged"})
    @JvmStatic
    public static final void setListeners(PickerView pickerView, InverseBindingListener inverseBindingListener) {
        INSTANCE.setListeners(pickerView, inverseBindingListener);
    }

    public PickerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnItemSelectedListener(new OnItemSelectedListener() { // from class: jp.co.nipro.cocoron.ui.view.PickerView.1
            @Override // com.contrarywind.listener.OnItemSelectedListener
            public final void onItemSelected(int i) {
                InverseBindingListener itemListener = PickerView.this.getItemListener();
                if (itemListener != null) {
                    itemListener.onChange();
                }
            }
        });
    }

    public final InverseBindingListener getItemListener() {
        return this.itemListener;
    }

    public final void setItemListener(InverseBindingListener inverseBindingListener) {
        this.itemListener = inverseBindingListener;
    }

    /* compiled from: PickerView.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007¨\u0006\t"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/PickerView$Companion;", "", "()V", "setListeners", "", "view", "Ljp/co/nipro/cocoron/ui/view/PickerView;", "attrChange", "Landroidx/databinding/InverseBindingListener;", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @BindingAdapter({"app:currentItemAttrChanged"})
        @JvmStatic
        public final void setListeners(PickerView view, InverseBindingListener attrChange) {
            Intrinsics.checkNotNullParameter(view, "view");
            Intrinsics.checkNotNullParameter(attrChange, "attrChange");
            view.setItemListener(attrChange);
        }
    }

    public final InverseBindingListener getSelectingListener() {
        return this.selectingListener;
    }

    public final void setSelectingListener(InverseBindingListener inverseBindingListener) {
        this.selectingListener = inverseBindingListener;
    }

    public final void setSelecting(InverseBindingListener selectingListener) {
        Intrinsics.checkNotNullParameter(selectingListener, "selectingListener");
        this.selectingListener = selectingListener;
    }

    @Override // com.contrarywind.view.WheelView
    public void cancelFuture() {
        super.cancelFuture();
        InverseBindingListener inverseBindingListener = this.selectingListener;
        if (inverseBindingListener != null) {
            inverseBindingListener.onChange();
        }
    }
}

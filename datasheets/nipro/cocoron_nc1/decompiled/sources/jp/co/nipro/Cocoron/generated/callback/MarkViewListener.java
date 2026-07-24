package jp.co.nipro.cocoron.generated.callback;

import com.github.mikephil.charting.data.BarEntry;
import jp.co.nipro.cocoron.ui.view.MarkView;

/* loaded from: classes.dex */
public final class MarkViewListener implements MarkView.MarkViewListener {
    final Listener mListener;
    final int mSourceId;

    public interface Listener {
        void _internalCallbackOnChange1(int sourceId, MarkView callbackArg_0, BarEntry callbackArg_1);
    }

    public MarkViewListener(Listener listener, int sourceId) {
        this.mListener = listener;
        this.mSourceId = sourceId;
    }

    @Override // jp.co.nipro.cocoron.ui.view.MarkView.MarkViewListener
    public void onChange(MarkView callbackArg_0, BarEntry callbackArg_1) {
        this.mListener._internalCallbackOnChange1(this.mSourceId, callbackArg_0, callbackArg_1);
    }
}

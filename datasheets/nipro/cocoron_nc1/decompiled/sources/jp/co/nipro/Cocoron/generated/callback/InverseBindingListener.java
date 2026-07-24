package jp.co.nipro.cocoron.generated.callback;

/* loaded from: classes.dex */
public final class InverseBindingListener implements androidx.databinding.InverseBindingListener {
    final Listener mListener;
    final int mSourceId;

    public interface Listener {
        void _internalCallbackOnChange(int sourceId);
    }

    public InverseBindingListener(Listener listener, int sourceId) {
        this.mListener = listener;
        this.mSourceId = sourceId;
    }

    @Override // androidx.databinding.InverseBindingListener
    public void onChange() {
        this.mListener._internalCallbackOnChange(this.mSourceId);
    }
}

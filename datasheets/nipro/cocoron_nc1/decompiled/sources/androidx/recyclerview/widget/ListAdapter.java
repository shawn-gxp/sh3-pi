package androidx.recyclerview.widget;

import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.List;

/* loaded from: classes.dex */
public abstract class ListAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final AsyncListDiffer<T> mHelper;

    protected ListAdapter(DiffUtil.ItemCallback<T> itemCallback) {
        this.mHelper = new AsyncListDiffer<>(new AdapterListUpdateCallback(this), new AsyncDifferConfig.Builder(itemCallback).build());
    }

    protected ListAdapter(AsyncDifferConfig<T> asyncDifferConfig) {
        this.mHelper = new AsyncListDiffer<>(new AdapterListUpdateCallback(this), asyncDifferConfig);
    }

    public void submitList(List<T> list) {
        this.mHelper.submitList(list);
    }

    protected T getItem(int i) {
        return this.mHelper.getCurrentList().get(i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mHelper.getCurrentList().size();
    }
}

package android.support.p001v7.widget.util;

import android.support.p001v7.util.SortedList.Callback;
import android.support.p001v7.widget.RecyclerView.Adapter;

/* renamed from: android.support.v7.widget.util.SortedListAdapterCallback */
public abstract class SortedListAdapterCallback<T2> extends Callback<T2> {
    final Adapter mAdapter;

    public SortedListAdapterCallback(Adapter adapter) {
        this.mAdapter = adapter;
    }

    public void onInserted(int position, int count) {
        this.mAdapter.notifyItemRangeInserted(position, count);
    }

    public void onRemoved(int position, int count) {
        this.mAdapter.notifyItemRangeRemoved(position, count);
    }

    public void onMoved(int fromPosition, int toPosition) {
        this.mAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    public void onChanged(int position, int count) {
        this.mAdapter.notifyItemRangeChanged(position, count);
    }
}

package com.example.ballerevents;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EndlessRecyclerListener extends RecyclerView.OnScrollListener {
    public interface LoadMore { void onLoadMore(); }
    private final LoadMore cb;
    public EndlessRecyclerListener(LoadMore cb){ this.cb = cb; }

    @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
        LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
        if (lm == null) return;
        int last = lm.findLastVisibleItemPosition();
        int total = rv.getAdapter() == null ? 0 : rv.getAdapter().getItemCount();
        if (total > 0 && last >= total - 3) cb.onLoadMore();
    }
}

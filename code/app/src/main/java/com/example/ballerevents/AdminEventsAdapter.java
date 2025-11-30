package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class AdminEventsAdapter extends ListAdapter<Event, AdminEventsAdapter.VH> {

    /** Row click callback */
    public interface OnEventClick { void onClick(@NonNull Event event); }

    private final OnEventClick onEventClick;

    public AdminEventsAdapter(@NonNull OnEventClick onEventClick) {
        super(DIFF);
        this.onEventClick = onEventClick;
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<Event> DIFF = new DiffUtil.ItemCallback<Event>() {
        @Override public boolean areItemsTheSame(@NonNull Event a, @NonNull Event b) {
            return a.getId() != null && a.getId().equals(b.getId());
        }
        @Override public boolean areContentsTheSame(@NonNull Event a, @NonNull Event b) {
            String at = a.getTitle() == null ? "" : a.getTitle();
            String bt = b.getTitle() == null ? "" : b.getTitle();
            return at.equals(bt);
        }
    };

    static class VH extends RecyclerView.ViewHolder {
        final View root;
        final TextView tvTitle;
        VH(@NonNull View itemView) {
            super(itemView);
            root    = itemView;
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Event e = getItem(pos);
        h.tvTitle.setText(e.getTitle() == null ? "Untitled Event" : e.getTitle());
        h.root.setOnClickListener(v -> onEventClick.onClick(e));
    }

    @Override
    public long getItemId(int position) {
        Event e = getItem(position);
        return e.getId() != null ? e.getId().hashCode() : position;
    }
}

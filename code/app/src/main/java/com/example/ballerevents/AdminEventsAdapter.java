package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

public class AdminEventsAdapter extends ListAdapter<Event, AdminEventsAdapter.EventViewHolder> {

    public interface OnEventActionListener {
        void onDelete(Event event);
    }

    private final OnEventActionListener listener;

    public AdminEventsAdapter(OnEventActionListener listener) {
        super(EventDiffCallback);
        this.listener = listener;
    }

    @NonNull @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder h, int position) {
        Event e = getItem(position);
        if (e == null) return;

        h.tvTitle.setText(e.getTitle());
        h.tvDate.setText(e.getDate());
        h.tvLocation.setText(e.getLocationName());

        // Load image from URL using Glide
        Glide.with(h.itemView.getContext())
                .load(e.getEventPosterUrl())
                .placeholder(R.drawable.placeholder_image) // Use your placeholder
                .error(R.drawable.placeholder_image)
                .into(h.ivPoster);

        h.ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_admin_event_row, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> handleMenu(item, e));
            popup.show();
        });
    }

    private boolean handleMenu(MenuItem item, Event e) {
        if (item.getItemId() == R.id.action_delete) {
            listener.onDelete(e);
            return true;
        }
        return false;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardRoot;
        ImageView ivPoster, ivMenu;
        TextView tvTitle, tvDate, tvLocation;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot   = itemView.findViewById(R.id.card_root);
            ivPoster   = itemView.findViewById(R.id.ivPoster);
            ivMenu     = itemView.findViewById(R.id.ivMenu);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvDate     = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }

    private static final DiffUtil.ItemCallback<Event> EventDiffCallback = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
    };
}
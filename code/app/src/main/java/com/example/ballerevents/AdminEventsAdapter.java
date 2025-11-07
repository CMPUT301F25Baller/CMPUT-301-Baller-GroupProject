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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.EventViewHolder> {

    public interface OnEventActionListener {
        void onDelete(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private final OnEventActionListener listener;

    public AdminEventsAdapter(OnEventActionListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder h, int position) {
        Event e = events.get(position);
        h.tvDate.setText(e.getDate());
        h.tvTitle.setText(e.getTitle());
        h.tvLocation.setText("See details");
        h.ivPoster.setImageResource(R.drawable.placeholder_poster);

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

    @Override
    public int getItemCount() { return events.size(); }

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
}

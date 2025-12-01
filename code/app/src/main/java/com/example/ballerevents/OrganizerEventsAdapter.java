package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ballerevents.databinding.ItemEventOrganizerBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the organizer's own events list.
 */
public class OrganizerEventsAdapter
        extends RecyclerView.Adapter<OrganizerEventsAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;

    public OrganizerEventsAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemEventOrganizerBinding binding;

        public ViewHolder(ItemEventOrganizerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemEventOrganizerBinding binding =
                ItemEventOrganizerBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Event event = events.get(position);

        // These IDs must exist in item_event_organizer.xml
        holder.binding.tvEventTitle.setText(event.getTitle());
        holder.binding.tvEventDate.setText(event.getDate());
        holder.binding.tvEventLocation.setText(event.getLocationName());

        // No image loading here (ivPoster removed) to avoid compile error.

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}

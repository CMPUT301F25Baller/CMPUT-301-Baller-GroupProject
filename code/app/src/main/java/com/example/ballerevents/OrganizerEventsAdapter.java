package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ballerevents.databinding.ItemEventOrganizerBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying the list of events owned by a specific organizer.
 * <p>
 * This adapter binds event data (title, date, location) to the RecyclerView
 * using the {@code item_event_organizer} layout.
 * </p>
 */
public class OrganizerEventsAdapter
        extends RecyclerView.Adapter<OrganizerEventsAdapter.ViewHolder> {

    /**
     * Listener interface for handling click events on organizer event items.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event item is clicked.
         *
         * @param event The clicked event.
         */
        void onEventClick(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;

    /**
     * Constructs a new OrganizerEventsAdapter.
     *
     * @param listener The listener to handle event click actions.
     */
    public OrganizerEventsAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the data set and refreshes the RecyclerView.
     *
     * @param newEvents The new list of events to display.
     */
    public void submitList(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for organizer event items.
     */
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

        holder.binding.tvEventTitle.setText(event.getTitle());
        holder.binding.tvEventDate.setText(event.getDate());
        holder.binding.tvEventLocation.setText(event.getLocationName());

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
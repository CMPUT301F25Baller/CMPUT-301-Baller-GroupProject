package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemEventNearBinding;

/**
 * Adapter for displaying a vertical list of nearby events using
 * {@link ListAdapter} and {@link DiffUtil} for efficient updates.
 * <p>
 * Each item is shown using the compact layout {@code item_event_near.xml}.
 * The adapter supports click events through {@link OnEventClickListener}.
 * </p>
 */
public class NearEventAdapter extends ListAdapter<Event, NearEventAdapter.EventViewHolder> {

    /**
     * Listener interface for event card click actions.
     */
    public interface OnEventClickListener {
        /**
         * Callback invoked when an event card is selected.
         *
         * @param event The selected {@link Event}.
         */
        void onEventClick(Event event);
    }

    /** Listener instance used to handle click events on items. */
    private final OnEventClickListener onClickListener;

    /**
     * Creates a new {@link NearEventAdapter}.
     *
     * @param onClickListener A listener to be notified when an event card is clicked.
     */
    public NearEventAdapter(OnEventClickListener onClickListener) {
        super(EventDiffCallback);
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventNearBinding binding = ItemEventNearBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position), onClickListener);
    }

    /**
     * ViewHolder representing a single "near you" event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        /** Binding object for item_event_near.xml. */
        private final ItemEventNearBinding binding;

        /**
         * Constructs a new ViewHolder.
         *
         * @param binding ViewBinding for the event card layout.
         */
        public EventViewHolder(ItemEventNearBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds an {@link Event} model to the card views and attaches a click listener.
         *
         * @param event            The event data to display.
         * @param onClickListener  Listener to invoke when the card is tapped.
         */
        public void bind(Event event, OnEventClickListener onClickListener) {
            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventLocation.setText(event.getLocationName());
            binding.tvEventDate.setText(event.getDate());
            binding.tvEventPrice.setText(event.getPrice());

            // Load image via Glide with placeholder/error fallbacks
            Glide.with(binding.getRoot().getContext())
                    .load(event.getEventPosterUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(binding.ivEventImage);

            binding.getRoot().setOnClickListener(v -> onClickListener.onEventClick(event));
        }
    }

    /**
     * DiffUtil callback for comparing {@link Event} items.
     * <p>
     * Events are considered the same item if they have the same Firestore document ID.
     * </p>
     */
    private static final DiffUtil.ItemCallback<Event> EventDiffCallback =
            new DiffUtil.ItemCallback<Event>() {

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

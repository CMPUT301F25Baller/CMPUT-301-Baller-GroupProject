package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <-- IMPORT GLIDE
import com.example.ballerevents.databinding.ItemEventNearBinding;

/**
 * A RecyclerView.Adapter for displaying {@link Event} objects in a vertical,
 * compact card format (item_event_near.xml). Uses {@link ListAdapter}
 * for efficient list management.
 */
public class NearEventAdapter extends ListAdapter<Event, NearEventAdapter.EventViewHolder> {

    /**
     * A click listener interface to handle clicks on an event card.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event card is clicked.
         * @param event The Event object that was clicked.
         */
        void onEventClick(Event event);
    }

    private final OnEventClickListener onClickListener;

    /**
     * Constructs the adapter.
     * @param onClickListener The listener to be notified of click events.
     */
    public NearEventAdapter(OnEventClickListener onClickListener) {
        super(EventDiffCallback);
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventNearBinding binding = ItemEventNearBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position), onClickListener);
    }

    /**
     * ViewHolder for the "near you" event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventNearBinding binding;

        public EventViewHolder(ItemEventNearBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds an Event object to the view holder's views.
         * @param event The event data to display.
         * @param onClickListener The listener to attach to the card.
         */
        public void bind(Event event, OnEventClickListener onClickListener) {
            // --- UPDATED BINDING ---
            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventLocation.setText(event.getLocationName());
            binding.tvEventDate.setText(event.getDate());
            binding.tvEventPrice.setText(event.getPrice());

            // Load image using Glide
            Glide.with(binding.getRoot().getContext())
                    .load(event.getEventPosterUrl())
                    .placeholder(R.drawable.placeholder_image) // You must add this drawable
                    .error(R.drawable.placeholder_image)       // for placeholders
                    .into(binding.ivEventImage);
            // --- END OF UPDATE ---

            binding.getRoot().setOnClickListener(v -> onClickListener.onEventClick(event));
        }
    }

    /**
     * DiffUtil.ItemCallback for calculating list differences efficiently.
     * Compares events based on their unique Firestore document ID.
     */
    private static final DiffUtil.ItemCallback<Event> EventDiffCallback = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId().equals(newItem.getId()); // Compare String IDs
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
    };
}
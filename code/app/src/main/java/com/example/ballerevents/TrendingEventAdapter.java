package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemEventTrendingBinding;

/**
 * RecyclerView adapter for displaying {@link Event} objects inside a
 * horizontal carousel-like list using the wide card layout.
 * <p>
 * This adapter is primarily used for the "Popular" or "Trending" sections
 * of the dashboard.
 * </p>
 */
public class TrendingEventAdapter extends ListAdapter<Event, TrendingEventAdapter.EventViewHolder> {

    /**
     * Listener interface invoked when an event card is selected.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event item is clicked.
         *
         * @param event The event that was clicked.
         */
        void onEventClick(Event event);
    }

    private final OnEventClickListener onClickListener;

    /**
     * Creates a new adapter instance.
     *
     * @param onClickListener Listener that receives click events for each item.
     */
    public TrendingEventAdapter(OnEventClickListener onClickListener) {
        super(EventDiffCallback);
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventTrendingBinding binding =
                ItemEventTrendingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position), onClickListener);
    }

    /**
     * ViewHolder representing a single trending event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        private final ItemEventTrendingBinding binding;

        public EventViewHolder(ItemEventTrendingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds an {@link Event} object to the card UI.
         *
         * @param event            The event data being displayed.
         * @param onClickListener  Listener for click events on the card.
         */
        public void bind(Event event, OnEventClickListener onClickListener) {
            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventLocation.setText(event.getLocationName());
            binding.tvEventDate.setText(event.getDate());
            binding.tvPrice.setText(event.getPrice());

            Glide.with(binding.getRoot().getContext())
                    .load(event.getEventPosterUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(binding.ivEventBackground);

            binding.getRoot().setOnClickListener(v -> onClickListener.onEventClick(event));
        }
    }

    /**
     * DiffUtil callback for comparing {@link Event} objects by Firestore ID.
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
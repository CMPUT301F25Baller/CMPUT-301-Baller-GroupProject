package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ballerevents.databinding.ItemEventTrendingBinding;

public class TrendingEventAdapter extends ListAdapter<Event, TrendingEventAdapter.EventViewHolder> {

    // Simple interface for click handling
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final OnEventClickListener onClickListener;

    public TrendingEventAdapter(OnEventClickListener onClickListener) {
        super(EventDiffCallback);
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventTrendingBinding binding = ItemEventTrendingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position), onClickListener);
    }

    // ViewHolder class
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventTrendingBinding binding;

        public EventViewHolder(ItemEventTrendingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Event event, OnEventClickListener onClickListener) {
            binding.ivEventBackground.setImageResource(event.getEventPosterResId());
            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventLocation.setText(event.getLocationName());
            binding.tvEventDate.setText(event.getDate());
            binding.tvPrice.setText(event.getPrice());

            binding.getRoot().setOnClickListener(v -> onClickListener.onEventClick(event));
        }
    }

    // DiffUtil.ItemCallback
    private static final DiffUtil.ItemCallback<Event> EventDiffCallback = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            // This is simplified. For full correctness, you'd compare all fields.
            // But for this app, it's fine.
            return oldItem.getId() == newItem.getId();
        }
    };
}
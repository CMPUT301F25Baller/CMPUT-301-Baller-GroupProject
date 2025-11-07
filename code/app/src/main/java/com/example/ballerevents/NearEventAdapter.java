package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ballerevents.databinding.ItemEventNearBinding;

public class NearEventAdapter extends ListAdapter<Event, NearEventAdapter.EventViewHolder> {

    // Simple interface for click handling
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final OnEventClickListener onClickListener;

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

    // ViewHolder class
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventNearBinding binding;

        public EventViewHolder(ItemEventNearBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Event event, OnEventClickListener onClickListener) {
            binding.ivEventImage.setImageResource(event.getEventPosterResId());
            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventLocation.setText(event.getLocationName());
            binding.tvEventDate.setText(event.getDate());
            binding.tvEventPrice.setText(event.getPrice());

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
            return oldItem.getId() == newItem.getId();
        }
    };
}
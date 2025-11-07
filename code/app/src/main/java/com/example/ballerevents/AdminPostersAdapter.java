package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemAdminPosterBinding;

public class AdminPostersAdapter extends ListAdapter<Event, AdminPostersAdapter.VH> {

    public interface OnPosterClickListener {
        void onPosterClick(Event event);
    }

    private final OnPosterClickListener clickListener;

    public AdminPostersAdapter(OnPosterClickListener listener) {
        super(EventDiffCallback);
        this.clickListener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminPosterBinding b;
        VH(ItemAdminPosterBinding b) { super(b.getRoot()); this.b = b; }

        void bind(Event event, OnPosterClickListener listener) {
            b.tvLabel.setText(event.getTitle());
            Glide.with(itemView.getContext())
                    .load(event.getEventPosterUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(b.ivPoster);
            itemView.setOnClickListener(v -> listener.onPosterClick(event));
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminPosterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Event event = getItem(pos);
        if (event != null) {
            h.bind(event, clickListener);
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
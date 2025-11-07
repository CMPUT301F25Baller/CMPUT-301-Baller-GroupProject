package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class AdminImagesAdapter extends ListAdapter<Event, AdminImagesAdapter.VH> {

    interface ImageActions {
        void onPreview(Event event);
        void onDelete(Event event);
    }

    private final ImageActions actions;

    public AdminImagesAdapter(ImageActions actions) {
        super(EventDiffCallback);
        this.actions = actions;
        setHasStableIds(true);
    }

    @Override public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvLabel;
        VH(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_poster, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Event event = getItem(pos);
        if (event == null) return;

        Glide.with(h.itemView.getContext())
                .load(event.getEventPosterUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(h.ivPoster);

        h.tvLabel.setText(event.getTitle());

        h.itemView.setOnClickListener(v -> { if (actions != null) actions.onPreview(event); });
        h.itemView.setOnLongClickListener(v -> {
            if (actions != null) actions.onDelete(event);
            return true;
        });
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
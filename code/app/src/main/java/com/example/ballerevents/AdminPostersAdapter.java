package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemAdminPosterBinding;

/**
 * RecyclerView adapter used on the Admin Dashboard and Admin Images screen
 * to display event poster thumbnails.
 *
 * <p>This adapter binds each {@link Event}'s poster URL and title into a
 * compact visual layout. Glide is used for efficient image loading and caching.</p>
 */
public class AdminPostersAdapter extends ListAdapter<Event, AdminPostersAdapter.VH> {

    /**
     * Callback interface for poster click actions.
     * Implemented by the parent Activity or Fragment.
     */
    public interface OnPosterClickListener {
        /**
         * Called when the user taps a poster thumbnail.
         * @param event The event associated with the clicked poster.
         */
        void onPosterClick(Event event);
    }

    private final OnPosterClickListener clickListener;

    /**
     * Constructs the adapter with a required click listener.
     *
     * @param listener Callback invoked when a poster is clicked.
     */
    public AdminPostersAdapter(OnPosterClickListener listener) {
        super(EventDiffCallback);
        this.clickListener = listener;
    }

    /**
     * ViewHolder that binds the poster image and label using view binding.
     */
    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminPosterBinding b;

        VH(ItemAdminPosterBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        /**
         * Binds a single {@link Event} to the row layout.
         *
         * @param event    The event whose poster is being shown.
         * @param listener Callback to notify when poster is tapped.
         */
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

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(
                ItemAdminPosterBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Event event = getItem(position);
        if (event != null) {
            holder.bind(event, clickListener);
        }
    }

    /**
     * DiffUtil callback that identifies items by Firestore document ID.
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
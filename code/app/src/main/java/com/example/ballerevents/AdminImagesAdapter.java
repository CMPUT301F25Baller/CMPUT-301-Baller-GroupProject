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

/**
 * Adapter for displaying event posters in the Admin Images gallery.
 *
 * <p>This adapter renders a grid of event posters, each showing:
 * <ul>
 *   <li>a thumbnail loaded via Glide</li>
 *   <li>a text label (event title)</li>
 * </ul>
 *
 * <p>User interactions:</p>
 * <ul>
 *     <li><b>Click</b> → preview the poster in a dialog</li>
 *     <li><b>Long-press</b> → prompt to delete (clear poster URL)</li>
 * </ul>
 *
 * <p>The adapter uses {@link DiffUtil} for efficient list updates and supports stable IDs.</p>
 */
public class AdminImagesAdapter extends ListAdapter<Event, AdminImagesAdapter.VH> {

    /**
     * Callback interface implemented by {@link com.example.ballerevents.AdminImagesActivity}.
     * Provides actions for previewing or deleting a poster image.
     */
    interface ImageActions {
        /**
         * Called when the user taps a poster thumbnail.
         *
         * @param event the associated event whose poster should be previewed
         */
        void onPreview(Event event);

        /**
         * Called when the user long-presses a poster thumbnail.
         *
         * @param event the event whose poster URL should be cleared
         */
        void onDelete(Event event);
    }

    /** Listener for preview/delete actions. */
    private final ImageActions actions;

    /**
     * Creates a new adapter instance.
     *
     * @param actions callback handler for user interactions
     */
    public AdminImagesAdapter(ImageActions actions) {
        super(EventDiffCallback);
        this.actions = actions;
        setHasStableIds(true);
    }

    /**
     * Provides stable item IDs based on the Firestore event ID hash.
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }

    /**
     * ViewHolder containing references to poster thumbnail and label.
     */
    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvLabel;

        /**
         * @param itemView inflated row layout for item_admin_poster.xml
         */
        VH(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }

    /**
     * Inflates the poster grid item layout.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_poster, parent, false);
        return new VH(v);
    }

    /**
     * Binds the poster image and title, and wires up click/long-press interactions.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Event event = getItem(pos);
        if (event == null) return;

        // Load poster thumbnail
        Glide.with(h.itemView.getContext())
                .load(event.getEventPosterUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(h.ivPoster);

        // Label under the image
        h.tvLabel.setText(event.getTitle());

        // Click → preview
        h.itemView.setOnClickListener(v -> {
            if (actions != null) actions.onPreview(event);
        });

        // Long press → delete
        h.itemView.setOnLongClickListener(v -> {
            if (actions != null) actions.onDelete(event);
            return true;
        });
    }

    /**
     * DiffUtil callback based on Firestore document ID.
     * Only the ID is checked for changes since posters rarely change
     * and contents are minimal.
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

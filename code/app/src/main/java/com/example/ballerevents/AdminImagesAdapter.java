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
 * This allows the administrator to browse images uploaded by organizers and remove them if necessary[cite: 105, 108].
 *
 * <p>User interactions:</p>
 * <ul>
 * <li><b>Click:</b> Previews the poster in a dialog.</li>
 * <li><b>Long-press:</b> Prompts to delete (clear) the poster URL.</li>
 * </ul>
 */
public class AdminImagesAdapter extends ListAdapter<Event, AdminImagesAdapter.VH> {

    /**
     * Callback interface implemented by {@link AdminImagesActivity}.
     * Provides actions for previewing or deleting a poster image.
     */
    interface ImageActions {
        /**
         * Called when the user taps a poster thumbnail.
         *
         * @param event The associated event whose poster should be previewed.
         */
        void onPreview(Event event);

        /**
         * Called when the user long-presses a poster thumbnail.
         *
         * @param event The event whose poster URL should be cleared.
         */
        void onDelete(Event event);
    }

    private final ImageActions actions;

    /**
     * Creates a new adapter instance.
     *
     * @param actions Callback handler for user interactions.
     */
    public AdminImagesAdapter(ImageActions actions) {
        super(EventDiffCallback);
        this.actions = actions;
        setHasStableIds(true);
    }

    /**
     * Provides stable item IDs based on the Firestore event ID hash.
     *
     * @param position The position of the item within the adapter's data set.
     * @return A unique long identifier for the item.
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

        VH(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }

    /**
     * Inflates the poster grid item layout.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new VH that holds the View for each poster.
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
     *
     * @param h   The ViewHolder which should be updated to represent the contents of the item.
     * @param pos The position of the item within the adapter's data set.
     */
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

        h.itemView.setOnClickListener(v -> {
            if (actions != null) actions.onPreview(event);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (actions != null) actions.onDelete(event);
            return true;
        });
    }

    /**
     * DiffUtil callback based on Firestore document ID.
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
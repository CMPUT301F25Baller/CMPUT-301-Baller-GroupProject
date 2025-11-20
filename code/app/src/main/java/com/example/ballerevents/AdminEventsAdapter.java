package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

/**
 * Adapter for displaying event rows in the Admin "All Events" list.
 *
 * <p>This adapter renders a poster thumbnail, title, date, and location for each event.
 * It also provides an overflow menu (three dots) for admin actions such as deleting
 * an event. Actions are forwarded to the hosting Activity via
 * {@link AdminEventsAdapter.OnEventActionListener}.
 *
 * <p>Image loading is performed using Glide, and the adapter relies on a {@link DiffUtil}
 * callback for efficient list updates.
 */
public class AdminEventsAdapter extends ListAdapter<Event, AdminEventsAdapter.EventViewHolder> {

    /**
     * Listener interface for handling row-level actions such as delete.
     */
    public interface OnEventActionListener {
        /**
         * Called when the admin selects the "Delete" action for an event.
         *
         * @param event the event associated with the action
         */
        void onDelete(Event event);
    }

    /** Callback target for row actions. */
    private final OnEventActionListener listener;

    /**
     * Constructs the adapter.
     *
     * @param listener callback for admin actions such as delete
     */
    public AdminEventsAdapter(OnEventActionListener listener) {
        super(EventDiffCallback);
        this.listener = listener;
    }

    /**
     * Inflates {@code item_admin_event.xml} for each row.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(v);
    }

    /**
     * Binds event data (title, date, location, poster image) and wires up the overflow menu.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder h, int position) {
        Event e = getItem(position);
        if (e == null) return;

        // Basic fields
        h.tvTitle.setText(e.getTitle());
        h.tvDate.setText(e.getDate());
        h.tvLocation.setText(e.getLocationName());

        // Glide: load poster URL
        Glide.with(h.itemView.getContext())
                .load(e.getEventPosterUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(h.ivPoster);

        // Overflow menu (delete action)
        h.ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_admin_event_row, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> handleMenu(item, e));
            popup.show();
        });
    }

    /**
     * Handles overflow menu item clicks.
     *
     * @param item the menu item clicked
     * @param e    the associated event
     * @return true if handled
     */
    private boolean handleMenu(MenuItem item, Event e) {
        if (item.getItemId() == R.id.action_delete) {
            listener.onDelete(e);
            return true;
        }
        return false;
    }

    /**
     * ViewHolder containing references to all row UI components.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardRoot;
        ImageView ivPoster, ivMenu;
        TextView tvTitle, tvDate, tvLocation;

        /**
         * Binds view references on construction.
         *
         * @param itemView the inflated row view
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot   = itemView.findViewById(R.id.card_root);
            ivPoster   = itemView.findViewById(R.id.ivPoster);
            ivMenu     = itemView.findViewById(R.id.ivMenu);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvDate     = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }

    /**
     * DiffUtil callback that checks item identity and content equality
     * based solely on event IDs, which uniquely identify Firestore docs.
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

package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying notification rows. Each item represents a
 * single notification with a sender, message content, timestamp, and optional
 * action buttons (e.g., "Mark as Read" and "Open").
 *
 * <p>This adapter uses a simple internal list and does not use DiffUtil, as
 * most notification displays do not require complex item animations.</p>
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    /**
     * Represents the UI model for a single notification row.
     * <p>
     * Includes metadata about sender, content, time, avatar image, and whether
     * additional actions should be displayed.
     * </p>
     */
    public static class NotificationItem {
        /** Name or identifier of the sender. */
        public final String sender;
        /** Notification body/content text. */
        public final String message;
        /** Display-friendly timestamp (e.g., "Just now"). */
        public final String time;
        /** Drawable resource ID for the sender avatar. */
        public final int avatarResId;
        /** Whether this notification row should display action buttons. */
        public final boolean hasActions;

        /**
         * Creates a notification UI model.
         *
         * @param sender     Name of notification source.
         * @param message    Notification content text.
         * @param time       Display timestamp string.
         * @param avatarResId Drawable resource for avatar.
         * @param hasActions  Whether to show action buttons.
         */
        public NotificationItem(String sender, String message, String time, int avatarResId, boolean hasActions) {
            this.sender = sender;
            this.message = message;
            this.time = time;
            this.avatarResId = avatarResId;
            this.hasActions = hasActions;
        }
    }

    /** Backing list used for rendering notification rows. */
    private final List<NotificationItem> items = new ArrayList<>();

    /** Optional listener for notification action callbacks. */
    private OnActionListener onActionListener;

    /**
     * Listener interface for action button interactions.
     */
    public interface OnActionListener {
        /**
         * Invoked when the "Mark as Read" button is pressed.
         *
         * @param position Adapter position.
         * @param item     Notification item associated with the action.
         */
        void onMarkRead(int position, NotificationItem item);

        /**
         * Invoked when the "Open" button is pressed.
         *
         * @param position Adapter position.
         * @param item     Notification item associated with the action.
         */
        void onOpen(int position, NotificationItem item);
    }

    /**
     * Assigns a listener for notification row actions.
     *
     * @param l Listener implementation.
     */
    public void setOnActionListener(OnActionListener l) { this.onActionListener = l; }

    /**
     * Replaces the current list of notifications with a new one.
     * <p>
     * This method clears internal data and calls {@link #notifyDataSetChanged()},
     * making it suitable for moderate-sized notification lists.
     * </p>
     *
     * @param newItems A new list of {@link NotificationItem} objects.
     */
    public void submitList(List<NotificationItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem it = items.get(position);

        h.tvSender.setText(it.sender);
        h.tvMessage.setText(it.message);
        h.tvTime.setText(it.time);
        h.ivAvatar.setImageResource(
                it.avatarResId != 0 ? it.avatarResId : R.drawable.placeholder_avatar1
        );

        // Show or hide action row
        h.actionsRow.setVisibility(it.hasActions ? View.VISIBLE : View.GONE);

        h.btnMarkRead.setOnClickListener(v -> {
            if (onActionListener != null) {
                onActionListener.onMarkRead(h.getBindingAdapterPosition(), it);
            }
        });

        h.btnOpen.setOnClickListener(v -> {
            if (onActionListener != null) {
                onActionListener.onOpen(h.getBindingAdapterPosition(), it);
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    /**
     * ViewHolder for a single notification row, holding references to the avatar,
     * text fields, and optional action buttons.
     */
    static class VH extends RecyclerView.ViewHolder {

        /** Sender's avatar image view. */
        final ShapeableImageView ivAvatar;
        /** Sender name text. */
        final TextView tvSender;
        /** Message text body. */
        final TextView tvMessage;
        /** Timestamp text (e.g., "2m ago"). */
        final TextView tvTime;
        /** Row containing "Mark as Read" and "Open" buttons. */
        final LinearLayout actionsRow;
        /** Action button for marking a notification as read. */
        final Button btnMarkRead;
        /** Action button for opening the notification. */
        final Button btnOpen;

        /**
         * Constructs a ViewHolder around the notification row layout.
         *
         * @param itemView The root inflated layout view.
         */
        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar    = itemView.findViewById(R.id.ivAvatar);
            tvSender    = itemView.findViewById(R.id.tvSender);
            tvMessage   = itemView.findViewById(R.id.tvMessage);
            tvTime      = itemView.findViewById(R.id.tvTime);
            actionsRow  = itemView.findViewById(R.id.actionsRow);
            btnMarkRead = itemView.findViewById(R.id.btnMarkRead);
            btnOpen     = itemView.findViewById(R.id.btnOpen);
        }
    }
}

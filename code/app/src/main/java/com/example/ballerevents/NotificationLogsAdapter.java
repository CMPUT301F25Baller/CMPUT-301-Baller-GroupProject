package com.example.ballerevents;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter for displaying a list of {@link NotificationLog} entries.
 * <p>
 * Uses {@link ListAdapter} with {@link DiffUtil} to efficiently update rows when
 * notifications are marked as read or modified. Each row includes an avatar,
 * title, timestamp, unread indicator dot, and action buttons.
 * </p>
 */
public class NotificationLogsAdapter
        extends ListAdapter<NotificationLog, NotificationLogsAdapter.VH> {

    /**
     * Listener interface for notification log actions.
     */
    public interface OnItemAction {
        /**
         * Triggered when the user marks a log item as read.
         *
         * @param log The notification entry being acted upon.
         */
        void onMarkRead(NotificationLog log);

        /**
         * Triggered when the user opens a log item for more details.
         *
         * @param log The clicked notification entry.
         */
        void onOpen(NotificationLog log);

        // New actions for Invitations
        // Actions for Invitations
        void onAccept(NotificationLog log);
        void onDecline(NotificationLog log);
    }

    /** Action listener instance for handling row-level interactions. */
    private final OnItemAction actions;

    /**
     * Constructs a new adapter instance.
     *
     * @param actions Callback interface handling row click events.
     */
    public NotificationLogsAdapter(OnItemAction actions) {
        super(DIFF_CALLBACK);
        this.actions = actions;
    }

    /**
     * DiffUtil callback for comparing {@link NotificationLog} entries.
     * <p>
     * Items are considered the same based on their unique {@code id}.
     * Contents are compared based on read state, title, timestamp, and avatar resource.
     * </p>
     */
    private static final DiffUtil.ItemCallback<NotificationLog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NotificationLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLog oldItem, @NonNull NotificationLog newItem) {
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationLog oldItem, @NonNull NotificationLog newItem) {
                    return oldItem.isRead == newItem.isRead &&
                            oldItem.title.equals(newItem.title) &&
                            oldItem.isInvitation == newItem.isInvitation;
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Uses the same item layout as the standard notification adapter
        // Ensure "item_notification_row" matches your actual XML file name
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationLog n = getItem(position);

        h.tvMessage.setText(n.title); // Mapping title to main message view
        h.tvTime.setText(n.timestamp);
        h.ivAvatar.setImageResource(n.avatarRes);

        // Logic to switch between "Mark Read" vs "Accept/Decline"
        if (n.isInvitation) {
            // -- INVITATION MODE --
            h.btnOpen.setText("Accept");
            h.btnOpen.setTextColor(Color.parseColor("#4CAF50")); // Green for Accept
            h.btnOpen.setVisibility(View.VISIBLE);
            h.btnOpen.setOnClickListener(v -> actions.onAccept(n));

            h.btnMarkRead.setText("Decline");
            h.btnMarkRead.setTextColor(Color.parseColor("#F44336")); // Red for Decline
            h.btnMarkRead.setVisibility(View.VISIBLE);
            h.btnMarkRead.setOnClickListener(v -> actions.onDecline(n));

        } else {
            // -- STANDARD NOTIFICATION MODE --
            h.btnOpen.setText("Open");
            h.btnOpen.setTextColor(Color.GRAY);
            h.btnOpen.setVisibility(View.VISIBLE);
            h.btnOpen.setOnClickListener(v -> actions.onOpen(n));

            h.btnMarkRead.setText("Mark Read");
            h.btnMarkRead.setTextColor(Color.GRAY);
            // Hide "Mark Read" if already read to clean up UI, or keep it disabled
            h.btnMarkRead.setVisibility(n.isRead ? View.GONE : View.VISIBLE);
            h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(n));
        }

        // Show dot only if unread
        if (h.unreadDot != null) {
            h.unreadDot.setVisibility(n.isRead ? View.INVISIBLE : View.VISIBLE);
        }
    }

    /**
     * ViewHolder for a notification log row. Holds references to all views in
     * {@code row_notification_log.xml}.
     */
    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView tvMessage;
        final TextView tvTime;
        final Button btnMarkRead; // Used as "Decline" or "Mark Read"
        final Button btnOpen;     // Used as "Accept" or "Open"
        final View actionsRow;
        final View unreadDot;

        /**
         * Constructs a ViewHolder for the log row layout.
         *
         * @param v Root inflated row view.
         */
        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvMessage = v.findViewById(R.id.tvMessage); // Note: xml likely uses tvMessage
            tvTime = v.findViewById(R.id.tvTime);
            btnMarkRead = v.findViewById(R.id.btnMarkRead);
            btnOpen = v.findViewById(R.id.btnOpen);
            actionsRow = v.findViewById(R.id.actionsRow);
            unreadDot = v.findViewById(R.id.unreadDot); // Bind the dot view
        }
    }
}

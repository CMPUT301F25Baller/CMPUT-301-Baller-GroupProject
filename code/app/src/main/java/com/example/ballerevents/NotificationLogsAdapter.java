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
    }

    /** Action listener instance for handling row-level interactions. */
    private final OnItemAction actions;

    /**
     * Constructs a new adapter instance.
     *
     * @param actions Callback interface handling row click events.
     */
    public NotificationLogsAdapter(OnItemAction actions) {
        super(DIFF);
        this.actions = actions;
    }

    /**
     * DiffUtil callback for comparing {@link NotificationLog} entries.
     * <p>
     * Items are considered the same based on their unique {@code id}.
     * Contents are compared based on read state, title, timestamp, and avatar resource.
     * </p>
     */
    private static final DiffUtil.ItemCallback<NotificationLog> DIFF =
            new DiffUtil.ItemCallback<NotificationLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    return a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    return a.isRead == b.isRead
                            && a.title.equals(b.title)
                            && a.timestamp.equals(b.timestamp)
                            && a.avatarRes == b.avatarRes;
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_notification_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationLog n = getItem(position);

        h.ivAvatar.setImageResource(n.avatarRes);
        h.tvTitle.setText(n.title);
        h.tvTime.setText(n.timestamp);

        // Show unread indicator dot only if not read
        h.unreadDot.setVisibility(n.isRead ? View.INVISIBLE : View.VISIBLE);

        // Bind row actions
        h.btnOpen.setOnClickListener(v -> actions.onOpen(n));
        h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(n));
        h.itemView.setOnClickListener(v -> actions.onOpen(n));
    }

    /**
     * ViewHolder for a notification log row. Holds references to all views in
     * {@code row_notification_log.xml}.
     */
    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View unreadDot;
        TextView tvTitle, tvTime, btnOpen, btnMarkRead;

        /**
         * Constructs a ViewHolder for the log row layout.
         *
         * @param v Root inflated row view.
         */
        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            unreadDot = v.findViewById(R.id.unreadDot);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvTime = v.findViewById(R.id.tvTime);
            btnOpen = v.findViewById(R.id.btnOpen);
            btnMarkRead = v.findViewById(R.id.btnMarkRead);
        }
    }
}

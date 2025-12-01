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
 * Supports standard notifications AND invitation-style notifications.
 */
public class NotificationLogsAdapter
        extends ListAdapter<NotificationLog, NotificationLogsAdapter.VH> {

    /** Listener interface for notification log actions. */
    public interface OnItemAction {
        void onMarkRead(NotificationLog log);
        void onOpen(NotificationLog log);
        void onAcceptInvite(NotificationLog log);
        void onRejectInvite(NotificationLog log);
    }

    private final OnItemAction actions;

    public NotificationLogsAdapter(OnItemAction actions) {
        super(DIFF_CALLBACK);
        this.actions = actions;
    }

    /** Improved diff callback merging both versions */
    private static final DiffUtil.ItemCallback<NotificationLog> DIFF_CALLBACK =
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
                .inflate(R.layout.item_notification_row, parent, false); // Keep your layout
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationLog n = getItem(position);

        h.ivAvatar.setImageResource(n.avatarRes);
        h.tvMessage.setText(n.title);
        h.tvTime.setText(n.timestamp);

        // Invitation type notification
        if (n.isInvitation) {
            h.btnMarkRead.setText("Accept");
            h.btnOpen.setText("Reject");

            h.btnMarkRead.setTextColor(Color.parseColor("#4CAF50"));
            h.btnOpen.setTextColor(Color.parseColor("#F44336"));

            h.btnMarkRead.setOnClickListener(v -> actions.onAcceptInvite(n));
            h.btnOpen.setOnClickListener(v -> actions.onRejectInvite(n));

            h.unreadDot.setVisibility(View.VISIBLE);
            h.actionsRow.setVisibility(View.VISIBLE);

        } else {
            // Regular notification
            h.btnMarkRead.setText("Mark Read");
            h.btnOpen.setText("Open");

            h.btnMarkRead.setTextColor(Color.GRAY);
            h.btnOpen.setTextColor(Color.GRAY);

            h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(n));
            h.btnOpen.setOnClickListener(v -> actions.onOpen(n));

            // show unread indicator dot
            h.unreadDot.setVisibility(n.isRead ? View.INVISIBLE : View.VISIBLE);
        }

        // Entire row opens notification (from the main branch)
        h.itemView.setOnClickListener(v -> actions.onOpen(n));
    }

    /** ViewHolder for notification rows */
    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView tvMessage;
        final TextView tvTime;
        final Button btnMarkRead, btnOpen;
        final View actionsRow, unreadDot;

        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            btnMarkRead = v.findViewById(R.id.btnMarkRead);
            btnOpen = v.findViewById(R.id.btnOpen);
            actionsRow = v.findViewById(R.id.actionsRow);
            unreadDot = v.findViewById(R.id.unreadDot);
        }
    }
}

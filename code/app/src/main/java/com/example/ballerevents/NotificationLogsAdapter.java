package com.example.ballerevents;

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

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationLogsAdapter
        extends ListAdapter<Notification, NotificationLogsAdapter.VH> {

    public interface OnItemAction {
        void onMarkRead(Notification notif);
        void onOpen(Notification notif);
        void onAcceptInvite(Notification notif);
        void onDeclineInvite(Notification notif);
        void onDelete(Notification notif);
        default void onFollowBack(Notification notif) {}
    }

    private final OnItemAction actions;
    private final boolean isAdminView; // NEW FLAG

    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    /**
     * Constructor.
     * @param actions Action listener
     * @param isAdminView If true, hides all interactive buttons (Accept/Decline/Follow).
     */
    public NotificationLogsAdapter(OnItemAction actions, boolean isAdminView) {
        super(DIFF);
        this.actions = actions;
        this.isAdminView = isAdminView;
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF =
            new DiffUtil.ItemCallback<Notification>() {
                @Override
                public boolean areItemsTheSame(@NonNull Notification a, @NonNull Notification b) {
                    return a.getId().equals(b.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Notification a, @NonNull Notification b) {
                    return a.isRead() == b.isRead()
                            && a.getMessage().equals(b.getMessage());
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = getItem(position);

        h.ivAvatar.setImageResource(R.drawable.ic_notification_alert);
        h.tvMessage.setText(n.getMessage());
        h.tvTitle.setText(n.getTitle() == null ? "Notification" : n.getTitle());

        if (n.getTimestamp() != null) {
            h.tvTime.setText(sdf.format(n.getTimestamp()));
        } else {
            h.tvTime.setText("–");
        }

        // --- NEW LOGIC: If Admin, Hide EVERYTHING interactive ---
        if (isAdminView) {
            h.actionsRow.setVisibility(View.GONE);
            h.btnMarkRead.setVisibility(View.GONE);
            h.unreadDot.setVisibility(View.GONE); // Admins don't need to see read/unread state visually

            // Still allow clicking the row to see details (handled by onOpen)
            h.itemView.setOnClickListener(v -> actions.onOpen(n));
            return; // Stop here for Admin
        }
        // --------------------------------------------------------

        String type = n.getType() != null ? n.getType() : "";
        boolean invite = "invitation".equalsIgnoreCase(type);
        boolean follower = "new_follower".equalsIgnoreCase(type);

        h.actionsRow.setVisibility(View.GONE);
        h.btnMarkRead.setVisibility(View.GONE);

        if (invite) {
            h.actionsRow.setVisibility(View.VISIBLE);
            h.btnAccept.setText("Accept");
            h.btnAccept.setVisibility(View.VISIBLE);

            h.btnDecline.setVisibility(View.VISIBLE);
            h.btnDecline.setText("Decline");

            h.btnAccept.setOnClickListener(v -> actions.onAcceptInvite(n));
            h.btnDecline.setOnClickListener(v -> actions.onDeclineInvite(n));

        } else if (follower) {
            h.actionsRow.setVisibility(View.VISIBLE);
            h.btnAccept.setText("Follow Back");
            h.btnAccept.setVisibility(View.VISIBLE);
            h.btnDecline.setVisibility(View.GONE);

            h.btnAccept.setOnClickListener(v -> actions.onFollowBack(n));

        } else {
            h.btnMarkRead.setVisibility(View.VISIBLE);
            h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(n));
        }

        h.unreadDot.setVisibility(n.isRead() ? View.INVISIBLE : View.VISIBLE);

        h.itemView.setOnClickListener(v -> actions.onOpen(n));
        h.itemView.setOnLongClickListener(v -> {
            actions.onDelete(n);
            return true;
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvTitle, tvMessage, tvTime;
        View unreadDot, actionsRow;
        Button btnMarkRead, btnAccept, btnDecline;

        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvTitle = v.findViewById(R.id.tvSender);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            unreadDot = v.findViewById(R.id.unreadDot);
            actionsRow = v.findViewById(R.id.actionsRow);
            btnMarkRead = v.findViewById(R.id.btnMarkRead);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline = v.findViewById(R.id.btnDecline);
        }
    }
}
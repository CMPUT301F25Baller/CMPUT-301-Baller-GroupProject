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
    }

    private final OnItemAction actions;
    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public NotificationLogsAdapter(OnItemAction actions) {
        super(DIFF);
        this.actions = actions;
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

        boolean invite = "invitation".equalsIgnoreCase(n.getType());
        h.actionsRow.setVisibility(invite ? View.VISIBLE : View.GONE);

        if (invite) {
            h.btnAccept.setOnClickListener(v -> actions.onAcceptInvite(n));
            h.btnDecline.setOnClickListener(v -> actions.onDeclineInvite(n));
        }

        h.unreadDot.setVisibility(n.isRead() ? View.INVISIBLE : View.VISIBLE);

        h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(n));
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
            btnAccept = v.findViewById(R.id.btnMarkRead);
            btnDecline = v.findViewById(R.id.btnOpen);
        }
    }
}

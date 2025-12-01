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

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adapter that supports two item types:
 * - Notification  (Firestore)
 * - NotificationLog (static + invite)
 */
public class NotificationLogsAdapter
        extends ListAdapter<Object, NotificationLogsAdapter.VH> {

    public interface OnItemAction {
        void onMarkRead(Object item);
        void onOpen(Object item);
        void onAcceptInvite(Object item);
        void onRejectInvite(Object item);
    }

    private final OnItemAction actions;

    // Formatter for Notification timestamps (Date -> String)
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public NotificationLogsAdapter(OnItemAction actions) {
        super(DIFF_CALLBACK);
        this.actions = actions;
    }

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Object>() {
                @Override
                public boolean areItemsTheSame(@NonNull Object a, @NonNull Object b) {
                    if (a instanceof Notification && b instanceof Notification) {
                        return ((Notification) a).getId().equals(((Notification) b).getId());
                    }
                    if (a instanceof NotificationLog && b instanceof NotificationLog) {
                        return ((NotificationLog) a).id.equals(((NotificationLog) b).id);
                    }
                    return false;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Object a, @NonNull Object b) {
                    return a.equals(b);
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Object item = getItem(position);

        // --------- Firestore Notification ---------
        if (item instanceof Notification) {
            Notification notif = (Notification) item;

            h.ivAvatar.setImageResource(R.drawable.ic_notification_alert);

            // You can choose title or message here depending on UI design.
            // Using message for main body:
            h.tvMessage.setText(notif.getMessage());

            // FIX: format Date -> String
            if (notif.getTimestamp() != null) {
                h.tvTime.setText(timeFormat.format(notif.getTimestamp()));
            } else {
                h.tvTime.setText("–");
            }

            h.unreadDot.setVisibility(notif.isRead() ? View.INVISIBLE : View.VISIBLE);

            h.btnMarkRead.setText("Mark Read");
            h.btnOpen.setText("Open");

            h.btnMarkRead.setTextColor(Color.BLACK);
            h.btnOpen.setTextColor(Color.BLACK);

            h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(notif));
            h.btnOpen.setOnClickListener(v -> actions.onOpen(notif));

            h.itemView.setOnClickListener(v -> actions.onOpen(notif));
            return;
        }

        // --------- Static NotificationLog (e.g., demo / invite) ---------
        if (item instanceof NotificationLog) {
            NotificationLog log = (NotificationLog) item;

            h.ivAvatar.setImageResource(log.avatarRes);
            h.tvMessage.setText(log.title);
            h.tvTime.setText(log.timestamp);   // already a String

            if (log.isInvitation) {
                h.unreadDot.setVisibility(View.VISIBLE);

                h.btnMarkRead.setText("Accept");
                h.btnOpen.setText("Reject");

                h.btnMarkRead.setTextColor(Color.parseColor("#4CAF50"));
                h.btnOpen.setTextColor(Color.parseColor("#F44336"));

                h.btnMarkRead.setOnClickListener(v -> actions.onAcceptInvite(log));
                h.btnOpen.setOnClickListener(v -> actions.onRejectInvite(log));
            } else {
                h.unreadDot.setVisibility(log.isRead ? View.INVISIBLE : View.VISIBLE);

                h.btnMarkRead.setText("Mark Read");
                h.btnOpen.setText("Open");

                h.btnMarkRead.setTextColor(Color.BLACK);
                h.btnOpen.setTextColor(Color.BLACK);

                h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(log));
                h.btnOpen.setOnClickListener(v -> actions.onOpen(log));
            }

            h.itemView.setOnClickListener(v -> actions.onOpen(log));
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvMessage, tvTime;
        Button btnMarkRead, btnOpen;
        View unreadDot;

        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            btnMarkRead = v.findViewById(R.id.btnMarkRead);
            btnOpen = v.findViewById(R.id.btnOpen);
            unreadDot = v.findViewById(R.id.unreadDot);
        }
    }
}

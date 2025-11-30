package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;

public class NotificationLogsAdapter
        extends ListAdapter<NotificationLog, NotificationLogsAdapter.VH> {

    public NotificationLogsAdapter() { super(DIFF); }

    private static final DiffUtil.ItemCallback<NotificationLog> DIFF =
            new DiffUtil.ItemCallback<NotificationLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    return a.id != null && a.id.equals(b.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    boolean sameTime = (a.timestamp == null && b.timestamp == null)
                            || (a.timestamp != null && b.timestamp != null && a.timestamp.equals(b.timestamp));
                    return a.read == b.read
                            && eq(a.message, b.message)
                            && eq(a.type, b.type)
                            && eq(a.eventTitle, b.eventTitle)
                            && eq(a.senderName, b.senderName)
                            && eq(a.recipientName, b.recipientName)
                            && sameTime;
                }
                private boolean eq(Object x, Object y) { return (x == y) || (x != null && x.equals(y)); }
            };

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSub, tvMeta, tvBadge;
        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvSub   = v.findViewById(R.id.tvSub);
            tvMeta  = v.findViewById(R.id.tvMeta);
            tvBadge = v.findViewById(R.id.tvBadge);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false); // singular
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        NotificationLog n = getItem(pos);

        String title = n.type != null ? n.type : "Notification";
        if (n.eventTitle != null && !n.eventTitle.isEmpty()) title += " • " + n.eventTitle;
        h.tvTitle.setText(title);

        h.tvSub.setText(n.message != null ? n.message : "");

        String who = (n.senderName != null ? n.senderName : "Organizer")
                + " → " + (n.recipientName != null ? n.recipientName : "Entrant");
        String when = n.timestamp != null
                ? DateFormat.getDateTimeInstance().format(n.timestamp)
                : "";
        h.tvMeta.setText(who + (when.isEmpty() ? "" : " • " + when));

        h.tvBadge.setVisibility(n.read ? View.GONE : View.VISIBLE);
        h.tvBadge.setText("NEW");
    }
}

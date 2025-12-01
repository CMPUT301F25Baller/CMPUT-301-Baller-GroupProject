package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView adapter for admin notification logs.
 * Renders each log using item_notification_log.xml.
 */
public class NotificationLogsAdapter extends ListAdapter<NotificationLog, NotificationLogsAdapter.VH> {

    public interface Callbacks {
        void onOpen(NotificationLog log);
        void onMarkRead(NotificationLog log);
    }

    private final Callbacks callbacks;
    private final DateFormat timeFmt = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public NotificationLogsAdapter(Callbacks callbacks) {
        super(DIFF);
        this.callbacks = callbacks;
    }

    private static final DiffUtil.ItemCallback<NotificationLog> DIFF =
            new DiffUtil.ItemCallback<NotificationLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    return a.getId() != null && a.getId().equals(b.getId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    return a.isRead() == b.isRead()
                            && safe(a.getTitle()).equals(safe(b.getTitle()))
                            && safe(a.getMessage()).equals(safe(b.getMessage()))
                            && ts(a.getTimestamp()) == ts(b.getTimestamp());
                }
                private String safe(String s) { return s == null ? "" : s; }
                private long ts(Timestamp t) { return t == null ? 0L : t.toDate().getTime(); }
            };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationLog log = getItem(position);

        h.tvTitle.setText(log.safeTitle());

        StringBuilder body = new StringBuilder();
        if (log.getOrganizerName() != null && !log.getOrganizerName().isEmpty()) {
            body.append("From ").append(log.getOrganizerName()).append(" • ");
        }
        if (log.getRecipientName() != null && !log.getRecipientName().isEmpty()) {
            body.append("To ").append(log.getRecipientName()).append(" • ");
        }
        if (log.getEventTitle() != null && !log.getEventTitle().isEmpty()) {
            body.append(log.getEventTitle()).append(" • ");
        }
        body.append(log.safeMessage());
        h.tvMessage.setText(body.toString());

        Date d = log.getTimestamp() == null ? new Date(0) : log.getTimestamp().toDate();
        h.tvTime.setText(timeFmt.format(d));

        // Show actions only for invitation-type items that aren't read (optional UX)
        boolean showActions = !log.isRead() && "INVITE".equalsIgnoreCase(log.getType());
        h.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);

        h.btnAccept.setOnClickListener(v -> callbacks.onOpen(log));
        h.btnDecline.setOnClickListener(v -> callbacks.onMarkRead(log));
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View layoutActions;
        Button btnAccept, btnDecline;
        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            layoutActions = v.findViewById(R.id.layoutActions);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline = v.findViewById(R.id.btnDecline);
        }
    }
}

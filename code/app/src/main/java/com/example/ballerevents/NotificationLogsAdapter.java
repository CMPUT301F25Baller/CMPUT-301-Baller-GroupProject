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

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adapter for displaying Notifications.
 * Handles "invitation" types by showing Accept/Decline buttons.
 */
public class NotificationLogsAdapter extends ListAdapter<Notification, NotificationLogsAdapter.VH> {

    public interface OnItemAction {
        void onAcceptInvite(Notification notif);
        void onDeclineInvite(Notification notif);
        void onDelete(Notification notif);
    }

    private final OnItemAction actions;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.US);

    public NotificationLogsAdapter(OnItemAction actions) {
        super(DIFF_CALLBACK);
        this.actions = actions;
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK = new DiffUtil.ItemCallback<Notification>() {
        @Override
        public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            return oldItem.isRead() == newItem.isRead() &&
                    oldItem.getTitle().equals(newItem.getTitle());
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Notification notif = getItem(position);

        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());

        if (notif.getTimestamp() != null) {
            holder.tvTime.setText(sdf.format(notif.getTimestamp()));
        } else {
            holder.tvTime.setText("Just now");
        }

        // Show/Hide Action Buttons based on type
        if ("invitation".equals(notif.getType())) {
            holder.layoutActions.setVisibility(View.VISIBLE);

            holder.btnAccept.setOnClickListener(v -> actions.onAcceptInvite(notif));
            holder.btnDecline.setOnClickListener(v -> actions.onDeclineInvite(notif));
        } else {
            holder.layoutActions.setVisibility(View.GONE);
        }
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
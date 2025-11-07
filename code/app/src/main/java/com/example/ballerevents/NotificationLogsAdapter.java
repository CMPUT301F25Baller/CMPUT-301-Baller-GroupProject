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

public class NotificationLogsAdapter
        extends ListAdapter<NotificationLog, NotificationLogsAdapter.VH> {

    public interface OnItemAction {
        void onMarkRead(NotificationLog log);
        void onOpen(NotificationLog log);
    }

    private final OnItemAction actions;

    public NotificationLogsAdapter(OnItemAction actions) {
        super(DIFF);
        this.actions = actions;
    }

    private static final DiffUtil.ItemCallback<NotificationLog> DIFF =
            new DiffUtil.ItemCallback<NotificationLog>() {
                @Override public boolean areItemsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    return a.id.equals(b.id);
                }
                @Override public boolean areContentsTheSame(@NonNull NotificationLog a, @NonNull NotificationLog b) {
                    return a.isRead == b.isRead
                            && a.title.equals(b.title)
                            && a.timestamp.equals(b.timestamp)
                            && a.avatarRes == b.avatarRes;
                }
            };

    @NonNull @Override
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
        h.unreadDot.setVisibility(n.isRead ? View.INVISIBLE : View.VISIBLE);

        h.btnOpen.setOnClickListener(v -> actions.onOpen(n));
        h.btnMarkRead.setOnClickListener(v -> actions.onMarkRead(n));
        h.itemView.setOnClickListener(v -> actions.onOpen(n));
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View unreadDot;
        TextView tvTitle, tvTime, btnOpen, btnMarkRead;
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

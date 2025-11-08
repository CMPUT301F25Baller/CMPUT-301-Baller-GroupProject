package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public static class NotificationItem {
        public final String sender;
        public final String message;
        public final String time;     // e.g., "Just now"
        public final int avatarResId; // drawable resource
        public final boolean hasActions;

        public NotificationItem(String sender, String message, String time, int avatarResId, boolean hasActions) {
            this.sender = sender;
            this.message = message;
            this.time = time;
            this.avatarResId = avatarResId;
            this.hasActions = hasActions;
        }
    }

    private final List<NotificationItem> items = new ArrayList<>();
    private OnActionListener onActionListener;

    public interface OnActionListener {
        void onMarkRead(int position, NotificationItem item);
        void onOpen(int position, NotificationItem item);
    }

    public void setOnActionListener(OnActionListener l) { this.onActionListener = l; }

    public void submitList(List<NotificationItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem it = items.get(position);
        h.tvSender.setText(it.sender);
        h.tvMessage.setText(it.message);
        h.tvTime.setText(it.time);
        h.ivAvatar.setImageResource(it.avatarResId != 0 ? it.avatarResId : R.drawable.placeholder_avatar1);

        h.actionsRow.setVisibility(it.hasActions ? View.VISIBLE : View.GONE);

        h.btnMarkRead.setOnClickListener(v ->
        { if (onActionListener != null) onActionListener.onMarkRead(h.getBindingAdapterPosition(), it); });

        h.btnOpen.setOnClickListener(v ->
        { if (onActionListener != null) onActionListener.onOpen(h.getBindingAdapterPosition(), it); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ShapeableImageView ivAvatar;
        final TextView tvSender, tvMessage, tvTime;
        final LinearLayout actionsRow;
        final Button btnMarkRead, btnOpen;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar   = itemView.findViewById(R.id.ivAvatar);
            tvSender   = itemView.findViewById(R.id.tvSender);
            tvMessage  = itemView.findViewById(R.id.tvMessage);
            tvTime     = itemView.findViewById(R.id.tvTime);
            actionsRow = itemView.findViewById(R.id.actionsRow);
            btnMarkRead= itemView.findViewById(R.id.btnMarkRead);
            btnOpen    = itemView.findViewById(R.id.btnOpen);
        }
    }
}

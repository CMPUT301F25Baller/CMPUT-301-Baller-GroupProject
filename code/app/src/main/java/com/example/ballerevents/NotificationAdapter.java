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

/**
 * RecyclerView adapter for displaying notification rows.
 * <p>
 * Supports standard notifications (Mark Read/Open) and interactive Invitations (Accept/Reject).
 * This adapter uses the standalone {@link NotificationItem} class.
 * </p>
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private List<NotificationItem> items = new ArrayList<>();
    private final OnNotificationActionListener actionListener;

    /**
     * Interface to handle button clicks from the Activity/Fragment.
     */
    public interface OnNotificationActionListener {
        void onAccept(NotificationItem item);
        void onReject(NotificationItem item);
        void onMarkRead(NotificationItem item);
    }

    /**
     * Constructs a new NotificationAdapter.
     * @param actionListener Listener for handling button actions.
     */
    public NotificationAdapter(OnNotificationActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Updates the list of items and refreshes the RecyclerView.
     * @param newItems The new list of notifications to display.
     */
    public void submitList(List<NotificationItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NotificationItem item = items.get(position);

        holder.tvSender.setText(item.sender);
        holder.tvMessage.setText(item.message);
        holder.tvTime.setText(item.timeLabel);
        holder.ivAvatar.setImageResource(item.avatarRes);

        // Handle Action Buttons visibility and logic
        if (item.hasActions) {
            holder.actionsRow.setVisibility(View.VISIBLE);

            if (item.isInvitation) {
                // Configure for Invitation (Accept / Reject)
                holder.btnAction1.setText("Accept");
                holder.btnAction2.setText("Reject");

                // Bind invitation actions
                holder.btnAction1.setOnClickListener(v -> actionListener.onAccept(item));
                holder.btnAction2.setOnClickListener(v -> actionListener.onReject(item));

                // Ensure buttons are enabled for invites
                holder.btnAction1.setEnabled(true);

            } else {
                // Configure for Standard Notification (Mark Read / Open)
                holder.btnAction1.setText("Mark as Read");
                holder.btnAction2.setText("Open");

                // Disable "Mark Read" if already read
                if (item.isRead) {
                    holder.btnAction1.setEnabled(false);
                    holder.btnAction1.setText("Read");
                } else {
                    holder.btnAction1.setEnabled(true);
                }

                holder.btnAction1.setOnClickListener(v -> actionListener.onMarkRead(item));
                // Define 'Open' logic if needed, or hide button
            }
        } else {
            holder.actionsRow.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for notification rows.
     */
    static class VH extends RecyclerView.ViewHolder {
        final ShapeableImageView ivAvatar;
        final TextView tvSender;
        final TextView tvMessage;
        final TextView tvTime;
        final LinearLayout actionsRow;

        // Generic references to the two buttons
        final Button btnAction1; // Left button (Accept / Mark Read)
        final Button btnAction2; // Right button (Reject / Open)

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar    = itemView.findViewById(R.id.ivAvatar);
            tvSender    = itemView.findViewById(R.id.tvSender);
            tvMessage   = itemView.findViewById(R.id.tvMessage);
            tvTime      = itemView.findViewById(R.id.tvTime);
            actionsRow  = itemView.findViewById(R.id.actionsRow);

            // Map buttons to layout IDs
            btnAction1 = itemView.findViewById(R.id.btnMarkRead);
            btnAction2 = itemView.findViewById(R.id.btnOpen);
        }
    }
}

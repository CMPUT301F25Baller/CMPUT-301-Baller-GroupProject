package com.example.ballerevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a user's event history with status indicators.
 * <p>
 * Displays the event details and dynamically calculates the user's status
 * (Waitlist, Selected, Enrolled, Cancelled) based on the event's lists.
 * </p>
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;
    private final String currentUserId;

    /**
     * Constructs the adapter.
     *
     * @param currentUserId The ID of the currently logged-in user.
     * @param listener      Listener for item click events.
     */
    public EventHistoryAdapter(String currentUserId, OnEventClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    /**
     * Updates the data set and refreshes the RecyclerView.
     *
     * @param newEvents The new list of events to display.
     */
    public void submitList(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate());

        Glide.with(context)
                .load(event.getEventPosterUrl())
                .placeholder(R.drawable.placeholder_coldplay_banner)
                .into(holder.ivImage);

        // Determine User Status logic
        String statusText = "Unknown";
        int colorRes = android.R.color.darker_gray;

        boolean isWaitlisted = event.getWaitlistUserIds() != null && event.getWaitlistUserIds().contains(currentUserId);
        boolean isSelected = event.getSelectedUserIds() != null && event.getSelectedUserIds().contains(currentUserId);
        boolean isCancelled = event.getCancelledUserIds() != null && event.getCancelledUserIds().contains(currentUserId);

        String inviteStatus = "pending";
        if (event.getInvitationStatus() != null) {
            inviteStatus = event.getInvitationStatus().getOrDefault(currentUserId, "pending");
        }

        if (isSelected) {
            if ("accepted".equals(inviteStatus)) {
                statusText = "ENROLLED ✅";
                colorRes = android.R.color.holo_green_dark;
            } else {
                statusText = "SELECTED! 🎉";
                colorRes = android.R.color.holo_purple;
            }
        } else if (isCancelled) {
            statusText = "Declined";
            colorRes = android.R.color.holo_red_dark;
        } else if (isWaitlisted) {
            statusText = "Waitlisted ⏳";
            colorRes = android.R.color.holo_orange_dark;
        }

        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(context.getColor(colorRes));

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvStatus;
        ImageView ivImage;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvStatus = itemView.findViewById(R.id.tvStatusBadge);
            ivImage = itemView.findViewById(R.id.ivEventImage);
        }
    }
}
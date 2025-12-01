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

import java.util.List;

/**
 * RecyclerView adapter displaying a list of {@link UserProfile} entries
 * representing entrants on an event's waitlist.
 * <p>
 * Updated to support item clicks for selecting/notifying specific users.
 */
public class WaitlistUserAdapter extends RecyclerView.Adapter<WaitlistUserAdapter.ViewHolder> {

    /** Interface to handle clicks on user rows. */
    public interface OnUserClickListener {
        void onUserClick(UserProfile user);
    }

    private final List<UserProfile> users;
    private final Context context;
    private final OnUserClickListener listener;

    /**
     * Constructor for the adapter.
     *
     * @param users    List of UserProfile objects to display.
     * @param context  Context for Glide and layout inflation.
     * @param listener Callback for user clicks.
     */
    public WaitlistUserAdapter(List<UserProfile> users, Context context, OnUserClickListener listener) {
        this.users = users;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = users.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        Glide.with(context)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .circleCrop()
                .into(holder.ivAvatar);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvEmail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
        }
    }
}
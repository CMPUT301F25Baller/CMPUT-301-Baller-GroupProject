package com.example.ballerevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter displaying a list of {@link UserProfile} entries.
 * <p>
 * Updated Features:
 * <ul>
 * <li>Multi-selection via Checkboxes.</li>
 * <li>Tracks selected User IDs internally.</li>
 * </ul>
 */
public class WaitlistUserAdapter extends RecyclerView.Adapter<WaitlistUserAdapter.ViewHolder> {

    /** Interface to notify the Activity when the selection count changes. */
    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    private final List<UserProfile> users;
    private final Context context;
    private final OnSelectionChangeListener selectionListener;

    // Tracks the IDs of currently selected users
    private final Set<String> selectedUserIds = new HashSet<>();

    /**
     * Constructor for the adapter.
     *
     * @param users             List of UserProfile objects to display.
     * @param context           Context for Glide and layout inflation.
     * @param selectionListener Callback for when selection count updates.
     */
    public WaitlistUserAdapter(List<UserProfile> users, Context context, OnSelectionChangeListener selectionListener) {
        this.users = users;
        this.context = context;
        this.selectionListener = selectionListener;
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
        String uid = user.getUid(); // Uses the alias method we added to UserProfile

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        Glide.with(context)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .circleCrop()
                .into(holder.ivAvatar);

        // Set Checkbox state based on internal Set
        holder.cbSelect.setChecked(selectedUserIds.contains(uid));

        // Handle Row Click -> Toggle Selection
        holder.itemView.setOnClickListener(v -> {
            if (uid != null) {
                toggleSelection(uid);
                notifyItemChanged(holder.getAdapterPosition()); // Refresh UI for this row

                if (selectionListener != null) {
                    selectionListener.onSelectionChanged(selectedUserIds.size());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Toggles the selection state of a user ID.
     * @param uid The user ID to toggle.
     */
    private void toggleSelection(String uid) {
        if (selectedUserIds.contains(uid)) {
            selectedUserIds.remove(uid);
        } else {
            selectedUserIds.add(uid);
        }
    }

    /**
     * @return A list of the full UserProfile objects that are currently selected.
     */
    public List<UserProfile> getSelectedUsers() {
        List<UserProfile> selectedProfiles = new ArrayList<>();
        for (UserProfile user : users) {
            if (selectedUserIds.contains(user.getUid())) {
                selectedProfiles.add(user);
            }
        }
        return selectedProfiles;
    }

    /**
     * Clears all selections and updates the UI.
     */
    public void clearSelection() {
        selectedUserIds.clear();
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionChanged(0);
    }

    /** ViewHolder class for cacheing view references. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvEmail;
        CheckBox cbSelect;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            cbSelect = itemView.findViewById(R.id.cb_select);
        }
    }
}
package com.example.ballerevents;

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
 * Adapter for displaying a list of {@link UserProfile} entries in the Organizer Waitlist.
 * <p>
 * Key features:
 * <ul>
 * <li><b>Multi-selection:</b> Uses checkboxes to select multiple users for batch actions.</li>
 * <li><b>Individual Actions:</b> Clicking a row opens a detailed options dialog.</li>
 * </ul>
 * </p>
 */
public class WaitlistUserAdapter extends RecyclerView.Adapter<WaitlistUserAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(UserProfile user);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    private final List<UserProfile> users;
    private final OnSelectionChangeListener selectionListener;
    private OnItemClickListener itemClickListener;
    private final Set<String> selectedUserIds = new HashSet<>();

    /**
     * Constructs a new WaitlistUserAdapter.
     *
     * @param users             The list of users to display.
     * @param selectionListener Listener for updates to the selection count.
     */
    public WaitlistUserAdapter(List<UserProfile> users, OnSelectionChangeListener selectionListener) {
        this.users = users;
        this.selectionListener = selectionListener;
    }

    /**
     * Sets the listener for row click events (viewing user details).
     *
     * @param listener The click listener.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
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
        String uid = user.getUid();

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .circleCrop()
                .into(holder.ivAvatar);

        // Reset listener to prevent recycling issues
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedUserIds.contains(uid));

        // Checkbox click: Toggles selection state
        holder.cbSelect.setOnClickListener(v -> {
            toggleSelection(uid);
            if (selectionListener != null) {
                selectionListener.onSelectionChanged(selectedUserIds.size());
            }
        });

        // Row click: Triggers detailed action (e.g., options dialog)
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Toggles the selection status of a specific user ID.
     */
    private void toggleSelection(String uid) {
        if (selectedUserIds.contains(uid)) {
            selectedUserIds.remove(uid);
        } else {
            selectedUserIds.add(uid);
        }
    }

    /**
     * Returns a list of all currently selected UserProfiles.
     *
     * @return List of selected users.
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
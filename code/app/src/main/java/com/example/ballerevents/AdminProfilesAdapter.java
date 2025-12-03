package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemAdminProfileBinding;

import java.util.Locale;

/**
 * Adapter for displaying a list of user profiles in the Admin interface.
 * Handles the display of user details (avatar, name, email, role) and provides
 * a context menu for administrative actions like deletion.
 */
public class AdminProfilesAdapter extends ListAdapter<UserProfile, AdminProfilesAdapter.VH> {

    /**
     * Interface for handling user interactions with profile items.
     */
    public interface OnProfileActionListener {
        /**
         * Triggered when a profile item is clicked.
         * @param profile The selected user profile.
         */
        void onProfileClick(UserProfile profile);

        /**
         * Triggered when the delete action is selected from the menu.
         * @param profile The user profile to be deleted.
         */
        default void onDelete(UserProfile profile) {}
    }

    private final OnProfileActionListener listener;

    /**
     * Constructs the adapter with a specific listener.
     *
     * @param listener The listener for profile actions.
     */
    public AdminProfilesAdapter(OnProfileActionListener listener) {
        super(ProfileDiffCallback);
        this.listener = listener;
    }

    /**
     * ViewHolder class for binding profile data to the view.
     */
    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminProfileBinding b;

        VH(ItemAdminProfileBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        /**
         * Binds the UserProfile data to the UI elements.
         *
         * @param p        The UserProfile object.
         * @param listener The listener for click events.
         */
        void bind(UserProfile p, OnProfileActionListener listener) {
            b.tvName.setText(p.getName());
            b.tvEmail.setText(p.getEmail());

            String role = p.getRole();
            if (role != null && !role.isEmpty()) {
                String displayRole = role.substring(0, 1).toUpperCase(Locale.ROOT) + role.substring(1);
                b.tvRole.setText(displayRole);
            } else {
                b.tvRole.setText("Entrant");
            }

            Glide.with(itemView.getContext())
                    .load(p.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_avatar1)
                    .error(R.drawable.placeholder_avatar1)
                    .into(b.ivAvatar);

            itemView.setOnClickListener(v -> listener.onProfileClick(p));

            b.ivMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.menu_admin_profile_row);

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        listener.onDelete(p);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        UserProfile p = getItem(pos);
        if (p != null) holder.bind(p, listener);
    }

    private static final DiffUtil.ItemCallback<UserProfile> ProfileDiffCallback =
            new DiffUtil.ItemCallback<UserProfile>() {
                @Override
                public boolean areItemsTheSame(@NonNull UserProfile old, @NonNull UserProfile newItem) {
                    return old.getId().equals(newItem.getId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull UserProfile old, @NonNull UserProfile newItem) {
                    return old.getId().equals(newItem.getId());
                }
            };
}
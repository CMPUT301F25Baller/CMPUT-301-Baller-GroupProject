package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemAdminProfileBinding;

/**
 * RecyclerView adapter used by the Admin to display a list of user
 * profiles within {@link AdminProfilesActivity}. Each row displays:
 * <ul>
 *     <li>User name</li>
 *     <li>User email</li>
 *     <li>User avatar (loaded using Glide)</li>
 * </ul>
 *
 * <p>The adapter exposes a {@link OnProfileClickListener} callback so that
 * clicking a row allows the admin to initiate actions such as viewing
 * details or deleting the user.
 *
 * <p>This adapter uses {@link ListAdapter} with a {@link DiffUtil.ItemCallback}
 * for efficient, animated list updates when Firestore data changes.
 */
public class AdminProfilesAdapter extends ListAdapter<UserProfile, AdminProfilesAdapter.VH> {

    /**
     * Listener interface for handling row-click events on a user profile.
     */
    public interface OnProfileClickListener {
        /**
         * Called when the admin taps a profile row.
         *
         * @param profile The selected {@link UserProfile}.
         */
        void onProfileClick(UserProfile profile);
    }

    private final OnProfileClickListener clickListener;

    /**
     * Creates the adapter with a click listener to notify when a profile is tapped.
     *
     * @param clickListener callback invoked on profile click
     */
    public AdminProfilesAdapter(OnProfileClickListener clickListener) {
        super(ProfileDiffCallback);
        this.clickListener = clickListener;
    }

    /**
     * ViewHolder that binds a single profile row using ViewBinding.
     */
    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminProfileBinding b;

        VH(ItemAdminProfileBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        /**
         * Binds the given {@link UserProfile} to the row UI elements.
         * Loads the avatar using Glide and forwards click events.
         *
         * @param p        the user profile to bind
         * @param listener click listener for row selection
         */
        void bind(UserProfile p, OnProfileClickListener listener) {
            b.tvName.setText(p.getName());
            b.tvEmail.setText(p.getEmail());

            Glide.with(itemView.getContext())
                    .load(p.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_avatar1)
                    .error(R.drawable.placeholder_avatar1)
                    .into(b.ivAvatar);

            itemView.setOnClickListener(v -> listener.onProfileClick(p));
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
        if (p != null) {
            holder.bind(p, clickListener);
        }
    }

    /**
     * DiffUtil callback used to efficiently compute changes in the list.
     * Compares user IDs to determine whether items or their contents match.
     */
    private static final DiffUtil.ItemCallback<UserProfile> ProfileDiffCallback =
            new DiffUtil.ItemCallback<UserProfile>() {
                @Override
                public boolean areItemsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
                    // Since IDs uniquely map to Firestore docs, equality on ID is enough for contents
                    return oldItem.getId().equals(newItem.getId());
                }
            };
}

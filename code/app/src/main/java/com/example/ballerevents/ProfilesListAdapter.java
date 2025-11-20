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
 * ListAdapter for displaying {@link UserProfile} items inside an admin-style
 * profile list layout (item_admin_profile.xml).
 *
 * <p>
 * Each item shows:
 * <ul>
 *     <li>Profile picture</li>
 *     <li>User name</li>
 * </ul>
 * and invokes a click callback when selected.
 * </p>
 *
 * <p>
 * Uses a stable ID derived from the profile document ID where possible to
 * help RecyclerView maintain item identity efficiently.
 * </p>
 */
public class ProfilesListAdapter extends ListAdapter<UserProfile, ProfilesListAdapter.VH> {

    /**
     * Listener invoked when a profile row is clicked.
     */
    public interface OnProfileClick {
        /**
         * Called when a user profile is clicked.
         *
         * @param p the clicked profile
         */
        void onClick(UserProfile p);
    }

    /** Callback invoked when a profile item is selected. */
    private final OnProfileClick onClick;

    /**
     * Constructs the adapter.
     *
     * @param onClick click listener for profile rows
     */
    public ProfilesListAdapter(OnProfileClick onClick) {
        super(DIFF);
        this.onClick = onClick;
        setHasStableIds(true);
    }

    /**
     * DiffUtil callback comparing user profiles by ID, name, and avatar URL.
     */
    private static final DiffUtil.ItemCallback<UserProfile> DIFF =
            new DiffUtil.ItemCallback<UserProfile>() {
                @Override
                public boolean areItemsTheSame(@NonNull UserProfile o, @NonNull UserProfile n) {
                    return safe(o.getId()).equals(safe(n.getId()));
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull UserProfile o,
                        @NonNull UserProfile n
                ) {
                    return safe(o.getId()).equals(safe(n.getId()))
                            && safe(o.getName()).equals(safe(n.getName()))
                            && safe(o.getProfilePictureUrl()).equals(safe(n.getProfilePictureUrl()));
                }

                /** Utility to avoid null-safety issues in comparisons. */
                private String safe(String s) {
                    return s == null ? "" : s;
                }
            };

    /**
     * Provides a stable long ID for each profile based on its document ID.
     *
     * @param position adapter position
     * @return a stable hash value
     */
    @Override
    public long getItemId(int position) {
        String id = getItem(position).getId();
        return id == null ? position : id.hashCode();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProfileBinding b = ItemAdminProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserProfile p = getItem(position);
        h.bind(p, onClick);
    }

    /**
     * ViewHolder for profile rows inside the admin-style list.
     */
    static class VH extends RecyclerView.ViewHolder {
        private final ItemAdminProfileBinding b;

        VH(@NonNull ItemAdminProfileBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        /**
         * Binds the given profile data into the UI.
         *
         * @param p       the profile to display
         * @param onClick click listener for the row
         */
        void bind(UserProfile p, OnProfileClick onClick) {
            b.tvName.setText(p.getName() == null ? "User" : p.getName());

            Glide.with(b.getRoot().getContext())
                    .load(p.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_avatar1)
                    .error(R.drawable.placeholder_avatar1)
                    .into(b.ivAvatar);

            b.getRoot().setOnClickListener(v -> onClick.onClick(p));
        }
    }
}

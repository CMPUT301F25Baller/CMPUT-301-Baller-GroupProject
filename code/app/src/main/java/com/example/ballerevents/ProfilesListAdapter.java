package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemAdminProfileBinding;

import java.util.Locale;

/**
 * A safe, read-only adapter for displaying lists of user profiles.
 * Used for "Following" and "Followers" lists where no admin actions (delete) are allowed.
 */
public class ProfilesListAdapter extends ListAdapter<UserProfile, ProfilesListAdapter.VH> {

    public interface OnProfileClick {
        void onClick(UserProfile p);
    }

    private final OnProfileClick onClick;

    public ProfilesListAdapter(OnProfileClick onClick) {
        super(DIFF);
        this.onClick = onClick;
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<UserProfile> DIFF =
            new DiffUtil.ItemCallback<UserProfile>() {
                @Override
                public boolean areItemsTheSame(@NonNull UserProfile o, @NonNull UserProfile n) {
                    return safe(o.getId()).equals(safe(n.getId()));
                }

                @Override
                public boolean areContentsTheSame(@NonNull UserProfile o, @NonNull UserProfile n) {
                    return safe(o.getId()).equals(safe(n.getId()));
                }

                private String safe(String s) { return s == null ? "" : s; }
            };

    @Override
    public long getItemId(int position) {
        String id = getItem(position).getId();
        return id == null ? position : id.hashCode();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse the existing item layout, but we will hide the menu button
        ItemAdminProfileBinding b = ItemAdminProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserProfile p = getItem(position);
        if (p != null) h.bind(p, onClick);
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemAdminProfileBinding b;

        VH(@NonNull ItemAdminProfileBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(UserProfile p, OnProfileClick onClick) {
            b.tvName.setText(p.getName() == null ? "User" : p.getName());
            b.tvEmail.setText(p.getEmail());

            // Show Role
            String role = p.getRole();
            if (role != null && !role.isEmpty()) {
                String displayRole = role.substring(0, 1).toUpperCase(Locale.ROOT) + role.substring(1);
                b.tvRole.setText(displayRole);
            } else {
                b.tvRole.setText("Entrant");
            }

            // Hide the Admin Menu (Three Dots) - Critical Fix for Security
            b.ivMenu.setVisibility(android.view.View.GONE);

            Glide.with(b.getRoot().getContext())
                    .load(p.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_avatar1)
                    .error(R.drawable.placeholder_avatar1)
                    .into(b.ivAvatar);

            b.getRoot().setOnClickListener(v -> onClick.onClick(p));
        }
    }
}
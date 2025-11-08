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
 * RecyclerView adapter for admin profile cards. Shows avatar, name, and email,
 * and invokes the provided click listener when a row is tapped.
 */

public class AdminProfilesAdapter extends ListAdapter<UserProfile, AdminProfilesAdapter.VH> {

    public interface OnProfileClickListener {
        void onProfileClick(UserProfile profile);
    }

    private final OnProfileClickListener clickListener;

    public AdminProfilesAdapter(OnProfileClickListener clickListener) {
        super(ProfileDiffCallback);
        this.clickListener = clickListener;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminProfileBinding b;
        VH(ItemAdminProfileBinding b) { super(b.getRoot()); this.b = b; }

        void bind(UserProfile p, OnProfileClickListener listener) {
            b.tvName.setText(p.getName());
            b.tvEmail.setText(p.getEmail()); // Added email

            Glide.with(itemView.getContext())
                    .load(p.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_avatar1)
                    .error(R.drawable.placeholder_avatar1)
                    .into(b.ivAvatar);

            itemView.setOnClickListener(v -> listener.onProfileClick(p));
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        UserProfile p = getItem(pos);
        if (p != null) {
            h.bind(p, clickListener);
        }
    }

    private static final DiffUtil.ItemCallback<UserProfile> ProfileDiffCallback = new DiffUtil.ItemCallback<UserProfile>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserProfile oldItem, @NonNull UserProfile newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
    };
}
package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemAdminProfileBinding;

import java.util.List;

/**
 * Adapter for displaying a simple list of chosen entrants (Final Entrants).
 * Uses the {@code item_admin_profile} layout to show avatar, name, and email.
 */
public class OrganizerFinalEntrantsAdapter
        extends RecyclerView.Adapter<OrganizerFinalEntrantsAdapter.ViewHolder> {

    private final List<UserProfile> users;

    public OrganizerFinalEntrantsAdapter(List<UserProfile> users) {
        this.users = users;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdminProfileBinding binding;

        public ViewHolder(ItemAdminProfileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemAdminProfileBinding binding =
                ItemAdminProfileBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = users.get(position);

        holder.binding.tvName.setText(user.getName());
        holder.binding.tvEmail.setText(user.getEmail());

        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .into(holder.binding.ivAvatar);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
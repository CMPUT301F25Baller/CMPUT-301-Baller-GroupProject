package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ballerevents.databinding.ItemProfileSimpleBinding;

import java.util.List;

public class OrganizerLotteryWinnersAdapter extends RecyclerView.Adapter<OrganizerLotteryWinnersAdapter.ViewHolder> {

    private final List<UserProfile> profiles;

    public OrganizerLotteryWinnersAdapter(List<UserProfile> profiles) {
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProfileSimpleBinding binding = ItemProfileSimpleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile p = profiles.get(position);
        holder.binding.tvName.setText(p.getName());
        holder.binding.tvEmail.setText(p.getEmail());
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemProfileSimpleBinding binding;

        ViewHolder(ItemProfileSimpleBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}

package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ProfilesListAdapter extends ListAdapter<Profile, ProfilesListAdapter.VH> {

    interface OnProfileClick {
        void onClick(Profile p);
    }

    private final OnProfileClick onClick;

    public ProfilesListAdapter(OnProfileClick onClick) {
        super(DIFF);
        this.onClick = onClick;
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<Profile> DIFF =
            new DiffUtil.ItemCallback<Profile>() {
                @Override
                public boolean areItemsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
                    return oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
                    // Simple compare; adjust if you add more fields
                    return oldItem.id.equals(newItem.id)
                            && oldItem.name.equals(newItem.name)
                            && oldItem.avatarResId == newItem.avatarResId;
                }
            };

    @Override
    public long getItemId(int position) {
        return getItem(position).id.hashCode();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Profile p = getItem(position);

        String name = p.name != null ? p.name : p.id;
        h.tvName.setText(name);

        if (p.avatarResId != 0) {
            h.ivAvatar.setImageResource(p.avatarResId);
        } else {
            h.ivAvatar.setImageResource(android.R.color.darker_gray);
        }

        h.itemView.setOnClickListener(v -> onClick.onClick(p));
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}

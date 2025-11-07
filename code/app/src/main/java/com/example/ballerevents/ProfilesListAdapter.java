package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ItemAdminProfileBinding;

public class ProfilesListAdapter extends ListAdapter<UserProfile, ProfilesListAdapter.VH> {

    public interface OnProfileClick { void onClick(UserProfile p); }

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
                    return safe(o.getId()).equals(safe(n.getId()))
                            && safe(o.getName()).equals(safe(n.getName()))
                            && safe(o.getProfilePictureUrl()).equals(safe(n.getProfilePictureUrl()));
                }
                private String safe(String s){ return s==null? "": s; }
            };

    @Override public long getItemId(int position) {
        String id = getItem(position).getId();
        return id == null ? position : id.hashCode();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProfileBinding b = ItemAdminProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserProfile p = getItem(position);
        h.bind(p, onClick);
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemAdminProfileBinding b;
        VH(@NonNull ItemAdminProfileBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
        void bind(UserProfile p, OnProfileClick onClick){
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

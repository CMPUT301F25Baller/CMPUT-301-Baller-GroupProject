package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ballerevents.databinding.ItemAdminProfileBinding;
import java.util.ArrayList;
import java.util.List;

public class AdminProfilesAdapter extends RecyclerView.Adapter<AdminProfilesAdapter.VH> {
    private final List<Profile> data = new ArrayList<>();

    public void submitList(List<Profile> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminProfileBinding b;
        VH(ItemAdminProfileBinding b) { super(b.getRoot()); this.b = b; }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Profile p = data.get(pos);
        h.b.ivAvatar.setImageResource(p.avatarResId);
        h.b.tvName.setText(p.name);
    }

    @Override public int getItemCount() { return data.size(); }
}

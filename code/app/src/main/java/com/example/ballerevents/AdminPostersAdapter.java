package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ballerevents.databinding.ItemAdminPosterBinding;
import java.util.ArrayList;
import java.util.List;

public class AdminPostersAdapter extends RecyclerView.Adapter<AdminPostersAdapter.VH> {
    private final List<ImageAsset> data = new ArrayList<>();

    public void submitList(List<ImageAsset> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminPosterBinding b;
        VH(ItemAdminPosterBinding b) { super(b.getRoot()); this.b = b; }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminPosterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ImageAsset img = data.get(pos);
        h.b.ivPoster.setImageResource(img.drawableResId);
        h.b.tvLabel.setText(img.label);
    }

    @Override public int getItemCount() { return data.size(); }
}

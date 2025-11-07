package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.VH> {

    interface ImageActions {
        void onPreview(ImageAsset img);
        void onDelete(ImageAsset img);
    }

    private final List<ImageAsset> data = new ArrayList<>();
    private final ImageActions actions;

    public AdminImagesAdapter(ImageActions actions) {
        this.actions = actions;
        setHasStableIds(true);
    }

    public void submitList(List<ImageAsset> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @Override public long getItemId(int position) {
        return data.get(position).id.hashCode();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvLabel;
        VH(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_poster, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ImageAsset img = data.get(pos);
        h.ivPoster.setImageResource(img.drawableResId);
        h.tvLabel.setText(img.label != null ? img.label : img.id);

        h.itemView.setOnClickListener(v -> { if (actions != null) actions.onPreview(img); });
        h.itemView.setOnLongClickListener(v -> {
            if (actions != null) actions.onDelete(img);
            return true;
        });
    }

    @Override public int getItemCount() { return data.size(); }
}

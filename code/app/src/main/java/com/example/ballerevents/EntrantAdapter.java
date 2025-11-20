package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private final List<Entrant> entrants;

    public EntrantAdapter(List<Entrant> entrants) {
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant e = entrants.get(position);

        holder.avatar.setImageResource(e.getAvatarResId());
        holder.name.setText(e.getName());
        holder.status.setText(e.getStatus());
        holder.time.setText(e.getTimeAgo());
        String status = e.getStatus().toLowerCase();

        if (status.contains("cancelled") || status.contains("declined")){
            holder.ignoreButton.setVisibility(View.VISIBLE);
            holder.replaceButton.setVisibility(View.VISIBLE);
            holder.ignoreButton.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "Ignored", Toast.LENGTH_SHORT).show());

            holder.replaceButton.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "Replaced", Toast.LENGTH_SHORT).show());

        } else {
            holder.ignoreButton.setVisibility(View.GONE);
            holder.replaceButton.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return entrants.size();
    }

    static class EntrantViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView avatar;
        TextView name, status, time;
        MaterialButton ignoreButton, replaceButton;

        EntrantViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.imgAvatar);

            // These two are inside your LinearLayout
            LinearLayout nameStatusLayout = itemView.findViewById(R.id.tvNameStatus);
            name  = (TextView) nameStatusLayout.getChildAt(0);
            status = (TextView) nameStatusLayout.getChildAt(1);

            time = itemView.findViewById(R.id.tvTime);

            ignoreButton = itemView.findViewById(R.id.btnIgnore);
            replaceButton = itemView.findViewById(R.id.btnReplace);
        }
    }
}

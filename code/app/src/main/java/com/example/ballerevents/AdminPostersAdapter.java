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

import com.bumptech.glide.Glide;

/** Admin grid of event posters. No tvLabel required. */
public class AdminPostersAdapter extends ListAdapter<Event, AdminPostersAdapter.VH> {

    public interface PosterActions {
        void onPreview(@NonNull Event event);
        void onDelete(@NonNull Event event);
    }

    private final PosterActions actions;

    public AdminPostersAdapter(@NonNull PosterActions actions) {
        super(DIFF);
        this.actions = actions;
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<Event> DIFF = new DiffUtil.ItemCallback<Event>() {
        @Override public boolean areItemsTheSame(@NonNull Event a, @NonNull Event b) {
            String ia = a.getId(), ib = b.getId();
            return ia != null && ia.equals(ib);
        }
        @Override public boolean areContentsTheSame(@NonNull Event a, @NonNull Event b) {
            String ap = a.getEventPosterUrl() == null ? "" : a.getEventPosterUrl();
            String bp = b.getEventPosterUrl() == null ? "" : b.getEventPosterUrl();
            String at = a.getTitle() == null ? "" : a.getTitle();
            String bt = b.getTitle() == null ? "" : b.getTitle();
            return ap.equals(bp) && at.equals(bt);
        }
    };

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;

        VH(@NonNull View v) {
            super(v);
            ivPoster = v.findViewById(R.id.ivPoster);
            tvTitle  = v.findViewById(R.id.tvTitle);
        }

        void bind(@NonNull Event e, @NonNull PosterActions actions) {
            tvTitle.setText(e.getTitle() == null ? "Untitled Event" : e.getTitle());

            String url = e.getEventPosterUrl();
            Glide.with(ivPoster.getContext())
                    .load(url == null || url.isEmpty() ? null : url)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(ivPoster);

            itemView.setOnClickListener(v -> actions.onPreview(e));
            itemView.setOnLongClickListener(v -> { actions.onDelete(e); return true; });
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_poster, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), actions);
    }

    @Override
    public long getItemId(int position) {
        Event e = getItem(position);
        return e.getId() != null ? e.getId().hashCode() : position;
    }
}

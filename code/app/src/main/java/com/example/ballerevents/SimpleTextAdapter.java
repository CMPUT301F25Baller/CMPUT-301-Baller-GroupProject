package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A minimal RecyclerView.Adapter that displays each string entry
 * as a simple {@link TextView} row using the built-in layout
 * {@code android.R.layout.simple_list_item_1}.
 *
 * <p>
 * This adapter is intended for lightweight logs or simple text lists
 * where no custom view holder logic is required.
 * </p>
 */
public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.VH> {

    /** Backing list of string entries to display. */
    private final List<String> data;

    /**
     * Creates a new simple text adapter.
     *
     * @param data List of strings to display.
     */
    public SimpleTextAdapter(List<String> data) {
        this.data = data;
    }

    /**
     * Basic ViewHolder that stores the underlying TextView.
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;

        VH(@NonNull TextView tv) {
            super(tv);
            this.tv = tv;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        // Add horizontal padding for better visual spacing
        int padH = (int) (16 * parent.getResources().getDisplayMetrics().density);
        tv.setPadding(padH, tv.getPaddingTop(), padH, tv.getPaddingBottom());

        return new VH(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tv.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}

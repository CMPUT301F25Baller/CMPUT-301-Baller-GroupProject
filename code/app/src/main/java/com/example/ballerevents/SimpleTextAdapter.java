package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A minimal RecyclerView.Adapter that displays a list of strings.
 * <p>
 * This adapter uses the built-in {@code android.R.layout.simple_list_item_1}
 * layout to render each entry as a simple TextView. It is intended for
 * lightweight logs or debug lists where custom ViewHolders are not required.
 * </p>
 */
public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.VH> {

    private final List<String> data;

    /**
     * Creates a new simple text adapter.
     *
     * @param data The list of strings to display.
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
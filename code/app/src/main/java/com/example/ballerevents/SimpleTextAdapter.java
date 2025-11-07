package com.example.ballerevents;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Tiny adapter that renders each item as a simple TextView row. */
public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.VH> {
    private final List<String> data;

    public SimpleTextAdapter(List<String> data) {
        this.data = data;
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;
        VH(@NonNull TextView tv) { super(tv); this.tv = tv; }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        // Give list items some side padding so horizontal lists look nice
        int padH = (int) (16 * parent.getResources().getDisplayMetrics().density);
        tv.setPadding(padH, tv.getPaddingTop(), padH, tv.getPaddingBottom());
        return new VH(tv);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tv.setText(data.get(position));
    }

    @Override public int getItemCount() { return data.size(); }
}


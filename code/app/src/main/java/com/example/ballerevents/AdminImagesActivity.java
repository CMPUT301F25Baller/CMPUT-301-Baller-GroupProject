package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AdminImagesActivity extends AppCompatActivity implements AdminImagesAdapter.ImageActions {

    private RecyclerView recycler;
    private View progress;
    private AdminImagesAdapter adapter;

    // Use your existing stub repo (same pattern as elsewhere in your code)
    private final AdminRepository repo = new StubAdminRepository();
    private final List<ImageAsset> allImages = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_images);

        MaterialToolbar top = findViewById(R.id.topAppBar);
        if (top != null) {
            top.setTitle("Event Posters");
            top.setSubtitle("Tap to preview • Hold to delete"); // hint requested
            top.setNavigationOnClickListener(v -> finish());
        }

        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new AdminImagesAdapter(this);
        recycler.setAdapter(adapter);

        loadImages();
    }

    private void loadImages() {
        progress.setVisibility(View.VISIBLE);
        repo.getRecentImages(list -> {
            progress.setVisibility(View.GONE);
            allImages.clear();
            if (list != null) allImages.addAll(list);
            adapter.submitList(new ArrayList<>(allImages));
        });
    }

    // === ImageActions ===

    @Override
    public void onPreview(ImageAsset img) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null, false);
        ImageView iv = dialogView.findViewById(R.id.ivPreview);
        iv.setImageResource(img.drawableResId);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDelete(ImageAsset img) {
        new AlertDialog.Builder(this)
                .setTitle("Delete poster?")
                .setMessage("This removes the poster from the current list (MVP).")
                .setPositiveButton("Delete", (d, w) -> {
                    allImages.remove(img);
                    adapter.submitList(new ArrayList<>(allImages));
                    Toast.makeText(this, "Poster deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

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

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityAdminImagesBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class AdminImagesActivity extends AppCompatActivity implements AdminImagesAdapter.ImageActions {

    private ActivityAdminImagesBinding binding;
    private AdminImagesAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminImagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        binding.recycler.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new AdminImagesAdapter(this);
        binding.recycler.setAdapter(adapter);

        loadEvents();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.recycler.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void loadEvents() {
        setLoading(true);

        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(60)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Event> events = snap.toObjects(Event.class);
                    adapter.submitList(events);
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to load posters: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // === AdminImagesAdapter.ImageActions ===

    @Override
    public void onPreview(@Nullable Event event) {
        if (event == null) return;
        String url = event.getEventPosterUrl();
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "No poster to preview", Toast.LENGTH_SHORT).show();
            return;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null, false);
        ImageView iv = view.findViewById(R.id.ivPreview);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .fitCenter()
                .into(iv);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .show();
    }

    @Override
    public void onDelete(@Nullable Event event) {
        if (event == null || event.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Remove poster?")
                .setMessage("This will clear the poster URL for this event.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (d, w) -> {
                    db.collection("events").document(event.getId())
                            .update("eventPosterUrl", null)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Poster removed", Toast.LENGTH_SHORT).show();
                                loadEvents();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .show();
    }
}

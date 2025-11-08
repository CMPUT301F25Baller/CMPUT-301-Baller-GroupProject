package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/**
 * Grid gallery of event posters. Loads events from Firestore and displays
 * their poster URLs as thumbnails. Tap to preview; long-press prompts
 * to clear the poster URL (does not delete the event).
 */


public class AdminImagesActivity extends AppCompatActivity implements AdminImagesAdapter.ImageActions {

    private static final String TAG = "AdminImagesActivity";
    private RecyclerView recycler;
    private View progress;
    private AdminImagesAdapter adapter;
    private FirebaseFirestore db;

    private final List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_images);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar top = findViewById(R.id.topAppBar);
        if (top != null) {
            top.setTitle("Event Posters");
            top.setSubtitle("Tap to preview • Long press to delete");
            top.setNavigationOnClickListener(v -> finish());
        }

        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new AdminImagesAdapter(this);
        recycler.setAdapter(adapter);

        loadImages();
    }

    private void loadImages() {
        progress.setVisibility(View.VISIBLE);
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progress.setVisibility(View.GONE);
                    allEvents.clear();
                    allEvents.addAll(queryDocumentSnapshots.toObjects(Event.class));
                    adapter.submitList(new ArrayList<>(allEvents));
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Log.w(TAG, "Error loading events for images", e);
                });
    }

    // === ImageActions ===

    @Override
    public void onPreview(Event event) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null, false);
        ImageView iv = dialogView.findViewById(R.id.ivPreview);

        Glide.with(this)
                .load(event.getEventPosterUrl())
                .error(R.drawable.placeholder_image)
                .into(iv);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDelete(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete poster?")
                .setMessage("This will clear the event poster URL for '" + event.getTitle() + "'. It will not delete the event itself.")
                .setPositiveButton("Clear URL", (d, w) -> {
                    deletePosterFromEvent(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePosterFromEvent(Event event) {
        db.collection("events").document(event.getId())
                .update("eventPosterUrl", "") // Set the URL to an empty string
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Poster URL cleared", Toast.LENGTH_SHORT).show();
                    loadImages(); // Reload the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error clearing URL", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error updating event poster URL", e);
                });
    }
}
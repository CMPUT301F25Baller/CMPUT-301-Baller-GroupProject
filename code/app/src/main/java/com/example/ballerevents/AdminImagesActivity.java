package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for the Administrator to view and manage uploaded event posters.
 * Allows browsing of all images and deletion of specific posters if deemed necessary[cite: 105, 108].
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

        // Setup New Header manually since we aren't using ViewBinding for this simple Activity
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText("Event Posters");

        progress = findViewById(R.id.progress);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new AdminImagesAdapter(this);
        recycler.setAdapter(adapter);

        loadImages();
    }

    /**
     * Loads all events from Firestore to extract and display their posters.
     */
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

    @Override
    public void onPreview(Event event) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null, false);
        ImageView iv = dialogView.findViewById(R.id.ivPreview);
        Glide.with(this).load(event.getEventPosterUrl()).error(R.drawable.placeholder_image).into(iv);
        new AlertDialog.Builder(this).setView(dialogView).setPositiveButton("Close", null).show();
    }

    @Override
    public void onDelete(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete poster?")
                .setMessage("Clear poster for '" + event.getTitle() + "'?")
                .setPositiveButton("Clear URL", (d, w) -> deletePosterFromEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Removes the poster URL from the specified event document in Firestore.
     *
     * @param event The event whose poster is being deleted.
     */
    private void deletePosterFromEvent(Event event) {
        db.collection("events").document(event.getId())
                .update("eventPosterUrl", "")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Poster URL cleared", Toast.LENGTH_SHORT).show();
                    loadImages();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error clearing URL", Toast.LENGTH_SHORT).show());
    }
}
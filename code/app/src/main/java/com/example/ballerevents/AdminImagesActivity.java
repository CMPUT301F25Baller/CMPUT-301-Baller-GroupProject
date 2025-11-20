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
 * Activity that displays a grid gallery of event poster images for administrators.
 *
 * <p>This screen fetches all events from Firestore and displays their
 * {@code eventPosterUrl} fields in a 3-column grid via {@link AdminImagesAdapter}.
 *
 * <ul>
 *     <li><b>Tap</b> an image → Opens a large preview dialog.</li>
 *     <li><b>Long-press</b> → Prompts the admin to clear the poster URL
 *         (does not delete the event itself).</li>
 * </ul>
 *
 * <p>This is useful for administrators needing to inspect or clean up event posters.
 */
public class AdminImagesActivity extends AppCompatActivity implements AdminImagesAdapter.ImageActions {

    private static final String TAG = "AdminImagesActivity";

    /** RecyclerView that shows the poster thumbnails. */
    private RecyclerView recycler;

    /** Progress spinner shown while loading Firestore data. */
    private View progress;

    /** Adapter responsible for rendering poster thumbnails. */
    private AdminImagesAdapter adapter;

    /** Firestore instance for loading and updating events. */
    private FirebaseFirestore db;

    /** Cache of all events loaded from Firestore. */
    private final List<Event> allEvents = new ArrayList<>();

    /**
     * Initializes toolbar, RecyclerView, adapter, and begins loading event images.
     */
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

    /**
     * Loads all events from Firestore and displays their poster URLs in the grid.
     * <p>The method updates a local cache and displays a progress spinner
     * while asynchronous Firestore loading is in progress.</p>
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

    // -------------------------------------------------------------------------
    // ImageActions implementation
    // -------------------------------------------------------------------------

    /**
     * Shows a dialog containing a large preview of the event's poster image.
     *
     * @param event the event whose poster should be previewed
     */
    @Override
    public void onPreview(Event event) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_image_preview, null, false);

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

    /**
     * Prompts the admin to confirm clearing the event's poster URL.
     *
     * <p>This does not delete the event, only resets {@code eventPosterUrl} to an empty string.</p>
     *
     * @param event the event whose poster URL is about to be cleared
     */
    @Override
    public void onDelete(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete poster?")
                .setMessage("This will clear the event poster URL for '" + event.getTitle() +
                        "'. It will not delete the event itself.")
                .setPositiveButton("Clear URL", (d, w) -> deletePosterFromEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Updates the Firestore document by setting {@code eventPosterUrl} to an empty string.
     *
     * <p>After successful update, the list of posters is reloaded to reflect the change.</p>
     *
     * @param event the event whose poster field should be cleared
     */
    private void deletePosterFromEvent(Event event) {
        db.collection("events").document(event.getId())
                .update("eventPosterUrl", "")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Poster URL cleared", Toast.LENGTH_SHORT).show();
                    loadImages();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error clearing URL", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error updating event poster URL", e);
                });
    }
}

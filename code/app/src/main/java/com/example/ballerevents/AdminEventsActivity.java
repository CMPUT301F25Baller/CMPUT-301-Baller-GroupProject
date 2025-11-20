package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminEventsBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Activity that allows administrators to view, filter, and delete events stored in Firestore.
 *
 * <p>This screen displays a vertical list of events using {@link AdminEventsAdapter},
 * supports case-insensitive filtering by event title, and enables permanent deletion after
 * confirmation. All event data is read from the Firestore <b>events</b> collection.
 *
 * <p>Key features:
 * <ul>
 *     <li>Loads all events on startup</li>
 *     <li>Filter-as-you-type search bar</li>
 *     <li>Delete event via confirmation dialog</li>
 *     <li>Firestore integration for reading and deleting</li>
 * </ul>
 *
 * <p>This class implements {@link AdminEventsAdapter.OnEventActionListener} to receive
 * delete actions emitted by the adapter.
 */
public class AdminEventsActivity extends AppCompatActivity implements AdminEventsAdapter.OnEventActionListener {

    /** Tag used for internal logging. */
    private static final String TAG = "AdminEventsActivity";

    /** ViewBinding for the admin events layout. */
    private AdminEventsBinding binding;

    /** Firestore instance used to load and delete events. */
    private FirebaseFirestore db;

    /** Adapter that renders event rows with delete controls. */
    private AdminEventsAdapter adapter;

    /**
     * Local in-memory cache of all events loaded from Firestore.
     * Used to support client-side filtering.
     */
    private List<Event> allEvents = new ArrayList<>();

    /**
     * Initializes layout, toolbar, RecyclerView, Firestore, adapters, and search listeners.
     *
     * @param savedInstanceState previously saved state (unused)
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        adapter = new AdminEventsAdapter(this);

        binding.rvEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvEvents.setAdapter(adapter);

        binding.topAppBar.setTitle("Events");
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        loadAllEvents();

        // Search bar: live filtering
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads all events from Firestore and updates the adapter.
     *
     * <p>Data is retrieved via a single `.get()` call and converted to {@link Event} objects.
     * Once loaded, the list is stored in {@link #allEvents} for client-side filtering.
     */
    private void loadAllEvents() {
        binding.progress.setVisibility(View.VISIBLE);

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progress.setVisibility(View.GONE);

                    allEvents = queryDocumentSnapshots.toObjects(Event.class);
                    adapter.submitList(new ArrayList<>(allEvents)); // Use a copy for DiffUtil
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    Log.w(TAG, "Error loading all events", e);
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Filters events by case-insensitive title match.
     *
     * @param query search text entered by the admin; null treated as empty
     */
    private void filter(String query) {
        if (query == null) query = "";
        String q = query.toLowerCase();

        List<Event> filtered;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            filtered = allEvents.stream()
                    .filter(e -> e.getTitle() != null && e.getTitle().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>();
            for (Event e : allEvents) {
                if (e.getTitle() != null && e.getTitle().toLowerCase().contains(q)) {
                    filtered.add(e);
                }
            }
        }

        adapter.submitList(filtered);
    }

    /**
     * Callback invoked when the delete button is pressed for a given event.
     *
     * <p>This method displays a confirmation dialog before removing the event from Firestore.
     *
     * @param event the event selected for deletion
     */
    @Override
    public void onDelete(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event?")
                .setMessage("Are you sure you want to permanently delete '" +
                        event.getTitle() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEventFromFirestore(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Permanently deletes an event from Firestore using its document ID.
     *
     * <p>After deletion succeeds, the event is also removed from the local cache and the UI is
     * refreshed based on the current filtered query.
     *
     * @param event event instance whose ID should be removed from Firestore
     */
    private void deleteEventFromFirestore(Event event) {
        db.collection("events").document(event.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();

                    // Remove from local cache
                    allEvents.remove(event);

                    // Re-filter using current text in the search bar
                    filter(binding.etSearch.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting event", e);
                });
    }
}

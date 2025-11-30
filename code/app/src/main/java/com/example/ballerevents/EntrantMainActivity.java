package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

// The binding class name must match your layout file name.
import com.example.ballerevents.databinding.EntrantMainBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot; // Added for role check
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


import java.util.ArrayList;
import java.util.List;

/**
 * Main home screen for users (Entrants and Organizers).
 *
 * <p>This activity is responsible for:
 * <ul>
 * <li>Showing horizontally scrolling lists of trending and "near you" events.</li>
 * <li>Providing a search bar and category chips to filter all events.</li>
 * <li>Listening in real-time to the {@code events} collection in Firestore.</li>
 * <li>Navigating to {@link DetailsActivity} when an event card is tapped.</li>
 * <li>Navigating to the appropriate Profile/Organizer Dashboard from the top-left menu button based on role.</li>
 * </ul>
 */
public class EntrantMainActivity extends AppCompatActivity {

    private static final String TAG = "EntrantMainActivity";

    /** ViewBinding for the entrant main screen (generated from {@code entrant_main.xml}). */
    private EntrantMainBinding binding;

    /** Adapter for the horizontal "Trending" event list. */
    private TrendingEventAdapter trendingAdapter;

    /** Adapter for the horizontal "Near You" event list. */
    private NearEventAdapter nearAdapter;

    /** Adapter for the vertical search results list (uses same card layout as trending). */
    private TrendingEventAdapter searchAdapter;

    /** Firestore instance used to read the {@code events} collection. */
    private FirebaseFirestore db;

    private FirebaseAuth auth;

    /**
     * In-memory cache of all events loaded from Firestore.
     * Used by the search and chip filtering logic.
     */
    private List<Event> allEvents = new ArrayList<>();

    /** Collection of currently selected category tags from the filter chips. */
    private List<String> selectedTags = new ArrayList<>();

    /** Active Firestore snapshot listener for the {@code events} collection. */
    private ListenerRegistration allEventsListener;

    // QR Code Scanner Launcher
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    String scannedEventId = result.getContents();
                    Log.d(TAG, "Scanned QR Code: " + scannedEventId);

                    if (scannedEventId != null && !scannedEventId.isEmpty()) {
                        // Navigate directly to the event details
                        Intent intent = new Intent(EntrantMainActivity.this, DetailsActivity.class);
                        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, scannedEventId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    /**
     * Called when the activity is first created.
     * Sets up ViewBinding, Firestore, RecyclerViews, and UI listeners.
     *
     * @param savedInstanceState prior state, if any (unused here)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the correct binding class
        binding = EntrantMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // Initialize Auth

        setupRecyclerViews();
        setupListeners();

        // QR Code Button Logic
        binding.fabQr.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan Event QR Code");
            options.setBeepEnabled(false);

            // Use our custom Activity to force portrait mode
            options.setCaptureActivity(PortraitCaptureActivity.class);
            options.setOrientationLocked(true); // Locks to the Activity's orientation (Portrait)

            options.setBarcodeImageEnabled(true);
            barcodeLauncher.launch(options);
        });
    }

    /**
     * Starts the real-time event listener every time the activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        loadAllEvents(); // Attach listener in onStart
    }

    /**
     * Removes the Firestore snapshot listener when the activity is stopped
     * to avoid leaks and unnecessary updates.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (allEventsListener != null) {
            allEventsListener.remove(); // Detach listener in onStop
        }
    }

    /**
     * Configures the RecyclerViews for trending, near-you, and search results.
     */
    private void setupRecyclerViews() {
        // Trending
        trendingAdapter = new TrendingEventAdapter(this::launchDetailsActivity);
        binding.rvTrending.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrending.setAdapter(trendingAdapter);

        // Near You
        nearAdapter = new NearEventAdapter(this::launchDetailsActivity);
        binding.rvNearYou.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvNearYou.setAdapter(nearAdapter);

        // Search
        searchAdapter = new TrendingEventAdapter(this::launchDetailsActivity);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(searchAdapter);
    }

    /**
     * Attaches a real-time snapshot listener to the Firestore {@code events} collection.
     */
    private void loadAllEvents() {
        if (allEventsListener != null) {
            allEventsListener.remove();
        }

        allEventsListener = db.collection("events")
                .orderBy("title", Query.Direction.ASCENDING) // stable order for UI
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) {
                        Log.w(TAG, "Error loading events", e);
                        Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allEvents.clear();
                    List<Event> trending = new ArrayList<>();
                    List<Event> near = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        allEvents.add(event);
                        if (event.isTrending()) {
                            trending.add(event);
                        } else {
                            near.add(event);
                        }
                    }

                    // Submit copies to avoid accidental adapter list mutation issues
                    trendingAdapter.submitList(new ArrayList<>(trending));
                    nearAdapter.submitList(new ArrayList<>(near));

                    // Re-apply filter in case data changed while searching
                    performSearchAndFilter();

                    Log.d(TAG, "Loaded " + allEvents.size() + " total events.");
                });
    }

    /**
     * Configures UI listeners.
     * Modified: The menu button now performs a role check to determine the destination.
     */
    private void setupListeners() {
        // Menu button logic: Check role then navigate
        binding.btnMenu.setOnClickListener(v -> handleMenuNavigation());

        // Open notifications when bell icon is tapped
        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationLogsActivity.class);
            startActivity(intent);
        });

        // Search Bar
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearchAndFilter();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip Listeners for event categories
        setupChipListener(binding.chipMusic);
        setupChipListener(binding.chipExhibition);
        setupChipListener(binding.chipStandUp);
        setupChipListener(binding.chipTheater);
    }

    /**
     * Checks the current user's role in Firestore and navigates to the
     * appropriate profile activity:
     * <ul>
     * <li><b>Organizer</b> -> {@link OrganizerActivity}</li>
     * <li><b>Entrant</b> -> {@link ProfileActivity}</li>
     * </ul>
     */
    private void handleMenuNavigation() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if ("organizer".equals(role)) {
                            // If user is an organizer, go to the Organizer Dashboard
                            startActivity(new Intent(EntrantMainActivity.this, OrganizerActivity.class));
                        } else {
                            // Default for entrants (and admin browsing as entrant)
                            startActivity(new Intent(EntrantMainActivity.this, ProfileActivity.class));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user role", e);
                    // Fallback to basic profile on error
                    startActivity(new Intent(EntrantMainActivity.this, ProfileActivity.class));
                });
    }

    /**
     * Helper method that attaches a checked-change listener to a single filter chip.
     */
    private void setupChipListener(Chip chip) {
        chip.setOnCheckedChangeListener((button, isChecked) -> {
            String tag = chip.getText().toString();
            if (isChecked) {
                if (!selectedTags.contains(tag)) selectedTags.add(tag);
            } else {
                selectedTags.remove(tag);
            }
            performSearchAndFilter();
        });
    }

    /**
     * Applies search and tag filters to the cached events and updates the UI.
     */
    private void performSearchAndFilter() {
        String query = binding.etSearch.getText().toString();

        if (query.isEmpty() && selectedTags.isEmpty()) {
            binding.originalContentLayout.setVisibility(View.VISIBLE);
            binding.searchResultsLayout.setVisibility(View.GONE);
            return;
        }

        binding.originalContentLayout.setVisibility(View.GONE);
        binding.searchResultsLayout.setVisibility(View.VISIBLE);

        List<Event> filteredResults =
                EventFilter.performSearchAndFilter(allEvents, query, selectedTags);

        searchAdapter.submitList(filteredResults);

        if (filteredResults.isEmpty()) {
            binding.tvNoResults.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoResults.setVisibility(View.GONE);
        }
    }

    /**
     * Launches {@link DetailsActivity} for the given event.
     */
    private void launchDetailsActivity(Event event) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(intent);
    }
}
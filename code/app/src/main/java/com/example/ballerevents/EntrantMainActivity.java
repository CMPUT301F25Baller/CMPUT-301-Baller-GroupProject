package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

// The binding class name must match your layout file name.
// Your file is 'activity_main.xml', so this is correct.
import com.example.ballerevents.databinding.EntrantMainBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration; // Import for listener
import com.google.firebase.firestore.FirebaseFirestoreException; // Import for listener

import java.util.ArrayList;
import java.util.List;

/**
 * Main home screen for users with the <b>entrant</b> role.
 *
 * <p>This activity is responsible for:
 * <ul>
 *     <li>Showing horizontally scrolling lists of trending and "near you" events.</li>
 *     <li>Providing a search bar and category chips to filter all events.</li>
 *     <li>Listening in real-time to the {@code events} collection in Firestore.</li>
 *     <li>Navigating to {@link DetailsActivity} when an event card is tapped.</li>
 *     <li>Navigating to {@link ProfileActivity} from the top-left menu button.</li>
 * </ul>
 *
 * <p>Data Flow:
 * <ol>
 *     <li>{@link #loadAllEvents()} attaches a Firestore snapshot listener on {@code events}.</li>
 *     <li>All events are cached in {@link #allEvents} for client-side search/filter.</li>
 *     <li>Trending vs Near-You separation is based on {@link Event#isTrending()}.</li>
 *     <li>{@link #performSearchAndFilter()} delegates filtering logic to {@code EventFilter.performSearchAndFilter}.</li>
 * </ol>
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

    /**
     * In-memory cache of all events loaded from Firestore.
     * Used by the search and chip filtering logic.
     */
    private List<Event> allEvents = new ArrayList<>();

    /** Collection of currently selected category tags from the filter chips. */
    private List<String> selectedTags = new ArrayList<>();

    /** Active Firestore snapshot listener for the {@code events} collection. */
    private ListenerRegistration allEventsListener;

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

        setupRecyclerViews();
        setupListeners();
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
     * Configures the RecyclerViews for:
     * <ul>
     *     <li>Trending events (horizontal)</li>
     *     <li>Near-You events (horizontal)</li>
     *     <li>Search results (vertical)</li>
     * </ul>
     *
     * Adapters are initialized with a click callback that opens
     * {@link DetailsActivity} for the selected event.
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
     *
     * <p>Behavior:
     * <ul>
     *     <li>Removes any existing listener before attaching a new one.</li>
     *     <li>Orders events by title for stable UI ordering.</li>
     *     <li>Splits results into "Trending" and "Near You" lists based on {@link Event#isTrending()}.</li>
     *     <li>Re-applies search/filter conditions after every snapshot via {@link #performSearchAndFilter()}.</li>
     * </ul>
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
     * Configures UI listeners:
     * <ul>
     *     <li>Menu button → opens {@link ProfileActivity}.</li>
     *     <li>Search EditText → triggers {@link #performSearchAndFilter()} on text change.</li>
     *     <li>Category chips → toggles tags and re-filters.</li>
     * </ul>
     */
    private void setupListeners() {
        // Open Profile screen when the menu button is tapped
        binding.btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // 🔔 Open notifications when bell icon is tapped
        binding.btnNotifications.setOnClickListener(v -> {
            // TODO: pick the correct activity for your notifications UI
            Intent intent = new Intent(this, NotificationLogsActivity.class);
            // or: new Intent(this, NotificationLogsActivity.class);
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
     * Helper method that attaches a checked-change listener to a single filter chip.
     *
     * <p>When checked, the chip's text is added to {@link #selectedTags}.
     * When unchecked, it is removed. The filter is reapplied immediately.</p>
     *
     * @param chip the {@link Chip} to configure
     */
    private void setupChipListener(Chip chip) {
        // Use setOnCheckedChangeListener for filter chips
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
     * Applies search and tag filters to the cached events and updates the UI:
     * <ul>
     *     <li>If no query and no tags are active, shows the "original content" layout.</li>
     *     <li>Otherwise, shows the search results container.</li>
     *     <li>Filtering logic is delegated to {@link EventFilter#performSearchAndFilter(List, String, List)}.</li>
     *     <li>Displays or hides a "No Results" message accordingly.</li>
     * </ul>
     */
    private void performSearchAndFilter() {
        // Get the raw query. The filter class will normalize it.
        String query = binding.etSearch.getText().toString();

        // If query and tags are empty, show original content
        if (query.isEmpty() && selectedTags.isEmpty()) {
            binding.originalContentLayout.setVisibility(View.VISIBLE);
            binding.searchResultsLayout.setVisibility(View.GONE);
            return;
        }

        // Show search results
        binding.originalContentLayout.setVisibility(View.GONE);
        binding.searchResultsLayout.setVisibility(View.VISIBLE);

        // --- REFACTORED PART ---
        // Call the static helper method from our testable class
        List<Event> filteredResults =
                EventFilter.performSearchAndFilter(allEvents, query, selectedTags);
        // --- END OF REFACTOR ---

        searchAdapter.submitList(filteredResults);

        // Show "No Results" message
        if (filteredResults.isEmpty()) {
            binding.tvNoResults.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoResults.setVisibility(View.GONE);
        }
    }

    /**
     * Launches {@link DetailsActivity} for the given event.
     *
     * @param event the {@link Event} whose details should be displayed
     */
    private void launchDetailsActivity(Event event) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId()); // Pass String ID
        startActivity(intent);
    }
}

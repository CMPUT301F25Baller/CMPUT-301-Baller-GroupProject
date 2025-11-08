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
 * The main home screen for users with the "entrant" role.
 * Displays lists of trending and near-you events, and allows users
 * to search and filter all events.
 */
public class EntrantMainActivity extends AppCompatActivity {

    private static final String TAG = "EntrantMainActivity";

    /** This binding class is generated from 'activity_main.xml' */
    private EntrantMainBinding binding;
    private TrendingEventAdapter trendingAdapter;
    private NearEventAdapter nearAdapter;
    private TrendingEventAdapter searchAdapter; // Use Trending layout for search

    private FirebaseFirestore db;
    private List<Event> allEvents = new ArrayList<>(); // Cache all events for searching
    private List<String> selectedTags = new ArrayList<>();

    private ListenerRegistration allEventsListener; // Listener registration

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

    @Override
    protected void onStart() {
        super.onStart();
        loadAllEvents(); // Attach listener in onStart
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (allEventsListener != null) {
            allEventsListener.remove(); // Detach listener in onStop
        }
    }

    /**
     * Initializes the RecyclerViews for trending, near-you, and search results.
     */
    private void setupRecyclerViews() {
        // Trending
        trendingAdapter = new TrendingEventAdapter(this::launchDetailsActivity);
        binding.rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrending.setAdapter(trendingAdapter);

        // Near You
        nearAdapter = new NearEventAdapter(this::launchDetailsActivity);
        binding.rvNearYou.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvNearYou.setAdapter(nearAdapter);

        // Search
        searchAdapter = new TrendingEventAdapter(this::launchDetailsActivity);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(searchAdapter);
    }

    /**
     * Attaches a snapshot listener to the "events" collection in Firestore.
     * The list of all events, trending events, and near-you events will
     * update in real-time.
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
                        if (event.isTrending()) trending.add(event); else near.add(event);
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
     * Sets up click listeners for the profile menu, search bar, and filter chips.
     */
    private void setupListeners() {
        binding.btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
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

        // Chip Listeners
        setupChipListener(binding.chipMusic);
        setupChipListener(binding.chipExhibition);
        setupChipListener(binding.chipStandUp);
        setupChipListener(binding.chipTheater);
    }

    /**
     * Helper method to add a checked-change listener to a filter chip.
     * @param chip The Chip to attach the listener to.
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
     * Toggles view visibility and applies the current search query and tag filters
     * using the {@link EventFilter} helper class.
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
        List<Event> filteredResults = EventFilter.performSearchAndFilter(allEvents, query, selectedTags);
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
     * Navigates to the DetailsActivity for a specific event.
     * @param event The Event object to display details for.
     */
    private void launchDetailsActivity(Event event) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId()); // Pass String ID
        startActivity(intent);
    }
}
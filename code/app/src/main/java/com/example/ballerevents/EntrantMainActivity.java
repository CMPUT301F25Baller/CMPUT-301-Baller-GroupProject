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

import com.example.ballerevents.databinding.EntrantMainBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EntrantMainActivity extends AppCompatActivity {

    private static final String TAG = "EntrantMainActivity";

    private EntrantMainBinding binding;
    private TrendingEventAdapter trendingAdapter;
    private NearEventAdapter nearAdapter;
    private TrendingEventAdapter searchAdapter; // Use Trending layout for search

    private FirebaseFirestore db;
    private List<Event> allEvents = new ArrayList<>(); // Cache all events for searching
    private List<String> selectedTags = new ArrayList<>();
    private static String s(String v) { return v == null ? "" : v; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupRecyclerViews();
        setupListeners();

        loadAllEvents(); // Load all events into cache
    }

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

    private void loadAllEvents() {
        db.collection("events")
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

                        // Make sure ID is set even if @DocumentId doesn’t populate
                        event.setId(doc.getId());

                        allEvents.add(event);
                        if (event.isTrending()) trending.add(event); else near.add(event);
                    }

                    // Submit copies to avoid accidental adapter list mutation issues
                    trendingAdapter.submitList(new ArrayList<>(trending));
                    nearAdapter.submitList(new ArrayList<>(near));

                    Log.d(TAG, "Loaded " + allEvents.size() + " total events.");
                });
    }


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


    private void performSearchAndFilter() {
        String query = binding.etSearch.getText().toString().toLowerCase().trim();

        // If query and tags are empty, show original content
        if (query.isEmpty() && selectedTags.isEmpty()) {
            binding.originalContentLayout.setVisibility(View.VISIBLE);
            binding.searchResultsLayout.setVisibility(View.GONE);
            return;
        }

        // Show search results
        binding.originalContentLayout.setVisibility(View.GONE);
        binding.searchResultsLayout.setVisibility(View.VISIBLE);

        List<Event> filteredResults = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            filteredResults = allEvents.stream()
                    .filter(event -> {
                        // Match Search Query
                        boolean matchesQuery = query.isEmpty() ||
                                event.getTitle().toLowerCase().contains(query) ||
                                event.getDescription().toLowerCase().contains(query) ||
                                event.getOrganizer().toLowerCase().contains(query);

                        // Match Tags
                        boolean matchesTags = selectedTags.isEmpty() ||
                                (event.getTags() != null && event.getTags().containsAll(selectedTags));

                        return matchesQuery && matchesTags;
                    })
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // Fallback for older Android
            for (Event event : allEvents) {
                boolean matchesQuery = query.isEmpty() ||
                        event.getTitle().toLowerCase().contains(query) ||
                        event.getDescription().toLowerCase().contains(query) ||
                        event.getOrganizer().toLowerCase().contains(query);

                boolean matchesTags = selectedTags.isEmpty() ||
                        (event.getTags() != null && event.getTags().containsAll(selectedTags));

                if (matchesQuery && matchesTags) {
                    filteredResults.add(event);
                }
            }
        }

        searchAdapter.submitList(filteredResults);

        // Show "No Results" message
        if (filteredResults.isEmpty()) {
            binding.tvNoResults.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoResults.setVisibility(View.GONE);
        }
    }

    private void launchDetailsActivity(Event event) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId()); // Pass String ID
        startActivity(intent);
    }
}
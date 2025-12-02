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
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.EntrantMainBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrantMainActivity extends AppCompatActivity {

    private static final String TAG = "EntrantMainActivity";
    private EntrantMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TrendingEventAdapter trendingAdapter;
    private NearEventAdapter nearAdapter;
    private TrendingEventAdapter searchAdapter;

    private List<Event> allEvents = new ArrayList<>();
    private List<String> selectedTags = new ArrayList<>();
    private ListenerRegistration allEventsListener;

    // Filter Date Range (Null if cleared)
    private Date startDateFilter = null;
    private Date endDateFilter = null;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    Intent intent = new Intent(EntrantMainActivity.this, DetailsActivity.class);
                    intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, result.getContents());
                    startActivity(intent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerViews();
        setupListeners();

        binding.fabQr.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan Event QR Code");
            options.setBeepEnabled(false);
            options.setCaptureActivity(PortraitCaptureActivity.class);
            options.setOrientationLocked(true);
            barcodeLauncher.launch(options);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAllEvents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (allEventsListener != null) allEventsListener.remove();
    }

    private void setupRecyclerViews() {
        trendingAdapter = new TrendingEventAdapter(this::launchDetailsActivity);
        binding.rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrending.setAdapter(trendingAdapter);

        nearAdapter = new NearEventAdapter(this::launchDetailsActivity);
        binding.rvNearYou.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvNearYou.setAdapter(nearAdapter);

        searchAdapter = new TrendingEventAdapter(this::launchDetailsActivity);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(searchAdapter);
    }

    private void loadAllEvents() {
        if (allEventsListener != null) allEventsListener.remove();

        allEventsListener = db.collection("events")
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    allEvents.clear();
                    List<Event> trending = new ArrayList<>();
                    List<Event> near = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;
                        allEvents.add(event);
                        if (event.isTrending()) trending.add(event);
                        else near.add(event);
                    }

                    trendingAdapter.submitList(new ArrayList<>(trending));
                    nearAdapter.submitList(new ArrayList<>(near));
                    performSearchAndFilter();
                });
    }

    private void setupListeners() {
        binding.btnMenu.setOnClickListener(v -> handleMenuNavigation());
        binding.btnNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationLogsActivity.class)));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { performSearchAndFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        setupChipListener(binding.chipMusic);
        setupChipListener(binding.chipExhibition);
        setupChipListener(binding.chipStandUp);
        setupChipListener(binding.chipTheater);

        // NEW: Date Filter Listener
        binding.btnFilterDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Event Dates")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                startDateFilter = new Date(selection.first);
                endDateFilter = new Date(selection.second);

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.US);
                String dateText = sdf.format(startDateFilter) + " - " + sdf.format(endDateFilter);
                binding.btnFilterDate.setText(dateText);

                performSearchAndFilter();
            }
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void handleMenuNavigation() {
        if (auth.getCurrentUser() == null) return;
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && "organizer".equals(doc.getString("role"))) {
                        startActivity(new Intent(this, OrganizerActivity.class));
                    } else {
                        startActivity(new Intent(this, ProfileActivity.class));
                    }
                });
    }

    private void setupChipListener(Chip chip) {
        chip.setOnCheckedChangeListener((button, isChecked) -> {
            String tag = chip.getText().toString();
            if (isChecked) { if (!selectedTags.contains(tag)) selectedTags.add(tag); }
            else { selectedTags.remove(tag); }
            performSearchAndFilter();
        });
    }

    private void performSearchAndFilter() {
        String query = binding.etSearch.getText().toString();

        // If all filters match defaults/empty, show main layout
        boolean hasFilters = !query.isEmpty() || !selectedTags.isEmpty() || startDateFilter != null;

        if (!hasFilters) {
            binding.originalContentLayout.setVisibility(View.VISIBLE);
            binding.searchResultsLayout.setVisibility(View.GONE);
            // Reset Date Button Text
            binding.btnFilterDate.setText("Date");
            return;
        }

        binding.originalContentLayout.setVisibility(View.GONE);
        binding.searchResultsLayout.setVisibility(View.VISIBLE);

        List<Event> filteredResults = EventFilter.performSearchAndFilter(
                allEvents, query, selectedTags, startDateFilter, endDateFilter);

        searchAdapter.submitList(filteredResults);
        binding.tvNoResults.setVisibility(filteredResults.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void launchDetailsActivity(Event event) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(intent);
    }
}
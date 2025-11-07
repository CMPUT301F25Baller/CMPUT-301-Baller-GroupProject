package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminDashboardBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
/**
 * Admin landing screen that shows horizontally scrolling lists for
 * recent events, profiles, and posters. Data is loaded from Firestore
 * and adapters are wired for navigation to the full list screens.
 *
 * Navigation targets: {@link AdminEventsActivity}, {@link AdminProfilesActivity},
 * {@link AdminImagesActivity} and a stubbed logs chip.
 */


public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";
    private AdminDashboardBinding binding;
    private FirebaseFirestore db;

    private TrendingEventAdapter eventsAdapter;
    private AdminProfilesAdapter profilesAdapter;
    private AdminPostersAdapter postersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupRecyclerViews();
        setupAdapters();
        loadDashboardData();
        setupNavigation();
    }

    private void setupRecyclerViews() {
        binding.rvEvents.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvProfiles.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupAdapters() {
        // For Events, we can re-use the existing TrendingEventAdapter
        eventsAdapter = new TrendingEventAdapter(event -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(i);
        });

        // Use the Admin-specific adapters
        profilesAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Implement profile click
        });
        postersAdapter = new AdminPostersAdapter(event -> {
            // TODO: Implement poster click
        });

        binding.rvEvents.setAdapter(eventsAdapter);
        binding.rvProfiles.setAdapter(profilesAdapter);
        binding.rvImages.setAdapter(postersAdapter);
    }

    private void loadDashboardData() {
        // Load recent 10 events
        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING) // Example: order by date
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = queryDocumentSnapshots.toObjects(Event.class);
                    eventsAdapter.submitList(events);
                    // Also use this list for the "images" panel
                    postersAdapter.submitList(events);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading recent events", e));

        db.collection("users")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> profiles = queryDocumentSnapshots.toObjects(UserProfile.class);
                    profilesAdapter.submitList(profiles);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading recent profiles", e));
    }

    private void setupNavigation() {
        // === Navigation: open the Admin Events list screen ===
        binding.chipEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        binding.btnSeeAllEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        // === Navigation for Profiles ===
        binding.chipPeople.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminProfilesActivity.class));
        });

        binding.btnSeeAllProfiles.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfilesActivity.class)));

        // === Navigation for Images ===
        binding.chipImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));

        binding.btnSeeAllImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));

        // === Stub for Logs ===
        binding.chipLogs.setOnClickListener(v ->
                        Toast.makeText(this, "NotificationLogsActivity not yet implemented.", Toast.LENGTH_SHORT).show()
                // startActivity(new Intent(this, NotificationLogsActivity.class))
        );
    }
}
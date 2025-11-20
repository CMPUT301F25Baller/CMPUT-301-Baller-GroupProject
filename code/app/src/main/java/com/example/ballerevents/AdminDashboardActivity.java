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
 * Represents the administrator landing dashboard screen.
 *
 * <p>This activity displays three horizontally scrolling lists:
 * <ul>
 *     <li><b>Recent Events</b> – Latest event documents from Firestore.</li>
 *     <li><b>Recent Profiles</b> – Recent user profiles from Firestore.</li>
 *     <li><b>Recent Posters</b> – Uses event posters rendered via event adapter.</li>
 * </ul>
 *
 * <p>From this dashboard, the admin can navigate to:
 * <ul>
 *     <li>{@link AdminEventsActivity} – Full list of events</li>
 *     <li>{@link AdminProfilesActivity} – Full list of user profiles</li>
 *     <li>{@link AdminImagesActivity} – Full list of event posters</li>
 *     <li>Notification logs (placeholder stub)</li>
 * </ul>
 *
 * <p>This class reads Firestore data using one-time `.get()` calls and fills
 * the respective adapters. No real-time streaming is needed for this prototype.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    /** Log tag for debugging. */
    private static final String TAG = "AdminDashboard";

    /** ViewBinding for the admin dashboard layout. */
    private AdminDashboardBinding binding;

    /** Firebase Firestore reference for loading admin dashboard data. */
    private FirebaseFirestore db;

    /** Adapter for rendering recent event cards. */
    private TrendingEventAdapter eventsAdapter;

    /** Adapter for rendering recent profile items. */
    private AdminProfilesAdapter profilesAdapter;

    /** Adapter for rendering recent event posters. */
    private AdminPostersAdapter postersAdapter;

    /**
     * Initializes the dashboard, configures RecyclerViews,
     * initializes adapters, loads Firestore data, and attaches navigation handlers.
     *
     * @param savedInstanceState previously saved UI state (unused)
     */
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

    /**
     * Configures the layout managers for the three horizontal RecyclerViews.
     * Each list scrolls horizontally.
     */
    private void setupRecyclerViews() {
        binding.rvEvents.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        binding.rvProfiles.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        binding.rvImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    /**
     * Initializes the adapters used by the dashboard:
     * <ul>
     *     <li>TrendingEventAdapter – reused for the events section</li>
     *     <li>AdminProfilesAdapter – admin-specific adapter for profiles</li>
     *     <li>AdminPostersAdapter – admin-specific event poster adapter</li>
     * </ul>
     *
     * Also binds click listeners where applicable.
     */
    private void setupAdapters() {

        // Events (reusing existing adapter)
        eventsAdapter = new TrendingEventAdapter(event -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(i);
        });

        // Profiles list (admin-specific)
        profilesAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Implement profile click if needed for admin prototype
        });

        // Posters list (admin-specific)
        postersAdapter = new AdminPostersAdapter(event -> {
            // TODO: Implement poster click if needed for admin prototype
        });

        binding.rvEvents.setAdapter(eventsAdapter);
        binding.rvProfiles.setAdapter(profilesAdapter);
        binding.rvImages.setAdapter(postersAdapter);
    }

    /**
     * Loads recent events and recent user profiles from Firestore.
     *
     * <p>For this prototype:
     * <ul>
     *     <li>Events are ordered by descending date.</li>
     *     <li>User profiles are simply the newest 10 documents.</li>
     * </ul>
     *
     * Loaded data is submitted to the respective adapters.
     */
    private void loadDashboardData() {

        // === Load recent events ===
        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = queryDocumentSnapshots.toObjects(Event.class);
                    eventsAdapter.submitList(events);

                    // The "images" panel also uses event objects
                    postersAdapter.submitList(events);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading recent events", e));

        // === Load recent profiles ===
        db.collection("users")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> profiles = queryDocumentSnapshots.toObjects(UserProfile.class);
                    profilesAdapter.submitList(profiles);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading recent profiles", e));
    }

    /**
     * Attaches click listeners to all navigation elements in the dashboard:
     * <ul>
     *     <li>Events chip + "See all"</li>
     *     <li>Profiles chip + "See all"</li>
     *     <li>Images chip + "See all"</li>
     *     <li>Logs chip (placeholder)</li>
     * </ul>
     */
    private void setupNavigation() {

        // Events navigation
        binding.chipEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        binding.btnSeeAllEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        // Profiles navigation
        binding.chipPeople.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfilesActivity.class)));

        binding.btnSeeAllProfiles.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfilesActivity.class)));

        // Images navigation
        binding.chipImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));

        binding.btnSeeAllImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));

        // Logs (stub)
        binding.chipLogs.setOnClickListener(v ->
                Toast.makeText(this, "NotificationLogsActivity not yet implemented.", Toast.LENGTH_SHORT).show()
        );
    }
}

package com.example.ballerevents;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.AdminDashboardBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * Administrator landing dashboard screen.
 *
 * Shows three horizontal lists:
 *  - Recent Events
 *  - Recent Profiles
 *  - Recent Posters
 *
 * From here admin can navigate to:
 *  - AdminEventsActivity (events list)
 *  - AdminProfilesActivity (profiles list)
 *  - AdminImagesActivity (posters grid)
 *  - NotificationLogsActivity (notification audit logs)
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

    /** Configure horizontal RecyclerViews. */
    private void setupRecyclerViews() {
        binding.rvEvents.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvProfiles.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    /** Initialize adapters and bind click actions. */
    private void setupAdapters() {
        // Events (reuse existing adapter)
        eventsAdapter = new TrendingEventAdapter(event -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(i);
        });

        // Profiles (admin-specific)
        profilesAdapter = new AdminProfilesAdapter(profile -> {
            // optional: open profile details if desired
            // startActivity(new Intent(this, ProfileDetailsActivity.class)
            //        .putExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID, profile.getId()));
        });

        // Posters (admin-specific) — use anonymous class (two abstract methods)
        postersAdapter = new AdminPostersAdapter(new AdminPostersAdapter.PosterActions() {
            @Override
            public void onPreview(@NonNull Event event) {
                String url = event.getEventPosterUrl();
                if (url == null || url.isEmpty()) {
                    Toast.makeText(AdminDashboardActivity.this, "No poster to preview", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Inflate preview dialog
                final ImageView iv = (ImageView) getLayoutInflater()
                        .inflate(R.layout.dialog_image_preview, null, false)
                        .findViewById(R.id.ivPreview);

                Glide.with(AdminDashboardActivity.this)
                        .load(url)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .fitCenter()
                        .into(iv);

                new AlertDialog.Builder(AdminDashboardActivity.this)
                        .setView(iv.getRootView()) // the FrameLayout root from dialog_image_preview
                        .setPositiveButton("Close", (d, w) -> d.dismiss())
                        .show();
            }

            @Override
            public void onDelete(@NonNull Event event) {
                // Dashboard grid is read-only; guide user to Posters screen for deletion
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "Hold to delete in Posters screen (Admin → Images).",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        binding.rvEvents.setAdapter(eventsAdapter);
        binding.rvProfiles.setAdapter(profilesAdapter);
        binding.rvImages.setAdapter(postersAdapter);
    }

    /** Load recent events and profiles from Firestore. */
    private void loadDashboardData() {
        // Recent events (also feed posters row)
        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = queryDocumentSnapshots.toObjects(Event.class);
                    eventsAdapter.submitList(events);
                    postersAdapter.submitList(events);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading recent events", e));

        // Recent profiles
        db.collection("users")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> profiles = queryDocumentSnapshots.toObjects(UserProfile.class);
                    profilesAdapter.submitList(profiles);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading recent profiles", e));
    }

    /** Wire dashboard navigation. */
    private void setupNavigation() {
        // Events
        binding.chipEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminEventsActivity.class)));
        binding.btnSeeAllEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminEventsActivity.class)));

        // Profiles
        binding.chipPeople.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfilesActivity.class)));
        binding.btnSeeAllProfiles.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfilesActivity.class)));

        // Images (posters grid)
        binding.chipImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));
        binding.btnSeeAllImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));

        // Logs → open the implemented logs screen
        binding.chipLogs.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationLogsActivity.class)));
        // If you have a "See all" button for logs, wire it similarly:
        // binding.btnSeeAllLogs.setOnClickListener(v ->
        //         startActivity(new Intent(this, NotificationLogsActivity.class)));
    }
}

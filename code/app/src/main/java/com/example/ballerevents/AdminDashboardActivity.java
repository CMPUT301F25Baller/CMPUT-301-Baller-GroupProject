package com.example.ballerevents;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";
    private AdminDashboardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupNavigation();
        loadRecentEvents();
        loadRecentProfiles();
        loadRecentPosters();
        setupLogout();
    }

    private void setupNavigation() {
        binding.btnNavEvents.setOnClickListener(v -> startActivity(new Intent(this, AdminEventsActivity.class)));
        binding.btnSeeAllEvents.setOnClickListener(v -> startActivity(new Intent(this, AdminEventsActivity.class)));

        binding.btnNavProfiles.setOnClickListener(v -> startActivity(new Intent(this, AdminProfilesActivity.class)));
        binding.btnSeeAllProfiles.setOnClickListener(v -> startActivity(new Intent(this, AdminProfilesActivity.class)));

        binding.btnNavImages.setOnClickListener(v -> startActivity(new Intent(this, AdminImagesActivity.class)));
        binding.btnSeeAllImages.setOnClickListener(v -> startActivity(new Intent(this, AdminImagesActivity.class)));

        binding.btnNavLogs.setOnClickListener(v -> startActivity(new Intent(this, AdminLogsActivity.class)));
    }

    private void setupLogout() {
        binding.btnLogoutIcon.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadRecentEvents() {
        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            e.setId(doc.getId());
                            events.add(e);
                        }
                    }

                    // Use Adapter with corrected Keys
                    AdminEventsAdapter adapter = new AdminEventsAdapter(new AdminEventsAdapter.OnEventActionListener() {
                        @Override
                        public void onEventClick(Event event) {
                            Intent intent = new Intent(AdminDashboardActivity.this, DetailsActivity.class);
                            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(intent);
                        }

                        @Override
                        public void onDelete(Event event) {
                            db.collection("events").document(event.getId()).delete()
                                    .addOnSuccessListener(a -> loadRecentEvents());
                        }
                    });

                    binding.rvRecentEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rvRecentEvents.setAdapter(adapter);
                    adapter.submitList(events);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading recent events", e));
    }

    private void loadRecentProfiles() {
        db.collection("users")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> profiles = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserProfile p = doc.toObject(UserProfile.class);
                        if (p != null) {
                            p.setUid(doc.getId());
                            profiles.add(p);
                        }
                    }

                    AdminProfilesAdapter adapter = new AdminProfilesAdapter(new AdminProfilesAdapter.OnProfileActionListener() {
                        @Override
                        public void onProfileClick(UserProfile profile) {
                            Intent intent = new Intent(AdminDashboardActivity.this, ProfileDetailsActivity.class);
                            intent.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID, profile.getUid());
                            startActivity(intent);
                        }

                        @Override
                        public void onDelete(UserProfile profile) {
                            db.collection("users").document(profile.getId()).delete()
                                    .addOnSuccessListener(a -> loadRecentProfiles());
                        }
                    });

                    binding.rvRecentProfiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rvRecentProfiles.setAdapter(adapter);
                    adapter.submitList(profiles);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading recent profiles", e));
    }

    private void loadRecentPosters() {
        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> eventsWithPosters = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event e = doc.toObject(Event.class);
                        if (e != null && e.getEventPosterUrl() != null && !e.getEventPosterUrl().isEmpty()) {
                            e.setId(doc.getId());
                            eventsWithPosters.add(e);
                        }
                        if (eventsWithPosters.size() >= 5) break;
                    }
                    AdminPostersAdapter adapter = new AdminPostersAdapter(event -> {
                        startActivity(new Intent(this, AdminImagesActivity.class));
                    });
                    binding.rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rvImages.setAdapter(adapter);
                    adapter.submitList(eventsWithPosters);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading posters", e));
    }
}
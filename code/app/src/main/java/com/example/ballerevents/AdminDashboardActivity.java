package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
        binding.chipEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        binding.btnSeeAllEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        binding.chipPeople.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfilesActivity.class)));

        binding.btnSeeAllProfiles.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfilesActivity.class)));

        binding.chipImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));

        binding.btnSeeAllImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminImagesActivity.class)));

        binding.chipLogs.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLogsActivity.class))
        );
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadRecentEvents() {
        db.collection("events")
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = queryDocumentSnapshots.toObjects(Event.class);
                    // Manually assign IDs since .toObjects() doesn't
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        events.get(i).setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                    }

                    AdminEventsAdapter adapter = new AdminEventsAdapter(event -> {
                        Toast.makeText(this, "Clicked event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
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
                    List<UserProfile> profiles = queryDocumentSnapshots.toObjects(UserProfile.class);
                    // Assign IDs
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        profiles.get(i).setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                    }

                    AdminProfilesAdapter adapter = new AdminProfilesAdapter(profile -> {
                        Toast.makeText(this, "Clicked user: " + profile.getName(), Toast.LENGTH_SHORT).show();
                    });
                    binding.rvRecentProfiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rvRecentProfiles.setAdapter(adapter);
                    adapter.submitList(profiles);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading recent profiles", e));
    }

    private void loadRecentPosters() {
        db.collection("events")
                .whereNotEqualTo("eventPosterUrl", "")
                .limit(6)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = queryDocumentSnapshots.toObjects(Event.class);
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        events.get(i).setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                    }

                    AdminPostersAdapter adapter = new AdminPostersAdapter(event -> {
                        Toast.makeText(this, "Poster for " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    });
                    binding.rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    binding.rvImages.setAdapter(adapter);
                    adapter.submitList(events);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading posters", e));
    }
}
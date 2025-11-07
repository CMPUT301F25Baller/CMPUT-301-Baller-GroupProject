package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityProfileBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private DocumentReference userRef;
    private ListenerRegistration userListener;

    private UserProfile userProfile;

    // Reuse existing adapter & row for the “Joined Events” list
    private TrendingEventAdapter joinedAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRef = db.collection("users").document(auth.getCurrentUser().getUid());

        setupRecycler();
        setupClicks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for profile changes
        userListener = userRef.addSnapshotListener(this, (snap, e) -> {
            if (e != null) {
                Log.w(TAG, "Profile listen failed", e);
                return;
            }
            if (snap == null) return;

            if (!snap.exists()) {
                // Create a minimal profile if missing
                userRef.set(new UserProfile());
                return;
            }
            userProfile = snap.toObject(UserProfile.class);
            if (userProfile != null) {
                bindProfile(userProfile);
                loadJoinedEvents(userProfile.getAppliedEventIds());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    private void setupRecycler() {
        joinedAdapter = new TrendingEventAdapter(this::openEventDetails);
        binding.rvJoinedEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedEvents.setAdapter(joinedAdapter);
    }

    private void setupClicks() {
        binding.btnBackProfile.setOnClickListener(v -> finish());

        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        binding.btnNotificationLogs.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationLogsActivity.class)));
    }

    private void bindProfile(UserProfile p) {
        // Name & email
        binding.tvProfileName.setText(nullSafe(p.getName()));
        binding.tvProfileEmail.setText(nullSafe(p.getEmail()));

        // Avatar
        Glide.with(this)
                .load(nullSafe(p.getProfilePictureUrl()))
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .into(binding.ivProfilePicture);

        // Followers / Following (placeholder numbers if not modeled yet)
        // If you later add fields, set them here.
        binding.tvFollowerCount.setText("0");
        binding.tvFollowingCount.setText("0");

        // Interests as chips
        binding.chipGroupInterests.removeAllViews();
        List<String> interests = p.getInterests();
        if (interests != null && !interests.isEmpty()) {
            for (String it : interests) {
                Chip chip = new Chip(this);
                chip.setText(it);
                chip.setCheckable(false);
                binding.chipGroupInterests.addView(chip);
            }
        }
    }

    private void loadJoinedEvents(@Nullable List<String> appliedIds) {
        if (appliedIds == null || appliedIds.isEmpty()) {
            joinedAdapter.submitList(new ArrayList<>());
            return;
        }

        // Fetch each event doc by id (simple & reliable for prototype).
        // If this grows large, batch with whereIn in groups of 10.
        List<Event> results = new ArrayList<>();
        binding.rvJoinedEvents.setVisibility(View.VISIBLE);

        for (String id : appliedIds) {
            db.collection("events").document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            // ensure id is set
                            try {
                                // add a setter in Event if you haven’t already
                                e.getClass().getMethod("setId", String.class).invoke(e, doc.getId());
                            } catch (Exception ignore) { /* no-op */ }
                            results.add(e);
                            // update list as they arrive (keeps UI responsive)
                            joinedAdapter.submitList(new ArrayList<>(results));
                        }
                    })
                    .addOnFailureListener(err -> Log.w(TAG, "load event failed: " + id, err));
        }
    }

    private void openEventDetails(Event event) {
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(i);
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}

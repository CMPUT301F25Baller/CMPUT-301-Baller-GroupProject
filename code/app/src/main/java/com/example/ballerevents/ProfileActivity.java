package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityProfileBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query; // Import Query
import com.google.firebase.firestore.FieldPath; // Import FieldPath

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the current user's profile information, including their "About Me,"
 * "Interests," and a list of events they have joined.
 * Data is loaded in real-time from Firestore.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ActivityProfileBinding binding;
    private NearEventAdapter joinedEventsAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        setupListeners();
        setupRecyclerView();
    }

    /**
     * Attaches the Firestore listener when the activity starts.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Load data every time the activity is started
        // This ensures data is fresh after returning from EditProfileActivity
        loadProfileData();
    }

    /**
     * Detaches the Firestore listener when the activity stops.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Sets listeners for the "Back" and "Edit Profile" buttons.
     */
    private void setupListeners() {
        binding.btnBackProfile.setOnClickListener(v -> finish());
        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });
    }

    /**
     * Attaches a snapshot listener to the user's document in Firestore.
     * This will update the profile UI in real-time if data changes.
     */
    private void loadProfileData() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userListener = userRef.addSnapshotListener(this, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current user data: " + snapshot.getData());
                UserProfile userProfile = snapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    binding.tvProfileName.setText(userProfile.getName());
                    binding.tvFollowingCount.setText(String.valueOf(userProfile.getFollowingCount()));
                    binding.tvFollowerCount.setText(String.valueOf(userProfile.getFollowerCount()));
                    binding.tvAboutMe.setText(userProfile.getAboutMe());

                    // Load profile picture
                    Glide.with(this)
                            .load(userProfile.getProfilePictureUrl())
                            .placeholder(R.drawable.placeholder_avatar1)
                            .error(R.drawable.placeholder_avatar1)
                            .into(binding.ivProfilePicture);

                    // Populate Interests
                    binding.chipGroupInterests.removeAllViews(); // Clear old chips
                    List<String> interests = userProfile.getInterests();
                    if (interests != null) {
                        for (String interest : interests) {
                            Chip chip = new Chip(this);
                            chip.setText(interest);
                            binding.chipGroupInterests.addView(chip);
                        }
                    }

                    // After loading profile, load their joined events
                    loadJoinedEvents(userProfile.getAppliedEventIds());
                }
            } else {
                Log.d(TAG, "No such document");
            }
        });
    }

    /**
     * Initializes the RecyclerView for the user's joined events.
     */
    private void setupRecyclerView() {
        joinedEventsAdapter = new NearEventAdapter(event -> {
            // Go to event details when a joined event is clicked
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvJoinedEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedEvents.setAdapter(joinedEventsAdapter);
    }

    /**
     * Fetches the event details for all events the user has applied to.
     * @param eventIds A list of event document IDs from the user's profile.
     */
    private void loadJoinedEvents(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            Log.d(TAG, "User has no joined events.");
            joinedEventsAdapter.submitList(new ArrayList<>()); // Submit empty list
            return;
        }

        // Query the "events" collection for any document whose ID is in our list
        db.collection("events").whereIn(FieldPath.documentId(), eventIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> joinedEvents = queryDocumentSnapshots.toObjects(Event.class);
                    joinedEventsAdapter.submitList(joinedEvents);
                    Log.d(TAG, "Loaded " + joinedEvents.size() + " joined events.");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading joined events", e);
                });
    }
}
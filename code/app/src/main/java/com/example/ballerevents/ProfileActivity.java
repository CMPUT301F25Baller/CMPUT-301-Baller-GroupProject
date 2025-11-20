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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying the authenticated user's profile, including:
 *
 * <ul>
 *     <li>Profile picture</li>
 *     <li>Name and "About Me"</li>
 *     <li>Interests (as chips)</li>
 *     <li>Follower/Following counts</li>
 *     <li>List of events the user has joined</li>
 * </ul>
 *
 * <p>
 * A real-time Firestore listener updates the UI whenever the user's
 * profile document changes, ensuring that profile edits or count changes
 * are reflected immediately.
 * </p>
 */
public class ProfileActivity extends AppCompatActivity {

    /** Log tag for debugging. */
    private static final String TAG = "ProfileActivity";

    /** ViewBinding for accessing UI elements. */
    private ActivityProfileBinding binding;

    /** RecyclerView adapter for the user's joined events. */
    private NearEventAdapter joinedEventsAdapter;

    /** Firestore database reference. */
    private FirebaseFirestore db;

    /** FirebaseAuth instance for identifying the current user. */
    private FirebaseAuth mAuth;

    /** Authenticated user's UID. */
    private String currentUserId;

    /** Active snapshot listener for the user document. */
    private ListenerRegistration userListener;

    /**
     * Initializes Firebase instances, ViewBinding, and UI listeners.
     */
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
     * Attaches a Firestore listener each time the activity starts.
     * Ensures the profile is always refreshed after editing.
     */
    @Override
    protected void onStart() {
        super.onStart();
        loadProfileData();
    }

    /**
     * Removes the Firestore listener to avoid memory leaks.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Sets click listeners for "Back" and "Edit Profile" buttons.
     */
    private void setupListeners() {
        binding.btnBackProfile.setOnClickListener(v -> finish());
        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );
    }

    /**
     * Subscribes to real-time updates on the current user's Firestore document.
     * When the user profile changes, the UI is updated accordingly.
     */
    private void loadProfileData() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userListener = userRef.addSnapshotListener(this, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
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

                    // Render interests
                    binding.chipGroupInterests.removeAllViews();
                    List<String> interests = userProfile.getInterests();
                    if (interests != null) {
                        for (String interest : interests) {
                            Chip chip = new Chip(this);
                            chip.setText(interest);
                            binding.chipGroupInterests.addView(chip);
                        }
                    }

                    // Load joined events after profile is processed
                    loadJoinedEvents(userProfile.getAppliedEventIds());
                }
            } else {
                Log.d(TAG, "No such document");
            }
        });
    }

    /**
     * Sets up the RecyclerView that displays the user's joined events.
     */
    private void setupRecyclerView() {
        joinedEventsAdapter = new NearEventAdapter(event -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvJoinedEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedEvents.setAdapter(joinedEventsAdapter);
    }

    /**
     * Fetches full event documents for all events the user has joined.
     *
     * @param eventIds List of Firestore document IDs for joined events.
     */
    private void loadJoinedEvents(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            joinedEventsAdapter.submitList(new ArrayList<>());
            Log.d(TAG, "User has no joined events.");
            return;
        }

        db.collection("events")
                .whereIn(FieldPath.documentId(), eventIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> joinedEvents = queryDocumentSnapshots.toObjects(Event.class);
                    joinedEventsAdapter.submitList(joinedEvents);
                    Log.d(TAG, "Loaded " + joinedEvents.size() + " joined events.");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading joined events", e));
    }
}

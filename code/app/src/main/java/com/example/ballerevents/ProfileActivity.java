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
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying the entrant's profile and event history.
 * <p>
 * This activity allows users to:
 * <ul>
 * <li>View their profile details (Name, Bio, Stats, Interests).</li>
 * <li>See a list of events they have applied for.</li>
 * <li>View the status of each application (e.g., "Waitlisted", "Selected").</li>
 * <li>Navigate to edit their profile or log out.</li>
 * </ul>
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration userListener;

    private EventHistoryAdapter historyAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            setupRecyclerView();
            setupRealtimeProfileListener();
            setupButtons();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Sets up click listeners for the back, edit profile, and logout buttons.
     */
    private void setupButtons() {
        if (binding.btnBackProfile != null) {
            binding.btnBackProfile.setOnClickListener(v -> finish());
        }

        if (binding.btnEditProfile != null) {
            binding.btnEditProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, EditProfileActivity.class))
            );
        }

        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Initializes the RecyclerView with the {@link EventHistoryAdapter}.
     * Passes the current user ID so the adapter can determine the correct status for each event.
     */
    private void setupRecyclerView() {
        historyAdapter = new EventHistoryAdapter(currentUserId, event -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvJoinedEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedEvents.setAdapter(historyAdapter);
    }

    /**
     * Sets up a real-time listener for the user's profile document.
     * Updates the UI and loads joined events whenever the profile changes.
     */
    private void setupRealtimeProfileListener() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                UserProfile userProfile = snapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    updateUI(userProfile);
                    loadJoinedEvents(userProfile.getAppliedEventIds());
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }

    /**
     * Updates the profile UI elements with the latest user data.
     *
     * @param user The user profile object.
     */
    private void updateUI(UserProfile user) {
        binding.tvName.setText(user.getName());
        binding.tvAboutMe.setText(user.getAboutMe());
        binding.tvFollowersCount.setText(String.valueOf(user.getFollowerIds().size()));
        binding.tvFollowingCount.setText(String.valueOf(user.getFollowingIds().size()));

        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .into(binding.ivProfileImage);

        binding.chipGroupInterests.removeAllViews();
        for (String interest : user.getInterests()) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setCheckable(false);
            binding.chipGroupInterests.addView(chip);
        }
    }

    /**
     * Fetches the details for the events the user has joined.
     *
     * @param eventIds List of event IDs the user has applied to.
     */
    private void loadJoinedEvents(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            historyAdapter.submitList(new ArrayList<>());
            return;
        }

        List<String> subset = eventIds.subList(0, Math.min(eventIds.size(), 10));

        db.collection("events")
                .whereIn(FieldPath.documentId(), subset)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> joinedEvents = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            joinedEvents.add(event);
                        }
                    }
                    historyAdapter.submitList(joinedEvents);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading joined events", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }
}
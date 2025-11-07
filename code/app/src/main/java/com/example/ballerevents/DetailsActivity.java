package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Make sure Glide is in your build.gradle
import com.example.ballerevents.databinding.EntrantEventDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String TAG = "DetailsActivity";

    private EntrantEventDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String currentUserId;
    private String eventId;
    private DocumentReference userRef;
    private DocumentReference eventRef;

    private Event mEvent;
    private UserProfile mUserProfile;
    private boolean mIsUserApplied = false;

    // Listeners to detach onStop
    private ListenerRegistration eventListener;
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = db.collection("users").document(currentUserId);

        // Get the String ID from the Intent
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        eventRef = db.collection("events").document(eventId);

        binding.btnJoinWaitlist.setOnClickListener(v -> handleWaitlistClick());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Listen for real-time updates to the event
        eventListener = eventRef.addSnapshotListener(this, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Event listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                mEvent = snapshot.toObject(Event.class);
                if (mEvent != null) {
                    populateStaticUi(mEvent);
                }
            } else {
                Log.d(TAG, "Event deleted or not found");
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Listen for real-time updates to the user's profile
        userListener = userRef.addSnapshotListener(this, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "User listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                mUserProfile = snapshot.toObject(UserProfile.class);
                if (mUserProfile != null) {
                    updateButtonAndCountUI();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listeners to save resources
        if (eventListener != null) {
            eventListener.remove();
        }
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Populates the UI with static event data (title, description, etc.)
     */
    private void populateStaticUi(Event event) {
        // Load image with Glide from URL
        Glide.with(this)
                .load(event.getEventPosterUrl()) // Use URL getter
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.ivEventBanner);

        // Load organizer icon with Glide from URL
        Glide.with(this)
                .load(event.getOrganizerIconUrl()) // Use URL getter
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .into(binding.ivOrganizer);

        binding.tvEventTitle.setText(event.getTitle());
        binding.tvEventDate.setText(event.getDate());
        binding.tvEventTime.setText(event.getTime());
        binding.tvEventLocationName.setText(event.getLocationName());
        binding.tvEventLocationAddress.setText(event.getLocationAddress());
        binding.tvOrganizerName.setText(event.getOrganizer());
        binding.tvAboutEventDescription.setText(event.getDescription());
    }

    /**
     * Updates the button text and waitlist count based on the user's application status.
     */
    private void updateButtonAndCountUI() {
        if (mUserProfile == null) return;

        List<String> appliedIds = mUserProfile.getAppliedEventIds();
        if (appliedIds == null) {
            appliedIds = new ArrayList<>();
        }

        mIsUserApplied = appliedIds.contains(eventId);

        // TODO: Get a real waitlist count.
        binding.tvWaitlistCount.setText("Join the lottery!");

        if (mIsUserApplied) {
            binding.btnJoinWaitlist.setText("Withdraw");
            // You can also change the color
            // binding.btnJoinWaitlist.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            binding.btnJoinWaitlist.setText("Join Waitlist");
            // Change color back
            // binding.btnJoinWaitlist.setBackgroundColor(getResources().getColor(R.color.your_primary_color));
        }
        binding.btnJoinWaitlist.setEnabled(true);
    }

    /**
     * Handles the logic for joining or withdrawing from the waitlist.
     */
    private void handleWaitlistClick() {
        binding.btnJoinWaitlist.setEnabled(false); // Disable button

        if (mIsUserApplied) {
            // User wants to WITHDRAW
            // This updates the "appliedEventIds" array in the "users" collection
            userRef.update("appliedEventIds", FieldValue.arrayRemove(eventId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Withdrawn from " + mEvent.getTitle(), Toast.LENGTH_SHORT).show();
                        // UI will update automatically from listener
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Withdrawal failed. Try again.", Toast.LENGTH_SHORT).show();
                        binding.btnJoinWaitlist.setEnabled(true);
                    });
        } else {
            // User wants to APPLY
            // This updates the "appliedEventIds" array in the "users" collection
            userRef.update("appliedEventIds", FieldValue.arrayUnion(eventId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Applied to lottery for " + mEvent.getTitle(), Toast.LENGTH_SHORT).show();
                        // UI will update automatically from listener
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Application failed. Try again.", Toast.LENGTH_SHORT).show();
                        binding.btnJoinWaitlist.setEnabled(true);
                    });
        }
    }
}
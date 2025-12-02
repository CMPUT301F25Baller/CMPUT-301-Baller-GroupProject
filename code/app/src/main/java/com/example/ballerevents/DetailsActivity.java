package com.example.ballerevents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location; // Requires android.location
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.EntrantEventDetailsBinding;
// The lines below REQUIRE the play-services-location dependency
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "com.example.ballerevents.EVENT_ID";
    private static final String TAG = "DetailsActivity";

    private EntrantEventDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration eventListener;
    private FusedLocationProviderClient fusedLocationClient;

    private String eventId;
    private String currentUserId;
    private Event mEvent;
    private UserProfile organizerProfile;

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchLocationAndJoin();
                } else {
                    Toast.makeText(this, "Location required. Joining without it.", Toast.LENGTH_SHORT).show();
                    joinWaitlist(null);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // This line will fail if dependency is missing
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }

        setupButtons();
    }

    // ... (Keep existing methods: onStart, onStop, updateUI, updateStatusUI, setupButtons) ...
    // Paste the rest of the methods from my previous response here to ensure completeness.
    // I am abbreviating to save space, but the key fix is the IMPORTS above.

    @Override
    protected void onStart() {
        super.onStart();
        // Listen to event changes in real-time
        eventListener = db.collection("events").document(eventId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        mEvent = snapshot.toObject(Event.class);
                        if (mEvent != null) {
                            mEvent.setId(snapshot.getId());
                            updateUI();
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventListener != null) eventListener.remove();
    }

    private void updateUI() {
        binding.tvTitle.setText(mEvent.getTitle());
        binding.tvDescription.setText(mEvent.getDescription());
        binding.tvDate.setText(mEvent.getDate() + " at " + mEvent.getTime());
        binding.tvLocation.setText(mEvent.getLocationName());

        // Update Waitlist Count (US 01.05.04)
        int waitingCount = (mEvent.getWaitlistUserIds() != null) ? mEvent.getWaitlistUserIds().size() : 0;
        binding.tvWaitlistCount.setText("👤 " + waitingCount + " Waiting");

        if (mEvent.getOrganizerId() != null) {
            DocumentReference userRef = db.collection("users").document(mEvent.getOrganizerId());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    organizerProfile = documentSnapshot.toObject(UserProfile.class);
                }
                if (organizerProfile != null) {
                    binding.tvOrganizerName.setText(organizerProfile.getName());
                    Glide.with(this)
                            .load(organizerProfile.getProfilePictureUrl())
                            .placeholder(R.drawable.placeholder_avatar1)
                            .into(binding.ivOrganizerAvatar);
                } else if (mEvent.getOrganizer() != null) {
                    binding.tvOrganizerName.setText(mEvent.getOrganizer());
                } else {
                    binding.tvOrganizerName.setText("Unknown Organizer");
                }
            });
        }

        Glide.with(this)
                .load(mEvent.getEventPosterUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(binding.ivEventPoster);

        updateStatusUI();
    }

    /**
     * Determines which buttons/status text to show based on User's lottery status.
     */
    private void updateStatusUI() {
        if (currentUserId == null) return;

        boolean isWaitlisted = mEvent.getWaitlistUserIds() != null && mEvent.getWaitlistUserIds().contains(currentUserId);
        boolean isSelected = mEvent.getSelectedUserIds() != null && mEvent.getSelectedUserIds().contains(currentUserId);
        boolean isCancelled = mEvent.getCancelledUserIds() != null && mEvent.getCancelledUserIds().contains(currentUserId);
        boolean isFull = mEvent.getWaitlistUserIds() != null && (mEvent.getMaxAttendees() == mEvent.getWaitlistUserIds().size());

        String status = "unknown";
        if (mEvent.getInvitationStatus() != null) {
            status = mEvent.getInvitationStatus().getOrDefault(currentUserId, "pending");
        }

        // Reset visibility
        binding.btnJoinWaitlist.setVisibility(View.GONE);
        binding.layoutInviteActions.setVisibility(View.GONE);
        binding.tvStatusMessage.setVisibility(View.GONE);

        if (isSelected) {
            if ("accepted".equals(status)) {
                // CONFIRMED
                binding.tvStatusMessage.setText("You are going! ✅");
                binding.tvStatusMessage.setVisibility(View.VISIBLE);
                binding.tvStatusMessage.setTextColor(getColor(android.R.color.holo_green_dark));
            } else {
                // PENDING INVITE (WON LOTTERY)
                binding.tvStatusMessage.setText("🎉 You won the lottery! Accept to confirm.");
                binding.tvStatusMessage.setVisibility(View.VISIBLE);
                binding.layoutInviteActions.setVisibility(View.VISIBLE);
            }
        } else if (isWaitlisted) {
            // STILL WAITING
            binding.tvStatusMessage.setText("You are on the waitlist. Check notifications for rules! 🤞");
            binding.tvStatusMessage.setVisibility(View.VISIBLE);
            binding.tvStatusMessage.setTextColor(getColor(android.R.color.darker_gray));
        } else if (isCancelled) {
            // DECLINED
            binding.tvStatusMessage.setText("You declined this invitation.");
            binding.tvStatusMessage.setVisibility(View.VISIBLE);
            binding.tvStatusMessage.setTextColor(getColor(android.R.color.holo_red_dark));
        } else if (isFull) {
            // FULL
            binding.tvStatusMessage.setText("The waitlist is currently full!");
            binding.tvStatusMessage.setVisibility(View.VISIBLE);
        } else {
            // NEW USER
            binding.btnJoinWaitlist.setVisibility(View.VISIBLE);
            binding.btnJoinWaitlist.setText("Join Waitlist");
        }
    }

    private void setupButtons() {
        // Back Button Logic
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        // Join Waitlist Logic
        binding.btnJoinWaitlist.setOnClickListener(v -> {
            if (mEvent == null) return;

            // Check Geolocation Requirement
            if (mEvent.isGeolocationRequired()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    fetchLocationAndJoin();
                }
            } else {
                joinWaitlist(null);
            }
        });

        // Accept Logic
        binding.btnAccept.setOnClickListener(v -> respondToInvite("accepted"));

        // Decline Logic
        binding.btnDecline.setOnClickListener(v -> respondToInvite("declined"));
    }

    private void fetchLocationAndJoin() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    joinWaitlist(new GeoPoint(location.getLatitude(), location.getLongitude()));
                } else {
                    Toast.makeText(this, "Could not fetch location. Joining anyway.", Toast.LENGTH_SHORT).show();
                    joinWaitlist(null);
                }
            });
        }
    }

    private void joinWaitlist(GeoPoint location) {
        if (mEvent == null) return;

        // 1. Add ID to waitlist
        db.collection("events").document(eventId)
                .update("waitlistUserIds", FieldValue.arrayUnion(currentUserId));

        // 2. Add Location if available
        if (location != null) {
            db.collection("events").document(eventId)
                    .update("entrantLocations." + currentUserId, location);
        }

        // 3. Add to User's applied list
        db.collection("users").document(currentUserId)
                .update("appliedEventIds", FieldValue.arrayUnion(eventId))
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                    // Automatically send the guidelines notification (US 01.05.05)
                    // sendLotteryGuidelinesNotification(); // Uncomment if you have this method
                });
    }

    private void respondToInvite(String response) {
        if (mEvent == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("invitationStatus." + currentUserId, response);

        if ("declined".equals(response)) {
            // If declined, move from Selected -> Cancelled
            // This frees up the slot for the Organizer to re-draw.
            updates.put("selectedUserIds", FieldValue.arrayRemove(currentUserId));
            updates.put("cancelledUserIds", FieldValue.arrayUnion(currentUserId));
        }

        db.collection("events").document(eventId).update(updates)
                .addOnSuccessListener(a -> Toast.makeText(this, "Response sent: " + response, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error sending response", Toast.LENGTH_SHORT).show());
    }
}
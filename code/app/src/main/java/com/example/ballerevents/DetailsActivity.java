package com.example.ballerevents;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for displaying detailed information about a specific event.
 *
 * <p>Features include:</p>
 * <ul>
 * <li>Viewing event details (Title, Description, Date, Location).</li>
 * <li>Joining the waitlist (with optional Geolocation).</li>
 * <li>Accepting or declining invitations to join the event.</li>
 * <li>Viewing the organizer's profile.</li>
 * </ul>
 */
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

    @Override
    protected void onStart() {
        super.onStart();
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

    /**
     * Updates the UI with the latest event data.
     * Fetches and displays organizer details and current waitlist count.
     */
    private void updateUI() {
        binding.tvTitle.setText(mEvent.getTitle());
        binding.tvDescription.setText(mEvent.getDescription());
        binding.tvDate.setText(mEvent.getDate() + " at " + mEvent.getTime());
        binding.tvLocation.setText(mEvent.getLocationName());

        int waitingCount = (mEvent.getWaitlistUserIds() != null) ? mEvent.getWaitlistUserIds().size() : 0;
        binding.tvWaitlistCount.setText("👤 " + waitingCount + " Waiting");

        if (mEvent.getOrganizerId() != null) {
            DocumentReference userRef = db.collection("users").document(mEvent.getOrganizerId());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    organizerProfile = documentSnapshot.toObject(UserProfile.class);
                    if (organizerProfile != null) organizerProfile.setId(documentSnapshot.getId());
                }

                if (organizerProfile != null) {
                    binding.tvOrganizerName.setText(organizerProfile.getName());
                    Glide.with(this)
                            .load(organizerProfile.getProfilePictureUrl())
                            .placeholder(R.drawable.placeholder_avatar1)
                            .into(binding.ivOrganizerAvatar);

                    View.OnClickListener viewProfile = v -> {
                        Intent intent = new Intent(DetailsActivity.this, ProfileDetailsActivity.class);
                        intent.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID, organizerProfile.getId());
                        startActivity(intent);
                    };
                    binding.tvOrganizerName.setOnClickListener(viewProfile);
                    binding.ivOrganizerAvatar.setOnClickListener(viewProfile);

                } else {
                    binding.tvOrganizerName.setText(mEvent.getOrganizer() != null ? mEvent.getOrganizer() : "Unknown");
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
     * Updates the status message and action buttons based on the user's relationship to the event.
     * Handles states: Waitlisted, Selected (Won), Accepted, Declined, Cancelled, and Full.
     */
    private void updateStatusUI() {
        if (currentUserId == null) return;

        boolean isWaitlisted = mEvent.getWaitlistUserIds() != null && mEvent.getWaitlistUserIds().contains(currentUserId);
        boolean isSelected = mEvent.getSelectedUserIds() != null && mEvent.getSelectedUserIds().contains(currentUserId);
        boolean isCancelled = mEvent.getCancelledUserIds() != null && mEvent.getCancelledUserIds().contains(currentUserId);
        boolean isFull = mEvent.getWaitlistUserIds() != null && (mEvent.getMaxAttendees() > 0 && mEvent.getWaitlistUserIds().size() >= mEvent.getMaxAttendees());

        String status = "unknown";
        if (mEvent.getInvitationStatus() != null) {
            status = mEvent.getInvitationStatus().getOrDefault(currentUserId, "pending");
        }

        binding.btnJoinWaitlist.setVisibility(View.GONE);
        binding.layoutInviteActions.setVisibility(View.GONE);
        binding.tvStatusMessage.setVisibility(View.GONE);

        if (isSelected) {
            if ("accepted".equals(status)) {
                binding.tvStatusMessage.setText("You are going! ✅");
                binding.tvStatusMessage.setVisibility(View.VISIBLE);
                binding.tvStatusMessage.setTextColor(getColor(android.R.color.holo_green_dark));
            } else {
                binding.tvStatusMessage.setText("🎉 You won the lottery! Accept to confirm.");
                binding.tvStatusMessage.setVisibility(View.VISIBLE);
                binding.layoutInviteActions.setVisibility(View.VISIBLE);
            }
        } else if (isWaitlisted) {
            binding.tvStatusMessage.setText("You are on the waitlist. Check notifications for rules! 🤞");
            binding.tvStatusMessage.setVisibility(View.VISIBLE);
            binding.tvStatusMessage.setTextColor(getColor(android.R.color.darker_gray));
        } else if (isCancelled) {
            binding.tvStatusMessage.setText("You declined this invitation.");
            binding.tvStatusMessage.setVisibility(View.VISIBLE);
            binding.tvStatusMessage.setTextColor(getColor(android.R.color.holo_red_dark));
        } else if (isFull) {
            binding.tvStatusMessage.setText("The waitlist is currently full!");
            binding.tvStatusMessage.setVisibility(View.VISIBLE);
        } else {
            binding.btnJoinWaitlist.setVisibility(View.VISIBLE);
            binding.btnJoinWaitlist.setText("Join Waitlist");
        }
    }

    private void setupButtons() {
        if (binding.btnBack != null) binding.btnBack.setOnClickListener(v -> finish());

        binding.btnJoinWaitlist.setOnClickListener(v -> {
            if (mEvent == null) return;
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

        binding.btnAccept.setOnClickListener(v -> respondToInvite("accepted"));
        binding.btnDecline.setOnClickListener(v -> respondToInvite("declined"));
    }

    /**
     * Requests the device's last known location and joins the waitlist with coordinates.
     */
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

    /**
     * Adds the current user to the waitlist in Firestore.
     * Optionally records geolocation if provided.
     *
     * @param location The GeoPoint of the user (can be null).
     */
    private void joinWaitlist(GeoPoint location) {
        if (mEvent == null) return;
        db.collection("events").document(eventId)
                .update("waitlistUserIds", FieldValue.arrayUnion(currentUserId));

        if (location != null) {
            db.collection("events").document(eventId)
                    .update("entrantLocations." + currentUserId, location);
        }

        db.collection("users").document(currentUserId)
                .update("appliedEventIds", FieldValue.arrayUnion(eventId))
                .addOnSuccessListener(a -> Toast.makeText(this, "Joined Waitlist!", Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the user's invitation status (accepted/declined) in Firestore.
     *
     * @param response The response string ("accepted" or "declined").
     */
    private void respondToInvite(String response) {
        if (mEvent == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("invitationStatus." + currentUserId, response);

        if ("declined".equals(response)) {
            updates.put("selectedUserIds", FieldValue.arrayRemove(currentUserId));
            updates.put("cancelledUserIds", FieldValue.arrayUnion(currentUserId));
        }

        db.collection("events").document(eventId).update(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Response sent: " + response, Toast.LENGTH_SHORT).show();
                    updateStatusUI();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error sending response", Toast.LENGTH_SHORT).show());
    }
}
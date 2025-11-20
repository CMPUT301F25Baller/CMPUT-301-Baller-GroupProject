package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

/**
 * Activity that displays detailed information about a single {@link Event} to an entrant.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Loads event details from Firestore in real time.</li>
 *     <li>Loads the current user's {@link UserProfile} in real time.</li>
 *     <li>Allows the user to join or withdraw from the event's lottery (waitlist).</li>
 *     <li>Updates the UI based on the user's membership in {@code appliedEventIds}.</li>
 * </ul>
 *
 * <p>The event to display is identified by a Firestore document ID passed via
 * {@link #EXTRA_EVENT_ID} in the launching {@link android.content.Intent}.</p>
 */
public class DetailsActivity extends AppCompatActivity {

    /**
     * Intent extra key used to pass the event's Firestore document ID
     * into this activity.
     */
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

    // Listeners to detach in onStop to avoid leaks and unnecessary updates.
    private ListenerRegistration eventListener;
    private ListenerRegistration userListener;

    /**
     * Initializes the activity, sets up bindings, resolves the event ID,
     * and prepares references to Firestore documents.
     *
     * @param savedInstanceState saved instance state bundle, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ensure a user is logged in before proceeding.
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = db.collection("users").document(currentUserId);

        // Retrieve the event ID from the Intent extras.
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        eventRef = db.collection("events").document(eventId);

        // UI listeners
        binding.btnJoinWaitlist.setOnClickListener(v -> handleWaitlistClick());
        binding.btnBack.setOnClickListener(v -> finish());

        setupAdminDeleteIfNeeded();
    }

    /**
     * Attaches Firestore listeners when the activity becomes visible,
     * so event and user data stay up to date in real time.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Listen for real-time updates to the event document
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

        // Listen for real-time updates to the current user's profile
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

    /**
     * Detaches Firestore listeners when the activity is no longer visible,
     * preventing memory leaks and unnecessary network activity.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (eventListener != null) {
            eventListener.remove();
        }
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Populates the UI with static event data: title, time, location,
     * description, and associated images (banner, poster, organizer avatar).
     *
     * @param event the {@link Event} whose details are being displayed
     */
    private void populateStaticUi(Event event) {
        // Load banner image (using the poster URL as the banner source)
        Glide.with(this)
                .load(event.getEventPosterUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(binding.ivEventBanner);

        // Load full poster image if present; otherwise, hide the poster view
        if (event.getEventPosterUrl() != null && !event.getEventPosterUrl().isEmpty()) {
            binding.ivEventPoster.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(event.getEventPosterUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(binding.ivEventPoster);
        } else {
            binding.ivEventPoster.setVisibility(View.GONE);
        }

        // Load organizer avatar from URL
        Glide.with(this)
                .load(event.getOrganizerIconUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .into(binding.ivOrganizer);

        // Text fields
        binding.tvEventTitle.setText(event.getTitle());
        binding.tvEventDate.setText(event.getDate());
        binding.tvEventTime.setText(event.getTime());
        binding.tvEventLocationName.setText(event.getLocationName());
        binding.tvEventLocationAddress.setText(event.getLocationAddress());
        binding.tvOrganizerName.setText(event.getOrganizer());
        binding.tvAboutEventDescription.setText(event.getDescription());
    }

    /**
     * Updates the state and label of the join/withdraw button based on
     * the current user's application status, and also updates the
     * waitlist info text.
     * <p>
     * The status is derived from {@link UserProfile#getAppliedEventIds()}.
     */
    private void updateButtonAndCountUI() {
        if (mUserProfile == null) return;

        List<String> appliedIds = mUserProfile.getAppliedEventIds();
        if (appliedIds == null) {
            appliedIds = new ArrayList<>();
        }

        mIsUserApplied = appliedIds.contains(eventId);

        // Placeholder: real implementation would show an actual count
        // derived from Firestore documents.
        binding.tvWaitlistCount.setText("Join the lottery!");

        if (mIsUserApplied) {
            binding.btnJoinWaitlist.setText("Withdraw");
            // Optionally adjust styles here if desired
        } else {
            binding.btnJoinWaitlist.setText("Join Waitlist");
        }

        binding.btnJoinWaitlist.setEnabled(true);
    }

    /**
     * Shows Delete Event button only if current user is admin.
     */
    private void setupAdminDeleteIfNeeded() {
        if (eventId == null || eventId.isEmpty()) return;
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    UserProfile user = snapshot.toObject(UserProfile.class);
                    if (user != null &&
                            "admin".equalsIgnoreCase(user.getRole())) {

                        // show delete button
                        binding.btnDeleteEvent.setVisibility(View.VISIBLE);
                        binding.btnDeleteEvent.setOnClickListener(v -> confirmDelete());
                    }
                });
    }

    private void confirmDelete() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Delete Event?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        eventRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting", e);
                });
    }


    /**
     * Handles the "Join Waitlist" / "Withdraw" button click.
     * <p>
     * This method:
     * <ul>
     *     <li>Disables the button to prevent double taps.</li>
     *     <li>Uses {@link FieldValue#arrayUnion(Object...)} or
     *         {@link FieldValue#arrayRemove(Object...)} to update
     *         {@code appliedEventIds} in the user's Firestore document.</li>
     *     <li>Shows a toast on success or failure.</li>
     * </ul>
     * The UI is updated indirectly via the Firestore listener on the user document.
     */
    private void handleWaitlistClick() {
        binding.btnJoinWaitlist.setEnabled(false); // Prevent rapid double-clicks

        if (mIsUserApplied) {
            // User wants to WITHDRAW from the lottery
            userRef.update("appliedEventIds", FieldValue.arrayRemove(eventId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                "Withdrawn from " + mEvent.getTitle(),
                                Toast.LENGTH_SHORT).show();
                        // UI will refresh via listener
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Withdrawal failed. Try again.",
                                Toast.LENGTH_SHORT).show();
                        binding.btnJoinWaitlist.setEnabled(true);
                    });
        } else {
            // User wants to APPLY to the lottery
            userRef.update("appliedEventIds", FieldValue.arrayUnion(eventId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                "Applied to lottery for " + mEvent.getTitle(),
                                Toast.LENGTH_SHORT).show();
                        // UI will refresh via listener
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Application failed. Try again.",
                                Toast.LENGTH_SHORT).show();
                        binding.btnJoinWaitlist.setEnabled(true);
                    });
        }
    }
}

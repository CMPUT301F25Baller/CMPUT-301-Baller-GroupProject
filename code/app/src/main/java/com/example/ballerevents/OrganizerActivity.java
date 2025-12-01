package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityOrganizerBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main activity for organizers, hosting a tab-based layout with the following sections:
 * <ul>
 *     <li>About – displays profile details</li>
 *     <li>Event – lists the organizer's events</li>
 *     <li>Following – prototype section for followed users/entities</li>
 * </ul>
 *
 * The activity loads the organizer's profile header, configures the ViewPager with
 * {@link OrganizerPagerAdapter}, supports event creation, messaging (prototype),
 * and provides tools for running lotteries and viewing chosen entrants.
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    /** How many entrants the lottery will choose at most. */
    private static final int LOTTERY_SLOTS = 10;

    private ActivityOrganizerBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    /** Logged-in organizer’s user ID. */
    private String currentUserId;

    /** The event selected in the Event tab (set by OrganizerEventFragment). */
    private String selectedEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ensure organizer is logged in
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Toolbar back button
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup ViewPager + Tabs
        OrganizerPagerAdapter pagerAdapter = new OrganizerPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setCurrentItem(1, false); // default to "Event" tab

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        // Create a new event
        binding.btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventCreationActivity.class))
        );

        // Messaging prototype
        binding.btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging prototype coming soon.", Toast.LENGTH_SHORT).show()
        );

        // Final entrants + winners
        binding.btnViewFinalEntrants.setOnClickListener(v -> openFinalEntrantsScreen());
        binding.btnViewLotteryWinners.setOnClickListener(v -> openLotteryWinnersScreen());

        // Run Lottery (MaterialButton)
        MaterialButton btnRunLottery = findViewById(R.id.btnRunLottery);
        if (btnRunLottery != null) {
            btnRunLottery.setOnClickListener(v -> runLotteryForSelectedEvent());
        } else {
            Log.w(TAG, "btnRunLottery not found in layout at runtime");
        }

        loadOrganizerHeaderInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh header on return from other screens
        loadOrganizerHeaderInfo();
    }

    /** Called by OrganizerEventFragment to register which event is selected. */
    public void setSelectedEventId(String eventId) {
        this.selectedEventId = eventId;
        Log.d(TAG, "Selected event: " + eventId);
    }

    /** Opens the screen showing chosenUserIds (Final Entrants). */
    private void openFinalEntrantsScreen() {
        if (selectedEventId == null || selectedEventId.isEmpty()) {
            Toast.makeText(this,
                    "Please select an event first from the Events tab.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, OrganizerFinalEntrantsActivity.class);
        i.putExtra(OrganizerFinalEntrantsActivity.EXTRA_EVENT_ID, selectedEventId);
        startActivity(i);
    }

    /** Opens the screen showing lottery winners (chosenUserIds list). */
    private void openLotteryWinnersScreen() {
        if (selectedEventId == null || selectedEventId.isEmpty()) {
            Toast.makeText(this,
                    "Select an event in the Events tab first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, OrganizerLotteryWinnersActivity.class);
        i.putExtra("eventId", selectedEventId);
        startActivity(i);
    }

    /** Wrapper for the “Run Lottery” button. */
    private void runLotteryForSelectedEvent() {
        if (selectedEventId == null || selectedEventId.isEmpty()) {
            Toast.makeText(this,
                    "Select an event in the Events tab first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        runSimpleLottery(selectedEventId, LOTTERY_SLOTS);
    }

    /**
     * Simple lottery algorithm:
     * 1. Query users who have applied (appliedEventIds contains eventId).
     * 2. Shuffle them randomly.
     * 3. Pick up to maxSlots.
     * 4. Save results into event.chosenUserIds.
     */
    private void runSimpleLottery(String eventId, int maxSlots) {
        db.collection("users")
                .whereArrayContains("appliedEventIds", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> waitlistIds = new ArrayList<>();
                    querySnapshot.getDocuments().forEach(doc -> waitlistIds.add(doc.getId()));

                    if (waitlistIds.isEmpty()) {
                        Toast.makeText(this,
                                "No users on the waitlist for this event.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Collections.shuffle(waitlistIds);
                    int count = Math.min(maxSlots, waitlistIds.size());
                    List<String> chosen = new ArrayList<>(waitlistIds.subList(0, count));

                    db.collection("events").document(eventId)
                            .update("chosenUserIds", chosen)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this,
                                            "Lottery complete. Chosen " + chosen.size() + " entrants.",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to save chosen entrants", e);
                                Toast.makeText(this,
                                        "Failed to save chosen entrants.",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading waitlist for lottery", e);
                    Toast.makeText(this,
                            "Error loading waitlist.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /** Loads organizer profile header (name + photo). */
    private void loadOrganizerHeaderInfo() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Organizer profile not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfile profile = snapshot.toObject(UserProfile.class);
            if (profile == null) return;

            binding.tvOrganizerName.setText(profile.getName());

            Glide.with(this)
                    .load(profile.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_avatar1)
                    .error(R.drawable.placeholder_avatar1)
                    .into(binding.ivOrganizerProfile);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load organizer profile", e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        });
    }
}

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
 * Main activity for organizers, hosting a tab-based layout with:
 *  - About tab
 *  - Event tab
 *  - Following tab
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    /** How many entrants the lottery will choose at most */
    private static final int LOTTERY_SLOTS = 10;

    private ActivityOrganizerBinding binding;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String currentUserId;

    /** The event currently selected in the Events tab (set from OrganizerEventFragment) */
    private String selectedEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Toolbar back
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // ViewPager + tabs
        OrganizerPagerAdapter pagerAdapter = new OrganizerPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setCurrentItem(1, false); // default to Event tab

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        // Buttons that are in the binding
        binding.btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventCreationActivity.class)));

        binding.btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging feature coming soon.", Toast.LENGTH_SHORT).show());

        binding.btnViewFinalEntrants.setOnClickListener(v -> openFinalEntrantsScreen());
        binding.btnViewLotteryWinners.setOnClickListener(v -> openLotteryWinnersScreen());

        // 🔹 Use findViewById for Run Lottery instead of binding
        MaterialButton btnRunLottery = findViewById(R.id.btnRunLottery);
        if (btnRunLottery != null) {
            btnRunLottery.setOnClickListener(v -> runLotteryForSelectedEvent());
        } else {
            Log.w(TAG, "btnRunLottery not found in layout at runtime");
        }

        // Load header
        loadOrganizerHeaderInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrganizerHeaderInfo();
    }

    /** Called by OrganizerEventFragment when the organizer taps an event card. */
    public void setSelectedEventId(String eventId) {
        this.selectedEventId = eventId;
        Log.d(TAG, "Selected event: " + eventId);
    }

    /** Open the screen that shows chosen entrants (using chosenUserIds). */
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

    /** Wrapper for the Run Lottery button. */
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
     * Simple lottery:
     *   1. Find all users whose appliedEventIds contains this eventId.
     *   2. Shuffle them and pick up to maxSlots.
     *   3. Save the chosen IDs to event.chosenUserIds.
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

    /** Loads organizer profile into the header. */
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


}

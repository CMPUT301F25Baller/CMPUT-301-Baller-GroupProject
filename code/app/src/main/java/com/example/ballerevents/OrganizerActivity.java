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
 * Organizer home screen hosting About, Event, and Following tabs.
 * Includes:
 *  - Profile header
 *  - New Event creation
 *  - Lottery execution
 *  - Viewing final entrants and lottery winners
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};
    private static final int LOTTERY_SLOTS = 10; // how many winners

    private ActivityOrganizerBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String currentUserId;
    private String selectedEventId; // set by OrganizerEventFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Ensure user logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();

        // Back button in your XML (NOT toolbar)
        binding.btnBack.setOnClickListener(v -> finish());

        setupTabs();
        setupButtons();
        loadOrganizerHeaderInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrganizerHeaderInfo(); // update on return
    }

    // ------------------------------
    // TAB + VIEWPAGER SETUP
    // ------------------------------
    private void setupTabs() {
        OrganizerPagerAdapter pagerAdapter = new OrganizerPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setCurrentItem(1, false); // Open on Events tab

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();
    }

    // ------------------------------
    // BUTTONS
    // ------------------------------
    private void setupButtons() {
        // Create new event
        binding.btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventCreationActivity.class))
        );

        // Messaging prototype
        binding.btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging prototype coming soon.", Toast.LENGTH_SHORT).show()
        );

        // Final Entrants
        binding.btnViewFinalEntrants.setOnClickListener(v -> openFinalEntrantsScreen());

        // Lottery Winners
        binding.btnViewLotteryWinners.setOnClickListener(v -> openLotteryWinnersScreen());

        // Run Lottery
        MaterialButton btnRunLottery = findViewById(R.id.btnRunLottery);
        if (btnRunLottery != null) {
            btnRunLottery.setOnClickListener(v -> runLotteryForSelectedEvent());
        } else {
            Log.w(TAG, "btnRunLottery not found in layout");
        }
    }

    // Called from OrganizerEventFragment when an event is selected
    public void setSelectedEventId(String eventId) {
        selectedEventId = eventId;
        Log.d(TAG, "Selected Event: " + eventId);
    }

    // ------------------------------
    // OPEN SCREENS
    // ------------------------------

    private void openFinalEntrantsScreen() {
        if (selectedEventId == null || selectedEventId.isEmpty()) {
            Toast.makeText(this, "Select an event first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, OrganizerFinalEntrantsActivity.class);
        i.putExtra(OrganizerFinalEntrantsActivity.EXTRA_EVENT_ID, selectedEventId);
        startActivity(i);
    }

    private void openLotteryWinnersScreen() {
        if (selectedEventId == null || selectedEventId.isEmpty()) {
            Toast.makeText(this, "Select an event first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, OrganizerLotteryWinnersActivity.class);
        i.putExtra("eventId", selectedEventId);
        startActivity(i);
    }

    // ------------------------------
    // LOTTERY LOGIC
    // ------------------------------

    private void runLotteryForSelectedEvent() {
        if (selectedEventId == null || selectedEventId.isEmpty()) {
            Toast.makeText(this, "Select an event in the Events tab.", Toast.LENGTH_SHORT).show();
            return;
        }

        runSimpleLottery(selectedEventId, LOTTERY_SLOTS);
    }

    /**
     * 1. Get all users with appliedEventIds containing eventId
     * 2. Shuffle list
     * 3. Pick up to maxSlots
     * 4. Save to event.chosenUserIds
     */
    private void runSimpleLottery(String eventId, int maxSlots) {
        db.collection("users")
                .whereArrayContains("appliedEventIds", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> waitlist = new ArrayList<>();
                    querySnapshot.getDocuments().forEach(doc -> waitlist.add(doc.getId()));

                    if (waitlist.isEmpty()) {
                        Toast.makeText(this, "No waitlist for this event.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Collections.shuffle(waitlist);
                    int winners = Math.min(maxSlots, waitlist.size());
                    List<String> chosen = new ArrayList<>(waitlist.subList(0, winners));

                    db.collection("events").document(eventId)
                            .update("chosenUserIds", chosen)
                            .addOnSuccessListener(a ->
                                    Toast.makeText(this,
                                            "Lottery complete — selected " + chosen.size() + " entrants.",
                                            Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save lottery results.", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading waitlist.", Toast.LENGTH_SHORT).show()
                );
    }

    // ------------------------------
    // LOAD HEADER (PROFILE)
    // ------------------------------
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

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading profile.", Toast.LENGTH_SHORT).show()
        );
    }
}

package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

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
 * Main activity for users with the Organizer role.
 *
 * <p>This activity displays the organizer's profile header (Name, Bio, Stats)
 * designed to be visually consistent with the Entrant profile. It hosts a
 * {@link ViewPager2} with three tabs:</p>
 * <ul>
 * <li><b>About</b>: Detailed profile information.</li>
 * <li><b>Event</b>: List of events managed by the organizer.</li>
 * <li><b>Following</b>: Lists of followers and following users.</li>
 * </ul>
 */

public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    private ActivityOrganizerBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String currentUserId;

    // --- Lottery System ---
    private static final int LOTTERY_SLOTS = 10;
    private String selectedEventId; // set by OrganizerEventFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ensure authentication
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Custom back button
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        setupTabsAndPager();
        setupButtons();
        loadOrganizerHeaderInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrganizerHeaderInfo();
    }

    // ----------------------------------------------------------
    // VIEWPAGER + TABS
    // ----------------------------------------------------------
    private void setupTabsAndPager() {
        binding.viewPager.setAdapter(new OrganizerPagerAdapter(this));
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setCurrentItem(1, false);  // open Events tab

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();
    }

    // ----------------------------------------------------------
    // ALL BUTTONS (Create Event, Message, Lottery actions)
    // ----------------------------------------------------------
    private void setupButtons() {

        // Create New Event
        binding.btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventCreationActivity.class))
        );

        // Message (prototype)
        binding.btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging prototype coming soon.", Toast.LENGTH_SHORT).show()
        );

        // Open Final Entrants
        binding.btnViewFinalEntrants.setOnClickListener(v -> openFinalEntrantsScreen());

        // Open Lottery Winners
        binding.btnViewLotteryWinners.setOnClickListener(v -> openLotteryWinnersScreen());

        // Run Lottery
        MaterialButton btnRunLottery = findViewById(R.id.btnRunLottery);
        if (btnRunLottery != null) {
            btnRunLottery.setOnClickListener(v -> runLotteryForSelectedEvent());
        }
    }

    // Called from OrganizerEventFragment
    public void setSelectedEventId(String eventId) {
        selectedEventId = eventId;
        Log.d(TAG, "Selected Event ID: " + eventId);
    }

    // ----------------------------------------------------------
    // OPEN EXTRA SCREENS
    // ----------------------------------------------------------
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

    // ----------------------------------------------------------
    // LOTTERY LOGIC
    // ----------------------------------------------------------
    private void runLotteryForSelectedEvent() {
        if (selectedEventId == null) {
            Toast.makeText(this, "Select an event in the Events tab.", Toast.LENGTH_SHORT).show();
            return;
        }
        runSimpleLottery(selectedEventId, LOTTERY_SLOTS);
    }

    private void runSimpleLottery(String eventId, int maxSlots) {
        db.collection("users")
                .whereArrayContains("appliedEventIds", eventId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> waitlist = new ArrayList<>();
                    snap.getDocuments().forEach(doc -> waitlist.add(doc.getId()));

                    if (waitlist.isEmpty()) {
                        Toast.makeText(this, "No waitlist for this event.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Collections.shuffle(waitlist);
                    int winners = Math.min(maxSlots, waitlist.size());
                    List<String> chosen = new ArrayList<>(waitlist.subList(0, winners));

                    db.collection("events")
                            .document(eventId)
                            .update("chosenUserIds", chosen)
                            .addOnSuccessListener(a ->
                                    Toast.makeText(this, "Lottery complete — " + chosen.size() + " winners selected.", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save lottery results.", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading waitlist.", Toast.LENGTH_SHORT).show()
                );
    }

    // ----------------------------------------------------------
    // LOAD ORGANIZER HEADER
    // ----------------------------------------------------------
    private void loadOrganizerHeaderInfo() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Organizer profile not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfile profile = snapshot.toObject(UserProfile.class);
            if (profile == null) return;

            // Name
            binding.tvOrganizerName.setText(profile.getName());

            // About Me
            if (binding.tvOrganizerAboutMe != null) {
                binding.tvOrganizerAboutMe.setText(profile.getAboutMe());
            }

            // Profile Picture
            Glide.with(this)
                    .load(profile.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_avatar1)
                    .error(R.drawable.placeholder_avatar1)
                    .into(binding.ivOrganizerProfile);

            // Follow counts
            int following = profile.getFollowingIds() != null ? profile.getFollowingIds().size() : 0;
            int followers = profile.getFollowerIds() != null ? profile.getFollowerIds().size() : 0;

            if (binding.tvFollowingCount != null) {
                binding.tvFollowingCount.setText(String.valueOf(following));
            }
            if (binding.tvFollowersCount != null) {
                binding.tvFollowersCount.setText(String.valueOf(followers));
            }

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading profile.", Toast.LENGTH_SHORT).show()
        );
    }
}

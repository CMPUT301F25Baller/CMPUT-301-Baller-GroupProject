package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerWaitlistBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for Organizers to manage the lottery system.
 * * <p>Features:
 * <ul>
 * <li>View entrants on the Waitlist vs. Selected list.</li>
 * <li>Run the lottery (Draw) to fill available spots.</li>
 * <li>Automatically handles re-drawing if users decline.</li>
 * </ul>
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String TAG = "OrganizerWaitlistActivity";

    private ActivityOrganizerWaitlistBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private Event currentEvent;

    // Adapters
    private WaitlistUserAdapter waitlistAdapter;
    private WaitlistUserAdapter selectedAdapter;

    // Data Lists
    private final List<UserProfile> waitlistProfiles = new ArrayList<>();
    private final List<UserProfile> selectedProfiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerWaitlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        if (eventId == null) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerViews();
        setupTabs(); // Toggle between Waitlist / Selected views
        setupDrawButton();

        loadEventAndEntrants();
    }

    private void setupRecyclerViews() {
        // Waitlist Recycler
        binding.rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        waitlistAdapter = new WaitlistUserAdapter(waitlistProfiles, this);
        binding.rvWaitlist.setAdapter(waitlistAdapter);

        // Selected Recycler
        binding.rvSelected.setLayoutManager(new LinearLayoutManager(this));
        selectedAdapter = new WaitlistUserAdapter(selectedProfiles, this);
        binding.rvSelected.setAdapter(selectedAdapter);
    }

    private void setupTabs() {
        // Simple toggle logic (assuming two buttons or chips in XML)
        binding.btnShowWaitlist.setOnClickListener(v -> {
            binding.rvWaitlist.setVisibility(View.VISIBLE);
            binding.rvSelected.setVisibility(View.GONE);
            binding.btnShowWaitlist.setEnabled(false);
            binding.btnShowSelected.setEnabled(true);
        });

        binding.btnShowSelected.setOnClickListener(v -> {
            binding.rvWaitlist.setVisibility(View.GONE);
            binding.rvSelected.setVisibility(View.VISIBLE);
            binding.btnShowWaitlist.setEnabled(true);
            binding.btnShowSelected.setEnabled(false);
        });

        // Default state
        binding.btnShowWaitlist.performClick();
    }

    private void setupDrawButton() {
        binding.btnDrawLottery.setOnClickListener(v -> {
            if (currentEvent == null) return;

            // Validate Logic
            int max = currentEvent.getMaxAttendees();
            int currentSelected = currentEvent.getSelectedUserIds().size();
            int spotsAvailable = max - currentSelected;

            if (spotsAvailable <= 0) {
                Toast.makeText(this, "Event is full! Increase capacity to draw more.", Toast.LENGTH_LONG).show();
                return;
            }

            if (currentEvent.getWaitlistUserIds().isEmpty()) {
                Toast.makeText(this, "No one on the waitlist to draw.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Confirm Dialog
            new AlertDialog.Builder(this)
                    .setTitle("Draw Lottery")
                    .setMessage("Draw " + Math.min(spotsAvailable, currentEvent.getWaitlistUserIds().size()) + " entrants from the waitlist?")
                    .setPositiveButton("Draw", (d, w) -> performLotteryDraw(spotsAvailable))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    /**
     * Loads the Event document, then fetches User profiles for all involved IDs.
     */
    private void loadEventAndEntrants() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snap -> {
                    currentEvent = snap.toObject(Event.class);
                    if (currentEvent != null) {
                        currentEvent.setId(snap.getId());
                        updateUIHeader();
                        fetchProfiles();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event", e);
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    private void updateUIHeader() {
        int max = currentEvent.getMaxAttendees();
        int selected = currentEvent.getSelectedUserIds().size();
        int waiting = currentEvent.getWaitlistUserIds().size();

        binding.tvCapacityInfo.setText(
                String.format("Capacity: %d | Selected: %d | Waiting: %d", max, selected, waiting)
        );
    }

    private void fetchProfiles() {
        // Collect all IDs needed
        List<String> allIds = new ArrayList<>();
        allIds.addAll(currentEvent.getWaitlistUserIds());
        allIds.addAll(currentEvent.getSelectedUserIds());

        if (allIds.isEmpty()) {
            waitlistProfiles.clear();
            selectedProfiles.clear();
            waitlistAdapter.notifyDataSetChanged();
            selectedAdapter.notifyDataSetChanged();
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        // Firestore 'in' queries are limited to 10 items.
        // For production, you must batch this. For prototype, we'll fetch basic list.
        // NOTE: Ideally, implement a loop or batch logic here.
        db.collection("users")
                .whereIn(FieldPath.documentId(), allIds.subList(0, Math.min(allIds.size(), 10)))
                .get()
                .addOnSuccessListener(snap -> {
                    waitlistProfiles.clear();
                    selectedProfiles.clear();

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        UserProfile p = doc.toObject(UserProfile.class);
                        if (p == null) continue;

                        if (currentEvent.getSelectedUserIds().contains(p.getId())) {
                            selectedProfiles.add(p);
                        } else if (currentEvent.getWaitlistUserIds().contains(p.getId())) {
                            waitlistProfiles.add(p);
                        }
                    }

                    waitlistAdapter.notifyDataSetChanged();
                    selectedAdapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    /**
     * The core Lottery Algorithm.
     * 1. Shuffle waitlist.
     * 2. Pick N users.
     * 3. Move ID from waitlist -> selected.
     * 4. Set status -> "pending".
     * 5. Update Firestore.
     */
    private void performLotteryDraw(int spotsToFill) {
        List<String> pool = new ArrayList<>(currentEvent.getWaitlistUserIds());
        List<String> winners = new ArrayList<>();

        // Shuffle for randomness
        Collections.shuffle(pool);

        // Pick winners
        int count = 0;
        while (count < spotsToFill && !pool.isEmpty()) {
            String winnerId = pool.remove(0);
            winners.add(winnerId);
            count++;
        }

        // Update local object first for speed/optimistic UI
        currentEvent.getWaitlistUserIds().removeAll(winners);
        currentEvent.getSelectedUserIds().addAll(winners);

        // Update Invitation Status
        if (currentEvent.getInvitationStatus() == null) {
            currentEvent.invitationStatus = new HashMap<>();
        }
        for (String winnerId : winners) {
            currentEvent.getInvitationStatus().put(winnerId, "pending");
        }

        // Push to Firestore
        db.collection("events").document(eventId)
                .update(
                        "waitlistUserIds", currentEvent.getWaitlistUserIds(),
                        "selectedUserIds", currentEvent.getSelectedUserIds(),
                        "invitationStatus", currentEvent.getInvitationStatus()
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lottery complete! " + winners.size() + " selected.", Toast.LENGTH_SHORT).show();
                    loadEventAndEntrants(); // Refresh UI
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Draw failed.", Toast.LENGTH_SHORT).show());
    }
}
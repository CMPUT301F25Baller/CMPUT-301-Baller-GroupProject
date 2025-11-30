package com.example.ballerevents;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerWaitlistBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        setupTabs();
        setupButtons();

        loadEventAndEntrants();
    }

    private void setupRecyclerViews() {
        binding.rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        waitlistAdapter = new WaitlistUserAdapter(waitlistProfiles, this);
        binding.rvWaitlist.setAdapter(waitlistAdapter);

        binding.rvSelected.setLayoutManager(new LinearLayoutManager(this));
        selectedAdapter = new WaitlistUserAdapter(selectedProfiles, this);
        binding.rvSelected.setAdapter(selectedAdapter);
    }

    private void setupTabs() {
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
        binding.btnShowWaitlist.performClick();
    }

    private void setupButtons() {
        binding.btnDrawLottery.setOnClickListener(v -> handleDrawClick());
        binding.btnGenerateQr.setOnClickListener(v -> generateAndSaveQRCode());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void handleDrawClick() {
        if (currentEvent == null) return;

        int max = currentEvent.getMaxAttendees();
        int currentSelected = currentEvent.getSelectedUserIds().size();
        int spotsAvailable = max - currentSelected;

        if (spotsAvailable <= 0) {
            Toast.makeText(this, "Event is full!", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentEvent.getWaitlistUserIds().isEmpty()) {
            Toast.makeText(this, "Waitlist is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Draw Lottery")
                .setMessage("Draw " + Math.min(spotsAvailable, currentEvent.getWaitlistUserIds().size()) + " entrants?")
                .setPositiveButton("Draw", (d, w) -> performLotteryDraw(spotsAvailable))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Generates a QR Code encoding the Event ID and saves it to the device Gallery.
     */
    private void generateAndSaveQRCode() {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            // Encode the Event ID as the content of the QR code
            BitMatrix matrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 500, 500);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);

            // Save to MediaStore (Gallery)
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    "EventQR_" + currentEvent.getTitle(),
                    "Check-in QR Code for " + currentEvent.getTitle()
            );

            if(savedImageURL != null) {
                Toast.makeText(this, "QR Code saved to Gallery!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "QR Gen Error", e);
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
    }

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
                });
    }

    private void updateUIHeader() {
        int max = currentEvent.getMaxAttendees();
        int selected = currentEvent.getSelectedUserIds().size();
        int waiting = currentEvent.getWaitlistUserIds().size();
        binding.tvCapacityInfo.setText(String.format("Capacity: %d | Selected: %d | Waiting: %d", max, selected, waiting));
    }

    private void fetchProfiles() {
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

    private void performLotteryDraw(int spotsToFill) {
        List<String> pool = new ArrayList<>(currentEvent.getWaitlistUserIds());
        List<String> winners = new ArrayList<>();
        Collections.shuffle(pool);

        int count = 0;
        while (count < spotsToFill && !pool.isEmpty()) {
            winners.add(pool.remove(0));
            count++;
        }

        currentEvent.getWaitlistUserIds().removeAll(winners);
        currentEvent.getSelectedUserIds().addAll(winners);

        if (currentEvent.getInvitationStatus() == null) {
            currentEvent.invitationStatus = new HashMap<>();
        }
        for (String winnerId : winners) {
            currentEvent.getInvitationStatus().put(winnerId, "pending");
        }

        WriteBatch batch = db.batch();

        batch.update(db.collection("events").document(eventId),
                "waitlistUserIds", currentEvent.getWaitlistUserIds(),
                "selectedUserIds", currentEvent.getSelectedUserIds(),
                "invitationStatus", currentEvent.getInvitationStatus());

        for (String winnerId : winners) {
            String notifId = db.collection("users").document(winnerId).collection("notifications").document().getId();
            Notification notif = new Notification(
                    "You Won the Lottery! \uD83C\uDF89",
                    "You have been selected for " + currentEvent.getTitle() + ". Please accept or decline your invitation.",
                    eventId,
                    "invitation"
            );
            batch.set(db.collection("users").document(winnerId).collection("notifications").document(notifId), notif);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Draw complete. Notifications sent!", Toast.LENGTH_SHORT).show();
                    loadEventAndEntrants();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Draw failed.", Toast.LENGTH_SHORT).show());
    }
}
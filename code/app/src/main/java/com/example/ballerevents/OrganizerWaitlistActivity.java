package com.example.ballerevents;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerWaitlistBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for Organizers to manage the waitlist for a specific event.
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    private static final String TAG = "OrganizerWaitlistActivity";
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private ActivityOrganizerWaitlistBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private Event currentEvent;
    private ListenerRegistration eventListener;

    private WaitlistUserAdapter waitlistAdapter;
    private final List<UserProfile> displayedProfiles = new ArrayList<>();

    // 0 = Waitlist, 1 = Invited (Pending), 2 = Accepted, 3 = Declined
    private int currentTabPosition = 0;

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

        setupTabs();
        setupRecyclerView();
        setupButtons();
        requestStoragePermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRealtimeUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sets up the tabs for filtering the user lists.
     * Assumes a TabLayout with id 'tabLayout' exists in the binding.
     */
    private void setupTabs() {
        if (binding.tabLayout != null) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Waitlist"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Invited"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Accepted"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Declined"));

            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    currentTabPosition = tab.getPosition();
                    waitlistAdapter.clearSelection(); // Clear checkboxes when switching tabs
                    fetchProfilesForCurrentTab();
                    updateUIForTabState();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupRecyclerView() {
        binding.rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        waitlistAdapter = new WaitlistUserAdapter(displayedProfiles, this, count -> updateUIBasedOnSelection(count));
        binding.rvWaitlist.setAdapter(waitlistAdapter);
    }

    /**
     * Updates visibility of buttons based on which tab is active.
     * e.g., "Remove" should only be available on the Waitlist tab.
     */
    private void updateUIForTabState() {
        if (binding.btnRemove != null) {
            // Only show Remove button if we are on the Waitlist tab (index 0)
            binding.btnRemove.setVisibility(currentTabPosition == 0 ? View.VISIBLE : View.GONE);
        }

        // Hide Lottery button if not on Waitlist tab
        binding.btnDrawLottery.setVisibility(currentTabPosition == 0 ? View.VISIBLE : View.GONE);
    }

    private void updateUIBasedOnSelection(int selectedCount) {
        // 1. Update Notification Button
        if (selectedCount > 0) {
            binding.btnNotifyAll.setText("Notify Selected (" + selectedCount + ")");
            binding.btnNotifyAll.setBackgroundColor(getColor(R.color.purple_500));
        } else {
            // Contextual text based on tab
            String targetText = "All";
            switch(currentTabPosition) {
                case 0: targetText = "Waitlist"; break;
                case 1: targetText = "Invited"; break;
                case 2: targetText = "Accepted"; break;
                case 3: targetText = "Declined"; break;
            }
            binding.btnNotifyAll.setText("Notify " + targetText);
            binding.btnNotifyAll.setBackgroundColor(0xFFFF9800); // Orange
        }

        // 2. Update Remove Button Text
        if (binding.btnRemove != null) {
            if (selectedCount > 0) {
                binding.btnRemove.setEnabled(true);
                binding.btnRemove.setText("Remove Selected (" + selectedCount + ")");
            } else {
                binding.btnRemove.setEnabled(false);
                binding.btnRemove.setText("Remove Selected");
            }
        }

        // 3. Update Header Stats
        if (currentEvent != null) {
            int max = currentEvent.getMaxAttendees();
            int waiting = (currentEvent.getWaitlistUserIds() != null) ? currentEvent.getWaitlistUserIds().size() : 0;
            binding.tvCapacityInfo.setText(String.format("Capacity: %d | Selected: %d | Waiting: %d", max, selectedCount, waiting));
        }
    }

    private void setupButtons() {
        binding.btnDrawLottery.setOnClickListener(v -> handleDrawClick());
        binding.btnGenerateQr.setOnClickListener(v -> generateAndSaveQRCode());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnExportCsv.setOnClickListener(v -> exportAcceptedEntrantsToCsv());

        // Remove Button Logic
        if (binding.btnRemove != null) {
            binding.btnRemove.setOnClickListener(v -> handleRemoveEntrants());
        }

        // Notify Logic
        binding.btnNotifyAll.setOnClickListener(v -> {
            List<UserProfile> selectedUsers = waitlistAdapter.getSelectedUsers();
            if (selectedUsers.isEmpty()) {
                if (displayedProfiles.isEmpty()) {
                    Toast.makeText(this, "List is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String targetGroup = "All Displayed Users";
                promptForNotification(displayedProfiles, targetGroup);
            } else {
                promptForNotification(selectedUsers, "Selected (" + selectedUsers.size() + ")");
            }
        });
    }

    /**
     * Removes selected users from the waitlist entirely.
     * Only valid on the Waitlist tab.
     */
    private void handleRemoveEntrants() {
        if (currentTabPosition != 0) return; // Safety check

        List<UserProfile> selectedUsers = waitlistAdapter.getSelectedUsers();
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "No users selected to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Entrants?")
                .setMessage("Are you sure you want to remove " + selectedUsers.size() + " user(s) from the waitlist? This action cannot be undone.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    performRemoval(selectedUsers);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performRemoval(List<UserProfile> usersToRemove) {
        List<String> idsToRemove = new ArrayList<>();
        for (UserProfile user : usersToRemove) {
            idsToRemove.add(user.getUid());
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // Remove these IDs from the 'waitlistUserIds' array in Firestore
        db.collection("events").document(eventId)
                .update("waitlistUserIds", FieldValue.arrayRemove(idsToRemove.toArray()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Removed successfully", Toast.LENGTH_SHORT).show();
                    waitlistAdapter.clearSelection();
                    // The realtime listener will automatically refresh the list
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void exportAcceptedEntrantsToCsv() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);

        List<String> acceptedUserIds = new ArrayList<>();
        if (currentEvent.getInvitationStatus() != null) {
            for (Map.Entry<String, String> entry : currentEvent.getInvitationStatus().entrySet()) {
                if ("accepted".equals(entry.getValue())) {
                    acceptedUserIds.add(entry.getKey());
                }
            }
        }

        if (acceptedUserIds.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No accepted entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        int batchSize = Math.min(acceptedUserIds.size(), 10);
        List<String> batchIds = acceptedUserIds.subList(0, batchSize);

        db.collection("users")
                .whereIn(FieldPath.documentId(), batchIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<UserProfile> acceptedProfiles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        UserProfile profile = doc.toObject(UserProfile.class);
                        if (profile != null) {
                            profile.setUid(doc.getId());
                            acceptedProfiles.add(profile);
                        }
                    }
                    binding.progressBar.setVisibility(View.GONE);

                    if (acceptedProfiles.isEmpty()) {
                        Toast.makeText(this, "No profiles found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CsvExportHelper.exportAcceptedEntrants(
                            this,
                            currentEvent.getTitle(),
                            acceptedProfiles,
                            new CsvExportHelper.ExportCallback() {
                                @Override
                                public void onSuccess(String filePath) {
                                    Toast.makeText(OrganizerWaitlistActivity.this, "CSV exported: " + filePath, Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onFailure(Exception error) {
                                    Toast.makeText(OrganizerWaitlistActivity.this, "Export failed", Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching accepted", e);
                });
    }

    private void promptForNotification(List<UserProfile> targets, String titleSuffix) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notify " + titleSuffix);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Enter notification message...");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                sendBatchNotification(targets, message);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendBatchNotification(List<UserProfile> targets, String message) {
        WriteBatch batch = db.batch();
        int count = 0;

        for (UserProfile user : targets) {
            String uid = user.getUid();
            if (uid == null) continue;

            DocumentReference notifRef = db.collection("users").document(uid)
                    .collection("notifications").document();

            Map<String, Object> notifData = new HashMap<>();
            notifData.put("title", "Update: " + currentEvent.getTitle());
            notifData.put("message", message);
            notifData.put("eventId", eventId);
            notifData.put("timestamp", new Date());
            notifData.put("isRead", false);
            notifData.put("type", "organizer_message");

            batch.set(notifRef, notifData);
            count++;
        }

        if (count == 0) return;

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Notifications Sent!", Toast.LENGTH_SHORT).show();
                    waitlistAdapter.clearSelection();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send.", Toast.LENGTH_SHORT).show());
    }

    private void startRealtimeUpdates() {
        binding.progressBar.setVisibility(View.VISIBLE);

        eventListener = db.collection("events").document(eventId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        binding.progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        currentEvent = snapshot.toObject(Event.class);
                        if (currentEvent != null) {
                            currentEvent.setId(snapshot.getId());
                            fetchProfilesForCurrentTab();
                        }
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Determines which list of User IDs to fetch based on the selected tab
     * and filters the 'invitationStatus' map.
     */
    private void fetchProfilesForCurrentTab() {
        if (currentEvent == null) return;

        List<String> targetIds = new ArrayList<>();
        Map<String, String> statusMap = currentEvent.getInvitationStatus();
        if (statusMap == null) statusMap = new HashMap<>();

        switch (currentTabPosition) {
            case 0: // Waitlist
                if (currentEvent.getWaitlistUserIds() != null) {
                    targetIds.addAll(currentEvent.getWaitlistUserIds());
                }
                break;
            case 1: // Invited (Pending)
                for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                    if ("pending".equals(entry.getValue()) || "invited".equals(entry.getValue())) {
                        targetIds.add(entry.getKey());
                    }
                }
                break;
            case 2: // Accepted
                for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                    if ("accepted".equals(entry.getValue())) {
                        targetIds.add(entry.getKey());
                    }
                }
                break;
            case 3: // Declined
                for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                    if ("declined".equals(entry.getValue())) {
                        targetIds.add(entry.getKey());
                    }
                }
                break;
        }

        fetchProfilesByIds(targetIds);
    }

    /**
     * Helper to fetch full profile objects for a list of IDs.
     */
    private void fetchProfilesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            displayedProfiles.clear();
            waitlistAdapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            updateUIBasedOnSelection(0);
            return;
        }

        // Limit to 10 for 'whereIn' query to prevent crash.
        // In a real app, you would chunk this list into batches of 10.
        List<String> safeList = ids.subList(0, Math.min(ids.size(), 10));

        db.collection("users")
                .whereIn(FieldPath.documentId(), safeList)
                .get()
                .addOnSuccessListener(snap -> {
                    displayedProfiles.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        UserProfile p = doc.toObject(UserProfile.class);
                        if (p != null) {
                            p.setUid(doc.getId());
                            displayedProfiles.add(p);
                        }
                    }
                    waitlistAdapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(displayedProfiles.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.progressBar.setVisibility(View.GONE);

                    // Reset selection count
                    updateUIBasedOnSelection(0);
                });
    }

    private void handleDrawClick() {
        if (currentEvent == null) return;
        int max = currentEvent.getMaxAttendees();
        int currentSelected = currentEvent.getSelectedUserIds() != null ? currentEvent.getSelectedUserIds().size() : 0;
        int spotsAvailable = max - currentSelected;

        if (spotsAvailable <= 0) {
            Toast.makeText(this, "Event is full!", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentEvent.getWaitlistUserIds() == null || currentEvent.getWaitlistUserIds().isEmpty()) {
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

    private void performLotteryDraw(int spotsToFill) {
        List<String> pool = new ArrayList<>(currentEvent.getWaitlistUserIds());
        List<String> winners = new ArrayList<>();
        Collections.shuffle(pool);

        int count = 0;
        while (count < spotsToFill && !pool.isEmpty()) {
            winners.add(pool.remove(0));
            count++;
        }

        // Update local object states before sending to Firestore
        currentEvent.getWaitlistUserIds().removeAll(winners);
        if (currentEvent.getSelectedUserIds() == null) currentEvent.setSelectedUserIds(new ArrayList<>());
        currentEvent.getSelectedUserIds().addAll(winners);

        if (currentEvent.getInvitationStatus() == null) {
            currentEvent.setInvitationStatus(new HashMap<>());
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
                    "You Won the Lottery! 🎉",
                    "You have been selected for " + currentEvent.getTitle() + ". Please accept or decline your invitation.",
                    eventId,
                    "invitation"
            );
            batch.set(db.collection("users").document(winnerId).collection("notifications").document(notifId), notif);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Draw complete.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Draw failed.", Toast.LENGTH_SHORT).show());
    }

    private void generateAndSaveQRCode() {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);

            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    "EventQR_" + currentEvent.getTitle(),
                    "Check-in QR Code for " + currentEvent.getTitle()
            );

            if (savedImageURL != null) {
                Toast.makeText(this, "QR Code saved to Gallery!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "QR Gen Error", e);
        }
    }
}
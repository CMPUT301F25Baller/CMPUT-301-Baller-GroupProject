package com.example.ballerevents;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerWaitlistBinding;
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
 * Allows viewing entrants, drawing lottery winners, and sending notifications.
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String TAG = "OrganizerWaitlistActivity";
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private enum ViewMode {
        WAITLIST, SELECTED, CANCELLED, ENROLLED
    }

    private ActivityOrganizerWaitlistBinding binding;
    private FirebaseFirestore db;
    private String eventId;
    private Event currentEvent;
    private ListenerRegistration eventListener;

    private WaitlistUserAdapter listAdapter;
    private final List<UserProfile> displayedProfiles = new ArrayList<>();
    private ViewMode currentMode = ViewMode.WAITLIST;

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
        if (eventListener != null) eventListener.remove();
    }

    /**
     * Requests storage permissions for saving QR codes (Android 6.0 to 9.0).
     */
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

    /**
     * Initializes the RecyclerView and its adapter.
     */
    private void setupRecyclerView() {
        binding.rvWaitlist.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Adapter (2 arguments: List + Listener)
        listAdapter = new WaitlistUserAdapter(displayedProfiles, count -> updateUIBasedOnSelection(count));

        // Set Item Click Listener
        listAdapter.setOnItemClickListener(this::showUserOptions);

        binding.rvWaitlist.setAdapter(listAdapter);
    }

    /**
     * Updates the UI buttons and text based on how many items are selected.
     * @param selectedCount Number of currently selected items.
     */
    private void updateUIBasedOnSelection(int selectedCount) {
        String modeName = getModeName(currentMode);
        if (selectedCount > 0) {
            binding.btnNotifyAll.setText("Notify Selected (" + selectedCount + ")");
            binding.btnNotifyAll.setBackgroundColor(getColor(R.color.purple_500));
        } else {
            binding.btnNotifyAll.setText("Notify All " + modeName);
            binding.btnNotifyAll.setBackgroundColor(0xFFFF9800);
        }

        if (currentEvent != null) {
            int max = currentEvent.getMaxAttendees();
            int waiting = (currentEvent.getWaitlistUserIds() != null) ? currentEvent.getWaitlistUserIds().size() : 0;
            binding.tvCapacityInfo.setText(String.format("Capacity: %d | Checked: %d | Waiting: %d", max, selectedCount, waiting));
        }
    }

    private String getModeName(ViewMode mode) {
        switch (mode) {
            case SELECTED: return "Selected";
            case CANCELLED: return "Cancelled";
            case ENROLLED: return "Enrolled";
            default: return "Waitlist";
        }
    }

    /**
     * Sets up click listeners for all interactive buttons.
     */
    private void setupButtons() {
        binding.btnDrawLottery.setOnClickListener(v -> handleDrawClick());
        binding.btnGenerateQr.setOnClickListener(v -> generateAndSaveQRCode());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnExportCsv.setOnClickListener(v -> exportAcceptedEntrantsToCsv());
        binding.btnFilter.setOnClickListener(this::showFilterMenu);

        binding.btnNotifyAll.setOnClickListener(v -> {
            List<UserProfile> selectedUsers = listAdapter.getSelectedUsers();
            if (selectedUsers.isEmpty()) {
                if (displayedProfiles.isEmpty()) {
                    Toast.makeText(this, "List is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                promptForNotification(displayedProfiles, "All " + getModeName(currentMode));
            } else {
                promptForNotification(selectedUsers, "Selected (" + selectedUsers.size() + ")");
            }
        });
    }

    /**
     * Displays a popup menu to filter the user list by status.
     * @param v The view to anchor the popup to.
     */
    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add(0, 0, 0, "Show Waitlist");
        popup.getMenu().add(0, 1, 1, "Show Selected (Invited)");
        popup.getMenu().add(0, 2, 2, "Show Cancelled");
        popup.getMenu().add(0, 3, 3, "Show Enrolled (Accepted)");

        popup.getMenu().add(1, 4, 4, "View Entrant Map 🗺️");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0: currentMode = ViewMode.WAITLIST; break;
                case 1: currentMode = ViewMode.SELECTED; break;
                case 2: currentMode = ViewMode.CANCELLED; break;
                case 3: currentMode = ViewMode.ENROLLED; break;
                case 4:
                    Intent intent = new Intent(this, OrganizerMapActivity.class);
                    intent.putExtra(OrganizerMapActivity.EXTRA_EVENT_ID, eventId);
                    startActivity(intent);
                    return true;
            }
            binding.tvTitle.setText(getModeName(currentMode));
            binding.btnDrawLottery.setVisibility(currentMode == ViewMode.WAITLIST ? View.VISIBLE : View.GONE);

            listAdapter.clearSelection();
            fetchProfilesForCurrentMode();
            return true;
        });
        popup.show();
    }

    /**
     * Shows options (Message, Cancel) when a specific user is clicked.
     * @param user The user profile selected.
     */
    private void showUserOptions(UserProfile user) {
        boolean isCancellable = (currentMode == ViewMode.SELECTED || currentMode == ViewMode.ENROLLED);
        List<String> options = new ArrayList<>();
        options.add("Send Message");
        if (isCancellable) options.add("Cancel Entrant");

        new AlertDialog.Builder(this)
                .setTitle(user.getName())
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String selection = options.get(which);
                    if (selection.equals("Send Message")) {
                        promptForNotification(Collections.singletonList(user), user.getName());
                    } else if (selection.equals("Cancel Entrant")) {
                        confirmCancelEntrant(user);
                    }
                })
                .show();
    }

    private void confirmCancelEntrant(UserProfile user) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Entrant")
                .setMessage("Remove " + user.getName() + "? This frees a spot.")
                .setPositiveButton("Yes", (d, w) -> cancelEntrant(user))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelEntrant(UserProfile user) {
        String uid = user.getUid();
        if (uid == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("selectedUserIds", FieldValue.arrayRemove(uid));
        updates.put("cancelledUserIds", FieldValue.arrayUnion(uid));
        updates.put("invitationStatus." + uid, "cancelled");

        db.collection("events").document(eventId).update(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();
                    sendCancellationNotification(user);
                });
    }

    private void sendCancellationNotification(UserProfile user) {
        DocumentReference notifRef = db.collection("users").document(user.getUid())
                .collection("notifications").document();
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Event Update: " + currentEvent.getTitle());
        data.put("message", "Your invitation has been cancelled.");
        data.put("eventId", eventId);
        data.put("timestamp", new Date());
        data.put("isRead", false);
        notifRef.set(data);
    }

    /**
     * Exports the list of accepted entrants to a CSV file.
     */
    private void exportAcceptedEntrantsToCsv() {
        if (currentEvent == null) return;
        List<String> acceptedIds = new ArrayList<>();
        if (currentEvent.getInvitationStatus() != null) {
            for (Map.Entry<String, String> entry : currentEvent.getInvitationStatus().entrySet()) {
                if ("accepted".equals(entry.getValue())) acceptedIds.add(entry.getKey());
            }
        }
        if (acceptedIds.isEmpty()) {
            Toast.makeText(this, "No accepted entrants.", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .whereIn(FieldPath.documentId(), acceptedIds.subList(0, Math.min(acceptedIds.size(), 10)))
                .get()
                .addOnSuccessListener(snap -> {
                    List<UserProfile> profiles = snap.toObjects(UserProfile.class);
                    CsvExportHelper.exportAcceptedEntrants(this, currentEvent.getTitle(), profiles, new CsvExportHelper.ExportCallback() {
                        @Override public void onSuccess(String path) {
                            Toast.makeText(OrganizerWaitlistActivity.this, "Saved: " + path, Toast.LENGTH_LONG).show();
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        @Override public void onFailure(Exception e) {
                            Toast.makeText(OrganizerWaitlistActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    });
                });
    }

    private void promptForNotification(List<UserProfile> targets, String titleSuffix) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notify " + titleSuffix);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);
        builder.setPositiveButton("Send", (d, w) -> {
            String msg = input.getText().toString().trim();
            if (!msg.isEmpty()) sendBatchNotification(targets, msg);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendBatchNotification(List<UserProfile> targets, String message) {
        WriteBatch batch = db.batch();
        int count = 0;
        for (UserProfile user : targets) {
            if (user.getUid() == null) continue;
            DocumentReference ref = db.collection("users").document(user.getUid())
                    .collection("notifications").document();
            Map<String, Object> data = new HashMap<>();
            data.put("title", "Update: " + currentEvent.getTitle());
            data.put("message", message);
            data.put("timestamp", new Date());
            data.put("isRead", false);
            batch.set(ref, data);
            count++;
        }
        if (count > 0) {
            batch.commit().addOnSuccessListener(a -> {
                Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show();
                listAdapter.clearSelection();
            });
        }
    }

    private void startRealtimeUpdates() {
        binding.progressBar.setVisibility(View.VISIBLE);
        eventListener = db.collection("events").document(eventId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed", e);
                        binding.progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        currentEvent = snapshot.toObject(Event.class);
                        if (currentEvent != null) {
                            currentEvent.setId(snapshot.getId());
                            fetchProfilesForCurrentMode();
                        }
                    }
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    private void fetchProfilesForCurrentMode() {
        if (currentEvent == null) return;
        List<String> targetIds = new ArrayList<>();
        Map<String, String> statusMap = currentEvent.getInvitationStatus() != null ? currentEvent.getInvitationStatus() : new HashMap<>();

        switch (currentMode) {
            case WAITLIST:
                if (currentEvent.getWaitlistUserIds() != null) targetIds.addAll(currentEvent.getWaitlistUserIds());
                break;
            case SELECTED:
                for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                    if ("pending".equals(entry.getValue()) || "invited".equals(entry.getValue())) targetIds.add(entry.getKey());
                }
                break;
            case CANCELLED:
                if (currentEvent.getCancelledUserIds() != null) targetIds.addAll(currentEvent.getCancelledUserIds());
                break;
            case ENROLLED:
                for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                    if ("accepted".equals(entry.getValue())) targetIds.add(entry.getKey());
                }
                break;
        }
        fetchProfilesByIds(targetIds);
    }

    private void fetchProfilesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            displayedProfiles.clear();
            listAdapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            updateUIBasedOnSelection(0);
            return;
        }
        db.collection("users")
                .whereIn(FieldPath.documentId(), ids.subList(0, Math.min(ids.size(), 10)))
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
                    listAdapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(displayedProfiles.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.progressBar.setVisibility(View.GONE);
                    updateUIBasedOnSelection(0);
                });
    }

    /**
     * Prepares and displays the Lottery Draw dialog.
     * Calculates available spots and allows the user to specify how many to sample.
     */
    private void handleDrawClick() {
        if (currentEvent == null) return;
        int max = currentEvent.getMaxAttendees();
        int selected = (currentEvent.getSelectedUserIds() != null) ? currentEvent.getSelectedUserIds().size() : 0;

        // Calculate maximum possible spots to fill
        int maxSpots = max - selected;

        // Safety check if event is full
        if (maxSpots <= 0) {
            Toast.makeText(this, "Event is already at full capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cap the draw amount at the number of people actually on the waitlist
        int waitlistSize = (currentEvent.getWaitlistUserIds() != null) ? currentEvent.getWaitlistUserIds().size() : 0;
        int defaultDrawAmount = Math.min(maxSpots, waitlistSize);

        if (waitlistSize == 0) {
            Toast.makeText(this, "No one on the waitlist to draw", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Input Field for the Dialog
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(defaultDrawAmount));

        // Show Dialog allowing user to SPECIFY the number (US 02.05.02)
        new AlertDialog.Builder(this)
                .setTitle("Draw Lottery")
                .setMessage("How many entrants to sample? (Max: " + defaultDrawAmount + ")")
                .setView(input)
                .setPositiveButton("Draw", (dialog, which) -> {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        int amount = Integer.parseInt(text);
                        if (amount > 0 && amount <= defaultDrawAmount) {
                            performLotteryDraw(amount);
                        } else {
                            Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Executes the lottery logic: shuffles waitlist, picks winners, moves them to selected list,
     * and sends notifications.
     * @param spots The number of entrants to sample.
     */
    private void performLotteryDraw(int spots) {
        if (currentEvent.getWaitlistUserIds() == null) return;

        List<String> pool = new ArrayList<>(currentEvent.getWaitlistUserIds());
        Collections.shuffle(pool);

        // Double check bounds to prevent crash
        int actualDraw = Math.min(pool.size(), spots);
        List<String> winners = pool.subList(0, actualDraw);
        List<String> losers = new ArrayList<>(pool.subList(actualDraw, pool.size()));

        // Update Event Data locally
        currentEvent.getWaitlistUserIds().removeAll(winners);
        if (currentEvent.getSelectedUserIds() == null) currentEvent.setSelectedUserIds(new ArrayList<>());
        currentEvent.getSelectedUserIds().addAll(winners);

        if (currentEvent.getInvitationStatus() == null) currentEvent.setInvitationStatus(new HashMap<>());
        for (String w : winners) currentEvent.getInvitationStatus().put(w, "pending");

        // Batch Write to Firestore
        WriteBatch batch = db.batch();
        batch.update(db.collection("events").document(eventId),
                "waitlistUserIds", currentEvent.getWaitlistUserIds(),
                "selectedUserIds", currentEvent.getSelectedUserIds(),
                "invitationStatus", currentEvent.getInvitationStatus());

        // Notify Winners
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

        // Notify Losers
        for (String loserId : losers) {
            String notifId = db.collection("users").document(loserId).collection("notifications").document().getId();
            Notification notif = new Notification(
                    "Lottery Update",
                    "You were not selected in the recent draw for " + currentEvent.getTitle() + ". You remain on the waitlist for future chances.",
                    eventId,
                    "info"
            );
            batch.set(db.collection("users").document(loserId).collection("notifications").document(notifId), notif);
        }

        batch.commit().addOnSuccessListener(a -> Toast.makeText(this, "Draw Complete", Toast.LENGTH_SHORT).show());
    }

    /**
     * Generates a QR code for the event and saves it to the device gallery.
     */
    private void generateAndSaveQRCode() {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            String saved = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QR_" + eventId, "Event QR");
            Toast.makeText(this, saved != null ? "Saved to Gallery" : "Error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Log.e(TAG, "QR Error", e); }
    }
}
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
 *
 * <p>This activity provides the following features:</p>
 * <ul>
 * <li><b>Real-time Dashboard:</b> Displays live capacity, checked selection count, and total waitlist count.</li>
 * <li><b>Waitlist Management:</b> Lists all entrants currently on the waitlist.</li>
 * <li><b>Multi-Selection:</b> Allows organizers to select specific entrants via checkboxes.</li>
 * <li><b>Smart Notifications:</b> Sends notifications to either "Selected" users or "All" users based on selection state.</li>
 * <li><b>Lottery System:</b> Randomly samples entrants from the waitlist and moves them to the "Selected" list in Firestore.</li>
 * <li><b>QR Generation:</b> Generates and saves a promotional QR code for the event.</li>
 * <li><b>CSV Export:</b> Exports accepted entrants' names and emails to a CSV file.</li>
 * </ul>
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    /** Intent extra key for passing the Event ID. */
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    private static final String TAG = "OrganizerWaitlistActivity";
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    /** View binding for the activity layout. */
    private ActivityOrganizerWaitlistBinding binding;

    /** Firestore instance for database operations. */
    private FirebaseFirestore db;

    /** The ID of the event being managed. */
    private String eventId;

    /** Local object representation of the current event data. */
    private Event currentEvent;

    /** Listener for real-time updates on the event document. */
    private ListenerRegistration eventListener;

    /** Adapter for displaying the list of entrants. */
    private WaitlistUserAdapter waitlistAdapter;

    /** List of user profiles currently on the waitlist. */
    private final List<UserProfile> waitlistProfiles = new ArrayList<>();

    /**
     * Called when the activity is first created.
     * Initializes the UI, Firestore, and setup methods.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in onSaveInstanceState.
     */
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

    /**
     * Called when the activity becomes visible.
     * Starts listening to real-time Firestore updates.
     */
    @Override
    protected void onStart() {
        super.onStart();
        startRealtimeUpdates();
    }

    /**
     * Called when the activity is no longer visible.
     * Removes the Firestore listener to conserve resources.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    /**
     * Requests storage permission for saving CSV and QR codes.
     */
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Only needed for Android 6-9
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }
        // Android 10+ uses scoped storage (no permission needed)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot save files.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Configures the RecyclerView with a LayoutManager and the {@link WaitlistUserAdapter}.
     * Passes a lambda to handle selection changes, updating the UI accordingly.
     */
    private void setupRecyclerView() {
        binding.rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        // Pass listener that updates buttons AND stats bar when checkboxes change
        waitlistAdapter = new WaitlistUserAdapter(waitlistProfiles, this, count -> updateUIBasedOnSelection(count));
        binding.rvWaitlist.setAdapter(waitlistAdapter);
    }

    /**
     * Updates the "Notify" button text/color and the "Selected" count in the header
     * based on how many checkboxes are currently active.
     *
     * @param selectedCount The number of users currently selected in the adapter.
     */
    private void updateUIBasedOnSelection(int selectedCount) {
        // 1. Update Notification Button Appearance
        if (selectedCount > 0) {
            binding.btnNotifyAll.setText("Notify Selected (" + selectedCount + ")");
            binding.btnNotifyAll.setBackgroundColor(getColor(R.color.purple_500));
        } else {
            binding.btnNotifyAll.setText("Notify All Waitlist");
            binding.btnNotifyAll.setBackgroundColor(0xFFFF9800); // Orange
        }

        // 2. Update Header Stats (Selected = Checkbox Selection)
        if (currentEvent != null) {
            int max = currentEvent.getMaxAttendees();
            int waiting = (currentEvent.getWaitlistUserIds() != null) ? currentEvent.getWaitlistUserIds().size() : 0;

            binding.tvCapacityInfo.setText(String.format("Capacity: %d | Selected: %d | Waiting: %d", max, selectedCount, waiting));
        }
    }

    /**
     * Sets up click listeners for all interactive buttons in the activity.
     */
    private void setupButtons() {
        binding.btnDrawLottery.setOnClickListener(v -> handleDrawClick());
        binding.btnGenerateQr.setOnClickListener(v -> generateAndSaveQRCode());
        binding.btnBack.setOnClickListener(v -> finish());

        // NEW: Export CSV Button
        binding.btnExportCsv.setOnClickListener(v -> exportAcceptedEntrantsToCsv());

        // Smart Notify Button Logic
        binding.btnNotifyAll.setOnClickListener(v -> {
            List<UserProfile> selectedUsers = waitlistAdapter.getSelectedUsers();

            if (selectedUsers.isEmpty()) {
                // Scenario A: No selection -> Notify ALL in waitlist
                if (waitlistProfiles.isEmpty()) {
                    Toast.makeText(this, "Waitlist is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                promptForNotification(waitlistProfiles, "All Waitlist Entrants");
            } else {
                // Scenario B: Selection active -> Notify only SELECTED
                promptForNotification(selectedUsers, "Selected (" + selectedUsers.size() + ")");
            }
        });
    }

    // --- CSV EXPORT LOGIC ---

    /**
     * Exports accepted entrants to a CSV file.
     *
     * <p>This method queries Firestore for all users who have "accepted" their
     * invitation (invitationStatus == "accepted"), fetches their full profiles,
     * and exports their names and emails to a CSV file saved in Downloads.</p>
     */
    private void exportAcceptedEntrantsToCsv() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);

        // Get list of user IDs who accepted
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

        // Fetch user profiles (Firestore 'whereIn' limited to 10 items)
        // For production, you'd batch this in groups of 10
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
                        Toast.makeText(this, "No profiles found for accepted entrants", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Export to CSV
                    CsvExportHelper.exportAcceptedEntrants(
                            this,
                            currentEvent.getTitle(),
                            acceptedProfiles,
                            new CsvExportHelper.ExportCallback() {
                                @Override
                                public void onSuccess(String filePath) {
                                    Toast.makeText(OrganizerWaitlistActivity.this,
                                            "CSV exported successfully to " + filePath,
                                            Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Exception error) {
                                    Toast.makeText(OrganizerWaitlistActivity.this,
                                            "Export failed: " + error.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "CSV export failed", error);
                                }
                            }
                    );

                    // Show info dialog about export
                    new AlertDialog.Builder(this)
                            .setTitle("Export Complete")
                            .setMessage("Exported " + acceptedProfiles.size() + " accepted entrants.\n\n" +
                                    "File saved to Downloads folder.\n" +
                                    "Note: If more than 10 entrants accepted, only the first 10 were exported " +
                                    "(Firestore limitation).")
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch accepted entrants: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error fetching accepted entrants", e);
                });
    }

    // --- NOTIFICATION LOGIC ---

    /**
     * Displays an input dialog allowing the organizer to type a custom message.
     *
     * @param targets     The list of users who will receive the notification.
     * @param titleSuffix A string appended to the dialog title (e.g., "All Users" or "Selected (2)").
     */
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
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Sends a notification to a list of users using a Firestore WriteBatch.
     *
     * @param targets The list of UserProfile objects to notify.
     * @param message The message body entered by the organizer.
     */
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
                    waitlistAdapter.clearSelection(); // Clear checkboxes after sending
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send.", Toast.LENGTH_SHORT).show());
    }

    // --- REAL-TIME DATA LOGIC ---

    /**
     * Attaches a real-time listener to the Firestore document for this event.
     * This ensures the UI updates immediately if the lottery is run or users join/leave.
     */
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
                            // Initial header update (Selected starts at 0 for checked state)
                            updateUIBasedOnSelection(0);
                            fetchWaitlistProfiles();
                        }
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Fetches full UserProfile documents for the IDs currently in the waitlist.
     * <p>Note: This uses a 'whereIn' query which is limited to 10 items in Firestore.
     * For production with large lists, this should be batched or paginated.</p>
     */
    private void fetchWaitlistProfiles() {
        List<String> waitlistIds = currentEvent.getWaitlistUserIds();

        if (waitlistIds == null || waitlistIds.isEmpty()) {
            waitlistProfiles.clear();
            waitlistAdapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        // Limit to 10 for 'whereIn' query to prevent crash
        db.collection("users")
                .whereIn(FieldPath.documentId(), waitlistIds.subList(0, Math.min(waitlistIds.size(), 10)))
                .get()
                .addOnSuccessListener(snap -> {
                    waitlistProfiles.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        UserProfile p = doc.toObject(UserProfile.class);
                        if (p != null) {
                            p.setUid(doc.getId());
                            waitlistProfiles.add(p);
                        }
                    }
                    waitlistAdapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(waitlistProfiles.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    // --- LOTTERY & QR LOGIC ---

    /**
     * Validates the event state and triggers the lottery draw dialog if valid.
     */
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

    /**
     * Executes the random lottery draw logic.
     * <ol>
     * <li>Shuffles the waitlist.</li>
     * <li>Selects the top N entrants (where N is spots available).</li>
     * <li>Updates the Event document (moves users from waitlist -> selected).</li>
     * <li>Sends "You Won" notifications to the winners.</li>
     * </ol>
     *
     * @param spotsToFill The number of entrants to select.
     */
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
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Draw complete. Stats updating...", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Draw failed.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Generates a QR Code for the current event ID and saves it to the device's gallery.
     */
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
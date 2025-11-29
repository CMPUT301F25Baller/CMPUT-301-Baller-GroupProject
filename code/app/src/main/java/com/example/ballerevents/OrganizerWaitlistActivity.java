package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerWaitlistBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizerWaitlistActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String TAG = "OrganizerWaitlistActivity";

    private ActivityOrganizerWaitlistBinding binding;
    private FirebaseFirestore db;

    private String eventId;
    private String eventTitle = "Event";
    private final List<UserProfile> entrants = new ArrayList<>();
    private WaitlistUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerWaitlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        // Ensure we bind to the correct recycler view ID from your XML
        adapter = new WaitlistUserAdapter(entrants, this);
        binding.rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWaitlist.setAdapter(adapter);

        if (binding.btnRunLottery != null) {
            binding.btnRunLottery.setOnClickListener(v -> showLotteryDialog());
        }

        if (eventId != null) {
            fetchEventDetails();
            loadWaitlist();
        } else {
            Toast.makeText(this, "Error: No Event ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String title = snapshot.getString("title");
                        if (title != null && !title.isEmpty()) {
                            eventTitle = title;
                        }
                    }
                });
    }

    private void loadWaitlist() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        db.collection("users")
                .whereArrayContains("appliedEventIds", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    entrants.clear();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            UserProfile profile = doc.toObject(UserProfile.class);
                            if (profile != null) {
                                // CRITICAL: Manually set ID to ensure it is available for the lottery
                                profile.setId(doc.getId());
                                entrants.add(profile);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(entrants.isEmpty() ? View.VISIBLE : View.GONE);

                    if (binding.tvWaitlistCount != null) {
                        binding.tvWaitlistCount.setText("Waitlist: " + entrants.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading waitlist", e);
                    Toast.makeText(this, "Failed to load waitlist", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    private void showLotteryDialog() {
        if (entrants.isEmpty()) {
            Toast.makeText(this, "No entrants to sample from.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Run Lottery");
        builder.setMessage("How many entrants do you want to select?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Select", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                int count = Integer.parseInt(val);
                runLottery(count);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void runLottery(int numberToSelect) {
        if (numberToSelect > entrants.size()) {
            Toast.makeText(this, "Cannot select more than waitlist size.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Shuffle and Pick
        List<UserProfile> shuffled = new ArrayList<>(entrants);
        Collections.shuffle(shuffled);
        List<UserProfile> selectedUsers = shuffled.subList(0, numberToSelect);
        List<String> selectedIds = new ArrayList<>();

        WriteBatch batch = db.batch();

        for (UserProfile u : selectedUsers) {
            if (u.getId() == null) {
                Log.e(TAG, "User ID is null! Skipping user.");
                continue;
            }
            selectedIds.add(u.getId());

            // 2a. Update User: Add eventId to 'invitedEventIds'
            // This fixes the issue you saw where the user didn't have the reference
            batch.update(db.collection("users").document(u.getId()),
                    "invitedEventIds", FieldValue.arrayUnion(eventId));

            // 2b. Prepare Notification for THIS user
            String notifId = db.collection("users").document(u.getId()).collection("notifications").document().getId();

            Map<String, Object> notification = new HashMap<>();
            notification.put("title", "Congratulations!");
            notification.put("message", "You were chosen for " + eventTitle + ". Accept invitation?");
            notification.put("timestamp", FieldValue.serverTimestamp());
            notification.put("isRead", false);
            notification.put("isInvitation", true);
            notification.put("eventId", eventId);

            // Log path for debugging where the notification is actually going
            Log.d(TAG, "Queueing notification for: users/" + u.getId() + "/notifications/" + notifId);

            batch.set(db.collection("users").document(u.getId()).collection("notifications").document(notifId), notification);
        }

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "No valid users selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Update Event: Add to 'chosenUserIds'
        batch.update(db.collection("events").document(eventId),
                "chosenUserIds", FieldValue.arrayUnion(selectedIds.toArray()));

        // 4. Commit
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Lottery complete! Invitations sent to " + selectedIds.size() + " users.", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lottery failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lottery commit failed", e);
        });
    }
}
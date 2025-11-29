package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Activity displaying a list of notification logs to the user.
 * <p>
 * The screen contains a RecyclerView showing log entries in text form,
 * along with chips that allow switching between:
 * <ul>
 *     <li>Only new/unread items</li>
 *     <li>All notification logs</li>
 * </ul>
 * A "Mark All" button allows marking all notifications as read and updates
 * the RecyclerView accordingly.
 * </p>
 *
 * <p>This activity uses {@link SimpleTextAdapter} to render the text rows.</p>
 */
public class NotificationLogsActivity extends AppCompatActivity implements NotificationLogsAdapter.OnItemAction {

    /**
     * ViewBinding for accessing the notification logs layout.
     */
    private ActivityNotificationLogsBinding binding;

    /**
     * RecyclerView adapter for displaying notification text rows.
     */
    private NotificationLogsAdapter adapter;

    private FirebaseFirestore db;

    private String userId;

    // List to hold combined logs
    private List<NotificationLog> displayedLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        setupRecyclerView();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new NotificationLogsAdapter(this);
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLogs.setAdapter(adapter);

        // Setup Chips (Simple filter logic)
        binding.chipAll.setOnClickListener(v -> adapter.submitList(new ArrayList<>(displayedLogs)));
        binding.chipNew.setOnClickListener(v -> filterNewLogs());
        binding.btnMarkAll.setOnClickListener(v -> markAllRead());
    }

    private void loadData() {
        // 1. Get Static Logs (Mock data)
        List<NotificationLog> staticLogs = NotificationLogsStore.getAll();

        // 2. Get Real Invitations from Firestore
        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);
                        if (user != null) {
                            List<String> invitedIds = user.getInvitedEventIds();

                            if (invitedIds != null && !invitedIds.isEmpty()) {
                                fetchEventDetailsAndMerge(invitedIds, staticLogs);
                            } else {
                                // No invites, just show static logs
                                displayedLogs = staticLogs;
                                adapter.submitList(displayedLogs);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Fallback to static only on error
                        displayedLogs = staticLogs;
                        adapter.submitList(displayedLogs);
                    });
        } else {
            displayedLogs = staticLogs;
            adapter.submitList(displayedLogs);
        }
    }

    /**
     * Fetches event titles for invitations to create nice log entries.
     */
    private void fetchEventDetailsAndMerge(List<String> invitedIds, List<NotificationLog> staticLogs) {
        // This is a simplified fetch loop. In production, consider a 'whereIn' query if IDs < 10.
        List<NotificationLog> invitationLogs = new ArrayList<>();

        // Counter to know when all async fetches are done
        final int[] pendingFetches = {invitedIds.size()};

        for (String eventId : invitedIds) {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(eventDoc -> {
                        if (eventDoc.exists()) {
                            String title = eventDoc.getString("title");
                            NotificationLog inviteLog = new NotificationLog(
                                    eventId, // ID
                                    "You're invited to join " + title, // Title/Msg
                                    "Action Required", // Timestamp label
                                    R.drawable.ic_notification_alert, // Ensure this drawable exists
                                    false, // isRead
                                    true,  // isInvitation
                                    eventId // eventId
                            );
                            invitationLogs.add(inviteLog);
                        }
                        checkAndCombine(pendingFetches, invitationLogs, staticLogs);
                    })
                    .addOnFailureListener(e -> checkAndCombine(pendingFetches, invitationLogs, staticLogs));
        }
    }

    private void checkAndCombine(int[] counter, List<NotificationLog> invites, List<NotificationLog> staticLogs) {
        counter[0]--;
        if (counter[0] <= 0) {
            // All fetches done, merge lists
            // Put invites at the top
            displayedLogs.clear();
            displayedLogs.addAll(invites);
            displayedLogs.addAll(staticLogs);
            adapter.submitList(new ArrayList<>(displayedLogs));
        }
    }

    // --- Action Implementations ---

    @Override
    public void onAcceptInvite(NotificationLog log) {
        if (userId == null) return;

        db.collection("users").document(userId)
                .update(
                        "joinedEventIds", FieldValue.arrayUnion(log.eventId),
                        "invitedEventIds", FieldValue.arrayRemove(log.eventId),
                        "appliedEventIds", FieldValue.arrayRemove(log.eventId)
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Invitation Accepted!", Toast.LENGTH_SHORT).show();
                    // Remove this log from the list locally to update UI immediately
                    removeLogFromList(log);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to accept.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRejectInvite(NotificationLog log) {
        if (userId == null) return;

        db.collection("users").document(userId)
                .update("invitedEventIds", FieldValue.arrayRemove(log.eventId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Invitation Declined.", Toast.LENGTH_SHORT).show();
                    removeLogFromList(log);
                });
    }

    @Override
    public void onMarkRead(NotificationLog log) {
        // For static mock data, we just update local state
        log.isRead = true;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onOpen(NotificationLog log) {
        Toast.makeText(this, "Opening details for " + log.title, Toast.LENGTH_SHORT).show();
    }

    // --- Helpers ---

    private void removeLogFromList(NotificationLog log) {
        List<NotificationLog> current = new ArrayList<>(adapter.getCurrentList());
        // Find by ID and remove
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).id.equals(log.id)) {
                current.remove(i);
                break;
            }
        }
        displayedLogs = current;
        adapter.submitList(current);
    }

    private void filterNewLogs() {
        List<NotificationLog> filtered = new ArrayList<>();
        for (NotificationLog n : displayedLogs) {
            if (!n.isRead) filtered.add(n);
        }
        adapter.submitList(filtered);
    }

    private void markAllRead() {
        for (NotificationLog n : displayedLogs) {
            n.isRead = true;
        }
        adapter.notifyDataSetChanged();
    }
}
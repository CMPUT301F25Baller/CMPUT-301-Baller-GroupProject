package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays the user's notification log.
 * Fetches real data from users/{uid}/notifications while also showing
 * static mock logs and event invitations.
 */
public class NotificationLogsActivity extends AppCompatActivity implements NotificationLogsAdapter.OnItemAction {

    private ActivityNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration notifListener;

    private String userId;
    private List<Notification> allNotifications = new ArrayList<>();
    private List<NotificationLog> staticLogs = new ArrayList<>();
    private List<Object> displayedLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        setupRecyclerView();
        setupButtons();
        loadStaticLogs();

        if (userId != null) {
            setupRealtimeListener();
            loadEventInvitations();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            adapter.submitList(staticLogs);
        }
    }

    // -------------------------------------------------------------------------
    // SETUP
    // -------------------------------------------------------------------------

    private void setupRecyclerView() {
        adapter = new NotificationLogsAdapter(this);
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLogs.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.chipAll.setOnClickListener(v -> adapter.submitList(new ArrayList<>(displayedLogs)));
        binding.chipNew.setOnClickListener(v -> filterNewLogs());
        binding.btnMarkAll.setOnClickListener(v -> markAllAsRead());
    }

    private void loadStaticLogs() {
        staticLogs = NotificationLogsStore.getAll(); // Your local mock notifications
    }

    // -------------------------------------------------------------------------
    // REAL-TIME FIRESTORE NOTIFICATION FETCH
    // -------------------------------------------------------------------------

    private void setupRealtimeListener() {
        notifListener = db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("NotifActivity", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        allNotifications = snapshots.toObjects(Notification.class);

                        for (int i = 0; i < snapshots.size(); i++) {
                            allNotifications.get(i).setId(snapshots.getDocuments().get(i).getId());
                        }

                        mergeAndDisplayLogs();
                    }
                });
    }

    // -------------------------------------------------------------------------
    // EVENT INVITATION FETCH
    // -------------------------------------------------------------------------

    private void loadEventInvitations() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile user = documentSnapshot.toObject(UserProfile.class);
                    if (user != null && user.getInvitedEventIds() != null && !user.getInvitedEventIds().isEmpty()) {
                        fetchEventDetailsAndMerge(user.getInvitedEventIds());
                    } else {
                        mergeAndDisplayLogs();
                    }
                })
                .addOnFailureListener(e -> mergeAndDisplayLogs());
    }

    private void fetchEventDetailsAndMerge(List<String> invitedIds) {
        List<NotificationLog> inviteLogs = new ArrayList<>();
        final int[] pending = {invitedIds.size()};

        for (String eventId : invitedIds) {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(eventDoc -> {
                        if (eventDoc.exists()) {
                            String title = eventDoc.getString("title");
                            NotificationLog log = new NotificationLog(
                                    eventId,
                                    "You're invited to join " + title,
                                    "Action Required",
                                    R.drawable.ic_notification_alert,
                                    false,
                                    true,
                                    eventId
                            );
                            inviteLogs.add(log);
                        }
                        checkMergeCompletion(pending, inviteLogs);
                    })
                    .addOnFailureListener(e -> checkMergeCompletion(pending, inviteLogs));
        }
    }

    private void checkMergeCompletion(int[] pending, List<NotificationLog> invites) {
        pending[0]--;
        if (pending[0] <= 0) {
            displayedLogs.addAll(invites);
            mergeAndDisplayLogs();
        }
    }

    // -------------------------------------------------------------------------
    // MERGE STATIC, REALTIME, AND INVITE LOGS
    // -------------------------------------------------------------------------

    private void mergeAndDisplayLogs() {
        displayedLogs.clear();
        displayedLogs.addAll(allNotifications);
        displayedLogs.addAll(staticLogs);
        adapter.submitList(new ArrayList<>(displayedLogs));
        binding.tvEmpty.setVisibility(displayedLogs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // -------------------------------------------------------------------------
    // ACTIONS — INVITATION RESPONSE / DELETE / READ
    // -------------------------------------------------------------------------

    @Override
    public void onAcceptInvite(Object item) {
        if (item instanceof NotificationLog log && userId != null) {
            db.collection("users").document(userId)
                    .update(
                            "joinedEventIds", FieldValue.arrayUnion(log.eventId),
                            "invitedEventIds", FieldValue.arrayRemove(log.eventId),
                            "appliedEventIds", FieldValue.arrayRemove(log.eventId)
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Invitation accepted!", Toast.LENGTH_SHORT).show();
                        removeLog(log);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to accept", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDeclineInvite(Object item) {
        if (item instanceof NotificationLog log && userId != null) {
            db.collection("users").document(userId)
                    .update("invitedEventIds", FieldValue.arrayRemove(log.eventId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                        removeLog(log);
                    });
        }
    }

    @Override
    public void onDeleteNotification(Object item) {
        if (item instanceof Notification notif && userId != null) {
            db.collection("users").document(userId)
                    .collection("notifications")
                    .document(notif.getId())
                    .delete();
        }
    }

    @Override
    public void onMarkRead(Object item) {
        if (item instanceof Notification notif && userId != null) {
            db.collection("users").document(userId)
                    .collection("notifications")
                    .document(notif.getId())
                    .update("read", true);
        } else if (item instanceof NotificationLog log) {
            log.isRead = true;
        }
        adapter.notifyDataSetChanged();
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private void filterNewLogs() {
        List<Object> filtered = new ArrayList<>();
        for (Object n : displayedLogs) {
            if (n instanceof Notification && !((Notification) n).isRead()) {
                filtered.add(n);
            }
            if (n instanceof NotificationLog && !((NotificationLog) n).isRead) {
                filtered.add(n);
            }
        }
        adapter.submitList(filtered);
    }

    private void markAllAsRead() {
        for (Object n : displayedLogs) {
            if (n instanceof Notification && userId != null) {
                db.collection("users").document(userId)
                        .collection("notifications")
                        .document(((Notification) n).getId())
                        .update("read", true);
            } else if (n instanceof NotificationLog) {
                ((NotificationLog) n).isRead = true;
            }
        }
        Toast.makeText(this, "All marked as read", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    private void removeLog(Object log) {
        displayedLogs.remove(log);
        adapter.submitList(new ArrayList<>(displayedLogs));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifListener != null) notifListener.remove();
    }
}

package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for displaying and managing user notifications.
 *
 * <p>Features include:</p>
 * <ul>
 * <li>Real-time monitoring of user notifications.</li>
 * <li>Responding to event invitations (Accept/Decline).</li>
 * <li>Following users back directly from notifications.</li>
 * <li>Marking notifications as read or deleting them.</li>
 * </ul>
 */
public class NotificationLogsActivity extends AppCompatActivity {

    private ActivityNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration notifListener;
    private List<Notification> allNotifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (binding.btnBack != null) binding.btnBack.setOnClickListener(v -> finish());
        binding.tvTitle.setText("Notifications");

        setupRecyclerView();

        if (auth.getCurrentUser() != null) {
            setupRealtimeListener();
        } else {
            finish();
        }

        binding.btnMarkAll.setOnClickListener(v -> markAllAsRead());
    }

    /**
     * Initializes the RecyclerView adapter in interactive mode.
     */
    private void setupRecyclerView() {
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationLogsAdapter(new NotificationLogsAdapter.OnItemAction() {
            @Override public void onMarkRead(Notification notif) { markAsRead(notif); }
            @Override public void onOpen(Notification notif) { markAsRead(notif); }
            @Override public void onAcceptInvite(Notification notif) { respondToInvite(notif, "accepted"); }
            @Override public void onDeclineInvite(Notification notif) { respondToInvite(notif, "declined"); }
            @Override public void onDelete(Notification notif) { deleteNotification(notif); }
            @Override public void onFollowBack(Notification notif) { performFollowBack(notif); }
        }, false);

        binding.rvLogs.setAdapter(adapter);
    }

    /**
     * Sets up a real-time listener for the current user's notifications collection.
     */
    private void setupRealtimeListener() {
        String userId = auth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        notifListener = db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (e != null) return;
                    if (snapshots != null) {
                        allNotifications = snapshots.toObjects(Notification.class);
                        for (int i = 0; i < snapshots.size(); i++) {
                            allNotifications.get(i).setId(snapshots.getDocuments().get(i).getId());
                        }
                        adapter.submitList(new ArrayList<>(allNotifications));
                        binding.tvEmpty.setVisibility(allNotifications.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    /**
     * Handles the "Follow Back" action from a new follower notification.
     */
    private void performFollowBack(Notification notif) {
        String targetUserId = notif.getSenderId();
        if (targetUserId == null) {
            Toast.makeText(this, "Cannot follow unknown user", Toast.LENGTH_SHORT).show();
            return;
        }
        String myId = auth.getCurrentUser().getUid();
        WriteBatch batch = db.batch();
        DocumentReference myRef = db.collection("users").document(myId);
        batch.update(myRef, "followingIds", FieldValue.arrayUnion(targetUserId));
        DocumentReference targetRef = db.collection("users").document(targetUserId);
        batch.update(targetRef, "followerIds", FieldValue.arrayUnion(myId));

        batch.commit().addOnSuccessListener(a -> {
            Toast.makeText(this, "You followed them back!", Toast.LENGTH_SHORT).show();
            markAsRead(notif);
        });
    }

    /**
     * Handles accepting or declining an event invitation.
     */
    private void respondToInvite(Notification notif, String status) {
        if (notif.getEventId() == null) return;
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("invitationStatus." + userId, status);

        if ("declined".equals(status)) {
            updates.put("selectedUserIds", FieldValue.arrayRemove(userId));
            updates.put("cancelledUserIds", FieldValue.arrayUnion(userId));
        } else if ("accepted".equals(status)) {
            updates.put("selectedUserIds", FieldValue.arrayRemove(userId));
        }

        db.collection("events").document(notif.getEventId()).update(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Response sent: " + status, Toast.LENGTH_SHORT).show();
                    markAsRead(notif);
                });
    }

    private void deleteNotification(Notification notif) {
        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("notifications").document(notif.getId()).delete();
    }

    private void markAsRead(Notification notif) {
        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("notifications").document(notif.getId()).update("read", true);
    }

    private void markAllAsRead() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("notifications").get()
                .addOnSuccessListener(q -> db.runBatch(batch -> q.forEach(doc -> batch.update(doc.getReference(), "read", true))));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifListener != null) notifListener.remove();
    }
}
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
            Toast.makeText(this, "Please log in.", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnMarkAll.setOnClickListener(v -> markAllAsRead());
    }

    private void setupRecyclerView() {
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationLogsAdapter(new NotificationLogsAdapter.OnItemAction() {
            @Override
            public void onMarkRead(Notification notif) { markAsRead(notif); }

            @Override
            public void onOpen(Notification notif) { markAsRead(notif); }

            @Override
            public void onAcceptInvite(Notification notif) { respondToInvite(notif, "accepted"); }

            @Override
            public void onDeclineInvite(Notification notif) { respondToInvite(notif, "declined"); }

            @Override
            public void onDelete(Notification notif) { deleteNotification(notif); }
        });
        binding.rvLogs.setAdapter(adapter);
    }

    private void setupRealtimeListener() {
        String userId = auth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        notifListener = db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Log.w("NotifActivity", "Listen failed", e);
                        return;
                    }
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

    private void respondToInvite(Notification notif, String status) {
        if (notif.getEventId() == null) return;
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("invitationStatus." + userId, status);

        if ("declined".equals(status)) {
            updates.put("selectedUserIds", FieldValue.arrayRemove(userId));
            updates.put("cancelledUserIds", FieldValue.arrayUnion(userId));
        } else if ("accepted".equals(status)) {
            updates.put("selectedUserIds", FieldValue.arrayRemove(userId)); // Move out of selected
            // You might have an 'acceptedUserIds' field or similar logic in your event model
            // updates.put("acceptedUserIds", FieldValue.arrayUnion(userId));
        }

        db.collection("events").document(notif.getEventId()).update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Response: " + status, Toast.LENGTH_SHORT).show();
                    markAsRead(notif);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send response", Toast.LENGTH_SHORT).show());
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
package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationLogsActivity extends AppCompatActivity implements NotificationLogsAdapter.OnItemAction {

    private static final String TAG = "NotificationActivity";
    private ActivityNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration notificationsListener;
    private String currentUserId;

    private List<NotificationLog> allLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        setupRecycler();
        setupChips();

        if (currentUserId != null) {
            // DEBUG: Show the user ID to ensure it matches 'chosenUserIds' in Firestore
            Toast.makeText(this, "Logged in as: " + currentUserId, Toast.LENGTH_LONG).show();
            startListeningForNotifications();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecycler() {
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationLogsAdapter(this);
        binding.rvLogs.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipAll.setChecked(true);
        binding.chipAll.setOnClickListener(v -> filterList(false));
        binding.chipNew.setOnClickListener(v -> filterList(true));
        binding.btnMarkAll.setOnClickListener(v -> markAllAsRead());
    }

    private void startListeningForNotifications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Listening for notifications for User ID: " + currentUserId);

        // --- CRITICAL FIX ---
        // I have commented out .orderBy("timestamp") temporarily.
        // If this query works now, it means you were missing a Firestore Index.
        // You can add it back later, check Logcat for the "Create Index" link.
        notificationsListener = db.collection("users").document(currentUserId)
                .collection("notifications")
                // .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots != null) {
                        Log.d(TAG, "Loaded " + snapshots.size() + " notifications.");
                        allLogs.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            allLogs.add(mapDocumentToLog(doc));
                        }
                        filterList(binding.chipNew.isChecked());
                    } else {
                        Log.d(TAG, "Snapshot was null");
                    }
                });
    }

    private NotificationLog mapDocumentToLog(DocumentSnapshot doc) {
        String title = doc.getString("message");
        if (title == null) title = doc.getString("title");
        if (title == null) title = "New Notification";

        boolean isRead = Boolean.TRUE.equals(doc.getBoolean("isRead"));
        boolean isInvitation = Boolean.TRUE.equals(doc.getBoolean("isInvitation"));
        String eventId = doc.getString("eventId");

        Date date = doc.getDate("timestamp");
        String timeLabel = (date != null)
                ? new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
                : "Just now";

        return new NotificationLog(
                doc.getId(),
                title,
                timeLabel,
                R.drawable.placeholder_avatar1,
                isRead,
                isInvitation,
                eventId
        );
    }

    private void filterList(boolean showOnlyUnread) {
        List<NotificationLog> filtered = new ArrayList<>();
        if (showOnlyUnread) {
            for (NotificationLog log : allLogs) {
                if (!log.isRead) filtered.add(log);
            }
        } else {
            filtered.addAll(allLogs);
        }

        adapter.submitList(filtered);
        binding.tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAccept(NotificationLog log) {
        if (log.eventId == null) return;
        WriteBatch batch = db.batch();
        batch.update(db.collection("events").document(log.eventId), "enrolledUserIds", FieldValue.arrayUnion(currentUserId));
        batch.update(db.collection("events").document(log.eventId), "chosenUserIds", FieldValue.arrayRemove(currentUserId));
        batch.delete(db.collection("users").document(currentUserId).collection("notifications").document(log.id));

        batch.commit()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Invitation Accepted!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to accept.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDecline(NotificationLog log) {
        if (log.eventId == null) return;
        WriteBatch batch = db.batch();
        batch.update(db.collection("events").document(log.eventId), "chosenUserIds", FieldValue.arrayRemove(currentUserId));
        batch.delete(db.collection("users").document(currentUserId).collection("notifications").document(log.id));

        batch.commit()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Declined.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to decline.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMarkRead(NotificationLog log) {
        db.collection("users").document(currentUserId).collection("notifications").document(log.id).update("isRead", true);
    }

    @Override
    public void onOpen(NotificationLog log) {
        onMarkRead(log);
    }

    private void markAllAsRead() {
        WriteBatch batch = db.batch();
        boolean hasUnread = false;
        for (NotificationLog log : allLogs) {
            if (!log.isRead) {
                batch.update(db.collection("users").document(currentUserId).collection("notifications").document(log.id), "isRead", true);
                hasUnread = true;
            }
        }
        if (hasUnread) {
            batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(this, "All marked as read", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationsListener != null) notificationsListener.remove();
    }
}
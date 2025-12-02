package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin view for inspecting all system notifications.
 * Uses a Collection Group query to fetch notifications across all users.
 */
public class AdminLogsActivity extends AppCompatActivity {

    private static final String TAG = "AdminLogsActivity";
    private ActivityNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private FirebaseFirestore db;
    private List<Notification> allLogs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Admin Header Setup
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }
        binding.tvTitle.setText("System Logs");

        // Hide Entrant-specific controls
        binding.btnMarkAll.setVisibility(View.GONE);
        binding.chipGroup.setVisibility(View.GONE);

        setupRecycler();
        loadGlobalLogs();
    }

    private void setupRecycler() {
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationLogsAdapter(new NotificationLogsAdapter.OnItemAction() {
            @Override
            public void onMarkRead(Notification notif) { /* Read-only for admin */ }

            @Override
            public void onOpen(Notification notif) {
                showLogDetails(notif);
            }

            @Override
            public void onAcceptInvite(Notification notif) { /* Admin cannot act */ }

            @Override
            public void onDeclineInvite(Notification notif) { /* Admin cannot act */ }

            @Override
            public void onDelete(Notification notif) {
                Toast.makeText(AdminLogsActivity.this, "Logs are read-only", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvLogs.setAdapter(adapter);
    }

    private void loadGlobalLogs() {
        if (binding.progressBar != null) binding.progressBar.setVisibility(View.VISIBLE);

        // US 03.08.01: Review logs of ALL notifications.
        db.collectionGroup("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);

                    allLogs.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            n.setId(doc.getId());

                            // Extract Recipient ID from path: users/{USER_ID}/notifications/{DOC_ID}
                            String path = doc.getReference().getPath();
                            String[] segments = path.split("/");
                            String recipientId = (segments.length >= 2) ? segments[1] : "Unknown";

                            // Hack: Prepend "To: UserID" to title for admin visibility
                            // (A better way is to update the model, but this works for logs)
                            String originalTitle = n.getTitle() != null ? n.getTitle() : "Notification";
                            // We don't have a setTitle, so we rely on the dialog to show details

                            // Store the recipient ID in a temporary way or just use for dialog
                            // For list display, we display it as is.
                            allLogs.add(n);
                        }
                    }

                    adapter.submitList(new ArrayList<>(allLogs));

                    if (binding.tvEmpty != null) {
                        binding.tvEmpty.setText("No system logs found.");
                        binding.tvEmpty.setVisibility(allLogs.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading global logs", e);
                    Toast.makeText(this, "Log Load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLogDetails(Notification n) {
        // Since we don't have the parent reference easily here without the list logic above,
        // we'll just show the content.

        StringBuilder details = new StringBuilder();
        details.append("Type: ").append(n.getType()).append("\n\n");
        details.append("Message:\n").append(n.getMessage()).append("\n\n");
        details.append("Event ID: ").append(n.getEventId() != null ? n.getEventId() : "N/A").append("\n");
        details.append("Date: ").append(n.getTimestamp());

        new AlertDialog.Builder(this)
                .setTitle("Log Detail: " + n.getTitle())
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
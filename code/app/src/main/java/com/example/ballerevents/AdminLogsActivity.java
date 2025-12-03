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

        if (binding.btnBack != null) binding.btnBack.setOnClickListener(v -> finish());
        binding.tvTitle.setText("System Logs");
        binding.btnMarkAll.setVisibility(View.GONE);
        binding.chipGroup.setVisibility(View.GONE);

        setupRecycler();
        loadGlobalLogs();
    }

    private void setupRecycler() {
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));

        // --- PASS 'true' FOR ADMIN VIEW (READ ONLY) ---
        adapter = new NotificationLogsAdapter(new NotificationLogsAdapter.OnItemAction() {
            @Override public void onMarkRead(Notification notif) {}
            @Override public void onOpen(Notification notif) { showLogDetails(notif); }
            @Override public void onAcceptInvite(Notification notif) {}
            @Override public void onDeclineInvite(Notification notif) {}
            @Override public void onDelete(Notification notif) {
                Toast.makeText(AdminLogsActivity.this, "Logs are read-only", Toast.LENGTH_SHORT).show();
            }
        }, true); // <--- 'true' means Admin View

        binding.rvLogs.setAdapter(adapter);
    }

    private void loadGlobalLogs() {
        if (binding.progressBar != null) binding.progressBar.setVisibility(View.VISIBLE);

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
                });
    }

    private void showLogDetails(Notification n) {
        StringBuilder details = new StringBuilder();
        details.append("From ID: ").append(n.getSenderId() != null ? n.getSenderId() : "System").append("\n");
        details.append("Type: ").append(n.getType()).append("\n");
        details.append("Event ID: ").append(n.getEventId() != null ? n.getEventId() : "N/A").append("\n\n");
        details.append("Message:\n").append(n.getMessage());

        new AlertDialog.Builder(this)
                .setTitle("Log Details")
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
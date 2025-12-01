package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin view of notification logs.
 * Pulls ALL notifications sent to entrants by organizers using a
 * collection group query on users/*notifications, ordered by time.
 *
         * Note: Uses findViewById() to avoid requiring ViewBinding.
        */
        public class NotificationLogsActivity extends AppCompatActivity implements NotificationLogsAdapter.Callbacks {

            // UI
            private ImageButton btnBack;
            private RecyclerView rvLogs;
            private Chip chipNew, chipAll;
            private MaterialButton btnMarkAll;
            private TextView tvEmpty;

            // Data
            private FirebaseFirestore db;
            private ListenerRegistration listener;
            private NotificationLogsAdapter adapter;

            private final List<NotificationLog> all = new ArrayList<>();
            private final List<NotificationLog> onlyUnread = new ArrayList<>();

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_notification_logs); // layout has these IDs. :contentReference[oaicite:3]{index=3}

                // Init Firestore
                db = FirebaseFirestore.getInstance();

                // Find views
                btnBack = findViewById(R.id.btnBack);
                rvLogs = findViewById(R.id.rvLogs);
                chipNew = findViewById(R.id.chipNew);
                chipAll = findViewById(R.id.chipAll);
                btnMarkAll = findViewById(R.id.btnMarkAll);
                tvEmpty = findViewById(R.id.tvEmpty);

                // Back
                btnBack.setOnClickListener(v -> finish());

                // Recycler
                rvLogs.setLayoutManager(new LinearLayoutManager(this));
                adapter = new NotificationLogsAdapter(this);
                rvLogs.setAdapter(adapter);

                // Chips
                chipAll.setChecked(true);
                chipNew.setOnClickListener(v -> publish());
                chipAll.setOnClickListener(v -> publish());

                // Mark all
                btnMarkAll.setOnClickListener(v -> markAllAsRead());

                startListening();
            }

            private void startListening() {
                stopListening();

                tvEmpty.setVisibility(View.GONE);

                // Read from every user's notifications subcollection
                listener = db.collectionGroup("notifications")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(500) // safety cap
                        .addSnapshotListener((snap, err) -> {
                            if (err != null) {
                                Log.e("NotificationLogs", "listen failed", err);
                                Toast.makeText(this, "Failed to load logs", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (snap == null) return;

                            all.clear();
                            onlyUnread.clear();

                            for (QueryDocumentSnapshot d : snap) {
                                NotificationLog log = d.toObject(NotificationLog.class);

                                // Only logs sent by organizers to entrants (accept legacy field names)
                                String toRole = asString(d, "toRole", "recipientRole", "roleTo");
                                String fromRole = asString(d, "fromRole", "senderRole", "roleFrom");
                                boolean okTo = toRole == null || toRole.equalsIgnoreCase("entrant");
                                boolean okFrom = fromRole == null || fromRole.equalsIgnoreCase("organizer");
                                if (!(okTo && okFrom)) continue;

                                // Backfill missing id/timestamp/title/message defensively
                                if (log.getId() == null) {
                                    log = new NotificationLog(
                                            d.getId(),
                                            log.getTitle(),
                                            log.getMessage(),
                                            d.contains("timestamp") ? d.getTimestamp("timestamp") : Timestamp.now()
                                    );
                                }
                                all.add(log);
                                if (!log.isRead()) onlyUnread.add(log);
                            }
                            publish();
                        });
            }

            private void publish() {
                List<NotificationLog> data = chipNew.isChecked() ? onlyUnread : all;
                adapter.submitList(new ArrayList<>(data));
                tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onOpen(NotificationLog log) {
                // In admin review, "Open" simply marks as read (or could deep-link later)
                onMarkRead(log);
            }

            @Override
            public void onMarkRead(NotificationLog log) {
                // Admin review should NOT affect entrant read state
                db.collectionGroup("notifications")
                        .whereEqualTo(FieldPath.documentId(), log.getId())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(q -> {
                            if (!q.isEmpty()) {
                                q.getDocuments().get(0).getReference().update("adminReviewed", true);
                            }
                        });
            }


            private void markAllAsRead() {
                db.collectionGroup("notifications")
                        .whereEqualTo("read", false)
                        .limit(500)
                        .get()
                        .addOnSuccessListener(q -> db.runBatch(b -> q.forEach(doc -> b.update(doc.getReference(), "read", true))))
                        .addOnSuccessListener(unused -> Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show());
            }

            private void markAllAsReviewed() {
                db.collectionGroup("notifications")
                        .whereEqualTo("adminReviewed", null)
                        .limit(500)
                        .get()
                        .addOnSuccessListener(q ->
                                db.runBatch(b -> q.forEach(doc ->
                                        b.update(doc.getReference(), "adminReviewed", true)
                                ))
                        )
                        .addOnSuccessListener(unused ->
                                Toast.makeText(this, "All notifications marked as reviewed", Toast.LENGTH_SHORT).show()
                        );
            }


            private static String asString(QueryDocumentSnapshot d, String... keys) {
                for (String k : keys) {
                    Object v = d.get(k);
                    if (v instanceof String) return (String) v;
                }
                return null;
            }

            private void stopListening() {
                if (listener != null) {
                    listener.remove();
                    listener = null;
                }
            }

            @Override
            protected void onDestroy() {
                super.onDestroy();
                stopListening();
            }
        }

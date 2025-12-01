package com.example.ballerevents;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class NotificationLogsActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationActionListener {

    private RecyclerView rvLogs;
    private Chip chipNew, chipAll;
    private MaterialButton btnMarkAll;
    private NotificationAdapter adapter;

    private final List<NotificationItem> allNotifications = new ArrayList<>();

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_logs);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        initViews();
        setupRecycler();
        setupChips();
        loadNotifications();
    }

    // ------------------------------------------------------------------
    // INITIALIZATION
    // ------------------------------------------------------------------

    private void initViews() {
        rvLogs = findViewById(R.id.rvLogs);
        chipNew = findViewById(R.id.chipNew);
        chipAll = findViewById(R.id.chipAll);
        btnMarkAll = findViewById(R.id.btnMarkAll);

        // Back button from your XML
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        adapter = new NotificationAdapter(this);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        rvLogs.setAdapter(adapter);
    }

    private void setupChips() {
        chipAll.setChecked(true);

        // Show all notifications
        chipAll.setOnClickListener(v ->
                adapter.submitList(new ArrayList<>(allNotifications)));

        // Show unread only
        chipNew.setOnClickListener(v -> {
            List<NotificationItem> unread = new ArrayList<>();
            for (NotificationItem item : allNotifications) {
                if (!item.isRead) unread.add(item);
            }
            adapter.submitList(unread);
        });

        // Mark all as read
        btnMarkAll.setOnClickListener(v -> markAllAsRead());
    }

    // ------------------------------------------------------------------
    // LOAD FIRESTORE NOTIFICATIONS
    // ------------------------------------------------------------------

    private void loadNotifications() {
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(snap -> {
                    allNotifications.clear();

                    for (QueryDocumentSnapshot doc : snap) {
                        Notification notif = doc.toObject(Notification.class);

                        NotificationItem item = new NotificationItem(
                                notif.getId(),
                                R.drawable.ic_notification_alert,  // default avatar
                                notif.getTitle(),
                                notif.getMessage(),
                                notif.getTimestamp() == null
                                        ? "–"
                                        : timeFormat.format(notif.getTimestamp()),
                                true,                       // hasActions
                                notif.isRead(),
                                "invitation".equalsIgnoreCase(notif.getType()),
                                notif.getEventId()
                        );

                        allNotifications.add(item);
                    }

                    adapter.submitList(new ArrayList<>(allNotifications));
                });
    }

    // ------------------------------------------------------------------
    // MARK ALL AS READ
    // ------------------------------------------------------------------

    private void markAllAsRead() {
        for (NotificationItem item : allNotifications) {
            item.isRead = true;
        }

        adapter.submitList(new ArrayList<>(allNotifications));

        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        doc.getReference().update("read", true);
                    }
                });
    }

    // ------------------------------------------------------------------
    // ACTION BUTTONS: Accept / Reject / Mark Read
    // ------------------------------------------------------------------

    @Override
    public void onAccept(@NonNull NotificationItem item) {
        markOneAsRead(item);

        // TODO: Update Event.invitationStatus here
        // Example:
        // db.collection("events").document(item.eventId)
        //     .update("invitationStatus." + uid, "accepted");
    }

    @Override
    public void onReject(@NonNull NotificationItem item) {
        markOneAsRead(item);

        // TODO: Update Event.invitationStatus here
        // Example:
        // db.collection("events").document(item.eventId)
        //     .update("invitationStatus." + uid, "declined");
    }

    @Override
    public void onMarkRead(@NonNull NotificationItem item) {
        markOneAsRead(item);
    }

    private void markOneAsRead(NotificationItem item) {
        item.isRead = true;
        adapter.notifyDataSetChanged();

        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .document(item.id)
                .update("read", true);
    }
}
